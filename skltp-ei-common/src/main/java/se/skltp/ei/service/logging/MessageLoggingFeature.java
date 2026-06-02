/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.service.logging;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

public class MessageLoggingFeature extends AbstractFeature {

  private MessageInInterceptor loggingInInterceptor;
  private MessageOutInterceptor loggingOutInterceptor;


  public MessageLoggingFeature(int maxPayloadSize, String loggerName, String componentId) {
    MessageLogEventSender sender = new MessageLogEventSender(loggerName, componentId);
    loggingInInterceptor = new MessageInInterceptor(sender);
    loggingOutInterceptor = new MessageOutInterceptor(sender);

    setLimit(maxPayloadSize);
  }

  @Override
  protected void initializeProvider(InterceptorProvider provider, Bus bus) {
    provider.getInInterceptors().add(this.loggingInInterceptor);
    provider.getInFaultInterceptors().add(this.loggingInInterceptor);
    provider.getOutInterceptors().add(this.loggingOutInterceptor);
    provider.getOutFaultInterceptors().add(this.loggingOutInterceptor);
  }

  public void setLimit(int limit) {
    this.loggingInInterceptor.setLimit(limit);
    this.loggingOutInterceptor.setLimit(limit);
  }

}