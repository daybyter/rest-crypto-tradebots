/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.ui;

import de.andreas_rueckert.trade.app.action.ActionLoadRuleSet;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.bot.rule.RuleBot;
import de.andreas_rueckert.trade.bot.rule.RuleSetFile;
import de.andreas_rueckert.trade.bot.TradeBot;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;


/**
 * This class implements a J2EE GUI for the RuleBot class.
 */
public class RuleBotUI implements ActionListener, TradeBotUI {

    // Inner classes

    /**
     * A model to display the loaded rule sets in a table.
     */
    class RuleSetTableModel extends AbstractTableModel {  

	// Instance variables

	/**
	 * The names of the columns.
	 */
	private String [] _columnNames = { "Rule sets" };

	/**
	 * The list of rule sets.
	 */
	private ArrayList<RuleSetFile> _tradeRules;


	// Constructors

	/**
	 * Create a new trade rule table model instance.
	 *
	 * @param tradeRules A list of trade rule files.
	 */
	public RuleSetTableModel( ArrayList<RuleSetFile> tradeRules) {

	    // Store the trade rules in the table model.
	    _tradeRules = tradeRules;
	}


	// Methods

	/**
	 * Get the class for a given column.
	 *
	 * @param column The column to query.
	 *
	 * @return The class for the data of the given column.
	 */
	public Class getColumnClass( int column) {
	    return getValueAt( 0, column).getClass();
	}

	/**
	 * Get the number of columns.
	 */
	public int getColumnCount() {
	   
	    return _columnNames.length;
	}

	/**
	 * Get the name of a given column.
	 *
	 * @param column The column to query.
	 *
	 * @return The column name of the given column.
	 */
	public String getColumnName( int column) {

	    return _columnNames[ column];
	}

	/**
	 * Get the number of rows.
	 */
	public int getRowCount() {

	    return _tradeRules.size();
	}

	/**
	 * Get the value of a given cell position.
	 *
	 * @param row The row index to query.
	 * @param column The column index to query.
	 *
	 * @return The value for this cell.
	 */
	public Object getValueAt( int row, int column) {

	    // Column should be always 0 for now.
	    return _tradeRules.get( row).getRuleSetName();
	}

	/**
	 * Query, if a given cell is editable.
	 *
	 * @param row The row index of the cell.
	 * @param column The column index of the cell.
	 *
	 * @return true, if the given cell editable. False otherwise.
	 */
	public boolean isCellEditable( int row, int column) {

	    // The rule set files are not editable for now.
	    // If a button is added, this cell must be made editable then!
	    return false;
	}
    }


    // Static variables


    // Instance variables

    /**
     * A field to show the name of the trade rule to add.
     */
    private JTextField _addRuleSetField;

    /**
     * A table to show the loaded rule sets.
     */
    private JTable _loadedRuleSets;

    /**
     * A button to open a new rule set file.
     */
    private JButton _openButton;

    /**
     * A button to start and stop the bot.
     */
    private JButton _startStopButton;

    /**
     * The time interval for the trade thread.
     */
    private JTextField _updateIntervalField;

    /**
     * A reference to the (rule)trade bot, so we can check it's status etc.
     */
    private RuleBot _ruleBot = null;

    /**
     * The JPanel with the user interface.
     */
    private JPanel _uiPanel = null;


    // Constructors

    /**
     * Create a new UI for the rule bot.
     *
     * @param tradeBot The trade bot.
     */
    public RuleBotUI( TradeBot tradeBot) {
	_ruleBot = (RuleBot)tradeBot;
    }


    // Methods

    /**
     * The user clicked on the UI.
     *
     * @param e The triggered action event.
     */
    public void actionPerformed( ActionEvent e) {
	
	// If the user wants to open a new file...
	if( e.getSource() == _openButton) {

	    // a_rueckert: I reuse the ActionLoadRuleSet here, since I don't want to recode the FileOpen dialog for rule sets.
	    File fileToOpen = ActionLoadRuleSet.getInstance().fileOpen();

	    if( fileToOpen != null) {  // If the user did not cancel the file open dialog.
		
		try {

		    // Create a rule set from the selected file-
		    RuleSetFile ruleSet = new RuleSetFile( fileToOpen);

		    TradeApp.getApp().addProjectFile( ruleSet);  // Add this rule set to the global project files.

		    // If we got this far, the rule set seems to be opened ok, so we can store the new rule set in the rule bot.
		    _ruleBot.addTradeRules( ruleSet);

		    // Enable the start button now,since a rule set should be available.
		    _startStopButton.setEnabled( true);

		} catch( IOException ioe) {
		    JOptionPane.showMessageDialog( null, "Cannot read rule set from file", "File read error: " + ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	    }
	} else if( e.getSource() == _startStopButton) {

	    if( _ruleBot.isStopped()) {  // If the bot is stopped at the moment.

		// Get the current update interval from the UI.
		_ruleBot.setUpdateInterval( Integer.parseInt( _updateIntervalField.getText()));

		_ruleBot.start();  // Start the bot.

		_openButton.setEnabled( false);  // A new rule set cannot be loaded, while the button is running with the current rule set.

		_updateIntervalField.setEnabled( false);  // Don't let the user change the update interval, while the bot is running.

		_startStopButton.setText( "Stop");  // The button becomes a stop button now.

	    } else {  // The bot is running at the moment.

		_ruleBot.stop();  // Stop the bot.

		_openButton.setEnabled( true);  // While the bot is stopped, a new rule set can be loaded.

		_updateIntervalField.setEnabled( true);  // Let the user change the update interval, while the bot is not running.

		_startStopButton.setText( "Start");  // The button becomes a start button now.
	    }
	}
    }

    /**
     * Get the JPanel with the user interface for the bot.
     *
     * @return The JPanel with the user interface for the bot.
     */
    public JPanel getUIPanel() {

	if( _uiPanel == null) {       // if there is no UI panel yet,
	    _uiPanel = new JPanel();  // create one.

	    _uiPanel.setLayout( new BorderLayout());

	    // Create a panel for the current settings, like trade site, currency pair etc.
	    JPanel settingsPanel = new JPanel();
	    // settingsPanel.setLayout( new GridLayout( 1, 2));
	    settingsPanel.add( new JLabel( "Trade check interval:"));
	    settingsPanel.add( _updateIntervalField = new JTextField( "" + _ruleBot.getUpdateInterval() / 1000, 6));
	    settingsPanel.add( new JLabel( "seconds"));

	    _uiPanel.add( settingsPanel, BorderLayout.NORTH);

	    // Create a panel for the current status.
	    JPanel statusPanel = new JPanel();
	    statusPanel.setLayout( new GridBagLayout());
	    GridBagConstraints constraints = new GridBagConstraints();
	    constraints.gridx = 0;
	    constraints.gridy = 0;
	    constraints.weightx = 1.0;
	    constraints.weighty = 0.2;

	    // Indicate that this panel is for the status.

	    JPanel headLinePanel = new JPanel();
	    headLinePanel.add( new JLabel( "Status"));

	    statusPanel.add( headLinePanel, constraints);

	    JPanel ruleSetPanel = new JPanel();
	    ruleSetPanel.setLayout( new GridBagLayout());
	    GridBagConstraints constraints2 = new GridBagConstraints();
	    constraints2.gridx = 0;
	    constraints2.gridy = 0;
	    constraints2.weightx = 1.0;
	    constraints2.weighty = 0.8;
	
	    JPanel listPanel = new JPanel();
	    listPanel.add( new JLabel( "Trade rules:"));
	    listPanel.add( _loadedRuleSets = new JTable( new RuleSetTableModel( _ruleBot.getTradeRules())));
	    _loadedRuleSets.setPreferredSize( new Dimension( 200, 200));
	    
	    ruleSetPanel.add( listPanel, constraints2);
	    
	    JPanel addRuleSetPanel = new JPanel();
	    addRuleSetPanel.add( new JLabel( "Add rule set:"));
	    addRuleSetPanel.add( _addRuleSetField = new JTextField( "", 20));
	    _openButton = new JButton( "Open");  // Button to open a new ruleset file.
	    _openButton.addActionListener( this);
	    addRuleSetPanel.add( _openButton);
	    
	    constraints2.gridy = 1;
	    constraints2.weighty = 0.2;
	    ruleSetPanel.add( addRuleSetPanel, constraints2);

	    constraints.gridy = 1;
	    constraints.weighty = 0.6;
	    statusPanel.add( ruleSetPanel, constraints);

	    // Create a panel for the funds.
	    JPanel fundsPanel = new JPanel();
	    fundsPanel.add( new JLabel( "Funds: "));
	    
	    constraints.gridy = 2;
	    constraints.weighty = 0.2;
	    statusPanel.add( fundsPanel, constraints);

	    _uiPanel.add( statusPanel, BorderLayout.CENTER);
				   

	    // Create a panel for the buttons.
	    JPanel buttonPanel = new JPanel();
	    _startStopButton = new JButton( "Start");  // Button to start and stop the bot.
	    _startStopButton.addActionListener( this);
	    _startStopButton.setEnabled( false);  // The start button is disabled, until a rule set was successfully loaded.
	    buttonPanel.add( _startStopButton);
	    
	    _uiPanel.add( buttonPanel, BorderLayout.SOUTH);  // Add the button at the bottom of the panel.
	}
	return _uiPanel;
    }


    /**
     * Update the shown values.
     *
     * @param inputValues The input values for the calculation.
     * @param outputValues The output values of the calculation.
     */
    public void updateValues( BigDecimal [] inputValues, BigDecimal [] outputValues) {
    }
}