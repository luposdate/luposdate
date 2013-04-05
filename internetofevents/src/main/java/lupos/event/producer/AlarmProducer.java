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
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.event.producer.ProducerBase;
import lupos.event.util.Literals;
import lupos.event.communication.SerializingMessageService;
import lupos.event.communication.TcpConnectInfo;
import lupos.event.communication.TcpMessageTransport;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

/**
 * Asks for a producer type and lets choose an event to return in the Console.
 * 
 * @author Anderson, Kutzner
 * 
 */
public class AlarmProducer extends ProducerBase {

	// Defines the interval...
	// Waiting is done within the produce method!
	private static final int INTERVAL = 0;

	private boolean alreadySent = false;
	private boolean periodic;
	private long waitingTime;
	private List<List<Triple>> listOfTriples;

	/**
	 * Constructor
	 */
	public AlarmProducer(SerializingMessageService msgService,
			List<List<Triple>> listOfTriples, boolean periodic, long waitingTime)
			throws Exception {
		super(msgService, INTERVAL);
		this.listOfTriples = listOfTriples;
		this.periodic = periodic;
		this.waitingTime = waitingTime;
	}

	@Override
	public List<List<Triple>> produce() {
		if (!periodic && alreadySent) {
			System.exit(0);
		} else if (!periodic) {
			try {
				Thread.sleep(waitingTime);
			} catch (InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			System.out.println("Sending:" + this.listOfTriples);
			alreadySent = true;
			return listOfTriples;
			// return ProducerBase.fold(listOfTriples.get(0));
		} else /** periodic */ { 
			while (true) {
				try {
					Thread.sleep(waitingTime);
				} catch (InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
				System.out.println("Sending:" + this.listOfTriples);
				alreadySent = true;
				return listOfTriples;
				// return ProducerBase.fold(listOfTriples.get(0));	
				
			}
		}
		
		return null;			
	}

	/**
	 * Main-method that starts the AlarmProducer and asks for an event to alarm
	 * to.
	 * 
	 * @param args
	 *            command line parameter
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// create communication channel
		SerializingMessageService msgService = new SerializingMessageService(
				TcpMessageTransport.class);
		msgService.connect(new TcpConnectInfo(
				ProducerBase.askForHostOfBroker(), 4444));

		// create new AlarmWindow
		AlarmWindow aw = new AlarmWindow();

		List<List<Triple>> listOfTriples = aw.getTriples();

		// create new AlarmProducer
		final AlarmProducer ap = new AlarmProducer(msgService, listOfTriples, aw.isPeriodic(), aw.getWaitingTime());

		// start AlarmProducer
		ap.start();
	}

	
	/**
	 * The GUI for the AlarmProducer.
	 */
	public static  class AlarmWindow extends JDialog implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5592407818049740067L;
		// create Panel
		private JPanel windowPanel = new JPanel();
		private JPanel eventPanel = new JPanel();
		private JPanel predicatePanel = new JPanel();
		private JPanel timePanel = new JPanel();
		private JPanel okPanel = new JPanel();
		private JPanel southPanel = new JPanel();

		private JComboBox eventCB;
		private JComboBox periodCB;

		private ButtonGroup buttonGroup;
		
		private JRadioButton oneTimeB;
		private JRadioButton periodB;

		private JButton okB;
		private JButton plusB;

		private JLabel eventL;
		
		private JFormattedTextField dateFTF;
		private JFormattedTextField timeFTF;


	    private boolean periodic = false;
	    private long waitingTime;
	    private List<List<Triple>> listOfTriples = new ArrayList<List<Triple>>();
	    
		
		/**
		 * Constructor of the AlarmWindow
		 */
		public AlarmWindow() {
			super();		
			this.setModalityType(ModalityType.APPLICATION_MODAL);
			this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			windowPanel.setLayout(new BorderLayout());

			
			/**Event Panel*/
			eventPanel.setLayout(new GridLayout(1, 2, 5, 5));

			eventL = new JLabel("Choose an event: ");
			eventPanel.add(eventL);

			// Array for our JComboBox
			String comboBoxEvents[] = { "AlarmEvent", "CountEvent", "MtGoxEvent",
					"SysMonEvent", "TwitterSearchEvent", "ButtonClickedEvent",
					"BTDevicesEvent", "EbayAuctionEvent", "WeatherEvent",
					"MoonEvent" };
			
			// creates JComboBox with Events
			eventCB = new JComboBox(comboBoxEvents);
			eventCB.setEditable(true);
			// add JComboBox to eventPanel
			eventPanel.add(eventCB);
			
			
			/**Predicate Panel*/
			predicatePanel = new JPanel();
			predicatePanel.setLayout(new GridLayout(0, 1, 5, 5));
			predicatePanel.setBorder(BorderFactory.createTitledBorder("Additional Information"));
			PredObjPanel predObj1 = new PredObjPanel();
			PredObjPanel predObj2 = new PredObjPanel();

			// creates a + Button to add new PredObjPanels
			plusB = new JButton("+");
			plusB.addActionListener(this);
			// adds plusButton to the predicatePanel
			predicatePanel.add(plusB);

			// adds two PredicateObjectPanels to the predicatePanel
			predicatePanel.add(predObj1);
			predicatePanel.add(predObj2);

			
			/**Time Panel*/
			timePanel.setLayout(new GridLayout(2, 3, 10, 10));

			// creates and selects the one-time-Button
			oneTimeB = new JRadioButton("once: ", true);
			oneTimeB.addActionListener(this);
			timePanel.add(oneTimeB);

			// creates two Textfields to chose the wanted date and time
		    dateFTF = new JFormattedTextField(new SimpleDateFormat("dd.MM.yyyy"));  
		    dateFTF.setValue(new Date()); // today as standard text 
		    timeFTF = new JFormattedTextField(new SimpleDateFormat("HH:mm"));  
		    timeFTF.setValue(new Date()); // now as standard text
			timePanel.add(dateFTF);
			timePanel.add(timeFTF);    

			// creates the period-Button
			periodB = new JRadioButton("periodic every: ");
			periodB.addActionListener(this);
			timePanel.add(periodB);

			// adds the one-time-Button and the period-Button to the Button Group, so that only one is selected at a time
			buttonGroup = new ButtonGroup();
			buttonGroup.add(oneTimeB);
			buttonGroup.add(periodB);

			// Array for our JComboTime
			String comboBoxTime[] = {"month", "week", "day", "hour", "minute"};
			periodCB = new JComboBox(comboBoxTime);
			timePanel.add(periodCB);
			
			
			/**OK Panel*/
			// creates an ok-Button to confirm the input
			okB = new JButton("OK");
			okB.addActionListener(this);
			okPanel.add(okB);
			
			
			/**South Panel*/
			southPanel.setLayout(new BorderLayout());
			southPanel.add(timePanel, BorderLayout.NORTH);
			southPanel.add(okPanel, BorderLayout.SOUTH);		
			
			
			/** Window Panel*/
			// add Panels to WindowPanel
			windowPanel.add(eventPanel, BorderLayout.NORTH);
			windowPanel.add(predicatePanel, BorderLayout.CENTER);
			windowPanel.add(southPanel, BorderLayout.SOUTH);
			
			
			/**Alarm Window*/
			// add WindowPanel to Frame
			add(windowPanel);
			setTitle("AlarmProducer");
			pack();
			setVisible(true);
		}

		
		
		/**
		 * ActionListener
		 * Function of the PlusButton.
		 * Function of the OkButton.
		 * 
		 * @param e
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == plusB) {
				// add a new PredObjPanel to the predicatePanel
				predicatePanel.add(new PredObjPanel());
				pack();
			} 
			
			
			
			else if (e.getSource() == okB) {
				
				// no error 
				boolean error = false;
				   
			    // check which event is chosen in the CombiBox
			    String chosenEvent = eventCB.getSelectedItem().toString();
			
			   	// creates an array list
				List<Triple> triples = new ArrayList<Triple>();

				Literal type;
				
				// compare the chosenEvent with Strings of Events to know which event one wants to produce
				if (chosenEvent == "AlarmEvent") {
					type = LazyLiteral.getLiteral("<http://localhost/events/Alarm/AlarmEvent>"); 
				}  else if (chosenEvent == "CountEvent") {
					type = LazyLiteral.getLiteral("<http://localhost/events/Count/CountEvent>");
				} else if (chosenEvent == "MtGoxEvent") {
					type = LazyLiteral.getLiteral("<http://localhost/events/MtGox/MtGoxEvent>");
				} else if (chosenEvent == "SysMonEvent") {
					type = LazyLiteral.getLiteral("<http://localhost/events/SysMon/SysMonEvent>");
				} else if (chosenEvent == "TwitterSearchEvent") {
					type = LazyLiteral.getLiteral("<http://localhost/events/TwitterSearch/TwitterSearchEvent>");
				} else if (chosenEvent == "ButtonClickedEvent") {
					type = LazyLiteral.getLiteral("<http://localhost/events/ButtonClicked/ButtonClickedEvent>");
				} else if (chosenEvent == "BTDevicesEvent") {
					type = LazyLiteral.getLiteral("<http://localhost/events/BTDevices/BTDevicesEvent>");
				} else if (chosenEvent == "EbayAuctionEvent") {
					type = LazyLiteral.getLiteral("<http://localhost/events/Ebay/EbayAuctionEvent>");
				} else if (chosenEvent == "WeatherEvent") {
					type = LazyLiteral.getLiteral("<http://localhost/events/Weather/WeatherEvent>");
				} else if (chosenEvent == "MoonEvent") {
					type = LazyLiteral.getLiteral("<http://localhost/events/Moon/MoonEvent>");
				} else {
					type = LazyLiteral.getLiteral(chosenEvent);
				}
			
				// add triples of the chosen event to triples
				triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Literals.RDF.TYPE, type));			
			
				/**
				 * Gets all components from the predicatePanel and checks the validity.
				 */
				for(Component component: predicatePanel.getComponents()){
					if(component instanceof PredObjPanel){
						PredObjPanel pop = (PredObjPanel) component;
						
						Literal s = null;
						Literal k = null;

						// check if predicates in valid format
						try {
							s = this.getLiteral(pop.predTF.getText());
						} catch (ParseException e1) {
							System.err.println("Invalid Predicate!");
						}

						// check if objects in valid format
						try {
							k = this.getLiteral(pop.objTF.getText());
						} catch (ParseException e2) {
							System.err.println("Invalid Object!");
						}

						// checks if one of the predicates or objects was null 
						if (s == null || k == null) {
							
							// error
							error = true;
							// open Error-Massage-Window
							JOptionPane.showMessageDialog(timePanel,
									"Please select valid predicates and objects! "
											+ "\n"
											+ "(In format <predicate> <object>)",
									"Wrong Predicate/Object",
									JOptionPane.ERROR_MESSAGE, null);
							break;

						} else {
							// creates new triple from the Literals and adds them to triples 
							triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, LazyLiteral.getLiteral(pop.predTF.getText()), LazyLiteral.getLiteral(pop.objTF.getText())));
						}
					}
				}
				
				// returns the triples in the console
				System.out.println("triples:" + triples);
				
				
				
				/**
				 * Creates one event at the chosen time.
				 */
				if (oneTimeB.isSelected()) {
					
					// periodic isn't selected
					periodic = false;
					
					// current system date and time
					Date currentDate = new Date(); 
					Date currentTime = new Date(); 
					
					// define the formats of the current system date and time
					SimpleDateFormat dateSDF = new SimpleDateFormat();
				    dateSDF.applyPattern("dd.MM.yyyy");
				    dateSDF.format(currentDate);
					SimpleDateFormat timeSDF = new SimpleDateFormat();
					timeSDF.applyPattern("HH:mm");
				    timeSDF.format(currentTime);			    
				    
				    
				    // parse the inputDate and inputTime
				    String inputDate = (String) dateFTF.getText();
				    String inputTime = (String) timeFTF.getText();
				    SimpleDateFormat inputParser = new SimpleDateFormat("dd.MM.yyyy'T'HH:mm");

				    Date date_out = null;
				    try {
				        date_out = inputParser.parse(inputDate+"T"+inputTime);
				    } catch (final java.text.ParseException e1) {
				        e1.printStackTrace();
				    }

				    
				    // check if time is valid
				    long diff = date_out.getTime()-currentTime.getTime();
				    if(diff<0){
				    	error = true;
				    	// Time over
						JOptionPane.showMessageDialog(timePanel,
			                    "Please select a time in the future!", 
			                    "Wrong time", JOptionPane.ERROR_MESSAGE, null);
					} 
				    
				    // sets the waitingTime
				    waitingTime = diff;
				    
				}
				
				
				/**
				 * Creates events over the chosen period.
				 */			
				else /** periodic Button is selected */ {
					
					periodic = true;

					// check which event is chosen in the CombiBox
					String chosenPeriod = periodCB.getSelectedItem().toString();				
					
					// set waitingTime in Milliseconds
					if (chosenPeriod == "month") {
						waitingTime = 2628000000L;
					} else if (chosenPeriod == "week") {
						waitingTime = 604800000L;					
					} else if (chosenPeriod == "day") {
						waitingTime = 86400000L;					
					} else if (chosenPeriod == "hour") {
						waitingTime = 3600000L;					
					} else {
						waitingTime = 60000L;
					}
					
				}
				
				// no error 
				if(!error){
					// adds triples to the listOfTriples
					listOfTriples.add(triples);
					// make the window invisible
					this.setVisible(false);
				}	
			}
		}

		
		private Literal getLiteral(String content) throws ParseException {
				final SimpleNode node = SPARQL1_1Parser.parseGraphTerm(content, null);
				return LazyLiteral.getLiteral(node, false);
		}
		
		
		/**
		 * returns true if the periodic button is selected, false if the oneTimeButton is selected
		 * 
		 * @return
		 */
		public boolean isPeriodic() {
			return periodic;
		}


		/**
		 * returns the wainting time
		 * 
		 * @return waitingTime
		 */
		public long getWaitingTime() {
			return waitingTime;
		}


		/**
		 * returns a List of triples
		 * 
		 * @return triples
		 */
		public List<List<Triple>> getTriples() {		
			return listOfTriples;
		}
		
	    
	    
	    
	    
	    
	    
	    
		/**
		 * Creates a Panel with textfields to choose a predicate and an object and a button to delete the fields.
		 */
		class PredObjPanel extends JPanel implements ActionListener {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4875871221708827678L;
			private JTextField predTF;
			private JTextField objTF;
			private JButton minusB;

			public PredObjPanel() {
				predTF = new JTextField("<predicate>");
				objTF = new JTextField("<object>");
				minusB = new JButton("-");

				setLayout(new GridLayout(0, 3, 5, 5));

				add(predTF);
				add(objTF);
				add(minusB);

				minusB.addActionListener(this);
			}

			/**
			 * Function of the MinusButton.
			 * 
			 * @param e
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// if minus Button is clicked
				if (e.getSource() == minusB) {
					
					// deletes the selected PredObjPanel
					predicatePanel.remove(this); 
					pack();	
					
				}

			}

		}
	}
}