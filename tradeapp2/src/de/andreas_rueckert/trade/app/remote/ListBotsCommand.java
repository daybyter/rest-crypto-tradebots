/**
 * Java implementation of a remote control command.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.remote;

import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.bot.TradeBot;
import java.util.Map;


/**
 * Command to list al bots along with their status (active/stopped).
 */
class ListBotsCommand implements RemoteCommand {

    // Static variables

    /**
     * The only instance of this command.
     */
    private static ListBotsCommand _instance = null;

    
    // Instance variables


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private ListBotsCommand() {
    }


    // Methods

        /**
     * Execute this command.
     *
     * @param arguments The arguments as a <name,value> map.
     *
     * @return The output of the command as a string.
     */
    public String execute( Map<String, String> arguments) {

	// Create a string buffer for the result.
	StringBuffer resultBuffer = new StringBuffer();

	// Fetch all the bots from the app and loop over them.
	for( Map.Entry< String, TradeBot> currentBotEntry : TradeApp.getApp().getRegisteredTradeBots().entrySet()) {

	    resultBuffer.append( currentBotEntry.getKey());  // Add the name of the bot to the result.

	    // Now add the current status of the bot to the result.
	    resultBuffer.append( " [");
	    resultBuffer.append( currentBotEntry.getValue().isStopped() ? "stopped" : "active");
	    resultBuffer.append( "]\n");
	}

	// Convert the buffer to a string and return it.
	return resultBuffer.toString();
    }

    /**
     * Get a description of this command (to be used for help).
     *
     * @return A description of this command.
     */
    public String getDescription() {
	
	// Just return a string with a description.
	return "listbots returns all the registered bots along with their status [active/stopped]";
    }

    /**
     * Get the only instance of this command.
     *
     * @return The only instance of this command.
     */
    public static ListBotsCommand getInstance() {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new ListBotsCommand();  // create a new one.
	}

	return _instance;  // Return the only instance of this command.
    }

    /**
     * Get the name of this command.
     *
     * @return The name of this command.
     */
    public String getName() {

	// Just return the name as a string.
	return "listbots";
    }
}
