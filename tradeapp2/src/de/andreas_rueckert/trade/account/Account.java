/**
 * Java implementation of an account (to be used for currencies etc).
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.account;

import de.andreas_rueckert.trade.currency.Currency;
import java.math.BigDecimal;


/**
 * Interface for accounts.
 */
public interface Account {

    // Static variables

    
    // Instance variables


    // Constructors


    // Methods

    /**
     * Add a value to the current balance,
     *
     * @param value The value to add.
     */
    public void addToBalance( BigDecimal value);

    /**
     * Get the current balance of this account.
     *
     * @return The current balance of this account.
     */
    public BigDecimal getBalance();

    /**
     * Get the currency of this account.
     *
     * @return The currency of this account.
     */
    public Currency getCurrency();

    /**
     * Get the id of this account.
     *
     * @return The id of this account.
     */
    public String getId();
    
    /**
     * Get the name of this account.
     *
     * @return The name of this account.
     */
    public String getName();

    /**
     * Set a new balance for this account.
     *
     * @param balance The new balance for this account.
     */
    public void setBalance( BigDecimal balance);
}
