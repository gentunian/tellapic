%include "../tellapic.i"
%include "arrays_java.i"

%javaconst(1);
%javaconstvalue("new java.math.BigInteger(\"4294967296\")") MAX_STREAM_SIZE;
%typemap(jni) char *BYTE "jbyteArray"
%typemap(jtype) char *BYTE "byte[]"
%typemap(jstype) char *BYTE "byte[]"
%typemap(in) char *BYTE {
    $1 = (char *) JCALL2(GetByteArrayElements, jenv, $input, 0); 
}

%typemap(argout) char *BYTE {
    JCALL3(ReleaseByteArrayElements, jenv, $input, (jbyte *) $1, 0); 
}

%typemap(javain) char *BYTE "$javainput"

/* Prevent default freearg typemap from being used */
%typemap(freearg) char *BYTE ""

%inline %{
  void custom_wrap(byte_t *rawfile, char *BYTE, unsigned long size)
  {
    int i;
    int j;
    /*
    for(j = 0; j < 10; j++) 
      {
	printf("byte %d = %c\n", j, rawfile[j]);
	for(i = 0; i < 8; i++)
	  printf("bit %d = %d\n", i, (rawfile[j]>>i) & 1);

      }
    */

    for(i = 0; i < size; i++)
      BYTE[i] = rawfile[i];

  }
  %}

%include "types.h"
%include "constants.h"
%include "tellapic.h"
%pragma(java) jniclassclassmodifiers="class"
