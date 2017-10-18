CC = g++
CFLAGS = -Wall -O2 -std=gnu++11 -I../undertaker -pthread

LDFLAGS = -pthread
LIBS = ../picosat/libpicosat.a -lPuma -lboost_system -lboost_regex -lboost_filesystem 

UNDERTAKER_OBJS = \
	../undertaker/ConditionalBlock.o \
	../undertaker/PumaConditionalBlock.o \
	../undertaker/ModelContainer.o \
	../undertaker/RsfConfigurationModel.o \
	../undertaker/RsfReader.o \
	../undertaker/CnfConfigurationModel.o \
	../undertaker/ConfigurationModel.o \
	../undertaker/Logging.o \
	../undertaker/PicosatCNF.o \
	../undertaker/Tools.o \
	../undertaker/bool.o \
	../undertaker/BoolExpLexer.o \
	../undertaker/BoolExpParser.o \
	../undertaker/BoolExpSimplifier.o \
	../undertaker/BoolExpGC.o \
	../undertaker/BoolExpSymbolSet.o

pilztaker: pilztaker.o
	$(CC) $(LDFLAGS) -o $@ $< $(UNDERTAKER_OBJS) $(LIBS)

pilztaker-static: pilztaker.o
	$(CC) $(LDFLAGS) -static-libstdc++ -o $@ $< $(UNDERTAKER_OBJS) -Wl,-Bstatic $(LIBS) -Wl,-Bdynamic

pilztaker.o: pilztaker.cpp
	$(CC) $(CFLAGS) -c -o $@ $<

.PHONY: clean
clean:
	rm -rf pilztaker.o pilztaker pilztaker-static
