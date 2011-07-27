from PyQt4 import QtCore, QtGui
from ChatUi import *

class ChatWidget(QtGui.QTabWidget, Ui_TabWidget):
    
    def __init__(self, parent = None):
        super(ChatWidget, self).__init__(parent)
        self.setupUi(self)
        self.actionShowSmileyPopup.toggled.connect(self.actionShowSmileyPopup_toggle)


    def actionShowSmileyPopup_toggle(self, toggled = None):
        if (toggled):
            print("show smiley popup")
        else:
            print("hide smiley popup");
