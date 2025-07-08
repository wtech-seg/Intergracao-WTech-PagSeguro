package br.com.wtech.totem;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

//remember Set ambient variables in the final OS
//setx PAGBANK_EMAIL "suavagaarapiraca@gmail.com"
//setx PAGBANK_TOKEN "B8B9DF4E0BF2479E8676AF43AA2B5FF7"
//setx PAGBANK_TOKEN "751a9ba1-cc97-4fa6-990d-4dede03e152c8f9704c845afa9eda182ba0e7c4a55eba1c2-5842-46df-b277-7671cfc091db"
//setx PAGBANK_TOKEN "450ede88-fdc2-4a1c-9eaf-e5f6eb3a00ed71f329e74d06a61e87ab82b4af5092127f50-b94f-43e7-895d-75923e0ec87b"

@SpringBootApplication
public class Application extends javafx.application.Application {

    private ConfigurableApplicationContext springContext;
    private Parent root;

    @Override
    public void init() throws Exception {
        // 1. Inicia o Spring e guarda o contexto
        springContext = SpringApplication.run(Application.class);

        // 2. Prepara o loader para a primeira tela
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/tela_inicial.fxml"));

        // 3. A LINHA MAIS IMPORTANTE: Conecta o loader com o Spring
        fxmlLoader.setControllerFactory(springContext::getBean);

        // 4. Carrega a interface
        root = fxmlLoader.load();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setTitle("Totem de Estacionamento");
        stage.show();
    }

    @Override
    public void stop() {
        // Encerra o Spring de forma limpa
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        // Apenas inicia o ciclo de vida do JavaFX
        launch(args);
    }
}