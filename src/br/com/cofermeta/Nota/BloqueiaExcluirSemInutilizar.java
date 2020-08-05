package br.com.cofermeta.Nota;

import br.com.cofermeta.util.MensagemUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class BloqueiaExcluirSemInutilizar implements EventoProgramavelJava {
    JdbcWrapper jdbc;
    NativeSql sql;

    //campo estático definindo o erro
    private static final String ERRO_NAO_EXCLUI_SEM_INUTILIZACAO = "Não é possível excluir a nota sem fazer a inutilização";

    //criação de método que impede a exclusão da nota caso ainda não tenha sido inutilizada
    public Boolean estaInutilizada (BigDecimal numNota, BigDecimal codEmp) throws Exception{
        sql = new NativeSql(jdbc);
        sql.appendSql("SELECT 1 FROM TGFINU WHERE NUMNOTA = :NUMNOTA AND CODEMP = :CODEMP");
        sql.setNamedParameter("NUMNOTA",numNota);
        sql.setNamedParameter("CODEMP",codEmp);
        ResultSet rs = sql.executeQuery();
        if (rs.next()){
            return true;
        }
        return false;
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
        //Chama o método
        DynamicVO nota = (DynamicVO)persistenceEvent.getVo();
        jdbc = persistenceEvent.getJdbcWrapper();

        //validação da inutilização da nota
        if(estaInutilizada(nota.asBigDecimal("NUMNOTA"),nota.asBigDecimal("CODEMP")) == false ) {
            MensagemUtils.disparaErro(ERRO_NAO_EXCLUI_SEM_INUTILIZACAO);
        } else {
            return;
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
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
