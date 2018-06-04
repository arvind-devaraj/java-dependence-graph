package sootapdg.PDG;

import soot.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.util.HashMultiMap;
import soot.util.MultiMap;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.SimpleLocalDefs;
import sootapdg.DummyFormalInAssignStmt;
import sootapdg.DummyFormalOutAssignStmt;

public class MDG {
	// the entry unit
	

	

	public MDG(SootMethod m) {
		
		System.out.print("\nMDG");
		
		Util.v().log("dataedge",m.getName() );
		
		
		JimpleBody body = (JimpleBody) m.getActiveBody();
		
		// Construct a new graph of the modified body 
		
		UnitGraph graph=new SESEGraph(body);
		
		
		GraphStore.v().method_entryunit.put(m, ((SESEGraph)graph).getEntry() );
		
		
		DDG ddg=new DDG(graph);
		
		//ddg.print();
		
		((SESEGraph)graph).add_dummy_edges();
		
		CDG cdg=new CDG(graph);
		
		
		//cdg.print() ;
		
		
		
		
	}
	
	
	public void slice(int n)
	{
		//Unit point=(Unit)unit_list.get(n);
		//System.out.print("\nSlicing criteria:"+point);
		//data_dep_graph.get_parents(point);
	}
	
	

	

	
	
	
	
	

}
