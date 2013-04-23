package se.skltp.ei.svc.service.api;

import java.text.MessageFormat;

public class EiException extends RuntimeException {

	private static final long serialVersionUID = -1536440597887391129L;

	private EiErrorCodeEnum code;
	private Object[] messageArgs;
	
	public EiException(EiErrorCodeEnum code, Object... messageArgs) {
		super((String)null);
		this.code = code;
		this.messageArgs = messageArgs;
	}
	
	public EiException(EiErrorCodeEnum code, Throwable cause, Object... messageArgs) {
		super(null, cause);
		this.code = code;
		this.messageArgs = messageArgs;
	}
	
	public String getCode() {
		return code.getErrorCode();
	}

	@Override
	public String getMessage() {
		StringBuffer sb = new StringBuffer(code.getErrorCode());
		String msg = MessageFormat.format(code.getMessageFormat(), messageArgs);
		sb.append(": ");
		sb.append(msg);
		return sb.toString();
	}
}
