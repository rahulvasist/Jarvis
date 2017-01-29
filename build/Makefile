SRC_DIR = ../src
SOURCES = jarvis.c
OBJECTS = $(SOURCES:.c=.o)


CC = xtensa-lx106-elf-gcc
CFLAGS	= -Os -g -O2 -Wpointer-arith -Wundef -Werror -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals  -D__ets__ -DICACHE_FLASH -I.
LDLIBS = -nostdlib -Wl,--start-group -lc -lgcc -lpp -lphy -lnet80211 -llwip -lwpa -lmain -ldriver -Wl,--end-group 
LDFLAGS = -Teagle.app.v6.ld

jarvis-0x00000.bin: jarvis
	esptool.py elf2image $^

%.o: $(SRC_DIR)/%.c
	$(CC) -c $< $(CFLAGS)

jarvis: $(OBJECTS)
	$(CC) -o $@ $^ $(LDFLAGS) $(LDLIBS)

flash: jarvis-0x00000.bin
	esptool.py write_flash 0 jarvis-0x00000.bin 0x10000 jarvis-0x10000.bin

clean:
	rm -f jarvis jarvis.o jarvis-0x00000.bin jarvis-0x10000.bin
