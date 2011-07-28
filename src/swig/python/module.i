%include "../tellapic.i"
%module "pytellapic"

%typemap(out) tellapic_float {
#ifdef BIG_ENDIAN_VALUE
  $result = PyFloat_FromDouble(POSH_FloatFromBigBits($1));
#else
  $result = PyFloat_FromDouble(POSH_FloatFromLittleBits($1));
#endif
 }

%typemap(in) tellapic_float {
#ifdef BIH_ENDIAN_VALUE
  $1 = POSH_BigFloatBits(PyFloat_AsDouble($input));
#else
  $1 = POSH_LittleFloatBits(PyFloat_AsDouble($input));
#endif
 }

%typemap(in) byte_t[MAX_FONTFACE_LEN] {
  $1 = (byte_t *)PyString_AsString($input);
 }

%typemap(out) byte_t[MAX_FONTFACE_LEN] {
  char * str = (char *)$1;
  $result = PyString_FromString(str);
 }

%typemap(in) byte_t[MAX_TEXT_SIZE] {
  $1 = (byte_t *)PyString_AsString($input);
 }

%typemap(out) byte_t[MAX_TEXT_SIZE] {
  char * str = (char *)$1;
  $result = PyString_FromString(str);
 }

%inline %{
  PyObject * wrapToFile(byte_t *rawbytes, unsigned long size)
  {
    char *name = "pytellapic.img";
    FILE *f = fopen("pytellapic.img", "w");
    fwrite(rawbytes, size, 1, f);
    return PyString_FromString(name);
  }
  %}

%include "posh/posh.h"
%include "tellapic/tellapic.h"
%include "tellapic/types.h"
%include "tellapic/constants.h"

