/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.rule;

import de.andreas_rueckert.trade.app.ProjectFile;
import java.io.File;
import java.io.IOException;
import javax.rules.StatefulRuleSession;


/**
 * Class to hold rule sets for the trade bot.
 */
public class RuleSetFile extends ProjectFile {

    // Static variables


    // Instance variables

    /**
     * An execution counter (to be used in rule sets).
     */
    private ExecutionCounter _executionCounter;

    /**
     * The name of the associated rule set.
     */
    private String _ruleSetName = null;

    /**
     * The stateful session for the executed rule set.
     */
    private StatefulRuleSession _session = null;


    // Constructors

    /**
     * Create a new rule set file instance from a given file.
     *
     * @param file The actual file.
     */
    public RuleSetFile( File file) throws IOException {
	super( file);

	// Create a new execution counter (to be passed to the rule engine, therefore
	// this is an object).
	_executionCounter = new ExecutionCounter();
    }


    // Methods

    /**
     * Get the execution counter of this rule set.
     *
     * @return The execution counter of this rule set.
     */
    public ExecutionCounter getExecutionCounter() {
	return _executionCounter;
    }

    /**
     * Get the associated RuleSet name of this rule set.
     *
     * @return The associated RuleSet name of this rule set.
     */
    public String getRuleSetName() {
	return _ruleSetName;
    }

    /**
     * Get the session with the state of the executed rule set.
     *
     * @return The session with the state of the executed rule set.
     */
    public StatefulRuleSession getSession() {
	return _session;
    }

    /**
     * Set the associated RuleSet name for this file.
     *
     * @param ruleSetName The new RuleSet name.
     */
    public void setRuleSetName( String ruleSetName) {
	_ruleSetName = ruleSetName;
    }

    /**
     * Set a new session for the executed rule set.
     *
     * @param session The new session for the executed rule set.
     */
    public void setSession( StatefulRuleSession session) {
	_session = session;
    }
}