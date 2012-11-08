

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

Then make requests:

```
$ curl http://localhost:8888/sample/

{"tokens":["This","is","a","sample","sentence","."]
,"pos":["DT","VBZ","DT","JJ","NN","."]
,"deps":[[-1,1],[1,0],[1,4],[1,5],[4,2],[4,3]]}

$ curl http://localhost:8888/sentence/ --data "The dog ran over the car."

{"tokens":["The","dog","ran","over","the","car","."]
,"pos":["DT","NN","VBD","IN","DT","NN","."]
,"deps":[[-1,2],[1,0],[2,1],[2,3],[2,6],[3,5],[5,4]]}
```

Serving pages
=============

Use your favorite static HTTP server.

For local serving the following works well:

```
$ cd site
$ python -m SimpleHTTPServer
```

