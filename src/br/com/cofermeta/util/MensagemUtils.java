package br.com.cofermeta.util;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class MensagemUtils {
    public static void disparaErro(String msg) throws Exception {
        StringBuffer mensagemTratada = new StringBuffer("");
        mensagemTratada.append("<font size='12'>");
        mensagemTratada.append("<b>").append("----------- Atenção -------------").append("</b>");
        mensagemTratada.append("<font>").append("\n\n");
        mensagemTratada.append("<font size='11'>");
        mensagemTratada.append(msg);
        mensagemTratada.append("<font>").append("\n");
        throw new Exception(mensagemTratada.toString());
    }

    public static void disparaMensagem(ContextoAcao contexto, String mensagem) throws Exception {
        String msgTratada = "<br><br><hr>" +
                "<b><span style=\"font-size: 1.2em\">" + mensagem + "</span></b><hr><br><br>";
        contexto.setMensagemRetorno(msgTratada);
    }

    public static void disparaMensagem(String s) {
    }
}
