/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.prototype;

import java.io.IOException;
import java.util.Random;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
import org.apache.xpath.CachedXPathAPI;
import org.openengsb.prototype.Router.Location;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @org.apache.xbean.XBean element="prototypeEndpoint"
 *                         description="Prototype Component" The only
 *                         Prototype-Domain-Endpoint. This Endpoint is
 *                         responsible for forwarding all requests to an actual
 *                         connector, based on an id and a lookup-table
 */
public class PrototypeEndpoint extends ProviderEndpoint {
    private static final String ID_XPATH = "./@id";
    private static final String ROOT_XPATH = ".";

    private static final CachedXPathAPI XPATH = new CachedXPathAPI();

    private Log logger = LogFactory.getLog(getClass());

    private Router router = new Router();

    public PrototypeEndpoint() {
        logger.info("CONSTRUCTOR");
    }

    public void setTest(String x) {
        System.out.println("WUUHUHU " + x);
    }

    public String getTest() {
        return "ola";
    }

    @Override
    protected void processInOnly(MessageExchange exchange, NormalizedMessage in) throws Exception {
        // simply forward to processInOutRequest; We'll distinguish between
        // InOut and InOnly later, based on the value of the out-message (i.e.
        // null or not-null)
        logger.info("ENTER processInOnlyRequest");
        processInOut(exchange, in, null);
    }

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        logger.info("ENTER processInOutRequest");
        // 1. get id and message/payload from normalizedMessage
        Node idNode = extractSingleNode(in, PrototypeEndpoint.ID_XPATH);
        Node rootNode = extractSingleNode(in, PrototypeEndpoint.ROOT_XPATH);
        Node commandNode = null;
        if (rootNode != null) {
            commandNode = rootNode.getFirstChild();
        }

        String id = null;
        if (idNode != null) {
            id = idNode.getNodeValue();
        }

        // 2. verify parameters
        if (rootNode == null) {
            throw new IllegalArgumentException("Missing in-message.");
        }

        if (id == null) {
            throw new IllegalArgumentException("Missing id.");
        }

        if (commandNode == null) {
            throw new IllegalArgumentException("Missing scm-command.");
        }

        String newId = String.valueOf(new Random().nextInt(2) + 1);

        logger.info("Got id " + id + " but use " + newId);

        id = newId;

        // 3. look up service and namespace from lookuptable
        Location location = router.resolve(id);

        String serviceName = location.getService();
        String namespace = location.getNamespace();

        if (serviceName == null) {
            throw new RuntimeException("Could not find mapping for id " + id);
        }

        // 4. set up and perform call
        if (out == null) {// we are actually processing a inOnly-MEP
            RobustInOnly inOnly = createRobustInOnlyMessage(serviceName, namespace, commandNode);
            getChannel().sendSync(inOnly);
            passOnErrors(exchange, inOnly);
        } else { // we are actually processing an InOut-MEP
            InOut inOut = createInOutMessage(serviceName, namespace, commandNode);
            getChannel().sendSync(inOut);
            out.setContent(inOut.getOutMessage().getContent());
            passOnErrors(exchange, inOut);
        }
    }

    /**
     * Method that acts accordingly to the Error-status of the out-Message
     * returned by the connector. This needs to be done in order to notify the
     * callee of errors. By examination of the servicemix-sourcecode, we are
     * required to simple set a fault, but (re-)throw an error, to handle this
     * appropriately.
     * 
     * @param callerExchange The caller's out-message
     * @param calleeExchange The callee's out-message
     * @throws Exception
     */
    private void passOnErrors(MessageExchange callerExchange, MessageExchange calleeExchange) throws Exception {
        logger.info("ENTER passOnErrors");
        if (calleeExchange.getStatus() == ExchangeStatus.ERROR) {
            callerExchange.setStatus(ExchangeStatus.ERROR);
        }

        if (calleeExchange.getFault() != null) {
            callerExchange.setFault(calleeExchange.getFault());
        }

        if (calleeExchange.getError() != null) {
            throw calleeExchange.getError();
        }
    }

    /**
     * Creates and configures a new Message-Object for the In-Out-MEP
     * 
     * @param service The configured entpoint's name as noted in the SU
     * @param namespace The service's namespace that is used in the SU
     * @param message The actual message as xml-String
     * @return The new and configured InOut-Message-Object
     * @throws MessagingException should something go wrong
     */
    private InOut createInOutMessage(String service, String namespace, Node message) throws MessagingException {
        logger.info("ENTER createInOutMessage");
        NormalizedMessage inMessage = new NormalizedMessageImpl();
        inMessage.setContent(new DOMSource(message));

        InOut inOut = getExchangeFactory().createInOutExchange();
        inOut.setService(new QName(namespace, service));
        inOut.setInMessage(inMessage);

        return inOut;
    }

    /**
     * Creates and configures a new Message-Object for the Out-Only-MEP
     * 
     * @param client The client used to create the empty Message-Object
     * @param service The configured entpoint's name as noted in the SU
     * @param namespace The namespace that is used in the SU
     * @param message The actual message as xml-String
     * @return The new and configured In-Only-Message-Object
     * @throws MessagingException should something go wrong
     */
    private RobustInOnly createRobustInOnlyMessage(String service, String namespace, Node message)
            throws MessagingException {
        logger.info("ENTER createRobustInOnlyMessage");
        NormalizedMessage inMessage = new NormalizedMessageImpl();
        inMessage.setContent(new DOMSource(message));

        RobustInOnly robustInOnly = getExchangeFactory().createRobustInOnlyExchange();
        robustInOnly.setService(new QName(namespace, service));
        robustInOnly.setInMessage(inMessage);

        return robustInOnly;
    }

    protected Node extractSingleNode(NormalizedMessage inMessage, String xPath) throws MessagingException,
            TransformerException, ParserConfigurationException, IOException, SAXException {
        logger.info("ENTER extractSingleNode");
        Node rootNode = getRootNode(inMessage);
        if (rootNode == null) {
            return null;
        } else {
            return XPATH.selectSingleNode(rootNode, xPath);
        }
    }

    private Node getRootNode(NormalizedMessage message) throws ParserConfigurationException, IOException, SAXException,
            TransformerException {
        logger.info("ENTER getRootNode");
        SourceTransformer sourceTransformer = new SourceTransformer();
        DOMSource messageXml = sourceTransformer.toDOMSource(message.getContent());

        Node rootNode = messageXml.getNode();

        if (rootNode instanceof Document) {
            return rootNode.getFirstChild();
        } else {
            return rootNode;
        }
    }
}
