import java.util.*;
import java.io.*;
public class RandomWeights{
    private ArrayList<ArrayList<List<Double>>> GENERATED_WEIGHTS = new ArrayList<ArrayList<List<Double>>>();
    private int ITERATIONS = 0;
 /**
     * @param args the command line arguments
     */
    public RandomWeights(int n){
        this.ITERATIONS = n;
    }
    public static void main(String[] args) {

        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the size of the matrix, n: ");
        int n = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter the number of matrices to generate: ");
        int iterations = Integer.parseInt(scanner.nextLine());
        RandomWeights random = new RandomWeights(iterations);
        scanner.close();
        double min = 0.111 ; double max = 9.000;
        
        int k = 0;
        while( k < iterations){
            ArrayList<List<Double>> matrix = new ArrayList<List<Double>>();
            for(int i=0; i<n ; i++){
                ArrayList<Double> list = new ArrayList<Double>();
                for(int j=0; j<n ; j++){
                    if(i==j){
                        list.add(1.000);
                    }
                    else{
                        list.add(random.getRandomDoubleBetweenRange(min,max));
                    }
                }
                matrix.add(list);
            }
            random.GENERATED_WEIGHTS.add(matrix);
            k++;
        }
        random.correctMatrices();
        random.printMatrices();
    }
    public static double getRandomDoubleBetweenRange(double min, double max){
        double weight = (Math.random()*((max-min)+1))+min;
        double val = (double)Math.round(weight*1000)/1000;
        return val;
    }
    public void printMatrices(){
        for(int i=0 ; i<ITERATIONS ; i++){
            System.out.println("MATRIX: "+ i);
            for(int j=0 ; j<GENERATED_WEIGHTS.get(i).size() ; j++){
                for(int k=0 ; k<GENERATED_WEIGHTS.get(i).size() ; k++){
                    double value = GENERATED_WEIGHTS.get(i).get(j).get(k);
                    System.out.print(value+ "    ");
                }
                System.out.println();
            }
         System.out.println();  
        }
    }
     public void correctMatrices(){
        for(int i=0 ; i<ITERATIONS ; i++){
            for(int j=0 ; j<GENERATED_WEIGHTS.get(i).size() ; j++){
                for(int k=1+j ; k<GENERATED_WEIGHTS.get(i).size() ; k++){
                    double value = 1/GENERATED_WEIGHTS.get(i).get(j).get(k);
                    double val = (double)Math.round(value*1000)/1000;
                    GENERATED_WEIGHTS.get(i).get(k).set(j,val);
                }
                //System.out.println();
            }
         //System.out.println();  
        }
    }
}