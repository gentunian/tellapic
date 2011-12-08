'''
Created on Oct 16, 2011

@author: sebastian.treu@gmail.com
'''
from PyQt4.QtGui import QWidget
from PyQt4.QtGui import QButtonGroup
from PyQt4.QtCore import pyqtSignal
from PyQt4.QtCore import pyqtSlot
from PyQt4.QtCore import Qt

from StrokeStyleWidgetUi import  Ui_StrokeStyleWidget

class StrokeStyleWidget(QWidget, Ui_StrokeStyleWidget):
    strokeWidthChanged = pyqtSignal(float)
    capStyleChanged    = pyqtSignal(int)
    joinStyleChanged   = pyqtSignal(int)
    dashStyleChanged   = pyqtSignal(float, float)
    miterLimitChanged  = pyqtSignal(float)

    def __init__(self, parent = None):
        super(StrokeStyleWidget, self).__init__(parent)
        self.setupUi(self)
        capGroup = QButtonGroup(self)
        capGroup.addButton(self.squareCapButton, Qt.SquareCap)
        capGroup.addButton(self.roundCapButton, Qt.RoundCap)
        capGroup.addButton(self.flatCapButton, Qt.FlatCap)
        capGroup.setExclusive(True)
        capGroup.buttonClicked[int].connect(self.capStyleChanged)
        joinGroup = QButtonGroup(self)
        joinGroup.addButton(self.roundJoinButton, Qt.RoundJoin)
        joinGroup.addButton(self.miterJoinButton, Qt.MiterJoin)
        joinGroup.addButton(self.bevelJoinButton, Qt.BevelJoin)
        joinGroup.setExclusive(True)
        joinGroup.buttonClicked[int].connect(self.joinStyleChanged)
        self.widthSpinner.valueChanged.connect(self.strokeWidthChanged)

    @pyqtSlot()
    def setStrokeWidht(self, width):
        self.widthSpinner.setValue(width)

    @pyqtSlot()
    def setCapStyle(self, cap):
        pass

    @pyqtSlot()
    def setJoinStyle(self, join):
        pass

    @pyqtSlot()
    def setDashStyle(self, dash, phase):
        pass

    @pyqtSlot()
    def setMiterLimit(self, miterlimit):
        pass

