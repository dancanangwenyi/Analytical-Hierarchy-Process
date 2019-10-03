import java.util.*;
import java.io.*;
public class AHProcess {
    //ATTRIBUTES
    private final double CR_COMPARE = 0.10;
    private final double RI = 0.90;
    private int N; //size of the matrix
    private double LAMBDA;
    private double CI;
    private double CR;
    private boolean SUCCESS;
    private ArrayList<List<Double>> MATRIX_A = new ArrayList<List<Double>>(); 
    private ArrayList<List<Double>> NORMALIZED_MATRIX_A = new ArrayList<List<Double>>(); 
    private ArrayList<List<Double>> WEIGHTS = new ArrayList<List<Double>>(); 
    private ArrayList<Double> CRITERIA_WEIGHTS = new ArrayList();
    private ArrayList<Double> WEIGHT_SUMS = new ArrayList();
    private ArrayList<Double> ROW_RATIO = new ArrayList();
    //CONSTRUCTOR
    public AHProcess(int n, ArrayList<List<Double>> matrixA){
        this.N = n;
        this.MATRIX_A = matrixA;
        this.SUCCESS = false;
        this.LAMBDA = 0.0;
        this.CI = 0.0;
        this.CR =0.0;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("Enter the size of the matrix A");
        Scanner input = new Scanner(System.in);
        int n = input.nextInt();
        ArrayList<List<Double>> MATRIX = new ArrayList<List<Double>>();
        input.close();
        try {
			Scanner scanner = new Scanner(new File("data.txt"));
			while (scanner.hasNextLine()) {
                String str = scanner.nextLine().trim();
                String[] stringData = str.split(" ");
                ArrayList<Double> doubleData = new ArrayList(stringData.length);
                int i =0;
                for(String value: stringData){
                    double v = Double.parseDouble(value);
                    double vals = (double)Math.round(v*1000)/1000;
                    doubleData.add(vals);
                    i++;
                }
                MATRIX.add(doubleData);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        AHProcess ahb = new AHProcess(n,MATRIX);
        ahb.normalizeMatrix();
        ahb.weightsMatrix();
        ArrayList<Double> weight_sums = ahb.calcWeightSums();
        double lambda = ahb.calcLambda();
        double ci = ahb.calcCI();
        double cr = ahb.calcCR();
        boolean good = ahb.is_OK();
        System.out.println("LAMBDA             : "+lambda);
        System.out.println("CONSISTENCY INDEX  : "+ci);
        System.out.println("CONSISTENCY RATIO  : "+cr);
        System.out.println("CR < 0.1: "+good);

        System.out.print("\nCRITERIA WEIGHTS: [");
        for(double v: ahb.CRITERIA_WEIGHTS){
            System.out.print(v);
            System.out.print(" ");
        }
        System.out.println("]\n");
        ahb.printMatrix();
    }
    //SETTING UP THE LENGTH OF THE MATRIX
    public void setN(int n){this.N = n;} 
    public int getN(){return N;}
    public ArrayList<List<Double>> getMatrix(){
        return MATRIX_A;
    }
    public void printMatrix(){
        System.out.println("---------MATRIX A---------");
        for(int n=0 ; n<MATRIX_A.size() ; n++)
        {
            for(int m=0 ; m<MATRIX_A.size() ; m++ )
            {
                System.out.print(MATRIX_A.get(n).get(m));
                System.out.print("   ");
            }
            System.out.println();
        }
        System.out.println("---------------------------");
        System.out.println("-----NORMALIZED MATRIX-----");
        for(int n=0 ; n<NORMALIZED_MATRIX_A.size() ; n++)
        {
            for(int m=0 ; m<NORMALIZED_MATRIX_A.size() ; m++ )
            {
                System.out.print(NORMALIZED_MATRIX_A.get(n).get(m));
                System.out.print("   ");
            }
            System.out.println();
        }
        System.out.println("--------------------------");
        System.out.println("------WEIGHTS MATRIX------");
        for(int n=0 ; n<WEIGHTS.size() ; n++)
        {
            for(int m=0 ; m<WEIGHTS.size() ; m++ )
            {
                System.out.print(WEIGHTS.get(n).get(m));
                System.out.print("   ");
            }
            System.out.println();
        }
    }
    public ArrayList<List<Double>> transposeMatrix(ArrayList<List<Double>> MATRIX ){
        ArrayList<List<Double>> transposed = new ArrayList<List<Double>>();
        for(int n=0 ; n<MATRIX.size() ; n++){
            ArrayList<Double> list = new ArrayList();
            for(int m=0 ; m<MATRIX.size() ; m++){
                list.add(MATRIX.get(m).get(n));
            }
            transposed.add(list);
        }
        return transposed;
    }
    public void normalizeMatrix(){
        ArrayList<List<Double>> transposed = new ArrayList<List<Double>>();
        ArrayList<Double> columns_sum = new ArrayList();
        for(int n=0 ; n<MATRIX_A.size() ; n++){
            double sum = 0.0;
            for(int m=0 ; m<MATRIX_A.size() ; m++){
                sum += MATRIX_A.get(m).get(n);
            }
            columns_sum.add(sum);
            //System.out.println(sum);
        }
        for(int s=0 ; s<columns_sum.size() ; s++){
            ArrayList<Double> normals = new ArrayList();
            for(int t=0 ; t<MATRIX_A.size() ; t++){
                 double element = MATRIX_A.get(t).get(s);
                 double col_sum = columns_sum.get(s);
                 double value = element/col_sum;
                 normals.add((double)Math.round(value * 1000)/1000);
            }
            transposed.add(normals);
        }
        NORMALIZED_MATRIX_A = transposeMatrix(transposed);
        //System.out.println(NORMALIZED_MATRIX_A.get(0).get(0));
        //System.out.println(MATRIX_A.get(0).get(0));
        // System.out.println(NORMALIZED_MATRIX_A.get(1).get(0));
    }
    public void weightsMatrix(){ 
        for(int n=0 ; n<NORMALIZED_MATRIX_A.size() ; n++){
            double sum = 0.0;
            double val =0.0;
            for(int m=0 ; m<NORMALIZED_MATRIX_A.size() ; m++){
                sum += NORMALIZED_MATRIX_A.get(n).get(m);
            }
            // System.out.println(sum);
            val = sum/(NORMALIZED_MATRIX_A.size());
            CRITERIA_WEIGHTS.add((double)Math.round(val*1000)/1000);
            //System.out.println(val);
        }
        for(int s=0 ; s<MATRIX_A.size() ; s++){
            ArrayList<Double> weits = new ArrayList();
            for(int t=0 ; t<MATRIX_A.size() ; t++){
                 double value = (MATRIX_A.get(t).get(s))*(CRITERIA_WEIGHTS.get(s));
                 weits.add((double)Math.round(value*1000)/1000);
            }
            WEIGHTS.add(weits);
        }
        WEIGHTS = transposeMatrix(WEIGHTS);
        // System.out.println(WEIGHTS.get(0).get(0));
        // System.out.println(MATRIX_A.get(0).get(0));
        // System.out.println(NORMALIZED_MATRIX_A.get(0).get(0));
       
    }
    public ArrayList<Double> calcWeightSums(){
       for(int s=0 ; s<WEIGHTS.size() ; s++){  
            double sum =0.0;
            for(int t=0 ; t<WEIGHTS.size() ; t++){
                 sum += (WEIGHTS.get(s).get(t));
            }
            WEIGHT_SUMS.add((double)Math.round(sum*1000)/1000);
        }
        return WEIGHT_SUMS;
    }
    public double calcLambda(){
        ArrayList<Double> vect = new ArrayList();
        double sum = 0.0;
        for(int s=0 ; s<CRITERIA_WEIGHTS.size() ; s++){  
            double ratio = WEIGHT_SUMS.get(s)/CRITERIA_WEIGHTS.get(s);
            ROW_RATIO.add((double)Math.round(ratio*1000)/1000);
        }
        for(int j=0 ; j<ROW_RATIO.size() ; j++){
            sum += ROW_RATIO.get(j);
        }
        LAMBDA = (double)Math.round((sum/ROW_RATIO.size())*1000)/1000;
        return LAMBDA;
    }
    public double calcCI(){
        double c = (LAMBDA - N) / (N-1);
        CI = (double)Math.round(c*1000)/1000;
        return CI;
    }
    public double calcCR(){
        CR = (double)Math.round((CI / RI)*1000)/1000;
        return CR;
    }
    public boolean is_OK(){
        double epsilon = 0.000000000001;
        return CR_COMPARE > CR;
    }
}
