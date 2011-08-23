from PyQt4.QtCore import Qt
from PyQt4.QtCore import QObject
from PyQt4.QtCore import QEvent

from PyQt4.QtGui import QApplication
from PyQt4.QtGui import QStandardItemModel
from PyQt4.QtGui import QIcon
from PyQt4.QtGui import QPixmap
from PyQt4.QtGui import QStandardItem

import pytellapic
import Drawing

IdentificationRole = Qt.UserRole + 0
NoUserId = -1 #Valid tellapic user id goes from 0 to 31

class TellapicEvent(QEvent):
    
    UpdateEvent       = 0 + QEvent.User
    NewImageEvent     = 1 + QEvent.User
    NewFigureEvent    = 2 + QEvent.User
    AddUserEvent      = 3 + QEvent.User
    RemoveUserEvent   = 4 + QEvent.User
    RemoveFigureEvent = 5 + QEvent.User
    SetFigureIdEvent  = 6 + QEvent.User
    DisconnectEvent   = 7 + QEvent.User
    SetUserIdEvent    = 8 + QEvent.User
    #FigureEvent       = 9 + QEvent.User
    
    def __init__(self, arg, eType = None):
        if (eType is not None):
            QEvent.__init__(self, eType)

        self.arg = arg

class TellapicShapeFactory(object):
    
    @classmethod
    def createShape(cls, drawingData):
        tool = drawingData.dcbyte & pytellapic.TOOL_MASK
        if (tool == pytellapic.TOOL_TEXT):
            shape = Drawing.DrawingText(drawingData)
            #shape.setText(drawingData.type.text.info)
            #shape.setFont(drawingData.type.text.color, drawingData.type.text.style, drawingData.type.text.face, drawingData.width)
        elif (tool == pytellapic.TOOL_RECT):
            shape = Drawing.DrawingShapeRectangle(drawingData)
        elif (tool == pytellapic.TOOL_ELLIPSE):
            shape = Drawing.DrawingShapeEllipse(drawingData)
        elif (tool == pytellapic.TOOL_LINE):
            shape = Drawing.DrawingShapeLine(drawingData)
            
        return shape
    
class TellapicEventDispatcher(QObject):

    def __init__(self, tellapicScene, userManager):
        QObject.__init__(self)
        self.tellapicScene = tellapicScene
        self.userManager   = userManager
    '''
    def customEvent(self, event):
        eType = event.type()
        if eType == TellapicEvent.UpdateEvent or eType == TellapicEvent.NewImageEvent or eType == TellapicEvent.NewShapeEvent:
            QApplication.postEvent(self.tellapicScene, TellapicEvent(event.arg, eType))
        elif eType == TellapicEvent.AddUserEvent or eType == TellapicEvent.RemoveUserEvent or eType == TellapicEvent.SetUserIdEvent:
            QApplication.postEvent(self.userManager, TellapicEvent(event.arg, eType))
    '''
        
    def customEvent(self, event):
        eType  = event.type()
        if eType == TellapicEvent.AddUserEvent or eType == TellapicEvent.RemoveUserEvent or eType == TellapicEvent.SetUserIdEvent:
            QApplication.postEvent(self.userManager, TellapicEvent(event.arg, eType))
        elif eType == TellapicEvent.UpdateEvent or eType == TellapicEvent.NewImageEvent or eType == TellapicEvent.NewShapeEvent:
            stream = event.arg
            if (pytellapic.tellapic_isfig(stream.header)):
                self.processIsFig(stream)
            elif (pytellapic.tellapic_isdrw(stream.header)):
                self.processIsDrw(stream)
            elif (pytellapic.tellapic_isctle(stream.header)):
                self.processIsCtlE(stream)
            elif (pytellapic.tellapic_isctl(stream.header)):
                self.processIsCtl(stream)
            elif (pytellapic.tellapic_isfile(stream.header)):
                self.processIsFile(stream)

    def processIsFig(self, stream):
        drawingData = self.__cloneDrawingDataFromStream(stream)
        tool = drawingData.dcbyte & pytellapic.TOOL_MASK
        print("\n---\nprocessIsFIg. ddata:",id(stream.data.drawing)," ddata.number:",stream.data.drawing.number)
        if stream.data.drawing.dcbyte == pytellapic.TOOL_EDIT_FIG:
            shape = self.tellapicScene.getDrawing(stream.data.drawing.number)
            if tool == pytellapic.TOOL_TEXT:
                pass
            else:
                shape.setDrawingData(drawingData)
                shape.setSelected(True)
            if shape is not None:
                QApplication.postEvent(self.tellapicScene, TellapicEvent(shape, TellapicEvent.UpdateEvent))
        else:
            shape = TellapicShapeFactory.createShape(drawingData)
            
            if shape is not None:
                QApplication.postEvent(self.tellapicScene, TellapicEvent(shape, TellapicEvent.NewFigureEvent))

    def processIsDrw(self, stream):
        pass

    def processIsFile(self, stream):
        imageName = pytellapic.wrapToFile(stream.data.file, stream.header.ssize - pytellapic.HEADER_SIZE)
        QApplication.postEvent(self.tellapicScene, TellapicEvent(imageName, TellapicEvent.NewImageEvent))

    def processIsCtlE(self, stream):
        if stream.header.cbyte == pytellapic.CTL_SV_CLADD:
            QApplication.postEvent(self.userManager, TellapicEvent(stream, TellapicEvent.AddUserEvent))
        elif stream.header.cbyte == pytellapic.CTL_CL_RMFIG:
            QApplication.postEvent(self.tellapicScene, TellapicEvent(stream, TellapicEvent.RemoveFigureEvent))
        elif stream.header.cbyte == pytellapic.CTL_SV_FIGID:
            QApplication.postEvent(self.tellapicScene, TellapicEvent(stream, TellapicEvent.SetFigureIdEvent))
    
    def processIsCtl(self, stream):
        if stream.header.cbyte == pytellapic.CTL_SV_CLRM:
            QApplication.postEvent(self.userManager, TellapicEvent(stream, TellapicEvent.RemoveUserEvent))
        elif stream.header.cbyte == pytellapic.CTL_CL_DISC:
            QApplication.postEvent(self.userManager, TellapicEvent(stream, TellapicEvent.DisconnectEvent))
        
    def processIsChatB(self, stream):
        pass

    def processIsChatP(self, stream):
        pass

    def processIsPong(self, stream):
        pass
    
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
    
class TellapicUserManager(QObject):

    def __init__(self, localUser):
        QObject.__init__(self)
        self.model = QStandardItemModel(0, 4)
        self.model.setHorizontalHeaderLabels(["Users","Show","Chat","Properties"])
        self.addUser(localUser)

    def addUser(self, user):
        parentItem = self.model.invisibleRootItem()
        parentItem.appendRow(user)

    def removeUser(self, user):
        parentItem = self.model.invisibleRootItem()
        parentItem.removeRow(user.row())
            
    def setUserId(self, pair):
        user = self.getUserByName(pair[0])
        if user is not None:
            user.setId(pair[1])

    def getUserById(self, ident):
        items = self.model.findItems("*", Qt.MatchWildcard, 0)
        for item in items:
            if ident == item.id:
                return item
        print("WARN: No user with id",ident,"found")
        return None

    def getUserByName(self, name):
        items = self.model.findItems(name, Qt.MatchExactly, 0)
        if len(items) == 1:
            return items[0]
        print("WARN: No user with name",name,"found")
        return None

    def customEvent(self, event):
        eType  = event.type()
        if eType == TellapicEvent.AddUserEvent:
            stream = event.arg
            self.addUser(TellapicRemoteUser(stream.data.control.idfrom, stream.data.control.info))
        elif eType == TellapicEvent.RemoveUserEvent:
            user = self.getUserById(event.arg.data.control.idfrom)
            if user is not None:
                self.removeUser(user)
        elif eType == TellapicEvent.SetUserIdEvent:
            self.setUserId(event.arg)

    def getModel(self):
        return self.model

class TellapicAbstractUser(QStandardItem):
    
    def __init__(self, ident, name):
        super(TellapicAbstractUser, self).__init__(QIcon(QPixmap(":/icons/resources/icons/app-icons/user.png")), name)
        print("TellapicAbstractUser constructor. ident: ",ident," name: ",name)
        self._icon = QIcon(QPixmap(":/icons/resources/icons/app-icons/user.png"))
        self.id = ident
        self._name = name

    @property
    def name(self):
        return self._name

    @name.setter
    def name(self, value):
        self._name = value

    def setId(self, value):
        self.setData(value, IdentificationRole)
        self.id = value

    def icon(self):
        return self._icon

    def addDrawing(self, drawing):
        drawing.setUser(self)
        drawingItem = QStandardItem(QIcon(QPixmap(":/icons/resources/icons/tool-icons/drawings.png")), drawing.name())
        drawingItem.setData(drawing.number, IdentificationRole)
        self.appendRow(drawingItem)

    def removeDrawing(self, drawing):
        item = self.getItem(drawing.number)
        self.removeRow(item.index())

    def getItem(self, number):
        pass

class TellapicLocalUser(TellapicAbstractUser):
    def __init__(self, ident, name):
        super(TellapicLocalUser, self).__init__(ident, name)

    def isRemote(self):
        return False

class TellapicRemoteUser(TellapicAbstractUser):
    def __init__(self, ident, name):
        super(TellapicRemoteUser, self).__init__(ident, name)

    def isRemote(self):
        return False

'''Stolen from http://stackoverflow.com/questions/36932/whats-the-best-way-to-implement-an-enum-in-python'''
class Enum(set):
    def __getattr__(self, name):
        if name in self:
            return name
        raise AttributeError
