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
 * 'stream.c' c file.
 *  -----------------
 *
 *****************************************************************************/

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>

#include "list.h"
#include "stream.h"


byte_t *STREAM_build_from_list(byte_t ctlbyte, list_t *arglist, int *strbytes)
{
  byte_t       *outstream = NULL;

  switch(ctlbyte)
    {
    case CTL_SV_IDLIST:
    case CTL_SV_RMCL:
    case CTL_SV_ADDCL:
    case CTL_SV_PWDOK:
    case CTL_SV_ASKPWD:
    case CTL_SV_PWDFAIL:
    case CTL_CL_ASKFILE:
    case CTL_CL_FILEOK:
      if ( !list_is_empty(arglist) )
	{
	  int         offset = 0;             /* offset used to move 1 byte across the data part of the stream */
	  list_node_t *tmp   = NULL;          /* temporal node used to gather arguments from the list          */

	  /* calculates the length of the stream, the number of ids */
	  /* 1 per byte plus the minimun packet size minus 1 (the   */
	  /* minimun packet size includes an id)                    */
	  *strbytes = arglist->count + STREAM_MIN_PACKET_SIZE - 1;

	  /* allocates memory for this stream */
	  outstream = (byte_t *) malloc(sizeof(byte_t) * (*strbytes));

	  /* proceed to fill in the data */
	  outstream[STREAM_IDX_ENDIANESS] = 0; //this is the endianesss TODO!!
	  outstream[STREAM_IDX_CTL]  = ctlbyte;
	  outstream[STREAM_IDX_LEN3] = (byte_t) (*strbytes >> 24) & 0xff;
	  outstream[STREAM_IDX_LEN2] = (byte_t) (*strbytes >> 16) & 0xff;
	  outstream[STREAM_IDX_LEN1] = (byte_t) (*strbytes >> 8) & 0xff;
	  outstream[STREAM_IDX_LEN0] = (byte_t) *strbytes & 0xff;

	  /* get the last offset byte */
	  offset = STREAM_IDX_LEN0;
	  do
	    {
	      offset++;
	      /* get the first argument from the list (an id) */
	      tmp = list_get_head(arglist);
	      /* copy the id to the offset-th byte */
	      outstream[offset] = *(byte_t *) list_get_item(tmp);
	      tmp = list_get_next(tmp);
	    }
	  while( tmp != NULL );
	  
	  /* copy the null byte at the end of the stream */
	  outstream[(*strbytes - 1)]  = (byte_t) STREAM_END_BYTE;
	}
      break;

    case CTL_SV_FILE: //TODO: ERROR CHECK!!
      {
	/* move the file position indicator to the end of file */
	fseek((FILE *) list_get_last_item(arglist), 0L, SEEK_END);

	/* get the current file position indicator to know the file length */
	/* and add the required space for this kind of stream              */
	*strbytes = ftell((FILE *) list_get_last_item(arglist)) + STREAM_MIN_PACKET_SIZE;
	
	/* get the file position indicator back to the beginning */
	rewind((FILE *)list_get_last_item(arglist));

	/* allocates memory to hold the stream data */
	outstream    = (byte_t *) malloc(sizeof(byte_t) * (*strbytes));

	/* proceed to fill in data */
	outstream[STREAM_IDX_ENDIANESS] = 0; //this is the endianesss TODO!!
	outstream[STREAM_IDX_CTL]  = ctlbyte;
	outstream[STREAM_IDX_LEN3] = (byte_t) (*strbytes >> 24) & 0xff;
	outstream[STREAM_IDX_LEN2] = (byte_t) (*strbytes >> 16) & 0xff;
	outstream[STREAM_IDX_LEN1] = (byte_t) (*strbytes >> 8)  & 0xff;
	outstream[STREAM_IDX_LEN0] = (byte_t) (*strbytes & 0xff);
	outstream[STREAM_IDX_ID]   = *(byte_t *) list_get_first_item(arglist);
	
	/* copy the file into 'outstream[STREAM_IDX_FILE]' position */
	read(fileno((FILE *) list_get_last_item(arglist)), outstream + STREAM_IDX_FILE, *strbytes - STREAM_MIN_PACKET_SIZE);

	/* set the null byte at the end of the stream */
	outstream[(*strbytes - 1)] = (byte_t) STREAM_END_BYTE;
      }
      break;

    case CTL_CL_PWD:
      {
	/* set the length of this stream */
	*strbytes = STREAM_PWD_SIZE + STREAM_MIN_PACKET_SIZE;
	
	printf("Building stream CTL_CL_PWD (%d) with size: %d\n",ctlbyte, *strbytes);
	/* allocates memory to hold the data for the stream */
	outstream = (byte_t *) malloc(sizeof(byte_t) * (*strbytes));

	/* proceed to fill in data */
	outstream[STREAM_IDX_ENDIANESS] = 0; //TODO!!
	outstream[STREAM_IDX_CTL] = ctlbyte;
	outstream[STREAM_IDX_LEN3] = (byte_t) (*strbytes >> 24) & 0xff;
	outstream[STREAM_IDX_LEN2] = (byte_t) (*strbytes >> 16) & 0xff;
	outstream[STREAM_IDX_LEN1] = (byte_t) (*strbytes >> 8)  & 0xff;
	outstream[STREAM_IDX_LEN0] = (byte_t) (*strbytes & 0xff);
	outstream[STREAM_IDX_ID]   = *(byte_t *) list_get_first_item(arglist);

	memcpy(outstream + STREAM_IDX_PWD, (byte_t *) list_get_last_item(arglist), STREAM_PWD_SIZE);
	outstream[(*strbytes - 1)] = STREAM_END_BYTE;
      }
      break;

    default:
      //ERROR
      break;
    }
  return outstream;
}



byte_t *STREAM_build(byte_t ctlbyte, ...)
{
  byte_t  *outstream = NULL;
  va_list argp;
  va_start(argp, ctlbyte);

  switch(ctlbyte)
    {
    case CTL_SV_IDLIST:
      {
	list_t      *idlist              = NULL;
	void        *client              = va_arg(argp, void *);
	void        *(*next)(void **arg) = va_arg(argp, void *(*)(void **));
	int         *strbytes            = va_arg(argp, int *);
	*strbytes = 0;
	idlist = list_make_empty(idlist);
	do
	  {
	    list_add_first_item(idlist, next(&client));
	  }
	while( list_get_first_item(idlist) != NULL);
	list_remove_first(idlist);
	if ( !list_is_empty(idlist) )
	  {
	    outstream    = (byte_t *) malloc(sizeof(byte_t) * (idlist->count + (STREAM_MIN_PACKET_SIZE - 1)));
	    outstream[(*strbytes)++] = ctlbyte;
	    *strbytes = *strbytes + (STREAM_IDX_ID - STREAM_IDX_LEN3);
	    while( !list_is_empty(idlist) )
	      {
		list_node_t *tmp = list_get_tail(idlist);
		outstream[(*strbytes)++] = *(byte_t *)list_get_item(tmp);
		list_remove(idlist, tmp);
		free(tmp);
	      }
	    outstream[(*strbytes)++]   = (byte_t) STREAM_END_BYTE;
	    outstream[STREAM_IDX_LEN3] = (byte_t) (*strbytes >> 24) & 0xff;
	    outstream[STREAM_IDX_LEN2] = (byte_t) (*strbytes >> 16) & 0xff;
	    outstream[STREAM_IDX_LEN1] = (byte_t) (*strbytes >> 8)  & 0xff;
	    outstream[STREAM_IDX_LEN0] = (byte_t) (*strbytes & 0xff);
	  }
	free(idlist);
      }
      break;
    case CTL_SV_RMCL:
    case CTL_SV_ADDCL:
    case CTL_SV_PWDOK:
    case CTL_SV_ASKPWD:
    case CTL_SV_PWDFAIL:
    case CTL_CL_ASKFILE:
    case CTL_CL_FILEINFOK:
    case CTL_CL_FILEOK:
      {
	int id       = va_arg(argp, int);
	int *strbytes= va_arg(argp, int *);
	*strbytes    = STREAM_MIN_PACKET_SIZE;
	outstream    = (byte_t *) malloc(sizeof(byte_t) * (*strbytes));
	outstream[STREAM_IDX_CTL]  = ctlbyte;
	outstream[STREAM_IDX_LEN3] = (byte_t) (*strbytes >> 24) & 0xff;
	outstream[STREAM_IDX_LEN2] = (byte_t) (*strbytes >> 16) & 0xff;
	outstream[STREAM_IDX_LEN1] = (byte_t) (*strbytes >> 8)  & 0xff;
	outstream[STREAM_IDX_LEN0] = (byte_t) (*strbytes & 0xff);
	outstream[STREAM_IDX_ID]   = (byte_t) id;
	outstream[*strbytes - 1]   = (byte_t) STREAM_END_BYTE;

      }
      break;

    case CTL_SV_FILE: //TODO: ERROR CHECK!!
      {
	int     id        = va_arg(argp, int);
	FILE    *file     = va_arg(argp, FILE *);
	int     *strbytes = va_arg(argp, int *);
	fseek(file, 0L, SEEK_END);
	*strbytes    = ftell(file) + STREAM_MIN_PACKET_SIZE;
	outstream    = (byte_t *) malloc(sizeof(byte_t) * (*strbytes));
	outstream[STREAM_IDX_CTL]  = ctlbyte;
	outstream[STREAM_IDX_LEN3] = (byte_t) (*strbytes >> 24) & 0xff;
	outstream[STREAM_IDX_LEN2] = (byte_t) (*strbytes >> 16) & 0xff;
	outstream[STREAM_IDX_LEN1] = (byte_t) (*strbytes >> 8)  & 0xff;
	outstream[STREAM_IDX_LEN0] = (byte_t) (*strbytes & 0xff);
	outstream[STREAM_IDX_ID]   = (byte_t) id;
	rewind(file);
	fread(outstream + STREAM_IDX_ID + 1, *strbytes - STREAM_MIN_PACKET_SIZE, 1, file);
	outstream[*strbytes - 1] = (byte_t) STREAM_END_BYTE;
      }
      break;

    case CTL_CL_PWD:
      {
	int    id          = va_arg(argp, int);
	unsigned char *pwd = va_arg(argp, unsigned char *);
	int   *strbytes    = va_arg(argp, int *);
	*strbytes = STREAM_PWD_SIZE + STREAM_MIN_PACKET_SIZE;
	outstream = (byte_t *) malloc(sizeof(byte_t) * (*strbytes));
	outstream[STREAM_IDX_ENDIANESS] = 0; //TODO!!
	outstream[STREAM_IDX_CTL] = ctlbyte;
	outstream[STREAM_IDX_LEN3] = (byte_t) (*strbytes >> 24) & 0xff;
	outstream[STREAM_IDX_LEN2] = (byte_t) (*strbytes >> 16) & 0xff;
	outstream[STREAM_IDX_LEN1] = (byte_t) (*strbytes >> 8)  & 0xff;
	outstream[STREAM_IDX_LEN0] = (byte_t) (*strbytes & 0xff);
	outstream[STREAM_IDX_ID]   = (byte_t) id;
	memcpy(outstream + STREAM_IDX_ID + 1, pwd, STREAM_PWD_SIZE);
	outstream[*strbytes - 1] = STREAM_END_BYTE;
      }
      break;

    default:
      //ERROR
      break;
    }
  va_end(argp);
  return outstream;
}



/****************************************/
/****************************************/
int STREAM_valid_header(const byte_t *header)
{
  int size = 0;

  /* if the pointer is the null pointer return false */
  if ( header == NULL ) return 0;

  /* check whether the stream size on the header is between the allowed size */
  size = STREAM_LENGTH(header);
  if ( size < STREAM_MIN_PACKET_SIZE && size > STREAM_MAX_PACKET_SIZE ) return 0;

  /* check if the control byte is some valid value */
  switch(STREAM_get_ctlbyte(header))
    {
    case CTL_SV_IDLIST:
    case CTL_SV_FILE:
    case CTL_SV_RMCL:
    case CTL_SV_ADDCL:
    case CTL_SV_ASKPWD:
    case CTL_SV_PWDOK:
    case CTL_SV_PWDFAIL:
    case CTL_SV_FILEINFO:
    case CTL_CL_FWD:
    case CTL_CL_FILEINFOK:
    case CTL_CL_DISC:
    case CTL_CL_ASKFILE:
    case CTL_CL_FILEOK:
    case CTL_CL_PWD:
      break;
    default:
      return 0;
    }
  
  /* check if the endian byte is 0 or 1 indicating the endianess */
  return ( (STREAM_get_endianess(header) == 0) | (STREAM_get_endianess(header) == 1 ));

}


/************************************************************************
 * STREAM_is_stream(const byte_t *stream, int size):
 ************************************************************************/
int STREAM_is_stream(const byte_t *stream, int size)
{
  /* A null pointer is not a valid stream */
  if (stream == NULL) return 0;

  /* a stream has a fixed size. Avoid segfaults */
  if (size < STREAM_MIN_PACKET_SIZE) return 0;
  if (size > STREAM_MAX_PACKET_SIZE) return 0;

  /* check if what stream says its size is it's what  */
  /* the server really have read.                     */
  /* If so, we can use later calls to STREAM_LENGTH() */
  /* and rely in that function value.                 */
  if ( size != STREAM_LENGTH(stream)) return 0;

  return STREAM_valid_header(stream);
}

/************************************************************************
 * STREAM_get_ctlbyte(const byte_t *stream, int size):
 ************************************************************************/
byte_t STREAM_get_ctlbyte(const byte_t *stream)
{
  return *(stream + STREAM_IDX_CTL);
}


/************************************************************************
 * STREAM_get_id(const byte_t *stream, int size):
 ************************************************************************/
byte_t STREAM_get_id(const byte_t *stream, int size) //change type to ID type
{
  if ( STREAM_is_stream(stream, size) )
    return *(stream + STREAM_IDX_ID);
  else
    return STREAM_ERR;
}

/************************************************************************
 ************************************************************************/
byte_t STREAM_get_endianess(const byte_t *stream)
{
  return *(stream + STREAM_IDX_ENDIANESS);
}


/************************************************************************
 * STREAM_get_pwd(byte_t *stream, int size, int id):
 ************************************************************************/
const byte_t *STREAM_get_pwd(const byte_t *stream)
{
  /* This function should be called after STREAM_is_stream() to ensure we are      */
  /* reading an moving on the pointer ok, and also we can trust in STREAM_LENGTH() */
  int size = STREAM_LENGTH(stream);
  
  if (size != STREAM_MIN_PACKET_SIZE + STREAM_PWD_SIZE )
    return NULL;
  else
    return (stream + STREAM_IDX_PWD);
}


/************************************
************************************/
int STREAM_wants_fwd(byte_t *stream, int size) //WHAT FOR???
{
  if ( STREAM_is_stream(stream, size))
    return (*stream == CTL_CL_FWD);
  else
    return 0;
}


int STREAM_is_press_event(byte_t drawctlbyte)
{
  return drawctlbyte & (EVENT_PRESS << 2);
}


int STREAM_is_drag_event(byte_t drawctlbyte)
{
  return drawctlbyte & (EVENT_DRAG << 2);
}


int STREAM_is_release_event(byte_t drawctlbyte)
{
  return drawctlbyte & (EVENT_RELEASE << 2);
}


int STREAM_get_event(byte_t drawctlbyte)
{
  if (STREAM_is_press_event(drawctlbyte))
    return (drawctlbyte & EVENT_MASK);
  if (STREAM_is_drag_event(drawctlbyte))
    return (drawctlbyte & EVENT_MASK);
  if (STREAM_is_release_event(drawctlbyte))
    return (drawctlbyte & EVENT_MASK);
  return EVENT_NOEVENT;
}


int STREAM_is_drawing(byte_t ctlbyte)
{
  return 1;
}

/***********************************************************************************************************
 * The packet sequence table:
 * -------------------------
 *
 * The packet sequence table is used as a collaborative diagram. The table has few
 * rows each of them a psequence_t structure. The row defines five basics things:
 *
 * + The client command corresponding the sequence (sequence = row ).
 * + The server command corresponding the sequence.
 * + A checking function concerning a command.
 * + An arguments list for the checking function.
 * + An arguments list to build the respective command.
 *
 * A sequence might look like this:
 *
 *   client command    server command   checking fn.    chk. fn. arg. list     build command arg. list
 * +-----------------+----------------+--------------+-----------------------+-------------------------+
 * |   CTL_CL_PWD    | CTL_SV_ASKPWD  |    F0        | [pcarg1, pcarg2 ... ] |  [pbarg1, pbarg2, ...]  |
 * +-----------------+----------------+--------------+-----------------------+-------------------------+
 * | CTL_CL_ASKFILE  | CTL_SV_PWDOK   |    F1        | [pcarg1, pcarg2 ... ] |  [pbarg1, pbarg2, ...]  |
 * +-----------------+----------------+--------------+-----------------------+-------------------------+
 * | CTL_CL_FILEOK   |  CTL_SV_FILE   |    F2        | [pcarg1, pcarg2 ... ] |  [pbarg1, pbarg2, ...]  |
 * +-----------------+----------------+--------------+-----------------------+-------------------------+
 *
 * Use:
 * ---
 *
 * The first row belongs to a handshake sequence. On the server side, we call PSEQ_start_sequence().
 * PSEQ_start_sequence(psequence_t, int, byte_t *, int *) will always return the first row of a matrix (0)
 * except if the function fails an error number will be returned (a negative number). It will try to build
 * a stream with the 'server command' column information together with the 'build command arg. list' from
 * the packet sequence table, and return it on the ouput parameter as well as its size.
 * Similarly, on the client side it will try to build a stream with the 'client command' column.
 *
 * It doesn't matter which side we are, build arguments will vary upon it. If we are on the server side
 * we'll create/update the table with the arguments required by the server command, and similarly if we
 * are on the client side. The same happens to the checking function. The checking function is a predefined
 * function by the server or the client (depending what we are writting) that checks the stream we receive 
 * upon the sequence and side we are (if needed, if not,we can provide a dummy function always returning success) 
 *
 * On the above table, the server will call PSEQ_start_sequence(). This will build a CTL_SV_ASKPWD stream to be
 * sent to the client. The client will read the stream, and feed it to the table with PSEQ_next_sequence().
 * The function will check if data is correct upon the sequence we are on. That is, if we are on sequence 0
 * the function will check if the 'server command' received is indeed the value on the 2nd column of the
 * first row (CTL_SV_ASKPWD). If a checking function was supplied, it will be called with the corresponding
 * arguments. If everything went ok, it will return the number of the next sequence and the command that
 * the client must send will be put on the output parameter together with its length. 
 *
 *****************************************************************************************************************/


/****************************************************************/
/****************************************************************/
psequence_t *PSEQ_build(int seqtype, int (*checker [])(void *arg))
{
  int i = 0;
  psequence_t *pseq = NULL;
  switch(seqtype)
    {
    case PSEQ_HNDSHK:
      pseq = (psequence_t *) malloc(sizeof(psequence_t) * PSEQ_HNDSHK_SIZE);
      for( i = 0; i < PSEQ_HNDSHK_SIZE; i++)
	{
	  pseq[i].pbargs   = list_make_empty(pseq[i].pbargs);
	  pseq[i].pcargs   = list_make_empty(pseq[i].pcargs);	  
	  pseq[i].pchecker = checker[i];
	}
      /***********************************/
      pseq[0].packet[PSEQ_CL] = CTL_CL_PWD;
      pseq[0].packet[PSEQ_SV] = CTL_SV_ASKPWD;
      /**************************************/

      /***************************************/
      pseq[1].packet[PSEQ_CL] = CTL_CL_ASKFILE;
      pseq[1].packet[PSEQ_SV] = CTL_SV_PWDOK;
      /*************************************/

      /**************************************/
      pseq[2].packet[PSEQ_CL] = CTL_CL_FILEOK;
      pseq[2].packet[PSEQ_SV] = CTL_SV_FILE;
      /************************************/
      break;

    default:
      return PSEQ_NULL;
    }
  return pseq;
}


/********************************************************************************/
/********************************************************************************/
int PSEQ_start_sequence(psequence_t pseq[], int from, byte_t **ostream, int *osbytes)
{
  printf("PSEQ_START_SEQUENCE: from: %d   -  stream: %d\n",from, pseq[PSEQ_START].packet[from]);
  *ostream = STREAM_build_from_list(pseq[PSEQ_START].packet[from], pseq[PSEQ_START].pbargs, osbytes); 

  return PSEQ_START;
}


/************************************
************************************/
int PSEQ_next_sequence(int seq, const psequence_t pseq[], int from, const byte_t *in, byte_t **out, int *osbytes)
{
  /* is 'from' is not a packet sequence from server (PSEQ_SV) or from client (PSEQ_CL) abort */
  if ( from != PSEQ_SV && from != PSEQ_CL ) return SEQ_ERR;

  printf("ctl istream: %d\n", STREAM_get_ctlbyte(in));
  /* if the sequence match, go on and build the "response stream" */
  if ( pseq[seq].packet[from]  == STREAM_get_ctlbyte(*out) &&
       pseq[seq].packet[!from] == STREAM_get_ctlbyte(in) )
    {
      printf("sequence match: \n");
      switch(pseq[seq].pchecker((void *)in))
	{
	case SEQ_NEEDS_RETRY:
	  /* recall the same sequence as before */
	  printf("needs retry\n");
	  return seq;

	case SEQ_OK:
	  /* build the correct stream */
	  seq++;
	  free(*out);
	  printf("Build stream for sequence %d with ctlbyte %d\n",seq,pseq[seq].packet[from]);
	  *out = STREAM_build_from_list(pseq[seq].packet[from], pseq[seq].pbargs, osbytes); 
	  return seq;

	case SEQ_LAST:
	  /* nothing more to do */
	  return ++seq;

	default:
	  return SEQ_ERR;
      }
    }
  else
    return SEQ_ERR;
}


/**************************************
**************************************/
void PSEQ_add_pbargs(psequence_t *pseq,  int seq, void *arg)
{
  list_add_last_item(pseq[seq].pbargs, arg);
}


/**************************************
**************************************/
void PSEQ_add_pcargs(psequence_t *pseq,  int seq, void *arg)
{
  list_add_last_item(pseq[seq].pcargs, arg);
}


/**************************************
**************************************/
void PSEQ_cleanup(int seqtype, psequence_t *pseq)
{
  switch(seqtype)
    {
      /* There's no way to generalize this free without getting inconclusing code */
    case PSEQ_HNDSHK:;
      list_node_t *tmp = NULL;
      tmp = list_get_head(pseq[0].pbargs);
      list_remove(pseq[0].pbargs, tmp);
      free(tmp);
      free(pseq[0].pbargs);
      tmp = list_get_head(pseq[1].pbargs);
      list_remove(pseq[1].pbargs, tmp);
      free(tmp);
      free(pseq[1].pbargs);
      tmp = list_get_head(pseq[2].pbargs);
      list_remove(pseq[2].pbargs, tmp);
      free(tmp);
      tmp = list_get_head(pseq[2].pbargs);
      list_remove(pseq[2].pbargs, tmp);
      free(tmp);
      break;

    default:
      break;
    }
}



/*************************************************************************
TODO: 'from' could be avoided??. 'stream' should have the client id of the
owner of the packet.
*************************************************************************/
stream_item_t *build_stream_item(byte_t *stream, size_t bytes, int from) 
{
  stream_item_t *item = (stream_item_t *) malloc(sizeof(stream_item_t));

  if ( item == NULL )
    {
      perror("item==null");
    }
  else 
    {
      /*****************************************************************/
      /* stream then can be freed easily. 'item' will be held on queue */
      /* until it gets freed */
      /***********************/
      memcpy(item->data, stream, bytes);
      item->nbytes = bytes;
      item->from   = from;
    }

  return item;
}
