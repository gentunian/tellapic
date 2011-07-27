from PyQt4 import QtCore, QtGui
from PyQt4.QtGui import *
import rsrc_rc
import cmath
import math

class ColorWidget(QtGui.QWidget):
    #__pyqtSignals__ = ("colorChanged(QColor)")
    colorChanged = QtCore.pyqtSignal(QtGui.QColor)

    def __init__(self, parent = None):
        super(ColorWidget, self).__init__(parent)
        self.colorWheel = ColorWidget.ColorWheel(self)
        self.colorLabel = ColorWidget.ColorLabel(self)
        self.alphaSlider = QSlider(self)
        self.alphaSlider.setOrientation(QtCore.Qt.Horizontal)
        self.alphaSlider.setRange(0, 255)
        self.alphaSlider.setSingleStep(1)
        self.alphaSlider.setValue(255)
        self.colorLabel.setAutoFillBackground(True)
        self.color = self.colorWheel.getColor()
        self.colorLabel.setBackgroundColor(self.color)
        self.layout = QtGui.QVBoxLayout(self)
        self.layout.addWidget(self.colorWheel)
        self.layout.addWidget(self.alphaSlider)
        self.layout.addWidget(self.colorLabel)
        QtCore.QObject.connect(self.alphaSlider, QtCore.SIGNAL("valueChanged(int)"), self.alphaSet)
        QtCore.QObject.connect(self.colorWheel, QtCore.SIGNAL("colorChanged(QColor)"), self.update)

    # Helper function for setting the colorLabel background color to color and emit the colorChanged signal
    def setColor(self, color):
        self.color = QColor(color.red(), color.green(), color.blue(), self.alphaSlider.value())
        self.colorLabel.setBackgroundColor(color)
        self.emit(QtCore.SIGNAL("colorChanged(QColor)"), self.color)

    @QtCore.pyqtSlot(QColor)
    def update(self, color):
        self.setColor(color)

    @QtCore.pyqtSlot(int)
    def alphaSet(self, alpha):
        self.setColor(self.color)

    # A label that can paint its background with translucent colors
    class ColorLabel(QtGui.QLabel):
        def __init__(self, parent = None):
            super(ColorWidget.ColorLabel, self).__init__(parent)
            self.color = QColor()

        def paintEvent(self, event):
            self.painter = QtGui.QPainter()
            self.painter.begin(self)
            size = self.size()
            self.painter.fillRect(0, 0, size.width(), size.height(), self.color)
            self.painter.end()

        def setBackgroundColor(self, color):
            self.color = color
            self.update()

    class ColorWheel(QtGui.QFrame):
        __pyqtSignals__ = ("colorChanged(QColor)")

        OuterWheelRadius = 94
        InnerWheelRadius = 74
        RestrictedRadius = InnerWheelRadius + (OuterWheelRadius - InnerWheelRadius) / 2
        WheelWidth = WheelHeight = 195
        MaskWidth  = MaskHeight  = 101
        MaskX = MaskY = 47
        MarkerXOffset = MarkerYOffset = 8

        def __init__(self, parent = None):
            super(ColorWidget.ColorWheel, self).__init__(parent)
            self.wheelImage  = QtGui.QImage(":/images/resources/images/wheel.png")
            self.wheelPixmap = QtGui.QPixmap.fromImage(self.wheelImage)
            self.maskImage   = QtGui.QImage(":/images/resources/images/mask.png")
            self.maskPixmap  = QtGui.QPixmap.fromImage(self.maskImage)
            self.marker      = QtGui.QPixmap(":/images/resources/images/marker.png")
            self.setMinimumSize(self.WheelWidth, self.WheelHeight)
            self.setMaximumSize(self.WheelWidth, self.WheelHeight)
            self.xOrigin = self.yOrigin = self.WheelWidth / 2
            self.wheelMarkerX  = self.xOrigin
            self.wheelMarkerY  = self.yOrigin - self.RestrictedRadius
            self.maskMarkerX = self.maskMarkerY = self.xOrigin
            self.maskBounds = QtCore.QRect(self.MaskX, self.MaskY, self.MaskWidth, self.MaskHeight)
            self.color = QColor()
            self.gradient = QColor(self.wheelImage.pixel(self.maskMarkerX, self.maskMarkerY))
            self.dragging = 0

        # A simple getter function
        def getColor(self):
            return self.color

        # Do the painting in the paint event
        def paintEvent(self, event):
            self.painter = QPainter()
            self.painter.begin(self)
            self.painter.drawPixmap(0, 0, self.wheelPixmap)
            # fill the square
            self.painter.fillRect(47, 47, 101, 101, self.gradient)
            self.painter.drawPixmap(self.MaskX, self.MaskY, self.maskPixmap)
            self.painter.drawPixmap(self.wheelMarkerX - self.MarkerXOffset, self.wheelMarkerY - self.MarkerYOffset, self.marker)
            self.painter.drawPixmap(self.maskMarkerX - self.MarkerXOffset, self.maskMarkerY - self.MarkerYOffset, self.marker)
            self.painter.end()
            if (self.dragging):
                grabbedImage = QtGui.QPixmap.grabWindow(self.winId(), self.MaskX, self.MaskY, self.MaskWidth, self.MaskHeight).toImage()
                self.color = QColor(grabbedImage.pixel(self.maskMarkerX - self.MaskX, self.maskMarkerY - self.MaskY))
                self.emit(QtCore.SIGNAL("colorChanged(QColor)"), self.color)

        # Initiate the drag event if we are using the left mouse button and set the correct coordinates
        # for the mask and wheel markers respectively
        def mousePressEvent(self, event):
            x = event.x()
            y = event.y()
            if (event.button() == QtCore.Qt.LeftButton):
                self.dragging = 1
                if self.maskBounds.contains(x, y):
                    point = self.setMaskMarkerPoint(x, y)
                else:
                    point = self.setWheelMarkerPoint(x, y)
                    self.gradient = QColor(self.wheelImage.pixel(point[0], point[1]))
            self.update()

        # If we are dragging, update the coordinates
        def mouseMoveEvent(self, event):
            x = event.x()
            y = event.y()
            if (self.dragging):
                if self.maskBounds.contains(x, y):
                    point = self.setMaskMarkerPoint(x, y)
                else:
                    point = self.setWheelMarkerPoint(x, y)
                    self.gradient = QColor(self.wheelImage.pixel(point[0], point[1]))
            self.update()
            
        # The release event will set the dragging flag to 0
        def mouseReleaseEvent(self, event):
            self.dragging = 0
            self.update()

        # Helper function that sets the current wheel marker coordinates
        def setWheelMarkerPoint(self, x, y):
            iNumber = -(self.xOrigin - x) + (y - self.yOrigin) * 1j
            angle = cmath.phase(iNumber)
            point = cmath.rect(self.RestrictedRadius, angle + math.pi)
            self.wheelMarkerX = self.xOrigin - point.real
            self.wheelMarkerY = self.yOrigin - point.imag
            return (self.wheelMarkerX, self.wheelMarkerY)

        # Helper function that sets the current mask marker coordinates
        def setMaskMarkerPoint(self, x, y):
            self.maskMarkerX = x
            self.maskMarkerY = y
            return (x,y)


if __name__ == "__main__":
    import sys
    app = QtGui.QApplication(sys.argv)
    main = QtGui.QMainWindow()
    color = ColorWidget(main)
    main.setCentralWidget(color)
    main.show()
    sys.exit(app.exec_())
