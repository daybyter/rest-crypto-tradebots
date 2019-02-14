/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.intersite;

import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.bot.intersite.ui.InterSiteBotUI;
import de.andreas_rueckert.trade.bot.NativeBotCore;
import de.andreas_rueckert.trade.bot.TradeBot;
import de.andreas_rueckert.trade.bot.TradeBotProperties;
import de.andreas_rueckert.trade.bot.ui.TradeBotUI;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.currency.CurrencyProvider;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.order.OrderFactory;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Random;


/**
 * This is a bot specifically for the trade of btc between
 * trading sites.
 */
public class InterSiteBot extends NativeBotCore implements TradeBot {

    // Inner classes


    // Static variables

    /**
     * The only instance of the bot (singleton pattern).
     */
    private static InterSiteBot _instance = null;


    // Instance variables

    /**
     * The input values for the triangle calculations.
     */
    public BigDecimal [] _inputValues = null;

    /**
     * The output values for the triangle calculations.
     */
    public BigDecimal [] _outputValues = null;

    /**
     * The trade paths to check.
     */
    private ArrayList<TradePath> _tradePaths = new ArrayList<TradePath>();

    /**
     * Create a randomizer for the update interval to make
     * the data request harder to detect as bot requests.
     */
    private Random _updateIntervalRandomizer = new Random();


    // Constructors

    /**
     * Create a new inter site bot.
     */
    private InterSiteBot() {

	// Use a 15s update interval.
	this._updateInterval = 15;

	// Set the name of this bot.
	this._name = "Intersite";

	// Set the version of this bot.
	this._versionString = "0.1.1 ( Kadlec )";

	// Add some trade paths.
	//addTradePath( "BTCe", "USD", "BTC", "Bitstamp", true);
	//addTradePath( "BTCe", "EUR", "BTC", "Bitcurex", true);
	//addTradePath( "BTCe", "BTC", "LTC", "Coins-E", true);
	//addTradePath( "BTCe", "USD", "BTC", "LakeBTC", true);
	//addTradePath( "LakeBTC", "USD", "BTC", "Bitfinex", true);
	addTradePath( "BTCe", "USD", "BTC", "Bitfinex", true);
	//addTradePath( "Huobi", "BTC", "LTC", "BTCe", true);
	//addTradePath( "Huobi", "CNY", "BTC", "ANX", true);
	//addTradePath( "Huobi", "CNY", "BTC", "BtcChina", true);
	// addTradePath( "ANX", "USD", "BTC", "BTCe", true);
	// addTradePath( "BTCe", "EUR", "BTC", "Bitcoin.de", true);
	addTradePath( "BTCe", "LTC", "BTC", "BtcChina", true);
	addTradePath( "BTCe", "BTC", "LTC", "BtcChina", true);
	addTradePath( "BTCe", "USD", "BTC", "OKCoin", true);
	//addTradePath( "BTCe", "USD", "BTC", "Bter", true);
	addTradePath( "OKCoin", "USD", "BTC", "Bitfinex", true);
	
	// Create the arrays for the data transfer to the UI.
	// One input and output value is required for each trade path.
	_inputValues = new BigDecimal[ getTradePaths().size()];
	_outputValues = new BigDecimal[ getTradePaths().size() * 2];

	// Set the default input values.
	for( int currentSite = 0; currentSite < _inputValues.length; ++currentSite) {
	    _inputValues[currentSite] = new BigDecimal( "10.0");
	}
    }


    // Methods

    /**
     * Add a new trade path to the list of trade paths.
     *
     * @param tradePath The new trade path to add to the list.
     */
    private void addTradePath( TradePath tradePath) {

	// Add this trade path to the list.
	_tradePaths.add( tradePath);
    }

    /**
     * Add a trade path and eventually also the reversed trade path.
     *
     * @param tradePath The trade path to add.
     * @param reverse_too Flag to indicate, that the reversed path should be added, too.
     */
    private void addTradePath( TradePath tradePath, boolean reverse_too) {

	// Add this trade path.
	addTradePath( tradePath);

	// If the user wants to add the reverse trade path, too,
	if( reverse_too) {

	    // Add the reverse trade path.
	    addTradePath( tradePath.reverse());
	}
    }

    /**
     * This is just a convenience method for making trade paths easier.
     *
     * @param tradeSiteName1 The name of the first trade site.
     * @param currencyName1 The name of the first currency.
     * @param currencyName2 The name of the second currency.
     * @param tradeSiteName2 The name of the second trade site.
     * @param reverse_too Flag to indicate, that the reversed path should be added, too.
     */
    private void addTradePath( String tradeSiteName1, String currencyName1, String currencyName2, String tradeSiteName2, boolean reverse_too) {
	
	// Try to get the trade site APIs.
	TradeSite tradeSite1 = ModuleLoader.getInstance().getRegisteredTradeSite( tradeSiteName1);
	TradeSite tradeSite2 = ModuleLoader.getInstance().getRegisteredTradeSite( tradeSiteName2);

	// Check, if the module loader found the trade sites.
	if( tradeSite1 == null) {

	    LogUtils.getInstance().getLogger().error( "Cannot find trade site: "+ tradeSiteName1);
	    
	    return;  // Cannot create trade path.
	}

	if( tradeSite2 == null) {
		    
	    LogUtils.getInstance().getLogger().error( "Cannot find trade site: "+ tradeSiteName2);
	    
	    return;  // Cannot create trade path.
	}

	// Now create the currencies.
	Currency currency1 = CurrencyProvider.getInstance().getCurrencyForCode( currencyName1);
	Currency currency2 = CurrencyProvider.getInstance().getCurrencyForCode( currencyName2);
	    
	// Since the currencies are created, if they don't exist, I don't check them separately.
	
	// Create a TradePath object and add it.
	addTradePath( new TradePath( tradeSite1, currency1, currency2, tradeSite2), reverse_too);
    }

    /**
     * Calculate a trade path for a given input value.
     *
     * @param tradePath The trade path to calculate.
     * @param inputAmount The input amount for the calculation.
     *
     * @return The output value of the trade sequence.
     */
    private Amount calculateTradePath( TradePath tradePath, Amount inputAmount) {

	System.out.println( "DEBUG: calculating trade path: " + tradePath.toString());
	
	// Create the 4 orders to do the actual transfer and subtract the fees at each step.

	System.out.println( "DEBUG: Create order 1");
	
	// 1. exchange funds to transfer currency
	SiteOrder exchangeOrder1 = createExchangeOrder( tradePath.getStartSite()
							, null
							, tradePath.getStartCurrency()
							, tradePath.getTransferCurrency()
							, inputAmount);

	if( exchangeOrder1 == null) {  // Cannot create this order?

	    return new Amount( "-1");
	}
	    
	// Compute the fee for this order.
	Price fee = tradePath.getStartSite().getFeeForOrder( exchangeOrder1);

	System.out.println( "DEBUG: buy order price is: " + exchangeOrder1.getPrice());
	
	Amount currentAmount = new Amount( exchangeOrder1.getOrderType() == OrderType.BUY 
					   ? inputAmount.divide( exchangeOrder1.getPrice(), MathContext.DECIMAL128) 
					   : inputAmount.multiply( exchangeOrder1.getPrice()));

	// Subtract the fee from the new amount
	currentAmount = new Amount( currentAmount.subtract( fee));

	// Step 2: withdraw funds

	// Create the withdraw order. Since we don't want to execute the order, we don't need a target account.

	System.out.println( "DEBUG: Create order 2");
	
	WithdrawOrder withDrawOrder = OrderFactory.createCryptoCoinWithdrawOrder( tradePath.getStartSite()
										  , null
										  , tradePath.getTransferCurrency()
										  , currentAmount
										  , null);

	// Calculate and subtract the fee for the withdrawal.
	fee = tradePath.getStartSite().getFeeForOrder( exchangeOrder1);

	currentAmount = new Amount( currentAmount.subtract( fee));

	// 3. deposit funds

	System.out.println( "DEBUG: Create order 3");
	
	DepositOrder depositOrder = OrderFactory.createCryptoCoinDepositOrder( tradePath.getTargetSite(), null, tradePath.getTransferCurrency(), currentAmount);

	// Calculate and subtract fee for the deposit.
	fee = tradePath.getTargetSite().getFeeForOrder( depositOrder);

	currentAmount = new Amount( currentAmount.subtract( fee));

	// 4. exchange funds to target currency.

	System.out.println( "DEBUG: Create order 4");
	
	SiteOrder exchangeOrder2 = createExchangeOrder( tradePath.getTargetSite()
							, null
							, tradePath.getTransferCurrency()
							, tradePath.getStartCurrency()
							, currentAmount);

	if( exchangeOrder2 == null) {  // If the order cannot be created.

	    return new Amount( "-1");  // Return a dummy.
	}

	System.out.println( "DEBUG: sell order price is: " + exchangeOrder1.getPrice());
		
	// Compute the fee for this order.
	fee = tradePath.getStartSite().getFeeForOrder( exchangeOrder2);

	currentAmount = new Amount( exchangeOrder2.getOrderType() == OrderType.BUY 
				    ? currentAmount.divide( exchangeOrder2.getPrice(), MathContext.DECIMAL128) 
				    : currentAmount.multiply( exchangeOrder2.getPrice()));

	// Subtract the fee from the new amount
	currentAmount = new Amount( currentAmount.subtract( fee));

	// Return the resulting amount.
	return currentAmount;
    }

    /**
     * Calculate the volume for a trade path.
     *
     * @param tradePath The trade path, that we calculate the volume for.
     *
     * @return The volume for the trade path.
     */
    private Amount calculateTradePathVolume( TradePath tradePath) {
	
	return new Amount( "-1");  // Just a dummy for now.
    }

    /**
     * Compute the triangle trades from the depth.
     */
    private void calculateTrianglesFromDepth() {

	System.out.println( "ToDo: rewrite calculateTrianglesFromDepth() with BigDecimal");

	// Set a default value for each result, since some calculations may fail...
	for( int i=0; i < _outputValues.length; ++i) {
	    _outputValues[i] = new BigDecimal( "-1");
	}

	// try {
	    // Calculate a simple intersite trade from btc-e to MtGox.
	    /*
	    BigDecimal current = _inputValues[0];
	    current = current.divide( _tradeSiteDepths[TRADESITE_BTCE].getSell(0).getPrice(), MathContext.DECIMAL128);
	    current = current.multiply( new BigDecimal( "100").subtract( _tradeSites[ TRADESITE_BTCE].getFeeForTrades()).divide( new BigDecimal( "100"), MathContext.DECIMAL128));
	    current = current.subtract( new BigDecimal( "0.01"));  // btce withdraw fee.
	    current = current.multiply(  _tradeSiteDepths[TRADESITE_MTGOX].getBuy(0).getPrice());
	    _outputValues[0] = current.multiply( new BigDecimal( "100").subtract( _tradeSites[ TRADESITE_MTGOX].getFeeForTrades()).divide( new BigDecimal( "100"), MathContext.DECIMAL128));
	} catch( TradeDataNotAvailableException tdnae) {
	    System.out.println( "Some trade data are not available: " + tdnae);
	    } */

	// try {
	    // The opposite direction.
	/*
	    double current = _inputValues[1].doubleValue();
	    current /= _tradeSiteDepths[TRADESITE_MTGOX].getSell(0).getPrice().doubleValue();
	    current *= 0.997;  // MtGox withdraw fee.
	    current *= _tradeSiteDepths[TRADESITE_BTCE].getBuy(0).getPrice().doubleValue();
	    current *= 0.998;
	    _outputValues[1] = new BigDecimal( current);
	} catch( TradeDataNotAvailableException tdnae) {
	    System.out.println( "Some trade data are not available: " + tdnae);
	    } */
 

	// try {
	    // Calculate a simple intersite trade from btc-e to Intersango.
	    /* double current = _inputValues[2].doubleValue();
	    current /= _tradeSiteDepths[TRADESITE_BTCE].getSell(0).getPrice().doubleValue();
	    current *= 0.998;
	    current -= 0.01;  // btce withdraw fee.
	    current *= 0.9965;  // Intersango deposit fee.
	    current *= _tradeSiteDepths[TRADESITE_INTERSANGO].getBuy(0).getPrice().doubleValue();
	    _outputValues[2] = new BigDecimal( current * 0.935);
	} catch( TradeDataNotAvailableException tdnae) {
	    System.out.println( "Some trade data are not available: " + tdnae);
	    } */

	//  {
	    // The opposite direction.
	    /* ble current = _inputValues[3].doubleValue();
	    current /= _tradeSiteDepths[TRADESITE_INTERSANGO].getSell(0).getPrice().doubleValue();
	    current *= 0.9935;  // Intersango trade fee.
	    current *= 0.9905;  // Intersango withdraw fee.
	    current *= _tradeSiteDepths[TRADESITE_BTCE].getBuy(0).getPrice().doubleValue();
	    current *= 0.998;
	    _outputValues[3] = new BigDecimal( current);
	} catch( TradeDataNotAvailableException tdnae) {
	    System.out.println( "Some trade data are not available: " + tdnae);
	    } */	
    }

    /**
     * Create an exchange order to convert funds to a new currency.
     *
     * @param tradeSite The trade site to use for the exchange.
     * @param userAccount The tradesite user account to use.
     * @param startCurrency The currency, that we have.
     * @param targetCurrency The currency, that we want to end up with.
     * @param inputAmount The amount, that we want to trade.
     *
     * @return The created order, or null, if there are prices available.
     */
    private SiteOrder createExchangeOrder( TradeSite tradeSite
					   , TradeSiteUserAccount userAccount
					   , Currency startCurrency
					   , Currency targetCurrency
					   , Amount inputAmount) {

	boolean isBuy = true;
	CurrencyPair usedPair = null;  // The used currency pair.

	if( startCurrency == null) {
	    System.out.println( "startCurrency is null");
	}
	if( targetCurrency == null) {
	    System.out.println( "targetCurrency is null");
	}
	if( tradeSite.getSupportedCurrencyPairs() == null) {
	    System.out.println( "Supported currency pairs is null");
	}
	
	// Create a currency pair from the 2 passed pairs.
	CurrencyPair targetPair = new CurrencyPairImpl( startCurrency, targetCurrency);

	// Find a fitting currency pair on this trading site.
	for( CurrencyPair currentPair : tradeSite.getSupportedCurrencyPairs()) {
	    
	    if( currentPair.equals( targetPair)) {

		usedPair = currentPair;
		isBuy = false;
		break;  // Currency pair found.

	    } else if( currentPair.invert().equals( targetPair)) {

		usedPair = currentPair;
		isBuy = true;
		break;  // Currency pair found.
	    }
	}

	// Check, if a currency pair was found.
	if( usedPair == null) {
	    
	    throw new CurrencyNotSupportedException( "createExchangeOrder: can't find a currency pair on trade site " 
						     + tradeSite.getName()
						     + " for " 
						     + startCurrency.getCode() 
						     + " and "
						     + targetCurrency.getCode());
	} else {

	    // Now create the actual order.
	    
	    // Get the price.
	    // System.out.println( "Intersitebot: check for the volume here and compute a price for the complete volume, that we would trade...");
	    Price orderPrice;
	    Depth currentDepth = ChartProvider.getInstance().getDepth( tradeSite, usedPair);

	    if( currentDepth == null) {
		return null;
	    }
	    
	    if( isBuy) {
		
		if( currentDepth.getSellSize() == 0) {  // If there are no sell orders.

		    LogUtils.getInstance().getLogger().error( "Cannot find prices for order at: "
							      + tradeSite.getName());
		    
		    return null;  // We cannot create this order.
		}
		
		orderPrice =  currentDepth.getSell(0).getPrice();

		if( orderPrice.compareTo( BigDecimal.ZERO) == 0) {

		     LogUtils.getInstance().getLogger().error( "Order price is 0 at: "
							      + tradeSite.getName());
		}
		
	    } else {

		if( currentDepth.getBuySize() == 0) {  // If there are no buy orders.

		    LogUtils.getInstance().getLogger().error( "Cannot find prices for order at: "
							      + tradeSite.getName());
		    
		    return null;  // We cannot create this order.
		}
		
		orderPrice = ChartProvider.getInstance().getDepth( tradeSite, usedPair).getBuy(0).getPrice();

		if( orderPrice.compareTo( BigDecimal.ZERO) == 0) {

		     LogUtils.getInstance().getLogger().error( "Order price is 0 at: "
							      + tradeSite.getName());
		}
	    }

	    // Since we have the price now, we can compute, the amount we can buy with our current input.
	    if( isBuy) {
		inputAmount = new Amount( inputAmount.divide( orderPrice, MathContext.DECIMAL128));
	    }
	    
	    return OrderFactory.createCryptoCoinTradeOrder( tradeSite
							    , userAccount
							    , isBuy ? OrderType.BUY : OrderType.SELL
							    , orderPrice
							    , usedPair
							    , inputAmount);
	}
    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static InterSiteBot getInstance() {
	if( _instance == null) {
	    _instance = new InterSiteBot();
	}
	return _instance;
    }

    /**
     * Get the properties of this bot.
     *
     * @return The properties of this bot.
     */
    public TradeBotProperties getProperties() {
	
	if( _properties == null) {                                      // If there are no propertes yet,
	    _properties = new TradeBotProperties( this.getInstance());  // create them.
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
     * Get the list of registered trade paths.
     *
     * @return The list of registered trade paths.
     */
    public ArrayList<TradePath> getTradePaths() {
	return _tradePaths;
    }

    /**
     * Get the UI for this bot.
     *
     * @return The UI for this bot.
     */
    public TradeBotUI getUI() {

	if( _botUI == null) {                    // If there is no UI yet,
	    _botUI = new InterSiteBotUI( this);  // create one. This is optional, since the bot
	}                                        // might run in daemon mode.
	return _botUI;
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

	// Create a ticker thread.
	_updateThread = new Thread() {

		@Override public void run() {
		    while( _updateThread == this) {

			// Loop over the trade paths.
			int currentIndex = 0;
			for( TradePath currentPath : _tradePaths) {
			    
			    _inputValues[ currentIndex] = new Amount( "10");  // Use 10 dollar/euro as the input.

			    // Compute the current tradePath and store the result in the display array.
			    _outputValues[ currentIndex * 2] = calculateTradePath( currentPath, new Amount( _inputValues[ currentIndex]));

			    // Compute the volume for the current trade path and store it also in the output fields.
			    _outputValues[ currentIndex * 2 + 1] = calculateTradePathVolume( currentPath);
			    
			    ++currentIndex; // Result from next trade path.
			}


			// Notify the user here, if there is a good deal?
			
			if( ! TradeApp.getApp().isDaemonMode()) {                // If there's an UI,
			    getUI().updateValues( _inputValues, _outputValues);  // show the results.
			}

			try {
			    sleep( _updateInterval * 1000 + _updateIntervalRandomizer.nextInt( 2 * 1000));
			} catch( InterruptedException ie) {
			    System.err.println( "Ticker or depth loop sleep interrupted: " + ie.toString());
			}
		    }
		}
	    };
	_updateThread.start();

    }
}
