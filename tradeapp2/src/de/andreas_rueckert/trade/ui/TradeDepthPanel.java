/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.ui;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.order.OrderBook;
import de.andreas_rueckert.trade.order.OrderFactory;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.util.graphics.CubicSpline;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import de.andreas_rueckert.util.StringUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.geom.Ellipse2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;


/**
 * A panel to display the depth with a simple trade chart, like btc-e does.
 */
public class TradeDepthPanel extends JPanel implements ActionListener {

    // Inner classes

    /**
     * A chart panel to visualize trades.
     */
    class ChartPanel extends JPanel {

	// Instance variables

	/**
	 * The end of the timespan, that the trades are in.
	 */
	long _endTimestamp;

	/**
	 * The color for the trade graph.
	 */
	private final Color _graphColor = new Color( 128, 128, 255);

	/**
	 * The start of the timespan, that the trades are in.
	 */
	long _startTimestamp;

	/**
	 * The color for the trade marks.
	 */
	private final Color _tradeColor = new Color( 255, 128, 128);

	/**
	 * An array with the list of trades to visualize.
	 */
	List<Trade> _trades = null;


	// Constructors

	/**
	 * Create a new ChartPanel instance.
	 *
	 * @param trades An array with the trades to visualize.
	 * @param startTimestamp The start of the timespan, the trades are in.
	 * @param endTimestamp The end of the timespan, the trades are in.
	 */
	ChartPanel( List<Trade> trades, long startTimestamp, long endTimestamp) {
	    super();

	    _trades = trades;
	    _startTimestamp = startTimestamp;
	    _endTimestamp = endTimestamp;

	    setPreferredSize( new Dimension( (int)( ORDER_PANEL_DIMENSION.getWidth()), 200));
	    setMinimumSize( getPreferredSize());
	}


	// Methods

	/**
	 * Draw a scale on a given panel.
	 *
	 * @param g The graphics context.
	 * @param minPrice The minimum price.
	 * @param maxPrace The maximum price.
	 */
	public void drawScale( Graphics g, long minPrice, long maxPrice) {
	    
	    long spread = maxPrice - minPrice;  // Just for performance tuning.
	    int maxY = getHeight() - 1;
	    int maxX = getWidth() - 1;

	    // Determine the correct units to use.
	    long currentUnit = 100000000;  // Start with 1 btc/ltc/<whatever>

	    // Now reduce the units, until we get some of them on the graph.
	    while( ( spread < 2 * currentUnit) && ( currentUnit > 1000)) {
		currentUnit /= 10;
	    }

	    g.setColor( new Color( 160, 160, 160));  // Draw the scale in gray.
	    g.setFont( g.getFont().deriveFont( 9.0f)); // Use a small font for the prices.
	    DecimalFormat scaleDecimalFormat = new DecimalFormat("#####.####");

	    // Now find the height, were to draw the lines.
	    for( int i = maxY; i > 0; --i) {
		long currentPrice = minPrice + (( spread * ( maxY - i)) / maxY);

		if( ( currentPrice % currentUnit) < ( spread / maxY)) {
		    g.drawLine( 0, i, maxX, i);
		    g.drawString( scaleDecimalFormat.format( ( ( currentPrice / currentUnit) * currentUnit) / 100000000.0), 0, i - 3);
		}
	    }
	}

	/**
	 * Paint the graphical chart.
	 *
	 * @param g The graphics context.
	 */
	public void paintComponent( Graphics g){
	    
	    super.paintComponent( g);

	    setBackground( Color.WHITE);

	    // System.out.println( "Drawing charts");

	    // Visualize the trades.
	    if( ( _trades != null) && ( _trades.size() > 1)) {

		// Find the maximum and minimum of the trades.
		BigDecimal maximum = new BigDecimal( Long.MIN_VALUE);  // Init with the default values.
		BigDecimal minimum = new BigDecimal( Long.MAX_VALUE);
	
		for( int currentTradeIndex = 0; currentTradeIndex < _trades.size(); ++currentTradeIndex) {
		    BigDecimal currentPrice = _trades.get( currentTradeIndex).getPrice();

		    if( currentPrice.compareTo( maximum) > 0) {
			maximum = currentPrice;
		    }

		    if( currentPrice.compareTo( minimum) < 0) {
			minimum = currentPrice;
		    }
		}

		// Make sure, minimum and maximum are at least 20 ct difference and round to 10 ct
		if( maximum.subtract( minimum).compareTo( new BigDecimal( "0.2")) <  0) {
		    BigDecimal diff = new BigDecimal( "0.2").subtract( maximum.subtract( minimum));

		    maximum = maximum.add( diff.divide( new BigDecimal( "2"))); // Distribute the change to both sides.
		    minimum = minimum.subtract( diff.divide( new BigDecimal( "2")));
		}
		if( maximum.remainder( new BigDecimal( "0.1")).compareTo( BigDecimal.ZERO) != 0) {
		    maximum.subtract( maximum.remainder( new BigDecimal( "0.1")));
		    maximum.add( new BigDecimal( "0.1"));
		}
		if( minimum.remainder( new BigDecimal( "0.1")).compareTo( BigDecimal.ZERO) != 0) {
		    minimum.subtract( minimum.remainder( new BigDecimal( "0.1")));
		}


		// ToDo: rewrite the following code using BigDecimal for _all_ calculations? (no int, long etc anymore?)
		
		// Now convert all the trades to points.
		int maxX = getWidth() - 1;  // The max x and y coordinates.
		// System.out.println( "Max x is: " + maxX);
		BigDecimal maxY = new BigDecimal( getHeight() - 1);
		BigDecimal spread = maximum.subtract( minimum);
		long timespan = _endTimestamp - _startTimestamp;
		// System.out.println( "Start time is: " + _startTimestamp);
		// System.out.println( "Timespan is: " + timespan);
		// System.out.println( "Timespan is: " + timespan);
		Point [] tradePoints = new Point[ _trades.size()];
		for( int currentTradeIndex = 0; currentTradeIndex < _trades.size(); ++currentTradeIndex) {
		    Trade currentTrade = _trades.get( currentTradeIndex);

		    // System.out.println( "Price is: " + currentTrade.getPrice());

		    int pointX = (int)( ( (long)maxX * ( currentTrade.getTimestamp() - _startTimestamp)) / timespan);

		    // System.out.println( "Current x is " + pointX);
		    
		    int pointY = maximum.subtract( currentTrade.getPrice()).multiply( maxY).divide( spread).intValue();

		    // System.out.println( "Current y is: " + pointY);

		    tradePoints[ currentTradeIndex] = new Point( pointX, pointY);
		}
		// System.out.println( "Converted " + tradePoints.length + " trades to points");

		// Draw a scale 
		drawScale( g, minimum.longValue(), maximum.longValue());

		// Draw the actual curve from the trades.
		paintTradeCurve( g, tradePoints);
	    } else {  // We cannot draw a graph.
		g.drawString( "Insufficient data for graph", 10, 10);
	    }
	}

	/**
	 * Paint a curve from the converted trades.
	 *
	 * @param g The graphics context.
	 * @param tradePoints The trades converted to points.
	 */
	private void paintTradeCurve( Graphics g, Point [] tradePoints) {

	    // Create a 2D context for sophisticated graphics operations.
	    Graphics2D g2 = (Graphics2D)g;

	    // Activate antialiasing for optimzed look.
	    g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	    final BasicStroke graphStroke = new BasicStroke( 3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

	    g2.setStroke(graphStroke);

	    // Paint the chart in blue for now.
	    g2.setColor( _graphColor);

	    // Convert the trade points to a b spline curve.
	    Point [] drawPoints = CubicSpline.getSplineCurve( tradePoints);

	    // Scale the price to the panel height.
	    for( int currentPointIndex = 1; currentPointIndex < drawPoints.length; ++currentPointIndex) {
		    
		// Draw a line to the previous point.
		g2.drawLine( drawPoints[ currentPointIndex - 1].x
			     , drawPoints[ currentPointIndex - 1].y
			     , drawPoints[ currentPointIndex].x
			     , drawPoints[ currentPointIndex].y);
	    } 

	    // Now mark the trades as red dots

	    g2.setColor( _tradeColor);

	    final double radius = 2;
	    for( int currentPointIndex = 0; currentPointIndex < tradePoints.length; ++currentPointIndex) {
		g2.draw( new Ellipse2D.Double( tradePoints[ currentPointIndex].getX() - radius
					       , tradePoints[ currentPointIndex].getY() - radius
					       , 2.0 * radius
					       , 2.0 * radius));
	    }
	}
    }


    /**
     * A table model for the depth.
     */
    class DepthTableModel extends AbstractTableModel {

	// Instance variables

	/**
	 * The column names.
	 */
	private String [] _columnNames = { "price", "amount", "total"}; 
      
	/**
	 * The orders as a collection.
	 */
	private List<DepthOrder> _orders = null;


	// Constructors

	/**
	 * Create a new table model for the depth orders.
	 *
	 * @param orders The orders to display.
	 */
	DepthTableModel( List<DepthOrder> orders) {
	    _orders = orders;
	}


	// Methods
	 
	/**
	 * Get the number of columns.
	 *
	 * @return The number of columns.
	 */
	public int getColumnCount() {
	    return _columnNames.length;
	}

	/**
	 * Get the number of rows.
	 *
	 * @return The number of rows of this table.
	 */
	public int getRowCount() {
	    return _orders.size();
	}
	
	/**
	 * Get the column name for a given index.
	 *
	 * @param column The index of the column.
	 *
	 * @return The column name for a given index.
	 */
	public String getColumnName( int column) {
	    return _columnNames[ column];
	}

	/**
	 * Get the value of a given table cell.
	 *
	 * @param row The row index of the cell.
	 * @param column The column index of the cell.
	 *
	 * @return The value for the cell.
	 */
	public Object getValueAt( int row, int column) {
	    DepthOrder order = _orders.get( row);

	    switch( column) {
	    case 0: return order.getPrice().toPlainString();
	    case 1: return order.getAmount().toPlainString();
	    default: return order.getPrice().multiply( order.getAmount()).toPlainString();
	    }
	}

	/**
	 * Get the class of a given column.
	 *
	 * @param column The column index.
	 *
	 * @return The class of this column.
	 */
	public Class getColumnClass( int column) {
	    String dummy = new String();

	    return dummy.getClass();
	}
    }


    /**
     * A thread type to fetch a depth in parallel.
     */
    class DepthFetchThread extends Thread {

	// Instance variables

	/**
	 * The currency pair to query.
	 */
	CurrencyPair _currencyPair;

	/**
	 * The depth to fetch.
	 */
	Depth _depth = null;

	/**
	 * The trade site to query.
	 */
	TradeSite _tradeSite;


	// Constructors

	/**
	 * Create a new thread to fetch a depth.
	 *
	 * @param tradeSite The trade site to query.
	 * @param currencyPair The currency pair to query.
	 */
	DepthFetchThread( TradeSite tradeSite, CurrencyPair currencyPair) {
	    
	    super( "DepthFetcher");

	    _tradeSite = tradeSite;
	    _currencyPair = currencyPair;
	}

	// Methods

	/**
	 * Get the fetched depth.
	 *
	 * @return The fetched depth.
	 */
	public final Depth getDepth() {
	    return _depth;
	}
	
	/**
	 * The actual code of the the thread.
	 */
	@Override public void run() {
	    _depth =  ChartProvider.getInstance().getDepth( _tradeSite, _currencyPair);
	}
    }

    
    /**
     * A thread type to create a panel from fetched data.
     */
    class PanelCreateThread extends Thread {

	// Instance variables

	/**
	 * The currency pair to query.
	 */
	CurrencyPair _currencyPair;

	/**
	 * The panel to fetch.
	 */
	JPanel _panel = null;

	/**
	 * The trade site to query.
	 */
	TradeSite _tradeSite;


	// Constructors

	PanelCreateThread( TradeSite tradeSite, CurrencyPair currencyPair) {

	    super( "PanelCreator");

	    _tradeSite = tradeSite;
	    _currencyPair = currencyPair;
	}


	// Methods

	/**
	 * Get the fetched panel.
	 *
	 * @return The fetched panel.
	 */
	JPanel getPanel() {
	    return _panel;
	}

	/**
	 * Get the name of the panel (= name of the queried tradesite).
	 *
	 * @return The name of the panel.
	 */
	String getPanelName() {
	    return _tradeSite.getName();
	}

	/**
	 * The actual code of the the thread.
	 */
	@Override public void run() {
	    _panel = getTradeDepthPanel( _tradeSite, _currencyPair);
	}
    }


    /**
     * A thread type to fetch trades in parallel.
     */
    class TradesFetchThread extends Thread {

	// Instance variables

	/**
	 * The currency pair to query.
	 */
	CurrencyPair _currencyPair;

	/**
	 * The timestamp of the oldest trade to fetch.
	 */
	long _startTimestamp;

	/**
	 * The fetched trades.
	 */
	List<Trade> _trades = null;

	/**
	 * The trade site to query.
	 */
	TradeSite _tradeSite;


	// Constructors

	/**
	 * Create a new thread to fetch trades.
	 *
	 * @param tradeSite The trade site to query.
	 * @param startTimestamp The timestamp of the oldest trade to fetch.
	 * @param currencyPair The currency pair to query.
	 */
	public TradesFetchThread( TradeSite tradeSite, long startTimestamp, CurrencyPair currencyPair) {
	    super( "TradesFetcher");

	    _tradeSite = tradeSite;
	    _startTimestamp = startTimestamp;
	    _currencyPair = currencyPair;
	}


	// Methods

	/**
	 * Get the fetched trades.
	 *
	 * @return The fetched trades.
	 */
	public List<Trade> getTrades() {
	    return _trades;
	}

	/**
	 * The actual code of the the thread.
	 */
	@Override public void run() {
	    _trades =  ChartProvider.getInstance().getTrades( _tradeSite, _currencyPair, _startTimestamp);
	}
    }


    // Static variables

    /**
     * The total dimension of the depth panel.
     */
    private static Dimension ORDER_PANEL_DIMENSION = new Dimension( 1000, 700);

    /**
     * The only instance of this class (singleton pattern).
     */
    private static TradeDepthPanel _instance = null;


    // Instance variables

    /**
     * All supported currency pairs for all trade sites.
     */
    private CurrencyPair [] _allSupportedCurrencyPairs = null;

    /**
     * Let the user select the currency pair via a combo box.
     */
    private JComboBox _currencyPairSelector;

    /**
     * The currently selected currency pair.
     */
    private CurrencyPair _currentCurrencyPair = null;

    /**
     * The tabbed pane with the depth panels.
     */
    private JTabbedPane _tabbedPane = null;

    /**
     * The user account for trading.
     */
    private TradeSiteUserAccount _userAccount = null;

    /**
     * Create a map for all the textfields, so there aren't lots of textfield
     * variables defined.
     */
    private Map<String, JTextField> _panelFields = new HashMap<String, JTextField>();


    // Constructors

    /**
     * Create a new panel instance. Constructor is private for singleton pattern.
     */
    private TradeDepthPanel() {
	super();

	// Create a tabbed pane with a tab for each trading site.
	_tabbedPane = new JTabbedPane();

	// Use a border layout to place the UI elements.
	setLayout( new BorderLayout());

	// Make a selector for all the available currency pairs.
	add( _currencyPairSelector = new JComboBox( getAllSupportedCurrencyPairs()), BorderLayout.NORTH);
	_currencyPairSelector.addActionListener( this);

	if( getAllSupportedCurrencyPairs().length > 0) {
	    setCurrencyPair( getAllSupportedCurrencyPairs()[0]);
	}

	add( _tabbedPane, BorderLayout.CENTER);  // Add the tabs to this panel.
    }


    // Methods
    
    /**
     * The user selected a new currency pair.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {

	// Did the user select a new currency pair?
        if( e.getSource() == _currencyPairSelector) {
	    setCurrencyPair( (CurrencyPair)_currencyPairSelector.getSelectedItem());	    
	}

	if( e.getActionCommand().equalsIgnoreCase( "Buy")) {
	    createOrderFromForm( true);
	}
	
	if( e.getActionCommand().equalsIgnoreCase( "Sell")) {
	    createOrderFromForm( false);
	}

	// Does the user want to set new order on the current spread?
	if( e.getActionCommand().equals( "Set on spread")) {

	    // Find the spread button for the current trading site.
	    // The title of a tab is the name of the trading site, so we can find the textfield via the tab name.
	    Amount amount = new Amount( "-1");
	    JTextField spreadAmountField = _panelFields.get( _tabbedPane.getTitleAt( _tabbedPane.getSelectedIndex()) + "_spreadAmountField");
	    if( spreadAmountField != null) {
		try {
		    amount = new Amount( spreadAmountField.getText());

		    System.out.println( "Setting " + amount + " on spread");
		    
		    // Get the current trade site.
		    TradeSite currentTradeSite = ModuleLoader.getInstance().getRegisteredTradeSite( _tabbedPane.getTitleAt( _tabbedPane.getSelectedIndex()));

		    if( currentTradeSite != null) {

			// Get the depth again from the chart provider.
			Depth depth = ChartProvider.getInstance().getDepth( currentTradeSite, _currentCurrencyPair);
			
			if( depth != null) {
			    List<DepthOrder> buyOrders = depth.getBuyOrders();
			    
			    if( buyOrders != null && buyOrders.size() > 0) {
				Price highestBuyPrice = buyOrders.get( 0).getPrice();

				// Now check the funds for that currency.
				if( TradeApp.getApp().getAccountManager().getBalance( currentTradeSite
										      , _currentCurrencyPair.getPaymentCurrency()).compareTo( amount.multiply( highestBuyPrice)) < 0) {
				    System.err.println( "Not enough funds for buy order");
				} else {

				    System.out.println( "Buying " + amount + " of " 
							+ _currentCurrencyPair.getCurrency().getCode() 
							+ " for " 
							+ highestBuyPrice);

				    // Add a new order to the order book.
				    String orderId = getOrderBook().add( OrderFactory.createCryptoCoinTradeOrder( currentTradeSite
														  , _userAccount
														  , OrderType.BUY
														  , highestBuyPrice
														  , _currentCurrencyPair
														  , amount));
				
				    if( orderId != null) {  // Adding the order worked?
					
					// Execute the order.
					OrderStatus newOrderStatus = getOrderBook().executeOrder( orderId);
				    }
				}
			    }
		
			    List<DepthOrder> sellOrders = depth.getSellOrders();
			    
			    if( sellOrders != null && sellOrders.size() > 0) {
				Price lowestSellPrice = sellOrders.get( 0).getPrice();
				
				// Now check the funds for that currency.
				if( TradeApp.getApp().getAccountManager().getBalance( currentTradeSite
										      , _currentCurrencyPair.getCurrency()).compareTo( amount) < 0) {
				    System.err.println( "Not enough funds for sell order");
				} else {

				    System.out.println( "Selling " + amount + " of " 
							+ _currentCurrencyPair.getCurrency().getCode() 
							+ " for " 
							+ lowestSellPrice);

				    // Add a new order to the order book.
				    String orderId = getOrderBook().add( OrderFactory.createCryptoCoinTradeOrder( currentTradeSite
														  , _userAccount
														  , OrderType.SELL
														  , lowestSellPrice
														  , _currentCurrencyPair
														  , amount));
				
				    if( orderId != null) {  // Adding the order worked?
					
					// Execute the order.
					OrderStatus newOrderStatus = getOrderBook().executeOrder( orderId);
				    }
				}
			    }
			}
		    }
		} catch( NumberFormatException nfe) {
		    System.err.println( "Cannot parse entered amount: " + nfe);
		}
	    }
	}

	if( e.getActionCommand().equals( "Update panel")) {  // Check, if the user wants to update the panel data.

	    // For now, the current tab is just removed and recreated. An real update of the tab data
	    // would be better, but would require more modifications to the sources... :-(
	    
	    // Get the current trade site.
	    TradeSite currentTradeSite = ModuleLoader.getInstance().getRegisteredTradeSite( _tabbedPane.getTitleAt( _tabbedPane.getSelectedIndex()));	    
	    
	    int selectedIndex = _tabbedPane.getSelectedIndex();

	    // Now remove the current tab.
	    _tabbedPane.removeTabAt( selectedIndex);

	    // Add the new panel at the previous index.
	    _tabbedPane.insertTab( currentTradeSite.getName(), null, getTradeDepthPanel( currentTradeSite, _currentCurrencyPair), null, selectedIndex);  

	    // Select the updated panel
	    _tabbedPane.setSelectedIndex( selectedIndex);
	}

	if( e.getActionCommand().equals( "Your open orders")) {  // Hack: check the open orders of the user.

	    // Get the current trade site.
	    TradeSite currentTradeSite = ModuleLoader.getInstance().getRegisteredTradeSite( _tabbedPane.getTitleAt( _tabbedPane.getSelectedIndex())); 

	    // Get the open orders from the trade site.
	    // Better use a user account here?
	    Collection<SiteOrder> openOrders = currentTradeSite.getOpenOrders( null);
	}
    }
    
    /**
     * Create an order for a buy or sell order from the form.
     *
     * @param buy true, if we want to create a buy order. false for a sell order.
     */
    private void createOrderFromForm( boolean buy) {

	// Get the amount and price for the order
	JTextField amountField = _panelFields.get( ( buy ? "buy" : "sell") + "_amount_" + _currentCurrencyPair.getCurrency().getCode());
	JTextField priceField = _panelFields.get( ( buy ? "buy" : "sell") + "_price_" +  _currentCurrencyPair.getCurrency().getCode());
	
	if( ( amountField != null) && ( priceField != null)) {

	    try {
		// Get the amount and the price from the form.
		Amount amount = new Amount( amountField.getText());
		Price price = new Price( priceField.getText());
		    
		// Get the current trade site.
		TradeSite currentTradeSite = ModuleLoader.getInstance().getRegisteredTradeSite( _tabbedPane.getTitleAt( _tabbedPane.getSelectedIndex()));

		getOrderBook().add( OrderFactory.createCryptoCoinTradeOrder( currentTradeSite
									     , _userAccount
									     , buy ? OrderType.BUY : OrderType.SELL
									     , price
									     , _currentCurrencyPair
									     , amount)); 
	    } catch( NumberFormatException nfe) {
		LogUtils.getInstance().getLogger().error( "Cannot parse amount or price for " + ( buy ? "buy" : "sell") + " order: " + nfe);
	    }   
	}
    }

    /**
     * Get all the supported currency pairs from all the trade sites.
     *
     * @return All the supported currency pairs from all trade sites.
     */
    public CurrencyPair [] getAllSupportedCurrencyPairs() {

	if( _allSupportedCurrencyPairs == null) {
	    _allSupportedCurrencyPairs = mergeAllCurrencyPairs();
	}
	return _allSupportedCurrencyPairs;
    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static TradeDepthPanel getInstance() {
	
	if( _instance == null) {                // If there is no instance yet,
	    _instance = new TradeDepthPanel();  // create one.
	}
	return _instance;                       // Return the only instance of this class.
    }

    /**
     * Get an order book to add orders.
     *
     * @return An order book.
     */
    private final OrderBook getOrderBook() {
	return CryptoCoinOrderBook.getInstance();
    }

    /**
     * Create a panel with an order form to buy or sell coins.
     *
     * @param buy Flag to indicate, that this is a buy form. If false, create a sell form.
     * @param currencyPair The currency pair to use.
     *
     * @return The created form.
     */
    private final JPanel getOrderPanel( boolean buy, CurrencyPair currencyPair) {
	JPanel result = new JPanel();

	result.setLayout( new GridBagLayout());

        result.setPreferredSize( new Dimension( ORDER_PANEL_DIMENSION.width / 2 - 20, 180));
	result.setMinimumSize( result.getPreferredSize());

	GridBagConstraints c1 = new GridBagConstraints();
	c1.gridx = 0;
	c1.gridy = 0;
	c1.weighty = 0.15;
	c1.gridwidth = 1;

	// This is the panel header
	result.add( new JLabel( ( buy ? "Buy" : "Sell") + " " + currencyPair.getCurrency().getCode()), c1);

	// Now the status of funds and price.
	JPanel fundsPanel = new JPanel();
	fundsPanel.setPreferredSize( new Dimension( ORDER_PANEL_DIMENSION.width / 2 - 20, 40));
	fundsPanel.setMinimumSize( fundsPanel.getPreferredSize());

	fundsPanel.add( new JLabel( "Your funds:"));

	JTextField fundsAmount = new JTextField( 14);
	fundsAmount.setEditable( false);
	fundsPanel.add( fundsAmount);

	c1.gridy = 1;
	result.add( fundsPanel, c1);

	JPanel orderFormPanel = new JPanel();
	orderFormPanel.setLayout( new GridBagLayout());

	orderFormPanel.setPreferredSize( new Dimension( ORDER_PANEL_DIMENSION.width / 2 - 20, 110));
	orderFormPanel.setMinimumSize( orderFormPanel.getPreferredSize());

	GridBagConstraints c = new GridBagConstraints();
	c.gridx = 0;
	c.gridy = 0;
	c.gridwidth = 1;
	orderFormPanel.add( new JLabel( "Amount " + currencyPair.getCurrency().getCode() + ":"), c);
	JTextField amountField = new JTextField( 14);
	_panelFields.put( ( buy ? "buy" : "sell") + "_amount_" + currencyPair.getCurrency().getCode(), amountField);
	c.gridx = 1;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridy = 0;
	c.gridwidth = 2;
	orderFormPanel.add( amountField, c);
	c.gridx = 0;
	c.gridy = 1;
	c.gridwidth = 1;
	orderFormPanel.add( new JLabel( "Price per " + currencyPair.getCurrency().getCode() + ":"), c);
	JTextField priceField = new JTextField( 14);
	_panelFields.put( ( buy ? "buy" : "sell") + "_price_" + currencyPair.getCurrency().getCode(), amountField);
	c.gridx = 1;
	c.gridy = 1;
	c.gridwidth = 1;
	orderFormPanel.add( priceField, c);
	c.gridx = 2;
	c.gridy = 1;
	c.gridwidth = 1;
	orderFormPanel.add( new JLabel( currencyPair.getPaymentCurrency().getCode()), c);
	c.gridx = 0;
	c.gridy = 2;
	c.gridwidth = 1;
	orderFormPanel.add( new JLabel( "Total:"), c);
	JTextField totalField = new JTextField( 14);
	totalField.setEditable( false);
	c.gridx = 1;
	c.gridy = 2;
	c.gridwidth = 2;
	orderFormPanel.add( totalField, c);
	c.gridx = 0;
	c.gridy = 3;
	c.gridwidth = 1;
	orderFormPanel.add( new JLabel( "Fee:"), c);
	JTextField feeField = new JTextField( 14);
	feeField.setEditable( false);
	c.gridx = 1;
	c.gridy = 3;
	c.gridwidth = 2;
	orderFormPanel.add( feeField, c);
	JButton orderButton = new JButton( buy ? "Buy" : "Sell");
	c.gridx = 2;
	c.gridy = 4;
	c.gridwidth = 1;
	orderFormPanel.add( orderButton, c);

	c1.gridy = 2;
	c1.weighty = 0.7;
	result.add( orderFormPanel, c1);

	return result;
    }

    /**
     * Get a JTable object, that displays some given orders.
     *
     * @param orders The orders to display.
     *
     * @return The table with the order data.
     */
    private final JTable getOrderTable( List<DepthOrder> orders) {
	return new JTable( new DepthTableModel( orders));
    }
    
    /**
     * Get a table of orders.
     *
     * @param orders The orders to display.
     *
     * @return A panel with the sell orders.
     */
    private final JPanel getTablePanel( List<DepthOrder> orders) {
	
	JPanel tablePanel = new JPanel();

	tablePanel.setPreferredSize( new Dimension(  (int)( ORDER_PANEL_DIMENSION.getWidth() / 2.05), (int)( ORDER_PANEL_DIMENSION.getHeight() / 2 - 40)));
	tablePanel.setMinimumSize( tablePanel.getPreferredSize());

	JTable table = getOrderTable( orders);

	// Set the widths of the columns
	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	table.getColumnModel().getColumn(0).setPreferredWidth( 150);
	table.getColumnModel().getColumn(1).setPreferredWidth( 150);
	table.getColumnModel().getColumn(2).setPreferredWidth( 150);


	tablePanel.add( new JScrollPane( table));
	table.setFillsViewportHeight( true);  // Fill the entire scroll pane.

	return tablePanel;
    }

    /**
     * Get a depth panel for a trade site and a currency pair.
     *
     * @param tradeSite The trade site for the trades.
     * @param currencyPair The currency pair to trade.
     *
     * @return A depth panel for the trade site.
     */
    JPanel getTradeDepthPanel( final TradeSite tradeSite, final CurrencyPair currencyPair) {

	final long endTimestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros();
	final long startTimestamp = endTimestamp - 1L * 60L * 60L * 1000000L;

	// Fetch the depth and trades in parallel for speedup.

	// Try to get the depth for this tradesite and currency pair.
	DepthFetchThread depthThread = new DepthFetchThread( tradeSite, currencyPair);
	depthThread.start();

	// Try to get the trades from the trading site for this currency pair and the last 6 hours.
	TradesFetchThread tradeThread = new TradesFetchThread( tradeSite, startTimestamp, currencyPair);
	tradeThread.start();

	// Now wait for the 2 threads to finish.
	try {
	    depthThread.join();
	    tradeThread.join();
	} catch( InterruptedException ie)  {
	    System.err.println( "Depth of trade fetch join in TradeDepthPanel interrupted: " + ie.toString());
	}

	// Fetch the data from the threads.
	Depth depth = depthThread.getDepth();
	List<Trade> trades = tradeThread.getTrades();

	if( depth == null) {
	    LogUtils.getInstance().getLogger().error( "ChartProvider returned depth null");
	    System.out.println( "Returned depth is null");
	}

	if( trades == null) {
	    LogUtils.getInstance().getLogger().error( "ChartProvider returned no trades for the last 8 hours for tradesite: " + tradeSite.getName());
	}

	JPanel depthPanel = new JPanel();  // Create a new panel for this trading site.
	
	depthPanel.setLayout( new GridBagLayout());  // There's no vertical FlowLayout, so we have to use GridBagLayout.
	
	GridBagConstraints c = new GridBagConstraints();

	ChartPanel tradeChartPanel = new ChartPanel( trades, startTimestamp, endTimestamp);
	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 1.0;
	c.weighty = 0.3;
	c.gridwidth = 2;
	depthPanel.add( tradeChartPanel, c);

	JPanel buyOrderPanel = getOrderPanel( true, currencyPair);
	c.gridx = 0;
	c.weightx = 1.0;
	c.gridy = 1;
	c.weighty = 0.3;
	c.gridwidth = 1;
	depthPanel.add( buyOrderPanel, c);

	JPanel sellOrderPanel = getOrderPanel( false, currencyPair);
	c.gridx = 1;
	c.weightx = 1.0;
	c.gridy = 1;
	c.weighty = 0.3;
	c.gridwidth = 1;
	depthPanel.add( sellOrderPanel, c);

	// Panel to speed up arbitrage trading
	JPanel fastTradePanel = new JPanel();
	fastTradePanel.setLayout( new FlowLayout());
	fastTradePanel.setPreferredSize( new Dimension( (int)( ORDER_PANEL_DIMENSION.getWidth() / 1.1), 40));
	fastTradePanel.setMinimumSize( fastTradePanel.getPreferredSize());
	JButton updateButton = new JButton( "Update panel");
	fastTradePanel.add( updateButton);
	updateButton.addActionListener( this);
	JTextField spreadAmountField = new JTextField( "0.1", 10);
	fastTradePanel.add( spreadAmountField);
	_panelFields.put( tradeSite.getName() + "_spreadAmountField", spreadAmountField);
	JButton spreadSetButton = new JButton( "Set on spread");
	fastTradePanel.add( spreadSetButton);
	spreadSetButton.addActionListener( this);
	JButton openOrdersButton = new JButton( "Your open orders");  // <- this is an hack to test the APIs. Better put the orders in their own panel!
	fastTradePanel.add( openOrdersButton);
	openOrdersButton.addActionListener( this);
	c.gridx = 0;
	c.weightx = 0;
	c.gridy = 2;
	c.weighty = 0.1;
	c.gridwidth = 2;
	depthPanel.add( fastTradePanel, c);

	c.gridx = 0;
	c.weightx = 0;
	c.gridy = 3;
	c.weighty = 0.1;
	c.gridwidth = 1;
	depthPanel.add( new JLabel( "Sell orders"), c);

	c.gridx = 1;
	c.weightx = 0;
	c.gridy = 3;
	c.weighty = 0.1;
	c.gridwidth = 1;
	depthPanel.add( new JLabel( "Buy orders"), c);

	JPanel sellDepthPanel = null;

	if( depth != null) {  // If we have a depth, add the sells to the panel.
	    sellDepthPanel = getTablePanel( depth.getSellOrders());
	
	    c.gridx = 0;
	    c.gridy = 4;
	    c.weighty = 0.2;
	    c.gridwidth = 1;

	    depthPanel.add( sellDepthPanel, c);
	}
	    
	JPanel buyDepthPanel = null;

	if( depth != null) {  // If we have a depth, add the buys to the panel.
	    buyDepthPanel = getTablePanel( depth.getBuyOrders());  

	    c.gridx = 1;
	    c.gridy = 4;
	    c.weighty = 0.2;
	    c.gridwidth = 1;

	    depthPanel.add( buyDepthPanel, c);
	}

	depthPanel.setPreferredSize( new Dimension( (int)( ORDER_PANEL_DIMENSION.getWidth()), (int)( ORDER_PANEL_DIMENSION.getHeight()))); 

	return depthPanel;
    }

    /**
     * Merge all the currency pairs in one single array.
     *
     * @return A sorted array with all the currency pair from all trading sites.
     */
    private CurrencyPair [] mergeAllCurrencyPairs() {
	
	// A buffer for the result.
	ArrayList<CurrencyPair> resultBuffer = new ArrayList<CurrencyPair>();

	// Get all the currency pairs from all the trading sites...
	for( TradeSite t : ModuleLoader.getInstance().getRegisteredTradeSites().values()) {

	    CurrencyPair [] supportedPairs = t.getSupportedCurrencyPairs();

	    for( CurrencyPair pair : supportedPairs) {  // Merge all the pairs into the buffer.

		boolean addPair = true;

		for( CurrencyPair p : resultBuffer) {
		    if( p.equals( pair)) {
			addPair = false;
			break;
		    }
		}

		if( addPair) {
		    resultBuffer.add( pair);
		}
	    }
	}

	// Now sort the whole result buffer.
	Collections.sort( resultBuffer);

	// Convert the result buffer to an array and return it.
	return resultBuffer.toArray( new CurrencyPairImpl [ resultBuffer.size()]);
    }

    /**
     * Set a new currency pair for the trading.
     *
     * @param currencyPair The new currency pair to use for trading.
     */
    private void setCurrencyPair( CurrencyPair currencyPair) {

	_currentCurrencyPair = currencyPair;  // Store the selected currency pair.
	
	_tabbedPane.removeAll();  // Remove all the tabs from the previous currency pair.
	
	_panelFields.clear();  // Remove all the panel field from the cache.

	// Create a list of threads to fetch and create all the data in parallel.
	ArrayList<PanelCreateThread> panelCreateThreads = new ArrayList<PanelCreateThread>();

	for( TradeSite t : ModuleLoader.getInstance().getRegisteredTradeSites().values()) {  // Loop over all the trading sites.
	    if( t.isSupportedCurrencyPair( currencyPair)) {  // If this trading site supports the currency pair.

		// Create a new thread to create the panel.
		PanelCreateThread newThread = new PanelCreateThread( t, currencyPair);

		newThread.start();  // Start it.
		panelCreateThreads.add( newThread);  // And add it to the list of threads.
	    }
	}

	// Now wait for all the fetch threads to finish.
	for( Thread createThread : panelCreateThreads) {
	    try {
		createThread.join();
	    } catch( InterruptedException ie)  {
		System.err.println( "Panel fetch join interrupted: " + ie.toString());
	    }
	}

	// Now add the new panels to the tabbed pane.
	for( PanelCreateThread createThread : panelCreateThreads) {

	    if( createThread.getPanel() != null) {                                  // If the panel exists,
		_tabbedPane.add( createThread.getPanelName(), createThread.getPanel());  // add it to the tabbed pane.
	    }
	}
    }
}
