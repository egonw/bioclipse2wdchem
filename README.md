# bioclipse2wdchem
Bioclipse scripts for [Wikidata:WikiProject Chemistry](https://www.wikidata.org/wiki/Wikidata:WikiProject_Chemistry).

Most scripts make use of Magnus Manke's [QuickStatements](http://tools.wmflabs.org/wikidata-todo/quick_statements.php) (QS).

## create Wikidata items from SMILES
The createWDitemsFromSMILES.groovy script allows creating of QS statements starting with the SMILES. If the optional Qitem ID
is given for the paper, then it is added to the new item too.

## validate consistency of SMILES, mass, and InChI
The validate.groovy script runs over all chemical compounds and validates if various structure informal bits are consistent.

