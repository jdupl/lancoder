lancoder
============

Lancoder provides video and audio encoding distributed across multiple machines in a local network, controlled by a simple web interface.

Currently under development but releases are mostly stable (still in alpha/beta stage).

### Encoding codecs
* x264, x265, VP8, VP9, Theora video encoding
* Opus, Vorbis, Speex, AAC, MP3, DTS, FLAC 
* And [more](https://github.com/jdupl/lancoder/tree/master/src/main/java/org/lancoder/common/codecs/impl) 


### Features
* 1 or 2 pass VBR encoding or CRF encoding
* Batch processing of directories
* Multiple audio tracks
* Stream copy
 

### Development
* Allow output in other containers than MKV
* Choose audio tracks from input
* Subtitles automuxing from source
* Use DVD and bluray file structure as source (no decryption)

---

## Dependencies

#### MkvMerge
MkvMerge from [MkvToolNix](https://www.bunkus.org/videotools/mkvtoolnix/) is required only for x265 encodings. 

#### ffmpeg (not libav)

This project uses [FFmpeg](http://ffmpeg.org) as the media encoder. It must be installed on every node. 

**Libav will not be supported.**

Instructions to install the real ffmpeg for Linux are below.


## How to get the [real ffmpeg](http://ffmpeg.org/)

#### ArchLinux
* Install directly from official packages

#### Debian Wheezy, Jessie
* Add source [Deb Multimedia](http://www.deb-multimedia.org/)

#### Fedora, CentOS
* Install from RPM Fusion

#### Ubuntu Trusty (14.04)
* Use this [PPA](https://launchpad.net/~mc3man/+archive/ubuntu/trusty-media)

#### Ubuntu Vivid (15.04)
* Install directly from official packages
* Universe must be sourced

#### Other distro/OS
* Refer to [official website](http://ffmpeg.org/download.html)

## Bugs
Please report bugs in the [issues](https://github.com/jdupl/lancoder/issues) section of the GitHub repository.

Keep in mind, current builds are not production ready.
