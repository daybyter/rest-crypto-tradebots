/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.util;

import java.util.Arrays;


/**
 * Class to hold some utility methods for strings.
 */
public class StringUtils {

    // Static variables


    // Instance variables


    // Constructors

    
    // Methods


    /**
     * Create a string of given length filled with a given character.
     * (i.e. fillString( ' ', 5) returns "     ") .
     *
     * @param character The character to fill the string with.
     * @param length The length of the string to create.
     *
     * @return The created string, that is filled with the given character.
     */
    public final static String fillString( Character character, int length) {

	char [] charBuffer = new char[ length];  // Create a character buffer for the result.

	Arrays.fill( charBuffer, character);     // Fill the array with the characters.

	return new String( charBuffer);          // Create a new string from the buffer and return it.
    }
}
