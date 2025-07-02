package br.com.wtech.totem;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//remember Set ambient variables in the final OS
//setx PAGSEGURO_EMAIL "suavagaarapiraca@gmail.com"
//setx PAGSEGURO_TOKEN "B8B9DF4E0BF2479E8676AF43AA2B5FF7"
//setx PAGSEGURO_TOKEN "751a9ba1-cc97-4fa6-990d-4dede03e152c8f9704c845afa9eda182ba0e7c4a55eba1c2-5842-46df-b277-7671cfc091db"
//setx PAGSEGURO_TOKEN "450ede88-fdc2-4a1c-9eaf-e5f6eb3a00ed71f329e74d06a61e87ab82b4af5092127f50-b94f-43e7-895d-75923e0ec87b"

@SpringBootApplication
public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/tela_inicial.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setScene(scene);
        stage.setFullScreen(true); // Para tela cheia no totem
        stage.setTitle("Totem de Estacionamento");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
        SpringApplication.run(Application.class, args);
    }
}