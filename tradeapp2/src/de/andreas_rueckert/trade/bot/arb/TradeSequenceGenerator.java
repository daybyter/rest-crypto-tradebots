/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb;

import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class encapsulates the methods to generate trade sequences 
 * in an efficient way (like multi-threaded methods).
 */
class TradeSequenceGenerator {

    // Inner classes

    /**
     * Create a class to distribute the currency pairs to the generator threads.
     */
    class CurrencyPairDistributor {

	// Instance variables

	/**
	 * All the currency pairs to distribute.
	 */
	CurrencyPair [] _currencyPairs;

	/**
	 * The current index of the distributed pairs.
	 */
	int _currentIndex = 0;
	

	// Constructors

	/**
	 * Create a new distributor object for a given list of pairs.
	 *
	 * @param currencyPairs
	 */
	CurrencyPairDistributor( CurrencyPair [] currencyPairs) {

	    _currencyPairs = currencyPairs;
	}


	// Methods

	/**
	 * Get the next currency pair or null, if there are no more
	 * currency pairs available.
	 *
	 * @return The next currency pair or null, if there are no more pairs available.
	 */
	public synchronized CurrencyPair getNextCurrencyPair() {

	    // An exchange might have returned no supported currency pairs, so check for this case also...
	    return _currencyPairs == null ? null : ( _currentIndex < _currencyPairs.length ? _currencyPairs[ _currentIndex++] : null);
	}
    }

    /**
     * For best performance on multiprocessor systems, it's important
     * to use several threads, so all core are kept busy.
     */
    class TradeSequenceGeneratorThread extends Thread {
	
	// Instance variables

	/**
	 * The currency pair distributor to get new currency pairs.
	 */
	CurrencyPairDistributor _distributor;

	/**
	 * The trade site to operate on.
	 */
	TradeSite _tradeSite;


	// Constructors

	/**
	 * Create a new generator thread. For now we just create one thread
	 * per currency pair. Should be enough for most pc's.
	 *
	 * @param distributor The currency distributor to get new currency pairs.
	 * @param tradeSite The trade site, we operate on.
	 */
	public TradeSequenceGeneratorThread( CurrencyPairDistributor distributor, TradeSite tradeSite) {

	    // Store the variables in this instance.
	    _distributor = distributor;
	    _tradeSite = tradeSite;
	}


	// Methods

	/**
	 * Generate a list of potential trade sequences for arb trading.
	 *
	 * @param currentSequence The current trade sequence to check.
	 */
	private void generateSequenceList( TradeSequence currentSequence) {

	    // Start with checking if it already is a complete trade sequence.
	    // That is the case, if the start currency and the end currency are identical.
	    TradePoint startPoint = currentSequence.getTradePoint( 0);  // Get the first trade point.
	    Currency startCurrency = startPoint.isBuy() ? startPoint.getTradedCurrencyPair().getPaymentCurrency() : startPoint.getTradedCurrencyPair().getCurrency();

	    TradePoint endPoint = currentSequence.getTradePoint( currentSequence.size() - 1);
	    Currency endCurrency = endPoint.isBuy() ? endPoint.getTradedCurrencyPair().getCurrency() : endPoint.getTradedCurrencyPair().getPaymentCurrency();

	    if( startCurrency.equals( endCurrency)) {  // If this sequence is complete, add it eventually to the list of results.

		// Pass this sequence to the parent class, so it could be added, if it is not
		// already in the result list.
		addSequenceIfNew( currentSequence);

		return;

	    } else {  // This sequence is not complete, so we try to extend it by one element.

		// Extend the list only if there is no maximum length set or this sequence is
		// shorter than the maximum length.
		if( ( MAX_SEQUENCE_LENGTH == -1) || ( currentSequence.size() < MAX_SEQUENCE_LENGTH)) {

		    // Get all the supported currency pairs from the trade site.
		    CurrencyPair [] allCurrencyPairs = _tradeSite.getSupportedCurrencyPairs();
		    
		    // It might happen, that fetching the pairs from the trade site did not work..
		    if( ( allCurrencyPairs != null) && ( allCurrencyPairs.length > 0)) {

			// Check all the currency pairs, if they have one currency, that equals the current end currency.
			// and the other currency is not already in the sequence
			for( int index = 0; index < allCurrencyPairs.length; ++index) {
			    
			    if( endCurrency.equals( allCurrencyPairs[ index].getCurrency())
				|| endCurrency.equals( allCurrencyPairs[ index].getPaymentCurrency())) { 
			    
				// Since a currency pair can never have 2 identical currencies, comparing with the payment currency is
				// just as good as ! endCurrency.equals( allCurrencyPairs[ index].getCurrency())
				TradePoint newTradePoint = new TradePoint( _tradeSite, allCurrencyPairs[ index], endCurrency.equals( allCurrencyPairs[ index].getPaymentCurrency()));
			    
				// Check, if the current sequence already contains this trade point, or 
				// the new target currency is already in the sequence.
				if( ! currentSequence.containsIgnoreBuy( newTradePoint)
				    && ! currentSequence.containsCurrencyAfterStart( newTradePoint.getResultingCurrency())) {
				    
				    // Create a new sequence with the added trade point.
				    TradeSequence extendedSequence = currentSequence.clone();
				    extendedSequence.addTradePoint( newTradePoint);
				    
				    // And continue the sequence list generation.
				    this.generateSequenceList( extendedSequence);
				}
			    }
			}
		    }
		}
	    }
	}

	/**
         * The actual code of the the thread.
	 * Request new sequences, until all of them are analyzed. 
         */
        @Override public void run() {

	    CurrencyPair currencyPair;  // The current currency pair to process.

	    // While there are more currency pairs.
	    while( ( currencyPair = _distributor.getNextCurrencyPair()) != null) {

		// Create a new trade sequence and add the first trade point to it.
		TradeSequence newSequence1 = new TradeSequence( _tradeSite);
		newSequence1.addTradePoint( new TradePoint( _tradeSite, currencyPair, true));
		
		// Continue the generation with this new list.
		// Pass the result array, so the results end up in the same list.
		generateSequenceList( newSequence1);
		
		// Now create a new trade sequence with the same currency pair, but the other direction.
		TradeSequence newSequence2 = new TradeSequence( _tradeSite);
		newSequence2.addTradePoint( new TradePoint( _tradeSite, currencyPair, false));

		// Continue the generation with this alternative list.
		// Pass the result array, so the results end up in the same list.
		generateSequenceList( newSequence2);
	    }
	}
    }


    // Static variables

    /**
     * The max sequence length - 1 (!).
     */
    final static int MAX_SEQUENCE_LENGTH = 4;

    /**
     * The only instance of this class (singleton pattern).
     * (Because it's designed to max out the machine it is
     * running on.)
     */
    private static TradeSequenceGenerator _instance = null;


    // Instance variables

    /**
     * The maximum number of threads.
     */
    private int _maxThreads = 1;  // Just a default value. To be overwritten in the constructor.

    /**
     * A map to split the results into small chunks, so we can check quicker, if a sequence
     * is already in the buffer.
     */
    private Map< CurrencyPair, List< TradeSequence>> _resultBuffer = new HashMap< CurrencyPair, List< TradeSequence>>();


    // Constructors

    /**
     * Create a new trade sequence generator. Since it's designed to max out the
     * machine it is running on, I make it a singleton.
     */
    private TradeSequenceGenerator() {

	// Use the number of cpu cores + 1 as the maximum number of threads,
	_maxThreads = Runtime.getRuntime().availableProcessors() + 1;
    }


    // Methods

    /**
     * Add a trade sequence, if it is not already in the result buffer.
     *
     * @param tradeSequence The trade sequence to check and add eventually.
     */
    synchronized void addSequenceIfNew( TradeSequence tradeSequence) {
	
	// Get the currency, that this trading sequence starts with.
	CurrencyPair startingCurrencyPair = tradeSequence.getStartingCurrencyPair();

	// Get the result buffer list for the starting currency.
	List<TradeSequence> currencyList = _resultBuffer.get( startingCurrencyPair);

	if( currencyList == null) {  // If there is no list yet.

	    // Create a new lis for this currency.
	    currencyList = new ArrayList<TradeSequence>();

	    // Add the sequence to this list.
	    currencyList.add( tradeSequence);

	    // Add the list to the result map.
	    _resultBuffer.put( startingCurrencyPair, currencyList);

	} else {

	    // If this sequence is not already in the list for this currency.
	    if( ! currencyList.contains( tradeSequence)) {

		// Add this sequence to the list.
		currencyList.add( tradeSequence);
	    }
	}
    }

    /**
     * Generate all the trade sequences for a given trade site.
     * This method has to be synchronized, because it clears the result
     * buffer when it is started.
     *
     * @param tradeSite The trade site to generate the sequences for.
     *
     * @return The list of generated trade sequences.
     */
    public List<TradeSequence> generateTradeSequences( TradeSite tradeSite) {

	_resultBuffer.clear();  // Clear the map for the results.

	// Get all the supported currency pairs from the trade site.
	CurrencyPair [] allCurrencyPairs = tradeSite.getSupportedCurrencyPairs();

	// Create a distributor for those pairs.
	CurrencyPairDistributor distributor = new CurrencyPairDistributor( allCurrencyPairs);

	// Create an array to store the threads
	Thread [] runningThreads = new Thread[ _maxThreads];

	// Now create the max number of threads and let them generate the sequences.
	for( int currentThreadIndex = 0; currentThreadIndex < _maxThreads; ++currentThreadIndex) {

	    // Create a new thread.
	    runningThreads[ currentThreadIndex] = new TradeSequenceGeneratorThread( distributor, tradeSite);

	    // Start this thread.
	    runningThreads[ currentThreadIndex].start();
	}

	// Now that all the threads are running, make sure, they are all terminated, before
	// we return the result.
	for( int currentThreadIndex = 0; currentThreadIndex < _maxThreads; ++currentThreadIndex) {

	    try {

		// Wait for this thread to complete.
		runningThreads[ currentThreadIndex].join();

	    } catch( InterruptedException ie) {  // I think we can ignore this? Thread already stopped?

		// Do nothing here...
	    }
	}

	System.out.println( "DEBUG: Generated "
			    + _resultBuffer.size()
			    + " sequences for "
			    + tradeSite.getName());
	
	// Now return the generated result as an array.
	return mergeResultMapIntoArray( _resultBuffer);
    }

    /**
     * Get the only instance of this class.
     *
     * @return The only instance of this class.
     */
    public static TradeSequenceGenerator getInstance() {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new TradeSequenceGenerator();  // create a new one.
	}

	return _instance;  // Return the only instance of this class.
    }

    /**
     * Merge the result buffer map into one single array of trade sequences.
     *
     * @param resultMap The result buffer map with the sequence array list for each start currency.
     *
     * @return One single array list with the sequences.
     */
    private final List<TradeSequence> mergeResultMapIntoArray( Map<CurrencyPair, List<TradeSequence>> resultMap) {

	// Create a buffer for the resulting array.
	List<TradeSequence> resultBuffer = new ArrayList<TradeSequence>();
	
	// Loop over the entire map and get the array for each starting currency.
	for( List<TradeSequence> currentSequenceList : resultMap.values()) {

	    // Add all the elements of the current sequence list to the result buffer.
	    resultBuffer.addAll( currentSequenceList);
	}

	return resultBuffer;  // Return the buffer with the concatenated lists.
    }
}
