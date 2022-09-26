import java.io.*;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XmlParser {
    static File file;
    static FileWriter fileWriter;
    static PrintWriter printWriter;
    public static void main(String[] args) throws Exception {
        if (args.length<2) {
            throw new Exception("Not enough arguments! " +
                    "First argument should be path to the root orcid directory, Second argument should be output file");
        }
        String directoryPath = args[0];
        file = new File(args[1]);
        fileWriter = new FileWriter(file);
        printWriter = new PrintWriter(fileWriter);
        traverseDirectory(directoryPath);
        fileWriter.close();
    }
    private static void traverseDirectory(String rootDirectoryPath) {
        final File folder = new File(rootDirectoryPath);
        listFilesForFolder(folder);
    }
    private static void listFilesForFolder(File folder) {
        for (File fileEntry: folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else if (fileEntry.getName().endsWith(".xml")) {
                readXml(fileEntry.getAbsolutePath());
            }
        }
    }
    private static void readXml(String fileName) {
        boolean isFound = false;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader eventReader =
                    factory.createXMLEventReader(new FileReader(fileName));
            String personID = null;
            while(eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.getEventType() == XMLStreamConstants.START_ELEMENT ) {
                    StartElement startElement = event.asStartElement();
                    String qName = startElement.getName().getLocalPart();
                    String prefix = startElement.getName().getPrefix();
                    if (!isFound && qName.equalsIgnoreCase("person")) {
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        personID = attributes.next().getValue();
                        isFound = true;
                    }
                    else if (prefix.contains("researcher-url") && qName.equalsIgnoreCase("url")) {
                        if (eventReader.hasNext()) {
                            event = eventReader.nextEvent();
                            if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
                               Characters researcherURL = event.asCharacters();
                               if (isFound) {
                                   String triple = createTripleResearcherURL(personID, researcherURL.getData());
                                   writeTriple(triple);
                               }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String createTripleResearcherURL(String personID, String researcherURL) {
        String exampleRelatedProperty = "<http://example.org/related>";
        String researcherUrlObject = "\"" + researcherURL + "\"";
        String orcidURL = "<https://orcid.org" + personID.replace("/person","") + ">";
        String triple = orcidURL + " " + exampleRelatedProperty + " " + researcherUrlObject + " .";
        //System.out.println(triple);
        return triple;
    }
    private static String createTripleAuthor(String personID) {
        String rdfType = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
        String dboAuthor = "<http:/dbpedia.org/ontology/Academic>";
        String orcidUrl = "<https://orcid.org" + personID.replace("/person","") + ">";
        String triple = orcidUrl + " " + rdfType + " " + dboAuthor + " .";
        return triple;
    }
    private static void writeTriple(String triple) throws IOException {
        printWriter.println(triple);
    }
}
