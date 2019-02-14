/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.bot.LogLevel;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.order.OrderFactory;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import java.math.BigDecimal;
import java.math.MathContext;


/**
 * Rule to check, if we should buy btc in a balance strategy.
 */
class BalanceBuyRule extends MarginTradeRule {

    // Variables

    /**
     * Cache the buy amount for the actual buy action.
     */
    private Amount _buyAmount = null;

    /**
     * Cache the price to buy for.
     */
    private Price _buyPrice = null;


    // Constructors

    /**
     * Create a new BuyRule instance.
     *
     * @param bot The hosting bot.
     * @param strategy The strategy, this rule belongs to.
     * @param tradeSite The exchange we trade on.
     * @param currencyPair The traded currency pair.
     * @param userAccount The account of the trading user.
     * @param margin The profit margin for this rule.
     */
    BalanceBuyRule( SpreadBot bot, Strategy strategy, TradeSite tradeSite, CurrencyPair currencyPair, TradeSiteUserAccount userAccount, float margin) {

	super( bot, strategy, tradeSite, currencyPair, userAccount, margin);
    }


    // Methods

    /**
     * Check if the buy rule is filled.
     *
     * @return true, if the buy condition is filled.
     */
    public boolean isConditionFilled() {

	setLastEvaluationTime();  // Store the execution time of this check.

	// If the value of the currency balance drops x % under the fiat value, buy <currency>.

	// Get the amount of the currency.
	Amount currencyAmount = getBot().getFunds( _tradeSite, _userAccount, _currencyPair.getCurrency());

	// Get the amount of the payment currency.
	Amount paymentCurrencyAmount = getBot().getFunds( _tradeSite, _userAccount, _currencyPair.getPaymentCurrency());

	// Get the best sell price, that is currently available.
	Depth currentDepth = ChartProvider.getInstance().getDepth( _tradeSite, _currencyPair);

	// The first sell order has the best price.
	// ToDo: consider volume here and use the first n orders.
	DepthOrder sellOrder = currentDepth.getSell( 0);

	// The total value of the currency fund is amount * sell.price
	Amount totalCurrencyValue = new Amount( currencyAmount.multiply( sellOrder.getPrice()));

	// Calculate virtual values based on the current leverage.
	//Amount virtualPaymentCurrencyAmount = new Amount( paymentCurrencyAmount.multiply( new BigDecimal( getStrategy().getLeverage())));
	//Amount virtualTotalCurrencyValue = new Amount( totalCurrencyValue.multiply( new BigDecimal( getStrategy().getLeverage())));

	// If the value is <margin> % less than the payment currency value, start buying.
	if( totalCurrencyValue.compareTo( paymentCurrencyAmount.percent( 100 - _margin)) < 0) {
	    //if( virtualTotalCurrencyValue.compareTo( virtualPaymentCurrencyAmount.percent( 100 - _margin)) < 0) {

	    // Compute the amount of <currency> to buy.
	    // Close the gap by buying half of the spread.
	    // Store the amount in the rule.
	    _buyAmount = new Amount( paymentCurrencyAmount.subtract( totalCurrencyValue).divide( sellOrder.getPrice(), MathContext.DECIMAL128).divide( new BigDecimal( "2")));

	    // Also store the target price for the buy.
	    _buyPrice = sellOrder.getPrice();

	    // Check, if we have enough money left (leverage!!!) after this order.
	    if( _buyAmount.multiply( _buyPrice).multiply( new BigDecimal( "2")).compareTo( paymentCurrencyAmount) < 0) {
		
		// Check if the amount is bigger than the minimum trade amount.
		if( _buyAmount.compareTo( getBot().getMinimumTradeAmount( _currencyPair.getCurrency())) > 0) {
		    
		    // Log some info, that this rule triggered.
		    logEvent( LogLevel.DEBUG, "rule for buying " + _currencyPair.getCurrency().getCode() + " triggered");
		    
		    return true;  // This rule is triggered.-

		} else {

		    // Log the created order.
		    logEvent( LogLevel.DEBUG, "buy rule triggered, but amount is not sufficient: " 
			      + _buyAmount 
			      + " " 
			      + _currencyPair.getCurrency().getCode());
		}

	    } else {

		// Log some info, that the leverage is too high.
		logEvent( LogLevel.INFO, "rule for buying finds leverage too high. Trying to reduce.");	

		if( getStrategy().getLeverage() > 2.0) {

		    getStrategy().setLeverage( getStrategy().getLeverage() - 1.0);
		}
	    }
	}

	return false;  // This rule did not trigger.
    }

    /**
     * The rule body.
     */
    public void executeBody() {

	// Just check, if we are in simulation mode and push an order, if not...
	if( ! isSimulation()) {

	    // Create a buy order.
	    String orderId = CryptoCoinOrderBook.getInstance().add( OrderFactory.createCryptoCoinTradeOrder( _tradeSite
													     , _userAccount
													     , OrderType.BUY
													     , _buyPrice
													     , _currencyPair
													     , _buyAmount));

	    // Execute the order.
	    CryptoCoinOrderBook.getInstance().executeOrder( orderId);

	    // Log the created order.
	    logEvent( LogLevel.INFO, "added order to buy " 
		      + _buyAmount + " " + _currencyPair.getCurrency().getCode() 
		      + " for price " 
		      + _buyPrice + " " + _currencyPair.getPaymentCurrency().getCode() 
		      + " per coin to the order book");

	} else {

	    logEvent( LogLevel.DEBUG, "not executing buy order due to simulation mode.");
	}
    }
}
