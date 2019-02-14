/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot;

import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.bot.ui.TradeBotUI;
import de.andreas_rueckert.trade.bot.ui.TrendBotUI;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import java.util.List;


/**
 * This bot trades by following trends.
 */
public class TrendBot implements TradeBot {

    // Inner classes
        
    /**
     * This class handles the trading for one currency pair on one trade site.
     */
    class CurrencyPairTradeThread extends Thread {
	
	// Instance variables
	/**
	 * The traded currency pair.
	 */
	CurrencyPair _tradedPair;
	
	/**
	 * The trade site to trade on.
	 */
	TradeSite _tradeSite;


	// Constructors

	CurrencyPairTradeThread( CurrencyPair tradedPair, TradeSite tradeSite) {

	    // Give the thread a reasonable name.
	    super( tradeSite.getName() + "_" + tradedPair.getCode() + "_trader");

	    // Store the trade site and the currency pair in the instance.
	    _tradedPair = tradedPair;
	    _tradeSite = tradeSite;
	}


	// Methods

	/**
	 * Get the traded currency pair of this trade thread.
	 *
	 * @return The traded currency pair of thie trading thread.
	 */
	CurrencyPair getTradedPair() {
	    return _tradedPair;
	}

	/**
	 * Get the trade site of this trading thread.
	 *
	 * @return The trade site of this trading thread.
	 */
	TradeSite getTradeSite() {
	    return _tradeSite;
	}
    }


    // Static variables


    // Instance variables

    /**
     * The properties of this bot.
     */
    protected TradeBotProperties _properties = null;

    /**
     * A list for the trade threads.
     */
    private List<CurrencyPairTradeThread> _threadList;

    /**
     * The UI for this bot.
     */
    private TradeBotUI _trendBotUI = null;

    /**
     * A logger for the trades.
     */
    protected TradeLogger _tradeLogger = null;


    // Constructors


    // Methods

    /**
     * Get the name of this bot.
     *
     * @return The name of this bot.
     */
    public String getName() {
	return "Trend";
    }

    /**
     * Get the properties of this bot.
     *
     * @return The properties of this bot.
     */
    public TradeBotProperties getProperties() {
	
	if( _properties == null) {                                 // If there are no propertes yet,
	    _properties = new TradeBotProperties( this);  // create them.
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
     * Get the UI for this bot.
     *
     * @return The UI for this bot.
     */
    public TradeBotUI getUI() {
	if( _trendBotUI == null) {                    // If there is no UI yet,
	    _trendBotUI = new TrendBotUI( this);      // create one. This is optional, since the bot
	}                                              // might run in daemon mode.
	return _trendBotUI;
    }

    /**
     * Get the version string of this bot.
     *
     * @return The version string of this bot.
     */
    public String getVersionString() {

	// Get the version of this bot as a string.
	return "0.1.1 ( Kuka )";
    }

    /**
     * Check, if the bot is currently stopped.
     *
     * @return true, if the bot is currently stopped. False otherwise.
     */
    public boolean isStopped() {

	// If there are no running threads, the bot must be stopped.
	return _threadList.isEmpty();
    }

    /**
     * Set the properties of this bot.
     *
     * @param propertyList The tradebot properties.
     */
    public void setSettings( PersistentPropertyList propertyList) {
    }

    /**
     * Start the trading of the bot.
     */
    public void start() {
	
	// This is an ugly hack. The trade sites and currency pairs to trade 
	// should come from an config file or should be determined by the
	// activity of the trade site and currency pair.

	// Hack: create a new thread
	CurrencyPairTradeThread newThread = new CurrencyPairTradeThread( new CurrencyPairImpl( "BTC", "USD")
									 , ModuleLoader.getInstance().getRegisteredTradeSite( "BTCe"));

	newThread.start();  // Start the thread.

	_threadList.add( newThread);  // Add the thread to the thread list.
    }
    
    /**
     * Stop the trading of the bot.
     */
    public void stop() {

	// Signal all trade threads to stop now.
	for( CurrencyPairTradeThread tradeThread : _threadList) {
	    tradeThread.stop();
	}

	try {
	    // Now wait for the trade threads to actually stop.
	    for( CurrencyPairTradeThread tradeThread : _threadList) {
		tradeThread.join();
	    }
	} catch( InterruptedException ie) {
	    LogUtils.getInstance().getLogger().error( "Problem when trying to stop a trade thread in the trend bot: " + ie);
	}

	// Now remove all the stopped threads from the list.
	_threadList.clear();
    }
}
