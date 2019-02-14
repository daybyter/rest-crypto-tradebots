/**
 * Exception to be thrown, if a trade data request is not allowed at the moment.
 * Most likely, the minimal request interval is not respected.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.site;


/**
 * Throw an exception, if a trade data request is not allowed at the moment.
 */
public class TradeDataRequestNotAllowedException extends RuntimeException {

    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new exception, indicating that some trade data request is not allowed at the moment.
     *
     * @param message The message to send.
     */
    public TradeDataRequestNotAllowedException( String message) {
	super( "This trade data request is not allowed at the moment: " + message);
    }


    // Methods
}