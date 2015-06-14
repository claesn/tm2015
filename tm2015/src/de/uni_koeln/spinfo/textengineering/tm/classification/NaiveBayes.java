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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.textengineering.tm.document.Document;

/**
 * Naive bayes classifier strategy to use for text classification.
 * 
 * @author Fabian Steeg
 */
public class NaiveBayes implements ClassifierStrategy {

	/** Total number of documents */
	private int docCount = 0;
	/** Number of documents for each class */
	private Map<String, Integer> classFrequencies = new HashMap<String, Integer>();
	/**
	 * For each class, we map a mapping of all the terms of that class to their term frequencies:
	 */
	private Map<String, Map<String, Integer>> termFrequenciesForClasses = new HashMap<String, Map<String, Integer>>();

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.classification.ClassifierStrategy#train(de.uni_koeln.spinfo.textengineering.tm.document.Document,
	 *      java.lang.String)
	 */
	@Override
	public ClassifierStrategy train(Document document) {
		/* Als "Klasse" des Dokuments nehmen wir sein topic */
		String c = document.getTopic();
		/*
		 * Wir zählen mit, wie viele Dokumente wir insgesamt haben, für die Berechnung der A-Priori-Wahrscheinlichkeit
		 * ('prior probability')
		 */
		docCount++;
		Integer classFreq = classFrequencies.get(c);
		if (classFreq == null) {
			/* Erstes Vorkommen der Klasse: */
			classFreq = 0;
		}
		classFrequencies.put(c, classFreq + 1);
		/*
		 * Für die Evidenz: Häufigkeit eines Terms in den Dokumenten einer Klasse.
		 */
		Map<String, Integer> termFreqs = termFrequenciesForClasses.get(c);
		if (termFreqs == null) {
			/* Erstes Vorkommen der Klasse: */
			termFreqs = new HashMap<String, Integer>();
		}
		/* Jetzt für jeden Term hochzählen: */
		for (String term : document.getTerms()) {
			Integer count = termFreqs.get(term);
			if (count == null) {
				/* Erstes Vorkommen des Terms: */
				count = 0;
			}
			/*
			 * Wir addieren hier die Termfrequenz (also die Häufigkeit des Terms im Dokument), die wir direkt aus dem
			 * Dokument bekommen. Die verschiedenen Classifier-Strategien sind somit zwar austauschbar, sie können
			 * jedoch in dieser Umsetzung nur mit unseren Document-Implementierungen zusammenarbeiten.
			 */
			termFreqs.put(term, count + document.getTermFrequencyOf(term));
		}
		termFrequenciesForClasses.put(c, termFreqs);
		return this;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.uni_koeln.spinfo.textengineering.tm.classification.ClassifierStrategy#classify(de.uni_koeln.spinfo.textengineering.tm.document.Document)
	 */
	@Override
	public String classify(Document document) {
		/* Das Maximum ... */
		double max = Double.NEGATIVE_INFINITY;
		Set<String> classes = termFrequenciesForClasses.keySet();
		/* ... bzw. die beste ... */
		String best = classes.iterator().next();
		/* ... der möglichen Klassen ... */
		for (String c : classes) {
			/* ... ist die Summe aus a-priori-Wahrscheinlichkeit ... */
			double prior = prior(c);
			/* ... und der Summe der Termwahrscheinlichkeiten: */
			double evidence = 0d;
			for (String term : document.getTerms()) {
				double cp = condprob(term, c);
				evidence = (double) (evidence + Math.log(cp));
			}
			/* Die eigentliche Naive-Bayes Berechnung: */
			double prob = prior + evidence;
			/* Und davon das Maximum: */
			if (prob >= max) {
				max = prob;
				best = c;
			}
		}
		return best;
	}

	private double prior(String c) {
		/* Die relative Frequenz der Klasse (a-priori-Wahrscheinlichkeit) */
		Integer classFreq = classFrequencies.get(c);
		double prior = (double) Math.log(classFreq / (double) docCount);
		return prior;
	}

	private double condprob(String term, String c) {
		Map<String, Integer> termFreqs = termFrequenciesForClasses.get(c);
		Integer tf = termFreqs.get(term);
		double condprob;
		if (tf != null) {
			condprob = tf / sum(termFreqs);
		} else {
			condprob = Double.NEGATIVE_INFINITY;
		}
		/*
		 * Wenn ein Term in den Trainingsdokumenten für die Klasse nicht vorkommt, ist die Evidenz unendlich klein (und
		 * nicht 0). Laut Theorie soll das über das sog. "Add-one-smoothing" gelöst werden, also:
		 * 
		 * condprob = (tf + 1) / (double) (sum(termFreqs) + (double) termFreqs.size());
		 * 
		 * bzw. wenn tf = null:
		 * 
		 * condprob = 1 / (double) (sum(termFreqs) + (double) termFreqs.size());
		 * 
		 * Dies soll verhindern, dass Terme nur aufgrund der Abwesenheit im Trainingsset negativ gewichtet werden, indem
		 * man immer einen Mindestwert von '1' annimmt. In unseren Beispielen führt das jedoch zu Problemen, weil es
		 * Klassen gibt, deren Dokumente sehr wenig Text enthalten (z.B. Welt: "autor", Spiegel: "fotostrecke"), und
		 * deshalb bei längeren Dokumenten für (fast) alle Terme zumindest die minimale Indikation zu diesen Klassen
		 * annehmen - was in der Summe eine zu starke Indikation ergibt... deshalb hier mit NEGATIVE_INFINITY.
		 */
		return condprob;
	}

	/* Die Summe der Häufigkeiten aller Termfrequenzen für die Klasse: */
	private double sum(Map<String, Integer> termFreqs) {
		int sum = 0;
		for (Integer i : termFreqs.values()) {
			sum += i;
		}
		return sum;
	}

    /**
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    

}
