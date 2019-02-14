/**
 * Java implementation of MountGox API.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.ui;

import de.andreas_rueckert.trade.site.TradeSite;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


/**
 * This class displays a list of trades as a table.
 */
public class TradeTable extends JTable implements ActionListener {

    // Static variables


    // Instance variables

    /**
     * A JPanel holding the table for easy display of a ticker.
     */
    private JPanel _panel = null;

    /**
     * The button to update the trade table.
     */
    private JButton _updateButton;


    // Constructors

    /**
     * Create a new trade table.
     *
     * @param tradeSote the interface to the trade website.
     */
    public TradeTable( TradeSite tradeSite) {
	super( new TradeTableModel( tradeSite));  // Pass the ticker table model to the JTable constructor.

	// Set some reasonable column widths
	setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
	int [] colWidths = { 80, 180, 40, 140, 140, 40};
	TableColumn column = null;
	for( int i = 0; i < 6; i++) {
	    column = getColumnModel().getColumn(i);
	    column.setPreferredWidth( colWidths[ i]); //third column is bigger
	}
    }


    // Methods

    /**
     * The user pressed the 'Update' button.
     *
     * @param event The action event.
     */
    public void actionPerformed( ActionEvent event) {
	TableModel model = getModel();

	if( model != null) {  // If we can access the model.

	    ((TradeTableModel)model).update();  // Cast the model and ask it to update the trade table.

	    invalidate();  // Repaint the table with the added new trades.
	    validate();
	    updateUI();
	}
    }


    /**
     * Get a JPanel with a ticker table.
     *
     * @param tradeSite The interface to the trade website.
     *
     * @return a JPanel with a ticker table.
     */
    public JPanel getPanel() {

	if( _panel == null) {
	    _panel = new JPanel();

	    _panel.setLayout( new BorderLayout());

	    _panel.add( new JScrollPane( this), BorderLayout.CENTER);

	    _panel.add( _updateButton = new JButton( "Update"), BorderLayout.SOUTH);
	    _updateButton.addActionListener( this);
	}

	return _panel;
    }
}