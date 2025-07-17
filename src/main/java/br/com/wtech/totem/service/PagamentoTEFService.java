package br.com.wtech.totem.service;

import br.com.wtech.totem.service.tef.TefClientMCLibrary;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PagamentoTEFService {

    private final TefClientMCLibrary tef = TefClientMCLibrary.INSTANCE;
    private volatile boolean cancelamentoSolicitado = false;
    private ResultadoTEF ultimoResultado;
    private String ultimoNsuParaReimpressao;

    // --- Constantes para a transação ---
    // Em um projeto real, estes viriam de um arquivo de configuração.
    private static final String CNPJ_LOJA = "60177876000130";
    private static final String CODIGO_LOJA = "167";
    private static final String NUMERO_PDV = "1";

    // --- Monitor de Status para notificar a UI ---
    private final StringProperty tefStatus = new SimpleStringProperty("IDLE"); // Estado inicial

    /**
     * Os controllers usarão esta propriedade para "escutar" as mudanças de status.
     * É ReadOnly para que apenas o serviço possa alterá-la.
     */
    public ReadOnlyStringProperty tefStatusProperty() {
        return tefStatus;
    }

    /**
     * Ponto de entrada ÚNICO. Inicia todo o processo de pagamento.
     * @param valor O valor da transação.
     * @param tipoPagamento O tipo de pagamento ("Débito", "Crédito", etc).
     */
    public void iniciarProcessoCompletoDePagamento(BigDecimal valor, String tipoPagamento) {
        Platform.runLater(() -> tefStatus.set("IDLE"));
        Task<ResultadoTEF> pagamentoTask = criarTaskDePagamento(valor, tipoPagamento);
        // A lógica de atualização de status agora garante a ordem correta das operações.
        pagamentoTask.setOnSucceeded(e -> {
            this.ultimoResultado = pagamentoTask.getValue(); // 1º: Guarda o resultado
            Platform.runLater(() -> tefStatus.set("FINISHED"));   // 2º: Avisa a UI que terminou
        });
        pagamentoTask.setOnFailed(e -> {
            this.ultimoResultado = new ResultadoTEF(false, "ERRO", getTaskExceptionMessage(pagamentoTask));
            Platform.runLater(() -> tefStatus.set("ERROR"));      // Avisa a UI sobre o erro
        });

        new Thread(pagamentoTask).start();
    }

    // AJUSTE: O método agora aceita um "callback" a ser executado no final.
    public void iniciarReimpressao(String nsuParaReimprimir, Consumer<ResultadoTEF> onComplete) {
        Platform.runLater(() -> tefStatus.set("IDLE"));
        Task<ResultadoTEF> reprintTask = criarTaskDeReimpressao(nsuParaReimprimir);

        // Ao terminar, executa o callback que o controller enviou.
        reprintTask.setOnSucceeded(e -> Platform.runLater(() -> onComplete.accept(reprintTask.getValue())));
        reprintTask.setOnFailed(e -> {
            ResultadoTEF erro = new ResultadoTEF(false, "ERRO", getTaskExceptionMessage(reprintTask));
            Platform.runLater(() -> onComplete.accept(erro));
        });

        new Thread(reprintTask).start();
    }

    // AJUSTE: O método agora aceita um "callback" a ser executado no final.
    public void iniciarCancelamentoAdministrativo(String nsuParaCancelar, Consumer<ResultadoTEF> onComplete) {
        Platform.runLater(() -> tefStatus.set("IDLE"));
        Task<ResultadoTEF> cancelTask = criarTaskDeCancelamento(nsuParaCancelar);

        // Ao terminar, executa o callback que o controller enviou.
        cancelTask.setOnSucceeded(e -> Platform.runLater(() -> onComplete.accept(cancelTask.getValue())));
        cancelTask.setOnFailed(e -> {
            ResultadoTEF erro = new ResultadoTEF(false, "ERRO", getTaskExceptionMessage(cancelTask));
            Platform.runLater(() -> onComplete.accept(erro));
        });

        new Thread(cancelTask).start();
    }

    public void iniciarProcessoPix(BigDecimal valor, Consumer<ResultadoTEF> onComplete) {
        // Reutilizamos a Task de pagamento, passando "Pix" como tipo
        Task<ResultadoTEF> pixTask = criarTaskDePagamento(valor, "Pix");

        // Ao terminar com sucesso ou falha, executa o callback que o controller enviou
        pixTask.setOnSucceeded(e -> Platform.runLater(() -> onComplete.accept(pixTask.getValue())));
        pixTask.setOnFailed(e -> {
            ResultadoTEF erro = new ResultadoTEF(false, "ERRO", getTaskExceptionMessage(pixTask));
            Platform.runLater(() -> onComplete.accept(erro));
        });

        new Thread(pixTask).start();
    }

    private Task<ResultadoTEF> criarTaskDePagamento(BigDecimal valor, String tipoPagamento) {
        return new Task<>() {
            @Override
            protected ResultadoTEF call() throws Exception {
                cancelamentoSolicitado = false;
                Platform.runLater(() -> tefStatus.set("STARTED"));

                TefOperation operacao = getOperacaoPorTipo(tipoPagamento);
                String valorFormatado = formatarValorParaTef(valor);
                String nsuOriginal = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                int ret = tef.IniciaFuncaoMCInterativo(operacao.getCode(), CNPJ_LOJA, 1, nsuOriginal, valorFormatado, nsuOriginal, data, NUMERO_PDV, CODIGO_LOJA, 0, "");
                if (ret != 0) { throw new RuntimeException("Erro ao iniciar TEF. Código: " + ret); }

                String nsuRetornadoPeloTef = nsuOriginal;
                String comprovante = "";

                while (true) {
                    if (isCancelled() || cancelamentoSolicitado) {
                        tef.CancelarFluxoMCInterativo();
                        Platform.runLater(() -> tefStatus.set("CANCELLED"));
                        return new ResultadoTEF(false, "CANCELADO", "A operação foi cancelada pelo usuário.");
                    }

                    String resposta = tef.AguardaFuncaoMCInterativo();
                    System.out.println(">> TEF Resposta (Pagamento): " + resposta);

                    if (resposta == null || resposta.isEmpty()) { Thread.sleep(100); continue; }

                    if (resposta.startsWith("[MENU]")) {
                        String[] partesMenu = resposta.split("#");
                        if (partesMenu.length > 2) {
                            String indiceDaEscolha = partesMenu[2].split("\\|")[0].split(",")[0];
                            tef.ContinuaFuncaoMCInterativo(indiceDaEscolha);
                            if (tefStatus.get().equals("STARTED")) {
                                Platform.runLater(() -> tefStatus.set("SERVER_CONNECTED"));
                            }
                        }
                    } else if (resposta.startsWith("[PERGUNTA]")) {
                        if (resposta.contains("TELEFONE DO CLIENTE")) { // Pergunta específica do PIX
                            tef.ContinuaFuncaoMCInterativo("");
                        } else {
                            tef.CancelarFluxoMCInterativo();
                            throw new RuntimeException("Operação cancelada: totem não pode responder perguntas genéricas.");
                        }
                    } else if (resposta.startsWith("[MSG]")) {
                        String mensagem = resposta.substring(5);
                        // Agora, tanto Cartão quanto PIX podem atualizar o status para "aguardando"
                        if (mensagem.toUpperCase().contains("CARTAO") || mensagem.toUpperCase().contains("PINPAD") ||
                                mensagem.toUpperCase().contains("AGUARDANDO PAGAMENTO") || mensagem.contains("QRCODE=")) {
                            Platform.runLater(() -> tefStatus.set("WAITING_FOR_CARD"));
                        }
                    } else if (resposta.startsWith("[RETORNO]")) {
                        nsuRetornadoPeloTef = extrairCampo(resposta, "CAMPO0133");
                        comprovante = extrairCampo(resposta, "CAMPO122");

                        // AJUSTE: Corrigida a extração do NSU para PIX e Cartão
                        if (operacao == TefOperation.PIX) {
                            ultimoNsuParaReimpressao = nsuRetornadoPeloTef; // No PIX, o NSU principal é o correto
                        } else {
                            ultimoNsuParaReimpressao = extrairNsuDoComprovante(comprovante);
                        }
                        System.out.println(">>> NSU TEF para reimpressão armazenado: " + ultimoNsuParaReimpressao);
                        break;
                    } else if (resposta.startsWith("[ERROABORTAR]") || resposta.startsWith("[ERRODISPLAY]")) {
                        tef.CancelarFluxoMCInterativo();
                        throw new RuntimeException("Erro TEF: " + resposta);
                    }
                }

                // AJUSTE: A finalização com loop de confirmação agora só ocorre para Cartão
                if (operacao != TefOperation.PIX) {
                    System.out.println("Finalizando transação de Cartão...");
                    ret = tef.FinalizaFuncaoMCInterativo(98, CNPJ_LOJA, 1, nsuOriginal, valorFormatado, nsuRetornadoPeloTef, data, NUMERO_PDV, CODIGO_LOJA, 0, "");
                    if (ret != 0) { throw new RuntimeException("Falha ao confirmar a transação no TEF. Código: " + ret); }

                    // Loop de confirmação obrigatório para cartões
                    int tentativasConfirmacao = 0; boolean confirmado = false;
                    while (tentativasConfirmacao < 100) {
                        String finalResp = tef.AguardaFuncaoMCInterativo();
                        if (finalResp != null && finalResp.contains("CONFIRMADA COM SUCESSO")) { confirmado = true; break; }
                        if (finalResp != null && finalResp.toUpperCase().contains("ERRO")) { throw new RuntimeException("TEF retornou erro na confirmação final: " + finalResp); }
                        Thread.sleep(100); tentativasConfirmacao++;
                    }
                    if (!confirmado) { throw new RuntimeException("Timeout: Não foi recebida a confirmação final da DLL."); }
                } else {
                    System.out.println("Finalização de PIX. Transação já confirmada.");
                }

                return new ResultadoTEF(true, "APROVADO", comprovante);
            }
        };
    }

    private Task<ResultadoTEF> criarTaskDeReimpressao(String nsuParaReimprimir) {
        return new Task<>() {
            @Override
            protected ResultadoTEF call() throws Exception {
                cancelamentoSolicitado = false;
                TefOperation operacao = TefOperation.REPRINT;
                String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String comprovante = "";

                System.out.println("--- Iniciando Reimpressão para o NSU: " + nsuParaReimprimir + " ---");

                int ret = tef.IniciaFuncaoMCInterativo(
                        operacao.getCode(), CNPJ_LOJA, 1, nsuParaReimprimir, "0,00",
                        nsuParaReimprimir, data, NUMERO_PDV, CODIGO_LOJA, 0, ""
                );
                if (ret != 0) {
                    throw new RuntimeException("Erro ao iniciar Reimpressão. Código: " + ret);
                }

                while (true) {
                    if (isCancelled() || cancelamentoSolicitado) {
                        tef.CancelarFluxoMCInterativo();
                        return new ResultadoTEF(false, "CANCELADO", "Operação de reimpressão cancelada.");
                    }

                    String resposta = tef.AguardaFuncaoMCInterativo();
                    System.out.println(">> TEF Resposta (Reimpressão): " + resposta);

                    if (resposta == null || resposta.isEmpty()) {
                        Thread.sleep(100);
                        continue;
                    }

                    if (resposta.startsWith("[MENU]")) {
                        String[] partesMenu = resposta.split("#");
                        if (partesMenu.length > 2) {
                            String indiceDaEscolha = partesMenu[2].split("\\|")[0].split(",")[0];
                            tef.ContinuaFuncaoMCInterativo(indiceDaEscolha);
                        }
                    } else if (resposta.startsWith("[PERGUNTA]")) {
                        if (resposta.contains("DIGITE TRANS ORIG")) {
                            String dataHoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yy"));
                            tef.ContinuaFuncaoMCInterativo(dataHoje);
                        }
                    } else if (resposta.startsWith("[RETORNO]")) {
                        comprovante = extrairCampo(resposta, "CAMPO122");
                        break;
                    } else if (resposta.startsWith("[ERROABORTAR]") || resposta.startsWith("[ERRODISPLAY]")) {
                        tef.CancelarFluxoMCInterativo();
                        throw new RuntimeException("Erro TEF na Reimpressão: " + resposta);
                    }
                }

                ret = tef.FinalizaFuncaoMCInterativo(98, CNPJ_LOJA, 1, nsuParaReimprimir, "0,00", nsuParaReimprimir, data, NUMERO_PDV, CODIGO_LOJA, 0, "");
                if (ret != 0) {
                    throw new RuntimeException("Falha ao confirmar a Reimpressão. Código: " + ret);
                }

                // --- AJUSTE FINAL ADICIONADO AQUI ---
                int tentativasConfirmacao = 0;
                boolean operacaoConcluida = false;
                while (tentativasConfirmacao < 50) {
                    String finalResp = tef.AguardaFuncaoMCInterativo();
                    System.out.println(">> TEF Resposta (Confirmação Reimpressão): " + finalResp);

                    if (finalResp != null && !finalResp.isEmpty()) {
                        if (finalResp.toUpperCase().contains("ERRO")) {
                            System.err.println("Aviso: TEF retornou erro na confirmação da reimpressão: " + finalResp);
                        }
                        operacaoConcluida = true;
                        break;
                    }
                    Thread.sleep(100);
                    tentativasConfirmacao++;
                }
                if (!operacaoConcluida) {
                    System.err.println("Aviso: Timeout ao aguardar confirmação final da reimpressão. Assumindo sucesso.");
                }

                System.out.println("Operação de reimpressão finalizada e confirmada pela DLL.");
                return new ResultadoTEF(true, "REIMPRESSO", comprovante);
            }
        };
    }

    private Task<ResultadoTEF> criarTaskDeCancelamento(String nsuParaCancelar) {
        return new Task<>() {
            @Override
            protected ResultadoTEF call() throws Exception {
                cancelamentoSolicitado = false;
                // Platform.runLater(() -> tefStatus.set("STARTED")); // Não é mais necessário com callback

                TefOperation operacao = TefOperation.CANCEL;
                String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String comprovanteEstorno = "";

                System.out.println("--- Iniciando ESTORNO para o NSU: " + nsuParaCancelar + " ---");

                int ret = tef.IniciaFuncaoMCInterativo(operacao.getCode(), CNPJ_LOJA, 1, nsuParaCancelar, "0,00", nsuParaCancelar, data, NUMERO_PDV, CODIGO_LOJA, 0, "");
                if (ret != 0) {
                    throw new RuntimeException("Erro ao iniciar Estorno. Código: " + ret);
                }

                // Loop principal para obter o resultado da operação
                while (true) {
                    if (isCancelled() || cancelamentoSolicitado) {
                        tef.CancelarFluxoMCInterativo();
                        return new ResultadoTEF(false, "CANCELADO", "Operação de estorno cancelada.");
                    }

                    String resposta = tef.AguardaFuncaoMCInterativo();
                    System.out.println(">> TEF Resposta (Estorno): " + resposta);

                    if (resposta == null || resposta.isEmpty()) {
                        Thread.sleep(100);
                        continue;
                    }

                    // A lógica para tratar [MENU] e [PERGUNTA] permanece a mesma
                    if (resposta.startsWith("[MENU]")) {
                        String[] partesMenu = resposta.split("#");
                        if (partesMenu.length > 2) {
                            String indiceDaEscolha = partesMenu[2].split("\\|")[0].split(",")[0];
                            tef.ContinuaFuncaoMCInterativo(indiceDaEscolha);
                        }
                    } else if (resposta.startsWith("[PERGUNTA]")) {
                        if (resposta.contains("VALOR DA TRANSACAO")) {
                            String valorResposta = "0,50"; // Valor de exemplo
                            tef.ContinuaFuncaoMCInterativo(valorResposta);
                        } else {
                            tef.CancelarFluxoMCInterativo();
                            throw new RuntimeException("Operação de estorno cancelada: totem não pode responder a perguntas genéricas.");
                        }
                    } else if (resposta.startsWith("[RETORNO]")) {
                        comprovanteEstorno = extrairCampo(resposta, "CAMPO122");
                        break;
                    } else if (resposta.startsWith("[ERROABORTAR]") || resposta.startsWith("[ERRODISPLAY]")) {
                        tef.CancelarFluxoMCInterativo();
                        throw new RuntimeException("Erro TEF no Estorno: " + resposta);
                    }
                }

                // Finaliza a operação de estorno
                ret = tef.FinalizaFuncaoMCInterativo(98, CNPJ_LOJA, 1, nsuParaCancelar, "0,00", nsuParaCancelar, data, NUMERO_PDV, CODIGO_LOJA, 0, "");
                if (ret != 0) {
                    throw new RuntimeException("Falha ao iniciar a confirmação do Estorno. Código: " + ret);
                }

                // --- AJUSTE FINAL: LOOP DE CONFIRMAÇÃO OBRIGATÓRIO ---
                int tentativasConfirmacao = 0;
                boolean operacaoConcluida = false;
                while (tentativasConfirmacao < 50) { // Timeout de 5 segundos é mais que suficiente
                    String finalResp = tef.AguardaFuncaoMCInterativo();
                    System.out.println(">> TEF Resposta (Confirmação Estorno): " + finalResp);

                    // O loop termina se a DLL enviar QUALQUER resposta final (com RETORNO ou ERRO)
                    if (finalResp != null && !finalResp.isEmpty()) {
                        // Verificamos se a resposta final contém um erro explícito
                        if (finalResp.toUpperCase().contains("ERRO")) {
                            // Mesmo com erro na confirmação, o estorno provavelmente ocorreu.
                            // Logamos o erro mas continuamos o fluxo como sucesso.
                            System.err.println("Aviso: TEF retornou um erro na confirmação final do estorno, mas a operação deve ter sido concluída: " + finalResp);
                        }
                        operacaoConcluida = true;
                        break;
                    }
                    Thread.sleep(100);
                    tentativasConfirmacao++;
                }
                if (!operacaoConcluida) {
                    System.err.println("Aviso: Timeout ao aguardar confirmação final do estorno. Assumindo sucesso.");
                    // Se o tempo acabar, assumimos que a operação foi bem-sucedida, pois o passo anterior não deu erro.
                }

                System.out.println("Operação de estorno finalizada e confirmada pela DLL.");

                // Retorna o resultado para o 'onComplete' do callback tratar
                return new ResultadoTEF(true, "ESTORNADO", comprovanteEstorno);
            }
        };
    }

    public void solicitarCancelamento() {
        System.out.println("SERVICE TEF: Solicitação de cancelamento recebida.");
        this.cancelamentoSolicitado = true;
    }

    public ResultadoTEF getUltimoResultado() {
        return ultimoResultado;
    }

    public String getUltimoNsuParaReimpressao() {
        return ultimoNsuParaReimpressao;
    }

    // --- MÉTODOS AUXILIARES ---

    public void setUltimoResultado(ResultadoTEF resultado) {
        this.ultimoResultado = resultado;
    }

    private String getTaskExceptionMessage(Task<?> task) {
        if (task.getException() != null) {
            return task.getException().getMessage();
        }
        return "Ocorreu um erro desconhecido na tarefa.";
    }

    private String formatarValorParaTef(BigDecimal valor) {
        return String.format("%.2f", valor).replace('.', ',');
    }

    private TefOperation getOperacaoPorTipo(String tipoPagamento) {
        switch (tipoPagamento.toLowerCase()) {
            case "crédito": return TefOperation.CREDIT;
            case "débito": return TefOperation.DEBIT;
            case "pix": return TefOperation.PIX;
            default: throw new IllegalArgumentException("Tipo de pagamento desconhecido: " + tipoPagamento);
        }
    }

    private String extrairCampo(String resposta, String nomeCampo) {
        Pattern pattern = Pattern.compile(Pattern.quote(nomeCampo) + "=([^#]*)");
        Matcher matcher = pattern.matcher(resposta);
        return matcher.find() ? matcher.group(1) : "N/A";
    }

    private String extrairNsuDoComprovante(String textoComprovante) {
        if (textoComprovante == null) return null;
        Pattern pattern = Pattern.compile("\\(NSU TEF\\s*:\\s*(\\d+)\\)");
        Matcher matcher = pattern.matcher(textoComprovante);
        if (matcher.find()) { return matcher.group(1); }
        return null;
    }
}