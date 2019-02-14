/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.action;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.bot.rule.RuleSetFile;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * Class to edit the settings for a rule set.
 */
public class ActionRuleSetSettings extends AbstractAction {

    // Static variables


    // Instance variables

    /**
     * The rule set file that has it's settings edited.
     */
    private RuleSetFile _ruleSetFile = null;


    // Constructors

    public ActionRuleSetSettings( RuleSetFile ruleSetFile) {
	super();

	putValue( NAME, "Edit settings");

	_ruleSetFile = ruleSetFile;  // Store a reference to the rule set file in this object.
    }

    
    // Methods

    /**
     * The user wants to edit the settings of a rule set.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {

	throw new NotYetImplementedException( "Editing the settings is not yet implemented");
    }
}