/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb;

import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;


/**
 * This interface implements a currency pair from a trade site, that is
 * fetched with a given priority.
 */
interface PrioritizedCurrencyPair extends CurrencyPair {

    // Variables


    // Methods

    /**
     * Get the timestamp of the last request as GMT relative microseconds.
     *
     * @return The timestamp of the last request as GMT relative microseconds.
     */
    long getLastRequestTimestamp();

    /**
     * Get the recommended request interval for this currency pair as microseconds.
     *
     * @return The recommended request interval for this currency pair as microseconds.
     */
    long getRequestInterval();

    /**
     * Get the trade site, from where this currency pair is fetched.
     *
     * @return The trade site, from where this currency pair is fetched.
     */
    TradeSite getTradeSite();
}
