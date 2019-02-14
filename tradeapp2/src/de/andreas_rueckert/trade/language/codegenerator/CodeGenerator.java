/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.language.codegenerator;

import java.util.Map;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;


/**
 * This class manages the generating of the drools drl files.
 */
public class CodeGenerator {


    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static CodeGenerator _instance = null;


    // Instance variables

    /**
     * The context for the velocity template.
     */
    VelocityContext _context;


    // Constructors

    /**
     * Private construtor for singleton pattern.
     */
    private CodeGenerator() {

	// Set log4j as the logger.
	Velocity.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute" );

	Velocity.setProperty("runtime.log.logsystem.log4j.logger", "DrlCodeGenerator");

	// Fetch the main template for a rule set.
	Template ruleSetTemplate = Velocity.getTemplate( "ruleset.vm");
    }


    // Methods

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static CodeGenerator getInstance() {

	if( _instance == null) {              // If there is no instance yet,
  
	    _instance = new CodeGenerator();  // create one.
	}

	return _instance;  // Return the only instance of this class.
    }

    /**
     * Init the code generator for a new compilation output.
     */
    public void init() {

	//  Initialize the template engine.
	Velocity.init();

	// Create a new context for the output.
	_context = new VelocityContext();
    }

    /**
     * Set a String variable value for the output.
     *
     * @param varname The name of the variable.
     * @param value The value of the variable.
     */
    public void put( String varname, String value) {

	// Set a var in the context.
	_context.put( varname, value);
    }

    /**
     * Set a Map variable value for the output.
     *
     * @param varname The name of the variable.
     * @param value The Map value of the variable.
     */
    public void put( String varname, Map value) {

	// Set a var in the context.
	_context.put( varname, value);
    }
}