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
  pointer = WriteByte(pointer, color.alpha);
  
  return pointer;
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
  
  pointer = _copy_drawing_color(pointer, text.color);
  pointer = WriteByte(pointer, text.style);
  pointer = WriteByte(pointer, text.facelen);
 #ifdef LITTLE_ENDIAN_VALUE
  pointer = POSH_WriteU16ToLittle(pointer, text.infolen);
#else
  pointer = POSH_WriteU16ToBig(pointer, text.infolen);
#endif
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

  pointer = _copy_drawing_color(pointer, figure.color);
  //pointer = _copy_drawing_point(pointer, figure.point2);
  pointer = WriteByte(pointer, figure.linejoin);
  pointer = WriteByte(pointer, figure.endcaps);
#ifdef LITTLE_ENDIAN_VALUE
  pointer = POSH_WriteU32ToLittle(pointer, figure.miterlimit);
  pointer = POSH_WriteU32ToLittle(pointer, figure.dash_phase);
  pointer = POSH_WriteU32ToLittle(pointer, figure.dash_array[0]);
  pointer = POSH_WriteU32ToLittle(pointer, figure.dash_array[1]);  
  pointer = POSH_WriteU32ToLittle(pointer, figure.dash_array[2]);
  pointer = POSH_WriteU32ToLittle(pointer, figure.dash_array[3]);
#else
  pointer = POSH_WriteU32ToBig(pointer, figure.miterlimit);
  pointer = POSH_WriteU32ToBig(pointer, figure.dash_phase);
  pointer = POSH_WriteU32ToBig(pointer, figure.dash_array[0]);
  pointer = POSH_WriteU32ToBig(pointer, figure.dash_array[1]);
  pointer = POSH_WriteU32ToBig(pointer, figure.dash_array[2]);
  pointer = POSH_WriteU32ToBig(pointer, figure.dash_array[3]);
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
  pointer = _copy_drawing_color(pointer, drawing.fillcolor);
  pointer = _copy_drawing_point(pointer, drawing.point2);
  
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
  int   etype   = stream.data.drawing.dcbyte & EVENT_MASK;
  int   tool    = stream.data.drawing.dcbyte & TOOL_MASK;
  void *pointer = rawstream;

  /* Copy the data section fixed part until point2 */
  pointer = _copy_drawing(pointer, stream.data.drawing);

  /* We need to copy the last fixed part that is not shared within ddata */
  pointer = _copy_drawing_point(pointer, stream.data.drawing.point1);

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

  /* We need to copy the last fixed part that is not shared within ddata */
  pointer = _copy_drawing_point(pointer, stream.data.drawing.point1);

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
_read_data_point(byte_t *data, int index)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU16FromLittle(data + index);
#else
  return POSH_ReadU16FromBig(data + index);
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
static tellapic_float
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
static tellapic_float
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
static color_t
_read_data_color_t(byte_t *data, int offset)
{
  color_t color;

  color.red   = data[offset];
  color.green = data[offset + 1];
  color.blue  = data[offset + 2];
  color.alpha = data[offset + 3];

  return color;
}


/**
 *
 */
static tellapic_float
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
static tellapic_float
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
static tellapic_float
_read_data_dash_array(byte_t * data, int index)
{
#ifdef LITTLE_ENDIAN_VALUE
  return POSH_ReadU32FromLittle(data + DDATA_DASHARRAY_INDEX + 4*index);
#else
  return POSH_ReadU32FromBig(data + DDATA_DASHARRAY_INDEX + 4*index);
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
  dest->data.drawing.idfrom          = _read_data_idfrom(data);
  dest->data.drawing.dcbyte          = _read_data_dcbyte(data);
  dest->data.drawing.dcbyte_ext      = _read_data_dcbyte_ext(data);
  dest->data.drawing.number          = _read_data_number(data); 
  dest->data.drawing.width           = _read_data_width(data);  
  dest->data.drawing.opacity         = _read_data_opacity(data);
  dest->data.drawing.fillcolor       = _read_data_color_t(data, DDATA_FILL_COLOR_INDEX);
  dest->data.drawing.point2.x        = _read_data_point(data, DDATA_COORDX2_INDEX); 
  dest->data.drawing.point2.y        = _read_data_point(data, DDATA_COORDY2_INDEX); 

  /* We will use this to define what we need to copy upon the selected tool or event. */
  tool  = dest->data.drawing.dcbyte & TOOL_MASK;
  etype = dest->data.drawing.dcbyte & EVENT_MASK;
  
  if (etype == EVENT_NULL || etype == EVENT_PRESS) 
    {
      dest->data.drawing.point1.x    = _read_data_point(data, DDATA_COORDX1_INDEX);
      dest->data.drawing.point1.y    = _read_data_point(data, DDATA_COORDY1_INDEX);

      /* TOOL_TEXT has different data. */
      if (tool != TOOL_TEXT)
	{
	  dest->data.drawing.type.figure.color         = _read_data_color_t(data, DDATA_STROKE_COLOR_INDEX);

	  dest->data.drawing.type.figure.miterlimit    = _read_data_miter_limit(data);
	  dest->data.drawing.type.figure.dash_phase    = _read_data_dash_phase(data);
	  dest->data.drawing.type.figure.dash_array[0] = _read_data_dash_array(data, 0);
	  dest->data.drawing.type.figure.dash_array[1] = _read_data_dash_array(data, 1);
	  dest->data.drawing.type.figure.dash_array[2] = _read_data_dash_array(data, 2);
	  dest->data.drawing.type.figure.dash_array[3] = _read_data_dash_array(data, 3);
	  dest->data.drawing.type.figure.linejoin      = _read_data_line_join(data);
	  dest->data.drawing.type.figure.endcaps       = _read_data_end_caps(data);
	} 
      else 
	{
	  dest->data.drawing.type.text.color   = _read_data_color_t(data, DDATA_TEXT_COLOR_INDEX);
	  dest->data.drawing.type.text.infolen = _read_data_text_len(data);
	  dest->data.drawing.type.text.style   = _read_data_text_style(data);
	  dest->data.drawing.type.text.facelen = _read_data_facelen(data);

	  /* Do some checks about the "truly" value of facelen to avoid buffer overrun. */
	  /* data[DDATA_FONTFACELEN_INDEX] is only 1 byte len, don't worry about endiannes. */
	  if (dest->data.drawing.type.text.facelen < 0)
	    dest->data.drawing.type.text.facelen = 0;
	  
	  /* assign the maximum value if it will overrun the data buffer. */
	  else if ( dest->data.drawing.type.text.facelen > MAX_FONTFACE_LEN)
	    dest->data.drawing.type.text.facelen = MAX_FONTFACE_LEN;

	  else
	    {
	    }

	  if (dest->data.drawing.type.text.infolen < 0)
	    dest->data.drawing.type.text.infolen = 0;

	  /* assign the maximum value if it will overrun the data buffer. */
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
 *
 */
static int
_do_wrapping(stream_t *stream, byte_t *data, tellapic_u32_t nbytes)
{
  tellapic_u32_t datasize = _get_header_ssize(&stream->header) - HEADER_SIZE;
  if (nbytes == 0) 
    {
    _set_header_cbyte(&stream->header, CTL_NOPIPE);
    _set_header_ssize(&stream->header, 0);
    }
  else if (nbytes < 0)
    {
      _set_header_cbyte(&stream->header, CTL_FAIL);
      _set_header_ssize(&stream->header, 0);
    }

  else if (nbytes == datasize) 
    {

      /* Copy the stream data section upon the control byte. */
      if (tellapic_ischatp(&stream->header))
	{
	  _wrap_privchat_data(stream, data, datasize);
	}
      else if (tellapic_ischatb(&stream->header))
	{
	  _wrap_broadchat_data(stream, data, datasize);
	}
      else if (tellapic_isfig(&stream->header) || tellapic_isdrw(&stream->header))
	{
	  _wrap_figure_data(stream, data);
	}
      else if (tellapic_isctle(&stream->header))
	{
	  _wrap_ctle_data(stream, data, datasize);
	}
      else if (tellapic_isfile(&stream->header))
	{
	  _wrap_file_data(stream, data, datasize);
	}
      else if (tellapic_isctl(&stream->header))
	{
	  _wrap_ctl_data(stream, data);
	}
      else if (tellapic_isping(&stream->header) || tellapic_ispong(&stream->header))
	{
	  /* nothing */
	}
      else
	{
	  printf("FAIL over HERE!\n");
	  _set_header_cbyte(&stream->header, CTL_FAIL);
	  _set_header_ssize(&stream->header, 0);
	}
    } 
  else if (nbytes > 0)
    {
    _set_header_cbyte(&stream->header, CTL_NOSTREAM);
    _set_header_ssize(&stream->header, 0);
    }
  return 0;
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

  stream.header = header;
  _do_wrapping(&stream, data, nbytes);

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

  stream.header = header;
  _do_wrapping(&stream, data, nbytes);

  free(data);
  return stream;
}


/**
 * Check if header is valid. If so, return the stream size. Else, return 0.
 */
POSH_PUBLIC_API(int)
_stream_header_ok(byte_t *header)
{
  /* /\* get the stream size from the header section *\/ */
  /* tellapic_u32_t ssize = _read_header_ssize(header); */

  /* /\* check if the stream size is upon valid values *\/ */
  /* if (ssize >= HEADER_SIZE && ssize <= MAX_STREAM_SIZE) */
  /*   { */
  /*     /\* if so, then check whether or not the control byte and the stream size are ok *\/ */
  /*     if ( !( header[CBYTE_INDEX] == CTL_CL_BMSG  && ssize <= MAX_BMSG_STREAM_SIZE   && ssize >= MIN_BMSG_STREAM_SIZE ) */
  /* 	   && */
  /* 	   !( header[CBYTE_INDEX] == CTL_CL_PMSG  && ssize <= MAX_PMSG_STREAM_SIZE   && ssize >= MIN_PMSG_STREAM_SIZE ) */
  /* 	   && */
  /* 	   !( header[CBYTE_INDEX] == CTL_CL_FIG   && ssize <= MAX_FIGTXT_STREAM_SIZE && ssize >= MIN_FIGTXT_STREAM_SIZE ) */
  /* 	   && */
  /* 	   !( header[CBYTE_INDEX] == CTL_CL_DRW   && (ssize == DRW_INIT_STREAM_SIZE || ssize == DRW_USING_STREAM_SIZE) ) */
  /* 	   && */
  /* 	   !( header[CBYTE_INDEX] == CTL_SV_FILE  && ssize <= MAX_STREAM_SIZE && ssize >= MIN_CTLEXT_STREAM_SIZE) */
  /* 	   && */
  /* 	   !( (header[CBYTE_INDEX] == CTL_CL_PWD || */
  /* 	       header[CBYTE_INDEX] == CTL_CL_NAME || */
  /* 	       header[CBYTE_INDEX] == CTL_SV_CLADD) && ssize <= MAX_CTLEXT_STREAM_SIZE && ssize >= MIN_CTLEXT_STREAM_SIZE ) */
  /* 	   && */
  /* 	   !(( header[CBYTE_INDEX] == CTL_CL_FILEASK ||  */
  /* 	       header[CBYTE_INDEX] == CTL_CL_FILEOK  || */
  /* 	       header[CBYTE_INDEX] == CTL_SV_PWDFAIL || */
  /* 	       header[CBYTE_INDEX] == CTL_SV_PWDOK   || */
  /* 	       header[CBYTE_INDEX] == CTL_CL_CLIST   || */
  /* 	       header[CBYTE_INDEX] == CTL_CL_DISC    || */
  /* 	       header[CBYTE_INDEX] == CTL_SV_CLRM    || */
  /* 	       header[CBYTE_INDEX] == CTL_SV_ID      || */
  /* 	       header[CBYTE_INDEX] == CTL_SV_FIGID   || */
  /* 	       header[CBYTE_INDEX] == CTL_SV_AUTHOK  || */
  /* 	       header[CBYTE_INDEX] == CTL_CL_PING    || */
  /* 	       header[CBYTE_INDEX] == CTL_SV_NAMEINUSE) && ssize == CTL_STREAM_SIZE ) */
  /* 	   ) */
  /* 	{ */
  /* 	  /\* header and stream size are invalid *\/ */
  /* 	  return 0; */
  /* 	} */
  /*     else */
  /* 	{ */
  /* 	  /\* header and ssize valids *\/ */
  /* 	  return ssize; */
  /* 	} */
  /*   } */
  header_t wheader;
  _wrap_stream_header(&wheader, header);

  if (wheader.ssize == 0 || wheader.cbyte == CTL_NOSTREAM)
    return 0;
  else
    return wheader.ssize;
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
POSH_PUBLIC_API(long)
tellapic_rawsend(tellapic_socket_t socket, byte_t *rawstream)
{
  tellapic_u32_t ssize = _read_stream_size(rawstream);

  tellapic_u32_t r = send(socket.s_socket, rawstream, ssize, 0);
  
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
      pointer = _copy_privchat_data(pointer, stream->data.chat, stream->header.ssize);
      break;

    case CTL_CL_BMSG:
      pointer = _copy_broadchat_data(pointer, stream->data.chat, stream->header.ssize);
      break;

    case CTL_CL_FIG:
      pointer = _copy_drawing_data(pointer, *stream);
      break;

    case CTL_CL_DRW:
      if (stream->header.ssize == DRW_INIT_STREAM_SIZE)
	pointer = _copy_drawing_init(pointer, *stream);
      else
	pointer = _copy_drawing_using(pointer, *stream);
      break;


    case CTL_SV_CLIST:
      /* Implementing this will complicate things. Instead, send CTL_SV_CLADD for each client the list has when an user ask for a client list. */
      /* The client will add the user if it wasn't already added. */
      break;

    case CTL_SV_FILE:
      pointer = _copy_file_data(pointer, stream->data.file, stream->header.ssize - HEADER_SIZE);
      break;

    case CTL_CL_PWD:
    case CTL_SV_CLADD:
    case CTL_CL_NAME:
      pointer = _copy_control_extended_data(pointer, stream->data.control, stream->header.ssize - HEADER_SIZE -1);
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
      pointer = _copy_control_data(pointer, stream->data.control);
      break;

    default:
      /* wrong header */
      return 0;
    }

  bytesSent = send(socket.s_socket, rawstream, stream->header.ssize, 0);

  free(rawstream);
  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(long)
tellapic_send_file(tellapic_socket_t socket, FILE *file, tellapic_u32_t filesize)
{
  long bytesSent = 0;
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

  bytesSent = send(socket.s_socket, rawstream, filesize + HEADER_SIZE, 0);

  free(rawstream);

  return bytesSent;  
}


/* /\** */
/*  * */
/*  *\/ */
/* POSH_PUBLIC_API(long) */
/* tellapic_send_struct(tellapic_socket_t socket, stream_t *stream) */
/* { */
/*   long       result = 0; */
/*   unsigned int  textlen = 0; */
  
/*   /\* If stream was a broadcast chat message *\/ */
/*   if (tellapic_ischatb(&(stream->header))) */
/*     { */
/*       for (textlen = 0; textlen < MAX_TEXT_SIZE && stream->data.chat.type.broadmsg[textlen] != '\0'; textlen++); */
/*       result  = tellapic_send_chatb( */
/* 				    socket, */
/* 				    stream->data.chat.idfrom, */
/* 				    textlen, */
/* 				    (char *)stream->data.chat.type.broadmsg */
/* 				    ); */
/*     } */



/*   /\* If stream was a private chat message *\/ */
/*   else if (tellapic_ischatp(&(stream->header))) */
/*     { */
/*       for (textlen = 0; textlen < MAX_TEXT_SIZE && stream->data.chat.type.privmsg.text[textlen] != '\0'; textlen++); */
/*       result  = tellapic_send_chatp( */
/* 				    socket, */
/* 				    stream->data.chat.idfrom, */
/* 				    stream->data.chat.type.privmsg.idto, */
/* 				    textlen, */
/* 				    (char *)stream->data.chat.type.privmsg.text */
/* 				    ); */
/*     } */


/*   /\* If stream was a control message *\/ */
/*   else if (tellapic_isctl(&(stream->header))) */
/*     { */
/*       result = tellapic_send_ctl( */
/* 				 socket, */
/* 				 stream->data.control.idfrom, */
/* 				 stream->header.cbyte */
/* 				 ); */
				 
/*     } */


/*   /\* If stream was an extended control message *\/ */
/*   else if (tellapic_isctle(&(stream->header))) */
/*     { */
/*       for (textlen = 0; textlen < MAX_INFO_SIZE && stream->data.control.info[textlen] != '\0'; textlen++); */
/*       result = tellapic_send_ctle( */
/* 				  socket, */
/* 				  stream->data.control.idfrom, */
/* 				  stream->header.cbyte, */
/* 				  textlen, */
/* 				  (char *)stream->data.control.info */
/* 				  ); */
/*     } */


/*   /\* If stream was a file message *\/ */
/*   else if (tellapic_isfile(&(stream->header))) */
/*     { */
/*     } */



/*   /\* If stream was a drawing message *\/ */
/*   else if (tellapic_isdrw(&(stream->header))) */
/*     { */
/*       if ( (stream->data.drawing.dcbyte & EVENT_PRESS) == EVENT_PRESS) */
/* 	{ */
/* 	  float dash_array[4]; */
/* #         ifdef LITTLE_ENDIAN_VALUE */
/* 	  dash_array[0] = POSH_FloatFromLittleBits(stream->data.drawing.type.figure.dash_array[0]); */
/* 	  dash_array[1] = POSH_FloatFromLittleBits(stream->data.drawing.type.figure.dash_array[1]); */
/* 	  dash_array[2] = POSH_FloatFromLittleBits(stream->data.drawing.type.figure.dash_array[2]); */
/* 	  dash_array[3] = POSH_FloatFromLittleBits(stream->data.drawing.type.figure.dash_array[3]); */
/* #         else */
/* 	  dash_array[0] = POSH_FloatFromBigBits(stream->data.drawing.type.figure.dash_array[0]); */
/* 	  dash_array[1] = POSH_FloatFromBigBits(stream->data.drawing.type.figure.dash_array[1]); */
/* 	  dash_array[3] = POSH_FloatFromBigBits(stream->data.drawing.type.figure.dash_array[2]); */
/* 	  dash_array[4] = POSH_FloatFromBigBits(stream->data.drawing.type.figure.dash_array[3]); */
/* #         endif */
/* 	  result = tellapic_send_drw_init( */
/* 					  socket, */
/* 					  stream->data.drawing.dcbyte, */
/* 					  stream->data.drawing.dcbyte_ext, */
/* 					  stream->data.drawing.idfrom, */
/* #                                         ifdef LITTLE_ENDIAN_VALUE */
/* 					  POSH_LittleU32(stream->data.drawing.number), */
/* 					  POSH_FloatFromLittleBits(stream->data.drawing.width), */
/* 					  POSH_FloatFromLittleBits(stream->data.drawing.opacity), */
/* #                                         else */
/* 					  POSH_BigU32(stream->data.drawing.number), */
/* 					  POSH_FloatFromBigBits(stream->data.drawing.width), */
/* 					  POSH_FloatFromBigBits(stream->data.drawing.opacity), */
/* #                                         endif */
/* 					  stream->data.drawing.fillcolor.red, */
/* 					  stream->data.drawing.fillcolor.green, */
/* 					  stream->data.drawing.fillcolor.blue, */
/* 					  stream->data.drawing.fillcolor.alpha, */
/* 					  stream->data.drawing.point1.x, */
/* 					  stream->data.drawing.point1.y, */
/* 					  stream->data.drawing.type.figure.color.red, */
/* 					  stream->data.drawing.type.figure.color.green, */
/* 					  stream->data.drawing.type.figure.color.blue, */
/* 					  stream->data.drawing.type.figure.color.alpha, */
/* 					  stream->data.drawing.type.figure.point2.x, */
/* 					  stream->data.drawing.type.figure.point2.y, */
/* 					  stream->data.drawing.type.figure.linejoin, */
/* 					  stream->data.drawing.type.figure.endcaps, */
/* #                                         ifdef LITTLE_ENDIAN_VALUE */
/* 					  POSH_FloatFromLittleBits(stream->data.drawing.type.figure.miterlimit), */
/* 					  POSH_FloatFromLittleBits(stream->data.drawing.type.figure.dash_phase), */
/* #                                         else */
/* 					  POSH_FloatFromBigBits(stream->data.drawing.type.figure.miterlimit), */
/* 					  POSH_FloatFromBigBits(stream->data.drawing.type.figure.dash_phase), */
/* #                                         endif */
/* 					  dash_array */
/* 					  ); */
/* 	} */
/*       else */
/* 	{ */
/* 	  result = tellapic_send_drw_using( */
/* 					   socket, */
/* 					   stream->data.drawing.dcbyte, */
/* 					   stream->data.drawing.dcbyte_ext, */
/* 					   stream->data.drawing.idfrom, */
/* #                                          ifdef LITTLE_ENDIAN_VALUE */
/* 					   POSH_LittleU32(stream->data.drawing.number), */
/* 					   POSH_FloatFromLittleBits(stream->data.drawing.width), */
/* 					   POSH_FloatFromLittleBits(stream->data.drawing.opacity), */
/* #                                          else */
/* 					   POSH_BigU32(stream->data.drawing.number), */
/* 					   POSH_FloatFromBigBits(stream->data.drawing.width), */
/* 					   POSH_FloatFromBigBits(stream->data.drawing.opacity), */
/* #                                          endif */
/* 					   stream->data.drawing.fillcolor.red, */
/* 					   stream->data.drawing.fillcolor.green, */
/* 					   stream->data.drawing.fillcolor.blue, */
/* 					   stream->data.drawing.fillcolor.alpha, */
/* 					   stream->data.drawing.point1.x, */
/* 					   stream->data.drawing.point1.y */
/* 					   ); */
/* 	} */
/*     } */



/*   /\* If stream was a figure message *\/ */
/*   else if (tellapic_isfig(&(stream->header))) */
/*     { */
/*       if (tellapic_isfigtxt(stream)) */
/* 	{ */
/* 	  result = tellapic_send_text( */
/* 				      socket, */
/* 				      stream->data.drawing.dcbyte, */
/* 				      stream->data.drawing.idfrom, */
/* #                                     ifdef LITTLE_ENDIAN_VALUE  */
/* 				      POSH_LittleU32(stream->data.drawing.number), */
/* 				      POSH_FloatFromLittleBits(stream->data.drawing.width), */
/* 				      POSH_FloatFromLittleBits(stream->data.drawing.opacity), */
/* #                                     else  */
/* 				      POSH_BigU32(stream->data.drawing.number), */
/* 				      POSH_FloatFromBigBits(stream->data.drawing.width), */
/* 				      POSH_FloatFromBigBits(stream->data.drawing.opacity), */
/* #                                     endif */
/* 				      stream->data.drawing.fillcolor.red, */
/* 				      stream->data.drawing.fillcolor.green, */
/* 				      stream->data.drawing.fillcolor.blue, */
/* 				      stream->data.drawing.fillcolor.alpha, */
/* 				      stream->data.drawing.point1.x, */
/* 				      stream->data.drawing.point1.y, */
/* 				      stream->data.drawing.type.text.color.red, */
/* 				      stream->data.drawing.type.text.color.green, */
/* 				      stream->data.drawing.type.text.color.blue, */
/* 				      stream->data.drawing.type.text.color.alpha, */
/* 				      stream->data.drawing.type.text.style, */
/* 				      stream->data.drawing.type.text.facelen, */
/* 				      (char *)stream->data.drawing.type.text.face, */
/* 				      stream->data.drawing.type.text.infolen, */
/* 				      (char *)stream->data.drawing.type.text.info */
/* 				      ); */
/* 	} */
/*       else */
/* 	{ */
/* 	  float dash_array[4]; */
/* #         ifdef LITTLE_ENDIAN_VALUE */
/* 	  dash_array[0] = POSH_FloatFromLittleBits(stream->data.drawing.type.figure.dash_array[0]); */
/* 	  dash_array[1] = POSH_FloatFromLittleBits(stream->data.drawing.type.figure.dash_array[1]); */
/* 	  dash_array[2] = POSH_FloatFromLittleBits(stream->data.drawing.type.figure.dash_array[2]); */
/* 	  dash_array[3] = POSH_FloatFromLittleBits(stream->data.drawing.type.figure.dash_array[3]); */
/* #         else */
/* 	  dash_array[0] = POSH_FloatFromBigBits(stream->data.drawing.type.figure.dash_array[0]); */
/* 	  dash_array[1] = POSH_FloatFromBigBits(stream->data.drawing.type.figure.dash_array[1]); */
/* 	  dash_array[2] = POSH_FloatFromBigBits(stream->data.drawing.type.figure.dash_array[2]); */
/* 	  dash_array[3] = POSH_FloatFromBigBits(stream->data.drawing.type.figure.dash_array[3]); */
/* #         endif  */
/* 	  result = tellapic_send_fig( */
/* 				     socket, */
/* 				     stream->data.drawing.dcbyte, */
/* 				     stream->data.drawing.dcbyte_ext, */
/* 				     stream->data.drawing.idfrom, */
/* #                                ifdef LITTLE_ENDIAN_VALUE  */
/* 				     POSH_LittleU32(stream->data.drawing.number), */
/* 				     POSH_FloatFromLittleBits(stream->data.drawing.width), */
/* 				     POSH_FloatFromLittleBits(stream->data.drawing.opacity), */
/* #                                else  */
/* 				     POSH_BigU32(stream->data.drawing.number), */
/* 				     POSH_FloatFromBigBits(stream->data.drawing.width), */
/* 				     POSH_FloatFromBigBits(stream->data.drawing.opacity), */
/* #                                endif */
/* 				     stream->data.drawing.fillcolor.red, */
/* 				     stream->data.drawing.fillcolor.green, */
/* 				     stream->data.drawing.fillcolor.blue, */
/* 				     stream->data.drawing.fillcolor.alpha, */
/* 				     stream->data.drawing.point1.x, */
/* 				     stream->data.drawing.point1.y, */
/* 				     stream->data.drawing.type.figure.color.red, */
/* 				     stream->data.drawing.type.figure.color.green, */
/* 				     stream->data.drawing.type.figure.color.blue, */
/* 				     stream->data.drawing.type.figure.color.alpha, */
/* 				     stream->data.drawing.type.figure.point2.x, */
/* 				     stream->data.drawing.type.figure.point2.y, */
/* 				     stream->data.drawing.type.figure.linejoin, */
/* 				     stream->data.drawing.type.figure.endcaps, */
/* #                                ifdef LITTLE_ENDIAN_VALUE  */
/* 				     POSH_FloatFromLittleBits(stream->data.drawing.type.figure.miterlimit), */
/* 				     POSH_FloatFromLittleBits(stream->data.drawing.type.figure.dash_phase), */
/* #                                else */
/* 				     POSH_FloatFromBigBits(stream->data.drawing.type.figure.miterlimit), */
/* 				     POSH_FloatFromBigBits(stream->data.drawing.type.figure.dash_phase), */
/* #                                endif */
/* 				     dash_array */
/* 				     );  */
/* 	} */
/*     } */

/*   return result; */
/* } */


/**
 *
 */
POSH_PUBLIC_API(long)
tellapic_send_text(tellapic_socket_t socket,
		   int tool,
		   int idfrom,
		   unsigned long dnum,
		   float w,
		   float op,
		   int fillred,
		   int fillgreen,
		   int fillblue,
		   int fillalpha,
		   int x2,
		   int y2,
		   int x1,
		   int y1,
		   int textred,
		   int textgreen,
		   int textblue,
		   int textalpha,
		   int style,
		   int facelen,
		   char *face,
		   int infolen,
		   char *info)
{
  long         bytesSent = 0;
  /* minus two because MIN_FIGTXT_STREAM_SIZE takes into account 1 char for face and 1 char for info as minimun lengths */
  tellapic_u32_t ssize = MIN_FIGTXT_STREAM_SIZE + facelen + infolen - 2; 
  byte_t         *rawstream = malloc(ssize);
  void           *pointer = rawstream;

  /*******************************************************************************************************************/  
  /* IMPORTANT: SEE WriteByte* IMPLEMENTATION. DO NOT ALTER THE ORDER AS THE POINTER IS MOVED BETWEEN FUNCTION CALLS */
  /*******************************************************************************************************************/
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
  pointer = WriteByte(pointer, (byte_t)fillred);                         /*  |     color red     |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)fillgreen);                       /*  |    color green    |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)fillblue);                        /*  |     color blue    |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)fillalpha);                       /*  |     color alpha   |  1 byte            */
  pointer = POSH_WriteU16ToLittle(pointer, POSH_LittleU16(x2));          /*  |     x point 2     |  2 bytes           */
  pointer = POSH_WriteU16ToLittle(pointer, POSH_LittleU16(y2));          /*  |     y point 2     |  2 bytes           */
  pointer = POSH_WriteU16ToLittle(pointer, POSH_LittleU16(x1));          /*  |     x point 1     |  2 bytes           */
  pointer = POSH_WriteU16ToLittle(pointer, POSH_LittleU16(y1));          /*  |     y point 1     |  2 bytes           */
  pointer = WriteByte(pointer, (byte_t)textred);                         /*  |     color red     |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)textgreen);                       /*  |    color green    |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)textblue);                        /*  |     color blue    |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)textalpha);                       /*  |     color alpha   |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)style);                           /*  |    font style     |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)facelen);                         /*  |   face name len   |  1 byte            */
  pointer = POSH_WriteU16ToLittle(pointer, POSH_LittleU16(infolen));     /*  |      infolen      |  2 bytes           */
  pointer = WriteBytes(pointer, face, facelen);                          /*  |   font face name  |  facenamelen bytes */
  pointer = WriteBytes(pointer, info, infolen);                          /*  |       text        |  infolen bytes     */
                                                                         /*  +-------------------+                    */
                                                                         /*  total bytes = 37 + facenamelen + infolen */
                                                                         
                                                                         
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
  pointer = WriteByte(pointer, (byte_t)fillred);                         /*  |     color red     |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)fillgreen);                       /*  |    color green    |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)fillblue);                        /*  |     color blue    |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)fillalpha);                       /*  |     color alpha   |  1 byte            */
  pointer = POSH_WriteU16ToBig(pointer, POSH_BigU16(x2));                /*  |     x point 2     |  2 bytes           */
  pointer = POSH_WriteU16ToBig(pointer, POSH_BigU16(y2));                /*  |     y point 2     |  2 bytes           */
  pointer = POSH_WriteU16ToBig(pointer, POSH_BigU16(x1));                /*  |     x point 1     |  2 bytes           */
  pointer = POSH_WriteU16ToBig(pointer, POSH_BigU16(y1));                /*  |     y point 1     |  2 bytes           */
  pointer = WriteByte(pointer, (byte_t)textred);                         /*  |     color red     |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)textgreen);                       /*  |    color green    |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)textblue);                        /*  |     color blue    |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)textalpha);                       /*  |     color alpha   |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)style);                           /*  |    font style     |  1 byte            */
  pointer = WriteByte(pointer, (byte_t)facelen);                         /*  |   face name len   |  1 byte            */
  pointer = POSH_WriteU16ToBig(pointer, POSH_BigU16(infolen));           /*  |      infolen      |  2 bytes           */
  pointer = WriteBytes(pointer, face, facelen);                          /*  |   font face name  |  facenamelen bytes */
  pointer = WriteBytes(pointer, info, infolen);                          /*  |       text        |  infolen bytes     */
                                                                         /*  +-------------------+                    */
                                                                         /*  total bytes = 37 + facenamelen + infolen */
#endif

  bytesSent = send(socket.s_socket, rawstream, ssize, 0);
  fflush(stdout);
  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(long)
tellapic_send_drw_using(tellapic_socket_t socket,
			int tool,
			int dcbyte_ext,
			int idfrom,
			unsigned long dnum,
			float w,
			float op,
			int fillred,
			int fillgreen,
			int fillblue,
			int fillalpha,
			int x2,
			int y2)
{
  long            bytesSent = 0;
  byte_t         *rawstream = malloc(DRW_USING_STREAM_SIZE);
  void           *pointer   = rawstream;

  /*******************************************************************************************************************/  
  /* IMPORTANT: SEE WriteByte* IMPLEMENTATION. DO NOT ALTER THE ORDER AS THE POINTER IS MOVED BETWEEN FUNCTION CALLS */
  /*******************************************************************************************************************/

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
  pointer = WriteByte(pointer, (byte_t)fillred);                        /* |   color red   | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)fillgreen);                      /* |   color green | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)fillblue);                       /* |   color blue  | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)fillalpha);                      /* |   color alpha | 1 byte  */
  pointer = POSH_WriteU16ToLittle(pointer, x2);                         /* |    x point 2  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, y2);                         /* |    y point 2  | 2 bytes */
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
  pointer = WriteByte(pointer, (byte_t)fillred);
  pointer = WriteByte(pointer, (byte_t)fillgreen);
  pointer = WriteByte(pointer, (byte_t)fillblue);
  pointer = WriteByte(pointer, (byte_t)fillalpha);
  pointer = POSH_WriteU16ToBig(pointer, x2);
  pointer = POSH_WriteU16ToBig(pointer, y2);
#endif  

  bytesSent = send(socket.s_socket, rawstream, DRW_USING_STREAM_SIZE, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(long)
tellapic_send_drw_init(tellapic_socket_t socket,
		       int tool,
		       int dcbyte_ext,
		       int idfrom,
		       unsigned long dnum,
		       float w,
		       float op,
		       int fillred,
		       int fillgreen,
		       int fillblue,
		       int fillalpha,
		       int x2,
		       int y2,
		       int x1,
		       int y1,
		       int strokered,
		       int strokegreen,
		       int strokeblue,
		       int strokealpha,
		       int lj,
		       int ec,
		       float ml,
		       float dp,
		       float da[])
{
  long            bytesSent = 0;
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
  pointer = WriteByte(pointer, (byte_t)fillred);                        /* |   color red   | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)fillgreen);                      /* |   color green | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)fillblue);                       /* |   color blue  | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)fillalpha);                      /* |   color alpha | 1 byte  */
  pointer = POSH_WriteU16ToLittle(pointer, x2);                         /* |    x point 2  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, y2);                         /* |    y point 2  | 2 bytes */  
  pointer = POSH_WriteU16ToLittle(pointer, x1);                         /* |    x point 1  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, y1);                         /* |    y point 1  | 2 bytes */
  pointer = WriteByte(pointer, (byte_t)strokered);                      /* |   color red   | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)strokegreen);                    /* |   color green | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)strokeblue);                     /* |   color blue  | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)strokealpha);                    /* |   color alpha | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)lj);                             /* |   line joins  | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)ec);                             /* |   end caps    | 1 byte  */  
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(ml));   /* |   miter limit | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(dp));   /* |   dash phase  | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[0]));/* |   dash array0 | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[1]));/* |   dash array1 | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[2]));/* |   dash array2 | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[3]));/* |   dash array3 | 4 bytes */
#else                                                                   /* +---------------+         */
                                                                        /* total bytes = 63 bytes    */
                                                                        
  pointer = WriteByte(pointer, 1);
  pointer = WriteByte(pointer, CTL_CL_DRW);
  pointer = POSH_WriteU32ToBig(pointer, DRW_INIT_STREAM_SIZE);
  pointer = WriteByte(pointer, (byte_t)idfrom);
  pointer = WriteByte(pointer, (byte_t)tool);
  pointer = WriteByte(pointer, (byte_t)dcbyte_ext);
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigU32(dnum));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(w));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(op));
  pointer = WriteByte(pointer, (byte_t)fillred);
  pointer = WriteByte(pointer, (byte_t)fillgreen);
  pointer = WriteByte(pointer, (byte_t)fillblue); 
  pointer = WriteByte(pointer, (byte_t)fillalpha);
  pointer = POSH_WriteU16ToBig(pointer, x2);
  pointer = POSH_WriteU16ToBig(pointer, y2);
  pointer = POSH_WriteU16ToBig(pointer, x1);
  pointer = POSH_WriteU16ToBig(pointer, y1);
  pointer = WriteByte(pointer, (byte_t)strokered);
  pointer = WriteByte(pointer, (byte_t)strokegreen);
  pointer = WriteByte(pointer, (byte_t)strokeblue); 
  pointer = WriteByte(pointer, (byte_t)strokealpha);
  pointer = WriteByte(pointer, (byte_t)lj);
  pointer = WriteByte(pointer, (byte_t)ec);
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(ml));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(dp));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[0]));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[1]));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[2]));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[3]));
#endif
  
  bytesSent = send(socket.s_socket, rawstream, DRW_INIT_STREAM_SIZE, 0);

  free(rawstream);

  return bytesSent;
}

/**
 *
 */
POSH_PUBLIC_API(long)
tellapic_send_fig(tellapic_socket_t socket,
		  int tool,
		  int dcbyte_ext,
		  int idfrom,
		  unsigned long dnum,
		  float w,
		  float op,
		  int fillred,
		  int fillgreen,
		  int fillblue,
		  int fillalpha,
		  int x2,
		  int y2,
		  int x1,
		  int y1,
		  int strokered,
		  int strokegreen,
		  int strokeblue,
		  int strokealpha,
		  int lj,
		  int ec,
		  float ml,
		  float dp,
		  float da[])
{
  long         bytesSent = 0;
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
  pointer = WriteByte(pointer, (byte_t)fillred);                        /* |   color red   | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)fillgreen);                      /* |   color green | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)fillblue);                       /* |   color blue  | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)fillalpha);                      /* |   color alpha | 1 byte  */
  pointer = POSH_WriteU16ToLittle(pointer, x2);                         /* |    x point 2  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, y2);                         /* |    y point 2  | 2 bytes */  
  pointer = POSH_WriteU16ToLittle(pointer, x1);                         /* |    x point 1  | 2 bytes */
  pointer = POSH_WriteU16ToLittle(pointer, y1);                         /* |    y point 1  | 2 bytes */
  pointer = WriteByte(pointer, (byte_t)strokered);                      /* |   color red   | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)strokegreen);                    /* |   color green | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)strokeblue);                     /* |   color blue  | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)strokealpha);                    /* |   color alpha | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)lj);                             /* |   line joins  | 1 byte  */
  pointer = WriteByte(pointer, (byte_t)ec);                             /* |   end caps    | 1 byte  */  
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(ml));   /* |   miter limit | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(dp));   /* |   dash phase  | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[0]));/* |   dash array0 | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[1]));/* |   dash array1 | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[2]));/* |   dash array0 | 4 bytes */
  pointer = POSH_WriteU32ToLittle(pointer, POSH_LittleFloatBits(da[3]));/* |   dash array1 | 4 bytes */
#else                                                                   /* +---------------+         */
  /* total bytes = 63 bytes    */
                                                                        
  pointer = WriteByte(pointer, 1);
  pointer = WriteByte(pointer, CTL_CL_FIG);
  pointer = POSH_WriteU32ToBig(pointer, FIG_STREAM_SIZE);
  pointer = WriteByte(pointer, (byte_t)idfrom);
  pointer = WriteByte(pointer, (byte_t)tool);
  pointer = WriteByte(pointer, (byte_t)dcbyte_ext);
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigU32(dnum));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(w));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(op));
  pointer = WriteByte(pointer, (byte_t)fillred);
  pointer = WriteByte(pointer, (byte_t)fillgreen);
  pointer = WriteByte(pointer, (byte_t)fillblue); 
  pointer = WriteByte(pointer, (byte_t)fillalpha);
  pointer = POSH_WriteU16ToBig(pointer, x2);
  pointer = POSH_WriteU16ToBig(pointer, y2);
  pointer = POSH_WriteU16ToBig(pointer, x1);
  pointer = POSH_WriteU16ToBig(pointer, y1);
  pointer = WriteByte(pointer, (byte_t)strokered); 
  pointer = WriteByte(pointer, (byte_t)strokegreen);
  pointer = WriteByte(pointer, (byte_t)strokeblue); 
  pointer = WriteByte(pointer, (byte_t)strokealpha);
  pointer = WriteByte(pointer, (byte_t)lj);
  pointer = WriteByte(pointer, (byte_t)ec);
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(ml));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(dp));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[0]));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[1]));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[2]));
  pointer = POSH_WriteU32ToBig(pointer, POSH_BigFloatBits(da[3]));
#endif

  bytesSent = send(socket.s_socket, rawstream, FIG_STREAM_SIZE, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(long)
tellapic_send_chatp(tellapic_socket_t socket, int idfrom, int idto, int textlen, char* text)
{
  long        bytesSent = 0;
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

  bytesSent = send(socket.s_socket, rawstream, ssize, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(long)
tellapic_send_chatb(tellapic_socket_t socket, int idfrom, int textlen, char* text)
{
  long        bytesSent = 0;
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

  bytesSent = send(socket.s_socket, rawstream, ssize, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(long)
tellapic_send_ctle(tellapic_socket_t socket, int idfrom, int ctle, int infolen,  char *info)
{
  long bytesSent = 0;
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

  bytesSent = send(socket.s_socket, rawstream, ssize, 0);

  free(rawstream);

  return bytesSent;
}


/**
 *
 */
POSH_PUBLIC_API(long)
tellapic_send_ctl(tellapic_socket_t socket, int idfrom, int ctl)
{
  long bytesSent = 0;
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
  

  bytesSent = send(socket.s_socket, rawstream, CTL_STREAM_SIZE, 0);

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
	   header->cbyte == CTL_CL_RMFIG ||
	   header->cbyte == CTL_SV_FIGID ||
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
