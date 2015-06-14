package de.uni_koeln.spinfo.textengineering.tm.classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus;
import de.uni_koeln.spinfo.textengineering.tm.corpus.CorpusDatabase;
import de.uni_koeln.spinfo.textengineering.tm.corpus.crawler.Crawler;
import de.uni_koeln.spinfo.textengineering.tm.document.Document;
import de.uni_koeln.spinfo.textengineering.tm.document.WebDocument;

/**
 * Material for the course 'Information-Retrieval and Text-Mining', University of Cologne.
 * (http://www.spinfo.phil-fak.uni-koeln.de/spinfo-textengineering.html)
 * 
 * @author Fabian Steeg, Claes Neuefeind
 */
public class TestClassifier {
	/*
	 * Textklassifikation: Delegation und Strategie für austauschbare
	 * Klassifikationsverfahren (wird schrittweise ausgebaut).
	 */
	private static final String DATA = "data/corpus-tm-3.db";
	private Corpus corpus;
	private Set<Document> testSet;
	private Set<Document> trainingSet;
	private ArrayList<Document> goldSet;
	private TextClassifier classifier;

	
	public static void main(final String[] args) {
		/* Hier (= Run as -> Java application) erstellen und crawlen (dauert). */
		Corpus c = CorpusDatabase.create(DATA);
		List<String> seed = Arrays.asList("http://www.spiegel.de", "http://www.welt.de");
		List<WebDocument> documents = Crawler.crawl(1, seed);
		c.addAll(documents);
	}

	@Before
	public void before() {
		/* Hier (vor jedem Test) nur öffnen. */
		corpus = CorpusDatabase.open(DATA);
		System.out.println("------------------------------------------------");
	}

	@After
	public void after() {
		/* Hier (nach jedem Test) schliessen. */
		corpus.close();
	}

	@Test
	public void welt() {
		/*
		 * Für unser Beispiel hier trainieren und klassifizieren wir mit den gleichen Dokumenten...
		 */
		String query = "welt";
		trainingSet = new HashSet<Document>(corpus.getDocumentsForSource(query));
		testSet = new HashSet<Document>(corpus.getDocumentsForSource(query));
		testEval(query);

	}

	@Test
	public void spiegel() {
		String query = "spiegel";
		trainingSet = new HashSet<Document>(corpus.getDocumentsForSource(query));
		testSet = new HashSet<Document>(corpus.getDocumentsForSource(query));
		testEval(query);

	}

	private void testEval(final String query) {
		goldSet = new ArrayList<Document>(testSet);
		classifier = new TextClassifier(new NaiveBayes(), trainingSet);
		System.out.println("Classification of documents from: " + query);
		System.out.println("------------------------------------------------");
		System.out.println("Training set: " + trainingSet.size());
		System.out.println("Test set: " + testSet.size());
		System.out.println("Gold set: " + goldSet.size());
		Map<Document,String> resultClasses = classifier.classify(testSet);
		System.out.println("Result: " + resultClasses);
		Double result = classifier.evaluate(resultClasses, goldSet);
		Assert.assertTrue("Result must not be null", result != null);
		System.out.println(String.format("Correct: %1.2f (%1.2f%%)", result, result * 100));
		
	}

}
