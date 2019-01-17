/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.intsvc.notification;

import javax.jws.WebService;

import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.expression.ExpressionEvaluator;
import org.mule.transformer.types.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;
import se.skltp.ei.intsvc.exception.CustomExceptionHandler;
import se.skltp.ei.svc.service.api.EiException;
import se.skltp.ei.svc.service.api.Header;
import se.skltp.ei.svc.service.api.ProcessInterface;

@WebService(
        serviceName = "ProcessNotificationResponderService",
        portName = "ProcessNotificationResponderPort",
        targetNamespace = "urn:riv:itintegration:engagementindex:ProcessNotification:1:rivtabp21")
public class NotificationWSBean implements ProcessNotificationResponderInterface, ExpressionEvaluator, MuleContextAware { //ExpressionEvaluator,

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(NotificationWSBean.class);

    private ProcessInterface blBean = null;

    private CustomExceptionHandler handler;
    private static final String NAME = "ei-evaluate-notify";
    public static String isError = "";

    public void setMuleContext(MuleContext muleContext) {
        handler = new CustomExceptionHandler(muleContext);
    }

    public String getName() {
        LOG.debug("Return evaluator name {}", NAME);
        return NAME;
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
    public ProcessNotificationResponseType processNotification(String logicalAddress, ProcessNotificationType parameters) {

        isError = "noError";
        ProcessNotificationResponseType response = new ProcessNotificationResponseType();
        try {
            // Validate the request (note no db-access will be performed)
            blBean.validateProcessNotification(new Header(null,logicalAddress,null), parameters);

        } catch (EiException e) {
            isError = "isError";
            // Create a error response
            handler.handleException(e);
            response.setComment(e.getMessage());
            response.setResultCode(ResultCodeEnum.ERROR);
            return response;
        }

        // Filter our own notifications if they are sent back from another EI instance
        parameters = blBean.filterProcessNotification(parameters);
        response.setComment(null);
        response.setResultCode(ResultCodeEnum.OK);
        isError = "noError";
        return response;
    }

    @Override
    public Object evaluate(String expression, MuleMessage message) {
        boolean notOk = isError.equals("isError");
        return notOk;
    }

    @Override
    public TypedValue evaluateTyped(String expression, MuleMessage message) {
        // TODO Auto-generated method stub
        return null;
    }
}