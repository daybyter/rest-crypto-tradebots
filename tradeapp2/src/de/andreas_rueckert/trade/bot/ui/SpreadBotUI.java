/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.ui;

import de.andreas_rueckert.trade.bot.spread.SpreadBot;
import de.andreas_rueckert.trade.bot.spread.Strategy;
import de.andreas_rueckert.trade.bot.TradeBot;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * A J2EE GUI for the SpreadBot class.
 */
public class SpreadBotUI implements ActionListener, TradeBotUI {

    // Static variables

    
    // Instance variables

    /**
     * A button to add a user account.
     */
    private JButton _addUserAccountButton;

    /**
     * Textfield to display the currency funds.
     */
    private JTextField _currencyFundsField;

    /**
     * The default format for bitcoin output in this app.
     */
    private DecimalFormat _defaultDecimalFormat = new DecimalFormat("#####.########");

    /**
     * Textfield to display the last logged trade action.
     */
    // private JTextField _lastActionField;
    private JTextArea _lastActionField;

    /**
     * A map to hold the input field for the 'new user account' form.
     */
    private Map< String, JTextField> _newUserAccountFields = new HashMap< String, JTextField>();

    /**
     * Textfield to display the payment currency funds.
     */
    private JTextField _paymentCurrencyFundsField;

    /**
     * The panel with the generic bot settings.
     */
    private JPanel _settingsTab = null;

    /**
     * A checkbox to indicate and activate the simulation mode.
     */
    private JCheckBox _simulationCheckBox;

    /**
     * A button to start and stop the bot.
     */
    private JButton _startStopButton = null;

    /**
     * The pane for the tabs.
     */
    JTabbedPane _tabs = null;

    /**
     * A reference to the trade bot, so we can check it's status etc.
     */
    private TradeBot _tradeBot = null;

    /**
     * The JPanel with the user interface.
     */
    private JPanel _uiPanel = null;


    // Constructors

    /**
     * Create a new UI for the spread bot.
     *
     * @param tradeBot The trade bot.
     */
    public SpreadBotUI( TradeBot tradeBot) {
	_tradeBot = tradeBot;
    }


    // Methods

    /**
     * The user clicked on the UI.
     *
     * @param e The triggered action event.
     */
    public void actionPerformed( ActionEvent e) {

	if( e.getSource() == _startStopButton) {

	    if( getBot().isStopped()) {  // If the bot is currently stopped,
		getBot().start();        // start it.
	    } else {                     // otherwise
		getBot().stop();         // stop it.
	    }
	    
	    // Set the new function of the start/stop button.
	    _startStopButton.setText( _tradeBot.isStopped() ? "Start" : "Stop");

	} else if( e.getSource() == _simulationCheckBox) {  // If the simulation checkbox was clicked.

	    getBot().setSimulation( _simulationCheckBox.isSelected());  // Set the simulation mode of the bot.

	} else if( e.getSource() == _addUserAccountButton) {  // The user wants to add a new user account

	    // Create a new user account for an exchange an copy all the data into this instance.
	    TradeSiteUserAccount newUserAccount = new TradeSiteUserAccount();

	    getAddUserAccountField( "accountName", newUserAccount);  // Try to get the account name from the form.
	    getAddUserAccountField( "email", newUserAccount);        // Try to get an email from the form.
	    getAddUserAccountField( "password", newUserAccount);     // Try to get a password from the form.
	    getAddUserAccountField( "APIkey", newUserAccount);       // Try to get an API key from the form
	    getAddUserAccountField( "secret", newUserAccount);       // Try to get a secret from the form.

	    // Now check all the account data, if they are sufficient to add the new user account.
	    if( newUserAccount.getAccountName() == null) {

		JOptionPane.showMessageDialog( null, "You must enter an account name", "New user account error", JOptionPane.ERROR_MESSAGE);

	    } else {  // Check, if any require account fields were entered (API key + secret or login + password).

		// If the user entered mail and password.
		if( ( ( newUserAccount.getEmail() != null) && ( newUserAccount.getPassword() != null))
		    // or API key and secret.
		    || ( ( newUserAccount.getAPIkey() != null) && ( newUserAccount.getSecret() != null))) {  
		    
		    // add the new user account
		    getBot().addTradeSiteUserAccount( newUserAccount);

		    // and display it.
		    updateUserAccountDisplay(); 

		} else {

		    JOptionPane.showMessageDialog( null
						   , "You must enter email and password or API key and secret of the new account"
						   , "New user account error"
						   , JOptionPane.ERROR_MESSAGE); 
		}
	    }
	}
    }
    
    /**
     * Try to get a field value from the addUserAccount form.
     *
     * @param fieldName The name of the form field.
     * @param userAccount The user account to modify.
     */
    private void getAddUserAccountField( String fieldName, TradeSiteUserAccount userAccount) {

	if( ( _newUserAccountFields.get( fieldName) != null) && !"".equals( _newUserAccountFields.get( fieldName).getText().trim())) {
	    userAccount.setParameter( fieldName, _newUserAccountFields.get( fieldName).getText().trim());
	}
    }

    /**
     * Create a panel to add a new user account.
     *
     * @return A panel to add a new user account.
     */
    private JPanel getAddUserAccountPanel() {

	JTextField currentTextField;  // A var for the currently added text field.

	JPanel resultPanel = new JPanel();

	resultPanel.setLayout( new GridBagLayout());

	GridBagConstraints c = new GridBagConstraints();

	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 1.0;
	c.gridwidth = 2;
	resultPanel.add( new JLabel( "New user"), c);  // Add a label for the dialog.

	c.gridy++;
	c.weightx = 0.5;
	c.gridwidth = 1;
	c.gridx = 0;

	resultPanel.add( new JLabel( "Account name:"), c);  // Give the account a name.
	
	c.gridx++;
	resultPanel.add( currentTextField = new JTextField( 20), c);
	_newUserAccountFields.put( "accountName", currentTextField);

	c.gridx = 0;
	c.gridy++;
	c.weightx = 0.5;
	c.gridwidth = 1;
	resultPanel.add( new JLabel( "Email address:"), c);
	
	c.gridx++;
	resultPanel.add( currentTextField = new JTextField( 20), c);
	_newUserAccountFields.put( "email", currentTextField);

	c.gridx = 0;
	c.gridy++;
	resultPanel.add( new JLabel( "Password:"), c);

	c.gridx++;
	resultPanel.add( currentTextField = new JTextField( 20), c);
	_newUserAccountFields.put( "password", currentTextField);

	c.gridx = 0;
	c.gridy++;
	resultPanel.add( new JLabel( "API key:"), c);

	c.gridx++;
	resultPanel.add( currentTextField = new JTextField( 20), c);
	_newUserAccountFields.put( "APIkey", currentTextField);

	c.gridx = 0;
	c.gridy++;
	resultPanel.add( new JLabel( "Secret:"), c);

	c.gridx++;
	resultPanel.add( currentTextField = new JTextField( 20), c);
	_newUserAccountFields.put( "secret", currentTextField);

	c.gridx = 0;
	c.gridy++;
	c.gridwidth = 2;
	c.weightx = 1.0;
	resultPanel.add( _addUserAccountButton = new JButton( "Add user account"), c);
	_addUserAccountButton.addActionListener( this);
	
	return resultPanel;
    }
    
    /**
     * Get the hosting tradebot.
     *
     * @return The hosting trade bot.
     */
    SpreadBot getBot() {

	return (SpreadBot)_tradeBot;
    }

    /**
     * Get a panel for a given strategy.
     *
     * @return A panel for the given strategy.
     */
    private JPanel getStrategyPanel( Strategy strategy) {
	
	JPanel resultPanel = new JPanel();
	
	resultPanel.setLayout( new BorderLayout());
	
	// Set a size for the UI Panel.
	resultPanel.setPreferredSize( new Dimension( 500, 200));
	
	// Simulation panel
	JPanel simulationPanel = new JPanel();
	simulationPanel.add( new JLabel( "Simulation mode:"));
	simulationPanel.add( _simulationCheckBox = new JCheckBox());
	_simulationCheckBox.setSelected( getBot().isSimulation());
	_simulationCheckBox.addActionListener( this);
	resultPanel.add( simulationPanel, BorderLayout.NORTH);
	
	JPanel spreadPanel = new JPanel();
	spreadPanel.setLayout( new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.weightx = 0.5;
	c.weighty = 0.2;
	c.gridx = 0;
	c.gridy = 0;
	spreadPanel.add( new JLabel( "" + getBot().getTradedCurrencyPair().getPaymentCurrency().getCode() + " funds. "), c);
	c.gridx = 1;
	spreadPanel.add( _paymentCurrencyFundsField = new JTextField( 20), c);
	_paymentCurrencyFundsField.setEditable( false);
	c.gridx = 0;
	c.gridy = 1;
	spreadPanel.add( new JLabel( "" + getBot().getTradedCurrencyPair().getCurrency().getCode() + " funds: "), c);
	c.gridx = 1;
	spreadPanel.add( _currencyFundsField = new JTextField( 20), c);
	_currencyFundsField.setEditable( false);
	c.weighty = 0.4;
	c.gridx = 0;
	c.gridy = 2;
	spreadPanel.add( new JLabel( "Last action:"), c);
	c.gridx = 1;
	spreadPanel.add( _lastActionField = new JTextArea( 5, 20), c);
	_lastActionField.setEditable( false);
	/* c.weighty = 0.2;
	   c.gridx = 0;
	   c.gridy = 3;
	   spreadPanel.add( new JLabel( "Profit margin:"), c);
	   c.weightx = 0.3;
	    c.gridx = 1;
	    spreadPanel.add( _profitMarginField = new JTextField( 20), c);
	    _profitMarginField.setEditable( false);	   
	    c.weightx = 0.2;
	    c.gridx = 2;
	    spreadPanel.add( new JLabel( "%"), c); */
	resultPanel.add( spreadPanel, BorderLayout.CENTER);
	
	return resultPanel;
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

	    _uiPanel.add( _tabs = new JTabbedPane(), BorderLayout.CENTER);

	    // Add a settings tab.

	    _settingsTab = new JPanel();

	    // Add a panel for each user account 
	    _settingsTab.setLayout( new GridLayout( getBot().getNumTradeSiteUserAccounts() + 1, 1));

	    for( int currentAccountIndex = 0; currentAccountIndex < getBot().getNumTradeSiteUserAccounts(); ++currentAccountIndex) {

		TradeSiteUserAccount currentUserAccount = getBot().getTradeSiteUserAccount( currentAccountIndex);

		JPanel accountPanel = new JPanel();

		accountPanel.setLayout( new GridLayout( 6, 2));

		JTextField currentTextField;  // Var to access the current text field.

		accountPanel.add( new JLabel( "Account name:"));
		accountPanel.add( currentTextField = new JTextField( currentUserAccount.getAccountName() != null
								     ? currentUserAccount.getAccountName()
								     : ""
								     , 20));
		currentTextField.setEditable( false);

		
		accountPanel.add( new JLabel( "Activated:"));
		accountPanel.add( currentTextField = new JTextField( currentUserAccount.isActivated() ? "yes" : "no", 20));
		currentTextField.setEditable( false);


		accountPanel.add( new JLabel( "Email address:"));
		accountPanel.add( currentTextField = new JTextField( currentUserAccount.getEmail() != null 
								     ? currentUserAccount.getEmail() 
								     : ""
								     , 20));
currentTextField.setEditable( false);
		
		accountPanel.add( new JLabel( "Password:"));
		accountPanel.add( currentTextField = new JTextField( currentUserAccount.getPassword() != null
								     ? currentUserAccount.getPassword()
								     : ""
								     , 20));
		currentTextField.setEditable( false);

		accountPanel.add( new JLabel( "API key: "));
		accountPanel.add( currentTextField = new JTextField( currentUserAccount.getAPIkey() != null
								     ? currentUserAccount.getAPIkey()
								     : ""
								     , 20));
		currentTextField.setEditable( false);

		accountPanel.add( new JLabel( "Secret: "));
		accountPanel.add( currentTextField = new JTextField( currentUserAccount.getSecret() != null
								     ? currentUserAccount.getSecret()
								     : ""
								     , 20));
		currentTextField.setEditable( false);

		_settingsTab.add( accountPanel);
	    }

	    _settingsTab.add( getAddUserAccountPanel());
	    
	    _tabs.addTab( "Settings", _settingsTab);
	    
	    // Add one tab for each strategy.
	    for( Strategy currentStrategy : getBot().getStrategies()) {

		_tabs.addTab( "Strategy: "+ currentStrategy.getName(), getStrategyPanel( currentStrategy));
	    }

	    // Add a button to start the bot.
	    _startStopButton = new JButton( "Start");
	    _startStopButton.addActionListener( this);
	    _uiPanel.add( _startStopButton, BorderLayout.SOUTH);
	}
	return _uiPanel;
    }

    /**
     * Update the list of available user accounts.
     */
    private void updateUserAccountDisplay() {

	// Just redraw the settings panel for now.
	_settingsTab.invalidate();
	_uiPanel.validate();
    }
    
    /**
     * Update the shown values.
     *
     * @param inputValues The input values for the calculation.
     * @param outputValues The output values of the calculation.
     */
    public void updateValues( BigDecimal [] inputValues, BigDecimal [] outputValues) {

	// _currencyFundsField.setText( outputValues[ 0] == null ? "null" : _defaultDecimalFormat.format( outputValues[ 0]));
	// _paymentCurrencyFundsField.setText( outputValues[ 1] == null ? "null" : _defaultDecimalFormat.format( outputValues[ 1]));

	// If there was a trade action, display it...
	String lastAction = getBot().getTradeLogger().getLastLoggedEntry();
	
	if( lastAction != null) {
	    _lastActionField.setText( lastAction);
	}

	// Display the current minimum profit.
	//_profitMarginField.setText( "" + getBot().getMargin());

	_uiPanel.invalidate();  // show the results.
	_uiPanel.validate();
    }
}
