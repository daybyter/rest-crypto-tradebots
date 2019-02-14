/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;
import java.util.Calendar;
import java.util.List;


/**
 * This inteface defines a trading strategy.
 */
public interface Strategy {

    // Variables


    // Methods
    
    /**
     * Add a new rule to the strategy.
     *
     * @param rule The new rule to add.
     */
    void addRule( Rule rule);

    /**
     * Get the interval to evaluate this strategy.
     *
     * @return The minimum timespan between 2 evaluations.
     */
    public long getEvaluationInterval();

    /**
     * Get the time of the last evaluation of this strategy.
     *
     * @return The time of the last evaluation of this strategy, or null, if this strategy was not yet evaluated.
     */
    public long getLastEvaluationTime();

    /**
     * Get the name of this strategy.
     *
     * @return The name of this strategy.
     */
    String getName();

    /**
     * Get the current leverage of this strategy.
     *
     * @return The current leverage of this strategy.
     */
    double getLeverage();

    /**
     * Get the rules for this strategy.
     *
     * @return The rules to trade with this strategy.
     */
    List<Rule> getRules();

    /**
     * Get a list of the used currency pairs.
     *
     * @return A collection of the used currency pairs.
     */
    List<CurrencyPair> getUsedCurrencyPairs();

    /**
     * Get a list of the used exchanges.
     *
     * @return A collection of the used exchanges.
     */
    List<TradeSite> getUsedTradeSites();

    /**
     * Check, if this strategy is currently activated.
     *
     * @return true, if this strategy is currently activated. False otherwise.
     */
    boolean isActivated();
    
    /**
     * Check, if this strategy is in simulation mode or is actually trading.
     *
     * @return true, if the bot is in simulation mode. False otherwise.
     */
    boolean isSimulation();

    /**
     * Set this strategy activated or deactivated.
     *
     * @param activated Flag to activate or deactivate this strategy.
     */
    void setActivated( boolean activated);

    /**
     * Set a leverage for this strategy.
     *
     * @param leverage The new leverage for this strategy.
     */
    void setLeverage( double leverage);
}
