lancoder
============

A cross-platform render farm for x264-x265 encodings using FFmpeg. Designed for portability and ease of set-up.

Currently under heavy development.

Don't ask for libav support (it's full of bugs).

WebUI controlled 

### Features
* x264 encoding (x265 in test) 
* 2 pass VBR encoding or CRF encoding
* Batch processing of directory
* Audio processing with many tracks
* Subtitles automuxing from source (dev)


---

## How to use
Please use the [wiki](https://github.com/jdupl/LANcoder3000/wiki/How-to-setup-lancoder).

#### Test environment
Currently tested on 6 workers nodes and 1 master including 
* Debian Wheezy servers with FFmpeg 1.0.9 from [Deb Multimedia](http://www.deb-multimedia.org/)
* Ubuntu family distros with [Jon Severinsson's FFmpeg PPA](https://launchpad.net/~jon-severinsson/+archive/ubuntu/ffmpeg)  FFmpeg 1.2
* Linux custom compiled versions of 2.2.x
* Windows 7 (sometimes) with FFmpeg 2.3

All muxing done with [mkvtoolnix 7.x](http://www.bunkus.org/videotools/mkvtoolnix/downloads.html#debian).
