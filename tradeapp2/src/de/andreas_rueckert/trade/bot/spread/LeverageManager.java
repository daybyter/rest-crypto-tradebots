/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import java.math.BigDecimal;


/**
 * Class to calculate leverages for trading.
 */
class LeverageManager {

    // Inner classes 

    
    // Static variables


    // Instance variables

    /**
     * The current leverage factor.
     */
    BigDecimal _leverageFactor = BigDecimal.ONE;


    // Constructors

    /**
     * Create a new leverage manager.
     *
     * @param leverageFactor The leverage of this manager.
     */
    public LeverageManager( BigDecimal leverageFactor) {

	// Store the leverage factor in the instance.
	_leverageFactor = leverageFactor;
    }


    // Methods
}
