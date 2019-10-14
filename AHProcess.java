import java.util.*;
import java.io.*;
public class AHProcess {
    //ATTRIBUTES
    private final double CR_COMPARE = 0.10;
    private final double RI = 0.90;
    private int NUM_WIFI_INTERFACES;
    private int NUM_LTE_INTERFACES;
    private int BEST_WIFI;
    private double BEST_WIFI_COST;
    private int BEST_LTE;
    private double BEST_LTE_COST;
    private int N; //size of the matrix
    private double LAMBDA;
    private double CI;
    private double CR;
    private ArrayList<List<Double>> WIFI_ALTERNATIVES = new ArrayList<List<Double>>();
    private ArrayList<List<Double>> LTE_ALTERNATIVES = new ArrayList<List<Double>>();
    private ArrayList<List<Double>> MATRIX_A = new ArrayList<List<Double>>(); 
    private ArrayList<List<Double>> NORMALIZED_MATRIX_A = new ArrayList<List<Double>>(); 
    private ArrayList<List<Double>> WEIGHTS = new ArrayList<List<Double>>(); 
    private ArrayList<Double> CRITERIA_WEIGHTS = new ArrayList();
    private ArrayList<Double> WEIGHT_SUMS = new ArrayList();
    private ArrayList<Double> ROW_RATIO = new ArrayList();
    private HashMap<Double, Double> WIFI_COST_MAP = new HashMap<Double, Double>();
    private HashMap<Double, Double> LTE_COST_MAP = new HashMap<Double, Double>();
    //CONSTRUCTOR
    public AHProcess(int n, ArrayList<List<Double>> matrixA){
        this.N = n;
        this.MATRIX_A = matrixA;
        this.LAMBDA = 0.0;
        this.CI = 0.0;
        this.CR =0.0;
        //  C D T B
        this.CRITERIA_WEIGHTS.add(0.137);
        this.CRITERIA_WEIGHTS.add(0.271);
        this.CRITERIA_WEIGHTS.add(0.537);
        this.CRITERIA_WEIGHTS.add(0.056);

    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.print("Enter the number WIFI interfaces: ");
        Scanner input = new Scanner(System.in);
        int n = input.nextInt();
        System.out.print("Enter the number LTE interfaces: ");
        int k = input.nextInt();
        ArrayList<List<Double>> MATRIX = new ArrayList<List<Double>>();
        input.close();
        try {
            Scanner scanner = new Scanner(new File("data.txt"));
            int i =0;
			while (scanner.hasNextLine() ) {
                String str = scanner.nextLine().trim();
                String[] stringData = str.split(" ");
                ArrayList<Double> doubleData = new ArrayList(stringData.length);
                for(String value: stringData){
                    double v = Double.parseDouble(value);
                    double vals = (double)Math.round(v*1000)/1000;
                    doubleData.add(vals);  
                }
                MATRIX.add(doubleData);
                i++;
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        AHProcess ahb = new AHProcess(n,MATRIX);
        ahb.NUM_LTE_INTERFACES = k;
        ahb.NUM_WIFI_INTERFACES = n;
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
        System.out.print("\nWEIGHTED SUM VALUE: [");
        for(double v: ahb.WEIGHT_SUMS){
            System.out.print(v);
            System.out.print(" ");
        }
        System.out.println("]\n");
        //ahb.printMatrix();
        ahb.generateWifiAlternatives(ahb.NUM_WIFI_INTERFACES);
        ahb.generateLteAlternatives(ahb.NUM_LTE_INTERFACES);
        ahb.bestRewardWifi();
        ahb.bestRewardLTE();
        System.out.println("\n\nBalance streaming data over WIFI: "+ahb.BEST_WIFI+" and LTE: "+ahb.BEST_LTE);
        ahb.balance();
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
           // CRITERIA_WEIGHTS.add((double)Math.round(val*1000)/1000);
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
    public static double getRandomDoubleBetweenRange(double min, double max){
        double weight = (Math.random()*((max-min)+1))+min;
        double val1 = (double)Math.round(weight);
        double val = (double)Math.round(val1*1000)/1000;
        return val;
    }
    public void generateWifiAlternatives(int n){
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("GENERATED WIFI ALTERNATIVES");
        for(int i=0 ; i<n ; i++){
                ArrayList<Double> temp_wifi = new ArrayList<Double>();
                //WIFI PARAMETERS BOUNDED 
                double wifi_cost = getRandomDoubleBetweenRange(5.0,50);
                double wifi_delay = getRandomDoubleBetweenRange(30.0,80.0);
                double wifi_throughput = getRandomDoubleBetweenRange(350.0,10000);
                double wifi_battery_consumption = getRandomDoubleBetweenRange(1.0,5.0);
                //System.out.println();
               // System.out.println(wifi_cost+"   "+wifi_delay+"   "+wifi_throughput+"   "+wifi_battery_consumption);
                temp_wifi.add(wifi_cost); temp_wifi.add(wifi_delay); 
                temp_wifi.add(wifi_throughput); temp_wifi.add(wifi_battery_consumption);
                WIFI_ALTERNATIVES.add(temp_wifi);
            
        } 
           
    }
    public void generateLteAlternatives(int n){
        //LTE PARAMETERS BOUNDED
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("GENERATED LTE ALTERNATIVES");
        for(int j=0 ; j<n ;j++){
            ArrayList<Double> temp_lte = new ArrayList<Double>();
            double lte_cost = getRandomDoubleBetweenRange(8.0,70);
            double lte_delay = getRandomDoubleBetweenRange(50.0,120.0);
            double lte_throughput = getRandomDoubleBetweenRange(1500.0,20000.0);
            double lte_battery_consumption = getRandomDoubleBetweenRange(2.0,8.0);
            //System.out.println();
            //System.out.println(lte_cost+"   "+lte_delay+"   "+lte_throughput+"   "+lte_battery_consumption);
            temp_lte.add(lte_cost); temp_lte.add(lte_delay); 
            temp_lte.add(lte_throughput); temp_lte.add(lte_battery_consumption);
            LTE_ALTERNATIVES.add(temp_lte);
        }
        
    }
    public void bestRewardWifi(){
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------");
        double minimum = 100000000;
        int best_wifi = 0;
        for(int i=0 ; i<WIFI_ALTERNATIVES.size() ; i++){
            double sum_alternative = 0.0;
           
            for(int j=0 ; j<WIFI_ALTERNATIVES.get(i).size() ;j++){
                if(j==2){
                    double norm = normalizeWifiThroughput(WIFI_ALTERNATIVES.get(i).get(j));
                    sum_alternative += 1/(CRITERIA_WEIGHTS.get(j)*norm);
                    sum_alternative = (double)Math.round(sum_alternative*1000)/1000;
                }
                else{
                    sum_alternative += CRITERIA_WEIGHTS.get(j)*WIFI_ALTERNATIVES.get(i).get(j);
                    sum_alternative = (double)Math.round(sum_alternative*1000)/1000;
                }  
            }
            WIFI_COST_MAP.put(sum_alternative,WIFI_ALTERNATIVES.get(i).get(2));
            //System.out.println("Total cost for wifi"+i+" is "+sum_alternative+" with throughput: "+WIFI_ALTERNATIVES.get(i).get(2));
            if(sum_alternative < minimum){
                minimum = sum_alternative;
                best_wifi=i;
            }
        }
        BEST_WIFI = best_wifi;
        BEST_WIFI_COST = minimum;
        System.out.println("\nBest wifi with the smallest cost is: WIFI "+best_wifi);
        System.out.println("\nBEST WIFI PARAMETERS:");
        System.out.println("C      D      T      B");
        for(int n=0 ; n<WIFI_ALTERNATIVES.get(best_wifi).size(); n++){
            double val = WIFI_ALTERNATIVES.get(best_wifi).get(n);
            System.out.print(val);
            System.out.print("  ");
        }
        System.out.println();
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------");
    }
    public double normalizeWifiThroughput(double throughput){ 
        double sum = 0.0;
        for(int k=0 ; k<WIFI_ALTERNATIVES.size() ;k++){
            sum += WIFI_ALTERNATIVES.get(k).get(2);
        }
        double norm = throughput/sum;
        return norm; 
    }
    public double normalizeLteThroughput(double throughput){ 
        double sum = 0.0;
        for(int k=0 ; k<LTE_ALTERNATIVES.size() ;k++){
            sum += LTE_ALTERNATIVES.get(k).get(2);
        }
        double norm = throughput/sum;
        return norm;
    }
    public void bestRewardLTE(){
        double minimum = 100000000;
        int best_lte = 0;
        for(int i=0 ; i<LTE_ALTERNATIVES.size() ; i++){
            double sum_alternative = 0.0;
            for(int j=0 ; j<LTE_ALTERNATIVES.get(i).size() ;j++){
                if(j==2){
                    double norm = normalizeLteThroughput(LTE_ALTERNATIVES.get(i).get(j));
                    sum_alternative += 1/(CRITERIA_WEIGHTS.get(j)*norm);
                    sum_alternative = (double)Math.round(sum_alternative*1000)/1000;
                }
                else{ 
                    sum_alternative += CRITERIA_WEIGHTS.get(j)*LTE_ALTERNATIVES.get(i).get(j);
                    sum_alternative = (double)Math.round(sum_alternative*1000)/1000;
                }
               
                
            }
            LTE_COST_MAP.put(sum_alternative,LTE_ALTERNATIVES.get(i).get(2));
            //System.out.println("Total cost for LTE"+i+" is "+sum_alternative+" with a T of: "+LTE_ALTERNATIVES.get(i).get(2));
            if(sum_alternative < minimum){
                minimum = sum_alternative;
                best_lte=i;
            }
        }
        BEST_LTE = best_lte;
        BEST_LTE_COST = minimum;
        System.out.println("\nBest LTE with the smallest cost is: LTE "+best_lte);
        System.out.println("\nBEST LTE PARAMETERS:");
        System.out.println("C      D      T      B");
        for(int n=0 ; n<LTE_ALTERNATIVES.get(best_lte).size(); n++){
            double val = LTE_ALTERNATIVES.get(best_lte).get(n);
            System.out.print(val);
            System.out.print("  ");
        }
        System.out.println();
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------");
    }
    public void balance(){
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------");
        double lte_percentage = (BEST_LTE_COST/(BEST_LTE_COST+BEST_WIFI_COST))*100;
        double wifi_percentage = (BEST_WIFI_COST/(BEST_LTE_COST+BEST_WIFI_COST))*100;
        lte_percentage = (double)Math.round(lte_percentage*1000)/1000;
        wifi_percentage = (double)Math.round(wifi_percentage*1000)/1000;
        System.out.println("\nDistribution of streaming load: ");
        System.out.println("WIFI "+BEST_WIFI+" : "+wifi_percentage+" %");
        System.out.println("LTE "+BEST_LTE+" : "+lte_percentage+" %");
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------");
    }
}
