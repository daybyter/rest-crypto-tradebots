/**
 * Java implementation of a chart provider.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.chart.logger;

import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.ImplementationChecker;
import de.andreas_rueckert.util.ModuleLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * This is meant to log data from trade sites.
 */
class DataLogger {

    // Inner classes

    /**
     * A pipeline with events to tasks to process.
     */
    class TaskPipeline {

	// Instance variables

	/**
	 * This is a queue with fetch events, that have to be process.
	 * Each event triggers a request to some tradesite.
	 */
	private LinkedList<FetchTask> _taskQueue = new LinkedList<FetchTask>();


	// Constructors


	// Methods

	/**
	 * Add a new task to the pipeline (it is sorted by the execution time of the task.
	 *
	 * @param task The new task to add.
	 */
	public final synchronized void addTask( FetchTask task) {

	    int currentPos = 0;  // Searching for the correct position starts with index 0.

	    // Check pipeline elements for their execution time.
	    while( ( currentPos < _taskQueue.size())
		   && ( getTask( currentPos).getScheduledTime() < task.getScheduledTime())) {

		++currentPos;  // Insert the task behind the current task.
	    }

	    // Add the new task at the correct position to keep the scheduled times sorted.
	    _taskQueue.add( currentPos, task);

	    // Notify any waiting threads, that there might be new work.
	    _taskQueue.notify();
	}

	/**
	 * Check, if the queue of tasks is empty or contains tasks to process.
	 *
	 * @return true, if the task queue is empty. False otherwise.
	 */
	public final synchronized boolean isEmpty() {
	    
	    // Just return the empty state of the task queue.
	    return _taskQueue.isEmpty();
	}

	/**
	 * Get the fetch task with a given index from the pipeline.
	 *
	 * @param index The index of the task.
	 *
	 * @return The fetch task with the given index.
	 *
	 * @throws IndexOutOfBoundsException if an element with the given index does not exist.
	 */
	public final synchronized FetchTask getTask( int index) throws IndexOutOfBoundsException {

	    return _taskQueue.get( index);
	}

	/**
	 * Get the first task of the pipeline.
	 *
	 * @return The first event of the pipeline.
	 *
	 * @throw NoSuchElementException if the pipeline is empty.
	 */
	public final synchronized FetchTask popTask() throws NoSuchElementException {
	    
	    // Get the first task of the queue.
	    return _taskQueue.removeFirst();
	}

	/**
	 * Push back a task to the pipeline.
	 *
	 * @param task The task to push back to the pipeline.
	 */
	public final synchronized void pushbackTask( FetchTask task) {

	    // Push the task at the top of the pipeline.
	    // This might be necessary, if it was not executable at the moment.
	    _taskQueue.push( task);
	    
	    // Notify waiting threads, that there is new work.
	    _taskQueue.notify();
	}
    }

    /**
     * A thread to fetch data and log them.
     */
    class WorkerThread extends Thread {
	
	// Instance variables


	// Constructors


	// ... Create thread to fetch data.

	
	// Methods

	/**
	 * The actual worker method.
	 */
	@Override public void run() {

	    while( ! isInterrupted()) {

		while( _tasks.isEmpty()) {  // While there are no tasks to perform,

		    try {

			wait();  // just wait.

		    } catch( InterruptedException ioe) {

			// Program exits?
		    }
		}

		// When there is a task to perform.
		
		FetchTask task = _tasks.popTask();  // Get the task.

		// Check, if an exception was thrown, because 2 threads might get a notification?

		// ... todo: write processing...
	    }
	}
    }


    // Static variables


    // Instance variables

    /**
     * The queue with the waiting tasks.
     */
    private TaskPipeline _tasks;


    // Constructors

    /**
     * Create a new data logger to fetch trade data from exchanges.
     */
    public DataLogger() {

	// Create a new queue for the tasks.
	_tasks = new TaskPipeline();
    }


    // Methods

    /**
     * Get a list of trade sites, that implement the fetching of trades.
     * 
     * @return A list of trade sites, that implement the fetching of the trades.
     */
    List<TradeSite> getTradeSitesWithTradesImplementation() {

	// Create a buffer for the results.
	List<TradeSite> resultBuffer = new ArrayList<TradeSite>();

	// Loop over all the availables trade sites.
	for( TradeSite currentTradeSite : ModuleLoader.getInstance().getRegisteredTradeSites().values()) {

	    if( ImplementationChecker.hasImplementationTrades( currentTradeSite)) {

		resultBuffer.add( currentTradeSite);
	    }
	}

	// Return the result.
	return resultBuffer;
    }
}