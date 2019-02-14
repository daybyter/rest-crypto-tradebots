/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.util;

import java.awt.Toolkit;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;


/**
 * Class to generate sound notifications.
 */
public class SoundUtils {

    // Inner classes

    /**
     * Class to hold the details of a sound.
     *
     * @see http://www.java-gaming.org/index.php/topic,24686.0 
     */
    class Sound {

	// Instance variables
	
	/**
	 * The actual audio clip.
	 */
	private Clip _clip;

	/**
	 * The filename of the audio file.
	 */
	private String _filename;


	// Constructors

	/**
	 * Create a new Sound instance for a given audio file.
	 *
	 * @param filename The name of the audio file.
	 */
	public Sound( String filename) {

	    // Store the filename in the sound object.
	    _filename = filename;

	    // Load the sound.
	    loadSound( filename);
	}


	// Methods

	/**
	 * Load a sound from a given filename.
	 *
	 * @param filename The name of the soundfile.
	 */
	private void loadSound( String filename) {

	    try {
		// Get an input stream for the audio file.
		// AudioInputStream ais = AudioSystem.getAudioInputStream( Sound.class.getResource( filename));

		// Create an audio clip object.
		// Clip clip = AudioSystem.getClip();

		// Get an input stream for the audio file.
		AudioInputStream ais = AudioSystem.getAudioInputStream( Sound.class.getResource( filename));

		// Create an audio clip object.
		Clip clip = (Clip)AudioSystem.getLine( new DataLine.Info( Clip.class, ais.getFormat()));
 
		// Open the audio clip.
		clip.open( ais);

		// Store the loaded audio clip in the instance.
		_clip = clip;

	    } catch( Exception e) {
		LogUtils.getInstance().getLogger().error( "Error while loading audio file " + filename + ": " + e);
	    }
	}

	/**
	 * Play this sound object.
	 */
	public void play() {

	    try {
		if( _clip != null) {  // If there is a loaded audio clip.

		    new Thread() {  // Create a thread to play it.

			public void run() {
			    synchronized( _clip) {

				_clip.stop();  // Stop the audio clip, if it is playing.
				_clip.setFramePosition(0);  // 'Rewind' the audio file.
				_clip.start();  // Start playing the audio file.
			    }
			}
		    }.start();  // Start the thread to play the sound.
		}
	    } catch( Exception e) {
		LogUtils.getInstance().getLogger().error( "Error while playing audio file " + _filename + ": " + e);
	    }
	}
    }


    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static SoundUtils _instance = null;


    // Instance variables

    /**
     * Beep sound.
     */
    private Sound _bell = new Sound( "sounds/boxing_bell.wav");


    // Constructors


    // Methods

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The ony instance of this class.
     */
    public static SoundUtils getInstance() {

	if( _instance == null) {           // If there is no instance yet...

	    _instance = new SoundUtils();  // ...create one.
	}

	return _instance;  // Return the only instance of this class.
    }

    /**
     * Play the terminal beep sound.
     */
    public void playBeep() {
	Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Play a bell sound.
     */
    public void playBell() {
	_bell.play();
    }
}