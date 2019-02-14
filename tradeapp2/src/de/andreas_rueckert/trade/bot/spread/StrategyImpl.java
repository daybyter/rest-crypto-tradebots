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
import de.andreas_rueckert.util.TimeUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * This class implements a strategy.
 */
public class StrategyImpl implements Strategy {

    // Static variables


    // Instance variables

    /**
     * Flag to indicate, if this strategy is currently activated.
     */
    private boolean _activated = true;

    /**
     * The minimum interval between 2 evaluations of this strategy.
     */
    private long _evaluationInterval = -1;
    
    /**
     * The current leverage.
     */
    private double _leverage = 1;

    /**
     * The name of the strategy.
     */
    private String _name;

    /**
     * The rules implementing this strategy.
     */
    private List<Rule> _rules = new ArrayList<Rule>();

    /**
     * Flag to activate simulation mode for this strategy.
     */
    private boolean _simulationMode = false;

    /**
     * A list of used currency pairs for trading this strategy.
     */
    private List<CurrencyPair> _usedCurrencyPairs = new ArrayList<CurrencyPair>();

    /**
     * A list of used trade site for trading this strategy.
     */
    private List<TradeSite> _usedTradeSites = new ArrayList<TradeSite>();


    // Constructors

    /**
     * Create a new trading strategy.
     *
     * @param name The name of the strategy.
     * @param evaluationInterval The interval in which the rules are evaluated.
     */
    public StrategyImpl( String name, String evaluationInterval) {

	_name = name;
	_evaluationInterval = TimeUtils.microsFromString( evaluationInterval);
    }

    /**
     * Create a new trading strategy.
     *
     * @param name The name of the strategy.
     * @param evaluationInterval The interval in which the rules are evaluated.
     * @param leverage The default leverage.
     */
    public StrategyImpl( String name, String evaluationInterval, double leverage) {

	this( name, evaluationInterval);

	setLeverage( leverage);  // Init the default leverage.
    }


    // Methods

    /**
     * Add a currency pair to the list of used currency pairs.
     *
     * @param currencyPair The currency pair to add.
     */
    public void addUsedCurrencyPair( CurrencyPair currencyPair) {

	_usedCurrencyPairs.add( currencyPair);
    }

    /**
     * Add a new rule to the strategy.
     *
     * @param rule The new rule to add.
     */
    public void addRule( Rule rule) {

	_rules.add( rule);
    }

    /**
     * Add a trade site to the list of used trade sites.
     *
     * @param tradeSite The trade site to add to the list of used trade sites.
     */
    public void addUsedTradeSite( TradeSite tradeSite) {

	_usedTradeSites.add( tradeSite);
    }

    /**
     * Get the interval to evaluate this strategy.
     *
     * @return The minimum timespan between 2 evaluations, or -1 if no such interval is defined.
     */
    public long getEvaluationInterval() {

	return _evaluationInterval;
    }

    /**
     * Get the time of the last evaluation of this strategy.
     *
     * @return The time of the last evaluation of this strategy, or -1, if this strategy was not yet evaluated.
     */
    public long getLastEvaluationTime() {

	long result = -1;

	// Loop over the rules to find the most recent time.
	for( Rule currentRule : _rules) {

	    long currentEvaluationTime = currentRule.getLastEvaluationTime();

	    if( currentEvaluationTime != -1) {  // If this rule was already evaluated.

		if( result == -1) {

		    result = currentEvaluationTime;

		} else if( result < currentEvaluationTime) {

		    result = currentEvaluationTime;
		}
	    }		
	}

	return result;  // Return the most recent evaluation time.
    }

    /**
     * Get the current leverage of this strategy.
     *
     * @return The current leverage of this strategy.
     */
    public double getLeverage() {

	return _leverage;
    }
 
    /**
     * Get the name of this strategy.
     *
     * @return The name of this strategy.
     */
    public String getName() {
	
	return _name;
    }

    /**
     * Get the rules for this strategy.
     *
     * @return The rules to trade with this strategy.
     */
    public List<Rule> getRules() {

	return _rules;
    }

    /**
     * Get a list of the used currency pairs.
     *
     * @return A collection of the used currency pairs.
     */
    public List<CurrencyPair> getUsedCurrencyPairs() {

	return _usedCurrencyPairs;
    }

    /**
     * Get a list of the used exchanges.
     *
     * @return A collection of the used exchanges.
     */
    public List<TradeSite> getUsedTradeSites() {

	return _usedTradeSites;
    }

    /**
     * Check, if this strategy is currently activated.
     *
     * @return true, if this strategy is currently activated. False otherwise.
     */
    public final boolean isActivated() {

	return _activated;
    }

    /**
     * Check, if this strategy is in simulation mode or is actually trading.
     *
     * @return true, if the bot is in simulation mode. False otherwise.
     */
    public boolean isSimulation() {
	
	return _simulationMode;
    }

    /**
     * Set this strategy activated or deactivated.
     *
     * @param activated Flag to activate or deactivate this strategy.
     */
    public final void setActivated( boolean activated) {

	_activated = activated;
    }

    /**
     * Set a leverage for this strategy.
     *
     * @param leverage The new leverage for this strategy.
     */
    public void setLeverage( double leverage) {

	_leverage = leverage;
    }

    /**
     * Set the simulation mode of the strategy.
     *
     * @param simulationMode Flag to indicate the simulation mode of the strategy.
     */
    public void setSimulation( boolean simulationMode) {

	_simulationMode = simulationMode;
    }
}
