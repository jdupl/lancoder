LANcoder3000
============

A cross-platform render farm for x264-x265 encodings. Designed for portability and ease of set-up.

Currently under heavy development.

FFmpeg support only (no libav)

WebUI controlled 

### Features
* x264 encoding (x265 soon) 
* 2 pass VBR encoding or CRF encoding
* Batch processing of directory
* Audio processing with many tracks (dev)
* Subtitles automuxing (todo)


---

## How to use
Please use the [wiki](https://github.com/jdupl/LANcoder3000/wiki/How-to-setup-LANcoder).

#### Test environment
Currently only tested on Debian Wheezy servers with FFmpeg 1.0.9 from [Deb Multimedia](http://www.deb-multimedia.org/) and [mkvtoolnix 7.x](http://www.bunkus.org/videotools/mkvtoolnix/downloads.html#debian).  
Tested on 6 workers nodes and 1 master.

Now also tested on Windows (sometimes) with FFmpeg 2.3.
