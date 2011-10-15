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
from PyQt4.QtCore import QTimer
from PyQt4.QtCore import QEvent

from PyQt4.QtGui import QPainter
from PyQt4.QtGui import QPainterPath
from PyQt4.QtGui import QColor
from PyQt4.QtGui import QBrush
from PyQt4.QtGui import QPen
from PyQt4.QtGui import QFont
from PyQt4.QtGui import QGraphicsItem
from PyQt4.QtGui import QAbstractGraphicsShapeItem
from PyQt4.QtGui import QGraphicsDropShadowEffect
from PyQt4.QtGui import QGraphicsScene
from PyQt4.QtGui import QGraphicsItemAnimation
from PyQt4.QtGui import QGraphicsRectItem
from PyQt4.QtGui import QIcon
from PyQt4.QtGui import QPixmap
from PyQt4.QtGui import QMenu
from PyQt4.QtGui import QActionGroup
from PyQt4.QtGui import QCursor
from PyQt4.QtGui import QTransform
from PyQt4.QtGui import QGraphicsPixmapItem
from PyQt4.QtGui import QGraphicsPathItem
from PyQt4.QtGui import QGraphicsView
from PyQt4.QtGui import QGraphicsSceneEvent
from PyQt4.QtGui import QGraphicsObject

from PyQt4.QtGui import QGraphicsItemGroup

import string
import cmath
import math

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
from Utils import Interval

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

class Tool(QObject):
    """Tool abstract class.
    An abstract base class for each <Some>Tool. It defines methods to be
    overriden by concrete implementations.
    """
    #__metaclass__ = abc.ABCMeta

    coordinateChanged = pyqtSignal(int, int)

    def __init__(self, name, model):
        super(Tool, self).__init__()
        self._name   = name
        self._model  = model
        self._cursor = Qt.CrossCursor
        self.active  = False

    def setCursor(self, c):
        self._cursor = c

    def cursor(self):
        return self._cursor

    def setActive(self, active):
        print("Setting tool '"+self._name+"' active field to:",active)
        self.active = active
        self.blockSignals(active)

    def isActive(self):
        return self.active

    @property
    def name(self):
        return self._name

    @property
    def scene(self):
        return self._scene

    @property
    def model(self):
        return self._model

    @abc.abstractmethod
    def mousePressed(self, event):
        point = event.scenePos()
        print("mouse pressed using tool "+self.name+" @ (",point.x(),",",point.y(),")")
        return

    @abc.abstractmethod
    def mouseDragged(self, event):
        point = event.scenePos()
        print("mouse dragged using tool "+self.name+" @ (",point.x(),",",point.y(),")")
        return

    @abc.abstractmethod
    def mouseMoved(self, event):
        point = event.scenePos()
        print("mouse moved using tool "+self.name+" @ (",point.x(),",",point.y(),")")
        return

    @abc.abstractmethod
    def mouseReleased(self, event):
        point = event.scenePos()
        print("mouse released using tool "+self.name+" @ (",point.x(),",",point.y(),")")
        return
    
    @abc.abstractmethod
    def canDraw(self):
        return

    @scene.setter
    def scene(self, scene):
        self._scene = scene

class DrawingTool(Tool):
    """DrawingTool class.
    An abstract Tool subclass that defines tools able to draw something.
    """
    #__metaclass__ = abc.ABCMeta

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

    def mousePressed(self, event):
        self._drawing = DrawingShapeRectangle.withModel(self.model)
        '''
        self._drawing.setPen(self.model.pen)
        self._drawing.setBrush(self.model.brush)
        self._drawing.setStrokeEnabled(self.model.shouldStroke)
        self._drawing.setFillEnabled(self.model.shouldFill)
        '''
        point = event.scenePos()
        self._drawing.setShapeBounds(point.x(), point.y(), point.x(), point.y())
        self.scene.addItem(self._drawing)
        self.point1 = point

    def mouseDragged(self, event):
        point = event.scenePos()
        self._drawing.setShapeBounds(self.point1.x(),
                                     self.point1.y(),
                                     point.x(),
                                     point.y()
                                     )

    def mouseMoved(self, event):
        pass

    def mouseReleased(self, event):
        #self._drawing.setPen(QPen(self.model.pen))
        #self._drawing.setBrush(QBrush(self.model.brush))
        #self._drawing.printComprehensiveDataInfo()
        self.scene.clearSelection()
        #self._drawing.setSelected(True)

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

class DrawingToolEllipse(DrawingTool):
    def __init__(self, model):
        super(DrawingToolEllipse, self).__init__(ToolEllipse, model)

    def mousePressed(self, point):
        self._drawing = DrawingShapeEllipse.withModel(self.model)

    def mouseDragged(self, point):
        pass

    def mouseMoved(self, point):
        pass

    def mouseReleased(self, point):
        pass

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
    def __init__(self, name, model):
        super(ControlTool, self).__init__(name, model)

    def canDraw(self):
        return False

class ControlToolSelector(ControlTool):
    def __init__(self, model):
        super(ControlToolSelector, self).__init__(ToolSelector, model)
        self._drawing   = None
        self.selectArea = None
        self.frame = QGraphicsRectItem(-150, -150, 300, 300)
        self.frame.setPen(QPen(QColor(), 10))
        self.frame.setFlag(QGraphicsItem.ItemIsMovable, True)
        self.group = None
        self.oldItem = None
        self.__connectSignals()

    def __connectSignals(self):
        self.model.fillEnabled.connect(self.enableFillOnDrawing)
        self.model.strokeEnabled.connect(self.enableStrokeOnDrawing)
        self.model.strokeWidthChanged.connect(self.changeStrokeWidthOnDrawing)
        self.model.strokeColorChanged.connect(self.changeStrokeColorOnDrawing)
        self.model.fillColorChanged.connect(self.changeFillColorOnDrawing)
        self.model.lineJoinsChanged.connect(self.changeLineJoinsOnDrawing)
        self.model.endCapsChanged.connect(self.changeEndCapsOnDrawing)
        self.model.miterLimitChanged.connect(self.changeMiterLimitOnDrawing)
        self.model.dashStyleChanged.connect(self.changeDashStyleOnDrawing)
    
    def __disconnectSignals(self):
        self.model.fillEnabled.disconnect(self.enableFillOnDrawing)
        self.model.strokeEnabled.disconnect(self.enableStrokeOnDrawing)
        self.model.strokeWidthChanged.disconnect(self.changeStrokeWidthOnDrawing)
        self.model.strokeColorChanged.disconnect(self.changeStrokeColorOnDrawing)
        self.model.fillColorChanged.disconnect(self.changeFillColorOnDrawing)
        self.model.lineJoinsChanged.disconnect(self.changeLineJoinsOnDrawing)
        self.model.endCapsChanged.disconnect(self.changeEndCapsOnDrawing)
        self.model.miterLimitChanged.disconnect(self.changeMiterLimitOnDrawing)
        self.model.dashStyleChanged.disconnect(self.changeDashStyleOnDrawing)

    def enableFillOnDrawing(self, enabled):
        print("Enable fill on drawing:", enabled)
        if self.group is not None:
            for item in self.group.groupList():
                item.setFillEnabled(enabled)

    def enableStrokeOnDrawing(self, enabled):
        print("Enable stroke on drawing:", enabled)
        if self.group is not None:
            for item in self.group.groupList():
                item.setStrokeEnabled(enabled)

    def changeStrokeWidthOnDrawing(self, width):
        print("Change stroke width on drawing:", width)
        if self.group is not None:
            for item in self.group.groupList():
                item.setStrokeWidth(width)

    def changeStrokeColorOnDrawing(self, color):
        print("Change stroke color on drawing:",color)
        if self.group is not None:
            for item in self.group.groupList():
                item.setStrokeColor(color)

    def changeFillColorOnDrawing(self, color):
        print("Change fill color on drawing:",color)
        if self.group is not None:
            for item in self.group.groupList():
                item.setBrush(QBrush(color))

    def changeLineJoinsOnDrawing(self, joins):
        print("Change line joins on drawing:",joins)
        if self.group is not None:
            for item in self.group.groupList():
                item.setLineJoins(joins)

    def changeEndCapsOnDrawing(self, caps):
        print("Change end caps on drawing:",caps)
        if self.group is not None:
            for item in self.group.groupList():
                item.setEndCaps(caps)

    def changeMiterLimitOnDrawing(self, ml):
        print("Change miter limit on drawing:",ml)

    def changeDashStyleOnDrawing(self, phase, array):
        print("Change dash style on drawing:",phase,":",array)

    def mousePressed(self, event):
        # The superclass method will only display where the event ocurrs'
        super(ControlToolSelector, self).mousePressed(event)
        self.point1 = event.scenePos()
        self.item = self.scene.itemAt(self.point1)
        if self.item is None:
            if self.group is not None:
                #self.group.ungroup()
                self.scene.destroyItemGroup(self.group)
                self.group = None
            self.scene.clearSelection()
            self.selectArea = QGraphicsRectItem(self.point1.x(), self.point1.y(), 0, 0)
            self.selectArea.setPen(QPen(QColor("red"), 0))
            self.selectArea.setBrush(QColor(255,0,0, 50))
            self.scene.addItem(self.selectArea)
        else:
            print("item is",self.item)
            if self.group is None:
                self.group = SelectionGroupFrame()
                self.group.setFocus()
                self.scene.addItem(self.group)
                self.group.addToGroup(self.item)
                self.group.mousePressEvent(event)
            elif self.item != self.group:
                if self.item not in self.group.childItems():
                    if event.modifiers() != Qt.ShiftModifier:
                        self.scene.destroyItemGroup(self.group)
                        self.group = SelectionGroupFrame()
                        self.group.setFocus()
                        self.scene.addItem(self.group)
                    self.group.addToGroup(self.item)
                self.group.mousePressEvent(event)
            else:
                #self.group.ungroup()
                self.scene.destroyItemGroup(self.group)
                self.scene.clearSelection()
                self.group = None


    def mouseDragged(self, event):
        # The superclass method will only display where the event ocurrs
        super(ControlToolSelector, self).mouseDragged(event)
        point = event.scenePos()
        if self.selectArea is not None:
            size = self.point1 - point
            self.selectArea.setRect(point.x(), point.y(), size.x(), size.y())
            """
            xOffset = self.scene.views()[0].horizontalScrollBar().value()
            yOffset = self.scene.views()[0].verticalScrollBar().value()
            viewPort = self.scene.views()[0].viewport()
            viewSize = viewPort.size()
            if point.x() > viewSize.width()+xOffset or point.x() < xOffset:
                print("drawg outside viewport")
            if point.y() > viewSize.height()+yOffset or point.y() < yOffset:
                print("drawg outside viewport")
            """
        elif self.item is not None and self.group is not None:
            self.group.mouseMoveEvent(event)

    def mouseMoved(self, event):
        # The superclass method will only display where the event ocurrs
        #super(ControlToolSelector, self).mouseMoved(event)
        item = self.scene.itemAt(event.scenePos())
        if item is not None:
            if self.oldItem == item:
                item.hoverMoveEvent(event)
            else:
                if self.oldItem is not None:
                    self.oldItem.hoverLeaveEvent(event)
                item.hoverEnterEvent(event)
            self.oldItem = item
        elif self.oldItem is not None:
            self.oldItem.hoverLeaveEvent(event)
            self.oldItem = None

    def mouseReleased(self, event):
        # The superclass method will only display where the event ocurrs
        super(ControlToolSelector, self).mouseReleased(event)
        if self.selectArea is not None:
            self.scene.setSelectionArea(self.selectArea.shape())
            if len(self.scene.selectedItems()) > 0:
                self.group = SelectionGroupFrame()
                self.scene.addItem(self.group)
                for item in self.scene.selectedItems():
                    self.group.addToGroup(item)
            self.scene.removeItem(self.selectArea)
        elif self.item is not None and self.group is not None:
            self.group.mouseReleaseEvent(event)
            ''' This workaround sucks. I need to determine a way
            for the item group to recalculate its bounding rect
            without applying any transformation so control points
            are drawn where they should be drawn. Below its a 
            quick and dirty solution. Just destroy the current
            item group, and recreate a new one. That will calculate
            its bounding rect without any transformation.
            l = self.group.groupList()
            m = self.group.mode
            self.scene.destroyItemGroup(self.group)
            self.group = SelectionGroupFrame()
            self.group.setFocus()
            self.scene.addItem(self.group)
            for i in l:
                self.group.addToGroup(i)
            self.group.setMode(m)
            '''
        self.item = self.selectArea = None

    def hasFontProperties(self):
        return True
        if self._drawing is None:
            return False
        return False

    def hasStrokeStylesProperties(self):
        return True
        if self._drawing is None:
            return False
        return True

    def hasStrokeColorProperties(self):
        return True
        if self._drawing is None:
            return False
        return True

    def hasFillColorProperties(self):
        return True
        if self._drawing is None:
            return False
        return True

    def hasTransparentProperties(self):
        return True
        if self._drawing is None:
            return False
        return True

class DrawingControlPoint(QGraphicsPathItem):
    TopRole         = 8 #binary [1,0,0,0] -> [top, left, bottom, right]
    TopRightRole    = 9 #binary [1,0,0,1] -> [top, left, bottom, right]
    RightRole       = 1 #binary [0,0,0,1] -> [top, left, bottom, right]
    BottomRightRole = 3 #binary [0,0,1,1] -> [top, left, bottom, right]
    BottomRole      = 2 #binary [0,0,1,0] -> [top, left, bottom, right]
    BottomLeftRole  = 6 #binary [0,1,1,0] -> [top, left, bottom, right]
    LeftRole        = 4 #binary [0,1,0,0] -> [top, left, bottom, right]
    TopLeftRole     = 12 #binary [1,1,0,0] -> [top, left, bottom, right]
    CenterRole      = 0
    '''
    Roles = [
             TopRole,
             TopRightRole,
             RightRole,
             BottomRightRole,
             BottomRole,
             BottomLeftRole,
             LeftRole,
             TopLeftRole,
             CenterRole
             ]
    '''
    RoleNames = {
                 TopRole         : 'TopRole',
                 TopRightRole    : 'TopRightRole',
                 RightRole       : 'RightRole',
                 BottomRightRole : 'BottomRightRole',
                 BottomRole      : 'BottomRole',
                 BottomLeftRole  : 'BottomLeftRole',
                 LeftRole        : 'LeftRole',
                 TopLeftRole     : 'TopLeftRole',
                 CenterRole      : 'CenterRole'
                 }

    '''Default shape is a 8x8 box. Subclasses must setPath() with a custom shape'''
    def __init__(self, parent):
        super(DrawingControlPoint, self).__init__(parent)
        #self.setVisible(False)
        self.setAcceptHoverEvents(True)
        self.setFlag(QGraphicsItem.ItemIsSelectable, True)
        #self.setFlag(QGraphicsItem.ItemIsMovable, True)
        #self.setFlag(QGraphicsItem.ItemSendsGeometryChanges, True)
        self.setPath(self.form())
        # Is this item pressed?
        self.pressed= 0
        # get the animation. Subclasses must implement configureAnimation() in
        # order to override default shrink/grow animation or return a None
        # animation in order to provide no animation at all.
        self.animation = self.configureAnimation()
        self.animated = False if self.animation is None else True
        # selectedBrush will be the background color of this item
        # when the user is pressing/draging it.
        self.selectedBrush = QColor("blue")
        # normalBrush will be the normal background color when
        # the owner of this control point (aka, its parent) is selected
        self.normalBrush = QColor("white")
        # normalPen will be the pen that outlines the item. A solid
        # black line for instance.
        self.normalPen = QPen(QColor(), 1)
        # previewPen and previewBrush are used when this item owner
        # when it's not selected, orders to show this control point.
        self.previewPen = QPen(QColor("gray"), 1, Qt.DashLine)
        self.previewBrush = QColor(0,0,0,0)
        if parent.isSelected():
            self.setPen(self.normalPen)
            self.setBrush(self.normalBrush)
        else:
            # Set both, the pen and the brush ready for a preview.
            self.setPen(self.previewPen)
            self.setBrush(self.previewBrush)

    def form(self):
        #self.bounds = QRectF(-4, -4, 8, 8)
        path = QPainterPath()
        path.addRect(QRectF(-4, -4, 8, 8))
        return path

    def configureAnimation(self):
        '''this sets a default animation. In order to unset animations,
        subclasses must implement this method and return None.'''
        timeLine = QTimeLine(1000)
        timeLine.setLoopCount(0)
        animation = QGraphicsItemAnimation()
        animation.setRotationAt(0, self.rotation())
        animation.setScaleAt(0.0, 1, 1)
        animation.setScaleAt(0.5, 2, 2)
        animation.setScaleAt(1.0, 1, 1)
        animation.setItem(self)
        animation.setTimeLine(timeLine)
        return animation

    def controlPointType(self):
        return self._type

    def mousePressEvent(self, event):
        # Don't do anything if parent isn't selected
        if self.parentItem() is not None and self.parentItem().isSelected() is False:
            return
        print("control point press event: ",event.scenePos(), "for",self)
        # The animation is intended to be used in hover events
        # so if the item is pressed, stop it.
        if self.animated:
            self.animation.timeLine().stop()
            self.animation.timeLine().setCurrentTime(0)
        self.pressed = 1
        self.setSelected(True)
        #super(DrawingControlPoint, self).mousePressEvent(event)

    def mouseReleaseEvent(self, event):
        # Don't do anything if parent isn't selected
        if self.parentItem() is not None and self.parentItem().isSelected() is False:
            return
        print("control point release event: ",event.scenePos(), "for",self)
        self.pressed = 0
        self.setSelected(False)
        #super(DrawingControlPoint, self).mouseReleaseEvent(event)

    def hoverEnterEvent(self, event):
        # Don't do anything if parent isn't selected
        if self.parentItem() is not None and self.parentItem().isSelected() is False:
            return
        print("control point hover enter event for",self)
        self.setBrush(self.selectedBrush)
        self.setZValue(1)
        if self.animated:
            self.animation.timeLine().start()

    def hoverMoveEvent(self, event):
        pass

    def hoverLeaveEvent(self, event):
        # Don't do anything if parent isn't selected
        if self.parentItem() is not None and self.parentItem().isSelected() is False:
            return
        print("control point hover leave event for",self)
        self.setBrush(self.normalBrush)
        self.setZValue(0)
        if self.animated:
            self.animation.timeLine().stop()
            self.animation  .timeLine().setCurrentTime(0)

    def setPreviewEffect(self, on):
        if on:
            self.setPen(self.previewPen)
            self.setBrush(self.previewBrush)
        else:
            self.setPen(self.normalPen)
            self.setBrush(self.normalBrush)

    def itemChange(self, change, value):
        if change == QGraphicsItem.ItemSelectedHasChanged:
            if value:
                self.setBrush(self.selectedBrush)
                self.setPen(self.normalPen)
            else:
                self.setBrush(self.normalBrush)
            return
        return super(DrawingControlPoint, self).itemChange(change, value)

class DrawingControlPointResize(DrawingControlPoint):
    Roles = {
             Interval(math.radians(338), math.radians(360)): DrawingControlPoint.TopRole,
             Interval(math.radians(0), math.radians(22)): DrawingControlPoint.TopRole,
             Interval(math.radians(23), math.radians(67)): DrawingControlPoint.TopRightRole,
             Interval(math.radians(68), math.radians(112)): DrawingControlPoint.RightRole,
             Interval(math.radians(113), math.radians(157)): DrawingControlPoint.BottomRightRole,
             Interval(math.radians(158), math.radians(202)): DrawingControlPoint.BottomRole,
             Interval(math.radians(203), math.radians(247)): DrawingControlPoint.BottomLeftRole,
             Interval(math.radians(248), math.radians(292)): DrawingControlPoint.LeftRole,
             Interval(math.radians(293), math.radians(337)): DrawingControlPoint.TopLeftRole
             }

    def __init__(self, angle, parent):
        '''angle corresponds to the angle where this control point is located
        in the parent bounding rect of size 1. '''
        super(DrawingControlPointResize, self).__init__(parent)
        self.cursorPixmap = self.createCursor(angle)
        self.setCursor(QCursor(self.cursorPixmap))
        self.setFlag(QGraphicsItem.ItemSendsGeometryChanges, True)
        self.radians   = math.radians(angle)
        self.role      = self.getRole(self.radians)
        self.topOffset = self.leftOffset = self.bottomOffset = self.rightOffset = 0
        self.updateLocation()
        rect = parent.boundingRect()
        print("---\nparent bounding rect:")
        print("topLeft:",rect.left(),",",rect.top())
        print("bottomRight:",rect.right(),",",rect.bottom())

    def createCursor(self, angle):
        cursorPath = QPainterPath()
        cursorPath.moveTo(0, -16)
        cursorPath.lineTo(-8, -8)
        cursorPath.lineTo(-2, -8)
        cursorPath.lineTo(-2, 8)
        cursorPath.lineTo(-8, 8)
        cursorPath.lineTo(0, 16)
        cursorPath.lineTo(8, 8)
        cursorPath.lineTo(2, 8)
        cursorPath.lineTo(2, -8)
        cursorPath.lineTo(8, -8)
        cursorPath.closeSubpath()
        pixmap = QPixmap(24,24)
        '''Size choosen as Qt doc said:
        We recommend using 32 x 32 cursors, because this size is supported on all platforms.
        Some platforms also support 16 x 16, 48 x 48, and 64 x 64 cursors.'''
        pixmap.fill(QColor(0,0,0,0))
        painter = QPainter()
        painter.begin(pixmap)
        painter.setRenderHint(QPainter.Antialiasing)
        painter.setPen(QPen(QColor("white"), 0.8))
        painter.setBrush(QColor("black"))
        transform = QTransform()
        transform.translate(12, 12)
        transform.rotate(angle)
        transform.scale(0.75, 0.75)
        painter.setTransform(transform)
        painter.drawPath(cursorPath)
        painter.end()
        return pixmap

    def updateLocation(self):
        #self.topOffset  = self.leftOffset = self.bottomOffset = self.rightOffset = 0
        parent = self.parentItem()
        pt = parent.transform().inverted()[0]
        bounds = pt.mapRect(parent.boundingRect())
        bounds.translate(-parent.pos())
        #self.role = self.getRole(math.radians(self.parentItem().rotation()))
        if self.role == self.TopLeftRole:
            self.setPos(bounds.topLeft())
            #self.topOffset = self.leftOffset = 1
        elif self.role == self.TopRole:
            self.setPos(bounds.center().x(), bounds.top())
            #self.topOffset = 1
        elif self.role == self.TopRightRole:
            self.setPos(bounds.right(), bounds.top())
            #self.topOffset = self.rightOffset = 1
        elif self.role == self.RightRole:
            self.setPos(bounds.right(), bounds.center().y())
            #self.rightOffset = 1
        elif self.role == self.BottomRightRole:
            self.setPos(bounds.bottomRight())
            #self.BottomOffset = self.rightOffset = 1
        elif self.role == self.BottomRole:
            self.setPos(bounds.center().x(), bounds.bottom())
            #self.BottomOffset = 1
        elif self.role == self.BottomLeftRole:
            self.setPos(bounds.left(), bounds.bottom())
            #self.BottomOffset = self.leftOffset = 1
        elif self.role == self.LeftRole:
            self.setPos(bounds.left(), bounds.center().y())
            #self.leftOffset = 1
        else:
            raise ValueError("Wrong role assigment.")
        transform = QTransform()
        transform.rotate(self.parentItem().rotation())
        self.setCursor(QCursor(self.cursorPixmap.transformed(transform, Qt.SmoothTransformation)))
        
    def getRole(self, rad):
        for interval in self.Roles:
            if interval.__contains__(rad):
                return self.Roles[interval]
        return None
    
    def mousePressEvent(self, event):
        super(DrawingControlPointResize, self).mousePressEvent(event)
        self.topOffset    = (self.role & self.TopRole) >> 3
        self.leftOffset   = (self.role & self.LeftRole) >> 2
        self.bottomOffset = (self.role & self.BottomRole) >> 1
        self.rightOffset  = (self.role & self.RightRole)
        '''
        parent  = self.parentItem()
        pixmap  = QPixmap(parent.boundingRect().width(), parent.boundingRect().height())
        pixmap.fill(QColor(0, 0, 0, 0))
        painter = QPainter()
        painter.begin(pixmap)
        painter.setPen(QPen(QColor("gray"),1.5))
        painter.setBrush(QColor(0, 0, 0, 0))
        painter.translate(pixmap.width()/2.0, pixmap.height()/2.0)
        painter.drawPath(parent.path)
        painter.end()
        self.ghost = QGraphicsPixmapItem(pixmap)
        self.ghost.setPos(parent.scenePos())
        self.ghost.setRotation(parent.rotation())
        self.ghost.setOffset(-pixmap.width()/2, -pixmap.height()/2)
        self.scene().addItem(self.ghost)
        '''
        parent  = self.parentItem()
        self.ghost = QGraphicsPathItem(parent.path())
        self.ghost.setPos(parent.scenePos())
        self.ghost.setRotation(parent.rotation())
        #self.ghost.setOffset(-parent.width()/2, -parent.height()/2)
        self.ghost.setPen(QPen(QColor("gray"), 1.5))
        self.scene().addItem(self.ghost)

    def mouseMoveEvent(self, event):
        if self.pressed:
            topLeft     = self.parentItem().sceneTopLeft()
            bottomRight = self.parentItem().sceneBottomRight()
            eventOffset = event.pos()
            newX1 = topLeft.x()     + (self.leftOffset   * eventOffset.x())
            newY1 = topLeft.y()     + (self.topOffset    * eventOffset.y())
            newX2 = bottomRight.x() + (self.rightOffset  * eventOffset.x())
            newY2 = bottomRight.y() + (self.bottomOffset * eventOffset.y())
            if event.modifiers() and Qt.ShiftModifier == Qt.ShiftModifier:
                self.parentItem().resize(newX1, newY1, newX2, newY2)
            else:
                transform = QTransform()
                transform.scale(newX1/topLeft.x(), newY1/topLeft.y())
                self.ghost.setTransform(transform)

    def mouseReleaseEvent(self, event):
        super(DrawingControlPointResize, self).mouseReleaseEvent(event)
        parent    = self.parentItem()
        self.scene().removeItem(self.ghost)
        #parent.setRotation(self.rotation)

class DrawingControlPointGuide(DrawingControlPoint):
    def __init__(self, parent):
        super(DrawingControlPointGuide, self).__init__(parent)
        self.setCursor(Qt.CrossCursor)
        self.timer = QTimer()
        self.timer.setSingleShot(True)
        self.timer.timeout.connect(self.fireMenu)
        self.menuPos = None

    def configureAnimation(self):
        ''' we dont want animations on this control point'''
        return None

    def fireMenu(self):
        print("fire menu here")
        menu = QMenu()
        name = menu.addAction(self.parentItem().name)
        name.setEnabled(False)
        menu.addSeparator()
        delete = menu.addAction("Delete Item")
        hide   = menu.addAction("Hide Item")
        guides = menu.addAction("Show guides")
        menu.addSeparator()
        modeMenu = menu.addMenu("Mode")
        modes = []
        modes.append(modeMenu.addAction(QIcon(":/icons/resources/icons/tool-icons/layer-select.png"), "Geometric Mode"))
        modes.append(modeMenu.addAction(QIcon(":/icons/resources/icons/tool-icons/layer-resize.png"), "Scale Mode"))
        modes.append(modeMenu.addAction(QIcon(":/icons/resources/icons/tool-icons/layer-rotate.png"), "Rotate Mode"))
        modes.append(modeMenu.addAction("Shear Mode"))
        guides.setCheckable(True)
        guides.setChecked(self.parentItem().showGuides)
        guides.toggled.connect(self.parentItem().setGuidesEnabled)
        delete.triggered.connect(self.parentItem().deleteAction)
        hide.triggered.connect(self.parentItem().hideAction)
        modes[Drawing.GeometricMode].setCheckable(True)
        modes[Drawing.RotateMode].setCheckable(True)
        modes[Drawing.ScaleMode].setCheckable(True)
        modes[Drawing.ShearMode].setCheckable(True)
        modes[Drawing.GeometricMode].setShortcut("Shift+1")
        modes[Drawing.RotateMode].setShortcut("Shift+2")
        modes[Drawing.ScaleMode].setShortcut("Shift+3")
        modes[Drawing.ShearMode].setShortcut("Shift+4")
        modes[Drawing.GeometricMode].setData(Drawing.GeometricMode)
        modes[Drawing.RotateMode].setData(Drawing.RotateMode)
        modes[Drawing.ScaleMode].setData(Drawing.ScaleMode)
        modes[Drawing.ShearMode].setData(Drawing.ShearMode)
        group = QActionGroup(modeMenu)
        group.addAction(modes[Drawing.GeometricMode])
        group.addAction(modes[Drawing.ScaleMode])
        group.addAction(modes[Drawing.RotateMode])
        group.addAction(modes[Drawing.ShearMode])
        group.triggered.connect(self.parentItem().setModeFromAction)
        modes[self.parentItem().mode].setChecked(True)
        menu.exec(self.menuPos)

    def updateLocation(self):
        if self.parentItem() is None:
            center = QPointF(0,0)
        else:
            center = self.parentItem().boundingRect().center()
        self.setPos(center)

    def hoverEnterEvent(self, event):
        self.timer.start(700)
        self.menuPos = event.screenPos()
        super(DrawingControlPointGuide, self).hoverEnterEvent(event)

    def hoverLeaveEvent(self, event):
        self.timer.stop()
        super(DrawingControlPointGuide, self).hoverLeaveEvent(event)

    def mousePressEvent(self, event):
        self.timer.stop()
        super(DrawingControlPointGuide, self).mousePressEvent(event)
        self.parentItem().mousePressEvent(event)
        self.setSelected(True)

    def mouseMoveEvent(self, event):
        self.parentItem().mouseMoveEvent(event)

    def mouseReleaseEvent(self, event):
        super(DrawingControlPointGuide, self).mouseReleaseEvent(event)
        self.parentItem().mouseReleaseEvent(event)

    def form(self):
        path = QPainterPath()
        path.addEllipse(-4, -4, 8, 8)
        path.moveTo(-6,0)
        path.lineTo(6,0)
        path.moveTo(0,-6)
        path.lineTo(0,6)
        return path

    def itemChange(self, change, value):
        if change == QGraphicsItem.ItemParentHasChanged:
            self.updateLocation()
        return super(DrawingControlPointGuide, self).itemChange(change, value)

class DrawingControlPointShear(DrawingControlPoint):
    Roles = [
             DrawingControlPoint.TopRole,
             DrawingControlPoint.RightRole,
             DrawingControlPoint.BottomRole,
             DrawingControlPoint.LeftRole,
             ]
    RoleFactors = {
                   DrawingControlPoint.TopRole    : (-1, 0),
                   DrawingControlPoint.BottomRole : (1, 0),
                   DrawingControlPoint.LeftRole   : (0, -1),
                   DrawingControlPoint.RightRole  : (0, 1)
                   }

    def __init__(self, role, parent):
        super(DrawingControlPointShear, self).__init__(parent)
        self.selectedBrush   = QColor("blue")
        self.normalBrush     = QColor("black")
        self.role            = role
        self.bounds          = QRectF(-8, -8, 16 ,16)
        if self.role == self.BottomRole or self.role == self.TopRole:
            self.rotate(90)
        self.factors = self.RoleFactors[self.role]
        self.updateLocation()
        self.setFlag(QGraphicsItem.ItemIgnoresTransformations, True)
        self.setBrush(self.normalBrush)

    def updateLocation(self):
        parent = self.parentItem()
        pt = parent.transform().inverted()[0]
        bounds = pt.mapRect(parent.boundingRect())
        bounds.translate(-parent.pos())
        if self.role == self.TopRole:
            self.setPos(bounds.center().x(), bounds.top()-6)
        elif self.role == self.BottomRole:
            self.setPos(bounds.center().x(), bounds.bottom()+6)
        elif self.role == self.RightRole:
            self.setPos(bounds.right()+8, bounds.center().y())
        else:
            self.setPos(bounds.left()-8, bounds.center().y())

    def configureAnimation(self):
        return None

    def form(self):
        path = QPainterPath()
        path.moveTo(0, 8)
        path.lineTo(-5, 3)
        path.lineTo(-2, 3)
        path.lineTo(-2, -3)
        path.lineTo(-5, -3)
        path.lineTo(0, -8)
        path.lineTo(5, -3)
        path.lineTo(2, -3)
        path.lineTo(2, 3)
        path.lineTo(5, 3)
        path.closeSubpath()
        return path

    def mousePressEvent(self, event):
        super(DrawingControlPointShear, self).mousePressEvent(event)
        if self.parentItem() is not None:
            #self.parentItem().setTransformOriginPoint(self.parentItem().boundingRect().center())
            pass

    def mouseMoveEvent(self, event):
        super(DrawingControlPointShear, self).mouseMoveEvent(event)
        point = event.scenePos() - self.pos()
        if self.parentItem() is not None:
            transform = QTransform()
            shearX = point.x() * self.factors[0] * 0.01
            shearY = point.y() * self.factors[1] * 0.01
            transform.shear(shearX, shearY)
            q = self.parentItem().boundingRect().center() - transform.map(self.parentItem().boundingRect().center())
            transform.translate(q.x(), q.y())
            self.parentItem().setTransform(transform)

    def mouseReleaseEvent(self, event):
        super(DrawingControlPointShear, self).mouseReleaseEvent(event)

class DrawingControlPointScale(DrawingControlPoint):
    Roles = [
             DrawingControlPoint.TopRole,
             DrawingControlPoint.RightRole,
             DrawingControlPoint.BottomRole,
             DrawingControlPoint.LeftRole,
             DrawingControlPoint.TopLeftRole,
             DrawingControlPoint.TopRightRole,
             DrawingControlPoint.BottomLeftRole,
             DrawingControlPoint.BottomRightRole
             ]
    RoleRotation = {
                    DrawingControlPoint.TopRole         : 0,
                    DrawingControlPoint.BottomRole      : 0,
                    DrawingControlPoint.RightRole       : 90,
                    DrawingControlPoint.LeftRole        : 90,
                    DrawingControlPoint.TopLeftRole     : 315,
                    DrawingControlPoint.TopRightRole    : 45,
                    DrawingControlPoint.BottomLeftRole  : 45,
                    DrawingControlPoint.BottomRightRole : 315
                    }
    RoleFactors = {
                   DrawingControlPoint.TopRole         : (1, 0),
                   DrawingControlPoint.BottomRole      : (1, 0),
                   DrawingControlPoint.LeftRole        : (-0.01, 0),
                   DrawingControlPoint.RightRole       : (0.01, 0),
                   DrawingControlPoint.TopLeftRole     : (-0.01, -0.01),
                   DrawingControlPoint.BottomLeftRole  : (1, 1),
                   DrawingControlPoint.TopRightRole    : (1, 1),
                   DrawingControlPoint.BottomRightRole : (1, 1)
                   }

    def __init__(self, role, parent):
        super(DrawingControlPointScale, self).__init__(parent)
        self.selectedBrush   = QColor("blue")
        self.normalBrush     = QColor("black")
        self.role            = role
        self.bounds          = QRectF(-8, -8, 16 ,16)
        self.rotate(self.RoleRotation[self.role])
        self.updateLocation()
        self.setFlag(QGraphicsItem.ItemIgnoresTransformations, True)
        self.setBrush(self.normalBrush)
        self.factors = self.RoleFactors[self.role]

    def updateLocation(self):
        parent = self.parentItem()
        pt = parent.transform().inverted()[0]
        bounds = pt.mapRect(parent.boundingRect())
        bounds.translate(-parent.pos())
        if self.role == self.TopRole:
            self.setPos(bounds.center().x(), bounds.top() - 8)
        elif self.role == self.BottomRole:
            self.setPos(bounds.center().x(), bounds.bottom() + 8)
        elif self.role == self.RightRole:
            self.setPos(bounds.right() + 8, bounds.center().y())
        elif self.role == self.LeftRole:
            self.setPos(bounds.left() - 8, bounds.center().y())
        elif self.role == self.TopLeftRole:
            self.setPos(bounds.topLeft() - QPointF(8,8))
        elif self.role == self.TopRightRole:
            self.setPos(bounds.right() + 8, bounds.top() - 8)
        elif self.role == self.BottomRightRole:
            self.setPos(bounds.right() + 8, bounds.bottom() + 8)
        else:
            self.setPos(bounds.left() - 8, bounds.bottom() + 8)

    def configureAnimation(self):
        return None

    def form(self):
        path = QPainterPath()
        path.moveTo(0, 8)
        path.lineTo(-5, 3)
        path.lineTo(-2, 3)
        path.lineTo(-2, -3)
        path.lineTo(-5, -3)
        path.lineTo(0, -8)
        path.lineTo(5, -3)
        path.lineTo(2, -3)
        path.lineTo(2, 3)
        path.lineTo(5, 3)
        path.closeSubpath()
        return path

    def mousePressEvent(self, event):
        super(DrawingControlPointScale, self).mousePressEvent(event)
        parent = self.parentItem()
        if parent is not None:
            #parent.setTransformOriginPoint(self.parentItem().boundingRect().center())
            pass

    def mouseMoveEvent(self, event):
        super(DrawingControlPointScale, self).mouseMoveEvent(event)
        point = event.scenePos() - self.pos()
        parent = self.parentItem()
        if parent is not None:
            ratioX = -(point.x() / parent.boundingRect().width())
            ratioY = point.y() / parent.boundingRect().height()
            #transform = QTransform()
            scaleX = 1 + ratioX# * self.factors[0]
            scaleY = 1 + ratioY# * self.factors[1]
            print("sx:",scaleX,"sy:",scaleY, "p.x:",point.x(), "width:",parent.boundingRect().width())
            #transform.scale(scaleX, scaleY)
            #self.parentItem().setTransform(transform)
            parent.setGroupScale(scaleX, scaleY)

    def mouseReleaseEvent(self, event):
        super(DrawingControlPointScale, self).mouseReleaseEvent(event)

class DrawingControlPointRotate(DrawingControlPoint):
    Roles = [
             DrawingControlPoint.TopLeftRole,
             DrawingControlPoint.TopRightRole,
             DrawingControlPoint.BottomLeftRole,
             DrawingControlPoint.BottomRightRole,
             ]
    RoleRotation = {
                    DrawingControlPoint.TopLeftRole     : 0,
                    DrawingControlPoint.BottomLeftRole  : -90,
                    DrawingControlPoint.BottomRightRole : -180,
                    DrawingControlPoint.TopRightRole    : -270
                    }

    def __init__(self, role, parent):
        super(DrawingControlPointRotate, self).__init__(parent)
        self.appliedRotation = 0
        self.selectedBrush   = QColor("blue")
        self.normalBrush     = QColor("black")
        self.bounds          = QRectF(-8, -8, 16, 16)
        self.role            = role
        self.rotate(self.RoleRotation[role])
        self.updateLocation()
        self.setFlag(QGraphicsItem.ItemIgnoresTransformations, True)
        self.setBrush(self.normalBrush)
        rect = parent.boundingRect()
        print("---\nparent bounding rect:")
        print("topLeft:",rect.left(),",",rect.top())
        print("bottomRight:",rect.right(),",",rect.bottom())

    def updateLocation(self):
        parent = self.parentItem()
        st = parent.sceneTransform()
        pt = parent.transform().inverted()[0]
        rect = parent.boundingRect()
        rect.translate(-parent.pos())
        print("---\nparent bounding rect:")
        print("topLeft:",rect.left(),",",rect.top(),")")
        print("bottomRight:",rect.right(),",",rect.bottom())
        if self.role == self.TopLeftRole:
            self.setPos(pt.map(rect.topLeft() - QPointF(4,4)))
        elif self.role == self.BottomLeftRole:
            self.setPos(pt.map(QPointF(rect.left() - 4, rect.bottom() + 4)))
        elif self.role == self.BottomRightRole:
            self.setPos(pt.map(rect.bottomRight() + QPointF(4,4)))
        else:
            self.setPos(pt.map(QPointF(rect.right() + 4, rect.top() - 4)))
        center   = rect.center()
        newPoint = self.pos()
        iNumber   = (newPoint.x() - center.x()) + -((newPoint.y() - center.y())) * 1j
        angle     = cmath.phase(iNumber)+1.5*math.pi
        self.angleOffset = (360-math.degrees(angle))%360

    def boundingRect(self):
        return self.bounds

    def shape(self):
        s = QPainterPath()
        s.addRect(self.bounds)
        return s

    def configureAnimation(self):
        '''
        timeLine = QTimeLine(1000)
        timeLine.setLoopCount(0)
        animation = QGraphicsItemAnimation()
        animation.setItem(self)
        animation.setTimeLine(timeLine)
        animation.setRotationAt(0.0, 0)
        animation.setRotationAt(0.25, 3)
        animation.setRotationAt(0.5, 0)
        animation.setRotationAt(0.75, -3)
        animation.setRotationAt(1.0, 0)
        return animation
        '''
        return None

    def form(self):
        path = QPainterPath()
        path.moveTo(-3, 6)
        path.lineTo(-6, 3)
        path.lineTo(-4, 3)
        path.lineTo(-4, -2)
        path.arcTo(-4, -4, 4, 4, 90, 90)
        path.lineTo(-2, -4)
        path.lineTo(3, -4)
        path.lineTo(3, -6)
        path.lineTo(6, -3)
        path.lineTo(3, 0)
        path.lineTo(3, -2)
        path.lineTo(-1, -2)
        path.arcTo(-2, -2, 2, 2, 90, 90)
        path.lineTo(-2, 3)
        path.lineTo(0, 3)
        path.closeSubpath()
        return path

    def mousePressEvent(self, event):
        super(DrawingControlPointRotate, self).mousePressEvent(event)
        parent  = self.parentItem()
        if parent is not None:
            #parent.setTransformOriginPoint(parent.boundingRect().center())
            pass
        '''
        pixmap  = QPixmap(parent.boundingRect().width(), parent.boundingRect().height())
        pixmap.fill(QColor(0,0,0,0))
        painter = QPainter()
        painter.begin(pixmap)
        painter.setPen(QPen(QColor("gray"),1))
        painter.setBrush(QColor(0,0,0,0))
        painter.translate(pixmap.width()/2.0, pixmap.height()/2.0)
        painter.drawPath(parent.shape())
        painter.end()
        self.ghost = QGraphicsPixmapItem(pixmap)
        self.ghost.setPos(parent.scenePos())
        self.ghost.setRotation(parent.rotation())
        self.ghost.setOffset(-pixmap.width()/2, -pixmap.height()/2)
        self.scene().addItem(self.ghost)
        '''

    def mouseReleaseEvent(self, event):
        super(DrawingControlPointRotate, self).mouseReleaseEvent(event)
        parent    = self.parentItem()
        #self.scene().removeItem(self.ghost)
        if parent is not None:
            #parent.setRotation(self.appliedRotation)
            parent.setGroupRotation(self.appliedRotation)
            pass

    def mouseMoveEvent(self, event):
        parent    = self.parentItem()
        rect = parent.boundingRect()
        if self.pressed and parent is not None:
            parentPos = rect.center()
            newPoint  = event.scenePos()
            iNumber   = (newPoint.x() - parentPos.x())-((newPoint.y() - parentPos.y())) * 1j
            angle     = cmath.phase(iNumber)+1.5*math.pi
            self.appliedRotation  = (360-math.degrees(angle))%360 - self.angleOffset
            #print("rotation:", self.rotation, "new angle", (360-math.degrees(angle))%360)
            #if event.modifiers() and Qt.ShiftModifier == Qt.ShiftModifier:
            #self.parentItem().setRotation((360-math.degrees(angle))%360)
            #self.parentItem().setRotation(self.appliedRotation)
            #transform = QTransform()
            #transform.rotate(self.appliedRotation)
            #parent.setTransformOriginPoint(0, 500)
            #parent.setTransform(transform)
            #parent.setRotation(self.appliedRotation)
            parent.setGroupRotation(self.appliedRotation)
            #else:
            #    self.ghost.setRotation(self.appliedRotation)

class SelectionGroupFrame(QGraphicsItemGroup):
    '''
    modeChanged = pyqtSignal(int)
    geometricModeSet = pyqtSignal()
    rotateModeSet = pyqtSignal()
    shearModeSet  = pyqtSignal()
    scaleModeSet  = pyqtSignal()
    '''
    GeometricMode = 0
    RotateMode    = 1
    ShearMode     = 2
    ScaleMode     = 3

    def __init__(self, parent = None):
        super(SelectionGroupFrame, self).__init__(parent)
        self.count = 0
        self.mode  = -1
        self.controlPoints = []
        self.controlPoints.append(DrawingControlPointGuide(self))
        self.setFlag(QGraphicsItem.ItemIsFocusable, True)
        self.setFlag(QGraphicsItem.ItemIsSelectable, True)
        self.setFlag(QGraphicsItem.ItemIsMovable, True)
        self.setFlag(QGraphicsItem.ItemSendsGeometryChanges, True)
        self.setSelected(True)

    def setGroupRotation(self, rotation):
        transform = QTransform()
        center = self.boundingRect().center()
        transform.translate(center.x(), center.y())
        transform.rotate(rotation)
        transform.translate(-center.x(), -center.y())
        self.setTransform(transform)

    def setGroupScale(self, sx, sy):
        transform = QTransform()
        transform.scale(sx, sy)
        for item in self.groupList():
            o = self.mapToItem(item, self.boundingRect().center())
            print("origin.x():",o.x(),"origin.y():",o.y())
            #item.setTransformOriginPoint(
            item.setTransform(transform)

    def boundingRect(self):
        bounds = QRectF()
        for b in self.groupList():
            itemTransform = b.sceneTransform()
            rect = b.boundingRect()
            mappedRect = itemTransform.mapRect(rect)
            #newRect = QRectF(mappedRect.left(), mappedRect.top(), mappedRect.right() - mappedRect.left(), mappedRect.bottom() - mappedRect.top())
            bounds |= mappedRect

        #print("bounds.topLeft:",bounds.topLeft())
        #print("\t---Getting bounds... (",bounds.left(),",",bounds.top()," (",bounds.right(),",",bounds.bottom(),")")
        return bounds

    def paint(self, painter, option, widget):
        #super(SelectionGroupFrame, self).paint(painter, option, widget)
        # just for avoid indenting this copypaste test
        #painter.drawRect(self.boundingRect())
        if True:
            st = self.sceneTransform()
            # Fetchs the painter transformation for knowing the translate values
            # while the user scrolls the view
            pt = painter.transform()
            # Gets this item boundingRect mapped
            #rect = st.mapRect(self.boundingRect())
            # Resets the used transformation
            painter.resetTransform()
            # Translates the 'selected' rect upon the scrolled view
            painter.translate(pt.m31() - st.m31(), pt.m32() - st.m32())
            fgColor = option.palette.windowText().color()
            bgColor = QColor(0 if fgColor.red()   > 127 else 255,
                             0 if fgColor.green() > 127 else 255,
                             0 if fgColor.blue()  > 127 else 255)
            painter.setPen(QPen(bgColor, 1, Qt.SolidLine))
            painter.setBrush(Qt.NoBrush)
            painter.drawRect(self.boundingRect())
            #painter.drawRect(rect)
            painter.setPen(QPen(option.palette.windowText(), 0, Qt.DashLine))
            painter.setBrush(Qt.NoBrush)
            painter.drawRect(self.boundingRect())
            #painter.drawRect(rect)
            painter.setTransform(pt)
        #pass

    def setMode(self, mode):
        if self.__isValidMode(mode) is not True:
            raise ValueError("mode is not a valid mode.")
        #if mode == self.mode:
        #    return
        for child in self.controlPoints:
            child.setParentItem(None)
            self.scene().removeItem(child)
        del self.controlPoints[:]
        self.mode = mode
        #print("mode has been set to",mode)
        #self.emit(SIGNAL("modeChanged(int)"), self.mode)
        print("in setmode: boundingRect().topLeft():",self.boundingRect().topLeft())
        if mode == self.GeometricMode:
            #self.emit(SIGNAL("geometricModeSet()"))
            for angle in [0, 45, 90, 135, 180, 225, 270, 315]:
                self.controlPoints.append(DrawingControlPointResize(angle, self))
        elif mode == self.RotateMode:
            #self.emit(SIGNAL("rotateModeSet()"))
            #for role in [DrawingControlPointRotate.TopLeftRole, DrawingControlPointRotate.TopRightRole, DrawingControlPointRotate.BottomLeftRole, DrawingControlPointRotate.BottomRightRole]:
            for role in DrawingControlPointRotate.Roles:
                self.controlPoints.append(DrawingControlPointRotate(role, self))
        elif mode == self.ScaleMode:
            for role in DrawingControlPointScale.Roles:
                self.controlPoints.append(DrawingControlPointScale(role, self))
            #self.emit(SIGNAL("scaleModeSet()"))
        elif mode == self.ShearMode:
            #for role in [DrawingControlPointShear.TopRole, DrawingControlPointShear.RightRole, DrawingControlPointShear.BottomRole, DrawingControlPointShear.LeftRole]:
            for role in DrawingControlPointShear.Roles:
                self.controlPoints.append(DrawingControlPointShear(role, self))
            #self.emit(SIGNAL("shearModeSer()"))

    def setNextMode(self):
        mode = self.mode + 1
        try:
            self.setMode((mode)%4)
        except:
            self.setMode((mode + 1)%4)

    def setModeFromAction(self, action):
        self.setMode(action.data())

    def __isValidMode(self, mode):
        return ((mode == self.GeometricMode  and self.count <= 1) or
                mode == self.ShearMode or
                mode == self.RotateMode or
                mode == self.ScaleMode)

    def keyPressEvent(self, event):
        print("key press event", event)
        if event.key() == Qt.Key_Space:
            self.setNextMode()

    def keyReleaseEvent(self, event):
        #print("key release event", event)
        pass

    def groupList(self):
        #return [val for val in self.childItems() if val not in self.controlPoints]
        return [val for val in self.childItems() if isinstance(val, Drawing)]

    def ungroup(self):
        for child in [val for val in self.childItems() if val not in self.controlPoints]:
            self.removeFromGroup(child)
            #self.scene().addItem(child)

    def addToGroup(self, item):
        print("BEFORE adding: boundingRect().topLeft():",self.boundingRect().topLeft())
        super(SelectionGroupFrame, self).addToGroup(item)
        if item in self.childItems():
            self.count += 1
            #item.setFlag(QGraphicsItem.ItemStacksBehindParent, True)
            print("AFTER adding: boundingRect().topLeft():",self.boundingRect().topLeft())
            #self.setPos(self.boundingRect().center())
            #self.setPos(100,100)
            #self.setTransformOriginPoint(self.boundingRect().center())
            #self.setPos(self.boundingRect().center())
            try:
                self.setMode(self.mode)
            except:
                #print("Falling back to another mode")
                self.setNextMode()

    def removeFromGroup(self, item):
        super(SelectionGroupFrame, self).removeFromGroup(item)
        if item in self.childItems():
            print("item",item,"has not been removed from group")
        else:
            self.count -= 1
            #item.setFlag(QGraphicsItem.ItemStacksBehindParent, False)
            self.setTransformOriginPoint(self.boundingRect().center())
        for point in self.controlPoints:
            point.updateLocation()

    def mousePressEvent(self, event):
        self.pressed = 1
        self.item = self.scene().itemAt(event.scenePos())
        if self.item in self.controlPoints:
            self.item.mousePressEvent(event)
        else:
            super(SelectionGroupFrame, self).mousePressEvent(event)

    def mouseMoveEvent(self, event):
        if self.pressed:
            for point in self.controlPoints:
                point.setVisible(False)
        if self.item in self.controlPoints:
            self.item.mouseMoveEvent(event)
        else:
            super(SelectionGroupFrame, self).mouseMoveEvent(event)

    def mouseReleaseEvent(self, event):
        self.pressed = 0
        if self.item in self.controlPoints:
            self.item.mouseReleaseEvent(event)
        else:
            super(SelectionGroupFrame, self).mouseReleaseEvent(event)
        for point in self.controlPoints:
            point.setVisible(True)
            point.updateLocation()

    def itemChange(self, change, value):
        if change == QGraphicsItem.ItemSceneChange:
            if value is None:
                for point in self.controlPoints:
                    self.scene().removeItem(point)
                del self.controlPoints[:]
        return super(SelectionGroupFrame, self).itemChange(change, value)

    def hoverEnterEvent(self, event):
        pass

    def hoverMoveEvent(self, event):
        pass
    
    def hoverLeaveEvent(self, event):
        pass

class Drawing(QGraphicsPathItem):#QGraphicsItem):
    def __init__(self, ddata = None):
        super(Drawing, self).__init__()
        print("Drawing constructor (",id(self)," ddata:",id(ddata))
        self.showGuides = True
        self.pressed = 0
        if ddata is None:
            self.ddata = pytellapic.ddata_t()
            self.ddata.number = 0 #TODO: set as constant
            self.setFillEnabled(False)
            self.setStrokeEnabled(True)
        else:
            self.__initData(ddata)
        self.setActive(True)
        self.setEnabled(True)
        #self.selectedStroke = QPen(QColor("yellow"), 1, Qt.DashLine, Qt.SquareCap, Qt.MiterJoin)
        self.setAcceptHoverEvents(True)
        #for angle in [0, 45, 90, 135, 180, 225, 270, 315]:
        #    self.controlPoints.append(DrawingControlPointResize(angle, self))
        #self.controlPoints.append(DrawingControlPointGuide(self))

    @classmethod
    def withUser(cls, user):
        obj = cls()
        obj.setUser(user)
        return obj

    def __initData(self, ddata):
        self.ddata = ddata
        self.setBrush(QBrush(self.getColorFromData(self.ddata.fillcolor)))
        self.setPen(QPen(QColor(), self.ddata.width))
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

    def setDrawingData(self, ddata):
        self.__initData(ddata)

    def setDcbyte(self, dcbyte, dcbyte_ext):
        self.ddata.dcbyte = dcbyte
        self.ddata.dcbyte_ext = dcbyte_ext

    def setNumber(self, number):
        self.ddata.number = number

    def setStrokeWidth(self, width):
        print("widht:",width)
        self.ddata.width = width
        oldPen = self.pen()
        self.setPen(QPen(oldPen.brush(), width, oldPen.style(), oldPen.capStyle(), oldPen.joinStyle()))
        self.update(self.boundingRect())

    def setOpacity(self, opacity):
        self.ddata.opacity = opacity
        #TODO

    def setFillColor(self, color):
        self.ddata.fillcolor.red   = color.red()
        self.ddata.fillcolor.green = color.green()
        self.ddata.fillcolor.blue  = color.blue()
        self.ddata.fillcolor.alpha = color.alpha()

    def setBrush(self, brush):
        super(Drawing, self).setBrush(brush)
        self.setFillColor(brush.color())
        self.update(self.boundingRect())

    def setBounds(self, x1, y1, x2, y2):
        ''' setBounds() sets the points in the respective order.
        This does not necessary mean that (x1,y1) is the top left
        corner of the bounding rectangle. (x1,y1) is the first
        point used to draw the shape, and (x2,y2) is the last point
        while drawing the shape. Take note on that. Could be something
        like this:
        
        (x2,y2) +-----------+                   +-----------+ (x1,y1)
                |           |        or         |           |
                |     O     |       this        |     O     |
                |           |                   |           |
                +-----------+ (x1,y1)   (x2,y2) +-----------+
        or this:
        (x1,y1) +-----------+
                |           |
                |     O     |
                |           |
                +-----------+ (x2,y2)
        
        and so on...
        
        Where O = (0,0) and defines item's origin in local coordinates.
        (x1,y1) and (x2,y2) though, are ABSOLUTE coordinates respective
        the scene. They are not local coordinates in the item coordinate
        system. They are GLOBAL coordinates relative to the scene received
        over the network to describe where the item should be placed.
        '''
        self.ddata.point1.x = int(x1) #the protocol is limited to integer coordinates
        self.ddata.point1.y = int(y1) #the protocol is limited to integer coordinates
        self.ddata.point2.x = int(x2) #the protocol is limited to integer coordinates
        self.ddata.point2.y = int(y2) #the protocol is limited to integer coordinates
        if self.ddata.point1.x < self.ddata.point2.x:
            left   = self.ddata.point1.x
            right  = self.ddata.point2.x
        else:
            left   = self.ddata.point2.x
            right  = self.ddata.point1.x
        if self.ddata.point1.y < self.ddata.point2.y:
            top    = self.ddata.point1.y
            bottom = self.ddata.point2.y
        else:
            top    = self.ddata.point2.y
            bottom = self.ddata.point1.y
        # setPos() will set the (x,y) coordinate based on this
        # QGraphicsItem center (0,0). Refer to Qt doc for more info.
        punto = QPointF(left + abs(self.ddata.point1.x - self.ddata.point2.x)/2,
                        top + abs(self.ddata.point1.y - self.ddata.point2.y)/2
                        )
        print("medio : ",punto.x(),",",punto.y())
        self.setPos(punto)
        #self.setPos(left + abs(self.ddata.point1.x - self.ddata.point2.x)/2,
        #           top  + abs(self.ddata.point1.y - self.ddata.point2.y)/2
        #          )
        self.prepareGeometryChange()
        for point in self.controlPoints:
            point.updateLocation()
    '''
    def setBoundingRect(self, x1, y1, x2, y2):
        offset = 0 if self.pen is None else self.pen.width()
        width  = abs(x1-x2)
        height = abs(y1-y2)
        self.bounds = QRectF(-width/2 - offset / 2,
                             -height/2 - offset / 2, 
                             width + offset,
                             height + offset
                             )

    def boundingRect(self):
    '''

    def paint(self, painter, option, widget = None):
        #painter.setClipRect(option.exposedRect)
        if self.shouldStroke:
            painter.setPen(self.pen())
        else:
            painter.setPen(QColor(0,0,0,0))
        if self.shouldFill:
            painter.setBrush(self.brush())
        else:
            painter.setBrush(QColor(0,0,0,0))
        #painter.setRenderHint(self.renderHint, False)
        #super(Drawing, self).paint(painter, option, widget)
        painter.drawPath(self.path())
        if self.isSelected():
            offset = self.pen().width() / 2
            # Sry for this var names, but I want it to be short.
            # Fetchs the scene transformation for mapping this item boundingRect
            st = self.sceneTransform()
            # Fetchs the painter transformation for knowing the translate values
            # while the user scrolls the view
            pt = painter.transform()
            # Gets this item boundingRect mapped
            rect = st.mapRect(self.boundingRect())
            # Resets the used transformation
            painter.resetTransform()
            # Translates the 'selected' rect upon the scrolled view
            painter.translate(pt.m31() - st.m31(), pt.m32() - st.m32())
            fgColor = option.palette.windowText().color()
            bgColor = QColor(0 if fgColor.red()   > 127 else 255,
                             0 if fgColor.green() > 127 else 255,
                             0 if fgColor.blue()  > 127 else 255)
            painter.setPen(QPen(bgColor, 1, Qt.SolidLine))
            painter.setBrush(Qt.NoBrush)
            painter.drawRect(rect)
            painter.setPen(QPen(option.palette.windowText(), 0, Qt.DashLine))
            painter.setBrush(Qt.NoBrush)
            painter.drawRect(rect)
            painter.setTransform(pt)

    def setStrokeEnabled(self, enabled):
        self.shouldStroke = enabled
        self.update(self.boundingRect())

    def setFillEnabled(self, enabled):
        self.shouldFill = enabled
        self.update(self.boundingRect())

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

    def sceneTopLeft(self):
        point1 = self.qtPoint1()
        point2 = self.qtPoint2()
        x = point1.x() if point1.x() < point2.x() else point2.x() 
        y = point1.y() if point1.y() < point2.y() else point2.y() 
        return QPoint(x,y)

    def sceneBottomRight(self):
        point1 = self.qtPoint1()
        point2 = self.qtPoint2()
        x = point1.x() if point1.x() > point2.x() else point2.x() 
        y = point1.y() if point1.y() > point2.y() else point2.y() 
        return QPoint(x,y)

    def getColorFromData(self, tellapicColor):
        return QColor(tellapicColor.red,
                            tellapicColor.green,
                            tellapicColor.blue,
                            tellapicColor.alpha
                            )

    '''
    def itemChange(self, change, value):
        print("changed", change,"value: ",value)
        if change == QGraphicsItem.ItemSelectedChange:
            print("selection changed")
            for point in self.controlPoints:
                try:
                    point.setPreviewEffect(not value)
                except:
                    pass
                point.setVisible(value)
            return value
        elif change == QGraphicsItem.ItemPositionChange and self.pressed:
            print("position changed")
            print("ItemPositiongChange ({x} ,{y})".format(x=value.x(),y=value.y()))
            # This is copied from Qt doc
            # Here should be the place to implement a snap to grid.
            newPos = value
            rect   = self.scene().sceneRect()
            if rect.contains(newPos) is False:
                newPos.setX(min(rect.right(), max(newPos.x(), rect.left())))
                newPos.setY(min(rect.bottom(), max(newPos.y(), rect.top())))
                return newPos
        elif change == QGraphicsItem.ItemPositionHasChanged and self.pressed:
            print("ItemPositionHasChanged ({x} ,{y})".format(x=value.x(),y=value.y()))
            oldPos = self.sceneTopLeft()+(self.sceneBottomRight()-self.sceneTopLeft())/2
            newPos = QPoint(int(value.x()), int(value.y()))
            offset = newPos - oldPos
            self.ddata.point1.x += offset.x()
            self.ddata.point1.y += offset.y()
            self.ddata.point2.x += offset.x()
            self.ddata.point2.y += offset.y()
            return #this return value is ignored. See Qt doc.
        elif change == QGraphicsItem.ItemRotationHasChanged:
            for point in self.controlPoints:
                point.updateLocation()
            return
        return super(Drawing, self).itemChange(change, value)
     '''

    def hoverEnterEvent(self, event):
        #super(Drawing, self).hoverEnterEvent(event)
        #self.setCursor(Qt.OpenHandCursor)
        pass

    def hoverMoveEvent(self, event):
        pass

    def hoverLeaveEvent(self, event):
        #super(Drawing, self).hoverEnterEvent(event)
        pass
    '''
    def mousePressEvent(self, event):
        self.pressed = 1
        #self.setCursor(Qt.ClosedHandCursor)
        super(Drawing, self).mousePressEvent(event)
    
    def mouseMoveEvent(self, event):
        super(Drawing, self).mouseMoveEvent(event)
    
    def mouseReleaseEvent(self, event):
        self.setCursor(Qt.OpenHandCursor)
        self.pressed = 0
        super(Drawing, self).mouseReleaseEvent(event)
    '''
    def hideAction(self):
        print("hide action here")

    def deleteAction(self):
        print("delete action here")

    def setGuidesEnabled(self, toggle):
        print("show guides here")
        self.showGuides = toggle

class DrawingText(Drawing):
    # Instantiates the DrawingText with a specific number if provided
    def __init__(self, ddata = None):
        super(DrawingText, self).__init__(ddata)
        print("DrawingText constructor.")
        self.drawing = QPainterPath()
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
        self.brush = QBrush(self.getColorFromData(self.ddata.type.text.color))
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
        self.brush      = QBrush(QColor(0, 0, 0, 0))
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
        if self.drawing.isEmpty() is not True:
            self.drawing = QPainterPath()        
            self.drawing.addText(x1, y1, self.font, self.text)

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
        self.drawing = QPainterPath()
        '''
        if ddata is not None:
            # Initiate this object values with the drawing data provided
            self.__initData(ddata)
        else:
            # Sets default values for this object
            self.__defaultValues()
        '''
        self.__setItemFlags()
        self.renderHint = QPainter.HighQualityAntialiasing

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
    '''
    def __defaultValues(self):
        self.pen   = QPen()
        self.brush = QBrush()
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
    '''
    def __setItemFlags(self):
        self.setFlag(QGraphicsItem.ItemIsMovable, True)
        self.setFlag(QGraphicsItem.ItemIsSelectable, True)
        self.setFlag(QGraphicsItem.ItemSendsScenePositionChanges, True)
        self.setFlag(QGraphicsItem.ItemSendsGeometryChanges, True)

    '''
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

    '''
    def setStrokeColor(self, color):
        self.ddata.type.figure.color.red   = color.red()
        self.ddata.type.figure.color.green = color.green()
        self.ddata.type.figure.color.blue  = color.blue()
        self.ddata.type.figure.color.alpha = color.alpha()
        oldPen = self.pen()
        self.setPen(QPen(color, oldPen.width(), oldPen.style(), oldPen.capStyle(), oldPen.joinStyle()))
        self.update(self.boundingRect())
    
    
    def setLineJoins(self, lj):
        self.ddata.type.figure.linejoin = PytellapicJoinsStyleMap[lj]
        oldPen = self.pen()
        self.setPen(QPen(oldPen.brush(), oldPen.width(), oldPen.style(), oldPen.capStyle(), lj))
        self.update(self.boundingRect())

    def setEndCaps(self, ec):
        self.ddata.type.figure.endcaps = PytellapicCapsStyleMap[ec]
        oldPen = self.pen()
        self.setPen(QPen(oldPen.brush(), oldPen.width(), oldPen.style(), ec, oldPen.joinStyle()))
        self.update(self.boundingRect())

    def setMiterLimit(self, ml):
        self.ddata.type.figure.miterlimit = ml
        oldPen = self.pen()
        oldPen.setMiterLimit(ml)
        self.setPen(oldPen)
        self.update(self.boundingRect())

    def setDashStyle(self, phase, array):
        self.ddata.type.figure.dash_array = array
        self.ddata.type.figure.dash_phase = phase
        self.pen.setDashOffset(phase)
        self.pen.setDashPattern(array)
        self.update(self.boundingRect())
    '''
    def setShapeBounds(self, x1, y1, x2, y2):
        super(DrawingShape, self).setBounds(x1, y1, x2, y2)
    '''
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
        print("+ fillcolor : {fillcolor}{a}".format(fillcolor=self.brush.color().name(), a=hex(self.brush.color().alpha())[2:]))
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
        #self.path = QPainterPath()

    def setShapeBounds(self, x1, y1, x2, y2):
        #super(DrawingShapeRectangle, self).setBounds(x1, y1, x2, y2)
        if self.drawing.isEmpty() is not True:
            self.drawing = QPainterPath()
        width = abs(x1-x2)
        height= abs(y1-y2)
        rect = QRectF(-abs(x1-x2)/2,
                      -abs(y2-y1)/2,
                      width,
                      height
                      )
        self.drawing.addRect(rect)
        self.setPath(self.drawing)
        self.setPos((x1+x2)/2, (y1+y2)/2)

    def resize(self, left, top, right, bottom):
        self.setShapeBounds(left, top, right, bottom)

class A(QGraphicsPathItem):
    def __init__(self):
        super(A, self).__init__()
        self.setFlag(QGraphicsItem.ItemIsSelectable, True)
        p = QPainterPath()
        p.addRect(QRectF(100,100, 100, 100))
        self.setPath(p)
        self.setBrush(QColor())
        self.setPen(QPen(QColor("blue"),10))

class DrawingShapeEllipse(DrawingShape):
    def __init__(self, ddata = None):
        super(DrawingShapeEllipse, self).__init__(ddata)
        self.name = "ellipse"
        self.drawing.moveTo(100,100)
        self.drawing.lineTo(100, 50)
        self.drawing.arcMoveTo(100, 0, 100, 100, 90)
        self.drawing.arcTo(100, 0, 100, 100, 90, 90)
        self.drawing.lineTo(10, 300)
        self.setPath(self.drawing)
        #super(DrawingShapeEllipse, self).setBounds(0,0,500,500)

    def setShapeBounds(self, x1, y1, x2, y2):
        super(DrawingShapeEllipse, self).setBounds(x1, y1, x2, y2)
        if self.drawing.isEmpty() is not True:
            self.drawing = QPainterPath()
        self.rect = QRectF(
            x1 if x1-x2<=0 else x2,
            y1 if y1-y2<=0 else y2, 
            abs(x1-x2),
            abs(y1-y2))
        self.drawing.addEllipse(self.rect)

class DrawingShapeLine(DrawingShape):
    def __init__(self, ddata = None):
        super(DrawingShapeLine, self).__init__(ddata)

    def setShapeBounds(self, x1, y1, x2, y2):
        super(DrawingShapeLine, self).setBounds( x1, y1, x2, y2)
        if self.drawing.isEmpty() is not True:
            self.drawing = QPainterPath()
        self.drawing.moveTo(x1, y1)
        self.drawinglineTo(x2, y2)
        self.drawing.closeSubpath()    

class ToolBoxModel(QObject):
    toolChanged        = pyqtSignal(QString)
    fillColorChanged   = pyqtSignal(QColor)
    strokeColorChanged = pyqtSignal(QColor)
    strokeWidthChanged = pyqtSignal(float)
    lineJoinsChanged   = pyqtSignal(int)
    endCapsChanged     = pyqtSignal(int)
    miterLimitChanged  = pyqtSignal(float)
    dashStyleChanged   = pyqtSignal(float, float)
    strokeEnabled      = pyqtSignal(bool)
    fillEnabled        = pyqtSignal(bool)

    def __init__(self):
        QObject.__init__(self) 
        self.initialValues()
        self.lastUsedTool = None
        self.tools = []
        self.tools.append(DrawingToolRectangle(self))
        self.tools.append(DrawingToolEllipse(self))
        self.tools.append(ControlToolSelector(self))

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
            active = tool.name == toolName
            tool.setActive(active)
            if active and self.lastUsedTool != tool:
                print("set tool: ",toolName)
                self.lastUsedTool = tool
                self.configureToolBox(tool)
                self.toolChanged.emit(toolName)

    def configureToolBox(self, tool):
        #if tool.canDraw():
        self.fontPropertyEnabled   = tool.hasFontProperties()
        self.strokePropertyEnabled = tool.hasStrokeStylesProperties()
        self.fillPropertyEnabled   = tool.hasFillColorProperties()

    def getLastUsedTool(self):
        return self.lastUsedTool

    def getToolByName(self, toolName):
        for tool in self.tools:
            if tool.name == toolName:
                return tool

    def setStrokeWidth(self, width):
        self.pen.setWidth(width)
        self.strokeWidthChanged.emit(width)

    def setLineJoins(self, joins):
        self.pen.setJoinStyle(joins)
        self.lineJoinsChanged.emit(joins)

    def setEndCaps(self, caps):
        self.pen.setCapStyle(caps)
        self.endCapsChanged.emit(caps)

    def setStrokeColor(self, color):
        #self.pen.setColor(QColor(color.red(), color.green(), color.blue(), color.alpha()))
        self.pen.setColor(color)
        self.strokeColorChanged.emit(color)

    def setFillColor(self, color):
        #self.brush.setColor(QColor(color.red(), color.green(), color.blue(), color.alpha()))
        self.brush.setColor(color)
        self.fillColorChanged.emit(color)

    def setMiterLimit(self, ml):
        self.pen.setMiterLimit(ml)
        self.miterLimitChanged.emit(ml)

    def setDashStyle(self, phase, array):
        self.pen.setDashOffset(phase)
        self.pen.setDashPattern(array)
        self.dashStyleChanged.emit(phase, array)

    def setStrokeEnabled(self, enabled):
        self.shouldStroke = enabled
        self.strokeEnabled.emit(enabled)

    def setFillEnabled(self, enabled):
        self.shouldFill = enabled
        self.fillEnabled.emit(enabled)

    def isFontPropertyEnabled(self):
        return self.fontPropertyEnabled

    def isStrokePropertyEnabled(self):
        return self.strokePropertyEnabled

    def isFillPropertyEnabled(self):
        return self.fillPropertyEnabled

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
    mouseCoordinatesChanged = pyqtSignal(QPointF)
    mouseYCoordinateChanged = pyqtSignal(QString)
    
    def __init__(self, model, parent = None):
        super(TellapicScene, self).__init__(parent)
        print("TellapicScene instantiated.")
        self.model = model
        for tool in self.model.tools:
            tool.scene = self
        self.model.toolChanged.connect(self.updateTool)
        self.tool = model.getLastUsedTool()
        self.background = QPixmap("bart.jpg")
        self.temporalItem = None
        #self.setForegroundBrush(QBrush(Qt.lightGray, Qt.CrossPattern))
        self.selectedEffect = SelectedEffect()
        self.pressed = 0
        self.coords = QPointF()
        self.guidePen = QPen(QColor("blue"),1)
        self.guidesEnabled = True

    def drawForeground(self, painter, rect):
        if self.guidesEnabled:
            painter.setClipRect(rect)
            painter.setPen(self.guidePen)
            painter.drawLine(self.coords.x(), rect.top(), self.coords.x(), rect.bottom())
            painter.drawLine(rect.left(), self.coords.y(), rect.right(), self.coords.y())
        cItem = self.itemAt(self.coords)
        if cItem is not None:
            self.views()[0].viewport().setCursor(cItem.cursor())
        elif self.tool is not None:
            self.views()[0].viewport().setCursor(self.tool.cursor())

    def mousePressEvent(self, event):
        if self.tool is not None:
            self.pressed = 1
            self.tool.mousePressed(event)

    def mouseMoveEvent(self, event):
        self.coords = event.scenePos()
        self.mouseCoordinatesChanged.emit(self.coords)
        if self.tool is not None:
            if self.pressed:
                self.tool.mouseDragged(event)
            else:
                self.tool.mouseMoved(event)
        self.invalidate()

    def mouseReleaseEvent(self, event):
        if self.tool is not None:
            self.tool.mouseReleased(event)
        self.pressed = 0

    def updateTool(self, toolName):
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

