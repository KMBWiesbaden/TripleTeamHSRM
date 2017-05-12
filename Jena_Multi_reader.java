package sprint02;


import org.apache.jena.rdf.model.*;

import java.io.InputStream;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.util.FileManager;


public class Jena_Multi_reader {

	public static void main(String[] args) {
		
		
		// existierendes RDF-Model einlesen
		
		Model model = ModelFactory.createDefaultModel();

		String inputFileName = "N-Triple.nt"; // ist ein File im N-Triple-Format
		
//        InputStream in = FileManager.get().open( inputFileName );
//        if (in == null) {
//            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
//        }
//        
        // read the N-Triple file
        model.read(inputFileName, "N-TRIPLES");
        //model.read(in, "");
		
        
        inputFileName = "KMB-RDF-Beta-Fake.rdf"; // ist ein File im RDF-XML-Format
		
        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        
        // read the RDF/XML file - geht auf beide Wege
		//model.read(inputFileName, "RDF/XML");
        model.read(in, "");
//                    
        System.out.println("****** Ausgabe eingelesene Models (N-Triple+RDF)*******\n");
        // write it to standard out
        model.write(System.out); 
        System.out.println("\n\n");
        
        
        System.out.println("****** Beispiel-Suche im Modell nach allen Paragraphen mit Stilelementen*******\n");
        String ttNamespace = "http://www.hsrm.de/somewhere/TT/";
        model.setNsPrefix( "hsrmtt", ttNamespace );
		
		Property st = ResourceFactory.createProperty(ttNamespace, "style");
		Property ih = ResourceFactory.createProperty(ttNamespace, "inhalt");
        
		// select all the resources with a VCARD.FN property
		ResIterator iter = model.listSubjectsWithProperty(st);
		if (iter.hasNext()) {
		    System.out.println("Die eingelesene Datenbank enthaelt folgende Stil-Elemente - via Ressource:");
		    while (iter.hasNext()) {
		        System.out.println("  " + iter.nextResource()	        
		                                      .getProperty(st)
		                                      .getString());
		    }
		} else {
		    System.out.println("Keine Stilelemente im Text vergeben.");
		}
		System.out.println("\n\n");
		
        StmtIterator iter2 = model.listStatements(
        	    new SimpleSelector(null, st, (RDFNode) null) {
        	        public boolean selects(Statement s)
        	            {return !(s.getString().isEmpty());}
        	    });
        
		if (iter2.hasNext()) {
		    System.out.println("Die eingelesene Datenbank enthaelt folgende Paragraphen mit Stil-Elemente - via Statement:");
		    while (iter2.hasNext()) {
		    	Statement state = iter2.nextStatement();
		    	Resource paragraph = model.getResource(state.getSubject().toString());
		    	String inhalt = paragraph.getProperty(ih).getString();
		    	System.out.println("Der Paragraph: " + state.getSubject().toString() + " hat diesen Inhalt: " + inhalt + " mit diesem Stilelement: " + state.getObject().toString());
		    }
		    
		} else {
		    System.out.println("Keine Stilelemente im Text vergeben.");
		}
		System.out.println("\n\n");
		
		
		// Statements ohne Praedikat 'Style'
        StmtIterator iter3 = model.listStatements(
        	    new SimpleSelector(null, null, (RDFNode) null));
        
		if (iter3.hasNext()) {
		    System.out.println("Die eingelesene Datenbank enthaelt folgende Paragraphen ohne Stil-Elemente - via Statement:");
		    while (iter3.hasNext()) {
		    	Statement state = iter3.nextStatement();
		    	Resource paragraph = model.getResource(state.getSubject().toString());
		    	if (paragraph.hasProperty(ih)&&(!paragraph.hasProperty(st))){
		    		System.out.println("Der Paragraph: " + state.getSubject().toString() + " hat diesen Inhalt: " + state.getObject().toString());  	
		    	}
		    }
		    
		} else {
		    System.out.println("Keine Stilelemente im Text vergeben.");
		}
		System.out.println("\n\n");
		
	}

		
		
}
