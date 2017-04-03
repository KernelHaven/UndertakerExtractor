# UndertakerExtractor
[Undertaker v1.6.1](http://vamos.informatik.uni-erlangen.de/trac/undertaker/))
-based code extractor for KernelHaven

## Capabilities
This extractor finds #ifdef blocks in source files (*.c, *.h, *.S) and extracts
the hierarchical condition structure.

## License
This extractor is licensed under GPLv3. Another license would be possible with
following restrictions:

The extractor contains undertaker which is under GPL-3.0. We do not link against
undertaker, so technically we are not infected by GPL. However a release under a
license other than GPL-3.0 would require the removal of the contained undertaker.