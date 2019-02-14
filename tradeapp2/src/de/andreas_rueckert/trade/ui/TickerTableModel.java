/**
 * Java implementation of MountGox API.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.mtgox.client.ui;

import de.andreas_rueckert.trade.CryptoCoinTrade;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;


/**
 * This class creates a model for the ticker table.
 */
class TickerTableModel extends AbstractTableModel {

    // Static variables


    // Instance variables

    /**
     * The column names of the ticker table.
     */
    private String [] _columnName = { "id", "timestamp", "type", "amount", "price", "currency" };

    /**
     * The list of trades.
     */
    private ArrayList<CryptoCoinTrade> _tradeList = new ArrayList<CryptoCoinTrade>();


    // Constructors


    // Methods

    /**
     * Get the number of columns.
     *
     * @return The number of columns.
     */
    public int getColumnCount() { 
	return _columnName.length; 
    }

    /**
     * Get the name of a given column index.
     * 
     * @param columnIndex The index of the column.
     *
     * @return The name for the given column index.
     */
    public String getColumnName(int columnIndex) {
        return _columnName[columnIndex].toString();
    }

    /**
     * Get the current number of rows (= the number of trades).
     *
     * @return The current number of rows.
     */
    public int getRowCount() { 
	return _tradeList.size(); 
    }
    
    /**
     * Get a cell value.
     *
     * @param row The row of the requested cell.
     * @param The column of the requested cell.
     *
     * @return The value of the requested cell.
     */
    public Object getValueAt( int row, int column) {
	CryptoCoinTrade trade = _tradeList.get( row);

	if( trade != null) {
	    switch( column) {  // Return the trade field for the corresponding index (see column names).
	    case 1: return trade.getId(); 
	    case 2: return trade.getTimestamp();
	    case 3: return trade.getType();
	    case 4: return trade.getAmount();
	    case 5: return trade.getPrice();
	    case 6: return trade.getCurrencyPair().getCurrency();
	    }
	}
	return null;  // No value available.
    }

    /**
     * Check, if a cell is editable. Since we only display the trade, no cell is editable.
     *
     * @param row The row of the checked cell.
     * @param column The column of the checked cell.
     *
     * @return false, since no cell can be edited.
     */
    public boolean isCellEditable( int row, int column) { 
	return false; 
    }

    /**
     * Set the value of a cell. Since our ticker is read-only, we don't set anything.
     *
     * @param value The new value.
     * @param row The row of the modified cell.
     * @param column The column of the modified cell.
     */
    public void setValueAt( Object value, int row, int column) {
    }
}