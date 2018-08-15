package at.bitfire.cadroid;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import lombok.Cleanup;
import lombok.Getter;

/*
PKIX x509 v3 (RFC 5280)
2.5.29.19 - Basic Constraints
BasicConstraintsSyntax ::= SEQUENCE {
	cA	BOOLEAN DEFAULT FALSE,
	pathLenConstraint INTEGER (0..MAX) OPTIONAL
}
*/

public class CertificateInfo {
	private static final String TAG = "cadroid.CertificateInfo";
	
	X509Certificate certificate;
	
	// @Getter would name it getCA instead of isCA
	Boolean cA;
	public Boolean isCA() { return cA; }
	
	@Getter Integer maxPathLength;
	

	public CertificateInfo(X509Certificate certificate) {
		this.certificate = certificate;
		
		decodeBasicConstraints();
	}
	
	
	// general info
	
	public String getSubjectName() {
		return certificate.getSubjectX500Principal().getName(X500Principal.RFC1779);
	}
	
	public String[] getSubjectAltNames() {
		try {
			LinkedList<String> altNames = new LinkedList<String>();
			if (certificate.getSubjectAlternativeNames() != null)
				for (List<?> asn1Name : certificate.getSubjectAlternativeNames()) {
					int type = (Integer)asn1Name.get(0);
					String value;
					try {
						value = (String)asn1Name.get(1);
					} catch(Exception e) {
						value = "?";
						Log.w(TAG, "Couldn't cast alternative subject name to String", e);
					}
					altNames.add(value + " [" + type + "]");
				}
			return altNames.toArray(new String[0]);
		} catch (CertificateParsingException e) {
			Log.w(TAG, "Couldn't parse Subject Alternative Names from certificate", e);
			return null;
		}		
	}
	
	public String getSerialNumber() {
		return certificate.getSerialNumber().toString(16);
	}
	
	public String getSignature(String algorithm) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update(certificate.getEncoded());
			String sig = "";
			boolean first = true;
			for (byte b : digest.digest()) {
				if (!first)
					sig += ":";
				else
					first = false;
				sig += String.format("%02X", b & 0xFF);
			}
			return sig;
		} catch(Exception e) {
			Log.e(TAG, "Couldn't calculate certificate digest", e);
			return e.getMessage();
		}
	}
	
	public Date getNotBefore() {
		return certificate.getNotBefore();
	}
	
	public Date getNotAfter() {
		return certificate.getNotAfter();
	}
	
	public boolean isCurrentlyValid() {
		try {
			certificate.checkValidity();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	
	// Basic Constraints info
	
	private void decodeBasicConstraints() {
		cA = null;
		maxPathLength = null;
		
		// get the DER-encoded OCTET STRING for the extension value
		byte[] b = certificate.getExtensionValue("2.5.29.19");
		
		if (b == null)		// no Basic Constraints extension
			return;
		
		try {
			@Cleanup InputStream is = new ByteArrayInputStream(b);
			
			// it's an OCTET STRING, get the content
			@Cleanup InputStream extension = ASN1Decoder.readOctetString(is);
			
			// get BasicConstraintsSyntax SEQUENCE
			@Cleanup InputStream seqBasicConstraints = ASN1Decoder.readSequence(extension);
			
			// read cA flag
			cA = false;		// DEFAULT FALSE
			if (seqBasicConstraints.available() > 0) {
				try {
					cA = ASN1Decoder.readBoolean(seqBasicConstraints);
				} catch (ASN1UnexpectedTypeException e) {
					// no cA flag (DEFAULT FALSE)
				}
				
				// read max. path length (OPTIONAL)
				if (seqBasicConstraints.available() > 0) {
					try {
						BigInteger bi = ASN1Decoder.readInteger(seqBasicConstraints);
						maxPathLength = bi.intValue();
					} catch (ASN1UnexpectedTypeException e) {
						Log.e(TAG, "Didn't find INTEGER when decoding pathLenConstraint", e);
					}
				}
			}
			
		} catch (ASN1UnexpectedTypeException e) {
			Log.e(TAG, "Unexpected ASN.1 data type when decoding Basic Constraints", e);
		} catch (IOException e) {
			Log.e(TAG, "I/O error when decoding Basic Constraints extension", e);
		}
	}
	
	protected CertPath singleCertificatePath(X509Certificate certificate) throws CertificateException {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		List<X509Certificate> list = new LinkedList<X509Certificate>();
		list.add(certificate);
		return certFactory.generateCertPath(list);
	}

}
