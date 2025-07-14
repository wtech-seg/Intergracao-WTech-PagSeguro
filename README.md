
# Integração WTech com PagSeguro

Este projeto tem como objetivo integrar sistemas da **WTech** com a API de pagamentos da **PagSeguro**, utilizando uma **DLL própria de comunicação**. O foco principal é facilitar pagamentos com cartão (crédito/débito), especialmente em contextos como totens de autoatendimento.

## 📦 Estrutura do Projeto

```
├── src/
│   ├── main/
│   │   └── java/br/com/wtech/totem/
│   │       ├── controller/
│   │       ├── entity/
│   │       ├── repository/
│   │       ├── service/
│   │       └── WtechTotemApplication.java
├── lib/
│   └── pagseguro.dll  # DLL de integração com o terminal
├── resources/
│   └── application.yml
├── pom.xml
└── README.md
```

## ⚙️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot**
- **Maven**
- **JDBC Template**
- **DLL nativa para integração com POS**
- **PagSeguro SDK / APIs**

## 🚀 Funcionalidades

- Comunicação direta com o terminal PagSeguro via DLL
- Execução de transações com cartão (débito/crédito)
- Consulta e manipulação de tickets de pagamento
- Inserção de registros no banco de dados e histórico de pagamentos
- Integração com sistemas WTech existentes

## 🧪 Como executar

### Pré-requisitos

- Java 17
- Maven
- Terminal PagSeguro configurado com a DLL compatível
- Sistema operacional compatível com a DLL (Windows)

### Passos

1. Clone o repositório:
   ```bash
   git clone https://github.com/wtech-seg/Intergracao-WTech-PagSeguro.git
   cd Intergracao-WTech-PagSeguro
   ```

2. Compile e execute a aplicação:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. A DLL `pagseguro.dll` deve estar disponível no diretório `lib/` e configurada no `java.library.path`.

## ⚠️ Observações

- Certifique-se de que o terminal esteja corretamente instalado e vinculado à aplicação.
- A comunicação com o terminal depende da presença e correta configuração da DLL.
- Apenas sistemas Windows são suportados nesta versão.

## 👨‍💻 Desenvolvido por

**Equipe de Integração - WTech**
- E-mail: suporte@wtechbrasil.com.br
- Site: [https://wtechbrasil.com.br](https://wtechbrasil.com.br)

## 📄 Licença

Este projeto é de uso interno da WTech e não está licenciado para uso externo.
