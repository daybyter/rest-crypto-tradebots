/**
 * Java code for ema crossing trading strategy.
 *
 * 
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013,2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread.strategies.java;

import de.andreas_rueckert.trade.bot.spread.CancelOrderRule;
import de.andreas_rueckert.trade.bot.spread.EmergencyStopRule;
import de.andreas_rueckert.trade.bot.spread.MACrossingTradeRule;
import de.andreas_rueckert.trade.bot.spread.SpreadBot;
import de.andreas_rueckert.trade.bot.spread.Strategy;
import de.andreas_rueckert.trade.bot.spread.StrategyImpl;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.currency.CurrencyProvider;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.util.ModuleLoader;


/**
 * This class implements a very simple EMA crossing strategy.
 */
public class EmaXStrategy extends StrategyImpl implements Strategy {

    // Static variables


    // Instance variables
    
    /**
     * The hosting bot. Better move this into the StrategyImpl class later?
     */
    private SpreadBot _bot = null;

    /**
     * The user account for trading this strategy. Move this into the StrategyImpl class later?
     */
    private TradeSiteUserAccount _userAccount = null;


    // Constructors

    /**
     * This is the main method, where the whole strategy is actually declared.
     *
     * @param bot The hosting bot of this strategy.
     * @param userAccount The user account to trade this strategy.
     */
    public EmaXStrategy( SpreadBot bot, TradeSiteUserAccount userAccount) {

	// Set a name and an interval for the strategy in the base class.
	super( "EMA crossing BTC<=>USD at btc-e", "30m");

	// Get a reference to the btc-e API implementation.
	TradeSite btceSite = ModuleLoader.getInstance().getRegisteredTradeSite( "BTCe");
	
	// Create a currency pair to trade
	CurrencyPair btcUsdPair = new CurrencyPairImpl( "BTC", "USD");

	// Add a list of rules to this strategy
	addRule( new EmergencyStopRule( _bot
					, this
					, btceSite
					, btcUsdPair
					, userAccount
					, new Price( "400.0"
						     , CurrencyProvider.getInstance().getCurrencyForCode( "USD"))));
	addRule( new CancelOrderRule( _bot, this, userAccount));
	addRule( new MACrossingTradeRule( _bot, this, btceSite, btcUsdPair, userAccount, "10d", "25d"));

	// Add some info about this strategy.
	addUsedCurrencyPair( btcUsdPair);
    }


    // Methods

}
