# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'ui/chatTabContentWidget.ui'
#
# Created: Tue Nov  8 10:17:02 2011
#      by: PyQt4 UI code generator 4.8.6
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_ChatTabContentWidget(object):
    def setupUi(self, ChatTabContentWidget):
        ChatTabContentWidget.setObjectName(_fromUtf8("ChatTabContentWidget"))
        ChatTabContentWidget.resize(440, 331)
        ChatTabContentWidget.setWindowTitle(QtGui.QApplication.translate("ChatTabContentWidget", "Form", None, QtGui.QApplication.UnicodeUTF8))
        self.verticalLayout = QtGui.QVBoxLayout(ChatTabContentWidget)
        self.verticalLayout.setSpacing(2)
        self.verticalLayout.setMargin(0)
        self.verticalLayout.setObjectName(_fromUtf8("verticalLayout"))
        self.horizontalLayout = QtGui.QHBoxLayout()
        self.horizontalLayout.setSpacing(3)
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        self.textField = QtGui.QLineEdit(ChatTabContentWidget)
        self.textField.setObjectName(_fromUtf8("textField"))
        self.horizontalLayout.addWidget(self.textField)
        self.smileyButton = QtGui.QToolButton(ChatTabContentWidget)
        self.smileyButton.setMaximumSize(QtCore.QSize(100, 24))
        self.smileyButton.setContextMenuPolicy(QtCore.Qt.ActionsContextMenu)
        self.smileyButton.setText(_fromUtf8(""))
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.smileyButton.setIcon(icon)
        self.smileyButton.setCheckable(False)
        self.smileyButton.setPopupMode(QtGui.QToolButton.MenuButtonPopup)
        self.smileyButton.setToolButtonStyle(QtCore.Qt.ToolButtonIconOnly)
        self.smileyButton.setArrowType(QtCore.Qt.NoArrow)
        self.smileyButton.setObjectName(_fromUtf8("smileyButton"))
        self.horizontalLayout.addWidget(self.smileyButton)
        self.verticalLayout.addLayout(self.horizontalLayout)
        self.textArea = QtGui.QTextEdit(ChatTabContentWidget)
        self.textArea.setEnabled(True)
        self.textArea.setUndoRedoEnabled(False)
        self.textArea.setReadOnly(True)
        self.textArea.setObjectName(_fromUtf8("textArea"))
        self.verticalLayout.addWidget(self.textArea)

        self.retranslateUi(ChatTabContentWidget)
        QtCore.QMetaObject.connectSlotsByName(ChatTabContentWidget)

    def retranslateUi(self, ChatTabContentWidget):
        pass

import rsrc_rc

class ChatTabContentWidget(QtGui.QWidget, Ui_ChatTabContentWidget):
    def __init__(self, parent=None, f=QtCore.Qt.WindowFlags()):
        QtGui.QWidget.__init__(self, parent, f)

        self.setupUi(self)

