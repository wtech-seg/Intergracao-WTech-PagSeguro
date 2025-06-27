package br.com.wtech.totem;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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