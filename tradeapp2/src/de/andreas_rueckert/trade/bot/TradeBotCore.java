/**
 * Java implementation of a tradebot.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot;

import de.andreas_rueckert.notification.EmailNotifier;
import de.andreas_rueckert.trade.account.Account;
import de.andreas_rueckert.trade.account.AccountManager;
import de.andreas_rueckert.trade.bot.rule.RuleSetFile;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.fee.FeeCalculator;
import de.andreas_rueckert.trade.order.OrderBook;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import javax.rules.admin.LocalRuleExecutionSetProvider;
import javax.rules.admin.RuleAdministrator;
import javax.rules.admin.RuleExecutionSet;
import javax.rules.admin.RuleExecutionSetCreateException;
import javax.rules.admin.RuleExecutionSetDeregistrationException;
import javax.rules.admin.RuleExecutionSetRegisterException;
import javax.rules.ConfigurationException;
import javax.rules.Handle;
import javax.rules.InvalidHandleException;
import javax.rules.InvalidRuleSessionException; 
import javax.rules.RuleExecutionSetNotFoundException; 
import javax.rules.RuleRuntime;
import javax.rules.RuleServiceProvider;
import javax.rules.RuleServiceProviderManager;
import javax.rules.RuleSessionCreateException; 
import javax.rules.RuleSessionTypeUnsupportedException;
import javax.rules.StatefulRuleSession;


/**
 * This is the main class for the bot engine.
 */
public class TradeBotCore {

    // Static variables

    /**
     * The fully qualified classname of a rule service provider.
     */
    private final static String DROOLS_SERVICE_PROVIDER_IMPLEMENTATION = "org.drools.jsr94.rules.RuleServiceProviderImpl";

    /**
     * The name under which the rule service provider is registered.
     */
    private final static String DROOLS_SERVICE_PROVIDER_REGISTRATION = "http://drools.org/";


    // Instance variables

    /**
     * The rule administrator.
     */
    RuleAdministrator _ruleAdministrator = null;

    /**
     * An execution set provider.
     */
    LocalRuleExecutionSetProvider _ruleExecutionSetProvider = null;

    /**
     * The currently used rule service provider.
     */
    RuleServiceProvider _ruleServiceProvider = null;


    // Constructors

    /**
     * Create a new tradebot.
     */
    public TradeBotCore() {

	try {
	    
	    // Load the rule service provider.
	    // Loading this class will automatically register this provider with the
	    // provider manager.
	    Class.forName( DROOLS_SERVICE_PROVIDER_IMPLEMENTATION);

	    // Get the rule service provider from the provider manager.
	    _ruleServiceProvider = RuleServiceProviderManager.getRuleServiceProvider( DROOLS_SERVICE_PROVIDER_REGISTRATION);

	    // Get the rule administrator.
	    _ruleAdministrator = _ruleServiceProvider.getRuleAdministrator();

	    // Get an execution set provider from the administrator.
	    _ruleExecutionSetProvider = _ruleAdministrator.getLocalRuleExecutionSetProvider( null );
	} catch( ClassNotFoundException cnfe) {  // The service provider implementation was not found.
	    System.err.println( "Found no service provider implementation: " + cnfe.toString());
	} catch( ConfigurationException cfe) {  // The rule service provider or the rule administrator couldn't be fetched.
	    System.err.println( "Error fetching service provider or rule administrator: " + cfe.toString());
	} catch( RemoteException re) {
	    System.err.println( "Error fetching the execution set provider: " + re.toString());
	}
    }


    // Methods

    /**
     * Execute the rules on a prepared stateful session.
     *
     * @param ruleSetFile The rule set file to execute.
     */
    public void executeRules( RuleSetFile ruleSetFile) {

	StatefulRuleSession session = ruleSetFile.getSession();

	if( session != null) {

	    try {
		// Execute the rules.
		session.executeRules();
	    } catch( RemoteException rme) {
		System.err.println( "Could not execute rules: " + rme);
	    } catch( InvalidRuleSessionException ire) {
		System.err.println( "Could not operate on invalid session: " + ire);
	    }
	}
    }

    /**
     * Load a set of rules from a URL.
     *
     * @param ruleSetFile The file, which contains the rules.
     *
     * @return The name of the rule set or null in case of an error.
     *
     * @throws IOException if the rule file could not be opened.
     */
    public String loadRules( RuleSetFile ruleSetFile) throws IOException {

	String setName = null;  // The name of the rule set.

	// Create a reader for the rules.
	FileReader fileReader = new FileReader( ruleSetFile.getFile());

	try {
	    // Create the RuleExecutionSet for the drl.
	    RuleExecutionSet ruleExecutionSet = _ruleExecutionSetProvider.createRuleExecutionSet( fileReader, null );

	    setName = ruleExecutionSet.getName();
	    
	    // Register the RuleExecutionSet with the RuleAdministrator.
	    _ruleAdministrator.registerRuleExecutionSet( setName, ruleExecutionSet, null);

	} catch( RuleExecutionSetCreateException rese) {
	    System.err.println( "Could not create rule execution set: " + rese.toString());
	} catch( RuleExecutionSetRegisterException rere) {
	    System.err.println( "Could not register rule execution set: " + rere.toString());
	}

	ruleSetFile.setRuleSetName( setName);  // Store the name of the RuleSet in the associated file,
	// since the RuleAdministrator seems to lack a method to query all the registered RuleSet objects.

	return setName;  // Return the registered name of the rule set or null in case of an error.
    }

    /**
     * Prepare a session to execute the rules later.
     *
     * @param ruleSetFile The file with the rule set.
     * @param accountManager The manager for the accounts.
     * @param chartProvider The charts to interpret.
     * @param feeCalculator A calculator for the fees.
     * @param orderBook The book to place orders.
     */
    public void prepareSession( RuleSetFile ruleSetFile
				, AccountManager accountManager
				, ChartProvider chartProvider
				, FeeCalculator feeCalculator
				, OrderBook orderBook
				, EmailNotifier emailNotifier) {
	
	StatefulRuleSession session = null;  // The new stateful rule session.

	try {
	    // Get a rule runtime.
	    RuleRuntime ruleRuntime = _ruleServiceProvider.getRuleRuntime();

	    // Create a map of properties for global variables.
	    Map properties = new HashMap();
	    properties.put( "ChartProvider", chartProvider);
	    properties.put( "FeeCalculator", feeCalculator);
	    properties.put( "OrderBook", orderBook);
	    properties.put( "EmailNotifier", emailNotifier);
	    properties.put( "ExecutionCounter", ruleSetFile.getExecutionCounter());  // Give the drl file the option to check, how
	                                                                             // often it was called.

	    // Create a stateful session from the runtime.
	    session = (StatefulRuleSession)ruleRuntime.createRuleSession( ruleSetFile.getRuleSetName()
									   , properties
									   , RuleRuntime.STATEFUL_SESSION_TYPE);
	    
	    // Add all accounts to the rule set.
	    for( Account a : accountManager.getAccounts()) {
		session.addObject( a);
	    }

	    // Add the created session to the rule set file.
	    ruleSetFile.setSession( session);

	} catch( ConfigurationException cfe) {
	    System.err.println( "Could not get rule runtime: " + cfe.toString());
	} catch( RuleExecutionSetNotFoundException renf) {
	    System.err.println( "Could not find rule execution set: " + renf.toString());
	} catch( RuleSessionCreateException rsce) {
	    System.err.println( "Could not create session: " + rsce);
	} catch( RuleSessionTypeUnsupportedException rste) {
	    System.err.println( "Could not create session: " + rste.toString());
	} catch( RemoteException rme) {
	    System.err.println( "Could not add object: " + rme);
	} catch( InvalidRuleSessionException ire) {
	    System.err.println( "Could not operate on invalid session: " + ire);
	}
    }

    /**
     * Remove a session from the bot.
     *
     * @param ruleSetFile The rule set file with the session.
     * @param accountManager The manager for the trade site accounts.
     */
    public void removeSession( RuleSetFile ruleSetFile
			       , AccountManager accountManager) {

	StatefulRuleSession session = ruleSetFile.getSession();	 // Get the session from the rule set file.

	// Destroy the session properly.

	try {
	    // Remove all objects from the session.
	    for( Object h : session.getHandles()) {
		session.removeObject( (Handle)h);
	    }
	} catch( InvalidHandleException ihe) {
	    System.err.println( "Cannot remove object via handle from session: " + ihe.toString());
	} catch( InvalidRuleSessionException irse) {
	    System.err.println( "Cannot fetch handles from session: " + irse.toString());
	} catch( RemoteException re) {
	    System.err.println( "Cannot remove objects from session via handle: " + re.toString());
	}
    }

    /**
     * Unload a rule set file from the bot.
     *
     * @param ruleSetFile The rule set file to unload.
     */
    public void unloadRules( RuleSetFile ruleSetFile) {

	try {
	    // Deregister the RuleExecutionSet from the RuleAdministrator.
	    _ruleAdministrator.deregisterRuleExecutionSet( ruleSetFile.getRuleSetName(), null);
	} catch( RemoteException re) {
	    System.err.println( "Error deregistering rule execution set from the rule administrator: " + re.toString());
	} catch( RuleExecutionSetDeregistrationException resde) {
	    System.err.println( "Error deregistering rule execution set from the rule administrator: " + resde.toString());
	}
    }
}