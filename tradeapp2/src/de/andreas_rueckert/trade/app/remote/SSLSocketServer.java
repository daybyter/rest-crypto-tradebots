/**
 * Java implementation of a service for remote control.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.remote;

import de.andreas_rueckert.persistence.PersistentProperties;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.app.remote.command.CommandListBots;
import de.andreas_rueckert.trade.app.remote.command.RemoteCommand;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.PasswordUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * This class provides an SSL socket to remote control an app.
 *
 * @see http://stilius.net/java/java_ssl.php
 *
 * @see http://stackoverflow.com/questions/5321003/socket-authentication-server
 * for the authentication code.
 */
public class SSLSocketServer implements PersistentProperties {

    // Static variables

    /**
     * The only instance of this class.
     */
    private static SSLSocketServer _instance = null;

    /**
     * The max length of a session in microseconds.
     */
    private final static long MAX_SESSION_LENGTH = 1L * 60L * 60L * 1000000L;  // 1 hour for now.

    /**
     * The port number for the server to listen at.
     */
    private static final int PORT = 8082;

    /**
     * The prompt for the user input.
     */
    private static final String PROMPT = "=>";

    /**
     * The states of the shell (organized like in a state machine).
     */
    private final static byte STATE_NOT_LOGGED_IN = 0;
    private final static byte STATE_ACCOUNT_REQUESTED = 1;
    private final static byte STATE_PASSWORD_REQUESTED = 2;
    private final static byte STATE_LOGGED_IN = 3;


    // Instance variables

    /**
     * The user name of the admin.
     */
    private String _login = null;

    /**
     * The hashed password of the admin.
     */
    private String _encryptedPassword = null;

    /**
     * A map of the registered commands.
     * I use the name of the command as the hash key for faster access to the command.
     */
    private Map< String, RemoteCommand> _registeredCommands = new HashMap< String, RemoteCommand>();

    /**
     * The server thread.
     */
    private Thread _serverThread = null;

    /**
     * The timestamp when the session was started.
     */
    private long _sessionStartTimestamp = -1L;

    /**
     * The current state of the shell.
     */
    private byte _state = STATE_NOT_LOGGED_IN;


    // Constructors

    /**
     * Create a new SSL socket server. 
     * Constructor is private for singleton pattern.
     */
    private SSLSocketServer() {

	// Register all the known commands.
	registerCommand( CommandListBots.getInstance( this));
    }


    // Methods

    /**
     * Check, if the user entered the correct account data.
     *
     * @param login The entered login.
     * @param password The entered password.
     *
     * @return true, if the entered account is correct. False otherwise.
     */
    private final boolean checkAccount( String login, String password) {

	// Check, if there are account data stored and if they
	// equal the entered account data.
	return (_login != null) 
	    && ( login != null)
	    && _login.equals( login)
	    && ( _encryptedPassword != null)
	    && ( password != null)
	    && PasswordUtils.getInstance().encryptPassword( password).equals( _encryptedPassword);
	
    }


    /**
     * Evaluate the current input line of the shell.
     *
     * @param inputLine The input line to evaluate.
     * @param printStream The output stream.
     */
    private final void eval( String inputLine, PrintStream printStream) {

	// Try to split command and arguments first.
	String [] parts = inputLine.split( " ");

	// The first part is usually the command.
	if( parts.length < 1) {  // Is there no command?
	    printStream.println( "Error: no command given");
	} else {
	    
	    // Try to find the (trimmed) command in the registered commands.
	    RemoteCommand givenCommand = _registeredCommands.get( parts[ 0].trim());

	    if( givenCommand == null) {  // If this is not a registered command...
		printStream.println( "'" + parts[ 0] + "' is not a registered command");
	    } else {

		// Transform the remaining arguments of the command line into a map and
		// pass them to the command.

		Map<String, String> parameterMap = new HashMap< String, String>();  // The map of parameters.
		
		for( int currentParameterIndex = 1; currentParameterIndex < parts.length; ++currentParameterIndex) {
		    
		    // Get the current argument and trim it.
		    String currentParameter = parts[ currentParameterIndex].trim();

		    // Now try to split the parameter into key and value (if there is a value in this parameters).
		    String [] splittedParameter = currentParameter.split( "=");

		    if( splittedParameter.length > 2) {  // If there is more than 1 '=' in the parameter,

			// Print an error...
			printStream.println( "Error: illegal parameter<=>value format: " + currentParameter);
			
			return;  // ...and stop the evaluation of the command.
		    }

		    // Add this parameter to the map of parameters. 
		    // If there is no value, just use null as the map hash value.
		    parameterMap.put( splittedParameter[0], splittedParameter.length == 2 ? splittedParameter[ 1] : null);
		}

		// Now are all the parameters parsed, so execute the command with the given parameters,
		// and print the returned string.
		printStream.print( givenCommand.execute( parameterMap));
	    }
	}
    }

    /**
     * Get the only instance of this class.
     *
     * @return The only instance of this class.
     */
    public static SSLSocketServer getInstance() {

	if( _instance == null) {  // If there is no instance of this class yet,

	    _instance = new SSLSocketServer();  // create one.
	}

	return _instance;  // Return the only instance of this class.
    }

    /**
     * Get the section name in the global property file.
     */
    public String getPropertySectionName() {
	return "SSLSocketServer";
    }
    
    /**
     * Get the registered commands.
     *
     * @return The registered commands.
     */
    public Collection<RemoteCommand> getRegisteredCommands() {

	return _registeredCommands.values();
    }

    /**
     * Get the settings of the btc-e client.
     *
     * @return The setting of the SSL socket server as a list.
     */
    public PersistentPropertyList getSettings() {

	// Get the settings from the base class.
	PersistentPropertyList result = new PersistentPropertyList();

	result.add( new PersistentProperty( "Login", null, _login, 2));
	result.add( new PersistentProperty( "Password", null, _encryptedPassword, 1, true));

	return result;
    }

    /**
     * Check, if the user is authenticated in the current session.
     *
     * @return true, if the user is authenticated at the moment. False otherwise.
     */
    private boolean isAuthenticated() {

	// Check, if the current session is not too old.
	return ( ( TimeUtils.getInstance().getCurrentGMTTimeMicros() - _sessionStartTimestamp) < MAX_SESSION_LENGTH);
    }

    /**
     * Register a new command in this server.
     *
     * @param command The command to register.
     */
    private void registerCommand( RemoteCommand command) {

	// Put this command in the map of the registered commands.
	_registeredCommands.put( command.getName(), command);
    }

    /**
     * Set new settings for the SSL server.
     *
     * @param settings The new settings for the SSL server.
     */
    public void setSettings( PersistentPropertyList settings) {
	
	String login = settings.getStringProperty( "Login");
	if( login != null) {
	    _login = login;  // Get the login from the settings.
	}
	String encryptedPassword =  settings.getStringProperty( "Password");
	if( encryptedPassword != null) {
	    _encryptedPassword = encryptedPassword;  // Get the encryptedPassword from the settings.
	}
    }

    /**
     * Start the remote control socket service.
     */
    public void start() {

	// Create a thread to listen for incoming socket connections.
	_serverThread = new Thread() {

		/**
		 * The main server thread.
		 *
		 * @see http://stackoverflow.com/questions/8656270/how-to-make-a-ssl-tcp-server-with-java
		 */
		@Override public void run() {

		    // Create a new socket factory...
		    SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		    SSLServerSocket sslserversocket = null;

		    while( _serverThread == this) {  // While the server is not stopped.

			try {

			    sslserversocket = (SSLServerSocket)sslserversocketfactory.createServerSocket( PORT);
			    
			    LogUtils.getInstance().getLogger().info( "Opening server socket for SSL connection");

			    // a "blocking" call which waits until a connection is requested
			    SSLSocket clientSocket = (SSLSocket)sslserversocket.accept();
			    
			    LogUtils.getInstance().getLogger().info( "Accepted connection from client");
			    
			    // open up IO streams
			    DataInputStream  input  = new DataInputStream( clientSocket.getInputStream());
			    OutputStream output = clientSocket.getOutputStream();
			    PrintStream printStream = new PrintStream( output);
			    
			    String currentInputLine;  // The current input.
			    
			    String enteredLogin = null;     // The login entered by the user.
			    String enteredPassword = null;  // The password entered by the user.
			    
			    // waits for data and reads it in until connection dies
			    // readLine() blocks until the server receives a new line from client
			    while( true) {
				
				if( _state == STATE_NOT_LOGGED_IN) {  // If the user is not logged in yet...

				    printStream.println( "Login: ");  // Ask him/her to log in.
				    printStream.flush();

				    if( ( enteredLogin = input.readLine()) != null) {  // If the entered an account..

					_state = STATE_ACCOUNT_REQUESTED;  // .. ask for a password next.

				    }
				} else if( _state == STATE_ACCOUNT_REQUESTED) {  // If the login was already entered...

				    printStream.println( "Password: ");  // Ask for the password now...
				    printStream.flush();

				    if( ( enteredPassword = input.readLine()) != null) {  // If the user entered a password.

					_state = STATE_PASSWORD_REQUESTED;  // Enter the new state.

					if( checkAccount( enteredLogin, enteredPassword)) {  //  Check the entered account data.

					    startSession();  // Start a new session.

					    _state = STATE_LOGGED_IN;  // The user is logged in now.

					    // Let the user know, that the login worked.
					    printStream.println( "Login successful\n");
					    printStream.flush();

					} else {

					    _state = STATE_NOT_LOGGED_IN;  // The account has to be entered again.

					    // Let the user know, that the login failed.
					    printStream.println( "Wrong user and/or password. Login failed.");
					    printStream.flush();
					}
				    }
				} else {
				    printStream.print( PROMPT);

				    if( ( currentInputLine = input.readLine()) == null) {  // If there is no input ( connection is dead)
					break;                              // end the input loop.
				    } else {

					// Evalute the user input.
					eval( currentInputLine, printStream);
				    }
				}
			    }

			    // close IO streams, then socket
			    LogUtils.getInstance().getLogger().info( "Closing connection with client");
			    output.close();
			    input.close();
			    clientSocket.close();

			} catch( IOException ioe) {  // There was an error in the SSL communication...

			    LogUtils.getInstance().getLogger().error( "IOException in SSLSocketServer: " + ioe);
			    
			    // Try to clean up...
			    if( ! sslserversocket.isClosed()) {

				try {  // Since the socket is still open, try to close it.

				    sslserversocket.close();

				    sslserversocket = null;  // Dispose the socket.

				} catch( IOException ioe2) {

				    LogUtils.getInstance().getLogger().error( "Cannot close SSL server socket after error: " + ioe2);

				}
			    }
			}
		    }
		}
	    };

	// Start the listening thread...
	_serverThread.start();
    }

    /**
     * Start a new session for an authenticated user.
     */
    private void startSession() {
	
	// Store the current microsecond time in the session timestamp.
	_sessionStartTimestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros();
    }

    /**
     * Stop the remote control socket service.
     */
    public void stop() {
	
	Thread serverThread = _serverThread;  // So we can join the thread later.
	
	_serverThread = null;  // Signal the thread to stop.

	try {
	    serverThread.join();  // Wait for the thread to end.
	} catch( InterruptedException ie)  {
	    System.err.println( "SSLSocketServer stop join interrupted: " + ie.toString());
	}
    }
}
