#ifndef _STREAM_H_
#define _STREAM_H_

#include <stdarg.h>

#define PWDLEN 32

typedef unsigned char byte_t;

/**********************************************************
 * CTL means control byte                                
 * CL means that message is from client                  
 * SV means that message is from server                  
 * Example:                                              
 *                                                       
 * CTL_SV_IDLIST will be the control byte of a message   
 * from the server to a client reporting the list of the 
 * connected clients.                                    
 * CTL_CL_FILE will be the control byte of a message from
 * the client to the server asking for a file            
 **********************************************************/
static const byte_t CTL_SV_IDLIST = 1;
static const byte_t CTL_CL_DISC   = 2;
static const byte_t CTL_SV_RMCL   = 3;
static const byte_t CTL_SV_ADDCL  = 4;
static const byte_t CTL_CL_FILE   = 5;
static const byte_t CTL_CL_PWD    = 9;
static const byte_t CTL_SV_ASKPWD = 10;

byte_t           *stream_build(byte_t ctlbyte, ...);
byte_t           *stream_get_pwd(byte_t *stream, int size, int id);
#endif
