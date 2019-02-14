/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.rule;

import de.andreas_rueckert.util.LogUtils;
import java.io.File;


/**
 * Class to load a file with rule sets in various formats.
 */
public class RuleSetLoader {

    // Static variables


    // Instance variables


    // Constructors


    // Methods

    /**
     * Get the extension of a file.
     * @see http://stackoverflow.com/questions/3571223/how-do-i-get-the-file-extension-of-a-file-in-java
     *
     * @param file The file to check.
     *
     * @return The file suffix as a lowercase string, or null, if there is no file suffix.
     */  
    public static String getExtension( File file) {

	String filename = file.getName();  // Get the filename.

	int i = filename.lastIndexOf( '.');  // Get the last dot in the filename.

	if( ( i > 0) && ( i < filename.length() - 1)) {  // If there is a suffix

	    return filename.substring(i+1).toLowerCase();  // Return it.
	}

	return null;  // Default return value is null.
    }

    /**
     * Load a rule set as a ctl (cryptocoin trading language) file.
     *
     * @param ruleSetFile The file with the rule sets to load.
     *
     * @return true, if the file was successfully loaded.
     */
    private boolean loadCtlFile( File ruleSetFile) {

	return false;  // Default is an error.
    }

    /**
     * Load a rule set as a drl (drools rule set) file.
     *
     * @param ruleSetFile The file with the rule sets to load.
     *
     * @return true, if the file was successfully loaded.
     */
    private boolean loadDrlFile( File ruleSetFile) {

	return false;  // Default is an error.
    }

    /**
     * Load a rule set in one of the file formats.
     *
     * @param ruleSetFile The file to load.
     *
     * @return true, if the file was successfully loaded. False otherwise.
     */
    public boolean loadFile( File ruleSetFile) {

	// Get the extension of the rule set file.
	String fileSuffix = getExtension( ruleSetFile);

	if( fileSuffix != null) {

	    if( fileSuffix.equalsIgnoreCase( "drl")) {  // If this is a drl file.

		return loadDrlFile( ruleSetFile);  // Try to load it.

	    } else if( fileSuffix.equalsIgnoreCase( "ctl")) {  // If this is a ctl file

		return loadCtlFile( ruleSetFile);  // Try to load it.
		
	    }  else {   // This is a unknown suffix.

		// Log, that we cannot identify the suffix of this file.
		// I don't throw an exception here, because it might stop the entire app, while other bots might still work.
		LogUtils.getInstance().getLogger().error( "Unknown file extension: " + fileSuffix + " in rule set loader");
	    }
	}

	// Log, that we cannot get the type of this file.
	// I don't throw an exception here, because it might stop the entire app, while other bots might still work.
	LogUtils.getInstance().getLogger().error( "Unknown file type of: " + ruleSetFile.getName() + " in rule set loader");

	return false;  // File loading failed.
    }
}