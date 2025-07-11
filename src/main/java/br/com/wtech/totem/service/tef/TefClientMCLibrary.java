package br.com.wtech.totem.service.tef;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface TefClientMCLibrary extends Library {
    TefClientMCLibrary INSTANCE =
            Native.load("TefClientMC", TefClientMCLibrary.class);

    int IniciaFuncaoMCInterativo(
            int iComando,
            String sCnpjCliente,
            int iParcela,
            String sCupom,
            String sValor,
            String sNsu,
            String sData,
            String sNumeroPDV,
            String sCodigoLoja,
            int iTipoComunicacao,
            String sParametro
    );

    String AguardaFuncaoMCInterativo();

    int ContinuaFuncaoMCInterativo(String sInformacao);

    int FinalizaFuncaoMCInterativo(
            int iComando,
            String sCnpjCliente,
            int iParcela,
            String sCupom,
            String sValor,
            String sNsu,
            String sData,
            String sNumeroPDV,
            String sCodigoLoja,
            int iTipoComunicacao,
            String sParametro
    );

    int CancelarFluxoMCInterativo();
}
