paperQ = null // paperQ = "Q22570477"
compoundQ = null // give ID if the compound already exists
smiles = "C([H])([H])([H])OC1=C(C([H])=C(C([H])=C1[H])C2=C([H])C(=O)C3=C([H])C([H])=C(C([H])=C3O2)O[H])O[H]"

def upgradeChemFormula(formula) {
  formula = formula.replace("0","₀");
  formula = formula.replace("1","₁");
  formula = formula.replace("2","₂");
  formula = formula.replace("3","₃");
  formula = formula.replace("4","₄");
  formula = formula.replace("5","₅");
  formula = formula.replace("6","₆");
  formula = formula.replace("7","₇");
  formula = formula.replace("8","₈");
  formula = formula.replace("9","₉");
}

mol = cdk.fromSMILES(smiles)

inchiObj = inchi.generate(mol)
inchiShort = inchiObj.value.substring(6)
key = inchiObj.key // key = "GDGXJFJBRMKYDL-FYWRMAATSA-N"

sparql = """
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
SELECT ?compound WHERE {
  ?compound wdt:P235 "$key" .
}
"""

if (bioclipse.isOnline()) {
  results = rdf.sparqlRemote(
    "https://query.wikidata.org/sparql", sparql
  )
  missing = results.rowCount == 0
  if (!missing) {
    existingQcode = results.get(1,"compound")
  }
} else {
  missing = true
}

formula = upgradeChemFormula(cdk.molecularFormula(mol))

// Create the Wikidata QuickStatement, see https://tools.wmflabs.org/wikidata-todo/quick_statements.php

item = "LAST" // set to Qxxxx if you need to append info, e.g. item = "Q22579236"

pubchemLine = ""
if (bioclipse.isOnline()) {
  pcResults = pubchem.search(key)
  if (pcResults.size == 1) {
    cid = pcResults[0]
    pubchemLine = "$item\tP662\t\"$cid\""
	sparql = """
	PREFIX wdt: <http://www.wikidata.org/prop/direct/>
	SELECT ?compound WHERE {
	  ?compound wdt:P662 "$cid" .
	}
	"""
	
	if (bioclipse.isOnline()) {
	  results = rdf.sparqlRemote(
	    "https://query.wikidata.org/sparql", sparql
	  )
	  missing = results.rowCount == 0
	  if (!missing) {
  	    existingQcode = results.get(1,"compound")
  	  }
	} else {
	  missing = true
	}
  }
}

paperProv = ""
if (paperQ != null) paperProv = "\tS248\t$paperQ"

if (!missing) {
  println "===================="
  println "$formula is already in Wikidata as " + existingQcode
  
  item = existingQcode.substring(32)
  pubchemLine = pubchemLine.replace("LAST", "Q" + existingQcode.substring(32))

  statement = """
    Q$item\tP31\tQ11173$paperProv
    Q$item\tP233\t\"$smiles\"
    Q$item\tP274\t\"$formula\"
    Q$item\tP234\t\"$inchiShort\"
    Q$item\tP235\t\"$key\"
    $pubchemLine
  """

  println statement
    
  println "===================="
} else {
  statement = """
    CREATE
    
    $item\tP31\tQ11173$paperProv
    $item\tDen\t\"chemical compound\"$paperProv
    $item\tP233\t\"$smiles\"
    $item\tP274\t\"$formula\"
    $item\tP234\t\"$inchiShort\"
    $item\tP235\t\"$key\"
    $pubchemLine
  """

  println "===================="
  println statement
  println "===================="
}

ui.open(mol)
