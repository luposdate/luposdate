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
package lupos.event.pubsub;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
public class Subscription implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;

	private final static ReentrantLock lock = new ReentrantLock();
	private static int max_id =0;
	private final static String computer_id = computeComputerId();

	private String name;

	private String query;

	/**
	 * Returns a unique id for each computer and probably for each client on this computer.
	 * It fails if two or more clients on the same computer in different vms
	 * call this method within the same millisecond.
	 *
	 * @return a unique id for each computer
	 */
	private static String computeComputerId(){
		String computername="Unknown Host";
		try {
			computername = InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		final Random rn = new Random(System.currentTimeMillis());
		return " (" + computername + ") " + rn.nextLong();
	}

	/**
	 * <p>Constructor for Subscription.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 */
	public Subscription(final String query) {
		this("NO_NAME", query);
	}

	/**
	 * <p>Constructor for Subscription.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param query a {@link java.lang.String} object.
	 */
	public Subscription(final String name, final String query) {
		this();
		this.name = name;
		this.query = query;
	}

	private Subscription() {
		lock.lock();
		try {
			this.id = "ID " + Subscription.max_id + Subscription.computeComputerId();
			Subscription.max_id++;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * <p>Setter for the field <code>name</code>.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(final String name) { this.name = name; }
	/**
	 * <p>Setter for the field <code>query</code>.</p>
	 *
	 * @param query a {@link java.lang.String} object.
	 */
	public void setQuery(final String query) { this.query = query; }

	/**
	 * <p>Getter for the field <code>query</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getQuery() { return this.query; }
	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() { return this.name; }
	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getId() { return this.id; }

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Checks for equality of this Subscription and another Subscription OR a String.
	 * In case of obj being a String, it gets directly compared with the id of this Subscription instance.
	 */
	@Override
	public boolean equals(final Object obj) {
		if(obj instanceof Subscription) {
			final Subscription other = (Subscription)obj;
			return this.id.equals(other.id);
		}
		else if(obj instanceof String) {
			final String str = (String)obj;
			return this.id.equals(str);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
}
