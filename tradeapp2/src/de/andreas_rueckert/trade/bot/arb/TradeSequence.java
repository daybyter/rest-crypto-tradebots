/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.TimeUtils;
import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a sequence of trades.
 */
public class TradeSequence {
    
    // Static variables


    // Instance variables 

    /**
     * Flag to indicate an active or deactivated trade sequence.
     */
    private boolean _active = true;
    
    /**
     * The timestamp of the last calculation.
     */
    private long _lastCalculationTimestamp = -1L;

    /**
     * The potential amount of the sequence.
     */
    private Amount _tradeAmount = null;

    /**
     * Input for the relative profitability.
     */
    private Price _tradeIndicatorInput;

    /**
     * Output for the relative profitability.
     */
    private Price _tradeIndicatorOutput;

    /**
     * The profit with the amount as input.
     */
    private Amount _tradeProfit;

    /**
     * The list of point in a trade sequence.
     */
    private List<TradePoint> _tradeSequence = new ArrayList<TradePoint>();

    /**
     * The trade site, this sequence is for.
     */
    private TradeSite _tradeSite = null;
    

    // Constructors

    /**
     * Create a new trade sequence for a given trade site.
     *
     * @param tradeSite The trade site, this sequence is for.
     */
    public TradeSequence( TradeSite tradeSite) {

	_tradeSite = tradeSite;
    }


    // Methods

    /**
     * Activate this trade sequence for trading.
     */
    public void activate() {

	_active = true;
    }
    
    /**
     * Add a new trade point to this sequence.
     *
     * @param tradePoint The new trade point to add.
     */
    public final void addTradePoint( TradePoint tradePoint) {

	_tradeSequence.add( tradePoint);  // Add this point to the list of trade points.
    }

    /**
     * Clone this trade sequence into a new sequence.
     *
     * @return The cloned trade sequence as a new TradeSequence object.
     */
    public TradeSequence clone() {

	// Create a new object.
	TradeSequence result = new TradeSequence( _tradeSite);

	// Copy all the points from this sequence to the new sequence.
	for( TradePoint currentPoint : _tradeSequence) {
	    result.addTradePoint( currentPoint);
	}

	return result;  // Return the cloned sequence.
    }

    /**
     * Convert the sequence with the trade data into a string.
     *
     * @return The trade sequence with the profit and the trade amount as a string.
     */
    public final String completeDataToString() {

	return toString() 
	    + " Amount: " + getTradeAmount()
	    + " Profit: " + getTradeProfit();
    }

    /**
     * Check, whether this trade sequence contains a given trade point.
     *
     * @param tradePoint The trade point to check,
     *
     * @return true, if the trade sequence contains the given trade point.
     */
    public boolean contains( TradePoint tradePoint) {

	// Just loop over the trade points
	for( TradePoint currentPoint : _tradeSequence) {  
	    if( currentPoint.equals( tradePoint)) {  // If the current point equals the given point,
		return true;                         // return true.
	    }
	}
	
	return false;  // Trade point not found in this list.
    }

    /**
     * Check if this sequence contains a currency other than at the start.
     *
     * @param currency The currency to look for.
     *
     * @return true, if the sequence contains the currency and it's not at the start of the sequence.
     */
    public boolean containsCurrencyAfterStart( Currency currency) {

	// Loop over all the trade points.
	for( int currentPointIndex = 0; currentPointIndex < _tradeSequence.size(); ++currentPointIndex) {

	    // Get the current trade point.
	    TradePoint currentTradePoint = _tradeSequence.get( currentPointIndex);
	
	    if( currentPointIndex == 0) {  // If this is the start point.

		// Just check if our currency is the result of the trade point.
		if( currentTradePoint.isResultingCurrency( currency)) {

		    return true;
		}

	    } else {  // For the other points.

		// Just check, if one of the currencies equals the given currency.
		if( currentTradePoint.containsCurrency( currency)) {

		    return true;
		}
	    }
		
	}

	return false;  // Currency not found in the sequence.
    }

    /**
     * Check, whether this trade sequence contains a given trade point, ignoring the buy flag.
     *
     * @param tradePoint The trade point to check,
     *
     * @return true, if the trade sequence contains the given trade point ignoring the buy flag.
     */
    public boolean containsIgnoreBuy( TradePoint tradePoint) {

	// Just loop over the trade points
	for( TradePoint currentPoint : _tradeSequence) {  
	    if( currentPoint.equalsIgnoreBuy( tradePoint)) {  // If the current point equals the given point, ignoring the buy flag,
		return true;                                  // return true.
	    }
	}
	
	return false;  // Trade point not found in this list.
    }

    /**
     * Check, if this trade sequence trade a given currency pair on a given trade site.
     *
     * @param tradeSite The trade site to check for.
     * @param currencyPair The currency pair to check for.
     */
    public boolean containsTradeSiteCurrencyPair( TradeSite tradeSite, CurrencyPair currencyPair) {

	// If there is a trade site given for this sequence and it equals
	// the trade site parameter
	if( ( getTradeSite() != null) && getTradeSite().equals( tradeSite)) {

	    // Check all the currency pairs for the given one.
	    for( TradePoint currentTradePoint : _tradeSequence) {

		// If current trade point trades the given currency pair.
		if( currentTradePoint.getTradedCurrencyPair().equals( currencyPair)) {

		    return true;  // Found the pair => return true !
		}
	    }
	}
	 

	return false;
    }

    /**
     * Deactivate this trade sequence.
     */
    public void deactivate() {

	_active = false;
    }

    /**
     * Get the GMT relative timestamp of the last calculation as microseconds.
     *
     * @return The GMT relative timestamp of the last calculation as microseconds, 
     *         or -1L, if the sequence was never calculated.
     */
    public final long getLastCalculationTimestamp() {

	return _lastCalculationTimestamp;
    }

    /**
     * Get the currency, that this trade sequence starts with (or null).
     *
     * @return The currency, that this trade sequence starts with (or null, if there are no trade points yet).
     */
    public final Currency getStartingCurrency() {

	// Get the first trade point.
	TradePoint startingPoint = getTradePoint( 0);

	if( startingPoint == null) {  // If there are no points yet,

	    return null;  // return null.
	}

	// Get the traded currency pair at this trade point.
	CurrencyPair tradedPair = startingPoint.getTradedCurrencyPair();

	// Return the currency, that we start with.
	return startingPoint.isBuy() ? tradedPair.getPaymentCurrency() : tradedPair.getCurrency();
    }

    /**
     * Get the currency pair, that this trade sequence starts with (or null).
     *
     * @return The currency pair, that this trade sequence starts with (or null, if there are no trade points yet).
     */
    public final CurrencyPair getStartingCurrencyPair() {

	// Get the first trade point.
	TradePoint startingPoint = getTradePoint( 0);

	if( startingPoint == null) {  // If there are no points yet,

	    return null;  // return null.
	}

	// Return the traded currency pair at this trade point.
	return startingPoint.getTradedCurrencyPair();
    }

    /**
     * Get the calculated amount for this trade sequence.
     *
     * @return The calculated amount for this trade sequence.
     */
    public Amount getTradeAmount() {
	return _tradeAmount;
    }
    
    /**
     * Get the calculated indicator input for this trade sequence.
     *
     * @return The calculated input of the indicator.
     */
    public Price getTradeIndicatorInput() {
	return _tradeIndicatorInput;
    }

    /**
     * Get the calculated indicator output for this trade sequence.
     *
     * @return  The calculated output of the indicator.
     */
    public Price getTradeIndicatorOutput() {
	return _tradeIndicatorOutput;
    }

    /**
     * Get a trade point with a given index.
     *
     * @param index The index of the trade point.
     *
     * @return The trade point with the given index.
     */
    public final TradePoint getTradePoint( int index) {
	return _tradeSequence.get( index);
    }

    /**
     * Get the calculated profit for this trade sequence.
     *
     * @return The calculated profit for this trade sequence.
     */
    public Amount getTradeProfit() {
	return _tradeProfit;
    }

    /**
     * Get the trade site, this sequence is for.
     *
     * @return The trade site, this sequence is for.
     */
    public TradeSite getTradeSite() {
	
	return _tradeSite;
    }

    /**
     * Get a list of all the currencies, that are used in thie sequence.
     *
     * @return A list of all the currencies, that are used in this sequence.
     */
    public List<Currency> getUsedCurrencies() {

	// Create a buffer for the result.
	List<Currency> result = new ArrayList<Currency>();

	// Loop over all the trade points
	for( TradePoint currentPoint : _tradeSequence) {

	    // Get the currency pair, that is traded at this point. 
	    CurrencyPair currentPair = currentPoint.getTradedCurrencyPair();

	    // Check both currencies, if they are already in the list and add them, if not.
	    if( ! result.contains( currentPair.getCurrency())) {

		result.add( currentPair.getCurrency());
	    }
	    if( ! result.contains( currentPair.getPaymentCurrency())) {

		result.add( currentPair.getPaymentCurrency());
	    }
	}

	return result;  // Return the resulting list.
    }

    /**
     * Check, if this trade sequence is active (used for trading).
     *
     * @return true, if this trade sequence is active (used for trading). False otherwise.
     */
    public final boolean isActive() {

	return _active;  // Just return the active flag.
    }

    /**
     * Reverse a this trade sequence and return the reversed sequence as a new trade sequence.
     *
     * @return The reversed trade sequence as a new object.
     */
    public final TradeSequence reverse() {
	
	TradeSequence result = new TradeSequence( _tradeSite);  // Create a new trade sequence.

	// Just loop backwards over the given sequence and reverse the order.
	for( int index = size() - 1; index >= 0; --index) {

	    TradePoint newPoint = getTradePoint( index).clone();

	    newPoint.setBuy( ! newPoint.isBuy());  // Reverse the buy/sell order.

	    result.addTradePoint( newPoint);  // Add this point to the new sequence.
	}

	return result;
    }

    /**
     * Set a new activated status for this sequence.
     *
     * @param active The new activate status for this sequence.
     */
    public final void setActive( boolean active) {

	_active = active;
    }

    /**
     * Set the timestamp of the last calculation to now.
     */
    public final void setLastCalculationTimestamp() {

	// Store the current timestamp in the sequence.
	_lastCalculationTimestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros();
    }

    /**
     * Set the calculated amount for this trade sequence.
     *
     * @param amount The calculated amount for this trade sequence.
     */
    public void setTradeAmount( Amount amount) {
	_tradeAmount = amount;
    }
    
    /**
     * Set the input for the trade indicator calculation.
     *
     * @param input The new input for the trade indicator calculation.
     */
    public void setTradeIndicatorInput( Price input) {
	_tradeIndicatorInput = input;
    }

    /**
     * Set the calculated indicator output for this trade sequence.
     *
     * @param output The calculated output of the indicator.
     */
    public void setTradeIndicatorOutput( Price output) {
	_tradeIndicatorOutput = output;
    }

    /**
     * Set the calculated profit for this trade sequence.
     *
     * @param profit The calculated profit for this trade sequence.
     */
    public void setTradeProfit( Amount profit) {
	_tradeProfit = profit;
    }
    
    /**
     * Get the size (length) of this trade sequence.
     *
     * @return The size of this trade sequence.
     */
    public final int size() {
	return _tradeSequence.size();
    }

    /**
     * Convert this trade sequence into a very rough string format.
     *
     * @return This trade sequence as a very rough string.
     */
    public final String toString() {

	StringBuffer resultBuffer = new StringBuffer();

	// Start with the name of the trade site.
	resultBuffer.append( getTradeSite().getName());
	resultBuffer.append( " : ");

	// For now just a rough sequence of the currencies
	for( int index = 0; index < _tradeSequence.size(); ++index) {

	    TradePoint currentPoint = getTradePoint( index);
	    CurrencyPair currencyPair = currentPoint.getTradedCurrencyPair();

	    if( index == 0) {  // If this is the first trade point, we have to display both currencies
		if( currentPoint.isBuy()) {
		    resultBuffer.append( currencyPair.getPaymentCurrency().toString());
		    resultBuffer.append( " => ");
		    resultBuffer.append( currencyPair.getCurrency().toString());
		} else {
		    resultBuffer.append( currencyPair.getCurrency().toString());
		    resultBuffer.append( " => ");
		    resultBuffer.append( currencyPair.getPaymentCurrency().toString());
		}
	    } else {
		resultBuffer.append( " => ");
		resultBuffer.append( currentPoint.isBuy() ? currencyPair.getCurrency().toString() : currencyPair.getPaymentCurrency().toString());
	    }
	}

	return resultBuffer.toString();
    }
}
