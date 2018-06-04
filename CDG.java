package sootapdg.PDG;
import soot.toolkits.graph.*;
import java.util.Iterator;
import java.util.List;

import soot.G;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.DominanceFrontier;
import soot.toolkits.graph.DominatorNode;
import soot.toolkits.graph.DominatorTree;
import soot.toolkits.graph.DominatorsFinder;

public class CDG {
	
	private void add_dep(Unit parent, List children) {
		Iterator i = children.iterator();
		
		while (i.hasNext()) {
			Unit child = (Unit) i.next();
			
			if ( child != null)
			{
				GraphStore.v().add_edge (parent, child, GraphStore.CONTROL);
			
			}
			
		}

	}

	public CDG(UnitGraph ug) {

		
		
		DominatorsFinder domFinder = new TwoWayDominatorsFinder(ug, false);
		DominatorTree domTree = new DominatorTree(domFinder);
		DominanceFrontier domFrontier = new TwoWayCytronDF(domTree, false);

		/*
		 * for all nodes n in domtree, find domfrontier. u depends on all
		 * elements c in the domfrontier. To find label for dependence of u on
		 * c, consider all successors u' of c and take the label on all c->u'
		 * edge where either u' = u or u post-dominates u'.
		 */

		Iterator domIt = domTree.iterator();
		while (domIt.hasNext()) {
			DominatorNode dode = (DominatorNode) domIt.next();
			Unit unit = (Unit) dode.getGode();
			List domFront = domFrontier.getDominanceFrontierOf(dode);

			if (domFront == null || domFront.size() == 0) {
				/* Empty dom frontier means it depends on "method entry" */
				
				SESEGraph sese=(SESEGraph)ug;
				Unit method_entry=(Unit) sese.getEntry();
				//GraphStore.v().add_edge(ug.getHeads().get(0),unit, GraphStore.CONTROL );
				GraphStore.v().add_edge(method_entry,unit, GraphStore.CONTROL );
				// addControlDep (m, ug.getEntry (), Boolean.TRUE, unit);
				continue;
			}

			Iterator parIt = domFront.iterator();
			while (parIt.hasNext()) {
				DominatorNode par = (DominatorNode) parIt.next();
				Unit parUnit = (Unit) par.getGode();

				List succs = ug.getSuccsOf(parUnit); // parent's succs
				if (succs == null || succs.size() == 0) {
					G.v().out.println("No succs for " + parUnit + "!!!");
					continue;
				}

				Iterator succIt = succs.iterator();
				while (succIt.hasNext()) {
					Unit succ = (Unit) succIt.next();
					if (succ == unit
							|| domTree.isImmediateDominatorOf(dode, domTree
									.getDode(succ))) {
						GraphStore.v().add_edge(parUnit,unit, GraphStore.CONTROL);
						Util.v().log("dataedge", "CTRL\t"+parUnit+"\t"+unit);
						
						// addControlDep (m, parUnit, label, unit);
					}
				}
			}
		}
		
		
		
		
		
	}
	
	
	public void print()
	{
		System.out.print("\nCDG\n");
		GraphStore.v().print(GraphStore.CONTROL);
		
		
	}
}
