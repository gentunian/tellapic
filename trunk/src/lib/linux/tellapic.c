#include <sys/socket.h>
#include <fcntl.h>
#include <arpa/inet.h>            
#include <resolv.h>
#include <netdb.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include "tellapic/tellapic.h"

void signal_handler(int sig, siginfo_t *info, void *context);

/**
 * A wrapper from read() C function 
 */
POSH_PUBLIC_API(size_t)
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
POSH_PUBLIC_API(size_t)
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
tellapic_connect_to(const char *hostname, const char *port)
{
  struct sigaction   sig_action;
  sig_action.sa_handler = signal_handler;
  sigemptyset(&sig_action.sa_mask);
  sigprocmask(SIG_BLOCK, &sig_action.sa_mask, NULL);
  sig_action.sa_flags = SA_SIGINFO;
  sig_action.sa_flags &= ~SA_RESTART;
  //sigaction(SIGINT, &sig_action, NULL);
  sigaction(SIGALRM, &sig_action, NULL);

  int sd;

  struct hostent *host;
  struct sockaddr_in addr;
  
  if ( (host = gethostbyname(hostname)) == NULL )
    return -1;

  sd = socket(AF_INET, SOCK_STREAM, 0);

  bzero(&addr, sizeof(addr));

  addr.sin_family      = AF_INET;
  addr.sin_port        = htons(atoi(port));
  addr.sin_addr.s_addr = *(long*)(host->h_addr);

  if ( connect(sd, (struct sockaddr*)&addr, sizeof(addr)) != 0 ) 
    {
      close(sd);
      return -1;
    }

  return sd;
}


/**
 *
 */
POSH_PUBLIC_API(void)
tellapic_close_socket(tellapic_socket_t socket) 
{
  close(socket);
}

/**
 *
 */
POSH_PUBLIC_API(void)
tellapic_interrupt_socket()
{
  alarm(1);
}

void signal_handler(int sig, siginfo_t *info, void *context) 
{
  printf("Signal caught: %d\n", sig);
}
