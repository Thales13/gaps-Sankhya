package br.com.cofermeta.Nota;

        import br.com.cofermeta.util.AcessoBanco;
        import br.com.cofermeta.util.MensagemUtils;
        import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
        import br.com.sankhya.jape.core.JapeSession;
        import br.com.sankhya.jape.event.PersistenceEvent;
        import br.com.sankhya.jape.event.TransactionContext;
        import br.com.sankhya.jape.vo.DynamicVO;
        import br.com.sankhya.modelcore.auth.AuthenticationInfo;
        import br.com.sankhya.modelcore.util.DynamicEntityNames;

        import java.sql.ResultSet;


public class PermissaoUsuarioEmitirNota implements EventoProgramavelJava {

    private void verificaSeUsuarioPodeEmitirNota(PersistenceEvent persistenceEvent) throws Exception {
        AcessoBanco acessoBanco = new AcessoBanco();
        Boolean confirmando = (Boolean) JapeSession.getProperty("CabecalhoNota.confirmando.nota", false);
        DynamicVO notaVO = (DynamicVO) persistenceEvent.getVo();
        if(confirmando || "L".equals(notaVO.asString("STATUSNOTA")) ){
            DynamicVO usuarioVO = (DynamicVO) persistenceEvent.getVo();
            int codUsuario = AuthenticationInfo.getCurrent().getUserID().intValue();
            ResultSet rsBuscaUsuario = acessoBanco.findOne("SELECT AD_PERMEMITIRNOTA FROM TSIUSU WHERE CODUSU ="+codUsuario);
            Boolean ehUsuarioPermiteEmitirNota = "S".equals(rsBuscaUsuario.getString(1));
            if(!ehUsuarioPermiteEmitirNota){
                MensagemUtils.disparaErro(ERRO_NAO_TEM_PERMISSAO_PARA_EMITIR_NOTA);
            }
        }

    }
    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        // não validar para o usuário SUP
        if(AuthenticationInfo.getCurrent().getUserID().intValue() == 0)
            return;

        // tratamentos para cabecalho da nota
        if(DynamicEntityNames.CABECALHO_NOTA.equals(persistenceEvent.getEntity().getName())) {
            DynamicVO notaVO = (DynamicVO) persistenceEvent.getVo();

            // somente venda eh validada
            if (!"V".equals(notaVO.asString("TIPMOV")))
                return;

            int codTipOper = notaVO.asInt("CODTIPOPER");
            if(codTipOper == 3297)
                return;

            if("A".equals(notaVO.asString("STATUSNFE")))
                return;

            if("A".equals(notaVO.asString("STATUSNFSE")))
                return;


            verificaSeUsuarioPodeEmitirNota(persistenceEvent);
        }
    }
    // constantes ==========================================================================
    private final String ERRO_NAO_TEM_PERMISSAO_PARA_EMITIR_NOTA =  " Este usuário logado não tem permissão para emitir nota fiscal. Favor procurar o seu Gerente. ";

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }
}

