package br.com.wtech.totem.service;

import br.com.wtech.totem.service.tef.TefClientMCLibrary;
import org.springframework.stereotype.Service;

@Service
public class TefService {

    private final TefClientMCLibrary tef = TefClientMCLibrary.INSTANCE;

    public String processarPagamento(
            int operacao,
            String cnpj,
            int parcelas,
            String cupom,
            String valor,
            String nsu,
            String data,         // yyyyMMdd
            String pdv,
            String codLoja
    ) {
        // 1) Inicia
        int ret = tef.IniciaFuncaoMCInterativo(
                operacao, cnpj, parcelas, cupom, valor, nsu, data, pdv, codLoja, 0, ""
        );
        if (ret != 0) {
            throw new RuntimeException("Erro IniciaFuncao: " + ret);
        }

        // 2) Loop de interação
        String resposta;
        do {
            resposta = tef.AguardaFuncaoMCInterativo();
            if (resposta.startsWith("[MENU]") || resposta.startsWith("[PERGUNTA]")) {
                // exibir menu/pergunta no front e capturar input do operador...
                //String escolha = /* obter do operador */;
                //tef.ContinuaFuncaoMCInterativo(escolha);
            }
            if (resposta.startsWith("[ERROABORTAR]") || resposta.startsWith("[ERRODISPLAY]")) {
                tef.CancelarFluxoMCInterativo();
                throw new RuntimeException("Fluxo abortado: " + resposta);
            }
        } while (!resposta.startsWith("[RETORNO]"));

        // 3) Confirmar transação (código 98)
        tef.FinalizaFuncaoMCInterativo(
                98, cnpj, parcelas, cupom, valor, nsu, data, pdv, codLoja, 0, ""
        );

        return resposta;  // será string com comprovante e dados :contentReference[oaicite:8]{index=8}
    }
}
