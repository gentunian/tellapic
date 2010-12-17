from PyQt4 import QtCore, QtGui
from waiting import Ui_Waiting
from main import Ui_Main
import tellapic
import threading


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
    id = 0
    fd = 0
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

    def __init__(self):
        QtGui.QDialog.__init__(self)

        # Set up the user interface from Designer.
        self.ui = Ui_Main()
        self.ui.setupUi(self)
        self.setModal(False)

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
            stream = tellapic.tellapic_build_control(value, int(self.ui.svcontrolIdFrom.text()), None)

        elif value in self.ctli:
            stream = tellapic.tellapic_build_control(value, int(self.ui.svcontrolIdFrom.text()), str(self.ui.svcontrolInfo.toPlainText()))

        elif value in self.ctlchat:
            stream = tellapic.tellapic_build_chat(value, int(self.ui.chatIdFrom.text()), int(self.ui.chatIdTo.text()), str(self.ui.chatText.text()))

        elif value in self.ctldrawing:
            if value == tellapic.CTL_CL_FIG:
                stream = tellapic.stream_t()
                stream.header.endian = 0
                stream.header.cbyte = value
                stream.header.ssize = 41
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

        tellapic.tellapic_send_data(self.fd, stream)


    @QtCore.pyqtSlot()
    def on_exitButton_clicked(self):
        if self.fd != 0:
            tellapic.tellapic_close_fd(self.fd)


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
        
    
    def updateUi(self, stream):
        self.ui.receiveHeaderEndian.setChecked(False)
        self.ui.receiveHeaderSSize.setText(str(stream.header.ssize))
        self.ui.receiveHeaderCByte.setCurrentIndex(self.ui.receiveHeaderCByte.findText(self.cbyte[stream.header.cbyte]))
        self.on_receiveHeaderCByte_activated(self.ui.receiveHeaderCByte.currentIndex())
        
        if stream.header.cbyte in self.ctl:
            self.ui.receiveSvcontrolIdFrom.setText(str(stream.data.control.idfrom))
            
        elif stream.header.cbyte in self.ctli:
            self.ui.receiveSvcontrolIdFrom.setText(str(stream.data.control.idfrom))
            self.ui.receiveSvcontrolInfo.setText(str(stream.data.control.info))
            
        elif stream.header.cbyte in self.ctlchat:
            self.ui.receiveChatIdFrom.setText(str(stream.data.chat.idfrom))
            if stream.header.cbyte == tellapic.CTL_CL_BMSG:
                self.ui.receiveChatText.setText(str(stream.data.chat.type.text))
            else:
                self.ui.receiveChatIdTo.setText(str(stream.data.chat.type.private.idto))
                self.ui.receiveChatText.setText(str(stream.data.chat.type.private.text))
                
        elif stream.header.cbyte in self.ctldrawing:
            if stream.header.cbyte == tellapic.CTL_CL_FIG:
                self.ui.drawingDCByte.setText(str(stream.data.drawing.dcbyte))
                self.ui.drawingNumber.setText(str(stream.data.drawing.dnumber))
                self.ui.drawingWidth.setText(stream.data.drawing.width)
                self.ui.drawingOpacity.setText(stream.data.drawing.opacity)
                self.ui.drawingColor.setText(str(stream.data.drawing.color.red) + str(stream.data.drawing.color.green) + str(stream.data.drawing.color.blue))
                self.ui.drawingXCoordinate.text(str(stream.data.drawing.point1.x))
                self.ui.drawingYCoordinate.text(str(stream.data.drawing.point1.y))
                self.ui.figureEndCaps.text(str(stream.data.drawing.type.figure.endcaps))
                self.ui.figureLineJoin.text(str(stream.data.drawing.type.figure.linejoin))
                self.ui.figureMiterLimit.text(str(stream.data.drawing.type.figure.miterlimit))
                self.ui.figureDashPhase.text(str(stream.data.drawing.type.figure.dash_phase))
                self.ui.figureEndXCoordinate.text(str(stream.data.drawing.type.figure.point2.x))
                self.ui.figureEndYCoordinate.text(str(stream.data.drawing.type.figure.point2.y))
            else:
                pass
            

class MyReader(QtCore.QThread):

    def __init__(self,  mdialog):
        QtCore.QThread.__init__(self)
        self.exiting = False
        self.dialog = mdialog
        self.ui = self.dialog.ui
        self.fd = tellapic.tellapic_connect_to("localhost", 4455)

    def __del__(self):
        self.exiting = True
        self.wait()


    def setfd(self, fd):
        print("setting fd to: ", fd)
        self.fd = fd
        self.start()


    
    def run(self):
        if self.fd != 0:
            while not self.exiting:
                print("running with fd: ", self.fd)
                stream = tellapic.tellapic_read_stream_b(self.fd)
                #self.dialog.setStream(stream)
                #self.emit(QtCore.SIGNAL("output()"))
                print("Stream read")

        else:
            return 0

if __name__ == "__main__":
    import sys
    app = QtGui.QApplication(sys.argv)
    ui = MainTest()
    ui.show() 
    sys.exit(app.exec_())
