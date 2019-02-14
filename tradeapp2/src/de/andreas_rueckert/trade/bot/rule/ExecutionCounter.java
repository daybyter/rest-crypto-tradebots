/**
 * Java implementation of a tradebot.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.rule;


/**
 * This class just counts the execution of a rule set.
 */
public class ExecutionCounter {

    // Static variables


    // Instance variables

    /**
     * The actual counter.
     */
    private int _counter = 0;


    // Constructors


    // Methods

    /**
     * Get the current counter value.
     *
     * @return The current counter value.
     */
    public int getCounter() {
	return _counter;
    }

    /**
     * Increment this counter.
     */
    public void increment() {
	_counter++;
    }

    /**
     * Reset this counter.
     */
    public void reset() {
	_counter = 0;
    }
}