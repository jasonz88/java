import java.lang.*;
import java.util.*;

public class Lab1 {
    
    public static void main(String[] args) {
        double[] transmissionRange = {5, 10, 15, 20, 25};
        
        System.out.println("---------------------------------");
        System.out.println("Results for the small input graph");
        System.out.println("---------------------------------");
        System.out.println("");
        VertexNetwork smallNetwork = new VertexNetwork("SmallInputGraph.txt");
        smallNetwork.gpsrAllPairs(true);
        smallNetwork.dijkstraAllPairs(true);
        
        System.out.println("---------------------------------");
        System.out.println("Results for the large input graph");
        System.out.println("---------------------------------");
        System.out.println("");
        VertexNetwork largeNetwork = new VertexNetwork("LargeInputGraph.txt");
        for (int r = 0; r < transmissionRange.length; r++) {
            System.out.println("Transmission Range = " + transmissionRange[r] + " meters.");
            System.out.println("");
            largeNetwork.setTransmissionRange(transmissionRange[r]);
            largeNetwork.gpsrAllPairs(false);
            largeNetwork.dijkstraAllPairs(false);
        }
    }
    
}

