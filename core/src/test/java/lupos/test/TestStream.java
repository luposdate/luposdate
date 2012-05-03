package lupos.test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.StreamQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.stream.NotifyStreamResult;
import lupos.engine.operators.stream.Stream;

public class TestStream {

	/**
	 * the frame of the GUI
	 */
	private JFrame frame;
	/**
	 * is used to stop the data stream by clicking on the Ok Button
	 */
	private volatile boolean loop = true;

	public class LabelDemo extends JPanel {
		/**
		 * This label will contain the computed average of the last 10
		 * triples...
		 */
		private final JLabel label;

		/**
		 * create the content of the demo frame
		 */
		public LabelDemo() {
			super();
			final GroupLayout groupLayout = new GroupLayout(this);
			this.setLayout(groupLayout);
			groupLayout.setAutoCreateGaps(true);
			groupLayout.setAutoCreateContainerGaps(true);

			// Create the other labels
			label = new JLabel("Placeholder");

			// Create tool tips
			label.setToolTipText("The computed average of the last 10 triples");

			final JLabel textLabel = new JLabel(
					"The computed average of the last 10 triples: ");

			final JButton okButton = new JButton("Ok");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent ae) {
					frame.dispose();
					loop = false;
				}
			});

			okButton.setSize(okButton.getPreferredSize());

			// Add the labels.
			groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
					.addGroup(
							groupLayout.createParallelGroup().addComponent(
									textLabel).addComponent(label)
									.addComponent(okButton)));

			groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
					.addComponent(textLabel).addComponent(label).addComponent(
							okButton));

		}

		public JLabel getLabel() {
			return label;
		}

	}

	/**
	 * Create the GUI and show it. For thread safety, this method uses the event
	 * dispatch thread.
	 */
	public void createAndShowGUI(final LabelDemo labelDemo) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);

				// Create and set up the window.
				frame = new JFrame("Stream Demo");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				// Add content to the window.
				frame.add(labelDemo);

				// Display the window.
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

	/**
	 * This method generates the data stream and processes the stream query
	 * 
	 * @param stream
	 *            the root operator of the stream query
	 */
	public void generateStream(final Stream stream) {
		stream.sendMessage(new StartOfEvaluationMessage());
		try {
			final Literal s = LiteralFactory.createURILiteral("<s>");
			final Literal p = LiteralFactory.createURILiteral("<p>");
			int i = 0;
			while (loop) {
				final int current = (i >= 100) ? 100 - (i % 100) : i;
				final Literal o = LiteralFactory.createTypedLiteral("\""
						+ current + "\"",
						"<http://www.w3.org/2001/XMLSchema#integer>");
				final Triple triple = new Triple(s, p, o);
				stream.consume(triple);

				i = (i + 1) % 200;
			}
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * This method starts the stream query processing by setting up the
	 * evaluator and starting the stream
	 * 
	 * @param query
	 *            the SPARQL query string
	 * @param notifyStreamResult
	 *            the object which is notified about intermediate stream results
	 * @return the root operator of the stream query
	 */
	public Stream generateQuery(final String query,
			final NotifyStreamResult notifyStreamResult) {
		try {
			final StreamQueryEvaluator evaluator = new StreamQueryEvaluator();
			evaluator.setupArguments();
			Bindings.instanceClass = BindingsMap.class;
			evaluator.getArgs().set("result", QueryResult.TYPE.MEMORY);
			evaluator.getArgs().set("codemap", LiteralFactory.MapType.HASHMAP);
			evaluator.getArgs().set("datastructure", Indices.DATA_STRUCT.HASHMAP);
			evaluator.init();

			evaluator.compileQuery(query);
			evaluator.logicalOptimization();
			evaluator.physicalOptimization();
			if (evaluator.getRootNode() instanceof Stream) {
				final Stream stream = (Stream) evaluator.getRootNode();
				stream.addNotifyStreamResult(notifyStreamResult);
				return stream;
			} else
				System.err.println("No stream query given!");
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method takes over the main job by initializing and starting all
	 */
	public void start() {
		// create and show the GUI
		final LabelDemo labelDemo = new LabelDemo();
		createAndShowGUI(labelDemo);

		// use the following object for retrieving the intermediate result of
		// the stream query
		final NotifyStreamResult notifyStreamResult = new NotifyStreamResult() {
			public void notifyStreamResult(final QueryResult result) {
				labelDemo.getLabel().setText(result.toString());
			}
		};

		// the stream query, which computes an average number of the last 10
		// triples
		final String query = 	"SELECT DISTINCT (avg(?o) as ?avg)\n"
								+ "STREAM INTERMEDIATERESULT DURATION 1000\n" + "WHERE {\n"
								+ "       WINDOW TYPE SLIDINGTRIPLES 10 {?s ?p ?o.} }";

		// now generate the query...
		final Stream stream = generateQuery(query, notifyStreamResult);
		if (stream != null)
			// ... and process the stream query using a generated data stream
			generateStream(stream);
	}

	/**
	 * This method is called when starting this java program
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(final String[] args) {
		new TestStream().start();
	}
}
