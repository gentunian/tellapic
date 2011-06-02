package ar.com.tellapic.graphics;

import java.util.Collection;
import java.util.Comparator;
import java.util.Observable;
import java.util.TreeSet;

import ar.com.tellapic.AbstractUser;

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
	public synchronized Collection<AbstractDrawing> getDrawings() {
		return drawings;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaManager#addDrawing(ar.com.tellapic.graphics.AbstractDrawing)
	 */
	@Override
	public synchronized void addDrawing(AbstractDrawing drawing) throws IllegalArgumentException {
		if (drawing == null)
			throw new IllegalArgumentException("Drawing cannot be null.");
		drawings.add(drawing);
	}


	/**
	 * @param drawing
	 */
	public synchronized void removeDrawing(AbstractDrawing drawing) {
		if (drawing == null)
			throw new IllegalArgumentException("Drawing cannot be null.");
		boolean b = drawings.remove(drawing);
		if (b) {}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaManager#selectDrawing(ar.com.tellapic.graphics.AbstractDrawing)
	 */
	@Override
	public synchronized void selectDrawing(AbstractDrawing drawing) throws IllegalArgumentException {
		AbstractDrawing[] da = drawings.toArray(new AbstractDrawing[0]);
		for(int i = 0; i < da.length; i++)
			da[i].setSelected(da[i].equals(drawing));
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaManager#selectDrawing(long)
	 */
	@Override
	public synchronized void selectDrawing(long number) {
		AbstractDrawing[] da = drawings.toArray(new AbstractDrawing[0]);
		for(int i = 0; i < da.length; i++)
			da[i].setSelected((da[i].getNumber() == number));
	}


	/**
	 * 
	 */
	@Override
	public synchronized void removeSelectedDrawing() {
		int i = 0;
		AbstractDrawing[] da = drawings.toArray(new AbstractDrawing[0]);
		for(i = 0; i < da.length; i++) {
			if (da[i].isSelected()) {
				AbstractUser user = da[i].getUser();
				user.removeDrawing(da[i]);
				return;
			}
		}
	}
}
