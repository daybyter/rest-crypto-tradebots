/**
 * Java implementation of MountGox API.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.ui;

import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.Trade;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;


/**
 * This class creates a model for the trade table.
 */
public class TradeTableModel extends AbstractTableModel {

    // Static variables


    // Instance variables

    /**
     * The column names of the ticker table.
     */
    private String [] _columnName = { "id", "timestamp", "type", "amount", "price", "currency" };

    /**
     * Interface to the trading website.
     */
    private TradeSite _tradeSite = null;

    /**
     * The list of trades.
     */
    private List<Trade> _tradeList = new ArrayList<Trade>();


    // Constructors

    /**
     * Create a new trade table for a given trading site.
     *
     * @param tradingSite The interface to the trading website.
     */
    public TradeTableModel( TradeSite tradeSite) {
	super();
	_tradeSite = tradeSite;
    }


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
     * Get the trade site of this model.
     *
     * @return The trade site of this model.
     */
    public TradeSite getTradeSite() {
	return _tradeSite;
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
	CryptoCoinTrade trade = (CryptoCoinTrade)_tradeList.get( row);

	if( trade != null) {
	    switch( column) {  // Return the trade field for the corresponding index (see column names).
	    case 0: return trade.getId(); 
	    case 1: return trade.getTimestamp();
	    case 2: return trade.getType();
	    case 3: return trade.getAmount();
	    case 4: return trade.getPrice();
	    case 5: return trade.getCurrencyPair().getCurrency();
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

    /**
     * Update the list of trades.
     */
    public void update() {

	if( _tradeSite != null) {  // If we have a connection to the trading website.

	    List<Trade> trades = _tradeSite.getTrades( 0, null);
	    
	    if( trades != null) {  // If we got returned a trade list.
		for( Trade t : trades) {              // Add all the trades to the list,
		    if( ! _tradeList.contains( t)) {  // that are not yet in the list of trades.
			_tradeList.add( t);
		    }
		}
	    }
	}
    }
}