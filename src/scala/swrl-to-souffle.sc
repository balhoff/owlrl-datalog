//> using scala "2.13"
//> using dep "org.geneontology::owl-to-rules:0.3.7"

import org.geneontology.jena.OWLtoRules
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.AxiomType
import org.semanticweb.owlapi.model.IRI
import org.apache.jena.reasoner.rulesys.Rule
import org.apache.jena.reasoner.rulesys.ClauseEntry
import org.apache.jena.reasoner.TriplePattern
import org.apache.jena.graph.Node
import org.apache.jena.graph.Node_ANY
import org.apache.jena.graph.Node_Variable
import org.apache.jena.graph.Node_URI
import org.apache.jena.graph.Node_Blank
import org.apache.jena.graph.Node_Literal
import scala.jdk.CollectionConverters._
import scala.util.Using
import java.io.File
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

def jenaToSouffle(rule: Rule): Option[String] =
  for {
    body <- rule.getBody.to(List).map(clauseEntryToSouffle).sequence
    souffleBody = body.mkString(", ")
    head <- rule.getHead.to(List).map(clauseEntryToSouffle).sequence
    souffleHead = head.mkString(", ")
  } yield s"$souffleHead :- $souffleBody."

def clauseEntryToSouffle(clause: ClauseEntry): Option[String] =
  if (clause.isInstanceOf[TriplePattern]) {
    val pattern = clause.asInstanceOf[TriplePattern]
    for {
      s <- nodeToTerm(pattern.getSubject())
      p <- nodeToTerm(pattern.getPredicate())
      o <- nodeToTerm(pattern.getObject())
    } yield s"quad($s, $p, $o, g)"
  } else None

def nodeToTerm(node: Node): Option[String] =
  node match {
    case any: Node_ANY           => None
    case variable: Node_Variable => Some(variable.getName)
    case uri: Node_URI           => Some(s""""<${uri.getURI}>"""")
    case blank: Node_Blank       => None
    case literal: Node_Literal   => None
  }

implicit class ListExtensions[T](val self: List[Option[T]]) extends AnyVal {

  def sequence: Option[List[T]] = if (self.contains(None)) None else Some(self.flatten)

}

val inputFile = new File(args(0))
val outputFile = new File(args(1))
val manager = OWLManager.createOWLOntologyManager()
val inputOntology = manager.loadOntology(IRI.create(inputFile))
val swrlRules = inputOntology.getAxioms(AxiomType.SWRL_RULE).asScala.to(Set)
val jenaRules = swrlRules.flatMap(OWLtoRules.translateAxiom)
val souffleRules = jenaRules.flatMap(jenaToSouffle)
Using.resource(new PrintWriter(outputFile, StandardCharsets.UTF_8)) { writer =>
  souffleRules.foreach(writer.println)
}
