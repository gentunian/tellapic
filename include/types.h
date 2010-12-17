/*****************************************************************************
 *   Copyright (c) 2010 Sebastián Treu.
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
 *****************************************************************************/
#ifndef TYPES_H_
#define TYPES_H_

#include "constants.h"

typedef unsigned char byte_t;  //basic byte unit


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
  int x;
  int y;
} point_t;


typedef struct {
  byte_t    endian;            //endian byte
  byte_t    cbyte;             //control byte
  int       ssize;
} header_t;


typedef struct {
  byte_t    idto;
  char      text[MAX_TEXT_SIZE];
} pmessage_t;


typedef struct {
  byte_t    idfrom;
  union {
    char       text[MAX_TEXT_SIZE];
    pmessage_t private;
  } type;
} message_t;


typedef struct {
  point_t      point2;       //byte 21   4 bytes on stream
  byte_t       linejoin;     //byte 25   1 byte on stream
  byte_t       endcaps;      //byte 26   1 byte on stream
  float        miterlimit;   //byte 27   4 bytes on stream
  float        dash_phase;   //byte 31   4 bytes on stream
  float        dash_array[2]; //byte 35  8 bytes on stream
} figure_t;


typedef struct {
  byte_t       style;                      //byte 21
  byte_t       facelen;                    //byte 22
  char         face[MAX_FONTFACE_LEN];     //byte 23
  char         info[MAX_TEXT_SIZE];        //byte 23+FONT_FACE_LEN
} text_t;


typedef struct {
  byte_t       idfrom;       //byte 0    1 byte on stream
  byte_t       dcbyte;       //byte 1    1 byte on stream
  int          number;       //byte 2    4 bytes on stream
  float        width;        //byte 6    4 bytes on stream
  float        opacity;      //byte 10   4 bytes on stream
  color_t      color;        //byte 14   3 bytes on stream
  point_t      point1;       //byte 17   4 bytes on stream
  union {
    figure_t   figure;
    text_t     text;        // total: FONT_FACE_LEN + MAX_TEX_SIZE + 1 bytes on stream
  } type;
} ddata_t;


typedef struct {
  byte_t   idfrom;
  byte_t   info[MAX_INFO_SIZE];
} svcontrol_t;


/*
typedef struct {
  union {
    ddata_t     drawing;
    message_t   chat;
    svcontrol_t control;
  } type;
} data_t;
*/

typedef struct {
  header_t header;
  //data_t   data;
  union {
    ddata_t     drawing;
    message_t   chat;
    svcontrol_t control;
  } data;
} stream_t;

#endif
