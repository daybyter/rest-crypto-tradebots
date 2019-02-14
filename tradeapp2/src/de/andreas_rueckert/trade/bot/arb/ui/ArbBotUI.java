/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb.ui;

import de.andreas_rueckert.trade.bot.arb.ArbBot;
import de.andreas_rueckert.trade.bot.arb.TradePoint;
import de.andreas_rueckert.trade.bot.arb.TradeSequence;
import de.andreas_rueckert.trade.bot.arb.TradeSiteInfo;
import de.andreas_rueckert.trade.bot.ui.TradeBotUI;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.site.TradeSiteUserAccountPersistenceSQLImpl;
import de.andreas_rueckert.trade.site.ui.TradeSiteUserAccountManagerDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


/**
 * This class implements a J2EE GUI for the ArbBot class.
 */
public class ArbBotUI implements ActionListener, TradeBotUI {

    // Inner classes

    /**
     * This class maps a sequence panel to a list position according 
     * to the resulting profit.
     */
    class TradeSequencePosition implements Comparable<TradeSequencePosition> {
	
	// Instance variables

	/**
	 * The trade sequence, that is mapped.
	 */
	TradeSequence _tradeSequence;


	// Constructors

	/**
	 * Create a new trade sequence position.
	 *
	 * @param tradeSequence The trade sequence, that is mapped.
	 */
	TradeSequencePosition( TradeSequence tradeSequence) {
	    _tradeSequence = tradeSequence;
	}

	
	// Methods

	/**
	 * Compare this position to another position.
	 *
	 * @param position The position to compare to.
	 *
	 * @return The result of the comparison.
	 */
	@Override
	public int compareTo( TradeSequencePosition position) {

	    if( getTradeSequence().getTradeIndicatorOutput() == null) {  // If there is no profit known yet,

		if( position.getTradeSequence().getTradeIndicatorOutput() == null) {  // Also no profit known yet.

		    return 0;

		} else {

		    return -1;  // A known profit is more than an unknown profit.

		}

	    } else {

		if( position.getTradeSequence().getTradeIndicatorOutput() == null) {  // No profit known for the argument yet.

		    return 1;

		} else { 

		    return getTradeSequence().getTradeIndicatorOutput().compareTo( position.getTradeSequence().getTradeIndicatorOutput());
		}
	    }

	    // Better sort according to the profit, not the percentage.
	    // return getTradeSequence().getTradeProfit().compareTo( position.getTradeSequence().getTradeProfit());
	}

	/**
	 * Get the trade sequence, that is mapped here.
	 *
	 * @return The trade sequence, that is mapped here.
	 */
	TradeSequence getTradeSequence() {
	    return _tradeSequence;
	}
    }


    // Static variables


    // Instance variables

    /**
     * A button to open the account manager window.
     */
    private JButton _accountManagerButton;

    /**
     * A reference to the trade bot, so we can check it's status etc.
     */
    private ArbBot _bot = null;

    /**
     * The default format for bitcoin output in this app.
     */
    private DecimalFormat _defaultDecimalFormat = new DecimalFormat("#####.########");

    /**
     * Add a 'more...' button to each sequence and add this button to a map, so we can quickly
     * get the sequence for the button.
     */
    private Map<JButton, TradeSequence> _detailButtonMapping = new HashMap<JButton, TradeSequence>();

    /**
     * The green background color for good results.
     */
    private Color LIGHT_GREEN = new Color( 200, 255, 200);
    
    /**
     * The red background color for bad results.
     */
    private Color LIGHT_RED = new Color( 255, 200, 200);    

    /**
     * A list of sequence positions to sort the trade sequence panels according to the calculated profit.
     */
    private ArrayList<TradeSequencePosition> _sequenceMapping;

    /**
     * The button to start and stop the bot.
     */
    private JButton _startStopButton;

    /**
     * The format to display the calculation timestamp of a sequence.
     */
    SimpleDateFormat _tradeSequenceTimestampFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Create a mapping from the trade sites to the checkboxes to activate them.
     */
    private Map< TradeSiteInfo, JCheckBox> _tradeSiteCheckBoxes = new HashMap< TradeSiteInfo, JCheckBox>();

    /**
     * Create a textfield for each trade site to show the date of the last data fetching.
     */
    private Map< TradeSiteInfo, JTextField> _tradeSiteUpdateInfoFields = new HashMap< TradeSiteInfo, JTextField>();

    /**
     * The panel with the trading sequences.
     */
    private JPanel _tradingPanel;
    
    /**
     * The JPanel with the user interface.
     */
    private JPanel _uiPanel = null;

    /**
     * Create a date format to display the updates from the exchanges.
     */
    private final SimpleDateFormat _updateDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    // Constructors
    
    /**
     * Create a new UI for the bot.
     *
     * @param tradeBot The trade bot.
     */
    public ArbBotUI( ArbBot bot) {
	_bot = bot;

	initTradeSite();
    }


    // Methods

    /**
     * The user clicked on the UI.
     *
     * @param e The triggered action event.
     */
    public void actionPerformed( ActionEvent e) {

	if( e.getSource() == _startStopButton) {

	    if( _bot.isStopped()) {
		_bot.start();
	    } else {
		_bot.stop();
	    }

	    // Starting the bot might have failed, that's why I query the stopped status a second time...
	    _startStopButton.setText( _bot.isStopped() ? "Start" : "Stop");

	    // Disable the checkboxes for the trade site activation, while the bot is running,
	    // because we cannot remove a trade site from the running bot.
	    for( Map.Entry currentEntry : _tradeSiteCheckBoxes.entrySet()) {

		// Get the current checkbox.
		JCheckBox currentCheckBox = (JCheckBox)currentEntry.getValue();

		currentCheckBox.setEnabled( _bot.isStopped());
	    }

	} else if( e.getSource() == _accountManagerButton) {

	    // Show the manager for the user accounts.
	    TradeSiteUserAccountManagerDialog.getInstance().setModal( true);
	    TradeSiteUserAccountManagerDialog.getInstance().setVisible( true);

	} else if( e.getSource() instanceof JButton) {  // This could be one of the 'more' buttons for a trade sequence.

	    // Check if there is a sequence in the detail button => trade sequence mapping.
	    TradeSequence currentSequence = _detailButtonMapping.get( (JButton)(e.getSource()));

	    if( currentSequence != null) {  // If there is a sequence, the user hit one of the 'more' buttons.

		// Open a modal dialog on this sequence.
		SequenceInfoDialog sequenceInfo = new SequenceInfoDialog( null, currentSequence, this);
		sequenceInfo.setVisible( true);
	    }

	} else {  // Loop over the checkboxes to (de-)activate some specific bot.

	    for( Map.Entry currentEntry : _tradeSiteCheckBoxes.entrySet()) {

		// Get the current checkbox.
		JCheckBox currentCheckBox = (JCheckBox)currentEntry.getValue();

		// Check, if this box was the source of the event.
		if( e.getSource() == currentCheckBox) {

		    // System.out.println( "DEBUG: triggering trde site");

		    // Get the trade site info for this checkbox.
		    TradeSiteInfo tradeSiteInfo = (TradeSiteInfo)currentEntry.getKey();

		    // Set the info for the bot to the new status of the checkbox.
		    tradeSiteInfo.setActivated( currentCheckBox.isSelected());

		    // Enable or disable the trade sequences for this trade site.
		    _bot.setTradeSequencesForTradeSiteEnabled( tradeSiteInfo.getTradeSite(), currentCheckBox.isSelected());

		    return;  // No need to check further for the source of the event.
		}
	    }
	}
    }

    /**
     * Get the bot, this UI is for.
     *
     * @return The bot, this UI is for.
     */
    ArbBot getBot() {

	return _bot;
    }
    
    /**
     * Render an UI panel for a given trade sequence.
     *
     * @param tradeSequence The given trade sequence.
     *
     * @return A JPanel object that visualizes the trade sequence.
     */
    private JPanel getPanelForTradeSequence( TradeSequence tradeSequence) {

	// Check, if this sequence is calculated and profitable
	boolean isCalculated = ( tradeSequence.getTradeIndicatorInput() != null) 
	    && ( tradeSequence.getTradeIndicatorOutput() != null);
	boolean isProfitable = isCalculated && ( tradeSequence.getTradeIndicatorOutput().compareTo( tradeSequence.getTradeIndicatorInput()) > 0);

	// System.out.println( "sequence is " + (isCalculated ? "calculated" : "not calculated"));

	JPanel resultPanel = new JPanel();  // The resulting panel.
	resultPanel.setLayout( new GridLayout( 2, 1));  

	JPanel currencyPanel = new JPanel();  // The panel to display the currency sequence.

	// Start with the name of the trade site.
	currencyPanel.add( new JLabel( tradeSequence.getTradeSite().getName() + " : "));

	// Create a text field to display the amount, we start with.
	JTextField startAmountField = new JTextField( tradeSequence.getTradeIndicatorInput() == null 
						      ? "10"
						      :_defaultDecimalFormat.format( tradeSequence.getTradeIndicatorInput()), 14);
        startAmountField.setEditable( false);
	currencyPanel.add( startAmountField);

	// Now loop over the trade points and create labels for them.
	for( int currentIndex = 0; currentIndex < tradeSequence.size(); ++currentIndex) {
	    
	    // Get the current trade point.
	    TradePoint currentPoint = tradeSequence.getTradePoint( currentIndex);

	    // Get the current currency pair.
	    CurrencyPair currentCurrencyPair = currentPoint.getTradedCurrencyPair();

	    if( currentIndex == 0) {  // We need both currencies for the first point.
		if( currentPoint.isBuy()) {
		    currencyPanel.add( new JLabel( currentCurrencyPair.getPaymentCurrency().getCode()));
		    currencyPanel.add( new JLabel( "=>"));
		    currencyPanel.add( new JLabel( currentCurrencyPair.getCurrency().getCode()));
		} else {
		    currencyPanel.add( new JLabel( currentCurrencyPair.getCurrency().getCode()));
		    currencyPanel.add( new JLabel( "=>"));
		    currencyPanel.add( new JLabel( currentCurrencyPair.getPaymentCurrency().getCode()));
		}
	    } else {  // Now only one currency for the further points.
		currencyPanel.add( new JLabel( "=>"));
		currencyPanel.add( new JLabel( currentPoint.isBuy() ? currentCurrencyPair.getCurrency().getCode() : currentCurrencyPair.getPaymentCurrency().getCode()));
	    }
	}
	

	// Now add a text field to display the result of the trade sequence.
	JTextField endAmountField = new JTextField( tradeSequence.getTradeIndicatorOutput() == null 
						    ? "10"
						    :_defaultDecimalFormat.format( tradeSequence.getTradeIndicatorOutput()), 14);
	endAmountField.setEditable( false);
	if( isCalculated) {
	    endAmountField.setBackground( isProfitable ? LIGHT_GREEN : LIGHT_RED);
	}
	currencyPanel.add( endAmountField);

	// Add a textfield for the timestamp of the calculation, if the sequence was already calculated.
	if( tradeSequence.getLastCalculationTimestamp() != -1L) {
	    
	    JTextField calculationTimestampField = new JTextField( 10);
	    calculationTimestampField.setText( _tradeSequenceTimestampFormat.format( new Date( tradeSequence.getLastCalculationTimestamp() / 1000L)));
	    currencyPanel.add( calculationTimestampField);
	}

	resultPanel.add( currencyPanel);  // Now add the currency sequence to the resulting panel.

	// Now create another panel to display the traded amount and the (potential) resulting profit.
	JPanel amountPanel = new JPanel();

	// Display the potential trade amount.
	amountPanel.add( new JLabel( "Potential trade amount:"));  
	JTextField tradeAmountField = new JTextField( tradeSequence.getTradeAmount() == null 
						      ? "-1"
						      :_defaultDecimalFormat.format( tradeSequence.getTradeAmount()), 14);
	tradeAmountField.setEditable( false);
	if( isCalculated) {
	    tradeAmountField.setBackground( isProfitable ? LIGHT_GREEN : LIGHT_RED);
	}
	amountPanel.add( tradeAmountField);

	// Display the potential profit.
	amountPanel.add( new JLabel( "Potential profit/loss:"));
	JTextField profitField = new JTextField( tradeSequence.getTradeProfit() == null 
						 ? "-1"
						 :_defaultDecimalFormat.format( tradeSequence.getTradeProfit()), 14);
	profitField.setEditable( false);
	if( isCalculated) {
	    profitField.setBackground( isProfitable ? LIGHT_GREEN : LIGHT_RED);
	}
	amountPanel.add( profitField);

	// Add a button for the sequence details.
	JButton detailButton = new JButton( "More...");

	// Only enable the button on calculated sequences.
	detailButton.setEnabled( isCalculated);  
	     
	// Notify the UI, if the button is pressed.
	detailButton.addActionListener( this);

	// Add the button to the map of detail buttons.
	_detailButtonMapping.put( detailButton, tradeSequence);

	// Add the button to the amount panel.
	amountPanel.add( detailButton);

	resultPanel.add( amountPanel);  // Now add the amounts to the trade sequence panel.

	return resultPanel;  // Return the generated panel.
    }

    /**
     * Get the JPanel with the user interface for the bot.
     *
     * @return The JPanel with the user interface for the bot.
     */
    public JPanel getUIPanel() {

	if( _uiPanel == null) {       // if there is no UI panel yet,
	    _uiPanel = new JPanel();  // create one.

	    //_uiPanel.setLayout( new BorderLayout());
	    _uiPanel.setLayout( new GridBagLayout());
	    GridBagConstraints uiConstraints = new GridBagConstraints();
	    uiConstraints.gridx = 0;
	    uiConstraints.gridy = 0;
	    uiConstraints.weightx = 1;
	    uiConstraints.weighty = 0.4;
	    uiConstraints.fill = GridBagConstraints.BOTH;

	    // Set a size for the scrollpanel.
	    _uiPanel.setPreferredSize( new Dimension( 800, 600));

	    // Create a panel to select a trade site.
	    JPanel tradesitePanel = new JPanel();
	     
	    // tradesitePanel.add( new JLabel( "Trade site(s): ")); 
	    
	    // Create a panel for the trade site names.
	    JPanel tradeSiteNamesPanel = new JPanel();
	    //tradeSiteNamesPanel.setLayout( new GridLayout( _bot.getSupportedTradeSites().size(), 3));
	    tradeSiteNamesPanel.setLayout( new GridBagLayout());
	    GridBagConstraints nameUiConstraints = new GridBagConstraints();
	    nameUiConstraints.gridx = 0;
	    nameUiConstraints.gridy = 0;
	    nameUiConstraints.weightx = 0.2;
	    //uiConstraints.weighty = 0.4;
	    //uiConstraints.fill = GridBagConstraints.BOTH;
	    
	    // Convert the supported trade sites to an array.
	    for( TradeSiteInfo currentSite : _bot.getSupportedTradeSites()) {

		JPanel checkboxPanel = new JPanel();

		// Create a checkbox for the current trade site.
		JCheckBox currentCheckBox = new JCheckBox( currentSite.getTradeSite().getName(), currentSite.isActivated());

		// Pass the events to this class.
		currentCheckBox.addActionListener( this);

		// Add the box to the panel.
		checkboxPanel.add( currentCheckBox);

		// Add the box to the trade site info => box mapping.
		_tradeSiteCheckBoxes.put( currentSite, currentCheckBox);

		// Add the panel with the checkbox to the tradeSitePanel.
		nameUiConstraints.gridx = 0;
		nameUiConstraints.weightx = 0.2;
		tradeSiteNamesPanel.add( checkboxPanel, nameUiConstraints);

		// Put the data fetched label and the textfield in their own panel.
		JPanel dataFetchedPanel = new JPanel();

		// Add a label to explain the following text field.
		dataFetchedPanel.add( new JLabel( "Data fetched: "));

		// Create a text field to show when the data from this trade site were fetched.
		JTextField updateInfoField = new JTextField( 16);

		// Add the text field to the panel.
		dataFetchedPanel.add( updateInfoField);

		dataFetchedPanel.validate();

		// Add the field to the map of update fields.
		_tradeSiteUpdateInfoFields.put( currentSite, updateInfoField);

		// Add the panel with the fetch info to the tradeSitePanel.
		nameUiConstraints.gridx++;
		nameUiConstraints.weightx = 0.4;
		tradeSiteNamesPanel.add( dataFetchedPanel, nameUiConstraints);

		// Add a combo box to select a user account to perform the actual trades.

		// Get the account names for this trade site.
		List<TradeSiteUserAccount> accountList = TradeSiteUserAccountPersistenceSQLImpl.getInstance().getAccountsForTradeSite( currentSite.getTradeSite());
		String [] accountNames;
		if( ! accountList.isEmpty()) {  // Copy the name to the combo box. (ToDo: save and restore the last selected account?)
		    accountNames = new String[ accountList.size()];
		    int accountIndex = 0;
		    for( TradeSiteUserAccount currentAccount : accountList) {
			accountNames[ accountIndex++] = currentAccount.getAccountName();
		    }
		} else {
		    accountNames = new String[ 1];
		    accountNames[ 0] = "No account available";
		}
		JComboBox accountSelectionBox = new JComboBox( accountNames);

		// Add the combo box to the panel.
		nameUiConstraints.gridx++;
		tradeSiteNamesPanel.add( accountSelectionBox, nameUiConstraints);

		// Move on to the next row.
		nameUiConstraints.gridy++;
	    }
	    
	    // _tradeSitesBox.addActionListener( this);
	    tradesitePanel.add( tradeSiteNamesPanel);

	    _uiPanel.add( tradesitePanel, uiConstraints);

	    // Create a new panel for the trading info.
	    _tradingPanel = new JPanel();
	    List<TradeSequence> tradeSequences = _bot.getTradeSequences();

	    _tradingPanel.setLayout( new GridLayout( tradeSequences.size() <= 100 ? tradeSequences.size() : 101 , 1));
	    
	    // System.out.println( "Adding " + tradeSequences.size() + " trade sequences");
	    
	    int currentFieldIndex = 0;
	    for( TradeSequence sequence : tradeSequences) {
		
		JPanel currentPanel = getPanelForTradeSequence( sequence);

		++currentFieldIndex;

		// Show the panel for now with emtpy values.
		_tradingPanel.add( currentPanel);

		// If 100 sequences were shown, just abort here with an indicator, that more
		// sequences exist.
		if( ( currentFieldIndex >= 100) && ( tradeSequences.size() > currentFieldIndex)) {

		    JPanel moreSequencesToComePanel = new JPanel();
		    moreSequencesToComePanel.add( new JLabel( "A total of " 
							      + tradeSequences.size() 
							      + " trade sequences exist, but they are not shown all"));
		    
		    // Let the user know about the missing tradesequences.
		    _tradingPanel.add( moreSequencesToComePanel);

		    break;
		}
	    }

	    uiConstraints.gridy = 1;
	    uiConstraints.weighty = 0.5;
	    _uiPanel.add( new JScrollPane( _tradingPanel), uiConstraints);

	    // Create a new panel for the button(s).
	    JPanel buttonPanel = new JPanel();

	    // Add a button to open the account manager.
	    buttonPanel.add( _accountManagerButton = new JButton( "Accounts"));
	    _accountManagerButton.addActionListener( this);

	    // Add a button to start the bot.
	    buttonPanel.add( _startStopButton = new JButton( "Start"));
	    _startStopButton.addActionListener( this);

	     uiConstraints.gridy = 2;
	     uiConstraints.weighty = 0.1;
	    _uiPanel.add( buttonPanel, uiConstraints);
	}
	
	return _uiPanel;
    }

    /**
     * Prepare the UI for a new trade site with new trade sequences.
     */
    private void initTradeSite() {

	// Add all the trade sequences to the sequence mapping.
	_sequenceMapping = new ArrayList<TradeSequencePosition>();

	for( TradeSequence currentSequence : _bot.getTradeSequences()) {

	    _sequenceMapping.add( new TradeSequencePosition( currentSequence));
	}
    }

    /**
     * Update the shown values.
     *
     * @param inputValues dummy for the interface.
     * @param outputValues dummy for the interface.
     */
    public synchronized void updateValues( BigDecimal [] inputValues, BigDecimal [] outputValues) {

	// When the new profits are set, resort the mapping.
	Collections.sort( _sequenceMapping);
	Collections.reverse( _sequenceMapping);  // Show the highest profit first.

	// Remove all the panels and add them again in the correct order.
	_tradingPanel.removeAll();

	// Remove all the buttons from the detail button => sequence mapping.
	_detailButtonMapping.clear();

	int currentFieldIndex = 0;
	for( TradeSequencePosition currentPosition : _sequenceMapping) {
	    
	    _tradingPanel.add( getPanelForTradeSequence( currentPosition.getTradeSequence()));

	    ++currentFieldIndex;

	    // If 100 sequences were shown, just abort here with an indicator, that more
	    // sequences exist.
	    if( ( currentFieldIndex >= 100) && ( _bot.getTradeSequences().size() > currentFieldIndex)) {

		JPanel moreSequencesToComePanel = new JPanel();
		moreSequencesToComePanel.add( new JLabel( "A total of " 
							  + _bot.getTradeSequences().size() 
							  + " trade sequences exist, but they are not shown all"));
		    
		// Let the user know about the missing tradesequences.
		_tradingPanel.add( moreSequencesToComePanel);

		break;
	    }
	}

	// Update the dates when the data were fetched from each of the trade sites.
	for( TradeSiteInfo currentSite : _bot.getSupportedTradeSites()) {

	    // Try to get the update info text field for this trade site.
	    JTextField currentUpdateField = _tradeSiteUpdateInfoFields.get( currentSite);

	    // If it was found (should always happen), update the info of the field.
	    if( currentUpdateField != null) {
		
		// Get the date of the last update from the bot.
		Date updateDate = _bot.getUpdateDate( currentSite.getTradeSite());

		// If there was no update yet, show this info too.
		if( updateDate == null) {

		    currentUpdateField.setText( "no complete data available");

		} else {

		    // Format the date and display it.
		    currentUpdateField.setText( _updateDateFormat.format( updateDate));
		}
	    }
	}

	// Redraw the rearranged panels.
	_tradingPanel.invalidate();
	_tradingPanel.validate();
    }
}
