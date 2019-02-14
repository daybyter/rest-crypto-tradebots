/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.site;


/**
 * Throw an exception, if a trade site is not found in the registered trading sites.
 */
public class TradeSiteNotFoundException extends RuntimeException {

    // Instance variables


    // Constructors

    /**
     * Create a new exception, indicating that a trading site was not found in the registered trading sites.
     *
     * @param message The message to send in the exception.
     */
    public TradeSiteNotFoundException( String message) {
	super( "Trading site not found: " + message);
    }


    // Methods
}