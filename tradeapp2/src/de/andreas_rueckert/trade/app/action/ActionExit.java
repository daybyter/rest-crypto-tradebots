/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * Handle the proper exit of the app (look for unsaved data etc).
 */
public class ActionExit extends AbstractAction {

    // Static variables

    /**
     * The only instance of this action (singleton pattern).
     */
    private static ActionExit _instance = null;


    // Instance variables


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private ActionExit() {
        putValue( NAME, "Quit");
    }


    // Methods

    /**
     * The user wants to exit the app.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {

	// ToDo: check for unsaved data etc...

        System.exit( 0);  // Just exit.
    }

    /**
     * Get the only instance of this action.
     *
     * @return The only instance of this action.
     */
    public static ActionExit getInstance() {
        if( _instance == null) {
            _instance = new ActionExit();
        }
        return _instance;
    }
}
