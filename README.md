# UndertakerExtractor

A code-model extractor for [KernelHaven](https://github.com/KernelHaven/KernelHaven).
This extractor uses [Undertaker v1.6.1](http://vamos.informatik.uni-erlangen.de/trac/undertaker/) to anaylze arbitrary C source code.

## Capabilities

This extractor finds #ifdef blocks in source files (`*.c`, `*.h`, `*.S`) and extracts the hierarchical condition structure.

## Usage

To use this extractor, set `code.extractor.class` to `net.ssehub.kernel_haven.undertaker.UndertakerExtractorFactory` in the KernelHaven properties.

### Dependencies

In addition to KernelHaven, this extractor has the following dependencies:
* Only runs on a Linux operating system

### Configuration

In addition to the default ones, this extractor has the following configuration options in the KernelHaven properties:

| Key | Mandatory | Default | Example | Description |
|-----|-----------|---------|---------|-------------|
| `code.extractor.hang_timeout` | no | `20000` | `20000` | Undertaker has a bug where it hangs forever on some few files of the Linux Kernel. This setting defines a timeout in milliseconds until the undertaker executable is forcibly terminated. |

## License
This extractor is licensed under GPLv3. Another license would be possible with following restrictions:

The extractor contains undertaker which is under GPL-3.0. We do not link against undertaker, so technically we are not infected by GPL. However a release under a license other than GPL-3.0 would require the removal of the contained undertaker.
