/**
 * Exception to be thrown, if a trade site does not support this kind of operation.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;


/**
 * Throw an exception, if a strategy with a given name is already loaded.
 * This is a good indication, that the 2 strategies might/could be identical.
 */
public class IdenticalStrategyAlreadyLoadedException extends RuntimeException {

    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new exception, indicating that this strategy might already be loaded.
     *
     * @param message The message to send.
     */
    public IdenticalStrategyAlreadyLoadedException( String message) {
	super( "A strategy with this name is already loaded: " + message);
    }


    // Methods
}