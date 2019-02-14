import java.io.InputStream;
import java.io.OutputStream;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class RetrieveSSLCert {
    public static void main(String[] args) throws Exception {
	if (args.length < 2) {
	    System.out.println("Usage: java RetrieveSSLCert <host> <port>");
	    return;
	}

	String host = args[0];
	int port = Integer.parseInt(args[1]);

	// create custom trust manager to ignore trust paths
	TrustManager trm = new X509TrustManager() {
	    public X509Certificate[] getAcceptedIssuers() {
		return null;
	    }

	    public void checkClientTrusted(X509Certificate[] certs, String authType) {
	    }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
	};

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[] { trm }, null);
        SSLSocketFactory factory =sc.getSocketFactory();
        SSLSocket socket =(SSLSocket)factory.createSocket(host, port);
        socket.startHandshake();
	SSLSession session = socket.getSession();
	java.security.cert.Certificate[] servercerts = session.getPeerCertificates();
	for (int i = 0; i < servercerts.length; i++) {
            System.out.print("-----BEGIN CERTIFICATE-----\n");
            System.out.print(new sun.misc.BASE64Encoder().encode(servercerts[i].getEncoded()));
            System.out.print("\n-----END CERTIFICATE-----\n");
	}

	socket.close();
    }
}

