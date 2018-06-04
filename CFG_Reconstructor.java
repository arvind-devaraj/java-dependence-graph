package sootapdg.PDG;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.PatchingChain;
import soot.util.*;
import java.util.Iterator;
import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.util.*;
import java.io.*;
import java.util.*;

import javax.swing.JLabel;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JAssignStmt;
import sootapdg.DummyActualInAssignStmt;
import sootapdg.DummyActualOutAssignStmt;
import sootapdg.DummyAssignStmt;
import sootapdg.DummyFormalInAssignStmt;
import sootapdg.DummyFormalOutAssignStmt;
import sootapdg.PDG.MethodPreprocessor;

public class CFG_Reconstructor {
//	 if the unit is in slice and the unit is not dummy then print it
	public  CFG_Reconstructor(Iterator classesIt) {
		System.out.println("\n\nPRINTING_NEW_CFG");
		while (classesIt.hasNext()) {
			System.out.print("Hi");
			SootClass current_class=(SootClass) classesIt.next();
			Iterator mIt = current_class.methodIterator();
			while (mIt.hasNext()) {
				SootMethod method = (SootMethod) mIt.next();
				System.out.println("\nmethod:" + method.getName());
				if (!method.isConcrete())
					continue;
				boolean is_init= method.getName().equals("<init>");
				
				if(is_init)
				{
					System.out.print("DB"+method);
				}
				else {
				remove_stmts(method);
				}
			}
			
			writeClass(current_class);
		}
		
	}//proc
	

	private void remove_stmts(SootMethod method)
	{

		JimpleBody body = (JimpleBody) method.getActiveBody();
		
		Chain new_units= new  PatchingChain( body.getUnits());
		Iterator new_it=new_units.iterator() ;

		Iterator unitsIt = body.getUnits().iterator();
		while (unitsIt.hasNext()) {
			Unit unit = (Unit) unitsIt.next();
			boolean in_slice = GraphStore.v().units_in_slice
					.contains(unit);
			
				
			boolean is_assign= unit instanceof JAssignStmt;
			boolean is_label=(unit instanceof JLabel);
			boolean is_call=unit instanceof JInvokeStmt;
			if(  ( (is_assign|| is_call) && !in_slice )  )
			{		
				  
			
					System.out.print("\nremoving "+unit);
				unitsIt.remove();
				
			
			}
		}
		System.out.print("new:"+ body.getUnits());
	

	}
	private void writeClass(SootClass current_class)
	{
		try {
			String fileName = SourceLocator.v().getFileNameFor(current_class, Options.output_format_class);
	        OutputStream streamOut = new JasminOutputStream(
	                                    new FileOutputStream(fileName));
	        PrintWriter writerOut = new PrintWriter(
	                                    new OutputStreamWriter(streamOut));
	        JasminClass jasminClass = new soot.jimple.JasminClass(current_class);
	        jasminClass.print(writerOut);
	        writerOut.flush();
	        streamOut.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}
	
}//class
