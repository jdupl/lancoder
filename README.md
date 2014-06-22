LANcoder3000
============

A render farm for x264 encodings. Disigned for portability and ease of set-up on *nix servers.

Currently under heavy development, but software is working.

### Features
* x264 encoding
* FFmpeg support only (no libav)
* 2 pass VBR encoding
* CRF encoding
* Batch processing all files in directory
* Auto remuxing 
* WebUI 

---

## How to use
Please use the wiki.

#### Test environnement
Currently only tested on Debian Wheezy servers with FFmpeg 1.0.9 from [Deb Multimedia](http://www.deb-multimedia.org/) and [mkvtoolnix 7.x](http://www.bunkus.org/videotools/mkvtoolnix/downloads.html#debian).  
Tested on 6 workers nodes and 1 master.
