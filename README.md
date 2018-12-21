# UndertakerExtractor


![Build Status](https://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=KernelHaven_UndertakerExtractor)

A code-model extractor for [KernelHaven](https://github.com/KernelHaven/KernelHaven).

This extractor uses [Undertaker v1.6.1](https://vamos.informatik.uni-erlangen.de/trac/undertaker/) to anaylze arbitrary C source code.

## Capabilities

This extractor finds #ifdef blocks in source files (`*.c`, `*.h`, `*.S`) and extracts the hierarchical condition structure.

## Usage

Place [`UndertakerExtractor.jar`](https://jenkins.sse.uni-hildesheim.de/view/KernelHaven/job/KernelHaven_UndertakerExtractor/lastSuccessfulBuild/artifact/build/jar/UndertakerExtractor.jar) in the plugins folder of KernelHaven.

To use this extractor, set `code.extractor.class` to `net.ssehub.kernel_haven.undertaker.UndertakerExtractor` in the KernelHaven properties.

## Dependencies

In addition to KernelHaven, this plugin has the following dependencies:
* Only runs on a Linux operating system

## License

This plugin is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html).

Another license would be possible with the following restriction:
* The plugin contains undertaker which is under GPLv3. We do not link against undertaker, so technically we are not infected by GPL. However a release under a license other than GPLv3 would require the removal of the contained undertaker.

## Used Tools

The following tools are used (and bundled in `res/`) by this plugin:

| Tool | Version | License |
|------|---------|---------|
| [PilzTaker](https://github.com/SSE-LinuxAnalysis/pilztaker) | [2017-07-19 (d5175c1)](https://github.com/SSE-LinuxAnalysis/pilztaker/commit/d5175c1cb80f9b6d67815836fbeeab1b8cc20d7c) | [GPLv3](https://www.gnu.org/licenses/gpl.html) |
| [Undertaker](https://vamos.informatik.uni-erlangen.de/trac/undertaker) | [v1.6.1](https://vamos.informatik.uni-erlangen.de/files/undertaker-1.6.1.tar.xz) | [GPLv3](https://www.gnu.org/licenses/gpl.html) |
