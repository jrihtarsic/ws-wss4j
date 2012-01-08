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
package org.swssf.policy.test;

import org.swssf.policy.PolicyEnforcer;
import org.swssf.policy.PolicyViolationException;
import org.swssf.wss.ext.WSSConstants;
import org.swssf.wss.ext.WSSecurityException;
import org.swssf.wss.impl.securityToken.HttpsSecurityToken;
import org.swssf.wss.securityEvent.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

/**
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class TransportBindingTest extends AbstractPolicyTestBase {

    @Test
    public void testPolicy() throws Exception {
        String policyString =
                "<sp:TransportBinding xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "<sp:IncludeTimestamp/>\n" +
                        "</wsp:Policy>\n" +
                        "</sp:TransportBinding>";
        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);
        TimestampSecurityEvent timestampSecurityEvent = new TimestampSecurityEvent();
        policyEnforcer.registerSecurityEvent(timestampSecurityEvent);

        RequiredElementSecurityEvent requiredElementSecurityEvent = new RequiredElementSecurityEvent();
        requiredElementSecurityEvent.setElement(WSSConstants.TAG_wsu_Timestamp);
        policyEnforcer.registerSecurityEvent(requiredElementSecurityEvent);

        HttpsTokenSecurityEvent httpsTokenSecurityEvent = new HttpsTokenSecurityEvent();
        httpsTokenSecurityEvent.setSecurityToken(new HttpsSecurityToken(true, "username", null));
        httpsTokenSecurityEvent.setTokenUsage(TokenSecurityEvent.TokenUsage.Signature);
        policyEnforcer.registerSecurityEvent(httpsTokenSecurityEvent);

        httpsTokenSecurityEvent.setTokenUsage(TokenSecurityEvent.TokenUsage.Encryption);
        policyEnforcer.registerSecurityEvent(httpsTokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        EncryptedElementSecurityEvent encryptedElementSecurityEvent = new EncryptedElementSecurityEvent(null, true, false);
        encryptedElementSecurityEvent.setElement(WSSConstants.TAG_dsig_Signature);
        policyEnforcer.registerSecurityEvent(encryptedElementSecurityEvent);

        encryptedElementSecurityEvent = new EncryptedElementSecurityEvent(null, true, false);
        encryptedElementSecurityEvent.setElement(WSSConstants.TAG_wsse11_SignatureConfirmation);
        policyEnforcer.registerSecurityEvent(encryptedElementSecurityEvent);

        SignedPartSecurityEvent signedPartSecurityEvent = new SignedPartSecurityEvent(null, true);
        signedPartSecurityEvent.setElement(WSSConstants.TAG_soap12_Body);
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testPolicyNotIncludeTimestamp() throws Exception {
        String policyString =
                "<sp:TransportBinding xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "</wsp:Policy>\n" +
                        "</sp:TransportBinding>";
        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        HttpsTokenSecurityEvent httpsTokenSecurityEvent = new HttpsTokenSecurityEvent();
        httpsTokenSecurityEvent.setSecurityToken(new HttpsSecurityToken(true, "username", null));
        httpsTokenSecurityEvent.setTokenUsage(TokenSecurityEvent.TokenUsage.Signature);
        policyEnforcer.registerSecurityEvent(httpsTokenSecurityEvent);

        httpsTokenSecurityEvent.setTokenUsage(TokenSecurityEvent.TokenUsage.Encryption);
        policyEnforcer.registerSecurityEvent(httpsTokenSecurityEvent);

        TimestampSecurityEvent timestampSecurityEvent = new TimestampSecurityEvent();
        policyEnforcer.registerSecurityEvent(timestampSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof PolicyViolationException);
        }
    }

    @Test
    public void testPolicySignatureNotEncrypted() throws Exception {
        String policyString =
                "<sp:TransportBinding xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "<sp:IncludeTimestamp/>\n" +
                        "</wsp:Policy>\n" +
                        "</sp:TransportBinding>";
        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        TimestampSecurityEvent timestampSecurityEvent = new TimestampSecurityEvent();
        policyEnforcer.registerSecurityEvent(timestampSecurityEvent);

        RequiredElementSecurityEvent requiredElementSecurityEvent = new RequiredElementSecurityEvent();
        requiredElementSecurityEvent.setElement(WSSConstants.TAG_wsu_Timestamp);
        policyEnforcer.registerSecurityEvent(requiredElementSecurityEvent);

        HttpsTokenSecurityEvent httpsTokenSecurityEvent = new HttpsTokenSecurityEvent();
        httpsTokenSecurityEvent.setSecurityToken(new HttpsSecurityToken(true, "username", null));
        httpsTokenSecurityEvent.setTokenUsage(TokenSecurityEvent.TokenUsage.Signature);
        policyEnforcer.registerSecurityEvent(httpsTokenSecurityEvent);

        httpsTokenSecurityEvent.setTokenUsage(TokenSecurityEvent.TokenUsage.Encryption);
        policyEnforcer.registerSecurityEvent(httpsTokenSecurityEvent);

        EncryptedElementSecurityEvent encryptedElementSecurityEvent = new EncryptedElementSecurityEvent(null, false, false);
        encryptedElementSecurityEvent.setElement(WSSConstants.TAG_dsig_Signature);
        policyEnforcer.registerSecurityEvent(encryptedElementSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testPolicyNotWholeBodySigned() throws Exception {
        String policyString =
                "<sp:TransportBinding xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "<sp:IncludeTimestamp/>\n" +
                        "</wsp:Policy>\n" +
                        "</sp:TransportBinding>";
        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);
        TimestampSecurityEvent timestampSecurityEvent = new TimestampSecurityEvent();
        policyEnforcer.registerSecurityEvent(timestampSecurityEvent);

        RequiredElementSecurityEvent requiredElementSecurityEvent = new RequiredElementSecurityEvent();
        requiredElementSecurityEvent.setElement(WSSConstants.TAG_wsu_Timestamp);
        policyEnforcer.registerSecurityEvent(requiredElementSecurityEvent);

        HttpsTokenSecurityEvent httpsTokenSecurityEvent = new HttpsTokenSecurityEvent();
        httpsTokenSecurityEvent.setSecurityToken(new HttpsSecurityToken(true, "username", null));
        httpsTokenSecurityEvent.setTokenUsage(TokenSecurityEvent.TokenUsage.Signature);
        policyEnforcer.registerSecurityEvent(httpsTokenSecurityEvent);

        httpsTokenSecurityEvent.setTokenUsage(TokenSecurityEvent.TokenUsage.Encryption);
        policyEnforcer.registerSecurityEvent(httpsTokenSecurityEvent);

        EncryptedElementSecurityEvent encryptedElementSecurityEvent = new EncryptedElementSecurityEvent(null, true, false);
        encryptedElementSecurityEvent.setElement(WSSConstants.TAG_dsig_Signature);
        policyEnforcer.registerSecurityEvent(encryptedElementSecurityEvent);

        encryptedElementSecurityEvent = new EncryptedElementSecurityEvent(null, true, false);
        encryptedElementSecurityEvent.setElement(WSSConstants.TAG_wsse11_SignatureConfirmation);
        policyEnforcer.registerSecurityEvent(encryptedElementSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        SignedPartSecurityEvent signedPartSecurityEvent = new SignedPartSecurityEvent(null, false);
        signedPartSecurityEvent.setElement(WSSConstants.TAG_soap12_Body);
        policyEnforcer.registerSecurityEvent(signedPartSecurityEvent);

        policyEnforcer.doFinal();
    }
}
