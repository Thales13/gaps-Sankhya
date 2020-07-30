package br.com.cofermeta.financeiro;

import br.com.cofermeta.util.AcessoBanco;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import com.sankhya.util.TimeUtils;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;


public class EstornoCaixa implements EventoProgramavelJava {

    @Override
    public void beforeUpdate(PersistenceEvent persistencia) throws Exception {
        if(DynamicEntityNames.FINANCEIRO.equals(persistencia.getEntity().getName())){
            boolean ehUsuarioCaixa = "S".equals(AuthenticationInfo.getCurrent().getUsuVO().getCAIXA());
            if(ehUsuarioCaixa){
                verificarSePodeEstornar(persistencia);
            }
        }
    }


    private void verificarSePodeEstornar(PersistenceEvent persistencia) throws Exception {
        DynamicVO financeiroVO = (DynamicVO) persistencia.getOldVO();
        Boolean estornando = JapeSession.getPropertyAsBoolean("mov.financeiro.estornando", Boolean.FALSE);
        if(!estornando)
            return;
        if("S".equals(financeiroVO.asString("AD_LIBERAESTORNO")))
            return;
        // verficar se o estorno pode ser feito
        podeEstornar(financeiroVO);

    }

    private void podeEstornar(DynamicVO financeiroVO) throws Exception {
        AcessoBanco acessoBanco = new AcessoBanco();
        try{
            SimpleDateFormat formatSomenteData = new SimpleDateFormat("dd-MM-yyyy");
            String dtBaixa = formatSomenteData.format(financeiroVO.asTimestamp("DHBAIXA"));
            String dtHoje = formatSomenteData.format(TimeUtils.getNow());

            // se data do estorno for igual a data de hoje, nao travar
            if (dtBaixa.equals(dtHoje))
                return;

            // se a data da baixa for igual a ultima de caixa aberta, não travar
            ResultSet rsUltimaCaixa = acessoBanco.findOne(SQL_GET_ULTIMO_CAIXA_FEITO_DIFERENTE_DE_HOJE);
            acessoBanco.closeSession();
            if (rsUltimaCaixa.next()){
                String dtUltimoCaixa = formatSomenteData.format(rsUltimaCaixa.getTimestamp(1));
                if(dtBaixa.equals(dtUltimoCaixa))
                    return;
            }

            throw new Exception(MENSAGEM_LIBERACAO);

        }catch (Exception e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

    }


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

    private final String MENSAGEM_LIBERACAO  = "" +
            "Necessária liberação do financeiro.\nCaixa já fechado.\n<b>Solicite Liberação do Financeiro.</b>";

    private final String SQL_GET_ULTIMO_CAIXA_FEITO_DIFERENTE_DE_HOJE = "" +
            "SELECT\n" +
            "  CAST(MAX(CAI.DTABERTURA) as DATE)\n" +
            "FROM\n" +
            "  TGFCAI CAI\n" +
            "WHERE\n" +
            "  CAST(CAI.DTABERTURA as DATE) < CAST(GETDATE() as DATE)\n" +
            "  AND CAST(CAI.DTABERTURA as DATE) = (\n" +
            "    SELECT\n" +
            "      CAST(MAX(CAI.DTABERTURA) as DATE)\n" +
            "    FROM\n" +
            "      TGFCAI CAI\n" +
            "    WHERE\n" +
            "      CAST(CAI.DTABERTURA as DATE) < CAST(GETDATE() as DATE)\n" +
            "  )";
}
