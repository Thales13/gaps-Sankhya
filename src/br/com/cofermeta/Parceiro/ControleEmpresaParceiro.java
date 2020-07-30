package br.com.cofermeta.Parceiro;

import br.com.cofermeta.util.AcessoBanco;
import br.com.cofermeta.util.MensagemUtils;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.confirmacao.lote.ResumoConfirmacaoLote;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import org.apache.poi.hssf.record.PageBreakRecord;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class ControleEmpresaParceiro implements  EventoProgramavelJava {

    // constantes ==========================================================================
    private static final String ERRO_PARCEIRO_NAO_PODE_SER_IGUAL_EMPRESA = "O parceiro não pode ser igual a empresa";
    private static final String ERRO_NAO_SE_PODE_VENDER_DE_COFERMETA_PARA_COFERMETA = "A venda não pode ser feita de cofermeta para cofermeta";
    private static final String ERRO_TRANSFERENCIA_NAO_PODE_COFERMETA_PARA_ALOC_ADMIB_SORETI = "A transferencia não pode ser de cofermeta para aloc e nem para admib";
    private static final String ERRO_NOTA_DE_SERVICO_NAO_EMITE_SE_PARCEIRO_IGUAL_EMPRESA = "A nota de serviço não pode ser emitida se o parceiro for igual a empresa";
    // Verifica se o parceiro é igual a empresa

    public void parceiroIgualEmpresa(PersistenceEvent persistenceEvent) throws Exception {
        try {
            AcessoBanco acessoBanco = new AcessoBanco();
            DynamicVO parceiroVO = (DynamicVO) persistenceEvent.getVo();
            int parceiro = parceiroVO.asInt("CODPARC");
            int empresa = parceiroVO.asInt("CODEMP");
            // Consulta para CNPJ parceiro onde o parceiro for igual ao do cabeçalho
            ResultSet parceiroSub = acessoBanco.findOne("SELECT SUBSTRING(CGC_CPF, 0,9) AS CGC_CPF FROM TGFPAR WHERE CODPARC =" + parceiro);
            // Consulta para CNPJ empresa onde o empresa for igual ao do cabeçalho
            ResultSet empresaSub = acessoBanco.findOne("SELECT SUBSTRING(CGC, 0,9) AS CGC FROM TSIEMP WHERE CODEMP =" + empresa);
            Boolean parceiroEmpresa = empresaSub.getString(1).equals(parceiroSub.getString(1));
            if (empresa == parceiro) {
                MensagemUtils.disparaErro(ERRO_PARCEIRO_NAO_PODE_SER_IGUAL_EMPRESA);
            } else if (!parceiroEmpresa) {
                MensagemUtils.disparaErro(ERRO_TRANSFERENCIA_NAO_PODE_COFERMETA_PARA_ALOC_ADMIB_SORETI);
            } else {
                return;
            }
            acessoBanco.closeSession();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }
    //Verifica se a venda está tentando ser feita de cofermeta para cofermeta
    public void vendaCofermetaParaCofermeta(PersistenceEvent persistenceEvent) throws Exception {
        try {
            AcessoBanco acessoBanco = new AcessoBanco();
            DynamicVO parceiroVO = (DynamicVO) persistenceEvent.getVo();
            int parceiro = parceiroVO.asInt("CODPARC");
            int empresa = parceiroVO.asInt("CODEMP");
            // Consulta para CNPJ parceiro onde o parceiro for igual ao do cabeçalho
            ResultSet parceiroSub = acessoBanco.findOne("SELECT SUBSTRING(CGC_CPF, 0,9) AS CGC_CPF FROM TGFPAR WHERE CODPARC =" + parceiro);
            // Consulta para CNPJ empresa onde o empresa for igual ao do cabeçalho
            ResultSet empresaSub = acessoBanco.findOne("SELECT SUBSTRING(CGC, 0,9) AS CGC FROM TSIEMP WHERE CODEMP =" + empresa);
            Boolean parceiroEmpresa = empresaSub.getString(1).equals(parceiroSub.getString(1));
            if (parceiroEmpresa) {
                MensagemUtils.disparaErro(ERRO_NAO_SE_PODE_VENDER_DE_COFERMETA_PARA_COFERMETA);
            } else {
                return;
            }
            acessoBanco.closeSession();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public void verificaParceiroIgualEmpresaOS (PersistenceEvent persistenceEvent) throws Exception{
        try{
            AcessoBanco acessoBanco = new AcessoBanco();
            DynamicVO parceiroVO = (DynamicVO) persistenceEvent.getVo();
            int parceiro = parceiroVO.asInt("CODPARC");
            int empresa = parceiroVO.asInt("CODEMP");
            if (parceiro == empresa){
                MensagemUtils.disparaErro(ERRO_NOTA_DE_SERVICO_NAO_EMITE_SE_PARCEIRO_IGUAL_EMPRESA);
            }
            else{
                return;
            }
            acessoBanco.closeSession();
        }catch (Exception e){
            e.printStackTrace();
            throw  new  Exception(e.getMessage());
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

        try {

            if (DynamicEntityNames.CABECALHO_NOTA.equals(persistenceEvent.getEntity().getName())) {
                DynamicVO notaVO = (DynamicVO) persistenceEvent.getVo();
                // Somente as TOP's abaixo são válidadas
                int codtipoOper = notaVO.asInt("TipoOperacao.CODTIPOPER");
                if (codtipoOper == 3121 || codtipoOper == 3128 || codtipoOper == 3122 || codtipoOper == 3129) {

                    parceiroIgualEmpresa(persistenceEvent);

                    // Válidações de vendas de cofermeta para cofermeta
                } else if (codtipoOper == 3100 || codtipoOper == 3104 || codtipoOper == 3102 || codtipoOper == 3123 || codtipoOper == 3127) {

                    vendaCofermetaParaCofermeta(persistenceEvent);

                } else if (codtipoOper == 3220 || codtipoOper == 3238){
                    verificaParceiroIgualEmpresaOS(persistenceEvent);
                }
                else{
                    return;
                }
            }
        }catch (Exception e){
                e.printStackTrace();
                throw  new  Exception(e.getMessage());
        }
    }

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
}

