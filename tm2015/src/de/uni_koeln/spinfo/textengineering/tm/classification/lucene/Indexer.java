/**
 * Material for the course 'Text-Engineering', University of Cologne.
 * (http://www.spinfo.uni-koeln.de/spinfo-textengineering.html)
 * <p/>
 * Copyright (C) 2015 Claes Neuefeind
 * <p/>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 */

package de.uni_koeln.spinfo.textengineering.tm.classification.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import de.uni_koeln.spinfo.textengineering.tm.document.Document;
import de.uni_koeln.spinfo.textengineering.tm.document.WebDocument;

/**
 * Wrapper class for Lucene's IndexWriter.
 * 
 * @author Claes Neuefeind
 */
public class Indexer {

	// das Herzstück der Lucene-Indexierung ist der sog. IndexWriter:
	private IndexWriter writer;

	public Indexer(String indexDir) throws IOException {
		/* Das Verzeichnis, in dem der Index gespeichert wird: */
		Directory luceneDir = new SimpleFSDirectory(new File(indexDir).toPath());
		/* Der Analyzer ist für das Preprocessing zuständig (Tokenizing etc) */
		Analyzer analyzer = new StandardAnalyzer();
		/* Der IndexWriter wird mit dem Analyzer konfiguriert: */
		writer = new IndexWriter(luceneDir, new IndexWriterConfig(analyzer));
		System.out.println("Creating index: " + writer.getDirectory().toString());
	}

	public void addAll(List<? extends Document> docs) throws Exception {
		writer.deleteAll();
		for (Document document : docs) {
			add(document);
		}
	}

	/*
	 * Unser Document muss erst in ein Lucene-Document konvertiert werden, bevor es zum Index hinzugefügt werden kann.
	 * Um den Namenskonflikt mit der Document-Klasse von Lucene zu vermeiden (gleichnamige Klassen), wird die Klasse
	 * 'org.apache.lucene.document.Document' direkt referenziert. Alternativ könnte der Crawler auch gleich Lucene-Docs
	 * zurückgeben - was aber eine Spezialisierung bedeuten würde, und damit unserem Ansatz widerspricht ...
	 */
	public void add(Document document) throws Exception {
		writer.addDocument(buildLuceneDocument(document));
	}

	/*
	 * Beschreibung der Dokumentstruktur - nur was hier definiert wird, kann später auch abgefragt werden.
	 */
	private org.apache.lucene.document.Document buildLuceneDocument(Document document) throws Exception {
		org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
		doc.add(new TextField("text", document.getText(), Store.YES));
		doc.add(new TextField("topic", document.getTopic(), Store.YES));
		doc.add(new TextField("source", document.getSource(), Store.YES));
		/* Zeitpunkt der Indexierung: */
		doc.add(new StringField("indexDate", DateTools.dateToString(new Date(), DateTools.Resolution.MINUTE),
				Field.Store.YES));
		/*
		 * Die jeweilige Root-URL dient hier als Alternative zur 'contains'-Suche mit Wilcard-Queries (da diese extrem
		 * rechenintensiv ist - eine aus Lucene-Sicht sauberere Lösung wäre z.B. die Verwendung eines NGramTokenizer auf
		 * das source-Feld).
		 */
		doc.add(new StringField("root", extractUrlRoot((WebDocument) document), Store.YES));
		return doc;
	}

	/* Hilfsmethode zur Ermittlung der URL-root. */
	private String extractUrlRoot(WebDocument document) {
		Pattern pattern = Pattern.compile("http://[a-z]*.([^/]+?).[a-z]*/.*");
		Matcher m = pattern.matcher(document.getSource());
		if (m.find()) {
			return m.group(1);
		}
		return document.getSource();
	}

	/*
	 * Hilfsmethode für Tests.
	 */
	public int getNumDocs() {
		return writer.numDocs();
	}

	/*
	 * Löscht den bestehenden Index (optional).
	 */
	public void deleteExistingIndex() throws IOException {
		System.out.println("Delete existing Index: " + writer.getDirectory().toString());
		writer.deleteAll();
	}

	public void close() throws IOException {
		writer.close();
	}
}