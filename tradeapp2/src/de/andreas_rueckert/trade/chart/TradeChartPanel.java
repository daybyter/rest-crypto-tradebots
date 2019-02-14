/**
 * Java implementation of a chart provider.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.chart;

import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.ModuleLoader;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Checkbox;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JPanel;


/**
 * This panel hold the panel with the actual charts and the UI to modify the chart.
 */
public class TradeChartPanel extends JPanel {

    // Static variables

    /**
     * The only instance of this panel (singleton pattern).
     */
    private static TradeChartPanel _instance = null;


    // Instance variables

    
    // Constructors

    /**
     * Create a new TradeChartPanel instance.
     */
    private TradeChartPanel() {
	super();

	setLayout( new BorderLayout());

	// Add a panel for the actual graphics
	add( TradeGraphicsPanel.getInstance(), BorderLayout.CENTER);

	// Add some basic UI to modify the chart
	
	JPanel chartUIPanel = new JPanel();

	// Activate the charts for each trading site.
	JPanel siteSelectionPanel = new JPanel();
	
	Map<String, TradeSite> tradeSites = ModuleLoader.getInstance().getRegisteredTradeSites();

	// Show the checkboxes in a vertical layout.
	siteSelectionPanel.setLayout( new GridLayout( tradeSites.size(), 1));

	// Add an enabled checkbox to activate a chart for each trading site.
	for( TradeSite t : tradeSites.values()) {
	    siteSelectionPanel.add( new Checkbox( t.getName(), true));
	}
	
	// Add the selection panel to the ui panel.
	chartUIPanel.add( siteSelectionPanel);

	// Create a combo box to select the displayed timespan.
	String [] timespans = { "1h", "2h", "4h", "6h", "12h" };
	JComboBox timespanSelection = new JComboBox( timespans);

	chartUIPanel.add( timespanSelection); 
	
	JPanel indicatorPanel = new JPanel();  // Add a panel for indicators

	indicatorPanel.add( new Checkbox( "SMA"));
	String [] timespans2 = { "2h", "4h", "6h", "12h", "24h"};
	indicatorPanel.add( new JComboBox( timespans2));

	chartUIPanel.add( indicatorPanel);

	add( chartUIPanel, BorderLayout.SOUTH);
    }


    // Methods

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static TradeChartPanel getInstance() {
	if( _instance == null) {
	    _instance = new TradeChartPanel();
	}
	return _instance;
    }
}