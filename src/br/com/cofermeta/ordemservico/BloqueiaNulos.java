package br.com.cofermeta.ordemservico;

import br.com.cofermeta.util.AcessoBanco;
import br.com.cofermeta.util.MensagemUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import com.sankhya.util.TimeUtils;
import java.math.BigDecimal;
import java.sql.ResultSet;



public class BloqueiaNulos implements EventoProgramavelJava {

    private static final String ERRO_CAMPOS_NAO_PODEM_SER_NULOS = "Campos Data Execução, Hora Inicial e Hora Final não podem ser nulos";

    AcessoBanco acessoBanco;

    public void verificaNulosInsert(PersistenceEvent persistenceEvent) throws Exception {
        try{
            DynamicVO ordemInsert = (DynamicVO) persistenceEvent.getVo();

            BigDecimal nuitemaux = ordemInsert.asBigDecimal("NUMITEM");
            if (nuitemaux == BigDecimal.valueOf(1))
            return;

            acessoBanco = new AcessoBanco();

            BigDecimal numOS = ordemInsert.asBigDecimal("NUMOS");
            BigDecimal nuitem = ordemInsert.asBigDecimal("NUMITEM").subtract(BigDecimal.valueOf(1));

            ResultSet prochoraini = acessoBanco.findOne("SELECT HRINICIAL " +
                "FROM TCSITE " +
                "WHERE NUMOS = " + numOS +
                "AND NUMITEM = " + nuitem);
            ResultSet prochorafin = acessoBanco.findOne("SELECT HRFINAL " +
                "FROM TCSITE " +
                "WHERE NUMOS = " + numOS +
                "AND NUMITEM = " + nuitem);
            ResultSet iniciexec = acessoBanco.findOne("SELECT INICEXEC " +
                "FROM TCSITE " +
                "WHERE NUMOS = " + numOS +
                "AND NUMITEM = " + nuitem);
            if (prochoraini.getString(1) == null ||
                prochorafin.getString(1) == null ||
                iniciexec.getString(1) == null) {
            MensagemUtils.disparaErro(ERRO_CAMPOS_NAO_PODEM_SER_NULOS);
            }else {
            return;
            }

        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }
    public void insereBancoUpdate (PersistenceEvent persistenceEvent) throws Exception {
        try {
            DynamicVO ordemVO = (DynamicVO) persistenceEvent.getVo();
            acessoBanco = new AcessoBanco();
            ResultSet rs = acessoBanco.findOne("SELECT CASE WHEN LEN(DATEPART(HOUR,GETDATE())) = 1 \n" +
                    "THEN concat(0,DATEPART(HOUR,GETDATE()), DATEPART(minute, getdate()))\n" +
                    "WHEN LEN(DATEPART(MINUTE,GETDATE())) = 1 \n" +
                    "THEN concat(DATEPART(HOUR,GETDATE()),0,DATEPART(minute, getdate()))\n" +
                    "WHEN LEN(DATEPART(HOUR,GETDATE())) = 1 AND LEN(DATEPART(MINUTE,GETDATE())) = 1 \n" +
                    "THEN concat(0,DATEPART(HOUR,GETDATE()),0,DATEPART(minute, getdate())) \n" +
                    "ELSE concat(DATEPART(HOUR,GETDATE()), DATEPART(MINUTE, GETDATE())) END");

            ordemVO.setProperty("HRINICIAL", rs.getBigDecimal(1));
            ordemVO.setProperty("HRFINAL", rs.getBigDecimal(1));
            ordemVO.setProperty("INICEXEC", TimeUtils.getNow());
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        verificaNulosInsert(persistenceEvent);
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        insereBancoUpdate(persistenceEvent);
    }

    // Métodos não utilizados.
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
