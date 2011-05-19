#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#define POSH_BUILDING_LIB
#include "tellapic/tellapic.h"


POSH_PUBLIC_API(size_t)
_read_nb(tellapic_socket_t socket, size_t totalbytes, byte_t *buf) 
{
  return 1;
}


POSH_PUBLIC_API(size_t)
_read_b(tellapic_socket_t socket, size_t totalbytes, byte_t *buf) 
{
  struct timeval tv;
  fd_set readset;
  size_t bytes;
  int r;

  /*
  FD_ZERO(&readset);
  FD_SET(socket.s_socket, &readset);
  tv.tv_sec = 1;

  r = select(0, &readset, NULL, NULL, &tv);
  if (r == 0 && socket.s_interrupt)
    bytes = 0;
  else
  */
  bytes = _read_s(socket.s_socket, totalbytes, buf);

  return bytes;
}


static size_t
_read_s(SOCKET socket, size_t totalbytes, byte_t *buf) 
{

  size_t bytesleft = totalbytes;
  size_t bytesread = 0;

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

  return bytesread;
}


POSH_PUBLIC_API(tellapic_socket_t)
tellapic_connect_to(const char *hostname, const char *port)
{
  tellapic_socket_t mysocket;
  /* From MSDN API DOC and Resources ... */
  WSADATA wsaData;
  int     iResult;
  struct addrinfo *result = NULL;
  struct addrinfo  *ptr = NULL;
  struct addrinfo  hints;
  
  mysocket.s_socket = INVALID_SOCKET;
  /* Initialize Winsock */
  iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
  if (iResult != 0)
    {
      printf("WSAStartup failed: %d\n", iResult);
      return mysocket;
    }

  ZeroMemory( &hints, sizeof(hints) );
  hints.ai_family = AF_UNSPEC;
  hints.ai_socktype = SOCK_STREAM;
  hints.ai_protocol = IPPROTO_TCP;

  /* Resolve the server address and port */
  iResult = getaddrinfo(hostname, port, &hints, &result);
  if (iResult != 0)
    {
      printf("getaddrinfo failed: %d\n", iResult);
      WSACleanup();
      return mysocket;
    }

  /* Attempt to connect to the first address returned by */
  /* the call to getaddrinfo */
  ptr=result;

  /* Create a SOCKET for connecting to server */
  mysocket.s_socket = socket(ptr->ai_family, ptr->ai_socktype, ptr->ai_protocol);

  if (mysocket.s_socket == INVALID_SOCKET)
    {
      printf("Error at socket(): %ld\n", WSAGetLastError());
      freeaddrinfo(result);
      WSACleanup();
      return mysocket;
    }

  /* Connect to server. */
  iResult = connect( mysocket.s_socket, ptr->ai_addr, (int)ptr->ai_addrlen);
  if (iResult == SOCKET_ERROR)
    {
      closesocket(mysocket.s_socket);
      mysocket.s_socket = INVALID_SOCKET;
    }

  freeaddrinfo(result);

  printf("INIT\n");

  if (mysocket.s_socket == INVALID_SOCKET)
    {
      printf("Unable to connect to server!\n");
      WSACleanup();
      return mysocket;
    }

  printf("Open socket: %d\n", mysocket.s_socket);
  mysocket.s_socket = mysocket.s_socket;
  mysocket.s_interrupt = 0;
  
  return mysocket;
}


POSH_PUBLIC_API(void)
tellapic_close_socket(tellapic_socket_t socket) 
{
  shutdown(socket.s_socket, SD_BOTH);
  closesocket(socket.s_socket);
  WSACleanup();
}


POSH_PUBLIC_API(void)
tellapic_interrupt_socket(tellapic_socket_t socket)
{
  /* This is a workaround to interrupt windows system call. */
  /* recv() function is not interrupted, but we use select() */
  /* with timeout. Every time select exits by a timeout, */
  /* the tellapic_socket_t interrupt flag is checked. */
  /* Ugly, but for now on I just don't know how to interrupt */
  /* recv() windows system call. */
  /* socket.s_interrupt = 1; */
}


POSH_PUBLIC_API(int)
tellapic_valid_socket(tellapic_socket_t socket)
{
  return (socket.s_socket != INVALID_SOCKET);
}
