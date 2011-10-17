from PyQt4 import QtCore, QtGui
from CustomChatWidgetUi import *

class CustomChatWidget(QtGui.QTabWidget, Ui_chatWidget):

    # This signal is emmited when an user types text in QLineEdit:textField
    #enteredText = QtCore.pyqtSignal(int)
    __pyqtSignals__ = ("enteredText(QtCore.QString")

    def __init__(self, parent = None):
        super(CustomChatWidget, self).__init__(parent)
        self.setupUi(self)
        self.buildSmileysMenu()
        self.smileyButton.setMenu(self.menu)
        self.smileyButton.setDefaultAction(self.actionGreenSmiley)

    @QtCore.pyqtSignature("appendText(QString")
    def appendText(self, text):
        self.textArea.append(text)

    @QtCore.pyqtSlot()
    def updateLastText(self):
        self.lastEnteredText = self.textField.text()
        self.textField.clear()
        self.emit(QtCore.SIGNAL("enteredText(QtCore.QString)"), self.lastEnteredText)

    # Returns the entered text
    def getLastEnteredText(self):
        return self.lastEnteredText

    def buildSmileysMenu(self):
        self.menu = QtGui.QMenu()
        self.menu.addAction(self.actionHeartBreakSmiley)
        self.menu.addAction(self.actionHeartSmiley)
        self.menu.addAction(self.actionConfuseSmiley)
        self.menu.addAction(self.actionCoolSmiley)
        self.menu.addAction(self.actionCrySmiley)
        self.menu.addAction(self.actionDrawSmiley)
        self.menu.addAction(self.actionEekBlueSmiley)
        self.menu.addAction(self.actionEvilSmiley)
        self.menu.addAction(self.actionGrinSmiley)
        self.menu.addAction(self.actionMadSmiley)
        self.menu.addAction(self.actionMoneySmiley)
        self.menu.addAction(self.actionGreenSmiley)
        self.menu.addAction(self.actionTongueSmiley)
        self.menu.addAction(self.actionRedSmiley)
        self.menu.addAction(self.actionWinkSmiley)
        self.menu.addAction(self.actionYesSmiley)
        self.menu.addAction(self.actionNoSmiley)
        self.actionHeartBreakSmiley.triggered.connect(self.actionHeartBreakSmiley_trigger)
        self.actionHeartSmiley.triggered.connect(self.actionHeartSmiley_trigger)
        self.actionConfuseSmiley.triggered.connect(self.actionConfuseSmiley_trigger)
        self.actionCoolSmiley.triggered.connect(self.actionCoolSmiley_trigger)
        self.actionCrySmiley.triggered.connect(self.actionCrySmiley_trigger)
        self.actionDrawSmiley.triggered.connect(self.actionDrawSmiley_trigger)
        self.actionEekBlueSmiley.triggered.connect(self.actionEekBlueSmiley_trigger)
        self.actionEvilSmiley.triggered.connect(self.actionEvilSmiley_trigger)
        self.actionGrinSmiley.triggered.connect(self.actionGrinSmiley_trigger)
        self.actionMadSmiley.triggered.connect(self.actionMadSmiley_trigger)
        self.actionMoneySmiley.triggered.connect(self.actionMoneySmiley_trigger)
        self.actionGreenSmiley.triggered.connect(self.actionGreenSmiley_trigger)
        self.actionTongueSmiley.triggered.connect(self.actionTongueSmiley_trigger)
        self.actionRedSmiley.triggered.connect(self.actionRedSmiley_trigger)
        self.actionWinkSmiley.triggered.connect(self.actionWinkSmiley_trigger)
        self.actionYesSmiley.triggered.connect(self.actionYesSmiley_trigger)
        self.actionNoSmiley.triggered.connect(self.actionNoSmiley_trigger)

    def actionHeartBreakSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionHeartBreakSmiley)

    def actionHeartSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionHeartSmiley)

    def actionConfuseSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionConfuseSmiley)

    def actionCoolSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionCoolSmiley)

    def actionCrySmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionCrySmiley)

    def actionDrawSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionDrawSmiley)

    def actionEekBlueSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionEekBlueSmiley)

    def actionEvilSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionEvilSmiley)

    def actionGrinSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionGrinSmiley)

    def actionMadSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionManSmiley)

    def actionMoneySmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionMoneySmiley)

    def actionGreenSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionGreenSmiley)

    def actionTongueSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionTongueSmiley)

    def actionRedSmiley_trigger(self):
        print("no smiley pressed")
        self.smileyButton.setDefaultAction(self.actionRedSmiley)

    def actionWinkSmiley_trigger(self):
        self.smileyButton.setDefaultAction(self.actionWinkSmiley)
        print("no smiley pressed")

    def actionYesSmiley_trigger(self):
        self.smileyButton.setDefaultAction(self.actionYesSmiley)
        print("no smiley pressed")

    def actionNoSmiley_trigger(self):
        self.smileyButton.setDefaultAction(self.actionNoSmiley)
        print("no smiley pressed")

if __name__ == "__main__":
    import sys
    app = QtGui.QApplication(sys.argv)
    main = QtGui.QMainWindow()
    chat = CustomChatWidget(main)
    main.setCentralWidget(chat)
    main.show()
    sys.exit(app.exec_())