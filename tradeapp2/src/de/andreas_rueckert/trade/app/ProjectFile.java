/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app;

import java.io.File;


/**
 * Base class for all kinds of project files.
 */
public class ProjectFile {

    // Static variables
    

    // Instance variables

    /**
     * The actual project file.
     */
    protected File _file = null;

    /**
     * The name of the project file.
     */
    protected String _name = null;


    // Constructors

    /**
     * Create a new ProjectFile instance from a given file.
     *
     * @param file The actual file.
     */
    public ProjectFile( File file) {
	_file = file;

	_name = file.getName();  // Set the name to the file name.
    }


    // Methods

    /**
     * Get the physical file of this project file.
     *
     * @return The physical file of this project file.
     */
    public File getFile() {
	return _file;
    }

    /**
     * Get the name of this project file.
     *
     * @return The name of this project file.
     */
    public String getName() {
	return _name;
    }

    /**
     * Convert this project file to a string. Mainly used for the project treeview at the moment.
     *
     * @return A string for this project file object.
     */
    public String toString() {
	return getName();
    }
}