/**
 * 
 */

package sootapdg.PDG;

import java.util.*;

import soot.*;
/**
 * @author arvind
 *
 */
public class CallGraphInfo {

	/**
	 * 
	 */
	static int FORWARD=1,BACKWARD=0;
	
	Hashtable method_child_list; // Hashtable<SootMethod,List_Methods_invoked>
	Hashtable method_parent_list; // Hashtable<SootMethod,List_Invoking_methods>
	Hashtable method_color; 	// Hashtable<SootMethod,Color>
	Hashtable method_start_time;// Hashtable<Time,SootMethod>
	Hashtable method_finish_time;
	
	Set edges_list;
	
	Hashtable method_rnode;		// Hashtable< SootMethod, RNode>
	List rnode_list;
	List rnode_list_inorder;
	
	
	
	
	int time,max_finish_time;
	
	public CallGraphInfo() {
		method_child_list=new Hashtable();
		method_parent_list=new Hashtable();
		method_color=new Hashtable();
		method_start_time=new Hashtable();
		method_finish_time=new Hashtable();
		edges_list=new HashSet();
		method_rnode=new Hashtable();
		rnode_list=new ArrayList();
		rnode_list_inorder=new ArrayList();
		
		
		time=0;
	}	
	
	void add(SootMethod parent, SootMethod child)
	{
		
		edges_list.add(new CallEdge(parent,child));
		List child_list= (List)method_child_list.get(parent);
		if(child_list==null)
		{
			child_list=new ArrayList();
			method_child_list.put(parent,child_list);
		}
		
		child_list.add(child);
		
		
		List parent_list= (List)method_parent_list.get(child);
		if(parent_list==null)
		{
			parent_list=new ArrayList();
			method_parent_list.put(child,parent_list);
		}
		
		parent_list.add(parent);
		
		System.out.print("\nadd_new_call" + parent.getName()  + "->" + child.getName());
		
		
	}
	
	List get_child_list(SootMethod parent)
	{
		List child_list=(List)method_child_list.get(parent);
		if( child_list==null ) child_list=new ArrayList();
		return  child_list;
		
	}
	
	List get_parent_list(SootMethod parent)
	{
		List parent_list=(List)method_parent_list.get(parent);
		if( parent_list==null ) parent_list=new ArrayList();
		return  parent_list;
		
	}
	
	/* returns WHITE if method is not found in hashtable */
	Character get_color(SootMethod m)
	{
		
		 return  (Character)method_color.get(m);
	}
	
	void set_color(SootMethod m,Character c)
	{
		method_color.put(m,c);
	}
	
	void set_start_time(SootMethod m,int time)
	{
		method_start_time.put(new Integer(time) , m);
	}
	
	void set_finish_time(SootMethod m,int time)
	{
		method_finish_time.put(new Integer(time) , m);
		
		max_finish_time=time;
	}
	SootMethod get_method_with_finish_time(int time)
	{
		SootMethod method= (SootMethod)method_finish_time.get(new Integer(time) );
		return method;
	}
	void DFS(SootClass cl)
	{
		time=0;
		
		/* Initialize all Nodes to WHITE */
		Iterator it=cl.methodIterator();
		while(it.hasNext())
		{
			SootMethod m=(SootMethod)it.next();
			set_color(m,DFSNodeColor.WHITE );
		}
		
		
			Iterator it2=cl.methodIterator();
			while(it2.hasNext())
			{
				SootMethod m=(SootMethod)it2.next();
				if( get_color(m)==DFSNodeColor.WHITE ) DFSVisit(m);
			}
		
		
		
	}
	void DFSVisit(SootMethod m)
	{
		System.out.print( m.getName());
		
		set_color(m,DFSNodeColor.GREY);
		
		set_start_time(m,++time);
		
		/* iterate over  the methods called by m */
		Iterator it=null;
		 it = get_child_list(m).iterator() ;
		
		
 		while(it.hasNext())
		{
			SootMethod next=(SootMethod) it.next();
			if( get_color(next) == DFSNodeColor.WHITE )
			{
				DFSVisit(next);
			}
			
		}
		set_color(m,DFSNodeColor.BLACK );
		
		
		set_finish_time(m,++time);
	}
	
	
	void RevDFS(SootClass cl)
	{
		
		
		/* Initialize all Nodes to WHITE */
		Iterator it=cl.methodIterator();
		while(it.hasNext())
		{
			SootMethod m=(SootMethod)it.next();
			set_color(m,DFSNodeColor.WHITE );
		}
		
		
		
			for(int i=max_finish_time;i>=0;i--)
			{
				SootMethod m=get_method_with_finish_time(i);
				if(m==null) continue;
				
				if( get_color(m)==DFSNodeColor.WHITE ) 
					{
						System.out.print("\nStarting new comp");
						ReducedCallNode red_node=new ReducedCallNode();
						rnode_list.add(red_node);
						RevDFSVisit(m,red_node);
					}
				
			}
		
		
		
	}
	
	
	void RevDFSVisit(SootMethod m,ReducedCallNode red_node)
	{
		red_node.add_method(m);
		method_rnode.put(m,red_node);
		
		System.out.print( m.getName());
		
		
		set_color(m,DFSNodeColor.GREY);
		/* iterate over  the methods calling  m */
		Iterator it=null;
		
		it= get_parent_list(m).iterator() ;
		
 		while(it.hasNext())
		{
			SootMethod next=(SootMethod) it.next();
			if( get_color(next) == DFSNodeColor.WHITE )
			{
				RevDFSVisit(next,red_node);
			}
			
		}
		set_color(m,DFSNodeColor.BLACK );
		
	}
	List get_SCC(SootClass cl)
	{
		DFS(cl);
		RevDFS(cl);// creates red_nodes;
		add_red_edges();
		TopSort();
		return rnode_list_inorder;
		
		
		
	}
	/* TopSort the reduced Call Graph */
	
	void DFSVisit(ReducedCallNode node)
	{
		
		
		node.set_visited() ;
		Set children=node.get_children() ;
		
  		Iterator it=children.iterator() ;
		
		while ( it.hasNext() )
		{
			
			ReducedCallNode child=(ReducedCallNode)it.next();
			if( child.is_visited()==false) DFSVisit(child);
		}
		/* nodes are printed in reverse topological order */
		node.print();
		rnode_list_inorder.add(node);
		
	}
	
	void TopSort()
	{
		DFSVisit( (ReducedCallNode ) (rnode_list.get(0) ));
		
	}
	
	void ComputeSummary(ReducedCallNode node)
	{
		
	}
	
	void ComputeSummary(SootMethod m)
	{
		
	}
	
	
	void print(SootClass cl)
	{
		System.out.print("\n The Methods are \n");
		Iterator it=cl.methodIterator();
		 while ( it.hasNext() )
		 {
			 Object key=it.next();
			 System.out.print( ((SootMethod)key).getName()  );
			 
		 }
		
	}
	
	void add_red_edges()
	{
		Iterator edges_it=edges_list.iterator();
		while( edges_it.hasNext() )
		{
			CallEdge edge=(CallEdge)edges_it.next() ;
			SootMethod p=edge.parent;
			SootMethod c=edge.child ;
			System.out.print("\n"+p.getName()+"--->"+c.getName());
			ReducedCallNode r_parent=(ReducedCallNode) method_rnode.get(p);
			ReducedCallNode r_child=(ReducedCallNode) method_rnode.get(c);
			
			/* avoid edges to same component */
			if( r_parent != r_child )
			{
				r_parent.add_child(r_child);
				
			}
			
				
			
		}
		
		
		
	}
	
	

}

class CallEdge
{
	SootMethod parent;
	SootMethod child;
	public CallEdge(SootMethod p,SootMethod c)
	{
		parent=p; child=c;
		
	}
	
	
}

class ReducedCallEdge
{
	ReducedCallNode parent;
	ReducedCallNode child;
}
class ReducedCallNode
{
	Set method_list;
	Set child_list;
	
	boolean visited;
	
	
	ReducedCallNode()
	{
		method_list=new HashSet();
		child_list=new HashSet();
	
		visited=false;
		
	}
	void add_method(SootMethod m)
	{
		
		method_list.add(m);
	}
	void add_child(ReducedCallNode child)
	{
		child_list.add(child);
	
	}
	
	
	void set_visited()
	{
		visited=true;
	}
	boolean is_visited()
	{
		return visited;
	}
	
	Set get_children()
	{
		return child_list;
	}
	
	void print()
	{
		Iterator it=method_list.iterator() ;
		System.out.print("[");
		while(it.hasNext())
		{
			SootMethod m=(SootMethod)it.next();
			System.out.print( m.getName() + " " );
		}
		System.out.print("]");
	}
	
}
class DFSNodeColor
{
	public static final Character WHITE = new Character('w');
	public static final Character GREY = new Character('g');
	public static final Character BLACK = new Character('b');
}

