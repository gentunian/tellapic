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
#include <openssl/ssl.h>
#include <openssl/err.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <pthread.h>
#include <signal.h>

#include "server.h"

int                img_fd;
int                sv_fd;
struct sockaddr_in serveraddr;
MUTEX_TYPE         stream_mutex  = PTHREAD_MUTEX_INITIALIZER;
MUTEX_TYPE         *mutex_buf;
pthread_cond_t     stream_cond   = PTHREAD_COND_INITIALIZER;
//char               stream[BUFFER_SIZE];
queue_item_t       stream;
SSL_CTX            *ctx;
int                CONTINUE = 1;

/*************** JUST FOR TESTING *************/
MUTEX_TYPE cmmutex = PTHREAD_MUTEX_INITIALIZER;
MUTEX_TYPE camutex = PTHREAD_MUTEX_INITIALIZER;
unsigned long cm = 0;
unsigned long ca = 0;
/**********************************************/

void *attend_client(void *arg);
void *send_to_clients(void *arg);
void r_free();
void thread_abort();
void free_client(int client_num);
int THREAD_get_error(int value, int severity, int tid);
int THREAD_setup();
void client_list_init();
void cleanup_read_thread(void *arg);
void cleanup_write_thread(void *arg);

tdata_t            thread_data[MAX_CLIENTS + 1];

int main(int argc, char *argv[]) 
{
  int                cl_fd = 0;
  int                i     = 0;
  int                rv    = 0;
  pthread_attr_t     joinattr;
  struct sockaddr_in clientaddr;
  struct sigaction   sig_action;


  sig_action.sa_handler = signal_handler;
  sigemptyset(&sig_action.sa_mask);
  sig_action.sa_flags = SA_RESTART;
  if ( sigaction(SIGINT, &sig_action, NULL) == -1)
    printf("[ERR]: Could not install signal handler!\n");
  if ( sigaction(SIGPIPE, &sig_action, NULL) == -1)
    printf("[ERR]: Could not install signal handler!\n");

  /* check program arguments and return a handle to the image file */
  img_fd = args_check(argv, argc);
  
  /* initialize SSL */
  ctx = init_server_ctx();   
  SSL_CTX_use_certificate_file(ctx, "cacert.pem", SSL_FILETYPE_PEM); 
  SSL_CTX_use_PrivateKey_file(ctx, "privkey.pem", SSL_FILETYPE_PEM);
  
  /* Prepare the server for listen */
  sv_fd = open_listener(atoi(argv[1]), &serveraddr);

  /* Initialize data buffer */
  memset(&stream, 0, BUFFER_SIZE);

  /**/
  if ( THREAD_setup() == 0 )
    printf("THREAD_setup error\n");
  

  /* Initialize thread/clients list */
  for( i = 0; i < MAX_CLIENTS + 1; i++)
    {
      thread_data[i].client = NULL;
      thread_data[i].tid   = (pthread_t) 0;
      thread_data[i].tnum  = i;
      thread_data[i].state = THREAD_STATE_NEW;
      THREAD_get_error(pthread_mutex_init(&thread_data[i].mutex, NULL), FATAL, i);
    }

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
      int addrlen = sizeof(clientaddr);
      printf(LINE);    
      printf(NFO_WAIT);

      /* When a SIGINT is caught, this socket is set to non-blocking. Then, the program   */
      /* main loop reads the new CONTINUE value (0) and ends up with the shutdown process */
      if ( (cl_fd = accept(sv_fd, (struct sockaddr *)&clientaddr, &addrlen)) == -1)
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
	      if ( new_thread_data(&thread_data[i], cl_fd, clientaddr) )
		{
		  printf("[NFO]:\tClient node allocation succeded for socket %d at position %d\n", cl_fd, i);
		  rv = pthread_create(&thread_data[i].tid, &joinattr, attend_client, (void *) &thread_data[i].tnum);		
		  if ( THREAD_get_error(rv, WARN, thread_data[i].tnum) )
		    {
		      printf("[WRN]:\tSpawning thread ocess failed! Client discarted.\n");
		      pthread_mutex_lock(&thread_data[i].mutex);
		      free_client(i);
		      thread_data[i].state = THREAD_STATE_FREE;
		      pthread_mutex_unlock(&thread_data[i].mutex);
		    }
		}
	      else
		{
		  pthread_mutex_lock(&thread_data[i].mutex);
		  thread_data[i].state = THREAD_STATE_FREE;
		  pthread_mutex_unlock(&thread_data[i].mutex);
		  printf("[WRN]:\tClient node allocation falied!\n");
		  close(cl_fd);
		}
	    }
	  else
	    {
	      printf("[NFO]:\tServer reached full capacity.\n");
	      close(cl_fd);
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
  close(img_fd);
  close(sv_fd);
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
      flag = fcntl(sv_fd, F_GETFL);
      /* this function should detach and free any thread, memory, etc */
      printf("\n\n--- Shutting down ---\n\n");
      CONTINUE = 0;
      fcntl(sv_fd, F_SETFL, flag | O_NONBLOCK);
      break;
    case SIGPIPE:
      printf("------------------------[WRN]:\tSIGPIPE\n");
      break;
    }
}



/******************************************************************
 * args_check(char *argv[], int argc):
 * ----------
 * Checks programs arguments ( why i inversed the argS??)
 *****************************************************************/
int args_check(char *argv[], int argc)
{
  FILE *img_file;
  int fd;
  /* Check if the program was called correctly*/
  if ( argc != 4) 
    {
      printf("Usage:\n ./server <port> <path-to-file> <md5-password>\n");
      exit(1);
    }
  else 
    {
      if ( atoi(argv[1]) < 1024 ) 
	{
	  printf("Port must be higher than 1024\n");
	  exit(1);
	}
      if ( (img_file = fopen(argv[2], "r")) == NULL ) 
	{
	  printf("Failed to open file: %s\n",argv[2]);
	  exit(1);
	}
    }
  fd = fileno(img_file);
  fclose(img_file);

  return fd;
}



/************************************************************
 *
 ***********************************************************/
int new_thread_data(tdata_t *thread_data, int cl_fd, struct sockaddr_in clientaddr)
{
  client_t *client =  (client_t *) malloc(sizeof(client_t));
  if ( client == NULL)
    return 0;
  else
    {
      pthread_mutex_lock(&thread_data->mutex);
      thread_data->client          = client;
      thread_data->client->fd      = cl_fd;
      thread_data->client->address = clientaddr;
      thread_data->client->ssl     = SSL_new(ctx);
      /* TODO: CHECK FOR RETURN VALUE */
      pthread_mutex_init(&thread_data->client->mutex, NULL);      
      thread_data->client->cl_info = NULL;
      pthread_mutex_unlock(&thread_data->mutex);
      return 1;
    }
}



/**********************************************************************
 * send_to_clients(void *arg):
 * ---------------
 * Thread for sending data to connected clients from a specific client
 **********************************************************************/
void *send_to_clients(void *arg)
{
  int i        = 0;
  int sender   = 0;
  int b;
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
	  /* WHAT IF CLIENT HAS LEFT ????????? */
	  pthread_mutex_lock(&thread_data[i].mutex);

	  if ( b = thread_data[i].state == THREAD_STATE_ACTIVE && i != stream.from )
	    {
	      printf("Sending %d bytes and string '%s' to client/thread %d\n", stream.nbytes, stream.buffer, i);
	      SSL_write(thread_data[i].client->ssl, stream.buffer, stream.nbytes);
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
  int *tnum             = (int *) arg;
  /* TODO: CLEAN UP THIS DECLARATIONS */
  int ssl_err_code = 0;
  int bytesread    = 0;
  int byteswrote   = 0;
  long ssl_mode    = 0;
  int fd_flag      = 0;
  int i            = 0;
  const int chunk_size = 1024;
  char buffer[chunk_size];      /*unused till now */
  char *outstream;              /*unused till now */
  int nbytes = 0;
  int err;
  fd_set master, copy;
  unsigned long errsv = 0;
  FD_ZERO(&master);
  FD_ZERO(&copy);
  FD_SET(thread_data[*tnum].client->fd, &master);
  printf("<thread %d>[NFO]:\tInitializing server attendance client thread on socket %d\n", *tnum, thread_data[*tnum].client->fd);

  /* get current fd flag */
  fd_flag = fcntl(thread_data[*tnum].client->fd, F_GETFL);

  pthread_mutex_lock(&thread_data[*tnum].mutex);

  /* associate fd with ssl structure */
  ssl_err_code = SSL_set_fd(thread_data[*tnum].client->ssl, thread_data[*tnum].client->fd);

  printf("<thread %d>[NFO]:\tTrying to associate ssl structure with fd %d\n", *tnum, thread_data[*tnum].client->fd);

  /* register cleanup function for this thread */
  pthread_cleanup_push(cleanup_read_thread, (void *) tnum);

  if ( !ssl_err_code)
    {
      printf("<thread %d>[ERR]:\tCannot associate fd with ssl structure\n", *tnum);
      pthread_mutex_unlock(&thread_data[*tnum].mutex);
      pthread_exit(NULL); //this will call cleanup function.
    }
  printf("<thread %d>[NFO]:\tSSL associated to socket %d\n", *tnum, thread_data[*tnum].client->fd);
  
  /* set fd to non blocking */
  fcntl(thread_data[*tnum].client->fd, F_SETFL, fd_flag | O_NONBLOCK);
  
  /* wait for a handshake. */
  if( wait_for_client(thread_data[*tnum].client) <= 0 )  
    {
      printf("<thread %d>[ERR]:\tcould not hanshake with client.\n", *tnum);
      pthread_mutex_unlock(&thread_data[*tnum].mutex);
      pthread_exit(NULL); //this will call cleanup function.p
    }
  
  printf(NFO_ACCEPT, *tnum, inet_ntoa(thread_data[*tnum].client->address.sin_addr), thread_data[*tnum].client->fd);
  printf(NFO_CIPHER, *tnum, SSL_get_version(thread_data[*tnum].client->ssl), SSL_get_cipher(thread_data[*tnum].client->ssl));
  pthread_mutex_lock(&cmmutex);
  cm++; // JUST FOR TESTING
  pthread_mutex_unlock(&cmmutex);
  
  thread_data[*tnum].state = THREAD_STATE_ACTIVE;
  printf("<thread %d>[NFO]:\tMain attendance loop start\n", *tnum);
  
  while( thread_data[*tnum].state == THREAD_STATE_ACTIVE )
    {
      copy = master;

      nbytes = SSL_read(thread_data[*tnum].client->ssl, buffer, chunk_size);
      err = SSL_get_error(thread_data[*tnum].client->ssl, nbytes);
      errsv = ERR_get_error();
      //      printf("<thread %d>[NFO]:\tSSL_read() = (%d) bytes (err=%d, str= %s)\n", tnum, nbytes, err, ERR_error_string(errsv, NULL));
      if ( nbytes <= 0 )
	{
	  /* see if we need to call SSL_read() again */
	  if ( err == SSL_ERROR_WANT_READ || err == SSL_ERROR_WANT_WRITE)
	    {
	      pthread_mutex_unlock(&thread_data[*tnum].mutex);
	      printf("<thread %d>[NFO]:\tWANT_READ on socket %d (ERR_get_error()=%ld)\n", *tnum, thread_data[*tnum].client->fd, errsv);
	      if ( select(thread_data[*tnum].client->fd+1, &copy, NULL, NULL, NULL) == -1)
		printf("<thread %d>:[ERR]:\tSelect fail\n",tnum);
	      pthread_mutex_lock(&thread_data[*tnum].mutex);		
	    }
	  else if ( err == SSL_ERROR_SYSCALL )
	    {
	      printf("<thread %d>[NFO]:\tERR_SYSCALL on socket %d (Client disconnects without hanshake) (ERR_get_error()=%ld\n", *tnum, thread_data[*tnum].client->fd, errsv);
	      pthread_mutex_unlock(&thread_data[*tnum].mutex);
	      pthread_exit(NULL); //this will call cleanup function.
	    }
	  else if ( err == SSL_ERROR_ZERO_RETURN )
	    {
	      printf("<thread %d>:[NFO]:\tSocket %d hung up ( ERR_get_error()=%ld\n", *tnum, errsv);
	      pthread_mutex_unlock(&thread_data[*tnum].mutex);
	      pthread_exit(NULL); //this will call cleanup function.
	    }
	  else {}
	}
      else
	{
	  pthread_mutex_unlock(&thread_data[*tnum].mutex);
	  buffer[nbytes] = '\0';
	  //printf("<thread %d>[NFO]:\tData read on socket %d. '%s' (%d bytes)\n", tnum, client->fd, buffer, nbytes);
	  /* it should be NICE to have a QUEUE! if we are locked */
	  
	  /* copy the buffer to 'stream', so sending thread will send it to clients */
	  pthread_mutex_lock(&stream_mutex);
			 
	  stream.from  = *tnum;
	  //client->nbytes = nbytes;
	  stream.nbytes = nbytes;
	  
	  /* strncpy is safe to use here as 'nbytes'>=1, 'buffer' != NULL and 'stream' != NULL */
	  strncpy(stream.buffer, buffer, nbytes);

	  /* signal sending thread */
	  pthread_cond_signal(&stream_cond);
	  
	  /* release mutex on stream buffer */
	  pthread_mutex_unlock(&stream_mutex);
	  memset(buffer, 0, sizeof(buffer));
	  
	  pthread_mutex_lock(&thread_data[*tnum].mutex); //for next reading in while loop
	}
      
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
void cleanup_read_thread(void *arg)
{
  int *tnum = (int *) arg;
  pthread_mutex_lock(&thread_data[*tnum].mutex);  
  printf("<thread %d>[NFO]:\tEntering read thread clean up function\n", *tnum );  
  free_client(*tnum);
  ERR_remove_state(pthread_self());
  thread_data[*tnum].state = THREAD_STATE_FREE;
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
void free_client(int client_num)
{
  //printf("ssl: free_client %d\n", client->fd);
  /* release ssl structure */

  SSL_free(thread_data[client_num].client->ssl);

  //printf("close: free_client %d\n", client->fd);
  /* close client file descriptor */

  close(thread_data[client_num].client->fd);

  //printf("ssl=null: free_client %d\n", client->fd);
  /* sanity assignment */

  thread_data[client_num].client->ssl = NULL;

  //  printf("pthread_mutex_destroy: free_client %d\n", client->fd);
  /* destroy client mutex */

  pthread_mutex_destroy(&thread_data[client_num].client->mutex);

  //  printf("free_client %d\n", client->fd);
  /* release client structure */

  free(thread_data[client_num].client);

  /* sanity assignment */
  thread_data[client_num].client = NULL;
}



/**************************************
 * wait_for_client(client_t *client):
 * ---------------
 **************************************/
int wait_for_client(client_t *client)
{
  struct timeval tv;
  fd_set master;
  fd_set copy;
  int    ssl_err_code;
  int    err;  

  FD_ZERO(&master);
  FD_ZERO(&copy);
  FD_SET(client->fd, &master);
  tv.tv_sec  = 5;
  tv.tv_usec = 0;
    
  ssl_err_code = SSL_accept(client->ssl);
  err = SSL_get_error(client->ssl, ssl_err_code);

  while( (err == SSL_ERROR_WANT_READ  ||  err == SSL_ERROR_WANT_WRITE) )
    {
      copy = master;
      if ( (select(client->fd+1, &copy, NULL, NULL, &tv)) == 0 )
	{
	  printf("[WRN]:\tselect() timeout reached\n");
	  break;
	}
      if ( FD_ISSET(client->fd, &copy) )
	{
	  ssl_err_code = SSL_accept(client->ssl);
	  err = SSL_get_error(client->ssl, ssl_err_code);
	}
    }
  return ssl_err_code;
}



/****************************************************

/****************************************************/
void client_list_init(tdata_t threads[])
{
  int i  = 0;
  /* TODO? */
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
