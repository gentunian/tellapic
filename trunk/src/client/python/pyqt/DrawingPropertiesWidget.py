'''
Created on Oct 17, 2011

@author: Sebastián Treu
'''
from PyQt4.QtGui import QWidget
from PyQt4.QtCore import pyqtSignal
from PyQt4.QtCore import pyqtSlot
from PyQt4.QtCore import QObject

from DrawingPropertiesWidgetUi import Ui_DrawingPropertiesWidget

class DrawingPropertiesWidget(QWidget, Ui_DrawingPropertiesWidget):


    def __init__(self, parent = None):
        super(DrawingPropertiesWidget, self).__init__(parent)
        self.setupUi(self)

    @pyqtSlot(QObject)
    def updateDrawingData(self, drawing):
        pass