package sprint01;


import org.apache.jena.rdf.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.jena.util.FileManager;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.SAXException;


public class Sprint01_Triple_Extractor_Beta {
	
	public static void main(String[] args) throws IOException, SAXException, TikaException {
		
		//String inputFile = "C:/Users/dr_be_000/Dropbox/KMB-Mini-KMB-Klaus/Informatik/WP/Quellmaterial/Docx/Benutzeranleitung DFAT.docx";
		
		String inputFile = fileChooser();
		
		Metadata md = getMetaDates(inputFile);
		processMetaDates(md, inputFile);
		
		jenaReadWrite();
	}
	
	
	  public static String fileChooser()
	  {
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

		 // RDF aufmachen und Basisangaben
		  String test ="<?xml version='1.0' encoding='UTF-8'?> <rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:hsrmtt='http://www.hsrm.de/somewhere/TT/'>   <rdf:Description rdf:about='file:///"+in+"'>";
		      
		  System.out.println("Metadata of the document:");
	      String[] metadataNames = md.names();
	      
	      for(String name : metadataNames) {
	         
	         switch(name){
	         case "dc:creator":
	             System.out.println("Creator-Triple: " + name + ": " + md.get(name));
	             test = test +"<dc:creator>"+md.get(name)+"</dc:creator>";
	             break;
	         case "Last-Author":
	             System.out.println("Contributer-Triple: " + name + ": " + md.get(name));
	             test = test +"<dc:contributor>"+md.get(name)+"</dc:contributor>";
	             break;
	         default:
	        	 System.out.println("\t\t "+ name + ": " + md.get(name));
	         } 
	      }
	      
	      // RDF schliessen
	      test = test + "</rdf:Description></rdf:RDF>";
	      
	      // Ausgabe in Datei
	      Writer fw = null;
	      
	      
	      try
	      {
	        fw = new FileWriter( "Sprint.xml" );
	        fw.write( test );
	      }
	      catch ( IOException e ) {
	        System.err.println( "Konnte Datei nicht erstellen" );
	      }
	      finally {
	        if ( fw != null )
	          try { fw.close(); } catch ( IOException e ) { e.printStackTrace(); }
	      }
	
	}
	
	

	public static void jenaReadWrite() {
				
		// liest existierendes RDF-Model ein
		// und gibt es auf die Standardausgabe aus
		
		Model model = ModelFactory.createDefaultModel();

		//String inputFileName = "bin/KMB-RDF-Beta-1.rdf"; // ist ein File im RDF-XML-Format

		String inputFileName = "Sprint.xml";
		
        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        
        // read the RDF/XML file
        model.read(in, "");
                    
        System.out.println("****** Ausgabe eingelesenes RDF*******\n");
        // write it to standard out
        model.write(System.out); 
        System.out.println("\n\n");
		
	}

		
		
}
