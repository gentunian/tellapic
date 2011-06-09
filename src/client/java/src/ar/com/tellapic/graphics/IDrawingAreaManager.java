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
	
	public void removeDrawing(AbstractDrawing drawing);
	
//	public void selectDrawing(AbstractDrawing drawing) throws IllegalArgumentException;
//	
//	public void selectDrawing(long number);
	
	public void removeSelectedDrawing();
}
