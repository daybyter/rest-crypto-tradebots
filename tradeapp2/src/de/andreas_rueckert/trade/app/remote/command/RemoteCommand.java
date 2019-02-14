/**
 * Java implementation of a remote control command.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.remote.command;

import java.util.Map;
import net.sf.json.JSONObject;


/**
 * Interface for a command to remote control this app.
 */
public interface RemoteCommand {

    // Variables


    // Methods
    
    /**
     * Execute this command.
     *
     * @param arguments The arguments as a <name,value> map.
     *
     * @return A json object with the return values or the error message.
     */
    public JSONObject execute( Map<String, String> arguments);

    /**
     * Get a description of this command (to be used for help).
     *
     * @return A description of this command.
     */
    public String getDescription();

    /**
     * Get the name of this command.
     *
     * @return The name of this command.
     */
    public String getName();
}
