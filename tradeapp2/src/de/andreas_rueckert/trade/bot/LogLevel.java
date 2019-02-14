/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot;

import java.util.logging.Level;


/**
 * Define some standard log levels to use in the bot framework.
 */
public class LogLevel extends Level {

    // static variables

    /**
     * Max info to debug the app.
     */
    public static final LogLevel DEBUG = new LogLevel( "Debug", 4);

    /**
     * Show info on the running app.
     */
    public static final LogLevel INFO = new LogLevel( "Info", 3);

    /**
     * Show just errors.
     */
    public static final LogLevel ERROR = new LogLevel( "error", 2);

    /**
     * Min info to just run the app.
     */
    public static final LogLevel NONE = new LogLevel( "None", 1);

    
    // Instance variables


    // Constructors

    /**
     * Create a new LogLevel instance.
     *
     * @param name The name of this log level.
     * @param level The int value of this level.
     */
    public LogLevel( String name, int level) {
	
	super( name, level);
    }
}