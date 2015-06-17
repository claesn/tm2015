/**
 * Material for the course 'Text-Mining', University of Cologne.
 * (http://www.spinfo.phil-fak.uni-koeln.de/spinfo-textmining.html)
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
package de.uni_koeln.spinfo.textengineering.tm.classification.weka;

import java.util.Set;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.SparseInstance;
import de.uni_koeln.spinfo.textengineering.tm.classification.ClassifierStrategy;
import de.uni_koeln.spinfo.textengineering.tm.corpus.Corpus;
import de.uni_koeln.spinfo.textengineering.tm.document.Document;
import de.uni_koeln.spinfo.textengineering.tm.document.FeatureVector;
import de.uni_koeln.spinfo.textengineering.tm.document.WebDocument;

/**
 * Adapter for Weka classifiers.
 * 
 * @author Fabian Steeg, Claes Neuefeind
 */
public class WekaAdapter implements ClassifierStrategy {
	private Classifier wekaClassifier;
	Corpus corpus;
	
	public WekaAdapter(Classifier classifier, Set<Document> trainingSet, Corpus c) {
		this.wekaClassifier = classifier;
		this.corpus = c;
		// TODO Fuer Weka brauchen wir jetzt ein paar Sachen:
		// 1. Die Groesse des Merkmalsvektors
		// 2. Die moeglichen Klassen
		// 3. Die Struktur der Trainingsdaten
	}

	@Override
	public ClassifierStrategy train(Document document) {
		// TODO Bei Weka können wir wieder dokumentweise trainieren ...
		return null;
	}

	@Override
	public String classify(Document document){
		
		try {
			double classifyInstance = wekaClassifier.classifyInstance(instance(document));
			// TODO mit dem double müssen wir den Klassennamen rekonstruieren ...
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Instance instance(Document document) {
		
		WebDocument d = (WebDocument) document;
		FeatureVector vector = d.getVector(corpus);
		String topic = d.getTopic();
		//TODO: vec in instance packen, topic in instance packen ...
		Instance instance = new SparseInstance(vector.getValues().size() + 1);
		
		return instance ;
	}

}
