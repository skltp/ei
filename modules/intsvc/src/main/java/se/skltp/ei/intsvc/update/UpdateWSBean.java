package se.skltp.ei.intsvc.update;

import javax.jws.WebService;

import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.expression.ExpressionEvaluator;
import org.mule.transformer.types.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.skltp.ei.intsvc.exception.CustomExceptionHandler;
import se.skltp.ei.svc.service.api.EiException;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.ProcessInterface;

@WebService(
        serviceName = "UpdateResponderService",
        portName = "UpdateResponderPort",
        targetNamespace = "urn:riv:itintegration:engagementindex:Update:1:rivtabp21")
public class UpdateWSBean implements UpdateResponderInterface, ExpressionEvaluator, MuleContextAware {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(UpdateWSBean.class);
    private static final String NAME = "ei-evaluate-update";
    private String owner;
    private ProcessInterface blBean = null;
    public static String isError = "";
    private CustomExceptionHandler handler;

    public void setMuleContext(MuleContext muleContext) {
        handler = new CustomExceptionHandler(muleContext);
    }

    public String getName() {
        LOG.debug("Return evaluator name {}", NAME);
        return NAME;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setBlBean(ProcessInterface blBean) {
        this.blBean = blBean;
    }

    /**
     *
     * @param logicalAddress
     * @param parameters
     * @return
     */
    @Override
    public UpdateResponseType update(String logicalAddress, UpdateType parameters) {
        isError = "";
        UpdateResponseType response;
        response = new UpdateResponseType();
        try {
            // Validate the request (note no db-access will be performed)
            blBean.validateUpdate(new Header(null,logicalAddress,null), parameters);

        } catch (EiException e) {
            // Create a error response
            handler.handleException(e);
            isError = "isError";
            response.setComment(e.getMessage());
            response.setResultCode(ResultCodeEnum.ERROR);
            return response;
        }
        // Create a default response
        // According to R6 owner should always be set to owner of the index
        for (EngagementTransactionType ett : parameters.getEngagementTransaction()) {
            ett.getEngagement().setOwner(owner);
        }
        response.setComment(null);
        response.setResultCode(ResultCodeEnum.OK);
        isError = "noError";
        return response;
    }

    @Override
    public Object evaluate(String expression, MuleMessage message) {
        return isError.equals("noError");
    }

    @Override
    public TypedValue evaluateTyped(String expression, MuleMessage message) {
        // TODO Auto-generated method stub
        return null;
    }
}
