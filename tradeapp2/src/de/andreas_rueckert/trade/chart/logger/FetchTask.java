/**
 * Java implementation of a chart provider.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.chart.logger;


/**
 * This class holds the info on a fetch task of the data logger.
 */
class FetchTask {

    // Inner classes

    
    // Static variables


    // Instance variables

    /**
     * The type of data to request from the trade site.
     */
    private FetchDataType _dataType;

    /**
     * This flag indicates, that this event should be rescheduled after execution.
     */
    private boolean _intervalEvent = false;

    /**
     * The time, when this event should be evaluated.
     */
    private long _scheduledTime;


    // Constructors


    // Methods

    /**
     * Get the scheduled time of this task.
     *
     * @return The scheduled time of this task.
     */
    public final long getScheduledTime() {

	return _scheduledTime;
    }
    
    /**
     * Set a new schedule time.
     *
     * @param scheduledTime The new time, that this event should be evaluated at.
     */
    public final void setScheduledTime( long scheduledTime) {
	
	_scheduledTime = scheduledTime;  // Store the new target time in the instance.
    }
}