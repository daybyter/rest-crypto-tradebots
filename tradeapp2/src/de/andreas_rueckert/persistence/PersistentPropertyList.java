/**
 * Java implementation of a list made of persistent property objects.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.persistence;

import java.util.ArrayList;


/**
 * This class represents a list of persistent property objects.
 * It's main purpose is, to encapsulate search methods for those 
 * property objects.
 */
public class PersistentPropertyList extends ArrayList<PersistentProperty> {

    // Static variables


    // Instance variables


    // Constructors


    // Methods

    /**
     * Get a property with a given name.
     *
     * @param name The name of the property.
     *
     * @return The property with the given name or null, if no such property in known.
     */
    public PersistentProperty getPropertyForName( String propertyName) {

	for( PersistentProperty property : this) {

	    if( propertyName.equals( property.getName())) {
		return property;
	    }
	}
	return null;  // Found no String value for this property name.
    }

    /**
     * Get a property, that is represented as a String object.
     *
     * @param propertyName The name of the property.
     *
     * @return The value of the property as a String object, or null, if the property is not in the list.
     */
    public String getStringProperty( String propertyName) {

	for( PersistentProperty property : this) {
	    if( propertyName.equals( property.getName())) {

		Object currentValue = property.getValue();  // Try to get a value for this property.
  
		if( ( currentValue != null) && ( currentValue instanceof String) && ( ! "null".equals( (String)currentValue))) {
		    return (String)currentValue;
		}
	    }
	}
	return null;  // Found no String value for this property name.
    }
}