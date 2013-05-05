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
