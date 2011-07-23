'''
 Python Test Module for tellapic

 @author: Sebastian Treu
'''
from PyQt4 import *
from PyQt4.QtCore import *
from PyQt4.QtGui import *
import pytellapic

class DrawingModel(object):
    def __init__(self):
        self.drawingList = []
    
    def binSearch(self, i, j, key):
        if (i < 0  or j < 0):
            return -1
        if (i == j):
            if (key == self.drawingList[i].number):
                return i
            else:
                return -1
        else:
            middle = int((i + j) / 2)
            if (self.drawingList[middle].number == key):
                return self.binSearch(middle, middle, key)

            elif (self.drawingList[middle].number < key):
                return self.binSearch(middle + 1, j, key)
            
            else:
                return self.binSearch(i, middle, key)

    def addDrawing(self, drawing):
        #print("adding drawing: ",drawing, " (key: ",drawing.number,") to model.")
        self.drawingList.append(drawing)
        self.drawingList.sort(key=lambda drawing: drawing.number)

    def getDrawing(self, key):
        #print("looking for key: ", key)
        index = self.binSearch(0, len(self.drawingList) - 1, key)
        #print("index was: ", index)
        drawing = None
        if (index != -1):
            drawing = self.drawingList[index]
        return drawing

    def drawings(self):
        return self.drawingList





# This should be an abstract class for a colection of tools
# but lets explote python polimorphism
class DrawingTool(object):
    def __init__(self):
        self.a = 1





# This tool will create rectangle shapes
class DrawingToolRectangle(DrawingTool):
    def __init__(self):
        DrawingTool.__init__(self)
        self.drawing = None

    # Delegate the mouse events from a QWidget to this tool
    def mousePressed(self, event):
        point = event.pos()
        self.x1 = point.x()
        self.y1 = point.y()
        self.drawing = DrawingShapeRectangle()
        self.drawing.setBounds(point.x(), point.y(), point.x(), point.y())
    
    # Delegate the mouse events from a QWidget to this tool
    def mouseDragged(self, event):
        point = event.pos()
        self.drawing.setBounds(self.x1, self.y1, point.x(), point.y())

    # Delegate the mouse events from a QWidget to this tool
    def mouseReleased(self, event):
        pass





# This should be another abstract class with a default behaviour and
# attributes. DrawginShape will draw shapes, and DrawingText...text.
class Drawing(object):
    PytellapicJoinsStyle = {
        pytellapic.LINE_JOINS_MITER : Qt.MiterJoin,
        pytellapic.LINE_JOINS_ROUND : Qt.RoundJoin,
        pytellapic.LINE_JOINS_BEVEL : Qt.BevelJoin
        }

    PytellapicCapsStyle = {
        pytellapic.END_CAPS_BUTT : Qt.FlatCap,
        pytellapic.END_CAPS_ROUND : Qt.RoundCap,
        pytellapic.END_CAPS_SQUARE : Qt.SquareCap
        }

    def __init__(self):
        self.pen        = QPen()
        self.brush      = QBrush()
        self.renderHint = QPainter.Antialiasing
        self.alpha      = QPainter.CompositionMode_SourceOver
        self.number     = 0

    # This class doesn't know about what is he drawing, but
    # he can manage some default behaviour and attributes
    def draw(self, painter):
        painter.setBrush(self.brush)
        painter.setPen(self.pen)
        painter.setRenderHint(self.renderHint)
    
    def setBounds(self, x1, y1, x2, y2):
        #print("Setting bounds: ", x1, " ", y1, " | ", x2, " ", y2)
        self.x1 = x1
        self.y1 = y1
        self.x2 = x2
        self.y2 = y2

    def setNumber(self, number):
        #print("Setting number to: ", number)
        self.number = number

    def setFillColor(self, color):
        self.brush = QColor(color.red, color.green, color.blue, color.alpha)

    def setWidth(self, width):
        #print("Setting width to: ", width)
        self.pen.setWidth(width)





class DrawingText(Drawing):
    PytellapicFontStyle = {
        pytellapic.FONT_STYLE_NORMAL : QFont.StyleNormal,
        pytellapic.FONT_STYLE_ITALIC : QFont.StyleItalic,
        }

    def __init__(self, drawing = None):
        if (drawing is None):
            Drawing.__init__(self)
        else:
            self.pen        = QPen()
            self.brush      = None
            self.renderHint = drawing.renderHint
            self.number     = drawing.number
            self.pen.setWidthF(0.05)

    def setFont(self, color, style, face, size):
        self.font = QFont()
        self.font.setFamily(face)
        try:
            fontStyle = self.PytellapicFontStyle[style]
            self.font.setStyle(fontStyle)
        except:
            self.font.setBold(True)
            self.font.setItalic(style == pytellapic.FONT_STYLE_BOLD_ITALIC)
        self.font.setPointSize(size)
        self.brush = QColor(color.red, color.green, color.blue, color.alpha)

    def setBounds(self, x1, x2, y1, y2):
        Drawing.setBounds(self, x1, x2, y1, y2)
        self.shape = QPainterPath()
        self.shape.addText(x1, x2, self.font, self.text)

    def setText(self, text):
        self.text = text

    def draw(self, painter):
        Drawing.draw(self, painter)
        painter.drawPath(self.shape)

    def setWidth(self, width):
        self.pen.setWidthF(0.05)
    
    def setFillColor(self, color):
        pass

# This could be also an abstract class for shapes
class DrawingShape(Drawing):
    def __init__(self):
        Drawing.__init__(self)

    # This class knows that he is a shape, so he can draw it
    # with the base clase implementation
    def draw(self, painter):
        Drawing.draw(self, painter)
        painter.drawPath(self.shape)

    # The brush this class will have to be drawn by draw()
    def setBrush(self, brush):
        self.pen.setBrush(brush)

    # The pen this class will have to be drawn by draw()
    def setPen(self, pen):
        self.pen = pen

    # The rendering hints this class will have to be drawn by draw()
    def setRenderHint(self, hint):
        self.renderHint = hint

    def setLineJoins(self, lj):
        #print("Setting line joins to: ", lj)
        self.pen.setJoinStyle(self.PytellapicJoinsStyle[lj])
    
    def setEndCaps(self, ec):
        #print("Setting end caps to: ", ec)
        self.pen.setCapStyle(self.PytellapicCapsStyle[ec])

    def setStrokeColor(self, color):
        self.pen.setColor(QColor(color.red, color.green, color.blue, color.alpha))

    def setMiterLimit(self, ml):
        self.pen.setMiterLimit(ml)

    def setDashStyle(self, phase, array):
        self.pen.setDashOffset(phase)
        self.pen.setDashPattern(array)





# This class should be concrete. He knows exactly that it consists
# of a shape as a rectangle.
class DrawingShapeRectangle(DrawingShape):

    def __init__(self, drawing = None):
        if (drawing is None):
            DrawingShape.__init__(self)
        else:
            self.pen = drawing.pen
            self.brush = drawing.brush
            self.renderHint = drawing.renderHint
            self.number = drawing.number
        self.shape = QPainterPath()

    # Every time setBounds is called, it will create a new QPainterPath() object
    # This was written for simplicity.
    def setBounds(self, x1, y1, x2, y2):
        Drawing.setBounds(self, x1, y1, x2, y2)
        self.shape = QPainterPath()
        self.shape.moveTo(x1, y1)
        self.shape.lineTo(x2, y1)
        self.shape.lineTo(x2, y2)
        self.shape.lineTo(x1, y2)
        self.shape.closeSubpath()

        
class DrawingShapeEllipse(DrawingShape):
    def __init__(self, drawing = None):
        if (drawing is None):
            DrawingShape.__init__(self)
        else:
            self.pen = drawing.pen
            self.brush = drawing.brush
            self.renderHint = drawing.renderHint
            self.number = drawing.number
        self.shape = QPainterPath()

    # Every time setBounds is called, it will create a new QPainterPath() object
    # This was written for simplicity.
    def setBounds(self, x1, y1, x2, y2):
        Drawing.setBounds(self, x1, y1, x2, y2)
        self.shape = QPainterPath()
        self.shape.addEllipse(QRectF(x1, y1, abs(x1-x2), abs(y1-y2)))
        self.shape.closeSubpath()

class DrawingShapeLine(DrawingShape):
    def __init__(self, drawing = None):
        if (drawing is None):
            DrawingShape.__init__(self)
        else:
            self.pen = drawing.pen
            self.brush = drawing.brush
            self.renderHint = drawing.renderHint
            self.number = drawing.number
        self.shape = QPainterPath()

    # Every time setBounds is called, it will create a new QPainterPath() object
    # This was written for simplicity.
    def setBounds(self, x1, y1, x2, y2):
        Drawing.setBounds(self, x1, y1, x2, y2)
        self.shape = QPainterPath()
        self.shape.moveTo(x1, y1)
        self.shape.lineTo(x2, y2)
        self.shape.closeSubpath()    
