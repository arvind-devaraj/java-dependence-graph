package sootapdg.PDG;

import soot.util.*;
import soot.Unit; 
import java.util.*;

public class GraphStore {

	private static GraphStore instance = null;
	

	private GraphStore() {
	}

	protected static GraphStore v() {
		if (instance == null)
			instance = new GraphStore();
		return instance;
	}

	Hashtable call_actin=new Hashtable();  // map call_unit-> act_in units
	Hashtable call_actout=new Hashtable();
	
	//Hashtable method_graph=new Hashtable();
	Hashtable method_formin=new Hashtable();
	Hashtable method_formout=new Hashtable();
	Hashtable method_entryunit=new Hashtable();
	
	
	Hashtable act_in_to_act_out=new Hashtable(); // map act_in->act_out units
	
	Hashtable unit_method=new Hashtable(); // map <unit,method>  
	
	
	
	Set units_in_slice=new HashSet();
	
	
	Graph control_dep_edges = new Graph();

	Graph data_dep_edges = new Graph();

	Graph param_in_edges = new Graph();

	Graph param_out_edges = new Graph();

	Graph call_edges = new Graph();
	
	//////////////////
	
	
	Graph summary_edges=new Graph();
	

 static int CONTROL = 1, DATA = 2, PARAM_IN = 3, PARAM_OUT = 4,
			CALL = 5 , SUMMARY=6 ;

	void add_edge(Object src, Object tgt, int type) {
		if (type == CONTROL)
			control_dep_edges.add_edge(src, tgt);
		if (type == DATA)
			data_dep_edges.add_edge(src, tgt);
		if (type == PARAM_IN)
			param_in_edges.add_edge(src, tgt);
		if (type == PARAM_OUT)
			param_out_edges.add_edge(src, tgt);
		if (type == CALL)
			call_edges.add_edge(src, tgt);
		if( type== SUMMARY )
			summary_edges.add_edge(src,tgt);
		Util.v().log("alledges","typ"+type+"\t"+src+"\t"+tgt);
	}
	
	public Set formalouts()
	{
		return param_out_edges.get_all_parents();
	}
	

	void slice(Unit point)
	{
		
		Util.v().log("slicing","\nSlicing w.r.t : "+point+"\n" );
		System.out.println("\nSlicing w.r.t : "+point );
		
		slice1(point);
		Set units_first_phase=new HashSet( units_in_slice );
		Iterator it=units_first_phase.iterator() ;
		while( it.hasNext() )
		{
			Unit u=(Unit)it.next() ;
			slice2(u);
		}
		System.out.println(".end slice");
		
	}
	void slice1(Unit point)
	{
		units_in_slice.add(point);
		
		Set parents=new HashSet();
	
		parents.addAll( call_edges.get_parents(point) );
		parents.addAll( control_dep_edges.get_parents(point));
		parents.addAll(data_dep_edges.get_parents(point));
		parents.addAll( param_in_edges.get_parents(point) );
		//parents.addAll( param_out_edges.get_parents(point));
		
		Util.v().log("slicing",point + "\t: depends on \t"+parents+"\n");
		System.out.println(point + "\t: depends on \t"+parents+"\n");
		Iterator it=parents.iterator() ;
		while( it.hasNext() )
		{
				Unit u=(Unit) it.next();
				if( ! units_in_slice.contains(u) )
				{
					//System.out.println(u);
				    slice1(u);
					
					
				}
		}
		
		
		//System.out.print(parents );
		
	}
	void slice2(Unit point)
	{
		units_in_slice.add(point);
		
		Set parents=new HashSet();
	
		//parents.addAll( call_edges.get_parents(point) );
		parents.addAll( control_dep_edges.get_parents(point));
		parents.addAll(data_dep_edges.get_parents(point));
		//parents.addAll( param_in_edges.get_parents(point) );
		parents.addAll( param_out_edges.get_parents(point));
		
		System.out.println(point+" > is dependent on \t \t"+parents+"\n");
		
		Iterator it=parents.iterator() ;
		while( it.hasNext() )
		{
				Unit u=(Unit) it.next();
				if( ! units_in_slice.contains(u) )
				{
					
				    slice2(u);
					
					
				}
		}
		
		
		//System.out.print(parents );
		
	}
	
	void print(int type) {
		if (type == CONTROL)
			control_dep_edges.print();
		if (type == DATA)
			data_dep_edges.print();
		if (type == PARAM_IN)
			param_in_edges.print();
		if (type == PARAM_OUT)
			param_out_edges.print();
		if (type == CALL)
			call_edges.print();

	}

}

class Graph {
	MultiMap src_tgt, tgt_src;
	

	public Graph() {
		src_tgt = new HashMultiMap();
		tgt_src = new HashMultiMap();

	}

	public void add_edge(Object src, Object tgt) {
		src_tgt.put(src, tgt);
		tgt_src.put(tgt, src);
	}

	public Set get_parents(Object node) {
		return tgt_src.get(node);
	}

	public Set get_children(Object node) {
		return src_tgt.get(node);
	}

	public Set get_all_parents()
	{
		return src_tgt.keySet() ;
	}
	
	public Set get_all_children()
	{
		return tgt_src.keySet() ;
	}
	
	public void print() {
		Iterator it = src_tgt.keySet().iterator();
		String output = new String();
		while (it.hasNext()) {
			Object src = it.next();
			System.out.println("{" + src + "->" + get_children(src) + "}");

		}

	}
}



class GraphTest {
	public static void main(String args[]) {
		Graph s = new Graph();
		s.add_edge("A", "B");
		s.add_edge("A", "C");
		s.add_edge("B", "C");
		s.print();
		/*
		 * System.out.print( s.get_parents("C")); System.out.print(
		 * s.get_children("A"));
		 */
	}
}
