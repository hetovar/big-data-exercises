package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

public class MovieRecommender{
    String pathToFile;
    static final String REGEX = "(product/productId: |review/userId: |review/score: ).+$";
    static final Pattern PATTERN = Pattern.compile(REGEX);
    Hashtable<String, Integer> users;
    Hashtable<String, Integer> movies;
    Hashtable<Integer, String> idMovies;
    static int numberOfReviews;

    public MovieRecommender(String path){
        pathToFile = path;
        numberOfReviews = 0;
        users = new Hashtable<String, Integer>();
        movies = new Hashtable<String, Integer>();
        idMovies = new Hashtable<Integer, String>();

        try{
            ReadFile();
        }
        catch(IOException ex){
            System.out.println("Fatal ERROR :(");
        }
    }

    // Implemente a method to read the file. LINE BY LINE
    private void ReadFile() throws IOException{
        InputStream inputFile = new GZIPInputStream(new FileInputStream(pathToFile));
        Reader decoder = new InputStreamReader(inputFile);
        BufferedReader buffered = new BufferedReader(decoder);

        File file = new File("testFile.csv");
        FileWriter fw = new FileWriter(file, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);

        createCVSFile(buffered, out);

        out.close();
        file.delete();
        inputFile.close();
    }

    private void createCVSFile(BufferedReader buffered, PrintWriter out) throws IOException {
        Matcher m;
        List<String> data = new ArrayList<String>();
        String line;

        while((line = buffered.readLine()) != null){
            m = PATTERN.matcher(line);

            if(m.matches()){
                line = line.replaceAll("(product/productId: |review/userId: |review/score: )", "");
                data.add(line);

                if(data.size() > 2){
                    if(!users.containsKey(data.get(1)))
                        users.put(data.get(1), users.size()+1);
                    if(!movies.containsKey(data.get(0))) {
                        movies.put(data.get(0), movies.size() + 1);
                        idMovies.put(movies.size() + 1, data.get(0));
                    }
                    out.println(users.get(data.get(1)) + "," + movies.get(data.get(0)) + "," + line);
                    data.clear();
                    numberOfReviews++;
                }
            }
        }
    }

    public int getTotalReviews(){
        return numberOfReviews;
    }

    public int getTotalProducts(){
        return movies.size();
    }

    public int getTotalUsers(){
        return users.size();
    }

    public List<String> getRecommendationsForUser(String userId){
        try {
            List<String> answer = new ArrayList<String>();
            DataModel model = new FileDataModel(new File("/home/hector/Documents/big-data-exercises-master/testFile.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            List<RecommendedItem> recommendations = recommender.recommend(users.get(userId), 3);
            for (RecommendedItem recommendation : recommendations)
                answer.add(idMovies.get((int) recommendation.getItemID() + 1));

            return answer;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TasteException e) {
            e.printStackTrace();
        }

        return null;

    }
}
