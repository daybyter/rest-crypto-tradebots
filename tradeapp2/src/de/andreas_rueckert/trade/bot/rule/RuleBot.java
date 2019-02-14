/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.rule;

import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.bot.NativeBotCore;
import de.andreas_rueckert.trade.bot.rule.RuleSetFile;
import de.andreas_rueckert.trade.bot.TradeBot;
import de.andreas_rueckert.trade.bot.TradeBotProperties;
import de.andreas_rueckert.trade.bot.ui.RuleBotUI;
import de.andreas_rueckert.trade.bot.ui.TradeBotUI;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.LogUtils;
import java.util.ArrayList;


/**
 * This bot is the first attempt to work with rule sets in a bot.
 */
public class RuleBot extends NativeBotCore implements TradeBot {

    // Static variables

    /**
     * The only instance of this bot (singleton pattern).
     */
    private static RuleBot _instance = null;


    // Instance variables

    /**
     * The rule sets for the trading strategies.
     */
    private ArrayList<RuleSetFile> _tradeRules = new ArrayList<RuleSetFile>();

    /**
     * The user interface for this bot.
     */
    private RuleBotUI _ruleBotUI = null;

    /**
     * The sleeping interval between 2 trade actions.
     */
    private int _updateInterval = 15 * 1000;  // Default is 15 seconds for now.

    /**
     * The currency pair to trade.
     */
    private CurrencyPair _tradedPair = null;

    /**
     * The loop to update the ticker.
     */
    private Thread _updateThread = null;


    // Constructors

    /**
     * Create a new RuleBot instance. Constructor is private for
     * singleton pattern.
     */
    private RuleBot() {

	// Set the basic data of the bot.

	_name = "Rule";  // Name of the bot.

	_versionString = "0.1.1 ( Messi )";  // Version string.
    }
    
    
    // Methods
    
    /**
     * Check, if all the required trade data for the rule engine are available.
     *
     * @return true, if all the trade data are available. false otherwise.
     */
    private boolean allTradeDataAvailable() {

	return true;  // Just a hack for now.
    }

    /**
     * Add the trade rules from a given file to the list of trade rules.
     *
     * @param tradeRuleFile A file with trade rules.
     */
    public void addTradeRules( RuleSetFile tradeRuleFile) {

	// Just add the file to the list of trade rule files.
	_tradeRules.add( tradeRuleFile);
    }

    /**
     * Get the only instance of this bot (singleton pattern).
     *
     * @return The only instance of this bot.
     */
    public static RuleBot getInstance() {
	if( _instance == null) {
	    _instance = new RuleBot();
	}
	return _instance;
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
     * Get the loaded trade rules.
     *
     * @return The loaded trade rules as an array.
     */
    public ArrayList<RuleSetFile> getTradeRules() {

	return _tradeRules;
    }

    /**
     * Get the UI for this bot.
     *
     * @return The UI for this bot.
     */
    public TradeBotUI getUI() {
	if( _ruleBotUI == null) {                    // If there is no UI yet,
	    _ruleBotUI = new RuleBotUI( this);       // create one. This is optional, since the bot
	}                                            // might run in daemon mode.
	return _ruleBotUI;
    }

    /**
     * Get the update interval for the rule bot.
     *
     * @return The update interval for the rule bot.
     */
    public int getUpdateInterval() {
	return _updateInterval;
    }

    /**
     * Set the properties of this bot.
     *
     * @param propertyList The tradebot properties.
     */
    public void setSettings( PersistentPropertyList propertyList) {
    }

    /**
     * Set a new rule set with trade rules for this bot.
     *
     * @param tradeRules A list of rule set file for the trading.
     */
    public void setTradeRules( ArrayList<RuleSetFile> tradeRules) {
	_tradeRules = tradeRules;
    }

    /**
     * Set a new update interval for the bot.
     *
     * @param updateInterval The new update interval for the bot.
     */
    public void setUpdateInterval( int updateInterval) {
	_updateInterval = updateInterval;
    }

    /**
     * Start the bot.
     */
    public void start() {

	// Create a ticker thread.
	_updateThread = new Thread() {

		@Override public void run(){

		    while( _updateThread == this) {
			
			// Execute all the available rule sets
			for( RuleSetFile ruleSetFile : _tradeRules) {
			    try {
				
				// Try to execute this rule set file.
				TradeApp.getApp().getTradeBot().executeRules( ruleSetFile);
				
			    } catch( TradeDataNotAvailableException tnae) {
				LogUtils.getInstance().getLogger().info( "Some trade data, requested by the rule set " + ruleSetFile.getName() + " are not available: " + tnae);
			    }
			}

			try {
			    sleep( _updateInterval);
			} catch( InterruptedException ie) {
			    System.err.println( "Rule bot sleep interrupted: " + ie.toString());
			}
		    }
		}
	    };
	_updateThread.start();  // Start the update thread.
    }
}
