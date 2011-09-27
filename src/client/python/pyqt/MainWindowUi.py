# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'newMainWidget.ui'
#
# Created: Mon Jul 25 13:04:30 2011
#      by: PyQt4 UI code generator 4.8.4
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_MainWindow(object):
    def setupUi(self, MainWindow):
        MainWindow.setObjectName(_fromUtf8("MainWindow"))
        MainWindow.resize(734, 576)
        MainWindow.setTabShape(QtGui.QTabWidget.Triangular)
        MainWindow.setDockOptions(QtGui.QMainWindow.AllowTabbedDocks|QtGui.QMainWindow.AnimatedDocks|QtGui.QMainWindow.VerticalTabs)
        self.centralwidget = QtGui.QWidget(MainWindow)
        self.centralwidget.setObjectName(_fromUtf8("centralwidget"))
        self.horizontalLayout = QtGui.QHBoxLayout(self.centralwidget)
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        self.graphicsView = QtGui.QGraphicsView(self.centralwidget)
        self.graphicsView.setObjectName(_fromUtf8("graphicsView"))
        self.horizontalLayout.addWidget(self.graphicsView)
        MainWindow.setCentralWidget(self.centralwidget)
        self.menubar = QtGui.QMenuBar(MainWindow)
        self.menubar.setGeometry(QtCore.QRect(0, 0, 734, 29))
        self.menubar.setObjectName(_fromUtf8("menubar"))
        MainWindow.setMenuBar(self.menubar)
        self.statusbar = QtGui.QStatusBar(MainWindow)
        self.statusbar.setObjectName(_fromUtf8("statusbar"))
        MainWindow.setStatusBar(self.statusbar)
        self.toolBar = QtGui.QToolBar(MainWindow)
        self.toolBar.setAutoFillBackground(True)
        self.toolBar.setAllowedAreas(QtCore.Qt.AllToolBarAreas)
        self.toolBar.setObjectName(_fromUtf8("toolBar"))
        MainWindow.addToolBar(QtCore.Qt.LeftToolBarArea, self.toolBar)
        self.actionSelector = QtGui.QAction(MainWindow)
        self.actionSelector.setCheckable(True)
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/tool-icons/selector.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionSelector.setIcon(icon)
        self.actionSelector.setObjectName(_fromUtf8("actionSelector"))
        self.actionEllipse = QtGui.QAction(MainWindow)
        self.actionEllipse.setCheckable(True)
        icon1 = QtGui.QIcon()
        icon1.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/tool-icons/ellipse.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionEllipse.setIcon(icon1)
        self.actionEllipse.setObjectName(_fromUtf8("actionEllipse"))
        self.actionRectangle = QtGui.QAction(MainWindow)
        self.actionRectangle.setCheckable(True)
        icon2 = QtGui.QIcon()
        icon2.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/tool-icons/rectangle.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionRectangle.setIcon(icon2)
        self.actionRectangle.setObjectName(_fromUtf8("actionRectangle"))
        self.actionLine = QtGui.QAction(MainWindow)
        self.actionLine.setCheckable(True)
        icon3 = QtGui.QIcon()
        icon3.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/tool-icons/line.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionLine.setIcon(icon3)
        self.actionLine.setObjectName(_fromUtf8("actionLine"))
        self.actionText = QtGui.QAction(MainWindow)
        self.actionText.setCheckable(True)
        icon4 = QtGui.QIcon()
        icon4.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/tool-icons/text.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionText.setIcon(icon4)
        self.actionText.setObjectName(_fromUtf8("actionText"))
        self.actionZoom = QtGui.QAction(MainWindow)
        self.actionZoom.setCheckable(True)
        icon5 = QtGui.QIcon()
        icon5.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/tool-icons/zoom.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionZoom.setIcon(icon5)
        self.actionZoom.setObjectName(_fromUtf8("actionZoom"))
        self.actionMarker = QtGui.QAction(MainWindow)
        self.actionMarker.setCheckable(True)
        icon6 = QtGui.QIcon()
        icon6.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/tool-icons/marker.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionMarker.setIcon(icon6)
        self.actionMarker.setObjectName(_fromUtf8("actionMarker"))
        self.actionPencil = QtGui.QAction(MainWindow)
        self.actionPencil.setCheckable(True)
        icon7 = QtGui.QIcon()
        icon7.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/tool-icons/pencil.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionPencil.setIcon(icon7)
        self.actionPencil.setObjectName(_fromUtf8("actionPencil"))
        self.toolBar.addAction(self.actionSelector)
        self.toolBar.addAction(self.actionEllipse)
        self.toolBar.addAction(self.actionRectangle)
        self.toolBar.addAction(self.actionLine)
        self.toolBar.addAction(self.actionText)
        self.toolBar.addAction(self.actionZoom)
        self.toolBar.addAction(self.actionMarker)
        self.toolBar.addAction(self.actionPencil)

        self.retranslateUi(MainWindow)
        QtCore.QMetaObject.connectSlotsByName(MainWindow)

    def retranslateUi(self, MainWindow):
        MainWindow.setWindowTitle(QtGui.QApplication.translate("MainWindow", "MainWindow", None, QtGui.QApplication.UnicodeUTF8))
        self.toolBar.setWindowTitle(QtGui.QApplication.translate("MainWindow", "toolBar", None, QtGui.QApplication.UnicodeUTF8))
        self.actionSelector.setText(QtGui.QApplication.translate("MainWindow", "selector", None, QtGui.QApplication.UnicodeUTF8))
        self.actionSelector.setShortcut(QtGui.QApplication.translate("MainWindow", "Ctrl+1", None, QtGui.QApplication.UnicodeUTF8))
        self.actionEllipse.setText(QtGui.QApplication.translate("MainWindow", "ellipse", None, QtGui.QApplication.UnicodeUTF8))
        self.actionEllipse.setShortcut(QtGui.QApplication.translate("MainWindow", "Ctrl+2", None, QtGui.QApplication.UnicodeUTF8))
        self.actionRectangle.setText(QtGui.QApplication.translate("MainWindow", "rectangle", None, QtGui.QApplication.UnicodeUTF8))
        self.actionRectangle.setShortcut(QtGui.QApplication.translate("MainWindow", "Ctrl+3", None, QtGui.QApplication.UnicodeUTF8))
        self.actionLine.setText(QtGui.QApplication.translate("MainWindow", "line", None, QtGui.QApplication.UnicodeUTF8))
        self.actionLine.setShortcut(QtGui.QApplication.translate("MainWindow", "Ctrl+4", None, QtGui.QApplication.UnicodeUTF8))
        self.actionText.setText(QtGui.QApplication.translate("MainWindow", "text", None, QtGui.QApplication.UnicodeUTF8))
        self.actionText.setShortcut(QtGui.QApplication.translate("MainWindow", "Ctrl+5", None, QtGui.QApplication.UnicodeUTF8))
        self.actionZoom.setText(QtGui.QApplication.translate("MainWindow", "zoom", None, QtGui.QApplication.UnicodeUTF8))
        self.actionZoom.setShortcut(QtGui.QApplication.translate("MainWindow", "Ctrl+6", None, QtGui.QApplication.UnicodeUTF8))
        self.actionMarker.setText(QtGui.QApplication.translate("MainWindow", "marker", None, QtGui.QApplication.UnicodeUTF8))
        self.actionMarker.setShortcut(QtGui.QApplication.translate("MainWindow", "Ctrl+7, Return", None, QtGui.QApplication.UnicodeUTF8))
        self.actionPencil.setText(QtGui.QApplication.translate("MainWindow", "pencil", None, QtGui.QApplication.UnicodeUTF8))
        self.actionPencil.setShortcut(QtGui.QApplication.translate("MainWindow", "Ctrl+8", None, QtGui.QApplication.UnicodeUTF8))

import rsrc_rc

if __name__ == "__main__":
    import sys
    app = QtGui.QApplication(sys.argv)
    MainWindow = QtGui.QMainWindow()
    ui = Ui_MainWindow()
    ui.setupUi(MainWindow)
    MainWindow.show()
    sys.exit(app.exec_())

