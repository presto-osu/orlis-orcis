package at.bitfire.cadroid;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Wrapper around a given HostnameVerifier
 * that sets
 *   - the requested host name (from the URL, not the certificate CN), and
 *    - whether the certificate's names match this host name
 * to ConnectionInfo for further processing
 */
public class InfoHostnameVerifier implements HostnameVerifier {
	protected HostnameVerifier defaultVerifier;

	ConnectionInfo info;
	
	
	InfoHostnameVerifier(HostnameVerifier defaultVerifier, ConnectionInfo info) {
		this.defaultVerifier = defaultVerifier;
		this.info = info;
	}

	@Override
	public boolean verify(String hostName, SSLSession session) {
		info.setHostName(hostName);
		info.setHostNameMatching(defaultVerifier.verify(hostName, session));
		return true;
	}
	
}
