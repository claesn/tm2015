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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
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
	private Corpus corpus;
	private int vectorSize;
	private List<String> classes;
	private Instances trainingSet;
	private boolean classifierBuilt = false;

	public WekaAdapter(Classifier classifier, Set<Document> trainingSet, Corpus c) {
		this.wekaClassifier = classifier;
		this.corpus = c;
		// Für Weka brauchen wir jetzt ein paar Sachen:
		// 1. Die Groesse des Merkmalsvektors
		WebDocument d = (WebDocument) trainingSet.iterator().next();// cast ist nötig, um an den Vektor zu kommen ...
		FeatureVector vector = d.getVector(corpus);
		this.vectorSize = vector.getValues().size();
		// 2. Die moeglichen Klassen
		this.classes = collectClasses(trainingSet);
		// 3. Die Struktur der Trainingsdaten
		this.trainingSet = initTrainingSet(trainingSet);
	}

	private List<String> collectClasses(Set<Document> trainingData) {
		Set<String> classes = new HashSet<String>();
		for (Document document : trainingData) {
			classes.add(document.getTopic());
		}
		return new ArrayList<String>(classes);
	}

	/*
	 * Hier beschreiben wir die Struktur unserer Daten, damit Weka sie interpretieren kann, hierfür wird zunächst ein
	 * Struktur-Vektor definiert, der in ein Instances-Objekt gepackt wird, das als Container für die konkreten
	 * Trainingsdaten dient.
	 */
	private Instances initTrainingSet(Set<Document> trainingData) {
		/* Der Vektor enthält die numerischen Merkmale (bei uns: tf-idf-Werte) sowie ein Klassenattribut: */
		ArrayList<Attribute> structureVector = new ArrayList<Attribute>(vectorSize + 1);
		/* Auch die Klasse wird in Weka als Vektor dargestellt: */
		ArrayList<String> classesVector = new ArrayList<String>(this.classes.size());
		for (String c : classes) {
			/*
			 * Da das Klassen-Attribut nicht numerisch ist (sondern, in Weka-Terminologie, ein nominales bzw.
			 * String-Attribut), müssen hier alle möglichen Attributwerte angegeben werden:
			 */
			classesVector.add(c);
		}
		/* An Stelle 0 unseres Strukturvektors kommt der Klassen-Vektor: */
		structureVector.add(new Attribute("topic", classesVector));
		for (int i = 0; i < vectorSize; i++) {
			/*
			 * An jeder weiteren Position unseres Merkmalsvektors haben wir ein numerisches Merkmal (repräsentiert als
			 * Attribute), dessen Name hier einfach seine Indexposition ist:
			 */
			structureVector.add(new Attribute(i + "")); // Merkmal i, d.h. was? > TF-IDF
		}
		/*
		 * Schliesslich erstellen wir einen Container, der Instanzen in der hier beschriebenen Struktur enthalten wird
		 * (also unsere Trainingsbeispiele):
		 */
		Instances result = new Instances("InstanceStructure", structureVector, vectorSize + 1);
		/*
		 * Wobei wir hier erneut angeben muessen, an welcher Stelle der Merkmalsvektoren die Klasse zu finden ist:
		 */
		result.setClassIndex(0);
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.classification.ClassifierStrategy#train(de.uni_koeln.spinfo.textengineering.tm.document.Document,
	 *      java.lang.String)
	 */
	@Override
	public ClassifierStrategy train(Document document) {
		trainingSet.add(instance(document));
		// wir merken uns, dass das Training noch nicht abgeschlossen ist ...
		classifierBuilt = false;
		return this;
	}

	private Instance instance(Document document) {

		List<Float> values = ((WebDocument) document).getVector(corpus).getValues();
		String topic = document.getTopic();

		/* Die Instanz enthält alle Merkmale: */
		double[] vals = new double[values.size() + 1];
		for (int i = 0; i < values.size(); i++) {
			vals[i + 1] = values.get(i);
		}
		Instance instance = new SparseInstance(1, vals);
		/*
		 * Weka muss 'erklärt' bekommen, was die Werte bedeuten - dies ist im Trainingsset beschrieben:
		 */
		instance.setDataset(trainingSet);
		/*
		 * Beim Training geben wir den Instanzen ein Klassenlabel, bei der Klassifikation ist die Klasse unbekannt:
		 */
		if (topic == null) {
			instance.setClassMissing(); // bei Klassifikation
		} else
			instance.setClassValue(topic); // beim Training
		return instance;
	}

	@Override
	public String classify(Document document) {
		try {
			// beim ersten Aufruf prüfen wir, ob der classifier bereits erstellt wurde:
			if (!classifierBuilt) {
				wekaClassifier.buildClassifier(trainingSet);
				classifierBuilt = true;
			}
			// Weka gibt als Ergebnis den Index des Klassen-Vektors zurück:
			int i = (int) wekaClassifier.classifyInstance(instance(document));
			// ... mit dem wir den Klassennamen rekonstruieren können:
			return classes.get(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String toString() {
		return String.format("%s for %s", getClass().getSimpleName(), wekaClassifier.getClass().getSimpleName());
	}

}
