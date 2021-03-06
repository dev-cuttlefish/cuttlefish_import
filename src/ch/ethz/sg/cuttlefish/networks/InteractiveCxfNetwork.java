/*
  
    Copyright (C) 2009  Markus Michael Geipel, David Garcia Becerra

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

package ch.ethz.sg.cuttlefish.networks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import ch.ethz.sg.cuttlefish.Cuttlefish;
import ch.ethz.sg.cuttlefish.layout.LayoutLoader;
import ch.ethz.sg.cuttlefish.misc.Observer;
import ch.ethz.sg.cuttlefish.misc.Subject;

public class InteractiveCxfNetwork extends CxfNetwork implements ISimulation,
		Subject {

	boolean done;
	private static final long serialVersionUID = 1L;
	private ArrayList<Observer> observers;
	private String currentLabel = "";
	private int currentSleepTime = 0;
	private int currentMaxStepUpdates = 50;

	public InteractiveCxfNetwork() {
		super();
		observers = new ArrayList<Observer>();
		setIncremental(true);
	}

	public InteractiveCxfNetwork(File graphFile) throws FileNotFoundException {
		load(graphFile);
		setIncremental(true);
		done = false;
	}

	public void loadInstructions(File instructionsFile) {
		setIncremental(true);
		try {
			fr = new FileReader(instructionsFile);
		} catch (FileNotFoundException fnfEx) {
			JOptionPane.showMessageDialog(null, fnfEx.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			Cuttlefish.err("cxf network file not found");
			fnfEx.printStackTrace();
		}

		br = new BufferedReader(fr);

		Token token;
		instructionTokens = new ArrayList<Token>();

		while ((token = getNextToken()) != null)
			instructionTokens.add(token);

		return;
	}

	public int getCurrentSleepTime() {
		return currentSleepTime;
	}

	public int getCurrentMaxStepUpdates() {
		return currentMaxStepUpdates;
	}

	private void execute(Token token) {
		if (token.type == null)
			return;

		if (token.type.equalsIgnoreCase("layout")) {
			String layout = token.var1;
			LayoutLoader.getInstance().setLayoutParameters(token.params);
			LayoutLoader.getInstance().setLayoutByName(layout);

		} else if (token.type.equalsIgnoreCase("addNode")) {
			if (hash.containsKey(token.id))
				System.out
						.println("WARNING: trying to add an existing node -- Use editNode");
			else {
				Vertex v = createVertex(token);
				hash.put(token.id, v);

				// add the new vertex somewhere near the existing ones
				v.setPosition(getPointNearGraph());
				addVertex(v);
			}
		} else if (token.type.equalsIgnoreCase("removeNode")) {
			if (hash.containsKey(token.id)) {
				Vertex v = hash.get(token.id);

				if (isDirected()) {
					for (Edge e : getOutEdges(v))
						removeEdge(e);
					for (Edge e : getInEdges(v))
						removeEdge(e);
				} else
					for (Edge e : getIncidentEdges(v))
						removeEdge(e);
				removeVertex(v);
				hash.remove(token.id);
			}

		} else if (token.type.equalsIgnoreCase("editNode")) {
			if (hash.containsKey(token.id)) {
				Vertex v = hash.get(token.id);
				editVertex(v, token);
			} else {
				JOptionPane.showMessageDialog(null,
						"Editing an inexistent node --- use addNode",
						"Warning", JOptionPane.WARNING_MESSAGE);
				System.out
						.println("WARNING: editing an inexistent node --- use addNode");
			}

		} else if (token.type.equalsIgnoreCase("addEdge")) {
			if (hash.containsKey(token.id_source)
					&& hash.containsKey(token.id_dest)) {
				Vertex vSource = hash.get(token.id_source);
				Vertex vDest = hash.get(token.id_dest);
				if (findEdge(vSource, vDest) != null)
					System.out.println("WARNING: the edge (" + token.id_source
							+ "," + token.id_dest
							+ ") already existed -- use editEdge");
				Edge e = createEdge(token);
				addEdge(e);
			} else if (!token.commit && !token.freeze) {
				JOptionPane.showMessageDialog(null,
						"One of the endpoints of the added edge ("
								+ token.id_source + "," + token.id_dest
								+ ") does not exist", "Warning",
						JOptionPane.WARNING_MESSAGE);
				System.out
						.println("WARNING: one of the endpoints of the added edge ("
								+ token.id_source
								+ ","
								+ token.id_dest
								+ ") does not exist");
			}
		} else if (token.type.equalsIgnoreCase("removeEdge")) {
			if (hash.containsKey(token.id_source)
					&& hash.containsKey(token.id_dest)) {
				Vertex vSource = hash.get(token.id_source);
				Vertex vDest = hash.get(token.id_dest);
				Edge e;
				if ((e = findEdge(vSource, vDest)) != null)
					removeEdge(e);
			} else {
				JOptionPane.showMessageDialog(null,
						"One of the endpoints of the added edge ("
								+ token.id_source + "," + token.id_dest
								+ ") does not exist", "Warning",
						JOptionPane.WARNING_MESSAGE);
				System.out
						.println("WARNING: one of the endpoints of the added edge ("
								+ token.id_source
								+ ","
								+ token.id_dest
								+ ") does not exist");
			}
		} else if (token.type.equalsIgnoreCase("editEdge")) {
			if (hash.containsKey(token.id_source)
					&& hash.containsKey(token.id_dest)) {
				Vertex vSource = hash.get(token.id_source);
				Vertex vDest = hash.get(token.id_dest);
				Edge e;
				if ((e = findEdge(vSource, vDest)) != null)
					editEdge(e, token);
				else
					System.out.println("WARNING: the edited edge ("
							+ token.id_source + "," + token.id_dest
							+ ") didn't exist -- use addEdge");
			} else {
				JOptionPane.showMessageDialog(null,
						"One of the endpoints of the added edge ("
								+ token.id_source + "," + token.id_dest
								+ ") does not exist", "Warning",
						JOptionPane.WARNING_MESSAGE);
				System.out
						.println("WARNING: one of the endpoints of the added edge ("
								+ token.id_source
								+ ","
								+ token.id_dest
								+ ") does not exist");
			}

		} else if (token.type.equalsIgnoreCase("options")) {
			System.out.println("Changing frame label to " + token.label);
			currentLabel = token.label;
			currentSleepTime = token.sleepTime;
			currentMaxStepUpdates = token.maxUpdateSteps;
			for (Observer o : observers)
				o.update(this);
		}
		return;
	}

	public String getCurrentLabel() {
		return currentLabel;
	}

	private void editVertex(Vertex v, Token token) {
		if (token.label != null)
			v.setLabel(token.label);
		if (token.size != null)
			v.setSize(token.size);
		if (token.shape != null)
			v.setShape(token.shape);
		if (token.color != null)
			v.setFillColor(token.color);
		if (token.borderColor != null)
			v.setBorderColor(token.borderColor);
		if (token.position != null)
			v.setPosition(token.position);
		if (token.borderWidth != null)
			v.setWidth(token.borderWidth);
		if (token.var1 != null)
			v.setVar1(token.var1);
		if (token.var2 != null)
			v.setVar2(token.var2);
		v.setExcluded(token.hide);
		return;
	}

	private void editEdge(Edge e, Token token) {
		if (token.weight != null)
			e.setWeight(token.weight);
		if (token.label != null)
			e.setLabel(token.label);
		if (token.size != null)
			e.setWidth(token.size);
		if (token.color != null)
			e.setColor(token.color);
		if (token.var1 != null)
			e.setVar1(token.var1);
		if (token.var2 != null)
			e.setVar2(token.var2);
		e.setExcluded(token.hide);
		return;
	}

	@Override
	public void reset() {
		instructionIndex = 0;
		currentLabel = "";

		reload();
		// resetNetwork(graphFile);

		for (Observer o : observers)
			o.update(this);

		done = false;
	}

	/*
	 * Unused: does not reset the network to the initial (pre-simulation)
	 * network but rather creates an intersection of the simulation network
	 * states
	 */
	@SuppressWarnings("unused")
	private void resetNetwork(File graphFile) {
		try {
			br = new BufferedReader(new FileReader(graphFile));
			ArrayList<Token> edgeTokens = new ArrayList<Token>();
			Token token;

			while ((token = getNextToken()) != null) {
				if (token.type.toLowerCase().contains("node")) {
					Vertex v = createVertex(token);

					if (hash.get(v.getId()) != null) {
						// the node is already in the network, reset the
						// attributes to the
						// ones specified in the cxf file
						Vertex w = hash.get(v.getId());
						w.setBorderColor(v.getBorderColor());
						w.setExcluded(v.isExcluded());
						w.setFillColor(v.getFillColor());
						w.setFixed(v.isFixed());
						w.setLabel(v.getLabel());
						w.setShadowed(v.isShadowed());
						w.setShape(v.getShape());
						w.setSize(v.getSize());
						w.setVar1(v.getVar1());
						w.setVar2(v.getVar2());
						w.setWidth(v.getWidth());
					} else {
						// the node was removed, reinsert it in the network
						addVertex(v);
						// we store the ids and vertices in a hash table
						hash.put(v.getId(), v);
					}
				} else if (token.type.toLowerCase().contains("edge")) {
					edgeTokens.add(token);
				} else if (!token.type.toLowerCase().equalsIgnoreCase(
						"configuration")) {
					if (!confirmFileFormatWarning("Unknown command in line "
							+ token.line, "cxf error"))
						return;
				}
			}

			// remember what edges are currently in the graph
			List<Edge> existingEdges = new LinkedList<Edge>();
			for (Edge e : getEdges()) {
				existingEdges.add(e);
			}

			for (Token t : edgeTokens) {
				// Check if the edge is already there
				Vertex source = hash.get(t.id_source);
				Vertex dest = hash.get(t.id_dest);
				if ((source == null) || (dest == null)) {
					if (!confirmFileFormatWarning(
							"Malformed edge (nonexistent endpoint): ("
									+ t.id_source + "," + t.id_dest
									+ ") in line " + t.line, "cxf error"))
						return;
				} else {
					// check if the edge is already in the graph
					Edge edge = findEdge(source, dest);
					Edge newEdge = createEdge(t);
					if (edge != null) {
						// the edge is there, just reinitialize its attributes
						edge.setColor(newEdge.getColor());
						edge.setExcluded(newEdge.isExcluded());
						edge.setLabel(newEdge.getLabel());
						edge.setShape(newEdge.getShape());
						edge.setVar1(newEdge.getVar1());
						edge.setVar2(newEdge.getVar2());
						edge.setWeight(newEdge.getWeight());
						edge.setWidth(newEdge.getWidth());
						// this edge is processed
						existingEdges.remove(edge);
					} else {
						addEdge(newEdge);
					}
				}
			}
			// now we remove all edges that are in the graph
			// but should not be there
			for (Edge e : existingEdges) {
				removeEdge(e);
			}
			line = null;

		} catch (FileNotFoundException fnfEx) {
			JOptionPane.showMessageDialog(null, fnfEx.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			Cuttlefish.err("Network file not found");
			fnfEx.printStackTrace();
		}

	}

	@Override
	public boolean update(long passedTime) {
		if (!done) {
			Token token = instructionTokens.get(instructionIndex);
			if (token.freeze) {
				execute(token);
				instructionIndex++;
				boolean commited = token.commit;
				while ((instructionIndex < instructionTokens.size())
						&& (!commited)) {
					token = instructionTokens.get(instructionIndex);
					execute(token);
					instructionIndex++;
					commited = token.commit;
				}

			} else if (instructionIndex < instructionTokens.size()) {
				execute(token);
				instructionIndex++;
			}
		}
		done = !(instructionIndex < instructionTokens.size());
		return !done;
	}

	@Override
	public void addObserver(Observer o) {
		observers.add(o);
	}

	@Override
	public void removeObserver(Observer o) {
		observers.remove(o);
	}
}
