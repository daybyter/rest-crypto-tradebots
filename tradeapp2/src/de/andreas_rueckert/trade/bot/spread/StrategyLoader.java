/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;


/**
 * This interface defines the methods to be declared by any strategy loader.
 */
interface StrategyLoader {

    // Methods

    /**
     * Get an instance of the strategy after it was successfully loaded.
     *
     * @return An instance of the loaded strategy.
     */
    Strategy getStrategyInstance();

    /**
     * Try to load the strategy.
     *
     * @return true, if the strategy was successfully loaded. False otherwise.
     */
    boolean load();
}