/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.gui.anotherSyntaxHighlighting;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import lupos.datastructures.parallel.BoundedBuffer;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE.TYPE_ENUM;
import lupos.misc.Tuple;

public class Colorer extends Thread {
	
	private final ILuposParser parser;
	private final LuposDocumentReader luposDocumentReader;
	private final LuposDocument doc;
	private final TreeMap<Integer, ILuposToken> tokens = new TreeMap<Integer, ILuposToken>();
	private final int WAITINGTIME;
	private final boolean startThread;
	
	private BoundedBuffer<UpdateEvent> buffer = new BoundedBuffer<UpdateEvent>();
	
	private static class UpdateEvent{
		public final boolean insert;
		public final int offset;
		public final int end;
		
		public UpdateEvent(final boolean insert, final int offset, final int end){
			this.insert = insert;
			this.offset = offset;
			this.end = end;
		}
	}
		
	public Colorer(final LuposDocument doc, final ILuposParser parser, final int WAITINGTIME){		
		this(doc, parser, WAITINGTIME, true);
	}
	
	public Colorer(final LuposDocument doc, final ILuposParser parser, final int WAITINGTIME, final boolean startThread){		
		this.parser = parser;
		this.doc = doc;
		this.doc.text.addKeyListener(new KeyListener(){
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				synchronized(Colorer.this){}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}			
		});
		this.luposDocumentReader = new LuposDocumentReader(doc);
		this.startThread = startThread;
		this.WAITINGTIME = WAITINGTIME;
		if(startThread){
			this.start();
		}
	}
	
	private void transmitUpdateEvent(final boolean insert, final int offset, final int end){
		if(this.startThread){
			try {
//				System.out.println(insert+" "+offset+" "+end);
				this.buffer.put(new UpdateEvent(insert, offset, end));
			} catch (InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}
	
	public void transmitInsertEvent(final int offset, final int end){
		this.transmitUpdateEvent(true, offset, end);
	}

	public void transmitRemoveEvent(final int offset, final int end){
		this.transmitUpdateEvent(false, offset, end);
	}
	
	public void colorOneTime(){
		synchronized (this) {
			this.tokens.clear();
			final String content = this.luposDocumentReader.getText();
			forceHighlightingInnerLoop(0, content.length(), content);
		}
	}
	
	@Override
	public void finalize(){
		this.buffer.endOfData();
	}
	
	@Override
	public void run() {
		try {
			UpdateEvent nextEvent = null; 
			do {
				LinkedList<Tuple<Integer, Integer>> areas = new LinkedList<Tuple<Integer, Integer>>();
				nextEvent = eventHandling(areas, nextEvent);
				forceHighlighting(areas, nextEvent);
				Thread.sleep(this.WAITINGTIME);
			} while(true);
		} catch (InterruptedException e) {
			System.err.println();
			e.printStackTrace();
		}
	}

	private UpdateEvent eventHandling(final List<Tuple<Integer, Integer>> areas, UpdateEvent nextEvent){
		try {
			do {
				UpdateEvent updateEvent = (nextEvent!=null)? nextEvent : this.buffer.get();
				nextEvent = null;
				if(updateEvent==null){
					return null;
				}
				// merge subsequent insertions and removals!
				while(!this.buffer.isCurrentlyEmpty()){
					nextEvent = this.buffer.get();
					UpdateEvent mergedUpdateEvent = mergeUpdateEvent(updateEvent, nextEvent);
					if(mergedUpdateEvent!=null){
						updateEvent = mergedUpdateEvent;
						nextEvent = null;
					}
				}
				final int offset = updateEvent.offset;
				final int end = updateEvent.end;
				if(updateEvent.insert){
					moveTokens(offset, end);
					if(!moveAreas(areas, offset, end)) {
						areas.add(new Tuple<Integer, Integer>(offset, end));
					}						
				} else {
					removeTokens(offset, end);
					if(!removeAreas(areas, offset, end)) {
						areas.add(new Tuple<Integer, Integer>(offset, offset));
					}
				}
				Thread.sleep(this.WAITINGTIME);
			} while(nextEvent!=null || !this.buffer.isCurrentlyEmpty());
		} catch (InterruptedException e) {
			System.err.println();
			e.printStackTrace();
		}
		return nextEvent;
	}
	
	private UpdateEvent forceHighlighting(final List<Tuple<Integer, Integer>> areas, UpdateEvent nextEvent){
		synchronized (this) {
			if(nextEvent!=null || !this.buffer.isCurrentlyEmpty()){
				// if currently some modifications have been done (should be few),
				// then handle these events such that all has been correctly updated!
				// new events should not occur because of the lock in the key listener
				nextEvent = eventHandling(areas, nextEvent);
			}
			this.doc.text.setRepaint(false);
			final String content = this.luposDocumentReader.getText();
			for(Tuple<Integer, Integer> area: areas){
					// determine start for rescanning:
					// rescan whole line, thus determine line start
					
					forceHighlightingInnerLoop(area.getFirst(), area.getSecond(), content);						
			}
			
			// However, this is wrong for tokens spanning over several lines
			// For this, rescanning is started for each line with an error token before offset
			// This could theoretically lead also to errors, but should not occur in practical cases
			// (Theoretical case: Insert token c between token a and b, and a b c is also (another) token (which is spanning over several lines))			

			LinkedList<ILuposToken> errorTokens = new LinkedList<ILuposToken>(); 
			for(ILuposToken token: this.tokens.values()){
				if(token.getDescription().isError()){
					errorTokens.add(token);
				}
			}
			
			int lastEnd = -1;
			for(ILuposToken token: errorTokens){
				final int beginChar = token.getBeginChar();
				if(lastEnd<beginChar){ // rescan only if not already done...
					lastEnd = forceHighlightingInnerLoop(beginChar, beginChar + token.getContents().length(), content);
				}
			}
			this.doc.text.setRepaint(true);
			this.doc.text.repaint();
		}
		return nextEvent;
	}
	
	private boolean removeAreas(final List<Tuple<Integer, Integer>> areas,
			final int offset, final int end) {
		// returns true is new area is already included!
		final int delta = end - offset;
		boolean result = false;
		for(Tuple<Integer, Integer> area: areas){
			if(area.getSecond()<offset){
				continue;
			} else if(area.getFirst()>end){
				area.setFirst(area.getFirst() - delta);
				area.setSecond(area.getSecond() - delta);
			} else {
				result = true;
				final int originalFirst = area.getFirst();
				area.setFirst(Math.min(originalFirst, offset));
				if(offset>=originalFirst && end<=area.getSecond()){
					area.setSecond(area.getSecond()-delta);
				} else if(offset<=originalFirst && end>=area.getSecond()) { 
					area.setSecond(offset);
				} else if(originalFirst<=offset) {
					area.setSecond(area.getSecond()-offset);
				} else {
					area.setSecond(area.getSecond()-end);
				}
			}
		}
		return result;		
	}

	private boolean moveAreas(List<Tuple<Integer, Integer>> areas,
			int offset, int end) {
		// returns true is new area is already included!
		final int delta = end - offset;
		boolean result = false;
		for(Tuple<Integer, Integer> area: areas){
			if(area.getSecond()<offset){
				continue;
			} else if(area.getFirst()>end){
				area.setFirst(area.getFirst() + delta);
				area.setSecond(area.getSecond() + delta);
			} else {
				result = true;
				area.setFirst(Math.min(area.getFirst(), offset));
				area.setSecond(Math.max(area.getSecond()+delta, end));
			}
		}
		return result;
	}

	private UpdateEvent mergeUpdateEvent(final UpdateEvent updateEvent, final UpdateEvent nextEvent){
		if(updateEvent.insert == nextEvent.insert){
			if(updateEvent.insert){
				// both events are insert events!
				if(updateEvent.end==nextEvent.offset){
//					System.out.println("1: merge to "+updateEvent.offset+"->"+nextEvent.end);
					return new UpdateEvent(true, updateEvent.offset, nextEvent.end);
				} else if(nextEvent.end==updateEvent.offset){
//					System.out.println("2: merge to "+nextEvent.offset+"->"+updateEvent.end);
					return new UpdateEvent(true, nextEvent.offset, updateEvent.end);
				}
			} else {
				// both events are remove events!	
				if(updateEvent.offset==nextEvent.offset){
//					System.out.println("3: merge to "+updateEvent.offset+"->"+(updateEvent.end + (nextEvent.end - nextEvent.offset)));
					return new UpdateEvent(false, updateEvent.offset, updateEvent.end + (nextEvent.end - nextEvent.offset));
				} else if(updateEvent.end==nextEvent.offset){
//					System.out.println("1: merge to "+updateEvent.offset+"->"+nextEvent.end);
					return new UpdateEvent(false, updateEvent.offset, nextEvent.end);
				} else if(nextEvent.end==updateEvent.offset){
//					System.out.println("2: merge to "+nextEvent.offset+"->"+updateEvent.end);
					return new UpdateEvent(false, nextEvent.offset, updateEvent.end);
				}
			}
		}
		return null;
	}

	private void moveTokens(int offset, int end){
		synchronized (this) {			
			Integer floorKey = this.tokens.floorKey(offset);
			if(floorKey==null){
				floorKey = offset;
			}			
			NavigableMap<Integer, ILuposToken> tailMap = this.tokens.tailMap(floorKey, true);
			LinkedList<Integer> toDelete = new LinkedList<Integer>();
			LinkedList<ILuposToken> toAdd = new LinkedList<ILuposToken>();
			final int length = end-offset;
			for(Entry<Integer, ILuposToken> entry: tailMap.entrySet()){
				ILuposToken oldToken = entry.getValue();
				final int beginCharOldToken = entry.getKey(); 			
				if(beginCharOldToken>=offset){
					toDelete.add(beginCharOldToken);
					toAdd.add(oldToken.create(oldToken.getDescription(), oldToken.getContents(), beginCharOldToken+length));
					continue;
				}
				final int endCharOldToken = beginCharOldToken + oldToken.getContents().length();
				if(endCharOldToken>offset){
					toDelete.add(beginCharOldToken);
					final String oldContent = oldToken.getContents();
					final int firstBorder = offset-beginCharOldToken;
					if(firstBorder>0){
						toAdd.add(oldToken.create(oldToken.getDescription().getErrorEnum(), oldContent.substring(0,offset-beginCharOldToken), beginCharOldToken));
					}
					final int secondBorder = offset-beginCharOldToken;
					if(offset-beginCharOldToken>0 && secondBorder<oldContent.length()){
						toAdd.add(oldToken.create(oldToken.getDescription().getErrorEnum(), oldContent.substring(secondBorder), end));
					}
				}
			}
			for(Integer indexOldToken: toDelete){
				this.tokens.remove(indexOldToken);
			}
			for(ILuposToken newToken: toAdd){
				this.tokens.put(newToken.getBeginChar(), newToken);
	//			this.doc.setCharacterAttributes(newToken.getBeginChar(), newToken.getContents().length(), LANGUAGE.getAttributeSet(newToken.getDescription()), true);
			}
		}
	}
	
	private void removeTokens(int offset, int end){
		synchronized (this) {			
			Integer floorKey = this.tokens.floorKey(offset);
			if(floorKey==null){
				floorKey = offset;
			}
			final int contentLenght = this.doc.text.getText().length();
			NavigableMap<Integer, ILuposToken> tailMap = this.tokens.tailMap(floorKey, true);
			LinkedList<Integer> toDelete = new LinkedList<Integer>();
			LinkedList<ILuposToken> toAdd = new LinkedList<ILuposToken>();
			final int length = end-offset;
			for(Entry<Integer, ILuposToken> entry: tailMap.entrySet()){
				ILuposToken oldToken = entry.getValue();			
				final int beginCharOldToken = entry.getKey();
				final int endCharOldToken = beginCharOldToken + oldToken.getContents().length();
				if(beginCharOldToken>=offset && endCharOldToken<=end){
					toDelete.add(beginCharOldToken);
					continue;
				}
				if(beginCharOldToken>=end){
					toDelete.add(beginCharOldToken);
					if(beginCharOldToken<contentLenght) {
						toAdd.add(oldToken.create(oldToken.getDescription(), oldToken.getContents(), beginCharOldToken-length));
					}
					continue;
				}
				if(endCharOldToken>offset){
					toDelete.add(beginCharOldToken);
					final String oldContent = oldToken.getContents();
					final int firstBorder = offset-beginCharOldToken; 
					if(firstBorder>0){
						toAdd.add(oldToken.create(oldToken.getDescription().getErrorEnum(), oldContent.substring(0, firstBorder), beginCharOldToken));
					}
					final int secondBorder = end - beginCharOldToken; // oldContent.length()-(endCharOldToken-end);
					if(secondBorder>0 && secondBorder<oldContent.length()){
						toAdd.add(oldToken.create(oldToken.getDescription().getErrorEnum(), oldContent.substring(secondBorder), offset));
					}
				}
			}
			for(Integer indexOldToken: toDelete){
				this.tokens.remove(indexOldToken);
			}
			for(ILuposToken newToken: toAdd){
				this.tokens.put(newToken.getBeginChar(), newToken);
	//			this.doc.setCharacterAttributes(newToken.getBeginChar(), newToken.getContents().length(), LANGUAGE.getAttributeSet(newToken.getDescription()), true);
			}
		}
	}
	
	private int forceHighlightingInnerLoop(int offset, int offsetEnd, final String content){
		final boolean firstTime = this.tokens.isEmpty(); 		
		int start = offset;
		// after end of text?
		final int contentLength = content.length();
		if(start>=contentLength){
			start = contentLength-1;
		}
		if(start<0){
			start=0;
		}
		while(start>0 && content.charAt(start)!='\n'){
			start--;
		}
		// or did we do something in a token spanning over several lines?
		// just determine the previous token and check its borders...
		Entry<Integer, ILuposToken> floorOffsetEntry = this.tokens.floorEntry(start);
		if(floorOffsetEntry!=null) {
			final int startCandidate = floorOffsetEntry.getKey();
			if(startCandidate<start && startCandidate + floorOffsetEntry.getValue().getContents().length()>=start){
				start = startCandidate;
			}
		}
		
		int result = start;
		
		try {
			ILuposToken token;
			this.parser.setReaderTokenFriendly(this.luposDocumentReader, start, content.length());
			ILuposToken previousToken = null;
			ILuposToken nextToken = null;
			do {
				token = (nextToken!=null)? nextToken : parser.getNextToken(content);
				nextToken = null;
								
				if(token!=null){
					
					if(previousToken!=null){
						// check if the two tokens combined are an error
						if(previousToken.getBeginChar()+previousToken.getContents().length()==token.getBeginChar()){
							// there is no space between them => check them further...
							// it looks like it is a problem (for SPARQL queries/RIF rules/RDF data) if e.g. two reserved words are written together...
							// decision is made in TYPE_ENUM classes...
							if(token.getDescription().errorWhenDirectlyFollowingToken(previousToken.getDescription())){
								token = token.create(token.getDescription().getErrorEnum(), previousToken.getContents()+token.getContents(), previousToken.getBeginChar());
							}
						}
					}
					
					// some scanners are not perfect: With this, we can allow to combine two tokens into one
					TYPE_ENUM typeToBeCombined = token.getDescription().combineWith(token.getContents());
					if(typeToBeCombined!=null){
						ILuposToken nextToken2 = parser.getNextToken(content);
						if(nextToken2!=null){
							if(nextToken2.getDescription() == typeToBeCombined){
								token = token.create(token.getDescription(), content.substring(token.getBeginChar(), nextToken2.getBeginChar()+nextToken2.getContents().length()), token.getBeginChar());
							} else {
								token = token.create(token.getDescription().getErrorEnum(), content.substring(token.getBeginChar(), nextToken2.getBeginChar()+nextToken2.getContents().length()), token.getBeginChar());
							}
						}
					}
					
					// make some type of context-sensitive check for errors:
					// determine the next token and check if it is in the expected set of tokens of the current token
					// (otherwise e.g. the RIF scanners seldom detects errors)
					Set<ILuposToken> expected = token.getDescription().expectedNextTokens();
					if(expected!=null){
						nextToken = parser.getNextToken(content);
						if(nextToken!=null){
							boolean flagFound = false;
							for(ILuposToken expectedToken: expected){
								if(expectedToken.getDescription() == nextToken.getDescription()){
									if(expectedToken.getContents().equals("") || expectedToken.getContents().compareTo(nextToken.getContents())==0){
										flagFound = true;
										break;
									}
								}
							}
							if(!flagFound){
								token = token.create(token.getDescription().getErrorEnum(), token.getContents(), token.getBeginChar());						
							}
						} else {
							token = token.create(token.getDescription().getErrorEnum(), token.getContents(), token.getBeginChar());						
						}
					}
					
					boolean flag = false;
					final int beginChar = token.getBeginChar();
					final int endChar = beginChar + token.getContents().length();
					if(!firstTime){
						// check tokens for old tokens, which overlap with the new one and remove those ones.
						Entry<Integer, ILuposToken> floorEntry;
						do {
							floorEntry = this.tokens.floorEntry(endChar-1);
							if(floorEntry!=null){
								ILuposToken oldToken = floorEntry.getValue();
								final int beginCharPreviousToken = oldToken.getBeginChar();
								if(beginCharPreviousToken == beginChar && oldToken.getContents().compareTo(token.getContents())==0){
									// case scanned token and old token are the same!									
									flag = true;
									break;
								} else if(beginCharPreviousToken + oldToken.getContents().length()>beginChar){
									// case overlapping token => remove old token!
									this.tokens.remove(floorEntry.getKey());
								} else {
									break;
								}
							}
						} while(floorEntry!=null);
					}
					
					this.tokens.put(beginChar, token);
					// System.out.println(token);
					
					final int length = token.getContents().length();
					
					this.doc.setCharacterAttributes(beginChar, length, LANGUAGE.getAttributeSet(token.getDescription()), true);
					
					result = beginChar + length;
					
					if(beginChar>offsetEnd && flag){
						break;
					}
					previousToken = token;
				}

			} while(token!=null);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex);
		}
		return result;
	}
}
