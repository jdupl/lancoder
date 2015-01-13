lancoder
============

Lancoder is a cross-platform render cluster for various video encodings.

It uses [FFmpeg](http://ffmpeg.org) as the encoder and is designed for portability and ease of set-up.

Lancoder allows video and audio encoding over multiple machines in a local network, controlled by a simple web interface.

Currently under development but releases are mostly stable (still in alpha/beta stage).

### Features
* x264, x265, VP8, VP9, Theora video encoding
* Opus, Vorbis, Speex, AAC, MP3, DTS, FLAC (and more) audio processing
* 2 pass and 1 pass VBR encoding or CRF encoding
* Batch processing of directories
* Multiple track audio processing
* Stream copy


### Development
* Allow output in other containers than MKV
* Choose audio tracks from input
* Subtitles automuxing from source
* Use DVD and bluray file structure as source (no decryption)

---

## How to use
Please use the [wiki](https://github.com/jdupl/lancoder/wiki/How-to-setup-lancoder) (needs to be updated).

It is important to install FFmpeg and NOT libav.

Technically, libav would work, but it's filled with bugs ! Libav will not be supported. Please make sure to run [FFmpeg](http://ffmpeg.org).

Ubuntu and Debian users will need to add a [ppa](https://launchpad.net/~mc3man/+archive/ubuntu/trusty-media) or a [debian source](http://deb-multimedia.org/).

#### Test environment
Currently tested on 6 workers nodes and 1 master including 
* Debian Wheezy and Jessie servers with FFmpeg from [Deb Multimedia](http://www.deb-multimedia.org/)
* Ubuntu family distros with [Jon Severinsson's FFmpeg PPA](https://launchpad.net/~jon-severinsson/+archive/ubuntu/ffmpeg)
* Linux custom compiled versions of FFmpeg 2.2.x and 2.3
* Windows 8 with FFmpeg 2.3

#### Bugs
Please report bugs in the [issues](https://github.com/jdupl/lancoder/issues) section of the GitHub repository.

Keep in mind, current builds are not production ready.
