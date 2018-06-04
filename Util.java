package sootapdg.PDG;

import java.io.*;
import java.util.*;

public class Util {

	boolean nodebug = false;

	private static Util o = null;

	private Util() {
	}

	public static Util v() {
		if (o == null)
			o = new Util();
		return o;
	}
//////////////////////////////////////////////////////////////
	
	public String sc_class, sc_method;  // slicing criteria
	public int sc_line;
	
	public void read_sc()
	{
		String filename="temp.sc";
		BufferedReader out;
		try {
			

			
				out = new BufferedReader(new FileReader(filename));
				if(out==null) { System.out.print("sc.txt not found"); }
				sc_class=out.readLine();
				sc_method=out.readLine();
				sc_line= Integer.parseInt( out.readLine() );
				System.out.println("slicing criteria: "+ sc_class + ": " + sc_method+" : "+sc_line);
				out.close();
			}

		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	////////////////////////////////////////////////////////////
	Hashtable filename_handle = new Hashtable();

	int timer = 0;

	public void log1(String filename, String msg) {
		BufferedWriter out;
		try {
			System.out.print("logging");
			out = (BufferedWriter) filename_handle.get(filename);
			if (out == null) {
				out = new BufferedWriter(new FileWriter(filename, true));
				filename_handle.put(filename, out);
			}
			out.write(timer);
			out.write(msg);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	HashMap filenames = new HashMap();

	public void log(String filename, String msg) {
		
		if (nodebug)
			return;
		BufferedWriter out;
		try {
			

			out = (BufferedWriter) filenames.get(filename);
			if (out == null) {
				out = new BufferedWriter(new FileWriter(filename));
				filenames.put(filename, out);
			} else {
				out = new BufferedWriter(new FileWriter(filename, true));

				out.write("\n" + (++timer) + "\t");
				out.write(msg);
				out.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void finalize() {
		Iterator i = filenames.keySet().iterator();
		while (i.hasNext()) {
			BufferedWriter out = (BufferedWriter) filenames.get(i.next());
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
