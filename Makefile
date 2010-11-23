CC       = gcc
BUILDDIR = build
SRCDIR   = src
INCDIR   = include
CFLAGS   = -ggdb -I$(INCDIR)


server: list.o libstream.so common.o
	$(CC)  $(CFLAGS) -lssl -lpthread -lstream -L$(BUILDDIR) $(SRCDIR)/server.c $(BUILDDIR)/list.o $(BUILDDIR)/common.o -o $(BUILDDIR)/$@

libstream.so: stream.o
	$(CC)  $(CFLAGS) -shared -Wl,-soname,$@ -o $(BUILDDIR)/$@ -lc $(BUILDDIR)/stream.o

stream.o: 
	$(CC)  $(CFLAGS) -Wall -fPIC -c $(SRCDIR)/stream.c -o $(BUILDDIR)/$@

list.o:
	$(CC) $(CFLAGS) $(SRCDIR)/list.c -c -o $(BUILDDIR)/$@

common.o:
	$(CC) $(CFLAGS) $(SRCDIR)/common.c -c -o $(BUILDDIR)/$@
clean:
	rm $(BUILDDIR)/*.o $(BUILDDIR)/server $(BUILDDIR)/lib*