/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot;

import de.andreas_rueckert.MissingAccountDataException;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.bot.ui.TradeBotUI;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.currency.CurrencyProvider;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.order.OrderBook;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteNotFoundException;
import de.andreas_rueckert.trade.ui.OrderDialog;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import de.andreas_rueckert.util.SoundUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Insets;
import java.awt.Toolkit;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * This is a (hacked) native bot specifically for the btc-e.com trading site.
 * It creates a JPanel, so it can be used directly in the main trading app.
 */
public class BtcENativeBot implements ActionListener, ItemListener, TradeBot, TradeBotUI {

    // Static variables

    /**
     * The currency pairs to trade on.
     */
    private final static CurrencyPair CURRENCY_PAIR_BTC_USD = new CurrencyPairImpl( "BTC", "USD");
    private final static CurrencyPair CURRENCY_PAIR_LTC_BTC = new CurrencyPairImpl( "LTC", "BTC");
    private final static CurrencyPair CURRENCY_PAIR_LTC_USD = new CurrencyPairImpl( "LTC", "USD");

    /**
     * The only instance of this class (singleton pattern).
     */
    private static BtcENativeBot _instance = null;


    // Instance variables

    /**
     * A checkbox to enable automatic trading.
     */
    private JCheckBox _autoTradeCheckBox = null;

    /**
     * Flag to enable automatic trading.
     */
    private boolean _autoTradeFlag = false;

    /**
     * The base update interval in second.
     */
    private int BASE_UPDATE_INTERVAL = 11;

    /**
     * The client to connect to the trade site.
     */
    private TradeSite _btcEClient = null;
    
    /**
     * The display for the btc/usd buy conversion rate.
     */
    private JTextField _btcUsdBuyField = null;

    /**
     * The btc / usd depth.
     */
    private Depth _btcUsdDepth = null;

    /**
     * The display for the btc/usd sell conversion rate.
     */
    private JTextField _btcUsdSellField = null;

    /**
     * The default value for the fee is 0.2 percent currently.
     */
    private BigDecimal _defaultFee = new BigDecimal( "0.2");

    /**
     * The default format for bitcoin output in this app.
     */
    private DecimalFormat _defaultDecimalFormat = new DecimalFormat("#####.########");

    /**
     * The text field for the current fee.
     */
    private JTextField _feeField = null;

    /**
     * Array of text fields to display the current funds.
     */
    private JTextField [] _fundsBalanceFields = null;

    /**
     * Create a map for the current balances.
     */
    private Map< Currency, Amount> _balanceMap = new HashMap< Currency, Amount>();

    /**
     * Flag to indicate, that we're in a automatic trade.
     */
    private boolean _inAutoTradeFlag = false;

    /**
     * The green background color for good results.
     */
    private Color LIGHT_GREEN = new Color( 200, 255, 200);
    
    /**
     * The red background color for bad results.
     */
    private Color LIGHT_RED = new Color( 255, 200, 200);    

    /**
     * The yellow background color for attention.
     */
    private Color LIGHT_YELLOW = new Color( 255, 255, 200);

    /**
     * The display for the ltc / btc buy conversion rate.
     */
    private JTextField _ltcBtcBuyField = null;

    /**
     * The ltc / btc depth.
     */
    private Depth _ltcBtcDepth = null;

    /**
     * The display for the ltc / btc sell conversion rate.
     */
    private JTextField _ltcBtcSellField = null;

    /**
     * The display for the ltc / usd buy conversion rate.
     */
    private JTextField _ltcUsdBuyField = null;

    /**
     * The ltc usd depth.
     */
    private Depth _ltcUsdDepth = null;

    /**
     * The display for the ltc / usd sell conversion rate.
     */
    private JTextField _ltcUsdSellField = null;

    /**
     * Flag to indicate a pending mail notification.
     */
    private boolean _mailNotificationPending = false;

    /**
     * The minimum trading amount for bitcoins.
     */
    private BigDecimal _minimumTradingAmountBTC = new BigDecimal( "0.1");

    /**
     * The minimum trading amount for litecoins.
     */
    private BigDecimal _minimumTradingAmountLTC = new BigDecimal( "1.0");

    /**
     * An index for the next ticker to fetch.
     */
    private int _nextTickerToFetch = 0;

    /**
     * An array with the 6 order amount fields.
     */
    private JTextField [] _orderAmountFields;

    /**
     * The properties of this bot.
     */
    protected TradeBotProperties _properties = null;

    /**
     * A checkbox to send mail notification.
     */
     private JCheckBox _sendMailCheckBox = null;

    /**
     * The button to start and stop the bot.
     */
    private JButton _startStopButton = null;

    /**
     * These are the buttons to trigger trades.
     */
    private JButton [] _tradeButtons = null;

    /**
     * A logger for the trades.
     */
    protected TradeLogger _tradeLogger = null;

    /**
     * The max amount you can trade.
     */
    private Amount _ubluAmount = new Amount( "-1");

    /**
     * The text field to display the max. amount to possibly trade.
     */
    private JTextField _ubluAmountField = null;

    /**
     * The UBLU calculation input.
     */
    private Amount _ubluInput = new Amount( "10");

    /**
     * The usd input for the ublu triangle.
     */
    private JTextField _ubluInputField = null;

    /**
     * The usd output for the ublu triangle.
     */
    private JTextField _ubluOutput = null;

    /**
     * The profit or loss of the potential ublu deal.
     */
    private JTextField _ubluProfitField = null;

    /**
     * An UI panel to control the bot and get some info on
     * it's activities.
     */
    private JPanel _uiPanel = null;

    /**
     * The max. you can trade.
     */
    private Amount _ulbuAmount = new Amount( "-1");

    /**
     * The text field to display the max. amount to possibly trade.
     */
    private JTextField _ulbuAmountField = null;

    /**
     * The ULBU calculation input.
     */
    private Amount _ulbuInput = new Amount( "10");

    /**
     * The usd input for the ulbu triangle.
     */
    private JTextField _ulbuInputField = null;

    /**
     * The usd output if the ulbu triangle.
     */
    private JTextField _ulbuOutput = null;

    /**
     * The profit or loss of the potential ulbu deal.
     */
    private JTextField _ulbuProfitField = null;

    /**
     * A button to update the funds to their current amount on the trading site.
     */
    private JButton _updateFundsButton = null;

    /**
     * Create a randomizer for the update interval to make
     * the data request harder to detect as bot requests.
     */
    private Random _updateIntervalRandomizer = new Random();

    /**
     * The loop to update the ticker.
     */
    private Thread _updateThread = null;


    // Constructors

    /**
     * Create a new instance of this bot.
     * Private constructor for singleton pattern.
     */
    private BtcENativeBot() {

	_btcEClient = ModuleLoader.getInstance().getRegisteredTradeSite( "BTCe");

	if( _btcEClient == null) {
	    throw new TradeSiteNotFoundException( "btc-e.com client not found for btc-e native bot!");
	}
    }


    // Methods

    /**
     * The user pressed a button on the UI panel.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {

	if( e.getSource() == _startStopButton) {  // The user wants to start or stop the bot.

	    if( isStopped()) {
		start();
		_startStopButton.setText( "Stop");
	    } else {
		stop();
		_startStopButton.setText( "Start");
	    }
	}

	if( e.getSource() == _updateFundsButton) {
	    updateFunds();          // Get the current funds.
	    _uiPanel.invalidate();  // and show the new values.
	    _uiPanel.validate();	    
	}

	// Check all the trade buttons
	for( int curButton = 0; curButton < _tradeButtons.length; ++curButton) {

	    // If the current button was pressed.
	    if( e.getSource() == _tradeButtons[ curButton]) {

		// Perform a single trade.
		doSingleTrade( curButton);

		System.out.println( "DEBUG: calling doSingleTrade()");

		break;  // No need to check the other buttons.
	    }
	}
    }
    
    /**
     * Compute the triangle trades from the depth.
     */
    public void calculateTrianglesFromDepth() {

        Amount ulbuResult = new Amount( "-1");
	Amount ubluResult = new Amount( "-1");  // Indicate an error as the default.

	Amount ulbuInput = _uiPanel != null ? new Amount( _ulbuInputField.getText()) : _ulbuInput;

	BigDecimal fee = BigDecimal.ONE.subtract( _uiPanel != null 
						  ? new BigDecimal( _feeField.getText()).divide( new BigDecimal( "100"), MathContext.DECIMAL128) 
						  : _defaultFee);

	Price ltcUsdRate = null;
	Price ltcBtcRate = null;
	Price btcUsdRate = null;
	Amount ltcUsdAmount = null;
	Amount ltcBtcAmount = null;
	Amount btcUsdAmount = null;
	
	BigDecimal current;

	Price ulbuProfit = new Price( "-1");
	Price ubluProfit = new Price( "-1");

	StringBuffer mailText = new StringBuffer();

	_ulbuAmount = _ubluAmount = null;  // Init amounts to default.

	if( _ltcUsdDepth.getSellSize() > 0) {  // If the are not enough sells, it won't work.

	    DepthOrder ltcUsdOrder = _ltcUsdDepth.getSell( 0);
	    
	    ltcUsdRate = ltcUsdOrder.getPrice();
	    ltcUsdAmount = ltcUsdOrder.getAmount();

	    current = ulbuInput.divide( ltcUsdRate, MathContext.DECIMAL128).multiply( fee);

	    if( _ltcBtcDepth.getBuySize() > 0) {

		DepthOrder ltcBtcOrder = _ltcBtcDepth.getBuy( 0);

		ltcBtcRate = ltcBtcOrder.getPrice();
		ltcBtcAmount = ltcBtcOrder.getAmount();
 
		current = current.multiply( ltcBtcRate).multiply( fee);
		
		if( _btcUsdDepth.getBuySize() > 0) {
		    
		    DepthOrder btcUsdOrder = _btcUsdDepth.getBuy( 0);

		    btcUsdRate = btcUsdOrder.getPrice();
		    btcUsdAmount = btcUsdOrder.getAmount();
	
		    ulbuResult = new Amount( current.multiply( btcUsdRate).multiply( fee));

		    // Now do the amount calculation backwards.
		    _ulbuAmount = new Amount( btcUsdAmount.divide( fee, MathContext.DECIMAL128));
		    _ulbuAmount = new Amount( _ulbuAmount.divide( ltcBtcRate, MathContext.DECIMAL128));
		    if( _ulbuAmount.compareTo( ltcBtcAmount) > 0) {
			_ulbuAmount = ltcBtcAmount;
		    }
		    _ulbuAmount = new Amount( _ulbuAmount.divide( fee, MathContext.DECIMAL128).multiply( ltcUsdRate));
		    if( _ulbuAmount.compareTo( ltcUsdAmount.multiply( ltcUsdRate)) > 0) {
			_ulbuAmount = new Amount( ltcUsdAmount.multiply( ltcUsdRate));
		    }

		    // There should be check for the actual available amount on the user's account here, once we have it...

		    // Now compute the potential profit or loss from this deal.
		    ulbuProfit = new Price( _ulbuAmount.divide( ltcUsdRate, MathContext.DECIMAL128).multiply( fee));
					    ulbuProfit = new Price( ulbuProfit.multiply( ltcBtcRate).multiply( fee).multiply( btcUsdRate).multiply( fee));
		    ulbuProfit = new Price( ulbuProfit.subtract( _ulbuAmount));

		    if( ( ( _uiPanel == null) || _sendMailCheckBox.isSelected()) && ( _ulbuAmount.compareTo( new BigDecimal( "0.1")) >= 0)) { 
			    mailText.append( "ulbu trade is profitable. Amount is " + _ulbuAmount + " with profit " + ulbuProfit + "\n");
		    }
		}
	    }
	}

	if( _uiPanel != null) {
	    
	    _ltcUsdBuyField.setText( ltcUsdRate != null ? "" + ltcUsdRate : "not available");
	    
	    _ltcBtcSellField.setText( ltcBtcRate != null ? "" + ltcBtcRate : "not available");
	    
	    _btcUsdSellField.setText( btcUsdRate != null ? "" + btcUsdRate : "not available");

	    _orderAmountFields[0].setText( "" + ltcUsdAmount);
	    _orderAmountFields[1].setText( "" + ltcBtcAmount);
	    _orderAmountFields[2].setText( "" + btcUsdAmount);
	    
	    _ulbuOutput.setText( ulbuResult != null ? _defaultDecimalFormat.format( ulbuResult) : "trade not possible");
	    
	    _ulbuOutput.setBackground( ulbuResult.compareTo( ulbuInput) > 0 
				       ? LIGHT_GREEN 
				       : ( ulbuResult.multiply( new BigDecimal("1.01")).compareTo( ulbuInput) > 0) ? LIGHT_YELLOW : LIGHT_RED);

	    _ulbuAmountField.setText( _defaultDecimalFormat.format( _ulbuAmount));

	    _ulbuProfitField.setText( _ulbuAmount != null ? _defaultDecimalFormat.format( ulbuProfit) : "not possible");

	    _ulbuProfitField.setBackground( ( _ulbuAmount != null) && ( ulbuProfit.compareTo( BigDecimal.ZERO) > 0)  ? LIGHT_GREEN : LIGHT_RED);

	    // Activate the trade buttons as required.
	    if( ulbuProfit.compareTo( BigDecimal.ZERO) <= 0) { // No profit => disable all buttons for this trade path.

		_tradeButtons[ 4].setEnabled( false);
		_tradeButtons[ 3].setEnabled( false);
		_tradeButtons[ 1].setEnabled( false);

	    } else {  // Profit => enable all button with some funds to trade.
	    
		// ToDo: set prices and amounts for each dialog...
		_tradeButtons[ 4].setEnabled( ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "USD")) == null) 
					      || ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "USD")).compareTo( BigDecimal.ZERO) > 0));
		_tradeButtons[ 3].setEnabled( ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "LTC")) == null) 
					      || ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "LTC")).compareTo( BigDecimal.ZERO) > 0));
		_tradeButtons[ 1].setEnabled( ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "BTC")) == null) 
					      || ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "BTC")).compareTo( BigDecimal.ZERO) > 0));

		// Play a sound to inform the user.
		SoundUtils.getInstance().playBeep();
	    }
	}

	Amount ubluInput = _uiPanel != null ? new Amount( _ubluInputField.getText()) : _ubluInput;

	// Reset the rates
	ltcUsdRate = null; 
	ltcBtcRate = null;
	btcUsdRate = null;

	// Reset the amounts
	ltcUsdAmount = null; 
	ltcBtcAmount = null;
	btcUsdAmount = null;

	if( _btcUsdDepth.getSellSize() > 0) {

	    DepthOrder btcUsdOrder = _btcUsdDepth.getSell( 0);

	    btcUsdRate = btcUsdOrder.getPrice();
	    btcUsdAmount = btcUsdOrder.getAmount();

	    current = ubluInput.divide( btcUsdRate, MathContext.DECIMAL128).multiply( fee);

	    if( _ltcBtcDepth.getSellSize() > 0) {

		DepthOrder ltcBtcOrder = _ltcBtcDepth.getSell( 0);

		ltcBtcRate = ltcBtcOrder.getPrice();
		ltcBtcAmount = ltcBtcOrder.getAmount();

		current = current.divide( ltcBtcRate, MathContext.DECIMAL128).multiply( fee);

		if( _ltcUsdDepth.getBuySize() > 0) {

		    DepthOrder ltcUsdOrder = _ltcUsdDepth.getBuy( 0);

		    ltcUsdRate = ltcUsdOrder.getPrice();
		    ltcUsdAmount = ltcUsdOrder.getAmount();
	
		    ubluResult = new Amount( current.multiply( ltcUsdRate).multiply( fee));

		    // Now do the amount calculation backwards.
		    _ubluAmount = ltcUsdAmount;
		    _ubluAmount = new Amount( _ubluAmount.divide( fee, MathContext.DECIMAL128));
		    if( _ubluAmount.compareTo( ltcBtcAmount) > 0) {
			_ubluAmount = ltcBtcAmount;
		    }
		    _ubluAmount = new Amount( _ubluAmount.multiply( ltcBtcRate).divide( fee, MathContext.DECIMAL128));
		    if( _ubluAmount.compareTo( btcUsdAmount) > 0) {
			_ubluAmount = btcUsdAmount;
		    }
		    _ubluAmount = new Amount( _ubluAmount.multiply( btcUsdRate));

		    // There should be check for the actual available amount on the user's account here, once we have it...

		    // Now compute the potential profit or loss from this deal.
		    ubluProfit = new Price( _ubluAmount.divide( btcUsdRate, MathContext.DECIMAL128).multiply( fee));
		    ubluProfit = new Price( ubluProfit.divide( ltcBtcRate, MathContext.DECIMAL128).multiply( fee).multiply( ltcUsdRate).multiply( fee));
		    ubluProfit = new Price( ubluProfit.subtract( _ubluAmount));

		    if( ( ( _uiPanel == null) || _sendMailCheckBox.isSelected()) && ( _ubluAmount.compareTo( new BigDecimal( "0.1")) >= 0 )) { 
			    mailText.append( "ublu trade is profitable. Amount is " + _ubluAmount + " with profit " + ubluProfit + "\n");
		    }
		}
	    }
	}

	if( _uiPanel != null) {

	    _btcUsdBuyField.setText( btcUsdRate != null ? "" + btcUsdRate : "not available");

	    _ltcBtcBuyField.setText( ltcBtcRate != null ? "" + ltcBtcRate : "not available");

	    _ltcUsdSellField.setText( ltcUsdRate != null ? "" + ltcUsdRate : "not available");

	    _orderAmountFields[3].setText( "" + btcUsdAmount);
	    _orderAmountFields[4].setText( "" + ltcBtcAmount);
	    _orderAmountFields[5].setText( "" + ltcUsdAmount);

	    _ubluOutput.setText( ubluResult != null ? _defaultDecimalFormat.format( ubluResult) : "trade not possible");

	    _ubluOutput.setBackground( ubluResult.compareTo( ubluInput) > 0 
				       ? LIGHT_GREEN 
				       : ( ubluResult.multiply( new BigDecimal("1.01")).compareTo( ubluInput) > 0) ? LIGHT_YELLOW : LIGHT_RED);

	    _ubluAmountField.setText( _defaultDecimalFormat.format( _ubluAmount));

	    _ubluProfitField.setText( _ubluAmount != null ? _defaultDecimalFormat.format( ubluProfit) : "not possible");
	    
	    _ubluProfitField.setBackground( ( _ubluAmount != null) && ( ubluProfit.compareTo( BigDecimal.ZERO) > 0)  ? LIGHT_GREEN : LIGHT_RED);


	    // Activate the trade buttons as required.
	    if( ubluProfit.compareTo( BigDecimal.ZERO) <= 0) { // No profit => disable all buttons for this trade path.

		_tradeButtons[ 0].setEnabled( false);
		_tradeButtons[ 2].setEnabled( false);
		_tradeButtons[ 5].setEnabled( false);

	    } else {  // Profit => enable all button with some funds to trade.
	    
		// ToDo: set prices and amounts for each dialog...
		_tradeButtons[ 0].setEnabled( ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "USD")) == null) 
					      || ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "USD")).compareTo( BigDecimal.ZERO) > 0));
		_tradeButtons[ 2].setEnabled( ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "BTC")) == null) 
					      || ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "BTC")).compareTo( BigDecimal.ZERO) > 0));
		_tradeButtons[ 5].setEnabled( ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "LTC")) == null) 
					      || ( _balanceMap.get( CurrencyProvider.getInstance().getCurrencyForCode( "LTC")).compareTo( BigDecimal.ZERO) > 0));

		// Play a sound to inform the user.
		SoundUtils.getInstance().playBeep();
	    }
	}

	if( ( ubluResult.compareTo( ubluInput) > 0) || ( ulbuResult.compareTo( ulbuInput) > 0)) {  // If we made a profit

	    if( _autoTradeFlag && ! _inAutoTradeFlag) {  // Do automatic trading and not already in a trade?
		
	    }

	    if( _uiPanel != null) {  // If the user has an UI.
		Toolkit.getDefaultToolkit().beep();  // beep, so he gets the info.
	    }
	}


	// Send mails only after 4 fetches (once a minute, or so), so we don't send mails
	// every few seconds...
	if( _mailNotificationPending && ( mailText.length() > 0)) {
     
	    try {
		TradeApp.getApp().getEmailNotifier().sendMail( "mail@andreas-rueckert.de"
							       , "ulbu or ublu deal is profitable"
							       , "Notification: \n\n" + mailText.toString());

		LogUtils.getInstance().getLogger().info( "Sent mail on potential profitable triangle trade");

		_mailNotificationPending = false;
		mailText = new StringBuffer();  // Delete the mail text.

	    } catch( Exception e) {  // We don't want the app to stop, just because the mail sending failed.
		System.err.println( "Could not send mail to notify about potential profit on btc-e.com: " + e.toString());
	    }
	}
    }

    /**
     * Check, if all the depths are available.
     *
     * @return true, if all the depths are available. False otherwise.
     */
    private boolean depthsAreAvailable() {
	return ( _ltcUsdDepth != null) && ( _ltcBtcDepth != null) &&	( _btcUsdDepth != null);
    }

    /**
     * Do a single conversion trade.
     *
     * @param currencyPairIndex The index of the currency pair.
     */
    private void doSingleTrade( int currencyPairIndex) {

	OrderDialog orderDialog = null;

	switch( currencyPairIndex) {
	case 4: orderDialog = new OrderDialog( null
					       , true
					       , "Buying ltc for usd"
					       , _btcEClient
					       , OrderType.BUY
					       , new Price( _ltcUsdBuyField.getText())
					       , CURRENCY_PAIR_LTC_USD
					       , new Amount( "0"));
	    break;
	case 3: orderDialog = new OrderDialog( null
					       , true
					       , "Selling ltc for btc"
					       , _btcEClient
					       , OrderType.SELL
					       , new Price( _ltcBtcSellField.getText())
					       , CURRENCY_PAIR_LTC_BTC
					       , new Amount( "0"));
	    break;
	case 1: orderDialog = new OrderDialog( null
					       , true
					       , "Selling btc for usd"
					       , _btcEClient
					       , OrderType.SELL
					       , new Price( _btcUsdSellField.getText())
					       , CURRENCY_PAIR_BTC_USD
					       , new Amount( "0"));
	    break;
	case 0: orderDialog = new OrderDialog( null
					       , true
					       , "Buying btc for usd"
					       , _btcEClient
					       , OrderType.BUY
					       , new Price( _btcUsdBuyField.getText())
					       , CURRENCY_PAIR_BTC_USD
					       , new Amount( "0"));
	    break;
	case 2: orderDialog = new OrderDialog( null
					       , true
					       , "Buying ltc for btc"
					       , _btcEClient
					       , OrderType.BUY
					       , new Price( _ltcBtcBuyField.getText())
					       , CURRENCY_PAIR_LTC_BTC
					       , new Amount( "0"));  
	    break;
	case 5: orderDialog = new OrderDialog( null
					       , true
					       , "Selling ltc for usd"
					       , _btcEClient
					       , OrderType.SELL
					       , new Price( _ltcUsdSellField.getText())
					       , CURRENCY_PAIR_LTC_USD
					       , new Amount( "0"));
	    break;
	}

	// Show the order dialog.
	orderDialog.show();

	// Now try to get the order from the dialog.
	// If the user canceled the dialog, it's null.

	SiteOrder order = orderDialog.getOrder();

	if( order != null) {  // If the user did not cancel the order.

	    // Add the order to the order book.
	    String orderId = getOrderBook().add( order);

	    // Now try to execute the order.
	    OrderStatus orderStatus = getOrderBook().executeOrder( orderId);

	    // If the order was not filled, inform the user...
	    if( orderStatus != OrderStatus.FILLED) {
		
		JOptionPane.showMessageDialog( getUIPanel(), "Order was not filled (completely)", "Order not filled", JOptionPane.WARNING_MESSAGE);
	    }
	}
    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static BtcENativeBot getInstance() {

	if( _instance == null) {              // If there is no instance yet,
	    _instance = new BtcENativeBot();  // create one.
	}

	return _instance;  // Return the only instance of this class.
    }

    /**
     * Get the name of this bot.
     *
     * @return The name of this bot.
     */
    public String getName() {
	return "BTCe";
    }

    /**
     * Get an order book to add orders.
     *
     * @return An order book.
     */
    private OrderBook getOrderBook() {
	return CryptoCoinOrderBook.getInstance();
    }

    /**
     * Get the properties of this bot.
     *
     * @return The properties of this bot.
     */
    public TradeBotProperties getProperties() {
	
	if( _properties == null) {                                 // If there are no propertes yet,
	    _properties = new TradeBotProperties( getInstance());  // create them.
	}
	return _properties;  
    }

    /**
     * Get the section name in a global property file (sort of an hack to avoid duplicated key names).
     */
    public String getPropertySectionName() {

	return "Bot_" + getName();
    }
    
    /**
     * Get the settings of this bot.
     *
     * @return The properties of this bot as a list.
     */
    public PersistentPropertyList getSettings() {

	return new PersistentPropertyList();  // No properties yet implemented, so return an empty list.
    }

    /**
     * Get the logger of this bot.
     *
     * @return The trade logger of this bot.
     */
    public TradeLogger getTradeLogger() {

	return _tradeLogger;
    }

    /**
     * This is a hacked method, since the UI panel was already integrated,
     * when I decided to define a separate UI interface. 
     *
     * @return The trade bot UI.
     */
    public TradeBotUI getUI() {
	return this;  // The UI was already integrated, so just return this.
    }

    /**
     * Get an UI panel to control the bot and get some infos on
     * the activities.
     *
     * @return A JPanel object to control the bot and get some infos 
     *         on it's activities.
     */
    public JPanel getUIPanel() {

	if( _uiPanel == null) {       // If there is no UI panel yet,

	    // Create an array for the order amounts.
	    _orderAmountFields = new JTextField[6];

	    // Create an array for the funds display.
	    _fundsBalanceFields = new JTextField[3];

	    // Create an array for the trade trigger buttons.
	    _tradeButtons = new JButton[6];

	    _uiPanel = new JPanel();  // create one.

	    _uiPanel.setLayout( new BorderLayout());

	    // Add panels for triangle trades.
	    
	    // The settings for the trades.
	    JPanel settingsPanel = new JPanel();

	    // Fee setting. Standard fee is 0.2% at the moment (july, 4th, 2012).
	    settingsPanel.add( new JLabel( "Fee:"));
	    settingsPanel.add( _feeField = new JTextField( "" + _defaultFee));
	    _feeField.setHorizontalAlignment( JTextField.RIGHT);
	    settingsPanel.add( new JLabel( "%"));

	    // Add a checkbox for the user, whether he wants mail notifications or not.
	    settingsPanel.add( _sendMailCheckBox = new JCheckBox( "Mail notification", false));

	    // Add a checkbox to enable automatic trading.
	    settingsPanel.add( _autoTradeCheckBox = new JCheckBox( "Automatic trading", _autoTradeFlag));
	    _autoTradeCheckBox.addItemListener( this);

	    // Add a button to update the funds.
	    settingsPanel.add( _updateFundsButton = new JButton( "Update funds"));
	    _updateFundsButton.addActionListener( this);
	
	    _uiPanel.add( settingsPanel, BorderLayout.NORTH);

	    JPanel calculationPanel = new JPanel();
	    calculationPanel.setLayout( new GridLayout( 9, 1));

	    JPanel fundsPanel = new JPanel();
	    fundsPanel.add( new JLabel( "Funds:  "));
	    fundsPanel.add( _fundsBalanceFields[0] = new JTextField( "0", 12));
	    _fundsBalanceFields[0].setEditable( false);
	    _fundsBalanceFields[0].setHorizontalAlignment( JTextField.RIGHT);
	    fundsPanel.add( new JLabel( "$  "));
	    fundsPanel.add( _fundsBalanceFields[1] = new JTextField( "0", 12));
	     _fundsBalanceFields[1].setEditable( false);
	     _fundsBalanceFields[1].setHorizontalAlignment( JTextField.RIGHT);
	    fundsPanel.add( new JLabel( "btc  "));
	    fundsPanel.add( _fundsBalanceFields[2] = new JTextField( "0", 12));
	     _fundsBalanceFields[2].setEditable( false);
	     _fundsBalanceFields[2].setHorizontalAlignment( JTextField.RIGHT);
	    fundsPanel.add( new JLabel( "ltc"));

	    calculationPanel.add( fundsPanel);


	    JPanel ratePanel1 = new JPanel();
	
	    ratePanel1.add( new JLabel( "Rates:"));
	    ratePanel1.add( new JLabel( "ltc/usd:"));
	    ratePanel1.add( _ltcUsdBuyField = new JTextField( "0.0000"));
	    _ltcUsdBuyField.setEditable( false);  // Do not simulate other rates for now.
	    ratePanel1.add( new JLabel( "ltc/btc:"));
	    ratePanel1.add( _ltcBtcSellField = new JTextField( "0.0000"));
	    _ltcBtcSellField.setEditable( false);
	    ratePanel1.add( new JLabel( "btc/usd:"));
	    ratePanel1.add( _btcUsdSellField = new JTextField( "0.0000"));
	    _btcUsdSellField.setEditable( false);

	    calculationPanel.add( ratePanel1);

	    JPanel ulbuOrderAmountPanel = new JPanel();
	    ulbuOrderAmountPanel.add( new JLabel( "Amounts:"));
	    ulbuOrderAmountPanel.add( _orderAmountFields[0] = new JTextField( "-1", 10));
	    _orderAmountFields[0].setEditable( false);
	    _orderAmountFields[0].setHorizontalAlignment( JTextField.RIGHT);
	    ulbuOrderAmountPanel.add( new JLabel( "ltc  "));
	    ulbuOrderAmountPanel.add( _orderAmountFields[1] = new JTextField( "-1", 10));
	    _orderAmountFields[1].setEditable( false);
	    _orderAmountFields[1].setHorizontalAlignment( JTextField.RIGHT);
	    ulbuOrderAmountPanel.add( new JLabel( "ltc  "));
	    ulbuOrderAmountPanel.add( _orderAmountFields[2] = new JTextField( "-1", 10));
	    _orderAmountFields[2].setEditable( false);
	    _orderAmountFields[2].setHorizontalAlignment( JTextField.RIGHT);
	    ulbuOrderAmountPanel.add( new JLabel( "btc"));
	    calculationPanel.add( ulbuOrderAmountPanel);
	    
	    // This is the panel for the
	    // usd => ltc => btc => usd 
	    // triangle
	    JPanel ulbuPanel = new JPanel();

	    // ToDo: add listener to modify _ulbuInput and _ubluInput in case of an input
	    // in the text fields!

	    ulbuPanel.add( _ulbuInputField = new JTextField( "" + _ulbuInput));
	    _ulbuInputField.setHorizontalAlignment( JTextField.RIGHT);
	    ulbuPanel.add( new JLabel( "usd"));
	    ulbuPanel.add( _tradeButtons[4] = new JButton( "=>"));
	    _tradeButtons[4].setMargin( new Insets( 1, 1, 1, 1)); 
	    ulbuPanel.add( new JLabel( "ltc"));
	    ulbuPanel.add( _tradeButtons[3] = new JButton( "=>"));
	    _tradeButtons[3].setMargin( new Insets( 1, 1, 1, 1));
	    ulbuPanel.add( new JLabel( "btc"));
	    ulbuPanel.add( _tradeButtons[1] = new JButton( "=>"));
	    _tradeButtons[1].setMargin( new Insets( 1, 1, 1, 1));
	    ulbuPanel.add( new JLabel( "usd"));

	    ulbuPanel.add( _ulbuOutput = new JTextField( "10.000000", 12));
	    _ulbuOutput.setEditable( false);
	    _ulbuOutput.setHorizontalAlignment( JTextField.RIGHT);
	    
	    calculationPanel.add( ulbuPanel);  // Add the ulbuPanel to the main panel.

	    JPanel ulbuAmountPanel = new JPanel();
	    ulbuAmountPanel.add( new JLabel( "Max. amount:"));
	    ulbuAmountPanel.add( _ulbuAmountField = new JTextField( "-1", 12));
	    _ulbuAmountField.setEditable( false);
	    _ulbuAmountField.setHorizontalAlignment( JTextField.RIGHT);
	    ulbuAmountPanel.add( new JLabel( "Profit:"));
	    ulbuAmountPanel.add( _ulbuProfitField = new JTextField( "-1", 12));
	    _ulbuProfitField.setEditable( false);
	    _ulbuProfitField.setHorizontalAlignment( JTextField.RIGHT);

	    calculationPanel.add( ulbuAmountPanel);
	    
	    JPanel ratePanel2 = new JPanel();

	    ratePanel2.add( new JLabel( "Rates:"));
	    ratePanel2.add( new JLabel( "btc/usd:"));
	    ratePanel2.add( _btcUsdBuyField = new JTextField( "0.0000"));
	    _btcUsdBuyField.setEditable( false);
	    ratePanel2.add( new JLabel( "ltc/btc:"));
	    ratePanel2.add( _ltcBtcBuyField = new JTextField( "0.0000"));
	    _ltcBtcBuyField.setEditable( false);
	    ratePanel2.add( new JLabel( "ltc/usd:"));
	    ratePanel2.add( _ltcUsdSellField = new JTextField( "0.0000"));
	    _ltcUsdSellField.setEditable( false);

	    calculationPanel.add( ratePanel2);

	    JPanel ubluOrderAmountPanel = new JPanel();
	    ubluOrderAmountPanel.add( new JLabel( "Amounts:"));
	    ubluOrderAmountPanel.add( _orderAmountFields[3] = new JTextField( "-1", 10));
	    _orderAmountFields[3].setEditable( false);
	    _orderAmountFields[3].setHorizontalAlignment( JTextField.RIGHT);
	    ubluOrderAmountPanel.add( new JLabel( "btc  "));
	    ubluOrderAmountPanel.add( _orderAmountFields[4] = new JTextField( "-1", 10));
	    _orderAmountFields[4].setEditable( false);
	    _orderAmountFields[4].setHorizontalAlignment( JTextField.RIGHT);
	    ubluOrderAmountPanel.add( new JLabel( "ltc  "));
	    ubluOrderAmountPanel.add( _orderAmountFields[5] = new JTextField( "-1", 10));
	    _orderAmountFields[5].setEditable( false);
	    _orderAmountFields[5].setHorizontalAlignment( JTextField.RIGHT);
	    ubluOrderAmountPanel.add( new JLabel( "ltc"));

	    calculationPanel.add( ubluOrderAmountPanel);

	    // This is the panel for the 
	    // usd => btc => ltc => usd
	    // triangle
	    JPanel ubluPanel = new JPanel();
	
	    ubluPanel.add( _ubluInputField = new JTextField( "" + _ubluInput));
	    _ubluInputField.setHorizontalAlignment( JTextField.RIGHT);
	    ubluPanel.add( new JLabel( "usd"));
	    ubluPanel.add( _tradeButtons[0] = new JButton( "=>"));
	    _tradeButtons[0].setMargin( new Insets( 1, 1, 1, 1)); 
	    ubluPanel.add( new JLabel( "btc"));
	    ubluPanel.add( _tradeButtons[2] = new JButton( "=>"));
	    _tradeButtons[2].setMargin( new Insets( 1, 1, 1, 1));
	    ubluPanel.add( new JLabel( "ltc"));
	    ubluPanel.add( _tradeButtons[5] = new JButton( "=>"));
	    _tradeButtons[5].setMargin( new Insets( 1, 1, 1, 1));
	    ubluPanel.add( new JLabel( "usd"));
	    ubluPanel.add( _ubluOutput = new JTextField( "10.000000", 12));
	    _ubluOutput.setEditable( false);
	    _ubluOutput.setHorizontalAlignment( JTextField.RIGHT);

	    calculationPanel.add( ubluPanel);

	    JPanel ubluAmountPanel = new JPanel();
	    ubluAmountPanel.add( new JLabel( "Max. amount:"));
	    ubluAmountPanel.add( _ubluAmountField = new JTextField( "-1", 12));
	    _ubluAmountField.setEditable( false);
	    _ubluAmountField.setHorizontalAlignment( JTextField.RIGHT);
	    ubluAmountPanel.add( new JLabel( "Profit:"));
	    ubluAmountPanel.add( _ubluProfitField = new JTextField( "-1", 12));
	    _ubluProfitField.setEditable( false);
	    _ubluProfitField.setHorizontalAlignment( JTextField.RIGHT);

	    calculationPanel.add( ubluAmountPanel);

	    _uiPanel.add( calculationPanel, BorderLayout.CENTER);

	    // Add a listener to all the trade buttons and disable them all by default.
	    for( int curButton = 0; curButton < _tradeButtons.length; ++curButton) {

		// Add this bot as the listener to the buttons.
		_tradeButtons[ curButton].addActionListener( this);

		// Disable this button.
		// The buttons are enabled later, if there are funds and some
		// potential profit.
		_tradeButtons[ curButton].setEnabled( false);
	    }

	    // Add a panel for the start/stop button.

	    JPanel buttonPanel = new JPanel();
	    
	    buttonPanel.add( _startStopButton = new JButton( "Start"));
	    _startStopButton.addActionListener( this);

	    _uiPanel.add( buttonPanel, BorderLayout.SOUTH);
	}

	return _uiPanel;  // Return the UI panel.
    }

    /**
     * Get the version string of this bot.
     *
     * @return The version string of this bot.
     */
    public String getVersionString() {

	// Get the version of this bot as a string.
	return "0.2.1 ( Briegel )";
    }

    /**
     * Check, if the bot is stopped.
     *
     * @return true, if the bot is stopped.
     */
    public boolean isStopped() {
	return _updateThread == null;  // If there is no update thread, the bot is stopped.
    }

    /**
     * The state of a checkbox has changed.
     *
     * @param e The item event.
     */
    public void itemStateChanged( ItemEvent e) {

	if( e.getSource() == _autoTradeCheckBox) {  // Check, if the auto trading was toggled.
	    _autoTradeFlag = _autoTradeCheckBox.isSelected();
	}
    }

    /**
     * Set the properties of this bot.
     *
     * @param propertyList The tradebot properties.
     */
    public void setSettings( PersistentPropertyList propertyList) {
    }
    
    /**
     * Start the bot.
     */
    public void start() {

	updateFunds();  // Show the initial funds.

	// Create a ticker thread.
	_updateThread = new Thread() {

		@Override public void run(){
		    while( _updateThread == this) {

			updateDepthsSequentially();  // Query new ticker data.
			
			if( depthsAreAvailable()) {  // If fetching the tickers worked.
			    
			    calculateTrianglesFromDepth();  // Compute the result of the trades.			    
			    
			    // Notify the user here, if there is a good deal?
			    
			    if( _uiPanel != null) {     // If there's an UI,
				_uiPanel.invalidate();  // show the results.
				_uiPanel.validate();
			    }
			}

			try {
			    sleep( BASE_UPDATE_INTERVAL * 1000 + _updateIntervalRandomizer.nextInt( 2 * 1000));
			} catch( InterruptedException ie) {
			    System.err.println( "Ticker or depth loop sleep interrupted: " + ie.toString());
			}
		    }
		}
	    };
	_updateThread.start();  // Start the update thread.
    }
    
    /**
     * Stop the bot.
     */
    public void stop() {

	Thread updateThread = _updateThread;  // So we can join the thread later.

	_updateThread = null;  // Signal the thread to stop.

	try {
	    updateThread.join();  // Wait for the thread to end.
	} catch( InterruptedException ie)  {
	    System.err.println( "Ticker stop join interrupted: " + ie.toString());
	}
    }

    /**
     * Update the funds.
     */
    private void updateFunds() {

	if( _uiPanel != null) {  // If there's an UI, display the current funds.

	    try {

		for( TradeSiteAccount a : _btcEClient.getAccounts( null)) {
		    
		    CurrencyImpl c = (CurrencyImpl)( a.getCurrency());
		    
		    // Store the balance in the balance map.
		    _balanceMap.put( c, new Amount( a.getBalance()));
		    
		    if( c.hasCode( "USD")) {
			_fundsBalanceFields[0].setText( _defaultDecimalFormat.format( a.getBalance()));
		    } else if( c.hasCode( "BTC")) {
			_fundsBalanceFields[1].setText( _defaultDecimalFormat.format( a.getBalance()));
		    } else if( c.hasCode( "LTC")) {
			_fundsBalanceFields[2].setText( _defaultDecimalFormat.format( a.getBalance()));
		    }
		}
	    } catch( MissingAccountDataException made) {
		LogUtils.getInstance().getLogger().warn( "No btc-e account data available:" + made);
	    }
	}
    }
    
    /**
    * Update depths sequentially.
    */
    private void updateDepthsSequentially() {
	
	switch( _nextTickerToFetch) {

	    // The btc / usd depth seems to change more often, so fetch it more often.
	case 0:
	case 2: 
	    _btcUsdDepth = _btcEClient.getDepth( CURRENCY_PAIR_BTC_USD);
	    if( _btcUsdDepth != null) { ++_nextTickerToFetch; }
	    break;
	case 1: 
	    _ltcBtcDepth = _btcEClient.getDepth( CURRENCY_PAIR_LTC_BTC);
	    if( _ltcBtcDepth != null) { ++_nextTickerToFetch; }
	    break;
	case 3: 
	    _ltcUsdDepth = _btcEClient.getDepth( CURRENCY_PAIR_LTC_USD);
	    if( _ltcUsdDepth != null) { _nextTickerToFetch = 0; }
	    break;
	}
    } 

    /**
     * Update the shown values.
     *
     * @param inputValues The input values for the calculation.
     * @param outputValues The output values of the calculation.
     */
    public void updateValues( BigDecimal [] inputValues, BigDecimal [] outputValues) {

	// Is not used in this dated implementation. This should be changed...
    }
}
