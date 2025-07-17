package br.com.wtech.totem.service;

import br.com.wtech.totem.entity.Ticket;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class LeitorService {

    private static final DateTimeFormatter FORMATADOR_LOG = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final JdbcTemplate jdbc;
    private Ticket ticketAtual;
    private String permanenciaFormatada;
    private LocalDateTime dataDaLeitura;
    private String ultimoTicketPagoCode;

    public LeitorService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void setTicketAtual(Ticket ticket) {
        this.ticketAtual = ticket;
        this.dataDaLeitura = LocalDateTime.now();
        calcularEArmazenarPermanencia();
    }

    public Ticket getTicketAtual() {
        return this.ticketAtual;
    }

    public String getPermanenciaFormatada() {
        return this.permanenciaFormatada != null ? this.permanenciaFormatada : "--:--";
    }

    public void limparTicketAtual() {
        this.ticketAtual = null;
        this.permanenciaFormatada = null;
        this.dataDaLeitura = null;
    }

    private void calcularEArmazenarPermanencia() {
        if (this.ticketAtual == null || this.ticketAtual.getEntryTime() == null) {
            this.permanenciaFormatada = "--:--";
            return;
        }

        Duration duracao = Duration.between(this.ticketAtual.getEntryTime(), this.dataDaLeitura);
        long horas = duracao.toHours();
        long minutos = duracao.toMinutesPart();
        this.permanenciaFormatada = String.format("%02d:%02d", horas, minutos);
    }

    public void finalizarTicketComDataDeLeitura() { // Nome do método ficou mais claro
        if (ticketAtual == null || dataDaLeitura == null) {
            System.err.println("Erro: Não há ticket ou data de leitura para finalizar.");
            return;
        }

        String dataFormatadaParaLog = dataDaLeitura.format(FORMATADOR_LOG);
        System.out.println("Finalizando ticket '" + ticketAtual.getTicketCode() + "' com a data/hora da leitura: " + dataFormatadaParaLog);
        String sql = "UPDATE est_tickets SET dt_final = ? WHERE cd_ticket = ?";
        int linhasAfetadas = jdbc.update(sql, this.dataDaLeitura, ticketAtual.getTicketCode());
        if (linhasAfetadas > 0) {
            ticketAtual.setExitTime(this.dataDaLeitura);
        }
    }
    /**
     * Atualiza o status do ticket atual para 3 (PAGO).
     * Chamado quando o pagamento é aprovado.
     */
    public void atualizarStatusParaPago() {
        Ticket ticket = getTicketAtual();
        if (ticket == null) {
            System.err.println("Erro: Não há ticket para atualizar status para PAGO.");
            return;
        }
        System.out.println("Atualizando status para '3' (PAGO) no ticket: " + ticket.getTicketCode());
        String sql = "UPDATE est_tickets SET fl_status = 3 WHERE cd_ticket = ?";
        int linhasAfetadas = jdbc.update(sql, ticket.getTicketCode());
        if (linhasAfetadas > 0) ticket.setStatus(3);
    }

    // --- Busca o Ticket no banco pelo código lido ---
    public Ticket buscarTicket(String ticketCode) {
        String sql = "SELECT * FROM est_tickets WHERE cd_ticket = ?";
        return jdbc.queryForObject(sql, new Object[]{ticketCode}, new TicketRowMapper());
    }

    public String getValorTotalFormatado() {
        Ticket ticket = getTicketAtual();

        if (ticket != null && ticket.getFinalValue() != null) {
            BigDecimal valor = ticket.getFinalValue();

            // Formata o valor para o padrão de moeda brasileiro
            NumberFormat formatadorDeMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            return formatadorDeMoeda.format(valor);
        } else {
            // Retorna o valor padrão se não houver dados
            return "R$ --,--";
        }
    }

    public int getVlFinal() {
        Ticket ticket = getTicketAtual();
        if (ticket != null && ticket.getFinalValue() != null) {
            // Multiplica por 100 e converte para int (centavos)
            return ticket.getFinalValue().multiply(new BigDecimal("100")).intValue();
        }
        return 0;
    }

    public void setUltimoTicketPago(String ticketCode) {
        this.ultimoTicketPagoCode = ticketCode;
    }

    public String getUltimoTicketPago() {
        return ultimoTicketPagoCode;
    }

    // --- RowMapper para a entidade Ticket ---
    private static class TicketRowMapper implements RowMapper<Ticket> {
        @Override
        public Ticket mapRow(ResultSet rs, int rowNum) throws SQLException {
            Ticket ticket = new Ticket();
            ticket.setTicketCode(rs.getString("cd_ticket"));
            ticket.setEntryTime(rs.getTimestamp("dt_inicial").toLocalDateTime());
            ticket.setExitTime(rs.getTimestamp("dt_final") != null ? rs.getTimestamp("dt_final").toLocalDateTime() : null);
            ticket.setFinalValue(rs.getBigDecimal("vl_final"));
            ticket.setTipoPagamento(rs.getObject("cd_tipo_pagamento") != null ? rs.getInt("cd_tipo_pagamento") : null);
            ticket.setStatus(rs.getInt("fl_status"));
            return ticket;
        }
    }
}