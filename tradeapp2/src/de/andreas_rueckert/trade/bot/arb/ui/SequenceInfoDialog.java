/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb.ui;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.bot.arb.TradePoint;
import de.andreas_rueckert.trade.bot.arb.TradeSequence;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * This class implements a dialog to show the details of a tradesequence.
 */
class SequenceInfoDialog extends JDialog implements ActionListener {

    // Inner classes


    // Static variables


    // Instance variables

    /**
     * The combo box to select a user account.
     */
    JComboBox _accountSelectionBox;

    /**
     * The bot UI, that displays all the sequences.
     */
    ArbBotUI _botUI;

    /**
     * The button to close the dialog.
     */
    JButton _closeButton;

    /**
     * The button to trigger the trading of the sequence.
     */
    JButton _tradeButton;


    // Constructors

    /**
     * Create a new info dialog for a given trade sequence.
     *
     * @param frame The parent frame (or null, if no parent is given).
     * @param tradeSequence The sequence, this info is for.
     */
    public SequenceInfoDialog( Frame parent, TradeSequence tradeSequence, ArbBotUI botUI) {
	
	// Call the JDialog constructor with a null owner, the tradesequence as the title
	// and make the dialog modal.
	super( parent, "Info: " + tradeSequence.toString(), true);

	// Store a reference to the bot UI in the instance.
	_botUI = botUI;

	// Get the content pane of the dialog.
	Container contentPane = getContentPane(); 

	// Set the layout to a border layout.
	contentPane.setLayout( new BorderLayout());

	// Create a panel to display the trade site.
	JPanel tradeSitePanel = new JPanel();
	
	// Display info on the used trade site.
	tradeSitePanel.add( new JLabel( "Trade site: " + tradeSequence.getTradeSite().getName()));
	
	// Add the panel to the dialog.
	contentPane.add( tradeSitePanel, BorderLayout.NORTH);

	// Create a panel to hold the info on the sequence.
	JPanel infoPanel = getInfoPanel( tradeSequence);

	// Add the main panel to the dialog.
	contentPane.add( infoPanel, BorderLayout.CENTER);

	// Create a panel to hold the buttons.
	JPanel buttonPanel = new JPanel();

	// Let the user select an account for trading.
	buttonPanel.add( new JLabel( "User account: "));

	// Get the names of the user accounts.
	List<String> accountNames = new ArrayList<String>();
	
	// Add the accounts to the list.
	for( TradeSiteUserAccount currentAccount : _botUI.getBot().getTradeSiteUserAccounts( tradeSequence.getTradeSite())) {

	    // Check, if the account is on the trade site of this sequence.
	    if( currentAccount.getTradeSite().equals( tradeSequence.getTradeSite())) {

		// Add the name of the current account to the list of available accounts,
		// if so.
		accountNames.add( currentAccount.getAccountName());
	    }
	}

	// Create a combo box to select one the accounts.
	_accountSelectionBox = new JComboBox( accountNames.toArray( new String[ accountNames.size()]));

	// Add the combo box to the panel.
	buttonPanel.add( _accountSelectionBox);

	// Create a button to trigger the trading of the whole sequence.
	buttonPanel.add( _tradeButton = new JButton( "Trade"));
	_tradeButton.setEnabled( false);  // Not ready at the moment.
	_tradeButton.addActionListener( this);

	// Create a button to close the dialog.
	buttonPanel.add( _closeButton = new JButton( "Close"));
	_closeButton.addActionListener( this);

	// Add the button panel to the dialog.
	contentPane.add( buttonPanel, BorderLayout.SOUTH);
	
	// Set the size of the dialog.
	setBounds( 200, 100, 1000, 250);
    }


    // Methods

    /**
     * The user clicked on some UI element in the dialog.
     *
     * @param event The action event, that was triggered.
     */
    public void actionPerformed( ActionEvent event) {

	if( event.getSource() == _closeButton) {  //  If the user wants to terminate this dialog.

	    setVisible( false);  // Hide this dialog.
	}
    }

    /**
     * Get a panel for a trade action.
     *
     * @param tradePoint The trade point to visualize.
     *
     * @return The generated panel for the trade activity.
     */
    private JPanel getActionPanel( TradePoint tradePoint) {

	// The generate panel.
	JPanel resultPanel = new JPanel();

	// Set an empty border for the panel to get some spacing.
	resultPanel.setBorder( BorderFactory.createEmptyBorder( 0, 20, 0, 20));

	// Set a layout for the panel, because we need several components.
	resultPanel.setLayout( new GridLayout( 3, 1));

	// Add a label for the pair.
	resultPanel.add( new JLabel( ( tradePoint.isBuy() ? " buy " : " sell ")));

	// Indicate a change.
	resultPanel.add( new JLabel( " => "));

	// Another line for the price.
	JPanel pricePanel = new JPanel();

	// maybe someone doesn't know, that this is a price?
	pricePanel.add( new JLabel( "Price: "));

	// Add a label for the price.
	pricePanel.add( new JLabel( "0"));

	// Add the price line to the result.
	resultPanel.add( pricePanel);
	
	return resultPanel;  // Return the generated panel.
    }

    /**
     * Create an info panel for a given trade sequence.
     *
     * @param tradeSequence The trade sequence to get the info on.
     *
     * @return A panel with info on the sequence.
     */
    private JPanel getInfoPanel( TradeSequence tradeSequence) {

	// Create a panel for the result.
	JPanel resultPanel = new JPanel();

	// For now just a rough sequence of the currencies
	for( int index = 0; index < tradeSequence.size(); ++index) {

	    TradePoint currentPoint = tradeSequence.getTradePoint( index);
	    CurrencyPair currencyPair = currentPoint.getTradedCurrencyPair();

	    if( index == 0) {  // If this is the first trade point, we have to display both currencies
		if( currentPoint.isBuy()) {
		    resultPanel.add( getPointPanel( currencyPair.getPaymentCurrency(), new Amount( "0")));
		    resultPanel.add( getActionPanel( currentPoint));
		    resultPanel.add( getPointPanel( currencyPair.getCurrency(), new Amount( "0")));
		} else {
		    resultPanel.add( getPointPanel( currencyPair.getCurrency(), new Amount( "0")));
		    resultPanel.add( getActionPanel( currentPoint));
		    resultPanel.add( getPointPanel( currencyPair.getPaymentCurrency(), new Amount( "0")));
		}
	    } else {
		resultPanel.add( getActionPanel( currentPoint));
		resultPanel.add( getPointPanel( currentPoint.isBuy() ? currencyPair.getCurrency() : currencyPair.getPaymentCurrency()
						, new Amount( "0")));
	    }
	}

	return resultPanel;  // Return the created panel.
    }

    /**
     * Get a panel for a given trade point.
     *
     * @param currency The held currency at this point.
     * @param amount The held amount at this point.
     *
     * @return a panel to visualize this trade point.
     */
    private JPanel getPointPanel( Currency currency, Amount amount) {

	// A panel for the result.
	JPanel resultPanel = new JPanel();
	
	// Set an empty border for the panel to get some spacing.
	resultPanel.setBorder( BorderFactory.createEmptyBorder( 10, 0, 10, 0));

	// Show the currency in the label
	JLabel amountLabel = new JLabel( "" + amount + " " + currency.toString());
					 
	// Make the label font somewhat bigger.
	amountLabel.setFont( new Font("Verdana", Font.PLAIN, 20));

	// Add the amount to the panel.
	resultPanel.add( amountLabel);

	return resultPanel;  // Return the created panel.
    }
}
