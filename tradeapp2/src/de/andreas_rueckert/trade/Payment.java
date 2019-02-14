/**
 * Java implementation of a fee calculator.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade;

import de.andreas_rueckert.trade.currency.Currency;


/**
 * This interface holds the data for a payment.
 */
public interface Payment {

    // Variables


    // Methods

    /**
     * Get the amount of the payment (usually nano-coin, or so).
     *
     * @return The amount to pay.
     */
    public long getAmount();

    /**
     * Get the currency of the payment.
     *
     * @return The currency for the payment.
     */
    public Currency getCurrency();
}
