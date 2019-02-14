/**
 * Java implementation of a remote control command.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.remote;

import java.util.Map;


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
     */
    public String execute( Map<String, String> arguments);

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
