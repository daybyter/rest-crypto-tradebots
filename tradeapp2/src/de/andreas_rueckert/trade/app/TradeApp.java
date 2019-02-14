/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app;

import de.andreas_rueckert.notification.EmailNotifier;
import de.andreas_rueckert.trade.account.AccountManager;
import de.andreas_rueckert.trade.app.remote.SSLSocketServer;
import de.andreas_rueckert.trade.fee.FeeCalculator;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.OrderBook;
import de.andreas_rueckert.trade.bot.arb.ArbBot;
import de.andreas_rueckert.trade.bot.BtcENativeBot;
import de.andreas_rueckert.trade.bot.intersite.InterSiteBot;
import de.andreas_rueckert.trade.bot.rule.RuleBot;
import de.andreas_rueckert.trade.bot.rule.RuleSetFile;
import de.andreas_rueckert.trade.bot.spread.SpreadBot;
import de.andreas_rueckert.trade.bot.TradeBot;
import de.andreas_rueckert.trade.bot.TradeBotCore;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.site.server.TradeServer;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.ui.TradeTable;
import de.andreas_rueckert.trade.ui.TradeTableModel;
import de.andreas_rueckert.util.ModuleLoader;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


/**
 * This class uses the MtGox API UI classes to test
 * the API itself.
 */
public class TradeApp {

    // Static variables

    /**
     * The manager for all the accounts.
     */
    private AccountManager _accountManager = null;

    /**
     * The main frame for the app.
     */
    private static TradeApp _app = null;

    /**
     * Persistence for the properties of some objects.
     */
    private TradeAppProperties _appProperties = null;

    /**
     * Flag to indicate, if we are running in daemon mode (no GUI).
     */
    private static boolean _daemonMode = false;

    /**
     * A user notifier, that sends mails.
     */
    private EmailNotifier _emailNotifier = null;

    /**
     * The calculator for trade fees.
     */
    private FeeCalculator _feeCalculator = null;

    /**
     * The logger for the app.
     */
    public static Logger _logger = Logger.getLogger( TradeApp.class);

    /**
     * The main frame for the app.
     */
    private static JFrame _mainFrame;

    /**
     * The list of project files.
     */
    private ArrayList<ProjectFile> _projectFiles = new ArrayList<ProjectFile>();


    // Instance variables

    /**
     * A list of registered trade bots.
     */
    private Map<String, TradeBot> _registeredTradeBots = new HashMap<String, TradeBot>();

    /**
     * An array with the filenames of the rulefiles to load.
     */
    private static ArrayList<String> _rulefilesToLoad = new ArrayList<String>();

    /**
     * The actual tradebot.
     */
    private TradeBotCore _tradeBot = null;

    /**
     * An array with the trade bot names to start.
     */
    private static ArrayList<String> _tradeBotsToStart = new ArrayList<String>();

    /**
     * An array with the trade servers to start.
     */
    private static ArrayList<String> _tradeServersToStart = new ArrayList<String>();

    /**
     * A list of trade table panels.
     */
    private HashMap<String, JPanel> _tradeTablePanels = null;

    
    // Constructors
    
    /**
     * Create a new MtGox app.
     */
    private TradeApp() {

	_app = this;

	// Create an account manager
	_accountManager = new AccountManager();

	// Create a new email notifier.
	_emailNotifier = new EmailNotifier( "", "", "", "");

	// Keep the settings for all trade sites persistent.
	// Keep the properties of the trade sites persistent.
	for( TradeSite currentSite : ModuleLoader.getInstance().getRegisteredTradeSites().values()) {
	    getAppProperties().registerPersistentPropertyObject( currentSite);	
	}

	// Keep the settings of the email notifier persistent.
	getAppProperties().registerPersistentPropertyObject( _emailNotifier);

	// Keep the settings of the SSL shell persistent.
	getAppProperties().registerPersistentPropertyObject( SSLSocketServer.getInstance());

	// The calculator for trade fees.
	_feeCalculator = new FeeCalculator();
	
	// Create the tradebot.
	_tradeBot = new TradeBotCore();

	// Register some trade bots.
	registerTradeBot( ArbBot.getInstance());
	registerTradeBot( BtcENativeBot.getInstance());
	registerTradeBot( InterSiteBot.getInstance());
	registerTradeBot( RuleBot.getInstance());
	registerTradeBot( SpreadBot.getInstance());

	try {  // Try to read the application properties, if they exist yet (might be the first time, that the app is called).
	    getApp().getAppProperties().read();
	} catch( IOException ioe) {
	    _logger.info( "Cannot read application properties: " + ioe.toString());  // No error, since the properties might not exist yet.
	}

	// Loop over the registered trade bots and read their property files.
	for( TradeBot currentBot : getRegisteredTradeBots().values()) {
	    
	    try {
		currentBot.getProperties().read();
	    }  catch( IOException ioe) {
		_logger.info( "Cannot read bot properties: " + ioe.toString());  // No error, since the properties might not exist yet.
	    }
	}

	if( ! _daemonMode) {
	    // Create the UI.
	    createUI();
	    _logger.info( "Creating graphical user interface");
	}

	// Now load all the rulefiles, that were given on the commandline.
	// Store them in a list for immediate execution (is this a hack?)

	ArrayList<RuleSetFile> ruleSetsToExecute = new ArrayList<RuleSetFile>();

	for( String filename : _rulefilesToLoad) {
	     try {  // Try to create a new character set from the given file.

		 RuleSetFile newRuleSetFile = new RuleSetFile( new File( filename));  // Create the file.

		 TradeApp.getApp().addProjectFile( newRuleSetFile);  // Add the rule set to the project.

		 ruleSetsToExecute.add( newRuleSetFile);  // Add it to the list of rule sets to be executed.
            } catch( IOException ioe) {
		 _logger.error( "Cannot read rule set from file: " + filename + " " + ioe.getMessage());
            }
	}

	// Now execute all the the rule sets for the first time.
	// Is this a hack? Should this be triggered by the chart provider?
	for( RuleSetFile ruleSet : ruleSetsToExecute) {

	    _logger.info( "Executing rule set: " + ruleSet.getName());

	    TradeApp.getApp().getTradeBot().executeRules( ruleSet);
	}

	// Now execute all the trade bots, the user wants to execute directly.
	for( String tradeBotName : _tradeBotsToStart) {

	    // Look, if a trade bot with this name is registered.
	    TradeBot bot = getRegisteredTradeBot( tradeBotName);

	    if( bot != null) {  // If the bot is registered, start it.

		_logger.info( "Starting trade bot: " + tradeBotName);

		bot.start();
	    } else {
		_logger.error( "Given trade bot not found: " + tradeBotName);	
	    }
	}

	// Now start all the trade server, that the user wants to start.
	for( String tradeServerName : _tradeServersToStart) {

	    // Look, if a trade server with this name is registered.
	    TradeServer server = ModuleLoader.getInstance().getRegisteredTradeServer( tradeServerName);

	    if( server != null) {  // If the server is registered, start it.

		_logger.info( "Starting trade server: " + tradeServerName);

		server.startService();

	    } else {
		_logger.error( "Given trade server not found: " + tradeServerName);	
	    }
	}


	// Start a SSL socket for remote control, if we are in daemon mode.
	if( _daemonMode) {

	    //SSLSocketServer.getInstance().start();

	    //_logger.info( "Opened server socket for remote control.");
	}
    }


    // Methods

    /**
     * Add a new project file to the app.
     *
     * @param projectFile The project file to add.
     */
    public void addProjectFile( ProjectFile projectFile) {
	_projectFiles.add( projectFile);

	if( ! _daemonMode) {
	    // Add an UI element for this file.
	    getProjectOverviewPanel().getProjectTree().addProjectFile( projectFile);
	}

	// If this is a rule set, add it to the bot.
	if( projectFile instanceof RuleSetFile) {
	    try {
		getTradeBot().loadRules( (RuleSetFile)projectFile);
		getTradeBot().prepareSession( (RuleSetFile)projectFile
					      , _accountManager
					      , getChartProvider()
					      , _feeCalculator
					      , getOrderBook()
					      , _emailNotifier);
	    } catch( IOException ioe) {
		_logger.error( "Could not load rule set from file: " + ioe.toString());
	    }
	}
    }

    /**
     * Add a new trade table panel.
     *
     * @param panel The new trade table to add.
     */
    void addTradeTablePanel( TradeTable table) {
	if( _tradeTablePanels == null) {
	    _tradeTablePanels = new HashMap<String, JPanel>();
	}
	_tradeTablePanels.put( ((TradeTableModel)table.getModel()).getTradeSite().getName(), table.getPanel());
    }

    /**
     * Create the user interface of the app.
     */
    private void createUI() {
	_mainFrame = new JFrame( "Trade application");
        // _mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
	_mainFrame.addWindowListener( new WindowAdapter() {  // This is just to save the properties on application exit.
		public void windowClosing( WindowEvent e) {
		    
		    try {   // Try to write the app properties on UI stop.
			TradeApp.getApp().getAppProperties().write();
		    } catch( IOException ioe) {
			_logger.error( "Cannot store app properties: " + ioe.toString());
		    }
		
		    // Check, if there are any running trade bots, and stop them.
		    for( TradeBot bot : getRegisteredTradeBots().values()) {
			
			if( ! bot.isStopped()) {  // If this bot is still running,
				bot.stop();           // stop it.
			}
		    }

		    // Try to write the properties of all bots.
		    // Loop over the registered trade bots and read their property files.
		    for( TradeBot currentBot : getRegisteredTradeBots().values()) {
			
			try {
			    currentBot.getProperties().write();
			}  catch( IOException ioe) {
			    _logger.error( "Cannot write bot properties: " + ioe.toString());  // This is an actual error!
			}
		    }
		    

		    _mainFrame.setVisible( false);  // Remove the main frame.
		    _mainFrame.dispose();
		    System.exit(0);  // And exit the app.
		}
	    });
	_mainFrame.setJMenuBar( TradeMenuBar.getInstance());
	_mainFrame.setLayout( new FlowLayout());
	_mainFrame.getContentPane().add( getProjectOverviewPanel());
        _mainFrame.pack();
        _mainFrame.setVisible(true);
    }

    /**
     * Get the account manager for the trade site accounts.
     */
    public AccountManager getAccountManager() {
	return _accountManager;
    }

    /**
     * Get the main trade app.
     *
     * @return The trade App.
     */
    public static TradeApp getApp() {
	return _app;
    }

    /**
     * Get the controller for persistent properties.
     *
     * @return The controller for persistent properties.
     */
    public TradeAppProperties getAppProperties() {
	if( _appProperties == null) {  // If there is not persistence for properties yet,
	    _appProperties = new TradeAppProperties();  // create a controlling object to do so.
	}

	return _appProperties;
    }

    /**
     * Get the main chart provider.
     *
     * @return The main chart provider.
     */
    private final ChartProvider getChartProvider() {
	return ChartProvider.getInstance();
    }

    /**
     * Get the email notifier.
     *
     * @return The email notifier.
     */
    public EmailNotifier getEmailNotifier() {
	return _emailNotifier;
    }

    /**
     * Return the order book for this app. If there is none yet, it will be created.
     *
     * @return The order book.
     */
    private OrderBook getOrderBook() {
	return CryptoCoinOrderBook.getInstance();
    }

    /**
     * Get the current list of project files.
     *
     * @return The current list of project files.
     */
    public ArrayList<ProjectFile> getProjectFiles() {
	return _projectFiles;
    }

    /**
     * Get the project overview panel.
     *
     * @return The project overview panel.
     */
    public ProjectOverviewPanel getProjectOverviewPanel() {
	return ProjectOverviewPanel.getInstance( getApp(), ModuleLoader.getInstance().getRegisteredTradeSites());
    }

    /**
     * Get a registered trade bot for a given name or null, if no trade bot with this name is registered.
     *
     * @return A registered trade bot with the given name, or null, if no such bot is given.
     */
    public TradeBot getRegisteredTradeBot( String tradeBotName) {
	return _registeredTradeBots.get( tradeBotName);
    }

    /**
     * Get all the registered trade bots.
     *
     * @return The registered trade bots.
     */
    public Map<String,TradeBot> getRegisteredTradeBots() {
	return _registeredTradeBots;
    }

    /**
     * Get the associated trade bot of this app.
     *
     * @return The trade bot of this app.
     */
    public TradeBotCore getTradeBot() {
	return _tradeBot;
    }

    /**
     * Get a panel with a trade table for a given site.
     *
     * @return A panel with a trade table or null, if there is no such table.
     */
    public JPanel getTradeTablePanel( TradeSite site) {
	return _tradeTablePanels.get( site.getName());
    }

    /**
     * Check, if the app is in daemon mode.
     *
     * @return true, if the app is in daemon mode. False otherwise.
     */
    public boolean isDaemonMode() {
	return _daemonMode;
    }

    /**
     * The main method to create the application.
     *
     * @param args The commandline arguments of the application.
     */
    public static void main(String [] args) {

	// Configure Log4j 
	BasicConfigurator.configure();
	
	// Check the commandline arguments.
	for( int argIndex = 0; argIndex < args.length; ) {

	    String argument = args[ argIndex];  // Get the current argument as a string.

	    if( "-daemon".equals( argument)) {  // Are we running in daemon mode?
		_daemonMode = true;
		++argIndex;
		_logger.info( "Starting in daemon mode (no GUI)");

	    } else if( "-rulefile".equals( argument)) {  // The user wants to load a rulefile.
		if( ++argIndex >= args.length) {
		    System.err.println( "-rulefile switch given but no filename to load");
		    System.exit( 0);
		} else {
		    _rulefilesToLoad.add( args[ argIndex++]);
		}		
	    } else if( "-startbot".equals( argument)) {  // The user wants to start a bot.
		if( ++argIndex >= args.length) {
		    System.err.println( "-startbot switch given but no trade bot name to start.");
		    System.exit( 0);
		} else {
		    _tradeBotsToStart.add( args[ argIndex++]);
		}
	    } else if( "-startserver".equals( argument)) {  // The user wants to start a bot.
		if( ++argIndex >= args.length) {
		    System.err.println( "-startserver switch given but no trade server name to start.");
		    System.exit( 0);
		} else {
		    _tradeServersToStart.add( args[ argIndex++]);
		}
	    } else if( "-help".equals( argument) 
		       || "--help".equals( argument)
		       || "-h".equals( argument)
		       || "-?".equals( argument)) {  // Also allow --help, -h and -? to get help
		showHelp();
		++argIndex;
		System.exit(0);  // Exit the app after showing the help.
	    } else {
		System.out.flush();
		System.out.println( "Unknown commandline argument: " + argument + "\n");
		showHelp();
		System.out.flush();
		System.exit(0);  // Exit the app after showing the help.
	    }
	}

	_app = new TradeApp();  // create new app.
    }

    /**
     * Register a new trade bot.
     *
     * @param tradeBot The trade bot to register.
     */
    private void registerTradeBot( TradeBot tradeBot) {

	_registeredTradeBots.put( tradeBot.getName(), tradeBot);
    }

    /**
     * Register a new trade site.
     *
     * @param site The new trade site to add to the list of sites.
     */
    /*    private void registerTradeSite( TradeSite site) {

	getChartProvider().registerTradeSite( site);  // Tell the chart provider about the new trade site.

	// Keep the properties of the trade sites persistent.
	getAppProperties().registerPersistentPropertyObject( site);

	if( ! _daemonMode) {
	    addTradeTablePanel( new TradeTable( site));  // Create UI elements for this trade site.
	}

	_logger.info( "Registered " + site.getName() + " interface");
	} */

    /**
     * Remove a given project file from the list of project files.
     *
     * @param projectFile The project file to remove.
     */
    public void removeProjectFile( ProjectFile projectFile) {
	_projectFiles.remove( projectFile);

	if( ! _daemonMode) {
	    // Add an UI element for this file.
	    getProjectOverviewPanel().getProjectTree().removeProjectFile( projectFile);
	}

	// If this is a rule set, add it to the bot.
	if( projectFile instanceof RuleSetFile) {
	    getTradeBot().removeSession( (RuleSetFile)projectFile, _accountManager);
	    getTradeBot().unloadRules( (RuleSetFile)projectFile);
	}
    }

    /**
     * Set a new panel in the app.
     *
     * @param newPanel The new panel to set.
     */
    public void setContentPanel( JPanel newPanel) {

	while( _mainFrame.getContentPane().getComponentCount() > 1) {  // Remove all content panels, but the project overview.
	    _mainFrame.getContentPane().remove( _mainFrame.getContentPane().getComponentCount() - 1); 
	}
	
	if( newPanel != null) {  // If there was actually a new panel given (newPanel might be null otherwise).
	    _mainFrame.getContentPane().add( newPanel);  // Add the new panel.
	    
	    _mainFrame.getContentPane().invalidate();  // Re-layout the UI.
	    _mainFrame.getContentPane().validate();
	    
	    _mainFrame.pack();  // Pack the frame.
	}
    }

    /**
     * Show the help text on the console.
     */
    private static void showHelp() {

	// Display a help text.
	System.out.println( "TradeApp by Andreas Rueckert <a_rueckert@gmx.net>\n\n"
			    + "-help or --help or -h or -? to display this help text.\n"
			    + "-daemon to start in daemon mode (no graphical user interface displayed).\n"
			    + "-rulefile <filename> to load (and execute once) a file with a rule set.\n\n");
    }
}
