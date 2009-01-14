/*
   
    Copyright (C) 2008  Markus Michael Geipel

    This program is free software: you can redistribute it and/or modify
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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.collections15.Transformer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.ethz.sg.cuttlefish.layout.ARF2Layout;
import ch.ethz.sg.cuttlefish.layout.FixedLayout2;
import ch.ethz.sg.cuttlefish.misc.Edge;
import ch.ethz.sg.cuttlefish.misc.Utils;
import ch.ethz.sg.cuttlefish.misc.Vertex;
import ch.ethz.sg.cuttlefish.misc.XMLUtil;
import ch.ethz.sg.cuttlefish.networks.BrowsableNetwork;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.*;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;

/**
 * Class for the main JPanel of cuttlefish
 */
public class CuttlefishPanel extends JPanel implements ItemListener,INetworkBrowser{

	private static final long serialVersionUID = 1L;
//	DefaultModalGraphMouse gm = new DefaultModalGraphMouse();  //  @jve:decl-index=0:
	private BrowsableNetwork network = null;  //  @jve:decl-index=0:
	private Layout<Vertex,Edge> layout = null;
	private VisualizationViewer<Vertex,Edge> visualizationViewer = null;
	private final ScalingControl scaler = new CrossoverScalingControl();  //  @jve:decl-index=0:
	private JTabbedPane menuPane = null;
	//private double scale=1.0;  //  @jve:decl-index=0:
	private JToggleButton jToggleButton = null;
	private JPanel viewPanel = null;
	private Hashtable<String, String> arguments = new Hashtable<String, String>();
	

	private Hashtable<String, BrowserWidget> widgetTable = new Hashtable<String, BrowserWidget>();
	private ArrayList<BrowserTab> tabArray = new ArrayList<BrowserTab>();  //  @jve:decl-index=0:
	private Document configuration;  //  @jve:decl-index=0:
	private DefaultModalGraphMouse<Vertex,Edge> graphMouse;  //  @jve:decl-index=0:
	private JButton jButton = null;
	private JButton jButton1 = null;
	private JButton jButton2 = null;
	private JButton jButton3 = null;
	private JPanel zoomPanel = null;
	private JPanel pickPanel = null;
	private JPanel layoutPanel = null;
	private JCheckBox layoutCheckBox = null;
	private JButton writeLayoutButton = null;
	/**
	 * This is the default constructor, initializes the rendering and the viewer
	 * @param configfile open file with the configuration
	 */
	public CuttlefishPanel(File configFile) {
		super();
		initialize(configFile);
		
		Transformer<Vertex, Shape> vertexShapeTransformer = new Transformer<Vertex, Shape>() {			
			public Shape transform(Vertex vertex) {
				Ellipse2D ellipse = new Ellipse2D.Float();
				ellipse.setFrameFromCenter(0,0,vertex.getRadius(),vertex.getRadius());
				return ellipse; } };
		
		Transformer<Edge, Paint> edgePaintTransformer = new Transformer<Edge, Paint>(){
			public Paint transform(Edge edge) {
				return new Color(Integer.parseInt(edge.getColor())); } };
		
		Transformer<Vertex,String> vertexLabelTransformer = new Transformer<Vertex,String>(){
			public String transform(Vertex vertex) {
				return vertex.getLabel(); } };
			
		Transformer<Edge,Stroke> edgeStrokeTransformer = new Transformer<Edge, Stroke>() {
			public Stroke transform(Edge edge) {
				return new BasicStroke(new Double(edge.getWidth()).intValue()); } };
			
		Transformer<Vertex, Stroke> vertexStrokeTransformer = new Transformer<Vertex, Stroke>(){
			public Stroke transform(Vertex vertex) {
				return new BasicStroke(new Double(vertex.getWidth()).intValue()); } };
			
		Transformer<Vertex, Paint> vertexPaintTransformer = new Transformer<Vertex, Paint>(){
			public Paint transform(Vertex vertex) {
				return vertex.getFillColor(); } };
		
				
		visualizationViewer.getRenderContext().setVertexShapeTransformer(vertexShapeTransformer);
		visualizationViewer.getRenderContext().setEdgeFillPaintTransformer(edgePaintTransformer);		
		visualizationViewer.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);
		visualizationViewer.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		visualizationViewer.getRenderContext().setVertexStrokeTransformer(vertexStrokeTransformer);
		visualizationViewer.getRenderContext().setVertexFillPaintTransformer(vertexPaintTransformer);
		//visualizationViewer.setGraphMouse(gm);
		visualizationViewer.setPickSupport(new ShapePickSupport<Vertex,Edge>(visualizationViewer));
        visualizationViewer.setGraphMouse(graphMouse);
        visualizationViewer.getPickedVertexState().addItemListener(this);
		
	}
	
	
	
	/**
	 * This method initializes this, creating the GraphMouse, the DocumentFactory 
	 * and loading the information contained in the configuration file
	 * @param configFile open file with the configuration
	 * @return void
	 */
	private void initialize(File configFile) {
	
		this.setLayout(new BorderLayout());
		this.setSize(1096, 200);
		this.setBackground(Color.gray);
		
        //visualizationViewer.setPickSupport(new ShapePickSupport());
        graphMouse = new DefaultModalGraphMouse<Vertex,Edge>();
        //visualizationViewer.setGraphMouse(gm);
        
		
        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
       
        factory.setValidating(false);  
        factory.setNamespaceAware(true);
       // factory.setSchema(arg0)
       // System.out.println(this.getClass().getResource("/ch/ethz/sg/cuttlefish/resources/datasources.xsd"));
       
        Schema schema;
		try {
			 File schemaFile = new File(this.getClass().getResource("/ch/ethz/sg/cuttlefish/resources/configuration.xsd").getFile());
			schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFile);
			 factory.setSchema(schema);
		} catch (SAXException e1) {
			System.err.println("Error in XML validation!");
			e1.printStackTrace();
		}
      
        
        try {
           DocumentBuilder builder = factory.newDocumentBuilder();
           configuration = builder.parse(configFile);
           Node arguments = configuration.getElementsByTagName("Arguments").item(0);
 	      this.arguments = XMLUtil.getArguments(arguments);
              
           NodeList tabs = configuration.getElementsByTagName("Tab");

           for( int i=0; i<tabs.getLength(); i++ ){
        	   	  Node tab = tabs.item(i);
        	      System.out.println( ""+i+": " + tab.getAttributes().getNamedItem("name").getNodeValue() );
        	      BrowserTab panel = new BrowserTab(tab.getAttributes().getNamedItem("condition").getNodeValue());
        	      panel.setBackground(Color.lightGray);
        	      panel.setLayout(new GridBagLayout());
        	      panel.setName(tab.getAttributes().getNamedItem("name").getNodeValue());
        	      NodeList widgets = tab.getChildNodes();
        	      
        	      GridBagConstraints boxConstraints = new GridBagConstraints();
	  			  boxConstraints.gridx = 0;
	  			  boxConstraints.gridy = 0;
	  			  boxConstraints.fill = GridBagConstraints.BOTH;
        	      panel.add(new Box.Filler(new Dimension(0,0), new Dimension(0,125), new Dimension(0,Integer.MAX_VALUE)), boxConstraints);
        	      tabArray.add(panel);
        	      int count = 1;
        	      for(int j=0; j<widgets.getLength(); j++){
        	    	  try{
            	    	  Node widget = widgets.item(j);
            	    	  if(widget.getNodeName().equals("Widget")){
            	    		  
            	    		  GroupPanel groupPanel = new GroupPanel();
            	    		  groupPanel.addItemListener(this);
            	    		  
            	    		  String className = widget.getAttributes().getNamedItem("class").getNodeValue();
            	    		  Class<?> clazz = Class.forName(className);
            	    		  BrowserWidget browserWidget = (BrowserWidget) clazz.newInstance();
            	  			 
            	    		  groupPanel.setLabel(widget.getAttributes().getNamedItem("name").getNodeValue());
            	  			  groupPanel.setBrowserWidget(browserWidget, widget.getAttributes().getNamedItem("id").getNodeValue());
            	  			  
            	  			  browserWidget.setBrowser(this);
            	  			  browserWidget.setArguments(XMLUtil.getArguments(widget));
            	  			  
            	  			  GridBagConstraints gridBagConstraints = new GridBagConstraints();
            	  			  gridBagConstraints.gridx = count++;
            	  			  gridBagConstraints.fill = GridBagConstraints.BOTH;
            	  			  gridBagConstraints.insets = new Insets(2, 2, 2, 2);
            	  			  gridBagConstraints.gridy = 0;
            	  			  
            	  			  panel.add(groupPanel, gridBagConstraints);
            	  			  widgetTable.put(widget.getAttributes().getNamedItem("id").getNodeValue(), browserWidget);
            	  			  browserWidget.init();
            	    	  }
        	    	  }catch(Exception e){
        	    		  
        	    		  e.printStackTrace();
        	    		  
        	    	  }
       	    	  
        	      }

           }
           
 	      
        } catch (Exception e) {
          System.err.println("Error in the configuration file!");
   		  System.err.println(e);
   		  System.exit(-1);
        }
        
		BrowsableNetwork temp = new BrowsableNetwork();
		//temp.setName(name);
		this.setNetwork(temp);
		this.add(getVisualizationViewer(), BorderLayout.CENTER);
		this.add(getMenuPane(), BorderLayout.NORTH);
		this.add(getViewPanel(), BorderLayout.SOUTH);
	}

	/**
	 * Getter for JUNG's VisualizationViewer, creating it if it does not exist
	 * @return VisualizationViewer	
	 */
	private VisualizationViewer<Vertex,Edge> getVisualizationViewer() {
		if (visualizationViewer == null) {
			visualizationViewer = new VisualizationViewer<Vertex,Edge>(layout);
			//visualizationViewer.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			visualizationViewer.setBackground(Color.white);
			
			GraphMouse graphMouse = new DefaultModalGraphMouse<Vertex,Edge>();
			visualizationViewer.setGraphMouse(graphMouse );
		}
		return visualizationViewer;
	}

	/**
	 * Gives the position file of the argument list
	 * @return File object with the position file
	 */
	public File getPositionFile(){
		String base = getArgument("layoutFolder");
		File positionData = new File((base == null ? "" : base) + network.getName()+".pos");
		return positionData;
	}
	
	/**
	 * Starts a dynamic or a static layout for the network
	 * @param isDynamic boolean to determine the dynamics of the network, 
	 * with ARF or a fixed layout
	 * @return void
	 */
	public void setLayout(boolean isDynamic){
		
		
		File positionData = getPositionFile();
		layout = null;
		
		if(!isDynamic){
			try{
				FixedLayout2<Vertex,Edge> fixed = new FixedLayout2<Vertex,Edge>(network);
				fixed.setPositionFile(positionData);
				layout = fixed;
				getLayoutCheckBox().setSelected(false);
			}catch(Exception e){
				System.out.println("Position file missing for network " +  network.getName() + " ("+positionData+")");
			}
		}
		
		if(layout == null){
			ARF2Layout<Vertex, Edge> arf = new ARF2Layout<Vertex, Edge>(network);
			arf.setForceCutoff(100);
			arf.setAttraction(0.15);
			layout = arf;
			getLayoutCheckBox().setSelected(true);
		}
		System.out.println("Set layout to " + layout.getClass());
		
		getVisualizationViewer().repaint();
		getVisualizationViewer().setGraphLayout(layout);
		//System.out.println("VV restarted");
	}

	/**
	 * Setter for the network subject of cuttlefish
	 * @param network Network object to use in cuttlefish
	 * @return void
	 */
	public void setNetwork(BrowsableNetwork network) {
		this.network = network;
		System.out.println("Set network " + network.getName() + " (" + network.getVertices().size() + " nodes)");
		
		setLayout(true);
		
		network.init();
		
		refreshAnnotations();
		updateWidgets();
	}



	/**
	 * Updates the network information of all the widgets in the list
	 * @return void
	 */
	private void updateWidgets() {
		for(BrowserWidget widget: widgetTable.values()){
			//System.out.println("updating " + widget.getId());
			widget.setNetwork(network);
			
		}
		
		getMenuPane().removeAll();
		for(BrowserTab tab: tabArray){
			if(tab.checkCondition(network)){
				getMenuPane().add(tab.getName(), tab);
			}
		}
	}



	public void itemStateChanged(ItemEvent arg0) {
		refreshAnnotations();
	}

	/**
	 * Getter for the network
	 * @return DirectedSparseGraph network in use in CuttleFish
	 */
	public DirectedSparseGraph<Vertex,Edge> getNetwork() {
		return network;
	}

	/**
	 * This method initializes menuPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getMenuPane() {
		if (menuPane == null) {
			menuPane = new JTabbedPane();
			menuPane.setBackground(Color.lightGray);
			menuPane.setForeground(Color.black);
		}
		return menuPane;
	}

	/**
	 * This method initializes jToggleButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJToggleButton() {
		if (jToggleButton == null) {
			jToggleButton = new JToggleButton();
			jToggleButton.setText("tools");
			jToggleButton.setSelected(true);
			jToggleButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					menuPane.setVisible(jToggleButton.isSelected());
				}
			});
		}
		return jToggleButton;
	}

	/**
	 * This method initializes viewPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getViewPanel() {
		if (viewPanel == null) {
			viewPanel = new JPanel();
			viewPanel.setLayout(new BoxLayout(getViewPanel(), BoxLayout.X_AXIS));
			viewPanel.setBackground(Color.DARK_GRAY);
			viewPanel.add(getJToggleButton(), null);
			viewPanel.add(getLayoutPanel(), null);
			viewPanel.add(getPickPanel(), null);
			//viewPanel.add(graphMouse.getModeComboBox());
			viewPanel.add(getZoomPanel(), null);
			//graphMouse.getModeComboBox().setPreferredSize(new Dimension());
			
			
		}
		return viewPanel;
	}

	public Layout<Vertex,Edge> getNetworkLayout() {
		return layout;
	}


	public void repaintViewer() {
		visualizationViewer.repaint();
		
	}

	public void refreshAnnotations() {
		
		//network.colorAll(Color.DARK_GRAY);
		for(BrowserWidget widget: widgetTable.values()){
			if((!widget.isClickable() || widget.isActive())&&widget.getNetwork()!=null){
				widget.updateAnnotations();
			}
		}
		network.updateAnnotations();
		network.applyShadows();
		System.gc();
		visualizationViewer.repaint();
	}


	public Document getConfiguration() {
		return configuration;
	}

	public void onNetworkChange() {
		System.out.println("Network changed " + network.getName());
		((IterativeContext)layout).step();
		refreshAnnotations();
		
	}
	
	public String getArgument(String name) {
		return arguments.get(name);
	}



	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("-");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					scaler.scale(visualizationViewer, 0.9f, visualizationViewer.getCenter());
				}
			});
		}
		return jButton;
	}



	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("+");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					scaler.scale(visualizationViewer, 1.1f, visualizationViewer.getCenter());
				}
			});
		}
		return jButton1;
	}



	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("--");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					scaler.scale(visualizationViewer, 0.5f, visualizationViewer.getCenter());
				}
			});
		}
		return jButton2;
	}


	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setText("++");
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					scaler.scale(visualizationViewer, 2.0f, visualizationViewer.getCenter());
				}
			});
		}
		return jButton3;
	}



	/**
	 * This method initializes zoomPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getZoomPanel() {
		if (zoomPanel == null) {
			zoomPanel = new JPanel();
			zoomPanel.setLayout(new GridBagLayout());
			zoomPanel.add(getJButton2(), new GridBagConstraints());
			zoomPanel.add(getJButton(), new GridBagConstraints());
			zoomPanel.add(getJButton1(), new GridBagConstraints());
			zoomPanel.add(getJButton3(), new GridBagConstraints());
			zoomPanel.setBackground(Color.gray);
		}
		return zoomPanel;
	}



	/**
	 * This method initializes pickPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPickPanel() {
		if (pickPanel == null) {
			pickPanel = new JPanel();
			pickPanel.setLayout(new GridBagLayout());
			pickPanel.add(graphMouse.getModeComboBox());
			pickPanel.setBackground(Color.GRAY);
		}
		return pickPanel;
	}



	/**
	 * This method initializes layoutPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getLayoutPanel() {
		if (layoutPanel == null) {
			layoutPanel = new JPanel();
			layoutPanel.setLayout(new GridBagLayout());
			layoutPanel.add(getLayoutCheckBox(), new GridBagConstraints());
			layoutPanel.setBackground(Color.gray);
			layoutPanel.add(getWriteLayoutButton(), new GridBagConstraints());
		}
		return layoutPanel;
	}



	/**
	 * This method initializes layoutCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getLayoutCheckBox() {
		if (layoutCheckBox == null) {
			layoutCheckBox = new JCheckBox();
			layoutCheckBox.setText("dynamic layout");
			layoutCheckBox.setBackground(Color.gray);
			layoutCheckBox.setForeground(Color.orange);
			layoutCheckBox.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					setLayout(layoutCheckBox.isSelected());
					
				}
				
			});
		}
		return layoutCheckBox;
	}



	public void resumeLayout() {
//		visualizationViewer.stop();	
	}



	public void stopLayout() {
//		visualizationViewer.restart();	
	}



	/**
	 * This method initializes writeLayoutButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getWriteLayoutButton() {
		if (writeLayoutButton == null) {
			writeLayoutButton = new JButton();
			writeLayoutButton.setText("save layout");
			writeLayoutButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						PrintStream p = new PrintStream(getPositionFile());
						Utils.writePositions(getNetwork(), p, getNetworkLayout());
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
					}
				}
			});
		}
		return writeLayoutButton;
	}

	
}  //  @jve:decl-index=0:visual-constraint="10,10"