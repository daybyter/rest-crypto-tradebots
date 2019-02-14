/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.action;

import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.bot.rule.RuleSetFile;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;


/**
 * Action to execute a rule set.
 */
public class ActionExecuteRuleSet extends AbstractAction {

    // Static variables


    // Instance variables

    /**
     * The rule set file to edit.
     */
    private RuleSetFile _ruleSetFile = null;


    // Constructors

    /**
     * Create a new action to execute a rule set.
     *
     * @param ruleSetFile The rule set file to execute.
     */
    public ActionExecuteRuleSet( RuleSetFile ruleSetFile) {
	super();

	putValue( NAME, "Execute rule set");

	_ruleSetFile = ruleSetFile;  // Store a reference to the rule set file in this object.
    }

    // Methods 

    /**
     * The user wants to execute a rule set.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {
	TradeApp.getApp().getTradeBot().executeRules( _ruleSetFile);
    }
}