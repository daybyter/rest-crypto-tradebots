/**
 * Java implementation of MountGox API.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.mtgox.client.ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;


/**
 * This class displays the current as a table.
 */
public class TickerTable extends JTable {

    // Static variables

    /**
     * A JPanel holding the table for easy display of a ticker.
     */
    private static JPanel _panelInstance;

    /**
     * A static table, if we just want to display the ticker in an easy way.
     */
    private static TickerTable _tableInstance;


    // Instance variables


    // Constructors

    /**
     * Create a new ticker table.
     */
    public TickerTable() {
	super( new TickerTableModel());  // Pass the ticker table model to the JTable constructor.
    }


    // Methods

    /**
     * Get a single instance of a ticker table.
     *
     * @return A single instance of a ticker table.
     */
    private static TickerTable getTableInstance() {

	if( _tableInstance == null) {
	    _tableInstance = new TickerTable();
	}

	return _tableInstance;
    }

    /**
     * Get a JPanel with a ticker table.
     *
     * @return a JPanel with a ticker table.
     */
    public static JPanel getPanelInstance() {

	if( _panelInstance == null) {
	    _panelInstance = new JPanel();

	    _panelInstance.setLayout( new BorderLayout());

	    _panelInstance.add( new JScrollPane( getTableInstance()), BorderLayout.CENTER);
	}

	return _panelInstance;
    }
}