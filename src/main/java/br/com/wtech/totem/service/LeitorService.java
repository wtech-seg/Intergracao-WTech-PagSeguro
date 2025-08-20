package br.com.wtech.totem.service;

import br.com.wtech.totem.entity.Ticket;
import org.springframework.dao.EmptyResultDataAccessException;
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
import java.time.temporal.ChronoUnit;
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

    public boolean isTicketVinculadoAPessoa(String ticketCode) {
        System.out.println("LEITOR SERVICE: Verificando vínculo, status e validade da tag '" + ticketCode + "' em ace_pessoas.");

        // A query agora consulta apenas a tabela de pessoas e verifica as 3 condições.
        String sql = "SELECT COUNT(*) FROM ace_pessoas WHERE cd_tag = ? AND fl_status = 1 AND dt_final > ?";

        try {
            // Passamos a data e hora atuais como parâmetro para a query
            Integer count = jdbc.queryForObject(sql, new Object[]{ticketCode, LocalDateTime.now()}, Integer.class);
            boolean isValido = count != null && count > 0;

            if (isValido) {
                System.out.println("LEITOR SERVICE: Vínculo de mensalista encontrado e 100% válido.");
            } else {
                System.err.println("LEITOR SERVICE: Nenhum vínculo de mensalista válido encontrado para a tag '" + ticketCode + "'. (Pode estar expirado, com status incorreto ou não existir).");
            }
            return isValido;
        } catch (Exception e) {
            System.err.println("LEITOR SERVICE: Ocorreu um erro ao verificar o vínculo da tag: " + e.getMessage());
            return false;
        }
    }

    public boolean finalizarTicketPorGratuidade(String ticketCode) {
        // Primeiro, verifica se o ticket é elegível para a gratuidade.
        if (!isElegivelParaGratuidade(ticketCode)) {
            return false; // Não é elegível, então não faz nada e retorna false.
        }

        // Se for elegível, executa a atualização no banco.
        System.out.println("Finalizando ticket '" + ticketCode + "' como SAÍDA LIVRE por gratuidade (Status=4, TipoPagamento=5)...");
        String sql = "UPDATE est_tickets SET fl_status = 4, cd_tipo_pagamento = 6, dt_final = ? WHERE cd_ticket = ?";

        try {
            int linhasAfetadas = jdbc.update(sql, LocalDateTime.now(), ticketCode);

            if (linhasAfetadas > 0) {
                System.out.println("Ticket '" + ticketCode + "' atualizado com sucesso.");
                // Após o sucesso, busca o ticket com os dados novos...
                Ticket ticketAtualizado = this.buscarTicket(ticketCode);
                // ...e o define como o ticket atual, pronto para a próxima tela.
                this.setTicketAtual(ticketAtualizado);
                return true; // Retorna true para confirmar o sucesso da operação completa.
            } else {
                System.err.println("A atualização para gratuidade falhou (nenhuma linha afetada).");
                return false;
            }
        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO ao finalizar ticket '" + ticketCode + "' por gratuidade: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método auxiliar PRIVADO que apenas VERIFICA a elegibilidade, sem alterar nada.
     */
    private boolean isElegivelParaGratuidade(String ticketCode) {
        try {
            Ticket ticket = this.buscarTicket(ticketCode);
            // Um ticket só é elegível se estiver aberto (status 1) e não tiver data de saída.
            if (ticket.getStatus() != 1 || ticket.getExitTime() != null) {
                return false;
            }

            LocalDateTime dataInicial = ticket.getEntryTime();
            LocalDateTime dataAtual = LocalDateTime.now();
            long diferencaEmMinutos = ChronoUnit.MINUTES.between(dataInicial, dataAtual);
            int minutosDeGratuidade = this.getMinutosGratuidade();

            return diferencaEmMinutos >= 0 && diferencaEmMinutos <= minutosDeGratuidade;

        } catch (EmptyResultDataAccessException e) {
            return false; // Ticket não existe, não é elegível.
        }
    }

    private int getMinutosGratuidade() {
        try {
            String sql = "SELECT qt_minutos_gratuidade FROM est_par_cobranca LIMIT 1";
            Integer minutos = jdbc.queryForObject(sql, Integer.class);
            return minutos != null ? minutos : 0;
        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO: Não foi possível buscar os parâmetros de gratuidade. " + e.getMessage());
            return 0;
        }
    }

    private Integer getTurnoAtual() {
        System.out.println("TURNO: Buscando turno atual aberto...");
        String sql = "SELECT cd_turno FROM est_turnos WHERE dt_final IS NULL ORDER BY cd_turno DESC LIMIT 1";
        try {
            Integer turnoId = jdbc.queryForObject(sql, Integer.class);
            System.out.println("TURNO: Encontrado turno aberto com ID: " + turnoId);
            return turnoId;
        } catch (EmptyResultDataAccessException e) {
            // Ocorre se nenhum turno estiver aberto.
            System.err.println("TURNO: Nenhum turno aberto (com dt_final nula) foi encontrado!");
            return null; // Retorna null se não encontrar um turno aberto.
        } catch (Exception e) {
            System.err.println("TURNO: Erro ao buscar o turno atual. Causa: " + e.getMessage());
            return null;
        }
    }

    public void registrarMovimentoDeSaida() {
        Ticket ticket = getTicketAtual();
        if (ticket == null) {
            System.err.println("MOVIMENTO: Ticket atual é nulo. Não foi possível registrar o movimento.");
            return;
        }

        // Não registra movimento para tickets "fantasma" de mensalista que não têm valor nem status real de pagamento.
        if (ticket.getFinalValue() == null && ticket.getStatus() == 3) {
            System.out.println("MOVIMENTO: Ticket de mensalista sem valor. Movimento não aplicável.");
            return;
        }

        String checkSql = "SELECT COUNT(*) FROM est_movimentos WHERE cd_ticket = ?";
        Integer count = jdbc.queryForObject(checkSql, new Object[]{ticket.getTicketCode()}, Integer.class);

        // Se a contagem for maior que 0, o registro já existe.
        if (count != null && count > 0) {
            System.out.println("MOVIMENTO: Movimentação para o ticket '" + ticket.getTicketCode() + "' já existe. Nenhuma nova movimentação será registrada.");
            return; // Encerra o método para não duplicar o registro
        }

        System.out.println("MOVIMENTO: Registrando movimentação para o ticket '" + ticket.getTicketCode() + "'.");
        String sql = "INSERT INTO est_movimentos (cd_ticket, dt_pagamento, vl_final, cd_tipo_pagamento, fl_status, DML_USR, DML_DATA, DML_IP, cd_turno) VALUES (?, ?, ?, ?, ?, 'Autopagamento', NOW(), NULL, ?)";

        try {
            String sqlTipoPagamento = "SELECT cd_tipo_pagamento FROM est_tickets WHERE cd_ticket = ?";
            Integer tipoPagamentoAtualizado = jdbc.queryForObject(sqlTipoPagamento, new Object[]{ticket.getTicketCode()}, Integer.class);

            Integer turnoId = getTurnoAtual();
            BigDecimal valorFinal = ticket.getFinalValue() != null ? ticket.getFinalValue() : BigDecimal.ZERO;

            jdbc.update(sql,
                    ticket.getTicketCode(),
                    ticket.getExitTime(),
                    valorFinal,
                    tipoPagamentoAtualizado,
                    3,
                    turnoId
            );
            System.out.println("MOVIMENTO: Movimentação registrada com sucesso.");

        } catch (Exception e) {
            System.err.println("ERRO CRÍTICO: Falha ao inserir registro na tabela est_movimentos para o ticket " + ticket.getTicketCode() + ". Causa: " + e.getMessage());
            e.printStackTrace();
        }
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