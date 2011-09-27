# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'chatUi.ui'
#
# Created: Mon Jul 25 23:37:51 2011
#      by: PyQt4 UI code generator 4.8.4
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_TabWidget(object):
    def setupUi(self, TabWidget):
        TabWidget.setObjectName(_fromUtf8("TabWidget"))
        TabWidget.resize(440, 356)
        TabWidget.setTabPosition(QtGui.QTabWidget.South)
        TabWidget.setTabShape(QtGui.QTabWidget.Rounded)
        self.mainTab = QtGui.QWidget()
        self.mainTab.setObjectName(_fromUtf8("mainTab"))
        self.verticalLayout = QtGui.QVBoxLayout(self.mainTab)
        self.verticalLayout.setObjectName(_fromUtf8("verticalLayout"))
        self.textArea = QtGui.QTextEdit(self.mainTab)
        self.textArea.setObjectName(_fromUtf8("textArea"))
        self.verticalLayout.addWidget(self.textArea)
        self.horizontalLayout = QtGui.QHBoxLayout()
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        self.textField = QtGui.QLineEdit(self.mainTab)
        self.textField.setObjectName(_fromUtf8("textField"))
        self.horizontalLayout.addWidget(self.textField)
        self.smileyButton = QtGui.QPushButton(self.mainTab)
        self.smileyButton.setMaximumSize(QtCore.QSize(24, 24))
        self.smileyButton.setContextMenuPolicy(QtCore.Qt.CustomContextMenu)
        self.smileyButton.setText(_fromUtf8(""))
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smiley-button.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.smileyButton.setIcon(icon)
        self.smileyButton.setCheckable(True)
        self.smileyButton.setObjectName(_fromUtf8("smileyButton"))
        self.horizontalLayout.addWidget(self.smileyButton)
        self.verticalLayout.addLayout(self.horizontalLayout)
        TabWidget.addTab(self.mainTab, _fromUtf8(""))
        self.actionEnterText = QtGui.QAction(TabWidget)
        self.actionEnterText.setObjectName(_fromUtf8("actionEnterText"))
        self.actionShowSmileyPopup = QtGui.QAction(TabWidget)
        self.actionShowSmileyPopup.setCheckable(True)
        self.actionShowSmileyPopup.setObjectName(_fromUtf8("actionShowSmileyPopup"))

        self.retranslateUi(TabWidget)
        TabWidget.setCurrentIndex(0)
        QtCore.QObject.connect(self.textField, QtCore.SIGNAL(_fromUtf8("returnPressed()")), self.actionEnterText.trigger)
        QtCore.QObject.connect(self.smileyButton, QtCore.SIGNAL(_fromUtf8("pressed()")), self.actionShowSmileyPopup.toggle)
        QtCore.QMetaObject.connectSlotsByName(TabWidget)

    def retranslateUi(self, TabWidget):
        TabWidget.setWindowTitle(QtGui.QApplication.translate("TabWidget", "TabWidget", None, QtGui.QApplication.UnicodeUTF8))
        TabWidget.setTabText(TabWidget.indexOf(self.mainTab), QtGui.QApplication.translate("TabWidget", "Main", None, QtGui.QApplication.UnicodeUTF8))
        self.actionEnterText.setText(QtGui.QApplication.translate("TabWidget", "enterText", None, QtGui.QApplication.UnicodeUTF8))
        self.actionShowSmileyPopup.setText(QtGui.QApplication.translate("TabWidget", "showSmileyPopup", None, QtGui.QApplication.UnicodeUTF8))

import rsrc_rc

if __name__ == "__main__":
    import sys
    app = QtGui.QApplication(sys.argv)
    TabWidget = QtGui.QTabWidget()
    ui = Ui_TabWidget()
    ui.setupUi(TabWidget)
    TabWidget.show()
    sys.exit(app.exec_())

