/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.intersite.ui;

import de.andreas_rueckert.trade.bot.intersite.InterSiteBot;
import de.andreas_rueckert.trade.bot.intersite.TradePath;
import de.andreas_rueckert.trade.bot.TradeBot;
import de.andreas_rueckert.trade.bot.ui.TradeBotUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * A J2EE GUI for the InterSiteBot class.
 */
public class InterSiteBotUI implements ActionListener, TradeBotUI {

    // Static variables


    // Instance variables

    /**
     * The default format for bitcoin output in this app.
     */
    private DecimalFormat _defaultDecimalFormat = new DecimalFormat("#####.########");

    /**
     * An input field list for the calculation.
     */
    private ArrayList<JTextField> _inputFields = new ArrayList<JTextField>();

    /**
     * The green background color for good results.
     */
    private Color LIGHT_GREEN = new Color( 200, 255, 200);
    
    /**
     * The red background color for bad results.
     */
    private Color LIGHT_RED = new Color( 255, 200, 200);    


    /**
     * An output field list for the calculation.
     */
    private ArrayList<JTextField> _outputFields = new ArrayList<JTextField>();

    /**
     * The button to start and stop the bot.
     */
    private JButton _startStopButton = null;

    /**
     * A reference to the trade bot, so we can check it's status etc.
     */
    private InterSiteBot _tradeBot = null;

    /**
     * The JPanel with the user interface.
     */
    private JPanel _uiPanel = null;


    // Constructors

    /**
     * Create a new UI for an intersite trading bot.
     *
     * @param tradeBot The trade bot.
     */
    public InterSiteBotUI( TradeBot tradeBot) {
	_tradeBot = (InterSiteBot)tradeBot;
    }


    // Methods

    /**
     * The user pressed a button on the UI panel.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {

	if( e.getSource() == _startStopButton) {
	    if( _tradeBot.isStopped()) {
		_tradeBot.start();
	    } else {
		_tradeBot.stop();
	    }

	    // Starting the bot might have failed, that's why I query the stopped status a second time...
	    _startStopButton.setText( _tradeBot.isStopped() ? "Start" : "Stop");
	}
    }

    /**
     * Create a panel for a trade path.
     *
     * @param tradePath The trade path to visualize.
     *
     * @return A panel to display the tradePath.
     */
    private JPanel createPanelForTradePath( TradePath tradePath) {

	JPanel result = new JPanel();

	result.add( new JLabel( tradePath.getStartSite().getName()));
	JTextField inputField = new JTextField( "-1" , 4);
	_inputFields.add( inputField);  // Add it to the list of input fields, so we can access it later.
	result.add( inputField);
	result.add( new JLabel( tradePath.getStartCurrency().getCode() 
				+ " => "
				+ tradePath.getTransferCurrency().getCode() 
				+ " => "
				+ tradePath.getTransferCurrency().getCode() 
				+ " => "
				+ tradePath.getStartCurrency().getCode()));
	JTextField outputField = new JTextField( "-1", 12);
	_outputFields.add( outputField);  // Add it to the list of output fields, so we can access it later.
	result.add( outputField);
	result.add( new JLabel( tradePath.getTargetSite().getName()));

	// Add a text field for the volume.
	result.add( new JLabel( " Volume:"));
	JTextField volumeField = new JTextField( "-1", 12);
	_outputFields.add( volumeField);
	result.add( volumeField);

	return result;
    }

    /**
     * Get the JPanel with the user interface for the bot.
     *
     * @return The JPanel with the user interface for the bot.
     */
    public JPanel getUIPanel() {

	if( _uiPanel == null) {       // if there is no UI panel yet,
	    _uiPanel = new JPanel();  // create one.
	    _uiPanel.setLayout( new BorderLayout());
	    
	    JPanel calculationPanel = new JPanel();
	    calculationPanel.setLayout( new GridLayout( _tradeBot.getTradePaths().size(), 1));

	    // Create a panel for each trade path and add it to the layout.
	    for( TradePath currentPath : _tradeBot.getTradePaths()) {
		calculationPanel.add( createPanelForTradePath( currentPath));
	    }

	    _uiPanel.add( calculationPanel, BorderLayout.CENTER);

	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add( _startStopButton = new JButton( "Start"));
	    _startStopButton.addActionListener( this);
	    _uiPanel.add( buttonPanel, BorderLayout.SOUTH);
	}
	return _uiPanel;
    }

    /**
     * Update the shown values.
     *
     * @param inputValues The input values for the calculation.
     * @param outputValues The output values of the calculation.
     */
    public void updateValues( BigDecimal [] inputValues, BigDecimal [] outputValues) {
	
	for( int i = 0; i < inputValues.length; ++i) {
	    _inputFields.get(i).setText( "" + inputValues[i]);
	    _outputFields.get(i * 2).setText( _defaultDecimalFormat.format( outputValues[ i * 2]));
	    _outputFields.get(i * 2).setBackground( outputValues[ i * 2].compareTo( inputValues[i]) > 0 ? LIGHT_GREEN : LIGHT_RED);
	}

	_uiPanel.invalidate();  // show the results.
	_uiPanel.validate();
    }
}
