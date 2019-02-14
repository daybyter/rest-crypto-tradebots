/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app;

import de.andreas_rueckert.trade.app.action.ActionEditRuleSet;
import de.andreas_rueckert.trade.app.action.ActionExecuteRuleSet;
import de.andreas_rueckert.trade.app.action.ActionLoadRuleSet;
import de.andreas_rueckert.trade.app.action.ActionRemoveRuleSet;
import de.andreas_rueckert.trade.app.action.ActionRuleSetSettings;
import de.andreas_rueckert.trade.bot.rule.RuleSetFile;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * A factory for context menus.
 */
class EditorContextMenuFactory {

    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static EditorContextMenuFactory _instance = null;


    // Instance variables


    // Constructors

    /**
     * Private constructor for this factory (singleton pattern).
     */
    private EditorContextMenuFactory() {
    }


    // Methods

    /**
     * Get a context menu for an object.
     *
     * @param object The object to get a contest for.
     *
     * @return The context menu for the object or null, if there is no context menu for this object.
     */
    public JPopupMenu getContextMenuFor( Object object) {

	// Did the user right-click on a specific rule set?
	if( object instanceof RuleSetFile) { return getContextMenuForRule( (RuleSetFile)object); }

	if( object instanceof String) {
	    String name = (String)object;  // This object is a node name.

	    if( "Rules".equals( name)) { return getContextMenuForRules(); }
	}

	return null;  // No context menu found for this object.
    }

    /**
     * Get a context menu for a single rule.
     *
     * @param ruleSetFile The rule set file to get the context menu for.
     *
     * @return A context menu for a single rule.
     */
    private JPopupMenu getContextMenuForRule( RuleSetFile ruleSetFile) {
	JPopupMenu menu = new JPopupMenu();  // Create a new popup menu.

	menu.add( new JMenuItem( new ActionEditRuleSet( ruleSetFile)));  // Edit the rule set.
	menu.add( new JMenuItem( new ActionExecuteRuleSet( ruleSetFile)));  // Execute this rule set.
	menu.add( new JMenuItem( new ActionRuleSetSettings( ruleSetFile)));  // Edit the setting of this rule set.
	menu.addSeparator();
	menu.add( new JMenuItem( new ActionRemoveRuleSet( ruleSetFile)));  // Remove this rule set from the project.

	return menu;
    }

    /**
     * Get a context menu for the 'Rules' parent node.
     *
     * @return A context menu for the 'Rules' parent node.
     */
    private JPopupMenu getContextMenuForRules() {
	JPopupMenu menu = new JPopupMenu();  // Create a new popup menu.

	menu.add( new JMenuItem( ActionLoadRuleSet.getInstance()));  // Add the menu items to the menu.
	
	return menu;
    }

    /**
     * Get the only instance of this factory (singleton pattern).
     *
     * @return The only instance of this factory.
     */
    public static EditorContextMenuFactory getInstance() {

	if( _instance == null) {  // If there is no factory instance yet,
	    _instance = new EditorContextMenuFactory();  // create one.
	}

	return _instance;  // Return the only instance of this factory.
    }
}