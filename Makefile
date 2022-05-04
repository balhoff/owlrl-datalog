SRC=src/datalog

bin/%: $(SRC)/%.dl
	mkdir -p bin
	souffle -c $< -o $@

go-lego-reacto.owl:
	curl -L -O 'http://purl.obolibrary.org/obo/go/extensions/go-lego-reacto.owl'

ontology.nt: go-lego-reacto.owl
	riot --output=ntriples $< >$@

ontology.facts: ontology.nt
	sed 's/ /\t/' <$< | sed 's/ /\t/' | sed 's/ \.$$//' >$@

ontology: bin/owl_from_rdf ontology.facts
	mkdir -p $@ && ./bin/owl_from_rdf -D $@ && touch ontology

model.ttl:
	curl -L -o $@ 'http://noctua.geneontology.org/download/gomodel:ZFIN_ZDB-MIRNAG-081203-25/owl'

model.nt: model.ttl
	riot --output=ntriples $< >$@

rdf.facts: model.nt
	sed 's/ /\t/' <$< | sed 's/ /\t/' | sed 's/ \.$$//' >$@

inferred.csv: rdf.facts ontology bin/owl_rl_abox
	time ./bin/owl_rl_abox

#inferred.csv: quad.facts ontology bin/owl_rl_abox_quads
#	time ./bin/owl_rl_abox_quads
