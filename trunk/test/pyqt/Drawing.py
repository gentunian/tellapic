'''
 Python Test Module for tellapic

 @author: Sebastian Treu
'''
import sys
import abc

from PyQt4.QtCore import Qt
from PyQt4.QtCore import SIGNAL
from PyQt4.QtCore import QObject
from PyQt4.QtCore import QRectF
from PyQt4.QtCore import QPoint
from PyQt4.QtCore import QPointF
from PyQt4.QtCore import pyqtSignal
from PyQt4.QtCore import QTimeLine

from PyQt4.QtGui import QPainter
from PyQt4.QtGui import QPainterPath
from PyQt4.QtGui import QColor
from PyQt4.QtGui import QPen
from PyQt4.QtGui import QFont
from PyQt4.QtGui import QGraphicsItem
from PyQt4.QtGui import QGraphicsEllipseItem
from PyQt4.QtGui import QGraphicsDropShadowEffect
from PyQt4.QtGui import QGraphicsScene
from PyQt4.QtGui import QGraphicsItemAnimation
from PyQt4.QtGui import QPixmap

import string

try:
    from PyQt4.QtCore import QString
except ImportError:
    # we are using Python3 so QString is not defined
    QString = type("")

if not hasattr(sys, "hexversion") or sys.hexversion < 0x03000000:
    from nonconflict import classmaker

import pytellapic
from Utils import TellapicEvent
from Utils import Enum

ToolRectangle = "rectangle"
ToolEllipse   = "ellipse"
ToolLine      = "line"
ToolSelector  = "selector"
ToolMarker    = "marker"
ToolPen       = "pen"
PytellapicJoinsStyleMap = {
    Qt.MiterJoin : pytellapic.LINE_JOINS_MITER,
    Qt.RoundJoin : pytellapic.LINE_JOINS_ROUND,
    Qt.BevelJoin : pytellapic.LINE_JOINS_BEVEL
    }
PytellapicCapsStyleMap = {
    Qt.FlatCap   : pytellapic.END_CAPS_BUTT,
    Qt.RoundCap  : pytellapic.END_CAPS_ROUND,
    Qt.SquareCap : pytellapic.END_CAPS_SQUARE
    }
PytellapicFontStyleMap = {
    QFont.StyleNormal : pytellapic.FONT_STYLE_NORMAL,
    QFont.StyleItalic : pytellapic.FONT_STYLE_ITALIC,
    QFont.Bold        : pytellapic.FONT_STYLE_BOLD,
    QFont.Bold + QFont.StyleItalic : pytellapic.FONT_STYLE_BOLD_ITALIC
    }
PytellapicControlByte = {
    pytellapic.CTL_CL_BMSG     : 'CTL_CL_BMSG',
    pytellapic.CTL_CL_PMSG     : 'CTL_CL_PMSG',
    pytellapic.CTL_CL_FIG      : 'CTL_CL_FIG',
    pytellapic.CTL_CL_DRW      : 'CTL_CL_DRW',
    pytellapic.CTL_CL_CLIST    : 'CTL_CL_CLIST', 
    pytellapic.CTL_CL_PWD      : 'CTL_CL_PWD',
    pytellapic.CTL_CL_FILEASK  : 'CTL_CL_FILEASK',
    pytellapic.CTL_CL_FILEOK   : 'CTL_CL_FILEOK', 
    pytellapic.CTL_CL_DISC     : 'CTL_CL_DISC', 
    pytellapic.CTL_CL_NAME     : 'CTL_CL_NAME',
    pytellapic.CTL_SV_CLRM     : 'CTL_SV_CLRM',
    pytellapic.CTL_SV_CLADD    : 'CTL_SV_CLADD',
    pytellapic.CTL_SV_CLIST    : 'CTL_SV_CLIST',
    pytellapic.CTL_SV_PWDASK   : 'CTL_SV_PWDASK',
    pytellapic.CTL_SV_PWDOK    : 'CTL_SV_PWDOK',
    pytellapic.CTL_SV_PWDFAIL  : 'CTL_SV_PWDFAIL',
    pytellapic.CTL_SV_FILE     : 'CTL_SV_FILE', 
    pytellapic.CTL_SV_ID       : 'CTL_SV_ID',
    pytellapic.CTL_SV_NAMEINUSE: 'CTL_SV_NAMEINUSE',
    pytellapic.CTL_SV_AUTHOK   : 'CTL_SV_AUTHOK',
    pytellapic.CTL_FAIL        : 'CTL_FAIL'
    }
QtLineJoinsMap = {
    pytellapic.LINE_JOINS_MITER : Qt.MiterJoin,
    pytellapic.LINE_JOINS_BEVEL : Qt.BevelJoin,
    pytellapic.LINE_JOINS_ROUND : Qt.RoundJoin
    }
QtEndCapsMap = {
    pytellapic.END_CAPS_SQUARE : Qt.SquareCap,
    pytellapic.END_CAPS_ROUND  : Qt.RoundCap,
    pytellapic.END_CAPS_BUTT   : Qt.FlatCap
}
QtFontStyleMap = {
    pytellapic.FONT_STYLE_NORMAL      : QFont.StyleNormal,
    pytellapic.FONT_STYLE_ITALIC      : QFont.StyleItalic,
    pytellapic.FONT_STYLE_BOLD_ITALIC : QFont.StyleItalic,
    pytellapic.FONT_STYLE_BOLD        : QFont.StyleNormal
}
QtFontWeightMap = {
    pytellapic.FONT_STYLE_BOLD        : QFont.Bold,
    pytellapic.FONT_STYLE_BOLD_ITALIC : QFont.Bold
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
    def drawing(self):
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
        self._drawing = DrawingShapeRectangle.withModel(self.model)
        '''
        self._drawing.setPen(self.model.pen)
        self._drawing.setBrush(self.model.brush)
        self._drawing.setStrokeEnabled(self.model.shouldStroke)
        self._drawing.setFillEnabled(self.model.shouldFill)
        '''
        self._drawing.setBounds(point.x(), point.y(), point.x(), point.y())

    # Delegate the mouse events from a QWidget to this tool
    def mouseDragged(self, point):
        self._drawing.setBounds(self._drawing.point1()[0],
                                self._drawing.point1()[1],
                                point.x(),
                                point.y()
                                )

    def mouseMoved(self, point):
        pass

    # Delegate the mouse events from a QWidget to this tool
    def mouseReleased(self, point):
        #self._drawing.setPen(QPen(self.model.pen))
        #self._drawing.setBrush(QBrush(self.model.brush))
        self._drawing.printComprehensiveDataInfo()

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

class DrawingControlPoint(QGraphicsEllipseItem):
    ControlPointType = Enum(["TOP_LEFT",
                             "TOP_RIGHT",
                             "BOTTOM_LEFT",
                             "BOTTOM_RIGHT",
                             "TOP",
                             "BOTTOM",
                             "LEFT",
                             "RIGHT"
                            ])
    
    __metaclass__ = abc.ABCMeta
    
    def __init__(self, parent):
        super(DrawingControlPoint, self).__init__(parent)
        self.setVisible(False)
        self.setBrush(QColor("white"))
        self.setAcceptHoverEvents(True)
        self.setFlag(QGraphicsItem.ItemIsSelectable, True)
        self.setRect(-4, -4, 8, 8)

    def controlPointType(self):
        return self._type

    def mousePressEvent(self, event):
        print("press event: ",event.pos())
        self.drag = 1

    def mouseReleaseEvent(self, event):
        self.drag = 0
        print("release event: ",event.pos())
        
    def hoverEnterEvent(self, event):
        self.setBrush(QColor("blue"))
        self.setSelected(True)
        self.setZValue(1)

    def hoverLeaveEvent(self, event):
        self.setBrush(QColor("white"))
        self.setSelected(False)
        self.setZValue(0)

    def paint(self, painter, option, widget):
        super(DrawingControlPoint, self).paint(painter, option, widget)
        if self.isSelected():
            painter.setPen(QColor("blue"))
            painter.drawLine(0,-9000,0,9000)
            painter.drawLine(-9000,0,9000,0)

class DrawingControlPointTopLeft(DrawingControlPoint):
    def __init__(self, parent):
        super(DrawingControlPointTopLeft, self).__init__(parent)
        self._type = self.ControlPointType.TOP_LEFT
        self.setCursor(Qt.SizeFDiagCursor)

    def updateLocation(self):
        point = self.parentItem().boundingRect()
        self.setPos(point.topLeft())

    def mouseMoveEvent(self, event):
        if self.drag:
            print("drag event: ",event.pos())
            point1 = self.parentItem().point1()
            point2 = self.parentItem().point2()
            self.parentItem().resize(point1[0]+event.pos().x(),
                                     point1[1]+event.pos().y(),
                                     point2[0],
                                     point2[1],
                                     )

class DrawingControlPointTopRight(DrawingControlPoint):
    def __init__(self, parent):
        super(DrawingControlPointTopRight, self).__init__(parent)
        self._type = self.ControlPointType.TOP_RIGHT
        self.setCursor(Qt.SizeBDiagCursor)

    def updateLocation(self):
        point = self.parentItem().boundingRect()
        self.setPos(point.topRight())

    def mouseMoveEvent(self, event):
        if self.drag:
            print("drag event: ",event.pos())
            point1 = self.parentItem().point1()
            point2 = self.parentItem().point2()
            self.parentItem().resize(point1[0],
                                     point1[1]+event.pos().y(),
                                     point2[0]+event.pos().x(),
                                     point2[1]
                                     )

class DrawingControlPointBottomLeft(DrawingControlPoint):
    def __init__(self, parent):
        super(DrawingControlPointBottomLeft, self).__init__(parent)
        self._type = self.ControlPointType.BOTTOM_LEFT
        self.setCursor(Qt.SizeBDiagCursor)

    def updateLocation(self):
        point = self.parentItem().boundingRect()
        self.setPos(point.bottomLeft())

    def mouseMoveEvent(self, event):
        if self.drag:
            print("drag event: ",event.pos())
            point1 = self.parentItem().point1()
            point2 = self.parentItem().point2()
            self.parentItem().resize(point1[0]+event.pos().x(),
                                     point1[1],
                                     point2[0],
                                     point2[1]+event.pos().y(),
                                     )

class DrawingControlPointBottomRight(DrawingControlPoint):
    def __init__(self, parent):
        super(DrawingControlPointBottomRight, self).__init__(parent)
        self._type = self.ControlPointType.BOTTOM_RIGHT
        self.setCursor(Qt.SizeFDiagCursor)

    def updateLocation(self):
        point = self.parentItem().boundingRect()
        self.setPos(point.bottomRight())

    def mouseMoveEvent(self, event):
        if self.drag:
            print("drag event: ",event.pos())
            point1 = self.parentItem().point1()
            point2 = self.parentItem().point2()
            self.parentItem().resize(point1[0],
                                     point1[1],
                                     point2[0]+event.pos().x(),
                                     point2[1]+event.pos().y(),
                                     )

class DrawingControlPointTop(DrawingControlPoint):
    def __init__(self, parent):
        super(DrawingControlPointTop, self).__init__(parent)
        self._type = self.ControlPointType.TOP
        self.setCursor(Qt.SizeVerCursor)

    def updateLocation(self):
        point = self.parentItem().boundingRect()
        self.setPos(point.center().x(), point.top())

    def mouseMoveEvent(self, event):
        if self.drag:
            print("drag event: ",event.pos())
            point1 = self.parentItem().point1()
            point2 = self.parentItem().point2()
            self.parentItem().resize(point1[0],
                                     point1[1]+event.pos().y(),
                                     point2[0],
                                     point2[1],
                                     )
class DrawingControlPointBottom(DrawingControlPoint):
    def __init__(self, parent):
        super(DrawingControlPointBottom, self).__init__(parent)
        self._type = self.ControlPointType.BOTTOM
        self.setCursor(Qt.SizeVerCursor)

    def updateLocation(self):
        point = self.parentItem().boundingRect()
        self.setPos(point.center().x(), point.bottom())

    def mouseMoveEvent(self, event):
        if self.drag:
            print("drag event: ",event.pos())
            point1 = self.parentItem().point1()
            point2 = self.parentItem().point2()
            self.parentItem().resize(point1[0],
                                     point1[1],
                                     point2[0],
                                     point2[1]+event.pos().y(),
                                     )

class DrawingControlPointLeft(DrawingControlPoint):
    def __init__(self, parent):
        super(DrawingControlPointLeft, self).__init__(parent)
        self._type = self.ControlPointType.LEFT
        self.setCursor(Qt.SizeHorCursor)
        
    def updateLocation(self):
        point = self.parentItem().boundingRect()
        self.setPos(point.left(), point.center().y())

    def mouseMoveEvent(self, event):
        if self.drag:
            print("drag event: ",event.pos())
            point1 = self.parentItem().point1()
            point2 = self.parentItem().point2()
            self.parentItem().resize(point1[0]+event.pos().x(),
                                     point1[1],
                                     point2[0],
                                     point2[1],
                                     )

class DrawingControlPointRight(DrawingControlPoint):
    def __init__(self, parent):
        super(DrawingControlPointRight, self).__init__(parent)
        self._type = self.ControlPointType.RIGHT
        self.setCursor(Qt.SizeHorCursor)
        self.updateLocation()
    
    def updateLocation(self):
        point = self.parentItem().boundingRect()
        self.setPos(point.right(), point.center().y())

    '''
    def configureAnimation(self):
        self.timeLine = QTimeLine(10000)
        self.timeLine.setLoopCount(0)
        self.animation = QGraphicsItemAnimation()
        #for i in range(200):
        #    self.animation.setScaleAt(i/400.0, 1 + (i/200.0), 1 + (i/200.0))
        #for i in range(200,400):
        #    self.animation.setScaleAt(i/400.0, 2 - (i/200.0), 2 - (i/200.0))
        #self.animation.setScaleAt(0.0, 1, 1)
        #self.animation.setScaleAt(0.5, 2, 2)
        #self.animation.setScaleAt(1.0, 1, 1)
        self.animation.setItem(self)
        self.animation.setTimeLine(self.timeLine)
    '''

    def mouseMoveEvent(self, event):
        if self.drag:
            print("drag event: ",event.pos())
            point1 = self.parentItem().point1()
            point2 = self.parentItem().point2()
            self.parentItem().resize(point1[0],
                                     point1[1],
                                     point2[0]+event.pos().x(),
                                     point2[1],
                                     )


class Drawing(QGraphicsItem):
    def __init__(self, ddata = None):
        super(Drawing, self).__init__()
        print("Drawing constructor (",id(self)," ddata:",id(ddata))
        self.bounds = QRectF()
        self.controlPoints = []
        if ddata is None:
            self.ddata = pytellapic.ddata_t()
            self.ddata.number = 0 #TODO: set as constant
            self.setFillEnabled(False)
            self.setStrokeEnabled(True)
        else:
            self.__initData(ddata)
        self.setActive(True)
        self.setEnabled(True)
        self.selectedStroke = QPen(QColor("yellow"), 1, Qt.DashLine, Qt.SquareCap, Qt.MiterJoin)
        self.setAcceptHoverEvents(True)
        for type in DrawingControlPoint.ControlPointType:
            className = "DrawingControlPoint"+string.capwords(type.replace("_", " ")).replace(" ", "")
            print(className)
            theClass = eval(className)(self)
            self.controlPoints.append(theClass)

    @classmethod
    def withUser(cls, user):
        obj = cls()
        obj.setUser(user)
        return obj

    def __initData(self, ddata):
        self.ddata = ddata
        self.brush = self.getColorFromData(self.ddata.fillcolor)
        self.pen   = QPen(QColor(), self.ddata.width)
        # Always set fill enabled from incoming streams as they inform
        # their no-fill with a transparent color. Avoid using 'if' cases
        # and fill with a transparent color.
        self.setFillEnabled(True)
        self.setStrokeEnabled(True)
        self.setBounds(self.ddata.point1.x,
                       self.ddata.point1.y,
                       self.ddata.point2.x,
                       self.ddata.point2.y
                       )
        #self.setPos(abs(self.ddata.point1.x - self.ddata.point2.x)/2,
        #            abs(self.ddata.point1.y - self.ddata.point2.y)/2
        #            )

    def setDrawingData(self, ddata):
        self.__initData(ddata)
        
    def setDcbyte(self, dcbyte, dcbyte_ext):
        self.ddata.dcbyte = dcbyte
        self.ddata.dcbyte_ext = dcbyte_ext

    def setNumber(self, number):
        self.ddata.number = number

    def setStrokeWidth(self, width):
        self.ddata.width = width
        self.pen.setWidth(width)
        self.update(self.boundingRect())

    def setOpacity(self, opacity):
        self.ddata.opacity = opacity
        #TODO

    def setFillColor(self, color):
        self.ddata.fillcolor.red   = color.red()
        self.ddata.fillcolor.green = color.green()
        self.ddata.fillcolor.blue  = color.blue()
        self.ddata.fillcolor.alpha = color.alpha()
        self.brush = QColor(color)#color.red(), color.green(), color.blue(), color.alpha())
        self.update(self.boundingRect())

    def setBrush(self, brush):
        self.setFillColor(brush)
        self.update(self.boundingRect())

    def setBounds(self, x1, y1, x2, y2):
        self.ddata.point1.x = int(x1)
        self.ddata.point1.y = int(y1)
        self.ddata.point2.x = int(x2)
        self.ddata.point2.y = int(y2)
        self.setPos(self.ddata.point1.x + abs(self.ddata.point1.x - self.ddata.point2.x)/2,
                    self.ddata.point1.y + abs(self.ddata.point1.y - self.ddata.point2.y)/2
                    )
        self.prepareGeometryChange()
        self.setBoundingRect(self.ddata.point1.x,
                             self.ddata.point1.y,
                             self.ddata.point2.x,
                             self.ddata.point2.y
                            )
        for point in self.controlPoints:
            point.updateLocation()

    def setBoundingRect(self, x1, y1, x2, y2):
        offset = 0 if self.pen is None else self.pen.width()
        '''
        self.bounds = QRectF((x1 if x1-x2<=0 else x2) - offset / 2,
                             (y1 if y1-y2<=0 else y2) - offset / 2, 
                             abs(x1-x2) + offset,
                             abs(y1-y2) + offset
                             )
        '''
        width  = abs(x1-x2)
        height = abs(y1-y2)
        self.bounds = QRectF(-width/2 - offset / 2,
                             -height/2 - offset / 2, 
                             width + offset,
                             height + offset
                             )

    def boundingRect(self):
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
        painter.setRenderHint(self.renderHint, True)
        painter.drawPath(self.shape)

    def setStrokeEnabled(self, enabled):
        self.shouldStroke = enabled
        self.update(self.boundingRect())

    def setFillEnabled(self, enabled):
        self.shouldFill = enabled
        self.update(self.boundingRect())

    @property
    def pen(self):
        return self._pen

    @pen.setter
    def pen(self, value):
        self._pen = value

    @property
    def brush(self):
        return self._brush

    @brush.setter
    def brush(self, value):
        self._brush = value
    
    @property
    def name(self):
        return self._name
    
    @name.setter
    def name(self, value):
        self._name = value

    def setUser(self, user):
        self.user = user

    def point1(self):
        return (self.ddata.point1.x, self.ddata.point1.y)

    def point2(self):
        return (self.ddata.point2.x, self.ddata.point2.y)

    def qtPoint1(self):
        return QPoint(self.ddata.point1.x, self.ddata.point1.y)

    def qtPoint2(self):
        return QPoint(self.ddata.point2.x, self.ddata.point2.y)

    def getColorFromData(self, tellapicColor):
        return QColor(tellapicColor.red,
                            tellapicColor.green,
                            tellapicColor.blue,
                            tellapicColor.alpha
                            )

    def hoverEnterEvent(self, event):
        super(Drawing, self).hoverEnterEvent(event)
        self.setCursor(Qt.OpenHandCursor)
        for point in self.controlPoints:
            point.setVisible(True)
            
    def hoverLeaveEvent(self, event):
        super(Drawing, self).hoverEnterEvent(event)
        for point in self.controlPoints:
            point.setVisible(False)
        
    def mousePressEvent(self, event):
        super(Drawing, self).mousePressEvent(event)
        self.drag = 1
        self.setCursor(Qt.ClosedHandCursor)

    def mouseMoveEvent(self, event):
        super(Drawing, self).mouseMoveEvent(event)

    def mouseReleaseEvent(self, event):
        super(Drawing, self).mouseReleaseEvent(event)
        self.setCursor(Qt.OpenHandCursor)

class DrawingText(Drawing):
    # Instantiates the DrawingText with a specific number if provided
    def __init__(self, ddata = None):
        super(DrawingText, self).__init__(ddata)
        print("DrawingText constructor.")
        self.shape = QPainterPath()
        self.font = QFont()
        if ddata is not None:
            self.__initData(ddata)
        else:
            self.__defaultValues()
        self.setAcceptTouchEvents(True)
        self.renderHint = QPainter.TextAntialiasing
        
    @classmethod
    def withUserAndStream(cls, user, stream):
        drawing = cls(stream.data.drawing)
        drawing.setUser(user)
        return drawing

    @classmethod
    def withModel(cls, model):
        drawing = cls()
        
        return drawing

    def __initData(self, ddata):
        self.pen = QPen(self.getColorFromData(self.ddata.type.text.color))
        self.brush = self.getColorFromData(self.ddata.type.text.color)
        self.font.setFamily(self.ddata.type.text.face)
        self.font.setStyle(QtFontStyleMap[self.ddata.type.text.style])
        try:
            weight = QtFontWeightMap[self.ddata.type.text.style]
        except:
            weight = QFont.Normal
        self.font.setWeight(weight)
        self.font.setPointSize(self.ddata.width)
        self.shouldFill = self.shouldStroke = True
        self.text = self.ddata.type.text.info
        self.setTextBounds(self.ddata.point1.x,
                           self.ddata.point1.y,
                           self.ddata.point2.x,
                           self.ddata.point2.y
                           )

    def __defaultValues(self):
        self.pen        = QPen(QColor())
        self.brush      = QColor(0, 0, 0, 0)
        self.name       = 'No Name Yet'
        self.shouldFill = False
        self.shouldStroke = True
        
    # Sets the font to be used to display the text
    def setDrawingData(self, ddata):
        super(DrawingText, self).setDrawingData(ddata)
        self.__initData(ddata)
        #self.brush = QColor(color.red(), color.green(), color.blue(), color.alpha())

    # Sets the text boundary.
    def setTextBounds(self, x1, y1, x2, y2):
        super(DrawingText, self).setBounds(x1, y1, x2, y2)
        if self.shape.isEmpty() is not True:
            self.shape = QPainterPath()
        self.shape.addText(x1, y1, self.font, self.text)

    @property
    def text(self):
        return self._text

    # Sets the text string to draw
    @text.setter
    def text(self, text):
        self._text = text
    
# This could be also an abstract class for shapes
class DrawingShape(Drawing):
    def __init__(self, ddata = None):
        super(DrawingShape, self).__init__(ddata)
        print("DrawingShape constructor (",id(self)," ddata:",id(ddata))
        # Instantiates the shape object that will be used to draw this object
        self.shape = QPainterPath()
        if ddata is not None:
            # Initiate this object values with the drawing data provided
            self.__initData(ddata)
        else:
            # Sets default values for this object
            self.__defaultValues()
        self.__setItemFlags()
        self.setAcceptTouchEvents(True)
        #self.renderHint = QPainter.HighQualityAntialiasing
        self.renderHint = QPainter.Antialiasing
        
    @classmethod
    def withUserAndStream(cls, user, stream):
        drawing = cls(stream.data.drawing)
        drawing.setUser(user)
        return drawing

    @classmethod
    def withModel(cls, model):
        drawing = cls()
        drawing.setPen(model.pen)
        drawing.setBrush(model.brush)
        drawing.setStrokeEnabled(model.shouldStroke)
        drawing.setFillEnabled(model.shouldFill)
        return drawing
    
    def __defaultValues(self):
        self.pen   = QPen()
        self.brush = QColor()
        self.name  = 'No Name Yet'
        self.setStrokeWidth(5)
        self.setFillColor(QColor(0, 0, 0, 0))
        self.setStrokeColor(QColor())
        self.setLineJoins(Qt.MiterJoin)
        self.setEndCaps(Qt.SquareCap)
        self.setMiterLimit(10)
        self.setDashStyle(0, [1, 0, 1, 0])
        self.setStrokeEnabled(True)
        self.setFillEnabled(False)

    def __setItemFlags(self):
        self.setFlag(QGraphicsItem.ItemIsMovable, True)
        self.setFlag(QGraphicsItem.ItemIsSelectable, True)
        self.setFlag(QGraphicsItem.ItemClipsToShape, True)
        self.setFlag(QGraphicsItem.ItemSendsScenePositionChanges, True)
        self.setFlag(QGraphicsItem.ItemSendsGeometryChanges, True)

    def __initData(self, ddata):
        self.ddata = ddata
        self.pen = QPen(self.getColorFromData(self.ddata.type.figure.color),
                        self.ddata.width,
                        Qt.CustomDashLine,
                        QtEndCapsMap[self.ddata.type.figure.endcaps],
                        QtLineJoinsMap[self.ddata.type.figure.linejoin]
                        )
        self.pen.setDashPattern(self.ddata.type.figure.dash_array)
        self.pen.setDashOffset(self.ddata.type.figure.dash_phase)
        self.pen.setMiterLimit(self.ddata.type.figure.miterlimit)
        #self.brush = QColor(self.getColorFromData(self.ddata.fillcolor))
        self.name = 'Who Knows'
        self.shouldFill = self.shouldStroke = True
        self.setShapeBounds(self.ddata.point1.x,
                            self.ddata.point1.y,
                            self.ddata.point2.x,
                            self.ddata.point2.y
                            )

    def setDrawingData(self, ddata):
        super(DrawingShape, self).setDrawingData(ddata)
        self.__initData(ddata)
        
    def setPen(self, pen):
        self.setStrokeWidth(pen.width())
        self.setLineJoins(pen.joinStyle())
        self.setEndCaps(pen.capStyle())
        self.setStrokeColor(pen.color())
        self.update(self.boundingRect())

    def setStrokeColor(self, color):
        self.ddata.type.figure.color.red   = color.red()
        self.ddata.type.figure.color.green = color.green()
        self.ddata.type.figure.color.blue  = color.blue()
        self.ddata.type.figure.color.alpha = color.alpha()
        self.pen.setColor(color)#QColor(color.red(), color.green(), color.blue(), color.alpha()))
        self.update(self.boundingRect())

    def setLineJoins(self, lj):
        self.ddata.type.figure.linejoin = PytellapicJoinsStyleMap[lj]
        self.pen.setJoinStyle(lj)
        self.update(self.boundingRect())

    def setEndCaps(self, ec):
        self.ddata.type.figure.endcaps = PytellapicCapsStyleMap[ec]
        self.pen.setCapStyle(ec)
        self.update(self.boundingRect())

    def setMiterLimit(self, ml):
        self.ddata.type.figure.miterlimit = ml
        self.pen.setMiterLimit(ml)
        self.update(self.boundingRect())

    def setDashStyle(self, phase, array):
        self.ddata.type.figure.dash_array = array
        self.ddata.type.figure.dash_phase = phase
        self.pen.setDashOffset(phase)
        self.pen.setDashPattern(array)
        self.update(self.boundingRect())

    def setShapeBounds(self, x1, y1, x2, y2):
        super(DrawingShape, self).setBounds(x1, y1, x2, y2)

    def printComprehensiveDataInfo(self):
        '''
        print("+-------- stream info --------+")
        print("+ endianness: {endian}".format(endian=self.stream.header.endian))
        print("+ cbyte     : {cbyte}".format(cbyte=PytellapicControlByte[self.stream.header.cbyte]))
        print("+ ssize     : {ssize}".format(ssize=self.stream.header.ssize))
        '''
        print("+ idfrom    : {idfrom}".format(idfrom=self.ddata.idfrom))
        print("+ dcbyte    : {dcbyte}".format(dcbyte=self.ddata.dcbyte))
        print("+ dcbyte ext: {dcbyteE}".format(dcbyteE=self.ddata.dcbyte_ext))
        print("+ number    : {number}".format(number=self.ddata.number))
        print("+ width     : {width}".format(width=self.ddata.width))
        print("+ opacity   : {alpha}".format(alpha=self.ddata.opacity))
        print("+ fillcolor : {fillcolor}{a}".format(fillcolor=self.brush.name(), a=hex(self.brush.alpha())[2:]))
        print("+ point 1   : ({x1}, {y1})".format(x1=self.ddata.point1.x, y1=self.ddata.point1.y))
        print("`·--+-------- figure info --------+")
        print("    + color     : {color}{a}".format(color=self.pen.color().name(), a=hex(self.pen.color().alpha())[2:]))
        print("    + point 2   : ({x2}, {y2})".format(x2=self.ddata.point2.x, y2=self.ddata.point2.y))
        print("    + linejoin  : {lj}".format(lj=self.ddata.type.figure.linejoin))
        print("    + endcaps   : {ec}".format(ec=self.ddata.type.figure.endcaps))
        print("    + miterlimit: {ml}".format(ml=self.ddata.type.figure.miterlimit))
        print("    + dash phase: {dp}".format(dp=self.ddata.type.figure.dash_phase))
        print("    + dash array: {da}".format(da=self.ddata.type.figure.dash_array))
        print("`·--+------------------------------+")


# This class should be concrete. He knows exactly that it consists
# of a shape as a rectangle.
class DrawingShapeRectangle(DrawingShape):
    # Calling the base class DrawingShpae constructor will
    # instantiates a QPainterPath() object: self.shape
    def __init__(self, ddata = None):
        super(DrawingShapeRectangle, self).__init__(ddata)
        print("DrawingShapeRectangle constructor (",id(self),"). ddata:",id(ddata))
        self.name = "rectangle"

    def setShapeBounds(self, x1, y1, x2, y2):
        super(DrawingShapeRectangle, self).setBounds(x1, y1, x2, y2)
        if self.shape.isEmpty() is not True:
            self.shape = QPainterPath()
        '''
        rect = QRectF(
            x1 if x1-x2<=0 else x2,
            y1 if y1-y2<=0 else y2, 
            abs(x1-x2),
            abs(y1-y2))
        '''
        rect = QRectF(
            -abs(x1-x2)/2,
            -abs(y2-y1)/2,
            abs(x1-x2),
            abs(y1-y2))
        self.shape.addRect(rect)
        
    def resize(self, newX1, newY1, newX2, newY2):
        self.setShapeBounds(newX1, newY1, newX2, newY2)

class DrawingShapeEllipse(DrawingShape):
    def __init__(self, ddata = None):
        super(DrawingShapeEllipse, self).__init__(ddata)
        self.name = "ellipse"

    def setShapeBounds(self, x1, y1, x2, y2):
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
    def __init__(self, ddata = None):
        super(DrawingShapeLine, self).__init__(ddata)

    def setShapeBounds(self, x1, y1, x2, y2):
        super(DrawingShapeLine, self).setBounds( x1, y1, x2, y2)
        if self.shape.isEmpty() is not True:
            self.shape = QPainterPath()
        self.shape.moveTo(x1, y1)
        self.shape.lineTo(x2, y2)
        self.shape.closeSubpath()    

class ToolBoxModel(QObject):
    toolChanged = pyqtSignal(QString)

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
        self.brush = QColor(0, 0, 0, 0,)
        self.pen = QPen(QColor(), 5, Qt.SolidLine, Qt.SquareCap, Qt.MiterJoin)

    def setTool(self, toolName):
        for tool in self.tools:
            print("tool: ", tool.name)
            if tool.name == toolName and self.lastUsedTool != tool:
                print("set tool: ",toolName)
                self.lastUsedTool = tool
                self.configureToolBox(tool)
                self.emit(SIGNAL("toolChanged(QString)"), toolName)

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

class SelectedEffect(QGraphicsDropShadowEffect):
    def __init__(self, parent = None):
        super(SelectedEffect, self).__init__(parent)
        self.pen = QPen(QColor("yellow"), 1, Qt.DashLine, Qt.SquareCap, Qt.MiterJoin)
        self.setBlurRadius(6)

    def draw(self, painter):
        super(SelectedEffect, self).draw(painter)
        painter.setPen(self.pen)
        rect = self.sourceBoundingRect()
        painter.drawRect(rect.x()+1, rect.y()+1, rect.width()-1, rect.height()-1)

class TellapicScene(QGraphicsScene):
    drawingSelectionChanged = pyqtSignal(QString)

    def __init__(self, model, parent = None):
        super(TellapicScene, self).__init__(parent)
        self.model = model
        self.model.toolChanged.connect(self.update)
        self.tool = model.getLastUsedTool()
        self.background = QPixmap("bart.jpg")
        print("TellapicScene instantiated.")
        self.temporalItem = None
        #self.setForegroundBrush(QBrush(Qt.lightGray, Qt.CrossPattern))
        self.selectedEffect = SelectedEffect()
    '''
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
        '''
    '''
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
        '''
        
    '''
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
        '''
        
    def update(self, toolName):
        self.tool = self.model.getToolByName(toolName)

    def drawBackground(self, painter, rect):
        r = rect.toRect()
        painter.drawPixmap(r.x(), r.y(),
                           self.background,
                           r.x(), r.y(),
                           self.background.width(), self.background.height()
                           )

    def customEvent(self, event):
        if event.type() == TellapicEvent.NewImageEvent:
            self.setBackgroundImage(event.arg)

        elif event.type() == TellapicEvent.NewFigureEvent:
            #item = self.buildNewShape(self.__getDrawingDataFromStream(event.arg))
            #print("item: ",item, "item.ddata: ",item.ddata)
            #item.printComprehensiveDataInfo()
            self.addItem(event.arg)

        elif event.type() == TellapicEvent.UpdateEvent:
            #shape = self.getDrawing(event.arg.data.drawing.number)
            if event.arg is not None:
                #self.editDrawingShape(shape, event.arg.data.drawing)
                self.views()[0].repaint()
            else:
                print("Another user is currently editing a shape that he/she draw when you weren't on session.")

    def getDrawing(self, number):
        for item in self.items():
            try:
                print("processing shape ",id(item)," with ddata: ",id(item.ddata),"item.ddata.number:",item.ddata.number)
                if item.ddata.number == number:
                    return item
            except:
                print("Item is not a drawing item")
        return None

    def setBackgroundImage(self, fileName):
        self.background = QPixmap(fileName)
        self.setSceneRect(0, 0, self.background.width(), self.background.height())
    '''
    def __cloneDrawingDataFromStream(self, stream):
        drawingData = pytellapic.ddata_t()
        drawingData.idfrom = stream.data.drawing.idfrom
        drawingData.dcbyte = stream.data.drawing.dcbyte
        drawingData.dcbyte_ext = stream.data.drawing.dcbyte_ext
        drawingData.number = stream.data.drawing.number
        drawingData.width  = stream.data.drawing.width
        drawingData.opacity = stream.data.drawing.opacity
        drawingData.fillcolor = stream.data.drawing.fillcolor
        drawingData.point1.x = stream.data.drawing.point1.x
        drawingData.point1.y = stream.data.drawing.point1.y
        drawingData.point2.x = stream.data.drawing.point2.x
        drawingData.point2.y = stream.data.drawing.point2.y
        if drawingData.dcbyte == pytellapic.TOOL_TEXT:
            drawingData.type.text = pytellapic.text_t()
            drawingData.type.text.color = stream.data.drawing.type.text.color
            drawingData.type.text.face = stream.data.drawing.type.text.face
            drawingData.type.text.facelen = stream.data.drawing.type.text.facelen
            drawingData.type.text.info = stream.data.drawing.type.text.info
            drawingData.type.text.infolen = stream.data.drawing.type.text.infolen
            drawingData.type.text.style = stream.data.drawing.type.text.style
        else:
            drawingData.type.figure = pytellapic.figure_t()
            drawingData.type.figure.color = stream.data.drawing.type.figure.color
            drawingData.type.figure.dash_array = stream.data.drawing.type.figure.dash_array
            drawingData.type.figure.dash_phase = stream.data.drawing.type.figure.dash_phase
            drawingData.type.figure.endcaps = stream.data.drawing.type.figure.endcaps
            drawingData.type.figure.linejoin = stream.data.drawing.type.figure.linejoin
            drawingData.type.figure.miterlimit = stream.data.drawing.type.figure.miterlimit
        return drawingData
    
    # Creates a new shape upon the stream received
    def buildNewShape(self, drawingData):
        #drawingDatab = stream.data.drawing
        tool        = drawingData.dcbyte & pytellapic.TOOL_MASK

        if (tool == pytellapic.TOOL_TEXT):
            shape = DrawingText()
            shape.setText(drawingData.type.text.info)
            shape.setFont(drawingData.type.text.color, drawingData.type.text.style, drawingData.type.text.face, drawingData.width)
        else:

            #rectangle.setDashStyle(drawingData.type.figure.dash_phase, drawingData.type.figure.dash_array)
            if (tool == pytellapic.TOOL_RECT):
                shape = DrawingShapeRectangle(drawingData)
            elif (tool == pytellapic.TOOL_ELLIPSE):
                shape = DrawingShapeEllipse(drawingData)
            else:
                return None

            # figure data is common for all drawings except Text
            
            shape.setStrokeColor(self.getColorFromStream(drawingData.type.figure.color))
            shape.setLineJoins(QtLineJoins[drawingData.type.figure.linejoin])
            shape.setEndCaps(QtEndCaps[drawingData.type.figure.endcaps])
            shape.setMiterLimit(drawingData.type.figure.miterlimit)
            shape.setStrokeWidth(drawingData.width)        

        shape.setBounds(drawingData.point1.x, drawingData.point1.y, drawingData.type.figure.point2.x, drawingData.type.figure.point2.y)
        shape.setFillColor(self.getColorFromStream(drawingData.fillcolor))
        shape.setFillEnabled(True)
        
        return shape
    '''
    def editDrawingShape(self, drawingShape, drawingData):
        #drawingShape.number = drawingData.number
        drawingShape.setStrokeWidth(drawingData.width)
        drawingShape.setFillColor(self.getColorFromStream(drawingData.fillcolor))
        drawingShape.setStrokeColor(self.getColorFromStream(drawingData.type.figure.color))
        drawingShape.setLineJoins(QtLineJoinsMap[drawingData.type.figure.linejoin])
        drawingShape.setEndCaps(QtEndCapsMap[drawingData.type.figure.endcaps])
        drawingShape.setMiterLimit(drawingData.type.figure.miterlimit)
        drawingShape.setBounds(drawingData.point1.x, drawingData.point1.y, drawingData.type.figure.point2.x, drawingData.type.figure.point2.y)
        drawingShape.setSelected(True)

        
