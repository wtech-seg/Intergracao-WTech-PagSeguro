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

        // --- AJUSTE APLICADO AQUI ---
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

    public void iniciarReimpressao(String nsuParaReimprimir) {
        Platform.runLater(() -> tefStatus.set("IDLE"));
        Task<ResultadoTEF> reprintTask = criarTaskDeReimpressao(nsuParaReimprimir);
        reprintTask.setOnSucceeded(e -> this.ultimoResultado = reprintTask.getValue());
        reprintTask.setOnFailed(e -> this.ultimoResultado = new ResultadoTEF(false, "ERRO", getTaskExceptionMessage(reprintTask)));
        new Thread(reprintTask).start();
    }

    public void iniciarCancelamentoAdministrativo(String nsuParaCancelar) {
        Platform.runLater(() -> tefStatus.set("IDLE"));
        Task<ResultadoTEF> cancelTask = criarTaskDeCancelamento(nsuParaCancelar);
        cancelTask.setOnSucceeded(e -> this.ultimoResultado = cancelTask.getValue());
        cancelTask.setOnFailed(e -> this.ultimoResultado = new ResultadoTEF(false, "ERRO", getTaskExceptionMessage(cancelTask)));
        new Thread(cancelTask).start();
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

                // --- 1. INICIA A FUNÇÃO ---
                int ret = tef.IniciaFuncaoMCInterativo(operacao.getCode(), CNPJ_LOJA, 1, nsuOriginal, valorFormatado, nsuOriginal, data, NUMERO_PDV, CODIGO_LOJA, 0, "");
                if (ret != 0) {
                    throw new RuntimeException("Erro ao iniciar TEF. Código: " + ret);
                }

                String nsuRetornadoPeloTef = nsuOriginal;
                String comprovante = "";

                // --- 2. LOOP DE COMUNICAÇÃO ---
                while (true) {
                    if (isCancelled() || cancelamentoSolicitado) {
                        tef.CancelarFluxoMCInterativo();
                        Platform.runLater(() -> tefStatus.set("CANCELLED"));
                        return new ResultadoTEF(false, "CANCELADO", "A operação foi cancelada pelo usuário.");
                    }

                    String resposta = tef.AguardaFuncaoMCInterativo();

                    System.out.println(">> TEF Resposta (Pagamento): " + resposta);

                    if (resposta == null || resposta.isEmpty()) {
                        Thread.sleep(100);
                        continue;
                    }

                    if (resposta.startsWith("[MENU]")) {
                        System.out.println("TEF MENU: " + resposta);
                        String[] partesMenu = resposta.split("#");
                        if (partesMenu.length > 2) {
                            String[] opcoes = partesMenu[2].split("\\|");
                            if (opcoes.length > 0) {
                                String indiceDaEscolha = opcoes[0].split(",")[0];
                                System.out.println("Selecionando automaticamente a primeira opção do menu: " + indiceDaEscolha);
                                tef.ContinuaFuncaoMCInterativo(indiceDaEscolha);

                                if (tefStatus.get().equals("STARTED")) {
                                    Platform.runLater(() -> tefStatus.set("SERVER_CONNECTED"));
                                }
                            }
                        }
                    } else if (resposta.startsWith("[MSG]")) {
                        String mensagem = resposta.substring(5);
                        //System.out.println("TEF MSG: " + mensagem);
                        if (mensagem.toUpperCase().contains("CARTAO") || mensagem.toUpperCase().contains("PINPAD")) {
                            Platform.runLater(() -> tefStatus.set("WAITING_FOR_CARD"));
                        }
                    } else if (resposta.startsWith("[PERGUNTA]")) {
                        tef.CancelarFluxoMCInterativo();
                        throw new RuntimeException("Operação cancelada: totem não pode responder perguntas.");
                    } else if (resposta.startsWith("[ERROABORTAR]") || resposta.startsWith("[ERRODISPLAY]")) {
                        tef.CancelarFluxoMCInterativo();
                        throw new RuntimeException("Erro TEF: " + resposta);
                    } else if (resposta.startsWith("[RETORNO]")) {
                        Platform.runLater(() -> tefStatus.set("TRANSACTION_DATA_RECEIVED"));
                        nsuRetornadoPeloTef = extrairCampo(resposta, "CAMP00133");
                        comprovante = extrairCampo(resposta, "CAMPO122");
                        ultimoNsuParaReimpressao = extrairNsuDoComprovante(comprovante);
                        System.out.println(">>> NSU TEF para reimpressão armazenado: " + ultimoNsuParaReimpressao);
                        break;
                    }
                }

                // --- 3. FINALIZA A TRANSAÇÃO ---
                ret = tef.FinalizaFuncaoMCInterativo(98, CNPJ_LOJA, 1, nsuOriginal, valorFormatado, nsuRetornadoPeloTef, data, NUMERO_PDV, CODIGO_LOJA, 0, "");

                System.out.println(comprovante);
                System.out.println("RETORNO DA FINALIZAÇÃO: " + ret);

                if (ret != 0) {
                    throw new RuntimeException("Falha ao confirmar a transação no TEF. Código: " + ret);
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
                Platform.runLater(() -> tefStatus.set("STARTED"));

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
                        Platform.runLater(() -> tefStatus.set("CANCELLED"));
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
                            String[] opcoes = partesMenu[2].split("\\|");
                            if (opcoes.length > 0) {
                                String indiceDaEscolha = opcoes[0].split(",")[0];
                                System.out.println("   -> Selecionando automaticamente a primeira opção do menu: " + indiceDaEscolha);
                                tef.ContinuaFuncaoMCInterativo(indiceDaEscolha);
                            }
                        }
                    } else if (resposta.startsWith("[PERGUNTA]")) { // --- AJUSTE ADICIONADO AQUI ---
                        System.out.println("TEF PERGUNTA (Reimpressão): " + resposta);
                        // Verifica se a pergunta é sobre a data da transação original
                        if (resposta.contains("DIGITE TRANS ORIG")) {
                            // Formata a data de HOJE como DDMMAA (somente números)
                            String dataHoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yy"));
                            System.out.println("   -> Respondendo à pergunta com a data de hoje: " + dataHoje);
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

                Platform.runLater(() -> tefStatus.set("FINISHED"));
                return new ResultadoTEF(true, "REIMPRESSO", comprovante);
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> tefStatus.set("ERROR"));
            }
        };
    }

    private Task<ResultadoTEF> criarTaskDeCancelamento(String nsuParaCancelar) {
        return new Task<>() {
            @Override
            protected ResultadoTEF call() throws Exception {
                cancelamentoSolicitado = false;
                Platform.runLater(() -> tefStatus.set("STARTED"));

                TefOperation operacao = TefOperation.CANCEL;
                String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String comprovanteEstorno = "";

                System.out.println("--- Iniciando ESTORNO para o NSU: " + nsuParaCancelar + " ---");

                int ret = tef.IniciaFuncaoMCInterativo(operacao.getCode(), CNPJ_LOJA, 1, nsuParaCancelar, "0,00", nsuParaCancelar, data, NUMERO_PDV, CODIGO_LOJA, 0, "");
                if (ret != 0) {
                    throw new RuntimeException("Erro ao iniciar Estorno. Código: " + ret);
                }

                while (true) {
                    if (isCancelled() || cancelamentoSolicitado) {
                        tef.CancelarFluxoMCInterativo();
                        Platform.runLater(() -> tefStatus.set("CANCELLED"));
                        return new ResultadoTEF(false, "CANCELADO", "Operação de estorno cancelada.");
                    }

                    String resposta = tef.AguardaFuncaoMCInterativo();

                    System.out.println(">> TEF Resposta (Estorno): " + resposta);

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
                    } else if (resposta.startsWith("[PERGUNTA]")) { // --- AJUSTE ADICIONADO AQUI ---
                        System.out.println("TEF PERGUNTA (Estorno): " + resposta);
                        // Verifica se a pergunta é sobre o VALOR da transação
                        if (resposta.contains("VALOR DA TRANSACAO")) {
                            String valorResposta = "0,50";
                            System.out.println("   -> Respondendo à pergunta com o valor: " + valorResposta);
                            tef.ContinuaFuncaoMCInterativo(valorResposta);
                        }
                    } else if (resposta.startsWith("[RETORNO]")) {
                        comprovanteEstorno = extrairCampo(resposta, "CAMPO122");
                        break;
                    } else if (resposta.startsWith("[ERROABORTAR]") || resposta.startsWith("[ERRODISPLAY]")) {
                        tef.CancelarFluxoMCInterativo();
                        throw new RuntimeException("Erro TEF no Estorno: " + resposta);
                    }
                }

                ret = tef.FinalizaFuncaoMCInterativo(98, CNPJ_LOJA, 1, nsuParaCancelar, "0,00", nsuParaCancelar, data, NUMERO_PDV, CODIGO_LOJA, 0, "");
                if (ret != 0) {
                    throw new RuntimeException("Falha ao confirmar o Estorno. Código: " + ret);
                }

                Platform.runLater(() -> tefStatus.set("FINISHED"));
                return new ResultadoTEF(true, "ESTORNADO", comprovanteEstorno);
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> tefStatus.set("ERROR"));
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
            //case "pix": return TefOperation.PIX;
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