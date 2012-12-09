import java.lang.*;
import java.util.*;
import java.io.*;

public class Lab2 {
    
    public static void main(String[] args) {
        ArrayList<String> inputStrings = new ArrayList<String>(0);
        Scanner scanString             = null;
        try {
            scanString = new Scanner(new BufferedReader(new FileReader("SampleInputFile.txt")));
            scanString.useLocale(Locale.US);
            while (scanString.hasNextLine()) {
                inputStrings.add(scanString.nextLine());
            }
        } catch (FileNotFoundException exception) {
            System.err.println("FileNotFoundException: " + exception.getMessage());
        } catch (IOException exception) {
            System.err.println("IOException: " + exception.getMessage());
        } finally {
            if (scanString != null) scanString.close();
        }
        
        for (int i = 0; i < inputStrings.size(); i++) {
            for (int j = i+1; j < inputStrings.size(); j++) {
                System.out.println("String X: " + inputStrings.get(i));
                System.out.println("String Y: " + inputStrings.get(j));
                System.out.println("Normalized Edit Distance Between X and Y = " + normalizedEditDistance(inputStrings.get(i), inputStrings.get(j)));
                System.out.println("Longest Common Subsequence Between X and Y (Full Table)    = " + LCSFullTable(inputStrings.get(i), inputStrings.get(j)));
                System.out.println("Longest Common Subsequence Between X and Y (Linear Memory) = " + LCSLinearMemory(inputStrings.get(i), inputStrings.get(j)));
                System.out.println("");
            }
        }
    }
    
    public static double normalizedEditDistance(String X, String Y) {
        int lenx=X.length();
        int leny=Y.length();
        int[] R1=new int[leny+1],R2=new int[leny+1];
        int i,j;
        
        for (i=0;i<=leny;i++)
          R1[i]=i;
        
        for(j=1;j<=lenx;j++)
        {
        	Arrays.fill(R2, 0);
        	R2[0]=j;
        	for(i=1;i<=leny;i++)
        	{
        		if(X.charAt(j-1)==Y.charAt(i-1))
        		{
        			R2[i]=R1[i-1];
        		}
        		else
        		{
        			R2[i]=(R2[i-1]<R1[i])?R2[i-1]+1:R1[i]+1;
        		}
        	}
        	R1=(int[])R2.clone();
        }
    	return R1[leny];
    }
    
    public static int [] subnormalizedEditDistance(String X, String Y) {
        int lenx=X.length();
        int leny=Y.length();
        int[] R1=new int[leny+1],R2=new int[leny+1];
        int i,j;
        
        for (i=0;i<=leny;i++)
        	R1[i]=i;
        
        for(j=1;j<=lenx;j++)
        {
        	Arrays.fill(R2, 0);
        	R2[0]=j;
        	for(i=1;i<=leny;i++)
        	{
        		if(X.charAt(j-1)==Y.charAt(i-1))
        		{
        			R2[i]=R1[i-1];
        		}
        		else
        		{
        			R2[i]=(R2[i-1]<R1[i])?R2[i-1]+1:R1[i]+1;
        		}
        	}
        	R1=(int[])R2.clone();
        }
    	return R2;
    }
    
    public static String LCSFullTable(String X, String Y) {
    	int lenx=X.length();
        int leny=Y.length();
        int[][] fullarray=new int[lenx+1][leny+1];
        int i,j;
        
        for (i=0;i<=leny;i++)
        	fullarray[0][i]=i;
        for (j=0;j<=lenx;j++)
        	fullarray[j][0]=j;
        	
        
        for(j=1;j<=lenx;j++)
        {
        	for(i=1;i<=leny;i++)
        	{
        		if(X.charAt(j-1)==Y.charAt(i-1))
        		{
        			fullarray[j][i]=fullarray[j-1][i-1];
        		}
        		else
        		{
        			fullarray[j][i]=(fullarray[j][i-1]<fullarray[j-1][i])?fullarray[j][i-1]+1:fullarray[j-1][i]+1;
        		}
        	}
        }
        i--;j--;
        Stack<Character> s=new Stack<Character>();
        while(i>0 && j>0)
        {
        	if(X.charAt(j-1)==Y.charAt(i-1))
        	{
        		s.push(X.charAt(j-1));
        		i--;
        		j--;
        	}
        	else if (fullarray[j][i-1]<=fullarray[j-1][i])
    		{
        		i--;
    		}
        	else j--;
        }
        String lsc=new String();
        while(!s.isEmpty())
        {
        	lsc+=s.pop();
        }
        return lsc;
    }
    
    public static String LCSLinearMemory(String X, String Y) {
    	int lenx=X.length();
        int leny=Y.length();
        int i,j;
        if(lenx == 0 || leny == 0) return "";
        if(lenx == 1)
        {
        	if(Y.contains(X)) return X;
        	else return "";
        }
        else if(leny == 1)
        {
        	if(X.contains(Y)) return Y;
        	else return "";
        }
    	else
    	{
    		int [] fmr=subnormalizedEditDistance(X.substring(0, lenx>>1),Y);
    		int [] rmr=subnormalizedEditDistance(new StringBuffer(X.substring(lenx>>1)).reverse().toString(),new StringBuffer(Y).reverse().toString());
    		for (i=0;i<=leny;i++)
    			rmr[i]+=fmr[leny-i];
    		int [] crmr=rmr.clone();
    		Arrays.sort(crmr);
    		List<Integer> rmrList = new ArrayList<Integer>();
    	    for (int index = 0; index <=leny; index++)
    	    {
    	        rmrList.add(rmr[index]);
    	    }
    		int spt=leny-rmrList.indexOf(crmr[0]);
    		return LCSLinearMemory(X.substring(0, lenx>>1),Y.substring(0,spt))+LCSLinearMemory(X.substring(lenx>>1),Y.substring(spt));
    	}
    }
}

