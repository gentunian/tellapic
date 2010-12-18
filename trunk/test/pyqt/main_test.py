from PyQt4 import QtCore, QtGui
#from waiting import Ui_Waiting
from main import Ui_Main
import tellapic
import select
import time
import Queue
import threading
import signal
import os

class MainTest(QtGui.QDialog):
    ctl = { tellapic.CTL_CL_FILEASK   : 'CTL_CL_FILEASK',
            tellapic.CTL_CL_FILEOK    : 'CTL_CL_FILEOK',
            tellapic.CTL_SV_PWDFAIL   : 'CTL_SV_PWDFAIL',
            tellapic.CTL_SV_PWDOK     : 'CTL_SV_PWDOK',
            tellapic.CTL_SV_PWDASK    : 'CTL_SV_PWDASK',
            tellapic.CTL_SV_CLIST     : 'CTL_SV_CLIST',
            tellapic.CTL_SV_CLRM      : 'CTL_SV_CLRM',
            tellapic.CTL_SV_ID        : 'CTL_SV_ID0',
            tellapic.CTL_SV_NAMEINUSE : 'CTL_SV_NAMEINUSE' 
            }
    
    ctli = { tellapic.CTL_SV_FILE  : 'CTL_SV_FILE',
             tellapic.CTL_CL_PWD   : 'CTL_CL_PWD',
             tellapic.CTL_CL_NAME  : 'CTL_CL_NAME', 
             tellapic.CTL_SV_CLADD : 'CTL_SV_CLADD' 
             }
    
    ctlchat = { tellapic.CTL_CL_BMSG : 'CTL_CL_BMSG', 
                tellapic.CTL_CL_PMSG : 'CTL_CL_PMSG'
                }
    
    ctldrawing = { tellapic.CTL_CL_FIG : 'CTL_CL_FIG',
                   tellapic.CTL_CL_DRW : 'CTL_CL_DRW'
                   }

    cbyte = {tellapic.CTL_CL_BMSG : 'CTL_CL_BMSG',
             tellapic.CTL_CL_PMSG : 'CTL_CL_PMSG',
             tellapic.CTL_CL_FIG  : 'CTL_CL_FIG',
             tellapic.CTL_CL_DRW : 'CTL_CL_DRW',
             tellapic.CTL_CL_CLIST: 'CTL_CL_CLIST', 
             tellapic.CTL_CL_PWD: 'CTL_CL_PWD',
             tellapic.CTL_CL_FILEASK: 'CTL_CL_FILEASK',
             tellapic.CTL_CL_FILEOK: 'CTL_CL_FILEOK', 
             tellapic.CTL_CL_DISC: 'CTL_CL_DISC', 
             tellapic.CTL_CL_NAME:'CTL_CL_NAME',
             tellapic.CTL_SV_CLRM:'CTL_SV_CLRM',
             tellapic.CTL_SV_CLADD:'CTL_SV_CLADD',
             tellapic.CTL_SV_CLIST:'CTL_SV_CLIST',
             tellapic.CTL_SV_PWDASK:'CTL_SV_PWDASK',
             tellapic.CTL_SV_PWDOK:'CTL_SV_PWDOK',
             tellapic.CTL_SV_PWDFAIL:'CTL_SV_PWDFAIL',
             tellapic.CTL_SV_FILE:'CTL_SV_FILE', 
             tellapic.CTL_SV_ID:'CTL_SV_ID',
             tellapic.CTL_SV_NAMEINUSE: 'CTL_SV_NAMEINUSE',
             tellapic.CTL_FAIL : 'CTL_FAIL'}

    def __init__(self, queue, endcommand, fd):
        QtGui.QDialog.__init__(self)

        # Set up the user interface from Designer.
        self.fd = fd
        self.id = fd
        self.ui = Ui_Main()
        self.ui.setupUi(self)
        self.setModal(False)
        self.queue = queue
        self.endcommand = endcommand
        self.ui.receiveButton.setEnabled(False)

        # Make some local modifications.
        #self.ui.colorDepthCombo.addItem("2 colors (1 bit per pixel)")
        # Connect up the buttons.
        #self.connect(self.ui.exitButton, QtCore.SIGNAL("clicked()"), self, QtCore.SLOT("reject()"))
    

    @QtCore.pyqtSlot()
    def on_sendButton_clicked(self):
        for value, name in self.cbyte.iteritems():
            if name == self.ui.headerCByte.currentText():
                break

        if value in self.ctl:
            stream = tellapic.tellapic_build_ctl(value, int(self.ui.svcontrolIdFrom.text()))

        elif value in self.ctli:
            stream = tellapic.tellapic_build_ctle(value, int(self.ui.svcontrolIdFrom.text()), self.ui.svcontrolInfo.toPlainText().length(), str(self.ui.svcontrolInfo.toPlainText()))

        elif value in self.ctlchat:
            stream = tellapic.tellapic_build_chat(value, int(self.ui.chatIdFrom.text()), int(self.ui.chatIdTo.text()),self.ui.chatText.text().lenght(), str(self.ui.chatText.text()))

        elif value in self.ctldrawing:
            if value == tellapic.CTL_CL_FIG:
                stream = tellapic.stream_t()
                stream.header.endian = 0
                stream.header.cbyte = value
                stream.header.ssize = tellapic.FIG_STREAM_SIZE
                stream.data.drawing.idfrom = self.id
                stream.data.drawing.dcbyte = int(self.ui.drawingDCByte.text())
                stream.data.drawing.dnumber = int(self.ui.drawingNumber.text())
                stream.data.drawing.width = float(self.ui.drawingWidth.text())
                stream.data.drawing.opacity = float(self.ui.drawingOpacity.text())
                stream.data.drawing.color.red   = 255
                stream.data.drawing.color.green = 125
                stream.data.drawing.color.blue  = 1
                stream.data.drawing.point1.x = int(self.ui.drawingXCoordinate.text())
                stream.data.drawing.point1.y = int(self.ui.drawingYCoordinate.text())
                stream.data.drawing.type.figure.endcaps = int(self.ui.figureEndCaps.text())
                stream.data.drawing.type.figure.linejoin = int(self.ui.figureLineJoin.text())
                stream.data.drawing.type.figure.miterlimit = float(self.ui.figureMiterLimit.text())
                stream.data.drawing.type.figure.dash_phase = float(self.ui.figureDashPhase.text())
                stream.data.drawing.type.figure.point2.x = int(self.ui.figureEndXCoordinate.text())
                stream.data.drawing.type.figure.point2.y = int(self.ui.figureEndYCoordinate.text())

        else:
            pass

        tellapic.tellapic_send(self.fd, stream)


    @QtCore.pyqtSlot()
    def on_exitButton_clicked(self):
        print("exiting gui...")
        tellapic.tellapic_close_fd(self.fd)
        self.endcommand()



    @QtCore.pyqtSlot()
    def on_receiveButton_clicked(self):
        if self.fd == 0:
            self.fd = tellapic.tellapic_connect_to("localhost", 4455)

        if self.fd <= 0:
            return 0

        stream = tellapic.tellapic_read_stream_b(self.fd);
        self.updateUi(stream)


    @QtCore.pyqtSlot(int)
    def on_tabWidget_currentChanged(self, i):
        if i == 0:
            self.ui.headerCByte.setCurrentIndex(16)
        elif i == 1:
            self.ui.headerCByte.setCurrentIndex(0)
        else:
            self.ui.headerCByte.setCurrentIndex(2)


    @QtCore.pyqtSlot(int)
    def on_recieveTabWidget_currentChanged(self, i):
        if i == 0:
            self.ui.receiveHeaderCByte.setCurrentIndex(16)
        elif i == 1:
            self.ui.receiveHeaderCByte.setCurrentIndex(0)
        else:
            self.ui.receiveHeaderCByte.setCurrentIndex(2)


    @QtCore.pyqtSlot(int)
    def on_headerCByte_activated(self, i):

        if i >= 0 and i <= 1:
            self.ui.tabWidget.setEnabled(True)
            self.ui.tabWidget.setCurrentIndex(1)
            if i == 0:
                self.ui.chatIdTo.setEnabled(False)
            else:
                self.ui.chatIdTo.setEnabled(True)

        elif i >= 2 and i <= 3 :
            self.ui.tabWidget.setEnabled(True)
            self.ui.tabWidget.setCurrentIndex(2)

        elif i > 3 and i < 19:
            self.ui.tabWidget.setEnabled(True)
            self.ui.tabWidget.setCurrentIndex(0)

        else:
            self.ui.tabWidget.setEnabled(False)


    @QtCore.pyqtSlot(int)
    def on_receiveHeaderCByte_activated(self, i):

        if i >= 0 and i <= 1:
            self.ui.receiveTabWidget.setCurrentIndex(1)

        elif i >= 2 and i <= 3 :
            self.ui.receiveTabWidget.setCurrentIndex(2)

        else:
            self.ui.receiveTabWidget.setCurrentIndex(0)
        
    
    def updateUi(self):
        #print("updateUi()")
        while self.queue.qsize():
            try:
                msg = self.queue.get(0)
                print("trying read...")
                stream = tellapic.tellapic_read_stream_b(self.fd)
                print("stream cbyte read: ", stream.header.cbyte, "size: ", stream.header.ssize)
                self.ui.receiveHeaderEndian.setChecked(False)
                self.ui.receiveHeaderSSize.setText(str(stream.header.ssize))
                self.ui.receiveHeaderCByte.setCurrentIndex(self.ui.receiveHeaderCByte.findText(self.cbyte[stream.header.cbyte]))
                self.on_receiveHeaderCByte_activated(self.ui.receiveHeaderCByte.currentIndex())
                
                if stream.header.cbyte in self.ctl:
                    self.ui.receiveSvcontrolIdFrom.setText(str(stream.data.control.idfrom))
                    
                elif stream.header.cbyte in self.ctli:
                    self.ui.receiveSvcontrolIdFrom.setText(str(stream.data.control.idfrom))
                    self.ui.receiveSvcontrolInfo.appendPlainText(str(stream.data.control.info))
                    
                elif stream.header.cbyte in self.ctlchat:
                    self.ui.receiveChatIdFrom.setText(str(stream.data.chat.idfrom))
                    if stream.header.cbyte == tellapic.CTL_CL_BMSG:
                        self.ui.receiveChatText.setText(str(stream.data.chat.type.text))
                    else:
                        self.ui.receiveChatIdTo.setText(str(stream.data.chat.type.private.idto))
                        self.ui.receiveChatText.appendPlaintText(str(stream.data.chat.type.private.text))
                        
                elif stream.header.cbyte in self.ctldrawing:
                    if stream.header.cbyte == tellapic.CTL_CL_FIG:
                        self.ui.drawingDCByte.setText(str(stream.data.drawing.dcbyte))
                        self.ui.drawingNumber.setText(str(stream.data.drawing.number))
                        self.ui.drawingWidth.setText(str(stream.data.drawing.width))
                        self.ui.drawingOpacity.setText(str(stream.data.drawing.opacity))
                        self.ui.drawingColor.setText(str(stream.data.drawing.color.red) + str(stream.data.drawing.color.green) + str(stream.data.drawing.color.blue))
                        self.ui.drawingXCoordinate.setText(str(stream.data.drawing.point1.x))
                        self.ui.drawingYCoordinate.setText(str(stream.data.drawing.point1.y))
                        self.ui.figureEndCaps.setText(str(stream.data.drawing.type.figure.endcaps))
                        self.ui.figureLineJoin.setText(str(stream.data.drawing.type.figure.linejoin))
                        self.ui.figureMiterLimit.setText(str(stream.data.drawing.type.figure.miterlimit))
                        self.ui.figureDashPhase.setText(str(stream.data.drawing.type.figure.dash_phase))
                        self.ui.figureEndXCoordinate.setText(str(stream.data.drawing.type.figure.point2.x))
                        self.ui.figureEndYCoordinate.setText(str(stream.data.drawing.type.figure.point2.y))
                    else:
                        pass

                self.queue.task_done()

            except Queue.Empty:
                pass


# stolen from: http://www.informit.com/articles/article.aspx?p=30708&seqNum=3
class ThreadClient:
    def __init__(self):
        # Create the queue
        self.queue = Queue.Queue()
        self.fd = tellapic.tellapic_connect_to("localhost", 4455)

        # Set up the GUI part
        self.gui = MainTest(self.queue, self.endApplication, self.fd)
        self.gui.show()

        # A timer to periodically call periodicCall :-)
        self.timer = QtCore.QTimer()
        QtCore.QObject.connect(self.timer, QtCore.SIGNAL("timeout()"), self.periodicCall)

        # Start the timer -- this replaces the initial call 
        # to periodicCall
        self.timer.start(100)

        # Set up the thread to do asynchronous I/O
        # More can be made if necessary
        self.running = 1
        self.thread1 = threading.Thread(target=self.workerThread1)
        self.thread1.start()


    def periodicCall(self):
        """
        Check every 100 ms if there is something new in the queue.
        """
        self.gui.updateUi()
        if not self.running:
            root.quit()
      
    def endApplication(self):
        while self.queue.qsize():
            self.queue.get(0)
            self.queue.task_done()
        self.running = 0
        tellapic.tellapic_close_fd(self.fd)
        print("ending thread")
        os.kill(os.getpid(), signal.SIGTERM)

    def workerThread1(self):
        """
        This is where we handle the asynchronous I/O. For example, 
        it may be a 'select()'.
        One important thing to remember is that the thread has to 
        yield control.
        """
        while self.running:
            # To simulate asynchronous I/O, we create a random number
            # at random intervals. Replace the following 2 lines 
            # with the real thing.
            try:
                print("waiting on select")
                r, w, e = select.select([self.fd], [], [])
                self.queue.put(1)
                self.queue.join()
            except select.error, v:
                print("error")
                raise
                break
            


if __name__ == "__main__":
    import sys
    root = QtGui.QApplication(sys.argv)
    client = ThreadClient()
    #ui = MainTest()
    #ui.show()
    sys.exit(root.exec_())


