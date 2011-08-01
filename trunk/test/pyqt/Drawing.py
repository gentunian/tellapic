'''
 Python Test Module for tellapic

 @author: Sebastian Treu
'''
from PyQt4 import *
from PyQt4.QtCore import *
from PyQt4.QtGui import *
import abc
import pytellapic
#from Utils import MyEvent
import sys
try:
    from PyQt4.QtCore import QString
except ImportError:
    # we are using Python3 so QString is not defined
    QString = type("")

if not hasattr(sys, "hexversion") or sys.hexversion < 0x03000000:
    from nonconflict import classmaker

ToolRectangle = "rectangle"
ToolEllipse   = "ellipse"
ToolLine      = "line"
ToolSelector  = "selector"
ToolMarker    = "marker"
ToolPen       = "pen"
PytellapicJoinsStyle = {
    Qt.MiterJoin : pytellapic.LINE_JOINS_MITER,
    Qt.RoundJoin : pytellapic.LINE_JOINS_ROUND,
    Qt.BevelJoin : pytellapic.LINE_JOINS_BEVEL
    }
PytellapicCapsStyle = {
    Qt.FlatCap   : pytellapic.END_CAPS_BUTT,
    Qt.RoundCap  : pytellapic.END_CAPS_ROUND,
    Qt.SquareCap : pytellapic.END_CAPS_SQUARE
    }
PytellapicFontStyle = {
    QFont.StyleNormal : pytellapic.FONT_STYLE_NORMAL,
    QFont.StyleItalic : pytellapic.FONT_STYLE_ITALIC
    }

class Tool(object):
    """Tool abstract class.
    An abstract base class for each <Some>Tool. It defines methods to be
    overriden by concrete implementations.
    """
    __metaclass__ = abc.ABCMeta

    def __init__(self, name, model):
        self._name = name
        self.model = model

    @property
    def name(self):
        return self._name

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
    
    @abc.abstractmethod
    def canDraw(self):
        return

class DrawingTool(Tool):
    """DrawingTool class.
    An abstract Tool subclass that defines tools able to draw something.
    """
    __metaclass__ = abc.ABCMeta

    def __init__(self, name, model):
        super(DrawingTool, self).__init__(name, model)

    def canDraw(self):
        return True

    @property
    def drawing():
        return self._drawing

    @abc.abstractmethod
    def hasFontProperties(self):
        return

    @abc.abstractmethod
    def hasStrokeStylesProperties(self):
        return

    @abc.abstractmethod
    def hasStrokeColorProperties(self):
        return

    @abc.abstractmethod
    def hasFillColorProperties(self):
        return

    @abc.abstractmethod
    def hasTransparentProperties(self):
        return

class DrawingToolRectangle(DrawingTool):
    """DrawingToolRectangle class.
    Concrete subclass that implements a DrawingTool. This class instantiates a rectangle shape and 
    uses it to generate a DrawingShape.
    """
    def __init__(self, model):
        super(DrawingToolRectangle, self).__init__(ToolRectangle, model)

    # Delegate the mouse events from a QWidget to this tool
    def mousePressed(self, point):
        self._drawing = DrawingShapeRectangle()
        self._drawing.setPen(self.model.pen)
        self._drawing.setBrush(self.model.brush)
        self._drawing.setStrokeEnabled(self.model.shouldStroke)
        self._drawing.setFillEnabled(self.model.shouldFill)
        self._drawing.setBounds(point.x(), point.y(), point.x(), point.y())

    # Delegate the mouse events from a QWidget to this tool
    def mouseDragged(self, point):
        self._drawing.setBounds(self._drawing.x1, self._drawing.y1, point.x(), point.y())

    def mouseMoved(self, point):
        pass

    # Delegate the mouse events from a QWidget to this tool
    def mouseReleased(self, point):
        self._drawing.setPen(QPen(self.model.pen))
        self._drawing.setBrush(QBrush(self.model.brush))

    @property
    def drawing(self):
        return self._drawing

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

class ControlTool(Tool):
    """ControlTool abstract class.
    This class defines tools able to do some sort of control.
    """
    def __init__(self, name):
        pass

    @property
    def drawing(self):
        return self._drawing

class Drawing(QtGui.QGraphicsItem):
    def __init__(self):
        #QtGui.QGraphicsItem.__init__(self)
        super(Drawing, self).__init__()
        print("Drawing constructor.")
        self.setActive(True)
        self.setEnabled(True)
        self.selectedStroke = QPen(QColor("yellow"), 1, Qt.DashLine, Qt.SquareCap, Qt.MiterJoin)
        self.bounds = QRectF()
        self.stream = pytellapic.stream_t()

    def setBounds(self, x1, y1, x2, y2):
        self._x1 = x1
        self._y1 = y1
        self._x2 = x2
        self._y2 = y2
        offset = 0 if self.pen is None else self.pen.width()
        self.bounds = QRectF(
            (x1 if x1-x2<=0 else x2) - offset / 2,
            (y1 if y1-y2<=0 else y2) - offset / 2, 
            abs(x1-x2) + offset,
            abs(y1-y2) + offset
            )
        self.prepareGeometryChange()

    def boundingRect(self):
        offset = self.pen.width()/2
        return self.bounds

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
        
class DrawingText(Drawing):
    # Some useful things for wrapping tellapic values to Qt values.

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
        print("DrawingText constructor.")
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

    def initialValues(self):
        self.pen        = QPen(QColor(), 5)
        self.brush      =  QBrush(QColor(0,0,0,255))
        self.renderHint = QPainter.TextAntialiasing | QPainter.HighQualityAntialiasing
        self.alpha      = 255
        self.number     = 0
        self.name       = 'No Name Yet'
        self.x1 = self.x2 = self.y1 = self.y2 = 0
        self.setFlag(QtGui.QGraphicsItem.ItemIsMovable, True)
        self.setFlag(QtGui.QGraphicsItem.ItemIsSelectable, True)
        self.setFlag(QtGui.QGraphicsItem.ItemClipsToShape, True)
        self.setFlag(QtGui.QGraphicsItem.ItemSendsScenePositionChanges, True)
        self.setAcceptTouchEvents(True)
        self.shouldFill = False
        self.shouldStroke = True

    def __init__(self, number = None):
        super(DrawingShape, self).__init__()
        print("DrawingShape constructor.")
        if number is not None:
            self.number = number
        self.shape = QPainterPath()
        self.initialValues()

    def draw(self, painter):
        super(DrawingShape, self).draw(painter)
        painter.drawPath(self.shape)
    
    def setPen(self, pen):
        self.pen = pen
        self.update(self.boundingRect())

    def setBrush(self, brush):
        self.brush = brush
        self.update(self.boundingRect())

    def setStrokeWidth(self, width):
        self.pen.setWidth(width)
        self.update(self.boundingRect())

    def setLineJoins(self, lj):
        self.pen.setJoinStyle(self.PytellapicJoinsStyle[lj])
        self.update(self.boundingRect())

    def setEndCaps(self, ec):
        self.pen.setCapStyle(self.PytellapicCapsStyle[ec])
        self.update(self.boundingRect())

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

    def setBounds(self, x1, y1, x2, y2):
        super(DrawingShape, self).setBounds(x1, y1, x2, y2)


# This class should be concrete. He knows exactly that it consists
# of a shape as a rectangle.
class DrawingShapeRectangle(DrawingShape):

    # Calling the base class DrawingShpae constructor will
    # instantiates a QPainterPath() object: self.shape
    def __init__(self, number = None):
        super(DrawingShapeRectangle, self).__init__()
        print("DrawingShapeRectangle constructor.")
        self.setDefaultValues()
        self.name = "rectangle"

    def setDefaultValues(self):
        self.pen.setCapStyle(Qt.SquareCap)
        self.pen.setJoinStyle(Qt.MiterJoin)
        self.pen.setStyle(Qt.SolidLine)

    # Every time setBounds is called, it will create a new QPainterPath() object
    # This was written for simplicity.
    def setBounds(self, x1, y1, x2, y2):
        super(DrawingShapeRectangle, self).setBounds(x1, y1, x2, y2)
        if self.shape.isEmpty() is not True:
            self.shape = QPainterPath()

        rect = QRectF(
            x1 if x1-x2<=0 else x2,
            y1 if y1-y2<=0 else y2, 
            abs(x1-x2),
            abs(y1-y2))
        self.shape.addRect(rect)

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

class ToolBoxModel(QObject):
    toolChanged = QtCore.pyqtSignal(QString)

    def __init__(self):
        QObject.__init__(self) 
        self.initialValues()
        self.tools = []
        self.lastUsedTool = None
        self.tools.append(DrawingToolRectangle(self))
 
    def initialValues(self):
        self.shouldStroke = True
        self.shouldFill   = False
        self.fontPropertyEnabled = False
        self.strokePropertyEnabled = False
        self.fillPropertyEnabled = False
        self.brush = QBrush(QColor(0, 0, 0, 0,))
        self.pen = QPen(QColor(), 5, Qt.SolidLine, Qt.SquareCap, Qt.MiterJoin)

    def setTool(self, toolName):
        for tool in self.tools:
            print("tool: ", tool.name)
            if tool.name == toolName and self.lastUsedTool != tool:
                print("set tool: ",toolName)
                self.lastUsedTool = tool
                self.configureToolBox(tool)
                self.emit(QtCore.SIGNAL("toolChanged(QString)"), toolName)

    def configureToolBox(self, tool):
        if tool.canDraw():
            self.fontPropertyEnabled = tool.hasFontProperties()
            self.strokePropertyEnabled = tool.hasStrokeStylesProperties()
            self.fillPropertyEnabled = tool.hasFillColorProperties()

    def getLastUsedTool(self):
        return self.lastUsedTool

    def getToolByName(self, toolName):
        for tool in self.tools:
            if tool.name == toolName:
                return tool

    def setStrokeWidth(self, width):
        self.pen.setWidth(width)

    def setLineJoins(self, joins):
        self.pen.setJoinStyle(joins)

    def setEndCaps(self, caps):
        self.pen.setCapStyle(caps)

    def setStrokeColor(self, color):
        self.pen.setColor(QColor(color.red(), color.green(), color.blue(), color.alpha()))

    def setFillColor(self, color):
        self.brush.setColor(QColor(color.red(), color.green(), color.blue(), color.alpha()))

    def setMiterLimit(self, ml):
        self.pen.setMiterLimit(ml)

    def setDashStyle(self, phase, array):
        self.pen.setDashOffset(phase)
        self.pen.setDashPattern(array)

    def setStrokeEnabled(self, enabled):
        self.shouldStroke = enabled

    def setFillEnabled(self, enabled):
        self.shouldFill = enabled

    def isFontPropertyEnabled(self):
        return self.fontPropertyEnabled

    def isStrokePropertyEnabled(self):
        return self.strokePropertyEnabled

    def isFillPropertyEnabled(self):
        return self.fillPropertyEnabled

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

class SelectedEffect(QtGui.QGraphicsDropShadowEffect):
    def __init__(self, parent = None):
        super(SelectedEffect, self).__init__(parent)
        self.pen = QPen(QColor("yellow"), 1, Qt.DashLine, Qt.SquareCap, Qt.MiterJoin)
        self.setBlurRadius(6)

    def draw(self, painter):
        super(SelectedEffect, self).draw(painter)
        painter.setPen(self.pen)
        rect = self.sourceBoundingRect()
        painter.drawRect(rect.x()+1, rect.y()+1, rect.width()-1, rect.height()-1)

class TellapicScene(QtGui.QGraphicsScene):
    drawingSelectionChanged = QtCore.pyqtSignal(QString)

    def __init__(self, model, parent = None):
        super(TellapicScene, self).__init__(parent)
        self.model = model
        self.model.toolChanged.connect(self.update)
        self.tool = model.getLastUsedTool()
        self.background = QtGui.QPixmap("bart.jpg")
        print("TellapicScene instantiated.")
        self.temporalItem = None
        self.setSceneRect(0, 0, self.background.width(), self.background.height())
        #self.setForegroundBrush(QBrush(Qt.lightGray, Qt.CrossPattern))
        self.selectedEffect = SelectedEffect()

    def mousePressEvent(self, event):
        pos = event.scenePos()
        print("Mouse pressed on TellpicScene at: ", pos.x(), pos.y())
        self.item = self.itemAt(pos)
        self.clearSelection()
        if self.item is not None:
            print("item at ",pos," is ", self.item)
            self.item.setSelected(True)
            self.item.setGraphicsEffect(self.selectedEffect)
            self.item.mousePressEvent(event)
        else:
            self.dragging = 1
            if self.tool is not None:
                self.tool.mousePressed(pos)
            #self.temporalItem = self.tool.drawing.item
                self.addItem(self.tool.drawing)

    def mouseMoveEvent(self, event):
        pos = event.scenePos()
        if self.item is not None:
            self.item.mouseMoveEvent(event)
        else:
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
        if self.item is not None:
            print("Mouse released when item enabled on TellpicScene at: ", pos.x(), pos.y())
            self.item.mouseReleaseEvent(event)
        else:
            print("Mouse released on TellpicScene at: ", pos.x(), pos.y())
            self.dragging = 0
            if self.tool is not None:
                self.tool.mouseReleased(pos)
                print(self.items())

    def update(self, toolName):
        self.tool = self.model.getToolByName(toolName)

    def drawBackground(self, painter, rect):
        r = rect.toRect()
        painter.drawPixmap(r.x(), r.y(),
                           self.background,
                           r.x(), r.y(),
                           self.background.width(), self.background.height()
                           )

