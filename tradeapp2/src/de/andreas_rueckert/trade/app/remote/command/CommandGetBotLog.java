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
import de.andreas_rueckert.trade.bot.TradeBot;
import de.andreas_rueckert.trade.bot.TradeLogger;
import java.io.IOException;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * Command to fetch the trade log of a specific bot.
 */
public class CommandGetBotLog extends CommandCore implements RemoteCommand {


    // Static variables

    /**
     * The only instance of this command.
     */
    private static CommandGetBotLog _instance = null;
    

    // Instance variables


    // Constructors

    /**
     * Private constructor for singleton pattern.
     *
     * @param sslSocketServer The hosting socket server for remote control.
     */
    private CommandGetBotLog( SSLSocketServer sslSocketServer) {
	
	super( sslSocketServer); 

	// Set the properties of this command implementation;
	_description = "get the tradelog of a given bot";
	_name = "getbotlog";
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

	String tradeBotName = arguments.get("name");

	if( tradeBotName == null) {

	    _jsonResponse.element( "success", false);  // Command failed!
	    _jsonResponse.element( "errormessage", "No tradebot name given");

	} else {
	
	    // Search the bot with the given name.
	    TradeBot bot = TradeApp.getApp().getRegisteredTradeBot( tradeBotName);

	    if( bot == null) {  // No bot with this name found

		_jsonResponse.element( "success", false);  // Command failed!
		_jsonResponse.element( "errormessage", "No registered tradebot with the name " + tradeBotName + " found");

	    } else {

		// Now we have the bot.
		// Try to get the logger from it.
		TradeLogger tradeLogger = bot.getTradeLogger();

		try {

		    // Request the complete log from the bot.
		    String [] fullLog = tradeLogger.getFullLog();

		    // Create a json array for the list of log entries.
		    JSONArray logEntries = new JSONArray();

		    // Loop over the log entries and add them to the result.
		    for( String currentEntry : fullLog) {
			
			logEntries.add( currentEntry);
		    }
		    
		    // Add the log entries as the result to the response.
		    _jsonResponse.element( "result", logEntries);
		    
		} catch( IOException ioe) {  // Error while reading the logfile.

		    _jsonResponse.element( "success", false);  // Command failed!
		    _jsonResponse.element( "errormessage", "Reading the logfile failed: " + ioe);
		}
	    }
	}

	return _jsonResponse;  // Return the result.
    }



    /**
     * Get the only instance of this command.
     *
     * @param sslSocketServer The hosting socket server for remote control.
     *
     * @return The only instance of this command.
     */
    public static CommandGetBotLog getInstance( SSLSocketServer sslSocketServer) {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new CommandGetBotLog( sslSocketServer);  // create a new one.
	}

	return _instance;  // Return the only instance of this command.
    }
}
