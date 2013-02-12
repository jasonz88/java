import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MyCompiler {

  public static void Save(String filename, String textToSave) {
		File file = new File(filename);
	    //file.delete();
	    try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(file));
	        out.write(textToSave);
	        out.close();
	    } catch (IOException e) {
	    }
	}

	public static void main(String[] args){
		//System.out.println("args[0]= " + args[0]);
		//System.out.println("args[1]= " + args[1]);
		//System.out.println(BaliCompiler.compiler(args[0]));

		Save(args[1],BaliCompiler.compiler(args[0]));
	}
}
