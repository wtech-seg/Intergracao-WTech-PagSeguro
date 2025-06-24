package br.com.wtech.totem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

//falta configurar variaveis de ambiente
//setx PAGSEGURO_EMAIL "seu-email@exemplo.com"
//setx PAGSEGURO_TOKEN "SEU_TOKEN_DO_PAGSEGURO"
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/tela_inicial.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setScene(scene);
        stage.setFullScreen(true); // Para tela cheia no totem
        stage.setTitle("Totem de Estacionamento");
        stage.show();
    }

    public static void main(String[] args) {
        System.out.println("Iniciando totem de pagamento. Aguarde...");

        launch();

//        PaymentService Service = new PaymentService(
//                System.getenv("PAGSEGURO_EMAIL"),
//                System.getenv("PAGSEGURO_TOKEN")
//        );

    }
}