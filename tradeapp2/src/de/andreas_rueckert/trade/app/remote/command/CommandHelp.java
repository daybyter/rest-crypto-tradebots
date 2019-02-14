/**
 * Java implementation of a remote control command.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.remote.command;

import de.andreas_rueckert.trade.app.remote.SSLSocketServer;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * Command to display help on the available commands.
 */
public class CommandHelp extends CommandCore implements RemoteCommand {

    // Static variables

    /**
     * The only instance of this command.
     */
    private static CommandHelp _instance = null;


    // Instance variables


    // Constructors

    /**
     * Private constructor for singleton pattern.
     *
     * @param sslSocketServer The hosting socket server for remote control.
     */
    private CommandHelp( SSLSocketServer sslSocketServer) {

	super( sslSocketServer); 

	// Set the properties of this command.
	_description = "get help for the available commands";
	_name = "help";
    }


    // Methods

    /**
     * Execute this command.
     *
     * @param arguments The arguments as a <name,value> map.
     *
     * @return The output of the command as a json object.
     */
    public JSONObject execute( Map<String, String> arguments) {

	// Create an array for the help info.
	JSONArray helpTexts = new JSONArray();

	for( RemoteCommand currentCommand : _sslSocketServer.getRegisteredCommands()) {

	    // Add the description of this command to the result.
	    helpTexts.add( currentCommand.getDescription());
	}

	// Convert the result to a complete json response.
	_jsonResponse.element( "success", true);  // Command worked!
	_jsonResponse.element( "result", helpTexts);  // Add the help text as the result.

	return _jsonResponse;  // Return the json response.
    }

    /**
     * Get the only instance of this command.
     *
     * @param sslSocketServer The hosting socket server for remote control.
     *
     * @return The only instance of this command.
     */
    public static CommandHelp getInstance( SSLSocketServer sslSocketServer) {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new CommandHelp( sslSocketServer);  // create a new one.
	}

	return _instance;  // Return the only instance of this command.
    }
}