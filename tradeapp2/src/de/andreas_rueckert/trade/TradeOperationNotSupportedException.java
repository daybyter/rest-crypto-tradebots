/**
 * Exception to be thrown, if a trade site does not support this kind of operation.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade;


/**
 * Throw an exception, if the current kind of trade operation is not supported.
 */
public class TradeOperationNotSupportedException extends RuntimeException {

    // Static variables

    
    // Instance variables


    // Constructors
    
    /**
     * Create a new exception, indicating that this trading operation is not supported.
     *
     * @param message The message to send.
     */
    public TradeOperationNotSupportedException( String message) {
	super( "This type of trading operation is not supported: " + message);
    }


    // Methods
}