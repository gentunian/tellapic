'''

@author: sebastian.treu@gmail.com
'''
from PyQt4.QtCore import pyqtSignature
from PyQt4.QtCore import pyqtSlot
from PyQt4.QtCore import pyqtSignal
from PyQt4.QtCore import QObject
from PyQt4.QtGui import QApplication
from PyQt4.QtGui import QTabWidget
from PyQt4.QtGui import QMenu
from PyQt4.QtGui import QMainWindow
try:
    from PyQt4.QtCore import QString
except ImportError:
    # we are using Python3 so QString is not defined
    QString = type("")

from queue import Queue

from ChatWidgetUi import Ui_chatWidget

class Message(QObject):
    def __init__(self, text, who, to = None):
        QObject.__init__(self)
        self.text = text
        self.to   = to
        self.who  = who

    def isPrivate(self):
        return self.to is not None

class ChatModel(QObject):
    messageAdded = pyqtSignal(Message)

    def __init__(self):
        QObject.__init__(self)
        self.sentQueue = Queue(0)
        self.messageList = []

    @pyqtSlot()
    def addMessage(self, message):
        self.messageList.append(message)
        self.messageAdded.emit(message)

class ChatWidget(QTabWidget, Ui_chatWidget):
    def __init__(self, model, parent = None):
        super(ChatWidget, self).__init__(parent)
        self.setupUi(self)
        #self.mainTab.textEntered.connect(self)

    @pyqtSlot()
    def addMessage(self, message):
        if message.isPrivate():
            pass
        else:
            pass


if __name__ == "__main__":
    import sys
    app = QApplication(sys.argv)
    main = QMainWindow()
    chat = ChatWidget(main)
    main.setCentralWidget(chat)
    main.show()
    sys.exit(app.exec_())
