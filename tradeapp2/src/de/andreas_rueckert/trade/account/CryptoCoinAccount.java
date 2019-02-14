/**
 * Java implementation of a cryptocoin account.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.account;


/**
 * Interface for a cryptocoin account.
 */
public interface CryptoCoinAccount extends Account {

    // Static variables


    // Methods

    /**
     * Get the cryptocoin address of this account.
     *
     * @return The cryptocoin address of this account.
     */
    public String getCryptoCoinAddress();

    /**
     * Set a new address for this account.
     *
     * @param cryptoCoinAddress The new address for this account.
     */
    public void setCryptoCoinAddress( String cryptoCoinAddress);
}