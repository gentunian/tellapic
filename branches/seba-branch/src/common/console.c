#include <pthread.h>

#include "console.h"


static int myCB (EObjectType cdktype GCC_UNUSED, void *object, void *clientData, chtype key GCC_UNUSED)
{
   CDKSWINDOW *swindow	= (CDKSWINDOW *)clientData;
   CDKENTRY *entry	= (CDKENTRY *)object;

   /* Let them play... */
   activateCDKSwindow (swindow, 0);

   /* Redraw the entry field. */
   drawCDKEntry (entry, ObjOf(entry)->box);
   return (FALSE);
}


void reader_cleanup(void *arg) {
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


void *reader(void *arg) {
  console_t *console = (console_t *) arg;
  int       input = fileno(stdin);
  int       fdmax = console->scrollwindow->infd + 1;
  fd_set    set;
  char      buff[256];
  int       cont = 1;

  noecho();
  pthread_cleanup_push(reader_cleanup, console);              /* register cleanup function for this thread */
  while(cont) {
    FD_ZERO(&set);
    FD_SET(input, &set);
    FD_SET(console->scrollwindow->infd, &set);

    int rv = select(fdmax, &set, NULL, NULL, NULL);

    if (rv < 0) {
      cont = 0;
    } else {
      if (FD_ISSET(input, &set)) {
	boolean f;
	chtype c = getchCDKObject(ObjOf(console->entry), &f);
	char *cmd = injectCDKEntry(console->entry, c);
	if (cmd != NULL && strlen(cmd) > 0) {
	  write(console->outfd, cmd, strlen(cmd));    /* send the input to the console user */
	  pthread_mutex_lock(&console->mutex);
	  cleanCDKEntry(console->entry);
	  refreshCDKScreen (console->screen);
	  pthread_mutex_unlock(&console->mutex);
	}
      }

      if (FD_ISSET(console->scrollwindow->infd, &set)) {
	read(console->scrollwindow->infd, buff, 256);
	pthread_mutex_lock(&console->mutex);
	addCDKSwindow(console->scrollwindow->swindow, buff, BOTTOM);
	refreshCDKScreen (console->screen);
	pthread_mutex_unlock(&console->mutex);
	memset(buff, 0, 256);
      }
    }

  }
  pthread_cleanup_pop(1);                             /* pop the cleanup function */
  pthread_exit(NULL);
}


void console_destroy(console_t *console) {
  close(console->scrollwindow->infd);
}


console_t *console_create(char *title, char *prompt) {

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
  if (console->scrollwindow == NULL) {
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
  /* Set up CDK Colors. */
  initCDKColor ();
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

  if (console->scrollwindow->swindow == NULL) { 
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

  pthread_t tid;
  pthread_create(&tid, NULL, reader, console);
  console->state = CONSOLE_ENABLED;
  return console;
}


int console_isenabled(console_t *console) {
  //if (console == NULL)
  //return 0;

  return (console->state == CONSOLE_ENABLED);
}


int console_disable(console_t *console) {
  if (console == NULL && console->state == CONSOLE_ENABLED)
    return 0;

  console->state = CONSOLE_DISABLED;
  def_prog_mode();
  endwin();
  return 1;
}


int console_enable(console_t *console) {
  if (console == NULL && console->state == CONSOLE_DISABLED)
    return 0;

  console->state = CONSOLE_ENABLED;
  reset_prog_mode();
  refresh();
  return 1;
}
