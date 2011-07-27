from PyQt4 import QtGui

class P(QtGui.QTabWidget):
	def __init__(self, parent = None):
		QtGui.QTabWidget.__init__(parent)

if __name__ == "__main__":
	import sys
	app = QtGui.QApplication(sys.argv)
	m = P(0)
	m.show()
	sys.exit(app.exec_())
