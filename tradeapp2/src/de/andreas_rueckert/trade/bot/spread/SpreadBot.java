/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.bot.LogLevel;
import de.andreas_rueckert.trade.bot.NativeBotCore;
import de.andreas_rueckert.trade.bot.TradeBot;
import de.andreas_rueckert.trade.bot.TradeBotProperties;
import de.andreas_rueckert.trade.bot.TradeLogger;
import de.andreas_rueckert.trade.bot.ui.SpreadBotUI;
import de.andreas_rueckert.trade.bot.ui.TradeBotUI;
import de.andreas_rueckert.trade.chart.ChartAnalyzer;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.currency.CurrencyProvider;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.Order;
import de.andreas_rueckert.trade.order.OrderFactory;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import de.andreas_rueckert.util.TimeUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.Map;


/**
 * This bot trades on spreads by using rules.
 */
public class SpreadBot extends NativeBotCore implements TradeBot {

    // Inner classes


    // Static variables

    /**
     * The only instance of the bot (singleton pattern).
     */
    static SpreadBot _instance = null;

    /**
     * The max numer of entries in the spread history.
     */
    static final long MAX_SPREAD_HISTORY_LENGTH = 60L * 12L; // 12 hours for now.


    // Instance variables

    /**
     * The time interval to analyze for trading.
     */
    private final long _analyzedTimeInterval = 60L * 1000000L;  // 1 minute for now...

    /**
     * The id's of the currently active orders.
     */
    private List<SiteOrder> _currentOrders = new ArrayList<SiteOrder>();
    
    /**
     * The current funds for each user account and currency.
     * Since the accounts from different exchanges should be different, this should work across exchanges.
     * So I don't have to use the exchange as an additional key here..
     */
    private Map<TradeSiteUserAccount, Collection<TradeSiteAccount>> _currentFunds = new HashMap<TradeSiteUserAccount, Collection<TradeSiteAccount>>();

    /**
     * The current log level.
     */
    private LogLevel _logLevel = LogLevel.INFO;

    /**
     * The max. traded amount is 1 coin.
     */
    private Amount _maxTradeAmount = new Amount( "1");

    /**
     * The minimum profit in percent.
     */
    private Price _minimumProfit = new Price( "0.1");

    /**
     * Minimum is 0.1 coins.
     */
    private Amount _minTradeAmount = new Amount( "0.05");

    /**
     * The properties of this trade bot.
     */
    private TradeBotProperties _properties;

    /**
     * The history of the spread.
     */
    private LinkedList<Price> _spreadHistory = new LinkedList<Price>();

    /**
     * A list of strategies.
     */
    private List<Strategy> _strategies = null;

    /**
     * The UI for this bot.
     */
    private TradeBotUI _spreadBotUI = null;

    /**
     * The traded currency pair.
     */
    private CurrencyPair _tradedPair = null;

    /**
     * The trade site to use for trading.
     */
    private TradeSite _tradeSite = null;

    /**
     * A list of user accounts.
     */
    private List<TradeSiteUserAccount> _tradeSiteUserAccounts = new ArrayList<TradeSiteUserAccount>();


    // Constructors

    /**
     * Create a new SpreadBot instance. Constructor is private
     * for singleton pattern.
     */
    private SpreadBot() {
	
	// Set the name of the bot.
	_name = "Spread";

	// Set the version string for this bot.
	_versionString = "0.1.2 ( Botchek )";

	// The update interval for the ticker loop.
	_updateInterval = 10 * 60;

	// Get the trade site for trading. Use btc-e for now...
	_tradeSite = ModuleLoader.getInstance().getRegisteredTradeSite( "BTCe");

	// Create a logger for this bot.
	_tradeLogger = new TradeLogger( "Spread_Bot.log");
	_tradeLogger.setMailRecipient( "mail@andreas-rueckert.de");  // <= ugly hack!!! Fix!!!
	//_tradeLogger.activateEmailNotification( true);
    }


    // Methods

    /**
     * Add an order to the list of currently executed orders.
     *
     * param order The order to add.
     */
    void addOrder( SiteOrder order) {

	_currentOrders.add( order);  // Add the id to the list of order id's.
    }

    /**
     * Add a collection of strategies to this bot.
     *
     * @param strategies The collection of strategies to add.
     * @param forceReplace This switch forces to replace an already loaded strategy with the same name.
     */
    private synchronized void addStrategies( Collection<Strategy> strategies, boolean forceReplace) {

	for( Strategy strategy : strategies) {

	    addStrategy( strategy, forceReplace);
	}
    }

    /**
     * Add a new strategy to this  bot.
     *
     * @param strategy The new strategy to add.
     * @param forceReplace This switch forces to replace an already loaded strategy with the same name. 
     */
    private synchronized void addStrategy( Strategy strategy, boolean forceReplace) {

	// Check, if a strategy with this name is already loaded.
	if( getStrategyForName( strategy.getName()) != null) {

	    if( forceReplace) {   // If strategies with the same should be replaced,
		                  // remove all strategies with this name.

		removeStrategyWithName( strategy.getName());

	    } else {  // Throw an exception, indicating that this exception cannot get loaded at the moment.

		throw new IdenticalStrategyAlreadyLoadedException( "A strategy with the name '" 
								   + strategy.getName() 
								   + "' is already loaded. Cannot load identical strategy twice.");
	    }
	}
	 
	_strategies.add( strategy);
    }

    /**
     * Load a new strategy from a file.
     *
     * @param StrategyFilename The name of the file holding the new strategy.
     *
     * @throws FileNotFoundException If the file was not found.
     * @throws IOException If an I/O error occured during file reading.
     */
    private void addStrategyFromFile( String strategyFilename) throws FileNotFoundException, IOException {

	throw new NotYetImplementedException( "Loading a strategy from a java file is not yet implemented");
    }

    /**
     * Add a new user account to the list of trade site accounts.
     *
     * @param tradeSiteUserAccount The new user account to add.
     */
    public void addTradeSiteUserAccount( TradeSiteUserAccount userAccount) {

	_tradeSiteUserAccounts.add( userAccount);
    }

    /**
     * Buy some amount of a currency at a given trade site.
     *
     * @param tradeSite The trade site to buy at.
     * @param currency The currency to buy.
     * @param amount The amount to buy.
     *
     * @return true, if the order went through.
     */
    boolean buy( TradeSite tradeSite, Currency currency, Amount amount) {

	return false;  // Default return value.
    }
    
    /**
     * Get the list of current orders.
     *
     * @return The list of current orders.
     */
    List<SiteOrder> getCurrentOrders() {
	
	return _currentOrders;
    }

    /**
     * Get the funds for a given currency.
     * The trade site is also stored in the user account, but not all accounts hold all the data here, so I
     * pass the trade site also as a parameter here at the moment. In the future, the trade site will be 
     * mandatory in the future, so the tradesite parameter could be removed then...
     *
     * @param tradeSite The exchange to query.
     * @param userAccount The tradesite user account to use.
     * @param currency The currency to use.
     *
     * @return The balance for this currency (or -1, if no account with this currency was found).
     */
    public Amount getFunds( TradeSite tradeSite, TradeSiteUserAccount userAccount, Currency currency) {

	if( _currentFunds.get( userAccount) == null) {                                 // If there are no accounts yet,
	    _currentFunds.put( userAccount, tradeSite.getAccounts( userAccount));      // fetch them from the trade site.
	} 

	if( _currentFunds.get( userAccount) == null) {
	    LogUtils.getInstance().getLogger().error( "Spreadbot cannot fetch accounts from trade site.");
	} else {
	    for( TradeSiteAccount account : _currentFunds.get( userAccount)) {  // Loop over the accounts.
		if( currency.equals( account.getCurrency())) {                  // If this accounts has the requested currency.
		    return new Amount( account.getBalance());                   // Return it's balance.
		}
	    }
	}

	return null;  // Cannot get any balance. 
    }

    /**
     * Get the only instance of this bot (singleton pattern).
     *
     * @return The only instance of this bot.
     */
    public static SpreadBot getInstance() {
	if( _instance == null) {          // If there is no instance yet,
	    _instance = new SpreadBot();  // create one.
	}
	return _instance;
    }

    /**
     * Get the current log level of this bot.
     *
     * @return The current log level.
     */
    public LogLevel getLogLevel() {

	return _logLevel;
    }

    /**
     * Get the margin in percent to balance the funds.
     *
     * @param orderType The type of the order (buy or sell).
     *
     * @return The margin in percent to balance the funds.
     */
    public float getMargin( OrderType orderType) {

	return orderType == OrderType.BUY ? 0.2f : 0.4f; 
    }

    /**
     * Get the minimum amount to trade.
     *
     * @param currency The currency to trade.
     */
    Amount getMinimumTradeAmount( Currency currency) {

	if( currency.hasCode( "BTC")) {

	    return new Amount( "0.01");  // Trade at least this many BTC.

	} else if( currency.hasCode( "LTC")) {

	    return new Amount( "0.01");  // Trade at least 0.1 LTC.

	} else if( currency.hasCode( "XPM")) {

	    return new Amount( "0.1");  // Trade at least this many XPM.

	} else if( currency.hasCode( "NMC")) {

	    return new Amount( "0.5");  // Trade at least this many NMC.

	} else if( currency.hasCode( "PPC")) {

	    return new Amount( "0.01");  // Trade at least this many PPC.
 
	} else {

	    throw new CurrencyNotSupportedException( "No minimum trading amount for currency: " + currency);
	}
    }

    /**
     * Get the number of trade site user accounts.
     *
     * @return The number of trade site user accounts.
     */
    public int getNumTradeSiteUserAccounts() {

	return _tradeSiteUserAccounts.size();
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

	// Get the settings from this bot.
	PersistentPropertyList result = new PersistentPropertyList();

	// Store the simulation mode.
	result.add( new PersistentProperty( "SimulationMode", null, "" + ( isSimulation() ? 1 : 0), 0));

	// Add every user account as a property
	for( int i = 0; i < _tradeSiteUserAccounts.size(); ++i) {

	    result.add( new PersistentProperty( "useraccount" + i, null, _tradeSiteUserAccounts.get( i).encodeAsPropertyValue(), i + 1));    
	}

	return result;
    }


    /**
     * Get the list of strategies.
     *
     * @return The list of strategies.
     */
    public List<Strategy> getStrategies() {

	if( _strategies == null) {  // If there are no strategies defined yet.
	    
	    _strategies = new ArrayList<Strategy>();

	    // Get a reference to the btc-e API implementation.
	    TradeSite btceSite = ModuleLoader.getInstance().getRegisteredTradeSite( "BTCe");

	    /*
	    
	    // Create a strategy to balance ltc<=>usd on btc-e

	    // Get an account for this strategy.
	    TradeSiteUserAccount btceLtcUsdAccount = getTradeSiteUserAccountForName( "balance_btce_ltc_usd");

	    if( ( btceLtcUsdAccount != null) && btceLtcUsdAccount.isActivated()) {

		StrategyImpl ltcUsdStrategy = new StrategyImpl( "Balance LTC<=>USD at btc-e", "10m", 10);

		// Create a currency pair to trade
		CurrencyPair ltcUsdPair = new CurrencyPairImpl( "LTC", "USD");

		// Add a list of rules.
		ltcUsdStrategy.addRule( new EmergencyStopRule( this
							       , ltcUsdStrategy
							       , btceSite
							       , ltcUsdPair
							       , btceLtcUsdAccount
							       , new Price( "1.5", CurrencyProvider.getInstance().getCurrencyForCode( "USD"))));
		ltcUsdStrategy.addRule( new CancelOrderRule( this, ltcUsdStrategy, btceLtcUsdAccount));
		ltcUsdStrategy.addRule( new BalanceBuyRule( this, ltcUsdStrategy, btceSite, ltcUsdPair, btceLtcUsdAccount, 1.5f));
		ltcUsdStrategy.addRule( new BalanceSellRule( this, ltcUsdStrategy, btceSite, ltcUsdPair, btceLtcUsdAccount, 3.0f));

		// Add some info about this strategy.
		ltcUsdStrategy.addUsedCurrencyPair( ltcUsdPair);
		ltcUsdStrategy.addUsedTradeSite( btceSite);

		// Add this strategy to the list of processed strategies.
	        addStrategy( ltcUsdStrategy, true); 

		// Do some side trading with XPM on the ltc/usd account.
		StrategyImpl btceXpmBtcStrategy = new StrategyImpl( "Balance XPM<=>BTC at btc-e", "10m", 10);

		// Create a currency pair to trade
		CurrencyPair xpmBtcPair = new CurrencyPairImpl( "XPM", "BTC");

		// Add a list of rules.
		btceXpmBtcStrategy.addRule( new EmergencyStopRule( this
								   , btceXpmBtcStrategy
								   , btceSite
								   , xpmBtcPair
								   , btceLtcUsdAccount
								   , new Price( "0.0015", CurrencyProvider.getInstance().getCurrencyForCode( "BTC"))));
		btceXpmBtcStrategy.addRule( new CancelOrderRule( this, btceXpmBtcStrategy, btceLtcUsdAccount));
		btceXpmBtcStrategy.addRule( new BalanceBuyRule( this, btceXpmBtcStrategy, btceSite, xpmBtcPair, btceLtcUsdAccount, 2.0f));
		btceXpmBtcStrategy.addRule( new BalanceSellRule( this, btceXpmBtcStrategy, btceSite, xpmBtcPair, btceLtcUsdAccount, 4.0f));

		// Add some info about this strategy.
		btceXpmBtcStrategy.addUsedCurrencyPair( xpmBtcPair);
		btceXpmBtcStrategy.addUsedTradeSite( btceSite);

		// Add this strategy to the list of processed strategies.
	        addStrategy( btceXpmBtcStrategy, true);
		
	    } else {

		LogUtils.getInstance().getLogger().error( "Cannot find user account for Btc-e ltc<=>usd strategy");
	    }

	    */

	    /*
	    // Add a strategy to trade btc<=>usd on btc-e
	    
	    // Get an account for this strategy.
	    TradeSiteUserAccount btceBtcUsdAccount = getTradeSiteUserAccountForName( "balance_btce_btc_usd");

	    if( ( btceBtcUsdAccount != null) && btceBtcUsdAccount.isActivated()) {

		StrategyImpl btcUsdStrategy = new StrategyImpl( "Balance BTC<=>USD at btc-e", "10m");

		// Create a currency pair to trade
		CurrencyPair btcUsdPair = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.USD);

		// Add a list of rules to this strategy
		btcUsdStrategy.addRule( new EmergencyStopRule( this, btcUsdStrategy, btceSite, btcUsdPair, btceBtcUsdAccount, new Price( "60.0", CurrencyImpl.USD)));
		btcUsdStrategy.addRule( new CancelOrderRule( this, btcUsdStrategy, btceBtcUsdAccount));
		btcUsdStrategy.addRule( new BalanceBuyRule( this, btcUsdStrategy, btceSite, btcUsdPair, btceBtcUsdAccount, 1.0f));
		btcUsdStrategy.addRule( new BalanceSellRule( this, btcUsdStrategy, btceSite, btcUsdPair, btceBtcUsdAccount, 2.0f));

		// Add some info about this strategy.
		btcUsdStrategy.addUsedCurrencyPair( btcUsdPair);
		btcUsdStrategy.addUsedTradeSite( btceSite);

		// Add this strategy to the list of processed strategies.
		addStrategy( btcUsdStrategy, true);

	    } else {

		LogUtils.getInstance().getLogger().error( "Cannot find user account for Btc-e btc<=>usd strategy");
	    }
	    */

	    // Add a strategy to trade ppc<=>usd on btc-e
	    TradeSiteUserAccount btcePpcUsdAccount = getTradeSiteUserAccountForName( "new_btce_trading1");

	    if( ( btcePpcUsdAccount != null) && btcePpcUsdAccount.isActivated()) {

		StrategyImpl ppcUsdStrategy = new StrategyImpl( "Balance PPC<=>USD at btc-e", "10m", 10);

		// Create a currency pair to trade
		CurrencyPair ppcUsdPair = new CurrencyPairImpl( "PPC", "USD");

		// Add a list of rules to this strategy
		ppcUsdStrategy.addRule( new EmergencyStopRule( this
							       , ppcUsdStrategy
							       , btceSite
							       , ppcUsdPair
							       , btcePpcUsdAccount
							       , new Price( "0.2", CurrencyProvider.getInstance().getCurrencyForCode( "USD"))));
		ppcUsdStrategy.addRule( new CancelOrderRule( this, ppcUsdStrategy, btcePpcUsdAccount));
		ppcUsdStrategy.addRule( new BalanceBuyRule( this, ppcUsdStrategy, btceSite, ppcUsdPair, btcePpcUsdAccount, 0.05f));
		ppcUsdStrategy.addRule( new BalanceSellRule( this, ppcUsdStrategy, btceSite, ppcUsdPair, btcePpcUsdAccount, 0.1f));

		// Add some info about this strategy.
		ppcUsdStrategy.addUsedCurrencyPair( ppcUsdPair);
		ppcUsdStrategy.addUsedTradeSite( btceSite);

		// Add this strategy to the list of processed strategies.
		addStrategy( ppcUsdStrategy, true);

	    } else {

		LogUtils.getInstance().getLogger().error( "Cannot find user account for Btc-e ppc<=>usd strategy");
	    }

	    // Add a strategy to trade LTC <=> BTC on btc-e
	    TradeSiteUserAccount btceLtcBtcAccount = getTradeSiteUserAccountForName( "new_btce_trading1");

	    if( ( btceLtcBtcAccount != null) && btceLtcBtcAccount.isActivated()) {

		StrategyImpl ltcBtcStrategy = new StrategyImpl( "Balance LTC<=>BTC at btc-e", "10m", 10);

		// Create a currency pair to trade
		CurrencyPair ltcBtcPair = new CurrencyPairImpl( "LTC", "BTC");

		// Add a list of rules to this strategy
		ltcBtcStrategy.addRule( new EmergencyStopRule( this
							       , ltcBtcStrategy
							       , btceSite
							       , ltcBtcPair
							       , btcePpcUsdAccount
							       , new Price( "0.004", CurrencyProvider.getInstance().getCurrencyForCode( "LTC"))));
		ltcBtcStrategy.addRule( new CancelOrderRule( this, ltcBtcStrategy, btcePpcUsdAccount));
		ltcBtcStrategy.addRule( new BalanceBuyRule( this, ltcBtcStrategy, btceSite, ltcBtcPair, btcePpcUsdAccount, 0.5f));
		ltcBtcStrategy.addRule( new BalanceSellRule( this, ltcBtcStrategy, btceSite, ltcBtcPair, btcePpcUsdAccount, 1.0f));

		// Add some info about this strategy.
		ltcBtcStrategy.addUsedCurrencyPair( ltcBtcPair);
		ltcBtcStrategy.addUsedTradeSite( btceSite);

		// Add this strategy to the list of processed strategies.
		addStrategy( ltcBtcStrategy, true);

	    } else {

		LogUtils.getInstance().getLogger().error( "Cannot find user account for Btc-e ltc<=>btc strategy");
	    }
	    
	    // Add another strategy to trade LTC <=> BTC on btc-e
	    TradeSiteUserAccount btceLtcBtcAccount2 = getTradeSiteUserAccountForName( "new_btce_trading2");

	    if( ( btceLtcBtcAccount2 != null) && btceLtcBtcAccount2.isActivated()) {

		StrategyImpl ltcBtcStrategy = new StrategyImpl( "Balance LTC<=>BTC at btc-e", "10m", 10);

		// Create a currency pair to trade
		CurrencyPair ltcBtcPair = new CurrencyPairImpl( "LTC", "BTC");

		// Add a list of rules to this strategy
		ltcBtcStrategy.addRule( new EmergencyStopRule( this
							       , ltcBtcStrategy
							       , btceSite
							       , ltcBtcPair
							       , btceLtcBtcAccount2
							       , new Price( "0.004", CurrencyProvider.getInstance().getCurrencyForCode( "LTC"))));
		ltcBtcStrategy.addRule( new CancelOrderRule( this, ltcBtcStrategy, btceLtcBtcAccount2));
		ltcBtcStrategy.addRule( new BalanceBuyRule( this, ltcBtcStrategy, btceSite, ltcBtcPair, btceLtcBtcAccount2, 0.5f));
		ltcBtcStrategy.addRule( new BalanceSellRule( this, ltcBtcStrategy, btceSite, ltcBtcPair, btceLtcBtcAccount2, 1.0f));

		// Add some info about this strategy.
		ltcBtcStrategy.addUsedCurrencyPair( ltcBtcPair);
		ltcBtcStrategy.addUsedTradeSite( btceSite);

		// Add this strategy to the list of processed strategies.
		addStrategy( ltcBtcStrategy, true);

	    } else {

		LogUtils.getInstance().getLogger().error( "Cannot find user account for Btc-e ltc<=>btc strategy");
	    }

	    if( ( btceLtcBtcAccount2 != null) && btceLtcBtcAccount2.isActivated()) {

		StrategyImpl ppcUsdStrategy = new StrategyImpl( "Balance PPC<=>USD at btc-e", "10m", 10);

		// Create a currency pair to trade
		CurrencyPair ppcUsdPair = new CurrencyPairImpl( "PPC", "USD");

		// Add a list of rules to this strategy
		ppcUsdStrategy.addRule( new EmergencyStopRule( this
							       , ppcUsdStrategy
							       , btceSite
							       , ppcUsdPair
							       , btceLtcBtcAccount2
							       , new Price( "0.2", CurrencyProvider.getInstance().getCurrencyForCode( "USD"))));
		ppcUsdStrategy.addRule( new CancelOrderRule( this, ppcUsdStrategy, btceLtcBtcAccount2));
		ppcUsdStrategy.addRule( new BalanceBuyRule( this, ppcUsdStrategy, btceSite, ppcUsdPair, btceLtcBtcAccount2, 0.5f));
		ppcUsdStrategy.addRule( new BalanceSellRule( this, ppcUsdStrategy, btceSite, ppcUsdPair, btceLtcBtcAccount2, 1.0f));

		// Add some info about this strategy.
		ppcUsdStrategy.addUsedCurrencyPair( ppcUsdPair);
		ppcUsdStrategy.addUsedTradeSite( btceSite);

		// Add this strategy to the list of processed strategies.
		addStrategy( ppcUsdStrategy, true);

	    } else {

		LogUtils.getInstance().getLogger().error( "Cannot find user account for Btc-e ppc<=>usd strategy");
	    }
	    
	    /*

	    // Add a strategy to trade nmc<=>btc on btc-e
	    if( ( btceBtcUsdAccount != null) && btceBtcUsdAccount.isActivated()) {

		StrategyImpl nmcBtcStrategy = new StrategyImpl( "Balance NMC<=>BTC at btc-e", "10m", 10);

		// Create a currency pair to trade
		CurrencyPair nmcBtcPair = new CurrencyPairImpl( "NMC", "BTC");

		// Add a list of rules to this strategy
		nmcBtcStrategy.addRule( new EmergencyStopRule( this
							       , nmcBtcStrategy
							       , btceSite
							       , nmcBtcPair
							       , btceBtcUsdAccount
							       , new Price( "0.002", CurrencyProvider.getInstance().getCurrencyForCode( "BTC"))));
		nmcBtcStrategy.addRule( new CancelOrderRule( this, nmcBtcStrategy, btceBtcUsdAccount));
		nmcBtcStrategy.addRule( new BalanceBuyRule( this, nmcBtcStrategy, btceSite, nmcBtcPair, btceBtcUsdAccount, 0.5f));
		nmcBtcStrategy.addRule( new BalanceSellRule( this, nmcBtcStrategy, btceSite, nmcBtcPair, btceBtcUsdAccount, 1.0f));

		// Add some info about this strategy.
		nmcBtcStrategy.addUsedCurrencyPair( nmcBtcPair);
		nmcBtcStrategy.addUsedTradeSite( btceSite);

		// Add this strategy to the list of processed strategies.
		addStrategy( nmcBtcStrategy, true);

	    } else {

		LogUtils.getInstance().getLogger().error( "Cannot find user account for Btc-e nmc<=>btc strategy");
	    }

	    */

	    /*
	    // Create a strategy for EMA crossing trading.
	    // Get an account for this strategy.
	    TradeSiteUserAccount emaXbtceBtcUsdAccount = getTradeSiteUserAccountForName( "emax_btce_btc_usd");

	    if( ( emaXbtceBtcUsdAccount != null) && emaXbtceBtcUsdAccount.isActivated()) {

		StrategyImpl btcUsdStrategy = new StrategyImpl( "EMA crossing BTC<=>USD at btc-e", "30m");
		btcUsdStrategy.setSimulation( true);

		// Create a currency pair to trade
		CurrencyPair btcUsdPair = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.USD);

		// Add a list of rules to this strategy
		btcUsdStrategy.addRule( new EmergencyStopRule( this, btcUsdStrategy, btceSite, btcUsdPair, emaXbtceBtcUsdAccount, new Price( "400.0", CurrencyImpl.USD)));
		btcUsdStrategy.addRule( new CancelOrderRule( this, btcUsdStrategy, emaXbtceBtcUsdAccount));
		btcUsdStrategy.addRule( new MACrossingTradeRule( this, btcUsdStrategy, btceSite, btcUsdPair, emaXbtceBtcUsdAccount, "10d", "25d"));

		// Add some info about this strategy.
		btcUsdStrategy.addUsedCurrencyPair( btcUsdPair);
		btcUsdStrategy.addUsedTradeSite( btceSite);

		// Add this strategy to the list of processed strategies.
		addStrategy( btcUsdStrategy, true);

	    } else {

		LogUtils.getInstance().getLogger().error( "Cannot find user account for ema crossing Btc-e btc<=>usd strategy");
	    }	    
	    */
	    
	    /*
	    // Get a reference to the MtGox API implementation.
	    TradeSite mtgoxSite = ModuleLoader.getInstance().getRegisteredTradeSite( "MtGox");

	    // Add a strategy to trade btc<=>usd on MtGox.
	    
	    // Get an account for this strategy.
	    TradeSiteUserAccount mtgoxBtcUsdAccount = getTradeSiteUserAccountForName( "balance_mtgox_btc_usd");

	    if( ( mtgoxBtcUsdAccount != null) && mtgoxBtcUsdAccount.isActivated()) {

		StrategyImpl mtgoxBtcUsdStrategy = new StrategyImpl( "Balance BTC<=>USD at MtGox", "10m");

		// Create a currency pair to trade
		CurrencyPair btcUsdPair = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.USD);

		// Add a list of rules to this strategy
		mtgoxBtcUsdStrategy.addRule( new EmergencyStopRule( this, mtgoxBtcUsdStrategy, mtgoxSite, btcUsdPair, mtgoxBtcUsdAccount, new Price( "300.0", CurrencyImpl.USD)));
		mtgoxBtcUsdStrategy.addRule( new CancelOrderRule( this, mtgoxBtcUsdStrategy, mtgoxBtcUsdAccount));
		mtgoxBtcUsdStrategy.addRule( new BalanceBuyRule( this, mtgoxBtcUsdStrategy, mtgoxSite, btcUsdPair, mtgoxBtcUsdAccount, 1.5f));
		mtgoxBtcUsdStrategy.addRule( new BalanceSellRule( this, mtgoxBtcUsdStrategy, mtgoxSite, btcUsdPair, mtgoxBtcUsdAccount, 3.0f));

		// Add some info about this strategy.
		mtgoxBtcUsdStrategy.addUsedCurrencyPair( btcUsdPair);
		mtgoxBtcUsdStrategy.addUsedTradeSite( mtgoxSite);

		// Add this strategy to the list of processed strategies.
		addStrategy( mtgoxBtcUsdStrategy, true);

	    } else {

		LogUtils.getInstance().getLogger().error( "Cannot find user account for MtGox btc<=>usd strategy");
	    }
	    */
	}
 
	return _strategies;
    }

    /**
     * Find a loaded strategy with a given name.
     *
     * @param strategyName The name of the strategy.
     *
     * @return The strategy or null, if no such strategy is found.
     */
    public Strategy getStrategyForName( String strategyName) {

	for( Strategy currentStrategy : _strategies) {

	    if( ( currentStrategy.getName() != null) && currentStrategy.getName().equals( strategyName)) {

		return currentStrategy;  // This is the strategy, we were looking for.
	    }
	}

	return null;  // No matching strategy found.
    }
 
    /**
     * Get the traded currency pair.
     *
     * @return The traded currency pair.
     */
    public CurrencyPair getTradedCurrencyPair() {

	if( _tradedPair == null) {  // If there is no traded currency pair set for now.

	    // Trade ltc for usd for now.
	    _tradedPair = new CurrencyPairImpl( "LTC", "USD");
	}

	return _tradedPair;  // Return the traded currency pair.
    }

    /**
     * Get the trade site, that we trade on.
     *
     * @return The used trade site.
     */
    public TradeSite getTradeSite() {

	return _tradeSite;  // Return the trade site instance.
    }

    /**
     * Get a trade site user account from the bot.
     *
     * @param index The index of the user account.
     *
     * @return The user account or null.
     */
    public TradeSiteUserAccount getTradeSiteUserAccount( int index) {

	return _tradeSiteUserAccounts.get( index);
    }

    /**
     * Get a trade site user account with a given name.
     * 
     * @param accountName The name of the account.
     *
     * @return The first found user account with this name or null.
     */
    public TradeSiteUserAccount getTradeSiteUserAccountForName( String accountName) {

	for( TradeSiteUserAccount currentAccount : _tradeSiteUserAccounts) {

	    String name = currentAccount.getAccountName();  // Get the name of this account.

	    if( ( name != null) && name.equals( accountName)) {  // if it exists and equals the requested name...

		return currentAccount;  // return this account.
	    }
	}

	return null;  // No account with this name found.
    }

    /**
     * Get the list of orders, that the current charts trigger.
     *
     * @return The list of orders, that the current charts trigger.
     */
    /*private List<SiteOrder> getTriggeredOrders() {

	// Create a new list for the result.
	List<SiteOrder> result = new ArrayList<SiteOrder>();

	// Get some data to analyze
	List<Trade> trades = ChartProvider.getInstance().getTrades( _tradeSite, _tradedPair, _analyzedTimeInterval);
	Price sma = ChartAnalyzer.getInstance().getSMA( _tradeSite, _tradedPair, _analyzedTimeInterval);
	
	// Conditions to trigger a trade
	// 1. at least 4 SMA crossings
	// 2. spread of the crossing trades > 2 * fee + profit
	// 3. No constant drop/rise in the last 10 mins 
	// 4. Sufficient volume in the fitting trades.

	// Compute the minimum spread (2 * fee + 5% for now).
	BigDecimal minimumSpread = sma.multiply( _tradeSite.getFeeForTrade().multiply( new BigDecimal( "2")).add( new BigDecimal( "5")).divide( new BigDecimal( "100")));

	// Check the SMA crossings
	int smaCrossings = 0;
	for( int i = 1; i < trades.size(); ++i) {

	    if( ( ( trades.get(i-1).getPrice().compareTo( sma) > 0) && ( trades.get(i).getPrice().compareTo( sma) < 0))
		|| ( ( trades.get(i-1).getPrice().compareTo( sma) < 0) && ( trades.get(i).getPrice().compareTo( sma) > 0))) {

		// Check if the trades differ more than 2 * fee + profit.
		BigDecimal difference = trades.get(i-1).getPrice().subtract( trades.get(i).getPrice()).abs();

		if( difference.compareTo( minimumSpread) > 0) {

		    	++smaCrossings;

		}
	    }
	}
	
	if( smaCrossings > 3) {
	    
	    // Now check for the spread
	    
	}
	
	return result;  // Return the list of triggered orders.
	} */

    /**
     * Get the UI for this bot.
     *
     * @return The UI for this bot.
     */
    public TradeBotUI getUI() {
	if( _spreadBotUI == null) {                    // If there is no UI yet,
	    _spreadBotUI = new SpreadBotUI( this);     // create one. This is optional, since the bot
	}                                              // might run in daemon mode.
	return _spreadBotUI;
    }

    /**
     * Get new funds at the next request for a given user account.
     *
     * @param userAccount The tradesite account of the user.
     */
    private void refetchFunds( TradeSiteUserAccount userAccount) {
	_currentFunds.put( userAccount, null);  // Just delete the currently stored funds for this user account.
    }

    /**
     * Remove an order from the list of currently executed orders.
     *
     * param orderId The id of the order to remove.
     */
    void removeOrder( String orderId) {

	_currentOrders.remove( orderId);  // Remove the id from the list of order id's.
    }

    /**
     * Remove all strategies with a given name.
     *
     * @param strategyName The name of the strategy to remove.
     */
    public synchronized void removeStrategyWithName( String strategyName) {

	for( int index = 0; index < _strategies.size(); ) {
	    
	    Strategy currentStrategy = _strategies.get( index);
	    
	    if( ( currentStrategy.getName() != null) && currentStrategy.getName().equals( strategyName)) {
		
		_strategies.remove( index);  // Remove this strategy.
		
	    } else {  // Check the next index.
		
		++index;
	    }
	}
    }

    /**
     * Sell some amount of a currency at a given trade site.
     *
     * @param tradeSite The trade site to sell at.
     * @param currency The currency to sell.
     * @param amount The amount to sell.
     *
     * @return true, if the order went through.
     */
    boolean sell( TradeSite tradeSite, Currency currency, Amount amount) {

	return false;  // Default return value.
    }

    /**
     * Set new settings for this bot.
     *
     * @param settings The new settings for this bot.
     */
    public void setSettings( PersistentPropertyList settings) {

	// Just loop over the settings.
	for( PersistentProperty currentProperty : settings) {

	    String propertyName = currentProperty.getName();

	    // This property is for the simulation mode.
	    if( propertyName.equalsIgnoreCase( "SimulationMode")) {

		String propertyValue = (String)currentProperty.getValue();

		if( propertyValue.equalsIgnoreCase( "1") || propertyValue.equalsIgnoreCase( "true")) {
		    setSimulation( true);
		} else if( propertyValue.equalsIgnoreCase( "0") || propertyValue.equalsIgnoreCase( "false")) {
		    setSimulation( false);
		} else {
		    LogUtils.getInstance().getLogger().error( "Unknown value for simulation mode in setTradeBotProperty: " + propertyValue);
		}
	    }

	    // Parse a user account.
	    if( propertyName.startsWith( "useraccount")) {

		String propertyValue = (String)currentProperty.getValue();

		// Get the index of the user account.
		int accountIndex = Integer.parseInt( propertyName.substring( "useraccount".length()).trim());

		if( accountIndex > ( _tradeSiteUserAccounts.size() - 1)) {

		    // Add the user account at the previous position, since the bot might rely on this.
		    _tradeSiteUserAccounts.add( TradeSiteUserAccount.fromPropertyValue( propertyValue));

		} else {
		    
		    // Add the user account at the previous position, since the bot might rely on this.
		    _tradeSiteUserAccounts.add( accountIndex, TradeSiteUserAccount.fromPropertyValue( propertyValue));
		}
	    }
	}
    }
	
    /**
     * Start the bot.
     */
    public void start() {

	// Create a ticker thread.
	_updateThread = new Thread() {

		@Override public void run(){

		    while( _updateThread == this) {
			
			// Loop over all the strategies.
			for( Strategy currentStrategy : getStrategies()) {
			    
			    // Check, if this strategy is currently activated.
			    if( currentStrategy.isActivated() 
				&& ( ( currentStrategy.getLastEvaluationTime() == -1)
				     || ( ( TimeUtils.getInstance().getCurrentGMTTimeMicros() - currentStrategy.getLastEvaluationTime()) > currentStrategy.getEvaluationInterval()))) {
			    
				// Loop over the list of rules and check them all for execution.
				for( Rule currentRule : currentStrategy.getRules()) {

				    System.out.println( "Evaluating strategie: " + currentStrategy.getName());
				    
				    try {
					    
					if( currentRule.isConditionFilled()) {  // If the condition of this rule is filled
					    
					    currentRule.executeBody();          // , execute it's body.
					    
					    // If this rule fired, refetch it's funds on the next run.
					    refetchFunds( currentRule.getTradeSiteUserAccount());
					}

				    } catch( TradeDataNotAvailableException tdnae) {  // If the bot couldn't any trade data,
					// log that, but continue..
					    
					LogUtils.getInstance().getLogger().error( "Spread bot could not get any data for a rule: " + tdnae);
				    }
				    
				    // Wait a bit after each rule to complete trades and avoid ban for fund fetching?
				    try {
					sleep( 1000);  // Sleep 1 seconds, so the requests from the last rule should be completed.
				    } catch( InterruptedException ie) {
					System.err.println( "Strategy delay sleep interrupted: " + ie.toString());
				    }
				}

				// Wait a couple of seconds between 2 strategies, since not all our exchange requests are properly
				// coordinated yet (getAccounts() seems to cause problems, if 2 strategies use different accounts
				// and the requests are coming from the same IP).
				try {
				    sleep( 5000);  // Sleep 5 seconds, so the requests from the last strategies should be completed.
				} catch( InterruptedException ie) {
				    System.err.println( "Strategy delay sleep interrupted: " + ie.toString());
				}
			    }
			}

			// Get the current funds to check, if we can create new orders.
			// refetchFunds();
			//BigDecimal currencyFunds = getFunds( _tradedPair.getCurrency());
			//BigDecimal paymentFunds = getFunds( _tradedPair.getPaymentCurrency());
			
			if( _spreadBotUI != null) {     // If there's an UI, update it.

			    BigDecimal [] outputValues = new BigDecimal[ 2];
			    //outputValues[ 0] =  getFunds( _tradedPair.getCurrency());
			    //outputValues[ 1] =  getFunds( _tradedPair.getPaymentCurrency());
			    
			    _spreadBotUI.updateValues( null, outputValues);
			}
		    }
			 
		    try {
			sleep( 1000);  // Sleep just 1 second.
		    } catch( InterruptedException ie) {
			System.err.println( "Spread bot loop sleep interrupted: " + ie.toString());
		    }
		}
	    };
	_updateThread.start();  // Start the update thread.
    }

    /**
     * Update the spread history.
     *
     * @param spread The current spread to add.
     */
    private void updateSpreadHistory( Price currentSpread) {

	// Add the spread to the spread history.
	_spreadHistory.add( currentSpread);

	// Remove the first spreads until the max length is reached.
	while( _spreadHistory.size() > MAX_SPREAD_HISTORY_LENGTH) {
	    
	    _spreadHistory.removeFirst();  // Remove the oldest spread.
	}
    }
}
