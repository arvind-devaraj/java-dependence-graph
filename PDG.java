package sootapdg.PDG;

import java.util.*;
import soot.jimple.internal.JAssignStmt;
import soot.*;
import soot.util.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import soot.Body;
import soot.G;
import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import soot.jimple.*;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.StmtBody;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;

import soot.shimple.Shimple;
import soot.shimple.ShimpleBody;
import soot.shimple.toolkits.scalar.ShimpleLocalDefs;

import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.DominanceFrontier;
import soot.toolkits.graph.DominatorNode;
import soot.toolkits.graph.DominatorTree;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.scalar.SimpleLocalDefs;
import sootapdg.DummyActualOutAssignStmt;
import sootapdg.DummyAssignStmt;
import sootapdg.DummyFormalInAssignStmt;
import sootapdg.DummyFormalOutAssignStmt;



import soot.Scene;

/**
 * @author Srihari Sukumaran
 */

public class PDG {



	public PDG(Iterator classesIt)

	{


		// Build SDG for every class
		while (classesIt.hasNext()) {
			SootClass cl = (SootClass) classesIt.next();
			build_SDG(cl);
			// add summary edges
			new SummaryBuilder(cl);
		}
		if(true)
		{

		SootClass c = Scene.v().getSootClass( Util.v().sc_class);
		Iterator mIt=c.methodIterator() ;
		while(mIt.hasNext())
		{
			SootMethod temp= (SootMethod)(mIt.next());
			if(temp.getName().equals( Util.v().sc_method )); 
				slice(temp, Util.v().sc_line );
		}
		}
		// Slice
		if(false)
		{

		SootClass c = Scene.v().getSootClass("test.ex_sdg");
		Iterator mIt=c.methodIterator() ;
		while(mIt.hasNext())
		{
			SootMethod temp= (SootMethod)(mIt.next());
			if(temp.getName().equals("main")) 
				slice(temp, 20);
		}
		}
		// construct new CFG, including only those units in slice

	
	}

	

	private void slice(SootMethod m, int line_no) {
		JimpleBody b = (JimpleBody) m.getActiveBody();
		Iterator unitsIt = b.getUnits().iterator();
		int line_count = 0;
		while (unitsIt.hasNext()) {

			Unit unit = (Unit) unitsIt.next();
			;
			if (line_no == line_count) {
				GraphStore.v().slice(unit);

			}
			line_count++;

		}

	}

	/**
	 * Returns the list of methods that should be considered entry points for
	 * building the CallGraph.
	 * 
	 * @param source
	 *            The main class in the Scene.
	 * @return The list of entry points.
	 */

	/* Creates SDG for methods in a class */
	private void build_SDG(SootClass cl) {
		/* BEGIN CALL GRAPH CONSTRUCTION */

		/* END CALL GRAPH CONSTRUCTION */

		Iterator methodIt = cl.methodIterator();
		while (methodIt.hasNext()) {
			SootMethod m = (SootMethod) methodIt.next();

			if (!m.isConcrete())
				continue;
			print("\n\nmethod: " + m.getName());
			MDG mdg = new MDG(m);
			//print_body(m.getActiveBody());
		}

	}

	// ///////////////////////////////////////////////////////////////////////////////////////
	
	void print_body(Body b) {
		System.out.print("\nPrinting Body of " + b.getMethod().getSignature()
				+ "\n");
		PatchingChain units = b.getUnits();
		Iterator it = units.iterator();
		int line_no = 1;
		while (it.hasNext()) {
			Unit u = (Unit) it.next();

			System.out.print( "\n" + line_no + " :" + "  " + u );
			
			line_no++;
		}

	}

	private void print(String s) {
		System.out.print(s);
	}

}
