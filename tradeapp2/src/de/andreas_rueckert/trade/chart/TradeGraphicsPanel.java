/**
 * Java implementation of a chart provider.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.chart;

import de.andreas_rueckert.trade.Trade;
import java.awt.Color;
import java.awt.Graphics;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.swing.JPanel;


/**
 * This panel displays the actual chart graphics.
 */
class TradeGraphicsPanel extends JPanel {

    // Inner classes

    /**
     * This class holds the data on a chart graph.
     */
    private class ChartGraphData {

	// Static variables


	// Instance variables

	/**
	 * The color to use for the graph.
	 */
	Color _color = null;

	/**
	 * The end time to draw to.
	 */
	long _endTime;

	/**
	 * The start time to draw from.
	 */
	long _startTime;

	/**
	 * The list of trades.
	 */
        Trade [] _trades = null;


	// Constructors


	// Methods
    }

    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static TradeGraphicsPanel _instance = null;


    // Instance variables

    /**
     * A list of data for each graph.
     */
    ArrayList<ChartGraphData> _chartGraphData = null;

    /**
     * The maximum vertical value.
     */
    private BigDecimal _maxVertical;
    
    /**
     * The minimum vertical value.
     */
    private BigDecimal _minVertical;


    // Constructors

    /**
     * The private constructor of this class (required for singleton pattern).
     */
    private TradeGraphicsPanel() {
	super();

	// Create a new list for the graph data.
	_chartGraphData = new ArrayList<ChartGraphData>();
    }
    

    // Methods

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static TradeGraphicsPanel getInstance() {
	if( _instance == null) {
	    _instance = new TradeGraphicsPanel();
	}
	return _instance;
    }

    /**
     * Overwrite paintComponent method to draw on the panel.
     *
     * @param g The graphics context.
     */
    protected void paintComponent( Graphics g) {
	super.paintComponent(g);

        g.drawLine(0, 0, 100, 100);
    }

    /**
     * Compute the correct scale for the graphs and store them in local vars.
     */
    private void scaleGraphs() {
	
	// Init to default values to indicate, those were not overwritten.
	_minVertical = new BigDecimal( Long.MAX_VALUE);
	_maxVertical = new BigDecimal( Long.MIN_VALUE);

	for( ChartGraphData c : _chartGraphData) {
	    
	    BigDecimal currentMinimum = ChartAnalyzer.getInstance().min( c._trades, c._startTime, c._endTime);

	    if( currentMinimum.compareTo( _minVertical) < 0) {
		_minVertical = currentMinimum;
	    }

	    BigDecimal currentMaximum = ChartAnalyzer.getInstance().max( c._trades, c._startTime, c._endTime);

	    if( currentMaximum.compareTo( _maxVertical) > 0) {
		_maxVertical = currentMaximum;
	    }
	}
    }
}