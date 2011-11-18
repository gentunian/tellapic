# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'drawingPropertiesWidget.ui'
#
# Created: Mon Oct 17 16:33:57 2011
#      by: PyQt4 UI code generator 4.8.5
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_DrawingPropertiesWidget(object):
    def setupUi(self, DrawingPropertiesWidget):
        DrawingPropertiesWidget.setObjectName(_fromUtf8("DrawingPropertiesWidget"))
        DrawingPropertiesWidget.resize(400, 300)
        DrawingPropertiesWidget.setWindowTitle(QtGui.QApplication.translate("DrawingPropertiesWidget", "Form", None, QtGui.QApplication.UnicodeUTF8))
        self.verticalLayout = QtGui.QVBoxLayout(DrawingPropertiesWidget)
        self.verticalLayout.setMargin(1)
        self.verticalLayout.setObjectName(_fromUtf8("verticalLayout"))
        self.tableWidget = QtGui.QTableWidget(DrawingPropertiesWidget)
        self.tableWidget.setAlternatingRowColors(True)
        self.tableWidget.setObjectName(_fromUtf8("tableWidget"))
        self.tableWidget.setColumnCount(2)
        self.tableWidget.setRowCount(3)
        item = QtGui.QTableWidgetItem()
        item.setText(QtGui.QApplication.translate("DrawingPropertiesWidget", "New Row", None, QtGui.QApplication.UnicodeUTF8))
        self.tableWidget.setVerticalHeaderItem(0, item)
        item = QtGui.QTableWidgetItem()
        item.setText(QtGui.QApplication.translate("DrawingPropertiesWidget", "New Row", None, QtGui.QApplication.UnicodeUTF8))
        self.tableWidget.setVerticalHeaderItem(1, item)
        item = QtGui.QTableWidgetItem()
        item.setText(QtGui.QApplication.translate("DrawingPropertiesWidget", "New Row", None, QtGui.QApplication.UnicodeUTF8))
        self.tableWidget.setVerticalHeaderItem(2, item)
        item = QtGui.QTableWidgetItem()
        item.setText(QtGui.QApplication.translate("DrawingPropertiesWidget", "Property", None, QtGui.QApplication.UnicodeUTF8))
        self.tableWidget.setHorizontalHeaderItem(0, item)
        item = QtGui.QTableWidgetItem()
        item.setText(QtGui.QApplication.translate("DrawingPropertiesWidget", "Value", None, QtGui.QApplication.UnicodeUTF8))
        self.tableWidget.setHorizontalHeaderItem(1, item)
        self.tableWidget.verticalHeader().setVisible(False)
        self.verticalLayout.addWidget(self.tableWidget)

        self.retranslateUi(DrawingPropertiesWidget)
        QtCore.QMetaObject.connectSlotsByName(DrawingPropertiesWidget)

    def retranslateUi(self, DrawingPropertiesWidget):
        self.tableWidget.setSortingEnabled(True)
        item = self.tableWidget.verticalHeaderItem(0)
        item = self.tableWidget.verticalHeaderItem(1)
        item = self.tableWidget.verticalHeaderItem(2)
        item = self.tableWidget.horizontalHeaderItem(0)
        item = self.tableWidget.horizontalHeaderItem(1)

