/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.bot.LogLevel;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.util.TimeUtils;
import java.util.Calendar;


/**
 * This is a base class for implemented rules.
 */
class RuleImpl implements Rule {

    // Static variables

    
    // Instance variables

    /**
     * The hosting bot.
     */
    protected SpreadBot _bot;

    /**
     * The time, when this rule was evaluated for the last time.
     * This could be used to check, if a bot is still running correctly.
     */
    protected long _lastEvaluationTime = -1;

    /**
     * The status of the rule.
     */
    protected RuleStatus _status;

    /**
     * The strategy, this rule belongs to. Null, if there is no such strategy.
     */
    protected Strategy _strategy = null;

    /**
     * The user account to use for the deleting.
     * Only rules with this account are deleted!
     */
    protected TradeSiteUserAccount _userAccount = null;
    

    // Constructors

    /**
     * Create a new rule implementation.
     *
     * @param bot The hosting bot.
     * @param strategy The strategy, this rule belongs to. Null, if there is no such strategy.
     * @param userAccount The trade site user account to use.
     */
    protected RuleImpl( SpreadBot bot, Strategy strategy, TradeSiteUserAccount userAccount) {

	_bot = bot;  // Store a referenc to the bot in this instance.
	_strategy = strategy;  // Same for strategy.
	_userAccount = userAccount;  // Same for user account.
    }


    // Methods

    /**
     * Buy some amount of a currency at a given trade site.
     *
     * @param tradeSite The trade site to buy at.
     * @param currency The currency to buy.
     * @param amount The amount to buy.
     *
     * @return true, if the order went through.
     */
    private boolean buy( TradeSite tradeSite, Currency currency, Amount amount) {

	// Just use the hosting bot to do the trade.
	return _bot.buy( tradeSite, currency, amount);
    }

    /**
     * The rule body.
     */
    public void executeBody() {

	// The base class has no body...
    }

    /**
     * Get the hosting bot.
     *
     * @return The hosting bot.
     */
    public SpreadBot getBot() {

	return _bot;  // Return the bot instance.
    }

    /**
     * Get the time of the last evaluation of this rule.
     *
     * @return The time of the last evaluation of this rule, or -1, if this rule was not yet evaluated.
     */
    public long getLastEvaluationTime() {

	return _lastEvaluationTime;
    }

    /**
     * Get the strategy this rule belongs to. Null, if it does not belong to any strategy.
     *
     * @return The strategy, this rule belong to. Null, if it does not belong to any strategy.
     */
    public Strategy getStrategy() {

	return _strategy;
    }

    /**
     * Get the user account, that is used by this rule.
     *
     * @return The user account for this rule.
     */
    public TradeSiteUserAccount getTradeSiteUserAccount() {
	
	return _userAccount;
    }

    /**
     * The condition for the rule.
     * If it is true, the rule fires.
     *
     * @return true, if the condition is filled. False otherwise.
     */
    public boolean isConditionFilled() {
	
	return false;  // Just a dummy default implementation.
    }

    /**
     * Check, if the execution of the rule body is complete,
     * i.e. if the triggered order are filled now.
     *
     * @return true, if the execution of the rule body is complete now. False otherwise.
     */
    public boolean isExecutionComplete() {
	
	return ( _status == RuleStatus.COMPLETED);  // Check, if the current status says, that the rule is completed.
    }

    /**
     * Check, if this rule runs in a strategy or bot, that is in simulation mode or is actually trading.
     *
     * @return true, if the bot or strategy is in simulation mode. False otherwise.
     */
    public boolean isSimulation() {

	return getBot().isSimulation() || ( ( getStrategy() != null) && getStrategy().isSimulation());
    }

    /**
     * Log some info the bots trade logger.
     *
     * @param level The log level to use for this message.
     * @param message The message to log.
     */
    protected void logEvent( LogLevel level, String message) {

	if( level.intValue() <= getBot().getLogLevel().intValue()) {  // If the message log level is <= than the current bot log level,
	                                                              // log this message.
	    StringBuffer logMessage = new StringBuffer();

	    if( getStrategy() != null) {  // Append the current strategy, if it is know.
		logMessage.append( "Strategy: '" + getStrategy().getName() + "' : ");
	    }

	    logMessage.append( message);  // Append the actual message.
	    
	    getBot().getTradeLogger().log( logMessage.toString());  // Write the message to the logger.

	}
    }

    /**
     * Sell some amount of a currency at a given trade site.
     *
     * @param tradeSite The trade site to sell at.
     * @param currency The currency to sell.
     * @param amount The amount to sell.
     *
     * @return true, if the order went through.
     */
    private boolean sell( TradeSite tradeSite, Currency currency, Amount amount) {

	// Just use the hosting bot to do the trade.
	return _bot.sell( tradeSite, currency, amount);
    }

    /**
     * Set the time, when the rule was evaluated.
     */
    protected void setLastEvaluationTime() {

	_lastEvaluationTime = TimeUtils.getInstance().getCurrentGMTTimeMicros();
    }

    /**
     * Wait for the execution of a rule body to complete.
     * This method will only return after the execution of this
     * rule body is complete.
     */
    public void waitForRuleCompletion() {

	while( _status != RuleStatus.COMPLETED) {  // While the rule with all it's side effects is not run through...

	    try {
		Thread.sleep( 1000);  // Wait a second...

	    } catch( InterruptedException ie) {

		System.err.println( "Waiting for rule completion interrupted: " + ie.toString());
	    }
	}
    }
}
