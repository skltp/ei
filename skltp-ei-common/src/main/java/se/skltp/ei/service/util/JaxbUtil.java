/*
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.skltp.ei.service.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Node;


@Log4j2
public class JaxbUtil {

  private JAXBContext jaxbContext;

  // Lazy instantiation through getters
  private Map<String, Object> unmarshallProps = null;
  private Map<String, Object> marshallProps = null;


  @SuppressWarnings("rawtypes")
  public JaxbUtil(Class... classesToBeBound) {
    try {
			if (log.isDebugEnabled()) {
				log.debug("Load JAXBContext based on classes: " + Arrays.toString(classesToBeBound));
			}
      jaxbContext = JAXBContext.newInstance(classesToBeBound);
    } catch (JAXBException e) {
      throw new JaxbUtilException(e);
    }
  }

  public JAXBContext getContext() {
    return jaxbContext;
  }


  /**
   * Marshal a JAXB object to a XML-string
   *
   * @return the XML string
   */
  public String marshal(Object jaxbObject) {

    // Precondition, check that the jaxbContext is set!
    if (jaxbContext == null) {
      log.error("Trying to marshal with a null jaxbContext, returns null. Check your configuration, e.g. jaxb-transformers!");
      return null;
    }

    try {

      StringWriter writer = new StringWriter();
      Marshaller marshaller = jaxbContext.createMarshaller();
      if (marshallProps != null) {
        for (Entry<String, Object> entry : marshallProps.entrySet()) {
          marshaller.setProperty(entry.getKey(), entry.getValue());
        }
      }
      marshaller.marshal(jaxbObject, writer);

      String xml = writer.toString();

			if (log.isDebugEnabled()) {
				log.debug("marshalled jaxb object of type {}, returns xml: {}", jaxbObject.getClass().getSimpleName(), xml);
			}

      return xml;
    } catch (JAXBException e) {
      throw new JaxbUtilException(e);
    }
  }


  /**
   * Unmarshal a xml payload into a JAXB object. Removes any leading JAXBElement, happens typically when the JAXB class doesn't
   * contain any @XmlRootElement annotation Supports the following added types of xml payloads: String, and byte[] + all the
   * stardard types that the JAXB Unmarshaller supports
   */
  @SuppressWarnings("rawtypes")
  public Object unmarshal(Object payload) {

    // Precondition, check that the jaxbContext is set!
    if (jaxbContext == null) {
      log
          .error("Trying to unmarshal with a null jaxbContext, returns null. Check your configuration, e.g. jaxb-transformers!");
      return null;
    }

    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      if (unmarshallProps != null) {
        for (Entry<String, Object> entry : unmarshallProps.entrySet()) {
          unmarshaller.setProperty(entry.getKey(), entry.getValue());
        }
      }
      Object jaxbObject;
      if (payload instanceof String) {
        String src = (String)payload;
        jaxbObject = unmarshaller.unmarshal(new StringReader(src));
      } else if (payload instanceof Node) {
        jaxbObject = unmarshaller.unmarshal((Node) payload);
      } else {
        // Out of alternatives, have to throw a unknown source type exception...
        throw new JaxbUtilException("Unknown sourcetype of the xml payload: " + payload.getClass().getName());
      }

      // Unmarshal done, postprocess by replacing any JAXBElement with the actual jaxb-object, see comment in the class-doc.
      if (jaxbObject instanceof JAXBElement) {
				if (log.isDebugEnabled()) {
					log.debug("Found a JAXBElement, returns it value");
				}
        jaxbObject = ((JAXBElement) jaxbObject).getValue();
      }

      log.debug("unmarshalled xml payload of type: {}, returns jaxb object of type {}", payload.getClass().getName(),
          jaxbObject.getClass().getName());

      return jaxbObject;
    } catch (JAXBException e) {
      throw new JaxbUtilException(e);
    }
  }

  public class JaxbUtilException extends RuntimeException {

    public JaxbUtilException(Throwable cause) {
      super(cause);
    }

    public JaxbUtilException(String message) {
      super(message);
    }
  }

}
