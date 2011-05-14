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
#include <errno.h>

#define POSH_BUILDING_LIB

#include "tellapic/types.h"
#include "tellapic/constants.h"
#include "tellapic/tellapic.h"

#if defined (linux) || defined (LINUX)
#  include <unistd.h>
#  include <sys/socket.h>
#elif defined (_WIN32) || defined (WIN32)
#  include <windows.h>
#endif

POSH_PUBLIC_API(size_t)
_read_b(tellapic_socket_t socket, size_t totalbytes, byte_t *buf);

POSH_PUBLIC_API(size_t)
_read_nb(tellapic_socket_t socket, size_t totalbytes, byte_t *buf);


/**
 *
 * _copy_* functions will copy data from a structure to a memory portion, 
 *         something like: _copy_<name>(void * stream, struct t).
 *
 * _read_<data|header|stream>_* functions will read a specific byte, bytes or structure from a memory portion moving the pointer forward,
 *         something like: _read_data_<name>(void * stream).
 *
 * _wrap_* functions will wrap memory to a structure,
 *         something like: _wrap_<name>(struct *dest, void *stream, ...)
 *
 * _set_* functions will set specific values in a structure.
 *         something like: _set_<name>(struct *t, type value).
 *
 * _get_* functions will get specific values from a structure.
 *        something like: _get_<name>(struct *t).
 *
 */



/**
 *
 */
static void
_set_header_endian(header_t *header, byte_t endian)
{
  header->endian = endian;
}


/**
 *
 */
static void
_set_header_cbyte(header_t *header, byte_t cbyte)
{
  header->cbyte = cbyte;
}


/**
 *
 */
static void
_set_header_ssize(header_t *header, tellapic_u32_t size)
{
#ifdef LITTLE_ENDIAN_VALUE
  header->ssize = POSH_LittleU32(size);
#else
  header->ssize = POSH_BigU32(size);
#endif
}


/**
 *
 */
/* static byte_t */
/* _get_header_endian(header_t *header) */
/* { */
/*   return header->endian; */
/* } */


/* /\** */
/*  * */
/*  *\/ */
/* static byte_t */
/* _get_header_cbyte(header_t *header) */
/* { */
/*   return header->cbyte; */
/* } */


/**
 *
 */
static tellapic_u32_t
_get_header_ssize(header_t *header)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_LittleU32(header->ssize);
#else
  return POSH_LittleU32(header->ssize);
#endif
}


/**
 *
 */
static void *
WriteBytes(byte_t *p, const void *bytes, tellapic_u32_t len)
{
  memcpy(p, bytes, len);

  p += len;

  return p;
}


/**
 *
 */
static void *
WriteByte(byte_t *p, byte_t byte)
{
  return WriteBytes(p, &byte, 1);
}


/**
 * Copies the text_t structure to the rawstream pointer
 * 
 * Returns the pointer
 */
static void*
_copy_text_data(byte_t *rawstream, text_t text)
{
  void *pointer = rawstream;

  pointer = WriteByte(pointer, text.style);
  pointer = WriteByte(pointer, text.facelen);
  pointer = WriteBytes(pointer, text.face, text.facelen);
  pointer = WriteBytes(pointer, text.info, text.infolen);

  return pointer;
}


/**
 * Copies a message_t structure to the rawstream pointer.
 * 
 * Returns the pointer
 */
static void *
_copy_privchat_data(byte_t *rawstream, message_t message, tellapic_u32_t ssize)
{
  void *pointer = rawstream;
	
  pointer = WriteByte(pointer, message.idfrom);
  pointer = WriteByte(pointer, message.type.privmsg.idto);
  pointer = WriteBytes(pointer, message.type.privmsg.text, ssize - HEADER_SIZE - 2);
	
  return pointer;
}


/**
 * Copies a message_t structure to the rawstream pointer.
 * 
 * Returns the pointer
 */
static void *
_copy_broadchat_data(byte_t *rawstream, message_t message, tellapic_u32_t ssize)
{
  void *pointer = rawstream;
	
  pointer = WriteByte(pointer, message.idfrom);
  pointer = WriteBytes(pointer, message.type.broadmsg, ssize - HEADER_SIZE - 1);
	
  return pointer;
}


/**
 * 
 */
static void *
_copy_file_data(byte_t *rawstream, byte_t *file, tellapic_u32_t size)
{
  void *pointer = rawstream;
  
  pointer = WriteBytes(pointer, file, size);
  
  return pointer;
}


/**
 * Copies the color_t structure to the rawstream pointer
 * 
 * Returns the pointer
 */
static void *
_copy_drawing_color(byte_t *rawstream, color_t color)
{
  void *pointer = rawstream;
  
  pointer = WriteByte(pointer, color.red);
  pointer = WriteByte(pointer, color.green);
  pointer = WriteByte(pointer, color.blue);
  
  return pointer;
}


/**
 * Copies the point_t structure to the rawstream pointer
 * 
 * Returns the pointer
 */
static void *
_copy_drawing_point(byte_t *rawstream, point_t point)
{
  void *pointer = rawstream;
  
#ifdef LITTLE_ENDIAN_VALUE
  pointer = POSH_WriteU16ToLittle(pointer, point.x);
  pointer = POSH_WriteU16ToLittle(pointer, point.y);
#else
  pointer = POSH_WriteU16ToBig(pointer, point.x);
  pointer = POSH_WriteU16ToBig(pointer, point.y);
#endif
  
  return pointer;
}


/**
 * Copies the figure_t structure to the rawstream pointer
 * 
 * Returns the pointer
 */
static void*
_copy_drawing_figure(byte_t *rawstream, figure_t figure)
{
  void *pointer = rawstream;
  
  pointer = _copy_drawing_point(pointer, figure.point2);
  pointer = WriteByte(pointer, figure.linejoin);
  pointer = WriteByte(pointer, figure.endcaps);
#ifdef LITTLE_ENDIAN_VALUE
  pointer = POSH_WriteU32ToLittle(pointer, figure.miterlimit);
  pointer = POSH_WriteU32ToLittle(pointer, figure.dash_phase);
  pointer = POSH_WriteU32ToLittle(pointer, figure.dash_array[0]);
  pointer = POSH_WriteU32ToLittle(pointer, figure.dash_array[1]);  
#else
  pointer = POSH_WriteU32ToBig(pointer, figure.miterlimit);
  pointer = POSH_WriteU32ToBig(pointer, figure.dash_phase);
  pointer = POSH_WriteU32ToBig(pointer, figure.dash_array[0]);
  pointer = POSH_WriteU32ToBig(pointer, figure.dash_array[1]);
#endif
  
  return pointer;
}


/**
 * Copies the drawing structure ddata_t to the rawstream pointer
 * 
 * Returns the pointer
 */
static void *
_copy_drawing(byte_t *rawstream, ddata_t drawing)
{
  void *pointer = rawstream;
  
  pointer = WriteByte(pointer, drawing.idfrom);
  pointer = WriteByte(pointer, drawing.dcbyte);
  pointer = WriteByte(pointer, drawing.dcbyte_ext);
#ifdef LITTLE_ENDIAN_VALUE
  pointer = POSH_WriteU32ToLittle(pointer, drawing.number);
  pointer = POSH_WriteU32ToLittle(pointer, drawing.width);
  pointer = POSH_WriteU32ToLittle(pointer, drawing.opacity);
#else
  pointer = POSH_WriteU32ToBig(pointer, drawing.number);
  pointer = POSH_WriteU32ToBig(pointer, drawing.width);
  pointer = POSH_WriteU32ToBig(pointer, drawing.opacity);  
#endif
  pointer = _copy_drawing_color(pointer, drawing.color);
  pointer = _copy_drawing_point(pointer, drawing.point1);
  
  return pointer;
}


/**
 * Copies the header section structure header 
 * to the header raw bytes.
 */
static void *
_copy_header_data(byte_t *rawheader, header_t header) 
{
  void *pointer = rawheader;
  
  pointer = WriteByte(pointer, header.endian);
  pointer = WriteByte(pointer, header.cbyte);
  
#ifdef LITTLE_ENDIAN_VALUE
  pointer = POSH_WriteU32ToLittle(pointer, header.ssize);
#else
  pointer = POSH_WriteU32ToBig(pointer, header.ssize);
#endif
  
  return pointer;
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
static void *
_copy_drawing_data(byte_t *rawstream, stream_t stream)
{
  int etype  = stream.data.drawing.dcbyte & EVENT_MASK;
  int tool   = stream.data.drawing.dcbyte & TOOL_MASK;

  /* Copy the data section fixed part */
  void *pointer = rawstream;
  pointer = _copy_drawing(rawstream, stream.data.drawing);

  if (etype == EVENT_NULL || etype == EVENT_PRESS) 
    {
      /* EVENT_NULL means that we are copying a deferred drawing. */
      /* Deferred drawing could be text or something else.        */
      /* Text has different kind of information over the stream.  */
      /* Also a initiated Tool in direct mode has the same meaning*/
      /* as a deferred drawing, but different when EVENT_RELEASE  */
      /* or EVENT_DRAG.                                           */
      if (tool != TOOL_TEXT) 
        pointer = _copy_drawing_figure(pointer, stream.data.drawing.type.figure);

      else
        /* Treat text different */
        pointer = _copy_text_data(pointer, stream.data.drawing.type.text);
    }
  else
    {
      /* Deferred mode with event EVENT_DRAG or EVENT_RELEASE */
    }

  return pointer;
}


/**
 * 
 */
static void *
_copy_drawing_using(byte_t *rawstream, stream_t stream)
{
  void *pointer = rawstream;
  
  pointer = _copy_drawing(pointer, stream.data.drawing);
  
  return pointer;
}


/**
 * Copies drawing data to the raw stream.
 */
static void *
_copy_drawing_init(byte_t *rawstream, stream_t stream)
{
  void *pointer = rawstream;
  
  pointer = _copy_drawing(pointer, stream.data.drawing);
  pointer = _copy_drawing_figure(pointer, stream.data.drawing.type.figure);
  
  return pointer;
}


/**
 * 
 */
static void *
_copy_control_extended_data(byte_t *rawstream, svcontrol_t control, tellapic_u32_t len)
{
  void *pointer = rawstream;

  pointer = WriteByte(pointer, control.idfrom);
  pointer = WriteBytes(pointer, control.info, len);

  return pointer;
}


/**
 * 
 */
static void *
_copy_control_data(byte_t *rawstream, svcontrol_t control)
{
  void *pointer = rawstream;

  pointer = WriteByte(pointer, control.idfrom);

  return pointer;
}


/**
 *
 */
static tellapic_u32_t
_read_header_ssize(byte_t *header)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU32FromLittle(header + SBYTE_INDEX);
#else
  return POSH_ReadU32FromBig(header + SBYTE_INDEX);
#endif
}


/**
 *
 */
static byte_t
_read_data_idfrom(byte_t *data)
{
  return data[DATA_IDFROM_INDEX];
}


/**
 *
 */
static byte_t
_read_data_idto(byte_t *data)
{
  return data[DATA_PMSG_IDTO_INDEX];
}


/**
 *
 */
static byte_t
_read_data_dcbyte(byte_t *data)
{
  return data[DDATA_DCBYTE_INDEX];
}


/**
 *
 */
static byte_t
_read_data_dcbyte_ext(byte_t *data)
{
  return data[DDATA_DCBYTE_EXT_INDEX];
}


/**
 *
 */
static tellapic_u16_t
_read_data_point1_x(byte_t *data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU16FromLittle(data + DDATA_COORDX1_INDEX);
#else
  return POSH_ReadU16FromBig(data + DDATA_COORDX1_INDEX);
#endif
}


/**
 *
 */
static tellapic_u16_t
_read_data_point1_y(byte_t *data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU16FromLittle(data + DDATA_COORDY1_INDEX);
#else
  return POSH_ReadU16FromBig(data + DDATA_COORDY1_INDEX);
#endif
}


/**
 *
 */
static tellapic_u16_t
_read_data_point2_x(byte_t *data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU16FromLittle(data + DDATA_COORDX2_INDEX);
#else
  return POSH_ReadU16FromBig(data + DDATA_COORDX2_INDEX);
#endif
}


/**
 *
 */
static tellapic_u16_t
_read_data_point2_y(byte_t *data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU16FromLittle(data + DDATA_COORDY2_INDEX);
#else
  return POSH_ReadU16FromBig(data + DDATA_COORDY2_INDEX);
#endif
}


/**
 *
 */
static tellapic_u32_t
_read_data_number(byte_t *data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU32FromLittle(data + DDATA_DNUMBER_INDEX);
#else
  return POSH_ReadU32FromBig(data + DDATA_DNUMBER_INDEX);
#endif
}


/**
 *
 */
static tellapic_u32_t
_read_data_width(byte_t *data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU32FromLittle(data + DDATA_WIDTH_INDEX);
#else
  return POSH_ReadU32FromBig(data + DDATA_WIDTH_INDEX);
#endif
}


/**
 *
 */
static tellapic_u32_t
_read_data_opacity(byte_t *data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU32FromLittle(data + DDATA_OPACITY_INDEX);
#else
  return POSH_ReadU32FromBig(data + DDATA_OPACITY_INDEX);
#endif
}


/**
 *
 */
static byte_t
_read_data_color_red(byte_t *data)
{
  return data[DDATA_COLOR_INDEX];
}


/**
 *
 */
static byte_t
_read_data_color_green(byte_t *data)
{
  return data[DDATA_COLOR_INDEX + 1];
}


/**
 *
 */
static byte_t
_read_data_color_blue(byte_t *data)
{
  return data[DDATA_COLOR_INDEX + 2];
}


/**
 *
 */
static tellapic_u32_t
_read_data_miter_limit(byte_t * data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU32FromLittle(data + DDATA_MITER_INDEX);
#else
  return POSH_ReadU32FromBig(data + DDATA_MITER_INDEX);
#endif
}


/**
 *
 */
static tellapic_u32_t
_read_data_dash_phase(byte_t * data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU32FromLittle(data + DDATA_DASHPHASE_INDEX);
#else
  return POSH_ReadU32FromBig(data + DDATA_DASHPHASE_INDEX);
#endif
}


/**
 *
 */
static tellapic_u32_t
_read_data_dash_array0(byte_t * data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU32FromLittle(data + DDATA_DASHARRAY_INDEX);
#else
  return POSH_ReadU32FromBig(data + DDATA_DASHARRAY_INDEX);
#endif
}


/**
 *
 */
static tellapic_u32_t
_read_data_dash_array1(byte_t * data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU32FromLittle(data + DDATA_DASHARRAY_INDEX + 4);
#else
  return POSH_ReadU32FromBig(data + DDATA_DASHARRAY_INDEX + 4);
#endif
}


/**
 *
 */
static byte_t
_read_data_line_join(byte_t * data)
{
  return data[DDATA_JOINS_INDEX];
}


/**
 *
 */
static byte_t
_read_data_end_caps(byte_t * data)
{
  return data[DDATA_CAPS_INDEX];
}


/**
 *
 */
static byte_t
_read_data_facelen(byte_t * data)
{
  return data[DDATA_FONTFACELEN_INDEX];
}

/**
 *
 */
static byte_t
_read_data_text_style(byte_t * data)
{
  return data[DDATA_FONTSTYLE_INDEX];
}


/**
 *
 */
static tellapic_u16_t
_read_data_text_len(byte_t * data)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU16FromLittle(data + DDATA_TEXTLEN_INDEX);
#else
  return POSH_ReadU16FromBig(data + DDATA_TEXTLEN_INDEX);
#endif
}


/**
 *
 */
static tellapic_u32_t
_read_stream_size(byte_t * stream)
{
  tellapic_u32_t size = 0;

#ifdef LITTLE_ENDIAN_VALUE
  size = POSH_ReadU32FromLittle(stream + SBYTE_INDEX);
#else
  size = POSH_ReadU32FromBig(stream + SBYTE_INDEX);
#endif

  return size;
}


/**
 *
 */
static byte_t
_read_stream_endian(byte_t *stream)
{
  return stream[ENDIAN_INDEX];
}

/**
 *
 */
static byte_t
_read_stream_cbyte(byte_t *stream)
{
  return stream[CBYTE_INDEX];
}


/**
 *
 */
static void
_wrap_stream_header(header_t *header, byte_t *stream)
{
  header->endian = _read_stream_endian(stream);
  header->cbyte  = _read_stream_cbyte(stream);
  header->ssize  = _read_stream_size(stream);

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
      printf("FAIl! read header. header.ssize: %d header.cbyte: %d\n", header->ssize, header->cbyte);
      _set_header_ssize(header, 0);
      _set_header_cbyte(header, CTL_NOSTREAM);
    }
}


/* /\** */
/*  * The best I can do. This is not portable as some systems may */
/*  * represents floating point with 64bit or with a 80bit precision. */
/*  * */
/*  * Despite that, system representations of floating points may */
/*  * differ having the same representation size. */
/*  * */
/*  * This is for create a stream. This library creates a stream, so */
/*  * we will create and pack every number in big endian (MSB First) or network byte order. */
/*  * */
/*  * The value variable will have the most significative byte in its first */
/*  * index for BIG_ENDIAN systems. As streams are sent over the network as */
/*  * a bunch of bytes: [byte0][byte1][byte2][byte3][byte4][byte5] inserting */
/*  * a 32bit floating point starting at stream byte2 will result in copying */
/*  * the MSB to byte2, then the next less significative byte to byte3 and so */
/*  * on until the less copying the LSB into byte5. That is: */
/*  * [byte2] = value[0]; */
/*  * [byte3] = value[1]; */
/*  * [byte4] = value[2]; */
/*  * [byte5] = value[3]; */
/*  * */
/*  * For little endian system, we then put the MSB first so to adapt it to */
/*  * the protocol. The first byte es the LSB in little endian systems, so */
/*  * the MSB will be the last index: */
/*  * [byte2] = value[3]; */
/*  * [byte3] = value[2]; */
/*  * [byte4] = value[1]; */
/*  * [byte5] = value[0]; */
/*  * */
/*  * This _pack() helper function should be used with caution. It is supposed */
/*  * to be used as a particular helper function with the most generality as it can. */
/*  *  */
/*  * stream must be a valid pointer and size should be the size of the library custom protocol */
/*  * defined type we want to wrap to stream. In particular, size should be the size */
/*  * of the value type. Value actual size could be bigger than size. */
/*  * */
/*  * This helper function can be used to put an unsigned char (byte_t) value in the stream as follows: */
/*  *  */
/*  * byte_t someByte = SOME_VALUE; */
/*  * _pack(stream + SOME_INDEX_IN_THE_STREAM, (void *)&someByte, sizeof(byte_t)); */
/*  * */
/*  * NOTE: Try avoiding the use of sizeof(). As we are creating our byte representation in the stream */
/*  * use the proper size of the type you want to pack. For example, coordinates in the stream are */
/*  * 2 bytes long, but can be held in an integer. Then you should do: */
/*  * */
/*  * int x1 = 129; */
/*  * _pack(stream + X1_INDEX, (void *)&x1, 2); // 2 or a defined constant MACRO (e.g. COORDINATE_TYPE_SIZE) */
/*  *\/ */
/* static void */
/* _pack(byte_t *stream, void *value, size_t size) */
/* { */
/*   byte_t *fval = (byte_t *) value; */
/*   int i = 0; */
  
/* #if (BIG_ENDIAN_VALUE) */

/*   for(i=0; i < size; i++) */
/*     stream[i] = fval[i]; */

/* #else */

/*   for(i=0; i < size; i++) */
/*     stream[i] = fval[size - i - 1]; */

/* #endif  */

/* } */


/* /\** */
/*  * */
/*  *\/ */
/* static void */
/* _packul(byte_t *stream, unsigned long value, size_t size) */
/* { */
/*   byte_t *fval = (byte_t *)&value; */
/*   int i = 0; */
  
/* #if (BIG_ENDIAN_VALUE) */

/*   for(i=0; i < size; i++) */
/*     stream[i] = fval[i]; */

/* #else */

/*   for(i=0; i < size; i++) */
/*     stream[i] = fval[size - i - 1]; */

/* #endif  */

/* } */


/* /\** */
/*  * */
/*  *\/ */
/* static void */
/* _packf(byte_t *stream, float value, size_t size) */
/* { */
/*   byte_t *fval = (byte_t *)&value; */
/*   int i = 0; */
  
/* #if (BIG_ENDIAN_VALUE) */

/*   for(i=0; i < size; i++) */
/*     stream[i] = fval[i]; */

/* #else */

/*   for(i=0; i < size; i++) */
/*     stream[i] = fval[size - i - 1]; */

/* #endif  */

/* } */


/* /\** */
/*  * So now we have a stream. The stream was created by this library. Numbers were */
/*  * packed with endianness in mind and the stream has in the header.endian section */
/*  * the information that concers about endianness. */
/*  * */
/*  * Decision was to send use MSB in the protocol and not the system native endianness. */
/*  * This will decrease the overhead of consulting the endian byte, and swapping the */
/*  * correspondent bytes. */
/*  * */
/*  * The variable 'stream' here should be a portion of memory where we want to */
/*  * unpack a float from, and not a whole stream.  */
/*  * */
/*  * What we have now in the first index of the stream is a MSB from a 32bit */
/*  * floating point. It depends upon the system endiannes to build a correte floating point number. */
/*  * */
/*  * So, if the system is BIG_ENDIAN we just copy the same order to the floating */
/*  * point number memory. If the system is LITTLE_ENDIAN, we reverse the bytes as */
/*  * we already know that the protocol is implemented with MSB first. */
/*  *  */
/*  * Same WARNINGS apply here as in _pack() function. If you can understand _pack() there is no */
/*  * more to say. If you don't, I can't be more expressive than that. */
/*  * */
/*  * ALWAYS take note that size is a STREAM CUSTOM PROTOCOL TYPE SIZE. That is, not a language */
/*  * standard size. If you want to _unpack() a coordinate from the stream, then you could do: */
/*  * */
/*  * int x1 = *(int *) _unpack(stream + X1_INDEX, COORDINATE_TYPE_SIZE); */
/*  * */
/*  *\/ */
/* static void * */
/* _unpack(const byte_t *stream, size_t size) */
/* { */
/*   byte_t *buf = malloc(size); */
/*   int i = 0; */

/* #if (BIG_ENDIAN_VALUE) */

/*   for(i=0; i < size; i++) */
/*     buf[i] = stream[i]; */

/* #else */

/*   for(i=0; i < size; i++) */
/*     buf[i] = stream[size - i - 1]; */

/* #endif */

/*   return (void *)buf; */
/* } */


/* /\** */
/*  * */
/*  *\/ */
/* unsigned long */
/* static _unpackul(const byte_t *stream, size_t size) */
/* { */
/*   unsigned int value = 0; */
/*   int i = 0; */

/*   for(i=0; i < size; i++) */
/*     value |= stream[i]<<8*(size - i - 1); */

/*   return value; */
/* } */


/* /\** */
/*  * */
/*  *\/ */
/* float */
/* static _unpackf(const byte_t *stream, size_t size) */
/* { */
/*   float value; */
/*   int i = 0; */

/* #if (BIG_ENDIAN_VALUE) */

/*   for(i=0; i < size; i++) */
/*     *((unsigned char *)&value+i) = stream[i]; */

/* #else */

/*   for(i=0; i < size; i++) */
/*     *((unsigned char*)&value+i) = stream[size - i - 1]; */

/* #endif   */

/*   return value; */
/* } */


/* /\** */
/*  * Extracts the byte-th byte from the data variable. */
/*  *\/ */
/* static byte_t */
/* _extbyte(int byte, unsigned long data) */
/* {  */
/*   return (data>>byte*8) & 0xff; */
/* } */


/**
 * Wraps the stream data section to a stream_t structure.
 * Note that 'data' should start on the data section of the stream, e.g.,
 * data[0] == stream[HEADER_SIZE]
 */
static void
_wrap_privchat_data(stream_t *dest, byte_t *data, tellapic_u32_t datasize)
{
  tellapic_u32_t textsize = datasize - PMSG_TEXT_OFFSET;

  dest->data.chat.idfrom = _read_data_idfrom(data);
  dest->data.chat.type.privmsg.idto = _read_data_idto(data);

  memcpy(dest->data.chat.type.privmsg.text, data + DATA_PMSG_TEXT_INDEX, textsize);

  /* Fill with '\0's and prevent segfault */
  if (textsize < MAX_TEXT_SIZE)
    memset(&dest->data.chat.type.privmsg.text[textsize], '\0', MAX_TEXT_SIZE - textsize);
}


/**
 * Wraps the stream data section to a stream_t structure.
 * Note that 'data' should start on the data section of the stream, e.g.,
 * data[0] == stream[HEADER_SIZE]
 */
static void
_wrap_broadchat_data(stream_t *dest, byte_t *data, tellapic_u32_t datasize)
{
  tellapic_u32_t textsize = datasize - BMSG_TEXT_OFFSET;

  dest->data.chat.idfrom = _read_data_idfrom(data);

  memcpy(dest->data.chat.type.broadmsg, data + DATA_BMSG_TEXT_INDEX, textsize);

  /* Fill with '\0's and prevent segfault */
  if (textsize < MAX_TEXT_SIZE)
    memset(&dest->data.chat.type.broadmsg[textsize], '\0', MAX_TEXT_SIZE - textsize);

}


/**
 * Wraps the stream data section to a stream_t structure.
 * Note that 'data' should start on the data section of the stream, e.g.,
 * data[0] == stream[HEADER_SIZE]
 */
static void
_wrap_ctle_data(stream_t *dest, byte_t *data, tellapic_u32_t datasize)
{

  dest->data.control.idfrom = _read_data_idfrom(data);

  memcpy(dest->data.control.info, data + 1, datasize - 1);

  /* Fill with '\0's and prevent segfault */
  if (datasize - 1 < MAX_INFO_SIZE)
    memset(&dest->data.control.info[datasize - 1], '\0', MAX_TEXT_SIZE - datasize - 1);

}


/**
 * Wraps the stream data section to a stream_t structure.
 * Note that 'data' should start on the data section of the stream, e.g.,
 * data[0] == stream[HEADER_SIZE]
 */
static void
_wrap_ctl_data(stream_t *dest, byte_t *data)
{

  dest->data.control.idfrom = _read_data_idfrom(data);

}


/**
 * Wraps the stream data section to a stream_t structure.
 * Note that 'data' should start on the data section of the stream, e.g.,
 * data[0] == stream[HEADER_SIZE]
 */
static void
_wrap_file_data(stream_t *dest, byte_t *data, long datasize)
{
  dest->data.file = malloc(datasize);

  memcpy(dest->data.file, data, datasize);
}


/**
 * Wraps the stream data section to a stream_t structure.
 * Note that 'data' should start on the data section of the stream, e.g.,
 * data[0] == stream[HEADER_SIZE]
 */
static void
_wrap_figure_data(stream_t *dest, byte_t *data) 
{
  int tool  = 0;
  int etype = 0;
  
  /* Copy the fixed section either for text or a figure. */
  dest->data.drawing.idfrom      = _read_data_idfrom(data);
  dest->data.drawing.dcbyte      = _read_data_dcbyte(data);
  dest->data.drawing.dcbyte_ext  = _read_data_dcbyte_ext(data);
  dest->data.drawing.point1.x    = _read_data_point1_x(data);
  dest->data.drawing.point1.y    = _read_data_point1_y(data);
  dest->data.drawing.number      = _read_data_number(data); 
  dest->data.drawing.width       = _read_data_width(data);  
  dest->data.drawing.opacity     = _read_data_opacity(data);
  dest->data.drawing.color.red   = _read_data_color_red(data);
  dest->data.drawing.color.green = _read_data_color_green(data);
  dest->data.drawing.color.blue  = _read_data_color_blue(data); 


  /* We will use this to define what we need to copy upon the selected tool or event. */
  tool  = dest->data.drawing.dcbyte & TOOL_MASK;
  etype = dest->data.drawing.dcbyte & EVENT_MASK;
  
  if (etype == EVENT_NULL || etype == EVENT_PRESS) 
    {
      /* TOOL_TEXT has different data. */
      if (tool != TOOL_TEXT) 
	{

	  dest->data.drawing.type.figure.point2.x      = _read_data_point2_x(data); 
	  dest->data.drawing.type.figure.point2.y      = _read_data_point2_y(data); 
	  dest->data.drawing.type.figure.miterlimit    = _read_data_miter_limit(data);
	  dest->data.drawing.type.figure.dash_phase    = _read_data_dash_phase(data);
	  dest->data.drawing.type.figure.dash_array[0] = _read_data_dash_array0(data);
	  dest->data.drawing.type.figure.dash_array[1] = _read_data_dash_array1(data);
	  dest->data.drawing.type.figure.linejoin      = _read_data_line_join(data);
	  dest->data.drawing.type.figure.endcaps       = _read_data_end_caps(data);
	} 
      else 
	{
	  dest->data.drawing.type.text.infolen = _read_data_text_len(data);
	  dest->data.drawing.type.text.style   = _read_data_text_style(data);
	  dest->data.drawing.type.text.facelen = _read_data_facelen(data);

	  /* Do some checks about the "truly" value of facelen to avoid buffer overrun. */
	  /* data[DDATA_FONTFACELEN_INDEX] is only 1 byte len, don't worry about endiannes. */
	  if (dest->data.drawing.type.text.facelen < 0)
	    dest->data.drawing.type.text.facelen = 0;
	  
	  /* Do not assign the maximum value if it will overrun the data buffer. */
	  else if ( dest->data.drawing.type.text.facelen > MAX_FONTFACE_LEN)
	    dest->data.drawing.type.text.facelen = MAX_FONTFACE_LEN;

	  else
	    {
	    }

	  if (dest->data.drawing.type.text.infolen < 0)
	    dest->data.drawing.type.text.infolen = 0;

	  /* Do not assign the maximum value if it will overrun the data buffer. */
	  else if ( dest->data.drawing.type.text.infolen > MAX_TEXT_SIZE)
	    dest->data.drawing.type.text.infolen = MAX_TEXT_SIZE;

	  else
	    {
	    }

	  /* textsize = datasize - DDATA_FONTFACE_INDEX - dest->data.drawing.type.text.facelen ; */

	  memcpy(dest->data.drawing.type.text.face, data + DDATA_FONTFACE_INDEX, dest->data.drawing.type.text.facelen);
	  if (dest->data.drawing.type.text.facelen < MAX_FONTFACE_LEN)
	    memset(&dest->data.drawing.type.text.face[dest->data.drawing.type.text.facelen], '\0', MAX_FONTFACE_LEN - dest->data.drawing.type.text.facelen);

	  memcpy(dest->data.drawing.type.text.info, data + DDATA_FONTFACE_INDEX + dest->data.drawing.type.text.facelen, dest->data.drawing.type.text.infolen);
	  if ( dest->data.drawing.type.text.infolen < MAX_TEXT_SIZE)
	    memset(&dest->data.drawing.type.text.info[dest->data.drawing.type.text.infolen], '\0', MAX_TEXT_SIZE - dest->data.drawing.type.text.infolen);
	}
    }
}


/**
 * Wraps bytes chunks in the header structure to be returned.
 * _b means blocking. For now, we just implement blocking mode.
 * If we successfuly read HEADER_SIZE bytes, we check the control
 * byte for a valid value. Also, we ensure that the header.ssize
 * is at least a valid value for the corresponding control byte.
 */
POSH_PUBLIC_API(header_t)
tellapic_read_header_b(tellapic_socket_t socket) 
{

  byte_t        *data  = malloc(HEADER_SIZE);
  int           nbytes = _read_b(socket, HEADER_SIZE, data);
  header_t      header;

  if (nbytes == HEADER_SIZE)
    {
      tellapic_u32_t ssize = _read_stream_size(data);
      if (ssize >= HEADER_SIZE && ssize <= MAX_STREAM_SIZE)
	_wrap_stream_header(&header, data);
    } 

  else if (nbytes > 0)
    /* We didn't complete reading what was expected */
    _set_header_cbyte(&header, CTL_NOSTREAM);

  else if (nbytes == 0) 
    {
      _set_header_cbyte(&header, CTL_NOPIPE);
      _set_header_ssize(&header, HEADER_SIZE);
      printf("nbytes == 0 on read_header\n");
    }

  else
    {
      printf("FAil read header\n");
      _set_header_cbyte(&header, CTL_FAIL);
    }

  /* Free the data stream as we already filled up the stream structure for clients. */
  free(data);

  return header;
}


/**
 * Read first the header, validate it, and then read the data section.
 * Note that tellapic_read_header_b() is a blocking function as well as any other *_b() function.
 *
 * This is the best effort I made for now. I REALLY need to avoid blocking I/O but that will be
 * added to TODO's list.
 *
 * Note that the header is a valid header already checked by the fuction, but another test should be
 * made to avoid reading data that is not coming and block.
 */
POSH_PUBLIC_API(stream_t)
tellapic_read_stream_b(tellapic_socket_t socket) 
{
  stream_t stream;

  stream.header = tellapic_read_header_b(socket);
   
  if ( stream.header.cbyte != CTL_FAIL     && 
       stream.header.cbyte != CTL_NOPIPE   &&
       stream.header.cbyte != CTL_NOSTREAM &&
       stream.header.cbyte != CTL_CL_TIMEOUT )

    stream = tellapic_read_data_b(socket, stream.header);
  
  return stream;
}


/**
 * Reads the data section upon the header passed as argument.
 * _b means blocking. For now, we just implement blocking mode.
 *
 * tellapic_read_header_b() ensures us that we have both, a valid
 * header, and the header size corresponds with HEADER_SIZE.
 */
POSH_PUBLIC_API(stream_t)
tellapic_read_data_b(tellapic_socket_t socket, header_t header) 
{
  tellapic_u32_t datasize = _get_header_ssize(&header) - HEADER_SIZE;
  byte_t         *data    = malloc(datasize);
  tellapic_u32_t nbytes   = _read_b(socket, datasize, data);
  stream_t       stream;
  
  /* This is important. It tells us that what header.ssize says is what we really read and     */
  /* what is indeed in the data buffer. So, is we move between 0 and header.ssize-HEADER_SIZE */
  /* we won't have SIGSEGV. This information is useful to _*cpy() functions.                  */
  if (nbytes == 0) 
    _set_header_cbyte(&stream.header, CTL_NOPIPE);

  else if (nbytes < 0)
    {
      _set_header_cbyte(&stream.header, CTL_FAIL);
      _set_header_ssize(&stream.header, 0);
    }

  else if (nbytes == datasize) 
    {
      /* Copy the header to the stream structure. */
      stream.header = header;

      /* Copy the stream data section upon the control byte. */
      switch(stream.header.cbyte) 
	{
	case CTL_CL_PMSG:
	  _wrap_privchat_data(&stream, data, datasize);
	  break; 

	case CTL_CL_BMSG:
	  _wrap_broadchat_data(&stream, data, datasize);
	  break;

	case CTL_CL_FIG:
	case CTL_CL_DRW:
	  _wrap_figure_data(&stream, data);
	  break;

	case CTL_SV_CLIST:
	  break;

	case CTL_SV_CLADD:
	case CTL_CL_PWD:
	case CTL_CL_NAME:
	  _wrap_ctle_data(&stream, data, datasize);
	  break;

	case CTL_SV_FILE:
	  _wrap_file_data(&stream, data, datasize);
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
	  _wrap_ctl_data(&stream, data);
	  break;
	  
	default:
	  printf("FAIL over HERE!\n");
	  _set_header_cbyte(&stream.header, CTL_FAIL);
	  break;
	}
    } 
  else if (nbytes > 0)
    _set_header_cbyte(&stream.header, CTL_NOSTREAM);

  free(data);
  return stream;
}


/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_read_stream_nb(tellapic_socket_t socket) 
{
  stream_t stream;

  stream.header = tellapic_read_header_nb(socket);
   
  if ( stream.header.cbyte != CTL_FAIL     && 
       stream.header.cbyte != CTL_NOPIPE   &&
       stream.header.cbyte != CTL_NOSTREAM &&
       stream.header.cbyte != CTL_CL_TIMEOUT )

    stream = tellapic_read_data_nb(socket, stream.header);
  
  return stream;
}


/**
 *
 */
POSH_PUBLIC_API(header_t)
tellapic_read_header_nb(tellapic_socket_t socket) 
{

  byte_t        *data  = malloc(HEADER_SIZE);
  tellapic_u32_t nbytes = _read_nb(socket, HEADER_SIZE, data);
  header_t       header;

  if (nbytes == HEADER_SIZE)
    {
      tellapic_u32_t ssize = _read_stream_size(data);
      if (ssize >= HEADER_SIZE && ssize <= MAX_STREAM_SIZE)
        _wrap_stream_header(&header, data);
      
    }
  else if (nbytes > 0)
    {
      /* We didn't complete reading what was expected */
      _set_header_cbyte(&header, CTL_NOSTREAM);
    }
  else if (nbytes == 0) 
    {
      _set_header_cbyte(&header, CTL_NOPIPE);
      _set_header_ssize(&header, HEADER_SIZE);
      printf("nbytes == 0 on read_header\n");
    }

  else
    {
      printf("FAil read header\n");
      _set_header_cbyte(&header, CTL_FAIL);
    }

  /* Free the data stream as we already filled up the stream structure for clients. */
  free(data);

  return header;
}


/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_read_data_nb(tellapic_socket_t socket, header_t header) 
{
  tellapic_u32_t datasize = _get_header_ssize(&header) - HEADER_SIZE;
  byte_t         *data    = malloc(datasize);
  tellapic_u32_t nbytes   = _read_nb(socket, datasize, data);
  stream_t       stream;


  /* This is important. It tells us that what header.ssize says is what we really read and     */
  /* what is indeed in the data buffer. So, is we move between 0 and header.ssize-HEADER_SIZE */
  /* we won't have SIGSEGV. This information is useful to _*cpy() functions.                  */
  if (nbytes == 0) 
    {
      _set_header_cbyte(&stream.header, CTL_NOPIPE);
    }
  else if (nbytes < 0)
    {
      _set_header_cbyte(&stream.header, CTL_FAIL);
      _set_header_ssize(&stream.header, 0);
    }

  else if (nbytes == datasize) 
    {
      /* Copy the header to the stream structure. */
      stream.header = header;

      /* Copy the stream data section upon the control byte. */
      switch(stream.header.cbyte) 
	{
	case CTL_CL_PMSG:
	  _wrap_privchat_data(&stream, data, datasize);
	  break;

	case CTL_CL_BMSG:
	  _wrap_broadchat_data(&stream, data, datasize);
	  break;

	case CTL_CL_FIG:
	case CTL_CL_DRW:
	  _wrap_figure_data(&stream, data);
	  break;

	case CTL_SV_CLIST:
	  break;

	case CTL_SV_CLADD:
	case CTL_CL_PWD:
	case CTL_CL_NAME:
	  _wrap_ctle_data(&stream, data, datasize);
	  break;

	case CTL_SV_FILE:
	  _wrap_file_data(&stream, data, datasize);
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
	  _wrap_ctl_data(&stream, data);
	  break;
	  
	default:
	  printf("FAIL over HERE!\n");
	  _set_header_cbyte(&stream.header, CTL_FAIL);
	  break;
	}
    } 
  else if (nbytes > 0)
    _set_header_cbyte(&stream.header, CTL_NOSTREAM);
  

  free(data);
  return stream;
}


/**
 * Check if header is valid. If so, return the stream size. Else, return 0.
 */
POSH_PUBLIC_API(int)
_stream_header_ok(byte_t *header)
{
  /* get the stream size from the header section */
  tellapic_u32_t ssize = _read_header_ssize(header);

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
POSH_PUBLIC_API(byte_t *)
tellapic_rawread_b(tellapic_socket_t socket)
{
  
  byte_t        *header= malloc(HEADER_SIZE);
  byte_t        *data  = NULL;
  tellapic_u32_t  nbytes = _read_b(socket, HEADER_SIZE, header);

  if (nbytes == HEADER_SIZE)
    {
      tellapic_u32_t ssize =  _stream_header_ok(header);

      if (ssize != 0)
	{
	  tellapic_u32_t rbytes = ssize - HEADER_SIZE;
	  data = malloc(ssize);
	  memcpy(data, header, HEADER_SIZE);

	  nbytes = _read_b(socket, rbytes, data + HEADER_SIZE);

	  /* Check for errors. If someone is found, free the current data structure */
	  /* build a new data structure to inform such error, and return data.      */
	  if (nbytes == 0)
	    {
	      free(data);
	      data = tellapic_build_rawstream(CTL_NOPIPE, socket);
	    }
	  else if (nbytes < 0)
	    {
	      free(data);
	      data = tellapic_build_rawstream(CTL_FAIL, socket);
	    }
	  else if (nbytes > 0 && nbytes != rbytes)
	    {
	      free(data);
	      data = tellapic_build_rawstream(CTL_NOSTREAM, socket);
	    }
	}
    }
  else if (nbytes == 0)
    data = tellapic_build_rawstream(CTL_NOPIPE, socket);

  else if (nbytes > 0)
    data = tellapic_build_rawstream(CTL_NOSTREAM, socket);

  else
    data = tellapic_build_rawstream(CTL_FAIL, socket);
  
  
  free(header);

  return data;
}


/**
 * pwd could and should be NULL.
 */
POSH_PUBLIC_API(char *)
tellapic_read_pwd(tellapic_socket_t socket, char *pwd, int *len)
{
  byte_t     *header  = malloc(HEADER_SIZE);

  tellapic_u32_t nbytes   = _read_b(socket, HEADER_SIZE, header);
  stream_t   stream;  /* This is used only as a placeholder. Avoid it for future release, use instead local variables*/

  *len = 0;
  
  _set_header_cbyte(&stream.header, CTL_FAIL);
  
  if (nbytes == HEADER_SIZE && header[CBYTE_INDEX] == CTL_CL_PWD) 
    {
      _set_header_endian(&stream.header, header[ENDIAN_INDEX]);
      _set_header_cbyte(&stream.header, header[CBYTE_INDEX]);
      _set_header_ssize(&stream.header, _read_header_ssize(header));
 
      if (stream.header.ssize <= MAX_CTLEXT_STREAM_SIZE && stream.header.ssize >= MIN_CTLEXT_STREAM_SIZE + 1)
	{
	  byte_t *data = malloc(stream.header.ssize - HEADER_SIZE);
	  nbytes = _read_b(socket, stream.header.ssize - HEADER_SIZE, data);

	  if (nbytes == stream.header.ssize - HEADER_SIZE)
	    {
	      _wrap_ctle_data(&stream, data, nbytes);
	      *len = nbytes - 1;
	      pwd = malloc(*len + 1);
#if defined (linux) || defined (LINUX)
	      strncpy(pwd, (char *)stream.data.control.info, *len);
#elif defined (WIN32) || (_WIN32)
	      strncpy_s(pwd, *len, (char *)stream.data.control.info, *len);
#endif
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
PUBLIC_API(tellapic_u32_t)
tellapic_rawsend(tellapic_socket_t socket, byte_t *rawstream)
{
  tellapic_u32_t ssize = _read_stream_size(rawstream);

  tellapic_u32_t r = send(socket, rawstream, ssize, 0);
  
  return r;
}


/**
 * TODO: this is not finished nor updated. 
 */
POSH_PUBLIC_API(int)
tellapic_send(tellapic_socket_t socket, stream_t *stream) 
{
  tellapic_u32_t bytesSent = 0;
  byte_t         *rawstream = malloc(stream->header.ssize);
  int            cbyte = stream->header.cbyte; 
  void           *pointer = rawstream;
  
  pointer = _copy_header_data(pointer, stream->header);
	
  switch(cbyte)
    {
    case  CTL_CL_PMSG:
      pointer = _copy_privchat_data(rawstream, stream->data.chat, stream->header.ssize);
      break;

    case CTL_CL_BMSG:
      pointer = _copy_broadchat_data(rawstream, stream->data.chat, stream->header.ssize);
      break;

    case CTL_CL_FIG:
      pointer = _copy_drawing_data(rawstream, *stream);
      break;

    case CTL_CL_DRW:
      if (stream->header.ssize == DRW_INIT_STREAM_SIZE)
	pointer = _copy_drawing_init(rawstream, *stream);
      else
	pointer = _copy_drawing_using(rawstream, *stream);
      break;


    case CTL_SV_CLIST:
      /* Implementing this will complicate things. Instead, send CTL_SV_CLADD for each client the list has when an user ask for a client list. */
      /* The client will add the user if it wasn't already added. */
      break;

    case CTL_SV_FILE:
      pointer = _copy_file_data(rawstream, stream->data.file, stream->header.ssize - HEADER_SIZE);
      break;

    case CTL_CL_PWD:
    case CTL_SV_CLADD:
    case CTL_CL_NAME:
      pointer = _copy_control_extended_data(rawstream, stream->data.control, stream->header.ssize - HEADER_SIZE -1);
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
      pointer = _copy_control_data(rawstream, stream->data.control);
      break;

    default:
      /* wrong header */
      return 0;
    }

  bytesSent = send(socket, rawstream, stream->header.ssize, 0);

  free(rawstream);
  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_file(tellapic_socket_t socket, FILE *file, tellapic_u32_t filesize)
{
  tellapic_u32_t bytesSent = 0;
  byte_t         *rawstream = malloc(filesize + HEADER_SIZE);
  void           *pointer = rawstream;

  pointer = WriteByte(pointer, 1);
  pointer = WriteByte(pointer, CTL_SV_FILE);

#ifdef POSH_LITTLE_ENDIAN
  pointer = POSH_WriteU32ToLittle(pointer, filesize + HEADER_SIZE);
#else
  pointer = POSH_WriteU32ToBit(pointer, filesize + HEADER_SIZE);
#endif

  fread(pointer, sizeof(byte_t), filesize, file);

  rewind(file);

  bytesSent = send(socket, rawstream, filesize + HEADER_SIZE, 0);

  free(rawstream);

  return bytesSent;  
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_struct(tellapic_socket_t socket, stream_t *stream)
{
  switch(stream->header.cbyte) 
    {

    case CTL_CL_FIG:
      if (stream->data.drawing.dcbyte == (TOOL_TEXT | EVENT_NULL) )
	{

	}
    }
    return 0;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_text(tellapic_socket_t socket, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int style, int facelen, char *face, int textlen, char *text)
{
  tellapic_u32_t bytesSent = 0;
  tellapic_u32_t ssize = MIN_FIGTXT_STREAM_SIZE + facelen + textlen;
  byte_t         *rawstream = malloc(ssize);
  void           *pointer = rawstream;
  
#ifdef LITTLE_ENDIAN_VALUE                                              /*  +-------------------+                    */
  pointer = WriteByte(pointer, 1);                                       /*  |    endianness     |  1 byte            */
  pointer = WriteByte(pointer, CTL_CL_FIG);                              /*  |      cbyte        |  1 byte            */
  pointer = POSH_WriteU32ToLittle(pointer, ssize);                       /*  |    stream size    |  4 bytes           */
  pointer = WriteByte(pointer, (byte_t)idfrom);                          /*  |      idfrom       |  1 byte            */
  pointer = WriteByte(pointer, TOOL_TEXT | EVENT_NULL);                  /*  |      dcbyte       |  1 byte            */
  pointer = WriteByte(pointer, 0);                                       /*  |    dcbyte_ext     |  1 byte            */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleU32(dnum));        /*  |   drawing number  |  4 bytes           */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(w));     /*  |       width       |  4 bytes           */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(op));    /*  |      opacity      |  4 bytes           */
  pointer = WriteByte(pointer, (byte_t)red);                             /*  |     color red     |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)green);                           /*  |    color green    |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)blue);                            /*  |     color blue    |  1 byte            */
  pointer = POSH_WriteU16ToLittle(pointer, POSH_LittleU16(x1));          /*  |     x point 1     |  2 bytes           */
  pointer = POSH_WriteU16ToLittle(pointer, POSH_LittleU16(y1));          /*  |     y point 1     |  2 bytes           */
  pointer = WriteByte(pointer, (byte_t)style);                           /*  |    font style     |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)facelen);                         /*  |   face name len   |  1 byte            */
  pointer = POSH_WriteU16ToLittle(pointer, POSH_LittleU16(textlen));     /*  |      infolen      |  2 bytes           */
  pointer = WriteBytes(pointer, face, facelen);                          /*  |   font face name  |  facenamelen bytes */
  pointer = WriteBytes(pointer, text, textlen);                          /*  |       text        |  infolen bytes     */
                                                                         /*  +-------------------+                    */
                                                                         /*  total bytes = 32 + facenamelen + infolen */
                                                                         
                                                                         
#else                                                                  /*  +-------------------+                    */
  pointer = WriteByte(pointer, 1);                                       /*  |    endianness     |  1 byte            */
  pointer = WriteByte(pointer, CTL_CL_FIG);                              /*  |      cbyte        |  1 byte            */
  pointer = POSH_WriteU32ToBig(pointer, ssize);                          /*  |    stream size    |  4 bytes           */
  pointer = WriteByte(pointer, (byte_t)idfrom);                          /*  |      idfrom       |  1 byte            */
  pointer = WriteByte(pointer, TOOL_TEXT | EVENT_NULL);                  /*  |      dcbyte       |  1 byte            */
  pointer = WriteByte(pointer, 0);                                       /*  |    dcbyte_ext     |  1 byte            */
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigU32(dnum));              /*  |   drawing number  |  4 bytes           */
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(w));           /*  |       width       |  4 bytes           */
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(op));          /*  |      opacity      |  4 bytes           */
  pointer = WriteByte(pointer, (byte_t)red);                             /*  |     color red     |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)green);                           /*  |    color green    |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)blue);                            /*  |     color blue    |  1 byte            */
  pointer = POSH_WriteU16ToBig(pointer, POSH_BigU16(x1));                /*  |     x point 1     |  2 bytes           */
  pointer = POSH_WriteU16ToBig(pointer, POSH_BigU16(y1));                /*  |     y point 1     |  2 bytes           */
  pointer = WriteByte(pointer, (byte_t)style);                           /*  |    font style     |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)facelen);                         /*  |   face name len   |  1 byte            */
  pointer = POSH_WriteU16ToBig(pointer, POSH_BigU16(textlen));           /*  |      infolen      |  2 bytes           */
  pointer = WriteBytes(pointer, face, facelen);                          /*  |   font face name  |  facenamelen bytes */
  pointer = WriteBytes(pointer, text, textlen);                          /*  |       text        |  infolen bytes     */
                                                                         /*  +-------------------+                    */
                                                                         /*  total bytes = 32 + facenamelen + infolen */
#endif

  bytesSent = send(socket, rawstream, ssize, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_drw_using(tellapic_socket_t socket, int tool, int dcbyte_ext, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1)
{
  tellapic_u32_t bytesSent = 0;
  byte_t         *rawstream = malloc(DRW_USING_STREAM_SIZE);
  void           *pointer   = rawstream;

#ifdef LITTLE_ENDIAN_VALUE                                             /* +---------------+         */
  pointer = WriteByte(pointer, 1);                                      /* |  endianness   | 1 byte  */
  pointer = WriteByte(pointer, CTL_CL_DRW);                             /* |     cbyte     | 1 byte  */
  pointer = POSH_WriteU32ToLittle(pointer, DRW_USING_STREAM_SIZE);      /* | stream size   | 4 bytes */
  pointer = WriteByte(pointer, (byte_t)idfrom);                         /* |    id from    | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)tool);                           /* |    dcbyte     | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)dcbyte_ext);                     /* |  dcbyte ext   | 1 byte  */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleU32(dnum));       /* | drawingnumber | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(w));    /* |     width     | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(op));   /* |    opacity    | 4 bytes */
  pointer = WriteByte(pointer, (byte_t)red);                            /* |   color red   | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)green);                          /* |   color green | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)blue);                           /* |   color blue  | 1 byte  */
  pointer = POSH_WriteU16ToLittle(pointer, x1);                         /* |    x point 1  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, y1);                         /* |    y point 1  | 2 bytes */
#else                                                                   /* +---------------+         */
  /*  total bytes = 28 bytes   */
  pointer = WriteByte(pointer, 1);
  pointer = WriteByte(pointer, CTL_CL_DRW);
  pointer = POSH_WriteU32ToBig(pointer, DRW_USING_STREAM_SIZE);
  pointer = WriteByte(pointer, (byte_t)idfrom);
  pointer = WriteByte(pointer, (byte_t)tool);
  pointer = WriteByte(pointer, (byte_t)dcbyte_ext);
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigU32(dnum));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(w));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(op));
  pointer = WriteByte(pointer, (byte_t)red);
  pointer = WriteByte(pointer, (byte_t)green);
  pointer = WriteByte(pointer, (byte_t)blue);
  pointer = POSH_WriteU16ToBig(pointer, x1);
  pointer = POSH_WriteU16ToBig(pointer, y1);
#endif  

  bytesSent = send(socket, rawstream, DRW_USING_STREAM_SIZE, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_drw_init(tellapic_socket_t socket, int tool, int dcbyte_ext, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int x2, int y2, int lj, int ec, float ml, float dp, float da[])
{
  tellapic_u32_t bytesSent = 0;
  byte_t         *rawstream = malloc(DRW_INIT_STREAM_SIZE);
  void           *pointer   = rawstream;
  
#ifdef LITTLE_ENDIAN_VALUE                                             /* +---------------+         */
  pointer = WriteByte(pointer, 1);                                      /* |  endianness   | 1 byte  */
  pointer = WriteByte(pointer, CTL_CL_DRW);                             /* |     cbyte     | 1 byte  */
  pointer = POSH_WriteU32ToLittle(pointer, DRW_INIT_STREAM_SIZE);       /* | stream size   | 4 bytes */
  pointer = WriteByte(pointer, (byte_t)idfrom);                         /* |    id from    | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)tool);                           /* |    dcbyte     | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)dcbyte_ext);                     /* |  dcbyte ext   | 1 byte  */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleU32(dnum));       /* | drawingnumber | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(w));    /* |     width     | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(op));   /* |    opacity    | 4 bytes */
  pointer = WriteByte(pointer, (byte_t)red);                            /* |   color red   | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)green);                          /* |   color green | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)blue);                           /* |   color blue  | 1 byte  */
  pointer = POSH_WriteU16ToLittle(pointer, x1);                         /* |    x point 1  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, y1);                         /* |    y point 1  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, x2);                         /* |    x point 2  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, y2);                         /* |    y point 2  | 2 bytes */  
  pointer = WriteByte(pointer, (byte_t)lj);                             /* |   line joins  | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)ec);                             /* |   end caps    | 1 byte  */  
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(ml));   /* |   miter limit | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(dp));   /* |   dash phase  | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[0]));/* |   dash array0 | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[1]));/* |   dash array1 | 4 bytes */
#else                                                                 /* +---------------+         */
  /* total bytes = 50 bytes    */
                                                                        
  pointer = WriteByte(pointer, 1);
  pointer = WriteByte(pointer, CTL_CL_DRW);
  pointer = POSH_WriteU32ToBig(pointer, DRW_INIT_STREAM_SIZE);
  pointer = WriteByte(pointer, (byte_t)idfrom);
  pointer = WriteByte(pointer, (byte_t)tool);
  pointer = WriteByte(pointer, (byte_t)dcbyte_ext);
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigU32(dnum));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(w));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(op));
  pointer = WriteByte(pointer, (byte_t)red);
  pointer = WriteByte(pointer, (byte_t)green);
  pointer = WriteByte(pointer, (byte_t)blue);
  pointer = POSH_WriteU16ToBig(pointer, x1);
  pointer = POSH_WriteU16ToBig(pointer, y1);
  pointer = POSH_WriteU16ToBig(pointer, x2);
  pointer = POSH_WriteU16ToBig(pointer, y2);
  pointer = WriteByte(pointer, (byte_t)lj);
  pointer = WriteByte(pointer, (byte_t)ec);
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(ml));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(dp));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[0]));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[1]));
#endif
  
  bytesSent = send(socket, rawstream, DRW_INIT_STREAM_SIZE, 0);

  free(rawstream);

  return bytesSent;
}

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_fig(tellapic_socket_t socket, int tool, int dcbyte_ext, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int x2, int y2, int lj, int ec, float ml, float dp, float da[])
{
  tellapic_u32_t bytesSent = 0;
  byte_t         *rawstream = malloc(FIG_STREAM_SIZE);
  void           *pointer   = rawstream;
  
#ifdef LITTLE_ENDIAN_VALUE                                             /* +---------------+         */
  pointer = WriteByte(pointer, 1);                                      /* |  endianness   | 1 byte  */
  pointer = WriteByte(pointer, CTL_CL_FIG);                             /* |     cbyte     | 1 byte  */
  pointer = POSH_WriteU32ToLittle(pointer, FIG_STREAM_SIZE);            /* | stream size   | 4 bytes */
  pointer = WriteByte(pointer, (byte_t)idfrom);                         /* |    id from    | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)tool);                           /* |    dcbyte     | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)dcbyte_ext);                     /* |  dcbyte ext   | 1 byte  */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleU32(dnum));       /* | drawingnumber | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(w));    /* |     width     | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(op));   /* |    opacity    | 4 bytes */
  pointer = WriteByte(pointer, (byte_t)red);                            /* |   color red   | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)green);                          /* |   color green | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)blue);                           /* |   color blue  | 1 byte  */
  pointer = POSH_WriteU16ToLittle(pointer, x1);                         /* |    x point 1  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, y1);                         /* |    y point 1  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, x2);                         /* |    x point 2  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, y2);                         /* |    y point 2  | 2 bytes */  
  pointer = WriteByte(pointer, (byte_t)lj);                             /* |   line joins  | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)ec);                             /* |   end caps    | 1 byte  */  
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(ml));   /* |   miter limit | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(dp));   /* |   dash phase  | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[0]));/* |   dash array0 | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[1]));/* |   dash array1 | 4 bytes */
#else                                                                   /* +---------------+         */
  /* total bytes = 50 bytes    */
                                                                        
  pointer = WriteByte(pointer, 1);
  pointer = WriteByte(pointer, CTL_CL_FIG);
  pointer = POSH_WriteU32ToBig(pointer, FIG_STREAM_SIZE);
  pointer = WriteByte(pointer, (byte_t)idfrom);
  pointer = WriteByte(pointer, (byte_t)tool);
  pointer = WriteByte(pointer, (byte_t)dcbyte_ext);
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigU32(dnum));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(w));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(op));
  pointer = WriteByte(pointer, (byte_t)red);
  pointer = WriteByte(pointer, (byte_t)green);
  pointer = WriteByte(pointer, (byte_t)blue);
  pointer = POSH_WriteU16ToBig(pointer, x1);
  pointer = POSH_WriteU16ToBig(pointer, y1);
  pointer = POSH_WriteU16ToBig(pointer, x2);
  pointer = POSH_WriteU16ToBig(pointer, y2);
  pointer = WriteByte(pointer, (byte_t)lj);
  pointer = WriteByte(pointer, (byte_t)ec);
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(ml));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(dp));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[0]));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[1]));
#endif

  bytesSent = send(socket, rawstream, FIG_STREAM_SIZE, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_chatp(tellapic_socket_t socket, int idfrom, int idto, int textlen, char* text)
{
  tellapic_u32_t bytesSent = 0;
  tellapic_u32_t ssize = HEADER_SIZE + textlen + 2;
  byte_t         *rawstream = malloc(ssize);
  void           *pointer   = rawstream;

  if (textlen <= 0)
    return -1;

#ifdef LITTLE_ENDIAN_VALUE                                             /* +------------------+ */
  pointer = WriteByte(pointer, 1);                                      /* |   endianness     | */
  pointer = WriteByte(pointer, CTL_CL_PMSG);                            /* |     cbyte        | */
  pointer = POSH_WriteU32ToLittle(pointer, ssize);                      /* |   stream size    | */
  pointer = WriteByte(pointer, (byte_t)idfrom);                         /* |     id from      | */
  pointer = WriteByte(pointer, (byte_t)idto);                           /* |     id to        | */
  pointer = WriteBytes(pointer, text, textlen);                         /* |       text       | */
                                                                        /* +------------------+ */
#else
  pointer = WriteByte(pointer, 1);
  pointer = WriteByte(pointer, CTL_CL_PMSG);
  pointer = POSH_WriteU32ToBig(pointer, ssize);
  pointer = WriteByte(pointer, (byte_t)idfrom);
  pointer = WriteByte(pointer, (byte_t)idto);
  pointer = WriteBytes(pointer, text, textlen);
#endif

  bytesSent = send(socket, rawstream, ssize, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_chatb(tellapic_socket_t socket, int idfrom, int textlen, char* text)
{
  tellapic_u32_t bytesSent = 0;
  tellapic_u32_t ssize = HEADER_SIZE + textlen + 1;
  byte_t         *rawstream = malloc(ssize);
  void           *pointer   = rawstream;

  if (textlen <= 0)
    return -1;

#ifdef LITTLE_ENDIAN_VALUE                                             /* +------------------+ */
  pointer = WriteByte(pointer, 1);                                      /* |   endianness     | */
  pointer = WriteByte(pointer, CTL_CL_BMSG);                            /* |     cbyte        | */
  pointer = POSH_WriteU32ToLittle(pointer, ssize);                      /* |   stream size    | */
  pointer = WriteByte(pointer, (byte_t)idfrom);                         /* |     id from      | */
  pointer = WriteBytes(pointer, text, textlen);                         /* |       text       | */
                                                                        /* +------------------+ */
#else
  pointer = WriteByte(pointer, 1);
  pointer = WriteByte(pointer, CTL_CL_BMSG);
  pointer = POSH_WriteU32ToBig(pointer, ssize);
  pointer = WriteByte(pointer, (byte_t)idfrom);
  pointer = WriteBytes(pointer, text, textlen);
#endif

  bytesSent = send(socket, rawstream, ssize, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_ctle(tellapic_socket_t socket, int idfrom, int ctle, int infolen,  char *info)
{
  tellapic_u32_t bytesSent = 0;
  tellapic_u32_t ssize = HEADER_SIZE + infolen + 1;
  byte_t         *rawstream = malloc(ssize);
  void           *pointer = rawstream;

  if (infolen <= 0)
    return -1;

#ifdef LITTLE_ENDIAN_VALUE                                              /* +------------------+ */
  pointer = WriteByte(pointer, 1);                                      /* |   endianness     | */
  pointer = WriteByte(pointer, (byte_t)ctle);                           /* |     cbyte        | */
  pointer = POSH_WriteU32ToLittle(pointer, ssize);                      /* |   stream size    | */
  pointer = WriteByte(pointer, (byte_t)idfrom);                         /* |     id from      | */
  pointer = WriteBytes(pointer, info, infolen);                         /* |       text       | */
#else
  pointer = WriteByte(pointer, 1);
  pointer = WriteByte(pointer, (byte_t)ctle);
  pointer = POSH_WriteU32ToBig(pointer, ssize);
  pointer = WriteByte(pointer, (byte_t)idfrom);
  pointer = WriteBytes(pointer, info, infolen);
#endif

  bytesSent = send(socket, rawstream, ssize, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_ctl(tellapic_socket_t socket, int idfrom, int ctl)
{
  tellapic_u32_t bytesSent = 0;
  byte_t         *rawstream = malloc(CTL_STREAM_SIZE);
  void           *pointer = rawstream;

#ifdef LITTLE_ENDIAN_VALUE                                             /* +------------------+ */
  pointer = WriteByte(pointer, 1);                                     /* |   endianness     | */
  pointer = WriteByte(pointer, (byte_t)ctl);                           /* |     cbyte        | */
  pointer = POSH_WriteU32ToLittle(pointer, CTL_STREAM_SIZE);           /* |   stream size    | */
  pointer = WriteByte(pointer, (byte_t)idfrom);                        /* |     id from      | */
                                                                       /* +------------------+ */
#else
  pointer = WriteByte(pointer, 1);
  pointer = WriteByte(pointer, (byte_t)ctl);
  pointer = POSH_WriteU32ToBig(pointer, CTL_STREAM_SIZE);
  pointer = WriteByte(pointer, (byte_t)idfrom);
#endif
  

  bytesSent = send(socket, rawstream, CTL_STREAM_SIZE, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(byte_t *)
tellapic_read_bytes_b(tellapic_socket_t socket, size_t chunk)
{
  byte_t *data = NULL;

  if (chunk <= 0)
    return NULL;

  data = malloc(chunk);


  _read_b(socket, chunk, data);

  return data;
}


/**
 * This should be deprecated
 */
POSH_PUBLIC_API(byte_t *)
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
	int  id       = va_arg(argp, int);
	void *pointer = NULL;
	outstream = malloc(CTL_STREAM_SIZE);
	pointer = outstream;
	pointer = WriteByte(pointer, 1);
	pointer = WriteByte(pointer, ctlbyte);
#       ifdef POSH_LITTLE_ENDIAN
	pointer = POSH_WriteU32ToLittle(pointer, CTL_STREAM_SIZE);
#       else
	pointer = POSH_WriteU32ToBig(pointer, CTL_STREAM_SIZE);
#       endif
	pointer = WriteByte(pointer, (byte_t)id);
      }
      break;


      /* A file headed stream */
    case CTL_SV_FILE:
      {
	FILE    *file  = va_arg(argp, FILE *);
	int     nbytes = 0;

	fseek(file, 0L, SEEK_END);
	nbytes = ftell(file) + HEADER_SIZE;
	if (nbytes > HEADER_SIZE && nbytes <= MAX_FILE_SIZE)
	  {
	    void *pointer = NULL;
	    outstream = malloc(nbytes);
	    pointer = outstream;
	    pointer = WriteByte(pointer, 1);
	    pointer = WriteByte(pointer, ctlbyte);
#           ifdef POSH_LITTLE_ENDIAN
	    pointer = POSH_WriteU32ToLittle(pointer, nbytes);
#           else
	    pointer = POSH_WriteU32ToBig(pointer, nbytes);
#           endif
	    rewind(file);
	    fread(pointer, sizeof(byte_t), nbytes - HEADER_SIZE, file);
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
	    void *pointer = NULL;
	    outstream = malloc(infolen + HEADER_SIZE + 1);
	    pointer = outstream;
	    pointer = WriteByte(pointer, 1);
	    pointer = WriteByte(pointer, ctlbyte);
#           ifdef POSH_LITTLE_ENDIAN
	    pointer = POSH_WriteU32ToLittle(pointer, infolen + HEADER_SIZE + 1);
#           else
	    pointer = POSH_WriteU32ToBig(pointer, infolen + HEADER_SIZE + 1);
#           endif
	    pointer = WriteByte(pointer, (byte_t)id);
	    memcpy(pointer, info, infolen);
	  }

      }
      break;

    default:
      /* ERROR */
      break;
    }
  va_end(argp);

  return outstream;
}


/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_build_ctle(int ctl, int idfrom, int infosize, char *info) 
{
  stream_t stream;

  if (infosize <= 0) /* TODO: add ctl checks */
    {
      stream.header.cbyte = CTL_FAIL;
      stream.header.ssize = 0;
      return stream;
    }

  stream.header.endian = 1;
  stream.header.cbyte  = (byte_t)ctl;
  stream.data.control.idfrom = (byte_t)idfrom;

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
POSH_PUBLIC_API(stream_t)
tellapic_build_ctl(int ctl, int idfrom) 
{
  stream_t stream;

  /* TODO: add ctl checks */

  stream.header.endian = 0;
  stream.header.cbyte  = (byte_t)ctl;
  stream.data.control.idfrom = (byte_t)idfrom;
  stream.header.ssize = HEADER_SIZE + 1;

  return stream;
}


/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_build_chat(int cbyte, int idfrom, int idto, int textsize,  char *text) /* TODO: something is wrong HERE! */
{
  stream_t stream;

  if (textsize <= 0 || cbyte != CTL_CL_PMSG || cbyte != CTL_CL_BMSG)
    {
      stream.header.cbyte = CTL_FAIL;
      return stream;
    }
    
  stream.header.endian = 0; /* TODO: implement the endian function */
  stream.header.cbyte  = (byte_t)cbyte; 
  stream.data.chat.idfrom = (byte_t)idfrom;

  if (cbyte == CTL_CL_PMSG) 
    {
      stream.data.chat.type.privmsg.idto = (byte_t)idto;
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
POSH_PUBLIC_API(void)
tellapic_free(stream_t *stream)
{
  /* The only allocated memmory from a stream_t structure is this: */
  free(stream->data.file);
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_istimeout(header_t *header)
{
  return header->cbyte == CTL_CL_TIMEOUT;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_isping(header_t *header)
{
  return header->cbyte == CTL_CL_PING;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_ispong(header_t *header)
{
  return header->cbyte == CTL_SV_PONG;
}


/**
 * Returns a non-zero value if 'stream' is a broadcast message.
 */
POSH_PUBLIC_API(int)
tellapic_ischatb(header_t *header) 
{
  return (header->cbyte == CTL_CL_BMSG && header->ssize <= MAX_BMSG_STREAM_SIZE   && header->ssize >= MIN_BMSG_STREAM_SIZE);
}


/**
 * Returns a non-zero value if 'stream' is a private message.
 */
POSH_PUBLIC_API(int)
tellapic_ischatp(header_t *header) 
{
  return (header->cbyte == CTL_CL_PMSG && header->ssize <= MAX_PMSG_STREAM_SIZE   && header->ssize >= MIN_PMSG_STREAM_SIZE);
}


/**
 * Returns a non-zero value if 'stream' is a control stream.
 */
POSH_PUBLIC_API(int)
tellapic_isctl(header_t *header)
{
  return ((header->cbyte == CTL_CL_FILEASK ||
	   header->cbyte == CTL_CL_FILEOK  ||
	   header->cbyte == CTL_SV_PWDFAIL ||
	   header->cbyte == CTL_SV_PWDOK   ||
	   header->cbyte == CTL_CL_CLIST   ||
	   header->cbyte == CTL_CL_DISC    ||
	   header->cbyte == CTL_SV_CLRM    ||
	   header->cbyte == CTL_SV_ID      ||
	   header->cbyte == CTL_SV_AUTHOK  ||
	   header->cbyte == CTL_SV_NAMEINUSE) && header->ssize == CTL_STREAM_SIZE);
}


/**
 * Returns a non-zero value if 'stream' is an extended control stream.
 */
POSH_PUBLIC_API(int)
tellapic_isctle(header_t *header) 
{
  return ((header->cbyte == CTL_CL_PWD   ||
	   header->cbyte == CTL_CL_NAME  ||
	   header->cbyte == CTL_SV_CLADD) && header->ssize <= MAX_CTLEXT_STREAM_SIZE && header->ssize >= MIN_CTLEXT_STREAM_SIZE) ;
}


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_isfile(header_t *header)
{
  return (header->cbyte == CTL_SV_FILE && header->ssize <= MAX_STREAM_SIZE && header->ssize >= MIN_CTLEXT_STREAM_SIZE);
}


/**
 * Returns a non-zero value if 'stream' is a drawing packet.
 */
POSH_PUBLIC_API(int)
tellapic_isdrw(header_t *header) 
{
  return (header->cbyte == CTL_CL_DRW  && (header->ssize == DRW_INIT_STREAM_SIZE || header->ssize == DRW_USING_STREAM_SIZE));
}


/**
 * Returns a non-zero value if 'stream' is a text drawn figure.
 */
POSH_PUBLIC_API(int)
tellapic_isfigtxt(stream_t *stream) 
{
  return (stream->header.cbyte == CTL_CL_FIG && 
	  (stream->data.drawing.dcbyte & TOOL_MASK) == TOOL_TEXT &&
	  stream->header.ssize <= MAX_FIGTXT_STREAM_SIZE &&
	  stream->header.ssize >= MIN_FIGTXT_STREAM_SIZE);
}


/**
 * Returns a non-zero value if 'stream' is a drawn figure.
 */
POSH_PUBLIC_API(int)
tellapic_isfig(header_t *header) 
{
  return (header->cbyte == CTL_CL_FIG && header->ssize <= MAX_FIGTXT_STREAM_SIZE && header->ssize >= MIN_FIGTXT_STREAM_SIZE);
}
