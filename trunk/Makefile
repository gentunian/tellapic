CC       = gcc
BUILDDIR = build
SRCDIR   = src
INCDIR   = include
CFLAGS   = -ggdb -I$(INCDIR)


server: list.o libstream.so
	$(CC) -lssl -lpthread -lstream -L$(BUILDDIR) $(SRCDIR)/server.c $(BUILDDIR)/list.o -o $(BUILDDIR)/$@

libstream.so: stream.o
	$(CC) -shared -Wl,-soname,$@ -o $(BUILDDIR)/$@ -lc $(BUILDDIR)/stream.o

stream.o: 
	$(CC) -Wall -fPIC -c $(SRCDIR)/stream.c -o $(BUILDDIR)/$@

list.o:
	$(CC) $(SRCDIR)/list.c -c -o $(BUILDDIR)/$@

clean:
	rm $(BUILDDIR)/*