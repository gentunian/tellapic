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

import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
public class FavouritesTableModel extends AbstractTableModel {
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
	
	
	public FavouritesTableModel() {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
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
		int i;
		for (i = 0; i < rowCount && i != rowIndex; i++);
		if (i < rowCount) {
			Element server = (Element) servers.item(i);
			return ((Element) server.getElementsByTagName(COLUMN_ELEMENTS[columnIndex]).item(0)).getTextContent();
		}
		return null;
	}

	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param name
	 * @param password
	 */
	public void addFavourite(String host, int port, String name, String password) {
				
		FileOutputStream fos = null;
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
			
			fos = new FileOutputStream(FAVOURITE_FILE);
			OutputFormat of = new OutputFormat("XML","UTF-8",true);
			of.setIndent(1);
			of.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(fos,of);

			serializer.asDOMSerializer();
			serializer.serialize(document.getDocumentElement() );
			fos.close();
			rowCount++;
			fireTableRowsInserted(rowCount-1, rowCount);
		} catch (FileNotFoundException e) {
			Utils.logMessage("No "+FAVOURITE_FILE+" file found.");
			return;
		} catch (IOException e) {
			Utils.logMessage("Could not create "+FAVOURITE_FILE);
			return;
		}
	}
}
