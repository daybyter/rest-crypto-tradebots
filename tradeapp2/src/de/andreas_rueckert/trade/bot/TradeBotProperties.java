/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot;

import de.andreas_rueckert.persistence.PersistentProperties;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.persistence.SortedProperties;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.util.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Properties;


/**
 * Class to read and write the properties of some trade bot.
 */
public class TradeBotProperties {

    // Static variables


    // Instance variables
    
    /**
     * The trade bot.
     */
    private TradeBot _tradeBot = null;
    
    /**
     * The filename of the property file.
     */
    private String _filename = null;

    /**
     * The properties of the bot.
     */
    private SortedProperties _properties;

    /**
     * This is an array of all the objects, that want to store their properties.
     */
    private ArrayList<PersistentProperties> _registeredPropertyObjects = new ArrayList<PersistentProperties>();


    // Constructors

    /**
     * Create a new property handler for a bot.
     *
     * @param tradeBot The trade bot, that needs persistent properties.
     */
    public TradeBotProperties( TradeBot tradeBot) {

	// Store a reference to the bot in this instance.
	_tradeBot = tradeBot;

	// Try to register this bot as persistent property object, if it implements the interface.
	if( _tradeBot instanceof PersistentProperties) {

	    registerPersistentPropertyObject( _tradeBot);
	}

	// Compute a name for the properties file
	_filename = System.getProperty("user.home") + "/.tradeapp/" + _tradeBot.getName() + "_bot" + ".properties";

	// Create a new property object.
	_properties = new SortedProperties();
    }


    // Methods

    /**
     * Check, that the main directory for the property files exists.
     */
    private final void check4PropertyDir() {
	File propertyFile = new File( _filename);  // Create temporarily a file for the actual property file.

	propertyFile.getParentFile().mkdirs();  // Create all the dirs to that path, if they don't exist yet.
    }

    /**
     * Read the properties from the property file.
     */
    public final void read() throws IOException {

	// Make sure, that the property directory exists.
	// check4PropertyDir();

	// Load the properties from their file.
	_properties.load( new FileInputStream( _filename));

	// Loop over the registered properties and try to set them.
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

	    // Now check, if there are settings in the properties, that are yet unknown to the bot
	    // like a number of user accounts, that the bots doesn't know, when it is started.
	    for( String propertyName : _properties.stringPropertyNames()) {

		// Remove the prefix from the property name, as it is used in the bot.
		String cleanedName = propertyName.substring( prefix.length());

		// Try to find this property in the bot setttings.
		PersistentProperty setting = currentSettings.getPropertyForName( cleanedName);

		// If this property is not yet known, add it to the settings.
		if( setting == null) {

		    // the priority currentSettings.size() is an ugly hack here! The bot should better provide a
		    // priority instead! Maybe create some emtpy accounts in getSettings() and remove the settings
		    // in setSettings then? But this would limit the number of total accounts?
		    currentSettings.add( new PersistentProperty( cleanedName, null, URLDecoder.decode( _properties.getProperty( propertyName)), currentSettings.size()));
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
     * Write the trade bot properties to their file.
     */
    public final void write() throws IOException {

	// Make sure, that the property directory exists.
	check4PropertyDir();

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