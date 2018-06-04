package sootapdg.PDG;
import soot.*;
import java.util.Iterator;
import java.util.*;

import soot.SootClass;
import soot.SootMethod;

public class Preprocessor {
	
	 private static Preprocessor o = null;
     private Preprocessor() {}
	
	 
	
	 
	 public static Preprocessor v()
     {
             if (o == null)
                     o = new Preprocessor();
             return o;
     }

	 
	 public void preprocess(Iterator classesIt)
	 {
//		 Preprocess every method of evey class
			while(classesIt.hasNext())
			{
				
			 Iterator mIt = ((SootClass)classesIt.next()).methodIterator();
				
				
				while (mIt.hasNext())
				{
					SootMethod m = (SootMethod)mIt.next();
					if (!m.isConcrete())
						continue;
					
					new MethodPreprocessor(m);
				}
			}
	 }
	
	 
	 public void set_return_types(Iterator classesIt)
	 {
	 }
	
	 
}
