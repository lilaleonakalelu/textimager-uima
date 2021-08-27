package org.hucompute.textimager.uima.julie;

import de.julielab.jcore.types.Sentence;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.hucompute.textimager.uima.julie.reader.JsonReader;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.IOException;

public class OpennlpSentence extends JulieBase {
    /**
     * Tagger address.
     * @return endpoint
     */
    @Override
    protected String getRestRoute() {
        return "/opennlpSentence";
    }

    @Override
    protected String getAnnotatorVersion() {
        return "0.0.1";
    }
    /**
     * Read Json and update jCas.
     * @param aJCas
     */
    @Override
    protected void updateCAS(JCas aJCas, JSONObject jsonResult) throws AnalysisEngineProcessException {
        try {
            JsonReader reader = new JsonReader();
            reader.UpdateJsonToCas(jsonResult, aJCas);

            for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
                de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence dsentence = new de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence(aJCas, sentence.getBegin(), sentence.getEnd());
                dsentence.addToIndexes();
            }
        }
        catch (UIMAException | IOException | SAXException ex) {
            throw new AnalysisEngineProcessException(ex);
        }

    }
}
