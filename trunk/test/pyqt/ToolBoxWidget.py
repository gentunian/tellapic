from PyQt4 import QtCore, QtGui
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
    StrokeStylePage  = 1
    StrokeColorPage  = 2
    FillColorPage    = 3
    FontPropertyPage = 4
    UsersPage        = 5
    ChatPage         = 6

    def __init__(self, model, parent = None):
        super(ToolBoxWidget, self).__init__(parent)
        self.setupUi(self)
        self.setCurrentIndex(0)
        self.actionCapSquare.toggled.connect(self.actionCapSquare_toggle)
        self.actionCapRound.toggled.connect(self.actionCapSquare_toggle)
        self.actionCapFlat.toggled.connect(self.actionCapSquare_toggle)
        self.actionJoinRound.toggled.connect(self.actionJoinRound_toggle)
        self.actionJoinBevel.toggled.connect(self.actionJoinBevel_toggle)
        self.actionJoinMiter.toggled.connect(self.actionJoinMiter_toggle)
        self.actionShouldFill.toggled.connect(self.actionShouldFill_toggle)
        self.actionShouldStroke.toggled.connect(self.actionShouldStroke_toggle)
        self.actionWidthChange.triggered.connect(self.actionWidthChange_trigger)
        self.actionDashPhaseChange.triggered.connect(self.actionDashPhaseChange_trigger)
        self.actionDashSet.triggered.connect(self.actionDashSet_trigger)
        self.actionChangeCharCounter.triggered.connect(self.actionChangeCharCounter_trigger)
        self.actionFontFamilySet.triggered.connect(self.actionFontFamilySet_trigger)
        self.actionFontSizeSet.triggered.connect(self.actionFontSizeSet_trigger)
        self.actionFontStyleBold.triggered.connect(self.actionFontStyleBold_toggle)
        self.actionFontStyleItalic.triggered.connect(self.actionFontStyleItalic_toggle)
        self.setItemEnabled(self.FontPropertyPage, False)
        self.setItemEnabled(self.StrokeStylePage, False)
        self.setItemEnabled(self.StrokeColorPage, False)
        self.setItemEnabled(self.FillColorPage, False)
        self.setItemEnabled(self.DrawingPropertiesPage, False)
        self.model = model
        self.model.toolChanged.connect(self.update)
        self.currentTool = None
        style = QtGui.QStyleFactory.create("Plastique")
        self.setStyle(style)

    def actionChangeCharCounter_trigger(self):
        doc    = self.textArea.document()
        chars  = doc.characterCount()

        if (chars > 512):
            cursor = self.textArea.textCursor()
            cursor.deletePreviousChar()
            chars = 512

        self.charCounter.display(512 - chars)
        
    def actionCapSquare_toggle(self, toggled = None):
        if (toggled and self.currentTool is not None):
            print("Setting caps to square cap.", toggled)
            self.currentTool.drawing.setEndCaps(pytellapic.END_CAPS_SQUARE)

    def actionCapRound_toggle(self, toggled = None):
        if (toggled and self.currentTool is not None):
            print("Setting caps to round cap.", toggled)
            self.currentTool.drawing.setEndCaps(pytellapic.END_CAPS_ROUND)

    def actionCapFlat_toggle(self, toggled = None):
        if (toggled and self.currentTool is not None):
            print("Setting caps to flat cap.", toggled)
            self.currentTool.drawing.setEndCaps(pytellapic.END_CAPS_BUTT)

    def actionJoinRound_toggle(self, toggled = None):
        if (toggled and self.currentTool is not None):
            print("Setting joins to round join.", toggled)
            self.currentTool.drawing.setLineJoins(pytellapic.LINE_JOINS_ROUND)

    def actionJoinBevel_toggle(self, toggled = None):
        if (toggled and self.currentTool is not None):
            print("Settings joins to bevel join.", toggled)
            self.currentTool.drawing.setLineJoins(pytellapic.LINE_JOINS_BEVEL)

    def actionJoinMiter_toggle(self, toggled = None):
        if (toggled and self.currentTool is not None):
            print("Setting joins to miter join.", toggled)
            self.currentTool.drawing.setLineJoins(pytellapic.LINE_JOINS_MITER)

    def actionShouldFill_toggle(self, toggled = None):
        if (self.currentTool is not None):
            self.currentTool.drawing.setFillEnabled(toggled)

    def actionShouldStroke_toggle(self, toggled = None):
        if (self.currentTool is not None):
            self.currentTool.drawing.setStrokeEnabled(toggled)

    def actionDashPhaseChange_trigger(self):
        print("value: ", self.dashPhaseSpinner.value())

    def actionWidthChange_trigger(self):
        if (self.currentTool is not None):
            print("Setting width to: ", self.widthSpinner.value())
            self.currentTool.drawing.setStrokeWidth(self.widthSpinner.value())

    def actionDashSet_trigger(self):
        print("index: ", self.dashCombo.currentIndex())

    def fillColorChanged(self, color):
        if (self.currentTool is not None):
            self.currentTool.drawing.setFillColor(color)
            values = "{r}, {g}, {b}, {a}".format(r = color.red(), 
                                             g = color.green(), 
                                             b = color.blue(), 
                                             a = color.alpha()
                                             )
            self.fillColorLabel.setStyleSheet("QLabel { background-color : rgba("+values+"); }")
            self.fillColorLabel.setToolTip(color.name()+hex(color.alpha())[2:])

    def strokeColorChanged(self, color):
        if (self.currentTool is not None):
            self.currentTool.drawing.setStrokeColor(color)
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
        self.currentTool = self.model.getToolByName(toolName)
        self.setItemEnabled(self.FontPropertyPage, self.currentTool.drawing.hasFontProperties())
        self.setItemEnabled(self.StrokeStylePage, self.currentTool.drawing.hasStrokeColorProperties())
        self.setItemEnabled(self.StrokeColorPage, self.currentTool.drawing.hasStrokeStylesProperties())
        self.setItemEnabled(self.FillColorPage, self.currentTool.drawing.hasFillColorProperties())
        
        if self.currentTool.drawing.hasFontProperties():
            pass
        if self.currentTool.drawing.hasStrokeColorProperties():
            color = self.currentTool.drawing.pen.color()
            self.strokeColorLabel.setStyleSheet("QLabel { background-color : %s}"%color.name());
        if self.currentTool.drawing.hasStrokeStylesProperties():
            style = self.currentTool.drawing.pen.capStyle()
            if style  == QtCore.Qt.SquareCap:
                self.squareCapButton.setChecked(True)
            elif style == QtCore.Qt.RoundCap:
                self.roundCapButton.setChecked(True)
            else:
                self.flatCapButton.setChecked(True)
            style = self.currentTool.drawing.pen.joinStyle()
            if style == QtCore.Qt.MiterJoin:
                self.miterJoinButton.setChecked(True)
            elif style == QtCore.Qt.RoundJoin:
                self.roundJoinButton.setChecked(True)
            else:
                self.roundBevelButton.setChecked(True)
            self.widthSpinner.setValue(self.currentTool.drawing.pen.width())
            self.shouldStrokeCheckbox.setChecked(self.currentTool.drawing.shouldStroke)

        if self.currentTool.drawing.hasFillColorProperties():
            self.shouldFillCheckbox.setChecked(self.currentTool.drawing.shouldFill)
            color = self.currentTool.drawing.brush.color()
            self.fillColorLabel.setStyleSheet("QLabel { background-color : %s}"%color.name());

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
