package ar.com.tellapic.graphics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Observable;
import java.util.TreeSet;

import ar.com.tellapic.TellapicAbstractUser;

public class DrawingAreaModel extends Observable implements IDrawingAreaState, IDrawingAreaManager{

	public static final String ADD_DRAWING = "AddDrawing";
	public static final String REMOVE_DRAWING = "RemoveDrawing";
	
	private Collection<AbstractDrawing> drawings;
	private Comparator<AbstractDrawing> myComparator;
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private static class Holder {
		private static final DrawingAreaModel INSTANCE = new DrawingAreaModel();
	}
	
	/**
	 * 
	 */
	private DrawingAreaModel() {
		myComparator = new Comparator<AbstractDrawing>(){
			@Override
			public int compare(AbstractDrawing o1, AbstractDrawing o2) {
				Long value1 = o1.getNumber();
				Long value2 = o2.getNumber();
				return value1.compareTo(value2);
			}
		};
		
		drawings = new TreeSet<AbstractDrawing>(myComparator);
	}
	
	/**
	 * 
	 * @return
	 */
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
		setChanged();
		notifyObservers(drawing);
	}

	/**
	 * @param drawing
	 */
	@Override
	public synchronized void removeDrawing(AbstractDrawing drawing) {
		if (drawing == null)
			throw new IllegalArgumentException("Drawing cannot be null.");
		drawings.remove(drawing);
		setChanged();
		notifyObservers(drawing);
	}

	/**
	 * 
	 * @param number
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public AbstractDrawing getDrawing(long number) {
		AbstractDrawing   drawing = null;
		AbstractDrawing[] da = drawings.toArray(new AbstractDrawing[0]);
		
		Arrays.sort(da, myComparator);
		
		int index = Arrays.binarySearch(da, number, new Comparator(){
			@Override
			public int compare(Object o1, Object o2) {
				Long l1 = ((AbstractDrawing) o1).getNumber();
				return l1.compareTo((Long)o2);
			}
		});
		
		if (index >= 0)
			drawing = da[index];
		
		return drawing;
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
				TellapicAbstractUser user = da[i].getUser();
				user.removeDrawing(da[i]);
				return;
			}
		}
	}
}
