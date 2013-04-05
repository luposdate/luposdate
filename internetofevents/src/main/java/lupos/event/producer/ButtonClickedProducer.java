/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.event.producer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.event.communication.SerializingMessageService;
import lupos.event.util.Literals;

/**
 * Shows a button and creates an event when it's clicked. 
 */
public class ButtonClickedProducer extends ProducerBase {
	
	public static final String NAMESPACE = "http://localhost/events/ButtonClicked/";
	private final Literal TYPE = Literals.createURI(NAMESPACE, "ButtonClickedEvent");
	
	private boolean buttonClicked = false;
	
	
	public ButtonClickedProducer(SerializingMessageService msgService) {
		super(msgService, 100);
	}
	
	synchronized private void setButtonClicked() {
		this.buttonClicked = true;
	}

	@Override
	synchronized public List<List<Triple>> produce() {
		if(!this.buttonClicked)
			return null;		
		this.buttonClicked = false;
		
		Triple typeTripel = new Triple(Literals.AnonymousLiteral.ANONYMOUS, Literals.RDF.TYPE, this.TYPE);		
		
		return ProducerBase.fold(Arrays.asList(typeTripel));
	}
	
	
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		// create producer
		final ButtonClickedProducer producer = new ButtonClickedProducer(msgService);
		
		// create gui
		JFrame view = new JFrame() {
			{
				this.setMinimumSize(new Dimension(200,100));
		        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		        this.setTitle("ButtonClickProducer");		        
		        getContentPane().setLayout(new BorderLayout());
		        JButton btn = new JButton("Generate Event");
		        btn.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent a) {
						producer.setButtonClicked();
					} });
		        getContentPane().add(btn, BorderLayout.CENTER);
			}
		};
		view.setVisible(true);
		
		producer.start();
	}	
}
