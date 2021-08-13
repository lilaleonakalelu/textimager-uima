package org.hucompute.textimager.uima.gervader;

import org.apache.uima.jcas.JCas;
import org.hucompute.textimager.uima.sentiment.base.SentimentBase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.texttechnologylab.annotation.AnnotationComment;

public class GerVaderSentiment extends SentimentBase {
    @Override
    protected String getDefaultDockerImage() {
        return "textimager-uima-service-gervader";
    }

    @Override
    protected String getDefaultDockerImageTag() {
        return "0.3";
    }

    @Override
    protected int getDefaultDockerPort() {
        return 8000;
    }

    @Override
    protected String getRestRoute() {
        return "/process";
    }

    @Override
    protected void updateCAS(JCas aJCas, JSONObject jsonResult) {
        if (jsonResult.has("selections")) {
            for (Object sels : jsonResult.getJSONArray("selections")) {
                JSONObject selection = (JSONObject) sels;
                String selectionAnnotation = selection.getString("selection");
                JSONArray sentences = selection.getJSONArray("sentences");
                for (Object sen : sentences) {
                    JSONObject sentence = (JSONObject) sen;

                    int begin = sentence.getJSONObject("sentence").getInt("begin");
                    int end = sentence.getJSONObject("sentence").getInt("end");

                    org.hucompute.textimager.uima.type.GerVaderSentiment sentiment = new org.hucompute.textimager.uima.type.GerVaderSentiment(aJCas, begin, end);
                    sentiment.setSentiment(sentence.getDouble("compound"));
                    sentiment.setPos(sentence.getDouble("pos"));
                    sentiment.setNeu(sentence.getDouble("neu"));
                    sentiment.setNeg(sentence.getDouble("neg"));
                    sentiment.addToIndexes();

                    AnnotationComment comment = new AnnotationComment(aJCas);
                    comment.setReference(sentiment);
                    comment.setKey("selection");
                    comment.setValue(selectionAnnotation);
                    comment.addToIndexes();

                    addAnnotatorComment(aJCas, sentiment);
                }
            }
        }
    }
}
