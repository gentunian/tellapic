
from PyQt4 import QtCore, QtGui
from PyQt4.QtGui import *
import rsrc_rc
import cmath
import math

class ColorWidget(QtGui.QWidget):
    """
    ColorWidget class
    @author Sebastian Treu
    
    ColorWidget is a QWidget that manages the selection of RGBA colors with ColorWheel class and a QSlider.

    Everytiime a color has been choosen, it will trigger a colorChanged() signal. It also shows in a QLabel
    the custom color choosen and also being selecting.
    """

    # colorChanged() signal used to indicate color selection.
    colorChanged = QtCore.pyqtSignal(QtGui.QColor)

    def __init__(self, parent = None):
        super(ColorWidget, self).__init__(parent)
        self.colorWheel   = ColorWidget.ColorWheel(self)
        self.colorDisplay = QLabel(self)
        self.alphaSlider  = QSlider(self)
        self.color = self.colorWheel.getColor()
        self.configureAlphaSlider()
        self.configureColorDisplay()
        self.configureLayout()
        self.configureSignals()

    def configureColorDisplay(self):
        """ Sets the displayed color label properties. """
        self.colorDisplay.setAutoFillBackground(True)
        self.colorDisplay.setStyleSheet("QLabel { background-color: rgb(100, 20, 200);")
        
    def configureLayout(self):
        """ Configures the layout of this widget. """
        colorLabel = QtGui.QLabel("Selected Color:")
        alphaLabel = QtGui.QLabel("Transparency:")
        self.horizontalLayout = QtGui.QHBoxLayout()
        self.formLayout       = QtGui.QFormLayout()
        self.layout           = QtGui.QVBoxLayout(self)
        self.horizontalLayout.addWidget(self.colorWheel)
        self.formLayout.setLabelAlignment(QtCore.Qt.AlignRight)
        self.formLayout.setHorizontalSpacing(22)
        self.formLayout.setVerticalSpacing(9)
        self.formLayout.setWidget(0, QtGui.QFormLayout.LabelRole, alphaLabel)
        self.formLayout.setWidget(0, QtGui.QFormLayout.FieldRole, self.alphaSlider)
        self.formLayout.setWidget(1, QtGui.QFormLayout.LabelRole, colorLabel)
        self.formLayout.setWidget(1, QtGui.QFormLayout.FieldRole, self.colorDisplay)
        self.formLayout.setHorizontalSpacing(22)
        self.layout.addLayout(self.horizontalLayout)
        self.layout.addLayout(self.formLayout)
        self.layout.setContentsMargins(9, 0, 9, 5)

    def configureAlphaSlider(self):
        """ Sets the transparency slider properties. """
        self.alphaSlider.setOrientation(QtCore.Qt.Horizontal)
        self.alphaSlider.setRange(0, 255)
        self.alphaSlider.setSingleStep(1)
        self.alphaSlider.setTracking(True)
        self.alphaSlider.setValue(255)

    def configureSignals(self):
        """ Sets the appropiate signal management. """
        self.alphaSlider.valueChanged.connect(self.alphaIsChanging)
        self.alphaSlider.sliderReleased.connect(self.alphaChanged)
        self.colorWheel.colorIsAdjusting.connect(self.setDisplayLabelBackground)
        self.colorWheel.colorChanged.connect(self.arrangeColor)

    def setColor(self, color):
        self.color = color
        self.color.setAlpha(self.alphaSlider.value())

    def arrangeColor(self, color):
        self.setColor(color)
        self.colorChanged.emit(self.color)

    def setDisplayLabelBackground(self, color):
        self.setColor(color)
        values = "{r}, {g}, {b}, {a}".format(r = self.color.red(), 
                                             g = self.color.green(), 
                                             b = self.color.blue(), 
                                             a = self.alphaSlider.value()
                                             )
        self.colorDisplay.setStyleSheet("QLabel { background-color: rgba("+values+"); }")
        self.colorDisplay.setToolTip(self.color.name()+hex(self.color.alpha())[2:])

    def alphaChanged(self):
        self.color.setAlpha(self.alphaSlider.value())
        self.colorChanged.emit(self.color)

    def alphaIsChanging(self, alpha):
        self.alpha = alpha
        self.setDisplayLabelBackground(self.color)
        if self.alphaSlider.isSliderDown() is False:
            self.color.setAlpha(self.alphaSlider.value())
            self.colorChanged.emit(self.color)



    class ColorWheel(QtGui.QFrame):
        """ 
        ColorWheel class.
	@author Sebastian Treu
        
        ColorWheel is a QFrame displaying a wheel with a color range and, a square box with a gradient of the 
        wheel selected color. This class only manages the selection of OPAQUE colors, that is, RGB colors
        with an alpha value of 255.

        When a new color is definetily choosen, it will fire colorChanged() signal. When a color is being
        choosen, it will fire a colorIsAdjusting() signal.
	"""	

        __pyqtSignals__ = ("colorChanged(QColor)", "colorIsAdjusting(QColor)")

        OuterWheelRadius = 94
        InnerWheelRadius = 74
        RestrictedRadius = InnerWheelRadius + (OuterWheelRadius - InnerWheelRadius) / 2
        WheelWidth = WheelHeight = 195
        MaskWidth  = MaskHeight  = 101
        MaskX = MaskY = 47
        MarkerXOffset = MarkerYOffset = 8
        NoAreaDrag    = 0
        MaskAreaDrag  = 1
        WheelAreaDrag = 2

        def __init__(self, parent = None):
            super(ColorWidget.ColorWheel, self).__init__(parent)
            self.wheelImage  = QtGui.QImage(":/images/resources/images/wheel.png")
            self.wheelPixmap = QtGui.QPixmap.fromImage(self.wheelImage)
            self.maskImage   = QtGui.QImage(":/images/resources/images/mask.png")
            self.maskPixmap  = QtGui.QPixmap.fromImage(self.maskImage)
            self.whatever    = QtGui.QPixmap(self.maskPixmap.width(), self.maskPixmap.height())
            self.marker      = QtGui.QPixmap(":/images/resources/images/marker.png")
            self.setMinimumSize(self.WheelWidth, self.WheelHeight)
            self.setMaximumSize(self.WheelWidth, self.WheelHeight)
            self.xOrigin = self.yOrigin = self.WheelWidth / 2
            self.wheelMarkerX  = self.xOrigin
            self.wheelMarkerY  = self.yOrigin - self.RestrictedRadius
            self.maskMarkerX = self.maskMarkerY = self.xOrigin
            self.maskBounds = QtCore.QRect(self.MaskX, self.MaskY, self.MaskWidth, self.MaskHeight)
            self.gradient = QColor(self.wheelImage.pixel(self.maskMarkerX, self.maskMarkerY))
            self.dragging = self.NoAreaDrag
            self.color = QColor()
            self.mousePressEvent(QtGui.QMouseEvent(QtCore.QEvent.MouseButtonPress,
                                                   QtCore.QPoint(self.wheelMarkerX,
                                                                 self.wheelMarkerY
                                                                 ),
                                                   QtCore.Qt.LeftButton,
                                                   QtCore.Qt.LeftButton,
                                                   QtCore.Qt.NoModifier
                                                   )
                                 )
            self.colorChanged.emit(self.color)

        # A simple getter function
        def getColor(self):
            return self.color

        # Do the painting in the paint event
        def paintEvent(self, event):
            offScreen = QPainter()
            offScreen.begin(self.whatever)
            offScreen.fillRect(0, 0, self.MaskWidth, self.MaskHeight, self.gradient)
            offScreen.drawPixmap(0, 0, self.maskPixmap)
            offScreen.end()
            onScreen = QPainter()
            onScreen.begin(self)
            onScreen.drawPixmap(0, 0, self.wheelPixmap)
            onScreen.drawPixmap(self.MaskX, self.MaskY, self.whatever)
            onScreen.drawPixmap(self.wheelMarkerX - self.MarkerXOffset, self.wheelMarkerY - self.MarkerYOffset, self.marker)
            onScreen.drawPixmap(self.maskMarkerX - self.MarkerXOffset, self.maskMarkerY - self.MarkerYOffset, self.marker)
            onScreen.end()
            if self.dragging:
                self.grabColor()
                self.colorIsAdjusting.emit(self.color)
                
        # 
        def grabColor(self):
            #grabbedImage = QtGui.QPixmap.grabWindow(self.winId(), self.MaskX, self.MaskY, self.MaskWidth, self.MaskHeight).toImage()
            grabbedImage = self.whatever.toImage()
            color = QColor(grabbedImage.pixel(self.maskMarkerX - self.MaskX, self.maskMarkerY - self.MaskY))
            self.color = QColor(color.red(), color.green(), color.blue())

        # Initiate the drag event if we are using the left mouse button and set the correct coordinates
        # for the mask and wheel markers respectively
        def mousePressEvent(self, event):
            x = event.x()
            y = event.y()
            if (event.button() == QtCore.Qt.LeftButton):
                if self.maskBounds.contains(x, y):
                    self.dragging = self.MaskAreaDrag
                    point = self.setMaskMarkerPoint(x, y)
                else:
                    point = self.setWheelMarkerPoint(x, y)
                    self.gradient = QColor(self.wheelImage.pixel(point[0], point[1]))
                    self.dragging = self.WheelAreaDrag
            self.update()

        # If we are dragging, update the coordinates
        def mouseMoveEvent(self, event):
            x = event.x()
            y = event.y()
            if self.dragging == self.MaskAreaDrag and self.maskBounds.contains(x, y):
                point = self.setMaskMarkerPoint(x, y)
            elif self.dragging == self.WheelAreaDrag:
                point = self.setWheelMarkerPoint(x, y)
                self.gradient = QColor(self.wheelImage.pixel(point[0], point[1]))
            self.update()
            
        # The release event will set the dragging flag to 0
        def mouseReleaseEvent(self, event):
            self.dragging = self.NoAreaDrag
            self.grabColor()
            self.colorChanged.emit(self.color)

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
