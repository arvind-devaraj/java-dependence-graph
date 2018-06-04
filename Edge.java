package sootapdg.PDG;

import soot.Unit;

class Edge
{
	Unit src,tgt;
	public Edge(Unit src,Unit tgt)
	{
		this.src=src; this.tgt=tgt;
	}
	
	public boolean equals(Object other) 
	{
		Edge o=(Edge)other;
		if( src.equals(o.src) && this.tgt.equals(o.tgt) ) return true;
		return false;
	}
	
	public int hashCode() 
	{
		return src.hashCode()+tgt.hashCode() ;
		
	}
}