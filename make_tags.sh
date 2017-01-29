#!/bin/bash

SDK_ROOT='/home/rahul/ESP8266/esp-open-sdk/sdk/'

find src/ -name '*.c' -o -name '*.h' > cscope.files
find $SDK_ROOT/include -name '*.h' >> cscope.files
find $SDK_ROOT/driver_lib/include/driver -name '*.h' >> cscope.files
