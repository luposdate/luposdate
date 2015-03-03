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
package lupos.event.consumer.querybuilder;

import lupos.datastructures.items.literal.URILiteral;

/**
 * Represents an event type which is a URI for the event type itself and a list of URIs for its properties.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class EventType {
	URILiteralWrapper eventUri;
	URILiteralWrapper[] properties;
	
	/**
	 * <p>Constructor for EventType.</p>
	 *
	 * @param eventUri a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @param properties a {@link lupos.datastructures.items.literal.URILiteral} object.
	 */
	public EventType(URILiteral eventUri, URILiteral... properties) {
		this.eventUri = new URILiteralWrapper(eventUri);
		this.properties  = new URILiteralWrapper[properties.length];
		for(int i=0; i<properties.length; i++)
			this.properties[i] = new URILiteralWrapper(properties[i]);
	}
	
	/**
	 * <p>Getter for the field <code>eventUri</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.URILiteral} object.
	 */
	public URILiteral getEventUri() {
		return this.eventUri.getWrappedLiteral();
	}
	
	/**
	 * <p>Getter for the field <code>properties</code>.</p>
	 *
	 * @return an array of {@link lupos.event.consumer.querybuilder.URILiteralWrapper} objects.
	 */
	public URILiteralWrapper[] getProperties() {
		return this.properties;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.eventUri.toString();
	}
}
