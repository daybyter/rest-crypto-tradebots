/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.action;

import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.bot.rule.RuleSetFile;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * Action to load a rule set into the bot.
 */
public class ActionLoadRuleSet extends AbstractAction {

    // Static variables

    /**
     * The only instance of this action.
     */
    private static ActionLoadRuleSet _instance = null;

    
    // Instance variables


    // Constructors

    /**
     * Create a new action to load rule sets.
     */
    private ActionLoadRuleSet() {
	super();

	putValue( NAME, "Load rule set");
    }

    
    // Methods

    /**
     * The user opens the rules context menu.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {

	File file = fileOpen();

	if( file != null) {  // Did the user not cancel the file opening?

            try {  // Try to create a new character set from the given file.
                TradeApp.getApp().addProjectFile( new RuleSetFile( file));
            } catch( IOException ioe) {
                JOptionPane.showMessageDialog( null, "Cannot read rule set from file", "File read error: " + ioe.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Get the only instance of this action (singleton pattern).
     *
     * @return The only instance of this action.
     */
    public static ActionLoadRuleSet getInstance() {
	if( _instance == null) {  // If there's no action instance yet,
	    _instance = new ActionLoadRuleSet();  // create one.
	}	
	return _instance;  // Return the instance.
    }

    /**
     * Show a dialog to open a file and return it, if the user selected a file to open.
     *
     * @return The new file to open, or null if the user cancelled the file opening.
     */
    public File fileOpen() {

	// Open a file dialog to select the file name.
	
        JFileChooser fileChooser = new JFileChooser();  // Create a new file chooser.

        fileChooser.addChoosableFileFilter( RuleFileFilter.getInstance());  // Add a file filter for rule sets.

        int returnVal = fileChooser.showOpenDialog( null);  // Show the file open dialog.

	// If the user closed the dialog by confirming the file name.
        if( returnVal == JFileChooser.APPROVE_OPTION) {

            return fileChooser.getSelectedFile();  // Return the file to open.
	}

	return null;  // The user cancelled the file opening.
    }
}