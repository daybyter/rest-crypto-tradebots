/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.persistence;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;


/**
 * This is just a small utility class to store properties sorted,
 * so they can easier be read in the property file.
 */
public class SortedProperties extends Properties {

    // Static variables


    // Instance variables

    
    // Constructors


    // Methods

    /**
     * Overwrite the original keys() method and sort
     * the keys.
     * 
     * @return The sorted keys.
     */
    public Enumeration keys() {
	Enumeration keysEnum = super.keys();  // Get the keys from the original Properties class.
 
	Vector<String> keyList = new Vector<String>();  // Convert them to a list of String objects.
	while(keysEnum.hasMoreElements()){
	    keyList.add((String)keysEnum.nextElement());
	}

	Collections.sort(keyList);  // Sort the strings.

	return keyList.elements();  // And return the sorted list.
  }
}