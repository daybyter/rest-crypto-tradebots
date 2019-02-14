/**
 * Java implementation of bitcoin trading.
 * This action maintains the required certificates to access trading
 * sites.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * @see http://www.xinotes.org/notes/note/1088/
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.action;

import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.AbstractAction;
import javax.swing.filechooser.FileFilter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.codec.binary.Base64;


/**
 * Show the certificates dialog.
 *
 * @see http://www.xinotes.org/notes/note/1088/
 * for the original sources to fetch a certificate.
 */
public class ActionCertificates extends AbstractAction {

    // Inner classes

    /**
     * A file filter, that accepts only directories.
     */
    class DirectoryFileFilter extends FileFilter {

	/**
	 * Check, if a given file is accepted in this file filter.
	 *
	 * @param file The file to check for acceptance.
	 *
	 * @return true, if the file is accepted.
	 */
	public boolean accept( File file) {

	    // Accept this file, if it is a directory.
	    return file.isDirectory();
	}

	/**
	 * Get a description of this file filter.
	 *
	 * @return A desciption of this file filter.
	 */
	public String getDescription() {

	    // Return a simple description of this class.
	    return "A FileFilter class to accept only directories";
	}
    }


    // Static variables

    /**
     * The only instance of this action (singleton pattern).
     */
    private static ActionCertificates _instance = null;


    // Instance variables

    /**
     * The panel for the certificate handling.
     */
    private JPanel _certificatesPanel = null;

    /**
     * A map of the text areas to display the certificates.
     */
    private Map< TradeSite, JTextArea> _displayAreas = new HashMap< TradeSite, JTextArea>();

    /**
     * A button to fetch all the certificates at once.
     */
    private JButton _fetchAllButton = null;

    /**
     * A map of the buttons to (re-)fetch the certificates.
     */
    private Map< JButton, TradeSite> _fetchButtons = new HashMap< JButton, TradeSite>();

    /**
     * A button to save all the certificates at once.
     */
    private JButton _saveAllButton = null;

    /**
     * A map of buttons to save a given certificate.
     */
    private Map< JButton, TradeSite> _saveButtons = new HashMap< JButton, TradeSite>();


    // Constructors
    
    /**
     * Private constructor for singleton pattern.
     */
    private ActionCertificates() {

	// Set a name for this action.
	putValue( NAME, "Certificates");
    }


    // Methods

    /**
     * The user wants to maintain the certificates.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {


	// Check, if the user wants to fetch all the certificates at once.
	if( e.getSource() == _fetchAllButton) {
	    fetchAllCertificates();
	}

	// Check, if the user wants to save all the buttons at once.
	if( e.getSource() == _saveAllButton) {
	    saveAllCertificates();
	}

	// Loop over the map of fetch buttons and trade sites to check, if the user
	// clicke on a fetch button.
	for( Map.Entry< JButton, TradeSite> buttonEntry : _fetchButtons.entrySet()) {

	    JButton currentButton = buttonEntry.getKey();         // Get the current button.
	    TradeSite currentTradeSite = buttonEntry.getValue();  // Get the current trade site.

	    if( e.getSource() == currentButton) {  // If the user clicked this fetch button.

		fetchCertificates( currentTradeSite);  // Fetch the certificate from this trade site.

		return;  // Don't check any further.
	    }
	}
	
	// Now loop over the map of save buttons to check, if the user clicked on
	// a save button
	for( Map.Entry< JButton, TradeSite> buttonEntry : _saveButtons.entrySet()) {
	    
	    JButton currentButton = buttonEntry.getKey();         // Get the current button.
	    TradeSite currentTradeSite = buttonEntry.getValue();  // Get the current trade site.

	    if( e.getSource() == currentButton) {  // If the user clicked this save button.

		saveCertificates( currentTradeSite);  // Save the certificate of this trade site.

		return;  // Don't check any further.
	    }
	}

	// Set the certificates panel in the app, if the user didn't click
	// any button.
	TradeApp.getApp().setContentPanel( getCertificatesPanel());
    }

    /**
     * This is a convenience method to fetch all the certificates at once.
     */
    private final void fetchAllCertificates() {

	// Just call the fetchCertificate method with no trade site.
	fetchCertificates( null);
    }

    /**
     * Fetch a SSL certificates from a given trade site.
     *
     * @param tradeSite The trade site to fetch the SSL certificate from, or null if all certificates should be fetched.
     */
    private final void fetchCertificates( TradeSite tradeSite) {

	// If there is no specific trade site given, fetch all the trade sites.
	if( tradeSite == null) {

	    // Loop over all the added trade sites...
	    for( TradeSite t : _fetchButtons.values()) {

		// System.out.println( "DEBUG: fetching certificate from: " + tradeSite.getName());

		// And fetch the certificate for each one.
		fetchCertificates( t);
	    }
	} else {

	    try {
		
		// create custom trust manager to ignore trust paths
		TrustManager trm = new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
			    return null;
			}
			
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}
			
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		    };
		
		SSLContext sc = SSLContext.getInstance("SSL");    // Create an SSL context.
		sc.init(null, new TrustManager[] { trm }, null);  // Init the context.
		SSLSocketFactory factory =sc.getSocketFactory();
		
		// Compute the domain of the trade site.
		String domain = tradeSite.getURL().trim();
		domain = domain.replaceAll( "^http://", "");   // Remove leading http prefix
		domain = domain.replaceAll( "^https://", "");
		domain = domain.replaceAll( "/$", "");  // Remove trailing slash.
		    
		// System.out.println( "DEBUG: domain is: " + domain);
		
		// Create a socket and just use the default HTTPS port for now.	   
		SSLSocket socket =(SSLSocket)factory.createSocket( domain, 443);

		socket.startHandshake();
		SSLSession session = socket.getSession();

		// Get all the certificates from the server.
		java.security.cert.Certificate[] servercerts = session.getPeerCertificates();
	    
		// Get the display area.
		JTextArea displayArea = _displayAreas.get( tradeSite);
		    
		if( displayArea != null) {  // If there is a JTextArea to display the certifictate...
			
		    // Create a string buffer for the displayed certificates.
		    StringBuffer displayBuffer = new StringBuffer();
		    
		    // Loop over the certificates
		    for (int i = 0; i < servercerts.length; i++) {
			    
			// And add them all to the buffer.
			displayBuffer.append( "-----BEGIN CERTIFICATE-----\n");
			// displayBuffer.append( new sun.misc.BASE64Encoder().encode( servercerts[i].getEncoded()));
			displayBuffer.append( Base64.encodeBase64String( servercerts[i].getEncoded()));  // Don't use sun.misc stuff anymore...
			displayBuffer.append( "\n-----END CERTIFICATE-----\n\n");
		    }
			
		    // System.out.println( "DEBUG: certificate is: " + displayBuffer.toString());
		    
		    // Now display the buffer content.
		    displayArea.setText( displayBuffer.toString());

		} else {

		    // The certificates cannot be displayed.
		    LogUtils.getInstance().getLogger().error( "ActionCertificates: cannot find display area for trade site: " + tradeSite.getName()); 
		}
		
		socket.close();  // Close socket to the server.

	    } catch( NoSuchAlgorithmException nsae) {
		    
		// This should never happen...
		LogUtils.getInstance().getLogger().error( "ActionCertificates: unknown algorithm SSL: " +nsae );

	    } catch( KeyManagementException kmee) {
		    
		// A problem occurred when the context was initialized.
		LogUtils.getInstance().getLogger().error( "ActionCertificates: cannot initialize context with own trust manager: " + kmee);
		    
	    } catch( SSLPeerUnverifiedException spue) {
		
		// A problem occurred while fetching the certificates.
		LogUtils.getInstance().getLogger().error( "ActionCertificates: cannot fetch certificates: " + spue);

	    } catch( CertificateEncodingException cee) {
		
		// A problem occurred while getting the certificate encoding.
		LogUtils.getInstance().getLogger().error( "ActionCertificates: problem with certificate encoding: " + cee);
		
	    } catch( IOException ioe) {
		    
		// Some IO error while reading the certificate from the server.
		LogUtils.getInstance().getLogger().error( "ActionCertificates: IO error while fetching the certificate from the server: " + ioe);
	    }
	}
    }

    /**
     * Get the panel for this action.
     *
     * @return The panel for the certificate handling.
     */
    private JPanel getCertificatesPanel() {

	if( _certificatesPanel == null) {       // If there is no certificates panel yet,
	    _certificatesPanel = new JPanel();  // create one.

	    _certificatesPanel.setLayout( new BorderLayout());  // Use a border layout for the entire dialog.

	    // Create a panel to display the actual certificates.
	    JPanel certificateListPanel = new JPanel();

	    // Show all the trade sites with their certificates in this panel.
	    certificateListPanel.setLayout( new GridBagLayout());

	    // Create constraints (to be reused for each element).
	    GridBagConstraints constraints = new GridBagConstraints();

	    // Start with row 0.
	    constraints.gridy = 0;

	    // Use some pixels of padding.
	    constraints.ipadx = constraints.ipady = 4;

	    // Loop over the trade sites
	    for( TradeSite t : ModuleLoader.getInstance().getRegisteredTradeSites().values()) {
		
		constraints.gridx = 0;  // Start with column 0.
		constraints.weightx = 0.2;  // The label is rather small.
		//constraints.gridwidth = 1;  // The label is rather small.

		// Add a label with the name.
		certificateListPanel.add( new JLabel( t.getName()), constraints);

		// Add a textarea to display the certificate
		JTextArea displayArea = new JTextArea( 7, 40);
		displayArea.setEditable( false);  // Make the area read-only.

		// Add the are to the map of areas.
		_displayAreas.put( t, displayArea);

		constraints.gridx++;  // Use the next columns.
		constraints.weightx = 0.6;  // The area is rather wide.

		// Add the text area to the panel.
		certificateListPanel.add( new JScrollPane( displayArea), constraints);
		
		// Add a panel with buttons to operate on the certificate
		JPanel localButtonPanel = new JPanel();

		// Create a layout for the buttons.
		GridLayout buttonLayout = new GridLayout( 2, 1);
		buttonLayout.setHgap( 8);
		buttonLayout.setVgap( 8);
		localButtonPanel.setLayout( buttonLayout);
		
		// Add a button the fetch the certificate.
		JButton fetchButton = new JButton( "Fetch certificates");
		fetchButton.addActionListener( this);

		localButtonPanel.add( fetchButton);  // Add the button to the button panel.

		// Add the button to the map of fetch buttons.
		_fetchButtons.put( fetchButton, t);

		// Add a button to store this certificate.
		JButton saveButton = new JButton( "Save certificates");
		saveButton.addActionListener( this);

		localButtonPanel.add( saveButton);  // Add the button to the button panel.

		// Add the button to the map of save buttons.
		_saveButtons.put( saveButton, t);

		// Compute constraints for the button panel.
		constraints.gridx++;  // Use the next column.
		constraints.weightx = 0.2;  // The buttons are rather small again.

		// Add the local button panel to the list panel.
		certificateListPanel.add( localButtonPanel, constraints);

		constraints.gridy++;  // Increase the y position.
	    }
	    
	    // Add the list of certificates to this panel.
	    _certificatesPanel.add( certificateListPanel, BorderLayout.CENTER);

	    // Now create a main button panel with functions for all certificates.
	    JPanel mainButtonPanel = new JPanel();

	    // Create a button to fetch the certificates from all the trade sites.
	    mainButtonPanel.add( _fetchAllButton = new JButton( "Fetch all certificates"));
	    _fetchAllButton.addActionListener( this);

	    // Create a button to save all certificates from all sites.
	    mainButtonPanel.add( _saveAllButton = new JButton( "Save all certificates"));
	    _saveAllButton.addActionListener( this);

	    // Add the main button panel to the dialog.
	    _certificatesPanel.add( mainButtonPanel, BorderLayout.SOUTH);
	}

	return _certificatesPanel;  // Return the panel for certificate handling.
    }

    /**
     * Get the only instance of this action.
     *
     * @return The only instance of this action.
     */
    public static ActionCertificates getInstance() {

        if( _instance == null) {                   // If there is no instance yet,
            _instance = new ActionCertificates();  // create a new one.
        }

        return _instance;  // Return the only instance of this instance.
    }

    /**
     * Save the certificates from all the trade sites to files.
     */
    private final void saveAllCertificates() {

	// Just reuse the save method with a null argument.
	saveCertificates( null);
    }

    /**
     * Save the certificates from a trade site to files.
     *
     * @param tradeSite The trade site to process or null, if all certificates should be saved.
     */
    private final void saveCertificates( TradeSite tradeSite) {

	// Create a file chooser to let the user select a directory.
	final JFileChooser fileChooser = new JFileChooser();

	// Let the user select a directory.
	fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);

	// Set a filter for directories only.
	fileChooser.setFileFilter( new DirectoryFileFilter());
	
	// Get the response from the chooser.
	int returnVal = fileChooser.showOpenDialog( null);

	if( returnVal == JFileChooser.APPROVE_OPTION) {  // If the user clicked 'ok' in the dialog.

            File outputDirectory = fileChooser.getSelectedFile();  // Get the directory from the dialog.

	    // Write the certificates to the selected directory.
	    writeCertificates( outputDirectory, tradeSite);
	}
    }

    /**
     * Write certificates to a given directory.
     *
     * @param outputDirectory The directory to write the certificates to.
     * @param tradeSite The tradeSite of the certificates or null, if all certificates should be written.
     */
    private final void writeCertificates( File outputDirectory, TradeSite tradeSite) {

	// If no tradeSite is given...
	if( tradeSite == null) {

	    // Loop over all the trade sites.
	    for( TradeSite t : _displayAreas.keySet()) {

		// And write each of them
		writeCertificates( outputDirectory, t);
	    }
	} else {  // A trade site was given
	    
	    // Get the string representation of all the certificates.
	    JTextArea displayArea = _displayAreas.get( tradeSite);

	    if( displayArea == null) {
		
		// This should never happen
		LogUtils.getInstance().getLogger().error( "Cannot find JTextArea for trade site: " + tradeSite.getName());

	    } else {

		// Get the text from the display area and trim it.
		String certificatesText = displayArea.getText().trim();
		
		// Split the text into the single certificates and write each one.
		String [] certificates = certificatesText.split( "\n\n");

		// Loop over the split certificates and write each one.
		for( int currentCertificateIndex = 0; currentCertificateIndex < certificates.length; ++currentCertificateIndex) {

		    try {

			// Create a certificate file name.
			File certificateFile = new File( outputDirectory, tradeSite.getName() + (currentCertificateIndex) + ".cer");
			
			// ToDo: check, if file already exists and if we should overwrite it?
			
			// Create a new print writer to write the text.
			PrintWriter printWriter = new PrintWriter( certificateFile);
			
			// Print the certificate to the file.
			printWriter.print( certificates[ currentCertificateIndex]);

			// And close the output file.
			printWriter.close();

		    } catch( FileNotFoundException fnfe) {

			LogUtils.getInstance().getLogger().error( "PrintWriter cannot write into certificate file: " + fnfe);
		    }
		}
	    }
	}
    }
}
