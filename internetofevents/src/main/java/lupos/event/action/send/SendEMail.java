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
package lupos.event.action.send;

import java.awt.BorderLayout;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * This class is for sending an email 
 */
public class SendEMail implements Send {

	private String to;
	private String from;
	private String host;
	private String user;
	private String password;
	private String subject;
	private final String contentType;
	
	public SendEMail(){
		this("text/plain");
	}
	
	public SendEMail(final String contentType){
		this.contentType = contentType;
	}
	
	@Override
	public void init() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(new JLabel("Subject"));
		JTextField tf_subject = new JTextField("Message Action for");
		panel.add(tf_subject);
		
		panel.add(new JLabel("EMail-Adress of Recipient"));
		JTextField tf_to = new JTextField("xyz@ifis.uni-luebeck.de");
		panel.add(tf_to);
		
		panel.add(new JLabel("EMail-Adress of Sender"));
		JTextField tf_from = new JTextField("xyz@ifis.uni-luebeck.de");
		panel.add(tf_from);
		
		panel.add(new JLabel("Host"));		
		JTextField tf_host = new JTextField("mail.ifis.uni-luebeck.de");
		panel.add(tf_host);
		
		panel.add(new JLabel("User"));
		JTextField tf_user = new JTextField("xyz");
		panel.add(tf_user);
		
		panel.add(new JLabel("Password"), BorderLayout.NORTH);
		
        JPasswordField passwordField = new JPasswordField(10);
        passwordField.setEchoChar('*');
        panel.add(passwordField, BorderLayout.CENTER);
        
        JOptionPane.showMessageDialog(
                null,
                panel,
                "Enter EMail Setup Information",
                JOptionPane.OK_OPTION);
        
        this.to = tf_to.getText();
		this.from = tf_from.getText();
		this.host = tf_host.getText();
		this.user = tf_user.getText();
		this.subject= tf_subject.getText();
        this.password = String.valueOf(passwordField.getPassword());
	}

	@Override
	public void sendContent(final String content) {
	      Properties properties = System.getProperties();
	      
	      properties.setProperty("mail.user", this.user);
	      properties.setProperty("mail.password", this.password);

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", this.host);

	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(properties);

	      try{
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);
	         
	         message.setHeader( "MIME-Version" , "1.0" );

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(this.from));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO, new InternetAddress(this.to));

	         // Set Subject: header field
	         message.setSubject(subject);//+content);

	         // Now set the actual message
	         //message.setText("You receive this email because you have been registered as recipient for events in the internet of events.\nYour notification information is "+content);
	         message.setText(content);
	         
	         // Set the content type since setText() resets to "text/plain"
	         message.setHeader( "Content-Type" , this.contentType); 

	         // Send message
	         Transport.send(message);
	         
	         System.out.println("Sent message successfully....");
	         
	      }catch (MessagingException mex) {
	    	  System.err.println(mex);
	         mex.printStackTrace();
	      }
	}
}
