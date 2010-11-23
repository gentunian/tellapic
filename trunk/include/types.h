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
  char*     text;
} pmessage_t;


typedef struct {
  byte_t    idfrom;
  union {
    char*      text;
    pmessage_t private;
  } type;
} message_t;


typedef struct {
  point_t      point2;       //byte 21
  byte_t       linejoin;     //byte 25
  byte_t       endcaps;      //byte 26
  float        miterlimit;   //byte 27
  float        dash_phase;   //byte 31
  float        dash_array[]; //byte 35
} figure_t;


typedef struct {
  byte_t       style;        //byte 21
  byte_t       namesize;     //byte 22
  char         *face;        //byte 23
  char         *info;        //byte 23+namesize
} text_t;


typedef struct {
  byte_t       idfrom;       //byte 0
  byte_t       dcbyte;       //byte 1
  int          number;       //byte 2
  float        width;        //byte 6
  float        opacity;      //byte 10
  color_t      color;        //byte 14
  point_t      point1;       //byte 17
  union {
    figure_t   figure;
    text_t     text;
  } type;
} ddata_t;


typedef struct {
  byte_t   idfrom;
  byte_t   *info;
} svcontrol_t;


typedef struct {
  union {
    ddata_t     drawing;
    message_t   chat;
    svcontrol_t control;
  } type;
} data_t;


typedef struct {
  header_t header;
  data_t   data;
} stream_t;

#endif
