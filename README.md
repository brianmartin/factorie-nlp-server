

This project provides NLP functions (currently part-of-speech tagging and
dependency parsing via a REST API). It also includes a demonstration page which
draws the arcs of the dependency parse.
![](https://raw.github.com/brianmartin/factorie-nlp-server/master/doc/screenshot.png)
Running the REST Server
================

Fire up sbt and run:

```
$ ./sbt11
> run
```

Serving pages
=============

Use your favorite static HTTP server.

For local serving the following works well:

```
$ cd site
$ python -m SimpleHTTPServer
```

