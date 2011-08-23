from PyQt4 import QtCore, QtGui
from MainWindowUi import Ui_MainWindow
from ToolBoxWidget import *
from ChatWidget import *
import Drawing
import NetManager
import Utils

class MainWindow(QtGui.QMainWindow, Ui_MainWindow):
    def __init__(self, model, scene, toolBox, parent = None):
        super(MainWindow, self).__init__(parent)
        self.setupUi(self)
        self.setActionGroup()
        self.model = model
        self.scene = scene
        self.graphicsView.setScene(self.scene)
        self.graphicsView.setViewportUpdateMode(QtGui.QGraphicsView.FullViewportUpdate)
        dock = QtGui.QDockWidget("Properties", self)
        #box = ToolBoxWidget(model)
        dock.setWidget(toolBox)
        self.addDockWidget(QtCore.Qt.RightDockWidgetArea, dock)
        self.scene.drawingSelectionChanged.connect(toolBox.update)


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
        elif (toolName == Drawing.ToolRectangle):
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
        
if __name__ == "__main__":
    import sys
    import argparse

    parser = argparse.ArgumentParser(description='Connects to a tellapic server')
    parser.add_argument('-c', '--host', required=True, nargs=1, type=str, help='the host name to connect to.')
    parser.add_argument('-p', '--port', required=True, nargs=1, type=int, help='the HOST port to use.')
    parser.add_argument('-u', '--user', required=True, nargs=1, type=str, help='the USERNAME to use.')
    parser.add_argument('-P', '--password', required=True, nargs=1, type=str, help='the server PASSWORD.')
    args = parser.parse_args()

    app = QtGui.QApplication(sys.argv)
    model = Drawing.ToolBoxModel()
    tellapicScene = Drawing.TellapicScene(model)
    userManager = Utils.TellapicUserManager(Utils.TellapicLocalUser(Utils.NoUserId, args.user[0]))
    toolBox = ToolBoxWidget(model, userManager)
    dispatcher = Utils.TellapicEventDispatcher(tellapicScene, userManager)
    thread = NetManager.NetManager(dispatcher, args.host[0], str(args.port[0]), args.user[0], args.password[0], model)
    thread.begin()

    main = MainWindow(model, tellapicScene, toolBox)
    main.show()
    sys.exit(app.exec_())
