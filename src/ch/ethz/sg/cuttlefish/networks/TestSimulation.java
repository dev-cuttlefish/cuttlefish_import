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

import java.util.Collection;
import java.util.ConcurrentModificationException;


public class TestSimulation extends BrowsableNetwork implements ISimulation {

	private static final long serialVersionUID = 1L;
	private Vertex lastInsert = null;
	
	public TestSimulation() {
		
	}
	
	public void reset() {
		setIncremental(true);
		lastInsert = null;
		Collection<Edge> edges = getEdges();
		while(!edges.isEmpty()) {
			try {
				removeEdge(edges.iterator().next());
			}
			catch (ConcurrentModificationException e)
			{}
			edges = getEdges();
		}

		Collection<Vertex> vertices = getVertices();
		while (!vertices.isEmpty())
		{ 
			try{
				removeVertex(vertices.iterator().next());
			}
			catch (ConcurrentModificationException e)
			{}
			vertices = getVertices();
		}
	}

	public boolean update(long passedTime) {
		setIncremental(true);
		Vertex v = new Vertex();
		addVertex(v);
		if(lastInsert!=null){
			Edge e = new Edge(v, lastInsert);
			super.addEdge(e);
		}
		lastInsert = v;
		return (getVertexCount() < 30);
	}
}