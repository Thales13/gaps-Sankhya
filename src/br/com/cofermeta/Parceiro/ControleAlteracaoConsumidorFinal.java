package br.com.cofermeta.Parceiro;

import br.com.cofermeta.util.MensagemUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.confirmacao.lote.ResumoConfirmacaoLote;

import java.math.BigDecimal;

public class ControleAlteracaoConsumidorFinal implements EventoProgramavelJava {

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        // não validar para o usuário SUP
        if(AuthenticationInfo.getCurrent().getUserID().intValue() == 0){
            return;
        }
        DynamicVO parceiroVO = (DynamicVO) persistenceEvent.getVo();
        if (parceiroVO.asBigDecimal("CODPARC").compareTo(BigDecimal.valueOf(2611)) == 0){
            MensagemUtils.disparaErro(ERRO_NAO_PODE_ALTERAR_PARCEIRO_CONSUMIDOR_FINAL);
        }

    }
    // constantes ==========================================================================
    private final String ERRO_NAO_PODE_ALTERAR_PARCEIRO_CONSUMIDOR_FINAL =  " O parceiro consumidor final, não pode ser alterado. Somente o CPD consegue alterar o mesmo. ";
}
