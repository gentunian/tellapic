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
 * The best I can do. This is not portable as some systems may
 * represents floating point with 64bit or with a 80bit precision.
 *
 * Despite that, system representations of floating points may
 * differ having the same representation size.
 *
 * This is for create a stream. This library creates a stream, so
 * we will create and pack every number in big endian (MSB First) or network byte order.
 *
 * The value variable will have the most significative byte in its first
 * index for BIG_ENDIAN systems. As streams are sent over the network as
 * a bunch of bytes: [byte0][byte1][byte2][byte3][byte4][byte5] inserting
 * a 32bit floating point starting at stream byte2 will result in copying
 * the MSB to byte2, then the next less significative byte to byte3 and so
 * on until the less copying the LSB into byte5. That is:
 * [byte2] = value[0];
 * [byte3] = value[1];
 * [byte4] = value[2];
 * [byte5] = value[3];
 *
 * For little endian system, we then put the MSB first so to adapt it to
 * the protocol. The first byte es the LSB in little endian systems, so
 * the MSB will be the last index:
 * [byte2] = value[3];
 * [byte3] = value[2];
 * [byte4] = value[1];
 * [byte5] = value[0];
 *
 * This __pack() helper function should be used with caution. It is supposed
 * to be used as a particular helper function with the most generality as it can.
 * 
 * stream must be a valid pointer and size should be the size of the library custom protocol
 * defined type we want to wrap to stream. In particular, size should be the size
 * of the value type. Value actual size could be bigger than size.
 *
 * This helper function can be used to put an unsigned char (byte_t) value in the stream as follows:
 * 
 * byte_t someByte = SOME_VALUE;
 * __pack(stream + SOME_INDEX_IN_THE_STREAM, (void *)&someByte, sizeof(byte_t));
 *
 * NOTE: Try avoiding the use of sizeof(). As we are creating our byte representation in the stream
 * use the proper size of the type you want to pack. For example, coordinates in the stream are
 * 2 bytes long, but can be held in an integer. Then you should do:
 *
 * int x1 = 129;
 * __pack(stream + X1_INDEX, (void *)&x1, 2); // 2 or a defined constant MACRO (e.g. COORDINATE_TYPE_SIZE)
 */
void
__pack(byte_t *stream, void *value, size_t size)
{
  byte_t *fval = (byte_t *) value;
  int i = 0;
  
#if (BIG_ENDIAN_VALUE)

  for(i; i < size; i++)
    stream[i] = fval[i];

#else

  for(i; i < size; i++)
    stream[i] = fval[size - i - 1];

#endif 

}


/**
 *
 */
void
__packul(byte_t *stream, unsigned long value, size_t size)
{
  byte_t *fval = (byte_t *)&value;
  int i = 0;
  
#if (BIG_ENDIAN_VALUE)

  for(i; i < size; i++)
    stream[i] = fval[i];

#else

  for(i; i < size; i++)
    stream[i] = fval[size - i - 1];

#endif 

}


/**
 *
 */
void
__packf(byte_t *stream, float value, size_t size)
{
  byte_t *fval = (byte_t *)&value;
  int i = 0;
  
#if (BIG_ENDIAN_VALUE)

  for(i; i < size; i++)
    stream[i] = fval[i];

#else

  for(i; i < size; i++)
    stream[i] = fval[size - i - 1];

#endif 

}


/**
 * So now we have a stream. The stream was created by this library. Numbers were
 * packed with endianness in mind and the stream has in the header.endian section
 * the information that concers about endianness.
 *
 * Decision was to send use MSB in the protocol and not the system native endianness.
 * This will decrease the overhead of consulting the endian byte, and swapping the
 * correspondent bytes.
 *
 * The variable 'stream' here should be a portion of memory where we want to
 * unpack a float from, and not a whole stream. 
 *
 * What we have now in the first index of the stream is a MSB from a 32bit
 * floating point. It depends upon the system endiannes to build a correte floating point number.
 *
 * So, if the system is BIG_ENDIAN we just copy the same order to the floating
 * point number memory. If the system is LITTLE_ENDIAN, we reverse the bytes as
 * we already know that the protocol is implemented with MSB first.
 * 
 * Same WARNINGS apply here as in __pack() function. If you can understand __pack() there is no
 * more to say. If you don't, I can't be more expressive than that.
 *
 * ALWAYS take note that size is a STREAM CUSTOM PROTOCOL TYPE SIZE. That is, not a language
 * standard size. If you want to __unpack() a coordinate from the stream, then you could do:
 *
 * int x1 = *(int *) __unpack(stream + X1_INDEX, COORDINATE_TYPE_SIZE);
 *
 */
void *
__unpack(const byte_t *stream, size_t size)
{
  byte_t *buf = malloc(size);
  int i = 0;

#if (BIG_ENDIAN_VALUE)

  for(i; i < size; i++)
    buf[i] = stream[i];

#else

  for(i; i < size; i++)
    buf[i] = stream[size - i - 1];

#endif

  return (void *)buf;
}


/**
 *
 */
unsigned long
__unpackul(const byte_t *stream, size_t size)
{
  unsigned int value = 0;
  int i = 0;

  for(i; i < size; i++)
    value |= stream[i]<<8*(size - i - 1);

  return value;
}


/**
 *
 */
float
__unpackf(const byte_t *stream, size_t size)
{
  float value;
  int i = 0;

#if (BIG_ENDIAN_VALUE)

  for(i; i < size; i++)
    *((unsigned char *)&value+i) = stream[i];

#else

  for(i; i < size; i++)
    *((unsigned char*)&value+i) = stream[size - i - 1];

#endif  

  return value;
}


/**
 * Extracts the byte-th byte from the data variable.
 */
byte_t
__extbyte(int byte, unsigned long data)
{ 

  return (data>>byte*8) & 0xff;

}


/**
 * Given a stream, sets the current ENDIAN value.
 *
 * This is currently an unused value. All data sent
 * will be sent in network byte order.
 *
 * It serves more than a reminder that data is MSB than
 * for anything else.
 */
void
__setendian(byte_t *stream)
{

  /* 0 means Little Endian */
  stream[ENDIAN_INDEX] = 1;

  /* The stream will be implemented with MSB first */

}


/**
 * Given a stream, sets the correspondent control byte.
 * No need to ask endian information for just a byte.
 */
void
__setcbyte(byte_t *stream, byte_t cbyte)
{

  stream[CBYTE_INDEX] = cbyte;

}


/**
 * Given a stream, sets the correspondent control byte.
 * No need to ask endian information for just a byte.
 */
void
__setdcbyte_ext(byte_t *stream, byte_t dcbyte_ext)
{

  stream[DDATA_DCBYTE_EXT_INDEX + HEADER_SIZE] = dcbyte_ext;

}


/**
 * Given a stream, sets the stream size bytes on the header
 * part of the stream.
 *
 * Endiannes is big endian or MSB First or network byte order.
 *
 * Note that size should be less than 2^32 - 1.
 */
void
__setssize(byte_t *stream, unsigned long size)
{

  /* Pack the unsigned long variable with just 4 LSB bytes.       */
  /* Any unsinged long that uses for representation more          */
  /* than 4 bytes should be discarted and though this function    */
  /* should not be called. 'size' will be packed in the header    */
  /* header.ssize section that allows only 4 bytes unsigned       */
  /* integers, restraining the protocol stream to 2^32 bytes long */

  __packul(stream + SBYTE_INDEX, size, 4);
}


/**
 * Given a stream, copies the id to the idfrom byte.
 *
 */
void
__setidfrom(byte_t *stream, byte_t id)
{

  stream[DATA_IDFROM_INDEX + HEADER_SIZE] = id;
  
}


/**
 * Given a stream, sets the drawing control byte.
 */
void
__setdcbyte(byte_t *stream, byte_t dcbyte)
{

  stream[DDATA_DCBYTE_INDEX + HEADER_SIZE] = dcbyte;
  
}


/**
 * Given a stream, sets the idto from a chat message.
 */
void
__setidto(byte_t *stream, byte_t id)
{

  stream[DATA_PMSG_IDTO_INDEX + HEADER_SIZE] = id;
  
}


/**
 * Given a stream, sets the drawing number of the drawing to be sent.
 */
void
__setdnum(byte_t *stream, unsigned int dnum)
{

  __packul(stream + DDATA_DNUMBER_INDEX + HEADER_SIZE, dnum, 4);

}


/**
 * Given a stream, copies the width (as floating point 32bit large) to it.
 */
void
__setwidth(byte_t *stream, float w)
{

  __packf(stream + DDATA_WIDTH_INDEX + HEADER_SIZE, w, 4);

}


/**
  * Given a stream, copies the opacity (as floating point 32bit large) to it.
 */
void
__setopacity(byte_t *stream, float op)  
{

  __packf(stream + DDATA_OPACITY_INDEX + HEADER_SIZE, op, 4);

}


/**
 * Given a stream, copies the red color attribute to it.
 */
void
 __setred(byte_t *stream, byte_t red)
{

  stream[DDATA_COLOR_INDEX + HEADER_SIZE] = red;
  
}


/**
 * Given a stream, copies the green color attribute to it.
 */
void
__setgreen(byte_t *stream, byte_t green)
{

  stream[DDATA_COLOR_INDEX + HEADER_SIZE + 1] = green;
  
}


/**
 * Given a stream, copies the blue color attribute to it.
 */
void
 __setblue(byte_t *stream, byte_t blue)
{

  stream[DDATA_COLOR_INDEX + HEADER_SIZE + 2] = blue;
  
}


/**
 *
 */
void
__setx1(byte_t *stream, unsigned int x1)
{

  //stream[DDATA_COORDX1_INDEX + HEADER_SIZE    ] = (x1>>8) & 0xff;
  //stream[DDATA_COORDX1_INDEX + HEADER_SIZE + 1] = x1 & 0xff;
  __packul(stream + DDATA_COORDX1_INDEX + HEADER_SIZE, x1, 2);

}


/**
 *
 */
void
__sety1(byte_t *stream, unsigned int y1)
{

  //stream[DDATA_COORDY1_INDEX + HEADER_SIZE    ] = (y1>>8) & 0xff;
  //stream[DDATA_COORDY1_INDEX + HEADER_SIZE + 1] = y1 & 0xff;
  __packul(stream + DDATA_COORDY1_INDEX + HEADER_SIZE, y1, 2);

}


/**
 *
 */
void
__setx2(byte_t *stream, unsigned int x2)
{

  //stream[DDATA_COORDX2_INDEX + HEADER_SIZE    ] = (x2>>8) & 0xff;
  //stream[DDATA_COORDX2_INDEX + HEADER_SIZE + 1] = x2 & 0xff;
  __packul(stream + DDATA_COORDX2_INDEX + HEADER_SIZE, x2, 2);

}


/**
 *
 */
void
__sety2(byte_t *stream, unsigned int y2)
{

  //stream[DDATA_COORDY2_INDEX + HEADER_SIZE    ] = (y2>>8) & 0xff;
  //stream[DDATA_COORDY2_INDEX + HEADER_SIZE + 1] = y2 & 0xff;
  __packul(stream + DDATA_COORDY2_INDEX + HEADER_SIZE, y2, 2);

}


/**
 *
 */
void
__setjoins(byte_t *stream, byte_t lj)
{

  stream[DDATA_JOINS_INDEX + HEADER_SIZE] = lj;
  
}


/**
 *
 */
void
__setcaps(byte_t *stream, byte_t ec)
{

  stream[DDATA_CAPS_INDEX + HEADER_SIZE] = ec;
  
}


/**
 *
 */
void
__setml(byte_t *stream, float ml)
{
  
  __packf(stream + DDATA_MITER_INDEX + HEADER_SIZE, ml, 4);

}


/**
 *
 */
void
__setdp(byte_t *stream, float dp)
{

  __packf(stream + DDATA_DASHPHASE_INDEX +HEADER_SIZE, dp, 4);

}


/**
 *
 */
void
__setda(byte_t *stream, float da[])
{
  
  __packf(stream + DDATA_DASHARRAY_INDEX + HEADER_SIZE,     da[0], 4);
  __packf(stream + DDATA_DASHARRAY_INDEX + HEADER_SIZE + 4, da[1], 4);

}


/**
 *
 */
void
__settextstyle(byte_t *stream, byte_t v)
{

  stream[DDATA_FONTSTYLE_INDEX + HEADER_SIZE] = v;

}


/**
 *
 */
void
__setfacelen(byte_t *stream, byte_t v)
{

  stream[DDATA_FONTLEN_INDEX + HEADER_SIZE] = v;
  
}


/**
 * Copies the header section structure header 
 * to the header raw bytes.
 */
void 
__setheader(byte_t *rawheader, header_t header) 
{
  rawheader[ENDIAN_INDEX] = header.endian;
  rawheader[CBYTE_INDEX]  = header.cbyte;

  /* MSB first */
  rawheader[SBYTE_INDEX]     = __extbyte(3, header.ssize);
  rawheader[SBYTE_INDEX + 1] = __extbyte(2, header.ssize);
  rawheader[SBYTE_INDEX + 2] = __extbyte(1, header.ssize);
  rawheader[SBYTE_INDEX + 3] = __extbyte(0, header.ssize);

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
__setddata(byte_t *rawstream, stream_t stream)
{
  int etype  = stream.data.drawing.dcbyte & EVENT_MASK;
  int tool   = stream.data.drawing.dcbyte & TOOL_MASK;

  /* Copy the data section fixed part */
  __setidfrom(rawstream, stream.data.drawing.idfrom);
  __setdcbyte(rawstream, stream.data.drawing.dcbyte);
  __setdcbyte_ext(rawstream, stream.data.drawing.dcbyte_ext);
  __setx1(rawstream, stream.data.drawing.point1.x);
  __sety1(rawstream, stream.data.drawing.point1.y);
  __setdnum(rawstream, stream.data.drawing.number);
  __setwidth(rawstream, stream.data.drawing.width);
  __setopacity(rawstream, stream.data.drawing.opacity);
  __setred(rawstream, stream.data.drawing.color.red);
  __setgreen(rawstream, stream.data.drawing.color.green);
  __setblue(rawstream, stream.data.drawing.color.blue);

  if (etype == EVENT_NULL || etype == EVENT_PRESS) 
    {
      /* EVENT_NULL means that we are copying a deferred drawing. */
      /* Deferred drawing could be text or something else.        */
      /* Text has different kind of information over the stream.  */
      /* Also a initiated Tool in direct mode has the same meaning*/
      /* as a deferred drawing, but different when EVENT_RELEASE  */
      /* or EVENT_DRAG.                                           */
      if (tool != TOOL_TEXT) 
	{
	  __setx2(rawstream, stream.data.drawing.type.figure.point2.x);
	  __sety2(rawstream, stream.data.drawing.type.figure.point2.y);
	  __setjoins(rawstream, stream.data.drawing.type.figure.linejoin);
	  __setcaps(rawstream, stream.data.drawing.type.figure.endcaps);
	  __setml(rawstream, stream.data.drawing.type.figure.miterlimit);
	  __setdp(rawstream, stream.data.drawing.type.figure.dash_phase);
	  __setda(rawstream, stream.data.drawing.type.figure.dash_array);
	} 
      else
	{
	  // Treat text different
	  __settextstyle(rawstream, stream.data.drawing.type.text.style);
	  __setfacelen(rawstream, stream.data.drawing.type.text.facelen);
	  memcpy(rawstream + DDATA_FONTFACE_INDEX + HEADER_SIZE, stream.data.drawing.type.text.face, stream.data.drawing.type.text.facelen);
	  long textsize = stream.header.ssize - (HEADER_SIZE + DDATA_TEXT_INDEX(stream.data.drawing.type.text.facelen));
	  memcpy(rawstream + DDATA_TEXT_INDEX(stream.data.drawing.type.text.facelen) + HEADER_SIZE, stream.data.drawing.type.text.info, textsize);
	}
    }
  else {
    /* Deferred mode with event EVENT_DRAG or EVENT_RELEASE */
  }
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
__setddata_using(byte_t *rawstream, stream_t stream)
{

  /* Copy the data section fixed part */
  __setidfrom(rawstream, stream.data.drawing.idfrom);
  __setdcbyte(rawstream, stream.data.drawing.dcbyte);
  __setdcbyte_ext(rawstream, stream.data.drawing.dcbyte_ext);
  __setx1(rawstream, stream.data.drawing.point1.x);
  __sety1(rawstream, stream.data.drawing.point1.y);
  __setdnum(rawstream, stream.data.drawing.number);
  __setwidth(rawstream, stream.data.drawing.width);
  __setopacity(rawstream, stream.data.drawing.opacity);
  __setred(rawstream, stream.data.drawing.color.red);
  __setgreen(rawstream, stream.data.drawing.color.green);
  __setblue(rawstream, stream.data.drawing.color.blue);

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
__setddata_init(byte_t *rawstream, stream_t stream)
{

  /* Copy the data section fixed part */
  __setidfrom(rawstream, stream.data.drawing.idfrom);
  __setdcbyte(rawstream, stream.data.drawing.dcbyte);
  __setdcbyte_ext(rawstream, stream.data.drawing.dcbyte_ext);
  __setx1(rawstream, stream.data.drawing.point1.x);
  __sety1(rawstream, stream.data.drawing.point1.y);
  __setdnum(rawstream, stream.data.drawing.number);
  __setwidth(rawstream, stream.data.drawing.width);
  __setopacity(rawstream, stream.data.drawing.opacity);
  __setred(rawstream, stream.data.drawing.color.red);
  __setgreen(rawstream, stream.data.drawing.color.green);
  __setblue(rawstream, stream.data.drawing.color.blue);
  __setx2(rawstream, stream.data.drawing.type.figure.point2.x);
  __sety2(rawstream, stream.data.drawing.type.figure.point2.y);
  __setjoins(rawstream, stream.data.drawing.type.figure.linejoin);
  __setcaps(rawstream, stream.data.drawing.type.figure.endcaps);
  __setml(rawstream, stream.data.drawing.type.figure.miterlimit);
  __setdp(rawstream, stream.data.drawing.type.figure.dash_phase);
  __setda(rawstream, stream.data.drawing.type.figure.dash_array);

}


/**
 * Copies a bunch of bytes from a streamed data section to a stream data structure
 * for any chat control byte.
 */
void
__chatdatacpy(stream_t *dest, byte_t *data, unsigned int datasize) 
{
  unsigned int textsize = 0;

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
 * Copies a bunch of bytes from a streamed data section into a stream 
 * data structure for any ctl extended control byte.
 */
void
__ctledatacpy(stream_t *dest, byte_t *data, unsigned long datasize)
{

  dest->data.control.idfrom = data[DATA_IDFROM_INDEX];

  memcpy(dest->data.control.info, data + 1, datasize - 1);

  /* Fill with '\0's and prevent segfault */
  if (datasize - 1 < MAX_INFO_SIZE)
    memset(&dest->data.control.info[datasize - 1], '\0', MAX_TEXT_SIZE - datasize - 1);

}


/**
 * Copies a bunch of bytes from a streamed data section into a tream 
 * data structure for ctl simple control byte.
 */
void
__ctldatacpy(stream_t *dest, byte_t *data)
{

  dest->data.control.idfrom = 0;
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
  dest->data.drawing.dcbyte_ext  = data[DDATA_DCBYTE_EXT_INDEX];
  dest->data.drawing.point1.x    = __unpackul(data + DDATA_COORDX1_INDEX, 2);
  dest->data.drawing.point1.y    = __unpackul(data + DDATA_COORDY1_INDEX, 2);
  dest->data.drawing.number      = __unpackul(data + DDATA_DNUMBER_INDEX, 4);
  dest->data.drawing.width       = __unpackf(data + DDATA_WIDTH_INDEX, 4);
  dest->data.drawing.opacity     = __unpackf(data + DDATA_OPACITY_INDEX, 4);
  dest->data.drawing.color.red   = data[DDATA_COLOR_INDEX];
  dest->data.drawing.color.green = data[DDATA_COLOR_INDEX + 1];
  dest->data.drawing.color.blue  = data[DDATA_COLOR_INDEX + 2];


  /* We will use this to define what we need to copy upon the selected tool or event. */
  int tool  = dest->data.drawing.dcbyte & TOOL_MASK;
  int etype = dest->data.drawing.dcbyte & EVENT_MASK;
  
  if (etype == EVENT_NULL || etype == EVENT_PRESS) 
    {
      /* TOOL_TEXT has different data. */
      if (tool != TOOL_TEXT) 
	{

	  dest->data.drawing.type.figure.point2.x      = __unpackul(data + DDATA_COORDX2_INDEX, 2);
	  dest->data.drawing.type.figure.point2.y      = __unpackul(data + DDATA_COORDY2_INDEX, 2);
	  dest->data.drawing.type.figure.miterlimit    = __unpackf(data + DDATA_MITER_INDEX, 4);
	  dest->data.drawing.type.figure.dash_phase    = __unpackf(data + DDATA_DASHPHASE_INDEX, 4);
	  dest->data.drawing.type.figure.dash_array[0] = __unpackf(data + DDATA_DASHARRAY_INDEX, 4);
	  dest->data.drawing.type.figure.dash_array[1] = __unpackf(data + DDATA_DASHARRAY_INDEX + 4, 4);
	  dest->data.drawing.type.figure.linejoin      = data[DDATA_JOINS_INDEX];
	  dest->data.drawing.type.figure.endcaps       = data[DDATA_CAPS_INDEX];
	} 
      else 
	{
	  dest->data.drawing.type.text.style = data[DDATA_FONTSTYLE_INDEX];

	  /* Do some checks about the "truly" value of facelen to avoid buffer overrun. */
	  /* data[DDATA_FONTLEN_INDEX] is only 1 byte len, don't worry about endiannes. */
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

	  memcpy(dest->data.drawing.type.text.face, data + DDATA_FONTFACE_INDEX, dest->data.drawing.type.text.facelen);
	  if (dest->data.drawing.type.text.facelen < MAX_FONTFACE_LEN)
	    memset(&dest->data.drawing.type.text.face[dest->data.drawing.type.text.facelen], '\0', MAX_FONTFACE_LEN - dest->data.drawing.type.text.facelen);

	  memcpy(dest->data.drawing.type.text.info, data + (DDATA_TEXT_INDEX(dest->data.drawing.type.text.facelen)), textsize);
	  if (textsize < MAX_TEXT_SIZE)
	    memset(&dest->data.drawing.type.text.info[textsize], '\0', MAX_FONTFACE_LEN - textsize);
	}
    }
}


/**
 * A wrapper from read() C function always in blocking mode. Returns the number of bytes
 * and places the read data in the output parameter buf.
 *
 * YOU SHOULD take care using this function and NOTICE that it will BLOCK your thread, and
 * also will set your file descriptor fd to blocking mode. So please, dont blame, if you
 * want non-blocking mode, write your own...
 */
size_t
__read_b(int fd, size_t totalbytes, byte_t *buf) 
{
  size_t bytesleft = totalbytes;
  size_t bytesread = 0;

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
      //bytesread = recv(fd, buf + bytesread, nbytes, 0);
      size_t bytes = read(fd, buf + bytesread, bytesleft);

      if (bytes > 0)
	{
	  bytesleft -= bytes;
	  bytesread += bytes;
	}
      else
	bytesleft = 0;
    }

  /* Restore the flags */
  fcntl(fd, F_SETFL, flags);

  return bytesread;
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

  if (stream.header.cbyte != CTL_FAIL && stream.header.cbyte != CTL_NOPIPE)
    stream = tellapic_read_data_b(fd, stream.header);
  
  return stream;
}


/**
 * pwd could and should be NULL.
 */
char *
tellapic_read_pwd(int fd, char *pwd, int *len)
{
  //byte_t     *header  = malloc(HEADER_SIZE);
  byte_t     header[HEADER_SIZE];
  int        nbytes   = __read_b(fd, HEADER_SIZE, header);
  stream_t   stream;  /* This is used only as a placeholder. Avoid it for future release, use instead local variables*/

  *len = 0;

  stream.header.cbyte = CTL_FAIL;

  if (nbytes == HEADER_SIZE && header[CBYTE_INDEX] == CTL_CL_PWD) 
    {
      stream.header.endian = header[ENDIAN_INDEX];
      stream.header.cbyte  = header[CBYTE_INDEX];
      stream.header.ssize  = __unpackul(header + SBYTE_INDEX, 4);
 
      if (stream.header.ssize <= MAX_CTLEXT_STREAM_SIZE && stream.header.ssize >= MIN_CTLEXT_STREAM_SIZE)
	{
	  //byte_t *data = malloc(stream.header.ssize - HEADER_SIZE);
	  byte_t data[stream.header.ssize - HEADER_SIZE];
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

	  //free(data);
	}
    }

  //free(header);

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
  //byte_t   *data    = malloc(datasize);
  byte_t   data[datasize];
  long     nbytes   = __read_b(fd, datasize, data);
  stream_t stream;


  /* This is important. It tells us that what header.ssize says is what we really read and     */
  /* what is indeed in the data buffer. So, is we move between 0 and header.ssize -HEADER_SIZE */
  /* we won't have SIGSEGV. This information is useful to __*cpy() functions.                  */
  if (nbytes == 0) 
      stream.header.cbyte = CTL_CL_DISC;

  else if (nbytes < 0)
    {
      stream.header.cbyte = CTL_FAIL;
      stream.header.ssize = 0;
    }

  else if (nbytes == datasize) 
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
	case CTL_CL_DRW:
	  __figdatacpy(&stream, data, datasize);
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
	case CTL_CL_DISC:
	case CTL_SV_CLRM:
	case CTL_SV_ID:
	case CTL_SV_NAMEINUSE:
	case CTL_SV_AUTHOK:
	  printf("nbytes: %d datasize: %d\n", nbytes, datasize);
	  __ctldatacpy(&stream, data);
	  break;

	default:
	  printf("FAIL over HERE!\n");
	  stream.header.cbyte = CTL_FAIL;
	  break;
	}
    } 

  //free(data);
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

  //byte_t        *data  = malloc(HEADER_SIZE);
  byte_t        data[HEADER_SIZE];
  int           nbytes = __read_b(fd, HEADER_SIZE, data);
  header_t      header;

  if (nbytes == HEADER_SIZE)
    {
      unsigned long ssize = __unpackul(data + SBYTE_INDEX, 4);
      if (ssize >= HEADER_SIZE && ssize <= MAX_STREAM_SIZE)
	{
	  header.endian = data[ENDIAN_INDEX];
	  header.cbyte  = data[CBYTE_INDEX];
	  header.ssize  = ssize;

	  /* This will check 2 things: */
	  /* First, if the header.cbyte corresponds with the header.ssize. */
	  /* And second, whether or not the header.cbyte is a valid value. */
	  if ( !(tellapic_ischatb(header)  && header.ssize <= MAX_BMSG_STREAM_SIZE   && header.ssize >= MIN_BMSG_STREAM_SIZE) &&
	       !(tellapic_ischatp(header)  && header.ssize <= MAX_PMSG_STREAM_SIZE   && header.ssize >= MIN_PMSG_STREAM_SIZE) &&
	       //!(tellapic_isfigtxt(header) && header.ssize <= MAX_FIGTXT_STREAM_SIZE && header.ssize > MIN_FIGTXT_STREAM_SIZE) &&
	       !(tellapic_isfig(header)    && header.ssize <= MAX_FIGTXT_STREAM_SIZE && header.ssize >= MIN_FIGTXT_STREAM_SIZE) &&
	       !(tellapic_isctle(header)   && header.ssize <= MAX_CTLEXT_STREAM_SIZE && header.ssize >= MIN_CTLEXT_STREAM_SIZE) &&
	       !(tellapic_isdrw(header)    && (header.ssize == DRW_INIT_STREAM_SIZE || header.ssize == DRW_USING_STREAM_SIZE)) &&
	       !(tellapic_isfile(header)   && header.ssize <= MAX_STREAM_SIZE && header.ssize >= MIN_CTLEXT_STREAM_SIZE) &&
	       !(tellapic_isctl(header)    && header.ssize == CTL_STREAM_SIZE)
	       //!(tellapic_isfig(header)    && header.ssize == FIG_STREAM_SIZE)
	       ) 
	    {
	      //free(data);
	      printf("FAIl! read header. header.ssize: %d header.cbyte: %d\n", header.ssize, header.cbyte);
	      header.ssize = 0;
	      header.cbyte = CTL_FAIL;
	      return header;
	    }
	}
    }
 
  else if (nbytes == 0) 
    {
      header.cbyte = CTL_NOPIPE;
      header.ssize = HEADER_SIZE;
      printf("nbytes == 0 on read_header\n");
    }

  else
    {
      printf("FAil read header\n");
      header.cbyte = CTL_FAIL;
    }

  // Free the data stream as we already filled up the stream structure for clients.
  //free(data);

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
 * TODO: this is not finished nor updated. 
 */
int 
tellapic_send(int socket, stream_t *stream) 
{

  byte_t *rawstream = malloc(stream->header.ssize);
  
  /* Copy the header to the raw stream data */
  __setheader(rawstream, stream->header);
  int cbyte = stream->header.cbyte;

  switch(cbyte)
    {
    case  CTL_CL_PMSG:
      __setidfrom(rawstream, stream->data.chat.idfrom);
      __setidto(rawstream, stream->data.chat.type.privmsg.idto);
      memcpy(rawstream + DATA_PMSG_TEXT_INDEX + HEADER_SIZE, stream->data.chat.type.privmsg.text, stream->header.ssize - HEADER_SIZE - 2);
      break;

    case CTL_CL_BMSG:
      __setidfrom(rawstream, stream->data.chat.idfrom);
      memcpy(rawstream + DATA_BMSG_TEXT_INDEX +HEADER_SIZE, stream->data.chat.type.broadmsg, stream->header.ssize - HEADER_SIZE - 1);
      break;

    case CTL_CL_FIG:
      __setddata(rawstream, *stream);
      break;

    case CTL_CL_DRW:
      if (stream->header.ssize == DRW_INIT_STREAM_SIZE)
	__setddata_init(rawstream, *stream);
      else
	__setddata_using(rawstream, *stream);
      break;


    case CTL_SV_CLIST:
      // Implementing this will complicate things. Instead, send CTL_SV_CLADD for each client the list has when an user ask for a client list.
      // The client will add the user if it wasn't already added.
      break;

    case CTL_SV_FILE:
      memcpy(rawstream + HEADER_SIZE, stream->data.file, stream->header.ssize - HEADER_SIZE);
      break;

    case CTL_CL_PWD:
    case CTL_SV_CLADD:
    case CTL_CL_NAME:
      memcpy(rawstream + DATA_CLADD_NAME_INDEX + HEADER_SIZE, stream->data.control.info, stream->header.ssize - HEADER_SIZE - 1);
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
      __setidfrom(rawstream, stream->data.control.idfrom);
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
tellapic_send_file(int fd, FILE *file, long filesize)
{

  byte_t *rawstream = malloc(filesize + HEADER_SIZE);

  __setendian(rawstream);
  __setcbyte(rawstream, CTL_SV_FILE);
  __setssize(rawstream, filesize + HEADER_SIZE);

  fread(rawstream + HEADER_SIZE, sizeof(byte_t), filesize, file);
  rewind(file);

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
  //byte_t *rawstream = malloc(ssize);
  byte_t rawstream[ssize];

  __setendian(rawstream);
  __setcbyte(rawstream, CTL_CL_FIG);
  __setdcbyte(rawstream, TOOL_TEXT | EVENT_NULL);
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

  memcpy(rawstream + HEADER_SIZE + DDATA_FONTFACE_INDEX, face, facelen);
  memcpy(rawstream + HEADER_SIZE + DDATA_TEXT_INDEX(facelen), text, textlen);
  
  int r = send(fd, rawstream, ssize, 0);

  //free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_drw_using(int fd, int tool, int dcbyte_ext, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1)
{
  //byte_t *rawstream = malloc(DRW_USING_STREAM_SIZE);
  byte_t rawstream[DRW_USING_STREAM_SIZE];

  __setendian(rawstream);
  __setcbyte(rawstream, CTL_CL_DRW);
  __setdcbyte_ext(rawstream, dcbyte_ext);
  __setssize(rawstream, DRW_USING_STREAM_SIZE);
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

  int r = send(fd, rawstream, DRW_USING_STREAM_SIZE, 0);

  //free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_drw_init(int fd, int tool, int dcbyte_ext, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int x2, int y2, int lj, int ec, float ml, float dp, float da[])
{
  //byte_t *rawstream = malloc(DRW_INIT_STREAM_SIZE);
  byte_t rawstream[DRW_INIT_STREAM_SIZE];

  __setendian(rawstream);
  __setcbyte(rawstream, CTL_CL_DRW);
  __setdcbyte_ext(rawstream, dcbyte_ext);
  __setssize(rawstream, DRW_INIT_STREAM_SIZE);
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

  int r = send(fd, rawstream, DRW_INIT_STREAM_SIZE, 0);

  //free(rawstream);

  return r;
}

/**
 *
 */
int
tellapic_send_fig(int fd, int tool, int dcbyte_ext, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int x2, int y2, int lj, int ec, float ml, float dp, float da[])
{
  //byte_t *rawstream = malloc(FIG_STREAM_SIZE);
  byte_t rawstream[FIG_STREAM_SIZE];

  __setendian(rawstream);
  __setcbyte(rawstream, CTL_CL_FIG);
  __setdcbyte_ext(rawstream, dcbyte_ext);
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

  //free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_chatp(int fd, int idfrom, int idto, int textlen, char* text)
{
  int    ssize = HEADER_SIZE + textlen + 2;
  //byte_t *rawstream = malloc(ssize);
  byte_t rawstream[ssize];

  __setendian(rawstream);
  __setcbyte(rawstream, CTL_CL_PMSG);
  __setssize(rawstream, ssize);
  __setidfrom(rawstream, idfrom);
  __setidto(rawstream, idto);

  memcpy(rawstream+HEADER_SIZE+DATA_PMSG_TEXT_INDEX, text, textlen);

  int r = send(fd, rawstream, ssize, 0);

  //free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_chatb(int fd, int idfrom, int textlen, char* text)
{
  int    ssize = HEADER_SIZE + textlen + 1;
  //byte_t *rawstream = malloc(ssize);
  byte_t rawstream[ssize];

  __setendian(rawstream);
  __setcbyte(rawstream, CTL_CL_BMSG);
  __setssize(rawstream, ssize);
  __setidfrom(rawstream, idfrom);

  memcpy(rawstream+HEADER_SIZE+DATA_BMSG_TEXT_INDEX, text, textlen);

  int r = send(fd, rawstream, ssize, 0);

  //free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_ctle(int fd, int idfrom, int ctle, int infolen, char *info)
{
  int    ssize = HEADER_SIZE + infolen + 1;
  //byte_t *rawstream = malloc(ssize);
  byte_t rawstream[ssize];

  __setendian(rawstream);
  __setcbyte(rawstream, ctle);
  __setssize(rawstream, ssize);
  __setidfrom(rawstream, idfrom);
  
  memcpy(rawstream + HEADER_SIZE + 1, info, infolen);  

  int r = send(fd, rawstream, ssize, 0);

  //free(rawstream);

  return r;
}


/**
 *
 */
int
tellapic_send_ctl(int fd, int idfrom, int ctl)
{
  //byte_t *rawstream = malloc(CTL_STREAM_SIZE);
  byte_t rawstream[CTL_STREAM_SIZE];

  __setendian(rawstream);
  __setcbyte(rawstream, ctl);
  __setssize(rawstream, CTL_STREAM_SIZE);
  __setidfrom(rawstream, idfrom);

  int r = send(fd, rawstream, CTL_STREAM_SIZE, 0);
  int i = 0;

  //free(rawstream);

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
      stream.header.ssize = 0;
      return stream;
    }

  stream.header.endian = 1;
  stream.header.cbyte  = ctl;
  stream.data.control.idfrom = idfrom;

  if (infosize < MAX_INFO_SIZE)
    {
      memcpy(stream.data.control.info, info, infosize);
      memset(stream.data.control.info + infosize, '\0', MAX_INFO_SIZE - infosize);
    }
  else
    {
      memcpy(stream.data.control.info, info, MAX_INFO_SIZE);
    }

  stream.header.ssize = HEADER_SIZE + infosize + 1;

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
void
tellapic_free(stream_t *stream)
{

  /* The only allocated memmory from a stream_t structure is this: */
  free(stream->data.file);

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
	  header.cbyte == CTL_CL_DISC    ||
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

  return (header.cbyte == CTL_SV_FILE);

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
