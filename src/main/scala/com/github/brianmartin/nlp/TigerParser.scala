package com.github.brianmartin.nlp

import cc.factorie._
import cc.factorie.app.nlp._
import cc.factorie.app.nlp.parse._

object TigerParser extends Parser {
  import scala.collection.mutable.HashMap
  import dependency.DTree
  import parser.Parser

  def load() = { _parser; () }

  private lazy val _parser = { println("Loading parser model..."); Parser.read("resources/ParserModel") }
  private def _parse(tokens: Array[String], tags: Array[String]): DTree =
    _parser.parse(new DTree(tokens, tags, new Array[Int](tokens.length), new Array[String](tokens.size)))

  def process(docs: Seq[Document]): Unit = docs.foreach(d => process(d))
  def process(doc: Document): Unit = doc.sentences.foreach { s => parse(s) }

  override def parse(s: Sentence) = {
    // println("parsing: " + s.phrase)
    val tree = new ParseTree(s)
    s.attr += tree
    val tigerTree = _parse(s.tokens.map(_.string).toArray, s.tokens.map(_.posLabel.categoryValue).toSeq.toArray)
    var pars: List[(ParseEdge, Token)] = Nil
    for (childIdx <- 0 until s.size) {
      val tigerToken = tigerTree.tokenAt(childIdx + 1)
      val parentIdx = tigerToken.getHead() - 1
      tree.setParent(childIdx, parentIdx)
      tree.label(childIdx).setCategory(tigerToken.getDeprel())(null)
    }
    pars.foreach(a => a._1.set(a._2)(null))
  }

}
