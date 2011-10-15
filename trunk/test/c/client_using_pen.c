#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <time.h>
#include <stdlib.h>
#include <limits.h>
#include <unistd.h>
#include <math.h>
#include <signal.h>
#include "tellapic/tellapic.h"

#define ARG_NUM 4
#define HOST 1
#define PORT 2
#define PWD  3
#ifndef WIDTH
#define WIDTH 15.0
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

int id = 0;
tellapic_socket_t s;
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
send_pwd(char *pwd, int *id)
{
  size_t   pwdlen  = strlen(pwd);
  stream_t stream;
  do {

    /* Send the session password */
    tellapic_send_ctle(s, 0, CTL_CL_PWD, pwdlen, pwd);

    /* Read server response */
    stream = tellapic_read_stream_b(s);

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
send_name(char *name, int id)
{
  size_t   namelen = strlen(name);
  stream_t stream;
  do {


    /* Send the test name */
    tellapic_send_ctle(s, id, CTL_CL_NAME, namelen, name);

    /* Read server response */
    stream = tellapic_read_stream_b(s);

    if (stream.header.cbyte == CTL_FAIL)
      return AUTH_ERROR;

  } while(stream.header.cbyte == CTL_SV_NAMEINUSE);

  return stream.header.cbyte;
}


/**
 *
 */
int
do_auth(char *pwd)
{
  time_t   now     = time(NULL); 
  int      clid    = 0;
  int      result  = 0;
  char     name[255];
  stream_t stream;

  result = send_pwd(pwd, &clid);

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
  result = send_name( name, clid);

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

  stream = tellapic_read_stream_b(s);

  if (stream.header.cbyte != CTL_SV_AUTHOK)
    return AUTH_ERROR;

  return stream.data.control.idfrom;
}


/**
 *
 */
void
do_parabol(int id, int h, int k, int p, int points)
{
  if (h<0 || k<0 || p<0 || points<0)
    return;
  printf("Drawing parabol at vertex (%d,%d) in focus %d with %d points\n", h,k,p,points);
  int startx = 1;
  int starty = 1;
  float da[2];
  da[0] = 1.0;
  da[1] = 1.0;
  /* vertice */
  int c = 0;
  tellapic_send_drw_init(s, 
			 TOOL_PATH | EVENT_PRESS, 
			 0, 
			 id, 
			 0,
			 WIDTH, 
			 OPACITY, 
			 0,
			 0,
			 0,
			 0,
			 startx, 
			 (pow(startx - h, 2) / (4*p)) + k, 
			 200, 
			 25, 
			 255,
			 255,
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
      printf("\rPoint: %d", c);
      fflush(stdout);
      /* Ecuacion de la parabola: (x-h)^2 = 4p(y-k) siendo (h, k) el vertice */
      tellapic_send_drw_using(s,
			      TOOL_PATH | EVENT_DRAG,
			      0, 
			      id, 
			      0,
			      WIDTH,
			      OPACITY,
			      0,
			      0,
			      0,
			      0,
			      startx,
			      (pow(startx - h, 2) / (4*p)) + k
			      );
      startx++;
      c++;
      usleep(10000);
    } 
  tellapic_send_drw_using(s,
			  TOOL_PATH | EVENT_RELEASE,
			  0, 
			  id, 
			  0,
			  WIDTH,
			  OPACITY,
			  0,
			  0,
			  0,
			  0,
			  startx,
			  (pow(startx - h, 2) / (4*p)) + k
			  );
  printf("\nDrawing finished\n------------\n");
}

/**
 *
 */
void
do_sen(int id, int points)
{
  int startx = 1;
  int starty = 1;
  float da[2];
  da[0] = 1.0;
  da[1] = 1.0;
  /* vertice */
  int c = 0;
  tellapic_send_drw_init(s, 
			 TOOL_PATH | EVENT_PRESS, 
			 0, 
			 id, 
			 0,
			 WIDTH, 
			 OPACITY, 
			 0,
			 0,
			 0,
			 0,
			 startx, 
			 sin(startx)*10+50,
			 200, 
			 25, 
			 255,
			 255,
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
      tellapic_send_drw_using(s,
			      TOOL_PATH | EVENT_DRAG,
			      0, 
			      id, 
			      0,
			      WIDTH,
			      OPACITY,
			      0,0,0,0,
			      startx,
			      sin(startx)*10+50
			      );
      startx += 4;
      c++;
      usleep(10000);
    }
  tellapic_send_drw_using(s,
			  TOOL_PATH | EVENT_RELEASE,
			  0, 
			  id, 
			  0,
			  WIDTH,
			  OPACITY,
			  0,0,0,0,
			  startx,
			  sin(startx)
			  );
}



void
signal_handler(int sig) 
{
  switch(sig)
    {
    case SIGINT:
      tellapic_send_ctl(s, id, CTL_CL_DISC);
      tellapic_read_stream_b(s);
      tellapic_close_socket(s);
      printf("\nExiting\n");
      exit(1);
      break;
    }
}

int
main(int argc, char **argv)
{
  stream_t file_stream;
  struct sigaction   sig_action;                                          /* signal handler for external signal management */

  sig_action.sa_handler = signal_handler;
  sigemptyset(&sig_action.sa_mask);
  sig_action.sa_flags = SA_RESTART;
  if ( sigaction(SIGINT, &sig_action, NULL) == -1)
    printf("Could not install signal handler!\n");
  
  if (argc != ARG_NUM)
    {
      usage();
      exit(1);
    }
  
  /* Connect to tellapic server and receive a socket for future messaging */
  s = tellapic_connect_to(argv[HOST], argv[PORT]);
  id = do_auth(argv[PWD]);
  
  if (id < 0)
    {
      printf("Test failed when authing...\n");
      exit(1);
    }

  printf("Connected to tellapic server!\n");
  
  /* We are authed, ask for file */
  tellapic_send_ctl(s, id, CTL_CL_FILEASK);
  
   file_stream = tellapic_read_stream_b(s);
   if (file_stream.header.cbyte != CTL_SV_FILE) 
     {
       printf("Not expected packet. Was %d\n", file_stream.header.cbyte);
       exit(1);
     } 

   tellapic_free(&file_stream);
  char input[10];
  int h = 0;
  int k = 0;
  int p = 0;
  int points = 0;

  while(points != -1 && h!=-1 && k!=-1 && p!=-1) {
    printf("\nSet parabol vertex (h,k), focus (p) and number of points (points) to draw (-1 to exit): h,k,p,points: ");
    fflush(stdout);
    scanf("%d,%d,%d,%d", &h, &k, &p, &points);
    getc(stdin);
    do_parabol(id, h,k,p,points);

  }
  /*printf("Drawing sin\n");
  do_sen(socket, id, 1000);
  */
  tellapic_send_ctl(s, id, CTL_CL_DISC);
  tellapic_read_stream_b(s);
  tellapic_close_socket(s);
  return 0;
}
