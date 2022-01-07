# owlrl-datalog

This is an implementation of reasoning rules for the [OWL RL profile](https://www.w3.org/TR/owl2-profiles/#OWL_2_RL) in [Soufflé](https://souffle-lang.github.io). 
The Soufflé rules are separated into two stages:

- The [first stage](https://github.com/balhoff/owlrl-datalog/blob/master/src/datalog/owl_from_rdf.dl) parses OWL axioms out of RDF representing the ontology Tbox, 
and outputs tables for each expression and axiom type.
- The [second stage](https://github.com/balhoff/owlrl-datalog/blob/master/src/datalog/owl_rl_abox.dl) inputs those tables as well as RDF representing an Abox dataset. 
It outputs the inferred class assertions and object property assertions according to the Tbox.

The two stage approach allows only computing the Abox inferences for many input datasets, handling the ontology only once. 
"Preparsing" the ontology also allows the Abox rules to be more efficient. 
See the [Makefile](https://github.com/balhoff/owlrl-datalog/blob/master/Makefile) for examples of how to run the rules. 
An [implementation that works with RDF quads](https://github.com/balhoff/owlrl-datalog/blob/master/src/datalog/owl_rl_abox_quads.dl) is also provided.
