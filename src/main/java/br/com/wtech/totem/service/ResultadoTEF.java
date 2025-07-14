package br.com.wtech.totem.service;

/**
 * Classe pública para guardar o resultado da transação TEF.
 * Sendo 'public', ela pode ser acessada por qualquer outra classe no projeto, como os controllers.
 */
public class ResultadoTEF {

    private final boolean aprovado;
    private final String status; // Um status simples como "APROVADO", "RECUSADO", "ERRO"
    private final String mensagemDetalhada; // Guarda o comprovante completo ou os detalhes de um erro

    public ResultadoTEF(boolean aprovado, String status, String mensagemDetalhada) {
        this.aprovado = aprovado;
        this.status = status;
        this.mensagemDetalhada = mensagemDetalhada;
    }

    /**
     * Retorna true se a transação foi aprovada, false caso contrário.
     */
    public boolean isAprovado() {
        return aprovado;
    }

    /**
     * Retorna o status geral da transação (ex: "APROVADO", "CANCELADO").
     */
    public String getStatus() {
        return status;
    }

    /**
     * Retorna os dados completos do comprovante em caso de sucesso,
     * ou uma mensagem detalhada em caso de erro.
     * Este é o método que o `TelaPagamentoSelecionadoController` usa.
     */
    public String getMensagemDetalhada() {
        return mensagemDetalhada;
    }
}