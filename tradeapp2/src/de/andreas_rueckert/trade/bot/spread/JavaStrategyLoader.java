/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.bot.spread;

import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.util.LogUtils;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;


/**
 * This class implements a strategy loader for java source code.
 */
class JavaStrategyLoader implements StrategyLoader {

    // Static variables


    // Instance variables

    /**
     * The hosting bot.
     */
    private SpreadBot _bot = null;

    /**
     * A class loader to load classes under the plugin root directory.
     */
    private URLClassLoader _classLoader = null;

    /**
     * The file with the java class for the strategy.
     */
    private File _javaFile = null;

    /**
     * The root directory of the plugins.
     */
    private File _pluginDirectory = null;

    /**
     * An instance of the loaded strategy.
     */
    private Strategy _strategy = null;

    /**
     * The name of the strategy class.
     */
    private String _strategyClass = null;

    /**
     * The user account for the trading.
     */
    private TradeSiteUserAccount _userAccount = null;


    // Constructors

    /**
     * Create a new java strategy loader instance.
     * (better pass the user account in the load method, so I can use different
     * accounts with the same loader?)
     *
     * @param bot The hosting bot.
     * @param userAccount The user account for trading.
     * @param pluginDirectory The root directory for the plugins.
     * @param javaFile The file with the java class for the strategy.
     */
    public JavaStrategyLoader( SpreadBot bot
			       , TradeSiteUserAccount userAccount
			       , File pluginDirectory
			       , String strategyClass) {

	_bot = bot;
	_userAccount = userAccount;
	_pluginDirectory = pluginDirectory;  // Store the root directory for the plugins.
	_strategyClass = strategyClass;  // Store the name of the strategy class.

	try {
	    // Create a class loader to load classes relative to the plugin root directory.
	    URLClassLoader _classLoader = URLClassLoader.newInstance( new URL[] { _pluginDirectory.toURI().toURL() });

	} catch( MalformedURLException mue) {
	    
	    LogUtils.getInstance().getLogger().error( "Plugindirectory " 
						      + _pluginDirectory  
						      + "cannot be converted to an URL: "
						      + mue);
	}
    }
    

    // Methods

    /**
     * Make sure, that all java sources are compiled and uptodate.
     *
     * @return true, if there are current class files for all the java files. False otherwise.
     */
    private final boolean allSourcesAreCompiled() {

	File [] javaFiles = getAllJavaFiles( _pluginDirectory, true);
	File [] classFiles = getAllClassFiles( _pluginDirectory, true);

	// Check, if there's a class file for each java file and it's timestamp is newer than the 
	// one of the java file.
	javafileLoop: for( File currentJavaFile : javaFiles) {

	    // Compute the name of the class file.
	    String javafileName = currentJavaFile.getName();
	    String currentClassfileName = javafileName.substring( 0, javafileName.length() - 4) + "class";

	    // Now check, if one of the class files matches this name.
	    for( File currentClassfile : classFiles) {

		if( currentClassfile.getName().equals( javafileName)) {  // If this is the class for the current source file,
		    
		    continue javafileLoop;  // check the next source file.
		}
	    }

	    // There's no class for this source file it seems...
	    return false;
	}

	return true; // All java files are compiled and the classes are uptodate
    }

    /**
     * Compile all the java files in the plugin directory.
     */
    private void compileAllJavaFiles() {

	// Get all java source files.
	File [] javaFiles = getAllJavaFiles( _pluginDirectory, true);

	// If there were java files found, compile them.
	if( javaFiles != null) {

	    System.out.println( "ToDo: JavaStrategyLoader.compileAllJavaFiles: set classpath and other compiler arguments.");

	    for( File currentFile : javaFiles) {

		compileJavaFile( currentFile);
	    }
	}
    }

    /**
     * Compile a java source code file.
     *
     * @param javaFile The file with the java sources.
     */
    private void compileJavaFile( File javaFile) {

	// Get the compiler.
	// This should only work from java version 1.6 on!
	JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();  

	// Compile the source code.
	// @see: http://docs.oracle.com/javase/6/docs/api/javax/tools/Tool.html#run%28java.io.InputStream,%20java.io.OutputStream,%20java.io.OutputStream,%20java.lang.String...%29
	compiler.run( null, null, null, javaFile.getPath());	
    }

    /**
     * Get all files with a given suffix.
     *
     * @param directory The directory to search for the files.
     * @param suffix The suffix as a String object (withouth the leading dot).
     * @param includeSubdirectores true, if also the sub directories should be searched.
     *
     * @return All the matching files as an array of file objects or null, if the directory couldn't be searched.
     */
    private File [] getAllFilesForSuffix( File directory, String suffix, boolean includeSubdirectories) {

	// Try the read all the files in the directory.
	File [] allFiles = directory.listFiles();

	if( allFiles != null) {

	    // Create a buffer for the resulting array.
	    ArrayList<File> resultBuffer = new ArrayList<File>();

	    // Avoid the concat in every loop iteration.
	    String stringToCompare = "." + suffix;

	    for( File currentFile : allFiles) {  // Loop over the files

		if( currentFile.isDirectory()) {

		    if( includeSubdirectories) {  // If the user wants the results from the subdirectories, too.

			File [] subDirResult = getAllFilesForSuffix( currentFile, suffix, includeSubdirectories);

			if( subDirResult != null) { // If there were any results

			    for( File currentFile2 : subDirResult) {  // Add those result to the result buffer, too.

				resultBuffer.add( currentFile2);
			    }
			}
		    }

		} else if( currentFile.getName().endsWith( stringToCompare)) {  // This is no sub directory.

		    resultBuffer.add( currentFile);
		}
	    }

	    // Convert the buffer to an array.
	    return resultBuffer.toArray( new File[ resultBuffer.size()]);
	}

	return null;  // No files found.
    }

    /**
     * Get all the class files in a directory.
     *
     * @param directory The directory to search for the java files.
     * @param includeSubdirectores true, if also the sub directories should be searched.
     *
     * @return The java files as an array of File objects.
     */
    private File [] getAllClassFiles( File directory, boolean includeSubdirectories) {
	
	return getAllFilesForSuffix( directory, "class", includeSubdirectories);
    }

    /**
     * Get all the java files in a directory.
     *
     * @param directory The directory to search for the java files.
     * @param includeSubdirectores true, if also the sub directories should be searched.
     *
     * @return The java files as an array of File objects.
     */
    private File [] getAllJavaFiles( File directory, boolean includeSubdirectories) {
	
	return getAllFilesForSuffix( directory, "java", includeSubdirectories);
    }

    /**
     * Get an instance of the strategy after it was successfully loaded.
     *
     * @return An instance of the loaded strategy.
     */
    public Strategy getStrategyInstance() {

	return _strategy;
    }

    /**
     * Try to load the strategy.
     *
     * @return true, if the strategy was successfully loaded. False otherwise.
     */
    public boolean load() {

	// Instantiate the actual strategy class and store the instance in the class var.
	_strategy = load( _strategyClass, _bot, _userAccount);

	return ( _strategy != null);  // Return true, if there's an actual instance of the class.
    }

    /**
     * Load a strategy from a classfile.
     *
     * @see http://stackoverflow.com/questions/2946338/how-do-i-programmatically-compile-and-instantiate-a-java-class
     * @see http://stackoverflow.com/questions/5658182/intializing-a-class-with-class-forname-and-which-have-a-constructor-which-take
     *
     * @param className The fully qualified class name (i.e. com.domainname.NewCoolStrategy ).
     * @param bot The hosting bot.
     * @param userAccount The user account to trade this strategy.
     *
     * @return The strategy or null if the loading failed.
     */
    private Strategy load( String className, SpreadBot bot, TradeSiteUserAccount userAccount) {

	if( ! allSourcesAreCompiled()) {  // Make sure, that the classes are newer than the sources.

	    compileAllJavaFiles();
	}

	try {

	    // Get a constructor with bot and user account as the parameters.
	    Constructor constructor = Class.forName( className).getConstructor( SpreadBot.class, TradeSiteUserAccount.class);
	    
	    // Now create the new strategy and return it.
	    return (Strategy)constructor.newInstance( bot, userAccount);	

	} catch( ClassNotFoundException cnfe) {
	    
	    LogUtils.getInstance().getLogger().error( "Strategy class " + className + " not found: " + cnfe);

	} catch( NoSuchMethodException nsme) {

	    LogUtils.getInstance().getLogger().error( "No matchin constructor in strategy class found: " + nsme);

	} catch( InstantiationException ie) {
	    
	    LogUtils.getInstance().getLogger().error( "Cannot instantiate strategy class: " + ie);

	} catch( IllegalAccessException iae) {

	    LogUtils.getInstance().getLogger().error( "Constructor in strategy class has no public access");

	} catch( InvocationTargetException ite) {

	    LogUtils.getInstance().getLogger().error( "The strategy constructors has thrown an exception: " + ite.getCause());
	
	}

	return null;  // Could not create strategy instance.
    }
}