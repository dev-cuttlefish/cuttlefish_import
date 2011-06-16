package ch.ethz.sg.cuttlefish.gui2.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import ch.ethz.sg.cuttlefish.gui2.CuttlefishToolbars;
import ch.ethz.sg.cuttlefish.gui2.NetworkPanel;

public class LayoutMenu extends AbstractMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ButtonGroup layoutButtons;
	private JMenuItem stopButton;
	private JMenuItem restartButton;
	private JRadioButtonMenuItem arf;
	private JRadioButtonMenuItem kcore;
	private JRadioButtonMenuItem fixed;
	private JRadioButtonMenuItem weightedArf;
	private JRadioButtonMenuItem spring;
	private JRadioButtonMenuItem kamada;
	private JRadioButtonMenuItem fruchterman;
	private JRadioButtonMenuItem isom;
	private JRadioButtonMenuItem circle;
	private JRadioButtonMenuItem tree;
	private JRadioButtonMenuItem radialTree;
	private Map<JRadioButtonMenuItem, String> layoutMap;

	public LayoutMenu(NetworkPanel networkPanel, CuttlefishToolbars toolbars) {
		super(networkPanel, toolbars);
		initialize();
		restartButton.doClick();
		this.setText("Layout");
	}
	
	private void initialize() {
		layoutButtons = new ButtonGroup();		
		arf = new JRadioButtonMenuItem("ARF");
		kcore = new JRadioButtonMenuItem("KCore");
		fixed = new JRadioButtonMenuItem("Fixed");
		weightedArf = new JRadioButtonMenuItem("Weighted ARF");
		spring = new JRadioButtonMenuItem("Spring");
		kamada = new JRadioButtonMenuItem("Kamada Kawai");
		fruchterman = new JRadioButtonMenuItem("Fruchterman Reingold");
		isom = new JRadioButtonMenuItem("ISO M");
		circle = new JRadioButtonMenuItem("Circle");
		tree = new JRadioButtonMenuItem("Tree");
		radialTree = new JRadioButtonMenuItem("Radial Tree");
		layoutButtons.add(arf);
		layoutButtons.add(kcore);
		layoutButtons.add(fixed);
		layoutButtons.add(weightedArf);
		layoutButtons.add(spring);
		layoutButtons.add(kamada);
		layoutButtons.add(fruchterman);
		layoutButtons.add(isom);
		layoutButtons.add(circle);
		layoutButtons.add(tree);
		layoutButtons.add(radialTree);
		
		layoutMap = new HashMap<JRadioButtonMenuItem, String>();
		layoutMap.put(arf, "ARFLayout");
		layoutMap.put(kcore, "KCore");
		layoutMap.put(fixed, "Fixed");
		layoutMap.put(weightedArf, "WeightedARFLayout");
		layoutMap.put(spring, "SpringLayout");
		layoutMap.put(kamada, "Kamada-Kawai");
		layoutMap.put(fruchterman, "Fruchterman-Reingold");
		layoutMap.put(isom, "ISOMLayout");
		layoutMap.put(circle, "CircleLayout");
		layoutMap.put(tree, "TreeLayout");
		layoutMap.put(radialTree, "RadialTreeLayout");
		
		arf.setSelected(true);
		
		stopButton = new JMenuItem("Stop");
		restartButton = new JMenuItem("Restart");
		
		restartButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
		stopButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));

		
		this.add(stopButton);
		this.add(restartButton);
		
		this.addSeparator();
		
		this.add(arf);
		this.add(kcore);
		this.add(fixed);
		this.add(weightedArf);
		this.add(spring);
		this.add(kamada);
		this.add(fruchterman);
		this.add(isom);
		this.add(circle);
		this.add(tree);
		this.add(radialTree);
		
		stopButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				networkPanel.stopLayout();
				stopButton.setEnabled(false);
				restartButton.setEnabled(true);
			}
		});
		
		restartButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				networkPanel.resumeLayout();
				restartButton.setEnabled(false);
				stopButton.setEnabled(true);
			}
		});
		
		arf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(arf); }
		});
		kcore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(kcore); }
		});
		fixed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(fixed); }
		});
		weightedArf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(weightedArf); }
		});
		spring.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(spring); }
		});
		kamada.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(kamada); }
		});
		fruchterman.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(fruchterman); }
		});
		isom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(isom); }
		});
		circle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(circle); }
		});
		tree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(tree); }
		});
		radialTree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { layoutSelected(radialTree); }
		});
	}
	
	private void layoutSelected(JRadioButtonMenuItem selected) {
		networkPanel.setLayout(layoutMap.get(selected ));
	}

}
