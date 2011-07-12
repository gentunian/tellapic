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
package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.event.MouseEvent;

import ar.com.tellapic.NetManager;
import ar.com.tellapic.SessionUtils;
import ar.com.tellapic.lib.tellapic;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
final public class DrawingToolRectangleNet extends DrawingToolRectangle {
	
//	private stream_t stream;
//	private point_t  point1;
//	private point_t  point2;
	
	public DrawingToolRectangleNet() {
		super("DrawingToolRectangleNet");
		
//		/* Creates new stream and header data */
//		stream = new stream_t();
//		point1 = new point_t();
//		point2 = new point_t();
//		header_t        header      = new header_t();
//		stream_t_data   data        = stream.getData();
//		ddata_t         drawingData = data.getDrawing();
//		
//		/* Cache and set the fixed part of the stream this tool is able to send */
//		header.setCbyte((short) tellapicConstants.CTL_CL_FIG);
//		header.setEndian((short) 0);
//		header.setSsize(tellapicConstants.FIG_STREAM_SIZE);
//		stream.setHeader(header);
//		
//		drawingData.setDcbyte((short) (tellapicConstants.TOOL_RECT | tellapicConstants.EVENT_NULL));
//		drawingData.setDcbyte_ext((short) 0);
//		drawingData.setIdfrom((short) SessionUtils.getId());
	}
	
		
	@Override
	public DrawingShape frame(String x, String y, String w, String h) {
		DrawingShape drawing = super.frame(x, y, w, h);
		
		sendGeneratedDrawing(drawing);

		return drawing;
	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.DrawingToolRectangle#mousePressed(java.awt.event.MouseEvent)
//	 */
//	public void mousePressed(MouseEvent e) {
//		super.mousePressed(e);
//		
//		DrawingShape drawing = (DrawingShape) getTemporalDrawing();
//		
//		if (drawing != null) {
//			PaintPropertyStroke strokeProperty = drawing.getPaintPropertyStroke();
//			PaintPropertyAlpha  alphaProperty  = drawing.getPaintPropertyAlpha();
//			PaintPropertyFill   fillProperty   = drawing.getPaintPropertyFill();
//			stream_t_data       data           = stream.getData();
//			ddata_t             drawingData    = data.getDrawing();
//			figure_t            figureData     = drawingData.getType().getFigure();
//			color_t             fillColor      = drawingData.getFillcolor();;
//			color_t             strokeColor    = drawingData.getType().getFigure().getColor();;
//			
//			/* Sets the fill color attributes */
//			fillColor.setAlpha((short) ((Color)fillProperty.getFillPaint()).getAlpha());
//			fillColor.setRed((short) ((Color)fillProperty.getFillPaint()).getRed());
//			fillColor.setGreen((short) ((Color)fillProperty.getFillPaint()).getGreen());
//			fillColor.setBlue((short) ((Color)fillProperty.getFillPaint()).getBlue());
//
//			/* Sets the stroke color attributes */
//			strokeColor.setAlpha((short)strokeProperty.getColor().getAlpha());
//			strokeColor.setRed((short)strokeProperty.getColor().getRed());
//			strokeColor.setGreen((short)strokeProperty.getColor().getGreen());
//			strokeColor.setBlue((short)strokeProperty.getColor().getBlue());
//			
//			/* Sets the drawing data */
//			drawingData.setNumber(drawing.getNumber());
//			drawingData.setOpacity(alphaProperty.getAlpha());
//			drawingData.setWidth((float) strokeProperty.getWidth());
//			drawingData.setPoint1(point1);
//			figureData.setColor(strokeColor);
//			figureData.setDash_array(strokeProperty.getDash());
//			figureData.setDash_phase(strokeProperty.getDash_phase());
//			figureData.setEndcaps((short) strokeProperty.getEndCaps().ordinal());
//			figureData.setLinejoin((short) strokeProperty.getLineJoins().ordinal());
//			figureData.setMiterlimit(strokeProperty.getMiterLimit());
//			figureData.setPoint2(point2);
//		}
//		
//		point1.setX((int) getFirstPoint().getX());
//		point1.setY((int) getFirstPoint().getY());
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.DrawingToolRectangle#mouseDragged(java.awt.event.MouseEvent)
//	 */
//	public void mouseDragged(MouseEvent e) {
//		super.mouseDragged(e);
//		
//		point2.setX((int) getLastPoint().getX());
//		point2.setY((int) getLastPoint().getY());
//	}
//	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Ellipse#onRelease(int)
	 */
	@Override
	public void mouseReleased(MouseEvent event) {
		super.mouseReleased(event);

		if (isSelected()) {
			DrawingShape drawing = (DrawingShape) getTemporalDrawing();
			if (drawing == null)
				return ;

			sendGeneratedDrawing(drawing);

			/* This tool has no more temporal drawings */
			setTemporalDrawing(null);
		}
	}
	
	/**
	 * 
	 * @param drawing
	 */
	private void sendGeneratedDrawing(DrawingShape drawing) {
		if (NetManager.getInstance().isConnected() && !getUser().isRemote()) {
			java.awt.Rectangle bounds = drawing.getShape().getBounds();
			tellapic.tellapic_send_fig(
					NetManager.getInstance().getSocket(),
					getToolId(), 
					0,
					SessionUtils.getId(), 
					0,
					(float) drawing.getPaintPropertyStroke().getWidth(),
					drawing.getPaintPropertyAlpha().getAlpha(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getRed(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getGreen(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getBlue(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getAlpha(),
					(int)bounds.getX(),
					(int)bounds.getY(),
					drawing.getPaintPropertyStroke().getColor().getRed(),
					drawing.getPaintPropertyStroke().getColor().getGreen(),
					drawing.getPaintPropertyStroke().getColor().getBlue(),
					drawing.getPaintPropertyStroke().getColor().getAlpha(),
					(int)(bounds.getX() + bounds.getWidth()),
					(int)(bounds.getY() + bounds.getHeight()),
					drawing.getPaintPropertyStroke().getLineJoins().ordinal(),
					drawing.getPaintPropertyStroke().getEndCaps().ordinal(),
					drawing.getPaintPropertyStroke().getMiterLimit(),
					drawing.getPaintPropertyStroke().getDash_phase(),
					drawing.getPaintPropertyStroke().getDash()
			);
		}
	}
}
