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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

/**
 * Wrapper class for Lucene's IndexSearcher.
 * 
 * @author Claes Neuefeind
 */

public class Searcher {

	private IndexSearcher searcher;
	private DirectoryReader reader;
	private int totalHits;

	public Searcher(String indexDir) throws IOException {
		/* Das Index-Verzeichnis: */
		Directory directory = new SimpleFSDirectory(new File(indexDir).toPath());
		/*
		 * Der IndexSearcher ist im Wesentlichen ein Wrapper um einen Reader, der für den Lese-Zugriff auf das
		 * Index-Verzeichnis zuständig ist:
		 */
		reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
	}

	/*
	 * Anders als im Seminar wird das Index-Feld mit übergeben, um gezielt nach 'topic' oder 'source' suchen zu können.
	 */
	public void search(String searchPhrase, String field, int noOfHitsToDisplay) throws ParseException, IOException {

		/* Die searchPhrase muss in ein Query-Objekt überführt werden: */
		Query query;
		/*
		 * Im 'Normalfall' können wir hierfür eine einfache TermQuery erstellen - eine Ausnahme ist das Feld 'source':
		 * Um hier eine "contains()"-Anfrage machen zu können, nutzen wir einen QueryParser, um die searchPhrase mit
		 * 'Wildcards' anzureichern (Achtung: 'leading wildcards' sind extrem rechenintensiv!).
		 */
		if (!field.equals("source")) {
			query = new TermQuery(new Term(field, searchPhrase));
		} else {
			QueryParser parser = new QueryParser(field, new StandardAnalyzer());
			parser.setAllowLeadingWildcard(true);// Problem hier: Sehr rechenintensiv!
			searchPhrase = "*" + searchPhrase + "*";
			query = parser.parse(searchPhrase);
		}
		System.out.println("query: " + query);

		/* Anschließend geben wir das Query-Objekt an eine Lucene-eigene search-Methode weiter: */
		TopDocs topDocs = searcher.search(query, noOfHitsToDisplay);
		totalHits = topDocs.totalHits;
		/* ... und geben das Ergebnis aus: */
		System.out.println(totalHits + " Treffer für " + searchPhrase + " (zeige erste "
				+ Math.min(totalHits, noOfHitsToDisplay) + "):");
		renderResults(topDocs);
	}

	/*
	 * Bei der Konsolenausgabe können wir analog zur Indexierung die enthaltenen Felder gezielt abfragen.
	 */
	private void renderResults(TopDocs topDocs) throws IOException {
		for (int i = 0; i < topDocs.scoreDocs.length; i++) {
			ScoreDoc scoreDoc = topDocs.scoreDocs[i];
			Document doc = searcher.doc(scoreDoc.doc);
			System.out.print("Document: " + doc.get("source"));
			System.out.print(" (vom: " + doc.get("indexDate") + ") -- ");
			System.out.println("topic: " + doc.get("topic"));
		}
	}

	/*
	 * Beim Umgang mit Ressourcen ist es immer gut, diese explizit freizugeben.
	 */
	public void close() throws IOException {
		reader.close();
	}

	/*
	 * Hilfsmethode für Assertions in unseren Tests.
	 */
	public int totalHits() {
		return totalHits;
	}

	public DirectoryReader getReader() {
		return reader;
	}

}
