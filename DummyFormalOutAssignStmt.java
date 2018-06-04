package sootapdg;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Jimple;
import soot.jimple.internal.JAssignStmt;

/**
 * @author Srihari Sukumaran
 */
public class DummyFormalOutAssignStmt extends JAssignStmt
{
	// private Unit nextStmt;

	public DummyFormalOutAssignStmt(Value variable, Value rvalue)
	{
		super(variable, rvalue);
		// nextStmt = null;
	}

	public DummyFormalOutAssignStmt(ValueBox variableBox, ValueBox rvalueBox)
	{
		super(variableBox, rvalueBox);
		// nextStmt = null;
	}
	
	public Object clone()
	{
		return new DummyFormalOutAssignStmt(Jimple.cloneIfNecessary(this.getLeftOp()), Jimple.cloneIfNecessary(this.getRightOp()));
	}

	public String toString()
	{
		return "FO:"+super.toString() ;
	}
	// public Unit getNextStatement() { return nextStmt; }
	// public void setNextStatement(Unit ns) { nextStmt = ns; }
}
