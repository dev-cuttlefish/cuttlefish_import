package ch.ethz.sg.cuttlefish.gui2.tasks;

import javax.swing.SwingWorker;

import ch.ethz.sg.cuttlefish.gui2.NetworkPanel;
import ch.ethz.sg.cuttlefish.networks.BrowsableNetwork;

public class OpenNetworkTask extends SwingWorker<Object, Object> {
	NetworkPanel networkPanel;
	BrowsableNetwork network;
	public OpenNetworkTask(NetworkPanel networkPanel, BrowsableNetwork network) {
		this.networkPanel = networkPanel;
		this.network = network;
	}
	@Override
	protected Object doInBackground() throws Exception {
		networkPanel.getStatusBar().setBusyMessage("Opening a network", this);
		
        networkPanel.onNetworkChange();
        networkPanel.getNetworkLayout().reset();
        networkPanel.repaintViewer();   
		networkPanel.stopLayout();  
		networkPanel.getStatusBar().setMessage("Network opened");
		return null;
	}
}
