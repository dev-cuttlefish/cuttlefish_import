/*

Copyright (C) 2009  Markus Michael Geipel, David Garcia Becerra, Petar Tsankov

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

import java.awt.Color;
import java.awt.HeadlessException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import ch.ethz.sg.cuttlefish.gui2.NetworkInitializer;
import ch.ethz.sg.cuttlefish.misc.Edge;
import ch.ethz.sg.cuttlefish.misc.Vertex;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * Network class that loads data from a database and sets the graph
 * 
 * @author David Garcia Becerra
 */
public class DBNetwork extends BrowsableNetwork {

	private static final long serialVersionUID = 1L;

	protected Connection conn;
	private HashMap<Integer, Vertex> hash = new HashMap<Integer, Vertex>();
	private String nodeFilter = "";
	private String edgeFilter = "";
	private String schemaName = "";
	private boolean directed = true;
	private boolean initialized = false;
	private Set<String> edgeTableRequiredColumns;
	private Set<String> nodeTableRequiredColumns;
	private Set<String> availableNodeTables;
	private Collection<String> networkNames;
	private Map<String, String> networkNodetableMap;
	private Map<String, Set<String>> tableAvailableColumnsMap;
	private String lastAction = "";

	private String edgeTable = "";
	private String nodeTable = "";
	private boolean derivedNodeTable = false;

	/**
	 * DBNetwork constructor. Initializes the edgeTableColumns and
	 * nodeTableColumns
	 */
	public DBNetwork() {
		nodeTableRequiredColumns = new HashSet<String>();
		nodeTableRequiredColumns.add("id");
		edgeTableRequiredColumns = new HashSet<String>();
		tableAvailableColumnsMap = new HashMap<String, Set<String>>();
		edgeTableRequiredColumns.add("id_origin");
		edgeTableRequiredColumns.add("id_dest");
		networkNames = new ArrayList<String>();
		networkNodetableMap = new HashMap<String, String>();
		availableNodeTables = new HashSet<String>();
		networkLoaded = true;
	}

	@Override
	public void graphicalInit(NetworkInitializer initializer) {
		initializer.initDBNetwork(this);
	}

	public boolean nodeTableIsDerived() {
		return derivedNodeTable;
	}

	public Set<String> availableNodeTables() {
		return availableNodeTables;
	}

	/**
	 * Method that connects the network to a specified database with user and
	 * password
	 * 
	 * @param dbName
	 *            name of the database without protocol or connection details
	 * @param userName
	 * @param password
	 */
	public boolean connect(String driverName, String urlName, String schemaName, String dbName, String userName, String password) {
		boolean connected = true;
		if (conn != null)
			disConnect();
		try {
			Class.forName(driverName).newInstance();
			String url = urlName + dbName + "/" + schemaName;
			conn = DriverManager.getConnection(url, userName, password);
			this.schemaName = schemaName;
			boolean isValid = true;
			/**
			 * The postgre JDBC driver does not implement
			 * the isValid method.
			 */
			if(driverName != "org.postgresql.Driver" ){ 
				isValid = (conn.isValid(100));
			}
			if ((conn == null) || !isValid)
				JOptionPane.showMessageDialog(null, null,
						"Error connecting to database " + dbName,
						JOptionPane.ERROR_MESSAGE);
		} catch (ClassNotFoundException cnfEx) {
			connected = false;
			JOptionPane.showMessageDialog(null, cnfEx.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			System.err.println("Class com.mysql.jdc.Driver not found");
			// cnfEx.printStackTrace();
		} catch (IllegalAccessException iaEx) {
			connected = false;
			JOptionPane.showMessageDialog(null, iaEx.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			System.err.println("Illegal access in database connection");
			// iaEx.printStackTrace();
		} catch (InstantiationException iEx) {
			connected = false;
			JOptionPane.showMessageDialog(null, iEx.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			System.err.println("Instantation exception");
			// iEx.printStackTrace();
		} catch (SQLException sqlEx) {
			connected = false;
			JOptionPane.showMessageDialog(null, sqlEx.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			System.err.println("SQL error");
			// sqlEx.printStackTrace();
		} catch (HeadlessException hEx) {
			connected = false;
			JOptionPane.showMessageDialog(null, hEx.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			System.err.println("SQL error");
			// hEx.printStackTrace();
		}
		if (connected) {
			getDirection();
			getNetworkNames(schemaName);
		}
		return connected;
	}

	
	/**
	 * Public method that returns the database schema name of the connection.
	 * 
	 * @return - The schema name
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * This method reads the mata data of the database and returns a collection
	 * of network names.
	 */
	public Collection<String> getNetworkNames(String schemaName) {
		List<String> edgeTables = new ArrayList<String>();
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet tablesResultSet = metaData.getTables(null, schemaName,
					null, null);
			while (tablesResultSet.next()) {
				Set<String> currentTableAvailableColumns = new HashSet<String>();
				String tableName = tablesResultSet.getString("TABLE_NAME");
				if (tableAvailableColumnsMap.containsKey(tableName))
					continue;
				Set<String> currentEdgeTableRequiredColumns = new HashSet<String>(
						edgeTableRequiredColumns);
				Set<String> currentNodeTableRequiredColumns = new HashSet<String>(
						nodeTableRequiredColumns);
				ResultSet columnsResultSet = metaData.getColumns(null,
						schemaName, tableName, null);
				while (columnsResultSet.next()) {
					String columnName = columnsResultSet
							.getString("COLUMN_NAME");
					currentTableAvailableColumns.add(columnName);
					if (currentEdgeTableRequiredColumns.contains(columnName))
						currentEdgeTableRequiredColumns.remove(columnName);
					if (currentNodeTableRequiredColumns.contains(columnName))
						currentNodeTableRequiredColumns.remove(columnName);
				}
				if (currentEdgeTableRequiredColumns.isEmpty()) {
					edgeTables.add(tableName);
					tableAvailableColumnsMap.put(tableName,
							currentTableAvailableColumns);
				}
				if (currentNodeTableRequiredColumns.isEmpty()) {
					availableNodeTables.add(tableName);
					tableAvailableColumnsMap.put(tableName,
							currentTableAvailableColumns);
				}
			}

			// Try to match an edge table to a node table
			for (String edgeTable : edgeTables) {
				if (networkNodetableMap.containsKey(edgeTable))
					continue;
				String matchedNodeTable = null;
				for (String nodeTable : availableNodeTables) {
					ResultSet foreignKeysResultSet = metaData
							.getCrossReference(null, schemaName, nodeTable,
									null, schemaName, edgeTable);
					boolean destFK = false;
					boolean originFK = false;
					while (foreignKeysResultSet.next()) {
						String fk = foreignKeysResultSet
								.getString("FKCOLUMN_NAME");
						String pk = foreignKeysResultSet
								.getString("PKCOLUMN_NAME");
						if (pk.compareTo("id") == 0) {
							if (fk.compareTo("id_origin") == 0)
								originFK = true;
							if (fk.compareTo("id_dest") == 0)
								destFK = true;
						}
					}
					if (destFK && originFK) {
						matchedNodeTable = nodeTable;
						break;
					}
				}

				if (matchedNodeTable != null) {
					networkNodetableMap.put(edgeTable, matchedNodeTable);
				}
				networkNames.add(edgeTable);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		return networkNames;
	}

	public void setNetwork(String networkName) {
		edgeTable = networkName;
		if (networkNodetableMap.containsKey(networkName)) {
			nodeTable = networkNodetableMap.get(networkName);
			derivedNodeTable = false;
		} else {
			// If no node table exists, then use a derived table
			// that contains the IDs of the nodes
			derivedNodeTable = true;
			nodeTable = "(SELECT DISTINCT id_origin AS id FROM " + edgeTable
					+ " UNION SELECT DISTINCT id_dest AS id FROM " + edgeTable
					+ ") derived_node_table";
		}
	}

	public boolean isView(String tableName) {
		boolean isView = false;
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet rs = metaData
					.getTables(null, schemaName, tableName, null);
			while (rs.next()) {
				if (rs.getString("TABLE_TYPE").compareToIgnoreCase("VIEW") == 0) {
					isView = true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isView;
	}

	/**
	 * Returns the name of the nodes table
	 * 
	 * @return
	 */
	public String getNodeTable() {
		return nodeTable;
	}
	
	/**
	 * Returns the name of the nodes table, prepended with the schema name
	 */
	public String getFullNodeTableName() {
		return schemaName+"."+nodeTable;
	}

	/**
	 * Returns the name of the edge table name
	 * 
	 * @return
	 */
	public String getEdgeTable() {
		return edgeTable;
	}
	
	/**
	 * Returns the name of the edge table name, prepended with the schema name
	 * 
	 * @return
	 */
	public String getFullEdgeTableName() {
		return schemaName+"."+edgeTable;
	}

	/**
	 * Method that queries the database to determine whether the network should
	 * be directed or not
	 */
	private void getDirection() {
		if (!initialized) {
			try {
				String queryString = "select * from Directed;";
				Statement st;
				st = conn.createStatement();
				ResultSet rs = st.executeQuery(queryString);
				rs.next();
				directed = rs.getBoolean(1);
			} catch (SQLException e) {
				initialized = true;
				directed = true; // if no view is defined, is directed by
									// default
			}
			initialized = true;
		}

	}

	/**
	 * Method that closes the connection to the database, if existed
	 */
	public void disConnect() {
		try {
			conn.close();
		} catch (SQLException sqlEx) {
			JOptionPane.showMessageDialog(null, sqlEx.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			System.err.println("SQL error");
			sqlEx.printStackTrace();
		}
	}

	/**
	 * Method that disposes all nodes and edges of the current network
	 */
	public void emptyNetwork() {
		hash.clear();
		clearGraph();

	}

	/**
	 * Setter for the filter on the queries for the nodes
	 * 
	 * @param nodeFilter
	 */
	public void setNodeFilter(String nodeFilter) {
		this.nodeFilter = nodeFilter;
	}

	/**
	 * Setter for the filter on the queries for the edges of the database
	 */
	public void setEdgeFilter(String edgeFilter) {
		this.edgeFilter = edgeFilter;
	}

	/**
	 * Function that applies certain filter to any query, transforming it to a
	 * filtered query
	 * 
	 * @param query
	 *            to modify
	 * @param filter
	 *            to add to the query
	 * @return extended query
	 */
	private String applyFilter(String query, String filter) {
		if (filter.equals(""))
			return query;
		int whereIndex = query.lastIndexOf("where");
		if (whereIndex == -1)
			whereIndex = query.lastIndexOf("WHERE");

		if (whereIndex == -1)
			return query + " where " + filter;

		String result = query.substring(0, whereIndex + 5);
		String whereClause = query.substring(whereIndex + 5);
		result = result + " " + filter + " and " + whereClause;
		return result;
	}

	/**
	 * Checks if the provided node id exists in the database
	 */
	public boolean checkNodeId(String nodeId) {
		String queryString = "SELECT * FROM " + schemaName+"."+nodeTable + " WHERE id = '"
				+ nodeId + "'";
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(queryString);
			int count = 0;
			//there should be at most 1 row since the id is a primary key
			while(rs.next())
				count++;
			if (count == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {			
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Executes a proper node query and adds all the resulting nodes to the
	 * network
	 * 
	 * @param queryString
	 */
	public void nodeQuery(String queryStringOriginal) {
		lastAction = "exploreNetwork";
		syncHashTable();
		Set<Vertex> visitedVertices = new HashSet<Vertex>();
		try {
			String queryString = applyFilter(queryStringOriginal, nodeFilter);
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(queryString);
			while (rs.next()) {
				int id = rs.getInt("id");
				Vertex v = new Vertex(id);				
				if (!derivedNodeTable) {
					if (tableAvailableColumnsMap.get(nodeTable).contains( "label")) {
						String label = rs.getString("label");
						if (label != null) { v.setLabel(label); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("color")) {
						String colorString = rs.getString("color");
						if (colorString != null) {
							int pos = 0;
							float R, G, B;
							R = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',', pos)));
							pos = colorString.indexOf(',', pos) + 1;
							G = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',', pos)));
							pos = colorString.indexOf(',', pos) + 1;
							B = Float.parseFloat(colorString.substring(pos));
							v.setFillColor(new Color(R, G, B));
						}
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("borderColor")) {
						String borderColorString = rs.getString("borderColor");
						if (borderColorString != null) {
							int pos = 0;
							float R, G, B;
							R = Float.parseFloat(borderColorString.substring(pos, borderColorString.indexOf(',', pos)));
							pos = borderColorString.indexOf(',', pos) + 1;
							G = Float.parseFloat(borderColorString.substring(pos, borderColorString.indexOf(',', pos)));
							pos = borderColorString.indexOf(',', pos) + 1;
							B = Float.parseFloat(borderColorString.substring(pos));
							v.setColor(new Color(R, G, B));
						}
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("size")) {
						float size = rs.getFloat("size");
						if (size != 0) { v.setSize(size); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("shape")) {
						String shape = rs.getString("shape");
						if (shape != null) { v.setShape(shape); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("width")) {
						int width = rs.getInt("width");
						if (width != 0) { v.setWidth(width); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("var1")) {
						String var1 = rs.getString("var1");
						if (var1 != null) { v.setVar1(var1); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("var2")) {
						String var2 = rs.getString("var2");
						if (var2 != null) { v.setVar2(var2); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("x") && tableAvailableColumnsMap.get(nodeTable).contains("y")) {
						Double x = rs.getDouble("x");
						Double y = rs.getDouble("y");
						if ((x != null) && (y != null)) { v.setPosition(x, y); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("fixed")) {
						v.setFixed(rs.getBoolean("fixed"));
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("hide")) {
						boolean hide = false;
						if (rs.getString("hide") != null) {hide = rs.getBoolean("hide"); }
						v.setExcluded(hide);
					}
				}
				if(hash.containsKey(v.getId())) {
					//the node is there, simply update the attributes if they are changed
					updateVertexAttributes(v);
					v = hash.get(v.getId());
				} else {
					addVertex(v);
					hash.put(id, v);	
				}
				visitedVertices.add(v);
			}
		} catch (SQLException sqlEx) {
			JOptionPane.showMessageDialog(null, sqlEx.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			System.err.println("SQL error");
			sqlEx.printStackTrace();
		}
		// remove vertices that are not visited, i.e. not present in the
		// database anymore
		// add them to a set to avoid concurrent modification exception
		Set<Vertex> verticesToRemove = new HashSet<Vertex>();
		for(Vertex v : getVertices()) {
			if(!visitedVertices.contains(v)) {
				verticesToRemove.add(v);
			}
		}
		for(Vertex v : verticesToRemove) {
			removeVertex(v);
			System.out.println("Removing " + v.getId());
			hash.remove(v.getId());
		}
		extendEdges();
	}

	public String getLastAction() {
		return lastAction;
	}
	
	/**
	 * Method that adds to the network the neighboring nodes up to certain
	 * distance from a center.
	 * 
	 * @param id
	 *            from the node to start the search from
	 * @param distance
	 *            maximum depth
	 * @param forward
	 *            whether the traversal is forward or backward
	 */
	public void extendNeighborhood(int id, int distance, boolean forward, Set<Vertex> visitedVertices, Set<Edge> visitedEdges) {
		lastAction = "exploreNode";
		// sync the hash table storing the nodes
		boolean firstNode = false;
		if(visitedVertices.size() == 0) {
			firstNode = true;
		}
		syncHashTable();
		
		Vertex v = new Vertex(id);;		
		// first read the vertex
		try {
			if (!derivedNodeTable) {
				String queryString = "select * from " + schemaName+"."+nodeTable + " where id = " + id + ";";
				queryString = applyFilter(queryString, nodeFilter);
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(queryString);
				while (rs.next()) {
					if (tableAvailableColumnsMap.get(nodeTable).contains("label")) {
						v.setLabel(rs.getString("label"));
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("color")) {
						String colorString = rs.getString("color");
						if (colorString != null) {
							int pos = 0;
							float R, G, B;
							R = Float.parseFloat(colorString.substring(pos, colorString.indexOf(',', pos)));
							pos = colorString.indexOf(',', pos) + 1;
							G = Float.parseFloat(colorString.substring(pos, colorString.indexOf(',', pos)));
							pos = colorString.indexOf(',', pos) + 1;
							B = Float.parseFloat(colorString.substring(pos));
							v.setFillColor(new Color(R, G, B));
						}
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("borderColor")) {
						String borderColorString = rs.getString("borderColor");
						if (borderColorString != null) {
							int pos = 0;
							float R, G, B;
							R = Float.parseFloat(borderColorString.substring(pos, borderColorString.indexOf(',', pos)));
							pos = borderColorString.indexOf(',', pos) + 1;
							G = Float.parseFloat(borderColorString.substring(pos, borderColorString.indexOf(',', pos)));
							pos = borderColorString.indexOf(',', pos) + 1;
							B = Float.parseFloat(borderColorString.substring(pos));
							v.setColor(new Color(R, G, B));
						}
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("size")) {
						float size = rs.getFloat("size");
						if (size != 0) { v.setSize(size); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("shape")) {
						String shape = rs.getString("shape");
						if (shape != null) { v.setShape(shape); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("width")) {
						int width = rs.getInt("width");
						if (width != 0) { v.setWidth(width); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("var1")) {
						String var1 = rs.getString("var1");
						if (var1 != null) { v.setVar1(var1); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("var2")) {
						String var2 = rs.getString("var2");
						if (var2 != null) { v.setVar2(var2); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("hide")) {
						boolean hide = false;
						if (rs.getString("hide") != null) { hide = rs.getBoolean("hide"); }
						v.setExcluded(hide);
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("x") && tableAvailableColumnsMap.get(nodeTable).contains("x")) {
						Double x = rs.getDouble("x");
						Double y = rs.getDouble("y");
						if ((x != null) && (y != null)) { v.setPosition(x, y); }
					}
					if (tableAvailableColumnsMap.get(nodeTable).contains("fixed")) {
						boolean fixed = rs.getBoolean("fixed");
						v.setFixed(fixed);
					}
				}
			}			
		} catch (SQLException sqlEx) {
			JOptionPane.showMessageDialog(null, sqlEx.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			System.err.println("Node not found");
			sqlEx.printStackTrace();
		}
		//check if we need to add the node or simply update its attributes
		if(hash.containsKey(v.getId())) {
			// the node is there, just update the attributes
			updateVertexAttributes(v);
			v = hash.get(v.getId());
		} else {
			// we need to add the node
			hash.put(v.getId(), v);
			addVertex(v);
		}
		visitedVertices.add(v);
		
		if (forward == true && distance > 0) {
			try {
				String queryString = "select * from " + schemaName+"."+edgeTable + " where id_origin =" + v.getId() + ";";
				queryString = applyFilter(queryString, edgeFilter);
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(queryString);
				while (rs.next()) {
					int id_dest = rs.getInt("id_dest");
					// we should check if the node was already explored, otherwise
					// a loop would circle around unnecessarily
					if(hash.get(id_dest) == null || !visitedVertices.contains(hash.get(id_dest))) {
						extendNeighborhood(id_dest, distance - 1, true, visitedVertices, visitedEdges);
					}
					Edge e = new Edge();
					if (tableAvailableColumnsMap.get(edgeTable).contains("label")) {
						e.setLabel(rs.getString("label"));
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("color")) {
						String colorString = rs.getString("color");
						if (colorString != null) {
							int pos = 0;
							float R, G, B;
							R = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',', pos)));
							pos = colorString.indexOf(',', pos) + 1;
							G = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',', pos)));
							pos = colorString.indexOf(',', pos) + 1;
							B = Float.parseFloat(colorString.substring(pos));
							e.setColor(new Color(R, G, B));
						}
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("width")) {
						int width = rs.getInt("width");
						if (width != 0) { e.setWidth(width); }
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("weight")) {
						float weight = rs.getFloat("weight");
						if (weight != 0) { e.setWeight(weight); }
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("var1")) {
						String var1 = rs.getString("var1");
						if (var1 != null) { e.setVar1(var1); }
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("var2")) {
						String var2 = rs.getString("var2");
						if (var2 != null) { e.setVar2(var2); }
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("hide")) {
						boolean hide = false;
						if (rs.getString("hide") != null) { hide = rs.getBoolean("hide"); }
						e.setExcluded(hide);
					}
					Edge edge = findEdge(id, id_dest);
					if(edge != null) {
						updateEdgeAttributes(e, edge);
						e = edge;
					} else {
						if (directed) {						
							addEdge(e, v, hash.get(id_dest), EdgeType.DIRECTED);							
						} else {						
							addEdge(e, v, hash.get(id_dest), EdgeType.UNDIRECTED);
						}	
					}
					visitedEdges.add(e);
				}
			} catch (SQLException sqlEx) {
				JOptionPane.showMessageDialog(null, sqlEx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				System.err.println("SQL error");
				sqlEx.printStackTrace();
			}
		}
		if ((forward == false) && (distance > 0)) {
			try {
				String queryString = "select * from " + schemaName+"."+edgeTable + " where id_dest =" + v.getId() + ";";
				queryString = applyFilter(queryString, edgeFilter);
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(queryString);
				while (rs.next()) {
					int id_origin = rs.getInt("id_origin");
					if(hash.get(id_origin) == null || !visitedVertices.contains(hash.get(id_origin))) {
						extendNeighborhood(id_origin, distance - 1, false, visitedVertices, visitedEdges);
					}					
					Edge e = new Edge();
					if (tableAvailableColumnsMap.get(edgeTable).contains("label")) {
						e.setLabel(rs.getString("label"));
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("color")) {
						String colorString = rs.getString("color");
						if (colorString != null) {
							int pos = 0;
							float R, G, B;
							R = Float.parseFloat(colorString.substring(pos, colorString.indexOf(',', pos)));
							pos = colorString.indexOf(',', pos) + 1;
							G = Float.parseFloat(colorString.substring(pos, colorString.indexOf(',', pos)));
							pos = colorString.indexOf(',', pos) + 1;
							B = Float.parseFloat(colorString.substring(pos));
							e.setColor(new Color(R, G, B));
						}
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("width")) {
						int width = rs.getInt("width");
						if (width != 0) { e.setWidth(width); }
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("weight")) {
						float weight = rs.getFloat("weight");
						if (weight != 0) { e.setWeight(weight); }
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("var1")) {
						String var1 = rs.getString("var1");
						if (var1 != null) { e.setVar1(var1); }
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("var2")) {
						String var2 = rs.getString("var2");
						if (var2 != null) { e.setVar2(var2); }
					}
					if (tableAvailableColumnsMap.get(edgeTable).contains("hide")) {
						boolean hide = false;
						if (rs.getString("hide") != null) {
							hide = rs.getBoolean("hide");
						}
						e.setExcluded(hide);
					}
					Edge edge = findEdge(id_origin, id);
					if(edge != null) {
						updateEdgeAttributes(e, edge);
						e = edge;
					} else {
						if (directed) {						
							addEdge(e, hash.get(id_origin), v, EdgeType.DIRECTED);							
						} else {						
							addEdge(e, hash.get(id_origin), v, EdgeType.UNDIRECTED);
						}	
					}
					visitedEdges.add(e);				
				}
			} catch (SQLException sqlEx) {
				JOptionPane.showMessageDialog(null, sqlEx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				System.err.println("SQL error");
				sqlEx.printStackTrace();
			}
		}
		// here the first node cleans up.. i.e. removes nodes and edges if
		// they were never visited
		if(firstNode) {
			Set<Edge> edgesToRemove = new HashSet<Edge>();
			Set<Vertex> verticesToRemove = new HashSet<Vertex>();
			for(Edge e : getEdges()) {
				if(!visitedEdges.contains(e)) { edgesToRemove.add(e); }
			}
			for(Edge e : edgesToRemove) { removeEdge(e); }
			for(Vertex w : getVertices()) {
				if(!visitedVertices.contains(w)) { verticesToRemove.add(w); }
			}
			for(Vertex w : verticesToRemove) { removeVertex(w); }
		}
	}

	private void syncHashTable() {
		if(hash.size() != getVertexCount() ) {			
			Set<Integer> toRemove = new HashSet<Integer>(hash.keySet());
			for(Vertex v : getVertices()) {
				toRemove.remove(v.getId());
			}
			for(Integer i : toRemove) {
				hash.remove(i);
			}
		}
	}

	private void updateVertexAttributes(Vertex v) {
		Vertex n = hash.get(v.getId());
		if(n.isExcluded() != v.isExcluded()) { n.setExcluded(v.isExcluded()); }
		if(n.isFixed() != v.isFixed()) { n.setFixed(v.isFixed()); }
		if(n.getColor() != v.getColor()) { n.setColor(v.getColor()); }
		if(n.getFillColor() != v.getFillColor()) { n.setFillColor(v.getFillColor()); }
		if(n.getLabel() != v.getLabel()) { n.setLabel(v.getLabel()); }
		if(n.getPosition() != v.getPosition()) { n.setPosition(v.getPosition()); }
		if(n.getShape() != v.getShape()) { n.setShape(v.getShape()); }
		if(n.getSize() != v.getSize()) { n.setSize(v.getSize()); }
		if(n.getWidth() != v.getWidth()) { n.setWidth(v.getWidth()); }
		if(n.getVar1() != v.getVar1()) { n.setVar1(v.getVar1()); }
		if(n.getVar2() != v.getVar2()) { n.setVar2(v.getVar2()); }
	}

	private void updateEdgeAttributes(Edge fromEdge, Edge toEdge) {
		if( toEdge.getLabel() != fromEdge.getLabel()) { toEdge.setLabel(fromEdge.getLabel()); }
		if( toEdge.getColor() != fromEdge.getColor()) { toEdge.setColor(fromEdge.getColor()); }
		if( toEdge.getShape() != fromEdge.getShape()) { toEdge.setShape(fromEdge.getShape()); }
		if( toEdge.getVar1() != fromEdge.getVar1()) { toEdge.setVar1(fromEdge.getVar1()); }
		if( toEdge.getVar2() != fromEdge.getVar2()) { toEdge.setVar2(fromEdge.getVar2()); }
		if( toEdge.getWeight() != fromEdge.getWeight()) { toEdge.setWeight(fromEdge.getWeight()); }
		if( toEdge.getWidth() != fromEdge.getWidth()) { toEdge.setWidth(fromEdge.getWidth()); }
	}
	
	private Edge findEdge(int id_orig, int id_dest) {
		Edge edge = null;
		if (directed) {			
			for(Edge cur_edge : getOutEdges(hash.get(id_orig))) {
				if(getDest(cur_edge) != null && getDest(cur_edge) == hash.get(id_dest)) {
					edge = cur_edge;
					break;
				}
			}
		} else {
			for(Edge cur_edge : getIncidentEdges(hash.get(id_orig)) ) {
				if(getEndpoints(cur_edge).getSecond() == hash.get(id_dest)) {
					edge = cur_edge;
					break;
				}
			}
		}
		return edge;
	}

	/**
	 * Method that counts the number of nodes that match the selected nodes
	 * filter
	 * 
	 * @return
	 */
	public Set<Integer> getSelectedNodes() {
		String sqlQuery = "SELECT id FROM " + schemaName+"."+nodeTable;
		sqlQuery = applyFilter(sqlQuery, nodeFilter);
		Statement st;
		Set<Integer> selectedNodes = new HashSet<Integer>();
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(sqlQuery);
			while (rs.next()) {
				selectedNodes.add(rs.getInt("id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return selectedNodes;
	}
	
	/**
	 * Counts the number of nodes that match the edge origin filter
	 */
	public Set<Integer> getOriginNodes() {
		return getEdgeFilteredNodes("origin");
	}
	
	/**
	 * Counts the number of nodes that match the edge destination filter
	 */
	public Set<Integer> getDestNodes() {
		return getEdgeFilteredNodes("dest");
	}
	
	/**
	 * Returns the set of nodes that match an origin or destination filter, or null if no such filter is specified
	 * @param type Specifies "origin" or "dest" node filter
	 * @return The set of matched nodes, null if there is no specified filter that matches 'type' 
	 */
	private Set<Integer> getEdgeFilteredNodes(String type) {
		Set<Integer> nodeSet = new HashSet<Integer>();
		String sqlQuery = "SELECT id FROM " + schemaName+"."+nodeTable;
		Statement st;
		ResultSet rs;
		String idField = "id_"+type; // Either "id_origin" or "id_dest"
		
		if(edgeFilter.contains(idField)) {
			String filter = edgeFilter;
			
			if(filter.contains(" AND ")) {
				for(String s: filter.split(" AND ")) {
					if (s.contains(idField)) {
						filter = s;
						break;
					}
				}
			}
			sqlQuery = applyFilter(sqlQuery, filter.replaceAll(idField, "id"));
			
			try {
				st = conn.createStatement();
				rs = st.executeQuery(sqlQuery);
				while (rs.next()) {
					nodeSet.add(rs.getInt("id"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return nodeSet;
		}
		
		return null;
	}

	/**
	 * This method takes a set of nodes as an input and returns a set of
	 * reachable nodes, i.e., all nodes that have an incoming edge starting from
	 * a node in the input set.
	 * 
	 * @param nodes
	 * @return
	 */
	public Set<Integer> reachableNeighbors(Set<Integer> nodes) {
		Set<Integer> reachable = new HashSet<Integer>();
		StringBuilder nodesList = new StringBuilder("(");
		for (int nodeId : nodes) {
			nodesList.append(Integer.toString(nodeId) + ',');
		}
		nodesList.setCharAt(nodesList.length() - 1, ')');
		String sqlQuery = "SELECT id_dest FROM " + schemaName+"."+edgeTable
				+ " WHERE id_origin IN " + nodesList;
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(sqlQuery);
			while (rs.next()) {
				reachable.add(rs.getInt("id_dest"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reachable;
	}

	/**
	 * This method counts the number of edges starting with an origin in the set
	 * of origin nodes and with a destination in the destination nodes list
	 * 
	 * @param originNodes
	 *            - The set of origin nodes
	 * @param destNodes
	 *            - The set of destination nodes
	 * @return The number of edges
	 */
	public int countEdges(Set<Integer> originNodes, Set<Integer> destNodes) {
		/*
		 * if the sets of origin or destination nodes is zero, then the
		 * set of edges is definitely 0.
		 */
		if(originNodes.size() == 0 && destNodes.size() == 0)
			return 0;
		
		StringBuilder originNodesList = new StringBuilder("(");		
		for (int nodeId : originNodes) {
			originNodesList.append(Integer.toString(nodeId) + ',');
		}
		originNodesList.setCharAt(originNodesList.length() - 1, ')');
		StringBuilder destNodesList = new StringBuilder("(");
		for (int nodeId : destNodes) {
			destNodesList.append(Integer.toString(nodeId) + ',');
		}
		destNodesList.setCharAt(destNodesList.length() - 1, ')');
		String sqlQuery = "SELECT count(*) as edgeCount FROM " + schemaName+"."+edgeTable
				+ " WHERE id_origin IN " + originNodesList + " AND id_dest IN "
				+ destNodesList;
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(sqlQuery);
			rs.next();
			return rs.getInt("edgeCount");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Method that adds all the edges of the subgraph induced in the database by
	 * the nodes already added to the displayed network
	 */
	public void extendEdges() {
		Set<Edge> visitedEdges = new HashSet<Edge>();
		for (Vertex v : getVertices()) {
			try {
				String queryString = "select * from " + schemaName+"."+edgeTable + " where id_origin =" + v.getId() + ";";
				queryString = applyFilter(queryString, edgeFilter);
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(queryString);
				while (rs.next()) {
					int id_dest = rs.getInt("id_dest");
					Vertex v_dest = hash.get(id_dest);
					if (v_dest != null) {
						Edge e = new Edge();

						if (tableAvailableColumnsMap.get(edgeTable).contains("label")) {
							e.setLabel(rs.getString("label"));
						}
						if (tableAvailableColumnsMap.get(edgeTable).contains("color")) {
							String colorString = rs.getString("color");
							if (colorString != null) {
								int pos = 0;
								float R, G, B;
								R = Float.parseFloat(colorString.substring(pos, colorString.indexOf(',', pos)));
								pos = colorString.indexOf(',', pos) + 1;
								G = Float.parseFloat(colorString.substring(pos, colorString.indexOf(',', pos)));
								pos = colorString.indexOf(',', pos) + 1;
								B = Float.parseFloat(colorString.substring(pos));
								e.setColor(new Color(R, G, B));
							}
						}
						if (tableAvailableColumnsMap.get(edgeTable).contains("width")) {
							int width = rs.getInt("width");
							if (width != 0) { e.setWidth(width); }
						}
						if (tableAvailableColumnsMap.get(edgeTable).contains("weight")) {
							float weight = rs.getFloat("weight");
							if (weight != 0) { e.setWeight(weight); }
						}
						if (tableAvailableColumnsMap.get(edgeTable).contains("var1")) {
							String var1 = rs.getString("var1");
							if (var1 != null) { e.setVar1(var1); }
						}
						if (tableAvailableColumnsMap.get(edgeTable).contains("var2")) {
							String var2 = rs.getString("var2");
							if (var2 != null) { e.setVar2(var2); }
						}
						if (tableAvailableColumnsMap.get(edgeTable).contains("hide")) {
							boolean hide = false;
							if (rs.getString("hide") != null) { hide = rs.getBoolean("hide"); }
							e.setExcluded(hide);
						}
						Edge edge = findEdge(v.getId(), v_dest.getId());
						if(edge != null) {
							updateEdgeAttributes(e, edge);
						} else {
							if (directed) {
								addEdge(e, v, v_dest, EdgeType.DIRECTED);
							} else {
								addEdge(e, v, v_dest, EdgeType.UNDIRECTED);
							}
							edge = e;
						}
						visitedEdges.add(edge);
					}
				}
			} catch (SQLException sqlEx) {
				JOptionPane.showMessageDialog(null, sqlEx.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
				System.err.println("SQL error");
				sqlEx.printStackTrace();
			}

		}
		//clean up edges that were not visited
		Set<Edge> edgesToRemove = new HashSet<Edge>();
		for(Edge e : getEdges()) {
			if(!visitedEdges.contains(e)) { edgesToRemove.add(e); }
		}
		for(Edge e : edgesToRemove) { removeEdge(e); }
	}

	/**
	 * Method that substracts the neighborhood from a selected vertex
	 * 
	 * @param vertex
	 */
	public void shrinkVertex(Vertex vertex) {
		for (Edge adjacentEdge : getOutEdges(vertex)) {
			Vertex neighbor = getOpposite(vertex, adjacentEdge);
			removeEdge(adjacentEdge);
			if (getNeighborCount(neighbor) < 1) {
				hash.put(neighbor.getId(), null);
				removeVertex(neighbor);
			}
		}
	}

	/**
	 * Method that substracts the predecessors of a selected vertex
	 * 
	 * @param vertex
	 */
	public void backShrinkVertex(Vertex vertex) {
		for (Edge adjacentEdge : getInEdges(vertex)) {
			Vertex neighbor = getOpposite(vertex, adjacentEdge);
			removeEdge(adjacentEdge);
			if (getNeighborCount(neighbor) < 1) {
				hash.put(neighbor.getId(), null);
				removeVertex(neighbor);
			}
		}
	}

	/**
	 * This method checks if Cuttlefish is connected to a database
	 * 
	 * @throws SQLException
	 */
	public boolean isConnected() {
		try {
			return conn != null && !conn.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setNodeTable(String selectedNodeTable) {
		derivedNodeTable = false;
		nodeTable = selectedNodeTable;
	}

}
