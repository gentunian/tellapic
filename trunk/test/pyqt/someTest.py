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
    def __init__(self, model):
        QtGui.QLabel.__init__(self)
        self.drawingList = []
        self.usedTool = DrawingToolRectangle()
        self.background = None
        self.zoomX = 1
        self.model = model

    def paintEvent(self, ev):
        self.p = QPainter()
        self.p.begin(self)
        self.p.scale(self.zoomX, self.zoomX)
        if (self.background is not None):
            """
            w = self.background.width()
            h = self.background.height()
            backCopy = self.background.scaled(w * self.zoomX, h * self.zoomX, Qt.KeepAspectRatio, Qt.FastTransformation)
            """
            self.p.drawPixmap(0, 0, self.background)
        self.p.setBackground(QtGui.QColor("black"))
        for drawing in self.model.drawings():
            #print("Drawing: ", drawing)
            drawing.draw(self.p)
        if (self.usedTool.drawing is not None):
            self.usedTool.drawing.draw(self.p)
        self.p.end()
        
    def addDrawing(self, drawing):
        self.model.addDrawing(drawing)
        self.update()

    """
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
    """

    def wheelEvent(self, ev):
        step =  float(ev.delta())/1000
        self.zoomX += step
        if (self.zoomX < 0.1):
            self.zoomX = 0.25
        if (self.zoomX > 2.5):
            self.xoomX = 2
        self.update()

    def setBackgroundImage(self, imageName):
        self.background = QPixmap(imageName)
        self.setPixmap(self.background)

    def customEvent(self,event):
        etype = event.type()
        arg   = event.arg
        if (etype == QEvent.User + 1):
            self.setBackgroundImage(arg)
        elif (etype == QEvent.User):
            self.addDrawing(arg)


class Main(QtGui.QMainWindow):
    
    def __init__(self, args):
        QtGui.QMainWindow.__init__(self)
        model = DrawingModel()
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
        self.painting = Painting(model)
        self.painting.setGeometry(QtCore.QRect(0, 0,90, 90))
        self.painting.setObjectName(_fromUtf8("scrollAreaWidgetContents"))
        self.scrollArea.setWidget(self.painting)
        self.gridLayout.addWidget(self.scrollArea, 0, 0, 1, 1)
        self.setCentralWidget(self.centralwidget)
        self.thread = NetManager(self.painting, args.host[0], str(args.port[0]), args.user[0], args.password[0], model)
        self.thread.begin()


    def resizeEvent(self, event):
        QMainWindow.resizeEvent(self,event)
        size = event.size()
        self.gridLayoutWidget.setGeometry(QtCore.QRect(0,0,size.width()-10, size.height()-60))




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
