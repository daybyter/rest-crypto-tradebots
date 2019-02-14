/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb;

import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.site.TradeSite;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds statistics on the best currency distribution.
 */
class CurrencyStats {

    // Inner classes

    /**
     * The currency distribution for a given trade site.
     */
    class TradeSiteCurrencyStats {

	// Variables

	/**
	 * The statistics for each currency.
	 */
	Map< Currency, BigDecimal> _currencyStatistics = new HashMap< Currency, BigDecimal>();

	/**
	 * The trade site, this statistics are for.
	 */
	TradeSite _tradeSite;


	// Constructors


	// Methods

	/**
	 * Count the appearance of a currency.
	 *
	 * @param currency The currency type.
	 */
	void countCurrency( Currency currency) {

	    // Get the counter for the given currency and increment it.
	    BigDecimal newCount = getCounter( currency).add( BigDecimal.ONE);

	    // Write the new counter value to the map.
	    _currencyStatistics.put( currency, newCount);
	}

	/**
	 * Get the counter for a given currency.
	 *
	 * @param currency The given currency.
	 *
	 * @return The counter for the given currency.
	 */
	BigDecimal getCounter( Currency currency) {

	    // If the currency is not yet in the statistics.
	    if( ! _currencyStatistics.containsKey( currency)) {
		
		// Add it with an initial value of 0.
		_currencyStatistics.put( currency, new BigDecimal( "0"));
	    }

	    // Return the counter for the given currency.
	    return _currencyStatistics.get( currency);
	}
    }


    // Static variables


    // Instance variables

    /**
     * Create currency statistics for each trade site.
     */
    Map< TradeSite, TradeSiteCurrencyStats> _currencyStats = new HashMap< TradeSite, TradeSiteCurrencyStats>();


    // Constructors
    
    
    // Methods

    /**
     * Update the statistics and add the data for a given trade sequence.
     *
     * @param tradeSequence The given trade sequence.
     */
    public void countTradeSequence( TradeSequence tradeSequence) {

	// Get the currency stats for the trade site of the sequence.
	TradeSiteCurrencyStats currencyStats = getTradeSiteCurrencyStats( tradeSequence.getTradeSite());

	// Loop over the currencies, that are used in the sequence and update the stats accordingly.
	for( Currency currentCurrency : tradeSequence.getUsedCurrencies()) {

	    // Count this currency for the statistics.
	    currencyStats.countCurrency( currentCurrency);
	}
    }

    /**
     * Get the statistics for a given trade site.
     *
     * @param tradeSite The trade site, we want the statistics for.
     *
     * @return The statistics for the given trade site.
     */
    TradeSiteCurrencyStats getTradeSiteCurrencyStats( TradeSite tradeSite) {

	// If there are no statistics for this trade site.
	if( ! _currencyStats.containsKey( tradeSite)) {

	    // Create them.
	    _currencyStats.put( tradeSite, new TradeSiteCurrencyStats());
	}

	// Return the statistics for this trade site.
	return _currencyStats.get( tradeSite);
    }
}