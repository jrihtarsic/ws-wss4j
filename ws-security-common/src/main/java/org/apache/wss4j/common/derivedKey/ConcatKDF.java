package org.apache.wss4j.common.derivedKey;

import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;


/**
 * Key DerivationAlgorithm implementation, defined in Section 5.8.1 of NIST SP 800-56A [SP800-56A], and is equivalent
 * to the KDF3 function defined in ANSI X9.44-2007 [ANSI-X9-44-2007] when the contents of the OtherInfo parameter
 * is structured as in NIST SP 800-56A.
 * <p>
 * Identifier:  http://www.w3.org/2009/xmlenc11#ConcatKDF
 */
public class ConcatKDF implements DerivationAlgorithm {

    private static final Logger LOG = LoggerFactory.getLogger(ConcatKDF.class);

    String algorithmURI;

    /**
     * Constructor ConcatKDF with digest algorithmURI parameter such as http://www.w3.org/2001/04/xmlenc#sha256,
     * http://www.w3.org/2001/04/xmlenc#sha512, etc.
     */
    public ConcatKDF(String algorithmURI) {
        this.algorithmURI = algorithmURI;
    }


    /**
     * Default Constructor which sets the default digest algorithmURI parameter:  http://www.w3.org/2001/04/xmlenc#sha256,
     */
    public ConcatKDF() {
        this(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256);
    }


    private static MessageDigest getDigestInstance(String algorithmURI) throws WSSecurityException {
        String algorithmID = JCEMapper.translateURItoJCEID(algorithmURI);

        if (algorithmID == null) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.UNSUPPORTED_ALGORITHM,
                    "unknownAlgorithm", new Object[]{algorithmURI});
        }

        MessageDigest md;
        String provider = JCEMapper.getProviderId();
        try {
            if (provider == null) {
                md = MessageDigest.getInstance(algorithmID);
            } else {
                md = MessageDigest.getInstance(algorithmID, provider);
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.UNSUPPORTED_ALGORITHM,
                    "unknownAlgorithm", new Object[]{algorithmURI});
        } catch (NoSuchProviderException ex) {
            LOG.warn("Provider {} not found", provider, ex);
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "noSecProvider");
        }

        return md;
    }

    /**
     * Key DerivationAlgorithm implementation as defined in Section 5.8.1 of NIST SP 800-56A [SP800-56A]
     * <ul>
     * <li> reps = ⎡ keydatalen / hashlen⎤.</li>
     * <li> If reps > (2>32 −1), then ABORT: output an error indicator and stop.</li>
     * <li> Initialize a 32-bit, big-endian bit string counter as 0000000116.</li>
     * <li> If counter || Z || OtherInfo is more than max_hash_inputlen bits long, then ABORT: output an error indicator and stop.
     * <li> For i = 1 to reps by 1, do the following:<ul>
     *     <li> Compute Hashi = H(counter || Z || OtherInfo).</li>
     *     <li> Increment counter (modulo 232), treating it as an unsigned 32-bit integer.</li>
     * </ul></li>
     * <li> Let Hhash be set to Hashreps if (keydatalen / hashlen) is an integer; otherwise, let Hhash  be set to the
     * (keydatalen mod hashlen) leftmost bits of Hashreps.</li>
     * <li>Set DerivedKeyingMaterial = Hash1 || Hash2 || ... || Hashreps-1 || Hhash</li>
     * </ul>
     *
     * @param secret    The "shared" secret to use for key derivation (e.g. the secret key)
     * @param otherInfo as specified in [SP800-56A] the optional  attributes:  AlgorithmID, PartyUInfo, PartyVInfo, SuppPubInfo and SuppPrivInfo attributes  are concatenated to form a bit string “OtherInfo” that is used with the key derivation function.
     * @param offset    the offset parameter is ignored by this implementation.
     * @param keyLength The length of the key to derive
     * @return The derived key
     * @throws WSSecurityException
     */
    @Override
    public byte[] createKey(byte[] secret, byte[] otherInfo, int offset, long keyLength) throws WSSecurityException {

        MessageDigest digest = getDigestInstance(algorithmURI);

        int iDigestLength = digest.getDigestLength();
        if (keyLength / iDigestLength > (long) Integer.MAX_VALUE) {
            LOG.error("Key size is to long to be derived with hash algorithm [{}]", digest.getAlgorithm());
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "errorInKeyDerivation");
        }
        digest.reset();
        ByteBuffer indexBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);

        ByteBuffer result = ByteBuffer.allocate((int) keyLength);
        int toGenerateSize = (int) keyLength;
        int counter = 1;
        while (toGenerateSize > 0) {
            indexBuffer.position(0);
            indexBuffer.putInt(counter++);
            indexBuffer.position(0);
            digest.update(indexBuffer);
            digest.update(secret);
            if (otherInfo != null && otherInfo.length > 0) {
                digest.update(otherInfo);
            }
            result.put(digest.digest(), 0, toGenerateSize < iDigestLength ? toGenerateSize : iDigestLength);
            toGenerateSize -= iDigestLength;
        }
        return result.array();
    }

    /**
     * Method concatenate the bitstrings in following order {@code algID || partyUInfo || partyVInfo || suppPubInfo || suppPrivInfo}.
     * to crate otherInfo as key derivation function input.
     * If named parameters are null the value is ignored.
     * Method parses the bitstring firs {@See https://www.w3.org/TR/xmlenc-core1/#sec-ConcatKDF} and then concatenates them to a byte array.
     *
     * @param sharedSecret The "shared" secret to use for key derivation (e.g. the secret key)
     * @param algID        A bit string that indicates how the derived keying material will be parsed and for which
     *                     algorithm(s) the derived secret keying material will be used.
     * @param partyUInfo   A bit string containing public information that is required by the
     *                     application using this KDF to be contributed by party U to the key derivation
     *                     process. At a minimum, PartyUInfo shall include IDU, the identifier of party U. See
     *                     the notes below..
     * @param partyVInfo   A bit string containing public information that is required by the
     *                     application using this KDF to be contributed by party V to the key derivation
     *                     process. At a minimum, PartyVInfo shall include IDV, the identifier of party V. See
     *                     the notes below.
     * @param suppPubInfo  bit string containing additional, mutually-known public information.
     * @param suppPrivInfo The suppPrivInfo A bit string containing additional, mutually-known public Information.
     * @param keyLength    The length of the key to derive
     * @return The resulting other info.
     */


    public byte[] createKey(final byte[] sharedSecret,
                            final String algID,
                            final String partyUInfo,
                            final String partyVInfo,
                            final String suppPubInfo,
                            final String suppPrivInfo,
                            final long keyLength)
            throws WSSecurityException {

        final byte[] otherInfo = concatParameters(algID, partyUInfo, partyVInfo, suppPubInfo, suppPrivInfo);

        return createKey(sharedSecret, otherInfo, 0, keyLength);
    }


    /**
     * Simple method to concatenate non-padded bitstream ConcatKDF parameters.
     * If parameters are null the value is ignored.
     *
     * @param parameters the parameters to concatenate
     * @return the concatenated parameters as byte array
     */
    public static byte[] concatParameters(final String... parameters) throws WSSecurityException {

        List<byte[]> byteParams = new ArrayList<>();
        for (String parameter : parameters) {
            byte[] bytes = parseBitString(parameter);
            byteParams.add(bytes);
        }
        // get bytearrays size
        int iSize = byteParams.stream().map(ConcatKDF::getSize).reduce(0, (a, b) -> a + b);

        ByteBuffer buffer = ByteBuffer
                .allocate(iSize);
        byteParams.stream().forEach(buffer::put);
        return buffer.array();
    }


    /**
     * The method validates the bitstring parameter structure and returns byte array of the parameter.
     * <p/>
     * The bitstring is divided into octets using big-endian encoding. Parameter starts with two characters (hex number) defining the number of padding bits followed by hex-string.
     * the length of the bitstring is not a multiple of 8 then add padding bits (value 0) as necessary to the last octet to make it a multiple of 8.
     *
     * @param kdfParameter the parameter to parse
     * @return the parsed parameter as byte array
     */
    private static byte[] parseBitString(final String kdfParameter) throws WSSecurityException {
        // ignore empty parameters
        if (kdfParameter == null || kdfParameter.isBlank()) {
            return new byte[0];
        }
        String kdfP = kdfParameter.trim();
        int paramLen = kdfP.length();
        // bit string mus have two chars following by first byte defining the number of padding bits
        if (paramLen < 4) {
            LOG.error("ConcatKDF parameter is to short");
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "errorInKeyDerivation");
        }
        if (paramLen % 2 != 0) {
            LOG.error("Invalid length of ConcatKDF parameter [{}]!", kdfP);
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "errorInKeyDerivation");
        }
        int iPadding = Integer.parseInt(kdfP.substring(0, 2), 16);
        if (iPadding != 0) {
            LOG.error("Padded ConcatKDF parameters ara not supported");
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "errorInKeyDerivation");
        }
        // skip first two chars
        kdfP = kdfP.substring(2);
        paramLen = kdfP.length();
        byte[] data = new byte[paramLen / 2];

        for (int i = 0; i < paramLen; i += 2) {
            data[i / 2] = (byte) ((Character.digit(kdfP.charAt(i), 16) << 4)
                    + Character.digit(kdfP.charAt(i + 1), 16));
        }
        return data;
    }

    private static int getSize(byte[] array) {
        return array == null ? 0 : array.length;
    }

    private static byte[] getBytes(byte[] array) {
        return array == null ? new byte[0] : array;
    }
}
