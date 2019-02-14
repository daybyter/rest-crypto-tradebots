/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import de.andreas_rueckert.trade.bot.LogLevel;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;


/**
 * This class implements a emergency stop rule in case the traded market goes belly up...
 */
public class EmergencyStopRule extends RuleImpl {

    // Variables

    /**
     * The traded currency pair.
     */
    protected CurrencyPair _currencyPair = null;

    /**
     * The minimum price to check for.
     */
    protected Price _minimumPrice;
	
    /**
     * The exchange, we trade on.
     */
    protected TradeSite _tradeSite;

	
    // Constructors

    /**
     * Create a new EmergencyStopRule.
     *
     * @param bot The hosting bot.
     * @param strategy The strategy this rule belongs to.
     * @param tradeSite The trade site, we trade on.
     * @param currencyPair The checked currency pair.
     * @param userAccount The account of the trading user.
     * @param minimumPrice The minimum price to check for.
     */
    public EmergencyStopRule( SpreadBot bot, Strategy strategy, TradeSite tradeSite, CurrencyPair currencyPair, TradeSiteUserAccount userAccount, Price minimumPrice) {
	    
	super( bot, strategy, userAccount);

	// Store the parameters in the rule instance.
	   
	_tradeSite = tradeSite;

	_currencyPair = currencyPair;
 
	_minimumPrice = minimumPrice;
    }


    // Methods

    /**
     * Check if the buy rule is filled.
     *
     * @return true, if the buy condition is filled.
     */
    public boolean isConditionFilled() {

	// Get the best buy price, that is currently available.
	Depth currentDepth = ChartProvider.getInstance().getDepth( _tradeSite, _currencyPair);

	// The first buy order has the best price.
	// ToDo: consider volume here and use the first n orders.
	DepthOrder buyOrder = currentDepth.getBuy( 0);

	// Check, if this buy price is lower than our minimum price.
	return ( buyOrder.getPrice().compareTo( _minimumPrice) < 0);
    }

    /**
     * The rule body.
     */
    public void executeBody() {

	// Just deactivate this strategy (and keep the others running. Lets hope, the other markets don't crash as well).
	getStrategy().setActivated( false);

	// Add a log entry, that the strategy was deactivated.
	// Since this is a major problem, log it as an error, so the user should take notice.
	logEvent( LogLevel.ERROR, "Emergency stop of strategy " + getStrategy().getName() + " due to current price < minimum price");
    }
}
