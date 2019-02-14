/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.action;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.bot.rule.RuleSetFile;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * Action to edit a loaded rule set.
 */
public class ActionEditRuleSet extends AbstractAction {

    // Static variables


    // Instance variables

    /**
     * The rule set file to edit.
     */
    private RuleSetFile _ruleSetFile = null;


    // Constructors

    /**
     * Create a new action to edit a rule set.
     */
    public ActionEditRuleSet( RuleSetFile ruleSetFile) {
	super();

	putValue( NAME, "Edit rule set");

	_ruleSetFile = ruleSetFile;  // Store a reference to the rule set file in this object.
    }


    // Methods

    /**
     * The user wants to edit a rule set.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {
	throw new NotYetImplementedException( "Editing a rule set is not yet implemented");
    }
}