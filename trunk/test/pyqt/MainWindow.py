from PyQt4 import QtCore, QtGui
from MainWindowUi import Ui_MainWindow
from ToolBoxWidget import *
from ChatWidget import *
import Drawing

class MainWindow(QtGui.QMainWindow, Ui_MainWindow):
    def __init__(self, model, parent = None):
        super(MainWindow, self).__init__(parent)
        self.setupUi(self)
        self.setActionGroup()
        self.model = model
        self.scene = Drawing.TellapicScene(model, self)
        self.graphicsView.setScene(self.scene)

    def setActionGroup(self):
        aGroup = QtGui.QActionGroup(self)
        aGroup.addAction(self.actionSelector)
        aGroup.addAction(self.actionEllipse)
        aGroup.addAction(self.actionRectangle)
        aGroup.addAction(self.actionLine)
        aGroup.addAction(self.actionText)
        aGroup.addAction(self.actionZoom)
        aGroup.addAction(self.actionMarker)
        aGroup.addAction(self.actionPencil)
        aGroup.triggered.connect(self.selectTool_trigger)

    def selectTool_trigger(self, action):
        toolName = action.text()
        self.model.setTool(toolName)
        if (toolName == "selector"):
            pass
        elif (toolName == "ellipse"):
            pass
        elif (toolName == Drawing.DrawingToolRectangle):
            pass
        elif (toolName == "line"):
            pass
        elif (toolName == "text"):
            pass
        elif (toolName == "zoom"):
            pass
        elif (toolName == "marker"):
            pass
        elif (toolName == "pencil"):
            pass
        
    def show(self):
        super(MainWindow, self).show()



if __name__ == "__main__":
    import sys
    app = QtGui.QApplication(sys.argv)
    model = Drawing.ToolModel()
    main = MainWindow(model)
    dock = QtGui.QDockWidget("Properties", main)
    box = ToolBoxWidget(model)
    dock.setWidget(box)
    main.addDockWidget(QtCore.Qt.RightDockWidgetArea, dock)
    main.show()
    sys.exit(app.exec_())
