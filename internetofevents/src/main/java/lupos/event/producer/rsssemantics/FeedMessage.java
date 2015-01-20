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
package lupos.event.producer.rsssemantics;

import java.net.URL;
import java.util.ArrayList;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.util.Literals;

// Einzelne RSS-Message

public class FeedMessage {

  String title;
  String description;
  String link;
  String author;
  String guid;
  private static final String NAMESPACE = "http://www.ifis.uni-luebeck.de/events/RSSSemantics/FeedMessage/";
  public static final URILiteral TYPE = Literals.createURI(FeedMessage.NAMESPACE,"RSSSemanticInterpretationEvent");
  
  public static class Predicates {
	  public static final URILiteral TITLE = Literals.createURI(FeedMessage.NAMESPACE, "Title");
	  public static final URILiteral DESCRIPTION = Literals.createURI(FeedMessage.NAMESPACE, "Description");
	  public static final URILiteral LINK = Literals.createURI(FeedMessage.NAMESPACE, "Link");
	  public static final URILiteral AUTHOR = Literals.createURI(FeedMessage.NAMESPACE, "Author");
	  public static final URILiteral GUID = Literals.createURI(FeedMessage.NAMESPACE, "guid");
  }
  
  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
	  if(!(this.description.equals(""))){
		  return this.description;
	  }
	  else return null;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLink() {
    return this.link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getAuthor() {
    return this.author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getGuid() {
    return this.guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }
  
	public ArrayList<Triple> generateTriples() throws Exception {
			
		// generate literals
		Literal messageTitleObject = Literals.createTyped("",Literals.XSD.String);
		Literal messageDescriptionObject = Literals.createTyped("",Literals.XSD.String);
		Literal messageLinkObject = Literals.createTyped("",Literals.XSD.String);
		Literal messageAuthorObject = Literals.createTyped("",Literals.XSD.String);
		Literal messageGuidObject = Literals.createTyped("",Literals.XSD.String);
		
		messageTitleObject = Literals.createTyped(this.getTitle()+"", Literals.XSD.String);
		messageDescriptionObject = Literals.createTyped(this.getDescription()+"", Literals.XSD.String);
		messageLinkObject = Literals.createTyped(new URL(this.getLink())+"", Literals.XSD.ANYURI);
		messageAuthorObject = Literals.createTyped(this.getAuthor()+"", Literals.XSD.String);
		messageGuidObject = Literals.createTyped(this.getGuid()+"", Literals.XSD.ANYURI);
		
		//generate triples
		Triple messageTypeTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Literals.RDF.TYPE, FeedMessage.TYPE);
		Triple messageTitleTriple = new Triple();
		Triple messageDescriptionTriple = new Triple();
		Triple messageLinkTriple = new Triple();
		Triple messageAuthorTriple = new Triple();
		Triple messageGuidTriple = new Triple();
		
		if(messageTitleObject != Literals.createTyped("",Literals.XSD.String)){
			messageTitleTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.TITLE, messageTitleObject);
		}
		if(messageDescriptionObject != Literals.createTyped("",Literals.XSD.String)){
			messageDescriptionTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.DESCRIPTION, messageDescriptionObject);
		}
		if(messageLinkObject != Literals.createTyped("",Literals.XSD.String)){
			messageLinkTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.LINK, messageLinkObject);
		}
		if(messageAuthorObject != Literals.createTyped("",Literals.XSD.String)){
			messageAuthorTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.AUTHOR, messageAuthorObject);
		}
		if(messageGuidObject != Literals.createTyped("",Literals.XSD.String)){
			messageGuidTriple = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.GUID, messageGuidObject);
		}
		
		// generate Triple List to return
		ArrayList<Triple> t = new ArrayList<Triple>();
		t.add(messageTypeTriple);
		t.add(messageTitleTriple);
		t.add(messageDescriptionTriple);
		t.add(messageLinkTriple);
		t.add(messageAuthorTriple);
		t.add(messageGuidTriple);
		return t;
	}


  
  // Umwandlung in String
  @Override
  public String toString() {
    return "FeedMessage [title=" + this.title + ", description=" + this.description
        + ", link=" + this.link + ", author=" + this.author + ", guid=" + this.guid
        + "]";
  }

} 