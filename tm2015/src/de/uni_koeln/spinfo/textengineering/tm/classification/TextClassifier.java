/**
 * Material for the course 'Text-Engineering', University of Cologne.
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
package de.uni_koeln.spinfo.textengineering.tm.classification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.textengineering.tm.document.Document;

/**
 * Text classification delegating the actual classification to a classifier
 * strategy.
 * @author Fabian Steeg
 */
public class TextClassifier {

	private ClassifierStrategy classifier;

    /**
     * @param classifier The classifier strategy to use for text classification
     * @param trainingSet The training set for this classifier
     */
	public TextClassifier(ClassifierStrategy strategy, Set<Document> trainingSet) {
		this.classifier = strategy;
		train(trainingSet);
	}

	private void train(Set<Document> trainingSet) {
        /* Wir trainieren mit jedem Dokument: */
		for (Document document : trainingSet) {
            /* Delegieren das eigentliche Training an unsere Strategie: */
			this.classifier = classifier.train(document);
		}
	}

    /**
     * @param documents The documents to classify
     * @return A mapping of documents to their class labels
     */
	public Map<Document, String> classify(Set<Document> testSet) {
		
		Map<Document, String> resultClasses = new HashMap<Document, String>();
		for (Document document : testSet) {
            /* Wie beim Training delegieren wir an die Strategie: */
			String classLabel = classifier.classify(document);
            /*
             * Und speichern die Ergebnisse in einer Map:
             */
			resultClasses.put(document, classLabel);
		}
		return resultClasses;
	}

    public Double evaluate(Map<Document, String> resultClasses,
            ArrayList<Document> gold) {
        /* Wir zählen die Anzahl der Übereinstimmungen: */
        int same = 0;
        for (Document document : gold) {
            String classLabel = resultClasses.get(document);
            if (classLabel.equalsIgnoreCase(document.getTopic())) {
                same++;
            }
        }
        /* Und berechnen daraus den Anteil korrekter Werte: */
        return same / (double) gold.size();
    }
	
}
