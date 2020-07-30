package br.com.cofermeta.ControleCaixa;

import br.com.cofermeta.util.MensagemUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

public class ControleAlteracoesFinanceirasNaNota implements EventoProgramavelJava {

    // regras de negocio ================================================================
    private void verificarSePodeFaturarAvista(PersistenceEvent persistenceEvent) throws Exception {
        Boolean confirmando = (Boolean) JapeSession.getProperty("CabecalhoNota.confirmando.nota", false);
        DynamicVO notaVO = (DynamicVO) persistenceEvent.getVo();
        if(confirmando || "L".equals(notaVO.asString("STATUSNOTA"))){
            int subTipoVenda = Integer.parseInt(notaVO.asString("TipoNegociacao.SUBTIPOVENDA"));
            // se for a vista ou cartão
            if( subTipoVenda == 1 || subTipoVenda == 6 || subTipoVenda == 7){
                boolean ehUsuarioCaixa = "S".equals(AuthenticationInfo.getCurrent().getUsuVO().getCAIXA());
                if(!ehUsuarioCaixa){
                    MensagemUtils.disparaErro(ERRO_NAO_PODE_FATURAR_FINANCEIRO_AVISTA);
                }
            }
        }
    }

    private void verificarSePodeAlterarTipoNegociacao(PersistenceEvent persistenceEvent) throws Exception {
        ModifingFields modifingFields = persistenceEvent.getModifingFields();
        if(modifingFields.isModifing("CODTIPVENDA")){
            DynamicVO notaVO = (DynamicVO) persistenceEvent.getVo();
            int subTipoVenda = Integer.parseInt(notaVO.asString("TipoNegociacao.SUBTIPOVENDA"));
            // se for a vista ou cartão
            if( subTipoVenda == 1 || subTipoVenda == 6 || subTipoVenda == 7){
                boolean ehUsuarioCaixa = "S".equals(AuthenticationInfo.getCurrent().getUsuVO().getCAIXA());
                if(!ehUsuarioCaixa){
                    MensagemUtils.disparaErro(ERRO_SOMENTE_CAIXA_PODE_MUDAR_NEGOCIACAO_AVISTA);
                }
            }
        }
    }

    private void verificarSePodeAlterarTipoTitulo(PersistenceEvent persistenceEvent) throws Exception {
        ModifingFields modifingFields = persistenceEvent.getModifingFields();
        if(modifingFields.isModifing("CODTIPTIT")){
            DynamicVO notaVO = (DynamicVO) persistenceEvent.getVo();
            int subTipoVenda = Integer.parseInt(notaVO.asString("TipoTitulo.SUBTIPOVENDA"));
            // se for a vista ou cartão
            if( subTipoVenda == 1 || subTipoVenda == 7 || subTipoVenda == 8){
                boolean ehUsuarioCaixa = "S".equals(AuthenticationInfo.getCurrent().getUsuVO().getCAIXA());
                if(!ehUsuarioCaixa){
                   MensagemUtils.disparaErro(ERRO_SOMENTE_CAIXA_PODE_MUDAR_NEGOCIACAO_AVISTA);

                }
            }
        }

    }

    // eventos utilizados ================================================================
    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        // não validar para o usuário SUP
        if(AuthenticationInfo.getCurrent().getUserID().intValue() == 0)
            return;

        // tratamentos para cabecalho da nota
        if(DynamicEntityNames.CABECALHO_NOTA.equals(persistenceEvent.getEntity().getName())){
            DynamicVO notaVO = (DynamicVO) persistenceEvent.getVo();

            // somente venda eh validada
            if(!"V".equals(notaVO.asString("TIPMOV")))
                return;

            int atualFin = notaVO.asInt("TipoOperacao.ATUALFIN");
            if(atualFin == 0)
                return;

            // somente tipo de negociação diferente de OP 100% eh validada (Pedido da Camila Brenck 26-03-2020)
            int codtipVenda = notaVO.asInt( "TipoNegociacao.CODTIPVENDA");
            if(codtipVenda == 160)
                return;

            // somente tipo de negociação diferente de OP parcial eh validada (Pedido da Camila Brenck 13-05-2020)
            if(codtipVenda == 153)
                return;

            int codtipoOper = notaVO.asInt( "TipoOperacao.CODTIPOPER");
            if(codtipoOper == 3249 || codtipoOper == 3234 )
                return;

            if("A".equals(notaVO.asString("STATUSNFE")))
                return;

            if("A".equals(notaVO.asString("STATUSNFSE")))
                return;

            verificarSePodeAlterarTipoNegociacao(persistenceEvent);
            verificarSePodeFaturarAvista(persistenceEvent);
        }

        // tratamentos para o financeiro
        if(DynamicEntityNames.FINANCEIRO.equals(persistenceEvent.getEntity().getName())){
            DynamicVO financeiroVO = (DynamicVO) persistenceEvent.getVo();

            // alterações de tipo de titulo somente serão permitidas se o documento fiscal estiver aprovado
            // caso contrário, somente o caixa pode modificar
            if(!"E".equals(financeiroVO.asString("ORIGEM")))
                return;

            // somente venda eh validada
            if(!"V".equals(financeiroVO.asString("TipoOperacao.TIPMOV")))
                return;

            // somente tipo de negociação diferente de OP 100% eh validada (Pedido da Camila Brenck 26-03-2020)
        //    int codtipVendaFin = financeiroVO.asInt( "TipoNegociacao.CODTIPVENDA");
        //    if(codtipVendaFin == 160)
        //        return;

            int atualFin = financeiroVO.asInt("TipoOperacao.ATUALFIN");
            if(atualFin == 0)
                return;

            if("A".equals(financeiroVO.asString("CabecalhoNota.STATUSNFE")))
                return;

            if("A".equals(financeiroVO.asString("CabecalhoNota.STATUSNFSE")))
                return;

            verificarSePodeAlterarTipoTitulo(persistenceEvent);
        }

    }

    // constantes ==========================================================================
    private final String ERRO_NAO_PODE_FATURAR_FINANCEIRO_AVISTA =  " Este pedido só pode ser faturado no caixa, pois a negociação é a vista. Solicite o faturamento pelo caixa ou retorne o pedido de venda para o vendedor. ";
    private final String ERRO_SOMENTE_CAIXA_PODE_MUDAR_NEGOCIACAO_AVISTA =  " Somente usuário caixa pode alterar o tipo de negociação e/ou tipo de título em um movimento de venda.\n Retorne o pedido de venda para o vendedor ou solicite alteração do caixa. ";

    // eventos não utilizados ================================================================

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

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
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
