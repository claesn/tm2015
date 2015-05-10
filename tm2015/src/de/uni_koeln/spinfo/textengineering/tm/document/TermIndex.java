/**
 * Material for the course 'Information-Retrieval and Text-Mining', University of Cologne.
 * (http://www.spinfo.phil-fak.uni-koeln.de/spinfo-textengineering.html)
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
package de.uni_koeln.spinfo.textengineering.tm.document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.textengineering.ir.boole.Preprocessor;
import de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus;

/*
 * Klasse zur Repräsentation eines Dokuments. Ermöglicht Zugriff auf den
 * Dokumentenvektor (noch nicht implementiert).
 */
/**
 * A document implementation that indexes term frequencies.
 * 
 * @author Fabian Steeg
 */
public final class TermIndex implements Document {

	private String content;
	/*
	 * Mapping der Terme zu ihren Häufigkeiten (term frequency tf, die Häufigkeit von Termen in diesem Dokument).
	 */
	private Map<String, Integer> termsAndFrequencies;
	private FeatureVector vector;

	/**
	 * @param content
	 *            The document content, will be preprocessed using a default preprocessor
	 */
	public TermIndex(final String content) {
		this.content = content;
		this.termsAndFrequencies = new HashMap<String, Integer>();
		Preprocessor preprocessor = new Preprocessor();
		List<String> tokens = preprocessor.process(content);
		for (String token : tokens) {
			add(token);
		}
	}

	/*
	 * Man sollte /immer/ toString überschreiben, erleichtert die Verwendung der Klasse in jeder Hinsicht sehr.
	 */

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s of length %s", this.getClass().getSimpleName(), content.length());
	}

	/**
	 * @param term
	 *            The term to add
	 * @return Returns this for cascading calls
	 */
	public TermIndex add(final String term) {
		Integer integer = termsAndFrequencies.get(term);
		/*
		 * Wenn der Term noch nicht vorkam, beginnen wir seine Vorkommen zu zählen (d.h. wir setzen 1), sonst zählen wir
		 * hoch:
		 */
		termsAndFrequencies.put(term, integer == null ? 1 : integer + 1);
		/* Für kaskadierte Aufrufe praktisch: */
		return this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.document.Document#getTerms()
	 */
	public Set<String> getTerms() {
		return termsAndFrequencies.keySet();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.document.Document#getContent()
	 */
	public String getText() {
		return content;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.document.Document#getSource()
	 */
	public String getSource() {
		return "Unknown";
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.document.Document#getTermFrequencyOf(java.lang.String)
	 */
	public Integer getTermFrequencyOf(final String term) {
		return termsAndFrequencies.get(term);
	}

	/**
	 * @param corpus
	 *            The context corpus to be used when computing a vector for this document
	 * @return A feature vector representation of this document as part of the given corpus
	 */
	public FeatureVector getVector(final Corpus corpus) {
		if (corpus == null) {
			throw new IllegalStateException("Have no Corpus!");
		}
		if (vector == null) {
			vector = new TfIdfFeatures(this, corpus).vector();
		}
		return vector;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.document.Document#getLocation()
	 */
	public URL getLocation() {
		try {
			File file = File.createTempFile("tm2", null);
			FileWriter writer = new FileWriter(file);
			writer.write(this.getText());
			writer.close();
			return file.toURI().toURL();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.document.Document#getTopic()
	 */
	public String getTopic() {
		return "Unknown";
	}

}
