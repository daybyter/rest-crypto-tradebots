/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.arb;


/**
 * An interface to notify listeners on the completion of a thread.
 */
interface TaskListener {
    
    // Variables
    

    // Methods
    
    /**
     * A task is completed.
     *
     * @param thread The thread, that executed the task.
     */
    public void taskFinished( Thread thread);
}
