/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2012 Andreas Rueckert
 */

package de.andreas_rueckert.persistence;

import java.util.List;


/**
 * This is an interface for all classes, that want their properties stored.
 */
public interface PersistentProperties {

    // Static variables


    // Methods

    /**
     * Get a list with the settings of the class.
     *
     * @return The persistent settings as a map.
     */
    public PersistentPropertyList getSettings();

    /**
     * Get the section name in the global property file (sort of an hack to avoid duplicated key names).
     */
    public String getPropertySectionName();

    /**
     * Set new settings for the class.
     *
     * @param settings The new settings for the class.
     */
    public void setSettings( PersistentPropertyList settings);
}