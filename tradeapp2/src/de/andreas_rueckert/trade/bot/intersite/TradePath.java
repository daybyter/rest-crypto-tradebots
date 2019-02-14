/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.intersite;

import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.site.TradeSite;


/**
 * This class encapsulates the data for a single tradepath.
 */
public class TradePath {

    // Instance vars

    /**
     * The currency, we start with.
     */
    private Currency _startCurrency;

    /**
     * The trade site, where the transfer is started.
     */
    private TradeSite _startSite;

    /**
     * The trade site, where we want to end the transfer.
     */
    private TradeSite _targetSite;

    /**
     * The currency, that is used to transfer the funds.
     */
    private Currency _transferCurrency;


    // Constructors

    /**
     * Create a new trade path for the intersite bot.
     *
     * @param startSite The tade site, where the transfer starts.
     * @param startCurrency The currency, that we start with.
     * @param tranferCurrency The currency to be used for the funds transfer.
     * @param targetSite The trade site, where the transfer ends.
     */
    public TradePath( TradeSite startSite, Currency startCurrency, Currency transferCurrency, TradeSite targetSite) {
	_startSite = startSite;
	_startCurrency = startCurrency;
	_transferCurrency = transferCurrency;
	_targetSite = targetSite;
    }


    // Methods

    /**
     * Get the start currency of this trade path.
     *
     * @return The start currency of trade path.
     */
    public Currency getStartCurrency() {
	return _startCurrency;
    }

    /**
     * Get the start site of this trade path.
     *
     * @return The start site of this trade path.
     */
    public TradeSite getStartSite() {
	return _startSite;
    }

    /**
     * Get the target site of this trade path.
     *
     * @return The target site of this trade path.
     */
    public TradeSite getTargetSite() {
	return _targetSite;
    }

    /**
     * Get the transfer currency of this trade path.
     *
     * @return The transfer currency of this trade path.
     */
    public Currency getTransferCurrency() {
	return _transferCurrency;
    }

    /**
     * Reverse this trade path.
     *
     * @return A new trade path, which is reverse to this one.
     */
    public TradePath reverse() {

	// Just switch start and target site in the new TradePath object.
	return new TradePath(  _targetSite, _startCurrency, _transferCurrency, _startSite);
    }

    /**
     * Convert this tradepath to a string representation.
     *
     * @return A string representation of this trade path.
     */
    public String toString() {

	return ""
	    + getStartSite().getName()
	    + " "
	    + getStartCurrency().getCode() 
	    + " => "
	    + getTransferCurrency().getCode() 
	    + " => "
	    + getTransferCurrency().getCode() 
	    + " => "
	    + getStartCurrency().getCode()
	    + " "
	    + getTargetSite().getName();
    }
}
