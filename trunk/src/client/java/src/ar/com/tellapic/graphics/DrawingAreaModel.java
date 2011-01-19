package ar.com.tellapic.graphics;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.utils.Utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

public class DrawingAreaModel extends Observable implements IDrawingAreaState, IDrawingAreaManager {

	//private ArrayList<Drawing> drawings;
	
	private ListMultimap<AbstractUser, Drawing> drawings;
	
	private static class ModelHolder {
		private static final DrawingAreaModel INSTANCE = new DrawingAreaModel();
	}
	
	private DrawingAreaModel() {
		//drawings = new ArrayList<Drawing>();
		drawings = Multimaps.synchronizedListMultimap(ArrayListMultimap.<AbstractUser, Drawing>create());
		
	}

	public static DrawingAreaModel getInstance() {
		return ModelHolder.INSTANCE;
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaState#getDrawing(int)
	 */
	@Override
	public List<Drawing> getDrawingsFrom(AbstractUser user) throws IndexOutOfBoundsException {
		return drawings.get(user);
	}

	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaState#getDrawings()
	 */
	@Override
	public Collection<Map.Entry<AbstractUser,Drawing>> getDrawingCollection() {
		return drawings.entries();
	}
	

	/* (non-Javadoc)
	@Override
	public Drawing getLastDrawing() {
		return drawings.get(drawings.size() - 1);
	}
	 */
		
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaManager#removeDrawing(int)
	 */
	@Override
	public void removeDrawing(int i) throws IndexOutOfBoundsException {
		//drawings.remove(i);
		setChanged();		
		notifyObservers();
	}

	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaManager#removeLastDrawing()
	 */
	@Override
	public void removeLastDrawing() {
		/*
		if (!drawings.isEmpty())
			drawings.remove(drawings.size() - 1);
		
		//TODO: think of a better notification
		setChanged();		
		notifyObservers();
		*/
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaManager#addDrawing(ar.com.tellapic.graphics.Drawing)
	 */
	@Override
	public void addDrawing(AbstractUser user, Drawing drawing) throws IllegalArgumentException {
		if (drawing == null || user == null)
			throw new IllegalArgumentException("drawing or user cannot be null");
		
		//drawings.add(drawing);
		drawings.put(user, drawing);
		Object[] pair = new Object[2];
		pair[0] = user;
		pair[1] = drawing;
		//Utils.logMessage("Drawing added. Notifyin observers.");
		setChanged();
		notifyObservers(pair);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaManager#removeDrawing(ar.com.tellapic.graphics.Drawing)
	 */
	@Override
	public void removeDrawingFrom(AbstractUser user) throws IllegalArgumentException {
		if (user == null)
			throw new IllegalArgumentException("user cannot be null");
		
		//drawings.remove(drawing);
		drawings.removeAll(user);
		setChanged();		
		notifyObservers();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IDrawingAreaState#getDrawings()
	 */
	@Override
	public ListMultimap<AbstractUser, Drawing> getDrawings() {
		return drawings;
	}
}
