# Self-Pay-Integration

Totem de Autopagamento de Estacionamento Drive-Thru usando Java, Spring Boot e PagSeguro.

## Estrutura do Projeto

```
Self-Pay-Integration/
├── pom.xml                     # Configurações Maven, dependências Spring Boot, PagSeguro e JavaFX
├── src/main/java/
│   └── br/com/wtech/totem/
│       ├── Application.java    # Ponto de entrada Spring Boot
│       ├── PaymentService.java # Comunicação com PagSeguro (Checkout Transparente v2)
│       ├── entity/
│       │   └── Ticket.java      # Entidade JPA para armazenar status do ticket
│       ├── repository/
│       │   └── TicketRepository.java
│       ├── service/
│       │   ├── GateService.java   # Libera a cancela (stub para hardware)
│       │   └── TicketService.java # Lógica de pagamento, persiste e aciona cancela
│       └── controller/
│           └── PaymentController.java # Endpoints REST (/api/pay, ...)
└── README.md                   # Documentação do projeto (este arquivo)
```

## Tecnologias

* **Java 17**
* **Spring Boot 3.1.4**

    * Spring Web (REST)
    * Spring Data JPA (persistência)
* **PagSeguro Checkout Transparente v2** (form-urlencoded + XML)
* **H2 Database** (ambiente de testes)
* **JavaFX 20** (frontend desktop, Windows)

## Funcionalidades

1. **`/api/pay`**: Recebe `{ ticketCode, amountInCents, cardToken }`, executa pagamento, marca ticket pago e abre cancela.
2. **`PaymentService`**: Monta e envia requisição ao PagSeguro, faz parsing do XML de retorno.
3. **`TicketService`**: Persiste status do ticket e aciona `GateService`.
4. **`GateService`**: Stub para liberar hardware da cancela.

## Como Rodar

1. Defina variáveis de ambiente para sandbox:
````
env PAGSEGURO\_EMAIL=[seu-email@sandbox.pagseguro.uol.com.br](mailto:seu-email@sandbox.pagseguro.uol.com.br)
env PAGSEGURO\_TOKEN=seu-token-de-sandbox
````
2. Compile e execute:
````
mvn clean package
mvn spring-boot:run
````

3. API estará disponível em `http://localhost:8080/api/pay`.

## Próximos Passos

* Implementar endpoints `/status` e `/refund`.
* Integrar com front-end JavaFX (controllers e views).
* Configurar banco de produção (MySQL/PostgreSQL).
* Homologação em ambiente de produção PagSeguro.

---

> **Observação**: A tela e lógica JavaFX estão iniciadas em `TelaInicialController.java`, para futura integração do front-end do totem.
