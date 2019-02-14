/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import java.util.Calendar;


/**
 * This interface defines a rule for the spread bot.
 */
interface Rule {

    // Variables


    // Methods

    /**
     * The rule body.
     */
    public void executeBody();

    /**
     * Get the hosting bot.
     *
     * @return The hosting bot.
     */
    public SpreadBot getBot();

    /**
     * Get the time of the last evaluation of this rule.
     *
     * @return The time of the last evaluation of this rule, or -1, if this rule was not yet evaluated.
     */
    public long getLastEvaluationTime();

    /**
     * Get the strategy this rule belongs to. Null, if it does not belong to any strategy.
     *
     * @return The strategy, this rule belong to. Null, if it does not belong to any strategy.
     */
    public Strategy getStrategy();

    /**
     * Get the user account, that is used by this rule.
     *
     * @return The user account for this rule.
     */
    public TradeSiteUserAccount getTradeSiteUserAccount();

    /**
     * The condition for the rule.
     * If it is true, the rule fires.
     *
     * @return true, if the condition is filled. False otherwise.
     */
    public boolean isConditionFilled();

    /**
     * Check, if the execution of the rule body is complete,
     * i.e. if the triggered order are filled now.
     *
     * @return true, if the execution of the rule body is complete now. False otherwise.
     */
    public boolean isExecutionComplete();

    /**
     * Check, if this rule runs in a strategy or bot, that is in simulation mode or is actually trading.
     *
     * @return true, if the bot or strategy is in simulation mode. False otherwise.
     */
    boolean isSimulation();

    /**
     * Wait for the execution of a rule body to complete.
     * This method will only return after the execution of this
     * rule body is complete.
     */
    public void waitForRuleCompletion();
}
