#!/bin/sh
JAR=jar
CLASS_PATH=classes
LIB_PATH=lib
echo :
echo : -------------------- MAKE NEMO JAR --------------------
echo :
$JAR -cf $LIB_PATH/nemo.jar -C .$CLASS_PATH it -C $CLASS_PATH org -C $CLASS_PATH java -C $CLASS_PATH test