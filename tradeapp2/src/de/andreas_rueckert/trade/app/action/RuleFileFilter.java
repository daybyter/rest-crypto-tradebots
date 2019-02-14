/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.action;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * File filter for rule sets.
 */
public class RuleFileFilter extends FileFilter {

    // Static variables

    /**
     * The only instance of this filter.
     */
    private static RuleFileFilter _instance = null;


    // Instance variables


    // Constructors

    /**
     * Create a new file filter for rule sets.
     */
    private RuleFileFilter() {
	super();
    }


    // Methods

    /**
     * Accept all directories and files with the rule set suffix.
     *
     * @param file The file to check for acceptance.
     *
     * @return true, if the file was accepted, or false otherwise.
     */
    public boolean accept(File file) {
        if( file.isDirectory()) { return true; }

        String extension = getFileExtension(file);

        return ( extension != null) && ( extension.equals( "xml") 
					 || extension.equals( "drl")
					 || extension.equals( "ctl"));
    }

    /**
     * The description of this filter filter.
     *
     * @return The description of this file filter.
     */
    public String getDescription() {
        return "A file filter for rule set files.";
    }

    /**
     * Get the extension of a given file.
     *
     * @param file The given file.
     *
     * @return The extension of the file.
     */
    private String getFileExtension( File file) {
        String filename = file.getName();
        int lastSeparatorIndex = filename.lastIndexOf( '.');

        return ( lastSeparatorIndex > 0) && ( lastSeparatorIndex < filename.length() - 1) ? filename.substring( ++lastSeparatorIndex).toLowerCase() : null;
    }

    /**
     * Get the only instance of this file filter.
     *
     * @return The only instance of this file filter.
     */
    public static RuleFileFilter getInstance() {
	if( _instance == null) {
	    _instance = new RuleFileFilter();
	}
	return _instance;
    }
}

