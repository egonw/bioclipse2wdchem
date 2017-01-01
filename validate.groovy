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

println "Found ${results.rowCount} results"

for (compound=1; compound<=results.rowCount; compound++) {
  itemID = results.get(compound, "compound")
  canonical = results.get(compound, "canonical")
  isomeric = results.get(compound, "isomeric")
  if (canonical != null && isomeric != null) {
    if (canonical.trim().length() > 0 && isomeric.trim().length() > 0) {
      try {
        molCan = cdk.fromSMILES(canonical)
        molIso = cdk.fromSMILES(isomeric)
        canForm = cdk.molecularFormula(molCan)
        isoForm = cdk.molecularFormula(molIso)
        if (canForm != isoForm) {
          ui.append("/Wikidata/wrongCombo.txt", "Canonical and isomeric SMILESes have different chemical formulas for $itemID: $canForm or $isoForm\n")
        } 
      } catch (Exception exception) {
        ui.append("/Wikidata/badSMILES.txt", "Bad SMILES for $itemID: $canonical or $isomeric\n")
      }
    }
  } else {
    if (canonical != null) {
      try {
        molCan = cdk.fromSMILES(canonical.trim())
      } catch (Exception exception) {
        ui.append("/Wikidata/badSMILES.txt", "Bad canonical SMILES for $itemID: $canonical Reason: ${exception.message}\n")
      }
    }
    if (isomeric != null) {
      try {
        molIso = cdk.fromSMILES(isomeric.trim())
      } catch (Exception exception) {
        ui.append("/Wikidata/badSMILES.txt", "Bad isomeric SMILES for $itemID: $isomeric Reason: ${exception.message}\n")
      }
    }
  }
}
