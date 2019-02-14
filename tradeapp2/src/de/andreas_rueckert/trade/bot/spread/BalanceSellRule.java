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
 * Rule to check, if we should sell btc in a balance strategy.
 */
class BalanceSellRule extends MarginTradeRule {

    // Variables

    /**
     * Cache the sell amount for the actual sell action.
     */
    private Amount _sellAmount = null;

    /**
     * Cache the price to sell for.
     */
    private Price _sellPrice = null;


    // Constructors

    /**
     * Create a new SellRule instance.
     *
     * @param bot The hosting bot.
     * @param strategy The strategy, this rule belongs to.
     * @param tradeSite The exchange we trade on.
     * @param currencyPair The traded currency pair.
     * @param userAccount The account of the trading user.
     * @param margin The profit margin for this rule.
     */
    BalanceSellRule( SpreadBot bot, Strategy strategy, TradeSite tradeSite, CurrencyPair currencyPair, TradeSiteUserAccount userAccount, float margin) {

	super( bot, strategy , tradeSite, currencyPair, userAccount, margin);
    }


    // Methods

    /**
     * Check if the sell rule is filled.
     *
     * @return true, if the sell condition is filled.
     */
    public boolean isConditionFilled() {

	setLastEvaluationTime();  // Store the execution time of this check.

	// If the value of the currency balance rises x % above the fiat value, sell <currency>.

	// Get the amount of the currency.
	Amount currencyAmount = getBot().getFunds( _tradeSite, _userAccount, _currencyPair.getCurrency());

	// Get the amount of the payment currency.
	Amount paymentCurrencyAmount = getBot().getFunds( _tradeSite, _userAccount, _currencyPair.getPaymentCurrency());

	// Get the best buy price, that is currently available.
	Depth currentDepth = ChartProvider.getInstance().getDepth( _tradeSite, _currencyPair);

	// The first buy order has the best price.
	// ToDo: consider volume here and use the first n orders.
	DepthOrder buyOrder = currentDepth.getBuy( 0);

	// The total value of the currency fund is amount * buy.price
	Amount totalCurrencyValue = new Amount( currencyAmount.multiply( buyOrder.getPrice()));

	// Calculate virtual values based on the current leverage.
	//Amount virtualPaymentCurrencyAmount = new Amount( paymentCurrencyAmount.multiply( new BigDecimal( getStrategy().getLeverage())));
	//Amount virtualTotalCurrencyValue = new Amount( totalCurrencyValue.multiply( new BigDecimal( getStrategy().getLeverage())));

	// If the value is <margin> % higher than the payment currency value, start selling.
	if( totalCurrencyValue.compareTo( paymentCurrencyAmount.percent( 100 + _margin)) > 0) {
	    //if( virtualTotalCurrencyValue.compareTo( virtualPaymentCurrencyAmount.percent( 100 + _margin)) > 0) {

	    // Compute the amount of <currency> to sell.
	    // Close the gap by buying half of the spread.
	    // Store the amount in the rule.
	    _sellAmount = new Amount( totalCurrencyValue.subtract( paymentCurrencyAmount).divide( buyOrder.getPrice(), MathContext.DECIMAL128).divide( new BigDecimal( "2")));

	    // Check, if we have enough money left (leverage!!!) after this order.
	    if( _sellAmount.multiply( new BigDecimal( "2")).compareTo( currencyAmount) < 0) {

		// Check if the amount is bigger than the minimum trade amount.
		if( _sellAmount.compareTo( getBot().getMinimumTradeAmount( _currencyPair.getCurrency())) > 0) {

		    // Also store the target price for the buy.
		    _sellPrice = buyOrder.getPrice();

		    // Log some info, that this rule triggered.
		    logEvent( LogLevel.DEBUG, "Rule for selling " + _currencyPair.getCurrency().getCode() + " triggered");
		    
		    return true;  // This rule is triggered.

		} else {
		    
		    // Log some info, that the trade amount is not sufficient.
		    logEvent( LogLevel.DEBUG, "Sell rule triggered, but amount is not sufficient: " 
			      + _sellAmount 
			      + " " 
			      + _currencyPair.getCurrency());
		}
	    } else {

		// Log some info, that the leverage is too high.
		logEvent( LogLevel.INFO, "rule for selling finds leverage too high. Trying to reduce.");	

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
													     , OrderType.SELL
													     , _sellPrice
													     , _currencyPair
													     , _sellAmount));

	    // Execute the order.
	    CryptoCoinOrderBook.getInstance().executeOrder( orderId);

	    // Log the created order.
	    logEvent( LogLevel.INFO, "added order to sell " 
		      + _sellAmount + " " + _currencyPair.getCurrency().getCode()
		      + " for price "
		      + _sellPrice + " " + _currencyPair.getPaymentCurrency().getCode()
		      + " per coin to the order book");

	} else {

	    // Log some info, since we are in simulation mode.
	    logEvent( LogLevel.DEBUG, "Not executing sell order due to simulation mode.");
	}
    }
}
