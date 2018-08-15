package at.bitfire.cadroid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

/**
 * Reads DER-encoded ASN.1 content
 */
public class ASN1Decoder {
	
	private final static int
		ASN1_TAG_BOOLEAN	= 0x01,
		ASN1_TAG_INTEGER	= 0x02,
		ASN1_TAG_STRING 	= 0x04,
		ASN1_TAG_SEQUENCE	= 0x30;
	
	
	public static boolean readBoolean(InputStream is) throws ASN1UnexpectedTypeException, IOException {
		assertType(is, ASN1_TAG_BOOLEAN);

		byte[] buffer = readBuffer(is);
		for (int i = 0; i < buffer.length; i++)
			if (buffer[i] != 0)
				return true;
		return false;
	}
	
	public static BigInteger readInteger(InputStream is) throws ASN1UnexpectedTypeException, IOException {
		assertType(is, ASN1_TAG_INTEGER);
		byte[] buffer = readBuffer(is);
		return new BigInteger(buffer);
	}
	
	public static InputStream readOctetString(InputStream is) throws ASN1UnexpectedTypeException, IOException {
		assertType(is, ASN1_TAG_STRING);
		return new ByteArrayInputStream(readBuffer(is));
	}
	
	public static InputStream readSequence(InputStream is) throws ASN1UnexpectedTypeException, IOException {
		assertType(is, ASN1_TAG_SEQUENCE);
		return new ByteArrayInputStream(readBuffer(is));
	}
	
	
	private static void assertType(InputStream is, int expectedType) throws ASN1UnexpectedTypeException, IOException {
		if (!is.markSupported())
			throw new IOException("CAdroid ASN1Decoder requires mark support for input streams");
			
		is.mark(1000);
		int type = is.read();
		if (type != expectedType) {
			is.reset();
			throw new ASN1UnexpectedTypeException("Type 0x" + Integer.toHexString(expectedType) +
				" expected, found 0x" + Integer.toHexString(type) + " instead, rewinding");
		}
	}
	
	private static byte[] readBuffer(InputStream is) throws ASN1UnexpectedTypeException, IOException {
		int length = is.read();
		if ((length & 0x80) == 0x80)
			throw new ASN1UnexpectedTypeException("Fields with more than 127 octets are not supported");
		
		byte[] buffer = new byte[length];
		is.read(buffer);
		return buffer;
	}

}
