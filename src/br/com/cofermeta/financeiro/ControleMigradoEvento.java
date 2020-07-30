package br.com.cofermeta.financeiro;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class ControleMigradoEvento implements EventoProgramavelJava {
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO financeiroVO = (DynamicVO) persistenceEvent.getVo();
        if("S".equals(financeiroVO.asString("AD_MIGRADO")))
        financeiroVO.setProperty("AD_MIGRADO","N");
    }

    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
