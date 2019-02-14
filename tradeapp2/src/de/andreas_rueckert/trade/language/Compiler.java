/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.language;

import de.andreas_rueckert.trade.language.codegenerator.CodeGenerator;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * This class uses a compiler and a code generator to compile trade rules.
 */
public class Compiler {

    // Static variables

    /**
     * The only instance of this class (Singleton pattern).
     */
    private static Compiler _instance = null;


    // Instance variables
   
    /**
     * The code generator for the compiler output.
     */
    CodeGenerator _codeGenerator = CodeGenerator.getInstance();

    /**
     * Store the global variables in the drools rule set as a hashmap.
     */
    Map<String, String> _globalVariables = new HashMap< String, String>();
    

    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private Compiler() {
    }


    // Methods

    /**
     * Add a global variable the rule set output.
     *
     * @param fullPath The complete name of the variable including the package.
     * @param alias The alias of the variable.
     */
    private void addGlobalVar( String fullPath, String alias) {

	// Store the var in the hashmap for the global vars.
	_globalVariables.put( alias, fullPath);
    }

    /**
     * Compile rule sets from an input to an output file.
     *
     * @param input The input file.
     * @param output The output file.
     */
    public boolean compile( File input, File output) {

	_codeGenerator.init();  // Init the code generator for the next compilation.

	// Add the global variables to the output.
	_codeGenerator.put( "allGlobalVars", _globalVariables);

	return false;  // Default is an error.
    }

    /**
     * Get the only instance of this compiler.
     *
     * @return The only instance of this compiler.
     */
    public static Compiler getInstance() {

	if( _instance == null) {  // If there is no class instance yet,

	    _instance = new Compiler();  // create one.
	}

	return _instance;  // Return the only instance of this class.
    }

    /**
     * Define the default variables for a trade rule set.
     */
    private void initGlobalVariables() {

	// Set the variables, so rules can access the framework functionality.

	// ChartProviver to access prices etc.
	addGlobalVar( "de.andreas_rueckert.chart.ChartProvider", "ChartProvider");

	// OrderBook to place orders.
       	addGlobalVar( "global de.andreas_rueckert.trade.order.OrderBook", "OrderBook");
    }
}