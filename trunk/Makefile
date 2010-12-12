CC       = gcc
BUILDDIR = build/
ABSBUILDDIR = /home/seba/UNIVERSIDAD/TrabajoFinal/tellapic/build
SRCDIR   = src/
LIBDIR   = bench/swig
INCDIR   = include/
CFLAGS   = -ggdb -I$(INCDIR)


server: list.o libtellapic.so common.o console.o
	$(CC)  $(CFLAGS) -lpthread -L$(ABSBUILDDIR) $(SRCDIR)/server/server.c -ltellapic $(BUILDDIR)/console.o $(BUILDDIR)/list.o $(BUILDDIR)/common.o -o $(BUILDDIR)/$@  -lcurses -lcdk

libtellapic.so: tellapic.o
	$(CC)  $(CFLAGS) -shared -Wl,-rpath,$(ABSBUILDDIR) -Wl,-soname,$@ -o $(BUILDDIR)/$@ -lc $(BUILDDIR)/tellapic.o

tellapic.o: 
	$(CC)  $(CFLAGS) -Wall -fPIC -c $(LIBDIR)/tellapic.c -o $(BUILDDIR)/$@

list.o:
	$(CC) $(CFLAGS) $(SRCDIR)/common/list.c -c -o $(BUILDDIR)/$@

common.o:
	$(CC) $(CFLAGS) $(SRCDIR)/common/common.c -c -o $(BUILDDIR)/$@

console.o:
	$(CC) $(CFLAGS) $(SRCDIR)/common/console.c -c -o $(BUILDDIR)/$@ -lcurses -lcdk 

clean:
	rm $(BUILDDIR)/*.o $(BUILDDIR)/server $(BUILDDIR)/lib*