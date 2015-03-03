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
package lupos.event.action.send;

import lupos.event.consumer.html.Utils;

/**
 * Class for creating a HTML page.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class GenerateHTML implements Send {

	private String name;
	private String outPutFolder;
	
	/**
	 * Constructor.
	 *
	 * @param name
	 *            the name of the file
	 * @param outPutFolder
	 *            the folder to save
	 */
	public GenerateHTML(String name, String outPutFolder) {
		this.name = name;
		this.outPutFolder = outPutFolder;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * For initializing.
	 */
	@Override
	public void init() {
		// no initialization necessary
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * For writing the file with given content.
	 */
	@Override
	public void sendContent(String content) {
		Utils.writeFile(this.outPutFolder + "HTML/" + this.name + ".html", content);
	}	
}
