package br.com.wtech.totem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//remember Set ambient variables in the final OS
//setx PAGSEGURO_EMAIL "suavagaarapiraca@gmail.com"
//setx PAGSEGURO_TOKEN "B8B9DF4E0BF2479E8676AF43AA2B5FF7"
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}