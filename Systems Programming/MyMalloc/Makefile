CC = gcc
CFLAGS = -std=c99 -Wall -Wextra -g
all: memgrind testing
memgrind: mymalloc.c memgrind.c mymalloc.h
	$(CC) $(CFLAGS) mymalloc.c memgrind.c -o memgrind

testing: mymalloc.c testing.c mymalloc.h
	$(CC) $(CFLAGS) mymalloc.c testing.c -o testing

clean:
	rm -f memgrind testing