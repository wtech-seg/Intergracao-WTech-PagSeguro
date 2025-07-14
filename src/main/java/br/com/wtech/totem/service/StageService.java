package br.com.wtech.totem.service;

import javafx.stage.Stage;
import org.springframework.stereotype.Service;

@Service
public class StageService {
    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}