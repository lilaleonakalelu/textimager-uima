package org.hucompute.textimager.uima.julie;

import de.julielab.jcore.types.Sentence;
import de.julielab.jcore.types.Token;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertArrayEquals;
/**
 * jpos
 *
 * @date 13.08.2021
 *
 * @author Grzegorz Siwiecki, Chieh Kang
 * @version 1.1
 *
 * This class provide jpos test case */
public class jposTest {
    public void init_jcas(JCas jcas, String text) {
        //split sentence to tokens
        String[] words = text.split(" ");

        Sentence sentence = new Sentence(jcas, 0, text.length());
        sentence.addToIndexes();
        //initialize index
        int index_start = 0;
        int index_end = 0;

        //loop for all words
        for (String word : words) {
            Token token = new Token(jcas);
            index_end = index_start + word.length();
            token.setBegin(index_start);
            token.setEnd(index_end);
            token.addToIndexes();
            index_start = index_end + 1;
        }
    }
    @Test
    public void testProcess() throws IOException, UIMAException {
        String Text = "Der kleine Baum";
        JCas jCas = JCasFactory.createText(Text);
        init_jcas(jCas, Text);
        AnalysisEngineDescription engine = createEngineDescription(Jpos.class);

        SimplePipeline.runPipeline(jCas, engine);

        String[] casPostag= (String[]) JCasUtil.select(jCas, Token.class).stream().map(a -> a.getPosTag(0).getValue()).toArray(String[]::new);

        String[] testPostag = new String[] {"ART", "ADJA", "NN"};

        assertArrayEquals(testPostag, casPostag);
    }
}