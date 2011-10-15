%include "../tellapic.i"
%module "pytellapic"

%{
  static int convert_darray(PyObject *input, tellapic_float *ptr, int size) {
    int i;
    if (!PySequence_Check(input)) 
      {
	PyErr_SetString(PyExc_TypeError,"Expecting a sequence");
	return 0;
      }
    if (PyObject_Length(input) != size) 
      {
	PyErr_SetString(PyExc_ValueError,"Sequence size mismatch");
	return 0;
      }
    for (i =0; i < size; i++) 
      {
	PyObject *o = PySequence_GetItem(input,i);
	float v = 0;
	if (PyFloat_Check(o))
	  v = PyFloat_AsDouble(o);
	else if (PyInt_Check(o))
	  v = (float)PyInt_AsLong(o);
	else
	  {
	    Py_XDECREF(o);
	    PyErr_SetString(PyExc_ValueError,"Expecting a sequence of floats");
	    return 0;
	  }
        #ifdef BIG_ENDIAN_VALUE
          ptr[i] = POSH_BigFloatBits(v);
        #else
	  ptr[i] = POSH_LittleFloatBits(v);
        #endif
	Py_DECREF(o);
      }
    return 1;
  }
%}

%typemap(out) tellapic_float[4] {
  PyObject *floatArray = PyList_New(0);
#ifdef BIG_ENDIAN_VALUE
  PyList_Append(floatArray, PyFloat_FromDouble(POSH_FloatFromBigBits($1[0])));
  PyList_Append(floatArray, PyFloat_FromDouble(POSH_FloatFromBigBits($1[1])));
  PyList_Append(floatArray, PyFloat_FromDouble(POSH_FloatFromBigBits($1[2])));
  PyList_Append(floatArray, PyFloat_FromDouble(POSH_FloatFromBigBits($1[3])));
#else
  PyList_Append(floatArray, PyFloat_FromDouble(POSH_FloatFromLittleBits($1[0])));
  PyList_Append(floatArray, PyFloat_FromDouble(POSH_FloatFromLittleBits($1[1])));
  PyList_Append(floatArray, PyFloat_FromDouble(POSH_FloatFromLittleBits($1[2])));
  PyList_Append(floatArray, PyFloat_FromDouble(POSH_FloatFromLittleBits($1[3])));
#endif
  $result = floatArray;
 }

%typemap(in) tellapic_float[4](tellapic_float temp[4]) {
  if (!convert_darray($input, temp, 4))
      return NULL;
  $1 = &temp[0];
 }

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
  if (PyString_Check($input))
    {
      $1 = (byte_t *)PyString_AsString($input);
    }
  else if  (PyUnicode_Check($input))
    {
      $1 = (byte_t *)PyUnicode_AsEncodedString($input, "utf-8", "Error ~");
      $1 = (byte_t *)PyBytes_AS_STRING($1);
    }
  else
    {
      PyErr_SetString(PyExc_TypeError,"Expected a string.");
      return NULL;
    }
 }

%typemap(out) byte_t[MAX_FONTFACE_LEN] {
  char * str = (char *)$1;
  $result = PyString_FromString(str);
 }

%typemap(in) byte_t[MAX_TEXT_SIZE] {
  if (PyString_Check($input))
    {
      $1 = (byte_t *)PyString_AsString($input);
    }
  else if  (PyUnicode_Check($input))
    {
      $1 = (byte_t *)PyUnicode_AsEncodedString($input, "utf-8", "Error ~");
      $1 = (byte_t *)PyBytes_AS_STRING($1);
    }
  else
    {
      PyErr_SetString(PyExc_TypeError,"Expected a string.");
      return NULL;
    }
 }

%typemap(out) byte_t[MAX_TEXT_SIZE] {
  char * str = (char *)$1;
  $result = PyString_FromString(str);
 }

%typemap(in) byte_t[MAX_INFO_SIZE] {
  if (PyString_Check($input))
    {
      $1 = (byte_t *)PyString_AsString($input);
    }
  else if  (PyUnicode_Check($input))
    {
      $1 = (byte_t *)PyUnicode_AsEncodedString($input, "utf-8", "Error ~");
      $1 = (byte_t *)PyBytes_AS_STRING($1);
    }
  else
    {
      PyErr_SetString(PyExc_TypeError,"Expected a string.");
      return NULL;
    }
 }

%typemap(out) byte_t[MAX_INFO_SIZE] {
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

