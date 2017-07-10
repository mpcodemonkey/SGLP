package games.tracks.configuration;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;
import games.tracks.configuration.net.NetworkConfigurationPanel;
import games.tracks.configuration.net.NetworkConfiguration;
import games.tracks.configuration.display.DisplayConfigurationPanel;
import games.tracks.configuration.display.DisplayConfiguration;

/**
 * Button for saving the configuration settings.
 */
public class ConfigurationSaveButton extends JButton implements ActionListener
{
  JFrame                    configFrame;
  NetworkConfigurationPanel ncPanel;
  DisplayConfigurationPanel dispPanel;
  NetworkConfiguration      netConf;
  DisplayConfiguration      dispConf;

  /**
   * Init.
   * @param frame A reference to the JFrame, for closing.
   * @param ncPanel The current NetworkConfigurationPanel.
   * @param netConf A reference to the game's NetworkConfiguration.
   * @param dispPanel The current DisplayPanel.
   * @param dispConf A reference to the game's DisplayConfiguration.
   */
  public ConfigurationSaveButton(JFrame configFrame,
    NetworkConfigurationPanel ncPanel, NetworkConfiguration netConf,
    DisplayConfigurationPanel dispPanel, DisplayConfiguration dispConf)
  {
    super("Save");

    this.configFrame = configFrame;
    this.ncPanel     = ncPanel;
    //this.netConf     = netConf;
    this.dispPanel   = dispPanel;
    this.dispConf    = dispConf;

    this.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.addActionListener(this);
  }

  /**
   * Fires when the save button is clicked.  Validate the network settings
   * and close.
   * @param e The ActionEvent.
   */
  @Override public void actionPerformed(ActionEvent e)
  {
    /***
     * Validate and store the network settings.
    ****/

    String ncPanelName = this.ncPanel.getName();
/*
    if (ncPanelName.equals("Single Player"))
    {
      // Single player - no server or port required.
      this.netConf.setMode(NetworkConfiguration.Mode.SINGLE_PLAYER);
    }
    else if (ncPanelName.equals("Demo"))
    {
      // Demo mode (bot) - no server or port required.
      this.netConf.setMode(NetworkConfiguration.Mode.DEMO);
    }
    else
    {
      if (ncPanelName.equals("Client"))
        this.netConf.setMode(NetworkConfiguration.Mode.CLIENT);
      else if (ncPanelName.equals("Server"))
        this.netConf.setMode(NetworkConfiguration.Mode.SERVER);

      // Validate the host and port.
      try
      {
        this.netConf.setServerAddress(InetAddress.getByName(this.ncPanel.getServerIP()));
        this.netConf.setPort(Integer.parseInt(this.ncPanel.getPort()));
      }
      catch (UnknownHostException ex)
      {
        JOptionPane.showMessageDialog(this.configFrame, "Invalid server IP.");
        return;
      }
      catch (NumberFormatException ex)
      {
        JOptionPane.showMessageDialog(this.configFrame, "Invalid port.");
        return;
      }
    }
*/
    /***
     * Validate and store the display settings.
    ****/

    String dispPanelName = this.dispPanel.getName();

    if (dispPanelName.equals("Fullscreen"))
    {
      this.dispConf.setMode(DisplayConfiguration.Mode.FULLSCREEN);
    }
    else
    {
      this.dispConf.setMode(DisplayConfiguration.Mode.WINDOWED);

      // Validate the width and height.
      try
      {
        this.dispConf.setWidth(Integer.parseInt(this.dispPanel.getDisplayWidth()));
        this.dispConf.setHeight(Integer.parseInt(this.dispPanel.getDisplayHeight()));
      }
      catch (NumberFormatException ex)
      {
        JOptionPane.showMessageDialog(this.configFrame, "Invalid width or height.");
        return;
      }
    }

    // These are helpful for debugging.
    //System.out.println("NET MODE: " + this.netConf.getMode());
    //System.out.println("IP: " + this.netConf.getServerAddress());
    //System.out.println("Port: " + this.netConf.getPort());

    //System.out.println("Mode: " + this.dispConf.getMode());
    //System.out.println("Width: " + this.dispConf.getWidth());
    //System.out.println("Height: " + this.dispConf.getHeight());

    this.configFrame.dispose();
  }
}
