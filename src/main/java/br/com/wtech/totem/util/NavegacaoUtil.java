package br.com.wtech.totem.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class NavegacaoUtil {

    private final ApplicationContext context;

    public NavegacaoUtil(ApplicationContext context) {
        // Esta mensagem SÓ PODE APARECER UMA VEZ, quando o Spring inicia.
        System.out.println("!!! NavegacaoUtil foi criado pelo SPRING !!!");
        this.context = context;
    }

    public void trocaTela(String fxmlPath, Node node) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("FXML não encontrado: " + fxmlPath);
                return;
            }
            SpringFXMLLoader loader = new SpringFXMLLoader(context);
            FXMLLoader fxmlLoader = loader.load(url);
            Parent root = fxmlLoader.getRoot();

            Scene scene = new Scene(root);
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Erro ao carregar tela: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
