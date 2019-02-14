/**
 * Java implementation of a mysql trade cache persistence.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.chart.persistence;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.PersistentProperties;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.LogUtils;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class to store trades in a mysql database.
 */
public class CachePersistenceMySQL implements CachePersistence {

    // Static variables


    // Instance variables

    /**
     * Flag to indicate, if the caching is activated.
     */
    private boolean _activated = false;

    /**
     * The connection to the database.
     */
    private Connection _connection;

    /**
     * A map with the create statements for the tables.
     */
    private Map< String, String> _createStatements;

    /**
     * The name of the database.
     */
    private String _databaseName;

    /**
     * The host, on which the database is running.
     */
    private String _host;

    /**
     * The password for the database access.
     */
    private String _password;
    
    /**
     * The list of tables for this service.
     */
    private String [] _tables;

    /**
     * The user name for the database access.
     */
    private String _username;


    // Constructors

    /**
     * Create a new mysql database connection.
     */
    public CachePersistenceMySQL() {

	// Set the list of required tables.
	_tables = new String[1];
	_tables[ 0] = "trades";

	// Create a map for the table create statements.
	// I use BigDecimal with a DECIMAL128 MatchContext at the moment (20121201).
	// Details of the format are here: http://en.wikipedia.org/wiki/Decimal128_floating-point_format
	// this means up to 34 decimal digits of significance and up to 6144 of exponent. Since mysql's Decimal
	// type (as of ver 5.1) can only store 30 decimal digits, I have to convert the BigDecimal to a String.
	// The max length of this String should be -0.<34 digits>E-6144 ,which is 43 characters, if I didn't
	// miss anything (a_rueckert).
	_createStatements = new HashMap< String, String>();
	_createStatements.put( "trades", 
			       "tradesite VARCHAR(8) NOT NULL" 
			       + ", currencypair VARCHAR(8) NOT NULL"
			       + ", siteid VARCHAR(32)"
			       + ", timestamp BIGINT NOT NULL"
			       + ", price VARCHAR(43) NOT NULL"
			       + ", amount VARCHAR(43) NOT NULL"
			       + ", index tradesite (tradesite)"
			       + ", index currencypair (currencypair)"
			       + ", index timestamp (timestamp)");

	// Register this object as persistent.
	TradeApp.getApp().getAppProperties().registerPersistentPropertyObject( this);
    }


    // Methods

    /**
     * Add a new trade to the mysql db.
     *
     * @param trade The new trade to add.
     * 
     * @return true, if the trade was successfully added. false otherwise.
     */
    public boolean add( Trade trade) {

	if( _activated) {

	    if( trade instanceof CryptoCoinTrade) {
		
		CryptoCoinTrade ctr = (CryptoCoinTrade)trade;
	    
		String addStatement = "INSERT INTO trades ( tradesite, currencypair, siteid, timestamp, price, amount) " 
		    + "VALUES (" + ctr.getSite() 
		    + "," + ctr.getCurrencyPair()
		    + "," + ctr.getId()
		    + "," + ctr.getTimestamp()
		    + "," + ctr.getPrice()
		    + "," + ctr.getAmount()
		    + ");";
	    
		return executeStatement( addStatement);
	    } else {
		LogUtils.getInstance().getLogger().error( "CachePersistenceMysql.add() can only store CryptoCoinTrade objects for now!");
	    }
	
	    return false;  // An error occured.
	}

	return true;  // An de-activated cache works always...
    }

    /**
     * Check, if the table(s) for this service are in the database.
     *
     * @return true, if the tables are in the database. false otherwise.
     */
    public boolean checkForTables() {

	try {
	    // Get the metadata from the connection.
	    DatabaseMetaData dbMetaData = _connection.getMetaData();
	    
	    // check if all the reqired tables are in the database.
	    for( int currentTableIndex = 0; currentTableIndex < _tables.length; ++currentTableIndex) {
	    
		ResultSet tableCheckResult = dbMetaData.getTables( null, null, _tables[ currentTableIndex], null);
	
		if( ! tableCheckResult.next()) {  // Does the table not exist?
		    return false;                 // Check failed.
		}
	    }
	} catch( SQLException se) {
	    LogUtils.getInstance().getLogger().error( "Error while checking for required tables in database: " + se);
	    return false;
	}
	    
	return true;  // All the tables were found.
    }

    /**
     * Connect to the database.
     */
    private void connect() {

	if( _username == null || _password == null) {
	    LogUtils.getInstance().getLogger().error( "CachePersistenceMySQL: missing username or password for database connection");
	} else {

	    // Create a new driver instance.
	    try {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
	    } catch( ClassNotFoundException cfe) {
		LogUtils.getInstance().getLogger().error( "JDBC MySQL driver not found: " + cfe);
	    } catch( IllegalAccessException iae) {
		LogUtils.getInstance().getLogger().error( "Illegal access while trying to instantiate JDBC MySQL driver: " + iae);
	    } catch( InstantiationException ie) {
		LogUtils.getInstance().getLogger().error( "Cannot instantiate JDBC MySQL driver: " + ie);
	    }

	    // Create an url for the connection.
	    String url = "jdbc:mysql://" + _host  + "/" + _databaseName;
	    
	    // Create a connection to the database.
	    try {
		_connection = DriverManager.getConnection( url, _username, _password);
	    } catch( SQLException se) {
		LogUtils.getInstance().getLogger().error( "SQL error while connecting to MySQL: " + se);
	    }
	}
    }

    /**
     * Check, if the database contains the given trade.
     *
     * @param trade The trade to check.
     *
     * @return true, if the trade is in the database. False otherwise.
     */
    public boolean contains( Trade trade) {

	boolean contains = false;


	if( trade instanceof CryptoCoinTrade) {

	    CryptoCoinTrade ctr = (CryptoCoinTrade)trade;

	    // For now, the easiest way to find a trade is the trade site name and the site id...
	    String queryStatement = "SELECT * from trades WHERE tradesite=" + ctr.getSite().getName() 
		+ " and siteId=" + ctr.getId() + ";";
	    
	    ResultSet resultSet = executeQuery( queryStatement);

	    try {
		// If the number of rows is > 0, the trade is in the database.
		while( resultSet.next()) {
		    contains = true;
		    break;  // We only need 1 row. That's enough to know.
		}
	    } catch( SQLException se) {
		throw new TradeDataNotAvailableException( "Error while checking for required tables in database: " + se);
	    }
	} else { 
	    throw new TradeDataNotAvailableException( "CachePersistenceMysql.contains() can only check for CryptoCoinTrades for now!");
	}
	
	return contains;
    }

    /**
     * If the tables do not exist, create them.
     *
     * @return true, if the tables were successfully created. false otherwise.
     */
    private boolean createTables() {

	// Loop over the map of create statements.
	for( Map.Entry<String, String> currentTable : _createStatements.entrySet()) {

	    String tablename = currentTable.getKey();
	    String createStatement = "CREATE TABLE " 
		+ tablename 
		+ " ("
		+ currentTable.getValue()
		+ " ) TYPE = MyISAM AUTO_INCREMENT=1;";

	    System.out.println( createStatement);

	    executeStatement( createStatement);  // Create the table.
	}

	return true;  // No errors.
    }

    /**
     * Execute a query on the database and return the result set.
     *
     * @param queryStatement The query to execute.
     *
     * @return The result of the query as a ResultSet object.
     */
    private final ResultSet executeQuery( String queryStatement) {

	try {
	    Statement statement = _connection.createStatement();  // Create a new statement.

	    ResultSet resultSet = statement.executeQuery( queryStatement);  // Execute the query and fetch the result.

	    return resultSet;  // Return the result.

	} catch (SQLException se) {
	    LogUtils.getInstance().getLogger().error( "Cannot execute query on mysql database: " + se);
	}

	return null;  // An error occured...
    }

    /**
     * Execute a statement on the mysql database.
     *
     * @param statement The statement to execute.
     */
    private final boolean executeStatement( String statementString) {

	Statement statement = null;

	try {
	    statement = _connection.createStatement();  // Create a new statement from the connection.

	    statement.executeUpdate( statementString);

	    return true;  // Statement worked.

	} catch( SQLException se) {
	    LogUtils.getInstance().getLogger().error( "Error executing a statement: " + se);
	} finally {
	    if( statement != null) {
		try {
		    statement.close();  // Close the operation.
		} catch( SQLException se) {
		    LogUtils.getInstance().getLogger().error( "Error closing the statement: " + se);
		}
	    }
	}

	return false;  // An error occured.
    }

    /**
     * Get the timestamp of the newest trade for a given trade site and currency pair.
     *
     * @param tradeSite The trade site to query.
     * @param currencyPair The currency pair to use.
     *
     * @return The gmt-relative timestamp of the newest trade, or -1 if no such trade is in the db.
     */
    public long getNewestTimestamp( TradeSite tradeSite, CurrencyPair currencyPair) {
	throw new NotYetImplementedException( "Getting the newest timestamp is not yet implemented for the mysql backup");
    }

    /**
     * Get the section name in the global property file.
     */
    public String getPropertySectionName() {
	return "CachePersistenceMySQL";
    }

    /**
     * Get the settings of the mysql persistence for trades.
     *
     * @return The setting of the mysql persistence as a list of key<=>value pairs.
     */
    public PersistentPropertyList getSettings() {

	// Create a new list.
	PersistentPropertyList result = new PersistentPropertyList();

	result.add( new PersistentProperty( "Username", null, _username, 5));
	result.add( new PersistentProperty( "Password", null, _password, 4));
	result.add( new PersistentProperty( "Database", null, _databaseName, 3));  
	result.add( new PersistentProperty( "Host", null, _host, 2)); 
	result.add( new PersistentProperty( "Activated_boolean", null, _activated ? "true" : "false", 1)); 

	return result;
    }

    /**
     * Merge a given list of trades into the database.
     *
     * @param trades A list of trades to merge.
     */
    public void merge( Trade [] trades) {

	// The following code is simple, but not very efficient, since it might be better if a whole set of
	// trades is neer than the newest trade in the database and then insert all the trades at once.
	for( int index=0; index < trades.length; ++index) {

	    if( ! contains( trades[ index])) {  // If this trade is not in the database.
		add( trades[ index]);           // add this trade to the database.
	    }
	}
    }

    /**
     * Activate or deactivate the persistence.
     *
     * @param activated If true, the persistence is activated. If false, it's deactivated.
     */
    public void setActive( boolean activated) {
	_activated = activated;
    }

    /**
     * Set new settings for the mysql cache persistence..
     *
     * @param settings The new settings for the mysql cache persistence.
     */
    public void setSettings( PersistentPropertyList settings) {

	String currentSetting = settings.getStringProperty( "Username");
	if( currentSetting != null) { 
	    _username = currentSetting;  // Get the username from the settings.
	}
	currentSetting = settings.getStringProperty( "Password");
	if( currentSetting != null) { 
	    _password = currentSetting;  // Get the password from the settings.
	}
	currentSetting = settings.getStringProperty( "Database");
	if( currentSetting != null) { 
	    _databaseName = currentSetting;  // Get the database name from the settings.
	}
	currentSetting = settings.getStringProperty( "Host");
	if( currentSetting != null) { 
	    _host = currentSetting;  // Get the hostname from the settings.
	}
	currentSetting = settings.getStringProperty( "Activated");
	if( currentSetting != null) { 
	    _activated = currentSetting.equals( "true");  // Get the hostname from the settings.
	}       
    }
}
