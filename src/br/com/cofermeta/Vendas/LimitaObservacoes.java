package br.com.cofermeta.Vendas;

import br.com.cofermeta.util.MensagemUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;

public class LimitaObservacoes implements EventoProgramavelJava {

    public void limitaCampoObservacao(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO libVO = (DynamicVO) persistenceEvent.getVo();
        BigDecimal chave = libVO.asBigDecimal("NUCHAVE");
        String observacao = libVO.asString("OBSERVACAO");
        if(observacao.length() > 180)  {
            MensagemUtils.disparaErro("Campo Observação está limitado a 180 caracteres.<br />Voce digitou " + observacao + " caracteres!<br />Reduza o tamanho do texto!");
        }else{
            return;
        }
    }

    public void validaNulo(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO vo = (DynamicVO) persistenceEvent.getVo();
        try {
            String observa = vo.asString("OBSERVACAO");
            if (observa == null) {
                return;
            } else {
                limitaCampoObservacao(persistenceEvent);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
    }
    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        validaNulo(persistenceEvent);
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