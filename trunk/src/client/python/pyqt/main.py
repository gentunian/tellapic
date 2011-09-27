'''
Created on Jul 18, 2011

@author: seba
'''
from PyQt4 import *
from PyQt4.QtGui import *
from PyQt4.QtCore import *
from mainWidget import *
from connectDialog import *
import sys

class Painting(QtGui.QWidget):

    def paintEvent(self, ev):
        self.p = QPainter()
        self.p.begin(self)
        self.p.setBackgroundColor(QtGui.QColor("white"))
        self.p.end()


class Main(QtGui.QMainWindow):
    
    def __init__(self):
        QtGui.QMainWindow.__init__(self)
        self.painting=Painting(self)
        self.setCentralWidget(self.painting)
        #self.ui = Ui_MainWindow()
        #self.ui.setupUi(self)

    def on_actionConnect_triggered(self,checked=None):
        if checked is None: return
        Dialog = QtGui.QDialog()
        ui = Ui_Dialog()
        ui.setupUi(Dialog)
        Dialog.exec_()

if __name__ == "__main__":
    app = QtGui.QApplication(sys.argv)
    main = Main()
    main.show()  
    sys.exit(app.exec_())
