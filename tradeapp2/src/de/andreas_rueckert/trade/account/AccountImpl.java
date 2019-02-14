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
 * Base class for accounts.
 */
public class AccountImpl implements Account {

    // Static variables

    
    // Instance variables

    /**
     * The balance.
     */
    private BigDecimal _balance = BigDecimal.ZERO;

    /**
     * The currency of this account.
     */
    private Currency _currency = null;

    /**
     * The id of this account.
     */
    private String _id = null;

    /**
     * The name of this account.
     */
    private String _name = null;


    // Constructors

    /**
     * Create a new account object.
     *
     * @param balance The new balance.
     * @param currency The used currency.
     */
    public AccountImpl( BigDecimal balance, Currency currency) {
	_balance = balance;
	_currency = currency;
    }


    // Methods

    /**
     * Add a value to the current balance,
     *
     * @param value The value to add.
     */
    public void addToBalance( BigDecimal value) {
	_balance = _balance.add( value);
    }

    /**
     * Get the current balance of this account.
     *
     * @return The current balance of this account.
     */
    public BigDecimal getBalance() {
	return _balance;
    }

    /**
     * Get the currency of this account.
     *
     * @return The currency of this account.
     */
    public Currency getCurrency() {
	return _currency;
    }

    /**
     * Get the id of this account.
     *
     * @return The id of this account.
     */
    public String getId() {
	return _id;
    }
    
    /**
     * Get the name of this account.
     *
     * @return The name of this account.
     */
    public String getName() {
	return _name;
    }
    
    /**
     * Set a new balance for this account.
     *
     * @param balance The new balance for this account.
     */
    public void setBalance( BigDecimal balance) {
	_balance = balance;
    }
}
