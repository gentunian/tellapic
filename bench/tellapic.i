%module tellapic
%{
  
#include "/home/seba/UNIVERSIDAD/TrabajoFinal/tellapic/include/types.h"
#include "/home/seba/UNIVERSIDAD/TrabajoFinal/tellapic/include/constants.h"
  
  extern int       connect_to(const char *hostname, int port);
  extern header_t  read_header_b(int fd);
  extern stream_t  read_data_b(int fd, header_t header);
  extern void      close_fd(int fd);
  extern int       send_data(int socket, stream_t *stream);
  %}

%include "/home/seba/UNIVERSIDAD/TrabajoFinal/tellapic/include/types.h"
%include "/home/seba/UNIVERSIDAD/TrabajoFinal/tellapic/include/constants.h"
%pragma(java) jniclassclassmodifiers="class"

extern int       connect_to(const char *hostname, int port);
extern header_t  read_header_b(int fd);
extern stream_t  read_data_b(int fd, header_t header);
extern void      close_fd(int fd);
extern int       send_data(int socket, stream_t *stream);

