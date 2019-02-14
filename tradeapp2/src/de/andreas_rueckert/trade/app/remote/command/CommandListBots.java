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
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * Command to list all bots along with their status (active/stopped).
 */
public class CommandListBots extends CommandCore implements RemoteCommand {

    // Static variables

    /**
     * The only instance of this command.
     */
    private static CommandListBots _instance = null;

    
    // Instance variables


    // Constructors

    /**
     * Private constructor for singleton pattern.
     *
     * @param sslSocketServer The hosting socket server for remote control.
     */
    private CommandListBots( SSLSocketServer sslSocketServer) {

	super( sslSocketServer); 

	// Set the properties of this command.
	_description = "listbots returns all the registered bots along with their status [active/stopped/dead]";
	_name = "listbots";
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

	JSONArray botList = new JSONArray();  // Create an array for the bots.

	// Fetch all the bots from the app and loop over them.
	for( Map.Entry< String, TradeBot> currentBotEntry : TradeApp.getApp().getRegisteredTradeBots().entrySet()) {

	    JSONObject botInfo = new JSONObject();  // Create an array for the bots.

	    botInfo.element( "name", currentBotEntry.getKey());  // Add the name of the bot to the result.

	    // Now add the current status of the bot to the result.
	    botInfo.element( "active", ! currentBotEntry.getValue().isStopped());

	    // Add the info to the list of bots.
	    botList.add( botInfo);
	}

	// Convert the buffer to a complete json response.
	_jsonResponse.element( "success", true);  // Command worked!
	_jsonResponse.element( "result", botList);  // Add the bot list as the result.

	return _jsonResponse;  // Return the json response.
    }

    /**
     * Get the only instance of this command.
     *
     * @param sslSocketServer The hosting socket server for remote control.
     *
     * @return The only instance of this command.
     */
    public static CommandListBots getInstance( SSLSocketServer sslSocketServer) {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new CommandListBots( sslSocketServer);  // create a new one.
	}

	return _instance;  // Return the only instance of this command.
    }
}
