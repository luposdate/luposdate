package lupos.gui.debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.application.CollectRIFResult;
import lupos.engine.operators.singleinput.Result;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.anotherSyntaxHighlighting.javacc.TurtleParser;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.util.ButtonEditor;
import lupos.gui.operatorgraph.util.JTableButtonRenderer;
import lupos.rif.datatypes.EqualityResult;
import lupos.rif.datatypes.Predicate;
import lupos.rif.datatypes.RuleResult;
import lupos.rif.model.Equality;

public class ShowResult extends CollectRIFResult {

	private final EvaluationDemoToolBar evaluationDemoToolbar;
	private CommentLabelElement lastCommentLabelElement = null;
	private final Result result;
	private static final int MAXIMUM_ROW_WIDTH = 400;

	public ShowResult(final EvaluationDemoToolBar evaluationDemoToolbar,
			final Result result) {
		this.evaluationDemoToolbar = evaluationDemoToolbar;
		this.result = result;
	}

	@Override
	public void call(final QueryResult res) {
		if (res != null) {
			final QueryResult resQueryResult = (res instanceof QueryResultDebug) ? ((QueryResultDebug) res)
					.getOriginalQueryResult() : res;
			if (resQueryResult instanceof RuleResult) {
				if (rr == null)
					rr = new RuleResult();
				rr.getPredicateResults().addAll(
						((RuleResult) resQueryResult).getPredicateResults());
				updateCommentPanel();
			} else if (resQueryResult instanceof EqualityResult) {
				if(er==null){
					er = new EqualityResult();
				}
				er.getEqualityResult().addAll(((EqualityResult)resQueryResult).getEqualityResult());
				updateCommentPanel();
			} else if (resQueryResult instanceof GraphResult) {
				if (gr == null)
					gr = new GraphResult(
							((GraphResult) resQueryResult).getTemplate());
				handleGraphResult(res, (GraphResult) resQueryResult);
			} else if (resQueryResult instanceof BooleanResult) {
				if (br_list == null)
					br_list = new LinkedList<BooleanResult>();
				final BooleanResult br = new BooleanResult();
				br_list.add(br);
				final Iterator<Bindings> itb = res.oneTimeIterator();
				if (!itb.hasNext())
					updateCommentPanel();
				else {
					final Bindings b = itb.next();
					qr.add(b);
					updateCommentPanel();
				}
			} else {
				if (qr == null)
					qr = QueryResult.createInstance();
				final Iterator<Bindings> itb = res.oneTimeIterator();
				if (!itb.hasNext())
					updateCommentPanel();
				while (itb.hasNext()) {
					final Bindings b = itb.next();
					qr.add(b);
					updateCommentPanel();
				}
			}
		}
	}

	private void handleGraphResult(final QueryResult res, final GraphResult gr2) {
		final Iterator<Bindings> itb = res.oneTimeIterator();
		if (!itb.hasNext())
			updateCommentPanel();
		int tripleToAddIndex = 0;
		while (itb.hasNext()) {
			itb.next();
			int i = 0;
			for (final Triple t : gr2.getGraphResultTriples()) {
				if (i >= tripleToAddIndex)
					gr.addGraphResultTriple(t);
				i++;
			}
			tripleToAddIndex = gr2.getGraphResultTriples().size();
			updateCommentPanel();
		}
	}

	@Override
	public void deleteResult(final QueryResult res) {
		super.deleteResult(res);
		updateCommentPanel();
	}

	@Override
	public void deleteResult() {
		super.deleteResult();
		updateCommentPanel();
	}

	public void updateCommentPanel() {
		try {
		lastCommentLabelElement = new CommentLabelElement(
				this.evaluationDemoToolbar.getOperatorGraphViewer()
						.getOperatorGraph(), lastCommentLabelElement, result,
				ShowResult.getResultPanel(false, this.getQueryResults(),this.evaluationDemoToolbar
							.getOperatorGraphViewer().getOperatorGraph()
							.getPrefix(), null, this.evaluationDemoToolbar
							.getOperatorGraphViewer().getOperatorGraph()));
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

	}
	
	public static JPanel getResultPanel(final boolean errorsInOntology,
			final QueryResult[] resultQueryEvaluator,
			final lupos.gui.operatorgraph.prefix.Prefix prefixInstance,
			final List<String> resultOrder, final OperatorGraph operatorGraph)
			throws Exception {

		int tables = 0;
		int booleanResults = 0;
		for (final QueryResult qr : resultQueryEvaluator) {
			if (qr instanceof BooleanResult)
				booleanResults++;
			else if (((qr == null || qr.isEmpty()) && !(qr instanceof GraphResult))
					|| (qr instanceof RuleResult && ((RuleResult) qr).isEmpty())
					|| (!(qr instanceof RuleResult)
							&& (qr instanceof GraphResult) && (((GraphResult) qr)
							.getGraphResultTriples() == null || ((GraphResult) qr)
							.getGraphResultTriples().size() == 0)))
				booleanResults++;
			else
				tables++;
		}

		final JLabel[] booleanResultsLabels = new JLabel[booleanResults];
		final JTable[] tablesJTable = new JTable[tables];

		int indexLabels = 0;
		int indexJTables = 0;

		for (final QueryResult qr : resultQueryEvaluator) {
			if (qr instanceof BooleanResult) {
				System.out.println(qr.toString());

				final JLabel l_noResult = new JLabel();
				l_noResult.setBackground(new Color(0, 0, 0, 0));
				l_noResult.setText(qr.toString());

				booleanResultsLabels[indexLabels++] = l_noResult;

			} else if (((qr == null || qr.isEmpty()) && !(qr instanceof GraphResult))
					|| (qr instanceof RuleResult && ((RuleResult) qr)
							.getPredicateResults().size() == 0)
					|| (!(qr instanceof RuleResult)
							&& !(qr instanceof EqualityResult)
							&& (qr instanceof GraphResult) && (((GraphResult) qr)
							.getGraphResultTriples() == null || ((GraphResult) qr)
							.getGraphResultTriples().size() == 0))
					|| (qr instanceof EqualityResult && qr.isEmpty())) {
				System.out.println("no result");

				final JLabel l_noResult = new JLabel();
				l_noResult.setText("No Result");
				l_noResult.setBackground(new Color(0, 0, 0, 0));
				booleanResultsLabels[indexLabels++] = l_noResult;
			} else if (qr instanceof EqualityResult) {
				final EqualityResult er = (EqualityResult) qr;
				final String[] tableHead = new String[] { "", "", "" };
				final Object[][] rows = new Object[er.size()][];
				int i = 0;
				for (final Equality eq : er.getEqualityResult())
					rows[i++] = new Object[] {
							eq.leftExpr.toString(prefixInstance), "=",
							eq.rightExpr.toString(prefixInstance) };
				tablesJTable[indexJTables++] = generateTable(rows, tableHead,
						operatorGraph);
			} else if (qr instanceof RuleResult) {
				final RuleResult gr = (RuleResult) qr;

				int max = 0;
				for (final Predicate p : gr.getPredicateResults()) {
					max = Math.max(max, p.getParameters().size());
				}

				final String[] tableHead = new String[max + 1];
				tableHead[0] = "Predicate";
				for (int i = 1; i < max + 1; i++)
					tableHead[i] = "Arg. " + i;

				final Object[][] rows = new Object[gr.getPredicateResults()
						.size()][];

				int i = 0;

				for (final Predicate p : gr.getPredicateResults()) {

					final Object[] row = new Object[max + 1];

					if (operatorGraph == null) {
						final LuposDocument document = new LuposDocument();
						final LuposJTextPane textPane = new LuposJTextPane(document);
						document.init(TurtleParser.createILuposParser(new LuposDocumentReader(document)), false);
						textPane.setText(p.getName().toString(prefixInstance));
						textPane.setEditable(false);
						row[0] = textPane;
					} else
						row[0] = p.getName().toString(prefixInstance);

					int index = 1;
					for (final Literal l : p.getParameters()) {
						if (operatorGraph == null) {
							final LuposDocument document = new LuposDocument();
							final LuposJTextPane textPane = new LuposJTextPane(document);
							document.init(TurtleParser.createILuposParser(new LuposDocumentReader(document)), false);
							textPane.setText(l.toString(prefixInstance));
							textPane.setEditable(false);

							row[index++] = textPane;
						} else
							row[index++] = l.toString(prefixInstance);
					}

					for (int i2 = index; i2 < max + 1; i2++)
						row[i2] = "";

					rows[i++] = row;
				}

				tablesJTable[indexJTables++] = generateTable(rows, tableHead,
						operatorGraph);
			} else if (qr instanceof GraphResult) {
				final GraphResult gr = (GraphResult) qr;

				final String[] tableHead = { "Subject", "Predicate", "Object" };

				final Object[][] rows = new Object[gr.getGraphResultTriples()
						.size()][];

				int i = 0;

				for (final Triple t : gr.getGraphResultTriples()) {
					if (operatorGraph == null) {
						final LuposDocument documentSubject = new LuposDocument();
						final LuposJTextPane textPaneSubject = new LuposJTextPane(documentSubject);
						documentSubject.init(TurtleParser.createILuposParser(new LuposDocumentReader(documentSubject)), false);
						textPaneSubject.setText(t.getSubject().toString(prefixInstance));
						textPaneSubject.setEditable(false);

						final LuposDocument documentPredicate = new LuposDocument();
						final LuposJTextPane textPanePredicate = new LuposJTextPane(documentPredicate);
						documentPredicate.init(TurtleParser.createILuposParser(new LuposDocumentReader(documentPredicate)), false);
						textPanePredicate.setText(t.getPredicate().toString(prefixInstance));
						textPanePredicate.setEditable(false);

						final LuposDocument documentObject = new LuposDocument();
						final LuposJTextPane textPaneObject = new LuposJTextPane(documentObject);
						documentObject.init(TurtleParser.createILuposParser(new LuposDocumentReader(documentObject)), false);
						textPaneObject.setText(t.getObject().toString(
								prefixInstance));
						textPaneObject.setEditable(false);

						rows[i++] = new Object[] { textPaneSubject,
								textPanePredicate, textPaneObject };
					} else
						rows[i++] = new String[] {
								t.getSubject().toString(prefixInstance),
								t.getPredicate().toString(prefixInstance),
								t.getObject().toString(prefixInstance) };
				}

				tablesJTable[indexJTables++] = generateTable(rows, tableHead,
						operatorGraph);
			} else {
				final HashSet<Variable> variables = new HashSet<Variable>();

				// get variables...
				for (final Bindings ba : qr) {
					variables.addAll(ba.getVariableSet());
				}

				// --- generate table head - begin ---
				final String[] tableHead = new String[variables.size()];

				int i = 0;

				// result order is defined...
				if (resultOrder != null && resultOrder.size() > 0) {
					for (final String s : resultOrder) {
						if (variables.contains(new Variable(s))) {
							tableHead[i++] = "?" + s;
						}
					}
				} else {
					// result order is not defined...
					for (final Variable v : variables) {
						tableHead[i++] = v.toString();
					}
				}
				// --- generate table head - end ---

				// --- generate table rows - begin ---
				final Object[][] rows = new Object[qr.size()][];

				i = 0;

				for (final Bindings ba : qr) {
					final Object[] row = new Object[variables.size()];

					int j = 0;

					// result order is defined...
					if (resultOrder != null && resultOrder.size() > 0) {
						for (final String s : resultOrder) {
							if (variables.contains(new Variable(s))) {
								final Literal literal = ba.get(new Variable(s));
								String value = "";

								if (literal != null) {
									value = literal.toString(prefixInstance);
								}

								if (operatorGraph == null) {
									final LuposDocument document = new LuposDocument();
									final LuposJTextPane textPane = new LuposJTextPane(document);
									document.init(TurtleParser.createILuposParser(new LuposDocumentReader(document)), false);
									textPane.setText(value);
									textPane.setEditable(false);

									row[j++] = textPane;
								} else
									row[j++] = value;
							}
						}
					} else { // result order is not defined...
						for (final Variable variable : variables) {
							final Literal literal = ba.get(variable);
							String value = "";

							if (literal != null) {
								value = literal.toString(prefixInstance);
							}

							if (operatorGraph == null) {
								final LuposDocument document = new LuposDocument();
								final LuposJTextPane textPane = new LuposJTextPane(document);
								document.init(TurtleParser.createILuposParser(new LuposDocumentReader(document)), false);
								textPane.setText(value);
								textPane.setEditable(false);

								row[j++] = textPane;
							} else
								row[j++] = value;
						}
					}

					rows[i++] = row;
				}
				// --- generate table rows - begin ---

				tablesJTable[indexJTables++] = generateTable(rows, tableHead,
						operatorGraph);
			}
		}
		return outputTableResult(errorsInOntology, tablesJTable, booleanResultsLabels,
				operatorGraph == null);
	}

	public static JPanel outputTableResult(boolean errorsInOntology, final JTable[] tables,
			final JLabel[] labels, final boolean transparentColorJTables) {

		final Color transparentColor = new Color(0, 0, 0, 0);

		final JPanel tPanel = new JPanel(new BorderLayout());
		tPanel.setLayout(new BoxLayout(tPanel, BoxLayout.Y_AXIS));
		tPanel.setBackground(transparentColor);

		
		if(errorsInOntology){
			final JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			errorPanel.setBackground(transparentColor);
			final JLabel info = new JLabel("Errors in ontology:");
			info.setBackground(transparentColor);
			errorPanel.add(info);
			tPanel.add(errorPanel);
			addTable(tables[0], transparentColorJTables, transparentColor, tPanel);
		}
		
		final JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		infoPanel.setBackground(transparentColor);
		final JLabel info = new JLabel("Result:");
		info.setBackground(transparentColor);
		infoPanel.add(info);
		tPanel.add(infoPanel);

		for (final JLabel label : labels) {
			label.setBackground(transparentColor);
			infoPanel.add(label);
		}

		boolean firstTime = true;
		for (final JTable table : tables) {
			if(errorsInOntology && firstTime){
				firstTime = false;
				continue;
			}
			addTable(table, transparentColorJTables, transparentColor, tPanel);
		}

		return tPanel;
	}
	
	private static void addTable(final JTable table, final boolean transparentColorJTables, final Color transparentColor, final JPanel tPanel){
		final JPanel tPanel2 = new JPanel(new BorderLayout());
		if (transparentColorJTables) {
			tPanel2.setBackground(transparentColor);
			table.setBackground(transparentColor);
		} else
			tPanel2.setBackground(Color.WHITE);

		tPanel2.add(table.getTableHeader(), BorderLayout.NORTH);
		tPanel2.add(table);

		final JPanel tPanel3 = new JPanel(new BorderLayout());
		tPanel3.setBackground(transparentColor);
		tPanel3.add(tPanel2, BorderLayout.WEST);
		tPanel.add(tPanel3);
	}

	private static JTable generateTable(final Object[][] rows,
			final String[] tableHead, final OperatorGraph operatorGraph) {

		final JTable resultTable;
		if (operatorGraph == null) {
			resultTable = new JTable(new TableModel() {
				private final String[] columns = tableHead;
				private final Object[][] data = rows;

				public void addTableModelListener(final TableModelListener tml) {
				}

				public void removeTableModelListener(
						final TableModelListener tml) {
				}

				public void setValueAt(final Object value, final int row,
						final int col) {
				}

				public Class<?> getColumnClass(final int col) {
					// return this.getValueAt(0, col).getClass();
					return JPanel.class;
				}

				public int getColumnCount() {
					return this.columns.length;
				}

				public String getColumnName(final int col) {
					return this.columns[col];
				}

				public int getRowCount() {
					return this.data.length;
				}

				public Object getValueAt(final int row, final int col) {
					return this.data[row][col];
				}

				public boolean isCellEditable(final int row, final int col) {
					return false;
				}
			});

			resultTable.setDefaultRenderer(
					JPanel.class,
					new JTableButtonRenderer(resultTable
							.getDefaultRenderer(JPanel.class)));
			resultTable.setDefaultEditor(JPanel.class, new ButtonEditor(
					new JCheckBox()));
		} else
			resultTable = new JTable(rows, tableHead);

		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		if (operatorGraph != null)
			CommentLabelElement.updateTable(resultTable, operatorGraph);
		else
			updateTable(resultTable, false);

		return resultTable;
	}
	
	public static void updateTable(final JTable table) {
		updateTable(table, true);
	}
	
	public static void updateTable(final JTable table,
			final boolean considerMaximum) {
		// determine the height of a scrollpane!
		final JScrollPane pane = new JScrollPane();
		pane
		.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		final Dimension d = pane.getPreferredSize();

		int maxHeight = 1;

		for (int i = 0; i < table.getColumnCount(); i++) {
			final JLabel sizeLabel = new JLabel(table.getColumnName(i));
			int maxWidth = sizeLabel.getPreferredSize().width + 2
			* table.getIntercellSpacing().width + 2;
			if (sizeLabel.getPreferredSize().height > maxHeight)
				maxHeight = sizeLabel.getPreferredSize().height;
			for (int j = 0; j < table.getRowCount(); j++) {
				final Object cell = table.getValueAt(j, i);
				if (cell != null) {
					int width;
					int height;
					if (cell instanceof JComponent) {
						final JComponent jcomponent = (JComponent) cell;
						width = jcomponent.getPreferredSize().width + 1;
						height = jcomponent.getPreferredSize().height + 1;
						if (considerMaximum && width > MAXIMUM_ROW_WIDTH) {
							height += d.height;
							width = MAXIMUM_ROW_WIDTH;
						}
					} else {
						final JLabel sizeLabel2 = new JLabel(cell.toString());
						width = sizeLabel2.getPreferredSize().width + 2
						* table.getIntercellSpacing().width + 2
						+ table.getInsets().left
						+ table.getInsets().right;
						height = sizeLabel2.getPreferredSize().height
						+ table.getInsets().top
						+ table.getInsets().bottom;
					}
					if (width > maxWidth)
						maxWidth = width;
					if (height > maxHeight)
						maxHeight = height;
				}
			}
			// table.getColumnModel().getColumn(i).setMinWidth(maxWidth);
			table.getColumnModel().getColumn(i).setPreferredWidth(maxWidth);
		}
		table.setRowHeight(maxHeight);
	}
}
