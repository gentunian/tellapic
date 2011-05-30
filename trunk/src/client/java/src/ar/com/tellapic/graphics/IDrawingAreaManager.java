/**
 * 
 */
package ar.com.tellapic.graphics;



/**
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IDrawingAreaManager {
	/**
	 * 
	 * @param drawing
	 * @throws IllegalArgumentException
	 */
	public void addDrawing(AbstractDrawing drawing) throws IllegalArgumentException;
//	
//	
//	/**
//	 * Removes a Drawing from a list.
//	 * 
//	 * @param i the position where the Drawing should be removed.
//	 * @throws IndexOutOfBoundsException if i is not a valid index.
//	 */
//	public void removeDrawing(int i) throws IndexOutOfBoundsException;
//	
//	
//	/**
//	 * Removes a Drawing from a list.
//	 * 
//	 * @param drawing the Drawing to be removed.
//	 * @throws NoSuchElementException if drawing does not exist.
//	 * @throws IllegalArgumentException if drawing is null.
//	 */
//	//public void removeDrawing(Drawing drawing) throws IllegalArgumentException;
//	public void removeDrawingFrom(AbstractUser user) throws IllegalArgumentException;
//	
//	
//	/**
//	 * Removes the last Drawing 
//	 */
//	public void removeLastDrawing();
}
