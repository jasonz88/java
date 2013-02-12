import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import edu.cornell.cs.sam.core.SamSymbolTable;
import edu.cornell.cs.sam.io.SamTokenizer;
import edu.cornell.cs.sam.io.Tokenizer.TokenType;

public class BaliCompiler
{
  //song: add keyword "else"
	static String keywords[] = { "int", "return", "if", "while", "break" , "true", "false", "else"};
	static Set<String> keywordsset = new HashSet<String>(Arrays.asList(keywords));
	static Set<String> methodsset = new HashSet<String>();
	static Set<String> methodssettmp = new HashSet<String>();

	static public class Methodspec{
		String methodname;
		int NumofPara;
		int NumofLocal;
		SamSymbolTable SymTable;
		public Methodspec(String mn, int size, SamSymbolTable st){
			methodname=mn;
			NumofLocal=0;
			NumofPara=size;
			SymTable=st;
		}
	}

	static HashMap<String,Methodspec> methodspecset=new HashMap<String,Methodspec>();

	static String compiler(String fileName) 
	{
		//returns SaM code for program in file
		try 
		{
			SamTokenizer f = new SamTokenizer (fileName);
			String pgm = getProgram(f);
			return pgm;
		} 
		catch (Exception e) 
		{
			System.err.println(e.getMessage());
			System.err.println("Fatal error: could not compile program");
			return "STOP\n";
		}
	}

	static String getProgram(SamTokenizer f)
	{
		try
		{
			String pgm="";
			while(f.peekAtKind()!=TokenType.EOF)
			{
				pgm+= getMethod(f);
			}
			//		There is a bug here. If the bali program contains "main"+String as a method,
			//		the indexOf("main") will cause stack overflow.
			//		So changed the string from "main" to "main:"

			//check if there are undefined method
			
			if(methodsset.containsAll(methodssettmp)==false){
				for(String i : methodssettmp)
					if(methodsset.contains(i)==false){
						throw new IllegalArgumentException("No "+i+" method found!!!");
					}
			}
			
			
			if(pgm.indexOf("main:")!=-1){
				//int strtofmain=pgm.indexOf("main");
				//int endofmain1=pgm.indexOf("mainEnd");
				//int endofmain=pgm.indexOf("JUMPIND", endofmain1)+9;
				String oscode="PUSHIMM 0 \nLINK\nJSR main\nPOPFBR\nSTOP\n";
				//pgm=oscode+pgm.substring(strtofmain,endofmain)+pgm.substring(0, strtofmain)+pgm.substring(endofmain);
				pgm=oscode+pgm;
			}
			else {throw new IllegalArgumentException("No \"main\" method found!!!");}
			return pgm;
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			System.err.println("Fatal error: could not compile program");
			return "STOP\n";
		}		
	}

	static String getMethod(SamTokenizer f)
	{
		//TODO: add code to convert a method declaration to SaM code.
		//Since the only data type is an int, you can safely check for int 
		//in the tokenizer.
		//TODO: add appropriate exception handlers to generate useful error msgs.

		//		TYPE ID '(' FORMALS? ')' BODY

		String RetStr="";

		//Exception template:
		//if(!()){ throw new IllegalArgumentException(", line number:"+f.lineNo());}
		if(!f.check("int")){ 
			throw new IllegalArgumentException("Invalid token, expecting \"int\", line number:"+f.lineNo());
		}

		//System.err.println(f.peekAtKind());
		if (!(f.peekAtKind().toString()=="WORD")){
			throw new IllegalArgumentException("Missing a method name, line number:"+f.lineNo());
		}

		String methodName = f.getWord();
		
		if(keywordsset.contains(methodName)) {
			throw new IllegalArgumentException("Invalid method name:"+methodName+", line number:"+f.lineNo());
		}
		
		if(!(methodsset.add(methodName))){ 
			throw new IllegalArgumentException("Duplicated method name:"+methodName+", line number:"+f.lineNo());
		}
		if(!(f.check ('('))){ 
			throw new IllegalArgumentException("Invalid token, expecting \"(\", line number:"+f.lineNo());
		}
		SamSymbolTable formals=new SamSymbolTable();
		getFormals(f,formals);
		Methodspec ms=new Methodspec(methodName,formals.getSymbols().size(),formals);

		//System.out.println(formals.toString());
		//assert f.check(')'): "Invalid token, line:" + f.lineNo();  // must be an closing parenthesis
		if(!(f.check (')'))){ 
			throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
		}
		//assert f.check('{'): "Invalid token, line:" + f.lineNo();  // must be an closing parenthesis
		if(!(f.check ('{'))){ 
			throw new IllegalArgumentException("Invalid token, expecting \"{\", line number:"+f.lineNo());
		}


		//You would need to read in formals if any
		//And then have calls to getDeclarations and getStatements.

		while (f.test("int")==true){
			RetStr+=getDeclaration(f,ms);
		}
		methodspecset.put(methodName,ms);			
		
		while (f.test('}')==false){
			RetStr+=getStatements(f,ms);
		}	


		//assert f.check('}') : "Invalid token, line: " + f.lineNo();
		if(!(f.check ('}'))){ 
			throw new IllegalArgumentException("Invalid token, expecting \"}\", line number:"+f.lineNo());
		}

		//		ADDSP c // c is number of locals
		//		code for B
		//		fEnd:
		//		STOREOFF r//r is offset of rv slot
		//		ADDSP c//pop locals off
		//		JUMPIND//return to callee
		return methodName+":\nADDSP "+
		Integer.toString(ms.NumofLocal)+"\n"+RetStr+methodName+
		"End: \nSTOREOFF "+ Integer.toString(-1-ms.NumofPara)+
		"\nADDSP "+Integer.toString(-ms.NumofLocal)+"\nJUMPIND \n";
	}

	static String getFormals(SamTokenizer f, SamSymbolTable st){

		//		FORMALS    -> TYPE ID (',' TYPE ID)*

		int AddrCount=0;
		String id;
		while (f.test(')')==false) {
			//assert f.check("int") : "Invalid token, line:" + f.lineNo();
			if(!(f.check ("int"))){ 
				throw new IllegalArgumentException("Invalid token, expecting \"int\" or \")\", line number:"+f.lineNo());
			}

			id=f.getWord();
			//assert st.resolveAddress(id)==-1 || id.equals(st.resolveSymbol(-1)) : "Duplicated variable name, line: " + f.lineNo();
			if(!(st.resolveAddress(id)==-1 || id.equals(st.resolveSymbol(-1)))){ 
				throw new IllegalArgumentException("Duplicated variable name:"+id+", line number:"+f.lineNo());
			}
			
			//assert keywordsset.contains(id)==false : "Invalid variable name, line: " + f.lineNo();
			if(keywordsset.contains(id)==true){ 
				throw new IllegalArgumentException("Invalid variable name:"+id+", line number:"+f.lineNo());
			}

			st.add(id,AddrCount++);
			if(f.test(')')) {
				break;
			}
			else {
				//assert f.check(',') : "Invalid token, line:" + f.lineNo();
				if(!(f.check(','))){ 
					throw new IllegalArgumentException("Invalid token, expecting \",\", line number:"+f.lineNo());
				}
			}
		}
		//offsets to FBR
		int n=st.getSymbols().size();
		for (String s : st.getSymbols())
			st.add(s, st.resolveAddress(s)-n);

		return "";
	}

	static String getExp(SamTokenizer f,SamSymbolTable st) 
	{
		//		EXP        -> LOCATION
		//        | LITERAL
		//        | METHOD '(' ACTUALS? ')'
		//        | '('EXP '+' EXP')'
		//        | '('EXP '-' EXP')'
		//        | '('EXP '*' EXP')'
		//        | '('EXP '/' EXP')'
		//        | '('EXP '&' EXP')'
		//        | '('EXP '|' EXP')'
		//        | '('EXP '<' EXP')'
		//        | '('EXP '>' EXP')'
		//        | '('EXP '=' EXP')'
		//        | '(''-' EXP')'
		//        | '(''!' EXP')'
		//        | '(' EXP ')'

		String RetStr="";
		switch (f.peekAtKind()) {
		//		INT        -> '-'? [1-9] [0-9]*
		case INTEGER: //E -> integer
			return "PUSHIMM " + f.getInt() + "\n";
		case OPERATOR:  
		{
			//		I found the following part really weird.
			//		I tested "if ((z>-1))", which should be correct in Bali Syntax,
			//		but the program reaches this part when parsing "-1". 
			//		Apparently, the SamTokenizer treats "-1" as "-" and "1", which is weird,
			//		but anyway I write the following code to cover this case.
			if (f.test('-')){
				//System.err.println("weird cases:");
				f.skipToken();

				//song: Bug here, does not check the type before calling f.getInt();
				if (!(f.peekAtKind().toString()=="INTEGER")){
					throw new IllegalArgumentException("Invalid token, expecting an integer, line number:"+f.lineNo());
				}
				int minus_num=f.getInt();
				minus_num = minus_num * -1;
				//System.err.println(minus_num);
				return "PUSHIMM "+minus_num+"\n";
			}

			//assert f.check('('): "Invalid token, line: "+f.lineNo();
			if(!(f.check('('))){ 
				throw new IllegalArgumentException("Invalid token, expecting \"(\", line number:"+f.lineNo());
			}
			//	        | '(''-' EXP')'
			if(f.test('-')){
				f.skipToken();
				RetStr=getExp(f,st);
				//assert f.check(')'): "Invalid token, line: "+f.lineNo();
				if(!(f.check(')'))){ 
					throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
				}
				return RetStr+"PUSHIMM -1 \nTIMES \n";
			}
			//        | '(''!' EXP')'
			else if(f.test('!')){
				f.skipToken();
				RetStr=getExp(f,st);
				//assert f.check(')'): "Invalid token, line: "+f.lineNo();
				if(!(f.check(')'))){ 
					throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
				}
				return RetStr+"NOT \n";
			}
			else{
				//		        | '('EXP '+' EXP')'
				//        | '('EXP '-' EXP')'
				//        | '('EXP '*' EXP')'
				//        | '('EXP '/' EXP')'
				//        | '('EXP '&' EXP')'
				//        | '('EXP '|' EXP')'
				//        | '('EXP '<' EXP')'
				//        | '('EXP '>' EXP')'
				//        | '('EXP '=' EXP')'
				//        | '(' EXP ')'

				RetStr=getExp(f,st);

				switch(f.getOp()){
				case '=': 
					RetStr+=getExp(f,st);
					//assert f.check(')'): "Invalid token, line: "+f.lineNo();
					if(!(f.check(')'))){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					return RetStr+"EQUAL \n";
				case '>':
					RetStr+=getExp(f,st);
					//assert f.check(')'): "Invalid token, line: "+f.lineNo();
					if(!(f.check(')'))){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					return RetStr+"GREATER \n";
				case '<':
					RetStr+=getExp(f,st);
					//assert f.check(')'): "Invalid token, line: "+f.lineNo();
					if(!(f.check(')'))){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					return RetStr+"LESS \n";
				case '|':
					RetStr+=getExp(f,st);
					//assert f.check(')'): "Invalid token, line: "+f.lineNo();
					if(!(f.check(')'))){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					return RetStr+"OR \n";
				case '&':
					RetStr+=getExp(f,st);
					//assert f.check(')'): "Invalid token, line: "+f.lineNo();
					if(!(f.check(')'))){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					return RetStr+"AND \n";
				case '+': 
					RetStr+=getExp(f,st);
					//assert f.check(')'): "Invalid token, line: "+f.lineNo();
					if(!(f.check(')'))){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					return RetStr+"ADD \n";
				case '-':
					RetStr+=getExp(f,st);
					//assert f.check(')'): "Invalid token, line: "+f.lineNo();
					if(!(f.check(')'))){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					return RetStr+"SUB \n";
				case '*':
					RetStr+=getExp(f,st);
					//assert f.check(')'): "Invalid token, line: "+f.lineNo();
					if(!(f.check(')'))){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					return RetStr+"TIMES \n";
				case '/':
					RetStr+=getExp(f,st);
					//assert f.check(')'): "Invalid token, line: "+f.lineNo();
					if(!(f.check(')'))){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					return RetStr+"DIV \n";
				case ')':
					return RetStr;
				default:
					//assert false: "Invalid token, line: "+f.lineNo();
					//return null;
					throw new IllegalArgumentException("Invalid token in EXP expression, line number:"+f.lineNo());
				}
			}	
		}
		case WORD:{
			//song: Bug here, need to skip token "true" or "false"
			if (f.test("true")){ 
				f.skipToken();
				return "PUSHIMM 1 \n";
			}
			if (f.test("false")){ 
				f.skipToken();
				return "PUSHIMM 0 \n";
			}
			String id;
			int Addr;
			id=f.getWord();
			
			//assert keywordsset.contains(id)==false : "Invalid variable name, line: " + f.lineNo();
			if(keywordsset.contains(id)==true){ 
				throw new IllegalArgumentException("Invalid variable name:"+id+", line number:"+f.lineNo());
			}
			//handle function call, check parameter correspondence
			//			f(e1,e2,en)
			//			PUSHIMM 0//return value slot
			//			Code for e1
			//			
			//			Code for en
			//			LINK//save FBR and update it
			//			JSR f
			//			POPFBR//restore FBR
			//			ADDSP n//pop parameters
			if (methodsset.contains(id)){
				//assert f.check('('): "Invalid token, line: "+f.lineNo();
				if(!(f.check('('))){ 
					throw new IllegalArgumentException("Invalid token, expecting \"(\", line number:"+f.lineNo());
				}
				RetStr="PUSHIMM 0 \n";
				
				for(int i=0;i<methodspecset.get(id).NumofPara-1;i++)
				{
					RetStr+=getExp(f,st);
					//assert f.check(','): "Invalid method call expression, line: "+f.lineNo();
					if(!(f.check(','))){ 
						throw new IllegalArgumentException("Invalid token, expecting \",\", line number:"+f.lineNo());
					}
				}

				//		Bug here if a method has no input arguments.
				//		Add the following line.
				if (methodspecset.get(id).NumofPara!=0)
					RetStr+=getExp(f,st);

				//assert f.check(')'): "Invalid method call expression, line: "+f.lineNo();
				if(!(f.check(')'))){ 
					throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
				}
				RetStr+="LINK \nJSR "+id+" \nPOPFBR \nADDSP "+
						Integer.toString(-methodspecset.get(id).NumofPara)+"\n";
				return RetStr;
			}
			
			//method call but the method is not declared or defined yet
			
			if(f.test('(')){
				methodssettmp.add(id);
				f.skipToken();
				int numofPara=0;
				RetStr="PUSHIMM 0 \n";
				while(f.test(')')==false){
					RetStr+=getExp(f,st);
					numofPara++;
					if(f.test(',')){
						f.skipToken();
					}
					else if(f.check(')')){
					break;
					}
					else{
						throw new IllegalArgumentException("Invalid token, expecting \",\", line number:"+f.lineNo());
					}
				}
				RetStr+="LINK \nJSR "+id+" \nPOPFBR \nADDSP "+
						Integer.toString(-numofPara)+"\n";
				return RetStr;
			}

			//variables
			//			System.out.println(id);
			//			System.out.println(st.toString());
			//System.out.println(st.resolveAddress("a"));
			//st.add("a", -2);
			//System.out.println(st.resolveAddress("a"));
			//			System.out.println(st.toString()+id);
			
			
			//song:
			if(keywordsset.contains(id)==true){ 
				throw new IllegalArgumentException("Invalid variable name:"+id+", line number:"+f.lineNo());
			}

			//assert st.resolveAddress(id)!=-1 || id.equals(st.resolveSymbol(-1)) : "Undefined variable, line: " + f.lineNo();
			if(!(st.resolveAddress(id)!=-1 || id.equals(st.resolveSymbol(-1)))){ 
				throw new IllegalArgumentException("Undefined variable name:"+id+", line number:"+f.lineNo());
			}
			
			Addr=st.resolveAddress(id);
			//			System.out.println("PUSHOFF " + Integer.toString(Addr)+"\n"+id);
			return "PUSHOFF " + Integer.toString(Addr)+"\n";
			
		}
		default:   
			//assert false :  "Invalid token, line: " + f.lineNo();
			//return null;
			throw new IllegalArgumentException("Invalid token in EXP expression, line number:"+f.lineNo());
		}
	}

	static String getDeclaration(SamTokenizer f, Methodspec ms)
	{
		//		VAR_DECL   -> TYPE ID ('=' EXP)? (',' ID ('=' EXP)?)* ';'

		String id;
		String RetStr=new String();
		int Offset=ms.NumofLocal+1; //offset from FBR, one more slot for PC!!!!!!
		//assert f.check("int") : "Invalid token, line: " + f.lineNo();
		if(!f.check("int")){ 
			throw new IllegalArgumentException("Invalid token, expecting \"int\", line number:"+f.lineNo());
		}
		while(f.test(';')==false){
			id=f.getWord();
			//assert ms.SymTable.resolveAddress(id)==-1 || id.equals(ms.SymTable.resolveSymbol(-1)): "Duplicated variable name, line: " + f.lineNo();
			if(!(ms.SymTable.resolveAddress(id)==-1 || id.equals(ms.SymTable.resolveSymbol(-1)))){ 
				throw new IllegalArgumentException("Duplicated variable name:"+id+", line number:"+f.lineNo());
			}
			//assert keywordsset.contains(id)==false : "Invalid variable name, line: " + f.lineNo();
			if(keywordsset.contains(id)==true){ 
				throw new IllegalArgumentException("Invalid variable name:"+id+", line number:"+f.lineNo());
			}
			ms.SymTable.add(id,++Offset);

			if(f.test(';')) 
			{
				f.skipToken();
				break;
			}
			else if(f.test('=')) 
			{
				f.skipToken();
				RetStr+=getExp(f,ms.SymTable)+"STOREOFF "+ms.SymTable.resolveAddress(id)+"\n";
				if(f.test(';')) {
					f.skipToken();
					break;
				}
				//assert f.check(',') : "Invalid token, line: " + f.lineNo();
				if(!f.check(',')){ 
					throw new IllegalArgumentException("Invalid token, expecting \",\" or \",\", line number:"+f.lineNo());
				}
			}
			else if(f.test(','))
			{
				f.skipToken();
			}
			//else assert false : "Invalid token, line: " + f.lineNo();
			else {
				throw new IllegalArgumentException("Invalid token, expecting \",\" or \";\", line number:"+f.lineNo());
			}
		}
		ms.NumofLocal=Offset-1;
		return RetStr;
	}

	static String getStatements(SamTokenizer f, Methodspec ms)
	{
		//		STMT       -> ASSIGN ';'
		//        | return EXP ';'
		//        | if '(' EXP ')' STMT else STMT
		//        | while '(' EXP ')' STMT
		//        | break ';'
		//        | BLOCK
		//        | ';'

		String id,RetStr,RetStr1;
		int Addr;
		switch (f.peekAtKind()) {
			case WORD:
			{
				//	        | return EXP ';'
				//			code for e
				//			JUMP fEnd//go to end of method
				if(f.test("return")){
					f.check("return");
					RetStr=getExp(f,ms.SymTable);
					//assert f.check(';') : "Invalid token, line: " + f.lineNo();
					if(!f.check(';')){ 
						throw new IllegalArgumentException("Invalid token, expecting \";\", line number:"+f.lineNo());
					}
					return RetStr+"JUMP "+ms.methodname+"End\n";
				}
				//			if '(' EXP ')' STMT else STMT
				//			code for e
				//			JUMPC newLabel1
				//			code for B2
				//			JUMP newLabel2
				//			newLabel1:
				//			code for B1
				//			newLabel2:
				else if(f.test("if")){
					f.skipToken();
					//assert f.check('(') : "Invalid token, line: " + f.lineNo();
					if(!f.check('(')){ 
						throw new IllegalArgumentException("Invalid token, expecting \"(\", line number:"+f.lineNo());
					}
					RetStr=getExp(f,ms.SymTable);
					String newlabel1=randomString(5);
					String newlabel2=randomString(5);
					RetStr+="JUMPC "+newlabel1+"\n";
					//assert f.check(')') : "Invalid token, line: " + f.lineNo();
					if(!f.check(')')){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					RetStr1=getStatements(f,ms);
					//assert f.check("else") : "Invalid token, line: " + f.lineNo();
					if(!f.check("else")){ 
						throw new IllegalArgumentException("Invalid token, expecting \"else\", line number:"+f.lineNo());
					}
					RetStr+=getStatements(f,ms)+"JUMP "+newlabel2+"\n"+newlabel1+":\n"+RetStr1+newlabel2+":\n";
					return RetStr;
				}
				//	          | while '(' EXP ')' STMT
				//	          JUMP newLabel1
				//	          newLabel2:
				//	          code for B
				//	          newLabel1:
				//	          code for e
				//	          JUMPC newLabel2
				else if(f.test("while"))
				{
					f.skipToken();
					//assert f.check('(') : "Invalid token, line: " + f.lineNo();
					if(!f.check('(')){ 
						throw new IllegalArgumentException("Invalid token, expecting \"(\", line number:"+f.lineNo());
					}
					RetStr=getExp(f,ms.SymTable);
					String newlabel1=randomString(5);
					String newlabel2=randomString(5);
					//assert f.check(')') : "Invalid token, line: " + f.lineNo();
					if(!f.check(')')){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					StringBuffer whileend=new StringBuffer();
					RetStr1=getStatements(f,ms,whileend);

					//		There is a problem here when there is no "break" inside the "while" loop.
					//		whileend contains a null string, but "whileend.equals(new StringBuffer())"
					//		will be false.
					//String tmp_string=whileend.toString();
					//System.err.println(tmp_string.equals(""));
					//

					//I found the following method works, but does not think it is a good one...
					//System.err.println(whileend.toString());
					if (whileend.toString().equals("")){
						return "JUMP "+newlabel1+" \n"+ newlabel2+": \n"+RetStr1+newlabel1+": \n"+RetStr+"JUMPC "+newlabel2+" \n";
					}
					else{
						String tmp=whileend.toString();
						//		A bug found here in senario: outter while does not have break, but inner while has one.
						//		The following statement does not clear the whileend buffer, and hence outter while loop also prints an end label.
						whileend.delete(0,whileend.length());
						//whileend=new StringBuffer();
						return "JUMP "+newlabel1+" \n"+ newlabel2+": \n"+RetStr1+newlabel1+": \n"+RetStr+"JUMPC "+newlabel2+" \n"+tmp+": \n";
					}
				}
				//ASSIGN ';'
				//			code for e
				//			STOREOFF yy
				else
				{
					id=f.getWord();
					
					if(keywordsset.contains(id)==true){ 
						throw new IllegalArgumentException("Invalid variable name:"+id+", line number:"+f.lineNo());
					}
					
					if(!(ms.SymTable.resolveAddress(id)!=-1 || id.equals(ms.SymTable.resolveSymbol(-1)))){ 
						throw new IllegalArgumentException("Undefined variable: "+id+", line number:"+f.lineNo());
					}
					Addr=ms.SymTable.resolveAddress(id);
					//assert f.check('=') : "Invalid token, line: " + f.lineNo();
					if(!f.check('=')){ 
						throw new IllegalArgumentException("Invalid token, expecting \"=\", line number:"+f.lineNo());
					}
					RetStr=getExp(f,ms.SymTable);
					//assert f.check(';') : "Invalid token, line: " + f.lineNo();
					if(!f.check(';')){ 
						throw new IllegalArgumentException("Invalid token, expecting \";\", line number:"+f.lineNo());
					}
					return RetStr+"STOREOFF " + Integer.toString(Addr)+"\n";
				}
			}
			case OPERATOR:
			{
				//			| BLOCK
				//			BLOCK      -> '{' STMT* '}'
				if(f.test('{'))
				{
					f.skipToken();
					RetStr="";
					while (!f.test('}')){
						RetStr+=getStatements(f, ms);
					}
					//System.out.println(f.peekAtKind());
					//assert f.check('}') : "Invalid token, line: " + f.lineNo();
					if(!f.check('}')){ 
						throw new IllegalArgumentException("Invalid token, expecting \"}\", line number:"+f.lineNo());
					}
					return RetStr;
				}
				else if(f.check(';')) return "\n";
				else {
					//assert false : "Invalid token, line: " + f.lineNo();
					//return null;
					throw new IllegalArgumentException("Invalid token, expecting \"{\", line number:"+f.lineNo());
				}
			}
			default:   {
				//assert false : "Invalid token, line: " + f.lineNo();
				//return null;
				throw new IllegalArgumentException("Invalid token in STMT expression, line number:"+f.lineNo());
			}
		}
	}

	//All the "getStatements" functions inside this function 
	//should call "getStatements" function with "whileend".
	static String getStatements(SamTokenizer f, Methodspec ms, StringBuffer whileend)
	{
		//		STMT       -> ASSIGN ';'
		//        | return EXP ';'
		//        | if '(' EXP ')' STMT else STMT
		//        | while '(' EXP ')' STMT
		//        | break ';'
		//        | BLOCK
		//        | ';'

		String id,RetStr,RetStr1;
		int Addr;
		switch (f.peekAtKind()) {
			case WORD:
			{
				//	        | return EXP ';'
				//			code for e
				//			JUMP fEnd//go to end of method
				if(f.test("return")){
					f.check("return");
					RetStr=getExp(f,ms.SymTable);
					//assert f.check(';') : "Invalid token, line: " + f.lineNo();
					if(!f.check(';')){ 
						throw new IllegalArgumentException("Invalid token, expecting \";\", line number:"+f.lineNo());
					}
					return RetStr+"JUMP "+ms.methodname+"End\n";
				}
				//			if '(' EXP ')' STMT else STMT
				//			code for e
				//			JUMPC newLabel1
				//			code for B2
				//			JUMP newLabel2
				//			newLabel1:
				//			code for B1
				//			newLabel2:
				else if(f.test("if")){
					f.skipToken();
					//assert f.check('(') : "Invalid token, line: " + f.lineNo();
					if(!f.check('(')){ 
						throw new IllegalArgumentException("Invalid token, expecting \"(\", line number:"+f.lineNo());
					}
					RetStr=getExp(f,ms.SymTable);
					String newlabel1=randomString(5);
					String newlabel2=randomString(5);
					RetStr+="JUMPC "+newlabel1+"\n";
					//assert f.check(')') : "Invalid token, line: " + f.lineNo();
					if(!f.check(')')){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					RetStr1=getStatements(f,ms,whileend);
					//assert f.check("else") : "Invalid token, line: " + f.lineNo();
					if(!f.check("else")){ 
						throw new IllegalArgumentException("Invalid token, expecting \"else\", line number:"+f.lineNo());
					}
					RetStr+=getStatements(f,ms,whileend)+"JUMP "+newlabel2+"\n"+newlabel1+":\n"+RetStr1+newlabel2+":\n";
					return RetStr;
				}			
				//	          | while '(' EXP ')' STMT
				//	          JUMP newLabel1
				//	          newLabel2:
				//	          code for B
				//	          newLabel1:
				//	          code for e
				//	          JUMPC newLabel2
				else if(f.test("while"))
				{
					f.skipToken();
					//assert f.check('(') : "Invalid token, line: " + f.lineNo();
					if(!f.check('(')){ 
						throw new IllegalArgumentException("Invalid token, expecting \"(\", line number:"+f.lineNo());
					}
					RetStr=getExp(f,ms.SymTable);
					String newlabel1=randomString(5);
					String newlabel2=randomString(5);
					//assert f.check(')') : "Invalid token, line: " + f.lineNo();
					if(!f.check(')')){ 
						throw new IllegalArgumentException("Invalid token, expecting \")\", line number:"+f.lineNo());
					}
					RetStr1=getStatements(f,ms,whileend);

					//		There is a problem here when there is no "break" inside the "while" loop.
					//		whileend contains a null string, but "whileend.equals(new StringBuffer())"
					//		will be false.
					//String tmp_string=whileend.toString();
					//System.err.println(tmp_string.equals(""));
					//

					//I found the following method works, but does not think it is a good one...
					if (whileend.toString().equals("")){
						return "JUMP "+newlabel1+" \n"+ newlabel2+": \n"+RetStr1+newlabel1+": \n"+RetStr+"JUMPC "+newlabel2+" \n";
					}
					else{
						String tmp=new String(whileend);
						//		A bug found here in senario: outter while does not have break, but inner while has one.
						//		The following statement does not clear the whileend buffer, and hence outter while loop also prints an end label.
						whileend.delete(0,whileend.length());
						//whileend=new StringBuffer();
						return "JUMP "+newlabel1+" \n"+ newlabel2+": \n"+RetStr1+newlabel1+": \n"+RetStr+"JUMPC "+newlabel2+" \n"+tmp+": \n";
					}
				}
				//			| break ';'
				//			a break statement must be lexically nested within one or more loops, 
				//			and when it is executed, it terminates the execution of the innermost loop in which it is nested.
				else if(f.test("break"))
				{
					f.skipToken();
					//assert f.check(';') : "Invalid token, line: " + f.lineNo();
					if(!f.check(';')){ 
						throw new IllegalArgumentException("Invalid token, expecting \";\", line number:"+f.lineNo());
					}
					if(whileend.length()==0){
						whileend.append(randomString(5));
					}
					
					//System.err.println(whileend);

					return "JUMP "+whileend+" \n";
				}
				//ASSIGN ';'
				//			code for e
				//			STOREOFF yy
				else
				{
					id=f.getWord();

					//song:
					if(keywordsset.contains(id)==true){ 
						throw new IllegalArgumentException("Invalid variable name:"+id+", line number:"+f.lineNo());
					}
					//assert ms.SymTable.resolveAddress(id)!=-1 || id.equals(ms.SymTable.resolveSymbol(-1)) : "Undefined variable, line: " + f.lineNo();
					if(!(ms.SymTable.resolveAddress(id)!=-1 || id.equals(ms.SymTable.resolveSymbol(-1)))){ 
						throw new IllegalArgumentException("Undefined variable: "+id+", line number:"+f.lineNo());
					}
					Addr=ms.SymTable.resolveAddress(id);
					//assert f.check('=') : "Invalid token, line: " + f.lineNo();
					if(!f.check('=')){ 
						throw new IllegalArgumentException("Invalid token, expecting \"=\", line number:"+f.lineNo());
					}
					RetStr=getExp(f,ms.SymTable);
					//assert f.check(';') : "Invalid token, line: " + f.lineNo();
					if(!f.check(';')){ 
						throw new IllegalArgumentException("Invalid token, expecting \";\", line number:"+f.lineNo());
					}

					//return getExp(f,ms.SymTable)+"STOREOFF " + Integer.toString(Addr)+"\n";
					return RetStr+"STOREOFF " + Integer.toString(Addr)+"\n";
				}
			}
			case OPERATOR:
			{
				//			| BLOCK
				//			BLOCK      -> '{' STMT* '}'
				if(f.test('{'))
				{
					//orginal code:
					//f.skipToken();
					//RetStr=getStatements(f, ms);
					//assert f.check('}') : "Invalid token, line: " + f.lineNo();
					//return RetStr;

					f.skipToken();
					RetStr="";
					while (!f.test('}')){
						RetStr+=getStatements(f, ms, whileend);
					}
					//System.out.println(f.peekAtKind());
					//assert f.check('}') : "Invalid token, line: " + f.lineNo();
					if(!f.check('}')){ 
						throw new IllegalArgumentException("Invalid token, expecting \"}\", line number:"+f.lineNo());
					}
					return RetStr;

				}
				else if(f.check(';')) return "\n";
				else {
					//assert false : "Invalid token, line: " + f.lineNo();
					//return null;
					throw new IllegalArgumentException("Invalid token, expecting \"{\", line number:"+f.lineNo());
				}
			}
			default:   {
				//assert false : "Invalid token, line: " + f.lineNo();
				//return null;
				throw new IllegalArgumentException("Invalid token in STMT expression, line number:"+f.lineNo());
			}
		}
	}

	static final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	static Random rnd = new Random();

	static String randomString( int len ) 
	{
		StringBuilder sb = new StringBuilder( len );
		for( int i = 0; i < len; i++ ) 
			sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		return sb.toString();
	}

}
