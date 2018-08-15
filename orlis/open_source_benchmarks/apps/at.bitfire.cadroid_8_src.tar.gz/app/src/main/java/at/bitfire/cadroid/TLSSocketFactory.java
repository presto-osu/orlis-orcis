package at.bitfire.cadroid;

import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * SSL socket factory to enable TLSv1.1 and TLSv1.2 on pre-Android 5.0 devices
 */
public class TLSSocketFactory extends SSLSocketFactory {
	private final static String TAG = "CAdroid.SocketFactory";

	private final static String[] PREFERRED_CIPHER_SUITES = new String[] {
		// allowed secure ciphers according to NIST.SP.800-52r1.pdf Section 3.3.1
		// TLS 1.2
		"TLS_RSA_WITH_AES_256_GCM_SHA384",
		"TLS_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
		"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
		"TLS_ECHDE_RSA_WITH_AES_128_GCM_SHA256",
		// maximum interoperability
		"TLS_RSA_WITH_3DES_EDE_CBC_SHA",
		"TLS_RSA_WITH_AES_128_CBC_SHA",
		// additionally
		"TLS_RSA_WITH_AES_256_CBC_SHA",
		"TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
		"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
		"TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
		"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"
	};

	private final SSLSocketFactory sslSocketFactory;

	public TLSSocketFactory(SSLSocketFactory delegate) {
		sslSocketFactory = delegate;
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return sslSocketFactory.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket() throws IOException {
		Socket socket = sslSocketFactory.createSocket();
		return configureSocket(socket);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		Socket socket = sslSocketFactory.createSocket(host, port);
		return configureSocket(socket);
	}

	@Override
	public Socket createSocket(InetAddress addr, int port) throws IOException {
		Socket socket = sslSocketFactory.createSocket(addr, port);
		return configureSocket(socket);
	}

	@Override
	public Socket createSocket(Socket k, String host, int port, boolean autoClose) throws IOException {
		Socket socket = sslSocketFactory.createSocket(k, host, port, autoClose);
		return configureSocket(socket);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
		Socket socket = sslSocketFactory.createSocket(host, port, localHost, localPort);
		return configureSocket(socket);
	}

	@Override
	public Socket createSocket(InetAddress inetAddress, int port, InetAddress localAddress, int localPort) throws IOException {
		Socket socket = sslSocketFactory.createSocket(inetAddress, port, localAddress, localPort);
		return configureSocket(socket);
	}


	protected SSLSocket configureSocket(Socket socket) {
		SSLSocket sslSocket = (SSLSocket)socket;

		sslSocket.setEnabledProtocols(getDefaultProtocols(sslSocket));
		log("Enabled protocols", sslSocket.getEnabledProtocols());

		sslSocket.setEnabledCipherSuites(getDefaultCipherSuites());
		log("Enabled cipher suites", sslSocket.getEnabledCipherSuites());

		return sslSocket;
	}

	protected static String[] getDefaultProtocols(SSLSocket sslSocket) {
		// Android versions below 4.1 do not support TLSv1.2 and TLSv1.1
		LinkedList<String> protocols = new LinkedList<>();
		for (String protocol : sslSocket.getSupportedProtocols())
			if (!protocol.contains("SSL"))  // don't enable any version of SSL anymore (disables SSLv3)
				protocols.add(protocol);
		return protocols.toArray(new String[0]);
	}

	@Override
	public String[] getDefaultCipherSuites() {
		/* algorithm:
		1) preferredCiphers are taken from the static set PREFERRED_CIPHERS,
		   but only the ones which are available (= supported ciphers)
		2) allowedCiphers = preferredCiphers + already enabled ciphers (for maximum compatibility)
		*/

		List<String> supportedCiphers = Arrays.asList(getSupportedCipherSuites());
		List<String> preferredCiphers = Arrays.asList(PREFERRED_CIPHER_SUITES);

		HashSet<String> allowedCiphers = new HashSet(preferredCiphers);     // take all preferred ciphers,
		allowedCiphers.retainAll(supportedCiphers);                         // but only keep supported ones

		// add previously enabled ciphers again for maximum compatibility
		// (may include TLS_EMPTY_RENEGOTIATION_INFO_SCSV etc.)
		allowedCiphers.addAll(Arrays.asList(sslSocketFactory.getDefaultCipherSuites()));

		return allowedCiphers.toArray(new String[0]);
	}


	private static void log(String title, String[] array) {
		String logMessage = title + ": ";
		logMessage += Arrays.toString(array).replace('[', ' ').replace(']', ' ').trim();
		Log.i(TAG, logMessage);
	}
}
