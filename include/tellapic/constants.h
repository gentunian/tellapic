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

#ifndef CONSTANTS_H_
#define CONSTANTS_H_

#define MAX_TEXT_SIZE    512
#define MAX_INFO_SIZE    128
#define MAX_FONTFACE_LEN 48

/* 2^32 - 1 == 11111111 11111111 111111111 11111111 Maximum value for header.ssize (4 bytes) */
#define MAX_STREAM_SIZE  4294967295ULL
#define MAX_FILE_SIZE    MAX_STREAM_SIZE - HEADER_SIZE - 1

/* from stream */
#define ENDIAN_INDEX               0
#define CBYTE_INDEX                1
#define SBYTE_INDEX                2
#define HEADER_SIZE                6

/* This data streams have a fixed size */
#define FIG_STREAM_SIZE              44 + HEADER_SIZE
#define CTL_STREAM_SIZE              1  + HEADER_SIZE
#define DRW_USING_STREAM_SIZE        22 + HEADER_SIZE
#define DRW_INIT_STREAM_SIZE         FIG_STREAM_SIZE

/* This streams size may be variable but not more than the values below */
#define MIN_FIGTXT_STREAM_SIZE       HEADER_SIZE + DDATA_FONTFACE_INDEX + 2
#define MIN_PMSG_STREAM_SIZE         HEADER_SIZE + 3
#define MIN_BMSG_STREAM_SIZE         HEADER_SIZE + 2
#define MIN_CTLEXT_STREAM_SIZE       HEADER_SIZE + 1
#define MAX_FIGTXT_STREAM_SIZE       MAX_TEXT_SIZE + MAX_FONTFACE_LEN + HEADER_SIZE + DDATA_FONTFACE_INDEX
#define MAX_PMSG_STREAM_SIZE         MAX_TEXT_SIZE + HEADER_SIZE + 2
#define MAX_BMSG_STREAM_SIZE         MAX_TEXT_SIZE + HEADER_SIZE + 1 
#define MAX_CTLEXT_STREAM_SIZE       MAX_INFO_SIZE + HEADER_SIZE + 1


#define PMSG_TEXT_OFFSET    2
#define BMSG_TEXT_OFFSET    1

/* All data sections has the sender's id as the first byte */
#define DATA_IDFROM_INDEX          0


/* Indexes for PMSG (private message) data section. */
/* Useful for getting data from data section array  */
#define DATA_PMSG_IDFROM_INDEX     DATA_IDFROM_INDEX
#define DATA_PMSG_IDTO_INDEX       1
#define DATA_PMSG_TEXT_INDEX       2


/* Indexes for BMSG (broadcast message) data section */
#define DATA_BMSG_IDFROM_INDEX     DATA_IDFROM_INDEX
#define DATA_BMSG_TEXT_INDEX       1


/* Indexes for FIG (drawn figure) data section */
#define DDATA_IDFROM_INDEX          DATA_IDFROM_INDEX
#define DDATA_DCBYTE_INDEX          1
#define DDATA_DCBYTE_EXT_INDEX      2
#define DDATA_DNUMBER_INDEX         3
#define DDATA_WIDTH_INDEX           7
#define DDATA_OPACITY_INDEX        11
#define DDATA_COLOR_INDEX          15
#define DDATA_COORDX1_INDEX        18
#define DDATA_COORDY1_INDEX        20
#define DDATA_COORDX2_INDEX        22
#define DDATA_COORDY2_INDEX        24
#define DDATA_JOINS_INDEX          26
#define DDATA_CAPS_INDEX           27
#define DDATA_MITER_INDEX          28
#define DDATA_DASHPHASE_INDEX      32
#define DDATA_DASHARRAY_INDEX      36

/* for text */
#define DDATA_FONTSTYLE_INDEX      22
#define DDATA_FONTFACELEN_INDEX    23
#define DDATA_FONTNAMELEN_INDEX    24
#define DDATA_FONTFACE_INDEX       25
#define DDATA_TEXT_INDEX(fontface_len)		\
  (DDATA_FONTFACE_INDEX + fontface_len)


/* Indexes for CLADD (client connected) data section */
#define DATA_CLADD_IDFROM_INDEX  DATA_IDFROM_INDEX
#define DATA_CLADD_NAME_INDEX    1


/* Indexes for CLRM (client disconnected) data section */
#define DATA_CLRM_IDFROM_INDEX   DATA_IDFROM_INDEX


/* Indexes for CLIST (clients list) data section */
#define DATA_CLIST_IDFROM_INDEX  DATA_IDFROM_INDEX


/* Indexes for PWDASK data section */
/* Indexes for PWD data section */
/* Indexes for PWDOK data section */
#define DATA_PWD_IDFROM_INDEX DATA_IDFROM_INDEX
#define DATA_PWD_INDEX        1


/* Indexes for FILEASK, FILE and FILEOK data section*/
#define DATA_FILE_IDFROM_INDEX DATA_IDFROM_INDEX
#define DATA_FILE_INDEX        1


/****************************/
/* Control byte from client */
/****************************/
#define CTL_CL_BMSG     0x11   /* is chat */
#define CTL_CL_PMSG     0x21   /* is chat */
#define CTL_CL_FIG      0x31   /* is fig  */
#define CTL_CL_DRW      0x41   /* is drw  */
#define CTL_CL_CLIST    0x51   /* is ctle */
#define CTL_CL_PWD      0x61   /* is ctle */
#define CTL_CL_FILEASK  0x71   /* is ctl  */
#define CTL_CL_FILEOK   0x81   /* is ctl  */
#define CTL_CL_DISC     0x91   /* is ctl  */
#define CTL_CL_NAME     0xa1   /* is ctle */
#define CTL_NOPIPE      0xb1 
#define CTL_NOSTREAM    0xc1
#define CTL_CL_PING     0xd1
#define CTL_CL_TIMEOUT  0xe1
#define CTL_FAIL        0x00


/* from server */
#define CTL_SV_CLADD     0x10   /* is ctle */
#define CTL_SV_CLRM      0x20   /* is ctl  */
#define CTL_SV_CLIST     0x30   /* is ctle */
#define CTL_SV_PWDASK    0x40   /* is ctl  */
#define CTL_SV_PWDOK     0x50   /* is ctl  */
#define CTL_SV_PWDFAIL   0x60   /* is ctl  */
#define CTL_SV_FILE      0x70   /* is ctle */
#define CTL_SV_ID        0x80   /* is ctl  */
#define CTL_SV_NAMEINUSE 0x90   /* is ctl  */
#define CTL_SV_AUTHOK    0xa0   /* is ctl  */
#define CTL_SV_PONG      0xb0


/* Face properties */
#define FONT_STYLE_NORMAL      0
#define FONT_STYLE_BOLD        1
#define FONT_STYLE_ITALIC      2
#define FONT_STYLE_BOLD_ITALIC FONT_STYLE_BOLD + FONT_STYLE_ITALIC


/* Tools masks */
#define TOOL_MASK       0xf0
#define TOOL_MARKER     0x10
#define TOOL_PATH       0x20
#define TOOL_ELLIPSE    0x30
#define TOOL_RECT       0x40
#define TOOL_TEXT       0x50
#define TOOL_ERASER     0x60
#define TOOL_PENCIL     0x70
#define TOOL_LINE       0x80


/* Events masks */
#define BUTTON_LEFT      0x1
#define BUTTON_RIGHT     0x2
#define BUTTON_MIDDLE    0x3
#define BUTTON_MASK      0x3
#define EVENT_MASK       0xc
#define EVENT_PRESS      0x4
#define EVENT_DRAG       0x8
#define EVENT_RELEASE    0xc
#define EVENT_CTL_DOWN   0x1
#define EVENT_SHIFT_DOWN 0x2
#define EVENT_ALT_DOWN   0x3
#define EVENT_NULL       0x0


/* 
 * An event example:
 *                                 tool       event   button
 *                           _______________  ______  ______
 *                          /               \/      \/      \
 *	   	   	    +---+---+---+---+---+---+---+---+
 *	drawing byte   =    | ? | ? | ? | ? | 0 | 1 | 0 | 1 |  Press Event Left Button
 *	   	   	    +---+---+---+---+---+---+---+---+
 *
 *
 *                                 tool       event   button
 *                           _______________  ______  ______
 *                          /               \/      \/      \
 *	   	   	    +---+---+---+---+---+---+---+---+
 *	drawing byte   =    | ? | ? | ? | ? | 1 | 0 | 1 | 1 |  Drag Event Middle Button
 *	   	   	    +---+---+---+---+---+---+---+---+
 *
 *
 *                                 tool       event   button
 *                           _______________  ______  ______
 *                          /               \/      \/      \
 *	   	   	    +---+---+---+---+---+---+---+---+
 *	drawing byte   =    | ? | ? | ? | ? | 1 | 1 | 1 | 0 |  Release Event Right Button
 *	   	   	    +---+---+---+---+---+---+---+---+
 *
 *
*/
#define EVENT_PLEFT     EVENT_PRESS   + BUTTON_LEFT
#define EVENT_PRIGHT    EVENT_PRESS   + BUTTON_RIGHT
#define EVENT_PMIDDLE   EVENT_PRESS   + BUTTON_MIDDLE
#define EVENT_DLEFT     EVENT_DRAG    + BUTTON_LEFT
#define EVENT_DRIGHT    EVENT_DRAG    + BUTTON_RIGHT
#define EVENT_DMIDDLE   EVENT_DRAG    + BUTTON_MIDDLE
#define EVENT_RLEFT     EVENT_RELEASE + BUTTON_LEFT
#define EVENT_RRIGHT    EVENT_RELEASE + BUTTON_RIGHT
#define EVENT_RMIDDLE   EVENT_RELEASE + BUTTON_MIDDLE

/* Example: A byte representing a Marker with a press left event: b = TOOL_MARKER + EVENT_PLEFT; */

#endif
