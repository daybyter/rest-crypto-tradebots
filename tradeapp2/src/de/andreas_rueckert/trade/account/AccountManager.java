/**
 * Java implementation of an AccountManager.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.account;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.ModuleLoader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Class to handle a number of accounts.
 */
public class AccountManager {
    
    // Static variables


    // Instance variables

    /**
     * Store the hashmaps as a hash map.
     */
    private List<Account> _accounts;


    // Constructors

    /**
     * Create a new account manager.
     */
    public AccountManager() {
    }


    // Methods

    /**
     * Get the accounts.
     *
     * @return A list of accounts.
     */
    public List<Account> getAccounts() {
	if( _accounts == null) {  // If the accounts were not fetched already.
	    _accounts = new ArrayList<Account>();  // Create a new list of accounts.
	}
	return _accounts;
    }

    /**
     * Get all the accounts from a given trade site.
     *
     * @param tradeSite The trade site to query.
     *
     * @return The list of accounts on this trade site.
     */
    public List<Account> getAccounts( TradeSite tradeSite) {

	// The following code seems kinda complicated. The main reason for it is simply, that
	// some of the trade site implementations lack the code for account fetching, so I want
	// to fetch only those accounts, that were really required.

	List<Account> result = new ArrayList<Account>();  // Start with an empty result.
	
	// Filter all accounts for the given trade site.
	for( Account account : getAccounts()) {
	    if( account instanceof TradeSiteAccount) {  // If this is a trade site account.
		TradeSiteAccount tradeSiteAccount = (TradeSiteAccount)account;

		if( tradeSite.equals( tradeSiteAccount.getTradeSite())) {  // If the trade site of this account equals the given trade site,
		    result.add( account);                                  // add the account to the result.
		}
	    }
	}

	// If we got no results so far, maybe the account of this trade site were not fetched yet?
	if( result.isEmpty()) {

	    Collection<TradeSiteAccount> siteAccounts = tradeSite.getAccounts( null);
	    
	    if( siteAccounts != null) {
		_accounts.addAll( siteAccounts);  // Add the collection of fetched accounts, if there are any.

		result.addAll( siteAccounts);

		return result;   // And return the fetched accounts.
	    }	
	    
	}

	return result;  // 
    }

    /**
     * Get the balance for a given trade site and currency.
     *
     * @param tradeSite The trade site to query.
     * @param currency The currency to query.
     *
     * @return The balance for the given account on this trade site or null, if no matching account was found.
     */
    public BigDecimal getBalance( TradeSite tradeSite, Currency currency) {

	// Loop over all the accounts on this trade site.
	for( Account account : getAccounts( tradeSite)) {
	    if( account.getCurrency().equals( currency)) {  // If this is an account for the given currency.
		return account.getBalance();                  // return the balance of this account.
	    }
	}
	return null;  // No matching account was found.
    }

    /**
     * Update the accounts for a given tradesite.
     *
     * @param tradeSite The trade site to query for accounts.
     */
    public void updateAccounts( TradeSite tradeSite) {

	Collection<TradeSiteAccount> siteAccounts = tradeSite.getAccounts( null);
	
	if( siteAccounts != null) {
	    _accounts.addAll( siteAccounts);  // Add the collection of fetched accounts, if there are any.
	}	
    }

    /**
     * Update all the accounts from all trade sites and store them in the local accounts list.
     */
    public void updateAllAccounts() {

	// Create an array list for the result.
	List<Account> _accounts = new ArrayList<Account>();

	// Loop over all the registered trade sites.
	for( TradeSite tradeSite : ModuleLoader.getInstance().getRegisteredTradeSites().values()) {

	    Collection<TradeSiteAccount> siteAccounts = tradeSite.getAccounts( null);
	    
	    if( siteAccounts != null) {
		_accounts.addAll( siteAccounts);  // Add the collection of fetched accounts, if there are any.
	    }
	}
    }
}
