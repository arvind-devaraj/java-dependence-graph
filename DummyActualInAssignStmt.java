package sootapdg;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Jimple;
import soot.jimple.internal.JAssignStmt;

/**
 * @author Srihari Sukumaran
 */
public class DummyActualInAssignStmt extends JAssignStmt
{
	// private Unit nextStmt;

	public DummyActualInAssignStmt(Value variable, Value rvalue)
	{
		super(variable, rvalue);
		// nextStmt = null;
	}

	public DummyActualInAssignStmt(ValueBox variableBox, ValueBox rvalueBox)
	{
		super(variableBox, rvalueBox);
		// nextStmt = null;
	}
	
	public Object clone()
	{
		return new DummyActualInAssignStmt(Jimple.cloneIfNecessary(this.getLeftOp()), Jimple.cloneIfNecessary(this.getRightOp()));
	}
	
	public String toString()
	{
		return "AI:"+super.toString() ;
	}

	// public Unit getNextStatement() { return nextStmt; }
	// public void setNextStatement(Unit ns) { nextStmt = ns; }
}
