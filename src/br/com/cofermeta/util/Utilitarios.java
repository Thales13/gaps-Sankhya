package br.com.cofermeta.util;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.jape.dao.EntityPropertyDescriptor;
import br.com.sankhya.jape.dao.PersistentObjectUID;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.LiberacaoSolicitada;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import com.sankhya.util.BigDecimalUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilitarios {

    /**
     * @param cabVO cabecalho do movimento
     * @return ATUALESTOQUE da top em questão preparado para TGFITE
     */
    public static BigDecimal getAtualEstoque(DynamicVO cabVO){
        boolean atualizaSoNaConfirmacao = "S".equals(cabVO.asString("TipoOperacao.ADIARATUALEST"));
        boolean ehCompra = "C".equals(cabVO.asString("TIPMOV"));
        if(atualizaSoNaConfirmacao && !ehCompra)
            return BigDecimal.valueOf(0);

        switch (cabVO.asString("TipoOperacao.ATUALEST")){
            case "B":
                return BigDecimal.valueOf(-1);
            case "E":
                return BigDecimal.valueOf(1);
            default:
                return BigDecimal.valueOf(0);
        }

    }



    public static void adicionarFilaEmail(String assunto, String mensagem,String destinatarios) throws Exception {
        JapeWrapper emailDAO = JapeFactory.dao("MSDFilaMensagem");
        Timestamp dtAgora = new Timestamp(System.currentTimeMillis());
        String emails[] = destinatarios.split(";");
        BigDecimal smtp = (BigDecimal) MGECoreParameter.getParameter("BH_CONSMTPPP");
        for (String email : emails) {
            FluidCreateVO creEmail = emailDAO.create();
            creEmail.set("CODCON", BigDecimal.ZERO);
            creEmail.set("CODSMTP", smtp);
            creEmail.set("EMAIL", email);
            creEmail.set("ASSUNTO", assunto);
            creEmail.set("MENSAGEM", mensagem.toCharArray());
            creEmail.set("DTENTRADA", dtAgora);
            creEmail.set("STATUS", "Pendente");
            creEmail.set("TIPOENVIO", "E");
            creEmail.set("MAXTENTENVIO", BigDecimalUtil.valueOf(3L));
            creEmail.save();
        }
    }

    public static Boolean emailValido(String email){
        boolean isEmailIdValid = false;
        if (email != null && email.length() > 0 && email.contains("@")) {
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            if (matcher.matches()) {
                isEmailIdValid = true;
            }
        }
        return isEmailIdValid;
    }

    public static Timestamp getDataMaxTipoOper(BigDecimal codTipOper) throws Exception {
        AcessoBanco acessoBanco = new AcessoBanco();
        try{
            return acessoBanco.findOne("SELECT MAX(DHALTER) AS DT FROM TGFTOP WHERE CODTIPOPER = ? ",codTipOper)
                    .getTimestamp("DT");
        }finally {
            acessoBanco.closeSession();
        }
    }

    public static Object isNull(Object valor, Object seNulo) {
        if (valor == null)
            return seNulo;
        return valor;
    }

    public static void confirmarNota(BigDecimal nuNota) throws Exception {
        String toResult="";
        CACHelper cacHelper = new CACHelper();

        BarramentoRegra barramento = BarramentoRegra.build(CACHelper.class,
                "regrasConfirmacaoCAC.xml", AuthenticationInfo.getCurrent());
        cacHelper.confirmarNota(nuNota, barramento, false);


        if (barramento.getLiberacoesSolicitadas().size() == 0 &&
                barramento.getErros().size() == 0) {
            System.out.println("Nota Confirmada " + nuNota + "");

        } else {
            if (barramento.getErros().size() > 0) {
                System.out.println("Erro na confirma��o " +
                        nuNota);

                for (Exception e : barramento.getErros()) {
                    toResult =
                            e.getMessage();
                    break;
                }
            }

            if (barramento.getLiberacoesSolicitadas().size() > 0) {
                System.out.println("Erro na confirma��o " + nuNota
                        + ". Foi solicitada libera��es");
                toResult = "Libera��es solicitadas - \n";
                for (LiberacaoSolicitada e :
                        barramento.getLiberacoesSolicitadas()) {
                    toResult += "Evento: "
                            + e.getEvento() + (e.getDescricao() != null ? " Descri��o:  "
                            + e.getDescricao() + "\n" : "\n");
                    break;
                }

            }

        }
        System.out.println(toResult);
    }

    public static DynamicVO duplicaRegistroVO(DynamicVO voOrigem, String entidade) throws Exception {
        return duplicaRegistroVO( voOrigem,  entidade, null);
    }

    public static DynamicVO duplicaRegistroVO(DynamicVO voOrigem, String entidade, Map<String, Object> map) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        EntityDAO rootDAO = dwfFacade.getDAOInstance(entidade);
        DynamicVO destinoVO = voOrigem.buildClone();
        limparPk(destinoVO, rootDAO);
        if (map != null)
            for (String campo : map.keySet())
                destinoVO.setProperty(campo, map.get(campo));
        PersistentLocalEntity createEntity = dwfFacade.createEntity(entidade, (EntityVO) destinoVO);
        DynamicVO save = (DynamicVO) createEntity.getValueObject();
        return save;
    }

    public static DynamicVO duplicaRegistroSemLimparPKVO(DynamicVO voOrigem, String entidade, Map<String, Object> map) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        EntityDAO rootDAO = dwfFacade.getDAOInstance(entidade);
        DynamicVO destinoVO = voOrigem.buildClone();
        if (map != null)
            for (String campo : map.keySet())
                destinoVO.setProperty(campo, map.get(campo));
        PersistentLocalEntity createEntity = dwfFacade.createEntity(entidade, (EntityVO) destinoVO);
        DynamicVO save = (DynamicVO) createEntity.getValueObject();
        return save;
    }

    private static void limparPk(DynamicVO vo, EntityDAO rootDAO) throws Exception {
        PersistentObjectUID objectUID = rootDAO.getSQLProvider().getPkObjectUID();
        EntityPropertyDescriptor[] pkFields = objectUID.getFieldDescriptors();
        for (EntityPropertyDescriptor pkField : pkFields) {
            vo.setProperty(pkField.getField().getName(), null);
        }
    }

    public static void recalculaImpostosNota(BigDecimal nuNota) throws Exception {
        ImpostosHelpper impostohelp = new ImpostosHelpper();
        impostohelp.setForcarRecalculo(true);
        impostohelp.setSankhya(false);
        impostohelp.calcularImpostos(nuNota);
    }

    public static Map<String, String> getCamposEntidade(String entidade) throws Exception {
        AcessoBanco banco = new AcessoBanco();
        Map<String, String> retorno = new HashMap<>();
        try {
            ResultSet rs = banco.find("SELECT NOMECAMPO, DESCRCAMPO FROM TDDCAM WHERE NOMETAB = ? ", entidade);
            while (rs.next()) {
                retorno.put(rs.getString(1), rs.getString(2));
            }
        } finally {
            banco.closeSession();
            return retorno;
        }
    }

    public static Timestamp getDataMaxTipoOper(int codtipvenda) throws Exception {
        return getDataMaxTipoOper(BigDecimal.valueOf(codtipvenda));
    }

    public static BigDecimal getBigDecimal(Object value ) {
        BigDecimal ret = null;
        if( value != null ) {
            if( value instanceof BigDecimal ) {
                ret = (BigDecimal) value;
            } else if( value instanceof String ) {
                ret = new BigDecimal( (String) value );
            } else if( value instanceof BigInteger) {
                ret = new BigDecimal( (BigInteger) value );
            } else if( value instanceof Number ) {
                ret = new BigDecimal( ((Number)value).doubleValue() );
            } else {
                throw new ClassCastException("Not possible to coerce ["+value+"] from class "+value.getClass()+" into a BigDecimal.");
            }
        }
        return ret;
    }
}
