#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <resolv.h>
#include <netdb.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>

#include "types.h"
#include "constants.h"
#include "tellapic.h"


/**
 *
 */
int
__extbyte(int byte, int data)
{ 
  return (data>>byte*8) & 0xff;
}


/**
 *
 */
void
__setendian(byte_t *str, int endian)
{
  str[ENDIAN_INDEX] = endian;
}


/**
 *
 */
void
__setcbyte(byte_t *str, int cbyte)
{
  str[CBYTE_INDEX] = cbyte;
}


/**
 *
 */
void
__setssize(byte_t *str, long v) {
  str[SBYTE_INDEX]  = (v>>24) & 0xff; 
  str[SBYTE_INDEX+1]= (v>>16) & 0xff; 
  str[SBYTE_INDEX+2]= (v>>8)  & 0xff; 
  str[SBYTE_INDEX+3]= (v>>0)  & 0xff;  
}


/**
 *
 */
void
__setidfrom(byte_t *str, int id)
{
  str[DATA_IDFROM_INDEX+HEADER_SIZE] = id;
}


/**
 *
 */
void
__setdcbyte(byte_t *str, int dcbyte)
{
  str[DDATA_DCBYTE_INDEX+HEADER_SIZE] = dcbyte;
}


/**
 *
 */
void
__setidto(byte_t *str, int id)
{
  str[DATA_PMSG_IDTO_INDEX+HEADER_SIZE] = id;
}


/**
 *
 */
void
__setdnum(byte_t *str, int dnum)
{
  str[DDATA_DNUMBER_INDEX+HEADER_SIZE] = dnum;
}


/**
 *
 */
void
__setwidth(byte_t *str, float w)
{
  str[DDATA_WIDTH_INDEX+HEADER_SIZE] = w;
}


/**
 *
 */
void
__setopacity(byte_t *str, float op)  
{
  str[DDATA_OPACITY_INDEX+HEADER_SIZE] = op;
}


/**
 *
 */
void
 __setred(byte_t *str, int red)
{
  str[DDATA_COLOR_INDEX+HEADER_SIZE] = red;
}


/**
 *
 */
void
__setgreen(byte_t *str, int green)
{
  str[DDATA_COLOR_INDEX+HEADER_SIZE+1] = green;
}


/**
 *
 */
void
 __setblue(byte_t *str, int blue)
{
  str[DDATA_COLOR_INDEX+2+HEADER_SIZE] = blue;
}


/**
 *
 */
void
__setx1(byte_t *str, int x1)
{
  str[DDATA_COORDX1_INDEX+HEADER_SIZE]   = (x1>>8) & 0xff;
  str[DDATA_COORDX1_INDEX+1+HEADER_SIZE] = (x1>>0) & 0xff;
}


/**
 *
 */
void
__sety1(byte_t *str, int y1)
{
  str[DDATA_COORDY1_INDEX+HEADER_SIZE]   = (y1>>8) & 0xff;
  str[DDATA_COORDY1_INDEX+1+HEADER_SIZE] = (y1>>0) & 0xff;
}


/**
 *
 */
void
__setx2(byte_t *str, int x2)
{
  str[DDATA_COORDX2_INDEX+HEADER_SIZE]   = (x2>>8) & 0xff;
  str[DDATA_COORDX2_INDEX+1+HEADER_SIZE] = (x2>>0) & 0xff;
}


/**
 *
 */
void
__sety2(byte_t *str, int y2)
{
  str[DDATA_COORDY2_INDEX+HEADER_SIZE]   = (y2>>8) & 0xff;
  str[DDATA_COORDY2_INDEX+1+HEADER_SIZE] = (y2>>0) & 0xff;
}


/**
 *
 */
void
__setjoins(byte_t *str, int lj)
{
  str[DDATA_JOINS_INDEX+HEADER_SIZE] = lj;
}


/**
 *
 */
void
__setcaps(byte_t *str, int ec)
{
  str[DDATA_CAPS_INDEX+HEADER_SIZE] = ec;
}


/**
 *
 */
void
__setml(byte_t *str, float ml)
{
  str[DDATA_MITER_INDEX+HEADER_SIZE] = ml;
}


/**
 *
 */
void
__setdp(byte_t *str, float dp)
{
  str[DDATA_DASHPHASE_INDEX+HEADER_SIZE] = dp;
}


/**
 *
 */
void
__setda(byte_t *str, float da[])
{
  str[DDATA_DASHARRAY_INDEX+HEADER_SIZE] = da[0];
  str[DDATA_DASHARRAY_INDEX+4+HEADER_SIZE] = da[1];
}

/**
 *
 */
void
__settextstyle(byte_t *str, int v)
{
  str[DDATA_FONTSTYLE_INDEX+HEADER_SIZE] = v;
}


/**
 *
 */
void
__setfacelen(byte_t *str, int v)
{
  str[DDATA_FONTLEN_INDEX+HEADER_SIZE] = v;
}


/**
 * Returns a non-zero value if 'stream' is a broadcast message.
 */
int 
tellapic_ischatb(header_t header) 
{

  return (header.cbyte == CTL_CL_BMSG);

}


/**
 * Returns a non-zero value if 'stream' is a private message.
 */
int 
tellapic_ischatp(header_t header) 
{
  return (header.cbyte == CTL_CL_PMSG);
}


/**
 * Returns a non-zero value if 'stream' is a control stream.
 */
int 
tellapic_isctl(header_t header)
{
  return (header.cbyte == CTL_CL_FILEASK ||
	  header.cbyte == CTL_CL_FILEOK  ||
	  header.cbyte == CTL_SV_PWDFAIL ||
	  header.cbyte == CTL_SV_PWDOK   ||
	  header.cbyte == CTL_CL_CLIST   ||
	  header.cbyte == CTL_SV_CLRM    ||
	  header.cbyte == CTL_SV_ID      ||
	  header.cbyte == CTL_SV_AUTHOK  ||
	  header.cbyte == CTL_SV_NAMEINUSE);
}


/**
 * Returns a non-zero value if 'stream' is an extended control stream.
 */
int 
tellapic_isctle(header_t header) 
{

  return (header.cbyte == CTL_CL_PWD   ||
	  header.cbyte == CTL_CL_NAME  ||
	  header.cbyte == CTL_SV_CLADD);
}


/**
 *
 */
int
tellapic_isfile(header_t header)
{
  return header.cbyte == CTL_SV_FILE;
}


/**
 * Returns a non-zero value if 'stream' is a drawing packet.
 */
int 
tellapic_isdrw(header_t header) 
{

  return (header.cbyte == CTL_CL_DRW);

}


/**
 * Returns a non-zero value if 'stream' is a text drawn figure.
 */
int 
tellapic_isfigtxt(stream_t stream) 
{

  return (stream.header.cbyte == CTL_CL_FIG &&
	  (stream.data.drawing.dcbyte & TOOL_MASK) == TOOL_TEXT);

}


/**
 * Returns a non-zero value if 'stream' is a drawn figure.
 */
int 
tellapic_isfig(header_t header) 
{

  return (header.cbyte == CTL_CL_FIG);

}


/**
 * Shift buf to the left with n bytes.
 */
long
__lbshift(byte_t *buf, int n) 
{
  byte_t *data  = (buf + n - 1);
  long    result = 0;
  int i;

  for(i = 0; i < n; i++)
    result |= *(data--) << 8*i;

  return result;
}


/**
 * A wrapper from read() C function always in blocking mode. Returns the number of bytes
 * and places the read data in the output parameter buf.
 *
 * YOU SHOULD take care using this function and NOTICE that it will BLOCK your thread, and
 * also will set your file descriptor fd to blocking mode. So please, dont blame, if you
 * want non-blocking mode, write your own...
 */
int
__read_b(int fd, size_t nbytes, byte_t *buf) 
{
  int bytesleft = nbytes;
  int bytesread = 0;

  /* This function should be feeded with a file descriptor with the O_NONBLOCK flag unset. */
  /* The *_b means that this function WILL block until data arrives. */
  /* Get the fd flags. */
  int flags = fcntl(fd, F_GETFL);

  /* If fd has O_NONBLOCK set, unset it. THIS IS MANDATORY, that's what _b means. */
  if ((flags & O_NONBLOCK) == O_NONBLOCK)
    fcntl(fd, F_SETFL, flags & ~O_NONBLOCK);

  /* Now we have a fd in blocking mode... */
  while(bytesleft > 0) 
    {
      bytesread = recv(fd, buf + bytesread, nbytes, 0);
      if (bytesread > 0)
	bytesleft -= bytesread;
      else
	bytesleft = 0;
    }

  /* Restore the flags */
  fcntl(fd, F_SETFL, flags);

  return bytesread;
}


/**
 * Copies the header section to header raw bytes;
 */
void 
__headercpy(byte_t *rawheader, header_t header) 
{
  rawheader[ENDIAN_INDEX] = header.endian;
  rawheader[CBYTE_INDEX]  = header.cbyte;
  int i;
  for (i = 0; i < 4; i++)
    rawheader[SBYTE_INDEX+i]  = __extbyte(4-i-1, header.ssize);
}


/**
 * Copies drawing data to the raw stream.
 *
 * ddata has specific platform dependent variable types sizes. For instance,
 * ddata.type.figure.point1.x is an integer variable that possibly has 4 bytes.
 * Instead, a protocol coordinate has 2 bytes for each coordinate, that is 4 bytes long.
 * Thus, an integer needs to be truncated or wrapped to the protocol coordinate _type_.
 * 
 */
void 
__ddatatostream(byte_t *rawstream, stream_t stream)
{
  int etype  = stream.data.drawing.dcbyte & EVENT_MASK;
  int tool   = stream.data.drawing.dcbyte & TOOL_MASK;

  /* Copy the data section fixed part */
  __setidfrom(rawstream, stream.data.drawing.idfrom);
  __setdcbyte(rawstream, stream.data.drawing.dcbyte);
  __setx1(rawstream, stream.data.drawing.point1.x);
  __sety1(rawstream, stream.data.drawing.point1.y);
  __setdnum(rawstream, stream.data.drawing.number);
  __setwidth(rawstream, stream.data.drawing.width);
  __setopacity(rawstream, stream.data.drawing.opacity);
  __setred(rawstream, stream.data.drawing.color.red);
  __setgreen(rawstream, stream.data.drawing.color.green);
  __setblue(rawstream, stream.data.drawing.color.blue);

  if (etype == EVENT_NULL) {
    // Deferred mode
    if (tool != TOOL_TEXT) {
      __setx2(rawstream, stream.data.drawing.type.figure.point2.x);
      __sety2(rawstream, stream.data.drawing.type.figure.point2.y);
      __setjoins(rawstream, stream.data.drawing.type.figure.linejoin);
      __setcaps(rawstream, stream.data.drawing.type.figure.endcaps);
      __setml(rawstream, stream.data.drawing.type.figure.miterlimit);
      __setdp(rawstream, stream.data.drawing.type.figure.dash_phase);
      __setda(rawstream, stream.data.drawing.type.figure.dash_array);
    } else {
      // Treat text different
      __settextstyle(rawstream, stream.data.drawing.type.text.style);
      __setfacelen(rawstream, stream.data.drawing.type.text.facelen);
      memcpy(rawstream+DDATA_FONTFACE_INDEX, stream.data.drawing.type.text.face, stream.data.drawing.type.text.facelen);
      long textsize = stream.header.ssize - (HEADER_SIZE + DDATA_TEXT_INDEX(stream.data.drawing.type.text.facelen));
      memcpy(rawstream+DDATA_TEXT_INDEX(stream.data.drawing.type.text.facelen), stream.data.drawing.type.text.info, textsize);
    }
  }
}


/**
 * Copies a bunch of bytes from data section to a stream data structure
 * for any chat control byte.
 */
void
__chatdatacpy(stream_t *dest, byte_t *data, long datasize) 
{
  long textsize = 0;

  dest->data.chat.idfrom = data[DATA_IDFROM_INDEX];

  if (dest->header.cbyte == CTL_CL_PMSG)
    {
      textsize = datasize - PMSG_TEXT_OFFSET;
      dest->data.chat.type.privmsg.idto = data[DATA_PMSG_IDTO_INDEX];
      memcpy(dest->data.chat.type.privmsg.text, data + DATA_PMSG_TEXT_INDEX, textsize);

      /* Fill with '\0's and prevent segfault */
      if (textsize < MAX_TEXT_SIZE)
	memset(&dest->data.chat.type.privmsg.text[textsize], '\0', MAX_TEXT_SIZE - textsize);
    }
  else
    {
      textsize = datasize - BMSG_TEXT_OFFSET;
      memcpy(dest->data.chat.type.broadmsg, data + DATA_BMSG_TEXT_INDEX, textsize);

      /* Fill with '\0's and prevent segfault */
      if (textsize < MAX_TEXT_SIZE)
	memset(&dest->data.chat.type.broadmsg[textsize], '\0', MAX_TEXT_SIZE - textsize);
    }
}


/**
 * Copies a bunch of bytes from data section into a tream 
 * data structure for any ctl extended control byte.
 */
void
__ctledatacpy(stream_t *dest, byte_t *data, long datasize)
{

  dest->data.control.idfrom = data[DATA_IDFROM_INDEX];

  memcpy(dest->data.control.info, data + 1, datasize - 1);

  /* Fill with '\0's and prevent segfault */
  if (datasize - 1 < MAX_INFO_SIZE)
    memset(&dest->data.control.info[datasize - 1], '\0', MAX_TEXT_SIZE - datasize - 1);

}


/**
 * Copies a bunch of bytes from data section into a tream 
 * data structure for ctl simple control byte.
 */
void
__ctldatacpy(stream_t *dest, byte_t *data)
{

  dest->data.control.idfrom = data[DATA_IDFROM_INDEX];

}


/**
 *
 */
void
__filedatacpy(stream_t *dest, byte_t *data, long datasize)
{
  dest->data.file = malloc(datasize);

  memcpy(dest->data.file, data, datasize);
}


/**
 * Copies a bunch of bytes from data section to a data structure.
 *
 *  Note that tellapic_read_data_b() has
 * ensure us that datasize is the actual data pointer size, and by deduction,
 * datasize + HEADER_SIZE is the total amount of data in the whole stream read.
 */
void
__figdatacpy(stream_t *dest, byte_t *data, long datasize) 
{

  /* Copy the fixed section either for text or a figure. */
  dest->data.drawing.idfrom      = data[DDATA_IDFROM_INDEX];
  dest->data.drawing.dcbyte      = data[DDATA_DCBYTE_INDEX];
  dest->data.drawing.point1.x    = __lbshift(data+DDATA_COORDX1_INDEX, 2);
  dest->data.drawing.point1.y    = __lbshift(data+DDATA_COORDY1_INDEX, 2);
  dest->data.drawing.number      = data[DDATA_DNUMBER_INDEX];
  dest->data.drawing.width       = data[DDATA_WIDTH_INDEX];
  dest->data.drawing.opacity     = data[DDATA_OPACITY_INDEX];
  dest->data.drawing.color.red   = data[DDATA_COLOR_INDEX];
  dest->data.drawing.color.green = data[DDATA_COLOR_INDEX+1];
  dest->data.drawing.color.blue  = data[DDATA_COLOR_INDEX+2];


  /* We will use this to define what we need to copy upon the selected tool or event. */
  int tool  = dest->data.drawing.dcbyte & TOOL_MASK;
  int etype = dest->data.drawing.dcbyte & EVENT_MASK;
  
  if (etype == EVENT_NULL) 
    {
      /* TOOL_TEXT has different data. */
      if (tool != TOOL_TEXT) 
	{
	  dest->data.drawing.type.figure.point2.x      = __lbshift(data+DDATA_COORDX2_INDEX, 2);
	  dest->data.drawing.type.figure.point2.y      = __lbshift(data+DDATA_COORDY2_INDEX, 2);
	  dest->data.drawing.type.figure.linejoin      = data[DDATA_JOINS_INDEX];
	  dest->data.drawing.type.figure.endcaps       = data[DDATA_CAPS_INDEX];
	  dest->data.drawing.type.figure.miterlimit    = data[DDATA_MITER_INDEX];
	  dest->data.drawing.type.figure.dash_phase    = data[DDATA_DASHPHASE_INDEX];
	  dest->data.drawing.type.figure.dash_array[0] = data[DDATA_DASHARRAY_INDEX];
	  dest->data.drawing.type.figure.dash_array[1] = data[DDATA_DASHARRAY_INDEX+4];
	} 
      else 
	{
	  dest->data.drawing.type.text.style = data[DDATA_FONTSTYLE_INDEX];

	  /* Do some checks about the "truly" value of facelen to avoid buffer overrun. */
	  if (data[DDATA_FONTLEN_INDEX] < 0)
	    dest->data.drawing.type.text.facelen = 0;
	  
	  /* Do not assign the maximum value if it will overrun the data buffer. */
	  else if (data[DDATA_FONTLEN_INDEX] > MAX_FONTFACE_LEN && !(datasize - DDATA_FONTFACE_INDEX - MAX_FONTFACE_LEN <= 0))
	    dest->data.drawing.type.text.facelen = MAX_FONTFACE_LEN;
	  else
	    {
	      /* Assume we exit the above condition with a false in the second term to avoid writting an else. */
	      dest->data.drawing.type.text.facelen = 0;

	      /* If we assume wrong, this will fix it. */
	      if(!(datasize - DDATA_FONTFACE_INDEX - data[DDATA_FONTLEN_INDEX] <= 0))
		dest->data.drawing.type.text.facelen = data[DDATA_FONTLEN_INDEX];
	    }
	  
	  /* We can be safe in assuming (at least I think...) that we won't overrun the buffer.     */
	  /* Note that, that doesn't mean data integrity. It's your fault if data was inconsistent. */
	  /* My job is to avoid SIGSEGV in this part of the code when memcopying...                 */
	  int textsize = datasize - DDATA_FONTFACE_INDEX - dest->data.drawing.type.text.facelen ;

	  memcpy(dest->data.drawing.type.text.face, data+DDATA_FONTFACE_INDEX, dest->data.drawing.type.text.facelen);
	  if (dest->data.drawing.type.text.facelen < MAX_FONTFACE_LEN)
	    memset(&dest->data.drawing.type.text.face[dest->data.drawing.type.text.facelen], '\0', MAX_FONTFACE_LEN - dest->data.drawing.type.text.facelen);

	  memcpy(dest->data.drawing.type.text.info, data+DDATA_TEXT_INDEX(dest->data.drawing.type.text.facelen), textsize);
	  if (textsize < MAX_TEXT_SIZE)
	    memset(&dest->data.drawing.type.text.info[textsize], '\0', MAX_FONTFACE_LEN - textsize);
	}
    }
}








/**
 * A helper function to read first the header, validates it, and then read the data section.
 * Note that tellapic_read_header_b() is a blocking function as well as any other *_b() function.
 *
 * This is the best effort I made for now. I REALLY need to avoid blocking I/O but that will be
 * added to TODO's list.
 *
 * Note that the header is a valid header already checked by the fuction, but another test should be
 * made to avoid reading data that is not coming and block.
 */
stream_t 
tellapic_read_stream_b(int fd) 
{
  stream_t stream;

  stream.header = tellapic_read_header_b(fd);

  if (stream.header.cbyte != CTL_FAIL || stream.header.cbyte != CTL_CL_DISC)
    stream = tellapic_read_data_b(fd, stream.header);
  
  return stream;
}



/**
 * pwd could and should be NULL.
 */
char *
tellapic_read_pwd(int fd, char *pwd, int *len)
{
  byte_t     *header  = malloc(HEADER_SIZE);
  int        nbytes   = __read_b(fd, HEADER_SIZE, header);
  stream_t   stream;  /* This is used only as a placeholder. Avoid it for future release, use instead local variables*/

  *len = 0;

  stream.header.cbyte = CTL_FAIL;

  if (nbytes == HEADER_SIZE && header[CBYTE_INDEX] == CTL_CL_PWD) 
    {
      stream.header.endian = header[ENDIAN_INDEX];
      stream.header.cbyte  = header[CBYTE_INDEX];
      stream.header.ssize  = __lbshift(header+SBYTE_INDEX, 4);

      if (stream.header.ssize <= MAX_CTLEXT_STREAM_SIZE && stream.header.ssize > MIN_CTLEXT_STREAM_SIZE)
	{
	  byte_t *data = malloc(stream.header.ssize - HEADER_SIZE);
	  int    error = 0;
	  nbytes = __read_b(fd, stream.header.ssize - HEADER_SIZE, data);
	  if (nbytes == stream.header.ssize - HEADER_SIZE)
	    {
	      __ctledatacpy(&stream, data, nbytes);
	      *len = nbytes - 1;
	      pwd = malloc(*len + 1);
	      strncpy(pwd, (char *)stream.data.control.info, *len);
	      pwd[*len] = '\0';
	    }

	  free(data);
	}
    }

  free(header);

  return pwd;
}



/**
 * Reads the data section upon the header passed as argument.
 * _b means blocking. For now, we just implement blocking mode.
 *
 * tellapic_read_header_b() ensures us that we have both, a valid
 * header, and the header size corresponds with HEADER_SIZE.
 */
stream_t
tellapic_read_data_b(int fd, header_t header) 
{
  long     datasize = header.ssize - HEADER_SIZE;
  byte_t   *data    = malloc(datasize);
  long     nbytes   = __read_b(fd, datasize, data);
  stream_t stream;


  /* This is important. It tells us that what header.ssize says is what we really read and     */
  /* what is indeed in the data buffer. So, is we move between 0 and header.ssize -HEADER_SIZE */
  /* we won't have SIGSEGV. This information is useful to __*cpy() functions.                  */
  if (nbytes == datasize) 
    {
      /* Copy the header to the stream structure. */
      stream.header = header;

      /* Copy the stream data section upon the control byte. */
      switch(stream.header.cbyte) 
	{
	case CTL_CL_PMSG:
	case CTL_CL_BMSG:
	  __chatdatacpy(&stream, data, datasize);
	  break;

	case CTL_CL_FIG:
	  __figdatacpy(&stream, data, datasize);
	  break;

	case CTL_CL_DRW:
	  break;

	case CTL_SV_CLIST:
	  break;

	case CTL_SV_CLADD:
	case CTL_CL_PWD:
	case CTL_CL_NAME:
	  __ctledatacpy(&stream, data, datasize);
	  break;

	case CTL_SV_FILE:
	  __filedatacpy(&stream, data, datasize);
	  break;

	case CTL_CL_FILEASK:
	case CTL_CL_FILEOK:
	case CTL_SV_PWDFAIL:
	case CTL_SV_PWDOK:
	case CTL_SV_PWDASK:
	case CTL_CL_CLIST:
	case CTL_SV_CLRM:
	case CTL_SV_ID:
	case CTL_SV_NAMEINUSE:
	case CTL_SV_AUTHOK:
	  __ctldatacpy(&stream, data);
	  break;

	default:
	  stream.header.cbyte = CTL_FAIL;
	  break;
	}
    } 
  else if (nbytes == 0) 
    stream.header.cbyte = CTL_CL_DISC;
  else
    stream.header.cbyte = CTL_FAIL;

  free(data);
  return stream;
}


/**
 * Wraps bytes chunks in the header structure to be returned.
 * _b means blocking. For now, we just implement blocking mode.
 * If we successfuly read HEADER_SIZE bytes, we check the control
 * byte for a valid value. Also, we ensure that the header.ssize
 * is at least a valid value for the corresponding control byte.
 */
header_t
tellapic_read_header_b(int fd) 
{

  byte_t     *data  = malloc(HEADER_SIZE);
  int        nbytes = __read_b(fd, HEADER_SIZE, data);
  header_t   header;

  if (nbytes == HEADER_SIZE) 
    {
      header.endian = data[ENDIAN_INDEX];
      header.cbyte  = data[CBYTE_INDEX];
      header.ssize  = __lbshift(data+SBYTE_INDEX, 4);

      /* This will check 2 things: */
      /* First, if the header.cbyte corresponds with the header.ssize. */
      /* And second, whether or not the header.cbyte is a valid value. */
      if ( !(tellapic_ischatb(header)  && header.ssize <= MAX_BMSG_STREAM_SIZE   && header.ssize >= MIN_BMSG_STREAM_SIZE) &&
	   !(tellapic_ischatp(header)  && header.ssize <= MAX_PMSG_STREAM_SIZE   && header.ssize >= MIN_PMSG_STREAM_SIZE) &&
	   //!(tellapic_isfigtxt(header) && header.ssize <= MAX_FIGTXT_STREAM_SIZE && header.ssize > MIN_FIGTXT_STREAM_SIZE) &&
	   !(tellapic_isfig(header)    && header.ssize <= MAX_FIGTXT_STREAM_SIZE && header.ssize >= MIN_FIGTXT_STREAM_SIZE) &&
	   !(tellapic_isctle(header)   && header.ssize <= MAX_CTLEXT_STREAM_SIZE && header.ssize >= MIN_CTLEXT_STREAM_SIZE) &&
	   !(tellapic_isdrw(header)    && (header.ssize == DRWI_STREAM_SIZE || header.ssize == DRWU_STREAM_SIZE)) &&
	   !(tellapic_isfile(header)   && header.ssize <= MAX_STREAM_SIZE && header.ssize >= MIN_CTLEXT_STREAM_SIZE) &&
	   !(tellapic_isctl(header)    && header.ssize == CTL_STREAM_SIZE)
	   //!(tellapic_isfig(header)    && header.ssize == FIG_STREAM_SIZE)
	   ) 
	{
	  free(data);
	  header.ssize = 0;
	  header.cbyte = CTL_FAIL;
	  return header;
	}
    } 
  else if (nbytes == 0) 
    header.cbyte = CTL_CL_DISC;

  else
    header.cbyte = CTL_FAIL;


  // Free the data stream as we already filled up the stream structure for clients.
  free(data);
  return header;
}


/**
 *
 */
void 
tellapic_close_fd(int fd) 
{

  close(fd);

}


/**
 *
 */
int 
tellapic_send(int socket, stream_t *stream) 
{
  byte_t *rawstream = malloc(stream->header.ssize);

  /* Copy the header to the raw stream data */
  __headercpy(rawstream, stream->header);
  int cbyte = stream->header.cbyte;

  switch(cbyte) 
    {
    case  CTL_CL_PMSG:
      rawstream[DATA_PMSG_IDFROM_INDEX+HEADER_SIZE] = stream->data.chat.idfrom;
      rawstream[DATA_PMSG_IDTO_INDEX+HEADER_SIZE]   = stream->data.chat.type.privmsg.idto;
      memcpy(rawstream+DATA_PMSG_TEXT_INDEX+HEADER_SIZE, stream->data.chat.type.privmsg.text, stream->header.ssize - HEADER_SIZE - 2);
      break;

    case CTL_CL_BMSG:
      rawstream[DATA_BMSG_IDFROM_INDEX+HEADER_SIZE] = stream->data.chat.idfrom;
      memcpy(rawstream+DATA_BMSG_TEXT_INDEX+HEADER_SIZE, stream->data.chat.type.broadmsg, stream->header.ssize - HEADER_SIZE - 1);
      break;

    case CTL_CL_FIG:
    case CTL_CL_DRW:
      __ddatatostream(rawstream, *stream);
      break;


    case CTL_SV_CLIST:
      // Implementing this will complicate things. Instead, send CTL_SV_CLADD for each client the list has when an user ask for a client list.
      // The client will add the user if it wasn't already added.
      break;

    case CTL_SV_FILE:
      memcpy(rawstream+HEADER_SIZE, stream->data.file, stream->header.ssize - HEADER_SIZE);
      break;

    case CTL_CL_PWD:
    case CTL_SV_CLADD:
    case CTL_CL_NAME:
      memcpy(rawstream+(DATA_CLADD_NAME_INDEX+HEADER_SIZE), stream->data.control.info, stream->header.ssize - HEADER_SIZE - 1);
    case CTL_CL_FILEASK:
    case CTL_CL_FILEOK:
    case CTL_SV_PWDFAIL:
    case CTL_SV_PWDOK:
    case CTL_SV_PWDASK:
    case CTL_CL_CLIST:
    case CTL_SV_CLRM:
    case CTL_SV_ID:
    case CTL_SV_NAMEINUSE:
    case CTL_SV_AUTHOK:
      rawstream[DATA_IDFROM_INDEX+HEADER_SIZE] = stream->data.control.idfrom;
      break;

    default:
      // wrong header
      return 0;
    }

  int r = send(socket, rawstream, stream->header.ssize, 0);

  free(rawstream);
  return r;
}


/**
 *
 */
int
tellapic_send_file(int fd, int filefd, long filesize)
{

  byte_t *rawstream = malloc(filesize + HEADER_SIZE);

  __setendian(rawstream, 0);
  __setcbyte(rawstream, CTL_SV_FILE);
  __setssize(rawstream, filesize);

  read(filefd, rawstream + HEADER_SIZE, filesize);

  int r = send(fd, rawstream, filesize + HEADER_SIZE, 0);

  free(rawstream);

  return r;  
}


/**
 *
 */
int
tellapic_send_text(int fd, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int style, int facelen, char *face, int textlen, char *text)
{
  int    ssize      = MIN_FIGTXT_STREAM_SIZE + facelen + textlen;
  byte_t *rawstream = malloc(ssize);

  __setendian(rawstream, 0);
  __setcbyte(rawstream, TOOL_TEXT | EVENT_NULL);
  __setssize(rawstream, ssize);
  __setidfrom(rawstream, idfrom);
  __setdnum(rawstream, dnum);
  __setwidth(rawstream, w);
  __setopacity(rawstream, op);
  __setred(rawstream, red);
  __setgreen(rawstream, green);
  __setblue(rawstream, blue);
  __setx1(rawstream, x1);
  __sety1(rawstream, y1);
  __settextstyle(rawstream, style);
  __setfacelen(rawstream, facelen);

  memcpy(rawstream+HEADER_SIZE+DDATA_FONTFACE_INDEX, face, facelen);
  memcpy(rawstream+HEADER_SIZE+DDATA_TEXT_INDEX(facelen), text, textlen);
  
  int r = send(fd, rawstream, ssize, 0);

  free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_fig(int fd, int tool, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int x2, int y2, int lj, int ec, float ml, float dp, float da[])
{
  byte_t *rawstream = malloc(FIG_STREAM_SIZE);

  __setendian(rawstream, 0);
  __setcbyte(rawstream, tool);
  __setssize(rawstream, FIG_STREAM_SIZE);
  __setidfrom(rawstream, idfrom);
  __setdcbyte(rawstream, tool);
  __setdnum(rawstream, dnum);
  __setwidth(rawstream, w);
  __setopacity(rawstream, op);
  __setred(rawstream, red);
  __setgreen(rawstream, green);
  __setblue(rawstream, blue);
  __setx1(rawstream, x1);
  __sety1(rawstream, y1);
  __setx2(rawstream, x2);
  __sety2(rawstream, y2);
  __setjoins(rawstream, lj);
  __setcaps(rawstream, ec);
  __setml(rawstream, ml);
  __setdp(rawstream, dp);
  __setda(rawstream, da);

  int r = send(fd, rawstream, FIG_STREAM_SIZE, 0);

  free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_chatp(int fd, int idfrom, int idto, int textlen, char* text)
{
  int    ssize = HEADER_SIZE + textlen + 2;
  byte_t *rawstream = malloc(ssize);

  __setendian(rawstream, 0);
  __setcbyte(rawstream, CTL_CL_PMSG);
  __setssize(rawstream, ssize);
  __setidfrom(rawstream, idfrom);
  __setidto(rawstream, idto);

  memcpy(rawstream+HEADER_SIZE+DATA_PMSG_TEXT_INDEX, text, textlen);

  int r = send(fd, rawstream, ssize, 0);

  free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_chatb(int fd, int idfrom, int textlen, char* text)
{
  int    ssize = HEADER_SIZE + textlen + 1;
  byte_t *rawstream = malloc(ssize);

  __setendian(rawstream, 0);
  __setcbyte(rawstream, CTL_CL_BMSG);
  __setssize(rawstream, ssize);
  __setidfrom(rawstream, idfrom);

  memcpy(rawstream+HEADER_SIZE+DATA_BMSG_TEXT_INDEX, text, textlen);

  int r = send(fd, rawstream, ssize, 0);

  free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_ctle(int fd, int idfrom, int ctle, int infolen, char *info)
{
  int    ssize = HEADER_SIZE + infolen + 1;
  byte_t *rawstream = malloc(ssize);

  __setendian(rawstream, 0);
  __setcbyte(rawstream, ctle);
  __setssize(rawstream, ssize);
  __setidfrom(rawstream, idfrom);
  
  memcpy(rawstream+HEADER_SIZE+1, info, infolen);  

  int r = send(fd, rawstream, ssize, 0);

  free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_ctl(int fd, int idfrom, int ctl)
{
  byte_t *rawstream = malloc(CTL_STREAM_SIZE);

  __setendian(rawstream, 0);
  __setcbyte(rawstream, ctl);
  __setssize(rawstream, CTL_STREAM_SIZE);
  __setidfrom(rawstream, idfrom);

  int r = send(fd, rawstream, CTL_STREAM_SIZE, 0);

  free(rawstream);

  return r;
}


/**
 *
 */
int 
tellapic_connect_to(const char *hostname, int port) 
{   
  int sd;
  struct hostent *host;
  struct sockaddr_in addr;

  if ( (host = gethostbyname(hostname)) == NULL )
      return -1;

  sd = socket(AF_INET, SOCK_STREAM, 0);

  bzero(&addr, sizeof(addr));

  addr.sin_family      = AF_INET;
  addr.sin_port        = htons(port);
  addr.sin_addr.s_addr = *(long*)(host->h_addr);

  if ( connect(sd, (struct sockaddr*)&addr, sizeof(addr)) != 0 ) 
    {
      close(sd);
      return -1;
    }

  return sd;
}


/**
 *
 */
stream_t 
tellapic_build_ctle(int ctl, int idfrom, int infosize, char *info) 
{
  stream_t stream;

  if (infosize <= 0) //TODO: add ctl checks
    {
      stream.header.cbyte = CTL_FAIL;
      return stream;
    }

  stream.header.endian = 0;
  stream.header.cbyte  = ctl;
  stream.data.control.idfrom = idfrom;

  if (infosize < MAX_INFO_SIZE)
    {
      memcpy(stream.data.control.info, info, infosize);
      memset(&stream.data.control.info[infosize], '\0', MAX_INFO_SIZE - infosize);
      stream.header.ssize = HEADER_SIZE + infosize + 1;
    }
  else
    {
      memcpy(stream.data.control.info, info, MAX_INFO_SIZE);
      stream.header.ssize = HEADER_SIZE + MAX_INFO_SIZE + 1;
    }

  return stream;
}


/**
 *
 */
stream_t
tellapic_build_ctl(int ctl, int idfrom) 
{
  stream_t stream;

 //TODO: add ctl checks

  stream.header.endian = 0;
  stream.header.cbyte  = ctl;
  stream.data.control.idfrom = idfrom;
  stream.header.ssize = HEADER_SIZE + 1;

  return stream;
}


/**
 *
 */
stream_t 
tellapic_build_chat(int cbyte, int idfrom, int idto, int textsize, char *text) //TODO: something is wrong HERE!
{
  stream_t stream;

  if (textsize <= 0 || cbyte != CTL_CL_PMSG || cbyte != CTL_CL_BMSG)
    {
      stream.header.cbyte = CTL_FAIL;
      return stream;
    }
    
  stream.header.endian = 0; // TODO: implement the endian function
  stream.header.cbyte  = cbyte; 
  stream.data.chat.idfrom = idfrom;

  if (cbyte == CTL_CL_PMSG) 
    {
      stream.data.chat.type.privmsg.idto = idto;
      if (textsize < MAX_TEXT_SIZE)
	{
	  memcpy(stream.data.chat.type.privmsg.text, text, textsize);
	  memset(&stream.data.chat.type.privmsg.text[textsize], '\0', MAX_TEXT_SIZE - textsize);
	  stream.header.ssize = HEADER_SIZE + textsize + 2;
	}
      else
	{
	  memcpy(stream.data.chat.type.privmsg.text, text, MAX_TEXT_SIZE);
	  stream.header.ssize = HEADER_SIZE + MAX_TEXT_SIZE + 2;
	}
    } 
  else 
    {
      if (textsize < MAX_TEXT_SIZE)
	{
	  memcpy(stream.data.chat.type.broadmsg, text, textsize);
	  memset(&stream.data.chat.type.broadmsg[textsize], '\0', MAX_TEXT_SIZE - textsize);
	  stream.header.ssize = HEADER_SIZE + textsize + 1;
	}
      else
	{
	  memcpy(stream.data.chat.type.broadmsg, text, MAX_TEXT_SIZE);
	  stream.header.ssize = HEADER_SIZE + MAX_TEXT_SIZE + 1;
	}
    }

  return stream;
}


/**
 *
 */
char *
tellapic_bytetp2charp(byte_t *value)
{
  return (char *)value;
}


/**
 *
 */
char
tellapic_bytet2char(byte_t value)
{
  return (char)value;
}








