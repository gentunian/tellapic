# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'networkStatus.ui'
#
# Created: Wed Sep 14 19:55:44 2011
#      by: PyQt4 UI code generator 4.8.5
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_NetworkStatusWidget(object):
    def setupUi(self, NetworkStatusWidget):
        NetworkStatusWidget.setObjectName(_fromUtf8("NetworkStatusWidget"))
        NetworkStatusWidget.resize(113, 41)
        NetworkStatusWidget.setWindowTitle(QtGui.QApplication.translate("NetworkStatusWidget", "Form", None, QtGui.QApplication.UnicodeUTF8))
        self.horizontalLayout = QtGui.QHBoxLayout(NetworkStatusWidget)
        self.horizontalLayout.setSpacing(0)
        self.horizontalLayout.setContentsMargins(0, 0, 2, 0)
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        self.formLayout = QtGui.QFormLayout()
        self.formLayout.setFieldGrowthPolicy(QtGui.QFormLayout.AllNonFixedFieldsGrow)
        self.formLayout.setLabelAlignment(QtCore.Qt.AlignRight|QtCore.Qt.AlignTrailing|QtCore.Qt.AlignVCenter)
        self.formLayout.setFormAlignment(QtCore.Qt.AlignRight|QtCore.Qt.AlignTop|QtCore.Qt.AlignTrailing)
        self.formLayout.setVerticalSpacing(1)
        self.formLayout.setObjectName(_fromUtf8("formLayout"))
        self.latencyLabel = QtGui.QLabel(NetworkStatusWidget)
        self.latencyLabel.setMinimumSize(QtCore.QSize(0, 0))
        self.latencyLabel.setMaximumSize(QtCore.QSize(99999, 99999))
        font = QtGui.QFont()
        font.setPointSize(10)
        self.latencyLabel.setFont(font)
        self.latencyLabel.setText(QtGui.QApplication.translate("NetworkStatusWidget", "Latency:", None, QtGui.QApplication.UnicodeUTF8))
        self.latencyLabel.setObjectName(_fromUtf8("latencyLabel"))
        self.formLayout.setWidget(0, QtGui.QFormLayout.LabelRole, self.latencyLabel)
        self.latencyValueLabel = QtGui.QLabel(NetworkStatusWidget)
        self.latencyValueLabel.setMinimumSize(QtCore.QSize(0, 0))
        self.latencyValueLabel.setMaximumSize(QtCore.QSize(9999999, 9999999))
        font = QtGui.QFont()
        font.setPointSize(10)
        self.latencyValueLabel.setFont(font)
        self.latencyValueLabel.setLayoutDirection(QtCore.Qt.RightToLeft)
        self.latencyValueLabel.setText(QtGui.QApplication.translate("NetworkStatusWidget", "9999 ms", None, QtGui.QApplication.UnicodeUTF8))
        self.latencyValueLabel.setAlignment(QtCore.Qt.AlignRight|QtCore.Qt.AlignTrailing|QtCore.Qt.AlignVCenter)
        self.latencyValueLabel.setObjectName(_fromUtf8("latencyValueLabel"))
        self.formLayout.setWidget(0, QtGui.QFormLayout.FieldRole, self.latencyValueLabel)
        self.statusLabel = QtGui.QLabel(NetworkStatusWidget)
        self.statusLabel.setMinimumSize(QtCore.QSize(0, 0))
        self.statusLabel.setMaximumSize(QtCore.QSize(99999, 99999))
        font = QtGui.QFont()
        font.setPointSize(10)
        self.statusLabel.setFont(font)
        self.statusLabel.setText(QtGui.QApplication.translate("NetworkStatusWidget", "Status:", None, QtGui.QApplication.UnicodeUTF8))
        self.statusLabel.setObjectName(_fromUtf8("statusLabel"))
        self.formLayout.setWidget(1, QtGui.QFormLayout.LabelRole, self.statusLabel)
        self.statusValueLabel = QtGui.QLabel(NetworkStatusWidget)
        self.statusValueLabel.setMinimumSize(QtCore.QSize(0, 0))
        self.statusValueLabel.setMaximumSize(QtCore.QSize(999999, 999999))
        self.statusValueLabel.setLayoutDirection(QtCore.Qt.RightToLeft)
        self.statusValueLabel.setText(_fromUtf8(""))
        self.statusValueLabel.setPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/status.png")))
        self.statusValueLabel.setScaledContents(False)
        self.statusValueLabel.setAlignment(QtCore.Qt.AlignLeading|QtCore.Qt.AlignLeft|QtCore.Qt.AlignVCenter)
        self.statusValueLabel.setMargin(0)
        self.statusValueLabel.setIndent(-1)
        self.statusValueLabel.setObjectName(_fromUtf8("statusValueLabel"))
        self.formLayout.setWidget(1, QtGui.QFormLayout.FieldRole, self.statusValueLabel)
        self.horizontalLayout.addLayout(self.formLayout)

        self.retranslateUi(NetworkStatusWidget)
        QtCore.QMetaObject.connectSlotsByName(NetworkStatusWidget)

    def retranslateUi(self, NetworkStatusWidget):
        pass

import rsrc_rc
