/**
 *   Copyright (c) 2010 Sebastián Treu.
 *
 *   This file is part of tellapic library.
 *
 *   Tellapic library is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation; version 3 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 */

#ifndef TYPES_H_
#define TYPES_H_

#include "tellapic/constants.h"
#include "posh/posh.h"

#if defined (linux) || defined (LINUX)
       typedef int tellapic_socket_t;
#elif defined (WIN32) || defined (_WIN32)
#      include <winsock2.h>
       typedef int tellapic_socket_t;
#else
#      error "Fuck you"
#endif



typedef posh_byte_t byte_t;  /* basic byte unit */
typedef posh_u16_t  tellapic_u16_t;
typedef posh_u32_t  tellapic_u32_t;
typedef posh_u32_t  tellapic_float;


typedef struct {
  byte_t red;
  byte_t green;
  byte_t blue;
} color_t;


typedef enum { 
  END_CAPS_BUTT,
  END_CAPS_SQUARE,
  END_CAPS_ROUND 
} endcaps_t;


typedef enum {
  LINE_JOIN_MITER,
  LINE_JOIN_ROUND,
  LINE_JOIN_BEVEL
} linejoin_t;


typedef struct {
  tellapic_u16_t x;
  tellapic_u16_t y;
} point_t;


typedef struct {
  byte_t             endian;            /* endian byte */
  byte_t             cbyte;             /* control byte */
  tellapic_u32_t      ssize;
} header_t;


typedef struct {
  byte_t    idto;
  byte_t    text[MAX_TEXT_SIZE];
} pmessage_t;


typedef struct {
  byte_t    idfrom;
  union {
    byte_t     broadmsg[MAX_TEXT_SIZE];
    pmessage_t privmsg;
  } type;
} message_t;


typedef struct {
  point_t        point2;       /* byte 22   4 bytes on stream */
  byte_t         linejoin;     /* byte 26   1 byte on stream */
  byte_t         endcaps;      /* byte 27   1 byte on stream */
  tellapic_float miterlimit;   /* byte 28   4 bytes on stream */
  tellapic_float dash_phase;   /* byte 32   4 bytes on stream */
  tellapic_float dash_array[2]; /* byte 36  8 bytes on stream */
} figure_t;


typedef struct {
  byte_t         style;                     /* byte 22 */
  byte_t         facelen;                   /* byte 23 */
  tellapic_u16_t infolen;                   /* byte 24 */
  byte_t         face[MAX_FONTFACE_LEN];    /* byte 26 */
  byte_t         info[MAX_TEXT_SIZE];       /* byte 26 + facelen */ 
} text_t;


typedef struct {
  byte_t                     idfrom;       /* byte 0    1 byte on stream */
  byte_t                     dcbyte;       /* byte 1    1 byte on stream */
  byte_t                     dcbyte_ext;   /* byte 2    1 byte on stream */
  tellapic_u32_t             number;       /* byte 3    4 bytes on stream */
  tellapic_float             width;        /* byte 7    4 bytes on stream */
  tellapic_float             opacity;      /* byte 11   4 bytes on stream */
  color_t                    color;        /* byte 15   3 bytes on stream */
  point_t                    point1;       /* byte 18   4 bytes on stream */
  union {
    figure_t                 figure;
    text_t                   text;         /* total: FONT_FACE_LEN + MAX_TEX_SIZE + 1 bytes on stream */
  } type;
} ddata_t;


typedef struct {
  byte_t   idfrom;
  byte_t   info[MAX_INFO_SIZE];
} svcontrol_t;


typedef struct {
  header_t header;
  union {
    ddata_t     drawing;
    message_t   chat; 
    svcontrol_t control;
    byte_t      *file;
  } data;
} stream_t;

#endif
