/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MaxEnt;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderCrossValidator;
import opennlp.tools.namefind.TokenNameFinderEvaluator;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.eval.FMeasure;
import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.BigramNameFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.OutcomePriorFeatureGenerator;
import opennlp.tools.util.featuregen.PreviousMapFeatureGenerator;
import opennlp.tools.util.featuregen.SentenceFeatureGenerator;
import opennlp.tools.util.featuregen.TokenClassFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;

/**
 *
 * @author DELL
 */
public class MaxEntClassifierHandler {
    
    public static void main(String[] arg){
        try{
        MaxEntClassifierHandler maxEntClassifierHandler = new MaxEntClassifierHandler();
        maxEntClassifierHandler.train();
        }catch(Exception e){
            System.out.println(e);
        }
    }
    
    private void train() throws FileNotFoundException, IOException{
        
        Charset charset = Charset.forName("UTF-8");
        ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream("outputNew3.train"), charset);
        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);
        TokenNameFinderModel model;

        int m = 4;
        
        AdaptiveFeatureGenerator featureGenerator = new CachedFeatureGenerator(
                new AdaptiveFeatureGenerator[]{
            new WindowFeatureGenerator(new TokenFeatureGenerator(), m, m),
            new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), m, m),
            new OutcomePriorFeatureGenerator(),
            new PreviousMapFeatureGenerator(),
            new BigramNameFeatureGenerator(),
            new SentenceFeatureGenerator(true, false)
        });

        
        String modelString = "";
        try {
            
            model =  NameFinderME.train("si","person", sampleStream,featureGenerator , Collections.<String, Object>emptyMap(), 100,4);
            //train("si", "person", sampleStream,Collections.<String, Object>emptyMap(), 100, 6);
            //NameFinderME.train("si", "person", sampleStream, TrainingParameters.defaultParams(), featureGenerator, Collections.<String, Object>emptyMap() );
            modelString = model.toString();
            
        } finally {
            
            sampleStream.close();
            
        }
        
        BufferedOutputStream modelOut = null;
        
        try {
            
            modelOut = new BufferedOutputStream(new FileOutputStream(write(modelString)));
            model.serialize(modelOut);
            
        } finally {
            
            if (modelOut != null) {
                modelOut.close();
            }
            
        }

        ObjectStream<String> line = new PlainTextByLineStream(new FileInputStream("outputNew1.test"), charset);
        ObjectStream<NameSample> sample = new NameSampleDataStream(line);

        TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(new NameFinderME(model));
        evaluator.evaluate(sample);

        FMeasure result = evaluator.getFMeasure();

        System.out.println(result.toString());
        
    }
    
    private void test() throws FileNotFoundException, IOException{
        
//        Charset charset = Charset.forName("UTF-8");
//        ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream("outputNew1.test"), charset);
//        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);
//        TokenNameFinderCrossValidator evaluator = new TokenNameFinderCrossValidator("si", 0, 5);
//        evaluator.evaluate(sampleStream, 10);
//
//        FMeasure result = evaluator.getFMeasure();
//
//        System.out.println(result.toString());
//        
    }
    
     public static File write(String content) throws IOException {

        File file = new File("model.");

        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();

        return file;
    }
}
