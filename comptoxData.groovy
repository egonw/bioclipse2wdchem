sparql = """
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
SELECT (substr(str(?compound),32) as ?wd) ?key ?dsstox WHERE {
  ?compound wdt:P235 ?key .
  MINUS { ?compound wdt:P3117 ?dsstox . }
}
"""

if (bioclipse.isOnline()) {
  results = rdf.sparqlRemote(
    "https://query.wikidata.org/sparql", sparql
  )
}

// make a map
map = new HashMap()
for (i=1;i<=results.rowCount;i++) {
  rowVals = results.getRow(i)
  map.put(rowVals[1], rowVals[0])  
}

new File(bioclipse.fullPath("/CompToxDash/dsstox_20160701.tsv")).eachLine{ line ->
  fields = line.split("\t")
  dsstox = fields[0]
  inchikey = fields[2]
  if (map.containsKey(inchikey)) {
    ui.append("/CompToxDash/mappings.txt", map.get(inchikey) + "\tP3117\t\"${dsstox}\"\tS248\tQ28061352\n")  
  }
}
