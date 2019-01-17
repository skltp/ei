package se.skltp.ei.intsvc.exception;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.soitoolkit.commons.mule.error.ServiceExceptionStrategy;

import se.skltp.ei.svc.service.api.EiException;

public class CustomExceptionHandler extends ServiceExceptionStrategy {

	public CustomExceptionHandler(MuleContext context) {
		super(context);
	}	
	
	public MuleEvent handleException(Exception e) {
		if (e instanceof EiException) {
			super.logException(e);
		} else {
			super.handleException(e, null);
		}
		return null;
	}
}
