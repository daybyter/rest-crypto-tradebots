/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.util.TimeFormatException;
import de.andreas_rueckert.util.TimeUtils;


/**
 * Base class for *MA crossing rules.
 */
class MACrossingRule extends RuleImpl {

    // Inner classes

    /**
     * The state of the short *MA vs the longer one.
     */
    public enum MAState { HIGHER, LOWER, UNDEFINED }
    

    // Static variables


    // Instance variables

    /**
     * The traded currency pair.
     */
    protected CurrencyPair _currencyPair = null;

    /**
     * The interval of the longer time period as a string.
     */
    protected String _longTimePeriod = null;

    /**
     * The default state of the short *MA is undefined, so we don't trigger
     * a trade by accident.
     */
    protected MAState _shortMAState = MAState.UNDEFINED;

    /**
     * The interval of the short MA time period as a string.
     */
    protected String _shortTimePeriod = null;

    /**
     * The exchange, we trade on.
     */
    protected TradeSite _tradeSite;


    // Constructors

    /**
     * Create a new MA crossing rule.
     *
     * @param bot The hosting bot.
     * @param strategy The strategy, this rule belongs to.
     * @param tradeSite The exchange we trade on.
     * @param currencyPair The traded currency pair.
     * @param userAccount The account of the trading user.
     * @param shortTimePeriod The short time period as a string (10d, or so).
     * @param longTimePeriod The long time period as a string (21d, or so).
     *
     * @throws TimeFormatException if the time could not be parsed.
     */
    MACrossingRule( SpreadBot bot
		    , Strategy strategy
		    , TradeSite tradeSite
		    , CurrencyPair currencyPair
		    , TradeSiteUserAccount userAccount
		    , String shortTimePeriod
		    , String longTimePeriod) {

	super( bot, strategy, userAccount);

	_shortTimePeriod = shortTimePeriod;
	_longTimePeriod = longTimePeriod;
    }


    // Methods
}
