package br.com.wtech.totem.service;

import br.com.wtech.totem.entity.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class FormaPagamentoService {

    private final JdbcTemplate jdbc;
    private final LeitorService leitorService; // Dependência para obter o ticket atual

    private String formaPagamento;

    // Injetando as dependências necessárias via construtor
    @Autowired
    public FormaPagamentoService(JdbcTemplate jdbc, LeitorService leitorService) {
        this.jdbc = jdbc;
        this.leitorService = leitorService;
    }

    public void setFormaPagamento(String forma) {
        this.formaPagamento = forma;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void limpar() {
        formaPagamento = null;
    }

    /**
     * Atualiza o tipo de pagamento do ticket atual no banco de dados.
     * @param tipoPagamento O código do tipo de pagamento (1=Débito, 2=Crédito, 3=Pix).
     * @return O número de linhas afetadas.
     */
    public int atualizarTipoPagamentoNoBanco(int tipoPagamento) {
        Ticket ticket = leitorService.getTicketAtual();

        if (ticket == null || ticket.getTicketCode() == null) {
            System.err.println("Erro: Não há ticket atual para atualizar o pagamento.");
            return 0;
        }

        System.out.println("Atualizando ticket '" + ticket.getTicketCode() + "' para o tipo de pagamento: " + tipoPagamento);

        String sql = "UPDATE est_tickets SET cd_tipo_pagamento = ? WHERE cd_ticket = ?";
        return jdbc.update(sql, tipoPagamento, ticket.getTicketCode());
    }
}