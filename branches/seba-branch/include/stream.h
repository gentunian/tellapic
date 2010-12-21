/*****************************************************************************
 *   Copyright (c) 2009 Sebastián Treu and Virginia Boscaro.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   Authors: 
 *         Sebastian Treu 
 *         Virginia Boscaro
 *
 *
 * ===========================================================================
 * 'stream.h' header file.
 *  ---------------------
 *
 * This is the header where some protocols functions and types are defined, as
 * well as constants and macros regarding to the data to be held by the stream.
 *
 *****************************************************************************/
#ifndef _STREAM_H_
#define _STREAM_H_

#include <stdarg.h>
#include "list.h"


/***************************/
/* a byte representation   */
/***************************/
typedef unsigned char byte_t;


/*******************************************/
/* an item from a list or queue containing */
/* the 'data' to store as bytes, how many  */
/* bytes this data has, and from who is it */
/*******************************************/
typedef struct stream_item {
  byte_t             *data;
  int                nbytes;
  unsigned short     from;
  unsigned short     pending;

} stream_item_t;


/**************************************************************************/
/* sequence row. Contains two functions. A checking function (pchecker)   */
/* and a builder function (pbuilder) with 'pbargs' and 'pcargs' arguments */
/* for both respectevely.                                                 */
/**************************************************************************/
typedef struct psequence {
  byte_t packet[2];
  byte_t *(*pbuilder)(void *arg);
  int     (*pchecker)(void *arg);
  list_t *pbargs;
  list_t *pcargs;

} psequence_t;



/************************************************************************************
 * CONSTANTS NAMES CONVENTIONS:
 * ---------------------------
 * o CTL means control byte.
 * o CL means that message is from client.
 * o SV means that message is from server.
 * o STREAM is used on streams.
 * o ERR means error.
 * o PSEQ means 'packet sequence' related.
 * o SEQ is for sequence.
 *
 * Example:                                              
 *                                                       
 * + CTL_SV_IDLIST will be the control byte of a message   
 * from the server to a client reporting the list of the 
 * connected clients.
 *
 * + CTL_CL_FILE will be the control byte of a message from
 * the client to the server asking for a file.
 *
 * + STREAM_ERR_SEQ is for an error on a sequence of streams
 *
 *
 * HEADER:
 * -------
 *                                               stream size
 *                            ________________________/\_________________________
 *                           /                                                   \
 * +------------+------------+------------+------------+------------+------------+
 * | endianness | ctl. byte  | size byte3 | size byte2 | size byte1 | size byte0 |
 * +------------+------------+------------+------------+------------+------------+
 *
 *
 *
 * CONTROL BYTE:
 * -------------
 *
 * +---------------------------------------+---------------------------------------+
 * |            H I G H  P A R T           |              L O W  P A R T           |
 * +---------+---------+---------+---------+---------+---------+---------+---------+
 * |  bit7   |   bit6  |   bit5  |   bit4  |   bit3  |   bit2  |   bit1  |   bit0  |
 * +---------+---------+---------+---------+---------+---------+---------+---------+
 *
 *
 * A STREAM:
 * --------
 *
 *           H E A D E R                             D A T A
 *  _____________/\_____________   _____________________/\_______________________
 * /                            \ /                                              \     null byte
 *   0    1    2    3    4    5    6    7    8    9    10   11   12   13   ...  N-1    /
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+ ... +----+----+
 * | EB | CB | SB3| SB2| SB1| SB0|    |    |    |    |    |    |    |    |     |    |    |
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+ ... +----+----+
 *
 * NOTE: The bits from a byte are numbered from rigth to left. The bytes from a stream 
 * are numbered from left to right.
 **********************************************************************************/


/*************************************************************/
/* define the start index of special bytes within the stream */
/*************************************************************/
#define STREAM_IDX_ENDIANESS   0      /* endianess byte index */
#define STREAM_IDX_CTL         1      /* control byte index  */
#define STREAM_IDX_LEN3        2      /* stream length byte 3 index */
#define STREAM_IDX_LEN2        3      /* stream length byte 2 index */
#define STREAM_IDX_LEN1        4      /* stream length byte 1 index */
#define STREAM_IDX_LEN0        5      /* stream length byte 0 index */
#define STREAM_IDX_ID          6      /* client id index     */
#define STREAM_IDX_PWD         7      /* pwd index?          */
#define STREAM_IDX_FILE        7      /* file index?         */


/**************************************/
/* define some special sizes required */
/**************************************/
#define STREAM_HEADER_SIZE     6      /* the header size is 6 bytes */
#define STREAM_FILEINFO_SIZE   7      /* 7 bytes for measuring the file size */
#define STREAM_MIN_PACKET_SIZE 8      /* minimun packet size is 8 bytes */
#define STREAM_MAX_PACKET_SIZE 5242880     /* maximun packet size is X bytes */
#define STREAM_PWD_SIZE        20     /* password length in bytes */

/**************************/
/* define error constants */
/**************************/
#define STREAM_ERR_SEQ        -1     /* stream sequence error */
#define STREAM_ERR           255     /* stream corrupted */
#define STREAM_ERR_BADHEADER -99     /* stream header corrupted */
#define SEQ_ERR               -1     /* sequence error */

/* ? */
#define STREAM_END_BYTE        0


/*******************************************/
/* define byte values for the control byte */
/*******************************************/
#define CTL_SV_IDLIST          1        /* server asks connected clients ids */
#define CTL_SV_FILE            2        /* server asks for a file to hold in memory */
#define CTL_SV_RMCL            3        /* server informs to remove a client */
#define CTL_SV_ADDCL           4        /* server informs to add a client */
#define CTL_SV_ASKPWD          5        /* server asks for password */
#define CTL_SV_PWDOK           6        /* server informs password is ok */
#define CTL_SV_PWDFAIL         10       /* server informs password is wrong */
#define CTL_SV_FILEINFO        11       /* server asks for file size */
#define CTL_CL_FWD             38       /* client .... ? */
#define CTL_CL_FILEINFOK       39       /* ... ? */
#define CTL_CL_DISC            40       /* client informs a normal disconnection */
#define CTL_CL_ASKFILE         41       /* client asks for a file */
#define CTL_CL_FILEOK          42       /* client inform file received ok */
#define CTL_CL_PWD             43       /* client sends a password */
#define LAST_CTL_BYTE          43       /* the las control byte is 43. Any greater number it's not a control byte */



/************************************************************************/
/* define values to validate a 'sequenced conversation with the server  */
/************************************************************************/
#define SEQ_LAST               4        /* ...? */
#define SEQ_NEEDS_RETRY        3        /* sequence needs to be repeated */
#define SEQ_CANT_CONTINUE      2        /* can't continue checking the sequence */
#define SEQ_OK                 5        /* sequence is ok */ 



/****************************************************************/
/* define table index values and some packet sequence constants */
/****************************************************************/
#define PSEQ_START           0        /* the first sequence of all */
#define PSEQ_HNDSHK          10       /* packet sequence that indicates a handshake sequence */
#define PSEQ_NULL            NULL     /* null value */
#define PSEQ_HNDSHK_SIZE     3        /* table size in rows for a handshake sequence */
#define PSEQ_CONN_0          0        /* row 0 */
#define PSEQ_CONN_1          1        /* row 1 */
#define PSEQ_CONN_2          2        /* row 2 */
#define PSEQ_CONN_3          3        /* ?? */
#define PSEQ_CL              0        /* packet sequence for client */
#define PSEQ_SV              1        /* packet sequence for server */



/***************************************************/
/* define values for events received in the stream */
/***************************************************/
#define CLICK_LEFT          1
#define CLICK_RIGHT         2
#define CLICK_MIDDLE        3
#define EVENT_PRESS         1
#define EVENT_DRAG          2
#define EVENT_RELEASE       3
#define EVENT_PRESS_L       ((EVENT_PRESS   << 2) + CLICK_LEFT)
#define EVENT_PRESS_R       ((EVENT_PRESS   << 2) + CLICK_RIGHT)
#define EVENT_PRESS_M       ((EVENT_PRESS   << 2) + CLICK_MIDDLE)
#define EVENT_DRAG_L        ((EVENT_DRAG    << 2) + CLICK_LEFT)
#define EVENT_DRAG_R        ((EVENT_DRAG    << 2) + CLICK_RIGHT)
#define EVENT_DRAG_M        ((EVENT_DRAG    << 2) + CLICK_MIDDLE)
#define EVENT_RELEASE_L     ((EVENT_RELEASE << 2) + CLICK_LEFT)
#define EVENT_RELEASE_R     ((EVENT_RELEASE << 2) + CLICK_RIGHT)
#define EVENT_RELEASE_M     ((EVENT_RELEASE << 2) + CLICK_MIDDLE)
#define EVENT_MASK          EVENT_RELEASE_M
#define EVENT_NOEVENT       -1




/***************************/
/* some macros definitions */
/***************************/
#define STREAM_LENGTH(stream)				\
  ((stream[STREAM_IDX_LEN3] & 0xff) << 24 |		\
   (stream[STREAM_IDX_LEN2] & 0xff) << 16 |		\
   (stream[STREAM_IDX_LEN1] & 0xff) <<  8 |		\
   (stream[STREAM_IDX_LEN0] & 0xff)			\
   )


/**********************/
/* functions profiles */
/**********************/
byte_t           *STREAM_build(byte_t ctlbyte, ...);
byte_t           *STREAM_build_from_list(byte_t ctlbyte, list_t *arglist, int *strbytes);
const byte_t     *STREAM_get_pwd(const byte_t *stream);
byte_t           STREAM_get_endianess(const byte_t *stream);
byte_t           STREAM_get_id(const byte_t *stream, int size);
byte_t           STREAM_get_ctlbyte(const byte_t *stream);
int              STREAM_is_stream(const byte_t *stream, int size);
int              STREAM_get_filesize(byte_t *stream, int size);
int              STREAM_wants_fwd(byte_t *stream, int size);
void             PSEQ_cleanup(int seqtype, psequence_t *pseq);
psequence_t      *PSEQ_build(int seqtype, int (*checker [])(void *arg));
int              PSEQ_next_sequence(int seq, const psequence_t pseq[], int from, const byte_t *in, byte_t **out, int *osbytes);
stream_item_t    *build_stream_item(byte_t *stream, size_t bytes, int from);
int              PSEQ_start_sequence(psequence_t pseq[], int from, byte_t **ostream, int *osbytes);

#endif
