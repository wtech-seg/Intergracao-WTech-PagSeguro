package br.com.wtech.totem.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PagamentoTEFService {

    private ResultadoTEF ultimoResultado;

    // Variáveis para controlar os testes e o cancelamento
    private boolean forcarRecusaParaTeste = false;
    private volatile boolean cancelamentoSolicitado = false;

    /**
     * Método de controle para testes.
     */
    public void simularProximoComoRecusado(boolean deveRecusar) {
        System.out.println("--- MODO DE TESTE: Próximo pagamento será " + (deveRecusar ? "RECUSADO." : "APROVADO.") + " ---");
        this.forcarRecusaParaTeste = deveRecusar;
    }

    /**
     * Método para os controllers solicitarem o cancelamento da operação.
     */
    public void solicitarCancelamento() {
        System.out.println("SERVICE TEF: Solicitação de cancelamento recebida.");
        this.cancelamentoSolicitado = true;
    }

    /**
     * Inicia a transação na maquininha, agora com suporte a cancelamento e simulação de recusa.
     */
    public ResultadoTEF iniciarPagamento(BigDecimal valor, String tipoPagamento) {
        System.out.println("SERVICE TEF: Iniciando transação de " + tipoPagamento + " no valor de " + valor);
        this.cancelamentoSolicitado = false; // Reseta o cancelamento no início

        try {
            // Espera em pequenos intervalos para checar se o cancelamento foi solicitado
            for (int i = 0; i < 10; i++) {
                if (cancelamentoSolicitado) {
                    System.out.println("TEF: Cancelamento detectado. Interrompendo transação.");
                    return new ResultadoTEF(false, "CANCELADO PELO USUÁRIO", null);
                }
                Thread.sleep(500); // Pausa de meio segundo
            }
            System.out.println("TEF: Interação na maquininha concluída.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("TEF: A transação foi interrompida.");
            return new ResultadoTEF(false, "Transação Interrompida", null);
        }

        // Verifica se o teste de recusa está ativo
        if (this.forcarRecusaParaTeste) {
            System.out.println("TEF: Simulação forçada de pagamento RECUSADO.");
            this.forcarRecusaParaTeste = false; // Reseta para o próximo pagamento ser normal
            ResultadoTEF resultadoRecusado = new ResultadoTEF(false, "RECUSADO NO TESTE", "Operação não autorizada.");
            this.ultimoResultado = resultadoRecusado;
            return resultadoRecusado;
        }

        // Se não foi cancelado nem recusado no teste, retorna sucesso
        String dadosDoComprovante = String.format(
                "VIA CLIENTE\nPagamento com %s\nAPROVADO\nVALOR: R$ %.2f", tipoPagamento, valor
        );
        ResultadoTEF resultadoAprovado = new ResultadoTEF(true, "APROVADO", dadosDoComprovante);

        this.ultimoResultado = resultadoAprovado;
        return resultadoAprovado;
    }

    public ResultadoTEF getUltimoResultado() {
        return ultimoResultado;
    }
}