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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_koeln.spinfo.textengineering.tm.classification.ClassifierStrategy;
import de.uni_koeln.spinfo.textengineering.tm.classification.NaiveBayes;
import de.uni_koeln.spinfo.textengineering.tm.classification.TextClassifier;
import de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus;
import de.uni_koeln.spinfo.textengineering.tm.corpus.CorpusDatabase;
import de.uni_koeln.spinfo.textengineering.tm.corpus.crawler.Crawler;
import de.uni_koeln.spinfo.textengineering.tm.document.Document;
import de.uni_koeln.spinfo.textengineering.tm.document.WebDocument;

/**
 * Material for the course 'Text-Engineering', University of Cologne.
 * (http://www.spinfo.phil-fak.uni-koeln.de/spinfo-textengineering.html)
 * 
 * @author Claes Neuefeind
 */
/*
 * Vergleichende Textklassifikation: Nutzung der lucene-classification-API.
 */
public class TestLuceneClassifier {
	private static final String LINE = "------------------------------------------------------------------------";
	private static final String DATA = "data/corpus-tm-4.db";
	private Corpus corpus;
	private Set<Document> testSet;
	private Set<Document> trainingSet;
	private ArrayList<Document> goldSet;
	private TextClassifier textClassifier;
	
	// für Lucene:
	private static final String indexDir = "index-tm";
	Searcher searcher;// für Index-Zugriffe (hier nur für den entsprechenden Test)

	public static void main(final String[] args) throws Exception {
		/* Hier (= Run as -> Java application) erstellen und crawlen (dauert). */
		Corpus c = CorpusDatabase.create(DATA);
		List<String> seed = Arrays.asList("http://www.spiegel.de", "http://www.welt.de");
		List<WebDocument> documents = Crawler.crawl(1, seed);
		c.addAll(documents);
		// für Lucene:
		Indexer indexer = new Indexer(indexDir);
		indexer.addAll(documents);
	}

	@Before
	public void before() {
		/* Hier (vor jedem Test) nur öffnen: */
		corpus = CorpusDatabase.open(DATA);
		System.out.println("Korpus enthält " + corpus.getNumberOfDocuments() + " Dokumente");
		try {
			/* ... und mittels Searcher Zugriff auf den Index gestatten: */
			searcher = new Searcher(indexDir);
			System.out.println("Index enthält " + searcher.getReader().numDocs() + " Dokumente");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(LINE);
	}

	@After
	public void after() throws IOException {
		/* Hier (nach jedem Test) schliessen. */
		corpus.close();
		searcher.close();
	}

	@Test
	public void testIndex() throws ParseException, IOException {

		int noOfHits = 10;// Länge der Ergebnisliste

		/* Suche nach einem String: */
		searcher.search("heute", "text", noOfHits);
		assertTrue("Das Suchergebnis sollte nicht leer sein.", searcher.totalHits() > 0);

		/* Topic-Suche: */
		searcher.search("themen", "topic", noOfHits);
		assertTrue("Das Suchergebnis sollte nicht leer sein.", searcher.totalHits() > 0);

		/* Suche nach Dokumenten aus einer bestimmten Quelle: */
		searcher.search("spiegel", "source", noOfHits);
		assertTrue("Das Suchergebnis sollte nicht leer sein.", searcher.totalHits() > 0);

		/* Source-Suche unter Verwendung der beim Indexieren extrahierten root-URL: */
		searcher.search("spiegel", "root", noOfHits);
		assertTrue("Das Suchergebnis sollte nicht leer sein.", searcher.totalHits() > 0);
	}

	@Test
	public void welt() {
		testWith("welt");
	}

	@Test
	public void spiegel() {
		testWith("spiegel");
	}

	private void testWith(final String query) {
		setupData(query);
		printInfo(query);

		// unser eigener Classifier:
		testEval(System.nanoTime(), query, new NaiveBayes());
		System.out.println(LINE);

		// und Lucene: der LuceneAdapter delegiert die Aufgabe an die Lucene-Api:
		testEval(System.nanoTime(), query, new LuceneAdapter());
		System.out.println(LINE);

	}

	private void setupData(final String query) {
		/*
		 * Im Beispiel trainieren und klassifizieren wir mit den gleichen Dokumenten und nehmen diese auch noch als
		 * Goldstandard an. Eine "richtige" Evaluation sieht getrennte Mengen vor, die mehrfach und in verschiedenen
		 * Zusammensetzungen gegen den Goldstandard getestet werden (Cross-Validation).
		 */
		trainingSet = new HashSet<Document>(corpus.getDocumentsForSource(query));
		testSet = trainingSet;
		goldSet = new ArrayList<Document>(testSet);
	}

	private void printInfo(final String query) {
		System.out.println("Classification of documents from: " + query + "... ");
		System.out.println(LINE);
		System.out.println("Training set: " + trainingSet.size());
		System.out.println("Testing set: " + testSet.size());
		System.out.println("Gold set: " + goldSet.size());
		System.out.println(LINE);
	}

	private void testEval(final long start, final String query, final ClassifierStrategy classifier) {

		//Den Classifier ...
		System.out.print(classifier + "... ");
		// ... trainiert ...
		textClassifier = new TextClassifier(classifier, trainingSet);
		// ... und eingesetzt:
		Map<Document, String> resultClasses = textClassifier.classify(testSet);
		//... Ergebnis ausgeben:
		System.out.println("Result: " + resultClasses);
		Double result = textClassifier.evaluate(resultClasses, goldSet);
		Assert.assertTrue("Result must not be null", result != null);
		//... Zeitmessung aufbereiten:
		long ns = System.nanoTime() - start;
		double ms = ns / 1000d / 1000d;
		double s = ms / 1000d;
		System.out.println(String.format("Correct: %1.2f (%1.2f%%); Time: %1.2f ms (%1.2f s.)", result, result * 100,
				ms, s));
	}

}
