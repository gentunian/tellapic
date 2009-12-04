#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "list.h"
#include "stream.h"


byte_t *stream_build_from_list(byte_t ctlbyte, list_t *arglist, int *strbytes)
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
	  outstream = (byte_t *) malloc(sizeof(byte_t) * (arglist->count + 2));
	  outstream[(*strbytes)++] = ctlbyte;
	  do /* do instead of while cause we already know that the list is empty */
	    {
	      list_node_t *tmp = list_get_head(arglist);
	      outstream[(*strbytes)++] = *(byte_t *) list_get_item(tmp);
	      list_remove(arglist, tmp);
	      free(tmp);
	    }
	  while( !list_is_empty(arglist) );
	  outstream[(*strbytes)++] = (byte_t) STREAM_END_BYTE;
	}
      free(arglist);//sure?
      break;

    case CTL_SV_FILEINFO:
      *strbytes = 0;
      if ( !list_is_empty(arglist) )
	{
	  int      bytes = 0;
	  fseek((FILE *) list_get_last_item(arglist), 0L, SEEK_END);
	  bytes     = ftell((FILE *) list_get_last_item(arglist));
	  outstream = (byte_t *) malloc(sizeof(byte_t) * STREAM_FILEINFO_SIZE);
	  outstream[STREAM_IDX_CTL] = ctlbyte;
	  outstream[STREAM_IDX_ID]  = *(byte_t *) list_get_first_item(arglist);
	  outstream[STREAM_IDX_ID+1]= (byte_t) (bytes & 0xff000000)>>24;
	  outstream[STREAM_IDX_ID+2]= (byte_t) (bytes & 0xff0000)>>16;
	  outstream[STREAM_IDX_ID+3]= (byte_t) (bytes & 0xff00)>>8;
	  outstream[STREAM_IDX_ID+4]= (byte_t) bytes & 0xff;
	  outstream[STREAM_IDX_ID+5]= (byte_t) STREAM_END_BYTE;
	  *strbytes = STREAM_FILEINFO_SIZE;
	  fseek((FILE *) list_get_last_item(arglist), 0L, SEEK_SET);
	}
      break;
    case CTL_SV_FILE: //TODO: ERROR CHECK!!
      fseek((FILE *) list_get_last_item(arglist), 0L, SEEK_END);
      *strbytes    = ftell((FILE *) list_get_last_item(arglist)) + STREAM_MIN_PACKET_SIZE;
      outstream    = (byte_t *) malloc(sizeof(byte_t) * (*strbytes));
      outstream[STREAM_IDX_CTL] = ctlbyte;
      outstream[STREAM_IDX_ID]  = *(byte_t *) list_get_first_item(arglist);
      fseek((FILE *) list_get_last_item(arglist), 0L, SEEK_SET);
      fread(outstream+2, (*strbytes - STREAM_MIN_PACKET_SIZE), 1, (FILE *) list_get_last_item(arglist));
      outstream[*strbytes - 1] = (byte_t) STREAM_END_BYTE;
      break;

    default:
      //ERROR
      break;
    }
  return outstream;
}

byte_t *stream_build(byte_t ctlbyte, ...)
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
	outstream[STREAM_IDX_ID+1]= (byte_t) (bytes & 0xff000000)>>24;
	outstream[STREAM_IDX_ID+2]= (byte_t) (bytes & 0xff0000)>>16;
	outstream[STREAM_IDX_ID+3]= (byte_t) (bytes & 0xff00)>>8;
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

    default:
      //ERROR
      break;
    }
  va_end(argp);
  return outstream;
}


/************************************************************************
 * stream_get_ctlbyte(const byte_t *stream, int size):
 ************************************************************************/
byte_t stream_get_ctlbyte(const byte_t *stream, int size)
{
  if ( stream_is_stream(stream, size) )
    return *(stream + STREAM_IDX_CTL);
  else
    return STREAM_ERR;
}


/************************************************************************
 * stream_get_id(const byte_t *stream, int size):
 ************************************************************************/
byte_t stream_get_id(const byte_t *stream, int size)
{
  if ( STREAM_IS_STREAM(stream, size) )
    return *(stream + STREAM_IDX_ID);
  else
    return STREAM_ERR;
}


/************************************************************************
 * stream_is_stream(const byte_t *stream, int size):
 ************************************************************************/
int stream_is_stream(const byte_t *stream, int size)
{
  return STREAM_IS_STREAM(stream, size);
}


/************************************************************************
 * stream_get_pwd(byte_t *stream, int size, int id):
 ************************************************************************/
byte_t *stream_get_pwd(byte_t *stream, int size, int id)
{
  /* check for correct data packet */
  if (stream_is_stream(stream, size)    && 
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

/************************************
************************************/
int next_sequence(int seq, byte_t clpacket, byte_t svpacket, const psequence_t PSEQ[], byte_t *ostream, int *osbytes)
{
  if (PSEQ[seq].clpacket == clpacket && PSEQ[seq].svpacket == svpacket)
    switch(PSEQ[seq].pchecker(PSEQ[seq].pbargs))
      {
      case SEQ_NEEDS_RETRY:
	return seq;
      case SEQ_CANT_CONTINUE:
	return SEQ_ERR;
      case SEQ_OK:
	seq++;
	free(ostream);
	ostream = stream_build_from_list(PSEQ[seq].svpacket, PSEQ[seq].pbargs, osbytes); 
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
psequence_t *packet_sequence_build(int seq, int (*checker [])(void *arg))
{
  int i = 0;
  psequence_t *PSEQ = NULL;
  switch(seq)
    {
    case PSEQ_CONN:
      PSEQ = (psequence_t *) malloc(sizeof(psequence_t) * PSEQ_CONN_SIZE);
      for( i = 0; i < PSEQ_CONN_SIZE; i++)
	PSEQ[i].pchecker = checker[i];
      PSEQ[0].clpacket = CTL_CL_PWD;
      PSEQ[0].svpacket = CTL_SV_ASKPWD;
      PSEQ[1].clpacket = CTL_CL_ASKFILE;
      PSEQ[1].svpacket = CTL_SV_PWDOK;
      PSEQ[2].clpacket = CTL_CL_FILEINFOK;
      PSEQ[2].svpacket = CTL_SV_FILEINFO;
      PSEQ[3].clpacket = CTL_CL_FILEOK;
      PSEQ[3].svpacket = CTL_SV_FILE;
      break;

    default:
      return PSEQ_NULL;
    }
  return PSEQ;
}


/**************************************
**************************************/
void packet_sequence_cleanup(int seq, psequence_t *PSEQ)
{
  switch(seq)
    {
      /* There's no way to generalize this free without getting inconclusing code */
    case PSEQ_CONN:;
      list_node_t *tmp = NULL;
      tmp = list_get_head(PSEQ[0].pbargs);
      list_remove(PSEQ[0].pbargs, tmp);
      free(tmp);
      free(PSEQ[0].pbargs);
      tmp = list_get_head(PSEQ[1].pbargs);
      list_remove(PSEQ[1].pbargs, tmp);
      free(tmp);
      free(PSEQ[1].pbargs);
      tmp = list_get_head(PSEQ[2].pbargs);
      list_remove(PSEQ[2].pbargs, tmp);
      free(tmp);
      tmp = list_get_head(PSEQ[2].pbargs);
      list_remove(PSEQ[2].pbargs, tmp);
      free(tmp);
      tmp = list_get_head(PSEQ[3].pbargs);
      list_remove(PSEQ[3].pbargs, tmp);
      free(tmp);
      tmp = list_get_head(PSEQ[3].pbargs);
      list_remove(PSEQ[3].pbargs, tmp);
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
