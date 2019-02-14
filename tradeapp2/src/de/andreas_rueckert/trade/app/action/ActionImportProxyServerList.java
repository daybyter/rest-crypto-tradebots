/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.action;

import de.andreas_rueckert.trade.site.request.ProxyRequestHandler;
import de.andreas_rueckert.trade.site.request.ui.ProxyServerListFileFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;


/**
 * Action to import a list of proxy servers.
 */
public class ActionImportProxyServerList extends AbstractAction {

    // Static variables

    /**
     * The only instance of this action (singleton pattern).
     */
    private static ActionImportProxyServerList _instance = null;


    // Instance variables


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private ActionImportProxyServerList() {

	putValue( NAME, "Import proxy list");
    }


    // Methods

    /**
     * The user triggered the action.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {

	// Open a file dialog to select the file with the proxy servers.
	File file = fileOpen();
	
	if( file != null) {

	    // Let the proxy code import the file.
	    ProxyRequestHandler.getInstance().read_csv_checkedproxylists_org( file);

	    // Just for development experience: test the imported proxies.
	    ProxyRequestHandler.getInstance().testRegisteredProxies();
	}
    }

    /**
     * Show a dialog to open a proxyserver file and return it, if the user selected a file to open.
     *
     * @return The new proxyserver file to open, or null if the user cancelled the file opening.
     */
    public File fileOpen() {

	// Open a file dialog to select the file name.
	
        JFileChooser fileChooser = new JFileChooser();  // Create a new file chooser.

        fileChooser.addChoosableFileFilter( ProxyServerListFileFilter.getInstance());  // Add a file filter for rule sets.

        int returnVal = fileChooser.showOpenDialog( null);  // Show the file open dialog.

	// If the user closed the dialog by confirming the file name.
        if( returnVal == JFileChooser.APPROVE_OPTION) {

            return fileChooser.getSelectedFile();  // Return the file to open.
	}

	return null;  // The user cancelled the file opening.
    }

    /**
     * Get the only instance of this action.
     *
     * @return The only instance of this action.
     */
    public static ActionImportProxyServerList getInstance() {

	if( _instance == null) {

	    _instance = new ActionImportProxyServerList();
	}

	return _instance;
    }
}