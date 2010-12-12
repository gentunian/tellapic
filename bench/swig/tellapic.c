#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <resolv.h>
#include <netdb.h>
#include <fcntl.h>
#include <unistd.h>

#include "types.h"
#include "constants.h"
#include "tellapic.h"

//TODO: implement free_stream() which frees memory allocated in a stream_t structure

/**
 * Functions Declarations
 **/
int        read_b(int fd, size_t nbytes, byte_t *buf);
int        g_shift(byte_t *buf, int n);
byte_t     extract_byte(int byte, int data);


/**
 * Functions Definitions
 */
int g_shift(byte_t *buf, int n) {
  byte_t *data  = (buf + n - 1);
  int    result = 0;
  int i;

  for(i = 0; i < n; i++)
    result |= *(data--) << 8*i;

  return result;
}


/**
 *
 */
byte_t extract_byte(int byte, int data) {
  return (data>>byte*8) & 0xff;
}


/**
 * A wrapper from read() C function always in blocking mode
 */
int read_b(int fd, size_t nbytes, byte_t *buf) {
  //byte_t  *data = malloc(nbytes * sizeof(byte_t));
  int bytesleft = nbytes;
  int bytesread = 0;

  // This function should be feeded with a file descriptor with the O_NONBLOCK flag unset.
  // The *_b means that this function WILL block until data arrives.
  // Get the fd flags.
  int flags = fcntl(fd, F_GETFL);

  // If fd has O_NONBLOCK set, unset it
  if ((flags & O_NONBLOCK) == O_NONBLOCK)
    fcntl(fd, F_SETFL, flags & ~O_NONBLOCK);

  // Now we have a fd in blocking mode
  while(bytesleft > 0) {
    bytesread = recv(fd, buf + bytesread, nbytes, 0);
    if (bytesread > 0)
      bytesleft -= bytesread;
    else
      bytesleft = 0;
  }
  // restore the flags
  fcntl(fd, F_SETFL, flags);

  // return the data read, or null depending if we succeded or not.
  return bytesread;
}


/**
 *
 */
void headercpy(byte_t *rawstream, header_t header) {
  rawstream[ENDIAN_INDEX] = header.endian;
  rawstream[CBYTE_INDEX]  = header.cbyte;
  rawstream[SBYTE_INDEX]  = extract_byte(3, header.ssize);
  rawstream[SBYTE_INDEX+1]= extract_byte(2, header.ssize);
  rawstream[SBYTE_INDEX+2]= extract_byte(1, header.ssize);
  rawstream[SBYTE_INDEX+3]= extract_byte(0, header.ssize);
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
void ddatacpy(byte_t *rawstream, ddata_t ddata, int ssize) {
  int etype  = ddata.dcbyte & EVENT_MASK;
  int tool   = ddata.dcbyte & TOOL_MASK;

  // Copy the data section fixed part
  rawstream[DDATA_IDFROM_INDEX]    = ddata.idfrom;
  rawstream[DDATA_DCBYTE_INDEX]    = ddata.dcbyte;  
  rawstream[DDATA_COORDX1_INDEX]   = extract_byte(1, ddata.point1.x);
  rawstream[DDATA_COORDX1_INDEX+1] = extract_byte(0, ddata.point1.x);
  rawstream[DDATA_COORDY1_INDEX]   = extract_byte(1, ddata.point1.y);
  rawstream[DDATA_COORDY1_INDEX+1] = extract_byte(0, ddata.point1.y);
  rawstream[DDATA_DNUMBER_INDEX]   = ddata.number;
  rawstream[DDATA_WIDTH_INDEX]     = ddata.width;
  rawstream[DDATA_OPACITY_INDEX]   = ddata.opacity;
  rawstream[DDATA_COLOR_INDEX]     = ddata.color.red;
  rawstream[DDATA_COLOR_INDEX+1]   = ddata.color.green;
  rawstream[DDATA_COLOR_INDEX+2]   = ddata.color.blue;

  if (etype == EVENT_NULL) {
    // Deferred mode
    if (tool != TOOL_TEXT) {
      rawstream[DDATA_COORDX2_INDEX]   = extract_byte(1, ddata.type.figure.point2.x);
      rawstream[DDATA_COORDX2_INDEX+1] = extract_byte(0, ddata.type.figure.point2.x);
      rawstream[DDATA_COORDY2_INDEX]   = extract_byte(1, ddata.type.figure.point2.y);
      rawstream[DDATA_COORDY2_INDEX+1] = extract_byte(0, ddata.type.figure.point2.y);
      rawstream[DDATA_JOINS_INDEX]     = ddata.type.figure.linejoin;
      rawstream[DDATA_CAPS_INDEX]      = ddata.type.figure.endcaps;
      rawstream[DDATA_MITER_INDEX]     = ddata.type.figure.miterlimit;
      rawstream[DDATA_DASHPHASE_INDEX] = ddata.type.figure.dash_phase;
    } else {
      // Treat text different
      rawstream[DDATA_FONTSTYLE_INDEX] = ddata.type.text.style;
      rawstream[DDATA_FONTLEN_INDEX]   = ddata.type.text.namesize;
      memcpy(rawstream+DDATA_FONTFACE_INDEX, ddata.type.text.face, ddata.type.text.namesize);
      int textsize = ssize - (HEADER_SIZE + DDATA_TEXT_INDEX(ddata.type.text.namesize));
      memcpy(rawstream+DDATA_TEXT_INDEX(ddata.type.text.namesize), ddata.type.text.info, textsize);
    }
  }
}


/**
 * Stream data section for CTL_CL_PMSG control byte
 */
data_t wrap_pmsg_data(byte_t *streamdata, int ssize) {
  data_t data;
  int textlength = ssize - HEADER_SIZE - DATA_PMSG_IDTO_INDEX;

  data.type.chat.idfrom = streamdata[DATA_PMSG_IDFROM_INDEX];
  data.type.chat.type.private.idto = streamdata[DATA_PMSG_IDTO_INDEX];
  data.type.chat.type.private.text = malloc((textlength) * sizeof(char));
  memcpy(data.type.chat.type.private.text, streamdata + DATA_PMSG_TEXT_INDEX, textlength);

  return data;
}


/**
 * Stream data section for CTL_CL_BMSG control byte
 */
data_t wrap_bmsg_data(byte_t *streamdata, int ssize) {
  data_t data;
  int textlength = ssize - HEADER_SIZE - DATA_PMSG_IDFROM_INDEX;

  data.type.chat.idfrom = streamdata[DATA_BMSG_IDFROM_INDEX];
  data.type.chat.type.text = malloc(textlength * sizeof(char));
  memcpy(data.type.chat.type.text, streamdata + DATA_BMSG_TEXT_INDEX, textlength);

  return data;
}


/**
 * Stream data section for CTL_CL_CLADD control byte
 */
data_t wrap_cladd_data(byte_t *streamdata, int ssize) {
  data_t data;
  int namelen = ssize - HEADER_SIZE - DATA_CLADD_IDFROM_INDEX;
  
  data.type.control.idfrom = streamdata[DATA_CLADD_IDFROM_INDEX];
  data.type.control.info   = malloc(namelen * sizeof(byte_t));
  memcpy(data.type.control.info, streamdata + DATA_CLADD_NAME_INDEX, namelen);

  return data;
}


/**
 * Stream data section CTL_CL_FIG control byte
 */
data_t wrap_ddata(byte_t *ddatastream, int ssize) {
  data_t data;

  data.type.drawing.idfrom      = ddatastream[DDATA_IDFROM_INDEX];
  data.type.drawing.dcbyte      = ddatastream[DDATA_DCBYTE_INDEX];
  data.type.drawing.point1.x    = g_shift(ddatastream+DDATA_COORDX1_INDEX, 2);
  data.type.drawing.point1.y    = g_shift(ddatastream+DDATA_COORDY1_INDEX, 2);
  data.type.drawing.number      = ddatastream[DDATA_DNUMBER_INDEX];
  data.type.drawing.width       = ddatastream[DDATA_WIDTH_INDEX];
  data.type.drawing.opacity     = ddatastream[DDATA_OPACITY_INDEX];
  data.type.drawing.color.red   = ddatastream[DDATA_COLOR_INDEX];
  data.type.drawing.color.green = ddatastream[DDATA_COLOR_INDEX+1];
  data.type.drawing.color.blue  = ddatastream[DDATA_COLOR_INDEX+2];

  int tool  = data.type.drawing.dcbyte & TOOL_MASK;
  int etype = data.type.drawing.dcbyte & EVENT_MASK;
  
  if (etype == EVENT_NULL) {
    // TOOL_TEXT has different data
    if (tool != TOOL_TEXT) {
      data.type.drawing.type.figure.point2.x   = g_shift(ddatastream+DDATA_COORDX2_INDEX, 2);
      data.type.drawing.type.figure.point2.y   = g_shift(ddatastream+DDATA_COORDY2_INDEX, 2);
      data.type.drawing.type.figure.linejoin   = ddatastream[DDATA_JOINS_INDEX];
      data.type.drawing.type.figure.endcaps    = ddatastream[DDATA_CAPS_INDEX];
      data.type.drawing.type.figure.miterlimit = ddatastream[DDATA_MITER_INDEX];
      data.type.drawing.type.figure.dash_phase = ddatastream[DDATA_DASHPHASE_INDEX];
      int dasharray_size = ssize - HEADER_SIZE - (DDATA_DASHARRAY_INDEX + 1);
      if (dasharray_size > 0) {
	//      data.type.drawing.type.figure.dash_array = ddatastream+DDATA_DASHARRAY_INDEX;
      }
    } else {
      data.type.drawing.type.text.style    = ddatastream[DDATA_FONTSTYLE_INDEX];
      data.type.drawing.type.text.namesize = ddatastream[DDATA_FONTLEN_INDEX];
      data.type.drawing.type.text.face     = malloc(data.type.drawing.type.text.namesize+1); //REMEMBER TO FREE THIS
      int textsize = ssize - (HEADER_SIZE + DDATA_TEXT_INDEX(data.type.drawing.type.text.namesize));
      data.type.drawing.type.text.info = malloc(textsize+1);                                 //REMEMBER TO FREE THIS
      memcpy(data.type.drawing.type.text.face, ddatastream+DDATA_FONTFACE_INDEX, data.type.drawing.type.text.namesize);
      memcpy(data.type.drawing.type.text.info, ddatastream+DDATA_TEXT_INDEX(data.type.drawing.type.text.namesize), textsize);
      data.type.drawing.type.text.face[data.type.drawing.type.text.namesize] = '\0';
      data.type.drawing.type.text.info[textsize] = '\0';
    }
  }
  return data;
}


/**
 *
 */
stream_t tellapic_read_stream_b(int fd) {
  stream_t stream;
  header_t header;
  header = tellapic_read_header_b(fd);
  stream.header = header;
  if (header.cbyte != CTL_FAIL || header.cbyte != CTL_CL_DISC)
    stream = tellapic_read_data_b(fd, header);
  return stream;
}


/**
 *
 */
stream_t tellapic_read_data_b(int fd, header_t header) {
  stream_t stream;
  byte_t *data = malloc(header.ssize - HEADER_SIZE);
  int nbytes = read_b(fd, header.ssize - HEADER_SIZE, data);

  
  if (nbytes == header.ssize - HEADER_SIZE) {
    // Copy the header to the stream structure
    stream.header = header;

    // Copy the stream data section upon the control byte    
    switch(header.cbyte) {
    case CTL_CL_PMSG:
      stream.data = wrap_pmsg_data(data, header.ssize);
      break;

    case CTL_CL_BMSG:
      stream.data = wrap_bmsg_data(data, header.ssize);
      break;

    case CTL_CL_FIG:
      stream.data = wrap_ddata(data, header.ssize);
      break;

    case CTL_CL_DRW:
      break;

    case CTL_SV_CLADD:
      stream.data = wrap_cladd_data(data, header.ssize);
      break;

    case CTL_SV_CLIST:
      break;
      
    case CTL_CL_PWD:
    case CTL_SV_FILE:
      stream.data.type.control.info = malloc(header.ssize - HEADER_SIZE - 1);
      memcpy(stream.data.type.control.info, data+(HEADER_SIZE+1), header.ssize - HEADER_SIZE - 1);
    case CTL_CL_FILEASK:
    case CTL_CL_FILEOK:
    case CTL_SV_PWDFAIL:
    case CTL_SV_PWDOK:
    case CTL_SV_PWDASK:
    case CTL_CL_CLIST:
    case CTL_SV_CLRM:
      stream.data.type.control.idfrom = data[DATA_IDFROM_INDEX];
      break;

    default:
      stream.header.cbyte = CTL_FAIL;
      break;
    }
  } else if (nbytes == 0) {
    stream.header.cbyte = CTL_CL_DISC;
  } else
    stream.header.cbyte = CTL_FAIL;

  free(data);
  return stream;
}


/**
 *
 */
header_t tellapic_read_header_b(int fd) {
  header_t header;
  byte_t *data = malloc(HEADER_SIZE);
  int nbytes = read_b(fd, HEADER_SIZE, data);
  
  if (nbytes == HEADER_SIZE) {
    header.endian = data[ENDIAN_INDEX];
    header.cbyte  = data[CBYTE_INDEX];
    //header.ssize  = data[SBYTE_INDEX]<<24 | data[SBYTE_INDEX + 1]<<16 | data[SBYTE_INDEX + 2]<<8 | data[SBYTE_INDEX + 3];
    header.ssize  = g_shift(data+SBYTE_INDEX, 4);
  } else if (nbytes == 0) {
    header.cbyte = CTL_CL_DISC;
  } else 
    header.cbyte = CTL_FAIL;

  // Free the data stream as we already filled up the stream structure for clients.
  free(data);

  return header;
}


/**
 *
 */
void tellapic_close_fd(int fd) {
  close(fd);
}


/**
 *
 */
int tellapic_send_data(int socket, stream_t *stream) {
  byte_t *rawstream = malloc(stream->header.ssize);

  // Copy the header to the raw stream data
  headercpy(rawstream, stream->header);
  int cbyte = stream->header.cbyte;
  switch(cbyte) {
  case  CTL_CL_PMSG:
    rawstream[DATA_PMSG_IDFROM_INDEX+HEADER_SIZE] = stream->data.type.chat.idfrom;
    rawstream[DATA_PMSG_IDTO_INDEX+HEADER_SIZE]   = stream->data.type.chat.type.private.idto;
    memcpy(rawstream+DATA_PMSG_TEXT_INDEX+HEADER_SIZE, stream->data.type.chat.type.private.text, stream->header.ssize - HEADER_SIZE - 2);
    break;

  case CTL_CL_BMSG:
    rawstream[DATA_BMSG_IDFROM_INDEX+HEADER_SIZE] = stream->data.type.chat.idfrom;
    memcpy(rawstream+DATA_BMSG_TEXT_INDEX+HEADER_SIZE, stream->data.type.chat.type.text, stream->header.ssize - HEADER_SIZE - 1);
    break;

  case CTL_CL_FIG:
  case CTL_CL_DRW:
    ddatacpy(rawstream+HEADER_SIZE, stream->data.type.drawing, stream->header.ssize);
    break;


  case CTL_SV_CLIST:
    // Implementing this will complicate things. Instead, send CTL_SV_CLADD for each client the list has when an user ask for a client list.
    // The client will add the user if it wasn't already added.
    break;

  case CTL_SV_FILE:
  case CTL_CL_PWD:
  case CTL_SV_CLADD:
    memcpy(rawstream+(DATA_CLADD_NAME_INDEX+HEADER_SIZE), stream->data.type.control.info, stream->header.ssize - HEADER_SIZE - 1);
  case CTL_CL_FILEASK:
  case CTL_CL_FILEOK:
  case CTL_SV_PWDFAIL:
  case CTL_SV_PWDOK:
  case CTL_SV_PWDASK:
  case CTL_CL_CLIST:
  case CTL_SV_CLRM:
    rawstream[DATA_IDFROM_INDEX+HEADER_SIZE] = stream->data.type.control.idfrom;
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
int tellapic_connect_to(const char *hostname, int port) {   
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

  if ( connect(sd, (struct sockaddr*)&addr, sizeof(addr)) != 0 ) {
      close(sd);
      return -1;
  }

  return sd;
}


/**
 *
 */
void tellapic_free(stream_t stream) {
  if (stream.header.cbyte == CTL_CL_FIG) {
    if ((stream.data.type.drawing.dcbyte & TOOL_MASK) == TOOL_TEXT) {
      free(stream.data.type.drawing.type.text.face);
      free(stream.data.type.drawing.type.text.info);
    } else {
      
    }
  } else if (stream.header.cbyte == CTL_CL_PMSG) {
    free(stream.data.type.chat.type.private.text);
  } else if (stream.header.cbyte == CTL_CL_BMSG) {
    free(stream.data.type.chat.type.text);
  } else if (stream.header.cbyte == CTL_SV_FILE && stream.header.cbyte == CTL_CL_PWD && stream.header.cbyte == CTL_SV_CLADD) {
    free(stream.data.type.control.info);
  }
}
