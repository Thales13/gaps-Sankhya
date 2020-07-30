package br.com.cofermeta.produtos;

import br.com.cofermeta.util.MensagemUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

import java.sql.ResultSet;




public class InsereEmpresasEstoquePorDivisao implements EventoProgramavelJava {

    JdbcWrapper jdbc;
    NativeSql sql;

    private void insereEmpresas (PersistenceEvent persistenceEvent) throws Exception {
        // Cria objeto produto
        DynamicVO produto = (DynamicVO)  persistenceEvent.getVo();

        // Atributo código do produto (Inteiro) do objeto produto

        int codprod = produto.asInt("CODPROD");

        // Novo acesso ao banco.

        sql = new NativeSql(jdbc);

        // Método usado para carregar o arquivo .sql com o select.

        sql.loadSql(InsereEmpresasEstoquePorDivisao.class,"sql/SelecionaEmpresasPorDivisao");

        // Seta um novo parametro nome "CODIVISAO" com o valor que será digitado no campo divisão no sankhya.

        sql.setNamedParameter("CODDIVISAO", produto.asInt("CODDIVISAO"));

        // Executa a QUERY carregada no método load.Sql.

        ResultSet rs = sql.executeQuery();

        // Percorre cada valor do select e insere ele no campo desejado
        while (rs.next()) {
            sql.cleanParameters();
            sql.setNamedParameter("CODEMP", rs.getBigDecimal("CODEMP"));
            sql.setNamedParameter("CODLOCAL", rs.getBigDecimal("LOCALPAD"));
            sql.setNamedParameter("CODPROD", codprod);
            sql.executeUpdate("INSERT INTO  TGFEST (CODEMP, CODLOCAL, CODPROD) VALUES (:CODEMP, :CODLOCAL, :CODPROD)");
        }

        jdbc.closeSession();
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        jdbc = persistenceEvent.getJdbcWrapper();
        try {
            insereEmpresas(persistenceEvent);
        } catch (Exception e) {
            e.printStackTrace();
            MensagemUtils.disparaErro(e.getMessage());
        } finally {
            jdbc.closeSession();
        }

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
