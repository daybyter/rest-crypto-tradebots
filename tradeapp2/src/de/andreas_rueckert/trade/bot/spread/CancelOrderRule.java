/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import de.andreas_rueckert.trade.bot.LogLevel;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.util.TimeUtils;
import java.util.ArrayList;
import java.util.List;


/**
 * Rule to cancel orders, that were not filled.
 */
public class CancelOrderRule extends RuleImpl {

    // Variables

    /**
     * A list of orders to cancel.
     */
    List<String> _ordersToCancel = new ArrayList<String>();


    // Constructors

    /**
     * Create a new CancelOrderRule instance.
     *
     * @param bot The hosting bot.
     * @param strategy The strategy, this rule belongs to.
     * @param userAccount The user account of the orders or null.
     */
    public CancelOrderRule( SpreadBot bot, Strategy strategy, TradeSiteUserAccount userAccount) {

	super( bot, strategy, userAccount);
    }


    // Methods

    /**
     * Check if the buy rule is filled.
     *
     * @return true, if the buy condition is filled.
     */
    public boolean isConditionFilled() {

	setLastEvaluationTime();  // Store the execution time of this check.

	// Remove the results from the previous checks.
	_ordersToCancel.clear();

	// Loop over the orders and check their status, if they are older than x minutes.
	for( SiteOrder currentOrder : getBot().getCurrentOrders()) {

	    // If this order has the current user account, if any was given...
	    if( ( _userAccount == null) 
		|| ( ( currentOrder.getTradeSiteUserAccount() != null) 
		     && _userAccount.equals( currentOrder.getTradeSiteUserAccount()))) {

		// Get the current status of the order.
		OrderStatus currentOrderStatus = CryptoCoinOrderBook.getInstance().checkOrder( currentOrder.getId());

		if( currentOrderStatus == OrderStatus.FILLED) {  // If the order is filled,
		    
		    getBot().removeOrder( currentOrder.getId());  // remove this order from the list of active orders.
		    
		} else {  // If the order is not filled, but too old, tag it for cancelation.
			

		    // If this order is older than 5 mins
		    if( ( TimeUtils.getInstance().getCurrentGMTTimeMicros() - currentOrder.getTimestamp()) > ( 5L * 60L * 1000000L)) {

			_ordersToCancel.add( currentOrder.getId());  // Add this order to the list of orders to cancel.
		    }
		}
	    }
	}

	// If there are orders to cancel, return true,
	return ( _ordersToCancel.size() > 0);
    }

    /**
     * The rule body.
     */
    public void executeBody() {

	// Remove all the filled orders from the list of current orders.
	for( String orderId : _ordersToCancel) {
		
	    if( CryptoCoinOrderBook.getInstance().cancelOrder( orderId)) {  // Try to remove the order
		    
		getBot().removeOrder( orderId);  // Worked!

		logEvent( LogLevel.INFO, "Remove order with ID: " + orderId);
	    }   
	}
    }

    /**
     * Get the user account, that is used by this rule.
     *
     * @return The user account for this rule.
     */
    public TradeSiteUserAccount getTradeSiteUserAccount() {

	return _userAccount;
    }
}
