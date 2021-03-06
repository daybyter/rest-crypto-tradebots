/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb;

import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.bot.arb.ui.ArbBotUI;
import de.andreas_rueckert.trade.bot.NativeBotCore;
import de.andreas_rueckert.trade.bot.TradeBot;
import de.andreas_rueckert.trade.bot.TradeBotProperties;
import de.andreas_rueckert.trade.bot.TradeLogger;
import de.andreas_rueckert.trade.bot.ui.TradeBotUI;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.order.OrderFactory;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;  // Just for the benchmarks.
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;  // Just for the benchmarks.
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This bot is an attempt to write a (more or less) universal arbitrage bot.
 */
public class ArbBot extends NativeBotCore implements TaskListener, TradeBot {

    // Inner classes

    /**
     * A class to fetch data from a trade site.
     */
    class DataFetchThread extends Thread {

	// Instance variables

	/**
	 * The listeners waiting for the thread to finish.
	 */
	List<TaskListener> _listeners = new ArrayList<TaskListener>();

	/**
	 * The trade site to query.
	 */
	TradeSite _tradeSite = null;


	// Constructors

	/**
	 * Create a new thread to fetch data from a trade site.
	 *
	 * @param tradeSite The trade site to query.
	 */
	public DataFetchThread( TradeSite tradeSite) {

	    _tradeSite = tradeSite;  // Store the trade site in the instance.
	}


	// Methods

	/**
	 * Add a task listener to this thread.
	 *
	 * @param listener The listener to add.
	 */
	public void addListener( TaskListener listener) {

	    // Add this listener to the list of listeners.
	    _listeners.add( listener);
	}

	/**
	 * Get the trade site, that this thread fetches.
	 *
	 * @return The trade site, that this thread fetches.
	 */
	TradeSite getTradeSite() {
	    
	    return _tradeSite;
	}

	/**
	 * Notify all the listeners, that this thread is finished.
	 */
	private void notifyListeners() {
	    
	    // Loop over all the listeners.
	    for( TaskListener currentListener : _listeners) {

		// Don't send an event. Just notify about the completion
		// with a reference to this thread.
		currentListener.taskFinished( this);
	    }
	}

	/**
	 * The fetch thread.
	 */
	@Override public void run() {
	    
	    // This is an ugly hack for now! Better use a method of the 
	    // ChartProvider! (Once it exists)
	    
	    // Get all the pairs for the trade site.
	    CurrencyPair [] allSupportedPairs = _tradeSite.getSupportedCurrencyPairs();
	    
	    // Fetching the pairs might not have worked...
	    if( ( allSupportedPairs != null) && ( allSupportedPairs.length > 0)) {
	
		try {

		    List<Depth> result = _tradeSite.getDepths( allSupportedPairs);
		    
		    // Create a new map for this trade site.
		    Map< CurrencyPair, Depth> currentMap = new HashMap< CurrencyPair, Depth>();
		    
		    // Now fill the map with the data.
		    for( Depth currentDepth : result) {
			
			if( currentDepth != null) {
			    
			    // Add the current depth to the map.
			    currentMap.put( currentDepth.getCurrencyPair(), currentDepth);
			}
		    }
		
		    // Add the current map to the depth cache.
		    _depthCache.put( _tradeSite, currentMap);

		} catch( TradeDataNotAvailableException tdnae) {
		    
		    LogUtils.getInstance().getLogger().error( "ArbBot price cache fill failed for " 
							      + _tradeSite.getName() 
							      + " : " + tdnae);
		}
	    }

	    // Put the date of this data fetching (the current date) into the map of update dates.
	    _updateDates.put( _tradeSite, new Date());

	    // Let the listeners know, that the thread is finished now.
	    notifyListeners();  
	}
    }

    /**
     * A thread, that handles the fetching and the evaluation for a single tradesite.
     */
    class TradeSiteThread extends Thread {

	// Variables

	/**
	 * The loop interval in milliseconds.
	 */
	private long _intervalMillis;

	/**
	 * Run flag to terminate the main loop.
	 */
	private boolean _run;

	/**
	 * The trade site, this thread trades on.
	 */
	private TradeSite _tradeSite;


	// Constructors

	/**
	 * Create a new bot thread for a given exchange.
	 *
	 * @param tradeSite The site to trade on.
	 * @param interval The main loop interval in microseconds(!).
	 */
	public TradeSiteThread( TradeSite tradeSite, long interval) {

	    _tradeSite = tradeSite;  // Store the trade site in the instance.

	    // I want to avoid this division in every loop iteration, so I do it once here.
	    _intervalMillis = interval / 1000L;
	}


	// Methods

	/**
	 * The main loop for each bot.
	 */
	@Override public void run() {

	    _run = true;  // Let the loop iterate while this flag is true.

	    while( _run && !isInterrupted()) {

		// Create a thread to fetch the data.
		// Should be replaced by a ChartProvider call later.
		DataFetchThread newFetchThread = new DataFetchThread( _tradeSite);
		
		// Now start the fetch thread.
		newFetchThread.start();  // Start fetching the depth from this exchange.

		try {
		    newFetchThread.join();  // Wait for this thread to complete.
		
		} catch( InterruptedException ie) {
		
		    // Try to stop the fetch thread.
		    newFetchThread.interrupt();

		    // LogUtils.getInstance().getLogger().error( "Joining ArbBot fetch thread interrupted: " + ie);

		    return;  // Just return from this thread.
		}

		// System.out.println( "DEBUG: depth data for " + _tradeSite.getName() + " fetched");
		
		// The data are now in the depth cache...

		// If this trade site is not active for trading, no thread should be started, so enable all sequences for this exchange.
		setTradeSequencesForTradeSiteEnabled( _tradeSite, true);

		// Check for missing currency pair prices and disable trade sequences with those pairs.
		disableTradeSequencesWithMissingData( _tradeSite);

		// Try to compute all the trade sequences for this exchange.
		_analyzer.calculateTradeSequences( getTradeSequences( _tradeSite));

		// System.out.println( "DEBUG: depth data for " + _tradeSite.getName() + " analyzed");
		
		// If the user wants to log opportunities, write them to a log file.
		if( LOG_OPPORTUNITIES) {

		    logOpportunities( _tradeSite);
		}

		// Update the UI with the latest results.
		getUI().updateValues( null, null);

		// System.out.println( "DEBUG: depth data for " + _tradeSite.getName() + " displayed");
		
		try {
		    
		    Thread.sleep( _intervalMillis);
		    
		} catch( InterruptedException ie) {
		    
		    // Stop the thread if, it got a interrupt signal.
		    _run = false;
		}
	    }
	}

	/**
	 * Stop this thread.
	 */
	public final void stopThread() {

	    _run = false;

	    interrupt();  // Try to interrupt this thread.
	}
    }

	
    // Static variables

    /**
     * The only instance of the bot (singleton pattern).
     */
    static TradeBot _instance = null;

    /**
     * Flag to enable/disable trade opportunities.
     */
    private final static boolean LOG_OPPORTUNITIES = true;

    /**
     * The max sequence length - 1 (!).
     */
    private final static int MAX_SEQUENCE_LENGTH = 4;

    /**
     * Flag to indicate, that the trade sequences should be written to a file.
     */
    final static boolean WRITE_TRADESEQUENCES_TO_FILE = false;


    // Instance variables

    /**
     * A buffer for all the available trade sites.
     */
    List<TradeSequence> _allTradeSequences = null;

    /**
     * An class to encapsulate the analyzer methods.
     */
    private TradeSequenceAnalyzer _analyzer;

    /**
     * This is just a hack, so I can optimize the depth requests better.
     */
    private Map< TradeSite, Map< CurrencyPair, Depth>> _depthCache = new HashMap< TradeSite, Map< CurrencyPair, Depth>>();

    /**
     * A map with the supported trade sites.
     */
    private Map<String,TradeSiteInfo> _supportedTradeSites = null;

    /**
     * A logger for the opportunities.
     */
    private TradeLogger _tradeLogger = null;

    /**
     * Use a map to access trade sequences for a given trade site quicker.
     */
    private Map< TradeSite, List< TradeSequence>> _tradeSequences = new HashMap< TradeSite, List< TradeSequence>>();

    /**
     * A map of threads for each trade site.
     */
    private Map< TradeSite, TradeSiteThread> _tradeSiteThreads = new HashMap< TradeSite, TradeSiteThread>();

    /**
     * A list of user accounts.
     */
    private Map<TradeSite,List<TradeSiteUserAccount>> _tradeSiteUserAccounts = new HashMap<TradeSite,List<TradeSiteUserAccount>>();

    /**
     * Create a map to store the date of the last data fetching for a given trade site.
     */
    private Map<TradeSite, Date> _updateDates = new HashMap< TradeSite, Date>();


    // Constructors

    /**
     * Create a new ArbBot instance.
     */
    public ArbBot() {

	// Set the name of the bot.
	_name = "Arb";

	// Set the version of this bot.
	_versionString = "0.1.1 ( Maradonna )";

	// Set the update interval for this bot.
	_updateInterval = 15;

	// Create a new analyzer for the sequences
	_analyzer = new TradeSequenceAnalyzer( this);

	// If the user wants to log the opportunities, create a logger.
	if( LOG_OPPORTUNITIES) {

	    _tradeLogger = new TradeLogger( "arbbot.log");
	}
    }
    

    // Methods

    /**
     * Add a new trade sequence to the list of used trade sequences.
     *
     * @param tradeSequence The trade sequence to add.
     */
    public final void addTradeSequence( TradeSequence tradeSequence) {
	
	// Get the trade site, this sequence is for.
	TradeSite tradeSite = tradeSequence.getTradeSite();

	// Get the sequence list for this trade site.
	List<TradeSequence> sequenceList = _tradeSequences.get( tradeSite);

	if( sequenceList == null) {  // If there is no list yet,

	    sequenceList = new ArrayList< TradeSequence>();  // create a new one.

	    // Add the sequence to the list of sequences.
	    _tradeSequences.put( tradeSite, sequenceList);
	}

	// Add the sequence to the list.
	sequenceList.add( tradeSequence);
    }

    /**
     * Add a new user account to the list of trade site accounts.
     *
     * @param tradeSiteUserAccount The new user account to add.
     */
    public void addTradeSiteUserAccount( TradeSiteUserAccount userAccount) {
       
	// Get the trade site of the account.
	TradeSite currentTradeSite = userAccount.getTradeSite();

	// Get the list of user account for this trade site.
	List< TradeSiteUserAccount> accountList = _tradeSiteUserAccounts.get( currentTradeSite);

	// If there is no account list for this trade site yet...
	if( accountList == null) {
	    
	    // ..create one.
	    accountList = new ArrayList<TradeSiteUserAccount>();
	}

	// Add the new account to this list.
	accountList.add( userAccount);
    }

    /**
     * Add a new user account to the list of trade site accounts.
     *
     * @param accountIndex The index (position) of the account.
     * @param tradeSiteUserAccount The new user account to add.
     */
    public void addTradeSiteUserAccount( int accountIndex, TradeSiteUserAccount userAccount) {
       
	// Get the trade site of the account.
	TradeSite currentTradeSite = userAccount.getTradeSite();

	// Get the list of user account for this trade site.
	List< TradeSiteUserAccount> accountList = _tradeSiteUserAccounts.get( currentTradeSite);

	// If there is no account list for this trade site yet...
	if( accountList == null) {
	    
	    // ..create one.
	    accountList = new ArrayList<TradeSiteUserAccount>();
	}

	// Add the new account to this list.
	accountList.add( accountIndex, userAccount);
    }

    /**
     * Disable the trade sequences of a given trade site, which cannot be calculated due to missing currency prices.
     *
     * @param tradeSite The trade site that the sequences are for.
     */
    private final void disableTradeSequencesWithMissingData( TradeSite tradeSite) {

	int disabledSequenceCount = 0;

	// Loop over all the depth cache entries and look for missing data.
	
	// If this site has currently any pairs enabled..
	// ( If the site is offline, this method might return null.)
	if( tradeSite.getSupportedCurrencyPairs() != null) {
	    
	    // Get the supported currency pairs of this trade site and loop over them.
	    for( CurrencyPair currentCurrencyPair : tradeSite.getSupportedCurrencyPairs()) {
		
		// Get the depth for this pair.
		Depth currentDepth = getDepthFromCache( tradeSite, currentCurrencyPair);
		
		// If there is no depth in the cache
		if( currentDepth == null) {
		
		    // Disable all trade sequence, who trade this pair on the current trade site.
		    for( TradeSequence currentSequence : getTradeSequences( tradeSite)) {
			
			// Check, if the sequence trades the pair.
			if( currentSequence.containsTradeSiteCurrencyPair( tradeSite, currentCurrencyPair)) {
			    
			    // If this sequence is active...
			    // The main purpose for this check is just the counting...which helps debugging...
			    if( currentSequence.isActive()) {
				
				// Deactivate it, so we don't get any errors.
				currentSequence.deactivate();
				
				// Count the disabled sequences for debugging.
				++disabledSequenceCount;
			    }
			}
		    }
		}
	    }
	}

	// System.out.println( disabledSequenceCount + " tradesequences disabled for site " + tradeSite.getName());
    }

    /**
     * Get the user accounts for a trade sequence.
     *
     * @param tradeSequence The trade sequence to get the account for.
     *
     * @return A list of user accounts, or null if no account is available.
     */
    List<TradeSiteUserAccount> getAccountsForTradeSequence( TradeSequence tradeSequence) {
	
	// Get the trade site of the sequence and fetch all the accounts 
	// for this trade site.
	return _tradeSiteUserAccounts.get( tradeSequence.getTradeSite());
    }

    /**
     * Get a depth from the local depth cache.
     *
     * @param tradeSite The trade site to use.
     * @param currencyPair The currency pair of the depth.
     *
     * @return The depth or null, if no such depth is in the cache.
     */
    Depth getDepthFromCache( TradeSite tradeSite, CurrencyPair currencyPair) {

	return _depthCache.get( tradeSite).get( currencyPair);
    }

    /**
     * Get the only instance of this bot (singleton pattern).
     *
     * @return The only instance of this bot.
     */
    public static TradeBot getInstance() {
	
	if( _instance == null) {       // Is there was no instance created so far
	    _instance = new ArbBot();  // , create one.
	}

	return _instance;  // Return the only instance of this bot.
    }

    /**
     * Get the orders to trade a sequence.
     *
     * @param tradeSequence The trade sequence to get the orders for.
     * @param safety The safety as percent.
     * @param userAccount The user account to use for the orders.
     *
     * @param The list of generated orders or null.
     */
    public final List<SiteOrder> getOrdersForTradeSequence( TradeSequence tradeSequence
							    , double safety
							    , TradeSiteUserAccount userAccount ) {

	// Use the order generator to create the orders.
	return OrderGenerator.getInstance( this).generateOrders( tradeSequence
								  , safety
								  , true
								  , userAccount);
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

	// Create a new list of properties for this bot.
	PersistentPropertyList persistentProperties = new PersistentPropertyList(); 

	// Write info on each trade site into the properties.
	for( TradeSiteInfo currentSiteInfo : getSupportedTradeSites()) {

	    persistentProperties.add( new PersistentProperty( "tradeSite_" + currentSiteInfo.getTradeSite().getName()
							      , null
							      , "" 
							      + ( currentSiteInfo.isActivated() ? "1" : "0")
							      + ","
							      + ( currentSiteInfo.isTradingAutomatically() ? "1" : "0")
							      , 2));

	}

	// Store the simulation mode.
	persistentProperties.add( new PersistentProperty( "SimulationMode", null, "" + ( isSimulation() ? 1 : 0), 0));

	// Add every user account as a property
	for( int i = 0; i < _tradeSiteUserAccounts.size(); ++i) {

	    // Loop over the list of accounts for this trade site.
	    for( TradeSiteUserAccount currentAccount :  _tradeSiteUserAccounts.get( i)) {

		persistentProperties.add( new PersistentProperty( "useraccount" + i, null, currentAccount.encodeAsPropertyValue(), i + 1));    
	    }
	}

	return persistentProperties;	
    }

    /**
     * Get the info for a trade site with a given name.
     *
     * @param tradeSiteName The name of the trade site.
     *
     * @return The info on this trade site or null, if a site with this name is not supported.
     */
    private final TradeSiteInfo getSupportedTradeSite( String tradeSiteName) {

	// Just return the entry of the map.
	return _supportedTradeSites.get( tradeSiteName);
    }

    /**
     * Get the list of supported trade sites of this bot.
     *
     * @return The list of supported trade sites.
     */
    public Collection<TradeSiteInfo> getSupportedTradeSites() {

	if( _supportedTradeSites == null) {  // If the list was not created yet.

	    _supportedTradeSites = new HashMap< String, TradeSiteInfo>();

	    // The names of the trade sites.
	    String [] tradeSiteNames = { "ANX"
					 , "Atomic-Trade"
					 , "Bitfinex"
					 , "Bittrex"
					 , "BtcChina"
					 , "BTCe"
					 , "Bter" 
					 , "Coins-E", /*"Cryptsy",*/ "Kraken"
					 , "Novaexchange"
					 /*, "MintPal" */ , "Poloniex" /*, "Vircurex" */
					 /*, "TradeSatoshi" */};

	    // Just add the sites manually for now...
	    for( String currentSiteName : tradeSiteNames) {

		// Try to get the API implementation with this name from the module loader.
		TradeSite currentSite = ModuleLoader.getInstance().getRegisteredTradeSite( currentSiteName);

		// If there is an implementaton with this name...
		if( currentSite != null) {

		    // we support it and enable it by default.
		    _supportedTradeSites.put( currentSiteName, new TradeSiteInfo( currentSite, true));

		}
	    }
	}
	
	return _supportedTradeSites.values();   // Return the list of trade sites.
    }

    /**
     * Get the list of all registered trade sequences from this bot.
     *
     * @return The list of registered trade sequences, or null, if no list could be created.
     */
    public synchronized List<TradeSequence> getTradeSequences() {

	if( _allTradeSequences == null) {

	    // Create a list for the result.
	    _allTradeSequences = new ArrayList<TradeSequence>();

	    // Just loop over the trade sequences for all site and add them to the result.
	    // This is rather inefficient, but easy to implement at the moment... :-(
	    for( TradeSiteInfo currentTradeSiteInfo : getSupportedTradeSites()) {

		// Try to get the sequences for this trade site.
		List<TradeSequence> currentList = getTradeSequences( currentTradeSiteInfo.getTradeSite());

		if( currentList != null) {   // If there is a list of sequences.

		    // Just add all the sequences to the result.
		    _allTradeSequences.addAll( currentList);
		}
	    }
	}
	
	return _allTradeSequences;  // Return the buffer with all the sequences.
    }

    /**
     * Get the list of registered trade sequences for a trade site from this bot.
     *
     * @param tradeSite The trade site of the trade sequences.
     *
     * @return The list of registered trade sequences, or null, if no list could be created.
     */
    public List<TradeSequence> getTradeSequences( TradeSite tradeSite) {

	if( _tradeSequences.get( tradeSite) == null) {  // If there are no sequences stored yet for this exchange.

	    //System.out.print( "Generating new trade sequences: ");

	    // Just to get an idea about the performance...
	    //System.out.println( ( new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format( new Date()));

	    _tradeSequences.put( tradeSite, TradeSequenceGenerator.getInstance().generateTradeSequences( tradeSite));

	    //System.out.print( resultBuffer.size() + " trade sequences generated: ");
	    //System.out.println( ( new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format( new Date()));
	}	

	return _tradeSequences.get( tradeSite);  // Return the list of trade sequences.
    }

    /**
     * Get a trade site user account from the bot.
     *
     * @param tradeSite The trade site of the account.
     * @param index The index of the user account.
     *
     * @return The user account or null.
     */
    public final TradeSiteUserAccount getTradeSiteUserAccount( TradeSite tradeSite, int index) {

	// Get the list of accounts for this trade sites.
	List<TradeSiteUserAccount> accountList = _tradeSiteUserAccounts.get( tradeSite);

	// If there is no list for this site yet, just return null for now.
	if( accountList == null) {

	    return null;
	}

	// Return the <index>th entry of the list.
	return accountList.get( index);
    }

    /**
     * Get a list with all the trade site user accounts from the bot.
     *
     * @param TradeSite tradeSite The trade site, that the account is for.
     *
     * @return The list of user accounts for this trade site or null.
     */
    public final List<TradeSiteUserAccount> getTradeSiteUserAccounts( TradeSite tradeSite) {

	return _tradeSiteUserAccounts.get( tradeSite);
    }

    /**
     * Get a trade site user account with a given name.
     * 
     * @param accountName The name of the account.
     *
     * @return The first found user account with this name or null.
     */
    public final TradeSiteUserAccount getTradeSiteUserAccountForName( String accountName) {

	// Loop over the entries of the map.
	for( List<TradeSiteUserAccount> accountList : _tradeSiteUserAccounts.values()) {

	    // Loop over the accounts for the current trade site.
	    for( TradeSiteUserAccount currentAccount : accountList) {

		String name = currentAccount.getAccountName();  // Get the name of this account.
		
		if( ( name != null) && name.equals( accountName)) {  // if it exists and equals the requested name...
		    
		    return currentAccount;  // return this account.
		}
	    }
	}

	return null;  // No account with this name found.
    }

    /**
     * Get the UI for this bot.
     *
     * @return The UI for this bot.
     */
    public TradeBotUI getUI() {
	if( _botUI == null) {                    // If there is no UI yet,
	    _botUI = new ArbBotUI( this);        // create one. This is optional, since the bot
	}                                        // might run in daemon mode.
	return _botUI;
    }

    /**
     * Get the date of the last data fetch from a given trade site.
     *
     * @param tradeSite The trade site, that was queried.
     *
     * @return The date of the last complete data fetch or null.
     */
    public final Date getUpdateDate( TradeSite tradeSite) {

	// Get the date from the map of dates.
	return _updateDates.get( tradeSite);
    }

    /**
     * Check, if the bot is currently stopped.
     *
     * @return true, if the bot is currently stopped. False otherwise.
     */
    public boolean isStopped() {

	// If there are no running threads anymore, the bot is stopped.
	return _tradeSiteThreads.isEmpty();
    }

    /**
     * Log the trading opportunities for a given trade site.
     *
     * @param tradeSite The trade site to log.
     */
    void logOpportunities( TradeSite tradeSite) {

	// Get the list of sequences for this trade site.
	List<TradeSequence> sequenceList = getTradeSequences( tradeSite);

	if( sequenceList != null) {  // If there is actually a list

	    for( TradeSequence currentSequence : sequenceList) {

		if( ( currentSequence.getTradeIndicatorOutput() != null)
		    && ( currentSequence.getTradeIndicatorInput() != null)
		    && ( currentSequence.getTradeIndicatorOutput().compareTo( currentSequence.getTradeIndicatorInput()) > 0)) {

		    _tradeLogger.log( "Arbitrage opportunity: " + currentSequence.completeDataToString());
		}
	    }
	}
    }

    /**
     * Clear and refill the depth cache.
     */
    private void refillDepthCache() throws TradeDataNotAvailableException {

	// Clear the depth cache.
	_depthCache.clear();

	// Create an array of threads, so we can fetch the data from the exchanges in parallel.
	List< Thread> depthFetchThread = new ArrayList< Thread>();

	// Loop over the supported trade sites.
	for( TradeSiteInfo currentTradeSiteInfo : getSupportedTradeSites()) {
	    
	    // Fetch the prices only, if trading on this site is activated.
	    if( currentTradeSiteInfo.isActivated()) {

		// System.out.println( "DEBUG: fetching prices for " + currentTradeSiteInfo.getTradeSite().getName());

		// Get the actual trade site from the info.
		final TradeSite currentTradeSite = currentTradeSiteInfo.getTradeSite();
	    
		// Create a new thread for each exchange.
		// I guess, we'll never have enough supported exchanges
		// here, so the number of threads becomes a problem.
		// But the multithreading should speed up the fetching,
		// I guess.
		DataFetchThread newFetchThread = new DataFetchThread( currentTradeSite);
		
		// Add the new thread to the list of fetching threads.
		depthFetchThread.add( newFetchThread);
		
		// Add the bot as a listener to the fetching thread.
		newFetchThread.addListener( this);

		// Now start the fetch thread.
		newFetchThread.start();  // Start fetching the depth from this exchange.
	    }
	}

	// Now wait for all the fetch threads to complete.
	for( Thread currentFetchThread : depthFetchThread) {
	    
	    try {
		currentFetchThread.join();  // Wait for this thread to complete.
		
	    } catch( InterruptedException ie) {
		
		LogUtils.getInstance().getLogger().error( "Joining ArbBot fetch thread interrupted: " + ie);
	    }
	}
    }

    /**
     * Enable or disable all trade sequences for a given trade site.
     * This method is called, when the user clicks on a trade site checkbox in the UI.
     *
     * @param tradeSite The trade site to enable or disable.
     * @param enabled true, if the trade sequences should be enabled.
     */
    public final void setTradeSequencesForTradeSiteEnabled( TradeSite tradeSite, boolean enabled) {

	// Just loop over all the sequences.
	for( TradeSequence currentSequence : getTradeSequences( tradeSite)) {

	    if( tradeSite.equals( currentSequence.getTradeSite())) {  // If this sequence is for the given trade site.

		currentSequence.setActive( enabled);
	    }    
	}
    }

    /**
     * Set the properties of this bot.
     *
     * @param propertyList The tradebot properties.
     */
    public void setSettings( PersistentPropertyList propertyList) {

	// Loop over the settings.
	for( PersistentProperty currentProperty : propertyList) {

	    // Get the name of the current property.
	    String propertyName = currentProperty.getName();

	    // Check, if this property is for a tradesite.
	    if( propertyName.startsWith( "tradeSite_")) {

		// Get the name of the trade site.
		String sitename = propertyName.substring( 10);

		// Try to the info on this site modify it according to the stored settings.
		TradeSiteInfo siteInfo = getSupportedTradeSite( sitename);

		if( siteInfo == null) {  // No site with this name found?

		    LogUtils.getInstance().getLogger().error( "Cannot set setting for trade site " + sitename + " for arb bot");

		} else {

		    // Get the value of this property.
		    String propertyValue = (String)(currentProperty.getValue());

		    // Since it's a colon separated list of values, separate them.
		    String [] values = propertyValue.split(",");

		    // Get the setting for the site activations.
		    siteInfo.setActivated( values[0].equals("1"));

		    // Get the setting for the automated trading.
		    siteInfo.setAutomaticTrading( values[1].equals("1"));
		}
	    }

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

		// Create the new user account.
		TradeSiteUserAccount newAccount = TradeSiteUserAccount.fromPropertyValue( propertyValue);

		// Get the trade site of the account.
		TradeSite tradeSite = newAccount.getTradeSite();

		// Get the list of user accounts for this trade site.
		List<TradeSiteUserAccount> accountList = _tradeSiteUserAccounts.get( tradeSite);

		if( ( accountList != null) && ( accountIndex > ( accountList.size() - 1))) {

		    // Add the user account at the previous position, since the bot might rely on this.
		    addTradeSiteUserAccount( newAccount);

		} else {
		    
		    // Add the user account at the previous position, since the bot might rely on this.
		    addTradeSiteUserAccount( accountIndex, newAccount);
		}
	    }
	}
    }

    /**
     * Start the bot.
     */
    public void start() {

	// Enable all trade sequences of the activated trade sites.
	//for( TradeSiteInfo currentTradeSiteInfo : getSupportedTradeSites()) {
	    
	//    setTradeSequencesForTradeSiteEnabled( currentTradeSiteInfo.getTradeSite(), currentTradeSiteInfo.isActivated());
	//}

	// Set all the update dates to null;
	_updateDates.clear();

	// Start a thread for each activated trade site
	for( TradeSiteInfo currentTradeSiteInfo : getSupportedTradeSites()) {

	    if( currentTradeSiteInfo.isActivated()) {
		
		// Get the trade site, this info is for.
		TradeSite currentTradeSite = currentTradeSiteInfo.getTradeSite();

		// Create a new thread for this trade site.
		TradeSiteThread newTradeSiteThread = new TradeSiteThread( currentTradeSite, (long)_updateInterval * 1000000L);

		// Start the thread
		newTradeSiteThread.start();

		// Add this thread to the list of threads.
		_tradeSiteThreads.put( currentTradeSite, newTradeSiteThread);
	    }
	}
    }

    /**
     * Stop this bot.
     */
    public void stop() {

	// Signal a stop the threads for all trade site.
	for( TradeSiteThread currentThread : _tradeSiteThreads.values()) {

	    currentThread.stopThread();
	}

	/*
	// Wait for all the threads to finish.
	for( Map.Entry<TradeSite, TradeSiteThread> currentThreadEntry : _tradeSiteThreads.entrySet()) {

	    // Get the site, the current thread is for.
	    TradeSite currentTradeSite = currentThreadEntry.getKey();

	    // Get the current thread.
	    TradeSiteThread currentThread = currentThreadEntry.getValue();

	    try {
		
		currentThread.join();  // Try to stop the current thread.

		// If the thread was stopped, remove it from the map of threads.
		_tradeSiteThreads.remove( currentTradeSite);

	    } catch( InterruptedException ie) {  // Cannot stop the current thread.

		LogUtils.getInstance().getLogger().error( "Cannot stop arb bot trading thread for " + currentTradeSite.getName());
	    }
	    }
	*/
	// If there are still any running threads, just remove them.
	// Not optimal, but how to kill them then?
	_tradeSiteThreads.clear();
    }

    /**
     * A thread completed it's task.
     *
     * @param thread The finished thread.
     */
    public void taskFinished( Thread thread) {

	if( thread instanceof DataFetchThread) {

	    DataFetchThread dataFetcher = (DataFetchThread)thread;

	    // System.out.println( "Data fetching from " + dataFetcher.getTradeSite().getName() + " completed.");
	}
    }
}
