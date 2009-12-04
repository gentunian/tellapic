#ifndef _STREAM_H_
#define _STREAM_H_

#include <stdarg.h>

#define PWDLEN 64

typedef unsigned char byte_t;

/**********************************************************
 * CTL means control byte                                
 * CL means that message is from client                  
 * SV means that message is from server                  
 * 
 * Example:                                              
 *                                                       
 * CTL_SV_IDLIST will be the control byte of a message   
 * from the server to a client reporting the list of the 
 * connected clients.                                    
 * CTL_CL_FILE will be the control byte of a message from
 * the client to the server asking for a file            
 **********************************************************/
#define STREAM_IDX_CTL         0
#define STREAM_IDX_ID          1
#define STREAM_IDX_PWD         2
#define STREAM_END_BYTE        0
#define STREAM_FILEINFO_SIZE   7

#define STREAM_MIN_PACKET_SIZE 3
#define STREAM_MAX_PACKET_SIZE 1024

#define STREAM_ERR_SEQ         -1
#define STREAM_ERR             255

#define CTL_SV_IDLIST  1
#define CTL_SV_FILE    2
#define CTL_SV_RMCL    3
#define CTL_SV_ADDCL   4
#define CTL_SV_ASKPWD  5
#define CTL_SV_PWDOK   6
#define CTL_SV_PWDFAIL 10
#define CTL_SV_FILEINFO 11

#define CTL_CL_FILEINFOK 39
#define CTL_CL_DISC    40  
#define CTL_CL_ASKFILE 41 
#define CTL_CL_FILEOK  42
#define CTL_CL_PWD     43

#define LAST_CTL_BYTE   43
#define SEQ_LAST             4
#define SEQ_NEEDS_RETRY      3
#define SEQ_CANT_CONTINUE    2
#define SEQ_OK               1
#define SEQ_ERR             -1
#define PSEQ_CONN            0
#define PSEQ_NULL            NULL
#define PSEQ_CONN_SIZE       4

typedef struct psequence {
  byte_t clpacket;
  byte_t svpacket;
  byte_t *(*pbuilder)(void *arg);
  int     (*pchecker)(void *arg);
  list_t *pbargs;
  list_t *pcargs;

} psequence_t;


#define STREAM_IS_STREAM(stream, size)			 \
  (*stream > 0 && *stream <= LAST_CTL_BYTE && size >= STREAM_MIN_PACKET_SIZE && size <= STREAM_MAX_PACKET_SIZE && *(stream+size-1) == '\0')

#define STREAM_END_IDX(stream, size)			 \
  if ( STREAM_IS_STREAM(stream, size))			 \
    return size-1;					 \
  else							 \
    return 0;						 

byte_t           *stream_build(byte_t ctlbyte, ...);
byte_t           *stream_build_from_list(byte_t ctlbyte, list_t *arglist, int *strbytes);
byte_t           *stream_get_pwd(byte_t *stream, int size, int id);
byte_t           stream_get_id(const byte_t *stream, int size);
byte_t           stream_get_ctlbyte(const byte_t *stream, int size);
int              stream_is_stream(const byte_t *stream, int size);
void             packet_sequence_cleanup(int seq, psequence_t *PSEQ);
psequence_t      *packet_sequence_build(int seq, int (*checker [])(void *arg));
int              next_sequence(int seq, byte_t clpacket, byte_t svpacket, const psequence_t PSEQ[], byte_t *ostream, int *osbytes);

#endif
