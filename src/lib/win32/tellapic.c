#include <winsock.h>
#include <winsock2.h>
#pragma comment(lib, "Ws2_32.lib")
#include "tellapic/tellapic.h"


/**
 *
 */
POSH_PUBLIC_API(tellapic_socket_t)
tellapic_connect_to(const char *hostname, int port)
{
  /* From MSDN API DOC and Resources ... */
  WSADATA wsaData;
  int     iResult;

  // Initialize Winsock
  iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
  if (iResult != 0) 
    {
      printf("WSAStartup failed: %d\n", iResult);
      return -1;
    }

  struct addrinfo *result = NULL,
    *ptr = NULL,
    hints;

  ZeroMemory( &hints, sizeof(hints) );
  hints.ai_family = AF_UNSPEC;
  hints.ai_socktype = SOCK_STREAM;
  hints.ai_protocol = IPPROTO_TCP;

  // Resolve the server address and port
  iResult = getaddrinfo(hostname, port, &hints, &result);
  if (iResult != 0) 
    {
      printf("getaddrinfo failed: %d\n", iResult);
      WSACleanup();
      return -1;
    }

  SOCKET ConnectSocket = INVALID_SOCKET;

  // Attempt to connect to the first address returned by
  // the call to getaddrinfo
  ptr=result;

  // Create a SOCKET for connecting to server
  ConnectSocket = socket(ptr->ai_family, ptr->ai_socktype, ptr->ai_protocol);

  if (ConnectSocket == INVALID_SOCKET) 
    {
      printf("Error at socket(): %ld\n", WSAGetLastError());
      freeaddrinfo(result);
      WSACleanup();
      return -1;
    }

  // Connect to server.
  iResult = connect( ConnectSocket, ptr->ai_addr, (int)ptr->ai_addrlen);
  if (iResult == SOCKET_ERROR) 
    {
      closesocket(ConnectSocket);
      ConnectSocket = INVALID_SOCKET;
    }

  // Should really try the next address returned by getaddrinfo
  // if the connect call failed
  // But for this simple example we just free the resources
  // returned by getaddrinfo and print an error message

  freeaddrinfo(result);

  if (ConnectSocket == INVALID_SOCKET) 
    {
      printf("Unable to connect to server!\n");
      WSACleanup();
      return -1;
    }

  return ConnectSocket;
}

