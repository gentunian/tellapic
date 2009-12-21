#ifndef _STREAM_H_
#define _STREAM_H_

#include <stdarg.h>
#include "list.h"

#define PWDLEN 20

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
#define STREAM_IDX_LEN         1
#define STREAM_IDX_ID          5
#define STREAM_IDX_PWD         6
#define STREAM_IDX_FILE        6
#define STREAM_HEADER_SIZE     5
#define STREAM_END_BYTE        0
#define STREAM_FILEINFO_SIZE   7
#define STREAM_MIN_PACKET_SIZE 7
#define STREAM_ERR_SEQ         -1
#define STREAM_ERR             255
#define STREAM_ERR_BADHEADER   -99
#define CTL_SV_IDLIST        1
#define CTL_SV_FILE          2
#define CTL_SV_RMCL          3
#define CTL_SV_ADDCL         4
#define CTL_SV_ASKPWD        5
#define CTL_SV_PWDOK         6
#define CTL_SV_PWDFAIL       10
#define CTL_SV_FILEINFO      11
#define CTL_CL_FWD           38
#define CTL_CL_FILEINFOK     39
#define CTL_CL_DISC          40  
#define CTL_CL_ASKFILE       41 
#define CTL_CL_FILEOK        42
#define CTL_CL_PWD           43
#define LAST_CTL_BYTE        43
#define SEQ_LAST             4
#define SEQ_NEEDS_RETRY      3
#define SEQ_CANT_CONTINUE    2
#define SEQ_SV_OK            5
#define SEQ_CL_OK            6  
#define SEQ_ERR             -1
#define PSEQ_CONN            0
#define PSEQ_NULL            NULL
#define PSEQ_CONN_SIZE       3
#define PSEQ_CONN_0          0
#define PSEQ_CONN_1          1
#define PSEQ_CONN_2          2
#define PSEQ_CONN_3          3
#define PSEQ_CL              0
#define PSEQ_SV              1

typedef struct psequence {
  byte_t packet[2];
  byte_t *(*pbuilder)(void *arg);
  int     (*pchecker)(void *arg);
  list_t *pbargs;
  list_t *pcargs;

} psequence_t;


#define STREAM_IS_STREAM(stream, size)			 \
  (*stream > 0 && *stream <= LAST_CTL_BYTE && size >= STREAM_MIN_PACKET_SIZE && *(stream+size-1) == '\0')

#define STREAM_LENGTH(stream, size)			\
  ((stream[STREAM_IDX_LEN  ] & 0xff) << 24 |		\
   (stream[STREAM_IDX_LEN+1] & 0xff) << 16 |		\
   (stream[STREAM_IDX_LEN+2] & 0xff) <<  8 |		\
   (stream[STREAM_IDX_LEN+3] & 0xff)			\
   )

#define STREAM_END_IDX(stream, size)			 \
  if ( STREAM_IS_STREAM(stream, size))			 \
    return size-1;					 \
  else							 \
    return 0;						 

byte_t           *STREAM_build(byte_t ctlbyte, ...);
byte_t           *STREAM_build_from_list(byte_t ctlbyte, list_t *arglist, int *strbytes);
byte_t           *STREAM_get_pwd(byte_t *stream, int size, int id);
byte_t           STREAM_get_id(const byte_t *stream, int size);
byte_t           STREAM_get_ctlbyte(const byte_t *stream, int size);
int              STREAM_is_stream(const byte_t *stream, int size);
int              STREAM_get_filesize(byte_t *stream, int size);
int              STREAM_wants_fwd(byte_t *stream, int size);
void             PSEQ_cleanup(int seqtype, psequence_t *pseq);
psequence_t      *PSEQ_build(int seqtype, int (*checker [])(void *arg));
int              PSEQ_next_sequence(int seq, byte_t clpacket, byte_t svpacket, const psequence_t pseq[], byte_t **ostream, int *osbytes);

#endif
