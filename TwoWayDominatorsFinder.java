/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Navindra Umanee <navindra@cs.mcgill.ca>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package sootapdg.PDG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.scalar.ArrayPackedSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.BoundedFlowSet;
import soot.toolkits.scalar.CollectionFlowUniverse;
import soot.toolkits.scalar.FlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.FlowUniverse;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * Wrapper class for a simple dominators (including post
 * dominators) analysis based on a simple flow analysis
 * algorithm.  Works with any DirectedGraph with a single head
 * (or a single 'tail' for post dominance).
 *
 * @author Ashok Sreenivas
 **/

public class TwoWayDominatorsFinder implements DominatorsFinder
{
    protected DirectedGraph graph;
    protected Map nodeToDominators;
	protected boolean dirn;

    /**
	 * Compute dominators for provided singled-headed directed
	 * graph and given direction (true = dom, false = post-dom).
     **/
    public TwoWayDominatorsFinder(DirectedGraph graph, boolean dirn)
	{
		buildTwoWayDominatorsFinder(graph, dirn);
	}

    private void buildTwoWayDominatorsFinder(DirectedGraph graph, boolean dirn)
    {
		this.dirn = dirn;
        this.graph = graph;
		FlowAnalysis analysis = dirn ?
         	(FlowAnalysis) new DomAnalysis(graph) :
			(FlowAnalysis) new PostDomAnalysis (graph);

        // build node to dominators map
        {
            nodeToDominators = new HashMap(graph.size() * 2 + 1, 0.7f);
            
            for(Iterator nodeIt = graph.iterator(); nodeIt.hasNext();) {
                Object node = nodeIt.next();
                FlowSet set = dirn ? 
					(FlowSet) analysis.getFlowAfter(node) :
					(FlowSet) analysis.getFlowBefore(node);
                nodeToDominators.put(node, set);
            }
        }
    }

    public DirectedGraph getGraph()
    {
        return graph;
    }
    
    public List getDominators(Object node)
    {
        // non-backed list since FlowSet is an ArrayPackedFlowSet
        return ((FlowSet) nodeToDominators.get(node)).toList();
    }

    public Object getImmediateDominator(Object node)
    {
        // root node (based on dirn)
		List l = dirn ? getGraph().getHeads() : getGraph().getTails();
        if(l.contains(node))
            return null;

	// could be memoised, I guess

        List dominatorsList = getDominators(node);
        dominatorsList.remove(node);

        Iterator dominatorsIt = dominatorsList.iterator();
        Object immediateDominator = null;

        while((immediateDominator == null) && dominatorsIt.hasNext()){
            Object dominator = dominatorsIt.next();

            if(isDominatedByAll(dominator, dominatorsList))
                immediateDominator = dominator;
        }

        if(immediateDominator == null)
            throw new RuntimeException("Assertion failed.");
        
        return immediateDominator;
    }

    public boolean isDominatedBy(Object node, Object dominator)
    {
        return getDominators(node).contains(dominator);
    }

    public boolean isDominatedByAll(Object node, Collection dominators)
    {
        return getDominators(node).containsAll(dominators);
    }
}

/**
 * Calculate dominators (and post dominators) for basic blocks.
 * <p> Uses the algorithm contained in Dragon book, pg. 670-1.
 * <pre>
 *       D(n0) := { n0 }
 *       for n in N - { n0 } do D(n) := N;
 *       while changes to any D(n) occur do
 *         for n in N - {n0} do
 *             D(n) := {n} U (intersect of D(p) over all predecessors p of n)
 * </pre>
 **/
class CommonDominatorsAnalysis 
{
    FlowSet emptySet;
    Map nodeToGenerateSet;
	boolean dirn;
	DirectedGraph graph;
    
    CommonDominatorsAnalysis(DirectedGraph graph, boolean dirn)
    {
		this.graph = graph;
		this.dirn = dirn;

        // define empty set, with proper universe for complementation
        {
            List nodes = new ArrayList();

            for(Iterator nodesIt = graph.iterator(); nodesIt.hasNext();)
                nodes.add(nodesIt.next());
            
            FlowUniverse nodeUniverse = new CollectionFlowUniverse(nodes);
            emptySet = new ArrayPackedSet(nodeUniverse);
        }

        // pre-compute generate sets
        {
            nodeToGenerateSet = new HashMap(graph.size() * 2 + 1, 0.7f);

            for(Iterator nodeIt = graph.iterator(); nodeIt.hasNext();){
                Object s = nodeIt.next();
                FlowSet genSet = (FlowSet) emptySet.clone();
                genSet.add(s, genSet);
                nodeToGenerateSet.put(s, genSet);
            }
        }
    }

    /**
     * All OUTs are initialized to the full set of definitions
     * OUT(Start) is tweaked in customizeInitialFlowGraph.
     **/
    protected Object newInitialFlow()
    {
        BoundedFlowSet initSet = (BoundedFlowSet) emptySet.clone();
        initSet.complement();
        return initSet;
    }

    /**
     * OUT(Start) contains only Start at initialization time.
     **/
    protected Object entryInitialFlow()
    {
        List entries = dirn ? graph.getHeads() : graph.getTails ();

        if(entries.size() != 1)
            throw new RuntimeException("Assertion failed:  Only one " + (dirn ? "head" : "tail") + " expected.");

        BoundedFlowSet initSet = (BoundedFlowSet) emptySet.clone();
        initSet.add(entries.get(0));
        return initSet;
    }

    /**
     * We compute out straightforwardly.
     **/
    protected void flowThrough(Object inValue, Object block, Object outValue)
    {
        FlowSet in = (FlowSet) inValue, out = (FlowSet) outValue;

        // Perform generation
        in.union((FlowSet) nodeToGenerateSet.get(block), out);
    }

    /**
     * All paths == Intersection.
     **/
    protected void merge(Object in1, Object in2, Object out)
    {
        FlowSet inSet1 = (FlowSet) in1,
            inSet2 = (FlowSet) in2;

        FlowSet outSet = (FlowSet) out;

        inSet1.intersection(inSet2, outSet);
    }

    protected void copy(Object source, Object dest)
    {
        FlowSet sourceSet = (FlowSet) source,
            destSet = (FlowSet) dest;

        sourceSet.copy(destSet);
    }
}

class DomAnalysis extends ForwardFlowAnalysis
{
	CommonDominatorsAnalysis domAnalysis;
    
    DomAnalysis(DirectedGraph graph)
    {
        super(graph);
		domAnalysis = new CommonDominatorsAnalysis (graph, true);
        doAnalysis();
    }

    /**
     * All OUTs are initialized to the full set of definitions
     * OUT(Start) is tweaked in customizeInitialFlowGraph.
     **/
    protected Object newInitialFlow()
    {
        return domAnalysis.newInitialFlow ();
    }

    /**
     * OUT(Start) contains only Start at initialization time.
     **/
    protected Object entryInitialFlow()
    {
		return domAnalysis.entryInitialFlow ();
    }

    /**
     * We compute out straightforwardly.
     **/
    protected void flowThrough(Object inValue, Object block, Object outValue)
    {
		domAnalysis.flowThrough (inValue, block, outValue);
    }

    /**
     * All paths == Intersection.
     **/
    protected void merge(Object in1, Object in2, Object out)
    {
		domAnalysis.merge (in1, in2, out);
    }

    protected void copy(Object source, Object dest)
    {
		domAnalysis.copy (source, dest);
    }
}

class PostDomAnalysis extends BackwardFlowAnalysis
{
	CommonDominatorsAnalysis domAnalysis;
    
    PostDomAnalysis(DirectedGraph graph)
    {
        super(graph);
		domAnalysis = new CommonDominatorsAnalysis (graph, false);
        doAnalysis();
    }

    /**
     * All OUTs are initialized to the full set of definitions
     * OUT(Start) is tweaked in customizeInitialFlowGraph.
     **/
    protected Object newInitialFlow()
    {
        return domAnalysis.newInitialFlow ();
    }

    /**
     * OUT(Start) contains only Start at initialization time.
     **/
    protected Object entryInitialFlow()
    {
		return domAnalysis.entryInitialFlow ();
    }

    /**
     * We compute out straightforwardly.
     **/
    protected void flowThrough(Object inValue, Object block, Object outValue)
    {
		domAnalysis.flowThrough (inValue, block, outValue);
    }

    /**
     * All paths == Intersection.
     **/
    protected void merge(Object in1, Object in2, Object out)
    {
		domAnalysis.merge (in1, in2, out);
    }

    protected void copy(Object source, Object dest)
    {
		domAnalysis.copy (source, dest);
    }
}
