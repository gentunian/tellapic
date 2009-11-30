#include <stdlib.h>
#include <string.h>

#include "list.h"
#include "stream.h"


byte_t *stream_build(byte_t ctlbyte, ...)
{
  byte_t  *outstream = NULL;
  va_list argp;
  va_start(argp, ctlbyte);

  if ( ctlbyte == CTL_SV_IDLIST)
    {
      list_t *idlist = NULL;
      int    i       = 0;
      void   *client = va_arg(argp, void *);
      void   *(*next)(void **arg) = va_arg(argp, void *(*)(void **));
      int    *size    = va_arg(argp, int *);
      idlist = list_make_empty(idlist);
      do
	{
	  list_add_first_item(idlist, next(&client));
	}
      while( list_get_first_item(idlist) != NULL);
      list_remove_first(idlist);
      if ( !list_is_empty(idlist) )
	{
	  *size        = idlist->count + 2;
	  outstream    = (byte_t *) malloc(sizeof(byte_t) * (*size));
	  outstream[i] = CTL_SV_IDLIST;
	  while( !list_is_empty(idlist) )
	    {
	      list_node_t *tmp = list_get_tail(idlist);
	      outstream[++i]   = *(byte_t *)list_get_item(tmp);
	      list_remove(idlist, tmp);
	      free(tmp);
	    }
	  outstream[++i] = 0;
	}
      free(idlist);
    }
  else if ( ctlbyte == CTL_SV_RMCL  ||
	    ctlbyte == CTL_SV_ADDCL || 
	    ctlbyte == CTL_SV_ASKPWD )
    {
      int id       = va_arg(argp, int);
      int *nbytes  = va_arg(argp, int *);
      outstream    = (byte_t *) malloc(sizeof(byte_t) * 3);
      outstream[0] = ctlbyte;
      outstream[1] = (byte_t) id;
      outstream[2] = 0;
      *nbytes      = 3;
    }
  else
    {
    }

  va_end(argp);
  return outstream;
}


byte_t stream_get_ctlbyte(const byte_t *stream)
{
  return stream[0]; 
}

byte_t stream_get_id(const byte_t *stream)
{
  return stream[1];
}

byte_t *stream_get_pwd(byte_t *stream, int size, int id)
{
  /* check for correct data packet */
  if ( *stream != CTL_CL_PWD || size != PWDLEN + 3 || *(stream + 1) != id)
    {
      *stream = '\0';
      return stream;
    }
  else
    return (stream + 2);    
}
/*******************************************************************************
 * Example:
 *
 * idliststream = build_stream(CTL_SV_IDLIST, clients, next_id);
 *
 * discstream   = build_stream(CTL_SV_RMCL, client->fd);
 */
