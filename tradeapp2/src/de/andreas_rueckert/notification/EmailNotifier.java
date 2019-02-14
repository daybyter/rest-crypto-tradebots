/**
 * Java implementation of a chart provider.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.notification;

import de.andreas_rueckert.persistence.PersistentProperties;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;


/**
 * A class to notify the user via email.
 */
public class EmailNotifier implements PersistentProperties {

    // Inner classes

    /**
     * Private class for SMTP authentication.
     * Taken from: http://www.tutorials.de/java/255387-email-mit-javamail-versenden.html
     */
    class MailAuthenticator extends Authenticator {
	
        /**
         * The user name.
         */
        private final String _user;
	
        /**
         * The password.
         */
        private final String _password;
 
        /**
         * Create a new object for SMTP authentication.
	 *
         * @param user The user name.
         * @param password The password.
         */
        public MailAuthenticator( String user, String password) {
            _user = user;
            _password = password;
        }
 
        /**
         * Create a new password authentication instance.
	 * 
         * @see javax.mail.Authenticator#getPasswordAuthentication()
         */
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication( _user, _password);
        }
    }


    // Static variables

    
    // Instance variables

    /**
     * The password for the SMTP transport.
     */
    String _password = null;
    
    /**
     * The sender address for the transport.
     */
    String _senderAddress = null;
    
    /**
     * The SMTP host to use for the transport.
     */
    String _smtpHost = null;
    
    /**
     * The user name for the SMTP transport.
     */
    String _username = null;


    // Constructors

    /**
     * Create a new mail notifier object.
     *
     * @param smtpHost The SMTP server to use.
     * @param username The user name of the account.
     * @param password The password to for the user.
     * @param senderAddress The email address of the sender.
     */
    public EmailNotifier( String smtpHost, String username, String password, String senderAddress) {
	_smtpHost = smtpHost;
	_username = username;
	_password = password;
	_senderAddress = senderAddress;
    }


    // Methods

    /**
     * Get a section name for the global property file.
     *
     * @return A section name for the global property file.
     */
    public String getPropertySectionName() {
	return "EmailNotifier";
    }

    /**
     * Get the settings of the mail provider.
     *
     * @return The current settings of the mail notifier.
     */
    public PersistentPropertyList getSettings() {
	
	// Create a new list for the settings.
        PersistentPropertyList result = new PersistentPropertyList();
	
	result.add( new PersistentProperty( "SMTP host", null, _smtpHost, 4));  // Copy the current mail parameters to the settings.
	result.add( new PersistentProperty( "Username", null, _username, 3));
	result.add( new PersistentProperty( "Password", null, _password, 2));
	result.add( new PersistentProperty( "Sender address", null, _senderAddress, 1));

	return result;  // Return the map with the settings.
    }

    /**
     * Send a new mail.
     *
     * @param recipientAddress The address of the recipient.
     * @param subject The subject of the mail.
     * @param text The text of the mail.
     */
    public void sendMail( String recipientsAddress, String subject, String text ) {

	if( ( _username == null) || ( "".equals( _username))) {
	    System.err.println( "No email username given and authentication is activated by default");
	    return;
	}
	
	if( ( _password == null) || ( "".equals( _password))) {
	    System.err.println( "No email password given and authentication is activated by default");
	    return;
	}

	// Create a new authenticator for the mail server.
        MailAuthenticator auth = new MailAuthenticator( _username, _password);
 
	// The mail properties.
        Properties properties = new Properties();

	if( ( _smtpHost == null) || ( "".equals(_smtpHost))) {
	    System.err.println( "No SMTP host given in configuration");
	    return;
	}
 
        // Add the mailserver address to the properties.
        properties.put( "mail.smtp.host", _smtpHost);
 
	// Activate the authentication, if the server requires it.
        properties.put( "mail.smtp.auth", "true");
 
	// Create a new session for the mail transport.
        Session session = Session.getDefaultInstance( properties, auth);
 
        try {
            // Create a new message.
            Message msg = new MimeMessage( session);
 
            // Set the sender and recipient address.
            msg.setFrom( new InternetAddress( _senderAddress));
            msg.setRecipients( Message.RecipientType.TO, InternetAddress.parse( recipientsAddress, false));
 
            // Set mail subject and text.
            msg.setSubject( subject);
            msg.setText( text);
 
            // Add optional additional header lines.
            // msg.setHeader("Test", "Test");
            msg.setSentDate( new Date( ));
 
            // Send the mail.
            Transport.send(msg);
        } catch( Exception e) {
            System.err.println( "Problem sending mail: " + e.toString());
	}
    }

    /**
     * Set new settings for the mail notifier.
     *
     * @param settings The new settings for the mail notifier.
     */
    public void setSettings( PersistentPropertyList settings) {

	// Copy the new settings to the instance variables.
	String host = settings.getStringProperty( "SMTP host");
	if( host != null) {
	    _smtpHost = host;  // Get the SMTP host from the settings.
	}
	String user = settings.getStringProperty( "Username");
	if( user != null) {
	    _username = user;  // Get the username from the settings.
	}
	String password = settings.getStringProperty( "Password");
	if( password != null) {
	    _password = password;  // Get the password from the settings.
	}
	String sender = settings.getStringProperty( "Sender address");
	if( sender != null) {
	    _senderAddress = sender;  // Get the mail sender address from the settings.
	}
    }
}