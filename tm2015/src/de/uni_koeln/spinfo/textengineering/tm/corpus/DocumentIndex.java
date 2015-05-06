/**
 * Material for the course 'Information-Retrieval', University of Cologne.
 * (http://www.spinfo.phil-fak.uni-koeln.de/spinfo-informationretrieval.html)
 * <p/>
 * Copyright (C) 2008-2009 Fabian Steeg
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
package de.uni_koeln.spinfo.textengineering.tm.corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koeln.spinfo.textengineering.tm.document.Document;

/*
 * Eine Klasse zu Repraesentation des Korpus und zum Zugriff auf die Dokumente des
 * Korpus. Verwaltet die document-frequencies für alle Terme des Corpus und stellt Zugriffsmethoden bereit.
 */
/**
 * An corpus implementation indexing terms.
 * 
 * @author Fabian Steeg
 */
public final class DocumentIndex implements Corpus {

	private List<Document> docs;
	private SortedSet<String> dictionary;
	private Map<String, Integer> documentFrequencies;

	/** Creates a new indexed corpus. */
	public DocumentIndex() {
		this.dictionary = new TreeSet<String>();
		this.documentFrequencies = new HashMap<String, Integer>();
		docs = new ArrayList<Document>();
	}

	/**
	 * @param docs
	 *            The docs to add to the corpus
	 */
	public DocumentIndex(final List<Document> docs) {
		for (Document document : docs) {
			add(document);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus#getDocuments()
	 */
	public List<Document> getDocuments() {
		return docs;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus#add(de.uni_koeln.spinfo.textengineering.tm.document.Document)
	 */
	public void add(final Document document) {
		Collection<String> terms = document.getTerms();
		if (terms == null)
			System.out.println("no terms");
		for (String newTerm : terms) {
			Integer integer = documentFrequencies.get(newTerm);
			documentFrequencies.put(newTerm, integer == null ? 1 : integer + 1);
		}
		dictionary.addAll(terms);
		docs.add(document);

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus#addAll(java.util.List)
	 */
	public void addAll(final List<? extends Document> documents) {
		for (Document document : documents) {
			add(document);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus#getDocumentFrequencyOf(java.lang.String)
	 */
	public Integer getDocumentFrequencyOf(final String term) {
		return documentFrequencies.get(term);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus#getDocumentsForSource(java.lang.String)
	 */
	public List<Document> getDocumentsForSource(final String query) {
		List<Document> result = new ArrayList<Document>();
		for (Document document : docs) {
			if (document.getSource().contains(query)) {
				result.add(document);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus#getDocumentsForTopic(java.lang.String)
	 */
	public List<Document> getDocumentsForTopic(final String query) {
		List<Document> result = new ArrayList<Document>();
		for (Document document : docs) {
			if (document.getTopic().contains(query)) {
				result.add(document);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus#getTerms()
	 */
	public Set<String> getTerms() {
		return this.dictionary;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus#getNumberOfDocuments()
	 */
	public Integer getNumberOfDocuments() {
		return docs.size();
	}

	/*
	 * Ein IndexedCorpus ist nicht persistent und muss daher nichts öffnen oder schließen. Alternative Implementierungen
	 * etwa über XML-Serialisierung würde hier dann z.B. den Stream öffnen und schließen o.Ä.
	 */

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus#close()
	 */
	public void close() {
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus#open()
	 */
	public void open() {
	}

}
