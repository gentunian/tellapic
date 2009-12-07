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
      *strbytes = 0;
      if ( !list_is_empty(arglist) )
	{
	  list_node_t *tmp = NULL;
	  outstream        = (byte_t *) malloc(sizeof(byte_t) * (arglist->count + 2));
	  outstream[(*strbytes)++] = ctlbyte;
	  do /* do instead of while cause we already know that the list is empty */
	    {
	      tmp = list_get_head(arglist);
	      outstream[(*strbytes)++] = *(byte_t *) list_get_item(tmp);
	      tmp = list_get_next(tmp);
	    }
	  while( tmp != NULL );
	  outstream[(*strbytes)++] = (byte_t) STREAM_END_BYTE;
	}
      break;

    case CTL_SV_FILEINFO:
      *strbytes = 0;
      if ( !list_is_empty(arglist) )
	{
	  int      bytes = 
	  fseek((FILE *)list_get_last_item(arglist), 0L, SEEK_END);
	  bytes     = ftell((FILE *) list_get_last_item(arglist));
	  outstream = (byte_t *) malloc(sizeof(byte_t) * STREAM_FILEINFO_SIZE);
	  outstream[STREAM_IDX_CTL] = ctlbyte;
	  outstream[STREAM_IDX_ID]  = *(byte_t *) list_get_first_item(arglist);
	  outstream[STREAM_IDX_ID+1]= (byte_t) (bytes >> 24) & 0xff;
	  outstream[STREAM_IDX_ID+2]= (byte_t) (bytes >> 16) & 0xff;
	  outstream[STREAM_IDX_ID+3]= (byte_t) (bytes >> 8) & 0xff;
	  outstream[STREAM_IDX_ID+4]= (byte_t) bytes & 0xff;
	  outstream[STREAM_IDX_ID+5]= (byte_t) STREAM_END_BYTE;
	  *strbytes = STREAM_FILEINFO_SIZE;
	  fseek((FILE *) list_get_last_item(arglist), 0L, SEEK_SET);
	}
      break;
    case CTL_SV_FILE: //TODO: ERROR CHECK!!
      {
	fseek((FILE *) list_get_last_item(arglist), 0L, SEEK_END);
	*strbytes = ftell((FILE *) list_get_last_item(arglist)) + STREAM_MIN_PACKET_SIZE;
	rewind((FILE *)list_get_last_item(arglist));
	outstream    = (byte_t *) malloc(sizeof(byte_t) * (*strbytes));
	outstream[STREAM_IDX_CTL] = ctlbyte;
	outstream[STREAM_IDX_ID]  = *(byte_t *) list_get_first_item(arglist);
	read(fileno((FILE *) list_get_last_item(arglist)), (byte_t *)outstream+2, (*strbytes) - STREAM_MIN_PACKET_SIZE);
	outstream[*strbytes - 1] = (byte_t) STREAM_END_BYTE;
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
	int         *size                = va_arg(argp, int *);
	*size = 0;
	idlist = list_make_empty(idlist);
	do
	  {
	    list_add_first_item(idlist, next(&client));
	  }
	while( list_get_first_item(idlist) != NULL);
	list_remove_first(idlist);
	if ( !list_is_empty(idlist) )
	  {
	    outstream    = (byte_t *) malloc(sizeof(byte_t) * (idlist->count + 2));
	    outstream[(*size)++] = ctlbyte;
	    while( !list_is_empty(idlist) )
	      {
		list_node_t *tmp = list_get_tail(idlist);
		outstream[(*size)++] = *(byte_t *)list_get_item(tmp);
		list_remove(idlist, tmp);
		free(tmp);
	      }
	    outstream[(*size)++] = (byte_t) STREAM_END_BYTE;
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
	int *nbytes  = va_arg(argp, int *);
	outstream    = (byte_t *) malloc(sizeof(byte_t) * STREAM_MIN_PACKET_SIZE);
	outstream[0] = ctlbyte;
	outstream[1] = (byte_t) id;
	outstream[2] = (byte_t) STREAM_END_BYTE;
	*nbytes      = STREAM_MIN_PACKET_SIZE;
      }
      break;
    case CTL_SV_FILEINFO:
      {
	int     id        = va_arg(argp, int);
	FILE    *file     = va_arg(argp, FILE *);
	int     *strbytes = va_arg(argp, int *);
	int     bytes     = 0;
	fseek(file, 0L, SEEK_END);
	bytes = ftell(file);
	outstream = (byte_t *) malloc(sizeof(byte_t) * STREAM_FILEINFO_SIZE);
	outstream[STREAM_IDX_CTL] = ctlbyte;
	outstream[STREAM_IDX_ID]  = (byte_t) id;
	outstream[STREAM_IDX_ID+1]= (byte_t) (bytes >> 24) & 0xff;
	outstream[STREAM_IDX_ID+2]= (byte_t) (bytes >> 16) & 0xff;
	outstream[STREAM_IDX_ID+3]= (byte_t) (bytes >> 8 ) & 0xff;
	outstream[STREAM_IDX_ID+4]= (byte_t) bytes & 0xff;
	outstream[STREAM_IDX_ID+5]= (byte_t) STREAM_END_BYTE;
	*strbytes = STREAM_FILEINFO_SIZE;
	fseek(file, 0L, SEEK_SET);
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
	outstream[STREAM_IDX_CTL] = ctlbyte;
	outstream[STREAM_IDX_ID]  = (byte_t) id;
	fseek(file, 0L, SEEK_SET);
	fread(outstream+2, (*strbytes - STREAM_MIN_PACKET_SIZE), 1, file);
	outstream[*strbytes - 1] = (byte_t) STREAM_END_BYTE;
      }
      break;

    case CTL_CL_PWD:
      {
	int    id          = va_arg(argp, int);
	unsigned char *pwd = va_arg(argp, unsigned char *);
	int   *strbytes    = va_arg(argp, int *);
	*strbytes = PWDLEN + STREAM_MIN_PACKET_SIZE;
	outstream = (byte_t *) malloc(sizeof(byte_t) * (*strbytes));
	outstream[STREAM_IDX_CTL] = ctlbyte;
	outstream[STREAM_IDX_ID]  = (byte_t) id;
	memcpy(outstream+2, pwd, PWDLEN);
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


/************************************************************************
 * STREAM_get_ctlbyte(const byte_t *stream, int size):
 ************************************************************************/
byte_t STREAM_get_ctlbyte(const byte_t *stream, int size)
{
  if ( STREAM_is_stream(stream, size) )
    return *(stream + STREAM_IDX_CTL);
  else
    return STREAM_ERR;
}


/************************************************************************
 * STREAM_get_id(const byte_t *stream, int size):
 ************************************************************************/
byte_t STREAM_get_id(const byte_t *stream, int size)
{
  if ( STREAM_IS_STREAM(stream, size) )
    return *(stream + STREAM_IDX_ID);
  else
    return STREAM_ERR;
}


/************************************************************************
 * STREAM_is_stream(const byte_t *stream, int size):
 ************************************************************************/
int STREAM_is_stream(const byte_t *stream, int size)
{
  return STREAM_IS_STREAM(stream, size);
}


/************************************************************************
 * STREAM_get_pwd(byte_t *stream, int size, int id):
 ************************************************************************/
byte_t *STREAM_get_pwd(byte_t *stream, int size, int id)
{
  /* check for correct data packet */
  if (STREAM_is_stream(stream, size)    && 
      id   == *(stream + STREAM_IDX_ID) && 
      size == PWDLEN + STREAM_MIN_PACKET_SIZE 
      )
    return (stream + STREAM_IDX_PWD);
  else
    {
      *stream = '\0';
      return stream;
    }

}


/************************************************************************
 * STREAM_get_filesize(byte_t *stream, int size):
 ************************************************************************/
int STREAM_get_filesize(byte_t *stream, int size)
{
  if ( STREAM_is_stream(stream, size) )
    {
      return ( ((int)(stream[2] & 0xff)) << 24 | 
	       ((int)(stream[3] & 0xff)) << 16 | 
	       ((int)(stream[4] & 0xff)) <<  8 | 
	       ((int)(stream[5] & 0xff))
	       );
    }
  else
    return -1;
}

/************************************
************************************/
int PSEQ_next_sequence(int seq, byte_t clpacket, byte_t svpacket, const psequence_t pseq[], byte_t **ostream, int *osbytes)
{
  if (pseq[seq].packet[PSEQ_CL] == clpacket && pseq[seq].packet[PSEQ_SV] == svpacket)
    switch(pseq[seq].pchecker(pseq[seq].pcargs))
      {
      case SEQ_NEEDS_RETRY:
	return seq;
      case SEQ_CL_OK:
	seq++;
	free(*ostream);
	*ostream = STREAM_build_from_list(pseq[seq].packet[PSEQ_SV], pseq[seq].pbargs, osbytes); 
	return seq;
      case SEQ_SV_OK:
	seq++;
	free(*ostream);
	*ostream = STREAM_build_from_list(pseq[seq].packet[PSEQ_CL], pseq[seq].pbargs, osbytes);
	return seq;
      case SEQ_LAST:
	return ++seq;
      default:
	return SEQ_ERR;
      }
  else
    return SEQ_ERR;
}


/**************************************
**************************************/
int PSEQ_this_sequence(int seq, int from, byte_t packet, const psequence_t pseq[], byte_t **ostream, int *osbytes)
{
  if (pseq[seq].packet[from] == packet)
    switch(pseq[seq].pchecker(pseq[seq].pcargs))
      {
      case SEQ_SV_OK:
	free(ostream);
	*ostream = STREAM_build_from_list(pseq[seq].packet[from], pseq[seq].pbargs, osbytes); 
	return seq;
      default:
	return SEQ_ERR;
      }
  else
    return SEQ_ERR;
}


/**************************************
**************************************/
psequence_t *PSEQ_build(int seqtype, int (*checker [])(void *arg))
{
  int i = 0;
  psequence_t *pseq = NULL;
  switch(seqtype)
    {
    case PSEQ_CONN:
      pseq = (psequence_t *) malloc(sizeof(psequence_t) * PSEQ_CONN_SIZE);
      for( i = 0; i < PSEQ_CONN_SIZE; i++)
	{
	  pseq[i].pbargs   = list_make_empty(pseq[i].pbargs);
	  pseq[i].pcargs   = list_make_empty(pseq[i].pcargs);	  
	  pseq[i].pchecker = checker[i];
	}
      pseq[0].packet[PSEQ_CL] = CTL_CL_PWD;
      pseq[0].packet[PSEQ_SV] = CTL_SV_ASKPWD;
      pseq[1].packet[PSEQ_CL] = CTL_CL_ASKFILE;
      pseq[1].packet[PSEQ_SV] = CTL_SV_PWDOK;
      pseq[2].packet[PSEQ_CL] = CTL_CL_FILEINFOK;
      pseq[2].packet[PSEQ_SV] = CTL_SV_FILEINFO;
      pseq[3].packet[PSEQ_CL] = CTL_CL_FILEOK;
      pseq[3].packet[PSEQ_SV] = CTL_SV_FILE;
      break;

    default:
      return PSEQ_NULL;
    }
  return pseq;
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
    case PSEQ_CONN:;
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
      tmp = list_get_head(pseq[3].pbargs);
      list_remove(pseq[3].pbargs, tmp);
      free(tmp);
      tmp = list_get_head(pseq[3].pbargs);
      list_remove(pseq[3].pbargs, tmp);
      free(tmp);
      break;

    default:
      break;
    }
}

/*******************************************************************************
 * Example:
 *
 * idliststream = build_stream(CTL_SV_IDLIST, clients, next_id);
 *
 * discstream   = build_stream(CTL_SV_RMCL, client->fd);
 */
