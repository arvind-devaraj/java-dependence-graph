package sootapdg.PDG;

import soot.toolkits.graph.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import soot.JimpleBodyPack;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.Body;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.toolkits.scalar.ArrayPackedSet;

import soot.toolkits.scalar.LocalUnitPair;
import soot.toolkits.scalar.SimpleLocalDefs;
import sootapdg.DummyFormalInAssignStmt;
import sootapdg.DummyFormalOutAssignStmt;
import soot.*;
import soot.jimple.*;

public class DDG {

	// the dependencies among units

	// contains the definition units for every use
	DefUseManager du_man;

	ArrayList unit_list;

	public DDG(UnitGraph graph) {

		unit_list = new ArrayList();

		Body body = graph.getBody();

		// DDG computation
		SimpleLocalDefs locdefs = new SimpleLocalDefs(graph);
				du_man = new DefUseManager(locdefs);

		Iterator unitsIt = body.getUnits().iterator();
		process_units(unitsIt);

	}

	private void process_units(Iterator unitsIt) {
		while (unitsIt.hasNext()) {

			Stmt unit = (Stmt) unitsIt.next();

			unit_list.add(unit);

			/* Don't build DU for InvokeStmt */
			if ( unit.containsInvokeExpr() ) {
				// get all possible method targets

				// changed
				process_assign_stmt(unit);
				process_call_stmt(unit);
			}
			if (unit instanceof AssignStmt) {

				process_assign_stmt(unit);
			}

		}

	}

	private void process_assign_stmt(Unit unit) {
		Iterator useboxIt = unit.getUseBoxes().iterator();
		Iterator defboxIt = unit.getDefBoxes().iterator();

		while (useboxIt.hasNext()) {
			Value use_val = ((ValueBox) useboxIt.next()).getValue();

			List def_list = du_man.get_def_list(use_val, unit);

			Iterator it = def_list.iterator();
			
			while (it.hasNext()) {
				
				Unit def_unit = (Unit) it.next();

				add_dep(def_unit, unit, GraphStore.DATA);
				
				
				Util.v().log("dataedge", "DATA\t" + def_unit + "\t" + unit + " ");

			}

		}

	}

	private void process_call_stmt(Unit call_unit) {

		/* Need to incorporate RTA here */
		Stmt stmt=(Stmt)call_unit;
		InvokeExpr expr=(InvokeExpr)stmt.getInvokeExpr();
		SootMethod called_method = expr.getMethod();
		Util.v().log("dataedge","CALL\t" + expr);

		if (!called_method.isConcrete())
			return;
		if (!called_method.hasActiveBody())
			return;

		Unit called_unit = (Unit) GraphStore.v().method_entryunit
				.get(called_method);

		add_dep(call_unit, called_unit, GraphStore.CALL);

		List actin = (List) GraphStore.v().call_actin.get(call_unit);
		List formin = (List) GraphStore.v().method_formin.get(called_method
				);

		List actout = (List) GraphStore.v().call_actout.get(call_unit);
		List formout = (List) GraphStore.v().method_formout.get(called_method
				);

		if (actin == null || formin == null || actout == null
				|| formout == null) {
			System.out.print("<error>\r\n" + actin + formin + actout + formout);
		}

		add_dep(actin, formin, GraphStore.PARAM_IN);
		
		Util.v().log("paramedge", formout+" "+actout);
		add_dep_2(formout, actout, GraphStore.PARAM_OUT);

		Util.v().log("dataedge","LIST\t actin:" + actin + " formin:" + formin);

		Util.v().log("dataedge","LIST\t formout:" + formout + " actout:" + actout);
	}

	private void add_dep(Unit def, Unit use, int type) {
		GraphStore.v().add_edge(def, use, type);
	}

	private void add_dep(List defs, List uses, int type) {
		Iterator it1 = defs.iterator();
		Iterator it2 = uses.iterator();
		while (it1.hasNext()) {
			Unit def = (Unit) it1.next();
			Unit use = (Unit) it2.next();
			if (def != null && use != null)
				add_dep(def, use, type);
		}

	}
	
	//single use ; many defs
	
	private void add_dep_2(List defs, List uses, int type) {
		Iterator it1 = defs.iterator();
		Iterator it2 = uses.iterator();
		Unit use = (Unit) it2.next();
		
		while (it1.hasNext()) {
			Unit def = (Unit) it1.next();
		
			if (def != null && use != null)
				add_dep(def, use, type);
		}

	}
	public void print() {
		System.out.print("\nDDG\n");
		GraphStore.v().print(GraphStore.DATA);

	}

}
