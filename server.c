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

MUTEX_TYPE         *mutex_buf; //this is initialized by THREAD_setup();
MUTEX_TYPE         clcmutex      = PTHREAD_MUTEX_INITIALIZER;
MUTEX_TYPE         stream_mutex  = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t     stream_cond   = PTHREAD_COND_INITIALIZER;
//char               stream[BUFFER_SIZE];
queue_item_t       stream; //this is shared
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
void               strcptobuf(const byte_t *src, int size, int from);
int                THREAD_get_error(int value, int severity, int tid);
int                THREAD_setup();
int                new_thread_data(int i, int clfd, struct sockaddr_in clientaddr);
int                THREAD_cleanup(void);
int                ssl_read_b(client_t *cl, int *err, byte_t *buf, size_t size, struct timeval *tv);
int                ssl_write_b(client_t *cl, int *err, const byte_t *buf, size_t size);
unsigned char      *hexastr2binary(unsigned char *sha1hexadigest);
int                htoi(unsigned char hexdigit);

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
      printf(LINE);    
      printf(NFO_WAIT);

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



/**********************************************************************
 * send_to_clients(void *arg):
 * ---------------
 * Thread for sending data to connected clients from a specific client
 **********************************************************************/
void *send_to_clients(void *arg)
{
  int   i   = 0;
  int   ec  = 0;
  int   rv  = 0;
  pthread_cleanup_push(cleanup_write_thread, NULL);
  printf("<thread %d>[NFO]:\tSending thread started.\n", SV_THREAD);
  while(1 )
    {
      /* lock shared buffer data */
      pthread_mutex_lock(&stream_mutex);      
      printf("<thread %d>[NFO]:\tWaiting for data to be sent.\n", SV_THREAD);
      /* wait until we are signaled or we are cancelled */
      pthread_cond_wait(&stream_cond, &stream_mutex);      
      /* thread is signaled. send data */      
      printf("<thread %d>[NFO]:\tGot data. Sending...\n", SV_THREAD);
      for( i = 0; i < MAX_CLIENTS; i++)
	{
	  pthread_mutex_lock(&thread_data[i].mutex);
	  if ( thread_data[i].state == THREAD_STATE_ACTIVE && i != stream.from )
	    {
	      pthread_mutex_lock(&thread_data[i].client->mutex);
	      do
		{
		  if ( thread_data[i].client->state == CLIENT_STATE_READY )
		    {
		      printf("Sending %d bytes and string '%s' to client/thread %d with state: %d\n", stream.nbytes, stream.buffer, i, clients[i].state);
		      rv = SSL_write(thread_data[i].client->ssl, stream.buffer, stream.nbytes);
		      ec = SSL_get_error(thread_data[i].client->ssl, rv);
		    }
		}
		while( ec == SSL_ERROR_WANT_WRITE || ec == SSL_ERROR_WANT_READ );
	      pthread_mutex_unlock(&thread_data[i].client->mutex);
	    }
	  pthread_mutex_unlock(&thread_data[i].mutex);
	}
      memset(stream.buffer, 0, sizeof(stream.buffer));
      pthread_mutex_unlock(&stream_mutex);
    }
  printf("<thread %d>[NFO]:\tSending thread closing.\n", SV_THREAD);
  pthread_cleanup_pop(1);
  pthread_exit(NULL);
}


/*******************************************************************
 * attend_client(void *arg):
 * ------------
 * Thread for receive new connections
 *******************************************************************/
void *attend_client(void *arg) 
{
  char      buffer[BUFFER_SIZE];
  int       *tnum      = (int *) arg;
  int       nbytes     = 0;
  int       err        = 0;
  int       osbytes    = 0;
  char      *ostream   = NULL;
  client_t  *tmpclient = clients;
  fd_set    master;
  fd_set    copy;

  FD_ZERO(&master);
  FD_ZERO(&copy);
  FD_SET(thread_data[*tnum].client->fd, &master);
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
  pthread_mutex_lock(&clcmutex);
  clccount++;
  cm++;
  pthread_mutex_unlock(&clcmutex);
  printf("<thread %d>[NFO]:\tSending id list to connected clients\n", *tnum);


  ostream = STREAM_build(CTL_SV_IDLIST, tmpclient, iterate_id, &nbytes);  //FINISH THIS!


  /* if there is connected clients send it */
  if ( ostream != NULL )
    strcptobuf(ostream, nbytes, *tnum);
  pthread_mutex_unlock(&thread_data[*tnum].client->mutex);
  thread_data[*tnum].client->state = CLIENT_STATE_READY;
  pthread_mutex_unlock(&thread_data[*tnum].client->mutex);
  printf("<thread %d>[NFO]:\tMain attendance loop start\n", *tnum);
  free(ostream);
  pthread_mutex_lock(&thread_data[*tnum].mutex);
  thread_data[*tnum].state = THREAD_STATE_ACTIVE;
  while( thread_data[*tnum].state == THREAD_STATE_ACTIVE )
    {
      pthread_mutex_unlock(&thread_data[*tnum].mutex);
      copy   = master;
      nbytes = ssl_read_b(thread_data[*tnum].client, &err, buffer, BUFFER_SIZE, NULL);
      if ( nbytes <= 0 )
	{
	  switch (err)
	    {
	    case SSL_ERROR_SSL:
	      printf("<thread %d>[NFO]:\tERR_SSL on socket %d (Client disconnects without hanshake)\n", *tnum, thread_data[*tnum].client->fd);
	      break;
	    case SSL_ERROR_SYSCALL:
	      printf("<thread %d>[NFO]:\tERR_SYSCALL on socket %d (Client disconnects without hanshake)\n", *tnum, thread_data[*tnum].client->fd);
	      break;
	    case SSL_ERROR_ZERO_RETURN:
	      printf("<thread %d>:[NFO]:\tSocket %d hung up\n", *tnum, thread_data[*tnum].client->fd);
	      break;
	    }
	  pthread_mutex_lock(&clcmutex);
	  clccount--;
	  pthread_mutex_unlock(&clcmutex);
	  pthread_exit(NULL);
	}
      else
	{
	  strcptobuf(buffer, nbytes, *tnum);
	  memset(buffer, 0, BUFFER_SIZE);
	}
      pthread_mutex_lock(&thread_data[*tnum].mutex);
    }
  pthread_mutex_unlock(&thread_data[*tnum].mutex);
  printf("<thread %d>[NFO]:\tAttendace thread closing on %d socket.\n", *tnum, thread_data[*tnum].client->fd);
  pthread_cleanup_pop(1);
  /* this thread has no more reason to live */
  pthread_exit(NULL);
}



/************************************
 *
 ************************************/
void strcptobuf(const byte_t *src, int size, int from)
{
  int i = 0;
  /* Don't forward anything. Check if src is a client stream */
  if ( STREAM_is_stream(src, size) )
    {
      pthread_mutex_lock(&stream_mutex);			 
      stream.from   = from;
      stream.nbytes = size;
      for(i = 0; i < stream.nbytes; i++)
	stream.buffer[i] = src[i];
      pthread_cond_signal(&stream_cond);	 
      pthread_mutex_unlock(&stream_mutex);
    }
}


/************************************
 *
 ************************************/
void cleanup_read_thread(void *arg)
{
  int *tnum = (int *) arg;
  free_client(*tnum);
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
  PSEQ_add_pbargs(pseq, PSEQ_CONN_3, (void *) &client->fd);
  PSEQ_add_pbargs(pseq, PSEQ_CONN_3, (void *) list_get_item(args.current));

  tv.tv_sec  = 5;
  tv.tv_usec = 0;
  ec         = ssl_accept_b(client, tv);
  if ( ec == SSL_ERROR_NONE ) 
    {
      ostream = STREAM_build(CTL_SV_ASKPWD, client->fd, &osbytes);
      do
	{
	  if ( ssl_write_b(client, &ec, ostream, osbytes) <= 0)
	    return 0;
	  if ( (isbytes = ssl_read_b(client, &ec, istream, BUFFER_SIZE, &tv)) <= 0 )
	    return 0;
	  else
	    {
	      byte_t clpacket = STREAM_get_ctlbyte(istream, isbytes);
	      byte_t svpacket = STREAM_get_ctlbyte(ostream, osbytes);
	      if ( seq == 0)
		PSEQ_add_pcargs(pseq, PSEQ_CONN_0, (void *) STREAM_get_pwd(istream, isbytes, client->fd));
	      seq = PSEQ_next_sequence(seq, clpacket, svpacket, pseq, &ostream, &osbytes);
	    }
	}
      while( seq >= 0 && seq < PSEQ_CONN_SIZE );
      //TODO: CLEAN UP LEAKS FROM LIST AND SO ON...PSEQ[0].pcargs PSEQ[i].pbargs
      free(ostream);
      PSEQ_cleanup(PSEQ_CONN, pseq);
      free(pseq);
      return seq;
    }
  else
    return 0;
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
int ssl_write_b(client_t *cl, int *err, const byte_t *buf, size_t size)
{
  int        bwrote   = 0;
  *err = SSL_ERROR_NONE;
  while( bwrote < size && *err == SSL_ERROR_NONE)
    {
      do
	{
	  bwrote +=  SSL_write(cl->ssl, buf, size);
	  *err    = SSL_get_error(cl->ssl, bwrote);
	}
      while( ( *err == SSL_ERROR_WANT_READ || *err == SSL_ERROR_WANT_WRITE ) );
    }  
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
  int i  = 0;
  /* Initialize thread/clients list */
  for( i = 0; i < MAX_CLIENTS; i++)
    {
      thread_data[i].client = NULL;
      thread_data[i].tid    = (pthread_t) 0;
      thread_data[i].tnum   = i;
      thread_data[i].state  = THREAD_STATE_NEW;
      clients[i].ssl        = NULL;
      clients[i].clinfo     = NULL;
      clients[i].clidx      = i;
      clients[i].state      = CLIENT_STATE_INIT;
      THREAD_get_error(pthread_mutex_init(&clients[i].mutex, NULL), FATAL, i);
      THREAD_get_error(pthread_mutex_init(&thread_data[i].mutex, NULL), FATAL, i);
    }

  /* The remaind thread initialization ( i == SV_THREAD) */
  thread_data[i].client = NULL;
  thread_data[i].tid    = (pthread_t) 0;
  thread_data[i].tnum   = i;
  thread_data[i].state  = THREAD_STATE_NEW;
  THREAD_get_error(pthread_mutex_init(&thread_data[i].mutex, NULL), FATAL, i);

  /* initialize data buffer */
  memset(stream.buffer, 0, BUFFER_SIZE);
  stream.nbytes = 0;
  stream.from   = 0;

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


/**********************************************************
 *********************************************************/
unsigned char *hexastr2binary(unsigned char *sha1hexadigest)
{  
  unsigned char *output = (unsigned char *) malloc(sizeof(unsigned char) * SHA_DIGEST_LENGTH + 1);
  int i = 0;

  output[SHA_DIGEST_LENGTH] = '\0';
  for(i = SHA_DIGEST_LENGTH - 1; i >= 0; i--)
    {
      output[i]  = htoi(sha1hexadigest[i * 2 + 1]);
      output[i] |= htoi(sha1hexadigest[i * 2])<<4;
    }  
  return output;
}


/**********************************************************
 *********************************************************/
int htoi(unsigned char hexdigit)
{
  char hex[16] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
  int i = 0;
  
  for(i = 0; i < 16 && hex[i] != hexdigit; i++);
  
  if( i == 16 )
    return -1;
  else
    return i;
}
