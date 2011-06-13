#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <time.h>
#include <stdlib.h>
#include <limits.h>
#include <unistd.h>
#include <math.h>
#include "tellapic/tellapic.h"

#define ARG_NUM 4
#define HOST 1
#define PORT 2
#define PWD  3
#ifndef WIDTH
#define WIDTH 5.0
#endif
#ifndef OPACITY
#define OPACITY 1.0
#endif
#ifndef ENDCAPS
#define ENDCAPS 1
#endif
#ifndef LINEJOINS
#define LINEJOINS 1
#endif
#ifndef MITERLIMIT
#define MITERLIMIT 10.0
#endif
#ifndef DASHPHASE
#define DASHPHASE 1.0
#endif

const char *TEST_PREFIX_NAME = "TEST";

/**
 *
 */
void
usage()
{
  printf("Usage: client_using_pen <localhost> <port> <password>\n");
}

#define AUTH_OK         1
#define AUTH_ERROR     -1
#define AUTH_PWDFAIL   -2
#define AUTH_NAMEFAIL  -3

/**
 *
 */
int
send_pwd(tellapic_socket_t socket, char *pwd, int *id)
{
  size_t   pwdlen  = strlen(pwd);
  stream_t stream;
  do {

    /* Send the session password */
    tellapic_send_ctle(socket, 0, CTL_CL_PWD, pwdlen, pwd);

    /* Read server response */
    stream = tellapic_read_stream_b(socket);

    if (stream.header.cbyte == CTL_FAIL)
      return AUTH_ERROR;

  } while(stream.header.cbyte == CTL_SV_PWDFAIL);

  *id = stream.data.control.idfrom;

  return stream.header.cbyte;
}


/**
 *
 */
int
send_name(tellapic_socket_t socket, char *name, int id)
{
  size_t   namelen = strlen(name);
  stream_t stream;
  do {


    /* Send the test name */
    tellapic_send_ctle(socket, id, CTL_CL_NAME, namelen, name);

    /* Read server response */
    stream = tellapic_read_stream_b(socket);

    if (stream.header.cbyte == CTL_FAIL)
      return AUTH_ERROR;

  } while(stream.header.cbyte == CTL_SV_NAMEINUSE);

  return stream.header.cbyte;
}


/**
 *
 */
int
do_auth(tellapic_socket_t socket, char *pwd)
{
  time_t   now     = time(NULL); 
  int      clid    = 0;
  int      result  = 0;
  char     name[255];
  stream_t stream;

  result = send_pwd(socket, pwd, &clid);

  if (result == CTL_SV_PWDFAIL)
    {
      printf("Password was wrong.\n");
      return AUTH_PWDFAIL;
    }

  if (result == AUTH_ERROR)
    {
      printf("Something wrong while receiving packets from server.\n");
      return result;
    }

  sprintf(name, "%s%ju", TEST_PREFIX_NAME, (uintmax_t)now);
  result = send_name(socket, name, clid);

  if (result == CTL_SV_NAMEINUSE)
    {
      printf("No name available.\n");
      return AUTH_NAMEFAIL;
    }

  if (result == AUTH_ERROR)
    {
      printf("Something wrong while receiving packets from server.\n");
      return result;
    }

  stream = tellapic_read_stream_b(socket);

  if (stream.header.cbyte != CTL_SV_AUTHOK)
    return AUTH_ERROR;

  return stream.data.control.idfrom;
}


/**
 *
 */
void
do_parabol(tellapic_socket_t socket, int id, int h, int k, int p, int points)
{
  int startx = 1;
  int starty = 1;
  float da[2];
  da[0] = 1.0;
  da[1] = 1.0;
  /* vertice */
  int c = 0;
  tellapic_send_drw_init(socket, 
			 TOOL_PATH | EVENT_PRESS, 
			 0, 
			 id, 
			 0,
			 WIDTH, 
			 OPACITY, 
			 200, 
			 25, 
			 255, 
			 startx, 
			 (pow(startx - h, 2) / (4*p)) + k, 
			 startx,  /* not used here */
			 starty,  /* not used here */
			 LINEJOINS, 
			 ENDCAPS, 
			 MITERLIMIT, 
			 DASHPHASE, 
			 da
			 );
  while(c < points) 
    {
      /* Ecuacion de la parabola: (x-h)^2 = 4p(y-k) siendo (h, k) el vertice */
      tellapic_send_drw_using(socket,
			      TOOL_PATH | EVENT_DRAG,
			      0, 
			      id, 
			      0,
			      WIDTH,
			      OPACITY,
			      200, 
			      25, 
			      255,
			      startx,
			      (pow(startx - h, 2) / (4*p)) + k
			      );
      startx++;
      c++;
      usleep(10000);
    } 
  tellapic_send_drw_using(socket,
			  TOOL_PATH | EVENT_RELEASE,
			  0, 
			  id, 
			  0,
			  WIDTH,
			  OPACITY,
			  200, 
			  25, 
			  255,
			  startx,
			  (pow(startx - h, 2) / (4*p)) + k
			  );
}

/**
 *
 */
void
do_sen(tellapic_socket_t socket, int id, int points)
{
  int startx = 1;
  int starty = 1;
  float da[2];
  da[0] = 1.0;
  da[1] = 1.0;
  /* vertice */
  int c = 0;
  tellapic_send_drw_init(socket, 
			 TOOL_PATH | EVENT_PRESS, 
			 0, 
			 id, 
			 0,
			 WIDTH, 
			 OPACITY, 
			 200, 
			 25, 
			 255, 
			 startx, 
			 sin(startx)*10+50,
			 startx,  /* not used here */
			 starty,  /* not used here */
			 LINEJOINS, 
			 ENDCAPS, 
			 MITERLIMIT, 
			 DASHPHASE, 
			 da
			 );
  while(c < points) 
    {
      /* Ecuacion de la parabola: (x-h)^2 = 4p(y-k) siendo (h, k) el vertice */
      tellapic_send_drw_using(socket,
			      TOOL_PATH | EVENT_DRAG,
			      0, 
			      id, 
			      0,
			      WIDTH,
			      OPACITY,
			      200, 
			      25, 
			      255,
			      startx,
			      sin(startx)*10+50
			      );
      startx += 4;
      c++;
      usleep(10000);
    }
  tellapic_send_drw_using(socket,
			  TOOL_PATH | EVENT_RELEASE,
			  0, 
			  id, 
			  0,
			  WIDTH,
			  OPACITY,
			  200, 
			  25, 
			  255,
			  startx,
			  sin(startx)
			  );
}


int
main(int argc, char **argv)
{
  stream_t file_stream;
  tellapic_socket_t socket;
  int id;
  
  if (argc != ARG_NUM)
    {
      usage();
      exit(1);
    }
  
  /* Connect to tellapic server and receive a socket for future messaging */
  socket = tellapic_connect_to(argv[HOST], argv[PORT]);
  id = do_auth(socket, argv[PWD]);
  
  if (id < 0)
    {
      printf("Test failed when authing...\n");
      exit(1);
    }
  
  /* We are authed, ask for file */
  tellapic_send_ctl(socket, id, CTL_CL_FILEASK);
  
  /* file_stream = tellapic_read_stream_b(socket); */
  /* if (file_stream.header.cbyte != CTL_SV_FILE) */
  /*   { */
  /*     printf("Not expected packet. Was %d\n", file_stream.header.cbyte); */
  /*     exit(1); */
  /*   } */

  //  tellapic_free(&file_stream);
  printf("Drawing parabol\n");
  do_parabol(socket, id, 565, 329, 150, 1000);
  /*printf("Drawing sin\n");
  do_sen(socket, id, 1000);
  */
  tellapic_close_socket(socket);
  return 0;
}
