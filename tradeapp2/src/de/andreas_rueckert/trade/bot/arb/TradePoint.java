/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb;

import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;


/**
 * This class handles the data for one station of the trading sequence.
 */
public class TradePoint {
    
    // Instance variables
    
    /**
     * Flag to indicate, if the currency is bought or sold.
     */
    private boolean _isBuy;
    
    /**
     * The currency pair, that is traded at this point.
     */
    private CurrencyPair _tradedPair;
    
    /**
     * The trade site, where this currency pair is traded.
     */
    private TradeSite _tradeSite;
    
	
    // Constructors

    /**
     * Create a new trade point.
     *
     * @param tradeSite The trade site to use for this point.
     * @param tradedPair The currency pair to trade.
     * @param isBuy flag to indicate, if there is a buy at this trade point.
     */
    public TradePoint( TradeSite tradeSite, CurrencyPair tradedPair, boolean isBuy) {

	// Store the parameters in the trade point instance.
	_tradeSite = tradeSite;
	_tradedPair = tradedPair;
	_isBuy = isBuy;
    }

    
    // Methods

    /**
     * Just check, if this trade point contains a give currency.
     *
     * @param currency The currency to check for.
     *
     * @return true, if one of the traded currencies equals the given currency.
     */
    public final boolean containsCurrency( Currency currency) {
	
	return _tradedPair.getCurrency().equals( currency) 
	    || _tradedPair.getPaymentCurrency().equals( currency);
    }

    /**
     * Clone this trade point into a new TradePoint object.
     *
     * @return The cloned trade point.
     */
    public final TradePoint clone() {

	// Create a new instance from the instance data.
	return new TradePoint( _tradeSite, _tradedPair, _isBuy);
    }

    /**
     * Check, if this trade point equals a given trade point.
     *
     * @param tradePoint The trade point to check for equality.
     *
     * @return true, if the 2 points are equals. false otherwise.
     */
    public final boolean equals( TradePoint tradePoint) {
	return _tradeSite.getName().equals( tradePoint.getTradeSite().getName())
	    && _tradedPair.equals( tradePoint.getTradedCurrencyPair())
	    && _isBuy == tradePoint.isBuy();
    }
    
    /**
     * Check, if this trade point equals a given trade point.
     *
     * @param tradePoint The trade point to check for equality.
     *
     * @return true, if the 2 points are equals. false otherwise.
     */
    public final boolean equalsIgnoreBuy( TradePoint tradePoint) {
	return _tradeSite.getName().equals( tradePoint.getTradeSite().getName())
	    && _tradedPair.equals( tradePoint.getTradedCurrencyPair());
    } 

    /**
     * Get the resultung currency of this arbitrage point.
     *
     * @return The resulting currency of this arbitrage point.
     */
    public final Currency getResultingCurrency() {
	return _isBuy ? _tradedPair.getCurrency() : _tradedPair.getPaymentCurrency();
    }

    /**
     * Get the traded currency pair.
     *
     * @return The traded currency pair.
     */
    public final CurrencyPair getTradedCurrencyPair() {
	return _tradedPair;
    }

    /**
     * Get the trade site of this trade point.
     *
     * @return The trade site of this trade point.
     */
    public final TradeSite getTradeSite() {
	return _tradeSite;
    }

    /**
     * Get the flag indicating a buy or sell at this trade point.
     *
     * @return A flag indicating if this point is a buy or sell.
     */
    public final boolean isBuy() {
	return _isBuy;
    }

    /**
     * Set a new value for the buy flag.
     *
     * @param isBuy The new value for the buy flag.
     */
    public final void setBuy( boolean isBuy) {
	_isBuy = isBuy;
    }

    /**
     * Check, if a given currency is the resulting currency of this trade point.
     *
     * @param currency The currency to check for.
     *
     * @return true, if the given currency is the resulting one of this trade point.
     */
    public final boolean isResultingCurrency( Currency currency) {

	return getResultingCurrency().equals( currency);
    }
}
