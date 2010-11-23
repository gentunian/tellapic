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
#include <stdarg.h>

#include "server.h"
#include "stream.h"
#include "common.h"

MUTEX_TYPE         *mutex_buf; //this is initialized by THREAD__setup();
MUTEX_TYPE         clcmutex = PTHREAD_MUTEX_INITIALIZER;
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


int main(int argc, char *argv[]) 
{
  int                clfd = 0;        /* client file descriptor */
  int                i    = 0;        /* iterator variable */
  int                rv   = 0;        /* returned value for some function */

  struct sockaddr_in clientaddr;      /* client address structure */
  struct sockaddr_in serveraddr;      /* server address structure */
  struct sigaction   sig_action;      /* signal handler for external signal management */

  pthread_attr_t     joinattr;        /* thread joinable attribute for joinable threads */

  tdata_t            thread_data[MAX_CLIENTS + 1]; /* this is the main shared data */


  /*****************************/
  /* initialize signal handler */
  /*****************************/
  sig_action.sa_handler = signal_handler;
  sigemptyset(&sig_action.sa_mask);
  sig_action.sa_flags = SA_RESTART;


  /******************************************************/
  /* TODO (24/05/10): more work on these error checking */
  /******************************************************/
  if ( sigaction(SIGINT, &sig_action, NULL) == -1)
    printf("[ERR]: Could not install signal handler!\n");
  if ( sigaction(SIGPIPE, &sig_action, NULL) == -1)
    printf("[ERR]: Could not install signal handler!\n");


  /***********************************************/
  /* initialize image list. TODO (24/05/10): ?!? */
  /***********************************************/
  args.imglist = list_make_empty(args.imglist);


  /*************************************************************/
  /* check program arguments and fill args_t structure in args */
  /*************************************************************/
  args_check(argc, argv);
  

  /****************************/
  /* initialize SSL on server */
  /****************************/
  ctx = init_server_ctx();   
  SSL_CTX_use_certificate_file(ctx, "cacert.pem", SSL_FILETYPE_PEM); 
  SSL_CTX_use_PrivateKey_file(ctx, "privkey.pem", SSL_FILETYPE_PEM);
  

  /*********************************/
  /* Prepare the server for listen */
  /*********************************/
  args.svfd = open_listener(atoi(argv[1]), &serveraddr);

  
  /**************************/
  /* TODO: (24/05/10): wtf? */
  /**************************/
  if ( THREAD_setup() == 0 )
    printf("THREAD_setup error\n");


  /********************************************************/
  /* Initialize main data structures and global variables */
  /********************************************************/
  data_init(thread_data);


  /*****************************************************/
  /* initialize and create a thread joinable attribute */
  /*****************************************************/
  pthread_attr_init(&joinattr);
  pthread_attr_setdetachstate(&joinattr, PTHREAD_CREATE_JOINABLE); 


  /****************************************/
  /* try to launch 'signal_client' thread */
  /****************************************/
  rv = pthread_create(&thread_data[SV_THREAD].tid, &joinattr, signal_client, thread_data);


  /**************************************************/
  /* TODO: Review this function and its use. Please */
  /**************************************************/
  THREAD_get_error(rv, FATAL, thread_data[SV_THREAD].tnum);


  /*************************************/
  /* set server thread state as active */
  /*************************************/
  thread_data[SV_THREAD].tstate = THREAD_STATE_ACT; /* is it necessary? */


  /***************************/
  /* server started normally */
  /***************************/
  while( CONTINUE  ) 
    {
      int notfound = 1;
      int addrlen  = sizeof(clientaddr);
      
      /****************************************/
      /* TODO: add some kind of verbose level */
      /****************************************/
      printf("\n-----------------------------------------------------------------\n");
      printf("[NFO]:\tWaiting for incoming connections\n");

      
      /*******************************************************************************************/
      /* When a SIGINT is caught, this socket (clfd) is set to non-blocking. Then, the program   */
      /* main loop reads the new CONTINUE value (0) and ends up with the shutdown process        */
      /*******************************************************************************************/
      if ( (clfd = accept(args.svfd, (struct sockaddr *)&clientaddr, &addrlen)) == -1)
	{
	  printf("[NFO]:\tConnection refused.\n");
	}
      else
	{
	  ca++;  //JUST FOR TESTING PURPOSES
	  printf("[NFO]:\tConnection attempt from %s.\n", inet_ntoa(clientaddr.sin_addr));
	  for( i = 0; i < MAX_CLIENTS && notfound; i++)
	    {
	      printf("[NFO]:\tsearching place on %d and locking\n",i);
	      pthread_mutex_lock(&thread_data[i].stmutex);

	      /**************************************************************************/
	      /* TODO (24/05/10): Set it to if-else or a switch. Asking 2 times in vain */
	      /**************************************************************************/
	      if ( thread_data[i].tstate == THREAD_STATE_NEW )
		{
		  printf("[NFO]:\tthread %d was never used\n",i);
		  thread_data[i].tstate = THREAD_STATE_INIT;
		  notfound = 0;
		}
	      if ( thread_data[i].tstate == THREAD_STATE_FREE )
		{		  
		  printf("[NFO]:\tthread %d was used.\n", i);
		  /*************************************************************************** 
		   * MAN PAGE:
		   * --------
		   * After a canceled thread has terminated, a join with that  thread  using
		   * pthread_join()  obtains  PTHREAD_CANCELED as the threads exit status.
		   * (Joining with a thread is the only way to know  that  cancellation  has
		   * completed.)
		   * RETURN VALUE
		   *   On  success, pthread_cancel() returns 0; on error, it returns a nonzero
		   *   error number.
		   *
		   * ERRORS
		   *   ESRCH  No thread with the ID thread could be found.
		   ***************************************************************************/
		  pthread_join(thread_data[i].tid, NULL);
		  thread_data[i].tstate = THREAD_STATE_INIT;
		  notfound = 0;
		}
	      pthread_mutex_unlock(&thread_data[i].stmutex);
	    }


	  /***************************************************************************/
	  /* If a FREE or NEW thread was found, allocate memory for it and launch it */
	  /***************************************************************************/
	  if ( notfound == 0 )
	    {

	      /*********************************************************/
	      /* the for exits after the 'i++' instruction is executed */
	      /* and the condition is tested. So, we decrement i once. */
	      /*********************************************************/
	      i--;


	      /************************************************/
	      /* we try to allocate memory for the new client */
	      /************************************************/
	      if ( new_client(&thread_data[i], clfd, clientaddr) )
		{

		  /******************************************************************************/
		  /* if memory allocation succeeds, we try to launch the client managing thread */
		  /******************************************************************************/
		  rv = pthread_create(&thread_data[i].tid, &joinattr, attend_client, &thread_data[i]);


		  /****************************************************************************/
		  /* having an error here is not fatal, so we call THREAD_get_error with WARN */
		  /****************************************************************************/
		  if ( THREAD_get_error(rv, WARN, thread_data[i].tnum) )
		    {
		      printf("[WRN]:\tSpawning thread process failed! Client discarted.\n");
		      
		      /********************************/
		      /* release the client structure */
		      /********************************/
		      free_client(thread_data[i].client);


		      /*******************************/
		      /* set the i-th thread to FREE */
		      /*******************************/
		      set_tstate(&thread_data[i], THREAD_STATE_FREE);
		    }
		}
	      else
                {
		  /****************************************************/
		  /* if 'new_client()' fails allocating ssl structure */
		  /* and 'client' was allocated we must free it.      */
		  /****************************************************/
		  if ( thread_data[i].client != NULL )
		    free(thread_data[i].client);

		  
		  /*********************************************/
		  /* set the thread state to THREAD_STATE_FREE */
		  /*********************************************/
                  set_tstate(&thread_data[i], THREAD_STATE_FREE);
		  

		  /*****************************/
		  /* close the file descriptor */
		  /*****************************/
		  close(clfd);

                  printf("[WRN]:\tClient node allocation falied!\n");
                }
	    }
	  else
	    {
	      printf("[NFO]:\tServer reached full capacity.\n");

	      /*****************************/
	      /* close the file descriptor */
	      /*****************************/
	      close(clfd);
	    }
	}
    }
  /* Start freeing resources */
  printf("<main thread>[NFO]:\tStart freeing resources\n");
  /*
  for( i = 0; i < MAX_CLIENTS + 1; i++)
v    {
      if ( thread_data[i].client != NULL)
	printf("<main thread>[NFO]:\tposition %d with state %d and fd %d. TID: %ld\n", i, thread_data[i].state, thread_data[i].client->fd, thread_data[i].tid);
      if ( thread_data[i].state != THREAD_STATE_NEW)
	{
	  printf("<main thread>[NFO]:\tSending cancel signal to thread with pid %ld and number %d\n", thread_data[i].tid, thread_data[i].tnum);
	  pthread_cancel(thread_data[i].tid);
	  pthread_join(thread_data[i].tid, NULL);
	  pthread_mutex_destroy(&thread_data[i].mutex);
	}
    }
  */
  printf("<main thread>[NFO]:\tWaiting for threads done!\n");
  /*
  r_free();
  */
  printf("<main thread>[NFO]:\tAllocated API structures released!\n");
  /*
  pthread_attr_destroy(&joinattr);
  */
  printf("\n--- END ---\n");

  /****************************************************/
  /********** TESTING PURPOSES ************************/
  /****************************************************/
  printf("\nserver exits correctly with a total of:\n");
  printf("\n\tConnection attemps: %ld\n",ca);
  printf("\tConnections made: %ld\n", cm);
  pthread_exit(NULL);
}



/***************************************************/
/***************************************************/
int should_send(tdata_t *thread, stream_item_t *item)
{
  /********************************/
  /*TODO: Think about private chat*/
  /********************************/
  byte_t *drawingbyte = NULL;

  if ( thread->tnum != item->from )
    {
      if (STREAM_is_drawing(item->data))
	{
	  /* TODO: create function profile drawingbyte = STREAM_get_drawingbyte(item->data);*/
	  if ( thread->client->fwdbitlist & (1 << item->from))
	    {

	      /****************************************************/
	      /* We can send whatever to the i-th client, but if  */
	      /* a release event if found, restore the fwd bit.   */
	      /****************************************************/
	      if ( STREAM_is_release_event(drawingbyte))
		thread->client->fwdbitlist &= ~(1 << item->from);
	      return 1;
	    }
	  else
	    /***********************************************************/
	    /* We need to check more things as the fwd bit is not set. */
	    /***********************************************************/
	    if (STREAM_is_press_event(drawingbyte))
	      return thread->client->fwdbitlist |= (1 << item->from);
	    else
	      return 0;
	}
      else
	return 1;
    }
  else
    return 0;
}



/**************************************************************************
 * void signal_client(void *arg):
 * -----------------------------
 *
 * This is a threaded function called without parameters. The main reason
 * for this thread to live is forwarding its queue items to the others 
 * threads queue. This thread has the MAIN queue where all the others
 * threads queue in it when they read data from a client. The data read
 * from a thread is then queued on this thread queue. This thread, queue
 * the item on all the others thread except the one that has read the data,
 * and signal the threads with a write signal to perform the appropiate
 * action. Then, the thread receives this 'write signal' and it goes
 * fetch data from its queue to be sent.
 * 
 *************************************************************************/
void *signal_client(void *arg)
{
  tdata_t *thread_data = (tdata_t *) arg;  /* thread data pointer. Points to first item. */

  stream_item_t *item = NULL;              /* item from some queue */

  unsigned short i = 0;                    /* iterator variable */

  pthread_cleanup_push(signal_client_cleanup, &thread_data[SV_THREAD]);
  printf("<thread %d>[NFO]:\tSending thread started.\n", SV_THREAD);
  while(1)
    {
      /* blocking function that pops out an item from SV_THREAD-th queue. */
      /* The block occurs when the queue is empty                         */
      item = (stream_item_t *) wait_for_data(&thread_data[SV_THREAD]);
      
      /************************************************************************************/
      /* We don't want to lose the 'item' reference and be cancelled when forwarding data */
      /************************************************************************************/
      pthread_setcancelstate(PTHREAD_CANCEL_DISABLE, NULL);
      for (i = 0; i < MAX_CLIENTS; i++)
	{
	  if (should_send(&thread_data[i], item))
	    {
	      pthread_mutex_lock(&thread_data[i].stmutex);
	      if ( thread_data[i].tstate != THREAD_STATE_END  &&
		   thread_data[i].tstate != THREAD_STATE_FREE &&
		   thread_data[i].tstate != THREAD_STATE_NEW 
		   )
		{

		  /********************************************************/
		  /* queue an item using mutexes on the i-th thread queue */
		  /********************************************************/
		  push_item(&thread_data[i], item);

		  printf("Item added to %d client\n", i);


		  /******************************************/
		  /* send a write signal to the i-th thread */
		  /******************************************/
		  send_write_signal(&thread_data[i]);
		}
	      pthread_mutex_unlock(&thread_data[i].stmutex);
	    }
	}

      /***************************************************************/
      /* we release the memory used by the item poped from the queue */
      /***************************************************************/
      free(item);
      

      /***************************************************************************************/
      /* Now we can be cancelled. Enable cancelling and test wheter or not we were cancelled */
      /***************************************************************************************/
      pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
      

      /*************************************************************/
      /* Creates a cancellation point to test if we were cancelled */
      /*************************************************************/
      pthread_testcancel();
    }
  printf("<thread %d>[NFO]:\tSending thread closing.\n", SV_THREAD);
  pthread_cleanup_pop(1);
  pthread_exit(NULL);
}


/*******************************************************************
 ******************************************************************/
void signal_client_cleanup(void *arg)
{
  tdata_t *thread = (tdata_t *) arg;

  printf("<thread %d>[NFO]:\tEntering write thread clean up function\n", SV_THREAD);

  /**********************************/
  /* release al items in the queue. */
  /* Let the queue pointer be freed */
  /* by the main thread             */
  /**********************************/
  free_thread_queue(thread);


  /*******************/
  /* close the pipes */
  /*******************/
  close(thread->event_reader);
  close(thread->event_writer);


  /*****************************/
  /* free thread's queue error */
  /*****************************/
  ERR_remove_state(pthread_self());


  /***************************************/
  /* set this state to free. (pointless) */
  /***************************************/
  set_tstate(thread, THREAD_STATE_FREE);

  /* destroy the mutexes and condition variables on main thread */
}


/**************************************/
/**************************************/
void send_write_signal(tdata_t *thread) 
{
  int rv = 0;
  char *dumb = NULL;
  
  *dumb = 'a';
  /******************************************/
  /* NOTE: 'write()' is a Cancellable Point */
  /******************************************/
  rv = write(thread->event_writer, (void *)dumb, sizeof(char *));
  if ( rv == -1)
    {
      if ( errno == EWOULDBLOCK || errno == EAGAIN)
	{
	}
      else
	perror("write() to pipe failed");
    }
}


/*******************************************************************
 * helper function
 ******************************************************************/
void push_item(tdata_t *thread, void *item) {
  pthread_mutex_lock(&thread->queuemutex);
  list_add_last_item(thread->queue, item);
  pthread_cond_signal(&thread->queuecond);
  pthread_mutex_unlock(&thread->queuemutex);
}


/*******************************************************************
 * helper function
 ******************************************************************/
void *pop_item(tdata_t *thread) {
  void *item = NULL;
  
  pthread_mutex_lock(&thread->queuemutex);
  item = (list_remove_first(thread->queue))->item;
  pthread_mutex_unlock(&thread->queuemutex);

  return item;
}


/*******************************************************************
 * helper function: blocking function. It waits on the i-th thread queue
 * for data. If it have some data, it pops out from queue and return it.
 * The main difference with pop is that if there's no data on the queue
 * we wait until some data is pushed.
 ******************************************************************/
void *wait_for_data(tdata_t *thread) {

  printf("tnum: %d\n", thread->tnum);
  void *item;

  /********************************/
  /* aquire a lock to the i queue */
  /********************************/
  pthread_mutex_lock(&thread->queuemutex);

  /****************************************************/
  /* if the queue is empty, wait until we get signaled*/
  /****************************************************/
  if ( list_is_empty(thread->queue)) {
    printf("Stream Queue is empty. Waiting...\n");

  
    /**********************************************************************************************/
    /* the mutes is released, when a signal is sent, the mutex is aquired again by this function. */
    /* NOTE: 'pthread_cond_wait()' is a Cancellable Point */
    /******************************************************/
    pthread_cond_wait(&thread->queuecond, &thread->queuemutex);

    printf("We got signaled.");
  }

  /**********************************************/
  /* At this point we have some data to process */
  /**********************************************/
  printf("Stream Queue is not empty");
  

  /*********************************************/
  /* get the first element on the list (queue) */
  /*********************************************/
  item = list_get_first_item(thread->queue);

  
  /****************************************************************/
  /* disconnect the first element (pop). Remember later to free it*/
  /****************************************************************/
  list_remove_first(thread->queue);


  /*****************************************/
  /* release the mutex and return the data */
  /*****************************************/
  pthread_mutex_unlock(&thread->queuemutex);

  return item;
}


/*******************************************************************
*******************************************************************/
unsigned short getconn_clnum()
{
  pthread_mutex_lock(&clcmutex);
  unsigned short value = clccount;
  pthread_mutex_unlock(&clcmutex);

  return value;
}


/***********************************************************************************
 * attend_client(void *arg):
 * -------------------------
 *
 * Thread for receive and manage new connections. Thread has a set of
 * possible states it can be on. This states are documented. Possible
 * states are: 
 * 
 * - B (Reached through: THREAD_STATE_WANTR, THREAD_STATE_WANTWR, THREAD_STATE_WAIT)
 *   This state is a blocking state. Thread is waiting for events. Events can be sent
 *   by the 'signal_client' thread, by the main thread, or by external read operations over
 *   the socket that corresponds to the thread client. Events sent by the 'signal_client' thread
 *   are write events. The 'signal_client' thread adds an item on the thread queue to be sent
 *   and after that it signals the thread with a write event (it tells the thread to go F).
 *   Events sent by the main thread are mostly managing thread events. A cancel thread 
 *   signal, a wait thread signal, etc. The remaining events are when the thread client 
 *   socket receives data from its client.
 *
 * - C (Reached through: THREAD_STATE_RH)
 *   This is the state where the thread try to reads a header from the connected client.
 *   If there were no header to read for, we go B, as we cannot do further operations
 *   and we need to wait for events to respond for. If there were some header, we proceed
 *   to D for trying to read the data corresponding that header.
 *
 * - D (Reached through: THREAD_STATE_RHOK)
 *   This state tries to read a data segment from the incoming stream. If we succeed, we
 *   continue to D. If we don't, we fall back to B state waiting for events.
 *
 * - E (Reached through: THREAD_STATE_RDOK)
 *   This state is reached when we successfully have read a complete stream from the client.
 *   The task here is to sent the stream to the 'signal_client' thread queue. Here, we go B
 *   if we don't have anything on our queue. Else, we go F state to process at least an item
 *   from our queue.
 *
 * - F (Reached through: THREAD_STATE_WR)
 *   This state sends a stream to the connected client. 
 *
 * - G (Reached through: THREAD_STATE_END or an external cancel signal)
 *   This state is reached whenever something went wrong. Every state could reach this
 *   one at any moment. After reaching this state, the thread will die by its owns means.
 ***********************************************************************************/
void *attend_client(void *arg) 
{  
  tdata_t   *thread = (tdata_t *) arg;  /* a thread data structure */

  int       fdmax   = MMAX(thread->event_reader, thread->client->fd);
  int       rv      = 0;                /* return value for some functions */
  int       nbytes  = 0;                /* nbytes = stream length          */

  byte_t    *stream = NULL;             /* stream = header + data     */
  byte_t    *data   = NULL;             /* data portion of 'stream'   */
  byte_t    *header = NULL;             /* header portion of 'stream' */

  fd_set    readfs;                     /* this will hold client->fd and event_reader */
  fd_set    copyfs;                     /* this is a required copy of 'readfs' */

  stream_item_t     *item = NULL;       /* queue item */

  /**********************************************/
  /* TODO: complete this constants or remove it */
  /**********************************************/
  const int NORMAL_EXIT = 0;
  const int CL_DISCONN  = 1;
  const int HNDSHK_FAIL = 2;


  /*********************************************/
  /* register cleanup function for this thread */
  /*********************************************/
  pthread_cleanup_push(attend_client_cleanup, thread);

  printf("attend_client fd: %d\n",thread->client->fd);
  /*************************/
  /* wait for a handshake. */
  /*************************/
  if( wait_for_client(thread->client) <= 0 )  
    {
      printf("<thread %d>[ERR]:\tcould not handshake with client.\n", thread->tnum);
      pthread_exit((void *)&HNDSHK_FAIL); //this will call cleanup function.
    }
  printf(NFO_ACCEPT, thread->tnum, inet_ntoa(thread->client->address.sin_addr), thread->client->fd);
  printf(NFO_CIPHER, thread->tnum, SSL_get_version(thread->client->ssl), SSL_get_cipher(thread->client->ssl));


  /*****************************************************************/
  /* adds 'event_reader' and 'fd' file descriptors to 'readfs' set */
  /*****************************************************************/
  FD_SET(thread->event_reader, &readfs);
  FD_SET(thread->client->fd, &readfs);

  /* increment the client count */
  //TODO:  clccountinc();


  /********************************/
  /* set the thread state to INIT */
  /********************************/
  set_tstate(thread, THREAD_STATE_INIT);

  printf("<thread %d>[NFO]:\tMain attendance loop start\n", thread->tnum);

  pthread_setcancelstate(PTHREAD_CANCEL_DISABLE, NULL);

  while( thread->tstate != THREAD_STATE_END )
    {
      copyfs = readfs;

      /********************************************************/
      /* First thing to do: Send our id to connected clients  */
      /* including this thread clietn.                        */
      /********************************************************/
      if ( thread->tstate == THREAD_STATE_INIT ) {

	/* get a list of connected clients */
	list_t *clist = get_connected_clients(thread);

	/* build the stream with CTL_SV_IDLIST control byte and 'clist' */
	stream = STREAM_build(CTL_SV_IDLIST, clist, &nbytes);

	if ( stream != NULL )
	  {  
	    printf("<thread %d>[NFO]:\tSending id list to connected clients\n", thread->tnum);

	    /* build an item from a stream */
	    item = build_stream_item(stream, nbytes, MSG_FROM_SERVER);

	    /* queue 'item' on thread SV_THREAD queue */
	    push_item(thread + SV_THREAD, item);

	    /* free the allocated memory */
	    free(stream);
	    nbytes = 0;
	      
	    set_tstate(thread, THREAD_STATE_RH);
	  }
	else {
	  /* if we cannot acomplish building the stream. Kill this thread */
	  set_tstate(thread, THREAD_STATE_END);
	  thread->error = THREAD_SOME_ERROR;
	}

	/* release unused memory from the connected client list */
	free_clist_node_memory(clist);
	free(clist);
      }


      /********************************************************/
      /* We enter C state if we need to read headers.         */
      /********************************************************/
      if ( thread->tstate == THREAD_STATE_RH )
	{
	  header = (byte_t *) try_read(thread, STREAM_HEADER_SIZE);
	  if (header != NULL)
	    set_tstate(thread, THREAD_STATE_RHOK);
	}


      /********************************************************/
      /* We go to D state after checks on control byte        */
      /********************************************************/
      if ( thread->tstate == THREAD_STATE_RHOK )
	{
	  if ( STREAM_valid_header(header) )
	    nbytes = STREAM_LENGTH(header);
      	  if ( nbytes <= 0 ) 
	    set_tstate(thread, THREAD_STATE_END);
      	  else
	    {
	      data = (byte_t *) try_read(thread, nbytes - STREAM_HEADER_SIZE);
	      if ( data != NULL )
		set_tstate(thread, THREAD_STATE_RDOK);
	    }
	}

      /* data is not allocated by me. Should I free it? */
      
      /********************************************************/
      /* If data read went ok, we go to E state and process   */
      /* the request to forward the data to other clients     */
      /********************************************************/
      if ( thread->tstate == THREAD_STATE_RDOK )
	{
	  /* build the whole stream : header + data */
      	  stream = (byte_t *) malloc(nbytes * sizeof(byte_t));
      	  memcpy(header, stream, STREAM_HEADER_SIZE);
      	  memcpy(data, stream + STREAM_HEADER_SIZE, nbytes - STREAM_HEADER_SIZE);

	  /* build a item from a stream */
	  item = build_stream_item(stream, nbytes, thread->tnum);
	  
	  /* queue 'item' on the signal_client thread queue and continue working */
	  push_item(&thread[SV_THREAD], item);

      	  /* 'stream', 'data' and 'header' should be freed now */
	  free(stream);
	  free(data);
	  free(header);

	  /* set if this thread should send data to its client */
	  if ( thread->shouldwrite )
	    set_tstate(thread, THREAD_STATE_WR);
	}


      /********************************************************/
      /* The B state is a locking state if we can't go furher */
      /* to any points. B controls where we need to send to or*/
      /* read from a client.                                  */
      /********************************************************/
      if ( thread->tstate == THREAD_STATE_WANTR  ||
	   thread->tstate == THREAD_STATE_WANTWR ||
	   thread->tstate == THREAD_STATE_WAIT  )
	{
	  pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);

	  /* this select will be signaled if needed to awake the thread */
	  /* to write or if a read from the net is detected*/
      	  rv = select(fdmax + 1, &copyfs, NULL, NULL, NULL);

	  pthread_setcancelstate(PTHREAD_CANCEL_DISABLE, NULL);
	  if ( rv < 0 ) {
	    /* Something on select() went wrong */
	    set_tstate(thread, THREAD_STATE_END);
	  }
	  else if ( FD_ISSET(thread->client->fd, &copyfs) ) {
	    /* if select() leaves with rv >= 0 and the client fd is set */
	    /* on 'copy', then we need to start a reading procedure and */
	    /* go to C state. */
	    set_tstate(thread, THREAD_STATE_RH);
	  }
	  else {
	    /* If none of the above condition are met, select() informs */
	    /* that event_reader was written. We received a 'write signal' */
	    /* so we need to write to the client. */
	    set_tstate(thread, THREAD_STATE_WR);
	  }

	  /*this works as a memory thing for when read finish (see E state).*/
	  thread->shouldwrite = FD_ISSET(thread->event_reader, &copyfs);
	}


      /********************************************************/
      /* This is when we need to send to this client some     */
      /* data. This F state should be reached if when thread  */
      /* data 'shouldwrite' flag is true and the client isn't */
      /* sending us something.                                */
      /********************************************************/
      if ( thread->tstate == THREAD_STATE_WR )
	try_write(thread);
      

    }
  printf("<thread %d>[NFO]:\tAttendace thread closing on %d socket.\n", thread->tnum, thread->client->fd);
  
  /****************************/
  /* pop the cleanup function */
  /****************************/
  pthread_cleanup_pop(1);


  /******************************************/
  /* this thread has no more reason to live */
  /******************************************/
  pthread_exit((void *)&NORMAL_EXIT);
}


/***************************/
/***************************/
void free_clist_node_memory(list_t *list)
{
  int         i = 0;
  list_node_t *tmpnode = NULL;
  
  for(i = list->count; i > 0; i = list->count) 
    {
      tmpnode = list_remove_first(list);
      free(((stream_item_t *)tmpnode->item)->data);
      free(tmpnode->item);
      free(tmpnode);
    }
}


/************************************
 ************************************/
void attend_client_cleanup(void *arg)
{
  tdata_t *thread = (tdata_t *) arg;

  printf("<thread %d>[NFO]:\tEntering thread clean up function\n", thread->tnum );  

  /********************************/
  /* release the client structure */
  /********************************/
  free_client(thread->client);


  /**********************************/
  /* release al items in the queue. */
  /* Let the queue pointer be freed */
  /* by the main thread             */
  /**********************************/
  free_thread_queue(thread);


  /**************************/
  /* close file descriptors */
  /**************************/
  close(thread->event_reader);
  close(thread->event_writer);
  

  /*****************************/
  /* free thread's queue error */
  /*****************************/
  ERR_remove_state(pthread_self());

  
  /**************************/
  /* set this state to free */
  /**************************/
  set_tstate(thread, THREAD_STATE_FREE);

}


/*************************************/
/*************************************/
void free_thread_queue(tdata_t *thread)
{
  pthread_mutex_lock(&thread->queuemutex);
  list_free(thread->queue);
  pthread_mutex_unlock(&thread->queuemutex);
}


/*******************************************************
 * returns a list of connected clients with the 'thread'
 * client at the beginning
 ******************************************************/
list_t *get_connected_clients(tdata_t *thread) 
{
  int       i      = 0;                       /* iterator variable */
  int       number[MAX_CLIENTS];              /* we are adding integers to the list, so we need a new object for every node */

  tdata_t   *tmp_thread = NULL;               /* iterator variable */

  list_t    *clist = list_make_empty(clist);  /* the list that will be returned */


  /**********************************************************************************/
  /* Sets 'tmp_thread' to point to the first thread and goes forward until 'thread' */
  /* position, putting on 'clist' the ids of the connected clients.                 */
  /**********************************************************************************/
  for(tmp_thread = thread - thread->tnum; tmp_thread != thread; thread++)
    {
      pthread_mutex_lock(&tmp_thread->stmutex);
      if ( tmp_thread->tstate != THREAD_STATE_NEW  && tmp_thread->tstate != THREAD_STATE_FREE && tmp_thread->tstate != THREAD_STATE_END) 
	{
	  number[tmp_thread->tnum] = tmp_thread->tnum;
	  list_add_last_item(clist, &number[tmp_thread->tnum]);
	}
      pthread_mutex_unlock(&tmp_thread->stmutex);
    }

  /***************************************************/
  /* Now, 'tmp_thread' is equals 'thread' so we put  */
  /*'thread' client id at the beguinning of 'clist'. */
  /***************************************************/
  number[tmp_thread->tnum] = tmp_thread->tnum;
  list_add_first_item(clist, &number[tmp_thread->tnum]);


  /*****************************************************************/
  /* If 'thread' was not the last thread, we need to move one and  */
  /* continue adding the client ids until we reach the last thread */
  /*****************************************************************/
  if ( tmp_thread->tnum != MAX_CLIENTS - 1 )
    {
      /* move one as 'tmp_thread' have been treated above */
      tmp_thread++;

      for(i = tmp_thread->tnum; i < MAX_CLIENTS; i++)
	{
	  pthread_mutex_lock(&tmp_thread[i].stmutex);
	  if ( tmp_thread[i].tstate != THREAD_STATE_NEW  && tmp_thread[i].tstate != THREAD_STATE_FREE && tmp_thread[i].tstate != THREAD_STATE_END) 
	    {
	      number[i] = i;
	      list_add_last_item(clist, &number[i]);
	    }
	  pthread_mutex_unlock(&tmp_thread[i].stmutex);
	}
    }
  
  /* if 'tmp_thread' was the last thread, then we are done. Return the list. */
  return clist;
}


/*******************************************************
 * helper function to set the thread state using mutexes
 ******************************************************/
void set_tstate(tdata_t *thread, thread_state_t state)
{
  pthread_mutex_lock(&thread->stmutex);
  thread->tstate = state;
  pthread_mutex_unlock(&thread->stmutex);
}



/**************************************
 * wait_for_client(client_t *client):
 * ---------------
 **************************************/
int wait_for_client(client_t *client)
{
  printf("ENTERING wait_for_client() function.\n");
  int            i        = 0;
  int            seq      = 0;                /* next sequence on pseq        */
  int            ec       = 0;                /* error code                   */
  int            clpbytes  = 0;               /* number of bytes read         */
  int            svpbytes  = 0;               /* number of bytes of ostream   */
  byte_t         *svpacket = NULL;            /* stream from the server       */
  byte_t         *clpacket = NULL;            /* stream from the client       */
  //  byte_t         istream[BUFFER_SIZE];       /* buffer to hold read data     */

  struct timeval tv;
  psequence_t    *pseq = NULL;
  int (*checkers [])(void *arg) = {
    check_pwd,
    check_dummy,
    check_dummy,
    check_last
  };

  /* allocates memory to hold incoming data */
  clpacket = (byte_t *) malloc(sizeof(byte_t) * BUFFER_SIZE);

  pseq = PSEQ_build(PSEQ_HNDSHK, checkers);
  PSEQ_add_pbargs(pseq, PSEQ_CONN_0, (void *) &client->fd);
  PSEQ_add_pbargs(pseq, PSEQ_CONN_1, (void *) &client->fd);
  PSEQ_add_pbargs(pseq, PSEQ_CONN_2, (void *) &client->fd);
  PSEQ_add_pbargs(pseq, PSEQ_CONN_2, (void *) list_get_item(args.currimg));

  tv.tv_sec  = 5;
  tv.tv_usec = 0;
  ec         = ssl_accept_b(client, tv);

  if ( ec == SSL_ERROR_NONE )
    {
      printf("ssl_accept() without errors.\n");
      seq = PSEQ_start_sequence(pseq, PSEQ_SV, &svpacket, &svpbytes);
      int ki;
      for( ki = 0; ki < svpbytes; ki++)
	{
	  printf(" %d ", svpacket[ki]);
	}
      printf("\nSEQUENCE: %d  -  bytes: %d\n",seq, svpbytes);
      do
  	{
  	  if ( ssl_write_b(client, &ec, svpacket, svpbytes) <= 0)
	    {
	      printf("ssl_write() error");
	      return 0;
	    }
  	  if ( (clpbytes = ssl_read_b(client, &ec, clpacket, BUFFER_SIZE, &tv)) <= 0 )
	    {
	      printf("ssl_read() error");
	      return 0;
	    }

	  if ( !STREAM_is_stream(svpacket, svpbytes) ) return 0; /*TODO: free memory used */
	  if ( !STREAM_is_stream(clpacket, clpbytes) ) return 0;

	  printf("\n----------------------------------------------------------------------\n");
	  printf("\nSent:\n"); 
	  printf("------\n"); 
	  int ki;
	  for( ki = 0; ki < svpbytes; ki++)
	    {
	      printf(" %d ", svpacket[ki]);
	    }
	  int k;
	  printf("\nReceived:\n"); 
	  printf("---------\n"); 
	  for( k = 0; k < clpbytes; k++)
	    {
	      printf(" %d ", clpacket[k]);
	    }
	  printf("\n----------------------------------------------------------------------\n");
	  /* we can feed with streams that are really streams */
  	  seq = PSEQ_next_sequence(seq, pseq, PSEQ_SV, clpacket, &svpacket, &svpbytes);
  	}
      while( seq >= 0 && seq < PSEQ_HNDSHK_SIZE );
      //TODO: CLEAN UP LEAKS FROM LIST AND SO ON...PSEQ[0].pcargs PSEQ[i].pbargs
      free(svpacket);
      PSEQ_cleanup(PSEQ_HNDSHK, pseq);
      free(pseq);
      return seq;
    }
  else
    return 0;
  return PSEQ_HNDSHK_SIZE;
}


/*********************************************************************************
 *
 *********************************************************************************/
int ssl_read_b(client_t *cl, int *err, void *buf, size_t size, struct timeval *tv)
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
      nbytes = SSL_read(cl->ssl, buf, size);
      *err   = SSL_get_error(cl->ssl, nbytes);
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
TODO: think about making a function that tries to read 'size' bytes. If any error happens, will return null.
TODO2: think about inform the client that read was unsuccessful.
 ***************************************************/
void *try_read(tdata_t *thread, int size)
{
  int    totalbytes = size;
  int    readbytes  = 0;
  int    rc    = 0;

  /* we allocate the required memory for this data segment */
  byte_t *data  = (byte_t *) malloc(sizeof(byte_t)*size);

  fd_set master;
  fd_set copy;

  struct timeval tv;
  tv.tv_sec = 30;
  
  /* Initialize 'master' and 'copy' to zero values */
  FD_ZERO(&master);
  FD_ZERO(&copy);

  /* add 'cl->fd' and 'thread_data[cl->clidx].event_reader' to 'master' set*/
  FD_SET(thread->client->fd, &master);
  FD_SET(thread->event_reader, &master);

  do
    {
      /* we try to read 'size' bytes into 'data' memory */
      readbytes = SSL_read(thread->client->ssl, data, totalbytes);
      rc        = SSL_get_error(thread->client->ssl, readbytes);

      if ( rc == SSL_ERROR_NONE )
	{
	  totalbytes -= readbytes;
	  data += readbytes;
	}
      else
	{
	  /* if one of these condition are met, return null */
	  if ( rc == SSL_ERROR_SYSCALL || rc == SSL_ERROR_ZERO_RETURN || rc == SSL_ERROR_SSL ) 
	    {
	      /* this thread is going to end */
	      set_tstate(thread, THREAD_STATE_END);
	      free(data);
	      return NULL;
	    }
	  else
	    {
	      rc = select(thread->client->fd + 1, &copy, NULL, NULL, &tv);
	      if ( rc <= 0 )
		{
		  /* do we inform the client here? */
		  set_tstate(thread, THREAD_STATE_WAIT);
		  return NULL;
		}
	      copy = master;
	    }
	}
    }
  while(totalbytes > 0);

  return data;
}


/*************************************************
 * try_write(tdata_t *thread):
 * --------------------------
 *
 * Attempts to send the first item on the 'thread'
 * thread queue.
 *************************************************/
int try_write(tdata_t *thread)
{
  int            rc     = 0;        /* return code for select()       */
  int            ec     = 0;        /* error code for SSL_get_error() */
  int            bwrote = 0;        /* bytes wrote by SSL_write() if successful */

  stream_item_t  *item = NULL;      /* an item queue */

  fd_set         writefs;           /* write file descriptor set */
  fd_set         copy;              /* a copy of writefs. Required when select() modifies the set */

  struct timeval tv;                /* timeval structure for select() */

  /* sets a timeout of 5 seconds */
  tv.tv_sec = 5;

  /* initialize the fd_set variables */
  FD_ZERO(&writefs);
  FD_ZERO(&copy);

  /* inserts 'thread->client->fd' on 'writefs' set */
  FD_SET(thread->client->fd, &writefs);

  /* we got the item poped out from the queue */
  item = (stream_item_t *) pop_item(thread);

  do
    {
      bwrote = SSL_write(thread->client->ssl, item->data, item->nbytes);
      ec = SSL_get_error(thread->client->ssl, bwrote);

      if ( ec == SSL_ERROR_NONE)
	{
	  /* if write was successful, 'bwrote' bytes were sent. */
	  /* We update the bytes quantity we need to send and   */
	  /* move the pointer forward to match that data.       */
	  item->nbytes -= bwrote;
	  item->data   += bwrote;
	}
      else
	{
	  if ( ec == SSL_ERROR_ZERO_RETURN || ec == SSL_ERROR_SYSCALL || ec == SSL_ERROR_SSL )
	    {
	      /* Fatal errors ends with the thread */
	      set_tstate(thread, THREAD_STATE_END);
	      return 0;
	    }
	  else
	    {
	      /* if we got a renegotiation we'll continue writting. */
	      /* if we got a timeout, return 0.                     */
	      rc = select(thread->client->fd + 1, &copy, NULL, NULL, &tv);
	      if ( rc <= 0 )
		{
		  /* TODO: do we inform the client here that write was unsuccessful? */
		  set_tstate(thread, THREAD_STATE_WAIT);
		  return 0;
		}
	      copy = writefs;
	    }
	}
    }
  while(item->nbytes > 0);
}


/*********************************************************************************
 *********************************************************************************/
int ssl_write_b(client_t *cl, int *err, const void *buf, size_t size)
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


/****************************************************
 *
/****************************************************/
void data_init(tdata_t *thread_data)
{
  int i = 0;

  /* Initialize thread structure */
  for( i = 0; i < MAX_CLIENTS + 1; i++)
    {
      data_init_thread(&thread_data[i], i);
      //data_init_client(&thread_data[i]);
      data_init_pipes(&thread_data[i]);
    }
}


/****************************************************
 *
/****************************************************/
void data_init_thread(tdata_t *thread, int i)
{
  thread->client = NULL; //(client_t *) malloc(sizeof(client_t));
  thread->tid    = (pthread_t) 0;
  thread->tnum   = i;
  thread->tstate = THREAD_STATE_NEW;

  /* This is memory allocated by me. Should be freed later */
  thread->queue  = list_make_empty(thread->queue);

  /* review this function */
  THREAD_get_error(pthread_mutex_init(&thread->queuemutex, NULL), FATAL, i);
  THREAD_get_error(pthread_mutex_init(&thread->stmutex, NULL), FATAL, i);
  THREAD_get_error(pthread_cond_init(&thread->queuecond, NULL), FATAL, i);
}


/****************************************************
 *
/****************************************************
void data_init_client(tdata_t *thread)
{
  thread->client->fd     = -1;
  thread->client->ssl    = NULL;
  thread->client->clinfo = NULL;
  thread->client->clidx  = thread->tnum;
  thread->client->fwdbitlist = 0;
}
*/


/****************************************************
 *
/****************************************************/
void data_init_pipes(tdata_t *thread)
{
  int j = 0;

  pipe(thread->pipefd);
  for( j = 0; j < 2; j++)
    if ( setnonblock(thread->pipefd[j]) == -1)
      {
	perror("setnonblock()");
	exit(1);
      }
  thread->event_reader = thread->pipefd[0];
  thread->event_writer = thread->pipefd[1];
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
 * free_client():
 * -----------
 * free used resources by a client
 ***********************************/
void free_client(client_t *client)
{
  /* release ssl structure */
  SSL_free(client->ssl);

  /* close file descriptor */
  close(client->fd);

  /* NULL assignment */ 
  client->ssl = NULL;

  /* reset */
  client->fwdbitlist = 0;

  /* free it */
  free(client);

  /* NULL assignment */
  client = NULL;
}


/************************************
************************************/
int check_pwd(void *arg)
{
  /* arg is a byte_t pointer and should be verified before with STREAM_is_stream() */
  static int    pwdf = 0;
  const byte_t  *pwd = STREAM_get_pwd((byte_t *) arg);

  if ( memcmp(args.pwd, pwd, STREAM_PWD_SIZE) )
    {
      pwdf++;
      if ( pwdf == MAX_PWD_TRIES )
	return SEQ_CANT_CONTINUE;
      else
	return SEQ_NEEDS_RETRY;
    }
  else
    return SEQ_OK;
}


/************************************
************************************/
int check_dummy(void *arg)
{
  return SEQ_OK;
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

  
  /*****************************/
  /* TODO: DOUBLE CHECK THIS! */
  /*****************************/
  while( list_is_empty(args.imglist) == false)
    {
      list_node_t *tmp = list_remove_first(args.imglist);
      fclose((FILE *) tmp->item);
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

/******************************/
/* TODO: REVIEW THIS FUNCTION */
/******************************/
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
  args.currimg = list_get_head(args.imglist);
}



/**************************************************************************
 * int new_client(tdata_t *thread, int clfd, struct sockaddr_in clientaddr)
 * ------------------------------------------------------------------------
 *
 * It allocates memory on 'thread' to hold a client structure. Then, init
 * all data to build a client node with an ssl connection.
 *
 * Returns 1 if successful and 0 if not.
 **************************************************************************/
int new_client(tdata_t *thread, int clfd, struct sockaddr_in clientaddr)
{
  thread->client = (client_t *) malloc(sizeof(client_t));

  if ( thread->client == NULL ) return 0;

  thread->client->clidx   = thread->tnum;
  thread->client->fd      = clfd;
  thread->client->address = clientaddr;
  thread->client->ssl     = SSL_new(ctx);
  
  /* set to NON_BLOCKING */
  if ( setnonblock(clfd) == -1) return 0;

  /* The calling function should be aware to release 'thread->client' */
  /* if the condition below is not met                                */
  return (thread->client->ssl != NULL && SSL_set_fd(thread->client->ssl, thread->client->fd));
}
