/**
 * Java implementation of a trade cache persistence.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.chart.persistence;

import de.andreas_rueckert.persistence.PersistentProperties;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.Trade;


/**
 * Interface to make a trade cache persistent by using
 * some service.
 */
public interface CachePersistence extends PersistentProperties {

    // Static variables


    // Methods

    /**
     * Activate or deactivate the persistence.
     *
     * @param activated If true, the persistence is activated. If false, it's deactivated.
     */
    public void setActive( boolean activated);

    /**
     * Add a new trade to the database.
     *
     * @param trade The new trade to add.
     * 
     * @return true, if the trade was successfully added. false otherwise.
     */
    public boolean add( Trade trade);

    /**
     * Get the timestamp of the newest trade for a given trade site and currency pair.
     *
     * @param tradeSite The trade site to query.
     * @param currencyPair The currency pair to use.
     *
     * @return The gmt-relative timestamp of the newest trade, or -1 if no such trade is in the db.
     */
    public long getNewestTimestamp( TradeSite tradeSite, CurrencyPair currencyPair);
}
