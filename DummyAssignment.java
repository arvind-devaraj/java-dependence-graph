package sootapdg;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Jimple;
import soot.jimple.internal.JAssignStmt;

/**
 * @author Srihari Sukumaran
 */
public class DummyAssignment extends JAssignStmt
{
	// private Unit nextStmt;

	public DummyAssignment(Value variable, Value rvalue)
	{
		super(variable, rvalue);
		// nextStmt = null;
	}

	public DummyAssignment(ValueBox variableBox, ValueBox rvalueBox)
	{
		super(variableBox, rvalueBox);
		// nextStmt = null;
	}
	
	public Object clone()
	{
		return new DummyAssignment(Jimple.cloneIfNecessary(this.getLeftOp()), Jimple.cloneIfNecessary(this.getRightOp()));
	}

	// public Unit getNextStatement() { return nextStmt; }
	// public void setNextStatement(Unit ns) { nextStmt = ns; }
}
