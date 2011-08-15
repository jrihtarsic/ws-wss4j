/**
 * Copyright 2010, 2011 Marc Giger
 *
 * This file is part of the streaming-webservice-security-framework (swssf).
 *
 * The streaming-webservice-security-framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The streaming-webservice-security-framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the streaming-webservice-security-framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.swssf.securityEvent;

/**
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AlgorithmSuiteSecurityEvent extends SecurityEvent {

    //@see http://docs.oasis-open.org/ws-sx/ws-securitypolicy/v1.3/os/ws-securitypolicy-1.3-spec-os.html#_Toc212617893
    //6.1

    public enum Usage {
        Sym_Sig,
        Asym_Sig,
        Dig,
        Enc,
        Sym_Key_Wrap,
        Asym_Key_Wrap,
        Comp_Key,
        Enc_KD,
        Sig_KD,
        C14n,
        Soap_Norm,
        STR_Trans,
        XPath,
    }

    private String algorithmURI;
    private Usage usage;

    public AlgorithmSuiteSecurityEvent(Event securityEventType) {
        super(securityEventType);
    }

    public String getAlgorithmURI() {
        return algorithmURI;
    }

    public void setAlgorithmURI(String algorithmURI) {
        this.algorithmURI = algorithmURI;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }
}
