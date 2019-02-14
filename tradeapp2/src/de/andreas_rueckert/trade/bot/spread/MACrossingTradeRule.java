/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.bot.LogLevel;
import de.andreas_rueckert.trade.chart.ChartAnalyzer;
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
import java.math.MathContext;


/**
 * Class to trigger a trade in a moving average crossing strategy.
 */
public class MACrossingTradeRule extends MACrossingRule {

    // Static variables


    // Instance variables

    /**
     * Flag to indicate, that a buy order was triggered.
     */
    boolean _buyTriggered;

    /**
     * Flag to indicate, that a sell order was triggered.
     */
    boolean _sellTriggered;


    // Constructors

    /**
     * Create a trade rule for a moving average crossing strategy.
     *
     * @param bot The hosting bot.
     * @param strategy The strategy, this rule belongs to.
     * @param tradeSite The exchange we trade on.
     * @param currencyPair The traded currency pair.
     * @param userAccount The account of the trading user.
     * @param shortTimePeriod The short time period as a string (10d, or so).
     * @param longTimePeriod The long time period as a string (21d, or so).
     */
    public MACrossingTradeRule(  SpreadBot bot
				 , Strategy strategy
				 , TradeSite tradeSite
				 , CurrencyPair currencyPair
				 , TradeSiteUserAccount userAccount
				 , String shortTimePeriod
				 , String longTimePeriod) {
	
	super( bot, strategy, tradeSite, currencyPair, userAccount, shortTimePeriod, longTimePeriod);
    }


    // Methods

      /**
     * Check if the buy rule is filled.
     *
     * @return true, if the buy condition is filled.
     */
    public boolean isConditionFilled() {

	setLastEvaluationTime();  // Store the execution time of this check.

	// Init the result flags.
	_buyTriggered = _sellTriggered = false;

	// Calculate the 2 MA time periods.
	
	Price shortPrice = ChartAnalyzer.getInstance().getEMA( _tradeSite, _currencyPair, _shortTimePeriod);  // Calculate the ema.
	
	Price longPrice = ChartAnalyzer.getInstance().getEMA( _tradeSite, _currencyPair, _longTimePeriod);  // Calculate the long ema.
	 
	if( shortPrice.compareTo( longPrice) != 0) {  // Ignore the situation, when the 2 prices are equal and just focus on a crossing.
	
	    // Set the new state of the short MA vs the longer MA.
	    MAState newShortMAState = ( shortPrice.compareTo( longPrice) > 0) ? MAState.HIGHER : MAState.LOWER;

	    // @see: https://bitcointalk.org/index.php?topic=60501.msg3265411#msg3265411
	    // ... When the 10 crosses over the 21, I buy.  When the 10 crosses under the 21, I sell or sell short.
	    // So trigger and actual buy only, if the short values crosses the longer interval!
	    
	    // It crossed, if the old state is the opposite of the new one.
	    // _shortMAState holds the last MA state. It might be undefined, so we have to check, if it
	    // is actually the other state.

	    // This should trigger a buy:
	    if( ( _shortMAState == MAState.LOWER) && ( newShortMAState == MAState.HIGHER)) {

		// Now check, if we have enough funds to trade.
		
		// To do so, get the best sell price and use the payment currency funds to 
		// calculate the amount of <currency>, that we could buy.
		Depth currentDepth = ChartProvider.getInstance().getDepth( _tradeSite, _currencyPair);

		// The first sell order has the best price.
		// ToDo: consider volume here and use the first n orders.
		DepthOrder sellOrder = currentDepth.getSell( 0);

		// ToDo: check, if this orders actually exists. OrderBook might be empty?
		Price bestPrice = sellOrder.getPrice();  // Get the best price from this order.

		// Get the amount of the payment currency.
		Amount paymentCurrencyAmount = getBot().getFunds( _tradeSite, _userAccount, _currencyPair.getPaymentCurrency());

		// Calculate the amount, that we can buy with our current funds.
		Amount buyAmount = new Amount( paymentCurrencyAmount.divide( bestPrice, MathContext.DECIMAL128));

		// If this amount is bigger than the minimum amount, trigger a buy order.
		if( buyAmount.compareTo( getBot().getMinimumTradeAmount( _currencyPair.getCurrency())) > 0) {

		    _buyTriggered = true;
		}
	    }

	    // This should trigger a sell
	    if( ( _shortMAState == MAState.HIGHER) && ( newShortMAState == MAState.LOWER)) {

		// Now check if we have any funds to trade.

		// Get the amount of the currency.
		Amount currencyAmount = getBot().getFunds( _tradeSite, _userAccount, _currencyPair.getCurrency());

		// Check, if the amount is higher than the minimum amount.
		if( currencyAmount.compareTo( getBot().getMinimumTradeAmount( _currencyPair.getCurrency())) > 0) {
		    
		    // Store a flag, that a sell was triggered.
		    _sellTriggered = true;


		}
	    } 

	    _shortMAState = newShortMAState;  // Set the current state of the 2 MA's.

	    // return true, if a buy or sell was triggerd.
	    return ( _buyTriggered || _sellTriggered);
	}

	return false;   // If the *MA's are equal, don't do anything.
    }

    /**
     * The rule body.
     */
    public void executeBody() {

	if( _buyTriggered) {  // If a buy order was triggered.
	
	    // Calculate the amount, that we can buy.
    
	    // Get the best buy price, that is currently available.
	    // We could actually just use the best sell price and sell a bit cheaper, using the spread.
	    // But I want to make sure, that the sell orders goes through, since I already
	    // ignore the volume for now.
	    // To make things perfect, the orders should be added up, until the traded volume is reached.
	    Depth currentDepth = ChartProvider.getInstance().getDepth( _tradeSite, _currencyPair);

	    // The first sell order has the best price.
	    // ToDo: consider volume here and use the first n orders.
	    // ToDo: check, if this orders actually exists. OrderBook might be empty?
	    Price buyPrice = currentDepth.getSell( 0).getPrice();  // Get the best price from this order.

	    // This is still rough, so I just trade all my funds for now...

	    // Get the amount of the payment currency.
	    Amount paymentCurrencyAmount = getBot().getFunds( _tradeSite, _userAccount, _currencyPair.getPaymentCurrency());

	    // Calculate the amount, that we can buy with our current funds.
	    Amount buyAmount = new Amount( paymentCurrencyAmount.divide( buyPrice, MathContext.DECIMAL128));

	    // Just check, if we are in simulation mode and push an order, if not...
	    if( ! isSimulation()) {
		
		// Create a buy order.
		String orderId = CryptoCoinOrderBook.getInstance().add( OrderFactory.createCryptoCoinTradeOrder( _tradeSite
														 , _userAccount
														 , OrderType.BUY
														 , buyPrice
														 , _currencyPair
														 , buyAmount));

		// Execute the order.
		CryptoCoinOrderBook.getInstance().executeOrder( orderId);
		
		// Log the created order.
		logEvent( LogLevel.INFO, "added order to buy " 
			  + buyAmount + " " + _currencyPair.getCurrency() 
			  + " for price " 
			  + buyPrice + " " + _currencyPair.getPaymentCurrency() 
		      + " per coin to the order book");
	    } else {
		
		// Add a log entry
		logEvent( LogLevel.INFO, "not executing order to buy " 
			  + buyAmount + " " + _currencyPair.getCurrency() 
			  + " for price " 
			  + buyPrice + " " + _currencyPair.getPaymentCurrency() 
			  + " per coin due to simulation mode");
	    }
	}

	if( _sellTriggered) {   // If a sell order was triggered...

	    // Now fetch all the data to actually create an order.
	    
	    // Get the best buy price, that is currently available.
	    // We could actually just use the best sell price and sell a bit cheaper, using the spread.
	    // But I want to make sure, that the sell orders goes through, since I already
	    // ignore the volume for now.
	    // To make things perfect, the orders should be added up, until the traded volume is reached.
	    Depth currentDepth = ChartProvider.getInstance().getDepth( _tradeSite, _currencyPair);

	    // Get the price of the first buy order.
	    Price sellPrice = currentDepth.getBuy( 0).getPrice();

	    // Get the amount of the currency.
	    Amount sellAmount = getBot().getFunds( _tradeSite, _userAccount, _currencyPair.getCurrency());

	    // Just check, if we are in simulation mode and push an order, if not...
	    if( ! isSimulation()) {
		
		// Create a sell order.
		String orderId = CryptoCoinOrderBook.getInstance().add( OrderFactory.createCryptoCoinTradeOrder( _tradeSite
														 , _userAccount
														 , OrderType.SELL
														 , sellPrice
														 , _currencyPair
														 , sellAmount));

		// Execute the order.
		CryptoCoinOrderBook.getInstance().executeOrder( orderId);
		
		// Log the created order.
		logEvent( LogLevel.INFO, "added order to sell " 
			  + sellAmount + " " + _currencyPair.getCurrency() 
			  + " for price " 
			  + sellPrice + " " + _currencyPair.getPaymentCurrency() 
			  + " per coin to the order book");

	    } else {

		// Add a log entry
		logEvent( LogLevel.INFO, "Not executed order to sell " 
			  + sellAmount + " " + _currencyPair.getCurrency() 
			  + " for price " 
			  + sellPrice + " " + _currencyPair.getPaymentCurrency() 
			  + " per coin due to simulation mode");

	    }
	}
    }
}
