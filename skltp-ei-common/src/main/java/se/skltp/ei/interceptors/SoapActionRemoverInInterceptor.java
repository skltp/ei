package se.skltp.ei.interceptors;

import static org.apache.cxf.binding.soap.SoapBindingConstants.SOAP_ACTION;

import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.EndpointSelectionInterceptor;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

@Log4j2
public class SoapActionRemoverInInterceptor extends AbstractSoapInterceptor {

  public SoapActionRemoverInInterceptor() {
    super(Phase.READ);
    addAfter(ReadHeadersInterceptor.class.getName());
    addAfter(EndpointSelectionInterceptor.class.getName());
  }

  public void handleMessage(SoapMessage message) {
    if (message.getVersion() instanceof Soap11) {
      Map<String, List<String>> headers = CastUtils.cast((Map) message.get(Message.PROTOCOL_HEADERS));
      if (headers != null) {
        List<String> sa = headers.get(SOAP_ACTION);
        if (sa != null && !sa.isEmpty()) {
          log.debug("SoapAction received: {}", sa.get(0));
        }
        headers.remove(SOAP_ACTION);
      }

    } else if (message.getVersion() instanceof Soap12) {
      log.debug("Soap12 is not supported now");
    }
  }
}