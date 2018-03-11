# UndertakerExtractor


![Build Status](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=KernelHaven_UndertakerExtractor)

A code-model extractor for [KernelHaven](https://github.com/KernelHaven/KernelHaven).

This extractor uses [Undertaker v1.6.1](http://vamos.informatik.uni-erlangen.de/trac/undertaker/) to anaylze arbitrary C source code.

## Capabilities

This extractor finds #ifdef blocks in source files (`*.c`, `*.h`, `*.S`) and extracts the hierarchical condition structure.

## Usage

Place [`UndertakerExtractor.jar`](https://jenkins.sse.uni-hildesheim.de/view/KernelHaven/job/KernelHaven_UndertakerExtractor/lastSuccessfulBuild/artifact/build/jar/UndertakerExtractor.jar) in the plugins folder of KernelHaven.

To use this extractor, set `code.extractor.class` to `net.ssehub.kernel_haven.undertaker.UndertakerExtractor` in the KernelHaven properties.

## Dependencies

In addition to KernelHaven, this plugin has the following dependencies:
* Only runs on a Linux operating system

## License

This plugin is licensed under GPLv3. Another license would be possible with following restrictions:

The plugin contains undertaker which is under GPL-3.0. We do not link against undertaker, so technically we are not infected by GPL. However a release under a license other than GPL-3.0 would require the removal of the contained undertaker.
