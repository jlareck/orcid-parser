# orcid-parser
Execute with command: 
```
mvn compile exec:java -Dexec.mainClass="XmlParser"  -Dexec.args="arg1 arg2"
```
`arg1` - root orcid directory

`arg2` - file where to save orcid triples
Example
```
mvn compile exec:java -Dexec.mainClass="XmlParser"  -Dexec.args="src/main/resources/orcid-files target/orcid-dataset-triples.ttl"
```
