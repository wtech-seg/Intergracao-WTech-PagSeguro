package br.com.wtech.totem;
//falta configurar variaveis de ambiente
//setx PAGSEGURO_EMAIL "seu-email@exemplo.com"
//setx PAGSEGURO_TOKEN "SEU_TOKEN_DO_PAGSEGURO"
public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando totem de pagamento. Aguarde...");

        PaymentService Service = new PaymentService(
                System.getenv("PAGSEGURO_EMAIL"),
                System.getenv("PAGSEGURO_TOKEN")
        );

    }
}