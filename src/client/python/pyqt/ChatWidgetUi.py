# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'ui/chatWidget.ui'
#
# Created: Tue Nov  8 10:49:51 2011
#      by: PyQt4 UI code generator 4.8.6
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_chatWidget(object):
    def setupUi(self, chatWidget):
        chatWidget.setObjectName(_fromUtf8("chatWidget"))
        chatWidget.resize(460, 333)
        chatWidget.setWindowTitle(_fromUtf8(""))
        chatWidget.setStyleSheet(_fromUtf8(""))
        self.verticalLayout = QtGui.QVBoxLayout(chatWidget)
        self.verticalLayout.setSpacing(3)
        self.verticalLayout.setMargin(1)
        self.verticalLayout.setObjectName(_fromUtf8("verticalLayout"))
        self.tabWidget = QtGui.QTabWidget(chatWidget)
        self.tabWidget.setStyleSheet(_fromUtf8(" QTabWidget::pane { /* The tab widget frame */\n"
"     border-top: 0px;\n"
" }\n"
"QTabWidget::tab-bar {\n"
"     left: 8px; /* move to the right by 5px */\n"
"     height: 29px;\n"
" }\n"
""))
        self.tabWidget.setTabPosition(QtGui.QTabWidget.North)
        self.tabWidget.setElideMode(QtCore.Qt.ElideNone)
        self.tabWidget.setTabsClosable(False)
        self.tabWidget.setMovable(True)
        self.tabWidget.setObjectName(_fromUtf8("tabWidget"))
        self.mainTab = QtGui.QWidget()
        self.mainTab.setObjectName(_fromUtf8("mainTab"))
        self.verticalLayout_2 = QtGui.QVBoxLayout(self.mainTab)
        self.verticalLayout_2.setSpacing(2)
        self.verticalLayout_2.setMargin(0)
        self.verticalLayout_2.setObjectName(_fromUtf8("verticalLayout_2"))
        self.mainTextArea = QtGui.QTextEdit(self.mainTab)
        self.mainTextArea.setStyleSheet(_fromUtf8("QTextEdit {\n"
"     border: 1px solid gray;\n"
"     border-radius: 8px;\n"
"     padding: 0 5px;\n"
"     background: white;\n"
"     selection-background-color: blue;\n"
" }"))
        self.mainTextArea.setObjectName(_fromUtf8("mainTextArea"))
        self.verticalLayout_2.addWidget(self.mainTextArea)
        self.tabWidget.addTab(self.mainTab, _fromUtf8(""))
        self.verticalLayout.addWidget(self.tabWidget)
        self.horizontalLayout = QtGui.QHBoxLayout()
        self.horizontalLayout.setSpacing(3)
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        self.inputText = QtGui.QLineEdit(chatWidget)
        self.inputText.setStyleSheet(_fromUtf8("QLineEdit {\n"
"     border: 1px solid gray;\n"
"     border-radius: 8px;\n"
"     padding: 0 6px;\n"
"     background: white;\n"
"     selection-background-color: darkgray;\n"
" }"))
        self.inputText.setObjectName(_fromUtf8("inputText"))
        self.horizontalLayout.addWidget(self.inputText)
        self.smileyButton = QtGui.QToolButton(chatWidget)
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
        self.actionHeartBreakSmiley = QtGui.QAction(chatWidget)
        icon1 = QtGui.QIcon()
        icon1.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/heart-break.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionHeartBreakSmiley.setIcon(icon1)
        self.actionHeartBreakSmiley.setText(QtGui.QApplication.translate("chatWidget", "heartBreakSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionHeartBreakSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", "</3", None, QtGui.QApplication.UnicodeUTF8))
        self.actionHeartBreakSmiley.setObjectName(_fromUtf8("actionHeartBreakSmiley"))
        self.actionHeartSmiley = QtGui.QAction(chatWidget)
        icon2 = QtGui.QIcon()
        icon2.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/heart.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionHeartSmiley.setIcon(icon2)
        self.actionHeartSmiley.setText(QtGui.QApplication.translate("chatWidget", "heartSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionHeartSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", "<3", None, QtGui.QApplication.UnicodeUTF8))
        self.actionHeartSmiley.setObjectName(_fromUtf8("actionHeartSmiley"))
        self.actionConfuseSmiley = QtGui.QAction(chatWidget)
        icon3 = QtGui.QIcon()
        icon3.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-confuse.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionConfuseSmiley.setIcon(icon3)
        self.actionConfuseSmiley.setText(QtGui.QApplication.translate("chatWidget", "confuseSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionConfuseSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", ":S", None, QtGui.QApplication.UnicodeUTF8))
        self.actionConfuseSmiley.setObjectName(_fromUtf8("actionConfuseSmiley"))
        self.actionCoolSmiley = QtGui.QAction(chatWidget)
        icon4 = QtGui.QIcon()
        icon4.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-cool.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionCoolSmiley.setIcon(icon4)
        self.actionCoolSmiley.setText(QtGui.QApplication.translate("chatWidget", "coolSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionCoolSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", "8)", None, QtGui.QApplication.UnicodeUTF8))
        self.actionCoolSmiley.setObjectName(_fromUtf8("actionCoolSmiley"))
        self.actionCrySmiley = QtGui.QAction(chatWidget)
        icon5 = QtGui.QIcon()
        icon5.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-cry.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionCrySmiley.setIcon(icon5)
        self.actionCrySmiley.setText(QtGui.QApplication.translate("chatWidget", "crySmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionCrySmiley.setToolTip(QtGui.QApplication.translate("chatWidget", ";(", None, QtGui.QApplication.UnicodeUTF8))
        self.actionCrySmiley.setObjectName(_fromUtf8("actionCrySmiley"))
        self.actionDrawSmiley = QtGui.QAction(chatWidget)
        icon6 = QtGui.QIcon()
        icon6.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-draw.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionDrawSmiley.setIcon(icon6)
        self.actionDrawSmiley.setText(QtGui.QApplication.translate("chatWidget", "drawSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionDrawSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", ":u", None, QtGui.QApplication.UnicodeUTF8))
        self.actionDrawSmiley.setObjectName(_fromUtf8("actionDrawSmiley"))
        self.actionEekBlueSmiley = QtGui.QAction(chatWidget)
        icon7 = QtGui.QIcon()
        icon7.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-eek-blue.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionEekBlueSmiley.setIcon(icon7)
        self.actionEekBlueSmiley.setText(QtGui.QApplication.translate("chatWidget", "eekBlueSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionEekBlueSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", "o.o", None, QtGui.QApplication.UnicodeUTF8))
        self.actionEekBlueSmiley.setObjectName(_fromUtf8("actionEekBlueSmiley"))
        self.actionEvilSmiley = QtGui.QAction(chatWidget)
        icon8 = QtGui.QIcon()
        icon8.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-evil.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionEvilSmiley.setIcon(icon8)
        self.actionEvilSmiley.setText(QtGui.QApplication.translate("chatWidget", "evilSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionEvilSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", "(6)", None, QtGui.QApplication.UnicodeUTF8))
        self.actionEvilSmiley.setObjectName(_fromUtf8("actionEvilSmiley"))
        self.actionGrinSmiley = QtGui.QAction(chatWidget)
        icon9 = QtGui.QIcon()
        icon9.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-grin.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionGrinSmiley.setIcon(icon9)
        self.actionGrinSmiley.setText(QtGui.QApplication.translate("chatWidget", "grinSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionGrinSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", ":D", None, QtGui.QApplication.UnicodeUTF8))
        self.actionGrinSmiley.setObjectName(_fromUtf8("actionGrinSmiley"))
        self.actionMadSmiley = QtGui.QAction(chatWidget)
        icon10 = QtGui.QIcon()
        icon10.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-mad.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionMadSmiley.setIcon(icon10)
        self.actionMadSmiley.setText(QtGui.QApplication.translate("chatWidget", "madSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionMadSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", ">(", None, QtGui.QApplication.UnicodeUTF8))
        self.actionMadSmiley.setObjectName(_fromUtf8("actionMadSmiley"))
        self.actionMoneySmiley = QtGui.QAction(chatWidget)
        icon11 = QtGui.QIcon()
        icon11.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-money.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionMoneySmiley.setIcon(icon11)
        self.actionMoneySmiley.setText(QtGui.QApplication.translate("chatWidget", "moneySmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionMoneySmiley.setToolTip(QtGui.QApplication.translate("chatWidget", "$$", None, QtGui.QApplication.UnicodeUTF8))
        self.actionMoneySmiley.setObjectName(_fromUtf8("actionMoneySmiley"))
        self.actionGreenSmiley = QtGui.QAction(chatWidget)
        icon12 = QtGui.QIcon()
        icon12.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-mr-green.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionGreenSmiley.setIcon(icon12)
        self.actionGreenSmiley.setText(QtGui.QApplication.translate("chatWidget", "greenSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionGreenSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", ">D", None, QtGui.QApplication.UnicodeUTF8))
        self.actionGreenSmiley.setObjectName(_fromUtf8("actionGreenSmiley"))
        self.actionTongueSmiley = QtGui.QAction(chatWidget)
        icon13 = QtGui.QIcon()
        icon13.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-razz.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionTongueSmiley.setIcon(icon13)
        self.actionTongueSmiley.setText(QtGui.QApplication.translate("chatWidget", "tongueSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionTongueSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", ":P", None, QtGui.QApplication.UnicodeUTF8))
        self.actionTongueSmiley.setObjectName(_fromUtf8("actionTongueSmiley"))
        self.actionRedSmiley = QtGui.QAction(chatWidget)
        icon14 = QtGui.QIcon()
        icon14.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-red.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionRedSmiley.setIcon(icon14)
        self.actionRedSmiley.setText(QtGui.QApplication.translate("chatWidget", "redSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionRedSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", ":$", None, QtGui.QApplication.UnicodeUTF8))
        self.actionRedSmiley.setObjectName(_fromUtf8("actionRedSmiley"))
        self.actionWinkSmiley = QtGui.QAction(chatWidget)
        icon15 = QtGui.QIcon()
        icon15.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/smiley-wink.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionWinkSmiley.setIcon(icon15)
        self.actionWinkSmiley.setText(QtGui.QApplication.translate("chatWidget", "winkSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionWinkSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", ";)", None, QtGui.QApplication.UnicodeUTF8))
        self.actionWinkSmiley.setObjectName(_fromUtf8("actionWinkSmiley"))
        self.actionYesSmiley = QtGui.QAction(chatWidget)
        icon16 = QtGui.QIcon()
        icon16.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/thumb-up.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionYesSmiley.setIcon(icon16)
        self.actionYesSmiley.setText(QtGui.QApplication.translate("chatWidget", "yesSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionYesSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", "(y)", None, QtGui.QApplication.UnicodeUTF8))
        self.actionYesSmiley.setObjectName(_fromUtf8("actionYesSmiley"))
        self.actionNoSmiley = QtGui.QAction(chatWidget)
        icon17 = QtGui.QIcon()
        icon17.addPixmap(QtGui.QPixmap(_fromUtf8(":/icons/resources/icons/app-icons/smileys/thumb.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.actionNoSmiley.setIcon(icon17)
        self.actionNoSmiley.setText(QtGui.QApplication.translate("chatWidget", "noSmiley", None, QtGui.QApplication.UnicodeUTF8))
        self.actionNoSmiley.setToolTip(QtGui.QApplication.translate("chatWidget", "(n)", None, QtGui.QApplication.UnicodeUTF8))
        self.actionNoSmiley.setObjectName(_fromUtf8("actionNoSmiley"))

        self.retranslateUi(chatWidget)
        self.tabWidget.setCurrentIndex(0)
        QtCore.QMetaObject.connectSlotsByName(chatWidget)

    def retranslateUi(self, chatWidget):
        self.tabWidget.setTabText(self.tabWidget.indexOf(self.mainTab), QtGui.QApplication.translate("chatWidget", "Main", None, QtGui.QApplication.UnicodeUTF8))

import rsrc_rc

class chatWidget(QtGui.QWidget, Ui_chatWidget):
    def __init__(self, parent=None, f=QtCore.Qt.WindowFlags()):
        QtGui.QWidget.__init__(self, parent, f)

        self.setupUi(self)

