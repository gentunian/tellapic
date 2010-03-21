/*
 *   Copyright (c) 2009 Sebastián Treu and Virginia Boscaro.
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
 *   Authors: 
 *         Sebastian Treu 
 *         Virginia Boscaro
 *
 */  
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <malloc.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <resolv.h>
#include <openssl/err.h>
#include <openssl/conf.h>
#include <openssl/engine.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <pthread.h>
#include <signal.h>

#include "server.h"
#include "stream.h"

#define MMAX(a, b) a > b? a:b;

MUTEX_TYPE         *mutex_buf; //this is initialized by THREAD__setup();
MUTEX_TYPE         clcmutex     = PTHREAD_MUTEX_INITIALIZER;
MUTEX_TYPE         streammutex  = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t     streamcond   = PTHREAD_COND_INITIALIZER;
list_t             *streamqueue;
int                clccount = 0; //shared
SSL_CTX            *ctx;
int                CONTINUE = 1; //this NEEDS to be global as the handler interrupt manages program execution
args_t             args;


/*************** JUST FOR TESTING *************/
MUTEX_TYPE         cmmutex = PTHREAD_MUTEX_INITIALIZER;
MUTEX_TYPE         camutex = PTHREAD_MUTEX_INITIALIZER;
unsigned long      cm = 0;
unsigned long      ca = 0;
/**********************************************/

void               *attend_client(void *arg);
void               *send_to_clients(void *arg);
void               r_free();
void               thread_abort();
void               free_client(int client_num);
void               client_list_init();
void               cleanup_read_thread(void *arg);
void               cleanup_write_thread(void *arg);
void               args_check(int argc, char *argv[]);
void               data_init();
void               data_init_thread(int i);
void               data_init_client(int i);
void               data_init_pipes(int i);
void               strcptobuf(const byte_t *src, int size, int from);
int                THREAD_get_error(int value, int severity, int tid);
int                THREAD_setup();
int                new_thread_data(int i, int clfd, struct sockaddr_in clientaddr);
int                THREAD_cleanup(void);
int                ssl_read_b(client_t *cl, int *err, byte_t *buf, size_t size, struct timeval *tv);
int                ssl_write_b(client_t *cl, int *err, const byte_t *buf, size_t size);
int                send_stream(client_t *cl, byte_t *stream, int isbytes, int nw);

byte_t             *ssl_read_stream(client_t *cl, int *streamsize, int *err);
int                check_pwd(void *arg);
int                check_last(void *arg);
int                check_dummy(void *arg);
int                add_stream_to_queue(byte_t *stream, size_t bytes, int from);


tdata_t            thread_data[MAX_CLIENTS + 1]; //this is the main shared data
client_t           clients[MAX_CLIENTS];         //this too.

int main(int argc, char *argv[]) 
{
  int                clfd = 0;
  int                i    = 0;
  int                rv   = 0;

  struct sockaddr_in clientaddr;
  struct sockaddr_in serveraddr;
  struct sigaction   sig_action;
  pthread_attr_t     joinattr;

  sig_action.sa_handler = signal_handler;
  sigemptyset(&sig_action.sa_mask);
  sig_action.sa_flags = SA_RESTART;
  if ( sigaction(SIGINT, &sig_action, NULL) == -1)
    printf("[ERR]: Could not install signal handler!\n");
  if ( sigaction(SIGPIPE, &sig_action, NULL) == -1)
    printf("[ERR]: Could not install signal handler!\n");

  /* initialize image list */
  args.imglist = list_make_empty(args.imglist);

  /* check program arguments and fill args_t structure in args */
  args_check(argc, argv);
  
  /* initialize SSL */
  ctx = init_server_ctx();   
  SSL_CTX_use_certificate_file(ctx, "cacert.pem", SSL_FILETYPE_PEM); 
  SSL_CTX_use_PrivateKey_file(ctx, "privkey.pem", SSL_FILETYPE_PEM);
  
  /* Prepare the server for listen */
  args.svfd = open_listener(atoi(argv[1]), &serveraddr);

  /**/
  if ( THREAD_setup() == 0 )
    printf("THREAD_setup error\n");

  /* Initialize main data structures and global variables */
  data_init();

  /* initialize thread joinable attribute */
  pthread_attr_init(&joinattr);
  pthread_attr_setdetachstate(&joinattr, PTHREAD_CREATE_JOINABLE); 

  /* launch sender thread */
  rv = pthread_create(&thread_data[SV_THREAD].tid, &joinattr, send_to_clients, (void *) SV_THREAD);

  /* set server thread state as active */
  thread_data[SV_THREAD].state = THREAD_STATE_ACTIVE;

  THREAD_get_error(rv, FATAL, thread_data[SV_THREAD].tnum);

  /* server started normally */
  while( CONTINUE  ) 
    {
      int notfound = 1;
      int addrlen  = sizeof(clientaddr);
      printf("\n-----------------------------------------------------------------\n");
      printf("[NFO]:\tWaiting for incoming connections\n");

      /* When a SIGINT is caught, this socket is set to non-blocking. Then, the program   */
      /* main loop reads the new CONTINUE value (0) and ends up with the shutdown process */
      if ( (clfd = accept(args.svfd, (struct sockaddr *)&clientaddr, &addrlen)) == -1)
	{
	  printf("[NFO]:\tConnection refused.\n");
	}
      else
	{
	  ca++;  //JUST FOR TESTING PURPOSES
	  printf("[NFO]:\tConnection attempt from %s.\n", inet_ntoa(clientaddr.sin_addr));
	  /* is there any place to run a thread? */
	  for( i = 0; i < MAX_CLIENTS && notfound; i++)
	    {
	      printf("searching place on %d and locking\n",i);
	      pthread_mutex_lock(&thread_data[i].mutex);
	      if ( thread_data[i].state == THREAD_STATE_NEW )
		{
		  printf("thread %d was never used\n",i);
		  thread_data[i].state = THREAD_STATE_INIT;
		  notfound = 0;
		}
	      if ( thread_data[i].state == THREAD_STATE_FREE )
		{
		  printf("thread %d was used, cancelling and waiting\n", i);
		  /*26/12/2009: REVIEW THIS PART*/
		  //pthread_cancel(thread_data[i].tid);
		  pthread_join(thread_data[i].tid, NULL);
		  thread_data[i].state = THREAD_STATE_INIT;
		  notfound = 0;
		}
	      pthread_mutex_unlock(&thread_data[i].mutex);
	    }
	  if ( notfound == 0 )
	    {
	      i--;
	      if ( new_thread_data(i, clfd, clientaddr) )
		{
		  rv = pthread_create(&thread_data[i].tid, &joinattr, attend_client, (void *) &thread_data[i].tnum);
		  if ( THREAD_get_error(rv, WARN, thread_data[i].tnum) )
		    {
		      printf("[WRN]:\tSpawning thread ocess failed! Client discarted.\n");
		      free_client(i);
		      pthread_mutex_lock(&thread_data[i].mutex);
		      thread_data[i].client = NULL; //is really necessary this assigment inside de lock??
		      thread_data[i].state  = THREAD_STATE_FREE;
		      pthread_mutex_unlock(&thread_data[i].mutex);
		    }
		}
	      else
                {
                  pthread_mutex_lock(&thread_data[i].mutex);
		  thread_data[i].client = NULL; //is really necessary this assigment inside de lock??
                  thread_data[i].state = THREAD_STATE_FREE;
                  pthread_mutex_unlock(&thread_data[i].mutex);
                  printf("[WRN]:\tClient node allocation falied!\n");
                  close(clfd);
                }
	    }
	  else
	    {
	      printf("[NFO]:\tServer reached full capacity.\n");
	      close(clfd);
	    }
	}
    }
  /* Start freeing resources */
  printf("<main thread>[NFO]:\tStart freeing resources\n");
  for( i = 0; i < MAX_CLIENTS + 1; i++)
    {
      if ( thread_data[i].client != NULL)
	printf("<main thread>[NFO]:\tposition %d with state %d and fd %d. TID: %ld\n", i, thread_data[i].state, thread_data[i].client->fd, thread_data[i].tid);
      if ( thread_data[i].state != THREAD_STATE_NEW)/* Send cancellation signal to ALL threads without checking errors */
	{
	  printf("<main thread>[NFO]:\tSending cancel signal to thread with pid %ld and number %d\n", thread_data[i].tid, thread_data[i].tnum);
	  pthread_cancel(thread_data[i].tid);
	  pthread_join(thread_data[i].tid, NULL);
	  pthread_mutex_destroy(&thread_data[i].mutex);
	}
    }
  printf("<main thread>[NFO]:\tWaiting for threads done!\n");
  /* Check if theres more data to free */
  r_free();
  printf("<main thread>[NFO]:\tAllocated API structures released!\n");
  pthread_attr_destroy(&joinattr);

  printf("\n--- END ---\n");
  /********** TESTING PURPOSES ************************/
  printf("\nserver exits correctly with a total of:\n");
  printf("\n\tConnection attemps: %ld\n",ca);
  printf("\tConnections made: %ld\n", cm);
  pthread_exit(NULL);
}


int should_send(unsigned short i, stream_item_t *item)
{
  /*TODO: Think about private chat*/
  byte_t            *drawingbyte = NULL;
  if ( i != item->from )
    {
      if (STREAM_is_drawing(item->data))
	{
	  drawingbyte = STREAM_get_drawingbyte(item->data);
	  if ( thread_data[i].fwdbitlist & (1 << item->from))
	    {
	      /* We can send whatever to the i-th client, but if  */
	      /* a release event if found, restore the fwd bit.   */
	      if ( STREAM_is_release_event(drawingbyte))
		thread_data[i].fwdbitlist &= ~(1 << item->from);
	      return 1;
	    }
	  else
	    /* We need to check more things as the fwd bit is not set. */
	    if (STREAM_is_press_event(drawingbyte))
	      return thread_data[i].fwdbitlist |= (1 << item->from);
	    else
	      return 0;
	}
      else
	return 1;
    }
  else
    return 0;
}


void signal_client(void *arg)
{
  stream_item_t *item = NULL;
  unsigned short i    = 0;
  int            rv   = 0;
  pthread_cleanup_push(cleanup_write_thread, NULL);
  printf("<thread %d>[NFO]:\tSending thread started.\n", SV_THREAD);
  while(1)
    {
      pthread_mutex_lock(&streammutex);
      if ( queue_is_empty(streamqueue))
	pthread_cond_wait(&streamcond, &streammutex);
      item = (stream_item_t *) queue_get_noremove_last(streamqueue); //get the last item queued
      pthread_mutex_unlock(&streammutex);
      for (i = 0; i < MAX_CLIENTS; i++)
	{
	  if (should_send(i, item))
	    {
	      pthread_mutex_lock(&clients[i]->stmutex);
	      if ( clients[i]->state == CLIENT_STATE_READY )
		{
		  rv = write(thread_data[i], item->number, sizeof(item->number)); //signals thread to write the 'num' element from the queue
		  if ( rv == -1)
		    {
		      if ( errno == EWOULDBLOCK || errno == EAGAIN)
			{
			}
		      else
			perror("write() to pipe failed");
		    }
		}
	      pthread_mutex_unlock(&clients[i]->stmutex);
	    }
	}
    }
  printf("<thread %d>[NFO]:\tSending thread closing.\n", SV_THREAD);
  pthread_cleanup_pop(1);
  pthread_exit(NULL);
}


unsigned short getconn_clnum()
{
  pthread_mutex_lock(&clcmutex);
  unsigned short value = clccount;
  pthread_mutex_unlock(&clcmutex);
  return value;
}


/*******************************************************************
 * attend_client(void *arg):
 * ------------
 * Thread for receive new connections
 *******************************************************************/
void *attend_client(void *arg) 
{  
  int       *tnum      = (int *) arg;
  int       fdmax      = MMAX(thread_data[*tnum].pipefd[0], thread_data[*tnum].clients->fd);
  int       isbytes    = 0;
  int       osbytes    = 0;
  int       err        = 0;
  int       rv         = 0;
  byte_t    *ostream   = NULL;
  byte_t    *istream   = NULL;
  byte_t    header[STREAM_HEADER_SIZE];
  client_t  *tmpclient = clients;
  /* register cleanup function for this thread */
  pthread_cleanup_push(cleanup_read_thread, (void *) tnum);
  /* wait for a handshake. */
  if( wait_for_client(thread_data[*tnum].client) <= 0 )  
    {
      printf("<thread %d>[ERR]:\tcould not handshake with client.\n", *tnum);
      pthread_exit(NULL); //this will call cleanup function.p
    }
  printf(NFO_ACCEPT, *tnum, inet_ntoa(thread_data[*tnum].client->address.sin_addr), thread_data[*tnum].client->fd);
  printf(NFO_CIPHER, *tnum, SSL_get_version(thread_data[*tnum].client->ssl), SSL_get_cipher(thread_data[*tnum].client->ssl));
  clccountinc();
  ostream = STREAM_build(CTL_SV_IDLIST, tmpclient, iterate_id, &osbytes);
  /* if there is connected clients send it */
  if ( ostream != NULL )
    {  
      printf("<thread %d>[NFO]:\tSending id list to connected clients\n", *tnum);
      send_stream(thread_data[*tnum].client, ostream, osbytes, 0);
      free(ostream);
      osbytes = 0;
    }
  pthread_mutex_lock(&thread_data[*tnum].client->mutex);
  thread_data[*tnum].client->state = CLIENT_STATE_READY;
  pthread_mutex_unlock(&thread_data[*tnum].client->mutex);
  pthread_mutex_lock(&thread_data[*tnum].mutex);
  thread_data[*tnum].state = THREAD_STATE_ACTIVE;
  printf("<thread %d>[NFO]:\tMain attendance loop start\n", *tnum);
  while( thread_data[*tnum].state == THREAD_STATE_ACTIVE )
    {
      pthread_mutex_unlock(&thread_data[*tnum].mutex);


      /********************************************************/
      /* We enter C state at the beginning or if we need to   */
      /********************************************************/
      if ( chktstate(THREAD_STATE_INIT, thread_data[*tnum]) || chktstate(THREAD_STATE_RH, thread_data[*tnum]))
	header = try_read_header(thread_data[*tnum]);


      /********************************************************/
      /* We go to D state after checks on control byte        */
      /********************************************************/
      if ( chktstate(THREAD_STATE_RHOK, thread_data[*tnum]))
	{
      	  slen = STREAM_get_streamlen(header);
      	  if ( slen <= 0 )
      	    settstate(THREAD_STATE_END, thread_data[*tnum]);
      	  else
      	    data = try_read_data(thread_data[*tnum], slen - STREAM_HEADER_SIZE);
	}


      /********************************************************/
      /* If data read went ok, we go to E state and process   */
      /* the request to forward the data to other clients     */
      /********************************************************/
      if ( chktstate(THREAD_STATE_RDOK, thread_data[*tnum]))
	{
      	  ostream = (byte_t *) malloc(slen * sizeof(byte_t));
      	  memcpy(header, ostream, STREAM_HEADER_SIZE);
      	  memcpy(data, ostream + STREAM_HEADER_SIZE, slen - STREAM_HEADER_SIZE);
      	  /* blabla */
      	  setiftstate(THREAD_STATE_WR, thread_data[*tnum], thread_data[*tnum].shouldwrite);
	}


      /********************************************************/
      /* The B state is a locking state if we can't go furher */
      /* to any points. B controls where we need to send to or*/
      /* read from a client.                                  */
      /********************************************************/
      if ( chktstate(THREAD_STATE_WANTR,    thread_data[*ntum]) ||
	   chktstate(THREAD_STATE_WANTWR,   thread_data[*ntum]) ||
	   chktstate(THREAD_STATE_WAIT, thread_data[*ntum])  )
	{
      	  rv = select(fdmax + 1, &copy, NULL, NULL, NULL);
      	  settstate(THREAD_STATE_WR, thread_data[*tnum]);
      	  setiftstate(THREAD_STATE_END, (rv < 0), thread_data[*tnum]);
	  setiftstate(THREAD_STATE_RH, FD_ISSET(thread_data[*tnum].client->fd, &copy), thread_data[*tnum]);
      	  if ( FD_ISSET(thread_data[*tnum].pipefd[0], &copy) )
      	    thread_data[*tnum].shouldwrite = 1; /*this works as a memory thing for when read finish (see E state).*/
      	  copy = readfs;
	}


      /********************************************************/
      /* This is when we need to send to this client some     */
      /* data. This F state should be reached if when thread  */
      /* data 'shouldwrite' flag is true and the client isn't */
      /* sending us something.                                */
      /********************************************************/
      if ( chktstate(THREAD_STATE_WR, thread_data[*tnum]))
	try_write(thread_data[*tnum]);
      

      pthread_mutex_lock(&thread_data[*tnum].mutex);
    }
  pthread_mutex_unlock(&thread_data[*tnum].mutex);	   
  printf("<thread %d>[NFO]:\tAttendace thread closing on %d socket.\n", *tnum, thread_data[*tnum].client->fd);
  pthread_cleanup_pop(1);
  /* this thread has no more reason to live */
  pthread_exit(NULL);
}


int chktstate(thread_state_t state, tdata_t thread)
{
  pthread_mutex_lock(&thread.mutex);
  int value = (thread.state == state);
  pthread_mutex_unlock(&thread.mutex);
  return value;
}


void settstate(thread_state_t state, tdata_t thread)
{
  pthread_mutex_lock(&thread.mutex);
  thread.state = state;
  pthread_mutex_unlock(&thread.mutex);
}


void setiftstate(thread_state_t state, int cond, tdata_t thread)
{
  if ( cond )
    settstate(state, thread);
}


int send_stream(client_t *cl, byte_t *istream, int isbytes)
{
  int rv = 0;
  //rv = add_stream_to_queue(istream, isbytes, cl->clidx);
  if ( enqueue_stream(istream, isbytes, cl->clidx) )
    {
      rv = write(thread_data[SV_THREAD].pipefd[1], (char) cl->clidx, 1);
      switch(errno)
	{
	case EAGAIN:
	case EWOULDBLOCK:
	  printf("<thread %d>", cl->clidx);
	  perror();
	  /* pthread_mutex_lock(&thread_data[SV_THREAD].pendmutex); */
	  /* thread_data[SV_THREAD].pending++; */
	  /* pthread_mutex_unlock(&thread_data[SV_THREAD].pendmutex); */
	  break;
	case ECONNRESET:
	case EPIPE:
	case ENOBUFS:
	case ENXIO:
	  perror();
	  break;
	default:
	  perror();
	  break;
	}
      printf("<thread %d> write return value: %d\n", cl->clidx, rv);
    }
}


int enqueue_stream(byte_t *stream, size_t bytes, int from)
{  
  //if ( STREAM_is_stream(stream, bytes) ) /* NOT TO BE DONE HERE. DO IT IN ATTEND_CLIENT INSTEAD */
    {
      stream_item_t       *item = (stream_item_t *) malloc(sizeof(stream_item_t));
      if ( item == NULL )
	{
	  perror("item==null");
	  return 0;
	}
      memcpy(item->data, stream, bytes);
      item->nbytes = bytes;
      item->from   = from;
      //list_add_last_item(streamqueue, (void *)item);
      pthread_mutex_lock(&stream_mutex);
      queue_add(streamqueue, (void *)item);
      pthread_mutex_unlock(&stream_mutex);
      return 1; //STREAM_OK
    }
    //  else
    //    return 0; //STREAM_NOT_STREAM
}


/**************************************
 * wait_for_client(client_t *client):
 * ---------------
 **************************************/
int wait_for_client(client_t *client)
{
  int            i        = 0;
  int            seq      = 0;                                               /* next sequence on pseq        */
  int            ec       = 0;                                               /* error code                   */
  int            isbytes  = 0;                                               /* number of bytes read         */
  int            osbytes  = 0;                                               /* number of bytes of ostream   */
  byte_t         *ostream = NULL;                                            /* stream to be sent            */
  byte_t         istream[BUFFER_SIZE];                                       /* buffer to hold read data     */
  struct timeval tv;
  psequence_t    *pseq = NULL;
  int (*checkers [])(void *arg) = {
    check_pwd,
    check_dummy,
    check_dummy,
    check_last
  };

  pseq = PSEQ_build(PSEQ_CONN, checkers);
  PSEQ_add_pbargs(pseq, PSEQ_CONN_0, (void *) &client->fd);
  PSEQ_add_pbargs(pseq, PSEQ_CONN_1, (void *) &client->fd);
  PSEQ_add_pbargs(pseq, PSEQ_CONN_2, (void *) &client->fd);
  PSEQ_add_pbargs(pseq, PSEQ_CONN_2, (void *) list_get_item(args.current));

  tv.tv_sec  = 5;
  tv.tv_usec = 0;
  ec         = ssl_accept_b(client, tv);
  /* if ( ec == SSL_ERROR_NONE )  */
  /*   { */
  /*     ostream = STREAM_build(CTL_SV_ASKPWD, client->fd, &osbytes); */
  /*     do */
  /* 	{ */
  /* 	  if ( ssl_write_b(client, &ec, ostream, osbytes) <= 0) */
  /* 	    return 0; */
  /* 	  if ( (isbytes = ssl_read_b(client, &ec, istream, BUFFER_SIZE, &tv)) <= 0 ) */
  /* 	    return 0; */
  /* 	  byte_t clpacket = STREAM_get_ctlbyte(istream, isbytes); */
  /* 	  byte_t svpacket = STREAM_get_ctlbyte(ostream, osbytes); */
  /* 	  if ( seq == 0) */
  /* 	    PSEQ_add_pcargs(pseq, PSEQ_CONN_0, (void *) STREAM_get_pwd(istream, isbytes, client->fd)); */
  /* 	  seq = PSEQ_next_sequence(seq, clpacket, svpacket, pseq, &ostream, &osbytes); */
  /* 	} */
  /*     while( seq >= 0 && seq < PSEQ_CONN_SIZE ); */
  /*     //TODO: CLEAN UP LEAKS FROM LIST AND SO ON...PSEQ[0].pcargs PSEQ[i].pbargs */
  /*     free(ostream); */
  /*     PSEQ_cleanup(PSEQ_CONN, pseq); */
  /*     free(pseq); */
  /*     return seq; */
  /*   } */
  /* else */
  /*   return 0; */
  return PSEQ_CONN_SIZE;
}


/*********************************************************************************
 *
 *********************************************************************************/
int ssl_read_b(client_t *cl, int *err, byte_t *buf, size_t size, struct timeval *tv)
{
  int nbytes = 0;
  int rc     = 0;
  fd_set master;
  fd_set copy;

  FD_ZERO(&master);
  FD_ZERO(&copy);
  FD_SET(cl->fd, &master);
  do
    {
      copy   = master;
      pthread_mutex_lock(&cl->mutex);
      nbytes = SSL_read(cl->ssl, buf, size);
      *err   = SSL_get_error(cl->ssl, nbytes);
      pthread_mutex_unlock(&cl->mutex);
    }
  while( ( *err == SSL_ERROR_WANT_READ || *err == SSL_ERROR_WANT_WRITE ) &&
         ( rc = select(cl->fd+1, &copy, NULL, NULL, tv) > 0 )
         );  
  return nbytes;
}


/*********************************************************************************
 *
 *********************************************************************************/
int ssl_accept_b(client_t *client, struct timeval tv)
{
  int    sslrc = 0;
  int    ec    = 0;
  int    rc    = 0;
  fd_set master;
  fd_set copy;
  
  FD_ZERO(&master);
  FD_ZERO(&copy);
  FD_SET(client->fd, &master);
  do
    {
      copy  = master;
      sslrc = SSL_accept(client->ssl);
      ec    = SSL_get_error(client->ssl, sslrc);
    }
  while( (ec == SSL_ERROR_WANT_READ  ||  ec == SSL_ERROR_WANT_WRITE) && 
	 (rc = select(client->fd+1, &copy, NULL, NULL, &tv)) > 0
	 );
  return ec;
}


/****************************************************
 ***************************************************/
byte_t *try_read_data(client_t *client, int size)
{
  int            action;
  int            rv = 0;
  fd_set         readfs;
  fd_set         copy;
  struct timeval tv;
  byte_t         data[size];
  tv.tv_sec = 4;
  FD_ZERO(&readfs);
  FD_SET(client->fd, &readfs);
  copy = readfs;
  data = try_read(client, size, &action); 
  while ( action == READ_WANT_READ || action == READ_WANT_WRITE )
    {
      rv = select(client->fd + 1, &copy, NULL, NULL, &tv);
      if ( rv < 0)
	{
	  perror("select() <= 0"); //select() call fail.
	  action = DISC_CL;
	}
      else if ( rv == 0)
	action = CANT_READ;
      else
	{
	  copy = readfs;
	  data = try_read(client, size, &action);
	}
    }
  setiftstate(THREAD_STATE_RDOK, (action == READ_OK), thread);
  setiftstate(THREAD_STATE_END,  (action == DISC_CL), thread);
  setiftstate(THREAD_STATE_WAIT, (action == CANT_READ), thread);
  return data;
}


/****************************************************
 ***************************************************/
byte_t *try_read_header(tdata_t thread)
{
  int    action;
  byte_t header[STREAM_HEADER_SIZE];
  header = try_read(thread.client, STREAM_HEADER_SIZE, &action);
  setiftstate(THREAD_STATE_RHOK,   (action == READ_OK), thread);
  setiftstate(THREAD_STATE_WANTR,  (action == READ_WANT_READ), thread);
  setiftstate(THREAD_STATE_WANTWR, (action == READ_WANT_WRITE), thread);
  setiftstate(THREAD_STATE_END,    (action == DISC_CL), thread);
  return header;
}


/****************************************************
 ***************************************************/
byte_t *try_read(client_t *client, int size, int *action)
{
  byte_t        buf[size];
  int           rv = 0;
  int           ec = 0;

  pthread_mutex_lock(&client->mutex);
  rv = SSL_read(client->ssl, buf, size);
  ec = SSL_get_error(client->ssl, rv);
  pthread_mutex_unlock(&client->mutex);

  switch(ec)
    {
    case SSL_ERROR_NONE:
      rv == size? *action = READ_OK : *action = DISC_CL;
      break;
    case SSL_ERROR_WANT_READ:
      *action = READ_WANT_READ;
      break;
    case SSL_ERROR_WANT_WRITE:
      *action = READ_WANT_WRITE;
      break;
    case SSL_ERROR_SYSCALL:
    case SSL_ERROR_ZERO_RETURN:
    case SSL_ERROR_SSL:
    default:
      *action = DISC_CL;
      break;
    } 
  return buf;
}


/*************************************************
 *************************************************/
int try_write(tdata_t thread)
{
  int            rv     = 0;
  int            ec     = 0;
  int            bwrote = 0;
  int            number = 0;
  stream_item_t  *node  = NULL;
  fd_set         writefs;
  fd_set         copy;
  struct timeval tv;
  tv.tv_sec = 3;
  FD_ZERO(&writefs);
  FD_SET(cl->fd, &writefs);
  rv = read(thread.pipefd[0], &number, sizeof(unsigned short));
  if ( rv < 0 ) /* more checks needed */
    return -1;
  if (thread.cl->last != NULL)
    {
      pthread_mutex_lock(&streammutex);
      node = queue_peek_next_node(streamqueue, last);
      /* check if null */
      pthread_mutex_unlock(&streammutex);
    }
  else
    {
      pthread_mutex_lock(&stream_mutex);
      node = queue_peek_first_node(streamqueue);
      while( node->number < number)
	node = queue_peek_next_node(streamqueue, node);
      pthread_mutex_unlock(&stream_mutex);
    }
  thread.cl->last = node;
  do
    {
      copy = writefs;
      rv   = SSL_write(thread.cl->ssl, node->data + bwrote, node->nbytes - bwrote);
      ec   = SSL_get_error(thread.cl->ssl, rv);
      switch(ec)
	{
	case SSL_ERROR_NONE:
	  bwrote += rv;
	  break;
	case SSL_ERROR_WANT_READ:
	case SSL_ERROR_WANT_WRITE:
	  if ( select(thread.cl->fd + 1, NULL, &copy, NULL, &tv) <= 0 )
	    bwrote = node->nbytes;
	  break;
	default:
	  bwrote = node->nbytes;
	  break;
	}
    }
  while( bwrote < node->nbytes);

  return ec;
}


/*********************************************************************************
 *********************************************************************************/
int ssl_write_b(client_t *cl, int *err, const byte_t *buf, size_t size)
{
  int        rv       = 0;
  int        bwrote   = 0;
  fd_set     writefs;
  fd_set     copy;
  FD_ZERO(&writefs);
  FD_SET(cl->fd, &writefs);
  *err = SSL_ERROR_NONE;
  do
    {
      copy = writefs;
      rv = SSL_write(cl->ssl, buf + bwrote , size - bwrote);
      if ( rv <= 0 )
	{
	  *err = SSL_get_error(cl->ssl, rv);
	  printf("err\n");
	  if ( *err == SSL_ERROR_WANT_READ || *err == SSL_ERROR_WANT_WRITE )
	    select(cl->fd+1, NULL, &writefs, NULL, NULL);
	  else
	    return 0;
	}
      else	
	bwrote += rv;
      //      printf("HERE: rv : %d  - err : %d  -  size : %d  -  bwrote: %d\n", rv, *err, size,bwrote);
    }
  while( bwrote < size );

  return bwrote;
}


/*********************************************************************************
 *
 *********************************************************************************/
void *iterate_id(void **arg)
{
  client_t **client = (client_t *)arg;
  int       *id     = NULL;
  pthread_mutex_lock(&(*client)->mutex);
  while ( (*client)->state == CLIENT_STATE_INIT && 
	  (*client)->clidx < MAX_CLIENTS - 1  )
    {
      pthread_mutex_unlock(&(*client)->mutex);
      (*client)++;
      pthread_mutex_lock(&(*client)->mutex);
    }
  pthread_mutex_unlock(&(*client)->mutex);

  if ( (*client)->clidx == MAX_CLIENTS - 1 )
    {
      pthread_mutex_lock(&(*client)->mutex);
      if ( (*client)->state == CLIENT_STATE_READY )
	id = &(*client)->fd;
      pthread_mutex_unlock(&(*client)->mutex);
    }
  else
    id = &(++(*client))->fd;

  return (void *) id;
}


/****************************************************
 *
/****************************************************/
void data_init()
{
  int i = 0;
  /* Initialize thread/clients list */
  for( i = 0; i < MAX_CLIENTS + 1; i++)
    {
      data_init_thread(i);
      data_init_client(i);
      data_init_pipes(i);
    }
  streamqueue = queue_make_empty(streamqueue);
}


/****************************************************
 *
/****************************************************/
void data_init_thread(int i)
{
  thread_data[i].client = NULL;
  thread_data[i].tid    = (pthread_t) 0;
  thread_data[i].tnum   = i;
  thread_data[i].state  = THREAD_STATE_NEW;
  THREAD_get_error(pthread_mutex_init(&thread_data[i].pendmutex, NULL), FATAL, i);
  if ( i == SV_THREAD )
    thread_data[i].pendqueue = queue_make_empty(thread_data[i].pendqueue);
}


/****************************************************
 *
/****************************************************/
void data_init_client(int i)
{
  if ( i != SV_THREAD)
    {
      clients[i].ssl    = NULL;
      clients[i].clinfo = NULL;
      clients[i].last   = NULL;
      clients[i].clidx  = i;
      clients[i].state  = CLIENT_STATE_INIT;
      THREAD_get_error(pthread_mutex_init(&clients[i].mutex, NULL), FATAL, i);
    }
}


/****************************************************
 *
/****************************************************/
void data_init_pipes(int i)
{
  int j = 0;
  pipe(thread_data[i].pipefd);
  for( j = 0; j < 2; j++)
    if ( setnonblock(thread_data[i].pipefd[j]) == -1)
      {
	perror("setnonblock()");
	exit(1);
      }
}


/****************************************************

/****************************************************/
int THREAD_get_error(int value, int severity, int tid)
{
  int rc = 0;
  switch(value)
    {
    case 0:

      break;
    case EBUSY:
      printf("<thread %d>[ERR]:\tEBUSY\n", tid);
      if ( severity == FATAL)
	exit(THREAD_ERR_EBUSY);
      break;
    case EINVAL:
      printf("<thread %d>[ERR]:\tEINVAL\n", tid);
      if ( severity == FATAL )
	exit(THREAD_ERR_EINVAL);
      break;
    case ENOMEM:
      printf("<thread %d>[ERR]:\tENOMEM\n", tid);
      if ( severity == FATAL)
	exit(THREAD_ERR_ENOMEM);
      break;
    case EPERM:
      printf("<thread %d>[ERR]:\tEPERM\n", tid);
      if (severity == FATAL)
	exit(THREAD_ERR_EPERM);
      break;
    case EAGAIN:
      printf("<thread %d>[ERR]:\tEAGAIN\n", tid);
      if (severity == FATAL)
	exit(THREAD_ERR_EAGAIN);
      break;
    }
  return rc;
}



/****************************************************
 * open_listener(int port, struct sockaddr_int *addr):
 * -------------
 * Creates, sets and binds a socket to be ready for using.
 * No error checking is needed on calling this function as it
 * exits the main program. No socket created, no server running.
 ****************************************************/
int open_listener(int port, struct sockaddr_in *addr)
{   
  int sd = 0;
  int val = 1;
  if ( (sd = socket( AF_INET, SOCK_STREAM, 0 )) == -1 ) {
    perror("Server Initialization error: cannot create socket().\n");
    exit(1);
  }
  
  if ( setsockopt( sd, SOL_SOCKET, SO_REUSEADDR, &val, sizeof( int ) ) == -1 ) {
    perror("Server Socket Options: error.\n");
    close( sd );
    exit(1);
  }
  /* set addr memory to zero */
  memset(addr, 0, sizeof(*addr));
  
  /* set addr struct */
  addr->sin_family = AF_INET;
  addr->sin_port   = htons( port) ;
  addr->sin_addr.s_addr = htonl( INADDR_ANY );

  /* bind socket file descriptor to sctructure */
  if ( bind( sd, (struct sockaddr *)addr, sizeof(*addr) ) != 0 ) {
    perror("Server Initialization error: can't bind() port.\n");
    close( sd );
    abort();
  }
  if ( listen( sd, 10 ) != 0 ) {
    perror("Server Initialization error: cannot listen().\n");
    close( sd );
    abort();
  }
  printf("Server Initialization: OK...\n");
  return sd;
}



/*******************
 * init_server_ctx():
 * ----------------
 * Initializes an ssl context and return it. No error checking is
 * needed on calling this function.
 *******************/
SSL_CTX* init_server_ctx(void)
{   
  SSL_METHOD *method;
  SSL_CTX *ctx;
  int options;
  
  SSL_load_error_strings();		/* load all error messages */
  OpenSSL_add_all_algorithms();	/* load & register all cryptos, etc. */
  SSL_library_init();
  method = SSLv23_server_method();		/* for java clients compatibility */

  ERR_load_crypto_strings();
  ctx = SSL_CTX_new(method);		/* create new context from method */
  if ( ctx == NULL )  {
    ERR_print_errors_fp(stderr);
    abort();
  }
  SSL_CTX_set_mode(ctx, SSL_MODE_ENABLE_PARTIAL_WRITE);
  SSL_CTX_set_mode(ctx, SSL_MODE_ACCEPT_MOVING_WRITE_BUFFER);

  options = SSL_CTX_get_options( ctx );
  SSL_CTX_set_options( ctx,  options | SSL_OP_NO_SSLv2 | SSL_OP_NO_SSLv3);
  return ctx;
}



/*******************************************************************
 *
 *******************************************************************/
static void locking_function(int mode, int n, const char * file, int line)
{
  if (mode & CRYPTO_LOCK)
    MUTEX_LOCK(mutex_buf[n]);
  else
    MUTEX_UNLOCK(mutex_buf[n]);
}



/*******************************************************************
 *
 *******************************************************************/
static unsigned long id_function(void)
{
  return ((unsigned long)THREAD_ID);
}



/*******************************************************************
 *
 *******************************************************************/
int THREAD_setup(void)
{
  int i;
  mutex_buf = (MUTEX_TYPE *)malloc(CRYPTO_num_locks() * sizeof(MUTEX_TYPE));
  if (!mutex_buf)
    return 0;
  for (i = 0; i < CRYPTO_num_locks(); i++)
    {
      if ( MUTEX_SETUP(mutex_buf[i]) != 0 )
	return 0;
    }
  CRYPTO_set_id_callback(id_function);
  CRYPTO_set_locking_callback(locking_function);
  return 1;
}



/*******************************************************************
 *
 *******************************************************************/
int THREAD_cleanup(void)
{
  int i;
  if (!mutex_buf)
    return 0;
  CRYPTO_set_id_callback(NULL);
  CRYPTO_set_locking_callback(NULL);
  for (i = 0; i < CRYPTO_num_locks(); i++)
    MUTEX_CLEANUP(mutex_buf[i]);
  free(mutex_buf);
  mutex_buf = NULL;
  return 1;
}


/*******************************************************************
 *
 *******************************************************************/
void clccountinc()
{
  pthread_mutex_lock(&clcmutex);
  clccount++;
  cm++;
  pthread_mutex_unlock(&clcmutex);
}


/*******************************************************************
 *
 *******************************************************************/
void clccountdec()
{
  pthread_mutex_lock(&clcmutex);
  clccount--;
  pthread_mutex_unlock(&clcmutex);
}


/************************************
 *
 ************************************/
void cleanup_read_thread(void *arg)
{
  int *tnum = (int *) arg;
  free_client(*tnum);
  clccountdec();
  pthread_mutex_lock(&thread_data[*tnum].mutex);  
  printf("<thread %d>[NFO]:\tEntering read thread clean up function\n", *tnum );  
  ERR_remove_state(pthread_self());
  thread_data[*tnum].client = NULL;
  thread_data[*tnum].state  = THREAD_STATE_FREE;
  printf("<thread %d>[NFO]:\tLeaving read thread clean up function\n", *tnum);
  pthread_mutex_unlock(&thread_data[*tnum].mutex);  
}


/************************************
 *
 ************************************/
void cleanup_write_thread(void *arg)
{
  printf("<thread %d>[NFO]:\tEntering write thread clean up function\n", SV_THREAD);
  ERR_remove_state(pthread_self());
  pthread_mutex_lock(&thread_data[SV_THREAD].mutex);
  thread_data[SV_THREAD].state = THREAD_STATE_FREE;
  pthread_mutex_unlock(&thread_data[SV_THREAD].mutex);
}


/************************************
 * free_client():
 * -----------
 * free used resources by client
 ***********************************/
void free_client(int clnum)
{
  /* reset client state */
  pthread_mutex_lock(&clients[clnum].mutex);
  clients[clnum].state = CLIENT_STATE_INIT;
  pthread_mutex_unlock(&clients[clnum].mutex);
  /* release ssl structure */
  SSL_free(clients[clnum].ssl);
  /* close file descriptor */
  close(clients[clnum].fd);
  /* NULL assignments */ 
  clients[clnum].ssl = NULL;
}


/************************************
************************************/
int check_pwd(void *arg)
{
  static int pwdf = 0;
  byte_t *pwd = (byte_t *) list_get_first_item((list_t *) arg);
  if ( memcmp(args.pwd, pwd, PWDLEN) )
    {
      pwdf++;
      if ( pwdf == MAX_PWD_TRIES )
	return SEQ_CANT_CONTINUE;
      else
	return SEQ_NEEDS_RETRY;
    }
  else
    return SEQ_CL_OK;
}


/************************************
************************************/
int check_dummy(void *arg)
{
  return SEQ_CL_OK;
}


/************************************
************************************/
int check_last(void *arg)
{
  return SEQ_LAST;
}


/*********************************************************/
/* thread_abort():
 * ------------
 * aborts the server initialization freeing up used memory.
 *********************************************************/
void thread_abort()
{
  printf("[FATAL]: Could not start server.\n");
  CONTINUE = 0;
  exit(-1);
}



/*********************************************************
 * r_free():
 * ------
 * Resources are freed.
 ********************************************************/
void r_free()
{ 
  CONF_modules_unload(1);
  EVP_cleanup();
  ENGINE_cleanup();
  CRYPTO_cleanup_all_ex_data();
  ERR_clear_error();
  ERR_remove_state(0);
  ERR_free_strings();
  SSL_CTX_free(ctx);
  THREAD_cleanup();
  pthread_cond_destroy(&stream_cond);
  pthread_mutex_destroy(&stream_mutex);

  while( list_is_empty(args.imglist) == false)
    {
      list_node_t *tmp = list_get_head(args.imglist);
      fclose( (FILE *) tmp->item);
      list_remove(args.imglist, tmp);
      free(tmp);
    }

  close(args.svfd);
}



/**********************************************************
 * signal_handler(int sig):
 * --------------
 * Handle singals
 *********************************************************/
void signal_handler(int sig) 
{
  printf("[HANDLER]: Signal %d caught.\n", sig);
  /* TODO: use this function to control program workflow and exit properly */
  int flag = 0; 
  switch(sig)
    {
    case SIGINT:
      flag = fcntl(args.svfd, F_GETFL);
      /* this function should detach and free any thread, memory, etc */
      printf("\n\n--- Shutting down ---\n\n");
      CONTINUE = 0;
      fcntl(args.svfd, F_SETFL, flag | O_NONBLOCK);
      break;
    case SIGPIPE:
      printf("------------------------[WRN]:\tSIGPIPE\n");
      sleep(3);
      break;
    }
}



/******************************************************************
 * args_check(int argc, char *argv[]):
 * ----------
 * Checks programs arguments
 *****************************************************************/
void args_check(int argc, char *argv[])
{
  FILE      *file = NULL;
  int       i     = 0;

  /* Check if the program was called correctly */
  if ( argc != 4) 
    {
      printf("Usage:\n ./server <port> <path-to-file> <sha1sum-password>\n");
      exit(1);
    }
  else 
    {
      if ( atoi(argv[1]) < 1024 ) 
	{
	  printf("Port must be higher than 1024\n");
	  exit(1);
	}
      if ( (file = fopen(argv[2], "r")) == NULL ) 
	{
	  printf("Failed to open file: %s\n",argv[2]);
	  exit(1);
	}
      for(i = 0; argv[3][i] != '\0'; i++);
      if ( i != SHA_DIGEST_LENGTH * 2)
	{
	  printf("Password must be encripted 160-bits long (sha1sum).\n");
	  exit(1);
	}
    }
  /* at this point args are ok */
  args.port = atoi(argv[1]);  
  args.pwd = hexastr2binary(argv[3]);
  list_add_first_item(args.imglist, file);
  args.current = list_get_head(args.imglist);
}



/************************************************************
 *
 ***********************************************************/
int new_thread_data(int i, int clfd, struct sockaddr_in clientaddr)
{
  clients[i].fd         = clfd;
  clients[i].address    = clientaddr;
  clients[i].ssl        = SSL_new(ctx);
  thread_data[i].client = &clients[i];
  
  /* set to NON_BLOCKING */
  fcntl(clfd, F_SETFL, fcntl(clfd, F_GETFL) | O_NONBLOCK);

  if ( clients[i].ssl == NULL || !SSL_set_fd(clients[i].ssl, clients[i].fd) )
    return 0;
  else
    return 1;
}

