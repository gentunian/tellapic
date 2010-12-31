from PyQt4 import QtCore, QtGui
#from waiting import Ui_Waiting
from main import Ui_Main
import pytellapic
import select
import time
import Queue
import threading
import signal
import os

class MainTest(QtGui.QDialog):
    ctl = { pytellapic.CTL_CL_FILEASK   : 'CTL_CL_FILEASK',
            pytellapic.CTL_CL_FILEOK    : 'CTL_CL_FILEOK',
            pytellapic.CTL_SV_PWDFAIL   : 'CTL_SV_PWDFAIL',
            pytellapic.CTL_SV_PWDOK     : 'CTL_SV_PWDOK',
            pytellapic.CTL_SV_PWDASK    : 'CTL_SV_PWDASK',
            pytellapic.CTL_SV_CLIST     : 'CTL_SV_CLIST',
            pytellapic.CTL_SV_CLRM      : 'CTL_SV_CLRM',
            pytellapic.CTL_SV_ID        : 'CTL_SV_ID0',
            pytellapic.CTL_SV_NAMEINUSE : 'CTL_SV_NAMEINUSE' 
            }
    
    ctli = { pytellapic.CTL_SV_FILE  : 'CTL_SV_FILE',
             pytellapic.CTL_CL_PWD   : 'CTL_CL_PWD',
             pytellapic.CTL_CL_NAME  : 'CTL_CL_NAME', 
             pytellapic.CTL_SV_CLADD : 'CTL_SV_CLADD' 
             }
    
    ctlchat = { pytellapic.CTL_CL_BMSG : 'CTL_CL_BMSG', 
                pytellapic.CTL_CL_PMSG : 'CTL_CL_PMSG'
                }
    
    ctldrawing = { pytellapic.CTL_CL_FIG : 'CTL_CL_FIG',
                   pytellapic.CTL_CL_DRW : 'CTL_CL_DRW'
                   }

    cbyte = {pytellapic.CTL_CL_BMSG : 'CTL_CL_BMSG',
             pytellapic.CTL_CL_PMSG : 'CTL_CL_PMSG',
             pytellapic.CTL_CL_FIG  : 'CTL_CL_FIG',
             pytellapic.CTL_CL_DRW : 'CTL_CL_DRW',
             pytellapic.CTL_CL_CLIST: 'CTL_CL_CLIST', 
             pytellapic.CTL_CL_PWD: 'CTL_CL_PWD',
             pytellapic.CTL_CL_FILEASK: 'CTL_CL_FILEASK',
             pytellapic.CTL_CL_FILEOK: 'CTL_CL_FILEOK', 
             pytellapic.CTL_CL_DISC: 'CTL_CL_DISC', 
             pytellapic.CTL_CL_NAME:'CTL_CL_NAME',
             pytellapic.CTL_SV_CLRM:'CTL_SV_CLRM',
             pytellapic.CTL_SV_CLADD:'CTL_SV_CLADD',
             pytellapic.CTL_SV_CLIST:'CTL_SV_CLIST',
             pytellapic.CTL_SV_PWDASK:'CTL_SV_PWDASK',
             pytellapic.CTL_SV_PWDOK:'CTL_SV_PWDOK',
             pytellapic.CTL_SV_PWDFAIL:'CTL_SV_PWDFAIL',
             pytellapic.CTL_SV_FILE:'CTL_SV_FILE', 
             pytellapic.CTL_SV_ID:'CTL_SV_ID',
             pytellapic.CTL_SV_NAMEINUSE: 'CTL_SV_NAMEINUSE',
             pytellapic.CTL_FAIL : 'CTL_FAIL'}

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
            pytellapic.tellapic_send_ctl(self.fd, int(self.ui.svcontrolIdFrom.text()), value)

        elif value in self.ctli:
            pytellapic.tellapic_send_ctle(self.fd, int(self.ui.svcontrolIdFrom.text()), value, self.ui.svcontrolInfo.toPlainText().length(), str(self.ui.svcontrolInfo.toPlainText()))

        elif value in self.ctlchat:
            try:
                idto = int(self.ui.chatIdTo.text())
                pytellapic.tellapic_send_chatp(self.fd, int(self.ui.chatIdFrom.text()), idto, self.ui.chatText.toPlainText().size(), str(self.ui.chatText.toPlainText()))
            except:
                idto = 0
                pytellapic.tellapic_send_chatb(self.fd, int(self.ui.chatIdFrom.text()), self.ui.chatText.toPlainText().size(), str(self.ui.chatText.toPlainText()))

        elif value in self.ctldrawing:
            if value == pytellapic.CTL_CL_FIG:
                stream = pytellapic.stream_t()
                stream.header.endian = 0
                stream.header.cbyte = value
                stream.header.ssize = pytellapic.FIG_STREAM_SIZE
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
                pytellapic.tellapic_send(self.fd, stream)

        else:
            pass




    @QtCore.pyqtSlot()
    def on_exitButton_clicked(self):
        print("exiting gui...")
        pytellapic.tellapic_close_fd(self.fd)
        self.endcommand()



    @QtCore.pyqtSlot()
    def on_receiveButton_clicked(self):
        if self.fd == 0:
            self.fd = pytellapic.tellapic_connect_to("arg1v1.dyndns.org", 4455)

        if self.fd <= 0:
            return 0

        stream = pytellapic.tellapic_read_stream_b(self.fd);
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
                stream = pytellapic.tellapic_read_stream_b(self.fd)
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
                    if stream.header.cbyte == pytellapic.CTL_CL_BMSG:
                        self.ui.receiveChatText.appendPlainText(str(stream.data.chat.type.broadmsg))
                    else:
                        self.ui.receiveChatIdTo.setText(str(stream.data.chat.type.private.idto))
                        self.ui.receiveChatText.appendPlaintText(str(stream.data.chat.type.privmsg.text))
                        
                elif stream.header.cbyte in self.ctldrawing:
                    if stream.header.cbyte == pytellapic.CTL_CL_FIG:
                        self.ui.receiveDrawingDCByte.setText(str(stream.data.drawing.dcbyte))
                        self.ui.receiveDrawingNumber.setText(str(stream.data.drawing.number))
                        self.ui.receiveDrawingWidth.setText(str(stream.data.drawing.width))
                        self.ui.receiveDrawingOpacity.setText(str(stream.data.drawing.opacity))
                        self.ui.receiveDrawingColor.setText(str(stream.data.drawing.color.red) + str(stream.data.drawing.color.green) + str(stream.data.drawing.color.blue))
                        self.ui.receiveDrawingXCoordinate.setText(str(stream.data.drawing.point1.x))
                        self.ui.receiveDrawingYCoordinate.setText(str(stream.data.drawing.point1.y))
                        self.ui.receiveFigureEndCaps.setText(str(stream.data.drawing.type.figure.endcaps))
                        self.ui.receiveFigureLineJoin.setText(str(stream.data.drawing.type.figure.linejoin))
                        self.ui.receiveFigureMiterLimit.setText(str(stream.data.drawing.type.figure.miterlimit))
                        self.ui.receiveFigureDashPhase.setText(str(stream.data.drawing.type.figure.dash_phase))
                        self.ui.receiveFigureEndXCoordinate.setText(str(stream.data.drawing.type.figure.point2.x))
                        self.ui.receiveFigureEndYCoordinate.setText(str(stream.data.drawing.type.figure.point2.y))
                    else:
                        pass

                self.queue.task_done()

            except Queue.Empty:
                pass


# stolen from: http://www.informit.com/articles/article.aspx?p=30708&seqNum=3
class ThreadClient:
    def __init__(self, host, port):
        # Create the queue
        self.queue = Queue.Queue()
        self.fd = pytellapic.tellapic_connect_to(host, port)

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
        pytellapic.tellapic_close_fd(self.fd)
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
            


def usage():
    print("python2 main_test.py -h <hostname> -p <port>")
    print("or")
    print("python2 main_test.py --host=<hostname> --port=<port>")



if __name__ == "__main__":
    import sys
    import argparse

    parser = argparse.ArgumentParser(description='Connects to a tellapic server')
    parser.add_argument('-c', '--host', required=True, nargs=1, type=str, help='the host name to connect to.')
    parser.add_argument('-p', '--port', required=True, nargs=1, type=int, help='the HOST port to use.')
    args = parser.parse_args()
    root = QtGui.QApplication(sys.argv)
    client = ThreadClient(args.host[0], args.port[0])
    sys.exit(root.exec_())


