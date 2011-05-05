package ch.ethz.sg.cuttlefish.gui.applet;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ch.ethz.sg.cuttlefish.gui2.CuttlefishToolbars;
import ch.ethz.sg.cuttlefish.gui2.NetworkPanel;
import ch.ethz.sg.cuttlefish.networks.BrowsableNetwork;
import ch.ethz.sg.cuttlefish.networks.CxfNetwork;

public class Cuttlefish extends Applet {

	private BrowsableNetwork network = null;
	private NetworkPanel networkPanel = null;
	private CuttlefishMenu menu = null;
	private CuttlefishToolbars toolbars = null;
	private final String NETWORK_FILE = "ch/ethz/sg/cuttlefish/examples/network2.cxf";
	
    public void init() {
    	try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (Exception e) { 
            System.err.println("createGUI didn't complete successfully");
        }          
    }

    public void start() {
    	this.setVisible(true);
    }

    public void stop() {
    }

    public void destroy() {
    }
    
    private void createGUI() {

    	this.setSize(1000, 700);
    	
    	JPanel container = new JPanel();
    	container.setSize(1000, 700);
    	container.setLayout(new BorderLayout() );
    	JPanel controlContainer = new JPanel();
    	controlContainer.setLayout(new BoxLayout(controlContainer, BoxLayout.Y_AXIS));
    	controlContainer.add(getMenu());
    	controlContainer.add(getToolbars());
    	container.add(controlContainer, BorderLayout.NORTH);
    	container.add(getNetworkPanel(), BorderLayout.CENTER);
    	this.add(container);
    	
		toolbars.getSimulationToolbar().addObserver(menu.getViewMenu());
		toolbars.getMouseToolbar().addObserver(menu.getViewMenu());
		toolbars.getZoomToolbar().addObserver(menu.getViewMenu());
		toolbars.getSimulationToolbar().setVisible(true);
		toolbars.getMouseToolbar().setVisible(true);
		toolbars.getZoomToolbar().setVisible(true);
    }
    
    private NetworkPanel getNetworkPanel() {
    	if(networkPanel == null) {
    		networkPanel = new NetworkPanel();
    		networkPanel.setNetwork(getNetwork());
    	}
    	return networkPanel;
    }
    
    private BrowsableNetwork getNetwork() {
    	if(network == null) {    		
    		network = new CxfNetwork(new File(NETWORK_FILE));
    	}
    	return network;
    }
    
    private CuttlefishToolbars getToolbars() {
    	if(toolbars == null) {
    		toolbars = new CuttlefishToolbars(getNetworkPanel());
    	}
    	return toolbars;
    }
    
    private CuttlefishMenu getMenu() {
    	if(menu == null) {
    		menu = new CuttlefishMenu(getNetworkPanel(), getToolbars());
    	}
    	return menu;
    	
    }

}
