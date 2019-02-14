/**
 * Java implementation of MountGox API.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Class to load and store user settings.
 */
public class UserSettings {

    // Static variables

    /**
     * The subdirectory for the app properties.
     */
    private static String SETTINGS_SUBDIRECTORY = ".mtgox";

    /**
     * The name of the property file.
     */
    private static String SETTINGS_FILENAME = "mtgox.properties";


    // Instance variables

    /**
     * The password for the MtGox service.
     */
    private String _mtGoxPassword;

    /**
     * The username for the MtGox service.
     */
    private String _mtGoxUser;

    /**
     * The system properties.
     */
    private Properties _properties = null;

    /**
     * The directory for the property file.
     */
    private String _propertyDirectory = null;


    // Constructors


    // Methods

    /**
     * Check if property directory exists and try to create it, if not.
     *
     * @return true, if the directory exists after the check. false otherwise.
     */
    private boolean checkPropertyDirectory() {
	File directory = new File( getPropertyDirectory());

	if( directory.exists()) {  // If the directory already exists
	    return true;           // Return ok...
	}

        return directory.mkdirs();  // Try to create all necessary subdirectories otherwise.
    }


    /**
     * Get the directory for the property file.
     *
     * @return The directory for the property file.
     */
    private String getPropertyDirectory() {
	if( _propertyDirectory == null) {
	    _propertyDirectory = System.getProperty( "user.home");

	    _propertyDirectory += System.getProperty( "file.separator");

	    _propertyDirectory += SETTINGS_SUBDIRECTORY;
	}

	return _propertyDirectory;
    }


    /**
     * Get the user properties.
     *
     * @return The user properties.
     */
    private Properties getProperties() {
	if( _properties == null) {
	    _properties = new Properties();
	}

	return _properties;
    }


    /**
     * Load the user specific settings.
     *
     * @return true, if the properties were successfully loaded, false otherwise.
     */
    public boolean load() {
        try {
	    File propsFile = new File( getPropertyDirectory(), SETTINGS_FILENAME);

            getProperties().load( new FileInputStream( propsFile));

	    return true;

        } catch( FileNotFoundException e) {
            System.err.println("MtGox property file not found");

	    return false;

        } catch( IOException ioe) {
            System.err.println( "Error loading user properties: " + ioe.toString());

	    return false;
        }
    }

    
    /**
     * Write the user properties to a file.
     *
     * @return true, if the properties were successfully written, false otherwise.
     */
    public boolean store() {

	try {

	    if( ! checkPropertyDirectory()) {
		System.err.println( "Could not create property directory");
		
		return false;
	    }

	    File propsFile = new File( getPropertyDirectory(), SETTINGS_FILENAME);

	    getProperties().store( new FileOutputStream( propsFile), null);

	    return true;

	} catch (IOException ioe) {
	    System.err.println( "Error writing user properties: " + ioe.toString());
	    
	    return false;
	}
    }
}