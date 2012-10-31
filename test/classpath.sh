#!/bin/bash

CLP=.
prepend_path()
{
  if ! eval test -z "\"\${$1##*:$2:*}\"" -o -z "\"\${$1%%*:$2}\"" -o -z "\"\${$1##$2:*}\"" -o -z "\"\${$1##$2}\"" ; then
    eval "$1=$2:\$$1"
  fi
}

prepend_path CLP `pwd`/libs/choco-2.1.1-20101108.160635-17.jar
prepend_path CLP `pwd`/libs/jade.jar
prepend_path CLP `pwd`/libs/jason.jar
prepend_path CLP `pwd`/libs/jdom.jar


echo $CLP




