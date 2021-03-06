/*
  
    Copyright (C) 2011  Markus Michael Geipel, David Garcia Becerra,
    Petar Tsankov

	This file is part of Cuttlefish.
	
 	Cuttlefish is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package ch.ethz.sg.cuttlefish.gui;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import javax.media.opengl.awt.GLCanvas;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.gephi.layout.spi.Layout;
import org.openide.util.NotImplementedException;

import ch.ethz.sg.cuttlefish.gui.undoable.UndoableControl;
import ch.ethz.sg.cuttlefish.gui.visualization.NetworkRenderer;
import ch.ethz.sg.cuttlefish.gui.visualization.mouse.GraphMouse;
import ch.ethz.sg.cuttlefish.gui.visualization.mouse.GraphMouse.Mode;
import ch.ethz.sg.cuttlefish.gui.visualization.mouse.GraphMouseImpl;
import ch.ethz.sg.cuttlefish.layout.LayoutLoader;
import ch.ethz.sg.cuttlefish.misc.Observer;
import ch.ethz.sg.cuttlefish.misc.Subject;
import ch.ethz.sg.cuttlefish.networks.BrowsableForestNetwork;
import ch.ethz.sg.cuttlefish.networks.BrowsableNetwork;
import ch.ethz.sg.cuttlefish.networks.Edge;
import ch.ethz.sg.cuttlefish.networks.InteractiveCxfNetwork;
import ch.ethz.sg.cuttlefish.networks.Vertex;

public class NetworkPanel extends JPanel implements Subject, INetworkBrowser,
		Runnable, java.util.Observer {

	private static final long serialVersionUID = 1L;

	private BrowsableNetwork network = null;
	private String currentLayout = null;
	private List<Observer> observers = null;
	private StatusBar statusBar = null;
	private int width;
	private int height;

	// Updated visualization
	private GLCanvas canvas = null;
	LayoutLoader layoutLoader = null;
	private NetworkRenderer networkRenderer = null;
	private GraphMouse gMouse;

	public NetworkPanel() {
		super();
		width = 1096;
		height = 200;
		initialize();
	}

	public NetworkPanel(int width, int height) {
		super();
		this.width = width;
		this.height = height;
		initialize();
	}

	public NetworkRenderer getNetworkRenderer() {

		if (networkRenderer == null) {
			networkRenderer = new NetworkRenderer(this, width, height);
			canvas = networkRenderer.getCanvas();
		}

		return networkRenderer;
	}

	public StatusBar getStatusBar() {
		if (statusBar == null)
			statusBar = new StatusBar();
		return statusBar;
	}

	public String getCurrentLayout() {
		return currentLayout;
	}

	private void initialize() {
		observers = new LinkedList<Observer>();

		setLayout(new BorderLayout());
		add(getStatusBar(), BorderLayout.SOUTH);
		setSize(width, height);

		BrowsableNetwork network = new BrowsableNetwork();
		networkRenderer = new NetworkRenderer(this, width, height);
		layoutLoader = LayoutLoader.initGUI(this);

		setNetwork(network);

		canvas = networkRenderer.getCanvas();
		add(canvas, BorderLayout.CENTER);

		gMouse = new GraphMouseImpl(this);
		gMouse.setMode(Mode.TRANSFORMING);

		// Add OpenGL and Mouse event listeners
		canvas.addGLEventListener(getNetworkRenderer());
		canvas.addMouseMotionListener(getNetworkRenderer().getGraphMouse());
		canvas.addMouseListener(getNetworkRenderer().getGraphMouse());
		canvas.addMouseWheelListener(getNetworkRenderer().getGraphMouse());
	}

	@Override
	public Layout getNetworkLayout() {
		return layoutLoader.getSelectedLayout();
	}

	@Override
	public void repaintViewer() {
		getNetworkRenderer().repaint();
	}

	/**
	 * Getter for the network
	 * 
	 * @return BrowsableNetwork in use in CuttleFish (wraps an
	 *         org.gephi.graph.api.MixedGraph)
	 */
	public BrowsableNetwork getNetwork() {
		return network;
	}

	@Override
	public void setNetwork(BrowsableNetwork network) {
		this.network = network;
		layoutLoader.setNetwork(network);
		/*
		 * Default layout: ARF If the network is not a forest and the layout is
		 * a tree layout, change the layout to the default ARF layout.
		 */

		if (getNetworkLayout() == null) {
			setLayoutByName(LayoutLoader.DEFAULT_LAYOUT);
		} else {
			layoutLoader.resetLayout();
		}

		refreshAnnotations();

		/* Undoable Controller */
		UndoableControl.getController().addObserver(this);
		UndoableControl.resetController();
	}

	@Override
	public void refreshAnnotations() {
		network.updateAnnotations();
		network.applyShadows();
		this.repaintViewer();
	}

	@Override
	public void onNetworkChange() {

		// //concurrent modification of the ARF layouts for simulation position
		// updates
		// if (layout instanceof ARF2Layout) {
		// ((ARF2Layout<Vertex, Edge>) layout).step();
		// ((ARF2Layout<Vertex, Edge>) layout).resetUpdates();
		// } else if (layout instanceof WeightedARF2Layout) {
		// ((WeightedARF2Layout<Vertex, Edge>) layout).step();
		// ((WeightedARF2Layout<Vertex, Edge>) layout).resetUpdates();
		// }
		//
		// if (layout instanceof FixedLayout) {
		// ((FixedLayout<Vertex, Edge>) layout).update();
		// }

		// non-iterative layouts need to be explicitly reset
		/*
		 * layout instanceof TreeLayout || layout instanceof RadialTreeLayout ||
		 */

		/*
		 * If the network is Interactive (simulation network), then do not reset
		 * the layout before it's completed.
		 */
		if (network instanceof InteractiveCxfNetwork
				&& layoutLoader.isLayoutRunning()) {
			return;
		}

		if (getNetworkLayout() instanceof BrowsableForestNetwork) {
			setNetwork(((BrowsableForestNetwork) getNetwork())
					.getOriginalNetwork());
		}

		resumeLayout();
	}

	@Override
	public void stopLayout() {
		layoutLoader.stopLayout();
	}

	public void centerLayout() {
		networkRenderer.centerNetwork();
		networkRenderer.repaint();
	}

	@Override
	public void resumeLayout() {

		layoutLoader.resetLayout();

		// boolean fixed = getNetworkLayout() instanceof FixedLayout;
		// getNetwork().fixVertices(!fixed);
	}

	public void setLayoutParameters(Map<String, String> parameters) {
		layoutLoader.setLayoutParameters(parameters);
	}

	@Override
	public void setLayoutByName(String selectedLayout) {
		layoutLoader.setLayoutByName(selectedLayout);

		for (Observer o : observers)
			o.update(this);
	}

	/**
	 * Getter for the graph mouse associated to the panel
	 * 
	 * @return EditingModalGraphMouse automatically created by JUNG2.0
	 */
	public GraphMouse getMouse() {
		return gMouse;
	}

	@Override
	public BufferedImage getSnapshot() {
		// Dimension size = getVisualizationViewer().getSize();
		// BufferedImage img = new BufferedImage(size.width, size.height,
		// BufferedImage.TYPE_INT_RGB);
		// Graphics2D g2 = img.createGraphics();
		// getVisualizationViewer().paint(g2);
		// return img;
		throw new NotImplementedException(
				"Not implemented yet for the Gephi Toolkit!");
	}

	private Set<Vertex> pickedVertices = new HashSet<Vertex>();

	@Override
	public Set<Vertex> getPickedVertices() {
		return pickedVertices;
	}

	public void selectVertex(Vertex v) {
		pickedVertices.clear();
		pickedVertices.add(v);
	}

	private Set<Edge> pickedEdges = new HashSet<Edge>();

	@Override
	public Set<Edge> getPickedEdges() {
		return pickedEdges;
	}

	public void selectEdge(Edge e) {
		pickedEdges.clear();
		pickedEdges.add(e);
	}

	@Override
	public void run() {
		while (true)
			;
	}

	@Override
	public void addObserver(Observer o) {
		observers.add(o);
	}

	@Override
	public void removeObserver(Observer o) {
		observers.remove(o);
	}

	@Override
	public void update(Observable o, Object arg) {
		refreshAnnotations();
	}

	public GLCanvas getCanvas() {
		return canvas;
	}

	public void errorPopup(String title, String message) {
		JOptionPane.showMessageDialog(this, message, title,
				JOptionPane.ERROR_MESSAGE);
	}
}
