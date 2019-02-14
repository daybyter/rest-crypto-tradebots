/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.ui;

import java.math.BigDecimal;
import javax.swing.JPanel;


/**
 * This class defines a simple interface for trade bots.
 */
public interface TradeBotUI {

    // Methods

    /**
     * Get an UI panel from the bot interface.
     *
     * @return an JPanel object to control the bot and get
     *         some infos on it's activities.
     */
    public JPanel getUIPanel();

    /**
     * Update the shown values.
     *
     * @param inputValues The input values for the calculation.
     * @param outputValues The output values of the calculation.
     */
    public void updateValues( BigDecimal [] inputValues, BigDecimal [] outputValues);
}