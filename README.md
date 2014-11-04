lancoder
============

A cross-platform render cluster for x264-x265 encodings using FFmpeg designed for portability and ease of set-up.

Lancoder allows video and audio encoding on multiple machines and is controlled by a simple web interface.

Currently under development but releases are mostly stable (still in alpha).

### Features
* x264 and x265 encoding
* 2 pass and 1 pass VBR encoding, CRF encoding
* Batch processing of directories
* Audio processing with many tracks
* Stream copy


### Features (in developpement)
* Subtitles automuxing from source
* WebM compiliance with VP8 and VP9
* DVD and bluray folder structure ripping (no encryption)

---

## How to use
Please use the [wiki](https://github.com/jdupl/lancoder/wiki/How-to-setup-lancoder).

It is important to have FFmpeg and NOT libav.

Technically, libav would work, but it's filled with bugs !

Bugs related to libav will remain unawnsered.

#### Test environment
Currently tested on 6 workers nodes and 1 master including 
* Debian Wheezy and Jessie servers with FFmpeg from [Deb Multimedia](http://www.deb-multimedia.org/)
* Ubuntu family distros with [Jon Severinsson's FFmpeg PPA](https://launchpad.net/~jon-severinsson/+archive/ubuntu/ffmpeg)
* Linux custom compiled versions of FFmpeg 2.2.x and 2.3
* Windows 8 with FFmpeg 2.3

All muxing done with [mkvtoolnix 7.x](http://www.bunkus.org/videotools/mkvtoolnix/downloads.html#debian).

#### Bugs
Please report bugs in the issues section of the GitHub repository.
