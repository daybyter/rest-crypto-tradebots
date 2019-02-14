/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot;

import de.andreas_rueckert.persistence.PersistentProperties;
import de.andreas_rueckert.trade.bot.ui.TradeBotUI;


/**
 * This is an interface for various trade bots.
 *
 * A bot should save it's properties, so I extend PersistentProperties here.
 */
public interface TradeBot extends PersistentProperties {


    // Methods

    /**
     * Get the name of this bot.
     *
     * @return The name of this bot.
     */
    public String getName();

    /**
     * Get the properties of this bot.
     *
     * @return The properties of this bot.
     */
    public TradeBotProperties getProperties();

    /**
     * Get the logger of this bot.
     *
     * @return The trade logger of this bot.
     */
    public TradeLogger getTradeLogger();

    /**
     * Get a trade bot ui.  
     *
     * @return A trade bot user interface.
     */
    public TradeBotUI getUI();

    /**
     * Get the version string of this bot.
     *
     * @return The version string of this bot.
     */
    public String getVersionString();

    /**
     * Check if the bot is stopped.
     *
     * @return true, if the bot is stopped.
     */
    public boolean isStopped();
    
    /**
     * Start the bot.
     */
    public void start();

    /**
     * Stop the bot.
     */
    public void stop();
}