package de.uni_koeln.spinfo.textengineering.ir.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class Indexer {

	// das Herzstück der Lucene-Indexierung ist der sog. IndexWriter:
	private IndexWriter writer;
	private int numDocs;

	public Indexer(String indexDir) throws IOException {
		/* Das Verzeichnis, in dem der Index gespeichert wird: */
		Directory luceneDir = new SimpleFSDirectory(new File(indexDir).toPath());
		/* Der Analyzer ist für das Preprocessing zuständig (Tokenizing etc) */
		Analyzer analyzer = new StandardAnalyzer();
		/* Der IndexWriter wird mit dem Analyzer konfiguriert: */
		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(luceneDir, conf);
	}

	/*
	 * Wenn unser Korpus aus Lucene-Documents besteht, sind die Schritte A.1 ("acquire content") und A.2
	 * ("build document") bereits abgehakt und die Dokumente können hier ganz einfach zum Index hinzugefügt werden:
	 */
	public void index(Corpus corpus) throws IOException {
		writer.deleteAll();
		List<Document> works = corpus.getWorks();
		for (Document work : works) {
			/*
			 * A.3 + A.4 (Analyse und Indexierung) sind bei Lucene gekapselt: Der Writer wird mit einem Analyzer
			 * initialisiert.
			 */
			writer.addDocument(work);
			System.out.print(".");
		}
		numDocs = writer.numDocs();
		System.out.println(" " + numDocs + " Dokumente hinzugefügt.");
		writer.close();
	}

	/*
	 * Falls nicht, dann müssen wir die Documents hier zunächst noch selbst erstellen (A.1 - "acquire data" sowie A.2 -
	 * 'build document') und dann jeweils einzeln zum Index hinzufügen. Das ist im Grunde der allgemeinere Fall: Lesen
	 * von Textdateien mittels eines einfachen Verzeichnis-Crawlers.
	 */
	public void index(String data) throws Exception {
		writer.deleteAll();
		/* Schritt A.1: Acquire data (Dateien einlesen) */
		File[] files = new File(data).listFiles();
		if (files == null) {
			System.out.println(" - Verzeichnis nicht gefunden!");
		} else {
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				if (f.isDirectory()) {
					index(f.getAbsolutePath());// rekursiv in Unterverzeichnisse
				}
				if (f.getName().endsWith(".txt") && f.exists()) {// nur *.txt
					/* Schritt A.2: build document */
					Document doc = buildLuceneDocument(f);
					/* Schritte A.3 + A.4: analyze + index document */
					writer.addDocument(doc);
					System.out.print(".");
				}
			}
			numDocs = writer.numDocs();
			System.out.println(" " + numDocs + " Dokumente hinzugefügt.");
			writer.close();
		}
	}

	/*
	 * Schritt A.2: 'build document' - Die Klasse Document ist ein Container für sog. 'Fields', welche die eigentlichen
	 * Daten kapseln. Strukturell ähnelt ein Field einer Map<Key, Value>, d.h. auf einen Key (ID) wird ein Value
	 * (textuelle Daten) abgebildet.
	 */
	private Document buildLuceneDocument(File f) throws Exception {
		String work = readFile(f);
		/*
		 * Den Dateiinhalt zunächst auszulesen ermöglicht ein eigenes Parsing, um die Inhalte gezielt verschiedenen
		 * 'Fields' zuzuordnen:
		 */
		Document doc = new Document();
		doc.add(new TextField("contents", work, Store.NO));
		doc.add(new TextField("text", work, Store.YES));
		/* Den Titel ermitteln wir analog zu unserem bisherigen Vorgehen: */
		String title = (work.trim().substring(0, work.trim().indexOf("\n"))).trim();
		doc.add(new TextField("title", title, Store.YES));
		/*
		 * Um eine zu unseren bisherigen Ergebnissen vergleichbare docId zu nutzen, können wir z.B. den CorpusSplitter
		 * zweckentfremden und die docId über den Dateinamen weitergeben. Alternativ (und eigentlich auch sauberer),
		 * könnte die docId beim Erstellen der Dateien im Sinne von einfachen Metadaten mit in die Datei geschrieben
		 * werden, und dann z.B. analog zum Titel extrahiert werden.
		 */
		String docId = f.getName().split("-")[0];
		doc.add(new IntField("docId", Integer.parseInt(docId), Store.YES));
		/* Noch ein Beispiel: Zeitpunkt der Indexierung: */
		doc.add(new StringField("indexDate", DateTools.dateToString(new Date(), DateTools.Resolution.MINUTE),
				Field.Store.YES));
		/* ... und der Dateiname: */
		doc.add(new StringField("filename", f.getCanonicalPath(), Store.YES));
		return doc;
	}

	/*
	 * Hilfsmethode, liest den Inhalt von f auf einen String.
	 */
	private String readFile(File f) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		StringBuffer sb = new StringBuffer();
		String line = "";
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		reader.close();
		return sb.toString();
	}

	/*
	 * Hilfsmethode für unsere Tests.
	 */
	public int getNumDocs() {
		return numDocs;
	}

	public void close() throws IOException {
		writer.close();// nicht vergessen ...
	}
}