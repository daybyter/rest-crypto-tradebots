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


/**
 * This class is a base class for rules, that trade a currency pair on a given margin.
 */
class MarginTradeRule extends RuleImpl {

    // Variables

    /**
     * The traded currency pair.
     */
    protected CurrencyPair _currencyPair = null;
	
    /**
     * The profit margin for this rule.
     */
    protected float _margin;

    /**
     * The exchange, we trade on.
     */
    protected TradeSite _tradeSite;


    // Constructors

    /**
     * Create a new trade rule for a given bot, a given currency pair, a given user account and a given profit margin.
     *
     * @param bot The hosting bot.
     * @param strategy The strategy, this rule belongs to.
     * @param currencyPair The traded currency pair.
     * @param userAccount The account of the trading user.
     * @param margin The minimum profit margin.
     */
    MarginTradeRule( SpreadBot bot, Strategy strategy, TradeSite tradeSite, CurrencyPair currencyPair, TradeSiteUserAccount userAccount, float margin) {

	super( bot, strategy, userAccount);

	// Store the parameters in the rule instance.
	   
	_tradeSite = tradeSite;

	_currencyPair = currencyPair;
 
	_margin = margin;
    }


    // Methods
}
