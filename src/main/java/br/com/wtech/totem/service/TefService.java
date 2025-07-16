// src/main/java/br/com/wtech/totem/service/TefService.java
package br.com.wtech.totem.service;

import br.com.wtech.totem.service.tef.TefClientMCLibrary;
import org.springframework.stereotype.Service;

@Service
public class TefService {

    private final TefClientMCLibrary tef = TefClientMCLibrary.INSTANCE;

    /**
     * Processa a transação TEF via DLL.
     * Retorna a string de resposta completa (ex: “[RETORNO] Autorizado ...”).
     */
    public String processarPagamento(
            int    operacao,
            String cnpj,
            int    parcelas,
            String cupom,
            String valor,
            String nsu,
            String data,
            String pdv,
            String codLoja
    ) {
        // 1) inicia
        int ret = tef.IniciaFuncaoMCInterativo(
                operacao, cnpj, parcelas, cupom, valor,
                nsu, data, pdv, codLoja, 0, ""
        );
        if (ret != 0) {
            throw new RuntimeException("Erro IniciaFuncao: " + ret);
        }

        // 2) loop de interação
        String resposta;
        do {
            resposta = tef.AguardaFuncaoMCInterativo();
            if (resposta.startsWith("[ERROABORTAR]") || resposta.startsWith("[ERRODISPLAY]")) {
                tef.CancelarFluxoMCInterativo();
                throw new RuntimeException("TEF abortado: " + resposta);
            }
            // COMANDO PARA MENU/PERGUNTA se necessário
        } while (!resposta.startsWith("[RETORNO]"));

        // 3) finaliza (código 98 = confirma)
        tef.FinalizaFuncaoMCInterativo(
                98, cnpj, parcelas, cupom, valor,
                nsu, data, pdv, codLoja, 0, ""
        );

        return resposta;
    }
}
