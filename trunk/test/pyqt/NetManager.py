'''
Net Manager

@author Sebastian Treu
'''
from PyQt4.QtCore import *
from PyQt4.QtGui import *
from PyQt4 import *
import select
from Drawing import *
import pytellapic
import signal, time
from Utils import MyEvent

class NetManager(QThread):
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
             pytellapic.CTL_SV_AUTHOK: 'CTL_SV_AUTHOK',
             pytellapic.CTL_FAIL : 'CTL_FAIL'}

    def __init__(self, receiver, host, port, user, pwd, model, parent = None):
        QThread.__init__(self, parent)
        self.model    = model
        self.receiver = receiver
        self.alive    = False
        self.socket   = pytellapic.tellapic_connect_to(host, port)
        if (pytellapic.tellapic_valid_socket(self.socket) != 0):
            self.alive = self.auth(user, pwd)
        #print("NetManager init: ", self.alive)

    def __end__(self):
        print("this is the NetManager end.")

    def sendString(self, string, cbyte, expected, clid = 0):
        pytellapic.tellapic_send_ctle(self.socket, clid, cbyte, string.__len__(), string)
        stream = pytellapic.tellapic_read_stream_b(self.socket)
        return (stream.header.cbyte == expected)

    def auth(self, user, pwd):
        stream = pytellapic.tellapic_read_stream_b(self.socket)
        r = (stream.header.cbyte == pytellapic.CTL_SV_ID)
        self.id = stream.data.control.idfrom
        if (r):
            r = self.sendString(pwd, pytellapic.CTL_CL_PWD, pytellapic.CTL_SV_PWDOK, self.id)
        if (r):
            r = self.sendString(user, pytellapic.CTL_CL_NAME, pytellapic.CTL_SV_AUTHOK, self.id)
        return r

    def run(self):
        #print("NetManager is running.")
        pytellapic.tellapic_send_ctl(self.socket, self.id, pytellapic.CTL_CL_FILEASK)
        while(self.alive):
            try:
                #print("waiting on select")
                r, w, e = select.select([self.socket.s_socket], [], [])
                stream = pytellapic.tellapic_read_stream_b(self.socket)
                #print("Event: ",self.cbyte[stream.header.cbyte])
                self.parseStream(stream)
            
            except select.error, v:
                print("error")
                raise
                break
            
    def parseStream(self, stream):
        if (pytellapic.tellapic_isfig(stream.header)):
            drawingData = stream.data.drawing
            # Is this an update of a drawn shape?
            shape = self.isShapeUpdate(drawingData)
            if (shape is not None):
                self.doShapeUpdate(shape, drawingData)
            else:
                shape = self.buildNewShape(stream)
            
            QtGui.QApplication.postEvent(self.receiver, MyEvent(shape))    
        elif (pytellapic.tellapic_isdrw(stream.header)):
            pass

        elif (pytellapic.tellapic_isfile(stream.header)):
            imageName = pytellapic.wrapToFile(stream.data.file, stream.header.ssize - pytellapic.HEADER_SIZE)
            QtGui.QApplication.postEvent(self.receiver, MyEvent(imageName, 1))

    def receive(self):
        stream = pytellapic.tellapic_read_stream_b(self.socket)
        string = self.cbyte[stream.header.cbyte]
        #print("Read: ",string)
        if (string == "CTL_FAIL"):
            self.alive = False
            
    def begin(self):
        self.start()

    def isShapeUpdate(self, ddata):
        return self.model.getDrawing(ddata.number)
        
    def editDrawingText(self, drawingText, drawingData):
        drawingText.setNumber(drawingData.number)
        drawingText.setText(drawingData.type.text.info)
        drawingText.setFont(drawingData.type.text.color, drawingData.type.text.style, drawingData.type.text.face, drawingData.width)
        drawingText.setBounds(drawingData.point1.x, drawingData.point1.y, drawingData.type.figure.point2.x, drawingData.type.figure.point2.y)
        drawingText.setWidth(drawingData.width)


    def editDrawingShape(self, drawingShape, drawingData):
        drawingShape.setNumber(drawingData.number)
        drawingShape.setWidth(drawingData.width)
        drawingShape.setFillColor(drawingData.fillcolor)
        drawingShape.setStrokeColor(drawingData.type.figure.color)
        drawingShape.setLineJoins(drawingData.type.figure.linejoin)
        drawingShape.setEndCaps(drawingData.type.figure.endcaps)
        drawingShape.setMiterLimit(drawingData.type.figure.miterlimit)
        drawingShape.setBounds(drawingData.point1.x, drawingData.point1.y, drawingData.type.figure.point2.x, drawingData.type.figure.point2.y)

    # Update the drawing shape or the drawing text
    def doShapeUpdate(self, shape, drawingData):
        #print("Do update")
        if (drawingData.dcbyte & pytellapic.TOOL_MASK == pytellapic.TOOL_EDIT_FIG):
            self.editDrawingShape(shape, drawingData)
        else:
            self.editDrawingText(shape, drawingData)


    # Creates a new shape upon the stream received
    def buildNewShape(self, stream):
        drawingData = stream.data.drawing
        tool        = drawingData.dcbyte & pytellapic.TOOL_MASK

        if (tool == pytellapic.TOOL_TEXT):
            shape = DrawingText()
            shape.setText(drawingData.type.text.info)
            shape.setFont(drawingData.type.text.color, drawingData.type.text.style, drawingData.type.text.face, drawingData.width)
        else:

            #rectangle.setDashStyle(drawingData.type.figure.dash_phase, drawingData.type.figure.dash_array)
            if (tool == pytellapic.TOOL_RECT):
                shape = DrawingShapeRectangle()

            elif (tool == pytellapic.TOOL_ELLIPSE):
                shape = DrawingShapeEllipse()

            elif (tool == pytellapic.TOOL_LINE):
                shape  = DrawingShapeLine()

            # figure data is common for all drawings except Text
            shape.setStrokeColor(drawingData.type.figure.color)
            shape.setLineJoins(drawingData.type.figure.linejoin)
            shape.setEndCaps(drawingData.type.figure.endcaps)
            shape.setMiterLimit(drawingData.type.figure.miterlimit)
        
        shape.setBounds(drawingData.point1.x, drawingData.point1.y, drawingData.type.figure.point2.x, drawingData.type.figure.point2.y)

        # ddata part is common for all figures, including text.
        shape.setNumber(drawingData.number)
        shape.setWidth(drawingData.width)
        shape.setFillColor(drawingData.fillcolor)

        return shape

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description='Connects to a tellapic server')
    parser.add_argument('-c', '--host', required=True, nargs=1, type=str, help='the host name to connect to.')
    parser.add_argument('-p', '--port', required=True, nargs=1, type=int, help='the HOST port to use.')
    parser.add_argument('-u', '--user', required=True, nargs=1, type=str, help='the USERNAME to use.')
    parser.add_argument('-P', '--password', required=True, nargs=1, type=str, help='the server PASSWORD.')
    args = parser.parse_args()
    #thread = NetManager(args.host[0], str(args.port[0]), args.user[0], args.password[0])

