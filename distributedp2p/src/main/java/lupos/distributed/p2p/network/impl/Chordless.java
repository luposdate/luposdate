/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.distributed.p2p.network.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lupos.datastructures.items.Triple;
import lupos.distributed.p2p.network.P2PTripleNetwork;
import lupos.distributed.storage.IStorage;
import lupos.distributed.storage.distributionstrategy.IDistribution;

import org.apache.log4j.Logger;

import cx.ath.troja.chordless.Chord;
import cx.ath.troja.chordless.ChordSet;
import cx.ath.troja.chordless.ServerInfo;
import cx.ath.troja.chordless.commands.Command;
import cx.ath.troja.chordless.commands.Sender;
import cx.ath.troja.chordless.dhash.DHash;
import cx.ath.troja.chordless.dhash.Delay;
import cx.ath.troja.nja.Identifier;
import de.rwglab.p2pts.DHashService;
// import de.rwglab.p2pts.IPutAction;
public class Chordless extends P2PTripleNetwork {

	/** Constant <code>l</code> */
	public static Logger l = Logger.getLogger(Chordless.class);

	/** Constant <code>OPTION_JDBC_DRIVER="jdbcDriver"</code> */
	public static final String OPTION_JDBC_DRIVER = "jdbcDriver";

	/** Constant <code>OPTION_JDBC_URL="jdbcUrl"</code> */
	public static final String OPTION_JDBC_URL = "jdbcUrl";

	private final static String DEFAULT_JDBC_DRIVER = "org.hsqldb.jdbcDriver";

	private final static String DEFAULT_JDBC_URL_PREFIX = "jdbc:hsqldb:mem:test";

	private static final int DEFAULT_LOCAL_PORT = 6000;

	private final DHashService p;

	/**
	 * Creates a new peer on the default port
	 *
	 * @return the peer
	 */
	public static DHashService createPeer() {
		return createPeer(DEFAULT_LOCAL_PORT);
	}

	/**
	 * Creates a new peer on the given port with the given master peer
	 *
	 * @param port
	 *            the port
	 * @param masterPeerAddress
	 *            master peer (ip)
	 * @param masterPeerPort
	 *            port of master peer
	 * @return peer
	 * @throws java.io.IOException
	 *             error during process
	 */
	public static DHashService createPeer(final int port,
			final InetAddress masterPeerAddress, final int masterPeerPort)
			throws IOException {

		final String jdbcDriver = DEFAULT_JDBC_DRIVER;
		final String jdbcUrl = DEFAULT_JDBC_URL_PREFIX + ":" + port;

		final InetSocketAddress p2pLocalAddress = new InetSocketAddress(port);
		final InetSocketAddress p2pBootstrapAddress = new InetSocketAddress(
				masterPeerAddress.getHostAddress(), masterPeerPort);

		return new DHashService("ChordLess", p2pLocalAddress,
				p2pBootstrapAddress, jdbcDriver, jdbcUrl);
	}

	/**
	 * Creates a new peer on the given port
	 *
	 * @param port
	 *            the given port
	 * @return the peer
	 */
	public static DHashService createPeer(final int port) {
		final String remoteHost = null;
		final Integer remotePort = null;

		final String jdbcDriver = DEFAULT_JDBC_DRIVER;
		final String jdbcUrl = DEFAULT_JDBC_URL_PREFIX + ":" + port;

		final InetSocketAddress p2pLocalAddress = new InetSocketAddress(port);
		final InetSocketAddress p2pBootstrapAddress = remoteHost != null
				&& remotePort != null ? new InetSocketAddress(remoteHost,
				remotePort) : null;

		return new DHashService("ChordLess", p2pLocalAddress,
				p2pBootstrapAddress, jdbcDriver, jdbcUrl);
	}

	/**
	 * New chord p2p network with the given peer.
	 *
	 * @param peer the peer
	 */
	public Chordless(final DHashService peer) {
		/*
		 * store peer and try to start the peer
		 */
		this.p = peer;
		peer.startAndWait();

		if (peer.getDhash().getBootstrapAddress() == null) {
			l.info(String.format(
					"New Chordless node (%s) connected as master on: %s", peer
							.getDhash().getIdentifier(), peer.getDhash()
							.getServerInfo().getAddress()));
		} else {
			l.info(String.format(
					"New Chordless node (%s) connected with %s on %s", peer
							.getDhash().getIdentifier(), peer.getDhash()
							.getBootstrapAddress(), peer.getDhash()
							.getServerInfo().getAddress()));
		}

		/*
		 * Adds an listener to chord, to be notified, if a new message arrives
		 */
//		peer.getDhash().addMessageListener(new IP2PMessageListener() {
//			@Override
//			public void onMessage(final String message, final String from) {
//
//				Chordless.this.onMessage(message, from);
//			}
//		});
		/*
		 * no copies, so each triple stored once
		 */
		peer.getDhash().setCopies(0);

//		peer.registerEntryAction("lupos.distributed.p2p.network.impl.Chordless$SubgraphRequest", new IPutAction() {
//
//			@Override
//			public boolean put(final Entry e, final Identifier s, final Returner<Object> r) {
//				System.out.println("got a subgraph: " + e.getValue());
//				final SubgraphRequest c = (SubgraphRequest) e.getValue();
//				r.call(null);
//				peer.getDhash().onMessage(c.message, c.from);
//				return false;
//			}
//		});
	}


	/** {@inheritDoc} */
	@Override
	public boolean contains(final String locationKey) {
		final List<Triple> res = this.get(locationKey);
		return (res != null && !res.isEmpty());
	}

	/** {@inheritDoc} */
	@Override
	public List<Triple> get(final String locationKey) {
		/*
		 * get the triples in the given key from whole P2P network
		 */
		final Delay<ChordSet<Triple>> delay = this.p.getDhasher().get(locationKey);
		final ChordSet<Triple> result = delay.get();
		final List<Triple> resultList = new LinkedList<Triple>();
		if (result != null) {
			for (final Triple t : result) {
				resultList.add(t);
			}
		}
		return resultList;
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final String locationKey, final Triple triple) {
		/*
		 * removes a triple from the P2P network
		 */
		final ChordSet<Triple> set = new ChordSet<Triple>();
		set.add(triple);
		final Delay<Boolean> delay = this.p.getDhasher().del(locationKey, set);
		delay.get();
	}

	/** {@inheritDoc} */
	@Override
	public void add(final String locationKey, final Triple value) {
		final ChordSet<Triple> set = new ChordSet<Triple>();
		set.add(value);
		Logger.getLogger(this.getClass()).debug(String.format("Insert triple %s in key %s (%s)",value,locationKey,locationKey));
		final Delay<Object> delay = this.p.getDhasher().put(locationKey, set);
		delay.get();
	}

	/**
	 * ChordCommand is a special command used to transmit the subgraph serialized
	 * as string. So a message and a sender-identification is to transmit via P2P connections.
	 *
	 * @author Bjoern
	 *
	 */
	public static class ChordCommand extends Command implements Serializable {
		private static final long serialVersionUID = 565284096384481446L;

		private String message = "";
		private ServerInfo c = null;
		private final String from;
		private String title = "";
		private Identifier id=null;

		@Override
		public ChordCommand clone() {
				return new ChordCommand(this.c, this.title,this.message, this.from,this.id);
		}

		@Override
		protected Identifier getRegardingIdentifier() {
			return this.id;
		}

		/**
		 * New subgraph message command
		 * @param c server information
		 * @param title a title for this command
		 * @param message the message
		 * @param from the sender (used for a later, asynchron answer)
		 */
		public ChordCommand(final ServerInfo c, final String title, final String message, final String from,
				final Identifier id) {
			super(c);
			this.id = id;
			this.title = title;
			this.c = c;
			this.from = from;
			this.message = message;
		}
		@Override
		public String toString() {
			return String.format("ChordCommand: %s", this.title);
		}

		protected void _execute(final Chord chord, final Sender sender) {
		}


		protected void executeHome(final DHash dhash) {
			dhash.deliver(this);
		}


		private void doIt(final Chord dhash) {
			this.message = Arrays.toString(this.message.getBytes());
			/*
			 * this ecma-script is evaluated on the receiver's chord instance,
			 * so we will access there the method "onMessage(String message, String from)"
			 */
			final String evalCode = String.format("chord.onMessage('%s', \"%s\")",
					this.message, this.from);
			/*
			 * we don't need the result, because the call is asynchrony, and the answer is sent back later!
			 */
			dhash.getScript().eval(evalCode);
		}

		@Override
		protected void execute(final Chord chord, final Sender sender) {
			this.doIt(chord);
		}
	}

	/**
	 * serializes the server's connection information as string
	 * @param a the server information
	 * @return the serialized string, has the format: <b>host;port</b>
	 */
	static String socketToString(final ServerInfo a) {
		String from = "(not serializable)";
		final SocketAddress ad = a.getAddress();
		if (ad instanceof InetSocketAddress) {
			final InetSocketAddress address = (InetSocketAddress) ad;
			from = address.getAddress().getHostAddress() + ";"
					+ address.getPort();
		}
		return from;
	}

	public static class SubgraphRequest implements Serializable {
		private static final long serialVersionUID = -6449921922624959213L;
		private String from="";
		private String message="";

		public SubgraphRequest(final String from, final String message) {
			this.from = from;
			this.message = message;
		}
	}


	/** {@inheritDoc} */
	@Override
	public void sendMessage(final String key, final String message) {
		/*
		 * get the identifier by the given key
		 */
//		Identifier identifier = Identifier.generate(key);
		final String from = socketToString(this.p.getDhash().getServerInfo());
//		String title = String.format("Subgraph-Request in %s, sent from %s",key,from);
//		String sgValue = String.format("{%s}{%s}",from,message);
		Logger.getLogger(this.getClass()).debug(String.format("Send Subgraph from %s to %s : %s",from,key,message));


		final SubgraphRequest req = new SubgraphRequest(from,message);
		final Delay<Object> delay = this.p.getDhasher().put(key, req);
		final Thread t = new Thread() {
			@Override
			public void run() {
				delay.get();
			}
		};
		t.setName("SubgraphRequest-Sender");
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Is the given ip address an old, IPv4-styled address?
	 * @param ipAddress the address
	 * @return is IPv4?
	 */
	static boolean isIPv4(final String ipAddress) {
		final Pattern pattern = Pattern
				.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
		final Matcher matcher = pattern.matcher(ipAddress);
		return matcher.find();
	}

	/** {@inheritDoc} */
	@Override
	public void sendMessageTo(final String peer, final String message) {
		/*
		 * the peer has to be formatted as well: HOST;PORT
		 */
		final String[] exploded = peer.split(";");
		if (exploded.length >= 2) {
			InetAddress address;
			try {
				/*
				 * get the receiver of this response
				 */
				address = InetAddress.getByName(exploded[0]);
				final int port = Integer.parseInt(exploded[1]);
				final SocketAddress sa = new InetSocketAddress(address, port);
				final String from = socketToString(this.p.getDhash().getServerInfo());
				final String title = String.format("Subgraph-Response from %s sent to %s",from,peer);
				/*
				 * create the command, and sent to the node, which asked for!
				 */
				final Command c = new ChordCommand(null, title, message, from,null);
				this.p.getDhash().send(c, sa);

			} catch (final UnknownHostException e) {
				Logger.getLogger(this.getClass()).error("Subgraph-Response receiver is unknown:",e);
			} catch (final ConnectException e) {
				Logger.getLogger(this.getClass()).error("Subgraph-Response connection error:",e);
			}
		} else {
			throw new RuntimeException(String.format(
					"Cannot send message to %s, because no valid address in specified format {HOST,PORT}.",
					peer));
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		/*
		 * give the peer address as identification
		 */
		if (this.p != null && this.p.getDhash() != null) {
			return String.format("%s", this.p.getDhash().getIdentifier());
		} else {
			return String.format("%s", this.p);
		}
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IStorage getLocalStorage(final IDistribution<?> distibution) {
		return new ChordlessLocalStorage(this.p, this.p.getDhash().getStorage()) {
			@Override
			public String toString() {
				/*
				 * gives a better identification to the connected peer
				 */
				return super.toString() + " of peer "
						+ Chordless.this.toString();
			}
		}.setDistribution(distibution);
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasLocalStorage() {
		/*
		 * yes chord supports local storage!
		 */
		return true;
	}
}
