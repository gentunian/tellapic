'''
 Python Test Module for tellapic

 @author: Sebastian Treu
'''
from PyQt4 import *
from PyQt4.QtCore import *
from PyQt4.QtGui import *
import abc
import pytellapic
from Utils import MyEvent
import sys
try:
    from PyQt4.QtCore import QString
except ImportError:
    # we are using Python3 so QString is not defined
    QString = type("")

if not hasattr(sys, "hexversion") or sys.hexversion < 0x03000000:
    from nonconflict import classmaker

ToolRectangle = "rectangle"
ToolEllipse = "ellipse"
ToolLine = "line"
ToolSelector = "selector"
ToolMarker = "marker"
ToolPen = "pen"

# This should be an abstract class for a colection of tools
class Tool(object):
    __metaclass__ = abc.ABCMeta
    
    @abc.abstractmethod
    def name(self):
        """Returns the concrete drawing tool name."""
        return
        
    @abc.abstractproperty
    def drawing(self):
        """Returns the drawing created"""
        return

    @abc.abstractmethod
    def mousePressed(self, event):
        return

    @abc.abstractmethod
    def mouseDragged(self, event):
        return

    @abc.abstractmethod
    def mouseMoved(self, event):
        return

    @abc.abstractmethod
    def mouseReleased(self, event):
        return

class DrawingTextTool(Tool):
    def __init__(self):
        self._drawing = DrawingText()

    def setFontStyle(self, size, face, style):
        pass

    # Delegate the mouse events from a QWidget to this tool
    def mousePressed(self, point):
        self._drawing.setBounds(point.x(), point.y(), point.x(), point.y())

    # Delegate the mouse events from a QWidget to this tool
    def mouseDragged(self, point):
        self._drawing.setBounds(self._drawing.x1, self._drawing.y1, point.x(), point.y())

    def mouseMoved(self, point):
        pass

    # Delegate the mouse events from a QWidget to this tool
    def mouseReleased(self, point):
        pass

    # The tool name
    def name(self):
        return ToolText

    @property
    def drawing(self):
        return self._drawing

class DrawingShapeTool(Tool):
    def __init__(self, name):
        self.name = name
        if self.name == ToolRectangle:
            self._drawing = DrawingShapeRectangle()
        elif self.name == ToolEllipse:
            self._drawing = DrawingShapeEllipse()
        elif self.name == ToolLine:
            self._drawing = DrawingShapeLine()
        else:
            pass
            # if self._drawing is None throw an exception

    def setStrokeStyle(self, width, caps, joins, dash, dashphase):
        self._drawing.setDashStyle(dash, dashphase)
        self._drawing.setEndCaps(caps)
        self._drawing.setLineJoins(joins)
        self._drawing.setStrokeWidth(width)

    # Delegate the mouse events from a QWidget to this tool
    def mousePressed(self, point):
        self._drawing.setBounds(point.x(), point.y(), point.x(), point.y())

    # Delegate the mouse events from a QWidget to this tool
    def mouseDragged(self, point):
        self._drawing.setBounds(self._drawing.x1, self._drawing.y1, point.x(), point.y())

    def mouseMoved(self, point):
        pass

    # Delegate the mouse events from a QWidget to this tool
    def mouseReleased(self, point):
        pass

    # The tool name
    def name(self):
        return self.name

    @property
    def drawing(self):
        return self._drawing

class ControlTool(Tool):
    def __init__(self, name):
        self.name = name
        if self.name == ToolSelector:
            self._drawing = None
        else:
            pass #throw except

    def setDrawing(self, drawing):
        self._drawing = drawing

    # Delegate the mouse events from a QWidget to this tool
    def mousePressed(self, point):
        pass #self._drawing.setBounds(point.x(), point.y(), point.x(), point.y())

    # Delegate the mouse events from a QWidget to this tool
    def mouseDragged(self, point):
        pass #self._drawing.setBounds(self._drawing.x1, self._drawing.y1, point.x(), point.y())

    def mouseMoved(self, point):
        pass

    # Delegate the mouse events from a QWidget to this tool
    def mouseReleased(self, point):
        pass

    # The tool name
    def name(self):
        return self.name

    @property
    def drawing(self):
        return self._drawing

class Drawing(QtGui.QGraphicsItem):
    def __init__(self):
        QtGui.QGraphicsItem.__init__(self)

    @property
    def pen(self):
        return self._pen

    @property
    def brush(self):
        return self._brush
    
    @property
    def renderHint(self):
        return self._renderHint

    @property
    def alpha(self):
        return self._alpha

    @property
    def number(self):
        return self._number

    @property
    def name(self):
        return self._name
    
    @property
    def x1(self):
        return self._x1

    @property
    def x2(self):
        return self._x2

    @property
    def y1(self):
        return self._y1

    @property
    def y2(self):
        return self._y2

    @number.setter
    def number(self, value):
        self._number = value

    @alpha.setter
    def alpha(self, value):
        self._alpha = value

    @renderHint.setter
    def renderHint(self, value):
        self._renderHint = value

    @brush.setter
    def brush(self, value):
        self._brush = value
 
    @pen.setter
    def pen(self, value):
        self._pen = value
    
    @name.setter
    def name(self, value):
        self._name = value

    @x1.setter
    def x1(self, value):
        self._x1 = value

    @x2.setter
    def x2(self, value):
        self._x2 = value

    @y1.setter
    def y1(self, value):
        self._y1 = value

    @y2.setter
    def y2(self, value):
        self._y2 = value

    def setBounds(self, x1, y1, x2, y2):
        self._x1 = x1
        self._y1 = y1
        self._x2 = x2
        self._y2 = y2
        self.prepareGeometryChange()

    def boundingRect(self):
        offset = self.pen.width()/2
        return QtCore.QRectF(
            (self.x1 if self.x1-self.x2<=0 else self.x2) - offset,
            (self.y1 if self.y1-self.y2<=0 else self.y2) - offset,
            abs(self.x2 - self.x1) + self.pen.width(),
            abs(self.y2 - self.y1) + self.pen.width())

    def paint(self, painter, option, widget = None):
        if self.shouldStroke:
            painter.setPen(self.pen)
        else:
            painter.setPen(QColor(0,0,0,0))
        if self.shouldFill:
            painter.setBrush(self.brush)
        else:
            painter.setBrush(QColor(0,0,0,0))
        painter.drawPath(self.shape)


class DrawingText(Drawing):
    # Some useful things for wrapping tellapic values to Qt values.
    PytellapicFontStyle = {
        pytellapic.FONT_STYLE_NORMAL : QFont.StyleNormal,
        pytellapic.FONT_STYLE_ITALIC : QFont.StyleItalic
        }

    # Initiate properties default values
    def initialValues(self):
        self._pen        = QPen(QColor(), 0.1)
        self._brush      = QBrush(QColor(0,0,0,0))
        self._renderHint = QPainter.TextAntialiasing | QPainter.HighQualityAntialiasing
        self._alpha      = 255
        self._number     = 0
        self._name       = 'No Name Yet'
        self._x1 = self._x2 = self._y1 = self._y2 = 0

    # Instantiates the DrawingText with a specific number if provided
    def __init__(self, number = None):
        if number is not None:
            self.number = number
        self.setFont(QColor(), QFont.StyleNormal, 'Serif', 12)
        self.initialValues()

    # Sets the font to be used to display the text
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
        self.brush = QColor(color.red(), color.green(), color.blue(), color.alpha())

    # Sets the text boundary.
    def setBounds(self, x1, x2, y1, y2):
        super(DrawingText, self).setBounds(x1, x2, y1, y2)
        self.shape = QPainterPath()
        self.shape.addText(x1, x2, self.font, self.text)

    @property
    def text(self):
        return self._text

    # Sets the text string to draw
    @text.setter
    def text(self, text):
        self._text = text
    
    # Draws the text
    def draw(self, painter):
        supe(DrawingText, self).draw(painter)
        painter.drawPath(self.shape)

    def hasFontProperties(self):
        return True

    def hasStrokeStylesProperties(self):
        return False

    def hasStrokeColorProperties(self):
        return False

    def hasFillColorProperties(self):
        return True

    def hasTransparentProperties(self):
        return True

# This could be also an abstract class for shapes
class DrawingShape(Drawing):

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

     # Initiate properties default values
    def initialValues(self):
        self._pen        = QPen(QColor(), 5)
        self._brush      =  QBrush(QColor(0,0,0,255))
        self._renderHint = QPainter.TextAntialiasing | QPainter.HighQualityAntialiasing
        self._alpha      = 255
        self._number     = 0
        self._name       = 'No Name Yet'
        self._x1 = self._x2 = self._y1 = self._y2 = 0

    def __init__(self, number = None):
        super(DrawingShape, self).__init__()
        if number is not None:
            self.number = number
        self.shape = QPainterPath()
        self.shouldFill = False
        self.shouldStroke = True
        self.initialValues()

    # Draws the shape
    def draw(self, painter):
        super(DrawingShape, self).draw(painter)
        painter.drawPath(self.shape)
    
    # Sets the pen width
    def setStrokeWidth(self, width):
        self.pen.setWidth(width)
        self.update(self.boundingRect())

    # Sets how the strokes will be joined
    def setLineJoins(self, lj):
        #print("Setting line joins to: ", lj)
        self.pen.setJoinStyle(self.PytellapicJoinsStyle[lj])
        self.update(self.boundingRect())

    # Sets how the strokes will end
    def setEndCaps(self, ec):
        #print("Setting end caps to: ", ec)
        self.pen.setCapStyle(self.PytellapicCapsStyle[ec])
        self.update(self.boundingRect())

    # Sets the color of the stroke
    def setStrokeColor(self, color):
        self.pen.setColor(QColor(color.red(), color.green(), color.blue(), color.alpha()))
        self.update(self.boundingRect())

    def setFillColor(self, color):
        self.brush.setColor(QColor(color.red(), color.green(), color.blue(), color.alpha()))
        self.update(self.boundingRect())

    def setMiterLimit(self, ml):
        self.pen.setMiterLimit(ml)
        self.update(self.boundingRect())

    def setDashStyle(self, phase, array):
        self.pen.setDashOffset(phase)
        self.pen.setDashPattern(array)
        self.update(self.boundingRect())

    def setStrokeEnabled(self, enabled):
        self.shouldStroke = enabled
        self.update(self.boundingRect())

    def setFillEnabled(self, enabled):
        self.shouldFill = enabled
        self.update(self.boundingRect())

    def hasFontProperties(self):
        return False

    def hasStrokeStylesProperties(self):
        return True

    def hasStrokeColorProperties(self):
        return True
    
    def hasFillColorProperties(self):
        return True

    def hasTransparentProperties(self):
        return True

    def setBounds(self, x1, y1, x2, y2):
        super(DrawingShape, self).setBounds(x1, y1, x2, y2)



# This class should be concrete. He knows exactly that it consists
# of a shape as a rectangle.
class DrawingShapeRectangle(DrawingShape):

    # Calling the base class DrawingShpae constructor will
    # instantiates a QPainterPath() object: self.shape
    def __init__(self, number = None):
        #super(DrawingShapeRectangle, self).__init__()
        DrawingShape.__init__(self)
        self.setDefaultValues()
        self.name = "rectangle"

    def setDefaultValues(self):
        self.pen.setCapStyle(self.PytellapicCapsStyle[pytellapic.END_CAPS_ROUND])
        self.pen.setJoinStyle(self.PytellapicJoinsStyle[pytellapic.LINE_JOINS_MITER])
        self.pen.setStyle(Qt.SolidLine)

    # Every time setBounds is called, it will create a new QPainterPath() object
    # This was written for simplicity.
    def setBounds(self, x1, y1, x2, y2):
        super(DrawingShapeRectangle, self).setBounds(x1, y1, x2, y2)
        if self.shape.isEmpty() is not True:
            self.shape = QPainterPath()
        self.rect = QRectF(
            x1 if x1-x2<=0 else x2,
            y1 if y1-y2<=0 else y2, 
            abs(x1-x2),
            abs(y1-y2))
        self.shape.addRect(self.rect)



class DrawingShapeEllipse(DrawingShape):
    def __init__(self, drawing = None):
        super(DrawingShapeEllipse, self).__init__()
        self.setDefaultValues()
        self.name = "ellipse"

    def setDefaultValues(self):
        self.pen.setCapStyle(self.PytellapicCapsStyle[pytellapic.END_CAPS_ROUND])
        self.pen.setJoinStyle(self.PytellapicJoinsStyle[pytellapic.LINE_JOINS_MITER])
        self.pen.setStyle(Qt.SolidLine)

    # Every time setBounds is called, it will create a new QPainterPath() object
    # This was written for simplicity.
    def setBounds(self, x1, y1, x2, y2):
        super(DrawingShapeEllipse, self).setBounds(x1, y1, x2, y2)
        if self.shape.isEmpty() is not True:
            self.shape = QPainterPath()
        self.rect = QRectF(
            x1 if x1-x2<=0 else x2,
            y1 if y1-y2<=0 else y2, 
            abs(x1-x2),
            abs(y1-y2))
        self.shape.addEllipse(self.rect)


class DrawingShapeLine(DrawingShape):
    def __init__(self, drawing = None):
        super(DrawingShapeLine, self).__init__()
        self.setDefaultValues()

    def setDefaultValues(self):
        self.pen.setCapStyle(self.PytellapicCapsStyle[pytellapic.END_CAPS_SQUARE])
        self.pen.setJoinStyle(self.PytellapicJoinsStyle[pytellapic.LINE_JOINS_MITER])
        self.pen.setStyle(Qt.SolidLine)

    # Every time setBounds is called, it will create a new QPainterPath() object
    # This was written for simplicity.
    def setBounds(self, x1, y1, x2, y2):
        super(DrawingShapeLine, self).setBounds( x1, y1, x2, y2)
        if self.shape.isEmpty() is not True:
            self.shape = QPainterPath()
        self.shape.moveTo(x1, y1)
        self.shape.lineTo(x2, y2)
        self.shape.closeSubpath()    


class ToolModel(QObject):
    toolChanged = QtCore.pyqtSignal(QString)

    def __init__(self):
        QObject.__init__(self) 
        #self.subscribers = []
        self.tools = []
        self.lastUsedTool = None
        self.tools.append(DrawingShapeTool(ToolRectangle))
        self.tools.append(DrawingShapeTool(ToolEllipse))
        self.tools.append(DrawingShapeTool(ToolLine))
        self.tools.append(DrawingShapeTool(ToolMarker))
        self.tools.append(DrawingShapeTool(ToolPen))
        self.tools.append(DrawingTextTool())
        self.tools.append(ControlTool(ToolSelector))

    def setTool(self, toolName):
        for tool in self.tools:
            print("tool: ", tool.name)
            if tool.name == toolName and self.lastUsedTool != tool:
                print("set tool: ",toolName)
                self.lastUsedTool = tool
                #for member in self.subscribers:
                    #QtGui.QApplication.postEvent(member, MyEvent(tool))
                self.emit(QtCore.SIGNAL("toolChanged(QString)"), toolName)

    def getLastUsedTool(self):
        return self.lastUsedTool

    def getToolByName(self, toolName):
        for tool in self.tools:
            if tool.name == toolName:
                return tool
        
    #def subscribe(self, o):
    #    self.subscribers.append(o)
        
    #def unsubscribe(self, o):
    #    self.subscribers.remove(o)

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
        self.drawingList.append(drawing)
        self.drawingList.sort(key=lambda drawing: drawing.number)

    def getDrawing(self, key):
        index = self.binSearch(0, len(self.drawingList) - 1, key)
        drawing = None
        if (index != -1):
            drawing = self.drawingList[index]
        return drawing

    def drawings(self):
        return self.drawingList

class TellapicScene(QtGui.QGraphicsScene):
    drawingSelectionChanged = QtCore.pyqtSignal(QString)

    def __init__(self, model, parent = None):
        super(TellapicScene, self).__init__(parent)
        self.model = model
        #self.model.subscribe(self)
        self.model.toolChanged.connect(self.update)
        self.tool = model.getLastUsedTool()
        self.addPixmap(QtGui.QPixmap("bart.jpg"))
        print("TellapicScene instantiated.")
        self.temporalItem = None

    def mousePressEvent(self, event):
        pos = event.scenePos()
        print("Mouse pressed on TellpicScene at: ", pos.x(), pos.y())
        self.dragging = 1
        if self.tool is not None:
            self.tool.mousePressed(pos)
            #self.temporalItem = self.tool.drawing.item
            self.addItem(self.tool.drawing)

    def mouseMoveEvent(self, event):
        pos = event.scenePos()
        if self.dragging:
            print("Mouse dragged on TellpicScene at: ", pos.x(), pos.y())
            if self.tool is not None:
                self.tool.mouseDragged(pos)
        else:
            print("Mouse moved on TellpicScene at: ", pos.x(), pos.y())
            if self.tool is not None:
                self.tool.mouseMoved(pos)

    def mouseReleaseEvent(self, event):
        pos = event.scenePos()
        print("Mouse released on TellpicScene at: ", pos.x(), pos.y())
        self.dragging = 0
        if self.tool is not None:
            pass#drawing = self.tool.mouseReleased(pos)
        print(self.items())

    def update(self, toolName):
        self.tool = self.model.getToolByName(toolName)

