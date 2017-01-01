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

def renewFile(file) {
  if (ui.fileExists(file)) ui.remove(file)
  ui.newFile(file)
  return file
}

mappingsFile = "/CompToxDash/mappings.txt"
renewFile(mappingsFile)

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
    ui.append(mappingsFile, map.get(inchikey) + "\tP3117\t\"${dsstox}\"\tS248\tQ28061352\n")  
  }
}
