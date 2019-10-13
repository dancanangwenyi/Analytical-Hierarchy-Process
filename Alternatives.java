import java.util.ArrayList;
import java.util.*;
import java.io.*;
public class Alternatives{
    private ArrayList<List<Double>> WIFI_ALTERNATIVES = new ArrayList<List<Double>>();
    private ArrayList<List<Double>> LTE_ALTERNATIVES = new ArrayList<List<Double>>();
    public static void main(String[] args) {
        Alternatives alternatives = new Alternatives();
        for(int i =0 ;i<20 ;i++){
            ArrayList<Double> temp_wifi = new ArrayList<Double>();
            ArrayList<Double> temp_lte = new ArrayList<Double>();
            //WIFI PARAMETERS BOUNDED 
            double wifi_cost = getRandomDoubleBetweenRange(5.0,50);
            double wifi_delay = getRandomDoubleBetweenRange(30.0,100.0);
            double wifi_throughput = getRandomDoubleBetweenRange(350.0,10000);
            double wifi_battery_consumption = getRandomDoubleBetweenRange(1.0,5.0);
            temp_wifi.add(wifi_cost); temp_wifi.add(wifi_delay); 
            temp_wifi.add(wifi_throughput); temp_wifi.add(wifi_battery_consumption);
            //LTE PARAMETERS BOUNDED
            double lte_cost = getRandomDoubleBetweenRange(8.0,70);
            double lte_delay = getRandomDoubleBetweenRange(50.0,120.0);
            double lte_throughput = getRandomDoubleBetweenRange(1500.0,20000.0);
            double lte_battery_consumption = getRandomDoubleBetweenRange(2.0,8.0);
            temp_lte.add(lte_cost); temp_lte.add(lte_delay); temp_lte.add(lte_throughput);
            temp_lte.add(lte_throughput); temp_lte.add(lte_battery_consumption);
            alternatives.WIFI_ALTERNATIVES.add(temp_wifi);
            alternatives.LTE_ALTERNATIVES.add(temp_lte);
           // temp_lte.clear();temp_wifi.clear();  
        }
        alternatives.printAlternatives();
    }

    public static double getRandomDoubleBetweenRange(double min, double max){
        double weight = (Math.random()*((max-min)+1))+min;
        double val1 = (double)Math.round(weight);
        double val = (double)Math.round(val1*1000)/1000;
        return val;
    }
    public void printAlternatives(){
        System.out.println("COST    DELAY    THROUGHPUT    BATTERY");
        System.out.println("WIFI ALTERNATIVES");
        for(int i =0 ; i< WIFI_ALTERNATIVES.size() ; i++){
            for(int j=0 ; j<WIFI_ALTERNATIVES.get(i).size(); j++){
                double val = WIFI_ALTERNATIVES.get(i).get(j);
                System.out.print(val);
                System.out.print("   ");
            }
            System.out.println();
        }
        System.out.println("LTE ALTERNATIVES");
        for(int i =0 ; i< LTE_ALTERNATIVES.size() ; i++){
            for(int j=0 ; j<LTE_ALTERNATIVES.get(i).size(); j++){
                double val = LTE_ALTERNATIVES.get(i).get(j);
                System.out.print(val);
                System.out.print("    ");
            }
            System.out.println();
        }
    }
    public int bestRewardWifi(){

        return 0;
    }
}

