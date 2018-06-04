package sootapdg.PDG;

import soot.jimple.*;

import soot.ValueBox;
import soot.toolkits.graph.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.JimpleBody;
import soot.jimple.internal.*;
import soot.jimple.internal.JNopStmt;
import soot.jimple.internal.JReturnStmt;

import sootapdg.DummyActualInAssignStmt;
import sootapdg.DummyActualOutAssignStmt;
import sootapdg.DummyAssignStmt;
import sootapdg.DummyFormalInAssignStmt;
import sootapdg.DummyFormalOutAssignStmt;

import java.util.Hashtable;

import soot.*;
import soot.util.*;

class MethodPreprocessor {

	List formal_in_assign_list = new ArrayList();

	List formal_out_assign_list = new ArrayList();

	Unit nop_stmt_entry, nop_stmt_exit;

	public MethodPreprocessor(SootMethod m) {

		Body b = m.getActiveBody();
		if (!(b instanceof JimpleBody))
			throw new RuntimeException("ERROR : Expecting a Jimple Body.");

		if (m.getName().equals("<init>"))
			return;
		JimpleBody body = (JimpleBody) b;

		// adds the unique start and end nodes

		// graph info is needed for inserting formal-out nodes
		// body is changed

		// /////////////////////////////

		// /////////////////////////////////

		GraphStore.v().method_formin.put(m, formal_in_assign_list);
		GraphStore.v().method_formout.put(m, formal_out_assign_list);
		/* Create a List containing dummy assignment stmts */

		for (int i = 0; i < m.getParameterCount(); i++) {
			Local loc = body.getParameterLocal(i);

			Unit dummy_form_in = new DummyFormalInAssignStmt(loc, loc);
			// Unit dummy_form_out=new DummyFormalOutAssignStmt(loc, loc);

			formal_in_assign_list.add(dummy_form_in);
			// formal_out_assign_list.add(dummy_form_out);

		}
		// insert formal-in list before the first non-identity statement */

		Unit non_id = body.getFirstNonIdentityStmt();

		// insert actual-in and actual out before call-stmts

		PatchingChain units = body.getUnits();

		Iterator unitsIt = units.snapshotIterator();

		units.insertBefore(formal_in_assign_list, non_id);

		while (unitsIt.hasNext()) {
			Stmt unit = (Stmt) unitsIt.next();
			if (unit.containsInvokeExpr())
				processCall(units, unit, m);

			if (unit instanceof JReturnStmt) {
				Value ret_val = ((JReturnStmt) unit).getOp();
				Unit dummy_form_out = new DummyFormalOutAssignStmt(ret_val,
						ret_val);
				units.insertBefore(dummy_form_out, unit);
				formal_out_assign_list.add(dummy_form_out);

			}

		}

		// set activebody to the modified body (modified by adding formals &
		// actuals ins and outs)
		// m.retrieveActiveBody();

		m.setActiveBody(body);

		print_body(m.getActiveBody());

	}

	private void processCall(PatchingChain units, Unit call_unit, SootMethod m) {

		Stmt s = (Stmt) call_unit;
		Chain loc_chain=m.getActiveBody().getLocals();
		
		InvokeExpr exp=s.getInvokeExpr() ;
		Iterator arglist_iterator = s.getInvokeExpr().getArgs().iterator();
		
		List actual_in_assign_list = new ArrayList();
		List actual_out_assign_list = new ArrayList();

		/* iterate over the arguments and create a list of dummy assign stmts */
		/* a=sum(a,b) */

		if (s instanceof AssignStmt) {

			Value res = ((AssignStmt) s).getLeftOp();
			if (res instanceof Local) {
				Local res_ = new JimpleLocal( ((Local)res).getName()+"_",((Local)res).getType());
				
				//here
				((AssignStmt)s).setLeftOp(res_);
				
				loc_chain.add(res_);
				
				Unit act_out = new DummyActualOutAssignStmt(res, res_);
				actual_out_assign_list.add(act_out);

				GraphStore.v().unit_method.put(act_out, m);
			}
		}
		int index=-1;
		while (arglist_iterator.hasNext()) {
			Value arg = (Value) arglist_iterator.next();
			Value d = arg;
			// Local d=get_base_local(arg);
			
			index++;
			
			if (d instanceof Local ) {
				Local d_ =new JimpleLocal( ((Local)d).getName() + "_", ((Local)d).getType());
				
				
				loc_chain.add(d_);
				//here
				exp.setArg(index,d_);
			
				Unit act_in = new DummyActualInAssignStmt( d_, d);

				actual_in_assign_list.add(act_in);

				GraphStore.v().unit_method.put(act_in, m);

				GraphStore.v().add_edge(call_unit, act_in, GraphStore.CONTROL);

			} else {
				actual_in_assign_list.add(new ArgNopStmt());

			}

		}

		GraphStore.v().call_actin.put(call_unit, actual_in_assign_list);
		GraphStore.v().call_actout.put(call_unit, actual_out_assign_list);

		units.insertBefore(actual_in_assign_list, call_unit);
		units.insertAfter(actual_out_assign_list, call_unit);
		/* REPLACE THE ORIGINAL_INVOKE_EXPR WITH THE MODIFIED ONE */

	}

	public List get_formal_in_assign_list() {
		return formal_in_assign_list;
	}

	public List get_formal_out_assign_list() {
		return formal_out_assign_list;
	}

	void print_body(Body b) {
		System.out.print("\nPrinting Body of " + b.getMethod().getSignature()
				+ "\n");
		PatchingChain units = b.getUnits();
		Iterator it = units.iterator();
		int line_no = 1;
		while (it.hasNext()) {
			Unit u = (Unit) it.next();

			System.out.println(line_no + " :" + "  " + u);

			line_no++;
		}

	}
}

class ExNopStmt extends JNopStmt {
	public String toString() {
		return "nop.e";
	}

}

class ArgNopStmt extends JNopStmt {
	public String toString() {
		return "nop.a";
	}

}

class ConNopStmt extends JNopStmt {
	public String toString() {
		return "nop.c";
	}

}
