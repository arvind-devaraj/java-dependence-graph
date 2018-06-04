package sootapdg.PDG;
import soot.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.SootClass;
import soot.Unit;
import sootapdg.DummyActualOutAssignStmt;
import sootapdg.DummyFormalInAssignStmt;

public class SummaryBuilder {
	
	Set PathEdges = new HashSet();

	Set SummaryEdges = new HashSet();

	List Worklist = new ArrayList();

	void propagate(Edge e) {
		if (!PathEdges.contains(e)) {
			PathEdges.add(e);
			Worklist.add(e);
		}
	}
	void add_summary_edge(Unit act_in_node,Unit act_out_node)
	{
		SootMethod src_method=(SootMethod) GraphStore.v().unit_method.get(act_in_node);
		SootMethod tgt_method=(SootMethod) GraphStore.v().unit_method.get(act_out_node);
		if(src_method!=null && tgt_method!=null && src_method.equals(tgt_method))
		{
		
		
		
		GraphStore.v().add_edge( act_in_node, act_out_node, GraphStore.SUMMARY);
		System.out.print( "\n	SUM " +act_in_node + " \t"+ act_out_node );
		
		}
	}

	public  SummaryBuilder(SootClass cl) {

		Set formalouts = GraphStore.v().formalouts();
		System.out.print("Building Summary" + formalouts.size());
		Iterator formal_it = formalouts.iterator();

		while (formal_it.hasNext()) {
			Unit form_out = (Unit) formal_it.next();
			Edge self_edge=new Edge(form_out, form_out);
			propagate(self_edge);
			System.out.print("\nAdding as form_out" + form_out);

		}
		while (!Worklist.isEmpty()) {
			Edge e = (Edge) Worklist.remove(0);
			
			// no need to check tgt instanceof FormOut
			// all m in (n,m) belonging to worklist are FormOuts
			
			if(e.src instanceof DummyFormalInAssignStmt)
			{
				//Unit form_in_node = e.src;  //n
				//Unit form_out_node = e.tgt; // m
				System.out.println("\n_ff:" + e.src + "->" + e.tgt);
				Iterator form_in_parents= get_param_in_parents(e.src);
				while( form_in_parents.hasNext() )
				{
					Unit act_in_node=(Unit) form_in_parents.next() ;
					
					Iterator form_out_children=get_param_out_children(e.tgt);
					while( form_out_children.hasNext())
					{
						Unit act_out_node=(Unit) form_out_children.next();
						
						add_summary_edge(act_in_node,act_out_node);
						
						HashSet new_edges=new HashSet();
						
						
						Iterator i=PathEdges.iterator() ;
						while( i.hasNext() )
						{
							Edge e2= (Edge)i.next() ;
							if( e2.src == act_out_node)
							{
								new_edges.add(new Edge(act_in_node,e2.tgt));
							}
						}
						
						Iterator j=new_edges.iterator();
						while(j.hasNext())
						{
							propagate( (Edge)j.next() );
						}
						//propagate(new Edge(act_in_node,));
					}
				
					
					
				}
			}	
			

			else {
				
				System.out.println("");
				Iterator it = get_DCS_parents_iterator(e.src);
				while (it.hasNext()) {
					Unit parent = (Unit) it.next();
					propagate(new Edge(parent, e.tgt));
					Util.v().log("summary","_edge:" + parent + "->" + e.tgt);
				}
				
			}//endif
		}//while

	}

	private Iterator get_param_in_parents(Object o)
	{
		Set parents=GraphStore.v().param_in_edges.get_parents(o);
		return parents.iterator() ;
	}
	
	private Iterator get_param_out_children(Object o)
	{
		Set children=GraphStore.v().param_out_edges.get_children(o);
		return children.iterator() ;
	}
		
	private Iterator get_data_and_control_parents_iterator(Object src) {
		Set data_parents = GraphStore.v().data_dep_edges.get_parents(src);
		Set control_parents = GraphStore.v().control_dep_edges.get_parents(src);
		List parents = new ArrayList();
		parents.addAll(data_parents);
		parents.addAll(control_parents);
		Iterator it = parents.iterator();
		return it;
	}

	private Iterator get_DCS_parents_iterator(Object src) {
		Set data_parents = GraphStore.v().data_dep_edges.get_parents(src);
		Set control_parents = GraphStore.v().control_dep_edges.get_parents(src);
		Set summary_parents= GraphStore.v().summary_edges.get_parents(src);
		
		List parents = new ArrayList();
		parents.addAll(data_parents);
		parents.addAll(control_parents);
		parents.addAll(summary_parents);
		
		Iterator it = parents.iterator();
		return it;
	}

	
	
	private Iterator get_summary_and_control_parents_iterator(Object src) {
		Set summary_parents = GraphStore.v().summary_edges.get_parents(src);
		Set control_parents = GraphStore.v().control_dep_edges.get_parents(src);
		List parents = new ArrayList();
		parents.addAll(summary_parents);
		parents.addAll(control_parents);
		Iterator it = parents.iterator();
		return it;
	}

}
