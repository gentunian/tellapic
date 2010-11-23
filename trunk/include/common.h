#ifndef _COMMON_H_
#define _COMMON_H_

#define MMAX(a, b) a > b? a:b;

int                   setnonblock(int fd);
unsigned char         *hexastr2binary(unsigned char *sha1hexadigest);
int                   htoi(unsigned char hexdigit);


#endif
