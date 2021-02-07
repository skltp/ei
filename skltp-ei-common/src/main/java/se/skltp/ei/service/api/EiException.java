/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.service.api;

import java.text.MessageFormat;

/**
 * Service model exception, typically raised when validation errors occurs. <p>
 *
 * Only to be instantiated from {@link EiErrorCodeEnum}.
 */
public class EiException extends RuntimeException {

  private static final long serialVersionUID = -1536440597887391129L;

  private final EiErrorCodeEnum code;
  private final Object[] messageArgs;

  /**
   * Creates an EiException.
   *
   * @param code the code
   * @param messageArgs the message format arguments matching the message format of the actual code
   */
  public EiException(EiErrorCodeEnum code, Object... messageArgs) {
    super((String) null);
    this.code = code;
    this.messageArgs = messageArgs;
  }

  /**
   /**
   * Creates an EiException.
   *
   * @param code the code
   * @param cause the causing exception
   * @param messageArgs the message format arguments matching the message format of the actual code
   */
  public EiException(EiErrorCodeEnum code, Throwable cause, Object... messageArgs) {
    super(null, cause);
    this.code = code;
    this.messageArgs = messageArgs;
  }

  /**
   * Returns the error code.
   *
   * @return the error code
   */
  public String getCode() {
    return code.getErrorCode();
  }

  @Override
  public String getMessage() {
    return String.format("%s: %s", code.getErrorCode(),
        MessageFormat.format(code.getMessageFormat(), messageArgs));
  }
}
