#!/usr/bin/env python

"""
configure the setup.py setting the sources files among other things setup.py needs
"""

import sys, argparse, re


class PlaceHolder:

    @property
    def extension_version(self):
        return "0.0.1"

    @property
    def defines(self):
        return self._defines

    @defines.setter
    def defines(self, value):
        if value is not None:
            for i in value:
                self._defines = re.sub("\[|'|\]|\"", "", str(i))
        else:
            self._defines=''

values = PlaceHolder()
parser = argparse.ArgumentParser(description='Setups python extension')
parser.add_argument('-f',
                    '--swig-file',
                    metavar='<SWIG generated file>',
                    required=True,
                    type=str,
                    help='the SWIG auto-generated file.',
                    )
parser.add_argument('-n',
                    '--extension-name',
                    required=True,
                    metavar='<Extension name>',
                    type=str,
                    help="the extension name (without the underscore '_')."
                    )
parser.add_argument('-i',
                    metavar=('<Source file1>', '<Source file2'),
                    dest='source_files',
                    type=str,
                    nargs='+',
                    help='source files to be linked against the SWIG generated file.'
                    )
parser.add_argument('--defines',
                    type=str,
                    metavar='<Extra Definitions>',
                    nargs='*',
                    action='append',
                    help="extra definitions such as macropreprocessor defines.",
                    )
                    

parser.parse_args(namespace=values)

f = open("TellapicConfig.py", "w")
f.write("#!/usr/bin/env python\n\n")
f.write("EXTENSION_NAME='{extension_name}'\n".format(extension_name=values.extension_name))
f.write("EXTENSION_VERSION='{extension_version}'\n".format(extension_version=values.extension_version))
f.write("SWIG_GENERATED_FILE='{swig_file}'\n".format(swig_file=values.swig_file))
f.write("SOURCE_FILES={source_files}\n".format(source_files=values.source_files))
f.write("OS_DEFINITIONS='{defines}'\n".format(defines=re.sub("\,","",values.defines)))
f.close()
