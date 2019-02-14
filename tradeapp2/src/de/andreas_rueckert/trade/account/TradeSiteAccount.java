/**
 * Java implementation of a tradesite account (to be used for currencies etc).
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.account;

import de.andreas_rueckert.trade.site.TradeSite;


/**
 * This is an interface for accounts on trade sites.
 */
public interface TradeSiteAccount extends Account {

    // Methods

    /**
     * Get the trade site, this account is on.
     *
     * @return The trade site, this account is on.
     */
    public TradeSite getTradeSite();
}