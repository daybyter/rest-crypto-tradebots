/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb;

import de.andreas_rueckert.trade.site.TradeSite;


/**
 * Info on a used trade site.
 */
public class TradeSiteInfo {

    // Instance variables

    /**
     * Flag to indicate, if we are trading on this trade site at the moment.
     */
    private boolean _activated = false;

    /**
     * Flag to indicate, if we can do automated trading on this exchange.
     */
    private boolean _automaticTrading = false;

    /**
     * The trade site, this info is for.
     */
    private TradeSite _tradeSite;


    // Constructors

    /**
     * Create a new trade site info with a trading status.
     *
     * @param tradeSite The trade site, this info is for.
     * @param activated true, if trading is enabled. False otherwise.
     */
    TradeSiteInfo( TradeSite tradeSite, boolean activated) {
	    
	_tradeSite = tradeSite;
	_activated = activated;
    }

    /**
     * Create a new trade site info with a trading status and the automatic trading flag.
     *
     * @param tradeSite The trade site, this info is for.
     * @param activated true, if trading is enabled. False otherwise.
     * @param automatic trading flag.
     */
    TradeSiteInfo( TradeSite tradeSite, boolean activated, boolean automaticTrading) {
	    
	this( tradeSite, activated);  // Use the constructor to save the first 2 parameters.

	// Store the flag fo automatic trading in the instance.
	_automaticTrading = automaticTrading;
    }
    

    // Methods

    /**
     * Get the trade site, this info is for.
     *
     * @return The trade site, this info is for.
     */
    public final TradeSite getTradeSite() {

	return _tradeSite;
    }

    /**
     * Check, if we currently trade of this trade site.
     *
     * @return true, if we currently trade on this trade site.
     */
    public final boolean isActivated() {

	return _activated;
    }

    /**
     * Check if automativ trading is activated for this exchange,
     *
     * @return true, if we trade automatically on this exchange. False otherwise.
     */
    public final boolean isTradingAutomatically() {

	return _automaticTrading;
    }

    /**
     * Enable or disable the trading on this trade site.
     *
     * @param activated true, if trading should we enabled. False otherwise.
     */
    public final void setActivated( boolean activated) {

	_activated = activated;
    }

    /**
     * Activate or deactivate the automated trading.
     *
     * @param activated The flag to activate or deactivate the automated trading.
     */
    public final void setAutomaticTrading( boolean activated) {

	_automaticTrading = activated;
    }
}