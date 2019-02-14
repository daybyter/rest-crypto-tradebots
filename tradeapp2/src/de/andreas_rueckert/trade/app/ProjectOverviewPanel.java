/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app;

import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.ui.TradeTable;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * This panel shows a project overview.
 */
class ProjectOverviewPanel extends JPanel {

    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static ProjectOverviewPanel _instance = null;


    /**
     * Reference to the trade app.
     */
    private static TradeApp _app = null;


    // Instance variables

    /**
     * The current project tree.
     */
    ProjectTree _projectTree;

    /**
     * A map of the registered trade sites.
     */
    Map _registeredTradeSites;


    // Constructors

    /**
     * Create a new overview panel.
     *
     * @param app The trade app.
     * @param registeredTradeSites A map with the registered trade sites.
     */
    ProjectOverviewPanel( TradeApp app, Map registeredTradeSites) {
	super();

	_app = app;

	_registeredTradeSites = registeredTradeSites;

	add( new JScrollPane( getProjectTree()));
    }
    

    // Methods

    /**
     * Get the only instance of this panel. 
     *
     * @return The only instance of this panel.
     */
    public static ProjectOverviewPanel getInstance() {
	return _instance;  // The panel should already exist at this point.
    }

    /**
     * Get the only instance of this class.
     *
     * @param app The trade app.
     * @param registeredTradeSites A map with the registered trade sites.
     *
     * @return The only instance of this class.
     */
    public static ProjectOverviewPanel getInstance( TradeApp app, Map registeredTradeSites) {
	if( _instance == null) {
	    _instance = new ProjectOverviewPanel( app, registeredTradeSites);
	}
	return _instance;
    }


    /**
     * Get a JTree representing the project.
     *
     * @return A JTree representing the project.
     */
    public ProjectTree getProjectTree() {
	if( _projectTree == null) {
	    _projectTree = new ProjectTree();
	}

	return _projectTree;
    }
}