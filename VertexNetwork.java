import java.lang.*;
import java.util.*;
import java.io.*;

public class VertexNetwork {
    /* DO NOT FORGET to add a graph representation and 
       any other fields and/or methods that you think 
       will be useful to the VertexNetwork class. 
       DO NOT FORGET to modify the constructors when you 
       add new fields to the VertexNetwork class. */
    
    private double            transmissionRange; // The transmission range of each vertex.
    private ArrayList<Vertex> location;          // A list of vertices in the network.
    
    VertexNetwork() {
        /* This constructor creates an empty list of vertex locations. 
           read The transmission range is set to 0.0. */
        transmissionRange = 0.0;
        location          = new ArrayList<Vertex>(0);
    }
    
    VertexNetwork(String locationFile) {
        /* This constructor creates a list of vertex locations read 
           from the plain text file locationFile. Each line of this 
           file specifies one location as an x-coordinate and a 
           y-coordinate separated by a tab or a space. The transmission 
           range of these vertices is set to 1.0. */
        this(1.0, locationFile);
    }
    
    VertexNetwork(String locationFile, double transmissionRange) {
        /* This constructor creates a list of vertex locations read 
           from the plain text file locationFile. Each line of this 
           file specifies one location as an x-coordinate and a 
           y-coordinate separated by a tab or a space. The transmission 
           range of these vertices is set to transmissionRange. */
        this(transmissionRange, locationFile);
    }
    
    VertexNetwork(double transmissionRange, String locationFile) {
        /* This constructor creates a list of vertex locations read 
           from the plain text file locationFile. Each line of this 
           file specifies one location as an x-coordinate and a 
           y-coordinate separated by a tab or a space. The transmission 
           range of these vertices is set to transmissionRange. */
        this.transmissionRange = transmissionRange;
        location               = new ArrayList<Vertex>(0);
        Scanner scanLocation   = null;
        try {
            scanLocation = new Scanner(new BufferedReader(new FileReader(locationFile)));
            scanLocation.useLocale(Locale.US);
            while (scanLocation.hasNext()) {
                location.add(new Vertex(scanLocation.nextDouble(), scanLocation.nextDouble()));
            }
        } catch (FileNotFoundException exception) {
            System.err.println("FileNotFoundException: " + exception.getMessage());
        } catch (IOException exception) {
            System.err.println("IOException: " + exception.getMessage());
        } finally {
            if (scanLocation != null) scanLocation.close();
        }
    }
    
    public void setTransmissionRange(double transmissionRange) {
        /* This method sets the transmission range to transmissionRange. */
        /* DO NOT FORGET to recompute your graph when you change the 
           transmissionRange to a new value. */
        this.transmissionRange = transmissionRange;
    }
    
    public ArrayList<Vertex> gpsrPath(int sourceIndex, int sinkIndex) {
        /* This method returns a path from a source at location sourceIndex 
           and a sink at location sinkIndex using the GPSR algorithm. An empty 
           path is returned if the GPSR algorithm fails to find a path. */
        /* The following code is meant to be a placeholder that simply 
           returns an empty path. Replace it with your own code that 
           implements the GPSR algorithm. */
      ArrayList<Vertex> locationcopy;
    	locationcopy=new ArrayList<Vertex>(location);
    	int currentIndex, nearestIndex;
    	double bestdist,dist;
    	ArrayList<Vertex> GPSRpath;
    	GPSRpath= new ArrayList<Vertex>(0);
    	currentIndex=sourceIndex;
    	Vertex sourcenode, sinknode, currentnode, nearestnode;
    	sourcenode=locationcopy.get(sourceIndex);
    	sinknode=locationcopy.get(sinkIndex);
    	dist=bestdist=sourcenode.distance(sinknode);
    	GPSRpath.add(sourcenode);
    	int i;
		
    	while(true)
    	{
    		currentnode=locationcopy.get(currentIndex);
    		nearestIndex=currentIndex;
    	for(i=0;i<locationcopy.size();i++)
    	{
    		if(locationcopy.get(i).distance(currentnode)<=transmissionRange)
    		{
    			if(bestdist>(dist=locationcopy.get(i).distance(sinknode))) 
    				{
    				bestdist=dist;
    				nearestIndex=i;
    				}	
    			if(dist==0) break;
    		}		
    	}
    	if(nearestIndex!=currentIndex)
    	{
    		nearestnode=locationcopy.get(nearestIndex);
    		GPSRpath.add(nearestnode);
    		if(dist==0) return GPSRpath;
    		locationcopy.remove(currentIndex);
    		if(sinkIndex>currentIndex) sinkIndex-=1;
    		if(nearestIndex>currentIndex) currentIndex=nearestIndex-1; else currentIndex=nearestIndex;
    		//currentIndex=locationcopy.indexOf(nearestnode);
    		//sinkIndex=locationcopy.indexOf(sinknode);
    	}
    	else return new ArrayList<Vertex>(0);
    	}
    }
    
    
    public ArrayList<Vertex> dijkstraPath(int sourceIndex, int sinkIndex) {
        /* This method returns a path from a source at location sourceIndex 
           and a sink at location sinkIndex using Dijkstra's algorithm. An empty 
           path is returned if Dijkstra's algorithm fails to find a path. */
        /* The following code is meant to be a placeholder that simply 
           returns an empty path. Replace it with your own code that 
           implements Dijkstra's algorithm. */
    	ArrayList<Vertex> locationcopy;
    	locationcopy=new ArrayList<Vertex>(location);
    	ArrayList<Vertex> dijpath;
    	dijpath= new ArrayList<Vertex>(0);
    	//dijpath.add(locationcopy.get(sourceIndex));
    	int i,j,maxdist=locationcopy.size()+1;
    	int[] DistArray=new int[locationcopy.size()], PreArray=new int[locationcopy.size()]; 
    	int[][] AdjArray=new int[locationcopy.size()][locationcopy.size()];
    	
  
    	
    	for(i=0;i<locationcopy.size();i++)
    		for(j=i;j<locationcopy.size();j++)
    			if(locationcopy.get(i).distance(locationcopy.get(j))<=transmissionRange)
    			{
    				AdjArray[i][j]=1;
    				AdjArray[j][i]=1;
    			}
    			else
    			{
    				AdjArray[i][j]=maxdist;
    				AdjArray[j][i]=maxdist;
    			}
    	
    	for(i=0;i<locationcopy.size();i++)
    		AdjArray[i][i]=0;
    		
		
    	int mdistindex,mindist;

    	   for(i=0;i<locationcopy.size();i++)
    	   {  
    		   DistArray[i]=AdjArray[sourceIndex][i];
    	      if(DistArray[i]<maxdist)  PreArray[i]=sourceIndex;
    	      else             PreArray[i]=-1;
    	   }
    	   PreArray[sourceIndex]=-1; AdjArray[sourceIndex][sourceIndex]=1;
    	   for(j=0;j<(locationcopy.size()-1);j++)
    	   {  
    		   mindist=maxdist;  
    		   mdistindex=-1;
    	      for(i=0;i<locationcopy.size();i++)
    	         if((AdjArray[i][i]==0)&&(DistArray[i]<mindist))
    	         {   
    	        	 mdistindex=i;
    	        	 mindist=DistArray[i];
    	         }
    	      if(mdistindex==-1)  return new ArrayList<Vertex>(0);
    	      else
    	      {  
    	    	     AdjArray[mdistindex][mdistindex]=1;
    	             for(i=0;i<locationcopy.size();i++)
    	             {
    	                if(AdjArray[i][i]==0)
    	                {
    	                   if(DistArray[mdistindex]+AdjArray[mdistindex][i]<DistArray[i])
    	                   {  
    	                	   DistArray[i]=DistArray[mdistindex]+AdjArray[mdistindex][i];
    	                       PreArray[i]=mdistindex;
    	                   }
    	                }
    	             }
    	       }
    	      if(AdjArray[sinkIndex][sinkIndex]==1) break;
    	   }
    	   Stack<Integer> s=new Stack<Integer>();
    	   i=sinkIndex;
    	   s.push(i);
    	   while(PreArray[i]!=-1)
    	   {
    		  s.push(PreArray[i]);
    		  i=PreArray[i];
    	   }
    	   while(!s.isEmpty())
    	   {
    		   dijpath.add(locationcopy.get(s.pop()));
    	   }
    	return dijpath;
    }
    
    public void gpsrAllPairs(boolean print) {
        /* This method calls the GPSR algorithm for all pairs of vertices and 
           displays the number of successful runs as well as the average time 
           taken for these successful runs. Note that the time measured is 
           system time and so you should run your code on a lightly loaded 
           computer to get consistent and meaningful timing results.
        /* DO NOT CHANGE the following code. */
        int  numSuccesses       = 0;
        long totalTimeSuccesses = 0;
        if (print) System.out.println("Paths between all pairs of vertices using the GPSR algorithm:");
        for (int i = 0; i < location.size(); i++) {
            for (int j = i+1; j < location.size(); j++) {
                long startTime           = System.nanoTime();
                ArrayList<Vertex> pathIJ = gpsrPath(i, j);
                long endTime             = System.nanoTime();
                if (!pathIJ.isEmpty()) {
                    numSuccesses++;
                    totalTimeSuccesses += (endTime - startTime);
                }
                if (print) System.out.println("I = " + i + " J = " + j + " : " + pathIJ.toString());
            }
        }
        System.out.println("The GPSR algorithm is successfull " + numSuccesses + "/" + location.size()*(location.size()-1)/2 + " times.");
        if (numSuccesses != 0) {
            System.out.println("The average time taken by the GPSR algorithm on successful runs is " + totalTimeSuccesses/numSuccesses + " nanoseconds.");
        } else {
            System.out.println("The average time taken by the GPSR algorithm on successful runs is N/A nanoseconds.");
        }
        System.out.println("");
    }
    
    public void dijkstraAllPairs(boolean print) {
        /* This method calls Dijkstra's algorithm for all pairs of vertices and 
           displays the number of successful runs as well as the average time 
           taken for these successful runs. Note that the time measured is 
           system time and so you should run your code on a lightly loaded 
           computer to get consistent and meaningful timing results.
        /* DO NOT CHANGE the following code. */
        int  numSuccesses       = 0;
        long totalTimeSuccesses = 0;
        if (print) System.out.println("Paths between all pairs of vertices using Dijkstra's algorithm:");
        for (int i = 0; i < location.size(); i++) {
            for (int j = i+1; j < location.size(); j++) {
                long startTime           = System.nanoTime();
                ArrayList<Vertex> pathIJ = dijkstraPath(i, j);
                long endTime             = System.nanoTime();
                if (!pathIJ.isEmpty()) {
                    numSuccesses++;
                    totalTimeSuccesses += (endTime - startTime);
                }
                if (print) System.out.println("I = " + i + " J = " + j + " : " + pathIJ.toString());
            }
        }
        System.out.println("Dijkstra's algorithm is successfull " + numSuccesses + "/" + location.size()*(location.size()-1)/2 + " times.");
        if (numSuccesses != 0) {
            System.out.println("The average time taken by Dijkstra's algorithm on successful runs is " + totalTimeSuccesses/numSuccesses + " nanoseconds.");
        } else {
            System.out.println("The average time taken by Dijkstra's algorithm on successful runs is N/A nanoseconds.");
        }
        System.out.println("");
    }
    
}

