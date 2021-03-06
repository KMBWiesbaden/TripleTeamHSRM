package sprint02;


import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.jena.util.FileManager;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class Sprint02_Triple_Extractor_Beta {
	
	// Namesspace hsrmtt hat keine weitere Hinterlegung!
	private static String rdf_kopf ="<?xml version='1.0' encoding='UTF-8'?> <rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:hsrmtt='http://www.hsrm.de/somewhere/TT/'> ";
	
	// file-Pfad muss komplett ab c: ergaenzt werden
	private static String rdf_descriptor_Start	= "<rdf:Description rdf:about='file:///";
	// hinter Pfadangabe
	private static String rdf_kopf_Schluss ="'>";
	private static String rdf_ressource_Schluss ="'/>";
	// Scliieser
	private static String rdf_descriptor_ende = "</rdf:Description>";
	private static String rdf_gesamt_ende = "</rdf:RDF>";
	
	private static String rdf_seq_kopf = "<rdf:type rdf:resource='http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq'/>";
	
	static String ttNamespace = "http://www.hsrm.de/somewhere/TT/";
	
	private static Model model = null;
	
	
	public static void main(String[] args) throws IOException, SAXException, TikaException {
		
		//String inputFile = "C:/Users/dr_be_000/Dropbox/KMB-Mini-KMB-Klaus/Informatik/WP/Quellmaterial/Docx/Benutzeranleitung DFAT.docx";
		String inputFile = "C:/Users/dr_be_000/Dropbox/KMB-Mini-KMB-Klaus/Informatik/WP/Quellmaterial/1_Arbeitsdaten/Stile-Test-Dokument.docx";
		model = modelStarter();
		
		//String inputFile = fileChooser();
		
		Metadata md = getMetaDates(inputFile);
		processMetaDates(md, inputFile);
		
		JADX_Analizer(inputFile);
		
		//sourceTypeIdentifierRDF(inputFile);
		
		sourceTypeIdentifierJena(inputFile, model);
		
		//jenaReadWrite();
		
        System.out.println("****** Ausgabe des globalen Models als RDF  *******\n");
        // write it to standard out
        model.write(System.out); 
        System.out.println("\n\n");
        
        System.out.println("****** Ausgabe des in den Jena-Triple-Store eingelesenen Models als N-Triple in .nt-Datei *******\n");
        // save as model in Ergebnisordner
		String outputFileName = "C:/Users/dr_be_000/Dropbox/KMB-Mini-KMB-Klaus/Informatik/WP/Ergebnisse/N-Triple.nt";
		
        OutputStream out;
		try {
			out = new FileOutputStream( outputFileName );
			model.write(out, "N-TRIPLES");
		} catch (FileNotFoundException e) {
			
			System.out.println("Model konnte nicht gespeichert werden *\n" + e.getMessage());
		}
        
	}
	
	
	public static Model modelStarter() {
		
		model = ModelFactory.createDefaultModel();
		String modelExist = javax.swing.JOptionPane.showInputDialog( "Soll ein bestehende Jena-Model geöffnet werden? J/N" );

		System.out.println("Eingabe: "+modelExist);
		
		if (modelExist.equals("J")){
			String modelFile = javax.swing.JOptionPane.showInputDialog( "Wie ist der Name des Models" );
			String modelType = javax.swing.JOptionPane.showInputDialog( "Welchen Typ hat das Model" );
			
			switch (modelType){
			case "TURTLE":
				model.read(modelFile, "TURTLE");
				break;
			case "N-TRIPLES":
				model.read(modelFile, "N-TRIPLES");
				break;
			case "RDF/XML":
				model.read(modelFile, "RDF/XML");
				break;
			default:
				System.out.println("Der Typ ist leider nicht bekannt. Es konnte kein model geöffnet werden\n");
				break;
			}
			
	     System.out.println("****** Es wurde dieses model zum Start eingelesen *******\n");
	        // write it to standard out
	        model.write(System.out); 
	        System.out.println("\n\n");
		}
		
		return model;
		
	}
	


	  public static void JADX_Analizer(String in) {
			

			// Einlesen des geparsten Dokuments analog https://www.tutorials.de/threads/docx-mit-java-bearbeiten.388343/
			System.out.println("****** XML-Baum mit JAXP-Parser ******* \n");
			
			String rdf = "";
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
				try {
					builder = factory.newDocumentBuilder();
					Document document;
					try {
						
						ZipFile docxFile = new ZipFile (in);
			            ZipEntry documentXML = docxFile.getEntry("word/document.xml");
			            InputStream documentXMLIS = docxFile.getInputStream(documentXML);
						
			            System.out.println("****** xml-Input-Stream toString ******* \n");
			            
			            String result = IOUtils.toString(documentXMLIS, StandardCharsets.UTF_8);
			            
			            System.out.println(result);
			            System.out.println("\n \n");
			            
			            // InputStream erneut aufrufen - da durchgelaufen
			            documentXMLIS = docxFile.getInputStream(documentXML);
						document = builder.parse(documentXMLIS);
						
						System.out.println("****** Analyse des Dokuments anhand XML-Baum mittels JADX Ausgabe als Konsolnen-Text ******* \n");
						System.out.println("****** Parallel Erzeugung der RDF-Beschreibungen der Ergebnisse ******* \n");
						
						rdf = rdf + rdf_kopf; 
						Element docElement = document.getDocumentElement();
						
						
						//rdf sequenz fuer alle Paragraphen aufmachen und Paragraphen rein
						String tripleSeq =  rdf_descriptor_Start + in + rdf_kopf_Schluss; 
						tripleSeq = tripleSeq + rdf_seq_kopf;
						for (int i=0; i<docElement.getElementsByTagName("w:p").getLength(); i++) {
							tripleSeq = tripleSeq + "<rdf:_"+ (i+1) +" rdf:resource='file:///"+ in + "#Paragraph_"+ i + rdf_ressource_Schluss; 
						}
						tripleSeq = tripleSeq + rdf_descriptor_ende;
						rdf = rdf + tripleSeq;
						
						// fuer alle Paragraphen-Knotentreffer und deren Inhalte
						int counter = 0;
						// fuer triple-Teile
						String tripleString ="";
						// Analyse nach w:p Elementen = Paragraph
						Element tElement = (Element) docElement.getElementsByTagName("w:p").item(counter);
						
				        while ((counter< docElement.getElementsByTagName("w:p").getLength())&&(counter<2000)) {
				        	// Zahl zur Vermeidung bzw zum Auffinden der 'Code-Injection' im Testdokument
				        	tElement = (Element) docElement.getElementsByTagName("w:p").item(counter);
				        	tripleString ="";
				        	// Text-Content = alles ausserhalb von <tags> ausgeben
				        	System.out.println("TRIPLE: Element " + counter + " hat Content: "+ tElement.getTextContent());
				        	// rdf-Triple mit Descriptor pro Paragraph erzeugen
				        		//triple aufmachen pro Paragraph
				        		tripleString = tripleString + rdf_descriptor_Start + in + "#Paragraph_"+ counter + rdf_kopf_Schluss; 
				        		// triple inhalt rein
				        		tripleString = tripleString + "<hsrmtt:inhalt>"+tElement.getTextContent()+"</hsrmtt:inhalt>";
				        		// descriptor und rdf schliesen
				        		tripleString = tripleString + rdf_descriptor_ende;
				     		// Gibt es zum Paragraphen w:pStyle-Elemente - dann Attribute von w:val ausgeben
				        	if (tElement.getElementsByTagName("w:pStyle").getLength()>0){
								Element pElement = (Element) tElement.getElementsByTagName("w:pStyle").item(0);
								System.out.println("\t TRIPLE: Element " + counter + " hat Style-Wert: "+ pElement.getAttribute("w:val"));
								// rdf-Triple zu Style mit Descriptor pro Paragraph erzeugen
				        		//triple aufmachen pro Paragraph
				        		tripleString = tripleString + rdf_descriptor_Start + in + "#Paragraph_"+ counter + rdf_kopf_Schluss; 
				        		// triple inhalt rein
				        		tripleString = tripleString + "<hsrmtt:style>"+pElement.getAttribute("w:val")+"</hsrmtt:style>";
				        		// descriptor und rdf schliesen
				        		tripleString = tripleString + rdf_descriptor_ende;
							}
				        	rdf = rdf + tripleString;
//				        	stringZwischenSpeicherer (tripleString);
//				        	jenaReadWrite();
							counter++;
							
				        }
				        docxFile.close();		
						System.out.println("\n\n");
					} catch (SAXException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				}catch (Exception e) {
					System.out.println("****** Irgendein Fehler bei der Traversierung******* \n");
				}	

			
				System.out.println("****** DURCHGELAUFEN ******* \n");
				rdf = rdf + rdf_gesamt_ende; 
	        	stringZwischenSpeicherer (rdf);
	        	jenaReadWrite();
				
			
		}  
	  
	
	public static Metadata getMetaDates(String in) throws IOException, SAXException, TikaException {

		  
		
		 //detecting the file type
	      ToXMLContentHandler handler = new ToXMLContentHandler(); // BodyContentHandler();
	      Metadata metadata = new Metadata();
	      FileInputStream inputstream = new FileInputStream(new File(in));
	      ParseContext pcontext = new ParseContext();
	      
	      //OOXml parser
	      OOXMLParser  msofficeparser = new OOXMLParser (); 
	      msofficeparser.parse(inputstream, handler, metadata,pcontext);
	      return metadata;
	}
	
	
	public static void processMetaDates(Metadata md, String in) throws IOException, SAXException, TikaException {

		 // RDF aufmachen und Basisangaben mit vordefinierten Tags sowie File-Ressource
		  //String metaDates ="<?xml version='1.0' encoding='UTF-8'?> <rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:hsrmtt='http://www.hsrm.de/somewhere/TT/'>   <rdf:Description rdf:about='file:///"+in+"'>";
		  String metaDates =rdf_kopf + rdf_descriptor_Start+in+rdf_kopf_Schluss;
			      
		  System.out.println("Metadata of the document:");
	      String[] metadataNames = md.names();
	  
	      System.out.println("****** Analyse des Dokuments: Metadaten mittels Tika Ausgabe als Konsolnen-Text ******* \n");
	      System.out.println("****** Parallel Erzeugung der RDF-Beschreibungen der Ergebnisse ******* \n");		
	      
	      for(String name : metadataNames) {
	         
	         switch(name){
	         case "dc:creator":
	             System.out.println("Creator-Triple: " + name + ": " + md.get(name));
	             metaDates = metaDates +"<dc:creator>"+md.get(name)+"</dc:creator>";
	             break;
	         case "Last-Author":
	             System.out.println("Contributer-Triple: " + name + ": " + md.get(name));
	             metaDates = metaDates +"<dc:contributor>"+md.get(name)+"</dc:contributor>";
	             break;
	         default:
	        	 System.out.println("\t\t "+ name + ": " + md.get(name));
	         } 
	      }
	      
	      // RDF schliessen mit vordefinierten Tags
	      //metaDates = metaDates + "</rdf:Description></rdf:RDF>";
	      metaDates = metaDates + rdf_descriptor_ende + rdf_gesamt_ende;
	      
	      stringZwischenSpeicherer (metaDates);
	      jenaReadWrite();
	}
	

// RDF-Triple erzeugen mit Benennung des Quell-Dokument-Types nach Namespace HSRM TT
	
	public static void sourceTypeIdentifierRDF(String in){

		 // RDF aufmachen und Basisangaben mit vordefinierten Tags sowie File-Ressource
		  String sourceType =rdf_kopf + rdf_descriptor_Start+in+rdf_kopf_Schluss;
			      
	      System.out.println("****** Erzeugung einer vordefinieten RDF-Beschreibungen Eingabe-Types (Word-Git-Jira) in RAF ns-HSRMTT ******* \n");		
	      
          sourceType = sourceType +"<hsrmtt:sourcetype>"+"Word-Document"+"</hsrmtt:sourcetype>";
	          
	      
	      // RDF schliessen mit vordefinierten Tags
	      sourceType = sourceType + rdf_descriptor_ende + rdf_gesamt_ende;
	      
	      stringZwischenSpeicherer (sourceType);
	      jenaReadWrite();
	}

	
	
// Model-Triple direkt als SPO-Statement erzeugen mit Benennung des Quell-Dokument-Types nach Namespace HSRM TT
	
		public static void sourceTypeIdentifierJena(String in, Model m){

			// m = ModelFactory.createDefaultModel();
			
			m.setNsPrefix( "hsrmtt", ttNamespace );
			
			Resource res1 = m.createResource("file:///"+in);
			
			Property st = ResourceFactory.createProperty(ttNamespace, "SourceType");
			
			res1.addProperty(st, "Word-DOC");
			
		    System.out.println("****** Internes Modell im soureTypeIdentifierJena ******* \n");		
		       
			m.write(System.out); 

		}
	

	public static void jenaReadWrite() {
				
		// liest existierendes RDF-Model ein
		// und gibt es auf die Standardausgabe aus
		
		//Model model = ModelFactory.createDefaultModel();
		// aktuell hier auskommentiert und einmalig in der Main geoeffnet.
		// dadurch ist mehrmaliges Einlesen unterschiedlicher RDF-Files in ein Model moeglich
		// nach jedem Einlesen erfolgt eine Ausgabe des kompletten, akttuellen Stand des Modells

		//String inputFileName = "bin/KMB-RDF-Beta-1.rdf"; // ist ein File im RDF-XML-Format

		// ist die Arbeitsdatei in die die verschiedenen Analysatoren ihre jeweiligen RDFs speichern
		String inputFileName = "Sprint.xml";
		
        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        
        // read the RDF/XML file
        model.read(in, "");
                    
        System.out.println("****** Ausgabe des in den Jena-Triple-Store eingelesenen Models als RDF aus dem Triple-Store *******\n");
        // write it to standard out
        model.write(System.out); 
        System.out.println("\n\n");
        
        System.out.println("****** Ausgabe des in den Jena-Triple-Store eingelesenen Models als N-Triple in .nt-Datei *******\n");
        // save as model in Ergebnisordner
		String outputFileName = "C:/Users/dr_be_000/Dropbox/KMB-Mini-KMB-Klaus/Informatik/WP/Ergebnisse/N-Triple.nt";
		
        OutputStream out;
		try {
			out = new FileOutputStream( outputFileName );
			model.write(out, "N-TRIPLES");
		} catch (FileNotFoundException e) {
			
			System.out.println("Model konnte nicht gespeichert werden *\n" + e.getMessage());
		}
		
	}
	
	// Hilfsmethoden
	
	public static void stringZwischenSpeicherer(String in) {
	 // Ausgabe in Datei
	    Writer fw = null;
	    
	    try
	    {
	      fw = new FileWriter( "Sprint.xml" );
	      fw.write( in);
	    }
	    catch ( IOException e ) {
	      System.err.println( "Konnte Datei nicht erstellen" );
	    }
	    finally {
	      if ( fw != null )
	        try { fw.close(); } catch ( IOException e ) { e.printStackTrace(); }
	    }

	}
	
	public static String fileChooser(){
	    JFileChooser fc = new JFileChooser();

	    int state = fc.showOpenDialog( null );

	    if ( state == JFileChooser.APPROVE_OPTION )
	    {
	      File file = fc.getSelectedFile();
	      System.out.println( file.getPath());
	      return file.getPath();
	    }
	    else
	      System.out.println( "Auswahl abgebrochen" );
		return null;

	  }
		
}
