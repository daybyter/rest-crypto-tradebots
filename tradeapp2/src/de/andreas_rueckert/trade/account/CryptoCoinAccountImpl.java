/**
 * Java implementation of a cryptocoin account (to be used for currencies etc).
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.account;

import de.andreas_rueckert.trade.currency.Currency;
import java.math.BigDecimal;


/**
 * Implementation of a cryptocoin account.
 */
public class CryptoCoinAccountImpl extends AccountImpl implements CryptoCoinAccount {

    // Static variables


    // Instance variables

    /**
     * The cryptocoin address of this account (i.e. bitcoin address, litecoin address etc).
     */
    private String _cryptoCoinAddress = null;


    // Constructors

    /**
     * Create a new cryptocoin account object.
     *
     * @param cryptoCoinAddress The cryptocoin address to use for this account.
     * @param balance The balance as nano-coins, or so.
     * @param currency The used currency.
     */
    public CryptoCoinAccountImpl( String cryptoCoinAddress, BigDecimal balance, Currency currency) {
	super( balance, currency);

	setCryptoCoinAddress( cryptoCoinAddress);
    }

    
    // Methods

    /**
     * Get the cryptocoin address of this account.
     *
     * @return The cryptocoin address of this account.
     */
    public String getCryptoCoinAddress() {
	return _cryptoCoinAddress;
    }

    /**
     * Set a new address for this account.
     *
     * @param cryptoCoinAddress The new address for this account.
     */
    public void setCryptoCoinAddress( String cryptoCoinAddress) {
	_cryptoCoinAddress = cryptoCoinAddress;
    }
}
