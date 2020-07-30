package br.com.cofermeta.produtos;

import br.com.cofermeta.util.MensagemUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import groovy.sql.Sql;
import org.springframework.jdbc.core.JdbcOperations;
import sun.misc.MessageUtils;

import java.math.BigDecimal;

public class BloqueiaMedidasEstoque implements EventoProgramavelJava {

    // mensagem de erro
    private static final String ERRO_NAO_ACEITA_CAMPO_NULO = "Campos 'Peso Bruto', 'Peso Líquido', 'Altura, 'Largura' e 'Espessura/Profundidade' da aba Medidas e Estoque não podem ser nulos." ;

    public void BloqueiaCampos(PersistenceEvent persistenceEvent) throws Exception {
        try {
            DynamicVO produto = (DynamicVO) persistenceEvent.getVo();

            // campos que não podem ficar vazios
            BigDecimal pesobrutoVO = produto.asBigDecimal("Produto.PESOBRUTO");
            BigDecimal pesoliqVO = produto.asBigDecimal("Produto.PESOLIQ");
            BigDecimal alturaVO = produto.asBigDecimal("Produto.ALTURA");
            BigDecimal larguraVO = produto.asBigDecimal("Produto.LARGURA");
            BigDecimal espessuraVO = produto.asBigDecimal("Produto.ESPESSURA");

            //condicao: se o campo estiver vazio ocorre o erro
            if (pesobrutoVO.toString() == null || pesoliqVO.toString() == null || alturaVO.toString() == null || larguraVO.toString() == null || espessuraVO.toString() == null) {
                MensagemUtils.disparaErro(ERRO_NAO_ACEITA_CAMPO_NULO);
                // nao ocorre nada se estiverem preenchidos corretamente
            } else {
                return;
            }
        } catch(Exception e) {
            e.printStackTrace();
            MensagemUtils.disparaErro(ERRO_NAO_ACEITA_CAMPO_NULO);
        }
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        // não deixa salvar a aba de Medidas e Estoques (Cadastro de Produtos) sem preencher todos os campos
        BloqueiaCampos(persistenceEvent);
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

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
