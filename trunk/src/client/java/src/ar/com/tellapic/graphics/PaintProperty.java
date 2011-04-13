package ar.com.tellapic.graphics;


/**
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class PaintProperty implements Cloneable {
	private String            name;
	private PaintPropertyType type;
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	public enum PaintPropertyType {
		STROKE,
		COLOR,
		ALPHA,
		FONT,
		ZOOM
	}

	public PaintProperty(PaintPropertyType type) throws IllegalArgumentException {
		boolean found = true;
		for(PaintPropertyType item : PaintPropertyType.values())
			if (item.compareTo(type) == 0 )
				found = true;
		
		if (!found) 
			throw new IllegalArgumentException();
		
		this.type = type;
		this.name = type.toString();
	}
	
	/**
	 * 
	 * @return the PaintPropertyType type.
	 */
	public PaintPropertyType getType() {
		return type;
	}
	
	
	/**
	 * 
	 * @return the PaintProperty name.
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			// This should never happen
			throw new InternalError(e.toString());
		}
	}
}
