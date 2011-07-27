from PyQt4.QtCore import QEvent

class MyEvent(QEvent):
    def __init__(self, arg, t = None):
        if (t is not None):
            QEvent.__init__(self, QEvent.User + t)
        else:
            QEvent.__init__(self, QEvent.User)
            
        self.arg = arg