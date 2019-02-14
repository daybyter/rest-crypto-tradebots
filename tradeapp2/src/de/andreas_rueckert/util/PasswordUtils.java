/**
 * Java implementation of password encryption.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.util;

import de.andreas_rueckert.trade.app.TradeApp;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * This class holds utility methods for passwords.
 */
public class PasswordUtils {

    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static PasswordUtils _instance = null;

    /**
     * The algorithm to use for password encryption.
     */
    private static String PASSWORD_ALGORITHM = "SHA-1";


    // Instance variables
    
    
    // Constructors


    // Methods

    /**
     * Encrypt a password and return a hex string of the encrypted password.
     *
     * @param password The password to encrypt.
     *
     * @return The encrypted password as a hex string.
     *
     * @see http://www.rgagnon.com/javadetails/java-0400.html
     */
    public final String encryptPassword( String password) {

        MessageDigest digest = null;  // Create a new message digest.

	try {

	    // Create a digest with the used algorithm.
	    digest = MessageDigest.getInstance( PASSWORD_ALGORITHM);
	    
	    digest.reset();
	    digest.update( password.getBytes());

	    byte [] digestBytes = digest.digest();
	    
	    StringBuffer encryptedPasswordBuffer = new StringBuffer( digestBytes.length * 2);
	
	    // Loop over the bytes of the digest.
	    for( int i = 0; i < digestBytes.length; ++i){
		
		int currentByte = digestBytes[i] & 0xff;
		
		if( currentByte < 16) {
		    encryptedPasswordBuffer.append( '0');
		}
		encryptedPasswordBuffer.append( Integer.toHexString( currentByte));
	    }
	    
	    return encryptedPasswordBuffer.toString().toUpperCase();

	} catch( NoSuchAlgorithmException nsae) {  // Should never happen. 

	    // This error should mean, that the JRE implementation is broken.
	    LogUtils.getInstance().getLogger().error( "SHA-1 Algorithm not available in PasswordUtils: " + nsae.toString());
	}

	return null;  // Should never be reached.
    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static PasswordUtils getInstance() {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new PasswordUtils();  // create one.
	}

	return _instance;  // Return the only instance.
    }
}
