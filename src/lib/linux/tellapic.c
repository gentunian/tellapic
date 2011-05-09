#include <sys/socket.h>
#include <fcntl.h>
#include <arpa/inet.h>            
#include <resolv.h>
#include <netdb.h>
#include "tellapic/tellapic.h"
#include <string.h>
#include <stdlib.h>


/**
 * A wrapper from read() C function 
 */
size_t
_read_nb(int fd, size_t totalbytes, byte_t *buf) 
{

  int    flag = fcntl(fd, F_GETFL);
  size_t nbytes = 0;

  fcntl(fd, F_SETFL, flag | O_NONBLOCK);

  nbytes = read(fd, buf, totalbytes);

  fcntl(fd, F_SETFL, flag);

  return nbytes;
}


/**
 * A wrapper from read() C function always in blocking mode. Returns the number of bytes
 * and places the read data in the output parameter buf.
 *
 * YOU SHOULD take care using this function and NOTICE that it will BLOCK your thread, and
 * also will set your file descriptor fd to blocking mode. So please, dont blame, if you
 * want non-blocking mode, write your own...
 */
size_t
_read_b(int fd, size_t totalbytes, byte_t *buf) 
{
  size_t bytesleft = totalbytes;
  size_t bytesread = 0;

  /* This function should be feeded with a file descriptor with the O_NONBLOCK flag unset. */
  /* The *_b means that this function WILL block until data arrives. */
  /* Get the fd flags. */
  int flags = fcntl(fd, F_GETFL);

  /* If fd has O_NONBLOCK set, unset it. THIS IS MANDATORY, that's what _b means. */
  if ((flags & O_NONBLOCK) == O_NONBLOCK)
    fcntl(fd, F_SETFL, flags & ~O_NONBLOCK);

  /* Now we have a fd in blocking mode... */
  while(bytesleft > 0) 
    {

      size_t bytes = read(fd, buf + bytesread, bytesleft);

      if (bytes > 0)
	{
	  bytesleft -= bytes;
	  bytesread += bytes;
	}
      else
	bytesleft = 0;
    }

  /* Restore the flags */
  fcntl(fd, F_SETFL, flags);

  return bytesread;
}


/**
 *
 */
POSH_PUBLIC_API(tellapic_socket_t)
tellapic_connect_to(const char *hostname, int port)
{
  int sd;

  struct hostent *host;
  struct sockaddr_in addr;
  
  if ( (host = gethostbyname(hostname)) == NULL )
    return -1;

  sd = socket(AF_INET, SOCK_STREAM, 0);

  bzero(&addr, sizeof(addr));

  addr.sin_family      = AF_INET;
  addr.sin_port        = htons(port);
  addr.sin_addr.s_addr = *(long*)(host->h_addr);

  if ( connect(sd, (struct sockaddr*)&addr, sizeof(addr)) != 0 ) 
    {
      close(sd);
      return -1;
    }

  return sd;
}
