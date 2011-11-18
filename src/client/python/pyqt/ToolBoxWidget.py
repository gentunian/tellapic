'''

@author: sebastian.treu@gmail.com
'''
from PyQt4.QtGui import QDoubleSpinBox

from ToolBoxUi import *

import Drawing
import pytellapic

try:
    from PyQt4.QtCore import QString
except ImportError:
    # we are using Python3 so QString is not defined
    QString = type("")

class ToolBoxWidget(QtGui.QToolBox, Ui_ToolBox):
    DrawingPropertiesPage = 0
    StrokeStylePage       = 1
    StrokeColorPage       = 2
    FillColorPage         = 3
    FontPropertyPage      = 4
    UsersPage             = 5
    ChatPage              = 6

    def __init__(self, model, uman, parent = None):
        super(ToolBoxWidget, self).__init__(parent)
        self.setupUi(self)
        self.setCurrentIndex(0)
        self.strokeStyleWidget.strokeWidthChanged.connect(self.actionWidthChange_trigger)
        self.strokeStyleWidget.capStyleChanged.connect(self.actionCapStyleChange_trigger)
        self.strokeStyleWidget.joinStyleChanged.connect(self.actionJoinStyleChange_trigger)
        
        self.actionDashSet.triggered.connect(self.actionDashSet_trigger)
        self.actionChangeCharCounter.triggered.connect(self.actionChangeCharCounter_trigger)
        self.actionFontFamilySet.triggered.connect(self.actionFontFamilySet_trigger)
        self.actionFontSizeSet.triggered.connect(self.actionFontSizeSet_trigger)
        self.actionFontStyleBold.triggered.connect(self.actionFontStyleBold_toggle)
        self.actionFontStyleItalic.triggered.connect(self.actionFontStyleItalic_toggle)
        #self.setItemEnabled(self.FontPropertyPage, False)
        self.setItemEnabled(self.FontPropertyPage, True)
        self.setItemEnabled(self.StrokeStylePage, False)
        self.setItemEnabled(self.StrokeColorPage, False)
        self.setItemEnabled(self.FillColorPage, False)
        #self.setItemEnabled(self.DrawingPropertiesPage, False)
        self.setItemEnabled(self.DrawingPropertiesPage, True)
        self.model = model
        self.model.toolChanged.connect(self.update)
        self.currentTool = None
        style = QtGui.QStyleFactory.create("Plastique")
        self.setStyle(style)
        self.treeView.setModel(uman.getModel())
        self.treeView.setColumnWidth(0, 170)
        self.treeView.setColumnWidth(1, 30)
        self.treeView.setColumnWidth(2, 30)
        self.treeView.setColumnWidth(3, 30)

    def actionChangeCharCounter_trigger(self):
        doc    = self.textArea.document()
        chars  = doc.characterCount()

        if (chars > 512):
            cursor = self.textArea.textCursor()
            cursor.deletePreviousChar()
            chars = 512

        self.charCounter.display(512 - chars)

    def actionJoinStyleChange_trigger(self, joins):
        print("Setting joins to :", joins)
        self.model.setLineJoins(joins)

    def actionCapStyleChange_trigger(self, caps):
        print("Setting caps to :", caps)
        self.model.setEndCaps(caps)

    """
    def actionCapSquare_toggle(self, toggled = None):
        if toggled:
            print("Setting caps to square cap.", toggled)
            self.model.setEndCaps(QtCore.Qt.SquareCap)

    def actionCapRound_toggle(self, toggled = None):
        if toggled:
            print("Setting caps to round cap.", toggled)
            self.model.setEndCaps(QtCore.Qt.RoundCap)

    def actionCapFlat_toggle(self, toggled = None):
        if toggled:
            print("Setting caps to flat cap.", toggled)
            self.model.setEndCaps(QtCore.Qt.FlatCap)

    def actionJoinRound_toggle(self, toggled = None):
        if toggled:
            print("Setting joins to round join.", toggled)
            self.model.setLineJoins(QtCore.Qt.RoundJoin)

    def actionJoinBevel_toggle(self, toggled = None):
        if toggled:
            print("Settings joins to bevel join.", toggled)
            self.model.setLineJoins(QtCore.Qt.BevelJoin)

    def actionJoinMiter_toggle(self, toggled = None):
        if toggled:
            print("Setting joins to miter join.", toggled)
            self.model.setLineJoins(QtCore.Qt.MiterJoin)
    """
    
    def actionShouldFill_toggle(self, toggled = None):
        self.model.setFillEnabled(toggled)

    def actionShouldStroke_toggle(self, toggled = None):
        self.model.setStrokeEnabled(toggled)

    def actionDashPhaseChange_trigger(self):
        print("value: ", self.dashPhaseSpinner.value())

    def actionWidthChange_trigger(self, value):
        print("Setting width to: ", value)
        self.model.setStrokeWidth(value)

    def actionDashSet_trigger(self):
        print("index: ", self.dashCombo.currentIndex())

    def fillColorChanged(self, color):
        self.model.setFillColor(color)
        values = "{r}, {g}, {b}, {a}".format(r = color.red(), 
                                             g = color.green(), 
                                             b = color.blue(), 
                                             a = color.alpha()
                                             )
        self.fillColorLabel.setStyleSheet("QLabel { background-color : rgba("+values+"); }")
        self.fillColorLabel.setToolTip(color.name()+hex(color.alpha())[2:])

    def strokeColorChanged(self, color):
        self.model.setStrokeColor(color)
        values = "{r}, {g}, {b}, {a}".format(r = color.red(), 
                                             g = color.green(), 
                                             b = color.blue(), 
                                             a = color.alpha()
                                             )
        self.strokeColorLabel.setStyleSheet("QLabel { background-color : rgba("+values+"); }")
        self.strokeColorLabel.setToolTip(color.name()+hex(color.alpha())[2:])

    def actionFontFamilySet_trigger(self):
        pass

    def actionFontSizeSet_trigger(self):
        pass

    def actionFontStyleItalic_toggle(self, toggled = None):
        pass

    def actionFontStyleBold_toggle(self, toggled = None):
        pass

    def update(self, toolName):
        #self.currentTool = self.model.getToolByName(toolName)
        self.setItemEnabled(self.FontPropertyPage, self.model.isFontPropertyEnabled())
        self.setItemEnabled(self.StrokeStylePage, self.model.isStrokePropertyEnabled())
        self.setItemEnabled(self.StrokeColorPage, self.model.isStrokePropertyEnabled())
        self.setItemEnabled(self.FillColorPage, self.model.isFillPropertyEnabled())
        self.shouldFillCheckbox.setChecked(self.model.shouldFill)
        self.shouldStrokeCheckbox.setChecked(self.model.shouldStroke)
        """
        if self.currentTool.hasFontProperties():
            pass
        if self.currentTool.hasStrokeColorProperties():
            self.shouldStrokeCheckbox.setChecked(self.currentTool.shouldStroke)
            color = self.currentTool.pen.color()
            self.strokeColorChanged(color)
        if self.currentTool.hasFillColorProperties():
            self.shouldFillCheckbox.setChecked(self.currentTool.shouldFill)
            color = self.currentTool.brush.color()
            self.fillColorChanged(color)
        if self.currentTool.hasStrokeStylesProperties():
            style = self.currentTool.pen.capStyle()
            if style  == QtCore.Qt.SquareCap:
                self.squareCapButton.setChecked(True)
            elif style == QtCore.Qt.RoundCap:
                self.roundCapButton.setChecked(True)
            else:
                self.flatCapButton.setChecked(True)
            style = self.currentTool.pen.joinStyle()
            if style == QtCore.Qt.MiterJoin:
                self.miterJoinButton.setChecked(True)
            elif style == QtCore.Qt.RoundJoin:
                self.roundJoinButton.setChecked(True)
            else:
                self.roundBevelButton.setChecked(True)
            self.widthSpinner.setValue(self.currentTool.pen.width())
            self.shouldStrokeCheckbox.setChecked(self.currentTool.shouldStroke)
            """

    def keyReleaseEvent(self, event):
        """    
        canMove = True if self.currentIndex() > 0 or self.currentIndex() < self.count() else False
        if (event.modifiers & QtCore.Qt.ControlModifier) == QtCore.Qt.ControlModifier:
            if event.keys() == QtCore.Qt.Key_PageUp:
                if canMove:
                    self.setCurrentIndex(self.currentIndex()+1)
            elif event.keys() == QtCore.Qt.Key_PageUp:
                if canMove:
                    self.setCurrentIndex(self.currentIndex()+1)
        """
        pass
    
    @QtCore.pyqtSlot(QString)
    def updateToolInfo(self, toolName):
        print("update tool info", toolName)
        tool = self.model.getToolByName(toolName)
        if tool is not None:
            self.setItemEnabled(self.FontPropertyPage, tool.hasFontProperties())
        


if __name__ == "__main__":
    import sys
    app = QtGui.QApplication(sys.argv)
    toolBox = ToolBoxWidget()
    toolBox.show()
    sys.exit(app.exec_())
