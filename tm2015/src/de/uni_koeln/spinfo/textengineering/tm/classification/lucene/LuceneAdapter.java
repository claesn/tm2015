/**
 *  Material for the course 'Text-Engineering', University of Cologne.
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

import org.apache.lucene.classification.Classifier;
import org.apache.lucene.util.BytesRef;

import de.uni_koeln.spinfo.textengineering.tm.classification.ClassifierStrategy;
import de.uni_koeln.spinfo.textengineering.tm.document.Document;

/**
 * Adapter for Lucene classifiers.
 * 
 * @author Claes Neuefeind
 */
public class LuceneAdapter implements ClassifierStrategy {

	/* Wir schreiben hier einen Adapter für das Lucene-eigene Interface 'Classifier': */
	private Classifier<BytesRef> classifier;
	/* Lucene-Classifier operieren immer auf einem Index: */
	private String indexDir;

	@Override
	public ClassifierStrategy train(Document document) {

		// TODO Training in Lucene.

		return null;
	}

	@Override
	public String classify(Document document) {

		// TODO Klassifikation mit Lucene.

		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("%s for %s", getClass().getSimpleName(), classifier.getClass().getSimpleName());
	}

}
