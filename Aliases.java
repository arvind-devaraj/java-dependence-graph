package sootapdg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Body;
import soot.CompilationDeathException;
import soot.G;
import soot.Local;
import soot.Main;
import soot.Pack;
import soot.PackManager;
import soot.PointsToAnalysis;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.toolkits.pointer.*;
import soot.options.Options;
import soot.toolkits.scalar.Pair;
import soot.util.Chain;

/**
 * @author Ashok Sreenivas
 */
/** Wrapper class and API to get different kinds of aliases of a
 * local. Optimised assuming only one 'kind' of aliases (among
 * locals, static fields and instance fields) will be required
 * for an application. Otherwise, can be made better. */

public class Aliases
{
	/** Returns the set of local aliases of a local loc */
	public static Set getLocalAliases (Local loc) 
	{
		computeLocalAliases ();
		return (Set) theAliases.locToLocals.get (loc);
	}

	/** Returns the set of static field aliases of a local loc */
	public static Set getStaticAliases (Local loc) 
	{
		computeStaticAliases ();
		return (Set) theAliases.locToStaticFields.get (loc);
	}

	/** Returns the set of instance field aliases of a local loc */
	public static Set getFieldAliases (Local loc) 
	{
		computeFieldAliases ();
		return (Set) theAliases.locToInstFields.get (loc);
	}

	/** Returns true if two loc2 is aliased to loc1 */
	public static boolean areAliased (Local loc1, Local loc2)
	{
		Set als = getLocalAliases (loc1);
		if (als == null)
			return false;
		return als.contains (loc2);
	}

	/** Returns true if loc2.fld is aliased to loc1. */
	public static boolean areAliased (Local loc1, Local loc2, SootField fld)
	{
		Pair p = new Pair (loc2, fld);
		Set als = getFieldAliases (loc1);
		if (als == null)
			return false;
		return als.contains (p);
	}

	/** Returns true if static field fld is aliased to loc1. */
	public static boolean areAliased (Local loc1, SootField fld)
	{
		Set als = getStaticAliases (loc1);
		if (als == null)
			return false;
		return als.contains (fld);
	}

	public static void main(String[] args)
	{
		try
		{
			String[] cmdLineArgs;
			cmdLineArgs = args;

			try {
				processCmdLine(cmdLineArgs);
				Scene.v().loadNecessaryClasses();
				PackManager.v().runPacks();
				computeLocalAliases (); 
				computeFieldAliases (); 
				computeStaticAliases (); 
				printStuff ();
				PackManager.v().writeOutput();
			} catch (CompilationDeathException e) {
				exitCompilation(e.getStatus(), e.getMessage());
				return;
			}
			exitCompilation(CompilationDeathException.COMPILATION_SUCCEEDED);
			return;
		} catch( OutOfMemoryError e ) {
			soot.G.v().out.println( "Out of memory." );
			throw e;
		}
	}
	
	private HashMap locToLocals;
	private HashMap locToStaticFields;
	private HashMap locToInstFields;

	private static Aliases theAliases = null; 	// Singleton class

	private Aliases() 
	{ 
		locToLocals = locToStaticFields = locToInstFields = null;
	}
	
	private static void computeLocalAliases ()
	{
		if (theAliases == null)		
			theAliases = new Aliases ();
			
		if (theAliases.locToLocals != null)		// Job already done!
			return; 

		theAliases.locToLocals = new HashMap ();
		Scene theScene = Scene.v();
		PointsToAnalysis pa = theScene.getPointsToAnalysis ();
		Chain classChain = theScene.getApplicationClasses();
		Iterator it1 = classChain.iterator();
		while(it1.hasNext())
		{
			SootClass cls = (SootClass) it1.next();
			Iterator methIt = cls.methodIterator();
			while(methIt.hasNext())
			{
				SootMethod method = (SootMethod) methIt.next();
				Body body = method.getActiveBody();
				Chain locals = body.getLocals ();
                Iterator lIt = locals.iterator();
                while(lIt.hasNext())
                {
                	Local loc = (Local) lIt.next();
					soot.PointsToSet ps = pa.reachingObjects (loc);
					HashSet locAliases = new HashSet ();
					theAliases.locToLocals.put (loc, locAliases);
					Chain locals1 = body.getLocals ();
					Iterator lIt1 = locals1.iterator();
					while(lIt1.hasNext())
					{
                		Local loc1 = (Local) lIt1.next();
						if (loc1 == loc)
							continue;
						soot.PointsToSet ps1 = pa.reachingObjects (loc1);
						if (ps.hasNonEmptyIntersection (ps1))
							locAliases.add (loc1); 
					}
                }
			}
		}
	}

	private static void computeStaticAliases ()
	{
		if (theAliases == null)		
			theAliases = new Aliases ();
			
		if (theAliases.locToStaticFields != null)	// Job already done!
			return; 

		theAliases.locToStaticFields = new HashMap ();
		Scene theScene = Scene.v();
		PointsToAnalysis pa = theScene.getPointsToAnalysis ();
		Chain classChain = theScene.getApplicationClasses();
		Iterator it1 = classChain.iterator();
		while(it1.hasNext())
		{
			SootClass cls = (SootClass) it1.next();
			Iterator methIt = cls.methodIterator();
			while(methIt.hasNext())
			{
				SootMethod method = (SootMethod) methIt.next();
				Body body = method.getActiveBody();
				Chain locals = body.getLocals ();
                Iterator lIt = locals.iterator();
                while(lIt.hasNext())
                {
                	Local loc = (Local) lIt.next();
					soot.PointsToSet ps = pa.reachingObjects (loc);
					HashSet locAliases = new HashSet ();
					theAliases.locToStaticFields.put (loc, locAliases);
					Chain locals1 = body.getLocals ();
					Iterator lIt1 = locals1.iterator();
					while(lIt1.hasNext())
					{
                		Local loc1 = (Local) lIt1.next();
						if (loc1 == loc)
							continue;
						Type t = loc1.getType ();
						if (!(t instanceof RefType))
							continue;
						RefType rt = (RefType) t;
						SootClass cls1 = rt.getSootClass ();
						if (cls1 == null)
						{
							G.v().out.println ("Reftype " + rt + " of " + 
								loc1 + " is not a class");
							continue;
						}
						Iterator fIt = cls1.getFields().iterator ();
						while (fIt.hasNext ())
						{
							SootField fld = (SootField) fIt.next();
							if (!fld.isStatic ())
								continue;
							soot.PointsToSet ps2;
							ps2 = pa.reachingObjects (fld);
							if (ps.hasNonEmptyIntersection (ps2))
								locAliases.add (fld);
						}
					}
                }
			}
		}
	}

	private static void computeFieldAliases ()
	{
		if (theAliases == null)		
			theAliases = new Aliases ();
			
		if (theAliases.locToInstFields != null)	// Job already done!
			return; 

		theAliases.locToInstFields = new HashMap ();
		Scene theScene = Scene.v();
		PointsToAnalysis pa = theScene.getPointsToAnalysis ();
		Chain classChain = theScene.getApplicationClasses();
		Iterator it1 = classChain.iterator();
		while(it1.hasNext())
		{
			SootClass cls = (SootClass) it1.next();
			Iterator methIt = cls.methodIterator();
			while(methIt.hasNext())
			{
				SootMethod method = (SootMethod) methIt.next();
				Body body = method.getActiveBody();
				Chain locals = body.getLocals ();
                Iterator lIt = locals.iterator();
                while(lIt.hasNext())
                {
                	Local loc = (Local) lIt.next();
					soot.PointsToSet ps = pa.reachingObjects (loc);
					HashSet locAliases = new HashSet ();
					theAliases.locToInstFields.put (loc, locAliases);
					Chain locals1 = body.getLocals ();
					Iterator lIt1 = locals1.iterator();
					while(lIt1.hasNext())
					{
                		Local loc1 = (Local) lIt1.next();
						if (loc1 == loc)
							continue;
						Type t = loc1.getType ();
						if (!(t instanceof RefType))
							continue;
						RefType rt = (RefType) t;
						SootClass cls1 = rt.getSootClass ();
						if (cls1 == null)
						{
							G.v().out.println ("Reftype " + rt + " of " + 
								loc1 + " is not a class");
							continue;
						}
						Iterator fIt = cls1.getFields().iterator ();
						while (fIt.hasNext ())
						{
							SootField fld = (SootField) fIt.next();
							if (fld.isStatic () ||
									!(fld.getType() instanceof RefType))
								continue;
							soot.PointsToSet ps2;
							ps2 = pa.reachingObjects (loc1, fld);
							if (ps.hasNonEmptyIntersection (ps2))
								locAliases.add (new Pair (loc1, fld));
						}
					}
                }
			}
		}
	}

	private static void printStuff ()
	{
		Scene theScene = Scene.v();

		PointsToAnalysis pa = theScene.getPointsToAnalysis ();
		if (pa instanceof PAG)
			G.v().out.println ("PAG ptr analysis!!");
		else if (pa instanceof DumbPointerAnalysis)	
			G.v().out.println ("Dumb ptr analysis!!");
		else
			G.v().out.println ("Unknown ptr analysis!!");

		Chain classChain = theScene.getApplicationClasses();
		Iterator it1 = classChain.iterator();
		while(it1.hasNext())
		{
			SootClass cls = (SootClass) it1.next();
			G.v().out.println("===========================================");
			G.v().out.println("CLASS: " + cls);
			Iterator methIt = cls.methodIterator();
			while(methIt.hasNext())
			{
				G.v().out.println("------------------------------------------");
				SootMethod method = (SootMethod) methIt.next();
				G.v().out.println("METHOD: " + method.getName ());
				Body body = method.getActiveBody();
				Chain locals = body.getLocals ();
				G.v().out.println ("#locals: " + locals.size());
                Iterator lIt = locals.iterator();
                while(lIt.hasNext())
                {
                	Local loc = (Local) lIt.next();
					G.v().out.println ("Local: " + loc + " aliased to:");
					Set als = getLocalAliases (loc);
					Iterator alIt;
					if (als != null)
					{
						alIt = als.iterator();
						while(alIt.hasNext())
						{
							Local l = (Local) alIt.next ();
							G.v().out.println ("    " + l);
						}
					}
					als = getStaticAliases (loc);
					if (als != null)
					{
						alIt = als.iterator();
						while(alIt.hasNext())
						{
							SootField f = (SootField) alIt.next ();
							G.v().out.println ("    " + 
								f.getDeclaringClass() + "." + f);
						}
					}
					als = getFieldAliases (loc);
					if (als != null)
					{
						alIt = als.iterator();
						while(alIt.hasNext())
						{
							Pair p = (Pair) alIt.next ();
							G.v().out.println ("    " + p.getO1() + "." + 
								p.getO2());
						}
					}
                }
			}
		}
	}

	private static void processCmdLine(String[] args) {

        if (!Options.v().parse(args))
            throw new CompilationDeathException(
                CompilationDeathException.COMPILATION_ABORTED,
                "Option parse error");

        if( PackManager.v().onlyStandardPacks() ) {
            for( Iterator packIt = PackManager.v().allPacks().iterator(); packIt.hasNext(); ) {
                final Pack pack = (Pack) packIt.next();
                Options.v().warnForeignPhase(pack.getPhaseName());
                for( Iterator trIt = pack.iterator(); trIt.hasNext(); ) {
                    final Transform tr = (Transform) trIt.next();
                    Options.v().warnForeignPhase(tr.getPhaseName());
                }
            }
        }
        Options.v().warnNonexistentPhase();

        if (Options.v().help()) {
            G.v().out.println(Options.v().getUsage());
            throw new CompilationDeathException(CompilationDeathException.COMPILATION_SUCCEEDED);
        }

        if (Options.v().phase_list()) {
            G.v().out.println(Options.v().getPhaseList());
            throw new CompilationDeathException(CompilationDeathException.COMPILATION_SUCCEEDED);
        }

        if(!Options.v().phase_help().isEmpty()) {
            for( Iterator phaseIt = Options.v().phase_help().iterator(); phaseIt.hasNext(); ) {
                final String phase = (String) phaseIt.next();
                G.v().out.println(Options.v().getPhaseHelp(phase));
            }
            throw new CompilationDeathException(CompilationDeathException.COMPILATION_SUCCEEDED);
        }

        if (args.length == 0 || Options.v().version()) {
            printVersion();
            throw new CompilationDeathException(CompilationDeathException.COMPILATION_SUCCEEDED);
        }

        postCmdLineCheck();
    }

    private static void exitCompilation(int status) {
        exitCompilation(status, "");
    }

    private static void exitCompilation(int status, String msg) {
        if(status == CompilationDeathException.COMPILATION_ABORTED) {
                G.v().out.println("compilation failed: "+msg);
        }
    }

    private static void postCmdLineCheck() {
        if (Options.v().classes().isEmpty()
        && Options.v().process_dir().isEmpty()) {
            throw new CompilationDeathException(
                CompilationDeathException.COMPILATION_ABORTED,
                "No main class specified!");
        }
    }
    
    private static void printVersion() {
        G.v().out.println("Soot version " + Main.v().versionString);

        G.v().out.println(
            "Copyright (C) 1997-2003 Raja Vallee-Rai and others.");
        G.v().out.println("All rights reserved.");
        G.v().out.println("");
        G.v().out.println(
            "Contributions are copyright (C) 1997-2003 by their respective contributors.");
        G.v().out.println("See the file 'credits' for a list of contributors.");
        G.v().out.println("See individual source files for details.");
        G.v().out.println("");
        G.v().out.println(
            "Soot comes with ABSOLUTELY NO WARRANTY.  Soot is free software,");
        G.v().out.println(
            "and you are welcome to redistribute it under certain conditions.");
        G.v().out.println(
            "See the accompanying file 'COPYING-LESSER.txt' for details.");
        G.v().out.println();
        G.v().out.println("Visit the Soot website:");
        G.v().out.println("  http://www.sable.mcgill.ca/soot/");
        G.v().out.println();
        G.v().out.println("For a list of command line options, enter:");
        G.v().out.println("  java soot.Main --help");
    }
}

