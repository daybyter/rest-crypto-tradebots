/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2013 Andreas Rueckert
 */

package de.andreas_rueckert.trade.app.action;

import de.andreas_rueckert.persistence.PersistentProperties;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.app.remote.SSLSocketServer;
import de.andreas_rueckert.trade.app.TradeApp;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.ModuleLoader;
import de.andreas_rueckert.util.PasswordUtils;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;


/**
 * Show the settings dialog.
 */
public class ActionSettings extends AbstractAction {

    // Static variables

    /**
     * The only instance of this action ( singleton pattern).
     */
    private static ActionSettings _instance = null;


    // Instance variables

    /**
     * The save button for the settings.
     */
    private JButton _saveButton = null;

    /**
     * The panel with the settings.
     */
    private JPanel _settingsPanel = null;

    /**
     * The mapping from the swing components to the object keys.
     */
    private Map<PersistentProperties,Map<String,JComponent>> _inputFieldMapping = null;


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private ActionSettings() {

	// Set a name for this action.
	putValue( NAME, "Settings");

	// Create a new mapping from the input fields to the object keys.
	_inputFieldMapping = new HashMap<PersistentProperties,Map<String, JComponent>>();
    }


    // Methods

    /**
     * The user wants to edit the settings.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {

	if( e.getSource() == _saveButton) {  // The user wants to save the settings.
	    saveSettings();
	} else {
	    TradeApp.getApp().setContentPanel( getSettingsPanel());
	}
    }

    /**
     * Get the only instance of this action.
     *
     * @return The only instance of this action.
     */
    public static ActionSettings getInstance() {

        if( _instance == null) {               // If there is no instance yet,
            _instance = new ActionSettings();  // create a new one.
        }

        return _instance;  // Return the only instance of this action.
    }


    /**
     * Get the settings panel.
     *
     * @return The settings panel.
     */
    private JPanel getSettingsPanel() {

	if( _settingsPanel == null) {       // If there is no settings panel yet
	    _settingsPanel = new JPanel();  // create one.

	    _settingsPanel.setLayout( new BorderLayout());  // Use the border layout for the settings.

	    JTabbedPane tabbedPane = new JTabbedPane();

	    JPanel mailSettings = new JPanel();

	    HashMap currentTabMapping = new HashMap<String, JTextField>();

	    // The following code might better iterate over the persistent properties in TradeAppProperties?

	    List<PersistentProperty> mailSettingsList = TradeApp.getApp().getEmailNotifier().getSettings();

	    // Sort the list according to their priorities.
	    Collections.sort( mailSettingsList);
	    Collections.reverse( mailSettingsList);

	    mailSettings.setLayout( new GridLayout( mailSettingsList.size(), 2));

	    for( PersistentProperty setting : mailSettingsList) {
		    
		mailSettings.add( new JLabel( ( setting.getTitle() != null ? setting.getTitle() : setting.getName()) + ":"));
		
		JTextField currentField = new JTextField( setting.getValue() != null ? setting.getValue().toString() : "", 30);
		
		mailSettings.add( currentField);

		currentTabMapping.put( setting.getName(), currentField);  // Add the textfield to the key <=> textfield mapping.
	    }

	    tabbedPane.add( "Mail", mailSettings);

	    _inputFieldMapping.put( TradeApp.getApp().getEmailNotifier(), currentTabMapping);  // Add the mapping of this tab to the global mapping.

	    // Add a tab for the SSL shell.
	    JPanel sslServerSettings = new JPanel();
	    
	    PersistentPropertyList sslServerSettingsList = SSLSocketServer.getInstance().getSettings();

	    // Sort the list according to their priorities. 
	    Collections.sort( sslServerSettingsList);
	    Collections.reverse( sslServerSettingsList);

	    sslServerSettings.setLayout( new GridLayout( sslServerSettingsList.size(), 2));

	    for( PersistentProperty setting : sslServerSettingsList) {
		    
		sslServerSettings.add( new JLabel( ( setting.getTitle() != null ? setting.getTitle() : setting.getName()) + ":"));
		
		JTextField currentField = new JTextField( setting.getValue() != null ? setting.getValue().toString() : "", 30);
		
		sslServerSettings.add( currentField);

		currentTabMapping.put( setting.getName(), currentField);  // Add the textfield to the key <=> textfield mapping.
	    }

	    tabbedPane.add( "Remote access", sslServerSettings);

	    _inputFieldMapping.put( SSLSocketServer.getInstance(), currentTabMapping);  // Add the mapping of this tab to the global mapping.


	    // Create a panel for the JDBC connection.
	    //JPanel jdbcSettings = new JPanel();	    

	    //PersistentPropertyList jdbcSettingsList = ChartProvider.getInstance().getCachePersistence().getSettings();

	    //currentTabMapping = new HashMap<String, JComponent>();

	    // Sort the list according to their priorities. 
	    //Collections.sort( jdbcSettingsList);
	    //Collections.reverse( jdbcSettingsList);
	    // End JDBC panel.

	    for( TradeSite t : ModuleLoader.getInstance().getRegisteredTradeSites().values()) {
	    
		PersistentPropertyList settingsList = t.getSettings();

		JPanel currentSettings = new JPanel();

		currentTabMapping = new HashMap<String, JTextField>();

		currentSettings.setLayout( new GridLayout( settingsList.size(), 2));

		// Sort the list according to their priorities. 
		Collections.sort( settingsList);
		Collections.reverse( settingsList);

		for( PersistentProperty setting : settingsList) {
		    
		    currentSettings.add( new JLabel( ( setting.getTitle() != null ? setting.getTitle() : setting.getName()) + ":"));

		    JTextField currentField = new JTextField( setting.getValue() != null ? setting.getValue().toString() : "", 30);

		    currentSettings.add( currentField);

		    // Put the field to the mapping of this tab.
		    currentTabMapping.put( setting.getName(), currentField);
		}

		tabbedPane.add( t.getName(), currentSettings);

		_inputFieldMapping.put( t, currentTabMapping);  // Add the tab mapping to the global mapping.
	    }

	    _settingsPanel.add( tabbedPane, BorderLayout.CENTER);

	    _saveButton = new JButton( "Save");

	    _saveButton.addActionListener( this);

	    _settingsPanel.add( _saveButton, BorderLayout.SOUTH);
	}

	return _settingsPanel;
    }

    /**
     * Save the settings panel settings to the objects.
     */
    private void saveSettings() {

	// Loop over the property objects.
	for( Map.Entry<PersistentProperties,Map<String,JComponent>> propertyObject : _inputFieldMapping.entrySet()) {
	    PersistentProperties pObject = propertyObject.getKey();

	    // Get a list with properties from the object and modify them according to the GUI entries.
	    // We cannot(!) just create new PersistentProperty objects, because we would lose some
	    // fields, like title or priority. That's why we have to modify the fetched properties here.
	    PersistentPropertyList newSettings = pObject.getSettings();

	    // Get the list of keys and textfields.
	    for( Map.Entry<String,JComponent> fieldMapping : propertyObject.getValue().entrySet()) {

		// Loop the current settings to find the currently checked UI element.
		for( PersistentProperty property : newSettings) {

		    // If this property fits this UI field.
		    if( property.getName().equals( fieldMapping.getKey())) {

			if( fieldMapping.getValue() instanceof JTextField) {

			    String inputFieldText = ((JTextField)fieldMapping.getValue()).getText();

			    // Set the new value from the UI input field.
			    // Check if this is an encrypted password, and encrypt the input if necessary.
			    property.setValue( property.isEncryptedPassword() 
					       ? PasswordUtils.getInstance().encryptPassword( inputFieldText)
					       : inputFieldText);
			    
			} else {  // Is this a checkbox
			    
			    // Set the new value from the UI input field.
			    property.setValue( ((JCheckBox)fieldMapping.getValue()).isSelected() ? "true" : "false");
			}
		    }
		}
	    }

	    pObject.setSettings( newSettings);  // Set the new settings for the object.
	}
    }
}
