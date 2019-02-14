/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app;

import de.andreas_rueckert.trade.bot.BtcENativeBot;
import de.andreas_rueckert.trade.bot.rule.RuleSetFile;
import de.andreas_rueckert.trade.bot.TradeBot;
import de.andreas_rueckert.trade.chart.TradeChartPanel;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.ui.TradeDepthPanel;
import de.andreas_rueckert.util.ModuleLoader;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/**
 * Class to display a tree view of the project.
 */
public class ProjectTree extends JTree {

    // Static variables


    // Instance variables

    /**
     * A list with the bot names.
     */
    ArrayList<String> _botNames = new ArrayList<String>();

    /**
     * The root node for the project.
     */
    DefaultMutableTreeNode _projectNode;

    /**
     * The root nodes for all kinds of project properties.
     */
    DefaultMutableTreeNode _accounts;
    DefaultMutableTreeNode _rules;
    DefaultMutableTreeNode _trades;


    // Constructors

    /**
     * Create a new project tree.
     */
    ProjectTree() {
        // Create a tree from the current project.
        _projectNode = getProjectHierachy();
	
        // Set the project tree as the root of the jtree.
        ((DefaultTreeModel)getModel()).setRoot( _projectNode);
	
	// Add a mouse listener for the node (and eventually context-) menus.
	addMouseListener( new MouseAdapter() {
		public void mousePressed( MouseEvent e) {
		    int row = getRowForLocation( e.getX(), e.getY());
		    TreePath tp = getPathForRow(row);
		    if( tp != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
			Object userObject = node.getUserObject();
			
			if( e.isPopupTrigger()) {  // If the user did a right-click...
			    JPopupMenu contextMenu = EditorContextMenuFactory.getInstance().getContextMenuFor( userObject);
                            
                            if( contextMenu != null) { // if there is a menu, show it.
                                contextMenu.show( (JComponent)e.getSource(), e.getX(), e.getY());
                            }
			} else {  // The user did a left click?
			    if( userObject instanceof TradeSite) {   // Does this represent a trade site.
				TradeApp.getApp().setContentPanel( TradeApp.getApp().getTradeTablePanel( (TradeSite)userObject));  // if so, display the trade panel.
			    }

			    if( userObject instanceof String) {
				String nodeText = (String)userObject;

				if( nodeText.equals( "Trading")) {
				    TradeApp.getApp().setContentPanel( TradeDepthPanel.getInstance());
				} else if( nodeText.equals( "Graphics")) {  // The user wants to see the trade charts.
				    TradeApp.getApp().setContentPanel( TradeChartPanel.getInstance());
				} else if( nodeText.equals( "Accounts")) {  // Just a hack to debug the account fetching.
				    TradeApp.getApp().getAccountManager().getAccounts();
				}

				// Check if the node name fits the registered bots.
				for( String botName : _botNames) {
				    if( nodeText.equals( botName)) {
					TradeBot bot = TradeApp.getApp().getRegisteredTradeBot( botName);
					if( bot != null) {
					    TradeApp.getApp().setContentPanel( bot.getUI().getUIPanel());
					}
				    }
				}
			    }

			    // This node is just other text, so no further action for now.
			}
		    }
		}
	    });
    }



    // Methods

    /**
     * Add a new project file.
     *
     * @param projectFile The project file to add.
     */
    public void addProjectFile( ProjectFile projectFile) {
	
	if( projectFile instanceof RuleSetFile) {  // Is this a rule set?
	    _rules.add( new DefaultMutableTreeNode( (RuleSetFile)projectFile));  // Add it to the 'rules' node.

	    updateUI();  // This a hack, since we don't have a real tree model yet.
	}
    }

    /**
     * Get the root node of the project.
     *
     * @return The root node of the project.
     */
    DefaultMutableTreeNode getProjectHierachy() {
	DefaultMutableTreeNode node = new DefaultMutableTreeNode( "project");

	// Add all types of nodes for various projet aspects.
	node.add( _accounts = new DefaultMutableTreeNode( "Accounts"));
	for( Map.Entry<String,TradeSite> siteEntry : ModuleLoader.getInstance().getRegisteredTradeSites().entrySet()) {  // All trade sites to the list of charts.
	    _accounts.add( new DefaultMutableTreeNode( siteEntry.getValue()));
	}

        node.add( _rules = new DefaultMutableTreeNode( "Rules"));

	node.add( new DefaultMutableTreeNode( "Trading"));

	node.add( _trades = new DefaultMutableTreeNode( "Trade charts"));  // Add a node for the trade charts.
	for( Map.Entry<String,TradeSite> siteEntry : ModuleLoader.getInstance().getRegisteredTradeSites().entrySet()) {  // All all trade sites to the list of charts.
	    _trades.add( new DefaultMutableTreeNode( siteEntry.getValue()));
	}
	_trades.add( new DefaultMutableTreeNode( "All"));  // Display the trades of all trading sites.
	_trades.add( new DefaultMutableTreeNode( "Graphics"));

	// Special node for hacked native bots.
	DefaultMutableTreeNode nativeBots = new DefaultMutableTreeNode( "Native bots");

	// Add the native bots here.
	for( Map.Entry<String,TradeBot> botEntry : TradeApp.getApp().getRegisteredTradeBots().entrySet()) {

	    String botName =  botEntry.getValue().getName();  // Get the name of the bot.

	    nativeBots.add( new DefaultMutableTreeNode( botName));  // Create a new tree node for the bot.

	    _botNames.add( botName);  // Store the name for mouse click checks later.
	}

	node.add( nativeBots);

	return node;
    }

    /**
     * Remove a project file from the tree.
     *
     * @param projectFile The project file to remove.
     */
    public void removeProjectFile( ProjectFile projectFile) {

	if( projectFile instanceof RuleSetFile) {  // Is this a rule set?
	    TreeModel model = getModel();

	    for( int nodeCount = 0; nodeCount < model.getChildCount( _rules); ) {
		Object currentNode = model.getChild( _rules, nodeCount);

		if( ( currentNode instanceof DefaultMutableTreeNode)
		    && projectFile.equals( ((DefaultMutableTreeNode)currentNode).getUserObject())) {
		    _rules.remove( nodeCount);
		} else {
		    nodeCount++;
		}
	    }

	    updateUI();  // This a hack, since we don't have a real tree model yet.
	}

    }
}