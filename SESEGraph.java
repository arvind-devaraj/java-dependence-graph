package sootapdg.PDG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import soot.*;
import soot.jimple.internal.*;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.jimple.internal.JNopStmt;
import soot.jimple.IntConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.TableSwitchStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.Pair;
import sootapdg.DummyFormalInAssignStmt;
import sootapdg.DummyFormalOutAssignStmt;
import sootapdg.PDG.MethodPreprocessor;


/**
 * @author Ashok Sreenivas
 */
/**
 * Represents a labelled CFG where the nodes are Unit instances, and edges may
 * have a 'label' indicating the condition under which they are taken.
 * 
 * @see Unit
 * @see UnitGraph
 * @see BriefUnitGraph
 * @see ExceptionalUnitGraph
 */


public class SESEGraph extends UnitGraph {

	/*
	 * Maps a <unit, unit> pair to a Boolean. The <unit,unit> represents an edge
	 * <src, dest> and the Boolean represents the label on that edge. If an edge
	 * doesn't belong to this map, then it is an 'unconditional' edge.
	 */
	protected Map edgeToLabel;

	protected Unit methEntry;

	protected Unit methExit;

	Body method_body;
	/**
	 * Constructs a LabelledBriefUnitGraph given a Body instance. Makes an SESE
	 * graph, i.e. ensures each method has exactly one exit.
	 * 
	 * @param body
	 *            The underlying body we want to make a graph for.
	 */

	public SESEGraph(Body body) {
		
		
		super(body);
		method_body=body;
		int size = unitChain.size();

		unitToSuccs = new HashMap(size * 2 + 1, 0.7f);
		unitToPreds = new HashMap(size * 2 + 1, 0.7f);

		buildUnexceptionalEdges(unitToSuccs, unitToPreds);
		buildHeadsAndTails(); // Compute heads and tails

		/*
		 * Create special entry and exit nodes for the method and make the graph
		 * a single exit one
		 */

		methEntry = new ExNopStmt();
		methExit = new ExNopStmt();
		unitChain.addFirst(methEntry);
		unitChain.addLast(methExit);

		// entry has no predecessors, exit has no successors

		unitToSuccs.put(methEntry, new ArrayList()); //
		unitToPreds.put(methEntry, new ArrayList());
		unitToSuccs.put(methExit, new ArrayList());
		unitToPreds.put(methExit, new ArrayList()); // 

		Iterator headIt = getHeads().iterator();
		while (headIt.hasNext()) {
			addEdge(unitToSuccs, unitToPreds, methEntry, (Unit) headIt.next());
		}

		Iterator tailIt = getTails().iterator();
		while (tailIt.hasNext()) {
			addEdge(unitToSuccs, unitToPreds, (Unit) tailIt.next(), methExit);
		}

		buildHeadsAndTails(); // Re-compute heads and tails

		
		
		//buildUnexceptionalEdges(unitToSuccs,unitToPreds);

		// makeMappedListsUnmodifiable(unitToSuccs);
		// makeMappedListsUnmodifiable(unitToPreds);
		soot.util.PhaseDumper.v().dumpGraph(this, body); // What is this!!?

	}

	/*
	 * public void add_special_edges() { iterate over stmts; if(goto stmt) add
	 * edge to its successor make list unmodifiable }
	 */
	public Unit getEntry() {
		return methEntry;
	}

	public Unit getExit() {
		return methExit;
	}
	
	public void add_dummy_edges()
	{
		
		Iterator i=body.getUnits().iterator();
		while(i.hasNext())
		{
			Unit curr_stmt=(Unit)i.next();
			if(curr_stmt instanceof JGotoStmt)
			{
				 if( i.hasNext() ) 
				 {
					 Unit next_stmt= (Unit)i.next() ;
					
					 addEdge(unitToSuccs,unitToPreds,curr_stmt,next_stmt);
					 Util.v().log("goto",curr_stmt+" : "+next_stmt);
				 }
			}
		}
	}
}
