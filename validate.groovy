sparql = """
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
PREFIX wd: <http://www.wikidata.org/entity/>
SELECT ?compound ?canonical ?isomeric WHERE {
  ?compound wdt:P31 wd:Q11173.
  OPTIONAL { ?compound wdt:P233 ?canonical. }
  OPTIONAL { ?compound wdt:P2017 ?isomeric. }
  FILTER (BOUND(?canonical) || BOUND(?isomeric))
  OPTIONAL { ?compound wdt:P2067 ?mass. }
  OPTIONAL { ?compound wdt:P234 ?inchi. }
  OPTIONAL { ?compound wdt:P235 ?inchikey. }
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

badSMILESFile = "/Wikidata/badSMILES.txt"
renewFile(badSMILESFile)
smilesComboFile = "/Wikidata/wrongCombo.txt"
renewFile(smilesComboFile)

println "Found ${results.rowCount} results"

for (compound=1; compound<=results.rowCount; compound++) {
  itemID = results.get(compound, "compound")
  canonical = results.get(compound, "canonical")
  isomeric = results.get(compound, "isomeric")
  molCan = null; molIso = null
  // check for bad SMILES
  if (canonical != null) {
    try {
      molCan = cdk.fromSMILES(canonical.trim())
    } catch (Exception exception) {
      ui.append(badSMILESFile, "Bad canonical SMILES for $itemID: $canonical Reason: ${exception.message}\n")
    }
  }
  if (isomeric != null) {
    try {
      molIso = cdk.fromSMILES(isomeric.trim())
    } catch (Exception exception) {
      ui.append(badSMILESFile, "Bad isomeric SMILES for $itemID: $isomeric Reason: ${exception.message}\n")
    }
  }
  // compare two SMILES for expected same chemical formula
  if (molCan != null && molIso != null) {
    canForm = cdk.molecularFormula(molCan)
    isoForm = cdk.molecularFormula(molIso)
    if (canForm != isoForm) {
      ui.append(smilesComboFile, "Canonical and isomeric SMILESes have different chemical formulas for $itemID: $canForm or $isoForm\n")
    }
  }
}
