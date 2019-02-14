/**
 * Java implementation of a bot state.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;


/**
 * The current status of a bot.
 */
public enum BotState {
    WAITING_FOR_PRICE               // The bot is waiting for the price to drop or rise to some given level.
	, WAITING_FOR_ORDER_FILL    // The bot os waiting for it's last order to fill. 
}
