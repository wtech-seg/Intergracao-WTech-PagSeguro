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
                "VALUES (?,'A',?,'/dev/usb/lp0', ?, ?, ?, ?)";
        jdbc.update(sql, ticket.getTicketCode(), LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), ipDoTotem, "autopagamento");
        System.out.println("IMPRESSAO SERVICE: Registro de impressão inserido com sucesso.");
    }

    private void registrarTagLeitura(Ticket ticket) {
        System.out.println("IMPRESSAO SERVICE: Registrando tag de leitura para o ticket: " + ticket.getTicketCode());
        String ipDestino = "Q192.168.0.96";
        String sql = "INSERT INTO ace_tag_leitura (NU_HASH_TAG, DT_LEITURA, CD_PORTA, FL_TIPO_LEITURA, DML_USR, DML_DATA, DML_IP, FL_AGUARDANDO, NU_TAG) " +
                "VALUES(?, ?, ?, 'Q', 'autopagamento', ?, ?, 'A', ?)";
        jdbc.update(sql, ticket.getTicketCode(), LocalDateTime.now(), ipDestino, LocalDateTime.now(), ipDestino, ticket.getTicketCode());
        System.out.println("IMPRESSAO SERVICE: Tag de leitura inserida com sucesso.");
    }

    private String getIpLocal() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }
}