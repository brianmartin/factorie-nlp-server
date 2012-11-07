package com.github.brianmartin.nlp

import cc.factorie.app.nlp.segment._
import cc.factorie._
import cc.factorie.app.nlp._
import cc.factorie.app.nlp.pos._

object POS {

  val modelDir = "resources/pos-model"
  def save(extraInfo: String = "") = PosModel.save(modelDir + extraInfo)

  var modelLoaded = false
  def load(modelDir: String = "resources/pos-model"): Unit = { PosModel.load(modelDir); modelLoaded = true; println("loaded POS from " + modelDir) }

  object PosFeaturesDomain extends CategoricalVectorDomain[String]
  class PosFeatures(val token: Token) extends BinaryFeatureVectorVariable[String] {
    def domain = PosFeaturesDomain
    override def skipNonCategories = true
  }

  // load fails without SparseWeights.  Related to freezing domains? --brian
  object PosModel extends TemplateModel(
    new TemplateWithDotStatistics1[PosLabel] {
      override def statisticsDomains = Seq(PosDomain)
      override def defaultFactorName = "bias"
    },
    new TemplateWithDotStatistics2[PosLabel, PosFeatures] with SparseWeights {
      override def statisticsDomains = Seq(PosDomain, PosFeaturesDomain)
      def unroll1(label: PosLabel) = Factor(label, label.token.attr[PosFeatures])
      def unroll2(tf: PosFeatures) = Factor(tf.token.attr[PosLabel], tf)
      override def defaultFactorName = "local"
    },
    new TemplateWithDotStatistics2[PosLabel, PosLabel] {
      override def statisticsDomains = Seq(PosDomain, PosDomain)
      def unroll1(label: PosLabel) = if (label.token.sentenceHasPrev) Factor(label.token.sentencePrev.attr[PosLabel], label) else Nil
      def unroll2(label: PosLabel) = if (label.token.sentenceHasNext) Factor(label, label.token.sentenceNext.attr[PosLabel]) else Nil
      override def defaultFactorName = "trans"
    })

  def initPosFeatures(document: Document): Unit = {
    for (token <- document) {
      val rawWord = token.string
      val word = cc.factorie.app.strings.simplifyDigits(rawWord)
      val features = token.attr += new PosFeatures(token)
      features += "W=" + word.toLowerCase
      features += "SHAPE=" + cc.factorie.app.strings.stringShape(rawWord, 2)
      features += "SUFFIX3=" + word.takeRight(3)
      features += "PREFIX3=" + word.take(3)
      if (token.isCapitalized) features += "CAPITALIZED"
      if (token.containsDigit) features += "NUMERIC"
      //if (token.containsDash) features += "DASH"
      //if (token.containsDash && token.containsDigit) features += "NUMDASH"
      if (token.isPunctuation) features += "PUNCTUATION"
    }
    for (sentence <- document.sentences)
      cc.factorie.app.chain.Observations.addNeighboringFeatureConjunctions(sentence, (t: Token) => t.attr[PosFeatures], List(-2), List(-1), List(1))
  }

  def process(document: Document): Unit = {
    import cc.factorie.bp._
    if (!modelLoaded) { println("loading POS2 model..."); load() }
    try { document.tokens.head.attr[PosLabel] } catch { case e: Exception => throw new Error("POS2 assumes PosLabels have already been added.") }
    initPosFeatures(document)
    for (sentence <- document.sentences if sentence.length > 0) { // TODO: remove sentence.length > 0 ?
      val fg = new FG(PosModel, sentence.tokens.map(_.attr[PosLabel]).toSet) with SumProductFG
      fg.inferTreeUpDown(iterations = 1, checkLoops = false)
      fg.setToMaxMarginal()
    }
  }

  val predictor = new VariableSettingsSampler[PosLabel](PosModel)

  def processBySampling(document: Document): Unit = {
    if (!modelLoaded) { println("loading POS2 model..."); load() }
    try { document.tokens.head.attr[PosLabel] } catch { case e: Exception => throw new Error("POS2 assumes PosLabels have already been added.") }
    initPosFeatures(document)
    predictor.processAll(document.map(_.attr[PosLabel]), 3)
  }

}
