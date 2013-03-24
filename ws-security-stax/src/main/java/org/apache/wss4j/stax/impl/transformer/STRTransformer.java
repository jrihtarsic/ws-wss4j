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
package org.apache.wss4j.stax.impl.transformer;

import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.stax.ext.Transformer;
import org.apache.xml.security.stax.impl.transformer.TransformIdentity;
import org.apache.xml.security.stax.impl.transformer.canonicalizer.Canonicalizer20010315_Excl;

import java.util.ArrayList;
import java.util.List;

public class STRTransformer extends TransformIdentity {

    @Override
    public void setTransformer(Transformer transformer) throws XMLSecurityException {
        if (!(transformer instanceof Canonicalizer20010315_Excl)) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.INVALID_SECURITY);
        }
        ((Canonicalizer20010315_Excl) transformer).setPropagateDefaultNamespace(true);
        List<String> inclusiveNamespacesPrefixList = new ArrayList<String>();
        inclusiveNamespacesPrefixList.add("#default");
        transformer.setList(inclusiveNamespacesPrefixList);
        super.setTransformer(transformer);
    }

    @Override
    public void setList(List list) throws XMLSecurityException {
        throw new UnsupportedOperationException();
    }
}
