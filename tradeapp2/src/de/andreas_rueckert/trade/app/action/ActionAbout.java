/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * Show some info on the app.
 */
public class ActionAbout extends AbstractAction {

    // Static variables

    /**
     * The only instance of this action ( singleton pattern).
     */
    private static ActionAbout _instance = null;


    // Instance variables


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private ActionAbout() {
	putValue( NAME, "Quit");
    }


    // Methods

    /**
     * The user wants to know about the app.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {

	// ToDo: show about-frame.

    }

    /**
     * Get the only instance of this action.
     *
     * @return The only instance of this action.
     */
    public static ActionAbout getInstance() {
        if( _instance == null) {
            _instance = new ActionAbout();
        }
        return _instance;
    }
}