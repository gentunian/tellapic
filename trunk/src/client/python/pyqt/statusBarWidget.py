# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'statusBar.ui'
#
# Created: Thu Sep 15 03:21:16 2011
#      by: PyQt4 UI code generator 4.8.5
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_ToolStatusWidget(object):
    def setupUi(self, ToolStatusWidget):
        ToolStatusWidget.setObjectName(_fromUtf8("ToolStatusWidget"))
        ToolStatusWidget.resize(631, 45)
        ToolStatusWidget.setWindowTitle(QtGui.QApplication.translate("ToolStatusWidget", "Form", None, QtGui.QApplication.UnicodeUTF8))
        self.horizontalLayout = QtGui.QHBoxLayout(ToolStatusWidget)
        self.horizontalLayout.setSpacing(1)
        self.horizontalLayout.setSizeConstraint(QtGui.QLayout.SetNoConstraint)
        self.horizontalLayout.setMargin(1)
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        self.gridLayout = QtGui.QGridLayout()
        self.gridLayout.setContentsMargins(1, -1, -1, -1)
        self.gridLayout.setHorizontalSpacing(6)
        self.gridLayout.setObjectName(_fromUtf8("gridLayout"))
        self.zoomComboBox = QtGui.QComboBox(ToolStatusWidget)
        self.zoomComboBox.setMinimumSize(QtCore.QSize(50, 32))
        self.zoomComboBox.setMaximumSize(QtCore.QSize(70, 32))
        self.zoomComboBox.setObjectName(_fromUtf8("zoomComboBox"))
        self.gridLayout.addWidget(self.zoomComboBox, 0, 1, 1, 1)
        self.formLayout_2 = QtGui.QFormLayout()
        self.formLayout_2.setSizeConstraint(QtGui.QLayout.SetMinimumSize)
        self.formLayout_2.setFieldGrowthPolicy(QtGui.QFormLayout.ExpandingFieldsGrow)
        self.formLayout_2.setFormAlignment(QtCore.Qt.AlignLeading|QtCore.Qt.AlignLeft|QtCore.Qt.AlignVCenter)
        self.formLayout_2.setSpacing(1)
        self.formLayout_2.setObjectName(_fromUtf8("formLayout_2"))
        self.xCoordLabel = QtGui.QLabel(ToolStatusWidget)
        self.xCoordLabel.setMinimumSize(QtCore.QSize(16, 12))
        self.xCoordLabel.setMaximumSize(QtCore.QSize(10, 12))
        font = QtGui.QFont()
        font.setPointSize(10)
        self.xCoordLabel.setFont(font)
        self.xCoordLabel.setText(QtGui.QApplication.translate("ToolStatusWidget", "X:", None, QtGui.QApplication.UnicodeUTF8))
        self.xCoordLabel.setObjectName(_fromUtf8("xCoordLabel"))
        self.formLayout_2.setWidget(0, QtGui.QFormLayout.LabelRole, self.xCoordLabel)
        self.xCoordValueLabel = QtGui.QLabel(ToolStatusWidget)
        self.xCoordValueLabel.setMinimumSize(QtCore.QSize(40, 12))
        self.xCoordValueLabel.setMaximumSize(QtCore.QSize(50, 12))
        font = QtGui.QFont()
        font.setPointSize(10)
        self.xCoordValueLabel.setFont(font)
        self.xCoordValueLabel.setText(QtGui.QApplication.translate("ToolStatusWidget", "9999", None, QtGui.QApplication.UnicodeUTF8))
        self.xCoordValueLabel.setObjectName(_fromUtf8("xCoordValueLabel"))
        self.formLayout_2.setWidget(0, QtGui.QFormLayout.FieldRole, self.xCoordValueLabel)
        self.yCoordLabel = QtGui.QLabel(ToolStatusWidget)
        self.yCoordLabel.setMinimumSize(QtCore.QSize(16, 12))
        self.yCoordLabel.setMaximumSize(QtCore.QSize(10, 12))
        font = QtGui.QFont()
        font.setPointSize(10)
        self.yCoordLabel.setFont(font)
        self.yCoordLabel.setText(QtGui.QApplication.translate("ToolStatusWidget", "Y:", None, QtGui.QApplication.UnicodeUTF8))
        self.yCoordLabel.setObjectName(_fromUtf8("yCoordLabel"))
        self.formLayout_2.setWidget(1, QtGui.QFormLayout.LabelRole, self.yCoordLabel)
        self.yCoordValueLabel = QtGui.QLabel(ToolStatusWidget)
        self.yCoordValueLabel.setMinimumSize(QtCore.QSize(50, 12))
        self.yCoordValueLabel.setMaximumSize(QtCore.QSize(50, 12))
        font = QtGui.QFont()
        font.setPointSize(10)
        self.yCoordValueLabel.setFont(font)
        self.yCoordValueLabel.setText(QtGui.QApplication.translate("ToolStatusWidget", "9999", None, QtGui.QApplication.UnicodeUTF8))
        self.yCoordValueLabel.setObjectName(_fromUtf8("yCoordValueLabel"))
        self.formLayout_2.setWidget(1, QtGui.QFormLayout.FieldRole, self.yCoordValueLabel)
        self.gridLayout.addLayout(self.formLayout_2, 0, 0, 1, 1)
        self.toolTipLabel = QtGui.QLabel(ToolStatusWidget)
        self.toolTipLabel.setMinimumSize(QtCore.QSize(0, 0))
        self.toolTipLabel.setMaximumSize(QtCore.QSize(16777215, 16))
        self.toolTipLabel.setStyleSheet(_fromUtf8(""))
        self.toolTipLabel.setFrameShape(QtGui.QFrame.NoFrame)
        self.toolTipLabel.setText(QtGui.QApplication.translate("ToolStatusWidget", "ToolTip", None, QtGui.QApplication.UnicodeUTF8))
        self.toolTipLabel.setObjectName(_fromUtf8("toolTipLabel"))
        self.gridLayout.addWidget(self.toolTipLabel, 0, 2, 1, 1)
        self.horizontalLayout.addLayout(self.gridLayout)

        self.retranslateUi(ToolStatusWidget)
        QtCore.QMetaObject.connectSlotsByName(ToolStatusWidget)

    def retranslateUi(self, ToolStatusWidget):
        pass

import rsrc_rc
