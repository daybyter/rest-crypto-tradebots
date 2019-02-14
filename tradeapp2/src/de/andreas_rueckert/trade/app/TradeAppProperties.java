/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app;

import de.andreas_rueckert.persistence.PersistentProperties;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.persistence.SortedProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/** 
 * Class to read (and write?) property files for the trade app.
 */
public class TradeAppProperties {
 
    // Static variables

    
    // Instance variables

    /**
     * The filename of the property file.
     */
    private String _filename = null;

    /**
     * The properties of the app.
     */
    private SortedProperties _properties;

    /**
     * This is an array of all the objects, that want to store their properties.
     */
    private ArrayList<PersistentProperties> _registeredPropertyObjects;


    // Constructors

    /**
     * Create a new property file reader.
     */
    public TradeAppProperties() {

	// Create a filename for the properties file.
	_filename = System.getProperty("user.home") + "/.tradeapp/" + "tradeapp.properties";

	_properties = new SortedProperties();

	// Create a new array list for the objects, that want to store their settings.
	_registeredPropertyObjects = new ArrayList<PersistentProperties>();
    }


    // Methods

    /**
     * Check, that the main directory for the tradeapp files exists.
     */
    private void check4TradeAppDir() {
	File propertyFile = new File( _filename);  // Create temporarily a file for the actual property file.

	// System.out.println( "Debug: checking for settings dir: " + propertyFile.getParentFile().toString());

	propertyFile.getParentFile().mkdirs();  // Create all the dirs to that path, if they don't exist yet.
    }

    /**
     * Read the properties from the property file.
     */
    public void read() throws IOException {

	// Load the physical property file.
	_properties.load( new FileInputStream( _filename));

	// Iterate over all the registered objects
	for( PersistentProperties pp : _registeredPropertyObjects) {
	    
	    // Get the prefix for the properties of this section.
	    String prefix = URLEncoder.encode( pp.getPropertySectionName()) + ".";

	    // Get the current map of settings from the object.
	    PersistentPropertyList currentSettings = pp.getSettings();

	    // Iterate over the current settings.
	    for( PersistentProperty setting : currentSettings) {

		// Compute the next property name
		String propertyName = prefix + URLEncoder.encode( setting.getName());
		    
		// Get the value for this property from the property file.
		String propertyValue = _properties.getProperty( propertyName);

		// If there is a value stored in the file, unescape it and store it in the current settings.
		if( propertyValue != null) {

		    setting.setValue( URLDecoder.decode( propertyValue));
		}
	    }
	
	    // Now store the modified settings back in the object.
	    pp.setSettings( currentSettings);
	}
    }

    /**
     * Register a new object to get it's properties stored.
     *
     * @param pobj The object to store it's properties.
     */
    public void registerPersistentPropertyObject( PersistentProperties pobj) {
	_registeredPropertyObjects.add( pobj);
    }

    /**
     * Write the properties to the property file.
     */
    public void write() throws IOException {

	check4TradeAppDir();  // Make sure, that the directory for the property file exists.

	// Iterate over all the registered objects
	for( PersistentProperties pp : _registeredPropertyObjects) {
	    
	    // Get the prefix for the properties of this section.
	    String prefix = URLEncoder.encode( pp.getPropertySectionName()) + ".";

	    // Now get the settings from the object.
	    for( PersistentProperty setting : pp.getSettings()) {

		// Now set each property in the file with the associated prefix. If the property is null, store an empty string.
		Object settingObject = setting.getValue();
		String settingString = settingObject == null ? "" : settingObject.toString();
		_properties.setProperty( prefix + URLEncoder.encode( setting.getName()), URLEncoder.encode( settingString));
	    }
	}

	_properties.store( new FileOutputStream( _filename), null);
    }
}
