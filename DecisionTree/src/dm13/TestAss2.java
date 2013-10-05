package dm13;

import auxiliary.DataSet;
import auxiliary.Evaluation;

/**
 *
 * @author daq
 */
public class TestAss2 {

    public static void main(String[] args) {
        // for Classification
        System.out.println("for Classification");
        String[] dataPaths = new String[]{"./data/breast-cancer.data", "./data/segment.data"};
        for (String path : dataPaths) {
            DataSet dataset = new DataSet(path);

            // conduct 10-cv 
            Evaluation eva = new Evaluation(dataset, "DecisionTree");
            eva.crossValidation();

            // print mean and standard deviation of accuracy
            System.out.println("Dataset:" + path + ", mean and standard deviation of accuracy:" + eva.getAccMean() + "," + eva.getAccStd());
        }

        // for Regression
        System.out.println("\nfor Regression");
        String[] dataPaths2 = new String[]{"./data/housing.data", "./data/meta.data"};
        for (String path : dataPaths2) {
            DataSet dataset = new DataSet(path);

            // conduct 10-cv 
            Evaluation eva = new Evaluation(dataset, "DecisionTree");
            eva.crossValidation();

            // print mean and standard deviation of RMSE
            System.out.println("Dataset:" + path + ", mean and standard deviation of RMSE:" + eva.getRmseMean() + "," + eva.getRmseStd());
        }
    }
}
