package br.com.wtech.totem.service;

/**
 * Classe pública para guardar o resultado da transação TEF.
 */
public class ResultadoTEF { // <-- A PALAVRA "public" RESOLVE O PROBLEMA
    private final boolean aprovado;
    private final String mensagem;
    private final String dadosComprovante;

    public ResultadoTEF(boolean aprovado, String mensagem, String dadosComprovante) {
        this.aprovado = aprovado;
        this.mensagem = mensagem;
        this.dadosComprovante = dadosComprovante;
    }

    public boolean isAprovado() { return aprovado; }
    public String getMensagem() { return mensagem; }
    public String getDadosComprovante() { return dadosComprovante; }
}