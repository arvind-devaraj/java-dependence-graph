package sootapdg;

import java.util.Date;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.CompilationDeathException;
import soot.G;
import soot.Local;
import soot.Pack;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Timers;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.options.Options;
import soot.shimple.Shimple;
import soot.shimple.ShimpleBody;
import soot.shimple.toolkits.scalar.ShimpleLocalUses;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.Chain;
import soot.util.queue.QueueReader;
import soot.jimple.spark.SparkTransformer;
import soot.options.SparkOptions;
import sootapdg.PDG.CFG_Reconstructor;
import sootapdg.PDG.PDG;
import sootapdg.PDG.Preprocessor;
 
/**
 * @author arvind
 *
 */
/**
 * @author arvind
 *
 */
public class Jimplify
{
	public static void main(String[] args)
	{
		t = new Jimplify();
		t.run(args);
	}
	
	private static Jimplify t;
	
	private Jimplify()
	{
	
	}
	
	/**
	 * @param args
	 * @return
	 */
	public int run(String[] args)
	{
		try
		{
			// Copied from soot.Main.run()
			// soot.Main.v().run(args);

			String[] cmdLineArgs;
			Date start, finish;

			cmdLineArgs = args;

			start = new Date();

			try {
				Timers.v().totalTimer.start();

				processCmdLine(cmdLineArgs);

				G.v().out.println("Soot started on " + start);

				Scene.v().loadNecessaryClasses();

				int defFormat = Options.v().output_format();

				// Do jimple now..
				Options.v().set_output_format(Options.output_format_jimple);
				
				Options.v().setPhaseOption("jb.ls","enabled:false");
				Options.v().setPhaseOption("jb","use-original-names:true");
				// Do all the required analyses
				PackManager.v().runPacks();
				
				

	           
	            ///////////////////////////////////////////////////////////////////// 
								/* The Starting point */
				
			
				Preprocessor.v().preprocess (reachableClasses());
			   
				/////////////////////////////////////////////////////////////////////
				//pdg.dump();
				
				Timers.v().totalTimer.end();

				// Print out time stats.				
				if (Options.v().time())
					Timers.v().printProfilingInformation();

			} catch (CompilationDeathException e) {
				Timers.v().totalTimer.end();
				exitCompilation(e.getStatus(), e.getMessage());
				return e.getStatus();
			}

			finish = new Date();

			G.v().out.println("Soot finished on " + finish);
			long runtime = finish.getTime() - start.getTime();
			G.v().out.println(
					"Soot has run for "
					+ (runtime / 60000)
					+ " min. "
					+ ((runtime % 60000) / 1000)
					+ " sec.");

			exitCompilation(CompilationDeathException.COMPILATION_SUCCEEDED);
			return CompilationDeathException.COMPILATION_SUCCEEDED;


			
		} catch( OutOfMemoryError e ) {
			soot.G.v().out.println( "Soot has run out of the memory allocated to it by the Java VM." );
			soot.G.v().out.println( "To allocate more memory to Soot, use the -Xmx switch to Java." );
			soot.G.v().out.println( "For example (for 400MB): java -Xmx400m soot.Main ..." );
			throw e;
		}
		
	}
	
    private Iterator reachableClasses() {
      
            return Scene.v().getApplicationClasses().iterator();
      
    }

	
	private void processCmdLine(String[] args) {

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

    private void exitCompilation(int status) {
        exitCompilation(status, "");
    }

    private void exitCompilation(int status, String msg) {
        if(status == CompilationDeathException.COMPILATION_ABORTED) {
                G.v().out.println("compilation failed: "+msg);
        }
    }

    private void postCmdLineCheck() {
        if (Options.v().classes().isEmpty()
        && Options.v().process_dir().isEmpty()) {
            throw new CompilationDeathException(
                CompilationDeathException.COMPILATION_ABORTED,
                "No main class specified!");
        }
    }
    
    /**
     * 
     */
    private void printVersion() {
     
    }

}




















