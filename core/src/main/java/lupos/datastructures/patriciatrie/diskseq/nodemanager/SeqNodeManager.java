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
package lupos.datastructures.patriciatrie.diskseq.nodemanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import lupos.datastructures.patriciatrie.disk.nodemanager.NodeInputStream;
import lupos.datastructures.patriciatrie.disk.nodemanager.NodeOutputStream;
import lupos.datastructures.patriciatrie.diskseq.DeSerializer;
import lupos.datastructures.patriciatrie.diskseq.DeSerializer.Writer;
import lupos.datastructures.patriciatrie.node.Node;


/**
 * NodeManager for a DBSeqTrie and its DBSeqNodes
 *
 * @author groppe
 * @version $Id: $Id
 */
public class SeqNodeManager {

	/** Name of the file, that contains the actual trie data */
	protected String fileName;
	
	/** Flag, if this trie contains the complete metadata */
	protected boolean completeMetadata;
	
	/** InputStream */
	protected NodeInputStream inputStream;
	
	/** OutputStream */
	protected NodeOutputStream outputStream;
	
	/**
	 * <p>Constructor for SeqNodeManager.</p>
	 *
	 * @param fileName
	 *            Name of the file, that contains the actual trie data
	 */
	public SeqNodeManager(final String fileName) {
		this.fileName = fileName;
		this.completeMetadata = true;
		this.inputStream = null;
		this.outputStream = null;
	}
	
	/**
	 * Releases the input stream.
	 */
	private void releaseInputStream() {
		if (this.inputStream != null) {
			try {
				this.inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.inputStream = null;
		}
	}
	
	/**
	 * Releases the output stream
	 */
	private void releaseOutputStream() {
		if (this.outputStream != null) {
			try {
				this.outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.outputStream = null;
		}
	}
	
	/**
	 * Prepares the input stream and reads the first byte from the file, which
	 * contains the completeMetadata-flag
	 */
	private void prepareInputStream() {
		try {
			this.releaseInputStream();
			this.releaseOutputStream();
			
			this.inputStream = new NodeInputStream(new BufferedInputStream(new FileInputStream(new File(this.fileName))));
			
			// First bit of each file is the completeMetadata flag
			this.completeMetadata = (this.inputStream.read() == 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prepares the output stream and writes the first byte to the file, which
	 * contains the completeMetadata-flag
	 */
	private void prepareOutputStream() {
		try {
			final File outputFile = new File(this.fileName);
			
			this.releaseInputStream();
			this.releaseOutputStream();
			
			if (outputFile.exists())
				outputFile.delete();
			
			outputFile.createNewFile();
			
			this.outputStream = new NodeOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
			
			// First bit of each file is the completeMetadata flag
			this.outputStream.write(this.completeMetadata ? 1 : 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads the next node from the input stream. If the input stream has not
	 * been initialized before, it will be initialized first.
	 *
	 * @return Node instance
	 * @param deSerializer a {@link lupos.datastructures.patriciatrie.diskseq.DeSerializer} object.
	 */
	public Node readNextNode(final DeSerializer deSerializer) {
		if (this.inputStream == null)
			this.prepareInputStream();
		
		try {
			return deSerializer.deserialize(this, this.inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Removes all entries from the file and resets the output stream
	 */
	public void clear() {
		this.completeMetadata = true;
		this.prepareOutputStream();
	}
	
	/**
	 * Resets the input stream to start reading from the beginning of the file
	 */
	public void readAgain() {
		this.prepareInputStream();
	}
	
	/**
	 * This class is just for the next method:
	 * RandomAccessFile should implement Writer, such that the deSerializer can be called! 
	 */
	public static class WriterRandomAccessFile extends RandomAccessFile implements Writer {
		public WriterRandomAccessFile(File file, String mode)
				throws FileNotFoundException {
			super(file, mode);
		}		
	}
	
	/**
	 * Writes the root node again to update the numberOfEntries stored in the
	 * trie.
	 *
	 * @param rootNode
	 *            rootNode to write again
	 * @param deSerializer a {@link lupos.datastructures.patriciatrie.diskseq.DeSerializer} object.
	 */
	public void writeRootNodeAgain(final DeSerializer deSerializer, final Node rootNode) {
		this.releaseInputStream();
		this.releaseOutputStream();
		
		try {
			final WriterRandomAccessFile file = new WriterRandomAccessFile(new File(this.fileName), "rw");
			
			// Output the content
			try {
				file.write(this.completeMetadata ? 1 : 0);

				deSerializer.serialize(rootNode, file);

			} catch (IOException e) {
				e.printStackTrace();
			}

			
			// ENDE TODO
			
			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Writes the next node to the output stream. If the output stream has not
	 * been initialized yet, it will be initialized as a new file.
	 *
	 * @param node
	 *            Node to store in the trie
	 * @param deSerializer a {@link lupos.datastructures.patriciatrie.diskseq.DeSerializer} object.
	 */
	public void writeNextNode(final DeSerializer deSerializer, final Node node) {

		if (this.outputStream == null){
			this.prepareOutputStream();
		}
		
		// Output the content
		try {
			deSerializer.serialize(node, this.outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes the given node and all of its children to the output stream.
	 *
	 * @param node
	 *            Node to write recursively
	 * @return Number of entries stored in this node and its children
	 * @param deSerializer a {@link lupos.datastructures.patriciatrie.diskseq.DeSerializer} object.
	 */
	public int writeNextNodeRecursive(final DeSerializer deSerializer, final Node node) {
		/*
		 * By recalculation the numberOfEntries from this node, merging of
		 * DBSeqTries, that do not contain the full metadata, can still be
		 * realized. The total entry count is the only relevant value and this
		 * can easily be calculated during the merge process.
		 */
		int numberOfEntries = 0;
		
		this.writeNextNode(deSerializer, node);
		
		if (node.getChildrenLength() == 0)
			numberOfEntries = node.getContentLength();
		
		for (int i = 0, j = node.getChildrenLength(); i < j; i++) {
			if (node.hasChild(i)){
				numberOfEntries += this.writeNextNodeRecursive(deSerializer, node.getChild(i));
			} else {
				numberOfEntries++;
			}
		}

		return numberOfEntries;
	}
	
	/**
	 * Sets the flag completeMetadata
	 *
	 * @param completeMetadata
	 *            Value to set for the flag
	 */
	public void setCompleteMetadata(final boolean completeMetadata) {
		this.completeMetadata = completeMetadata;
	}
	
	/**
	 * <p>hasCompleteMetadata.</p>
	 *
	 * @return <strong>true</strong> if the trie stored in this node manager
	 *         contains all metadata, <strong>false</strong> otherwise
	 */
	public boolean hasCompleteMetadata() {
		return this.completeMetadata;
	}
	
	/**
	 * Closes the NodeManager. Afterwards the NodeManager should not be used
	 * anymore.
	 */
	public void close() {
		this.releaseInputStream();
		this.releaseOutputStream();
	}

	/**
	 * deletes the file on disk
	 */
	public void release() {
		File file = new File(this.fileName);
		if(file.exists()){
			file.delete();
		}
	}
}
