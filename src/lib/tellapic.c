/**
 * Copyright (c) 2010 Sebastián Treu
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#if defined LINUX
#include <sys/socket.h>
#elif defined WIN32
#include <winsock.h>
#include <winsock2.h>
#endif

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
 * This _pack() helper function should be used with caution. It is supposed
 * to be used as a particular helper function with the most generality as it can.
 * 
 * stream must be a valid pointer and size should be the size of the library custom protocol
 * defined type we want to wrap to stream. In particular, size should be the size
 * of the value type. Value actual size could be bigger than size.
 *
 * This helper function can be used to put an unsigned char (byte_t) value in the stream as follows:
 * 
 * byte_t someByte = SOME_VALUE;
 * _pack(stream + SOME_INDEX_IN_THE_STREAM, (void *)&someByte, sizeof(byte_t));
 *
 * NOTE: Try avoiding the use of sizeof(). As we are creating our byte representation in the stream
 * use the proper size of the type you want to pack. For example, coordinates in the stream are
 * 2 bytes long, but can be held in an integer. Then you should do:
 *
 * int x1 = 129;
 * _pack(stream + X1_INDEX, (void *)&x1, 2); // 2 or a defined constant MACRO (e.g. COORDINATE_TYPE_SIZE)
 */
static void
 _pack(byte_t *stream, void *value, size_t size)
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
static void
_packul(byte_t *stream, unsigned long value, size_t size)
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
static void
_packf(byte_t *stream, float value, size_t size)
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
 * Same WARNINGS apply here as in _pack() function. If you can understand _pack() there is no
 * more to say. If you don't, I can't be more expressive than that.
 *
 * ALWAYS take note that size is a STREAM CUSTOM PROTOCOL TYPE SIZE. That is, not a language
 * standard size. If you want to _unpack() a coordinate from the stream, then you could do:
 *
 * int x1 = *(int *) _unpack(stream + X1_INDEX, COORDINATE_TYPE_SIZE);
 *
 */
static void *
_unpack(const byte_t *stream, size_t size)
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
static _unpackul(const byte_t *stream, size_t size)
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
static _unpackf(const byte_t *stream, size_t size)
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
static byte_t
_extbyte(int byte, unsigned long data)
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
static void
_setendian(byte_t *stream)
{

  /* 0 means Little Endian */
  stream[ENDIAN_INDEX] = 1;

  /* The stream will be implemented with MSB first */

}


/**
 * Given a stream, sets the correspondent control byte.
 * No need to ask endian information for just a byte.
 */
static void
_setcbyte(byte_t *stream, byte_t cbyte)
{

  stream[CBYTE_INDEX] = cbyte;

}


/**
 * Given a stream, sets the correspondent control byte.
 * No need to ask endian information for just a byte.
 */
static void
 _setdcbyte_ext(byte_t *stream, byte_t dcbyte_ext)
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
static void
_setssize(byte_t *stream, unsigned long size)
{

  /* Pack the unsigned long variable with just 4 LSB bytes.       */
  /* Any unsinged long that uses for representation more          */
  /* than 4 bytes should be discarted and though this function    */
  /* should not be called. 'size' will be packed in the header    */
  /* header.ssize section that allows only 4 bytes unsigned       */
  /* integers, restraining the protocol stream to 2^32 bytes long */

  _packul(stream + SBYTE_INDEX, size, 4);
}


/**
 * Given a stream, copies the id to the idfrom byte.
 *
 */
static void
_setidfrom(byte_t *stream, byte_t id)
{

  stream[DATA_IDFROM_INDEX + HEADER_SIZE] = id;
  
}


/**
 * Given a stream, sets the drawing control byte.
 */
static void
_setdcbyte(byte_t *stream, byte_t dcbyte)
{

  stream[DDATA_DCBYTE_INDEX + HEADER_SIZE] = dcbyte;
  
}


/**
 * Given a stream, sets the idto from a chat message.
 */
static void
_setidto(byte_t *stream, byte_t id)
{

  stream[DATA_PMSG_IDTO_INDEX + HEADER_SIZE] = id;
  
}


/**
 * Given a stream, sets the drawing number of the drawing to be sent.
 */
static void
_setdnum(byte_t *stream, unsigned int dnum)
{

  _packul(stream + DDATA_DNUMBER_INDEX + HEADER_SIZE, dnum, 4);

}


/**
 * Given a stream, copies the width (as floating point 32bit large) to it.
 */
static void
_setwidth(byte_t *stream, float w)
{

  _packf(stream + DDATA_WIDTH_INDEX + HEADER_SIZE, w, 4);

}


/**
  * Given a stream, copies the opacity (as floating point 32bit large) to it.
 */
static void
_setopacity(byte_t *stream, float op)  
{

  _packf(stream + DDATA_OPACITY_INDEX + HEADER_SIZE, op, 4);

}


/**
 * Given a stream, copies the red color attribute to it.
 */
static void
_setred(byte_t *stream, byte_t red)
{

  stream[DDATA_COLOR_INDEX + HEADER_SIZE] = red;
  
}


/**
 * Given a stream, copies the green color attribute to it.
 */
static void
_setgreen(byte_t *stream, byte_t green)
{

  stream[DDATA_COLOR_INDEX + HEADER_SIZE + 1] = green;
  
}


/**
 * Given a stream, copies the blue color attribute to it.
 */
static void
_setblue(byte_t *stream, byte_t blue)
{

  stream[DDATA_COLOR_INDEX + HEADER_SIZE + 2] = blue;
  
}


/**
 *
 */
static void
 _setx1(byte_t *stream, unsigned int x1)
{

  //stream[DDATA_COORDX1_INDEX + HEADER_SIZE    ] = (x1>>8) & 0xff;
  //stream[DDATA_COORDX1_INDEX + HEADER_SIZE + 1] = x1 & 0xff;
  _packul(stream + DDATA_COORDX1_INDEX + HEADER_SIZE, x1, 2);

}


/**
 *
 */
static void
 _sety1(byte_t *stream, unsigned int y1)
{

  //stream[DDATA_COORDY1_INDEX + HEADER_SIZE    ] = (y1>>8) & 0xff;
  //stream[DDATA_COORDY1_INDEX + HEADER_SIZE + 1] = y1 & 0xff;
  _packul(stream + DDATA_COORDY1_INDEX + HEADER_SIZE, y1, 2);

}


/**
 *
 */
static void
 _setx2(byte_t *stream, unsigned int x2)
{

  //stream[DDATA_COORDX2_INDEX + HEADER_SIZE    ] = (x2>>8) & 0xff;
  //stream[DDATA_COORDX2_INDEX + HEADER_SIZE + 1] = x2 & 0xff;
  _packul(stream + DDATA_COORDX2_INDEX + HEADER_SIZE, x2, 2);

}


/**
 *
 */
static void
 _sety2(byte_t *stream, unsigned int y2)
{

  //stream[DDATA_COORDY2_INDEX + HEADER_SIZE    ] = (y2>>8) & 0xff;
  //stream[DDATA_COORDY2_INDEX + HEADER_SIZE + 1] = y2 & 0xff;
  _packul(stream + DDATA_COORDY2_INDEX + HEADER_SIZE, y2, 2);

}


/**
 *
 */
static void
_setjoins(byte_t *stream, byte_t lj)
{

  stream[DDATA_JOINS_INDEX + HEADER_SIZE] = lj;
  
}


/**
 *
 */
static void
_setcaps(byte_t *stream, byte_t ec)
{

  stream[DDATA_CAPS_INDEX + HEADER_SIZE] = ec;
  
}


/**
 *
 */
static void
_setml(byte_t *stream, float ml)
{
  
  _packf(stream + DDATA_MITER_INDEX + HEADER_SIZE, ml, 4);

}


/**
 *
 */
static void
_setdp(byte_t *stream, float dp)
{

  _packf(stream + DDATA_DASHPHASE_INDEX +HEADER_SIZE, dp, 4);

}


/**
 *
 */
static void
_setda(byte_t *stream, float da[])
{
  
  _packf(stream + DDATA_DASHARRAY_INDEX + HEADER_SIZE,     da[0], 4);
  _packf(stream + DDATA_DASHARRAY_INDEX + HEADER_SIZE + 4, da[1], 4);

}


/**
 *
 */
static void
_settextstyle(byte_t *stream, byte_t v)
{

  stream[DDATA_FONTSTYLE_INDEX + HEADER_SIZE] = v;

}


/**
 *
 */
static void
_setfacelen(byte_t *stream, byte_t v)
{

  stream[DDATA_FONTLEN_INDEX + HEADER_SIZE] = v;
  
}


/**
 * Copies the header section structure header 
 * to the header raw bytes.
 */
static void
_setheader(byte_t *rawheader, header_t header) 
{
  rawheader[ENDIAN_INDEX] = header.endian;
  rawheader[CBYTE_INDEX]  = header.cbyte;

  /* MSB first */
  rawheader[SBYTE_INDEX]     = _extbyte(3, header.ssize);
  rawheader[SBYTE_INDEX + 1] = _extbyte(2, header.ssize);
  rawheader[SBYTE_INDEX + 2] = _extbyte(1, header.ssize);
  rawheader[SBYTE_INDEX + 3] = _extbyte(0, header.ssize);

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
static _setddata(byte_t *rawstream, stream_t stream)
{
  int etype  = stream.data.drawing.dcbyte & EVENT_MASK;
  int tool   = stream.data.drawing.dcbyte & TOOL_MASK;

  /* Copy the data section fixed part */
  _setidfrom(rawstream, stream.data.drawing.idfrom);
  _setdcbyte(rawstream, stream.data.drawing.dcbyte);
  _setdcbyte_ext(rawstream, stream.data.drawing.dcbyte_ext);
  _setx1(rawstream, stream.data.drawing.point1.x);
  _sety1(rawstream, stream.data.drawing.point1.y);
  _setdnum(rawstream, stream.data.drawing.number);
  _setwidth(rawstream, stream.data.drawing.width);
  _setopacity(rawstream, stream.data.drawing.opacity);
  _setred(rawstream, stream.data.drawing.color.red);
  _setgreen(rawstream, stream.data.drawing.color.green);
  _setblue(rawstream, stream.data.drawing.color.blue);

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
	  _setx2(rawstream, stream.data.drawing.type.figure.point2.x);
	  _sety2(rawstream, stream.data.drawing.type.figure.point2.y);
	  _setjoins(rawstream, stream.data.drawing.type.figure.linejoin);
	  _setcaps(rawstream, stream.data.drawing.type.figure.endcaps);
	  _setml(rawstream, stream.data.drawing.type.figure.miterlimit);
	  _setdp(rawstream, stream.data.drawing.type.figure.dash_phase);
	  _setda(rawstream, stream.data.drawing.type.figure.dash_array);
	} 
      else
	{
	  // Treat text different
	  _settextstyle(rawstream, stream.data.drawing.type.text.style);
	  _setfacelen(rawstream, stream.data.drawing.type.text.facelen);
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
static void 
_setddata_using(byte_t *rawstream, stream_t stream)
{

  /* Copy the data section fixed part */
  _setidfrom(rawstream, stream.data.drawing.idfrom);
  _setdcbyte(rawstream, stream.data.drawing.dcbyte);
  _setdcbyte_ext(rawstream, stream.data.drawing.dcbyte_ext);
  _setx1(rawstream, stream.data.drawing.point1.x);
  _sety1(rawstream, stream.data.drawing.point1.y);
  _setdnum(rawstream, stream.data.drawing.number);
  _setwidth(rawstream, stream.data.drawing.width);
  _setopacity(rawstream, stream.data.drawing.opacity);
  _setred(rawstream, stream.data.drawing.color.red);
  _setgreen(rawstream, stream.data.drawing.color.green);
  _setblue(rawstream, stream.data.drawing.color.blue);

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
static void 
_setddata_init(byte_t *rawstream, stream_t stream)
{

  /* Copy the data section fixed part */
  _setidfrom(rawstream, stream.data.drawing.idfrom);
  _setdcbyte(rawstream, stream.data.drawing.dcbyte);
  _setdcbyte_ext(rawstream, stream.data.drawing.dcbyte_ext);
  _setx1(rawstream, stream.data.drawing.point1.x);
  _sety1(rawstream, stream.data.drawing.point1.y);
  _setdnum(rawstream, stream.data.drawing.number);
  _setwidth(rawstream, stream.data.drawing.width);
  _setopacity(rawstream, stream.data.drawing.opacity);
  _setred(rawstream, stream.data.drawing.color.red);
  _setgreen(rawstream, stream.data.drawing.color.green);
  _setblue(rawstream, stream.data.drawing.color.blue);
  _setx2(rawstream, stream.data.drawing.type.figure.point2.x);
  _sety2(rawstream, stream.data.drawing.type.figure.point2.y);
  _setjoins(rawstream, stream.data.drawing.type.figure.linejoin);
  _setcaps(rawstream, stream.data.drawing.type.figure.endcaps);
  _setml(rawstream, stream.data.drawing.type.figure.miterlimit);
  _setdp(rawstream, stream.data.drawing.type.figure.dash_phase);
  _setda(rawstream, stream.data.drawing.type.figure.dash_array);

}


/**
 * Copies a bunch of bytes from a streamed data section to a stream data structure
 * for any chat control byte.
 */
static void
 _chatdatacpy(stream_t *dest, byte_t *data, unsigned int datasize) 
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
static void
_ctledatacpy(stream_t *dest, byte_t *data, unsigned long datasize)
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
static void
_ctldatacpy(stream_t *dest, byte_t *data)
{

  dest->data.control.idfrom = 0;
  dest->data.control.idfrom = data[DATA_IDFROM_INDEX];

}


/**
 *
 */
static void
_filedatacpy(stream_t *dest, byte_t *data, long datasize)
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
static void
_figdatacpy(stream_t *dest, byte_t *data, long datasize) 
{

  /* Copy the fixed section either for text or a figure. */
  dest->data.drawing.idfrom      = data[DDATA_IDFROM_INDEX];
  dest->data.drawing.dcbyte      = data[DDATA_DCBYTE_INDEX];
  dest->data.drawing.dcbyte_ext  = data[DDATA_DCBYTE_EXT_INDEX];
  dest->data.drawing.point1.x    = _unpackul(data + DDATA_COORDX1_INDEX, 2);
  dest->data.drawing.point1.y    = _unpackul(data + DDATA_COORDY1_INDEX, 2);
  dest->data.drawing.number      = _unpackul(data + DDATA_DNUMBER_INDEX, 4);
  dest->data.drawing.width       = _unpackf(data + DDATA_WIDTH_INDEX, 4);
  dest->data.drawing.opacity     = _unpackf(data + DDATA_OPACITY_INDEX, 4);
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

	  dest->data.drawing.type.figure.point2.x      = _unpackul(data + DDATA_COORDX2_INDEX, 2);
	  dest->data.drawing.type.figure.point2.y      = _unpackul(data + DDATA_COORDY2_INDEX, 2);
	  dest->data.drawing.type.figure.miterlimit    = _unpackf(data + DDATA_MITER_INDEX, 4);
	  dest->data.drawing.type.figure.dash_phase    = _unpackf(data + DDATA_DASHPHASE_INDEX, 4);
	  dest->data.drawing.type.figure.dash_array[0] = _unpackf(data + DDATA_DASHARRAY_INDEX, 4);
	  dest->data.drawing.type.figure.dash_array[1] = _unpackf(data + DDATA_DASHARRAY_INDEX + 4, 4);
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
 * A wrapper from read() C function 
 */
static size_t
_read_nb(int fd, size_t totalbytes, byte_t *buf, int *timeout) 
{
  size_t bytesleft = totalbytes;
  size_t bytesread = 0;
  fd_set readset;
  struct timeval to;

  int    flag = fcntl(fd, F_GETFL);

  fcntl(fd, F_SETFL, flag | O_NONBLOCK);

  *timeout = 0;

  while(bytesleft > 0 && *timeout != 1) 
    {

      FD_ZERO(&readset);
      FD_SET(fd, &readset);

      to.tv_sec  = 3;
      to.tv_usec = 0;

      int rc = select(fd + 1, &readset, NULL, NULL, &to);

      size_t bytes;
      if (rc > 0)
	bytes = read(fd, buf + bytesread, bytesleft);
      else if (rc == 0)
	*timeout = 1;
      else 
	bytes = 0; /* This means, stop accumulating bytes. */

      if (bytes > 0)
	{
	  bytesleft -= bytes;
	  bytesread += bytes;
	}
      else
	bytesleft = 0;
    }

  fcntl(fd, F_SETFL, flag);

  return bytesread;
}


/**
 * A wrapper from read() C function always in blocking mode. Returns the number of bytes
 * and places the read data in the output parameter buf.
 *
 * YOU SHOULD take care using this function and NOTICE that it will BLOCK your thread, and
 * also will set your file descriptor fd to blocking mode. So please, dont blame, if you
 * want non-blocking mode, write your own...
 */
static size_t
_read_b(int fd, size_t totalbytes, byte_t *buf) 
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
   
  if ( stream.header.cbyte != CTL_FAIL     && 
       stream.header.cbyte != CTL_NOPIPE   &&
       stream.header.cbyte != CTL_NOSTREAM &&
       stream.header.cbyte != CTL_CL_TIMEOUT )

    stream = tellapic_read_data_b(fd, stream.header);
  
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

  byte_t        *data  = malloc(HEADER_SIZE);
  int           timeout = 0;
  //byte_t        data[HEADER_SIZE];
  int           nbytes = _read_b(fd, HEADER_SIZE, data);
  header_t      header;

  if (timeout)
    {
      header.cbyte = CTL_CL_TIMEOUT;
      header.ssize = HEADER_SIZE;
    }
  else if (nbytes == HEADER_SIZE)
    {
      unsigned long ssize = _unpackul(data + SBYTE_INDEX, 4);
      if (ssize >= HEADER_SIZE && ssize <= MAX_STREAM_SIZE)
	{
	  header.endian = data[ENDIAN_INDEX];
	  header.cbyte  = data[CBYTE_INDEX];
	  header.ssize  = ssize;

	  /* This will check 2 things: */
	  /* First, if the header.cbyte corresponds with the header.ssize. */
	  /* And second, whether or not the header.cbyte is a valid value. */
	  if (!tellapic_ischatb(header) &&
	      !tellapic_ischatp(header) &&
	      !tellapic_isfig(header)   &&
	      !tellapic_isctle(header)  &&
	      !tellapic_isdrw(header)   &&
	      !tellapic_isfile(header)  &&
	      !tellapic_isctl(header)   &&
	      !tellapic_isping(header)  &&
	      !tellapic_ispong(header) 
	      ) 
	    {
	      //free(data);
	      printf("FAIl! read header. header.ssize: %d header.cbyte: %d\n", header.ssize, header.cbyte);
	      header.ssize = 0;
	      header.cbyte = CTL_NOSTREAM;
	      return header;
	    }
	}
    }
  else if (nbytes > 0)
    {
      /* We didn't complete reading what was expected */
      header.cbyte = CTL_NOSTREAM;
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
  free(data);

  return header;
}


/**
 *
 */
byte_t *
tellapic_read_bytes_b(int fd, size_t chunk)
{
  if (chunk <= 0)
    return NULL;

  byte_t *data = malloc(chunk);
  int p = 0;
  _read_b(fd, chunk, data);

  return data;
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
  //byte_t   data[datasize];
  int      timeout = 0;
  long     nbytes   = _read_b(fd, datasize, data);
  stream_t stream;


  if (timeout)
    {
      stream.header.cbyte = CTL_CL_TIMEOUT;
      stream.header.ssize = HEADER_SIZE;
    }
  /* This is important. It tells us that what header.ssize says is what we really read and     */
  /* what is indeed in the data buffer. So, is we move between 0 and header.ssize-HEADER_SIZE */
  /* we won't have SIGSEGV. This information is useful to _*cpy() functions.                  */
  else if (nbytes == 0) 
    {
      stream.header.cbyte = CTL_NOPIPE;
    }
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
	  _chatdatacpy(&stream, data, datasize);
	  break;

	case CTL_CL_FIG:
	case CTL_CL_DRW:
	  _figdatacpy(&stream, data, datasize);
	  break;

	case CTL_SV_CLIST:
	  break;

	case CTL_SV_CLADD:
	case CTL_CL_PWD:
	case CTL_CL_NAME:
	  _ctledatacpy(&stream, data, datasize);
	  break;

	case CTL_SV_FILE:
	  _filedatacpy(&stream, data, datasize);
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
	case CTL_CL_PING:
	case CTL_SV_PONG:
	  _ctldatacpy(&stream, data);
	  break;
	  
	default:
	  printf("FAIL over HERE!\n");
	  stream.header.cbyte = CTL_FAIL;
	  break;
	}
    } 
  else if (nbytes > 0)
    {
      stream.header.cbyte = CTL_NOSTREAM;
    }

  free(data);
  return stream;
}


/**
 *
 */
stream_t 
tellapic_read_stream_nb(int fd) 
{
  stream_t stream;

  stream.header = tellapic_read_header_nb(fd);
   
  if ( stream.header.cbyte != CTL_FAIL     && 
       stream.header.cbyte != CTL_NOPIPE   &&
       stream.header.cbyte != CTL_NOSTREAM &&
       stream.header.cbyte != CTL_CL_TIMEOUT )

    stream = tellapic_read_data_nb(fd, stream.header);
  
  return stream;
}


/**
 *
 */
header_t
tellapic_read_header_nb(int fd) 
{

  byte_t        *data  = malloc(HEADER_SIZE);
  int           timeout = 0;
  //byte_t        data[HEADER_SIZE];
  int           nbytes = _read_nb(fd, HEADER_SIZE, data, &timeout);
  header_t      header;

  if (timeout)
    {
      header.cbyte = CTL_CL_TIMEOUT;
      header.ssize = HEADER_SIZE;
    }
  else if (nbytes == HEADER_SIZE)
    {
      unsigned long ssize = _unpackul(data + SBYTE_INDEX, 4);
      if (ssize >= HEADER_SIZE && ssize <= MAX_STREAM_SIZE)
	{
	  header.endian = data[ENDIAN_INDEX];
	  header.cbyte  = data[CBYTE_INDEX];
	  header.ssize  = ssize;

	  /* This will check 2 things: */
	  /* First, if the header.cbyte corresponds with the header.ssize. */
	  /* And second, whether or not the header.cbyte is a valid value. */
	  if (!tellapic_ischatb(header) &&
	      !tellapic_ischatp(header) &&
	      !tellapic_isfig(header)   &&
	      !tellapic_isctle(header)  &&
	      !tellapic_isdrw(header)   &&
	      !tellapic_isfile(header)  &&
	      !tellapic_isctl(header)   &&
	      !tellapic_isping(header)  &&
	      !tellapic_ispong(header) 
	      ) 
	    {
	      //free(data);
	      printf("FAIl! read header. header.ssize: %d header.cbyte: %d\n", header.ssize, header.cbyte);
	      header.ssize = 0;
	      header.cbyte = CTL_NOSTREAM;
	      return header;
	    }
	}
    }
  else if (nbytes > 0)
    {
      /* We didn't complete reading what was expected */
      header.cbyte = CTL_NOSTREAM;
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
  free(data);

  return header;
}


/**
 *
 */
stream_t
tellapic_read_data_nb(int fd, header_t header) 
{
  long     datasize = header.ssize - HEADER_SIZE;
  byte_t   *data    = malloc(datasize);
  //byte_t   data[datasize];
  int      timeout = 0;
  long     nbytes   = _read_nb(fd, datasize, data, &timeout);
  stream_t stream;


  if (timeout)
    {
      stream.header.cbyte = CTL_CL_TIMEOUT;
      stream.header.ssize = HEADER_SIZE;
    }
  /* This is important. It tells us that what header.ssize says is what we really read and     */
  /* what is indeed in the data buffer. So, is we move between 0 and header.ssize-HEADER_SIZE */
  /* we won't have SIGSEGV. This information is useful to _*cpy() functions.                  */
  else if (nbytes == 0) 
    {
      stream.header.cbyte = CTL_NOPIPE;
    }
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
	  _chatdatacpy(&stream, data, datasize);
	  break;

	case CTL_CL_FIG:
	case CTL_CL_DRW:
	  _figdatacpy(&stream, data, datasize);
	  break;

	case CTL_SV_CLIST:
	  break;

	case CTL_SV_CLADD:
	case CTL_CL_PWD:
	case CTL_CL_NAME:
	  _ctledatacpy(&stream, data, datasize);
	  break;

	case CTL_SV_FILE:
	  _filedatacpy(&stream, data, datasize);
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
	case CTL_CL_PING:
	case CTL_SV_PONG:
	  _ctldatacpy(&stream, data);
	  break;
	  
	default:
	  printf("FAIL over HERE!\n");
	  stream.header.cbyte = CTL_FAIL;
	  break;
	}
    } 
  else if (nbytes > 0)
    {
      stream.header.cbyte = CTL_NOSTREAM;
    }

  free(data);
  return stream;
}


/**
 * Check if header is valid. If so, return the stream size. Else, return 0.
 */
int
_stream_header_ok(byte_t *header)
{
  /* get the stream size from the header section */
  unsigned long ssize = _unpackul(header + SBYTE_INDEX, 4);

  /* check if the stream size is upon valid values */
  if (ssize >= HEADER_SIZE && ssize <= MAX_STREAM_SIZE)
    {
      /* if so, then check whether or not the control byte and the stream size are ok */
      if ( !( header[CBYTE_INDEX] == CTL_CL_BMSG  && ssize <= MAX_BMSG_STREAM_SIZE   && ssize >= MIN_BMSG_STREAM_SIZE )
	   &&
	   !( header[CBYTE_INDEX] == CTL_CL_PMSG  && ssize <= MAX_PMSG_STREAM_SIZE   && ssize >= MIN_PMSG_STREAM_SIZE )
	   &&
	   !( header[CBYTE_INDEX] == CTL_CL_FIG   && ssize <= MAX_FIGTXT_STREAM_SIZE && ssize >= MIN_FIGTXT_STREAM_SIZE )
	   &&
	   !( header[CBYTE_INDEX] == CTL_CL_DRW   && (ssize == DRW_INIT_STREAM_SIZE || ssize == DRW_USING_STREAM_SIZE) )
	   &&
	   !( header[CBYTE_INDEX] == CTL_SV_FILE  && ssize <= MAX_STREAM_SIZE && ssize >= MIN_CTLEXT_STREAM_SIZE)
	   &&
	   !( (header[CBYTE_INDEX] == CTL_CL_PWD ||
	       header[CBYTE_INDEX] == CTL_CL_NAME ||
	       header[CBYTE_INDEX] == CTL_SV_CLADD) && ssize <= MAX_CTLEXT_STREAM_SIZE && ssize >= MIN_CTLEXT_STREAM_SIZE )
	   &&
	   !(( header[CBYTE_INDEX] == CTL_CL_FILEASK || 
	       header[CBYTE_INDEX] == CTL_CL_FILEOK  ||
	       header[CBYTE_INDEX] == CTL_SV_PWDFAIL ||
	       header[CBYTE_INDEX] == CTL_SV_PWDOK   ||
	       header[CBYTE_INDEX] == CTL_CL_CLIST   ||
	       header[CBYTE_INDEX] == CTL_CL_DISC    ||
	       header[CBYTE_INDEX] == CTL_SV_CLRM    ||
	       header[CBYTE_INDEX] == CTL_SV_ID      ||
	       header[CBYTE_INDEX] == CTL_SV_AUTHOK  ||
	       header[CBYTE_INDEX] == CTL_CL_PING    ||
	       header[CBYTE_INDEX] == CTL_SV_NAMEINUSE) && ssize == CTL_STREAM_SIZE )
	   )
	{
	  /* header and stream size are invalid */
	  return 0;
	}
      else
	{
	  /* header and ssize valids */
	  return ssize;
	}
    }
  return 0;
}





/**
 * Reads a stream an return it "as is" without wrapping it to a structure.
 * Useful for forwarding. data must be freed somewhere though.
 */
byte_t *
tellapic_rawread_b(int fd)
{
  
  byte_t        *header= malloc(HEADER_SIZE);
  byte_t        *data  = NULL;
  int           timeout = 0;
  int           nbytes = _read_nb(fd, HEADER_SIZE, header, &timeout);

  if (timeout)
    {
      free(header);
      free(data);
      return tellapic_build_rawstream(CTL_CL_TIMEOUT, fd);
    }

  if (nbytes == HEADER_SIZE)
    {
      int ssize =  _stream_header_ok(header);

      if (ssize != 0)
	{
	  int rbytes = ssize - HEADER_SIZE;
	  data = malloc(ssize);
	  memcpy(data, header, HEADER_SIZE);

	  nbytes = _read_nb(fd, rbytes, data + HEADER_SIZE, &timeout);

	  /* Check for errors. If someone is found, free the current data structure */
	  /* build a new data structure to inform such error, and return data.      */
	  if (timeout)
	    {
	      free(data);
	      data = tellapic_build_rawstream(CTL_CL_TIMEOUT, fd);
	    }
	  else if (nbytes == 0)
	    {
	      free(data);
	      data = tellapic_build_rawstream(CTL_NOPIPE, fd);
	    }
	  else if (nbytes < 0)
	    {
	      free(data);
	      data = tellapic_build_rawstream(CTL_FAIL, fd);
	    }
	  else if (nbytes > 0 && nbytes != rbytes)
	    {
	      free(data);
	      data = tellapic_build_rawstream(CTL_NOSTREAM, fd);
	    }
	}
    }
  else if (nbytes == 0)
    {
      data = tellapic_build_rawstream(CTL_NOPIPE, fd);
    }
  else if (nbytes > 0)
    {
      data = tellapic_build_rawstream(CTL_NOSTREAM, fd);
    }
  else
    {
      data = tellapic_build_rawstream(CTL_FAIL, fd);
    }
  
  //free(header);

  return data;
}


/**
 * pwd could and should be NULL.
 */
char *
tellapic_read_pwd(int fd, char *pwd, int *len)
{
  //byte_t     header[HEADER_SIZE];
  byte_t     *header  = malloc(HEADER_SIZE);
  int        timeout = 0;
  int        nbytes   = _read_nb(fd, HEADER_SIZE, header, &timeout);
  stream_t   stream;  /* This is used only as a placeholder. Avoid it for future release, use instead local variables*/

  *len = 0;
  
  if (timeout)
    return pwd;

  stream.header.cbyte = CTL_FAIL;

  if (nbytes == HEADER_SIZE && header[CBYTE_INDEX] == CTL_CL_PWD) 
    {
      stream.header.endian = header[ENDIAN_INDEX];
      stream.header.cbyte  = header[CBYTE_INDEX];
      stream.header.ssize  = _unpackul(header + SBYTE_INDEX, 4);
 
      if (stream.header.ssize <= MAX_CTLEXT_STREAM_SIZE && stream.header.ssize >= MIN_CTLEXT_STREAM_SIZE)
	{
	  byte_t *data = malloc(stream.header.ssize - HEADER_SIZE);
	  //byte_t data[stream.header.ssize - HEADER_SIZE];
	  int    error = 0;
	  nbytes = _read_nb(fd, stream.header.ssize - HEADER_SIZE, data, &timeout);

	  if (nbytes == stream.header.ssize - HEADER_SIZE && timeout == 0)
	    {
	      _ctledatacpy(&stream, data, nbytes);
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
tellapic_rawsend(int fd, byte_t *rawstream)
{
  unsigned long ssize = _unpackul(rawstream + SBYTE_INDEX, 4);

  int r = send(fd, rawstream, ssize, 0);
  
  return r;
}


/**
 * TODO: this is not finished nor updated. 
 */
int 
tellapic_send(int socket, stream_t *stream) 
{

  byte_t *rawstream = malloc(stream->header.ssize);
  
  /* Copy the header to the raw stream data */
  _setheader(rawstream, stream->header);
  int cbyte = stream->header.cbyte;

  switch(cbyte)
    {
    case  CTL_CL_PMSG:
      _setidfrom(rawstream, stream->data.chat.idfrom);
      _setidto(rawstream, stream->data.chat.type.privmsg.idto);
      memcpy(rawstream + DATA_PMSG_TEXT_INDEX + HEADER_SIZE, stream->data.chat.type.privmsg.text, stream->header.ssize - HEADER_SIZE - 2);
      break;

    case CTL_CL_BMSG:
      _setidfrom(rawstream, stream->data.chat.idfrom);
      memcpy(rawstream + DATA_BMSG_TEXT_INDEX +HEADER_SIZE, stream->data.chat.type.broadmsg, stream->header.ssize - HEADER_SIZE - 1);
      break;

    case CTL_CL_FIG:
      _setddata(rawstream, *stream);
      break;

    case CTL_CL_DRW:
      if (stream->header.ssize == DRW_INIT_STREAM_SIZE)
	_setddata_init(rawstream, *stream);
      else
	_setddata_using(rawstream, *stream);
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
      _setidfrom(rawstream, stream->data.control.idfrom);
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

  _setendian(rawstream);
  _setcbyte(rawstream, CTL_SV_FILE);
  _setssize(rawstream, filesize + HEADER_SIZE);

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

  _setendian(rawstream);
  _setcbyte(rawstream, CTL_CL_FIG);
  _setdcbyte(rawstream, TOOL_TEXT | EVENT_NULL);
  _setssize(rawstream, ssize);
  _setidfrom(rawstream, idfrom);
  _setdnum(rawstream, dnum);
  _setwidth(rawstream, w);
  _setopacity(rawstream, op);
  _setred(rawstream, red);
  _setgreen(rawstream, green);
  _setblue(rawstream, blue);
  _setx1(rawstream, x1);
  _sety1(rawstream, y1);
  _settextstyle(rawstream, style);
  _setfacelen(rawstream, facelen);

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

  _setendian(rawstream);
  _setcbyte(rawstream, CTL_CL_DRW);
  _setdcbyte_ext(rawstream, dcbyte_ext);
  _setssize(rawstream, DRW_USING_STREAM_SIZE);
  _setidfrom(rawstream, idfrom);
  _setdcbyte(rawstream, tool);
  _setdnum(rawstream, dnum);
  _setwidth(rawstream, w);
  _setopacity(rawstream, op);
  _setred(rawstream, red);
  _setgreen(rawstream, green);
  _setblue(rawstream, blue);
  _setx1(rawstream, x1);
  _sety1(rawstream, y1);

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

  _setendian(rawstream);
  _setcbyte(rawstream, CTL_CL_DRW);
  _setdcbyte_ext(rawstream, dcbyte_ext);
  _setssize(rawstream, DRW_INIT_STREAM_SIZE);
  _setidfrom(rawstream, idfrom);
  _setdcbyte(rawstream, tool);
  _setdnum(rawstream, dnum);
  _setwidth(rawstream, w);
  _setopacity(rawstream, op);
  _setred(rawstream, red);
  _setgreen(rawstream, green);
  _setblue(rawstream, blue);
  _setx1(rawstream, x1);
  _sety1(rawstream, y1);
  _setx2(rawstream, x2);
  _sety2(rawstream, y2);
  _setjoins(rawstream, lj);
  _setcaps(rawstream, ec);
  _setml(rawstream, ml);
  _setdp(rawstream, dp);
  _setda(rawstream, da);

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

  _setendian(rawstream);
  _setcbyte(rawstream, CTL_CL_FIG);
  _setdcbyte_ext(rawstream, dcbyte_ext);
  _setssize(rawstream, FIG_STREAM_SIZE);
  _setidfrom(rawstream, idfrom);
  _setdcbyte(rawstream, tool);
  _setdnum(rawstream, dnum);
  _setwidth(rawstream, w);
  _setopacity(rawstream, op);
  _setred(rawstream, red);
  _setgreen(rawstream, green);
  _setblue(rawstream, blue);
  _setx1(rawstream, x1);
  _sety1(rawstream, y1);
  _setx2(rawstream, x2);
  _sety2(rawstream, y2);
  _setjoins(rawstream, lj);
  _setcaps(rawstream, ec);
  _setml(rawstream, ml);
  _setdp(rawstream, dp);
  _setda(rawstream, da);

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

  _setendian(rawstream);
  _setcbyte(rawstream, CTL_CL_PMSG);
  _setssize(rawstream, ssize);
  _setidfrom(rawstream, idfrom);
  _setidto(rawstream, idto);

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

  _setendian(rawstream);
  _setcbyte(rawstream, CTL_CL_BMSG);
  _setssize(rawstream, ssize);
  _setidfrom(rawstream, idfrom);

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

  _setendian(rawstream);
  _setcbyte(rawstream, ctle);
  _setssize(rawstream, ssize);
  _setidfrom(rawstream, idfrom);
  
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

  _setendian(rawstream);
  _setcbyte(rawstream, ctl);
  _setssize(rawstream, CTL_STREAM_SIZE);
  _setidfrom(rawstream, idfrom);

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
byte_t *
tellapic_build_rawstream(byte_t ctlbyte, ...)
{
  /* The resulting stream */
  byte_t  *outstream = NULL;

  /* Initialize the variable argument list */
  va_list argp;
  va_start(argp, ctlbyte);

  switch(ctlbyte)
    {

      /* CTL streams */
    case CTL_FAIL:
    case CTL_NOSTREAM:
    case CTL_NOPIPE:
    case CTL_CL_FILEASK:
    case CTL_CL_FILEOK:
    case CTL_SV_PWDFAIL:
    case CTL_SV_PWDOK:
    case CTL_CL_CLIST:
    case CTL_CL_DISC:
    case CTL_SV_CLRM:
    case CTL_SV_ID:
    case CTL_SV_AUTHOK:
    case CTL_SV_NAMEINUSE:
    case CTL_SV_PONG:
    case CTL_CL_PING:
    case CTL_CL_TIMEOUT:
      {
	int id       = va_arg(argp, int);
	outstream    = malloc(CTL_STREAM_SIZE);
	_setendian(outstream);
	_setcbyte(outstream, ctlbyte);
	_setssize(outstream, CTL_STREAM_SIZE);
	_setidfrom(outstream, id);
      }
      break;


      /* A file headed stream */
    case CTL_SV_FILE: //TODO: ERROR CHECK!!
      {
	FILE    *file  = va_arg(argp, FILE *);
	int     nbytes = 0;

	fseek(file, 0L, SEEK_END);
	nbytes = ftell(file) + HEADER_SIZE;
	if (nbytes > HEADER_SIZE && nbytes <= MAX_FILE_SIZE)
	  {
	    outstream = malloc(nbytes);
	    _setendian(outstream);
	    _setcbyte(outstream, ctlbyte);
	    _setssize(outstream, nbytes);
	    
	    rewind(file);
	    fread(outstream + HEADER_SIZE, sizeof(byte_t), nbytes - HEADER_SIZE, file);
	    rewind(file);
	  }
      }
      break;


      /* CTL extended streams */
    case CTL_CL_PWD:
    case CTL_CL_NAME:
    case CTL_SV_CLADD:
      {
	int           id      = va_arg(argp, int);
	unsigned char *info   = va_arg(argp, unsigned char *);
	int           infolen = va_arg(argp, int);

	if (infolen > 0 && infolen + HEADER_SIZE <= MAX_CTLEXT_STREAM_SIZE)
	  {
	    outstream = malloc(infolen + HEADER_SIZE + 1);
	    _setendian(outstream);
	    _setcbyte(outstream, ctlbyte);
	    _setssize(outstream, infolen + HEADER_SIZE + 1);
	    _setidfrom(outstream, id);
	
	    memcpy(outstream + HEADER_SIZE + 1, info, infolen);

	  }

      }
      break;

    default:
      //ERROR
      break;
    }
  va_end(argp);

  return outstream;
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
 *
 */
int
tellapic_istimeout(header_t header)
{
  return header.cbyte == CTL_CL_TIMEOUT;
}


/**
 *
 */
int
tellapic_isping(header_t header)
{
  return header.cbyte == CTL_CL_PING;
}


/**
 *
 */
int
tellapic_ispong(header_t header)
{
  return header.cbyte == CTL_SV_PONG;
}


/**
 * Returns a non-zero value if 'stream' is a broadcast message.
 */
int
tellapic_ischatb(header_t header) 
{

  return (header.cbyte == CTL_CL_BMSG && header.ssize <= MAX_BMSG_STREAM_SIZE   && header.ssize >= MIN_BMSG_STREAM_SIZE);

}


/**
 * Returns a non-zero value if 'stream' is a private message.
 */
int
tellapic_ischatp(header_t header) 
{

  return (header.cbyte == CTL_CL_PMSG && header.ssize <= MAX_PMSG_STREAM_SIZE   && header.ssize >= MIN_PMSG_STREAM_SIZE);

}


/**
 * Returns a non-zero value if 'stream' is a control stream.
 */
int 
tellapic_isctl(header_t header)
{

  return ((header.cbyte == CTL_CL_FILEASK ||
	   header.cbyte == CTL_CL_FILEOK  ||
	   header.cbyte == CTL_SV_PWDFAIL ||
	   header.cbyte == CTL_SV_PWDOK   ||
	   header.cbyte == CTL_CL_CLIST   ||
	   header.cbyte == CTL_CL_DISC    ||
	   header.cbyte == CTL_SV_CLRM    ||
	   header.cbyte == CTL_SV_ID      ||
	   header.cbyte == CTL_SV_AUTHOK  ||
	   header.cbyte == CTL_SV_NAMEINUSE) && header.ssize == CTL_STREAM_SIZE);

}


/**
 * Returns a non-zero value if 'stream' is an extended control stream.
 */
int 
tellapic_isctle(header_t header) 
{

  return ((header.cbyte == CTL_CL_PWD   ||
	   header.cbyte == CTL_CL_NAME  ||
	   header.cbyte == CTL_SV_CLADD) && header.ssize <= MAX_CTLEXT_STREAM_SIZE && header.ssize >= MIN_CTLEXT_STREAM_SIZE) ;

}


/**
 *
 */
int
tellapic_isfile(header_t header)
{

  return (header.cbyte == CTL_SV_FILE && header.ssize <= MAX_STREAM_SIZE && header.ssize >= MIN_CTLEXT_STREAM_SIZE);

}


/**
 * Returns a non-zero value if 'stream' is a drawing packet.
 */
int 
tellapic_isdrw(header_t header) 
{

  return (header.cbyte == CTL_CL_DRW  && (header.ssize == DRW_INIT_STREAM_SIZE || header.ssize == DRW_USING_STREAM_SIZE));

}


/**
 * Returns a non-zero value if 'stream' is a text drawn figure.
 */
int 
tellapic_isfigtxt(stream_t stream) 
{

  return (stream.header.cbyte == CTL_CL_FIG &&  (stream.data.drawing.dcbyte & TOOL_MASK) == TOOL_TEXT && stream.header.ssize <= MAX_FIGTXT_STREAM_SIZE && stream.header.ssize >= MIN_FIGTXT_STREAM_SIZE);

}


/**
 * Returns a non-zero value if 'stream' is a drawn figure.
 */
int 
tellapic_isfig(header_t header) 
{

  return (header.cbyte == CTL_CL_FIG && header.ssize <= MAX_FIGTXT_STREAM_SIZE && header.ssize >= MIN_FIGTXT_STREAM_SIZE);

}
