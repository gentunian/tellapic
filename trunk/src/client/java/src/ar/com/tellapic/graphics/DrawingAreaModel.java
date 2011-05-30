package ar.com.tellapic.graphics;

import java.util.Collection;
import java.util.Comparator;
import java.util.Observable;
import java.util.TreeSet;

public class DrawingAreaModel extends Observable implements IDrawingAreaState, IDrawingAreaManager{

	private Collection<AbstractDrawing> drawings;
	private static class Holder {
		private static final DrawingAreaModel INSTANCE = new DrawingAreaModel();
	}
	
	private DrawingAreaModel() {
		drawings = new TreeSet<AbstractDrawing>(new Comparator<AbstractDrawing>(){
			@Override
			public int compare(AbstractDrawing o1, AbstractDrawing o2) {
				Long value1 = o1.getNumber();
				Long value2 = o2.getNumber();
				return value1.compareTo(value2);
			}
		});
	}
	

	public static DrawingAreaModel getInstance() {
		return Holder.INSTANCE;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaState#getDrawings()
	 */
	@Override
	public Collection<AbstractDrawing> getDrawings() {
		return drawings;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaManager#addDrawing(ar.com.tellapic.graphics.AbstractDrawing)
	 */
	@Override
	public void addDrawing(AbstractDrawing drawing) throws IllegalArgumentException {
		if (drawing == null)
			throw new IllegalArgumentException("Drawing cannot be null.");
		drawings.add(drawing);
	}

}
