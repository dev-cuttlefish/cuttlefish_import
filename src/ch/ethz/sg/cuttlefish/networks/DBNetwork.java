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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import ch.ethz.sg.cuttlefish.gui.NetworkInitializer;
import ch.ethz.sg.cuttlefish.misc.Edge;
import ch.ethz.sg.cuttlefish.misc.Vertex;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
* Network class that loads data from a database and sets the graph
* @author David Garcia Becerra
*/
public class DBNetwork extends BrowsableNetwork {

	private static final long serialVersionUID = 1L;
	
	private Connection conn;
	private HashMap<Integer,Vertex> hash = new HashMap<Integer,Vertex>();
	private String nodeFilter = "";
	private String edgeFilter = "";
	private String schemaName = "";
	private boolean directed = true;
	private boolean initialized = false;
	private Collection<String> edgeTableColumns;
	private Collection<String> nodeTableColumns;

	private String edgeTable = "";
	private String nodeTable = "";
	
	/**
	 * DBNetwork constructor.
	 * Initializes the edgeTableColumns and nodeTableColumns
	 */
	public DBNetwork() {
		//  nodeTableColumns = new ArrayList<String>() {{ add("id"); add("label"); add("color");
		//	add("borderColor"); add("size"); add("shape"); add("width"); add("hide");
		//	add("var1"); add("var2"); add("x"); add("y"); add("fixed"); }};
		nodeTableColumns = new ArrayList<String>() {{ add("id"); }};
		//  edgeTableColumns = new ArrayList<String>() {{ add("id_origin"); add("id_dest"); add("weight"); 
		//	add("label"); add("width"); add("color"); add("var1"); add("var2"); add("hide"); }};
		edgeTableColumns = new ArrayList<String>() {{ add("id_origin"); add("id_dest"); }};
	}
	

	@Override
	public void graphicalInit(NetworkInitializer initializer) {
		initializer.initDBNetwork(this);
	}
	
	/**
	 * Setter for the node table
	 * @param nodeTable
	 */
	public void setNodeTable(String nodeTable) {
		this.nodeTable = nodeTable;
	}
	
	/**
	 * Setter for the edge table
	 * @param edgeTable
	 */
	public void setEdgeTable(String edgeTable) {
		this.edgeTable = edgeTable;
	}
	
	/**
	 * Method that connects the network to a specified database with user and password
	 * @param dbName name of the database without protocol or connection details
	 * @param userName
	 * @param password
	 */
	public boolean connect(String dbName, String userName, String password) {
		boolean connected = true;
		if (conn != null)
			disConnect();
		try
		{
		  Class.forName("com.mysql.jdbc.Driver").newInstance();
		  String url = "jdbc:mysql://" + dbName;
		  conn = DriverManager.getConnection(url, userName, password);
		  if ((conn == null) || (!conn.isValid(100)))
				JOptionPane.showMessageDialog(null,null,"Error connecting to database "+ dbName,JOptionPane.ERROR_MESSAGE);
		}
		catch (ClassNotFoundException cnfEx) {
			connected = false;
			JOptionPane.showMessageDialog(null,cnfEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			System.err.println("Class com.mysql.jdc.Driver not found");
			//cnfEx.printStackTrace();			
			}
		catch (IllegalAccessException iaEx) {
			connected = false;
			JOptionPane.showMessageDialog(null,iaEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			System.err.println("Illegal access in database connection");
			//iaEx.printStackTrace();
		}
		catch (InstantiationException iEx) {
			connected = false;
			JOptionPane.showMessageDialog(null,iEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			System.err.println("Instantation exception");
			//iEx.printStackTrace();
		}
		catch (SQLException sqlEx) {
			connected = false;
			JOptionPane.showMessageDialog(null,sqlEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			System.err.println("SQL error");
			//sqlEx.printStackTrace();
		}	
		catch (HeadlessException hEx) {
			connected = false;
			JOptionPane.showMessageDialog(null,hEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			System.err.println("SQL error");
			//hEx.printStackTrace();
		}
		getDirection();
		schemaName = dbName.substring(dbName.indexOf('/')+1);
		getNodeTables(schemaName);
		getEdgeTables(schemaName);
		System.out.println("Successfully connected to: " + dbName);
		return connected;
	}
	
	/**
	 * Public method that returns the database schema name of the connection.
	 * @return - The schema name
	 */
	public String getSchemaName() {
		return schemaName;
	}
	
	/**
	 * This private method reads the database meta-data and returns a list of
	 * tables that matched the description provided in the columnNames collection.
	 * @param schemaName - The database schema
	 * @param columnNames - Collection of the column names
	 * @return - Collection of tables that match the column names 
	 */
	private Collection<String> getTables(String schemaName, Collection<String> columnNames) {
		String queryString = "select table_name, count(column_name) as matched_columns from information_schema.columns where table_schema='" + schemaName + "' and (";
		int columnCount = 0;
		for(String columnName : columnNames) {
			columnCount++;
			queryString += "column_name='" + columnName + "'";
			if(columnCount < columnNames.size() )
				queryString += " or ";
		}
		queryString += ") group by table_name having matched_columns=" + columnNames.size() + ";";		
		ArrayList<String> tables = new ArrayList<String>();
		try {
			System.out.println(queryString);
	      	Statement st;
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(queryString);
			while(rs.next())
				tables.add(rs.getString("table_name") );
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tables;
	}
	
	/**
	 * This method reads the database meta-data and returns a list of tables that match the
	 * format of a Cuttlefish edge table.
	 * @param schemaName - The database schema
	 * @return - List of Cuttlefish edge tables
	 */
	public Collection<String> getEdgeTables(String schemaName) {
		return getTables(schemaName, edgeTableColumns);
	}
	
	/**
	 * This method reads the database meta-data and returns a list of tables that match the
	 * format of a Cuttlefish node table.
	 * @param schemaName - The database schema
	 * @return - List of Cuttlefish node tables
	 */
	public Collection<String> getNodeTables(String schemaName) {
		return getTables(schemaName, nodeTableColumns);
	}
	
	/**
	 * Returns the name of the nodes table name
	 * @return
	 */
	public String getNodeTable() {
		return nodeTable;
	}
	
	/**
	 * Returns the name of the edge table name
	 * @return
	 */
	public String getEdgeTable() {
		return edgeTable;
	}

	
	/**
	 * Method that queries the database to determine whether the network should be directed or not
	 */
	private void getDirection(){
		if (! initialized)
		{
			try {
				String queryString = "select * from Directed;";
				System.out.println(queryString);
		      	Statement st;
					st = conn.createStatement();
					ResultSet rs = st.executeQuery(queryString);
					rs.next();
					directed = rs.getBoolean(1);
					System.out.println("directed = " + directed);
			} catch (SQLException e) {
				initialized = true;
				directed = true;    // if no view is defined, is directed by default
				System.out.println("view Directed does not exist");
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
			JOptionPane.showMessageDialog(null,sqlEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
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
	 * @param nodeFilter
	 */
	public void setNodeFilter(String nodeFilter)
	{
		this.nodeFilter = nodeFilter;
	}
	
	/**
	 * Setter for the filter on the queries for the edges of the database
	 */
	public void setEdgeFilter(String edgeFilter)
	{
		this.edgeFilter = edgeFilter;
	}
	
	/**
	 * Function that applies certain filter to any query, transforming it to a filtered query
	 * @param query to modify
	 * @param filter to add to the query
	 * @return extended query
	 */
	private String applyFilter(String query, String filter)
	{
		if (filter.equals(""))
			return query;
		int whereIndex = query.lastIndexOf("where");
		if (whereIndex == -1)
			whereIndex = query.lastIndexOf("WHERE");
		
		if (whereIndex == -1)
			return query + " where " + filter;
		
		String result = query.substring(0, whereIndex+5);
		String whereClause = query.substring(whereIndex+5);
		result = result + " " + filter + " and " + whereClause;		
		return result;
	}
	
	/**
	 * Checks if the provided node id exists in the database
	 */
	public boolean checkNodeId(String nodeId) {
		String queryString = "SELECT * FROM " + nodeTable + " WHERE id = '" + nodeId + "'";
		System.out.println(queryString);
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(queryString);
			rs.last();
			System.out.println(rs.getRow());
			if(rs.getRow() == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
	    	JOptionPane.showMessageDialog(null,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}		
		return false;
	}
	
	/**
	 * Executes a proper node query and adds all the resulting nodes to the network
	 * @param queryString
	 */
	public void nodeQuery(String queryStringOriginal) {
		    try
		    {
		      String queryString = applyFilter(queryStringOriginal, nodeFilter);
		      System.out.println(queryString);
		      Statement st = conn.createStatement();
		      ResultSet rs = st.executeQuery(queryString);
		      while (rs.next())
		      {
			    	Vertex v;
			        int id = rs.getInt("id");
     		    	if (hash.get(id) == null)
			        {
     		    		String label = rs.getString("label");
				        if (label != null)
				        	v = new Vertex(id, label);
				        else
				        	v = new Vertex(id);
				        
				        String colorString = rs.getString("color");
				        if (colorString != null)
				        {
				        	int pos = 0;
				        	float R, G, B;
				        	R = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',',pos)));
		    				pos = colorString.indexOf(',',pos)+1;
			    			G = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',',pos)));
			    			pos = colorString.indexOf(',',pos)+1;
			    			B = Float.parseFloat(colorString.substring(pos));
			    			v.setFillColor(new Color(R,G,B));
			    		}
				        
				        String borderColorString = rs.getString("borderColor");
				        if (borderColorString != null)
				        {
				        	int pos = 0;
				        	float R, G, B;
				        	R = Float.parseFloat(borderColorString.substring(pos,borderColorString.indexOf(',',pos)));
		    				pos = borderColorString.indexOf(',',pos)+1;
			    			G = Float.parseFloat(borderColorString.substring(pos,borderColorString.indexOf(',',pos)));
			    			pos = borderColorString.indexOf(',',pos)+1;
			    			B = Float.parseFloat(borderColorString.substring(pos));
			    			v.setColor(new Color(R,G,B));
			    		}
				        
				        float size = rs.getFloat("size");
				        if (size != 0)
				        	v.setSize(size);
				        
				        String shape = rs.getString("shape");
				        if (shape != null)
				        	v.setShape(shape);
				        
				        int width = rs.getInt("width");
				        if (width != 0)
				        	v.setWidth(width);
	
				        String var1 = rs.getString("var1");
				        if (var1 != null)
				        	v.setVar1(var1);
	
				        String var2 = rs.getString("var2");
				        if (var2 != null)
				        	v.setVar2(var2);
				        
				        Double x = rs.getDouble("x");
				        Double y = rs.getDouble("y");
				        
				        if ((x != null) && (y != null))
				        	v.setPosition(x, y);

				        boolean fixed = rs.getBoolean("fixed");
				        	v.setFixed(fixed);
				        
				        boolean hide =false;
				        if (rs.getString("hide") != null)	
				        	hide = rs.getBoolean("hide");
				        
				        v.setExcluded(hide);
		
				        addVertex(v);
				        
				        hash.put(id, v);
			        }
			        
		      }
		    }
		    catch (SQLException sqlEx)
		    {
		    	JOptionPane.showMessageDialog(null,sqlEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				System.err.println("SQL error");
				sqlEx.printStackTrace();
		    }
		    extendEdges();
	}
	
	/**
	 * Method that adds to the network the neighboring nodes up to certain distance from a center.
	 * @param id from the node to start the search from
	 * @param distance maximum depth
	 * @param forward whether the traversal is forward or backward
	 */
	public void extendNeighborhood(int id, int distance, boolean forward) {
		Vertex v = hash.get(id);
		if (v == null)
		{
			try
		    {
			  String queryString = "select * from " + nodeTable + " where id = " + id + ";";
			  queryString = applyFilter(queryString, nodeFilter);
		      System.out.println(queryString);
		      Statement st = conn.createStatement();
		      ResultSet rs = st.executeQuery(queryString);
		      while (rs.next())
		      {
		    	    String label = rs.getString("label");
			        if (label != null)
			        	v = new Vertex(id, label);
			        else
			        	v = new Vertex(id);
			        
			        String colorString = rs.getString("color");
			        if (colorString != null)
			        {
			        	int pos = 0;
			        	float R, G, B;
			        	R = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',',pos)));
	    				pos = colorString.indexOf(',',pos)+1;
		    			G = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',',pos)));
		    			pos = colorString.indexOf(',',pos)+1;
		    			B = Float.parseFloat(colorString.substring(pos));
		    			v.setFillColor(new Color(R,G,B));
		    		}
			        
			        String borderColorString = rs.getString("borderColor");
			        if (borderColorString != null)
			        {
			        	int pos = 0;
			        	float R, G, B;
			        	R = Float.parseFloat(borderColorString.substring(pos,borderColorString.indexOf(',',pos)));
	    				pos = borderColorString.indexOf(',',pos)+1;
		    			G = Float.parseFloat(borderColorString.substring(pos,borderColorString.indexOf(',',pos)));
		    			pos = borderColorString.indexOf(',',pos)+1;
		    			B = Float.parseFloat(borderColorString.substring(pos));
		    			v.setColor(new Color(R,G,B));
		    		}
			        
			        float size = rs.getFloat("size");
			        if (size != 0)
			        	v.setSize(size);
			        
			        String shape = rs.getString("shape");
			        if (shape != null)
			        	v.setShape(shape);
			        
			        int width = rs.getInt("width");
			        if (width != 0)
			        	v.setWidth(width);

			        String var1 = rs.getString("var1");
			        if (var1 != null)
			        	v.setVar1(var1);

			        String var2 = rs.getString("var2");
			        if (var2 != null)
			        	v.setVar2(var2);
			        
			        boolean hide =false;
			        if (rs.getString("hide") != null)	
			        	hide = rs.getBoolean("hide");
			        
			        v.setExcluded(hide);
			        
			        Double x = rs.getDouble("x");
			        Double y = rs.getDouble("y");
			        
			        if ((x != null) && (y != null))
			        	v.setPosition(x, y);

			        boolean fixed = rs.getBoolean("fixed");
			        	v.setFixed(fixed);
			        	
			        addVertex(v);
			        hash.put(id, v);
		      }
		    }
		    catch (SQLException sqlEx)
		    {
		    	JOptionPane.showMessageDialog(null,sqlEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				System.err.println("Node not found");
				sqlEx.printStackTrace();
		    }
		}
		if ((forward == true) && (distance > 0) && (v != null))
		{
			try
			  {
				  String queryString = "select * from " + edgeTable + " where id_origin =" + v.getId() + ";";
				  queryString = applyFilter(queryString, edgeFilter);
			      System.out.println(queryString);
			      Statement st = conn.createStatement();
			      ResultSet rs = st.executeQuery(queryString);
			      while (rs.next())
			      {
			    	  int id_dest = rs.getInt("id_dest");
			    	  extendNeighborhood(id_dest, distance-1, true); 
			   
			    	  Edge e = new Edge();
			  		 	
			  			String label = rs.getString("label");
			  			e.setLabel(label);
			  			
			  			String colorString = rs.getString("color");
				        if (colorString != null)
				        {
				        	int pos = 0;
				        	float R, G, B;
				        	R = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',',pos)));
		    				pos = colorString.indexOf(',',pos)+1;
			    			G = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',',pos)));
			    			pos = colorString.indexOf(',',pos)+1;
			    			B = Float.parseFloat(colorString.substring(pos));
			    			e.setColor(new Color(R,G,B));
			    		}
				        
				        int width = rs.getInt("width");
				        if (width != 0)
				        	e.setWidth(width);

				        float weight = rs.getFloat("weight");
				        if (weight != 0)
				        	e.setWeight(weight);
				        
				        String var1 = rs.getString("var1");
				        if (var1 != null)
				        	e.setVar1(var1);

				        String var2 = rs.getString("var2");
				        if (var2 != null)
				        	e.setVar2(var2);
				       
				        boolean hide =false;
				        if (rs.getString("hide") != null)	
				        	hide = rs.getBoolean("hide");
				        
				        e.setExcluded(hide);
				       
				        if (directed)
				        	addEdge(e, v , hash.get(id_dest), EdgeType.DIRECTED);
				        else
				         	addEdge(e, v , hash.get(id_dest), EdgeType.UNDIRECTED);
					       
			      }
			  }
		      catch (SQLException sqlEx)
			  {
			    	JOptionPane.showMessageDialog(null,sqlEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
					System.err.println("SQL error");
					sqlEx.printStackTrace();
			  }
		}
		if ((forward == false) && (distance > 0) && (v != null))
		{
			try
			  {
				  String queryString = "select * from " + edgeTable  + " where id_dest =" + v.getId() + ";";
				  queryString = applyFilter(queryString, edgeFilter);
			      System.out.println(queryString);
			      Statement st = conn.createStatement();
			      ResultSet rs = st.executeQuery(queryString);
			      while (rs.next())
			      {
			    	  int id_origin = rs.getInt("id_origin");
			    	  extendNeighborhood(id_origin, distance-1, false);  
			    	  
			    	  Edge e = new Edge();
			  			
			  			String label = rs.getString("label");
			  			e.setLabel(label);
			  			
			  			String colorString = rs.getString("color");
				        if (colorString != null)
				        {
				        	int pos = 0;
				        	float R, G, B;
				        	R = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',',pos)));
		    				pos = colorString.indexOf(',',pos)+1;
			    			G = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',',pos)));
			    			pos = colorString.indexOf(',',pos)+1;
			    			B = Float.parseFloat(colorString.substring(pos));
			    			e.setColor(new Color(R,G,B));
			    		}
				        
				        int width = rs.getInt("width");
				        if (width != 0)
				        	e.setWidth(width);

				        float weight = rs.getFloat("weight");
				        if (weight != 0)
				        	e.setWeight(weight);
				        
				        String var1 = rs.getString("var1");
				        if (var1 != null)
				        	e.setVar1(var1);

				        String var2 = rs.getString("var2");
				        if (var2 != null)
				        	e.setVar2(var2);
			
				        boolean hide =false;
				        if (rs.getString("hide") != null)	
				        	hide = rs.getBoolean("hide");
				        
				        e.setExcluded(hide);
				        
				        if (directed)
				        	addEdge(e, hash.get(id_origin) , v, EdgeType.DIRECTED);
				        else
				         	addEdge(e, hash.get(id_origin) , v, EdgeType.UNDIRECTED);
						   
			      }
			  }
		      catch (SQLException sqlEx)
			  {
			    	JOptionPane.showMessageDialog(null,sqlEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
					System.err.println("SQL error");
					sqlEx.printStackTrace();
			  }
		}
	}
	
	/**
	 * Method that counts the number of nodes that match
	 * the selected nodes filter
	 * @return
	 */
	public int selectedNodesCount() {
		String sqlQuery = "SELECT count(id) as nodesCount FROM " + nodeTable;
		String queryString = applyFilter(sqlQuery, nodeFilter);
	    Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(queryString);
			rs.next();
			return rs.getInt("nodesCount");
		} catch (SQLException e) {
			e.printStackTrace();
		}	    
		return -1;
	}
	
	/**
	 * Method that counts the number of edges that are selected
	 * according to the nodes filter and the edges filter.
	 * @return
	 */
	public int selectedEdgesCount() {
		String nodesQuery = "SELECT id FROM " + nodeTable;
		nodesQuery = applyFilter(nodesQuery, nodeFilter);
		String edgesQuery = "SELECT count(*) as edgesCount FROM " + edgeTable;
		if(edgeFilter.length() > 0)
			edgesQuery = applyFilter(edgesQuery, edgeFilter) + " AND ";
		else
			edgesQuery += " WHERE ";
		edgesQuery += "id_origin IN (" + nodesQuery + ") AND id_dest IN (" + nodesQuery + ")";
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(edgesQuery);
			rs.next();
			return rs.getInt("edgesCount");
		} catch (SQLException e) {
			e.printStackTrace();
		}	    
		return -1;
	}
	
	/**
	 * This method takes a set of nodes as an input and returns a set of reachable
	 * nodes, i.e., all nodes that have an incoming edge starting from a node
	 * in the input set.
	 * @param nodes
	 * @return
	 */
	public Set<Integer> reachableNeighbors(Set<Integer> nodes) {
		Set<Integer> reachable = new HashSet<Integer>();
		StringBuilder nodesList = new StringBuilder("(");
		for(int nodeId : nodes) {
			nodesList.append(Integer.toString(nodeId) + ',');
		}
		nodesList.setCharAt(nodesList.length()-1, ')');		
		String sqlQuery = "SELECT id_dest FROM " + edgeTable + " WHERE id_origin IN " + nodesList;
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(sqlQuery);
			while(rs.next()) {
				reachable.add(rs.getInt("id_dest"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reachable;
	}
	
	public int countEdges(Set<Integer> nodes) {
		StringBuilder nodesList = new StringBuilder("(");
		for(int nodeId : nodes) {
			nodesList.append(Integer.toString(nodeId) + ',');
		}
		nodesList.setCharAt(nodesList.length()-1, ')');		
		String sqlQuery = "SELECT count(*) as edgeCount FROM " + edgeTable + " WHERE id_origin IN " + nodesList;
		System.out.println(sqlQuery);
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
	 * Method that adds all the edges of the subgraph induced in the database by the nodes
	 * already added to the displayed network
	 */
	public void extendEdges() {
		for (Vertex v : getVertices())
		{
			  try
			  {
				  String queryString = "select * from " + edgeTable + " where id_origin =" + v.getId() + ";";
				  queryString = applyFilter(queryString, edgeFilter);
			      System.out.println(queryString);
			      Statement st = conn.createStatement();
			      ResultSet rs = st.executeQuery(queryString);
			      while (rs.next())
			      {
			    	  int id_dest = rs.getInt("id_dest");
			    	  Vertex v_dest = hash.get(id_dest);
			    	  if (v_dest != null)
			    	  {
				    		Edge e = new Edge();
				  			
				  			String label = rs.getString("label");
				  			e.setLabel(label);
				  			
				  			String colorString = rs.getString("color");
					        if (colorString != null)
					        {
					        	int pos = 0;
					        	float R, G, B;
					        	R = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',',pos)));
			    				pos = colorString.indexOf(',',pos)+1;
				    			G = Float.parseFloat(colorString.substring(pos,colorString.indexOf(',',pos)));
				    			pos = colorString.indexOf(',',pos)+1;
				    			B = Float.parseFloat(colorString.substring(pos));
				    			e.setColor(new Color(R,G,B));
				    		}
					        
					        int width = rs.getInt("width");
					        if (width != 0)
					        	e.setWidth(width);
	
					        float weight = rs.getFloat("weight");
					        if (weight != 0)
					        	e.setWeight(weight);
					        
					        String var1 = rs.getString("var1");
					        if (var1 != null)
					        	e.setVar1(var1);
	
					        String var2 = rs.getString("var2");
					        if (var2 != null)
					        	e.setVar2(var2);
					    
					        boolean hide =false;
					        if (rs.getString("hide") != null)	
					        	hide = rs.getBoolean("hide");
					        
					        e.setExcluded(hide);
				
					        if (directed)
					        	addEdge(e,v , v_dest, EdgeType.DIRECTED);
					        else
					         	addEdge(e,v , v_dest, EdgeType.UNDIRECTED);
						       
			    	  }
			      }
			  }
		      catch (SQLException sqlEx)
			  {
			    	JOptionPane.showMessageDialog(null,sqlEx.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
					System.err.println("SQL error");
					sqlEx.printStackTrace();
			  }

		}
	}
	
	/**
	 * Method that substracts the neighborhood from a selected vertex
	 * @param vertex
	 */
	public void shrinkVertex(Vertex vertex)
	{
		for (Edge adjacentEdge : getOutEdges(vertex))
		{
			Vertex neighbor = getOpposite(vertex, adjacentEdge);
			removeEdge(adjacentEdge);
			if (getNeighborCount(neighbor) < 1)
			{
				hash.put(neighbor.getId(), null);
				removeVertex(neighbor);
			}
		}
	}
	
	/**
	 * Method that substracts the predecessors of a selected vertex
	 * @param vertex
	 */
	public void backShrinkVertex(Vertex vertex)
	{
		for (Edge adjacentEdge : getInEdges(vertex))
		{
			Vertex neighbor = getOpposite(vertex, adjacentEdge);
			removeEdge(adjacentEdge);
			if (getNeighborCount(neighbor) < 1)
			{
				hash.put(neighbor.getId(), null);
				removeVertex(neighbor);
			}
		}
	}
	
	/**
	 * This method checks if Cuttlefish is connected to a database
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
	
}


