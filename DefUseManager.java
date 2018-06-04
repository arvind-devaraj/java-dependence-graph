package sootapdg.PDG;

import java.util.ArrayList;
import java.util.List;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.toolkits.scalar.SimpleLocalDefs;

public class DefUseManager {
	SimpleLocalDefs locdefs;
	
	public DefUseManager(SimpleLocalDefs locdefs)
	{
		this.locdefs=locdefs;
	}
	
	/* Every expression ( Local/IntConst/InstanceFieldRef/StaticFieldRef/AddExpr 
	 * implements the interface Value (having functions getUseBoxes(), getType())
	 * This function returns the definition list of a variable ( use ) at a particular
	 *  statement( unit)
	 */
	public List get_def_list(Value use,Unit unit)
	{
		
		List def_list=new ArrayList();
	
		if (use instanceof Local)
		{
			//Unit defUnit = (Unit) locdefs.getDefsOf((Local)use).get(0);
			 def_list= locdefs.getDefsOfAt( (Local)use, unit);
			
				return def_list;
		}
		else if (use instanceof InstanceFieldRef)
		{
			InstanceFieldRef ifr = (InstanceFieldRef)use;
			Value temp = ifr.getBase();
			if (! (temp instanceof Local))
				throw new RuntimeException("Base of " + ifr + " is not local.");
			
			 def_list= locdefs.getDefsOfAt((Local)temp,unit);
			return def_list;
		
		}
	
		return def_list;
		
		
	}	
}
