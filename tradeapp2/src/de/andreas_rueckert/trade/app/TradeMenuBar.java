/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app;

import de.andreas_rueckert.trade.app.action.ActionAbout;
import de.andreas_rueckert.trade.app.action.ActionCertificates;
import de.andreas_rueckert.trade.app.action.ActionExit;
import de.andreas_rueckert.trade.app.action.ActionImportProxyServerList;
import de.andreas_rueckert.trade.app.action.ActionSettings;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


/**
 * Menu bar for the trade app.
 */
class TradeMenuBar extends JMenuBar {

    // Static variables

    /**
     * The only instance of this class => singleton pattern.
     */
    private static TradeMenuBar _instance = null;


    // Instance variables

    /**
     * The edit menu.
     */
    JMenu _editMenu;

    /**
     * The file menu.
     */
    JMenu _fileMenu;

    /**
     * The help menu.
     */
    JMenu _helpMenu;

    /**
     * The utility menu.
     */
    JMenu _utilityMenu;


    // Constructors

    /**
     * Create a new menu bar for the trade app.
     */
    private TradeMenuBar() {

	// The menu options for the file menu.
	add( _fileMenu = new JMenu( "File"));
	_fileMenu.add( new JMenuItem( ActionImportProxyServerList.getInstance()));
	_fileMenu.addSeparator();
	_fileMenu.add( new JMenuItem( ActionExit.getInstance()));

	// Options for the edit menu.
	add( _editMenu = new JMenu( "Edit"));
	_editMenu.add( new JMenuItem( ActionSettings.getInstance()));

	// Options for the utility menu.
	add( _utilityMenu = new JMenu( "Utility"));
	_utilityMenu.add( new JMenuItem( ActionCertificates.getInstance()));

	// Help menu options.
	add( _helpMenu = new JMenu( "Help"));
	_helpMenu.add( new JMenuItem( ActionAbout.getInstance()));
    }


    // Methods
    
    /**
     * Get the only instance of the menu bar.
     *
     * @return The only instance of the menu bar (singleton pattern).
     */
    public static TradeMenuBar getInstance() {

	if( _instance == null) {
	    _instance = new TradeMenuBar();
	}
	
	return _instance;
    }
}
