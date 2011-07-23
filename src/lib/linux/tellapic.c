#include <sys/socket.h>
#include <fcntl.h>
#include <arpa/inet.h>            
#include <resolv.h>
#include <netdb.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include "tellapic/tellapic.h"


/**
 * A wrapper from read() C function 
 */
POSH_PUBLIC_API(size_t)
_read_nb(tellapic_socket_t socket, size_t totalbytes, byte_t *buf) 
{

  int    flag = fcntl(socket.s_socket, F_GETFL);
  size_t nbytes = 0;

  fcntl(socket.s_socket, F_SETFL, flag | O_NONBLOCK);

  nbytes = read(socket.s_socket, buf, totalbytes);

  fcntl(socket.s_socket, F_SETFL, flag);

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
POSH_PUBLIC_API(size_t)
_read_b(tellapic_socket_t socket, size_t totalbytes, byte_t *buf) 
{
  size_t bytesleft = totalbytes;
  size_t bytesread = 0;

  /* This function should be feeded with a file descriptor with the O_NONBLOCK flag unset. */
  /* The *_b means that this function WILL block until data arrives. */
  /* Get the fd flags. */
  int flags = fcntl(socket.s_socket, F_GETFL);

  /* If fd has O_NONBLOCK set, unset it. THIS IS MANDATORY, that's what _b means. */
  if ((flags & O_NONBLOCK) == O_NONBLOCK)
    fcntl(socket.s_socket, F_SETFL, flags & ~O_NONBLOCK);

  /* Now we have a fd in blocking mode... */
  while(bytesleft > 0) 
    {
      ssize_t bytes = read(socket.s_socket, buf + bytesread, bytesleft);

      if (bytes > 0)
	{
	  bytesleft -= bytes;
	  bytesread += bytes;
	}
      else
	{
	  bytesleft = 0;
	}
      fflush(stdout);
    }

  /* Restore the flags */
  fcntl(socket.s_socket, F_SETFL, flags);

  return bytesread;
}


/**
 *
 */
POSH_PUBLIC_API(tellapic_socket_t)
tellapic_connect_to(const char *hostname, const char *port)
{
  tellapic_socket_t  mysocket;
  struct hostent     *host;
  struct sockaddr_in addr;
  int sd;

  /* sig_action.sa_handler = signal_handler; */

  /* sigemptyset(&sig_action.sa_mask); */

  /* sigprocmask(SIG_SETMASK, &sig_action.sa_mask, NULL); */

  /* sig_action.sa_flags = 0; */

  /* sigaction(SIGINT, &sig_action, NULL); */

  mysocket.s_socket = -1;
  
  if ( (host = gethostbyname(hostname)) == NULL )
    return mysocket;

  sd = socket(AF_INET, SOCK_STREAM, 0);

  bzero(&addr, sizeof(addr));

  addr.sin_family      = AF_INET;
  addr.sin_port        = htons(atoi(port));
  addr.sin_addr.s_addr = *(long*)(host->h_addr);

  if ( connect(sd, (struct sockaddr*)&addr, sizeof(addr)) != 0 ) 
    {
      close(sd);
      return mysocket;
    }
  mysocket.s_socket = sd;
  return mysocket;
}


/**
 *
 */
POSH_PUBLIC_API(void)
tellapic_close_socket(tellapic_socket_t socket) 
{
  shutdown(socket.s_socket, SHUT_RDWR);
  close(socket.s_socket);
}

/**
 *
 */
POSH_PUBLIC_API(void)
tellapic_interrupt_socket(tellapic_socket_t socket)
{
  kill(getpid(), SIGINT);
}

POSH_PUBLIC_API(int)
tellapic_valid_socket(tellapic_socket_t socket)
{
  return (socket.s_socket > 0);
}
