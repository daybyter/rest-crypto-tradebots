/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot;

import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.util.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;


/**
 * This class is a simple logger for trading activity.
 */
public class TradeLogger {

    // Static variables

    /**
     * The number of messages in the mail buffer, before it is sent.
     */
    private final static int MAILBUFFER_SIZE = 10;


    // Instance variables

    /**
     * A date format for the log messages.
     */
    private SimpleDateFormat _dateLogFormat = new SimpleDateFormat(" yyyy/MM/dd HH:mm:ss");

    /**
     * The name of the logfile.
     */
    private String _filename = null;

    /**
     * A reader to read the logfile for status checks.
     */
    private BufferedReader _inputReader = null;

    /**
     * The last logged message.
     */
    private String _lastLoggedEntry = null;

    /**
     * A buffer for the log messages, that are send in the next mail.
     */
    private LinkedList<String> _mailBuffer = null;

    /**
     * The address for the mail notifications.
     */
    private String _mailRecipient = null;

    /**
     * A buffered writer to write the strings to the log file.
     */
    private BufferedWriter _outputWriter = null;

    /**
     * Flag to indicate, that the logger should mail the log entries from time to time.
     */
    private boolean _sendMailFlag = false;


    // Constructors

    /**
     * Create a new TradeLogger instance.
     *
     * @param filename The filename to use for the logger.
     */
    public TradeLogger( String filename) {

	_filename = filename;  // Store the filename in the instance.

	try{ 

	    // Create a file in the users home directory and a writer for it.
	    _outputWriter = new BufferedWriter( new FileWriter(  getAbsoluteLogfileName(), true));

	    // Create a reader for that same file.
	    _inputReader = new BufferedReader( new FileReader( getAbsoluteLogfileName()));

	} catch( IOException ioe) {

	    LogUtils.getInstance().getLogger().error( "Could not create or open logfile for trades: " + ioe);
	}
    }


    // Methods

    /**
     * Activate or deactivate the mail summary.
     *
     * @param active true, to activate the mail sending. False otherwise.
     */
    public final synchronized void activateEmailNotification( boolean active) {

	_sendMailFlag = active;

	// Create or destroy the buffer.
	_mailBuffer = _sendMailFlag ? new LinkedList<String>() : null;
    }

    /**
     * Check, if the mail buffer has to be sent.
     */
    private final synchronized void checkForMailSending() {

	if( _mailBuffer.size() >= MAILBUFFER_SIZE) {

	    // Send mail.
	    StringBuffer mailTextBuffer = new StringBuffer();

	    // Start with a mail header.
	    mailTextBuffer.append( "Recent tradebot activities\n");
	    mailTextBuffer.append( "------------------------------");

	    while( _mailBuffer.size() > 0) {
		
		// Remove the first log entry in the buffer and append it to the mail text.
		mailTextBuffer.append( _mailBuffer.removeFirst());
	    }

	    mailTextBuffer.append( "------------------------------");

	    TradeApp.getApp().getEmailNotifier().sendMail( _mailRecipient, "Tradebot notification", mailTextBuffer.toString());
	}
    }

    /**
     * The destructor for the logger.
     */
    protected void finalize ()  {
	try{
	    _outputWriter.close();   // Close the output file.

	    _inputReader.close();    // Close the input stream.

	} catch( IOException ioe) {
	    LogUtils.getInstance().getLogger().error( "Could not close logfile for trades: " + ioe);
	}
    }

    /**
     * Get the absolute filename of the logfile.
     *
     * @return The absolute filename of the logfile.
     */
    public final String getAbsoluteLogfileName() {

	// Store the trade log in the homedir of the user.
	return System.getProperty("user.home") + "/.tradeapp/" + _filename;
    }

    /**
     * Get the entire logfile as an array of strings.
     *
     * @return The logfile lines as an array of String objects.
     *
     * @throws IOException if reading the logfile did not work.
     */
    public final synchronized String [] getFullLog() throws IOException {

	ArrayList<String> resultBuffer = new ArrayList<String>();

	String currentLine = _inputReader.readLine();
	
	while( currentLine != null) {
	    
	    resultBuffer.add( currentLine);
	    
	    currentLine = _inputReader.readLine();
	}
	
	// Convert the buffer to an array and return it.
	return resultBuffer.toArray( new String[ resultBuffer.size()]);
    }
    
    /**
     * Return the last logged entry, or null if nothing was logged so far.
     *
     * @return The last log entry, or null, if there are no entries yet.
     */
    public final String getLastLoggedEntry() {

	return _lastLoggedEntry;
    }

    /**
     * Log a message.
     *
     * @param message The message to log.
     */
    public final synchronized void log( String message) {

	StringBuffer loggedMessageBuffer = new StringBuffer();  // A buffer to create the actually logged line.

	loggedMessageBuffer.append( _dateLogFormat.format( Calendar.getInstance().getTime()));
	loggedMessageBuffer.append( " - ");
	loggedMessageBuffer.append( message);
	loggedMessageBuffer.append( System.getProperty("line.separator"));  // End this line.

	String loggedMessage = loggedMessageBuffer.toString();
	
	try {
	    _outputWriter.write( loggedMessage);
	    _outputWriter.flush();

	    _lastLoggedEntry = loggedMessage;   // Store the message to return it on request.

	} catch( IOException ioe) {
	    LogUtils.getInstance().getLogger().error( "Could not write log to trade logfile: " + ioe);
	}

	// If the user wants mail, add the entry to the mail buffer.
	if( _sendMailFlag) {
	    
	    _mailBuffer.add( loggedMessage);

	    // Check, if the mail has to be sent now.
	    checkForMailSending();
	}
    }

    /**
     * Set the address of the mail recipient.
     *
     * @param mailAddress The mail address of the mail recipient.
     */
    public final void setMailRecipient( String mailAddress) {

	_mailRecipient = mailAddress;
    }
}
