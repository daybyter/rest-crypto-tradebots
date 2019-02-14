/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.NotEnoughOrdersException;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.order.OrderFactory;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.LogUtils;
import java.lang.reflect.Constructor;  // Just for debugging.
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;
import java.math.MathContext;


/**
 * This class holds methods to analyze trade sequences in various ways.
 */
class TradeSequenceAnalyzer {

    // Inner classes

    /**
     * A base class for analyzer threads.
     */
    class SequenceAnalyzerThreadBase extends Thread {

	// Instance variables

	/**
	 * The distributor for the sequences.
	 */
	private TradeSequenceDistributor  _distributor;

	
	// Constructors

	/**
	 * Create a new analyzer thread.
	 *
	 * @param distributor.
	 */
	public SequenceAnalyzerThreadBase( TradeSequenceDistributor distributor) {

	    _distributor = distributor;
	}


	// Methods

	/**
	 * The actual analyzer methods should override this method
	 * with the analyzing code.
	 *
	 * @param sequence The sequence to analyze.
	 */
	public void analyze( TradeSequence sequence) throws TradeDataNotAvailableException {

	    throw new NotYetImplementedException( "The analyze method is only implemented in subclasses of the analyzer thread.");
	}
	
	/**
         * The actual code of the the thread.
	 * Request new sequences, until all of them are analyzed. 
         */
        @Override public void run() {

	    TradeSequence currentSequence;  // The current sequence to process.

	    // While there are sequences to analyze.
	    while( ( currentSequence = _distributor.getNextSequence()) != null) {

		try {

		    // Call the analyzer method of the thread.
		    analyze( currentSequence);

		} catch( TradeDataNotAvailableException tdnae) {

		    LogUtils.getInstance().getLogger().error( "Exception in the calculation of the trade sequence "
							      + currentSequence.toString() 
							      + " : " 
							      + tdnae);
		}
	    }
	}
    }


    /**
     * This is a very basic analyzer just using the first order of the 
     * depth to get the price and volume.
     */
    class SequenceAnalyzerThreadMinimum extends SequenceAnalyzerThreadBase {

	// Instance variables


	// Constructors

	/**
	 * Create a new analyzer thread.
	 *
	 * @param distributor.
	 */
	public SequenceAnalyzerThreadMinimum( TradeSequenceDistributor distributor) {

	    super( distributor);
	}


	// Methods

	/**
	 * The actual analyzer method.
	 *
	 * @param sequence The sequence to analyze.
	 */
	public void analyze( TradeSequence sequence) {
	    
	    sequence.setTradeIndicatorInput( new Price( "10"));
	    Amount startAmount = Amount.MINUS_ONE;
	    Amount currentAmount = Amount.MINUS_ONE;

	    if( sequence.isActive()) {

		Price currentPrice = sequence.getTradeIndicatorInput();
		
		for( int index = 0; index < sequence.size(); ++index) {
		    
		    // Get the current trade point.
		    TradePoint currentPoint = sequence.getTradePoint( index);
		    
		    // Depth currentDepth = ChartProvider.getInstance().getDepth( currentPoint.getTradeSite(), currentPoint.getTradedCurrencyPair());
		    Depth currentDepth = getBot().getDepthFromCache( sequence.getTradeSite(), currentPoint.getTradedCurrencyPair());

		    // Start with a check, if there are actually any order in the depth.
		    if( ( currentPoint.isBuy() && ( currentDepth.getSellSize() == 0))
			|| ( ! currentPoint.isBuy() && ( currentDepth.getBuySize() == 0))) {  
			
			// No order in this depth => calculate the next sequence.
			// Complete the trade sequence values and set all the calculated values in the sequence.
			sequence.setTradeIndicatorOutput( new Price( "-1"));
			sequence.setTradeAmount( new Amount( "-1"));
			sequence.setTradeProfit( new Amount( "-1"));

			return;  // Next trade sequence.
		    }

		    // Try to get the current order.
		    DepthOrder currentOrder = currentPoint.isBuy() ? currentDepth.getSell( 0) : currentDepth.getBuy( 0); 

		    // Check the amount of the current order and adjust the start amount accordingly.
		    if( index == 0) {
			currentAmount = startAmount = currentOrder.getAmount();
		    } else {
			if( currentAmount.compareTo( currentOrder.getAmount()) > 0) {
			    startAmount = new Amount( startAmount.multiply( currentOrder.getAmount()).divide( currentAmount, MathContext.DECIMAL128));
			    currentAmount = currentOrder.getAmount();
			}
		    }
	    
		    if( currentPoint.isBuy()) {
			currentPrice = new Price( currentPrice.divide( currentOrder.getPrice(), MathContext.DECIMAL128));     // Compute the new price.
			currentAmount = new Amount( currentAmount.divide( currentOrder.getPrice(), MathContext.DECIMAL128));  // Compute the new amount.
		    } else {
			currentPrice = new Price( currentPrice.multiply( currentOrder.getPrice()));     // Compute the new price.
			currentAmount = new Amount( currentAmount.multiply( currentOrder.getPrice()));  // Compute the new amount.
		    }
	    
		    // Create an order to calculate the fee.
		    SiteOrder tradeOrder = OrderFactory.createCryptoCoinTradeOrder( sequence.getTradeSite()
										    , null  // No userAccount for now?
										    , ( currentPoint.isBuy() ? OrderType.BUY : OrderType.SELL)
										    , currentPrice
										    , currentPoint.getTradedCurrencyPair()
										    , currentAmount);
		
		    // Now request the fee for this order.
		    Price fee = sequence.getTradeSite().getFeeForOrder( tradeOrder);
		    
		    // Subtract the fee from the amount.
		    currentAmount = new Amount( currentAmount.subtract( fee));
		}

		// Complete the trade sequence values and set all the calculated values in the sequence.
		sequence.setTradeIndicatorOutput( currentPrice);
		sequence.setTradeAmount( startAmount);   

		Amount profit = new Amount( startAmount);  // We modify this amount, so clone it first.

		// The following calculation might not be 100% accurate, but should be good enough for now...
		profit = new Amount( profit.multiply( sequence.getTradeIndicatorOutput()).divide( sequence.getTradeIndicatorInput(), MathContext.DECIMAL128).subtract( startAmount)); // Profit is <end amount> - <start amount>.
		
		sequence.setTradeProfit( profit);  

		// Store the timestamp of this calculation in the sequence.
		sequence.setLastCalculationTimestamp(); 
	    } else {
		sequence.setTradeIndicatorOutput( new Price( "-1"));
		sequence.setTradeAmount( new Amount( "0"));   
		sequence.setTradeProfit( new Amount( "-1"));  
	    }
	}
    }

    /**
     * This analyzer is an improved minimal analyzer, that tries to find the
     * actual maximum volume for a profitable sequence. This volume does not
     * just use the first order of the depth, but gets the orders for a given price.
     */
    class SequenceAnalyzerThreadVolumeMax extends SequenceAnalyzerThreadMinimum {

	// Instance variables


	// Constructors

	/**
	 * Create a new analyzer thread.
	 *
	 * @param distributor.
	 */
	public SequenceAnalyzerThreadVolumeMax( TradeSequenceDistributor distributor) {

	    super( distributor);
	}


	// Methods

	/**
	 * The actual analyzer method.
	 *
	 * @param sequence The sequence to analyze.
	 */
	public void analyze( TradeSequence sequence) {

	    // Check, if this sequence is deactivated.
	    if( ! sequence.isActive()) {

		// Just set some dummy values for profit etc.
		sequence.setTradeIndicatorOutput( Price.MINUS_ONE);
		sequence.setTradeProfit( Amount.MINUS_ONE);
		sequence.setTradeAmount( Amount.MINUS_ONE);

		return;  // Abort further analyzation.
	    }

	    // Use the minimal analyzer method to check, if there any chance to make profit.
	    super.analyze( sequence);

	    // If so, try to optimize the profit by finding the max input amount.
	    if( sequence.getTradeProfit().compareTo( BigDecimal.ZERO) > 0) {

		// Start with the amount, that the minimum analyzer returned.
		Amount currentAmount = sequence.getTradeAmount();
		Amount currentProfit;

		try {

		    currentProfit = calculateSequenceProfit( sequence, currentAmount);

		} catch( NotEnoughOrdersException neoe) {

		    // Not really a bug. Orders might have change while this method was called.
		    LogUtils.getInstance().getLogger().info( "Not enough orders for initial profit in sequence analyzer");

		    // Reset the profit to 0 then?

		    return;  // Not even enough orders for the initial profit.
		}

		// If we cannot confirm the profit, stop the calculation here.
		if( currentProfit.compareTo( BigDecimal.ZERO) <= 0) {

		    // Set the new relative trade result.
		    setRelativeTradeResult( sequence
					    , new Price( "10")
					    , currentAmount
					    , currentProfit);

		    return;
		}

		Amount currentIncrement = currentAmount;
		
		BigDecimal tenDecimal = new BigDecimal( "10");  // Just to avoid the repeated creating of this object.

		// Calculate on 1 / 1000 of the initial amount.
		BigDecimal minIncrement = currentIncrement.divide( new BigDecimal( "1000"), MathContext.DECIMAL128);
		
		// Now try to increase that amount as long as the profit increase.
		while( true) {
			
		    // Increase the amount and see how it turns out.
		    Amount newAmount = currentAmount.add( currentIncrement);
		    Amount newProfit = Amount.ZERO;
		    boolean enoughOrders = true;
		    
		    try {
			newProfit = calculateSequenceProfit( sequence, currentAmount);
			
		    } catch( NotEnoughOrdersException neoe) {

			enoughOrders = false;  // There are not enough orders for this iteration.
		    }
		    
		    // If the new profit is bigger than the old one, continue.
		    if( enoughOrders && ( newProfit.compareTo( currentProfit) > 0)) {
			
			currentAmount = newAmount;
			currentProfit = newProfit;

		    } else {  // Reduce the increment.
			
			currentIncrement = new Amount( currentIncrement.divide( tenDecimal, MathContext.DECIMAL128));

			if( currentIncrement.compareTo( minIncrement) < 0) {
			    
			    // Set the calculated values in the trade sequence.
			    sequence.setTradeAmount( currentAmount);
			    sequence.setTradeProfit( currentProfit);

			    // Set the new relative trade result.
			    setRelativeTradeResult( sequence
						    , new Price( "10")
						    , currentAmount
						    , currentProfit);

			    // Store the timestamp of this calculation in the sequence.
			    sequence.setLastCalculationTimestamp(); 

			    return;  // Calculation completed.
			}
		    }
		}
	    }
	}

	/**
	 * Calculate the profit for a given input amount.
	 *
	 * @param sequence The sequence to calculate.
	 * @param The input amount.
	 *
	 * @throws NotEnoughOrdersException if there are not enough orders to trade this amount.
	 */
	protected Amount calculateSequenceProfit( TradeSequence sequence, Amount inputAmount) throws NotEnoughOrdersException {

	    Amount currentAmount = inputAmount;

	    // Now loop over the sequence points.
	    for( int index = 0; index < sequence.size(); ++index) {
	    
		// Get the current trade point.
		TradePoint currentPoint = sequence.getTradePoint( index);

		// Get the current depth for this currency pair.
		Depth currentDepth = getBot().getDepthFromCache( sequence.getTradeSite(), currentPoint.getTradedCurrencyPair());

		// Get a price for the current amount.
		// If this point is a buy, sum up the sell orders and vice versa.
		Price currentPrice = currentDepth.getPriceForAmount( currentAmount, ! currentPoint.isBuy());
	    
		/* if( currentPrice == null) {
		    System.out.println( "Current price is null");
		    } */

		/* 
		   System.out.println( "DEBUG: current price is: " 
				    + currentPrice 
				    + " for pair " 
				    + currentPoint.getTradedCurrencyPair().toString()
				    + " and amount "
				    + currentAmount);
		   System.out.flush(); 
		*/

		// Compute the new amount.
		currentAmount = new Amount( currentPoint.isBuy() 
					    ? currentAmount.divide( currentPrice, MathContext.DECIMAL128)
					    : currentAmount.multiply( currentPrice));

		// Create an order to calculate the fee.
		SiteOrder tradeOrder = OrderFactory.createCryptoCoinTradeOrder( sequence.getTradeSite()
										, null  // No userAccount for now?
										, ( currentPoint.isBuy() ? OrderType.BUY : OrderType.SELL)
										, currentPrice
										, currentPoint.getTradedCurrencyPair()
										, currentAmount);
		
		// Now request the fee for this order.
		Price fee = sequence.getTradeSite().getFeeForOrder( tradeOrder);
		
		// Subtract the fee from the amount.
		currentAmount = new Amount( currentAmount.subtract( fee));

		// If the amount gets negative, just return -1 as the default.
		if( currentAmount.signum() == -1) {

		    return Amount.MINUS_ONE;
		}
	    }

	    return new Amount( currentAmount.subtract( inputAmount));  // Return the last amount minus the input => profit.
	}

	/**
	 * Calculate and set (in the sequence) the relative trade 
	 * result for a trade sequence.
	 *
	 * @param tradeSequence The trade sequence to process.
	 * @param relativeAmount The input amount to trade.
	 * @param tradeAmount The calculated best trade amount.
	 * @param tradeProfit The profit from the trade amount.
	 */
	private final void setRelativeTradeResult( TradeSequence tradeSequence
						   , Price relativeAmount
						   , Amount tradeAmount
						   , Amount tradeProfit) {

	    // Add the multiplied profit to the relative trade amount.
	    tradeSequence.setTradeIndicatorOutput( new Price( relativeAmount.add( tradeProfit.multiply( relativeAmount.divide( tradeAmount, MathContext.DECIMAL128)))));
	}
    }

    /**
     * A distributor for trade sequences for multiple threads.
     * I don't want to start a new thread for every sequence, so I distribute
     * the sequences to <cores> running threads until all of them are analyzed.
     */
    class TradeSequenceDistributor {
	
	// Instance variables
	
	/**
	 * The index of the current sequence.
	 */
	private int _currentIndex = 0;

	/**
	 * The list of sequences to work on.
	 */
	private List< TradeSequence> _sequences;


	// Constructors

	/**
	 * Create a new distributor for trade sequences.
	 *
	 * @param sequences The sequences to distribute.
	 */
	public TradeSequenceDistributor( List< TradeSequence> sequences) {

	    _sequences = sequences;
	}
	
	
	// Methods

	/**
	 * Get the next sequence for a calculation.
	 * It's important to synchronize this method, so 2 calls are processed
	 * one after the other.
	 * 
	 * @return The next sequence to process, or null if there are no sequences left.
	 */
	public synchronized TradeSequence getNextSequence() {
	    
	    // Just return null to indicate, that there are no sequences left to process.
	    return _currentIndex < _sequences.size() ? _sequences.get( _currentIndex++) : null;
	}
    }


    // Static variables


    // Instance variables

    /**
     * The hosting bot.
     */
    private ArbBot _bot;
    
    /**
     * The number of cores, which is used to determine a reasonable number
     * of threads.
     */
    private int _nCores;


    // Constructors

    /**
     * Create a new analyzer instance.
     *
     * @param bot The hosting bot.
     */
    TradeSequenceAnalyzer( ArbBot bot) {
	
	_bot = bot;  // Store the bot in the instance.

	_nCores = Runtime.getRuntime().availableProcessors();  // Get the number of cores.
    }


    // Methods

    /**
     * Analyze a list of sequences, using a given analyzer thread class.
     * This is a generic method to be used by all analyzer methods.
     *
     * @param tradeSequences The trade sequences to analyze.
     * @param analyzerThreadClass The class type of the analyzer thread.
     */
    private void analyzeTradeSequences( List< TradeSequence> tradeSequences, Class analyzerThreadClass) {

	// Create a distributor for the sequences
	TradeSequenceDistributor distributor = new TradeSequenceDistributor( tradeSequences);

	// Create an array of threads.
	Thread [] analyzerThread = new Thread[ _nCores];
	
	try {

	    Constructor constructor = analyzerThreadClass.getConstructor( TradeSequenceAnalyzer.class, TradeSequenceDistributor.class);

	    // Now use all the availables cores to analyze the sequences by starting a number
	    // of analyzer threads.
	    for( int currentCore = 0; currentCore < analyzerThread.length; ++currentCore) {

		// Create an analyzer thread and pass the distributor to the constructor.
		// An alternative would be to define an interface with a setDistributor class, but
		// the constructor could also be used directly in some other code and is the
		// shorter version then...
		// BTW: an inner class has the parent class as the first argument to the constructor!
		analyzerThread[ currentCore] = (Thread)constructor.newInstance( this, distributor); 
		
		analyzerThread[ currentCore].start();
	    }

	} catch( InstantiationException ie) {
	    
	    LogUtils.getInstance().getLogger().error( "Cannot create analyzer thread: " + ie);
	    
	} catch( NoSuchMethodException nsme) {
	    
	    LogUtils.getInstance().getLogger().error( "Analyzer thread has no constructor with distributor: " + nsme);
	    
	} catch( IllegalAccessException iae) {
	    
	    LogUtils.getInstance().getLogger().error( "Analyzer constructor not accessible from parent class: " + iae);
	    
	} catch( InvocationTargetException ite) {
	    
	    LogUtils.getInstance().getLogger().error( "Cannot invoke analyzer constructor: " + ite);
	}

	// Wait for the threads to complete.
	for( int currentCore = 0; currentCore < analyzerThread.length; ++currentCore) {

	    try {

		if( analyzerThread[ currentCore] != null) {  // If there is a running thread,
		    analyzerThread[ currentCore].join();     // wait for it to end.
		}

	    } catch( InterruptedException ie) {

		LogUtils.getInstance().getLogger().error( "Cannot complete analyzer thread: " + ie);

	    }
	}
    }

    /**
     * Do simple approximation of the price, if always the first order of the depth is used.
     */
    void calculateTradeSequences( List< TradeSequence> tradeSequences) {

	// Analyze the sequences with the minimum analyzer thread class.
	//analyzeTradeSequences( tradeSequences, SequenceAnalyzerThreadMinimum.class);
	
	// Use the volume maximizer here.
	analyzeTradeSequences( tradeSequences, SequenceAnalyzerThreadVolumeMax.class);
    }

    /**
     * Get the hosting bot.
     *
     * @return The hosting bot.
     */
    ArbBot getBot() {

	return _bot;
    }
}