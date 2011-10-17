'''
Created on Oct 16, 2011

@author: Sebastián Treu
'''
from PyQt4.QtGui import QWidget
from PyQt4.QtGui import QButtonGroup
from PyQt4.QtGui import QFont
from PyQt4.QtCore import pyqtSignal
from PyQt4.QtCore import pyqtSlot
from PyQt4.QtCore import Qt

from FontWidgetUi import  Ui_FontWidget

try:
    from PyQt4.QtCore import QString
except ImportError:
    # we are using Python3 so QString is not defined
    QString = type("")

class FontWidget(QWidget, Ui_FontWidget):
    fontChanged       = pyqtSignal(QFont)
    textChanged       = pyqtSignal(QString)

    BoldStyle   = 0
    ItalicStyle = 1

    def __init__(self, parent = None):
        super(FontWidget, self).__init__(parent)
        self.setupUi(self)
        self.connectSignalsAndSlots()

    def connectSignalsAndSlots(self):
        self.fontStyleBoldButton.toggled.connect(self.fontStyleBoldButton_toggled)
        self.fontStyleItalicButton.toggled.connect(self.fontStyleItalicButton_toggled)
        self.fontSizeSpinner.valueChanged.connect(self.fontSizeSpinner_changed)
        self.fontComboBox.currentFontChanged.connect(self.fontChanged)
        self.fontChanged.connect(self.setTextAreaFont)
        self.fontTextArea.textChanged.connect(self.updateCharCounter)

    def disconnectSignalsAndSlots(self):
        self.fontStyleBoldButton.toggled.disconnect(self.fontStyleBoldButton_toggled)
        self.fontStyleItalicButton.toggled.disconnect(self.fontStyleItalicButton_toggled)
        self.fontSizeSpinner.valueChanged.disconnect(self.fontSizeSpinner_changed)
        self.fontComboBox.currentFontChanged.disconnect(self.fontChanged)
        self.fontChanged.disconnect(self.setTextAreaFont)
        self.fontTextArea.textChanged.disconnect(self.updateCharCounter)

    def updateCharCounter(self):
        doc    = self.fontTextArea.document()
        chars  = doc.characterCount()
        if (chars > 512):
            cursor = self.fontTextArea.textCursor()
            cursor.deletePreviousChar()
            chars = 512
        self.charCounter.display(512 - chars)

    def setTextAreaFont(self, font):
        self.fontTextArea.setFont(font)

    def fontStyleBoldButton_toggled(self, toggled):
        print("Font boldness set to:", toggled)
        self.fontStyleBoldButton.setToolTip("Bold is %s" % toggled)
        font = self.fontComboBox.currentFont()
        font.setBold(toggled)
        self.fontComboBox.setCurrentFont(font)

    def fontStyleItalicButton_toggled(self, toggled):
        print("Font italics set to:", toggled)
        self.fontStyleItalicButton.setToolTip("Italic is %s" % toggled)
        font = self.fontComboBox.currentFont()
        font.setItalic(toggled)
        self.fontComboBox.setCurrentFont(font)

    def fontSizeSpinner_changed(self):
        print("Font size changed to:", self.fontSizeSpinner.value())
        font = self.fontComboBox.currentFont()
        font.setPointSizeF(self.fontSizeSpinner.value())
        self.fontComboBox.setCurrentFont(font)

    @pyqtSlot()
    def setFontSize(self, size):
        self.fontSizeSpinner.setValue(size)

    @pyqtSlot()
    def setFontStyle(self, style):
        pass

    @pyqtSlot()
    def setFontFamily(self, font):
        self.fontComboBox.setCurrentFont(font)
        self.fontStyleBoldButton.toggled.emit(font.bold())
        self.fontStyleItalicButton.toggled.emit(font.italic())
        self.fontSizeSpinner.valueChanged.emit(font.pointSizeF())

