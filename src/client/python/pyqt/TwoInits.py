class A:
    def __init__(self):
        self.__m1()
    
    def __m1(self):
        print("m1 en A")

class B(A):
    def __init__(self):
        super(B, self).__init__()
        self.__m1()
        
    def __m1(self):
        print("m1 en B")
