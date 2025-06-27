package br.com.wtech.totem.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TelaInicialController {

    @FXML private GridPane root;
    @FXML private ImageView imgFundo;
    @FXML private Label labelHora;
    @FXML private Button btnIniciar;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        // Ajusta o tamanho do fundo e inicia o relógio
        imgFundo.fitWidthProperty().bind(root.widthProperty());
        imgFundo.fitHeightProperty().bind(root.heightProperty().multiply(0.8));
        iniciarRelogio();

        // Registra ESC para minimizar na Tela Inicial
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(evt -> {
                    if (evt.getCode() == KeyCode.ESCAPE) {
                        Stage stage = (Stage) root.getScene().getWindow();
                        stage.setIconified(true);
                    }
                });
            }
        });
    }

    private void iniciarRelogio() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> labelHora.setText(LocalTime.now().format(formatter))),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void handleIniciar() {
        System.out.println("Botão INICIAR pressionado!");

        String fxmlPath = "/fxml/tela_leitor.fxml";
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            System.err.println(">>> NÃO achou o FXML em: " + fxmlPath);
            return;
        }

        try {
            Parent telaLeitor = new FXMLLoader(url).load();
            Scene novaCena = new Scene(telaLeitor);

            Stage stage = (Stage) btnIniciar.getScene().getWindow();

            // Remove hint e atalho de saída do fullscreen
            stage.setFullScreenExitHint("");
            stage.setFullScreenExitKeyCombination(null);

            // Registra ESC para minimizar na Tela Leitor
            novaCena.setOnKeyPressed(evt -> {
                if (evt.getCode() == KeyCode.ESCAPE) {
                    stage.setIconified(true);
                }
            });

            // Troca a cena e entra em fullscreen imediatamente
            stage.setScene(novaCena);
            stage.setFullScreen(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Erro ao carregar " + fxmlPath);
            e.printStackTrace();
        }
    }
}
