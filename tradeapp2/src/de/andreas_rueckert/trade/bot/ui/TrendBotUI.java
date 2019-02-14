/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.ui;

import de.andreas_rueckert.trade.bot.TradeBot;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import javax.swing.JPanel;


/**
 * A J2EE GUI for the TrendBot class.
 */
public class TrendBotUI implements ActionListener, TradeBotUI {

    // Static variables


    // Instance variables

    /**
     * A reference to the trade bot, so we can check it's status etc.
     */
    private TradeBot _tradeBot = null;

    /**
     * The JPanel with the user interface.
     */
    private JPanel _uiPanel = null;


    // Constructors

    /**
     * Create a new UI for the trend bot.
     *
     * @param tradeBot The trade bot.
     */
    public TrendBotUI( TradeBot tradeBot) {
	_tradeBot = tradeBot;
    }


    // Methods

    /**
     * The user clicked on the UI.
     *
     * @param e The triggered action event.
     */
    public void actionPerformed( ActionEvent e) {
    }

    /**
     * Get the JPanel with the user interface for the bot.
     *
     * @return The JPanel with the user interface for the bot.
     */
    public JPanel getUIPanel() {

	if( _uiPanel == null) {       // if there is no UI panel yet,
	    _uiPanel = new JPanel();  // create one.
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
    }
}