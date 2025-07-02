//// src/main/java/br/com/wtech/totem/consumer/CommandConsumerMain.java
//package br.com.wtech.totem.consumer;
//
//import Conexao.ParamsMariaDB;
//import Conexao.ConexaoMariaDB;
//import Impressao.ImpUSBReceiptThread;
//import Log.LogWT;
//
//import java.sql.*;
//
//public class CommandConsumerMain {
//
//    public static void main(String[] args) throws Exception {
//        LogWT.mostraDireitos();
//        LogWT.setDebugWTech("N");
//
//        // Porta COM, ex: "COM3" ou "/dev/ttyUSB0"
//        String portaSerial = System.getenv("PRINT_PORT");
//        if (portaSerial == null) {
//            System.err.println("Informe a porta serial na variável PRINT_PORT.");
//            System.exit(1);
//        }
//
//        ParamsMariaDB.conectado();
//        LogWT.print("Conectado ao banco, usando porta " + portaSerial, "S");
//
//        while (true) {
//            try (Connection conn = ConexaoMariaDB.getConexaoMariaDB()) {
//                PreparedStatement ps = conn.prepareStatement(
//                        "SELECT id, ticket_code, device_code " +
//                                "  FROM ace_comandos " +
//                                " WHERE status = 0"
//                );
//                ResultSet rs = ps.executeQuery();
//
//                while (rs.next()) {
//                    long   id         = rs.getLong("id");
//                    String ticketCode = rs.getString("ticket_code");
//                    String deviceCode = rs.getString("device_code");
//
//                    LogWT.print("Processando comando ID=" + id + " TICKET=" + ticketCode, "S");
//
//                    // dispara thread que imprime o comprovante
//                    new ImpUSBReceiptThread(portaSerial, ticketCode);
//
//                    // marca como executado
//                    PreparedStatement upd = conn.prepareStatement(
//                            "UPDATE ace_comandos " +
//                                    "   SET status = 1, executed_at = NOW() " +
//                                    " WHERE id = ?"
//                    );
//                    upd.setLong(1, id);
//                    upd.executeUpdate();
//                    LogWT.print("Marca ace_comandos#"+id+" como EXECUTADO", "S");
//                }
//
//                Thread.sleep(500);  // 0.5s de delay entre polls
//            } catch (Exception e) {
//                LogWT.print("Erro no consumer: " + e.getMessage(), "S");
//                Thread.sleep(2000);
//            }
//        }
//    }
//}
