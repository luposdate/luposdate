/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.event.gui.querybuilder;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import lupos.datastructures.items.literal.URILiteral;
import lupos.event.producers.BTDevicesProducer;
import lupos.event.producers.ButtonClickedProducer;
import lupos.event.producers.CountProducer;
import lupos.event.producers.DBDelayProducer;
import lupos.event.producers.EventsProducer;
import lupos.event.producers.MtGoxProducer;
import lupos.event.producers.SysMonProducer;
import lupos.event.producers.TwitterSearchProducer;
import lupos.event.producers.WeatherProducer;
import lupos.event.producers.ebay.EbayProducer;
import lupos.event.producers.webpage.LottoProducer;
import lupos.event.producers.webpage.WaterTempProducer;
import lupos.event.producers.webpage.WindFinderProducer;
import lupos.event.util.Literals;



/**
 * A Panel which offers a graphical way to build stream-based SPARQL queries.
 *
 */
public class EventQueryBuilderView extends JPanel {

	/**
	 * Array of available event types //TODO: should not be hard-coded
	 */
	private EventType[] EVENTTYPES = {
			new EventType(Literals.createURI(CountProducer.NAMESPACE,"CountEvent"), Literals.createURI(CountProducer.NAMESPACE, "count")), 
			new EventType(Literals.createURI(MtGoxProducer.NAMESPACE,"MtGoxEvent"), Literals.createURI(MtGoxProducer.NAMESPACE,"lastTrade"), Literals.createURI(MtGoxProducer.NAMESPACE,"bestBid"), Literals.createURI(MtGoxProducer.NAMESPACE,"bestAsk")),
			new EventType(SysMonProducer.TYPE, 
					SysMonProducer.Predicates.UPTIME, 
					SysMonProducer.Predicates.CPU_USAGE,
					SysMonProducer.Predicates.CPU_FREQUENCY,
					SysMonProducer.Predicates.PID,
					SysMonProducer.Predicates.CPUS,
					SysMonProducer.Predicates.OS,
					SysMonProducer.Predicates.TOTAL_PHYSICAL_BYTES,
					SysMonProducer.Predicates.FREE_PHYSICAL_BYTES,
					SysMonProducer.Predicates.TOTAL_SWAP_BYTES,
					SysMonProducer.Predicates.FREE_SWAP_BYTES),
			new EventType(Literals.createURI(TwitterSearchProducer.NAMESPACE,"TwitterSearchEvent"), Literals.createURI("","text"), Literals.createURI("","id")),
			new EventType(Literals.createURI(ButtonClickedProducer.NAMESPACE,"ButtonClickedEvent")),
			new EventType(Literals.createURI(BTDevicesProducer.NAMESPACE,"BTDevicesEvent"), Literals.createURI(BTDevicesProducer.NAMESPACE,"inRange")),
			new EventType(Literals.createURI(EbayProducer.NAMESPACE,"EbayAuctionEvent"), 
					Literals.createURI(EbayProducer.NAMESPACE, "title"), 
					Literals.createURI(EbayProducer.NAMESPACE, "timeLeft"),
					Literals.createURI(EbayProducer.NAMESPACE, "currentPrice"),
					Literals.createURI(EbayProducer.NAMESPACE, "buyItNowPrice"),
					Literals.createURI(EbayProducer.NAMESPACE, "shippingCosts")),
			new EventType(DBDelayProducer.TYPE, 
					DBDelayProducer.TRAIN_NAME,
					DBDelayProducer.STATION_NAME,
					DBDelayProducer.DELAY,
					DBDelayProducer.DELAY_CAUSE,
					DBDelayProducer.LATITUDE,
					DBDelayProducer.LONGITUDE),
			new EventType(WeatherProducer.Predicates.TYPE, 
					WeatherProducer.Predicates.CURRENT_WEATHER, 
					WeatherProducer.Predicates.TEMPERATURE, 
					WeatherProducer.Predicates.FEELSLIKE, 
					WeatherProducer.Predicates.HUMIDITY, 
					WeatherProducer.Predicates.WIND,
					WeatherProducer.Predicates.OBSERVATION_TIME,
					WeatherProducer.Predicates.CURRENT_TIME,
					WeatherProducer.Predicates.ZT_SHORT,
					WeatherProducer.Predicates.ZT_LONG,
					WeatherProducer.Predicates.WIND_DEGREE,
					WeatherProducer.Predicates.WIND_KPH,
					WeatherProducer.Predicates.WIND_GUST_KPH,
					WeatherProducer.Predicates.PRESSURE_MB,
					WeatherProducer.Predicates.PRESSURE_IN,
					WeatherProducer.Predicates.DEWPOINT,
					WeatherProducer.Predicates.HEAT_INDEX,
					WeatherProducer.Predicates.WINDCHILL,
					WeatherProducer.Predicates.VISIBILITY_MI,
					WeatherProducer.Predicates.VISIBILITY_KM,
					WeatherProducer.Predicates.SOLARRADIATION,
					WeatherProducer.Predicates.UV,
					WeatherProducer.Predicates.PRECIP_1HR,
					WeatherProducer.Predicates.PRECIP_TODAY,
					WeatherProducer.Predicates.ICON,
					WeatherProducer.Predicates.ICON_URL,
					WeatherProducer.Predicates.FORECAST_URL,
					WeatherProducer.Predicates.HISTORY_URL,
					WeatherProducer.Predicates.OB_URL,
					WeatherProducer.Predicates.CITY_NAME,
					WeatherProducer.Predicates.COUNTRY_NAME,
					WeatherProducer.Predicates.COUNTRYCODE,
					WeatherProducer.Predicates.LATITUDE,
					WeatherProducer.Predicates.LONGITUDE,
					WeatherProducer.Predicates.ELEVATION,
					WeatherProducer.Predicates.CURRENT_CITY,
					WeatherProducer.Predicates.COUNTRY2CODE,
					WeatherProducer.Predicates.LATITUDE2,
					WeatherProducer.Predicates.LONGITUDE2,
					WeatherProducer.Predicates.ELEVATION2),
			new EventType(WindFinderProducer.TYPE, 
					WindFinderProducer.TIME,
					WindFinderProducer.WIND_DIRECTION,
					WindFinderProducer.WIND_SPEED,
					WindFinderProducer.PRECIPITATION,
					WindFinderProducer.PRESSURE,
					WindFinderProducer.TEMPERATURE),
			new EventType(WaterTempProducer.TYPE, 
					WaterTempProducer.TIME,
					WaterTempProducer.TEMP),
			new EventType(LottoProducer.TYPE, 
					LottoProducer.SIX_FROM_FOURTYNINE_1,
					LottoProducer.SIX_FROM_FOURTYNINE_2,
					LottoProducer.SIX_FROM_FOURTYNINE_3,
					LottoProducer.SIX_FROM_FOURTYNINE_4,
					LottoProducer.SIX_FROM_FOURTYNINE_5,
					LottoProducer.SIX_FROM_FOURTYNINE_6,
					LottoProducer.ZUSATZZAHL,
					LottoProducer.SUPERZAHL),
			new EventType(EventsProducer.EVENT_TYPE_OBJECT, 
					EventsProducer.HEADLINER_PREDICATE, 
					EventsProducer.ID_PREDICATE,
					EventsProducer.TITLE_PREDICATE,
					EventsProducer.START_PREDICATE,
					EventsProducer.NAME_PREDICATE,
					EventsProducer.STREET_PREDICATE,
					EventsProducer.POSTALCODE_PREDICATE, 
					EventsProducer.CITY_PREDICATE,
					EventsProducer.DESCRIPTION_PREDICATE,
					EventsProducer.URL_PREDICATE,
					EventsProducer.IMAGE_PREDICATE)
	};	
	
	private final List<EventWindowView> eventWindows = new ArrayList<EventWindowView>();
	
	private JSpinner intermediateSpinner;
	private JPanel eventsPanel;
	
	
	public EventQueryBuilderView() {
		initComponents();
	}
	
	private void initComponents() {
        
		super.setLayout(new GridBagLayout());
        
        JPanel mainPane = new JPanel(new GridBagLayout());
        
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new Insets(8, 4, 8, 4);
		mainPane.add(new JLabel("Compute new:"), c);
		
		SpinnerModel intermediateModel = new SpinnerNumberModel(1000,
				100, // min
				120000, // max (120000ms = 2min)
				100); // step
		this.intermediateSpinner = new JSpinner(intermediateModel);
		
		JPanel intermediatePane = new JPanel();
		intermediatePane.setLayout(new BoxLayout(intermediatePane, BoxLayout.LINE_AXIS));

		intermediatePane.add(this.intermediateSpinner);
		intermediatePane.add(Box.createRigidArea(new Dimension(3,0)));
		intermediatePane.add(new JLabel("ms"));
		
		c.gridx = 1;
		c.anchor = GridBagConstraints.LINE_START;
		mainPane.add(intermediatePane, c);
		

        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPane.add(new JSeparator(SwingConstants.HORIZONTAL), c);
        
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        
        this.eventsPanel = new JPanel();
        this.eventsPanel.setLayout(new BoxLayout(this.eventsPanel, BoxLayout.Y_AXIS));        
		
        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        mainPane.add(this.eventsPanel, c);
        
        JButton addEventWindowButton = new JButton("Add Event");
        addEventWindowButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				addEventWindow();
			}
		});
        
        c.gridy = 4;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.NONE;
        mainPane.add(addEventWindowButton, c);        
        
		GridBagConstraints c2 = new GridBagConstraints();
		c2.anchor = GridBagConstraints.FIRST_LINE_START;
		c2.fill = GridBagConstraints.BOTH;
		c2.weightx = 1.0;
		c2.weighty = 1.0;
		c2.insets = new Insets(8, 8, 8, 8);
        super.add(mainPane, c2);
   	}
	
	private void addEventWindow() {
		final EventWindowView ewv = new EventWindowView(this.EVENTTYPES);
		ewv.addRemoveActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				removeEventWindow(ewv);
			}
		});
		this.eventsPanel.add(ewv);
		this.eventsPanel.updateUI();
		this.eventWindows.add(ewv);
	}
	
	private void removeEventWindow(EventWindowView ewv) {
		this.eventsPanel.remove(ewv);
		this.eventsPanel.updateUI();
		this.eventWindows.remove(ewv);
	}
	
	public String getQuery() {
		int imd = (Integer)this.intermediateSpinner.getValue();
		
		List<EventWindowData> eventWindowDataList = new ArrayList<EventWindowData>();
		for(EventWindowView v : this.eventWindows) {
			if(v.getEventType() == null)
				break;
			
			EventWindowData d = new EventWindowData();
			d.eventType = v.getEventType();
			d.winOp = v.getWinOp();
			d.winParam = v.getWinParam();
			d.propertyFilterData = v.getPropertyFilterData();
			eventWindowDataList.add(d);
		}
		
		EventQueryBuilder builder = new EventQueryBuilder();
		return builder.buildQuery(imd, eventWindowDataList);
	}
	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Query Generator");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(450,450));
        frame.setPreferredSize(new Dimension(350,400));

        final EventQueryBuilderView queryBuilderView = new EventQueryBuilderView();
        
	    // Query TextArea
	    final JTextArea queryTextArea = new JTextArea();
	    //queryTextArea.setColumns(20);
	    queryTextArea.setTabSize(2);
	    queryTextArea.setLineWrap(true);
	    queryTextArea.setRows(7);
	    queryTextArea.setWrapStyleWord(true);
	    queryTextArea.setEditable(true);
	    JScrollPane queryTextAreaScrollPane = new JScrollPane(queryTextArea);
	    queryTextAreaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        JButton button = new JButton("Generate Query");
        button.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				queryTextArea.setText(queryBuilderView.getQuery());
			}
		});
        
        
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(queryBuilderView);
        panel.add(button);
        panel.add(button);
        
        frame.add(panel);
        frame.setVisible(true);		
	}
}
