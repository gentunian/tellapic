#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>

#include "common.h"


int setnonblock(int fd) {
  int flag = fcntl(fd, F_GETFL);
  if ( flag != -1 )
    return fcntl(fd, F_SETFL, flag | O_NONBLOCK);
  else {
    perror("setnonblock()");
    return flag;
  }
}

/*
unsigned char *hexastr2binary(unsigned char *sha1hexadigest) {  
  unsigned char *output = (unsigned char *) malloc(sizeof(unsigned char) * SHA_DIGEST_LENGTH );
  int i = 0;

  for(i = SHA_DIGEST_LENGTH - 1; i >= 0; i--) {
    output[i]  = htoi(sha1hexadigest[i * 2 + 1]);
    output[i] |= htoi(sha1hexadigest[i * 2])<<4;
  }  
  return output;
}
*/

int htoi(unsigned char hexdigit) {
  char hex[16] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
  int i = 0;
  for(i = 0; i < 16 && hex[i] != hexdigit; i++);
  if( i == 16 )
    return -1;
  else
    return i;
}
