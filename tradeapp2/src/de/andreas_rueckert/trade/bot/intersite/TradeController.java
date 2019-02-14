/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.intersite;

import de.andreas_rueckert.trade.Amount;
import java.math.BigDecimal;


/**
 * This class controls trades on a trade path in both directions.
 */
class TradeController {

    // Inner classes

    /**
     * This class holds the trading info on a trade path.
     */
    class TradePathInfo {

	// Instance variables

	/**
	 * The maximum amount to trade.
	 */
	Amount _maxTradeAmount = null;

	/**
	 * The minimum amount to trigger a trade.
	 */
	Amount _minTradeAmount = null;

	/**
	 * The minimum trade profit in percent.
	 */
	BigDecimal _minTradeProfit = null;


	// Constructors


	// Methods
    }


    // Static variables


    // Instance variables

    /**
     * Flag to indicate, that the bot should start a transfer of funds on it's own 
     * (automatic trading).
     */
    private boolean _automaticTransfer = false;

    /**
     * Flag to indicate, if the user is notified when a trade happened.
     * This might be necessary, if the bot cannot complete a trade on it's own.
     */
    private boolean _mailNotification = false;


    // Constructors


    // Methods
}