/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.swssf.policy.assertionStates;

import org.apache.ws.secpolicy.AssertionState;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.ws.secpolicy.model.SignedElements;
import org.apache.ws.secpolicy.model.XPath;
import org.swssf.policy.Assertable;
import org.swssf.wss.securityEvent.SecurityEvent;
import org.swssf.wss.securityEvent.SignedElementSecurityEvent;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SignedElementsAssertionState extends AssertionState implements Assertable {

    private List<QName> elements = new ArrayList<QName>();

    public SignedElementsAssertionState(AbstractSecurityAssertion assertion, boolean asserted) {
        super(assertion, asserted);

        if (assertion instanceof SignedElements) {
            SignedElements signedElements = (SignedElements) assertion;
            for (int i = 0; i < signedElements.getXPaths().size(); i++) {
                XPath xPath = signedElements.getXPaths().get(i);
                String[] xPathElements = xPath.getXPath().split("/");
                String[] xPathElement = xPathElements[xPathElements.length - 1].split(":");
                if (xPathElement.length == 2) {
                    String ns = xPath.getPrefixNamespaceMap().get(xPathElement[0]);
                    if (ns == null) {
                        throw new IllegalArgumentException("Namespace not declared");
                    }
                    elements.add(new QName(ns, xPathElement[1]));
                } else {
                    elements.add(new QName(xPathElement[1]));
                }
            }
        }
    }

    @Override
    public SecurityEvent.Event[] getSecurityEventType() {
        return new SecurityEvent.Event[]{
                SecurityEvent.Event.SignedElement
        };
    }

    public void addElement(QName element) {
        this.elements.add(element);
    }

    @Override
    public boolean assertEvent(SecurityEvent securityEvent) throws WSSPolicyException {
        SignedElementSecurityEvent signedElementSecurityEvent = (SignedElementSecurityEvent) securityEvent;
        //todo better matching until we have a streaming xpath evaluation engine (work in progress)

        for (int i = 0; i < elements.size(); i++) {
            QName qName = elements.get(i);
            if (qName.equals(signedElementSecurityEvent.getElement())) {
                if (signedElementSecurityEvent.isSigned()) {
                    setAsserted(true);
                    return true;
                } else {
                    //an element must be signed but isn't
                    setAsserted(false);
                    setErrorMessage("Element " + signedElementSecurityEvent.getElement() + " must be signed");
                    return false;
                }
            }
        }
        //if we return false here other signed elements will trigger a PolicyViolationException
        return true;
    }
}
