/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot;

import de.andreas_rueckert.trade.bot.ui.TradeBotUI;


/**
 * This is a base class for some native bots.
 */
public abstract class NativeBotCore {

    // Static variables


    // Instance variables

    /**
     * The user interface for this bot.
     */
    protected TradeBotUI _botUI = null;

    /**
     * The name of the bot.
     */
    protected String _name;

    /**
     * The properties of this bot.
     */
    protected TradeBotProperties _properties = null;

    /**
     * A flag to indicate, if this bot runs as a simulation currently.
     */
    protected boolean _simulationMode = false;

    /**
     * A logger for the trades.
     */
    protected TradeLogger _tradeLogger = null;

    /**
     * The base update interval in seconds.
     */
    protected int _updateInterval = 20;

    /**
     * The ticker loop.
     */
    protected Thread _updateThread = null;

    /**
     * The version of this bot as a string.
     */
    protected String _versionString = null;


    // Constructors


    // Methods

    /**
     * Get the name of this bot.
     *
     * @return The name of this bot.
     */
    public String getName() {
	return _name;
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
     * Get the version string of this bot.
     *
     * @return The version string of this bot.
     */
    public String getVersionString() {

	// Get the version of this bot as a string.
	return _versionString;
    }

    /**
     * Check, if this bot runs in simulation mode or is actually trading.
     *
     * @return true, if this bot runs in simulation mode. False otherwise.
     */
    public final boolean isSimulation() {

	return _simulationMode;
    }

    /**
     * Check, if the bot is currently stopped.
     *
     * @return true, if the bot is currently stopped. False otherwise.
     */
    public boolean isStopped() {
	return _updateThread == null;
    }

    /**
     * Set a new simulation status for this bot.
     *
     * @param simulationMode The new simulation status.
     */
    public final void setSimulation( boolean simulationMode) {

	_simulationMode = simulationMode;  // Store the new value in the instance.
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
}