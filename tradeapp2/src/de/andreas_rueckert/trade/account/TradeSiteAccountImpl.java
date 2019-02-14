/**
 * Java implementation of a tradesite account (to be used for currencies etc).
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.account;

import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.site.TradeSite;
import java.math.BigDecimal;


/**
 * This is the implementation for accounts on trade sites.
 */
public class TradeSiteAccountImpl extends AccountImpl implements TradeSiteAccount {

    // Static variables


    // Instance variables

    /**
     * The trade site, this account is on.
     */
    private TradeSite _tradeSite = null;


    // Constructors


    /**
     * Create a new trade site account object.
     *
     * @param balance The balance as nano-coins, or so.
     * @param currency The used currency.
     * @param tradeSite The trade site, this account is on.
     */
    public TradeSiteAccountImpl( BigDecimal balance, Currency currency, TradeSite tradeSite) {

	super( balance, currency);

	_tradeSite = tradeSite;  // Store the trade site in the object.
    }


    // Methods

    /**
     * Get the trade site, this account is on.
     *
     * @return The trade site, this account is on.
     */
    public TradeSite getTradeSite() {
	return _tradeSite;
    }
}
