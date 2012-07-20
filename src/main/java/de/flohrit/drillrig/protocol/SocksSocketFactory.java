package de.flohrit.drillrig.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

/**
 *
 * 
 */
public class SocksSocketFactory extends SocketFactory {

    private final String tunnelHost;
    private final int tunnelPort;
    
    public SocksSocketFactory(String proxyhost, int proxyport) {
        tunnelHost = proxyhost;
        tunnelPort = proxyport;
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

		java.net.Proxy socks = new java.net.Proxy(Type.SOCKS, new InetSocketAddress(tunnelHost, tunnelPort));
        Socket tunnel = new Socket(socks);
        
        return tunnel;
    }
}

