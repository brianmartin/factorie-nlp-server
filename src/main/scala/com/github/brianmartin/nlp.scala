package com.github.brianmartin

import com.codahale.jerkson.Json._
import nlp._

object Factorie {
  import cc.factorie.app.nlp.{Document, Sentence}
  import cc.factorie.app.nlp.parse.ParseTree
  import cc.factorie.app.nlp.segment.{Tokenizer, SentenceSegmenter}

  POS.load()
  TigerParser.load()

  def addPosTags(doc: Document) = doc.tokens.foreach(t => t.attr += new cc.factorie.app.nlp.pos.PosLabel(t, cc.factorie.app.nlp.pos.PosDomain.getCategory(0)))

  def makeDocSingleSentence(doc: Document) = new Sentence(doc, 0, doc.tokens.length)

  def getDepList(s: Sentence): Seq[Seq[Int]] = {
    val tree = s.attr[ParseTree]
    Seq(Seq(-1, tree.rootChildIndex)) ++ s.tokens.zipWithIndex.flatMap { case (t, i) => tree.children(i).map(c => Seq(i, c.indexInSentence)) }
  }

  def getPosList(s: Sentence): Seq[String] = s.tokens.map(_.posLabel.categoryValue)

  def jsonify(s: Sentence): String = {
    println(generate(Map("tokens" -> s.tokens.map(_.string),
                         "pos" -> getPosList(s),
                         "deps" -> getDepList(s))))
    generate(Map("tokens" -> s.tokens.map(_.string),
                 "pos" -> getPosList(s),
                 "deps" -> getDepList(s)))
  }

  def process(msg: String): Seq[String] = {
    println("Tokenizer.process recieved: " + msg)
    val doc = new Document("", msg)
    Tokenizer.process(doc)
    if (doc.length == 0)
      return Seq.empty[String]
    SentenceSegmenter.process(doc)
    if (doc.sentences.length == 0)
      makeDocSingleSentence(doc)
    addPosTags(doc)
    POS.processBySampling(doc)
    TigerParser.process(doc)
    doc.sentences.map(jsonify(_))
  }

  def sampleSentence(): String = process("This is a sample sentence.").head
}

