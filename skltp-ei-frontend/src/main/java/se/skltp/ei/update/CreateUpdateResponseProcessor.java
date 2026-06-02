/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v2.1 or later.
 * Please refer to the full license files at the project root.
 */
package se.skltp.ei.update;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;

@Service
public class CreateUpdateResponseProcessor implements Processor {

  @Override
  public void process(Exchange exchange) throws Exception {
    UpdateResponseType response= new UpdateResponseType();
    response.setResultCode(ResultCodeEnum.OK);
    response.setComment(null);
    exchange.getIn().setBody(response);
  }
}
