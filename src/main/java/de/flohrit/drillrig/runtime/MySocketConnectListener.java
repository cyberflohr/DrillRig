package de.flohrit.drillrig.runtime;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import net.schmizz.concurrent.Event;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.channel.Channel;
import net.schmizz.sshj.connection.channel.OpenFailException.Reason;
import net.schmizz.sshj.connection.channel.SocketStreamCopyMonitor;
import net.schmizz.sshj.connection.channel.forwarded.ConnectListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flohrit.drillrig.config.Forward;
import de.flohrit.drillrig.config.IpFilter;

public class MySocketConnectListener implements ConnectListener {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final SocketAddress addr;
	private final Forward forwardCfg;
	
	/** Create with a {@link SocketAddress} this listener will forward to. 
	 * @param forwardCfg */
	public MySocketConnectListener(Forward forwardCfg, SocketAddress addr) {
		this.addr = addr;
		this.forwardCfg = forwardCfg; 
	}

	/** On connect, confirm the channel and start forwarding. */
	@Override
	public void gotConnect(Channel.Forwarded chan) throws IOException {
		log.info("New connection from {}:{}", chan.getOriginatorIP(),
				chan.getOriginatorPort());

		if (isBlockedIp( chan.getOriginatorIP())) {
			chan.reject(Reason.ADMINISTRATIVELY_PROHIBITED, "");
			return;
		}
		
		final Socket sock = new Socket();
		sock.setSendBufferSize(chan.getLocalMaxPacketSize());
		sock.setReceiveBufferSize(chan.getRemoteMaxPacketSize());

		sock.connect(addr);
		chan.confirm();
		
		final Event<IOException> soc2chan = new StreamCopier(
				sock.getInputStream(), chan.getOutputStream()).bufSize(
				chan.getRemoteMaxPacketSize()).spawnDaemon("soc2chan");

		final Event<IOException> chan2soc = new StreamCopier(
				chan.getInputStream(), sock.getOutputStream()).bufSize(
				chan.getLocalMaxPacketSize()).spawnDaemon("chan2soc");

		SocketStreamCopyMonitor.monitor(5, TimeUnit.SECONDS, chan2soc,
				soc2chan, chan, sock);
	}

	private boolean isBlockedIp(String originatorIP) {

		IpFilter filter = forwardCfg.getFilter();
		if (filter != null) {
			if (filter.isEnabled()) {
				
				for (String mask : filter.getMask()) {
					if (Pattern.compile(mask).matcher(originatorIP).matches()) {
						return filter.isBlock();
					}
				}
				return !filter.isBlock();
			}
		}
		return false;
	}
}
