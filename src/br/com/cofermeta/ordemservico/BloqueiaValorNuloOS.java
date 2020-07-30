package br.com.cofermeta.ordemservico;

import br.com.cofermeta.util.MensagemUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import java.util.Date;
public class BloqueiaValorNuloOS implements EventoProgramavelJava {

    public static final String ERRO_CAMPOS_OBRIGATORIOS =  "Erro a OS não foi salva porque campos obrigatórios não foram preenchidos.";

    public void bloqueiaCamposOS(PersistenceEvent persistenceEvent) throws Exception {

        try {
            DynamicVO bloqueiaOS = (DynamicVO) persistenceEvent.getVo();
            Date inicexecVO = bloqueiaOS.asTimestamp("INICEXEC");
            Date hriniciVO = bloqueiaOS.asTimestamp("HRINICIAL");
            Date hrfinalVO = bloqueiaOS.asTimestamp("HRFINAL");
            if (inicexecVO.toString() == null || hrfinalVO.toString() == null || hriniciVO.toString() == null) {

                MensagemUtils.disparaErro(ERRO_CAMPOS_OBRIGATORIOS);
            } else {
                return;
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
        bloqueiaCamposOS(persistenceEvent);
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
