/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
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
