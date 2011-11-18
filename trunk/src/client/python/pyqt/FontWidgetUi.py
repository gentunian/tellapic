# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'fontWidget.ui'
#
# Created: Mon Oct 17 16:09:11 2011
#      by: PyQt4 UI code generator 4.8.5
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_FontWidget(object):
    def setupUi(self, FontWidget):
        FontWidget.setObjectName(_fromUtf8("FontWidget"))
        FontWidget.resize(400, 300)
        FontWidget.setWindowTitle(QtGui.QApplication.translate("FontWidget", "Form", None, QtGui.QApplication.UnicodeUTF8))
        self.verticalLayout = QtGui.QVBoxLayout(FontWidget)
        self.verticalLayout.setMargin(1)
        self.verticalLayout.setObjectName(_fromUtf8("verticalLayout"))
        self.formLayout_4 = QtGui.QFormLayout()
        self.formLayout_4.setSizeConstraint(QtGui.QLayout.SetMinimumSize)
        self.formLayout_4.setFieldGrowthPolicy(QtGui.QFormLayout.AllNonFixedFieldsGrow)
        self.formLayout_4.setLabelAlignment(QtCore.Qt.AlignRight|QtCore.Qt.AlignTrailing|QtCore.Qt.AlignVCenter)
        self.formLayout_4.setSpacing(0)
        self.formLayout_4.setObjectName(_fromUtf8("formLayout_4"))
        self.label_7 = QtGui.QLabel(FontWidget)
        self.label_7.setText(QtGui.QApplication.translate("FontWidget", "Family:", None, QtGui.QApplication.UnicodeUTF8))
        self.label_7.setObjectName(_fromUtf8("label_7"))
        self.formLayout_4.setWidget(0, QtGui.QFormLayout.LabelRole, self.label_7)
        self.fontComboBox = QtGui.QFontComboBox(FontWidget)
        self.fontComboBox.setMaximumSize(QtCore.QSize(16777215, 16777215))
        self.fontComboBox.setObjectName(_fromUtf8("fontComboBox"))
        self.formLayout_4.setWidget(0, QtGui.QFormLayout.FieldRole, self.fontComboBox)
        self.label_10 = QtGui.QLabel(FontWidget)
        self.label_10.setText(QtGui.QApplication.translate("FontWidget", "Style:", None, QtGui.QApplication.UnicodeUTF8))
        self.label_10.setObjectName(_fromUtf8("label_10"))
        self.formLayout_4.setWidget(1, QtGui.QFormLayout.LabelRole, self.label_10)
        self.horizontalLayout_6 = QtGui.QHBoxLayout()
        self.horizontalLayout_6.setSpacing(6)
        self.horizontalLayout_6.setObjectName(_fromUtf8("horizontalLayout_6"))
        self.fontStyleBoldButton = QtGui.QPushButton(FontWidget)
        self.fontStyleBoldButton.setMaximumSize(QtCore.QSize(24, 24))
        self.fontStyleBoldButton.setToolTip(QtGui.QApplication.translate("FontWidget", "Bold", None, QtGui.QApplication.UnicodeUTF8))
        self.fontStyleBoldButton.setText(_fromUtf8(""))
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/bold.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.fontStyleBoldButton.setIcon(icon)
        self.fontStyleBoldButton.setIconSize(QtCore.QSize(18, 18))
        self.fontStyleBoldButton.setCheckable(True)
        self.fontStyleBoldButton.setObjectName(_fromUtf8("fontStyleBoldButton"))
        self.horizontalLayout_6.addWidget(self.fontStyleBoldButton)
        self.fontStyleItalicButton = QtGui.QPushButton(FontWidget)
        self.fontStyleItalicButton.setMaximumSize(QtCore.QSize(24, 24))
        self.fontStyleItalicButton.setToolTip(QtGui.QApplication.translate("FontWidget", "Italic", None, QtGui.QApplication.UnicodeUTF8))
        self.fontStyleItalicButton.setText(_fromUtf8(""))
        icon1 = QtGui.QIcon()
        icon1.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/italic.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.fontStyleItalicButton.setIcon(icon1)
        self.fontStyleItalicButton.setIconSize(QtCore.QSize(18, 18))
        self.fontStyleItalicButton.setCheckable(True)
        self.fontStyleItalicButton.setObjectName(_fromUtf8("fontStyleItalicButton"))
        self.horizontalLayout_6.addWidget(self.fontStyleItalicButton)
        self.formLayout_4.setLayout(1, QtGui.QFormLayout.FieldRole, self.horizontalLayout_6)
        self.label_11 = QtGui.QLabel(FontWidget)
        self.label_11.setText(QtGui.QApplication.translate("FontWidget", "Size:", None, QtGui.QApplication.UnicodeUTF8))
        self.label_11.setObjectName(_fromUtf8("label_11"))
        self.formLayout_4.setWidget(2, QtGui.QFormLayout.LabelRole, self.label_11)
        self.fontSizeSpinner = QtGui.QDoubleSpinBox(FontWidget)
        self.fontSizeSpinner.setMaximumSize(QtCore.QSize(100, 16777215))
        self.fontSizeSpinner.setAccelerated(True)
        self.fontSizeSpinner.setObjectName(_fromUtf8("fontSizeSpinner"))
        self.formLayout_4.setWidget(2, QtGui.QFormLayout.FieldRole, self.fontSizeSpinner)
        self.label_12 = QtGui.QLabel(FontWidget)
        self.label_12.setText(QtGui.QApplication.translate("FontWidget", "Text:", None, QtGui.QApplication.UnicodeUTF8))
        self.label_12.setObjectName(_fromUtf8("label_12"))
        self.formLayout_4.setWidget(3, QtGui.QFormLayout.LabelRole, self.label_12)
        self.label_13 = QtGui.QLabel(FontWidget)
        self.label_13.setToolTip(QtGui.QApplication.translate("FontWidget", "Available chars for text entry.", None, QtGui.QApplication.UnicodeUTF8))
        self.label_13.setText(QtGui.QApplication.translate("FontWidget", "Chars left:", None, QtGui.QApplication.UnicodeUTF8))
        self.label_13.setObjectName(_fromUtf8("label_13"))
        self.formLayout_4.setWidget(4, QtGui.QFormLayout.LabelRole, self.label_13)
        self.charCounter = QtGui.QLCDNumber(FontWidget)
        self.charCounter.setToolTip(QtGui.QApplication.translate("FontWidget", "Available chars for text entry.", None, QtGui.QApplication.UnicodeUTF8))
        self.charCounter.setFrameShape(QtGui.QFrame.NoFrame)
        self.charCounter.setNumDigits(3)
        self.charCounter.setSegmentStyle(QtGui.QLCDNumber.Flat)
        self.charCounter.setProperty("value", 512.0)
        self.charCounter.setObjectName(_fromUtf8("charCounter"))
        self.formLayout_4.setWidget(4, QtGui.QFormLayout.FieldRole, self.charCounter)
        self.fontTextArea = QtGui.QPlainTextEdit(FontWidget)
        self.fontTextArea.setMinimumSize(QtCore.QSize(0, 0))
        self.fontTextArea.setObjectName(_fromUtf8("fontTextArea"))
        self.formLayout_4.setWidget(3, QtGui.QFormLayout.FieldRole, self.fontTextArea)
        self.verticalLayout.addLayout(self.formLayout_4)

        self.retranslateUi(FontWidget)
        QtCore.QMetaObject.connectSlotsByName(FontWidget)

    def retranslateUi(self, FontWidget):
        pass

import rsrc_rc
