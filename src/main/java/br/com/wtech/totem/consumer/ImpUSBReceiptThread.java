//// src/main/java/br/com/wtech/totem/consumer/ImpUSBReceiptThread.java
//package br.com.wtech.totem.consumer;
//
//import Conexao.ConexaoMariaDB;
//import Log.LogWT;
//
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//import java.sql.*;
//import java.text.SimpleDateFormat;
//import java.time.format.DateTimeFormatter;
//import java.util.Date;
//import java.util.TimeZone;
//
//public class ImpUSBReceiptThread extends Thread {
//
//    private final String portaSerial;
//    private final String ticketCode;
//
//    public ImpUSBReceiptThread(String portaSerial, String ticketCode) {
//        this.portaSerial = portaSerial;
//        this.ticketCode  = ticketCode;
//        start();
//    }
//
//    @Override
//    public void run() {
//        // 1) busca dados do ticket pago
//        String sql = """
//           SELECT
//             ET.vl_final,
//             ET.ds_observacao,
//             DATE_FORMAT(T.exit_time, '%d/%m/%Y %H:%i:%s') AS exit_time
//           FROM est_tickets ET
//           JOIN tickets    T   ON T.code = ET.cd_ticket
//           WHERE ET.cd_ticket = ?
//        """;
//
//        String vlFinal     = "0,00";
//        String dsObs       = "";
//        String exitTimeStr = DateTimeFormatter
//                .ofPattern("dd/MM/yyyy HH:mm:ss")
//                .format(java.time.LocalDateTime.now());
//
//        try (Connection conn = ConexaoMariaDB.getConexaoMariaDB();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setString(1, ticketCode);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                vlFinal     = rs.getString("vl_final");
//                dsObs       = rs.getString("ds_observacao");
//                exitTimeStr = rs.getString("exit_time");
//            }
//        } catch (SQLException ex) {
//            LogWT.print("Erro buscando dados do ticket " + ticketCode + ": " + ex, "S");
//        }
//
//        // 2) formata e envia ESC/POS para a COM
//        try (FileOutputStream fos = new FileOutputStream(portaSerial);
//             PrintStream ps = new PrintStream(fos)) {
//
//            // Init Impressora
//            ps.write(new byte[]{0x1B, 0x40}); // ESC @
//
//            ps.println("COMPROVANTE DE PAGAMENTO");
//            ps.println("-------------------------------");
//            ps.println("Ticket: " + ticketCode);
//            ps.println("Saída:  " + exitTimeStr);
//            ps.println("Valor:  R$ " + vlFinal);
//            if (!dsObs.isBlank()) ps.println("Obs:    " + dsObs);
//            ps.println("-------------------------------");
//            ps.println("\n\n\n");
//
//            // Corte parcial
//            ps.write(new byte[]{0x1D, 0x56, 0x41, 0x00}); // GS V A 0
//
//            ps.flush();
//        } catch (Exception e) {
//            LogWT.print("Falha ao imprimir no " + portaSerial + ": " + e, "S");
//        }
//    }
//}
