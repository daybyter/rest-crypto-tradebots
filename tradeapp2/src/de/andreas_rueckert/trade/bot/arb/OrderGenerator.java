/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.OrderFactory;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.util.LogUtils;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;


/**
 * This class generates orders for trade sequences.
 */
class OrderGenerator {

    // Inner classes


    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static OrderGenerator _instance = null;


    // Instance variables

    /**
     * The hosting bot.
     */
    private ArbBot _bot;


    // Constructors
    
    /**
     * Private constructor for singleton pattern.
     *
     * @param bot The hosting arbitrage bot.
     */
    private OrderGenerator( ArbBot bot) {

	_bot = bot;
    }


    // Methods

    /**
     * Create orders for a given trade sequence.
     *
     * @param tradeSequence The trade sequence.
     * @param safety Some safety percentage to subtract from the max trading amount. Just for rounding problems etc.
     * @param addDependencies Add dependencies between orders.
     * @param userAccount The user account on the exchange to use for this order.
     *
     * @return The list of generated orders, or null if it's not possible to create them.
     */
    List<SiteOrder> generateOrders( TradeSequence tradeSequence
				    , double safety
				    , boolean addDependencies
				    , TradeSiteUserAccount userAccount) {

	// Check, if the sequence is active.
	if( ! tradeSequence.isActive()) {

	    LogUtils.getInstance().getLogger().error( "OrderGenerator: trade sequence " 
						      + tradeSequence.toString() 
						      + "is not active, so I cannot generate orders");

	    return null;  // Do not generate any orders.
	} 

	// Check, was there is already a trade amount computed for this sequence.
	// There are several conditions possible for an unprofitable sequence, so I have to check them all... :-(
	Amount tradeAmount = tradeSequence.getTradeAmount();
	if( ( tradeAmount == null) || ( tradeAmount.compareTo( Amount.ZERO) <= 0)) {

	    LogUtils.getInstance().getLogger().error( "OrderGenerator: trade sequence "
						      + tradeSequence.toString()
						      + " not calculated or not profitable.");

	    return null;  // Do not generate any orders.
	}
	
	// Create a buffer for the result.
	List<SiteOrder> resultBuffer = new ArrayList<SiteOrder>();

	// Set the current trade amount and subtract the safety here.
	Amount currentAmount = new Amount( tradeAmount.multiply( new BigDecimal( 1 - safety / 100)));

	// Loop over the trade points of a sequence and create an order for each trade.
	for( int index = 0; index < tradeSequence.size(); ++index) {

	    // Get the current tradepoint.
	    TradePoint currentTradePoint = tradeSequence.getTradePoint( index);

	    // Get a price for the current amount.
	    Depth currentDepth = getBot().getDepthFromCache( currentTradePoint.getTradeSite(), currentTradePoint.getTradedCurrencyPair());

	    // Get a price for the current amount.
	    // If this point is a buy, sum up the sell orders and vice versa.
	    Price currentPrice = currentDepth.getPriceForAmount( currentAmount, ! currentTradePoint.isBuy());

	    // Create an order for this point.
	    SiteOrder newOrder = OrderFactory.createCryptoCoinTradeOrder( tradeSequence.getTradeSite()
									  , userAccount
									  , currentTradePoint.isBuy() ? OrderType.BUY : OrderType.SELL
									  , currentPrice
									  , currentTradePoint.getTradedCurrencyPair()
									  , currentAmount);

	    // If the user wants order dependencies, create them.
	    // Just let this order depend on the previous one.
	    if( addDependencies && ( resultBuffer.size() > 0)) {

		// Let this order depend on the previous order.
		newOrder.addDependency( resultBuffer.get( resultBuffer.size() - 1));
	    }
	    
	    // Add the order to the result
	    resultBuffer.add( newOrder);

	    // Get fee for this order.
	    Price currentFee = tradeSequence.getTradeSite().getFeeForOrder( newOrder);

	    // Check if the fee is actually returned and has the currect currency
	    if( ( currentFee == null) || ( currentFee.getCurrency() != currentTradePoint.getTradedCurrencyPair().getPaymentCurrency())) {

		LogUtils.getInstance().getLogger().error( "Returned fee for order " 
							  + newOrder.toString()
							  + " is null or has wrong currency." );

		return null;  // Stop oder generating.
	    }

	    // Convert the amount to the new currency.
	    currentAmount = new Amount( currentTradePoint.isBuy() 
					? currentAmount.divide( currentPrice, MathContext.DECIMAL128)
					: currentAmount.multiply( currentPrice));

	    // Subtract fee.
	    currentAmount = new Amount( currentAmount.subtract( currentFee));

	    // If the amount gets negative, something is seriously wrong, so stop the order generating.
	    if( currentAmount.signum() == -1) {
		
		LogUtils.getInstance().getLogger().error( "OrderGenerator: amount for tradesequence " 
							  + tradeSequence.toString()
							  +  "  got negative during order generating");

		return null;
	    }

	    
	}

	return null;  // Creating the orders failed.
    }

    /**
     * Get the hosting bot.
     *
     * @return The hosting bot.
     */
    ArbBot getBot() {

	return _bot;
    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @param bot The hosting aritrage bot.
     *
     * @return The only instance of this class.
     */
    public final static OrderGenerator getInstance( ArbBot bot) {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new OrderGenerator( bot);  // create one.
	}

	return _instance;  // And return it.
    }
}