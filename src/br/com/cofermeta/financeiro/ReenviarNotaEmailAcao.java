package br.com.cofermeta.financeiro;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.modelcore.comercial.nfe.ServicosNFeHelper2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

public class ReenviarNotaEmailAcao implements AcaoRotinaJava {
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] registros =  contextoAcao.getLinhas();
        Collection<BigDecimal> lisNotasSelecionadas = new ArrayList<>();
        ArrayList nuNotas = new ArrayList<>();
        try{
            for(Registro registro : registros){
                lisNotasSelecionadas.add(BigDecimal.valueOf(Integer.parseInt(registro.getCampo("NUNOTA").toString())));
            }
            ServicosNFeHelper2 servicosNFeHelper2 = ServicosNFeHelper2.build();
            servicosNFeHelper2.enviarEmailXMLNota(lisNotasSelecionadas);
            if(lisNotasSelecionadas.isEmpty()){
                contextoAcao.setMensagemRetorno("Nenhuma nota selecionada para envio do XML/PDF!!!");
            } else {
                contextoAcao.setMensagemRetorno("Envio realizado com sucesso!!!");
            }
        }catch (Exception e){
            e.printStackTrace();
            contextoAcao.mostraErro("Nota(s) selecionada(s) não tem XML/PDF gerado(s) no sistema!!!" + " " + e.getMessage());
        }


    }
}
