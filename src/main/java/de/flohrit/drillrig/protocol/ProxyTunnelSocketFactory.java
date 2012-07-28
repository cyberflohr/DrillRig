package de.flohrit.drillrig.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 */
public class ProxyTunnelSocketFactory extends SocketFactory {
	final static private Logger logger = LoggerFactory
			.getLogger(ProxyTunnelSocketFactory.class);


    private final String tunnelHost;
    private final int tunnelPort;
    private final String tunnelAuthUser, tunnelAuthPass;
    
    public ProxyTunnelSocketFactory(String proxyhost, int proxyport) {
        this(proxyhost, proxyport, null, null);
    }
    
    public ProxyTunnelSocketFactory(String proxyhost, int proxyport, String proxyUser, String proxyPass) {
        tunnelHost = proxyhost;
        tunnelPort = proxyport;

        tunnelAuthUser = proxyUser;
        tunnelAuthPass = proxyPass;
    }

    
    public Socket createSocket() throws IOException, UnknownHostException {
    	return createSocket(null, null, 0, true);
    }
    
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return createSocket(null, host, port, true);
    }

    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException, UnknownHostException {
        return createSocket(null, host, port, true);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return createSocket(null, host.getHostName(), port, true);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return createSocket(null, address.getHostName(), port, true);
    }

    public Socket createSocket(Socket s, final String host, final int port, boolean autoClose) throws IOException, UnknownHostException {

        Socket tunnel = new Socket() {

			public void connect(SocketAddress endpoint, int timeout)
					throws IOException {

				this.setSoTimeout(10000);
				try {
					super.connect(new InetSocketAddress(tunnelHost, tunnelPort), timeout);
				
					InetSocketAddress inet = (InetSocketAddress)endpoint;
			        doTunnelHandshake(this, inet.getHostName(), inet.getPort());
				} catch (IOException ioe) {
					logger.error("Connecting proxy host failed {}:{}, {}", new Object[] { tunnelHost, tunnelPort, ioe });
					return;
				}
				
			}
        	
        };
        
        return tunnel;
    }
    
    private void doTunnelHandshake(Socket tunnel, String host, int port)
            throws IOException {
        OutputStream out = tunnel.getOutputStream();

        StringBuilder sb = new StringBuilder();
        sb.append("CONNECT " + host + ":" + port + " HTTP/1.0\n");
        sb.append("Connection: keep-alive\n");
        sb.append("Proxy-Connection: Keep-Alive\n");
        sb.append("Pragma: no-cache\n");
        sb.append("User-Agent: " + sun.net.www.protocol.http.HttpURLConnection.userAgent + "\n");

//        if (!StringUtils.isEmpty(tunnelAuthUser)) {
            String credentials = new sun.misc.BASE64Encoder().encode((tunnelAuthUser+":"+tunnelAuthPass).getBytes());
        	sb.append("Proxy-Authorization: Basic " + credentials + "\r\n\r\n");
//        }
        
        logger.debug("Connecting to proxy: {}:{}", host, port);
        byte b[];
        try {
            b = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            b = sb.toString().getBytes();
        }
        out.write(b);
        out.flush();

        /*
         * We need to store the reply so we can create a detailed
         * error message to the user.
         */
        StringBuilder replyStr = new StringBuilder();
        int newlinesSeen = 0;
        boolean headerDone = false;     /* Done on first newline */

        InputStream in = tunnel.getInputStream();

        while (newlinesSeen < 2) {
            int i = in.read();
            if (i < 0) {
                throw new IOException("Unexpected EOF from proxy");
            }
            if (i == '\n') {
                headerDone = true;
                ++newlinesSeen;
            } else if (i != '\r') {
                newlinesSeen = 0;
                if (!headerDone) {
                	replyStr.append((char)i);
                }
            }
        }

        // We check for Connection Established 
        if (replyStr.toString().toLowerCase().indexOf("200 connection established") == -1) {
            throw new IOException("Unable to tunnel through " + tunnelHost + ":" + tunnelPort + ".  Proxy returns \"" + replyStr + "\"");
        }

    }    
}

