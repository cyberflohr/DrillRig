package de.flohrit.drillrig.runtime;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import net.schmizz.concurrent.Event;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.channel.Channel;
import net.schmizz.sshj.connection.channel.OpenFailException.Reason;
import net.schmizz.sshj.connection.channel.SocketStreamCopyMonitor;
import net.schmizz.sshj.connection.channel.forwarded.ConnectListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySocketConnectListener implements ConnectListener {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected final SocketAddress addr;

	/** Create with a {@link SocketAddress} this listener will forward to. */
	public MySocketConnectListener(SocketAddress addr) {
		this.addr = addr;
	}

	/** On connect, confirm the channel and start forwarding. */
	@Override
	public void gotConnect(Channel.Forwarded chan) throws IOException {
		log.info("New connection from {}:{}", chan.getOriginatorIP(),
				chan.getOriginatorPort());

		final Socket sock = new Socket();
		sock.setSendBufferSize(chan.getLocalMaxPacketSize());
		sock.setReceiveBufferSize(chan.getRemoteMaxPacketSize());

		// do confirmation here, because if host doesn't exists, the underlying transport session will be closed.
		chan.confirm();
		
		sock.connect(addr);
		

		final Event<IOException> soc2chan = new StreamCopier(
				sock.getInputStream(), chan.getOutputStream()).bufSize(
				chan.getRemoteMaxPacketSize()).spawnDaemon("soc2chan");

		final Event<IOException> chan2soc = new StreamCopier(
				chan.getInputStream(), sock.getOutputStream()).bufSize(
				chan.getLocalMaxPacketSize()).spawnDaemon("chan2soc");

		SocketStreamCopyMonitor.monitor(5, TimeUnit.SECONDS, chan2soc,
				soc2chan, chan, sock);
	}

}
