/**
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
 */

#include <pthread.h>
#include "console.h"


/**
 * Taken from cdk demos and examples.
 */
static
int myCB (EObjectType cdktype GCC_UNUSED, void *object, void *clientData, chtype key GCC_UNUSED)
{
   CDKSWINDOW *swindow	= (CDKSWINDOW *)clientData;
   CDKENTRY *entry	= (CDKENTRY *)object;

   /* Let them play... */
   activateCDKSwindow (swindow, 0);

   /* Redraw the entry field. */
   drawCDKEntry (entry, ObjOf(entry)->box);
   return (FALSE);
}


/**
 *
 */
void
reader_cleanup(void *arg) 
{
  console_t *console = (console_t *) arg;

  pthread_mutex_destroy(&console->mutex);
  console->state = CONSOLE_DISABLED;
  freeChar(console->prompt);
  freeChar(console->scrollwindow->title);
  //close(console->scrollwindow->infd);
  close(console->scrollwindow->outfd);
  close(console->infd);
  close(console->outfd);
  destroyCDKSwindow(console->scrollwindow->swindow);
  destroyCDKEntry(console->entry);
  destroyCDKScreen (console->screen);
  endCDK ();
  delwin(console->mwindow);
  endwin();
  free(console->scrollwindow);
  free(console);
  console = NULL;
}


/**
 *
 */
void
*reader(void *arg) 
{
  console_t *console = (console_t *) arg;              /* the console structure. */
  int       input = fileno(stdin);                     /* standard input file descriptor. */
  int       fdmax = console->scrollwindow->infd + 1;   /* maximun fd number for select() call. */
  fd_set    set;                                       /* the set of file descriptors. */
  char      buff[256];                                 /* a message buffer. */
  int       cont = 1;                                  /* the main loop variable. */

  noecho();
  pthread_cleanup_push(reader_cleanup, console);       /* register cleanup function for this thread. */

  int cmdlen = 0;
  /* Do work until we are told to stop */
  while(cont) 
    {
      /* Initialize and add standar input file descriptor and scroll window file descriptor to the set. */
      FD_ZERO(&set);
      FD_SET(input, &set);
      FD_SET(console->scrollwindow->infd, &set);
      
      int rv = select(fdmax, &set, NULL, NULL, NULL);
      
      if (rv < 0) 
	{
	  /* Error found on select() call. Destroy the console, treat it as fatal. */
	  cont = 0;
	} 
      else 
	{
	  /* Was the input from standard input? If so, read what the user is typing... */
	  if (FD_ISSET(input, &set)) 
	    {
	      boolean f;
	      /* Get a single character and echoed back to the command-line entry. If cmd is null */
	      /* then send it back to the client of this console.                                 */
	      chtype c = getchCDKObject(ObjOf(console->entry), &f);
	      char *cmd = injectCDKEntry(console->entry, c);
	      
	      char b[134];

	      if (cmd != NULL && cmdlen > 0) 
		{
		  char *str = malloc(cmdlen+1);
		  strncpy(str, cmd, cmdlen);
		  str[cmdlen] = '\0';
		  write(console->outfd, str, cmdlen+1);    /* forward the data input to the console client pipe. */
		  cmdlen = 0;
		  pthread_mutex_lock(&console->mutex);
		  cleanCDKEntry(console->entry);
		  refreshCDKScreen (console->screen);
		  pthread_mutex_unlock(&console->mutex);
		  free(str);
		}
	      else
		{
		  cmdlen++;
		}
	    }
	  
	  /* Was the input a message to be printed in the scroll window? */
	  if (FD_ISSET(console->scrollwindow->infd, &set)) 
	    {
	      read(console->scrollwindow->infd, buff, 256);  /* read what the client sent to us and echoed back. */
	      pthread_mutex_lock(&console->mutex);
	      addCDKSwindow(console->scrollwindow->swindow, buff, BOTTOM);
	      refreshCDKScreen (console->screen);
	      pthread_mutex_unlock(&console->mutex);
	      memset(buff, 0, 256);
	    }
	}
    }

  pthread_cleanup_pop(1);                             /* pop the cleanup function. */
  pthread_exit(NULL);
}


/**
 *
 */
void
console_destroy(console_t *console) 
{
  close(console->scrollwindow->infd);
}


/**
 *
 */
console_t *
console_create(char *title, char *prompt) 
{

  int consolepipe[2];
  if (pipe(consolepipe) == -1)
    return NULL;
  
  int swindowpipe[2];
  if (pipe(swindowpipe) == -1)
    return NULL;

  console_t *console = malloc(sizeof(console_t));
  if (console == NULL)
    return NULL;

  console->infd  = consolepipe[0];
  console->outfd = consolepipe[1];
  console->scrollwindow = malloc(sizeof(swindow_t));
  if (console->scrollwindow == NULL) 
    {
      close(console->infd);
      close(console->outfd);
      free(console);
      return NULL;
    }
  
  int bla, rows, cols;
  chtype *s = char2Chtype(prompt, &console->promptlen, &bla);
  freeChtype(s);
  console->prompt = copyChar(prompt);
  console->cmdlen = 0;
  console->scrollwindow->infd  = swindowpipe[0];
  console->scrollwindow->outfd = swindowpipe[1];
  console->scrollwindow->title = copyChar(title);
  console->mwindow = initscr ();
  pthread_mutex_init(&console->mutex, NULL);
  getmaxyx(stdscr, rows, cols);
  console->screen = initCDKScreen (console->mwindow);

  initCDKColor();
  keypad(stdscr, TRUE);                   /* enable keyboard mapping */
  nonl();                                 /* tell curses not to do NL->CR/NL on output */
  cbreak();                               /* take input chars one at a time, no wait for \n */

  /* Create the scrolling window. */
  console->scrollwindow->swindow = newCDKSwindow (console->screen,
						 CENTER,
						 TOP,
						  -8,
						  -2,
						  console->scrollwindow->title,
						  2048,
						  TRUE,
						  FALSE);

  /* Realease all memory */
  if (console->scrollwindow->swindow == NULL) 
    { 
      pthread_mutex_destroy(&console->mutex);
      freeChar(console->prompt);
      freeChar(console->scrollwindow->title);
      close(console->scrollwindow->infd);
      close(console->scrollwindow->outfd);
      close(console->infd);
      close(console->outfd);
      destroyCDKScreen (console->screen);
      endCDK ();
      endwin();
      free(console->scrollwindow);
      free(console);
      
      printf ("Oops. Could not make scrolling list. Is the window too small?\n");
      return NULL;
    }

  console->entry = newCDKEntry(console->screen,
			       LEFT,
			       BOTTOM,
			       NULL,
			       console->prompt,
			       A_BOLD|COLOR_PAIR(8),
			       COLOR_PAIR(24)|' ',
			       vMIXED,
			       cols - console->promptlen - 2,
			       1,
			       256,
			       FALSE,
			       FALSE);

  bindCDKObject (vENTRY, console->entry, KEY_TAB, myCB, console->scrollwindow->swindow);
  refreshCDKScreen(console->screen);

  /* Launch the reader thread. */
  pthread_t tid;
  pthread_create(&tid, NULL, reader, console);
  console->state = CONSOLE_ENABLED;

  return console;
}


/**
 *
 */
int
console_isenabled(console_t *console) 
{
  return (console->state == CONSOLE_ENABLED);
}


/**
 *
 */
int
console_disable(console_t *console) 
{
  if (console == NULL && console->state == CONSOLE_ENABLED)
    return 0;

  console->state = CONSOLE_DISABLED;
  def_prog_mode();
  endwin();
  return 1;
}


/**
 *
 */
int
console_enable(console_t *console) 
{
  if (console == NULL && console->state == CONSOLE_DISABLED)
    return 0;

  console->state = CONSOLE_ENABLED;
  reset_prog_mode();
  refresh();
  return 1;
}
