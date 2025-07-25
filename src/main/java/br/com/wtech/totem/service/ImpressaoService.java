package br.com.wtech.totem.service;

import br.com.wtech.totem.entity.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

@Service
public class ImpressaoService {

    private final JdbcTemplate jdbc;
    private final LeitorService leitorService; // Dependência para obter o ticket

    @Autowired
    public ImpressaoService(JdbcTemplate jdbc, LeitorService leitorService) {
        this.jdbc = jdbc;
        this.leitorService = leitorService;
    }

    /**
     * Orquestra todas as operações de banco de dados necessárias ao imprimir.
     */
    public void registrarOperacoesDeImpressao() {
        Ticket ticket = leitorService.getTicketAtual();

        if (ticket == null || ticket.getTicketCode() == null) {
            System.err.println("IMPRESSAO SERVICE: Operação cancelada. Nenhum ticket ativo.");
            return;
        }

        registrarImpressaoRecibo(ticket);
        registrarTagLeitura(ticket);
        atualizarStatusParaImpresso(ticket);
        leitorService.setUltimoTicketPago(ticket.getTicketCode());
    }

    /**
     * Atualiza o status do ticket para 2 (REGISTRA SAÍDA).
     * Chamado quando a impressão é iniciada.
     */
    private void atualizarStatusParaImpresso(Ticket ticket) {
        System.out.println("IMPRESSAO SERVICE: Atualizando status para '2' (REGISTRA SAÍDA) no ticket: " + ticket.getTicketCode());
        String sql = "UPDATE est_tickets SET fl_status = 2 WHERE cd_ticket = ?";
        int linhasAfetadas = jdbc.update(sql, ticket.getTicketCode());
        if (linhasAfetadas > 0) ticket.setStatus(2);
    }

    private void registrarImpressaoRecibo(Ticket ticket) {
        System.out.println("IMPRESSAO SERVICE: Registrando impressão de recibo para o ticket: " + ticket.getTicketCode());
        String ipDoTotem = getIpLocal();
        String sql = "INSERT INTO ace_qr_code (NO_QR_CODE, FL_SITUACAO, DT_VALIDADE_INI, CD_PORTA, DT_VALIDADE_FIM, DML_DATA, DML_IP, DML_USR) " +
                "VALUES (?,'A',?,'\\\\\\\\127.0.0.1\\\\ImpEstacionamento', ?, ?, ?, ?)";
        jdbc.update(sql, ticket.getTicketCode(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), ipDoTotem, "autopagamento");
        System.out.println("IMPRESSAO SERVICE: Registro de impressão inserido com sucesso.");
    }

    private void registrarTagLeitura(Ticket ticket) {
        System.out.println("IMPRESSAO SERVICE: Registrando tag de leitura para o ticket: " + ticket.getTicketCode());
        // 1. Define o valor padrão para o tipo de leitura.
        char tipoLeitura = 'Q';

        // 2. Verifica se o tipo de pagamento do ticket é 6.
        if (ticket.getTipoPagamento() != null && ticket.getTipoPagamento() == 6) {
            System.out.println("IMPRESSAO SERVICE: Tipo de pagamento 6 detectado. Alterando tipo de leitura para 'T'.");
            tipoLeitura = 'T'; // Se for 6, altera para 'T'.
        }

        String ipDestino = "Q192.168.0.96";
        String ipDoTotem = getIpLocal();

        String sql = "INSERT INTO ace_tag_leitura (NU_HASH_TAG, DT_LEITURA, CD_PORTA, FL_TIPO_LEITURA, DML_USR, DML_DATA, DML_IP, FL_AGUARDANDO, NU_TAG) " +
                "VALUES(?, ?, ?, ?, 'autopagamento', ?, ?, 'A', ?)";

        // 3. Usa a variável 'tipoLeitura' no comando INSERT.
        jdbc.update(sql,
                ticket.getTicketCode(),
                LocalDateTime.now(),
                ipDestino,
                String.valueOf(tipoLeitura), // Converte o char para String para o JDBC
                LocalDateTime.now(),
                ipDoTotem,
                ticket.getTicketCode()
        );System.out.println("IMPRESSAO SERVICE: Tag de leitura inserida com sucesso.");
    }

    private String getIpLocal() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }

    public void registrarOperacoesDeReimpressao(String ticketCode) {
        if (ticketCode == null || ticketCode.isBlank()) {
            System.err.println("REIMPRESSAO SERVICE: Operação cancelada. Código do ticket não fornecido.");
            return;
        }
        registrarImpressaoRecibo(ticketCode);
    }

    public void registrarOperacoesDeCancelamento(String ticketCode) {
        if (ticketCode == null || ticketCode.isBlank()) {
            System.err.println("CANCELAMENTO SERVICE: Operação cancelada. Código do ticket não fornecido.");
            return;
        }
        atualizarStatusParaCancelado(ticketCode);
        registrarImpressaoRecibo(ticketCode);
    }

    private void registrarImpressaoRecibo(String ticketCode) {
        System.out.println("IMPRESSAO SERVICE: Registrando impressão de recibo para o ticket: " + ticketCode);
        String ipDoTotem = getIpLocal();
        String sql = "INSERT INTO ace_qr_code (NO_QR_CODE, FL_SITUACAO, DT_VALIDADE_INI, CD_PORTA, DT_VALIDADE_FIM, DML_DATA, DML_IP, DML_USR) " +
                "VALUES (?,'A',?,'\\\\\\\\127.0.0.1\\\\ImpEstacionamento', ?, ?, ?, ?)";
        jdbc.update(sql, ticketCode, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), ipDoTotem, "autopagamento");
        System.out.println("IMPRESSAO SERVICE: Registro de impressão inserido com sucesso.");
    }

    private void atualizarStatusParaCancelado(String ticketCode) {
        System.out.println("CANCELAMENTO SERVICE: Atualizando status para '6' (Cancelado) no ticket: " + ticketCode);
        String sql = "UPDATE est_tickets SET fl_status = 6 WHERE cd_ticket = ?";
        int linhasAfetadas = jdbc.update(sql, ticketCode);
        if (linhasAfetadas > 0) {
            System.out.println("CANCELAMENTO SERVICE: Status do ticket " + ticketCode + " atualizado para 6 no banco de dados.");
        } else {
            System.err.println("CANCELAMENTO SERVICE: Nenhuma linha foi atualizada para o ticket " + ticketCode + ".");
        }
    }
}