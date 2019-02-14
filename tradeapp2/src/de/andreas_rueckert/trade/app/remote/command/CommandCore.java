/**
 * Java implementation of a remote control command.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.remote.command;

import de.andreas_rueckert.trade.app.remote.SSLSocketServer;
import de.andreas_rueckert.trade.app.TradeApp;
import net.sf.json.JSONObject;


/**
 * Core for all command implementations. Holds mainly convenience
 * and utility methods.
 */
class CommandCore {

    // Static variables

    
    // Instance variables

    /**
     * The description of the command as a String object.
     */
    protected String _description = null;

    /**
     * The json response from this command.
     *
     * @see http://json-lib.sourceforge.net/apidocs/jdk15/net/sf/json/JSONObject.html
     */
    JSONObject _jsonResponse = new JSONObject();

    /**
     * The name of the command as a String object.
     */
    protected String _name = null;

    /**
     * The hosting server for remote control.
     */
    protected SSLSocketServer _sslSocketServer = null;


    // Constructors

    /**
     * Create a new command instance.
     *
     * @param sslSocketServer The hosting socket server for remote control.
     */
    protected CommandCore( SSLSocketServer sslSocketServer) {

	_sslSocketServer = sslSocketServer;
    }


    // Methods

        /**
     * Get a description of this command (to be used for help).
     *
     * @return A description of this command.
     */
    public String getDescription() {
	
	// Just return a string with a description.
	return _description;
    }

    /**
     * Get the name of this command.
     *
     * @return The name of this command.
     */
    public String getName() {

	// Just return the name as a string.
	return _name;
    }
}