/**
 *   Copyright (c) 2010 Sebastián Treu.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 * @author
 *         Sebastian Treu 
 *         sebastian.treu(at)gmail.com
 *
 */  
package ar.com.tellapic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ar.com.tellapic.utils.Utils;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;



/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 * <favourites>
 *   <server name="0">
 *     <host>localhost</host>
 *     <port>4451</port>
 *     <name>seba</name>
 *     <password>seba</password>
 *   </server>
 * </favourites>
 */
public class FavouritesTableModel extends AbstractTableModel implements TableModel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static String rootElementName = "Favourites";
	public final static String xmlDeclaration = "<?xml version=\"1.0\"?>";
	private final static String FAVOURITE_FILE = System.getProperty("user.home")+"/.tellapic/favourites.xml";
	private final static int COLUMN_COUNT = 4;
	private final static String COLUMN_ELEMENTS[] = {
		"host",
		"port",
		"name",
		"password"
	};
	
	private Document               document;
	private int                    rowCount;
	private Element                favouriteRoot;
	private NodeList               servers;
	private DocumentBuilderFactory factory;
	private DocumentBuilder        builder;
	
	
	/**
	 * 
	 */
	public FavouritesTableModel() {
		rowCount = 0;
		initDocumentFactory();
	}
	
	
	/**
	 * 
	 */
	private void initDocumentFactory() {
		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
			document = builder.parse(FAVOURITE_FILE);
			favouriteRoot = (Element) document.getElementsByTagName("favourites").item(0);
			servers  = document.getElementsByTagName("server");
			rowCount = servers.getLength();
			
		} catch (IOException e) {
			Utils.logMessage("No "+FAVOURITE_FILE+" file found.");
			rowCount = 0;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}	
	}
	
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return rowCount;
	}

	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
//		int i;
//		for (i = 0; i < rowCount && i != rowIndex; i++);
		Object value = null;
		if (rowIndex < rowCount) {
			Element server = (Element) servers.item(rowIndex);
			value = ((Element) server.getElementsByTagName(COLUMN_ELEMENTS[columnIndex]).item(0)).getTextContent();
		}
		return value;
	}

	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param name
	 * @param password
	 */
	public boolean addFavourite(String host, int port, String name, String password) {
				
		//FileOutputStream fos = null;
		try {
			File file = new File(FAVOURITE_FILE);
			if (!file.exists()) {
				file.createNewFile();
				document = builder.newDocument();
				favouriteRoot = document.createElement("favourites");
				document.appendChild(favouriteRoot);
			}
			Element serverElement = document.createElement("server");
			Element hostElement = document.createElement("host");
			Element portElement = document.createElement("port");
			Element nameElement = document.createElement("name");
			Element passElement = document.createElement("password");

			serverElement.setAttribute("name", String.valueOf(rowCount+1));
			hostElement.setTextContent(host);
			portElement.setTextContent(String.valueOf(port));
			nameElement.setTextContent(name);
			passElement.setTextContent(password);

			serverElement.appendChild(hostElement);
			serverElement.appendChild(portElement);
			serverElement.appendChild(nameElement);
			serverElement.appendChild(passElement);

			favouriteRoot.appendChild(serverElement);
			
			saveToFile();
			if (rowCount == 0)
				initDocumentFactory();
			else
				rowCount++;
			
			fireTableRowsInserted(0, rowCount-1);
			
			return true;
		} catch (FileNotFoundException e) {
			Utils.logMessage("No "+FAVOURITE_FILE+" file found.");
			return false;
		} catch (IOException e) {
			Utils.logMessage("Could not create "+FAVOURITE_FILE);
			return false;
		}
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void addTableModelListener(TableModelListener l) {
		super.addTableModelListener(l);
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return COLUMN_ELEMENTS[columnIndex];
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void removeTableModelListener(TableModelListener l) {
		removeTableModelListener(l);
	}


	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Element server = (Element) servers.item(rowIndex);
		((Element) server.getElementsByTagName(COLUMN_ELEMENTS[columnIndex]).item(0)).setTextContent((String) aValue);
		
		try {
			saveToFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 * @param rowIndex
	 */
	public void removeFavourite(int rowIndex) {
		Element server = (Element) servers.item(rowIndex);
		Node parent = server.getParentNode();
		parent.removeChild(server);
		
		try {
			saveToFile();
			rowCount--;
			fireTableRowsDeleted(0, rowCount-1);
			initDocumentFactory();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 */
	private void saveToFile() throws FileNotFoundException, IOException{
		FileOutputStream fos = new FileOutputStream(FAVOURITE_FILE);
		OutputFormat of = new OutputFormat("XML","UTF-8",true);
		of.setIndent(1);
		of.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(fos,of);
		serializer.asDOMSerializer();
		serializer.serialize(document.getDocumentElement());
		fos.close();
	}
}
