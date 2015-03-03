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
package lupos.gui.anotherSyntaxHighlighting;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.BadLocationException;

/**
 * A reader interface for an abstract document.  Since
 * the syntax highlighting packages only accept Stings and
 * Readers, this must be used.
 * Since the close() method does nothing and a seek() method
 * has been added, this allows us to get some performance
 * improvements through reuse.  It can be used even after the
 * lexer explicitly closes it by seeking to the place that
 * we want to read next, and reseting the lexer.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class LuposDocumentReader extends InputStream {

    /**
     * Modifying the document while the reader is working is like
     * pulling the rug out from under the reader.  Alerting the
     * reader with this method (in a nice thread safe way, this
     * should not be called at the same time as a read) allows
     * the reader to compensate.
     *
     * @param position_parameter a int.
     * @param adjustment a int.
     */
    public void update(final int position_parameter, final int adjustment){
        if (position_parameter < this.position){
            if (this.position < position_parameter - adjustment){
                this.position = position_parameter;
            } else {
                this.position += adjustment;
            }
        }
    }

    /**
     * Current position in the document. Incremented
     * whenever a character is read.
     */
    private long position = 0;

    /**
     * Saved position used in the mark and reset methods.
     */
    private long mark = -1;

    /**
     * The document that we are working with.
     */
    private final LuposDocument document;

    /**
     * Construct a reader on the given document.
     *
     * @param luposDocument the document to be read.
     */
    public LuposDocumentReader(final LuposDocument luposDocument){
    	super();
        this.document = luposDocument;
    }

    /**
     * {@inheritDoc}
     *
     * Has no effect.  This reader can be used even after
     * it has been closed.
     */
    @Override
	public void close() {
    	// no effect
    }

    /**
     * {@inheritDoc}
     *
     * Save a position for reset.
     */
    @Override
	public synchronized void mark(final int readAheadLimit){
        this.mark = this.position;
    }

    /**
     * {@inheritDoc}
     *
     * This reader support mark and reset.
     */
    @Override
	public boolean markSupported(){
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * Read a single character.
     */
    @Override
    public int read(){
        if (this.position < this.document.getLength()){
            try {
                final char c = this.document.getText((int)this.position, 1).charAt(0);
                this.position++;
                return c;
            } catch (final BadLocationException x){
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Read and fill the buffer.
     * This method will always fill the buffer unless the end of the document is reached.
     *
     * @param cbuf the buffer to fill.
     * @return the number of characters read or -1 if no more characters are available in the document.
     */
    public int read(final char[] cbuf){
        return this.read(cbuf, 0, cbuf.length);
    }

    /**
     * Read and fill the buffer.
     * This method will always fill the buffer unless the end of the document is reached.
     */
    public int read(final char[] cbuf, final int off, final int len){
        if (this.position < this.document.getLength()){
            int length = len;
            if (this.position + length >= this.document.getLength()){
                length = this.document.getLength() - (int)this.position;
            }
            if (off + length >= cbuf.length){
                length = cbuf.length - off;
            }
            try {
                final String s = this.document.getText((int)this.position, length);
                this.position += length;
                for (int i=0; i<length; i++){
                    cbuf[off+i] = s.charAt(i);
                }
                return length;
            } catch (final BadLocationException x){
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Reader is always ready.
     *
     * @return true
     */
    public boolean ready() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * Reset this reader to the last mark, or the beginning of the document if a mark has not been set.
     */
    @Override
	public synchronized void reset(){
        if (this.mark == -1){
            this.position = 0;
        } else {
            this.position = this.mark;
        }
        this.mark = -1;
    }

    /**
     * {@inheritDoc}
     *
     * Skip characters of input.
     * This method will always skip the maximum number of characters unless
     * the end of the file is reached.
     */
    @Override
	public long skip(final long n){
        if (this.position + n <= this.document.getLength()){
            this.position += n;
            return n;
        } else {
            final long oldPos = this.position;
            this.position = this.document.getLength();
            return (this.document.getLength() - oldPos);
        }
    }

    /**
     * Seek to the given position in the document.
     *
     * @param n the offset to which to seek.
     */
    public void seek(final long n){
        if (n <= this.document.getLength()){
        	this.position = n;
        }else{
        	this.position = this.document.getLength();
        }
    }

    /**
     * Returns the text written on the LuposDocument. Returns null if the document is bad located.
     *
     * @return Returns the text as a string.
     */
    public String getText(){
    	String text = null;
    	try {
			text = this.document.getText(0, this.document.getLength());
		} catch (final BadLocationException e) {e.printStackTrace();}

		return text;
    }


    /**
     * Returns an input stream which contains the defined area within this LuposDocumentReader's stream.
     * Returns empty stream if begin Offset is greater than the text. Returns stream to end of text if
     * endOffset is greater than the text. Returns empty stream if beginOffset is greater than endOffset.
     *
     * @param beginOffsetParameter The offset of the beginning of the demanded stream.
     * @param endOffsetParameter The offset of the end of the demanded stream.
     * @return The bounded stream.
     */
    public InputStream getStreamWithOffset(final int beginOffsetParameter, final int endOffsetParameter){

    	int beginOffset = beginOffsetParameter;
    	int endOffset = endOffsetParameter;
    	String str = this.getText();

    	if (beginOffset > str.length() || beginOffset == -1) {
    		beginOffset = str.length();
    	}

    	if (endOffset > str.length() || endOffset == -1) {
    		endOffset = str.length();
    	}

    	if(endOffset < beginOffset){
    		endOffset = beginOffset;
    	}

    	str = str.substring(beginOffset, endOffset);

        final InputStream stream = new ByteArrayInputStream(str.getBytes());
    	return stream;
	}


	/**
	 * Returns this stream as a reader.
	 *
	 * @return The reader.
	 */
	public Reader getReader() {

		final String str = this.getText();
		final Reader reader = new StringReader(str);
		return reader;
	}
}
