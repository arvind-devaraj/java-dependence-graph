package sootapdg.PDG;

import java.util.Iterator;

import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.*;

public class CallDG {
	
	public CallDG(SESEGraph graph) {
		
		
		Body body= graph.getBody() ;
				// go through all statements in the body

		for (Iterator uIt =  body.getUnits().iterator(); uIt
				.hasNext();) {

			// a Soot object representing a JIMPLE statement
			Stmt s = (Stmt) uIt.next();

			// not all statements have calls inside them. if there
			// isn't a call, we just go to the next statement
			if (!s.containsInvokeExpr())
				continue;

			// get a Soot object that represents the call expression
			InvokeExpr call = (InvokeExpr) s.getInvokeExpr();
			
			// this is the static target, need to handle inheritance and polymorphism 
			// refer ChaAnalysis.java : processMethod
			Unit tgt=null;
			//call_edges.add_edge(call,tgt);

		}
	}

}
