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
 * This class holds the data for a payment.
 */
public class PaymentImpl implements Payment {

    // Static variables

    
    // Instance variables

    /**
     * The amount to pay.
     */
    private long _amount;

    /**
     * The currency for the payment.
     */
    private Currency _currency = null;


    // Constructors

    /**
     * Create a new payment.
     *
     * @param amount The amount to pay.
     * @param currency The currency, that is used for the payment.
     */
    public PaymentImpl( long amount, Currency currency) {
	_amount = amount;
	_currency = currency;
    }


    // Methods

    /**
     * Get the amount of the payment (usually nano-coin, or so).
     *
     * @return The amount to pay.
     */
    public long getAmount() {
	return _amount;
    }

    /**
     * Get the currency of the payment.
     *
     * @return The currency for the payment.
     */
    public Currency getCurrency() {
	return _currency;
    }
}
