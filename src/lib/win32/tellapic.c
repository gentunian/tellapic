#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif

#include <windows.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <iphlpapi.h>
#pragma comment(lib, "Ws2_32.lib")

#define POSH_BUILDING_LIB

#include "tellapic/tellapic.h"


/**
 * A wrapper from read() C function 
 */
POSH_PUBLIC_API(size_t)
_read_nb(tellapic_socket_t socket, size_t totalbytes, byte_t *buf) 
{
	return 1;

  //int    flag = fcntl(fd, F_GETFL);
  //size_t nbytes = 0;

  //fcntl(fd, F_SETFL, flag | O_NONBLOCK);

  //nbytes = read(fd, buf, totalbytes);

  //fcntl(fd, F_SETFL, flag);

  //return nbytes;
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
  /*int flags = fcntl(fd, F_GETFL);*/

  /* If fd has O_NONBLOCK set, unset it. THIS IS MANDATORY, that's what _b means. */
  /*if ((flags & O_NONBLOCK) == O_NONBLOCK)
    fcntl(fd, F_SETFL, flags & ~O_NONBLOCK);
*/
  /* Now we have a fd in blocking mode... */
  while(bytesleft > 0) 
    {
	  size_t bytes = recv(socket, buf + bytesread, bytesleft, 0);

      if (bytes > 0)
	{
	  bytesleft -= bytes;
	  bytesread += bytes;
	}
      else
	bytesleft = 0;
    }

  /* Restore the flags */
 /* fcntl(fd, F_SETFL, flags);*/

  return bytesread;
}


/**
 *
 */
POSH_PUBLIC_API(tellapic_socket_t)
tellapic_connect_to(const char *hostname, const char *port)
{
  /* From MSDN API DOC and Resources ... */
  SOCKET ConnectSocket = INVALID_SOCKET;
  WSADATA wsaData;
  int     iResult;
  struct addrinfo *result = NULL;
  struct addrinfo  *ptr = NULL;
  struct addrinfo  hints;

  printf("INIT\n");

  /* Initialize Winsock */
  iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
  if (iResult != 0) 
    {
      printf("WSAStartup failed: %d\n", iResult);
      return -1;
    }

  ZeroMemory( &hints, sizeof(hints) );
  hints.ai_family = AF_UNSPEC;
  hints.ai_socktype = SOCK_STREAM;
  hints.ai_protocol = IPPROTO_TCP;

  printf("INIT\n");

  /* Resolve the server address and port */
  iResult = getaddrinfo(hostname, port, &hints, &result);
  if (iResult != 0) 
    {
      printf("getaddrinfo failed: %d\n", iResult);
      WSACleanup();
      return -1;
    }

  printf("INIT\n");

  /* Attempt to connect to the first address returned by */
  /* the call to getaddrinfo */
  ptr=result;

  /* Create a SOCKET for connecting to server */
  ConnectSocket = socket(ptr->ai_family, ptr->ai_socktype, ptr->ai_protocol);

  printf("INIT\n");

  if (ConnectSocket == INVALID_SOCKET) 
    {
      printf("Error at socket(): %ld\n", WSAGetLastError());
      freeaddrinfo(result);
      WSACleanup();
      return -1;
    }

  /* Connect to server. */
  iResult = connect( ConnectSocket, ptr->ai_addr, (int)ptr->ai_addrlen);
  if (iResult == SOCKET_ERROR) 
    {
      closesocket(ConnectSocket);
      ConnectSocket = INVALID_SOCKET;
    }

  printf("INIT\n");

  /* Should really try the next address returned by getaddrinfo */
  /* if the connect call failed */
  /* But for this simple example we just free the resources */
  /* returned by getaddrinfo and print an error message */

  freeaddrinfo(result);

  printf("INIT\n");

  if (ConnectSocket == INVALID_SOCKET) 
    {
      printf("Unable to connect to server!\n");
      WSACleanup();
      return -1;
    }

  printf("Open socket: %d\n", ConnectSocket);
  return ConnectSocket;
}

