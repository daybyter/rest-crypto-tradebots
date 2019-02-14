/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

/**
 * The status of a rule
 */
public enum RuleStatus { 
    WAITING                 // Rule is waiting to be triggered.
    , TRIGGERED             // The rule was triggered, but is not yet completed.
    , COMPLETED             // The rule is completed (made orders are filled etc).
}
