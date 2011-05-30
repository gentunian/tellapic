/**
 * 
 */
package ar.com.tellapic.graphics;

import java.util.Collection;


/**
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IDrawingAreaState {
//
//	/**
//	 * Returns the list of drawn objects
//	 * 
//	 * @return the List of Drawing.
//	 */
//	public Collection<Map.Entry<AbstractUser,Drawing>> getDrawingCollection();

	/**
	 * 
	 */
	public Collection<AbstractDrawing> getDrawings();
//	
//	
//	/**
//	 * Returns the last drawn object
//	 * 
//	 * @return the last Drawing object.
//	 */
//	//public Drawing getLastDrawing();
//	
//	
//	/**
//	 * Returns the Drawing at position i in the list of Drawings.
//	 * 
//	 * @param i the index of the Drawing to be retrieved.
//	 * @return the Drawing at position i.
//	 * @throws IndexOutOfBoundsException if position i is out of bound in the list
//	 */
//	public List<Drawing> getDrawingsFrom(AbstractUser user) throws IndexOutOfBoundsException;
//	
//	
}
