package sootapdg;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Jimple;
import soot.jimple.internal.JAssignStmt;

/**
 * @author Srihari Sukumaran
 */
public class DummyFormalInAssignStmt extends JAssignStmt
{
	// private Unit nextStmt;

	public DummyFormalInAssignStmt(Value variable, Value rvalue)
	{
		super(variable, rvalue);
		// nextStmt = null;
	}

	public DummyFormalInAssignStmt(ValueBox variableBox, ValueBox rvalueBox)
	{
		super(variableBox, rvalueBox);
		// nextStmt = null;
	}
	
	public Object clone()
	{
		return new DummyFormalInAssignStmt(Jimple.cloneIfNecessary(this.getLeftOp()), Jimple.cloneIfNecessary(this.getRightOp()));
	}

	public String toString()
	{
		return "FI:"+super.toString() ;
	}
	// 
}
