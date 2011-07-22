from PyQt4 import *
from PyQt4.QtGui import *
from PyQt4.QtCore import *
from Drawing import *
from mainWidget import *
from NetManager import *
import sys

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Painting(QtGui.QLabel):
    def __init__(self):
        QtGui.QLabel.__init__(self)
        self.drawingList = []
        self.usedTool = DrawingToolRectangle()
        self.background = None

    def paintEvent(self, ev):
        self.p = QPainter()
        self.p.begin(self)
        if (self.background is not None):
            self.p.drawPixmap(0, 0, self.background)
        self.p.setBackground(QtGui.QColor("black"))
        for drawing in self.drawingList:
            print("Drawing: ", drawing)
            drawing.draw(self.p)
        if (self.usedTool.drawing is not None):
            self.usedTool.drawing.draw(self.p)
        self.p.end()
        
    def addDrawing(self, drawing):
        self.drawingList.append(drawing)
        self.drawingList.sort(key=lambda drawing: drawing.number)
        self.update()

    def mousePressEvent(self, ev):
        self.pressed = 1
        self.usedTool.mousePressed(ev)
        self.update()

    def mouseMoveEvent(self, ev):
        if (self.pressed == 1):
            self.usedTool.mouseDragged(ev)
            self.update()
        
    def mouseReleaseEvent(self, ev):
        self.pressed = 0
        self.usedTool.mouseReleased(ev)
        self.addDrawing(self.usedTool.drawing)
        self.update()

    def setBackgroundImage(self, imageName):
        self.background = QPixmap(imageName)
        self.setPixmap(self.background)

class Main(QtGui.QMainWindow):
    
    def __init__(self, args):
        QtGui.QMainWindow.__init__(self)
        self.ui = Ui_MainWindow()
        self.ui.setupUi(self)
        self.centralwidget = QtGui.QWidget(self)
        sizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Maximum, QtGui.QSizePolicy.Maximum)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.centralwidget.sizePolicy().hasHeightForWidth())
        self.centralwidget.setSizePolicy(sizePolicy)
        self.centralwidget.setObjectName(_fromUtf8("centralwidget"))
        self.gridLayoutWidget = QtGui.QWidget(self.centralwidget)
        self.gridLayoutWidget.setGeometry(QtCore.QRect(10, 10, 900, 481))
        self.gridLayoutWidget.setObjectName(_fromUtf8("gridLayoutWidget"))
        self.gridLayout = QtGui.QGridLayout(self.gridLayoutWidget)
        self.gridLayout.setMargin(0)
        self.gridLayout.setObjectName(_fromUtf8("gridLayout"))
        self.scrollArea = QtGui.QScrollArea()
        self.scrollArea.setWidgetResizable(True)
        self.scrollArea.setObjectName(_fromUtf8("scrollArea"))
        self.painting = Painting()
        self.painting.setGeometry(QtCore.QRect(0, 0,90, 90))
        self.painting.setObjectName(_fromUtf8("scrollAreaWidgetContents"))
        self.scrollArea.setWidget(self.painting)
        self.gridLayout.addWidget(self.scrollArea, 0, 0, 1, 1)
        self.setCentralWidget(self.centralwidget)
        self.thread = NetManager(self, args.host[0], str(args.port[0]), args.user[0], args.password[0])
        self.thread.begin()


    def resizeEvent(self, event):
        QMainWindow.resizeEvent(self,event)
        size = event.size()
        self.gridLayoutWidget.setGeometry(QtCore.QRect(0,0,size.width()-10, size.height()-60))

    def isShapeUpdate(self, ddata):
        if (len(self.painting.drawingList) == 0):
            return -1
        return self.binSearch(self.painting.drawingList, 0, len(self.painting.drawingList)-1, ddata.number)
        
    def binSearch(self, ilist, i, j, key):
        print("Searching ",key," i: ",i, " j: ",j)
        if (i == j):
            if (key == ilist[i].number):
                return i
            else:
                return -1
        else:
            middle = int((i + j) / 2)
            if (ilist[middle].number == key):
                return self.binSearch(ilist, middle, middle, key)

            elif (ilist[middle].number < key):
                return self.binSearch(ilist, middle + 1, j, key)
            
            else:
                return self.binSearch(ilist, i, middle, key)

    def editDrawingText(self, drawingText, data):
        pass

    def editDrawingShape(self, drawingShape, drawingData):
        drawingShape.setNumber(drawingData.number)
        drawingShape.setStrokeWidth(drawingData.width)
        drawingShape.setFillColor(drawingData.fillcolor)
        drawingShape.setStrokeColor(drawingData.type.figure.color)
        drawingShape.setLineJoins(drawingData.type.figure.linejoin)
        drawingShape.setEndCaps(drawingData.type.figure.endcaps)
        drawingShape.setMiterLimit(drawingData.type.figure.miterlimit)
        drawingShape.setBounds(drawingData.point1.x, drawingData.point1.y, drawingData.type.figure.point2.x, drawingData.type.figure.point2.y)

    def doShapeUpdate(self, index, drawingData):
        drawing = self.painting.drawingList[index]
        if (drawingData.dcbyte & pytellapic.TOOL_MASK == pytellapic.TOOL_EDIT_FIG):
            self.editDrawingShape(drawing, drawingData)
        else:
            self.editDrawingText(drawing, drawingData)
        self.painting.update()

    def processDrawingData(self, stream):
        drawingData = stream.data.drawing
        tool    = drawingData.dcbyte & pytellapic.TOOL_MASK
        drawing = DrawingShape()
        # ddata part is common for all figures, including text.
        drawing.setNumber(drawingData.number)
        drawing.setStrokeWidth(drawingData.width)
        drawing.setFillColor(drawingData.fillcolor)

        if (tool == pytellapic.TOOL_TEXT):
            shape = DrawingText(drawing)
            shape.setText(drawingData.type.text.info)
            shape.setFont(drawingData.type.text.color, drawingData.type.text.style, drawingData.type.text.face, drawingData.width)
        else:
            # figure data is common for all drawings except Text
            drawing.setStrokeColor(drawingData.type.figure.color)
            drawing.setLineJoins(drawingData.type.figure.linejoin)
            drawing.setEndCaps(drawingData.type.figure.endcaps)
            drawing.setMiterLimit(drawingData.type.figure.miterlimit)
            #rectangle.setDashStyle(drawingData.type.figure.dash_phase, drawingData.type.figure.dash_array)

            if (tool == pytellapic.TOOL_RECT):
                shape = DrawingShapeRectangle(drawing)

            elif (tool == pytellapic.TOOL_ELLIPSE):
                shape = DrawingShapeEllipse(drawing)

            elif (tool == pytellapic.TOOL_LINE):
                shape = DrawingShapeLine(drawing)

        shape.setBounds(drawingData.point1.x, drawingData.point1.y, drawingData.type.figure.point2.x, drawingData.type.figure.point2.y)            
        drawing = shape

        self.painting.addDrawing(drawing)


    def customEvent(self,event):
        print("Event: ",self.thread.cbyte[event.stream.header.cbyte])

        # Is a drawn figure?
        if (pytellapic.tellapic_isfig(event.stream.header)):
            drawingData = event.stream.data.drawing
            # Is this an update of a drawn shape?
            index = self.isShapeUpdate(drawingData)
            print("index: ", index)
            if (index != -1):
                self.doShapeUpdate(index, drawingData)
            else:
                self.processDrawingData(event.stream)
                
        elif (pytellapic.tellapic_isdrw(event.stream.header)):
            pass

        elif (event.stream.header.cbyte == pytellapic.CTL_SV_FILE):
            print("file?: ", event.stream.data.file)



if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description='Connects to a tellapic server')
    parser.add_argument('-c', '--host', required=True, nargs=1, type=str, help='the host name to connect to.')
    parser.add_argument('-p', '--port', required=True, nargs=1, type=int, help='the HOST port to use.')
    parser.add_argument('-u', '--user', required=True, nargs=1, type=str, help='the USERNAME to use.')
    parser.add_argument('-P', '--password', required=True, nargs=1, type=str, help='the server PASSWORD.')
    args = parser.parse_args()
    app = QtGui.QApplication(sys.argv)
    main = Main(args)
    main.show()
    sys.exit(app.exec_())
