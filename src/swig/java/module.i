%include "../tellapic.i"
%include "arrays_java.i"

%javaconst(1);
%javaconstvalue("new java.math.BigInteger(\"4294967296\")") MAX_STREAM_SIZE;

%typemap(jni) byte_t[MAX_TEXT_SIZE] "jstring"
%typemap(jtype) byte_t[MAX_TEXT_SIZE] "String"
%typemap(jstype) byte_t[MAX_TEXT_SIZE] "String"
%typemap(in) byte_t[MAX_TEXT_SIZE] {
  const byte_t *str = (byte_t *) JCALL2(GetStringUTFChars, jenv, $input, 0);
  $1 = str;
 }
%typemap(argout) byte_t[MAX_TEXT_SIZE] ""
%typemap(out) byte_t[MAX_TEXT_SIZE] {
  $result = JCALL1(NewStringUTF, jenv, (const char *)$1);
 }
%typemap(freearg) byte_t[MAX_TEXT_SIZE] {
  JCALL2(ReleaseStringChars, jenv, $input, (const jchar *)$1);
 }

%typemap(jni) byte_t[MAX_FONTFACE_LEN] "jstring"
%typemap(jtype) byte_t[MAX_FONTFACE_LEN] "String"
%typemap(jstype) byte_t[MAX_FONTFACE_LEN] "String"
%typemap(in) byte_t[MAX_FONTFACE_LEN] {
  const byte_t *str = (byte_t *) JCALL2(GetStringUTFChars, jenv, $input, 0);
  $1 = str;
 }
%typemap(argout) byte_t[MAX_FONTFACE_LEN] ""
%typemap(out) byte_t[MAX_FONTFACE_LEN] {
  $result = JCALL1(NewStringUTF, jenv, (const char *)$1);
 }
%typemap(freearg) byte_t[MAX_FONTFACE_LEN] {
  JCALL2(ReleaseStringChars, jenv, $input, (const jchar *)$1);
 }

%typemap(jni) tellapic_float[2] "jfloatArray"
%typemap(jtype) tellapic_float[2] "float[]"
%typemap(jstype) tellapic_float[2] "float[]"
%typemap(out) tellapic_float[2] {
#ifdef BIG_ENDIAN_VALUE
  float f[2];
  jfloatArray a = (jfloatArray) JCALL1(NewFloatArray,jenv, 2);
  f[0] = POSH_FloatFromBigBits($1[0]);
  f[1] = POSH_FloatFromBigBits($1[1]);
  JCALL4(SetFloatArrayRegion, jenv, a, 0, 2, (jfloat *) f);
  $result = a;
#else
  float f[2];
  jfloatArray a = (jfloatArray) JCALL1(NewFloatArray,jenv, 2);
  f[0] = POSH_FloatFromLittleBits($1[0]);
  f[1] = POSH_FloatFromLittleBits($1[1]);
  JCALL4(SetFloatArrayRegion, jenv, a, 0, 2, (jfloat *) f);
  $result = a;
#endif
 }

%typemap(in) tellapic_float[2] {
#ifdef BIG_ENDIAN_VALUE
  tellapic_u32_t f[2];
  float *pointer =  (float *) JCALL2(GetFloatArrayElements, jenv, $input, 0);
  f[0] = POSH_BigFloatBits(pointer[0]);
  f[1] = POSH_BigFloatBits(pointer[1]);
  $1 = f;
#else
  tellapic_u32_t f[2];
  float *pointer =  (float *) JCALL2(GetFloatArrayElements, jenv, $input, 0);
  f[0] = POSH_LittleFloatBits(pointer[0]);
  f[1] = POSH_LittleFloatBits(pointer[1]);
  $1 = f;
#endif
 }

%typemap(argout) tellapic_float[2] {
    JCALL3(ReleaseFloatArrayElements, jenv, $input, (jfloat *) $1, 0); 
}


%typemap(jni) tellapic_float "jfloat"
%typemap(jtype) tellapic_float "float"
%typemap(jstype) tellapic_float "float"

%typemap(out) tellapic_float {
#ifdef BIG_ENDIAN_VALUE
  $result = POSH_FloatFromBigBits($1);
#else
  $result = POSH_FloatFromLittleBits($1);
#endif
 }

%typemap(in) tellapic_float {
#ifdef BIH_ENDIAN_VALUE
  $1 = POSH_BigFloatBits($input);
#else
  $1 = POSH_LittleFloatBits($input);
#endif
 }


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
    for(i = 0; i < size; i++)
      BYTE[i] = rawfile[i];
  }
  %}

%include "posh/posh.h"
%include "tellapic/types.h"
%include "tellapic/constants.h"
%include "tellapic/tellapic.h"
%pragma(java) jniclassclassmodifiers="class"
