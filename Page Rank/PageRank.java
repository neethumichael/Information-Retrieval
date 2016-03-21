import java.io.*;
import java.util.*;

public class PageRank {

	public ArrayList<String> P = new ArrayList<String>(); 
	public ArrayList<String> test = new ArrayList<String>();
	public ArrayList<String> S = new ArrayList<String>();
	public ArrayList<String> I = new ArrayList<String>(); 
	public Map<String, Set<String>> M = new HashMap<String, Set<String>>();
	public Map<String,Integer> L = new HashMap<String,Integer>(); 
	Map<String,Double> PR= new HashMap<String,Double>();
	public List<String> temp2 = new ArrayList<String>(); 
	public ArrayList<Double> perplexity = new ArrayList<Double>(); 
	public Map<String, Integer> ILINK= new HashMap<String, Integer>();

	double sinkPR;
	double d = 0.85,N;
	double log2 = (Math.log(2));
	double initialValue = 0;
	double perplexvalue;

	public PrintWriter out2 = null;

	public static void main(String args[])
	{
		PageRank ir = new PageRank();
		String fileName =args[0];
		System.out.println(fileName);
		if(fileName.equals("test-data.txt"))
		{	
			ir.readFile(fileName);
			ir.createL();
			ir.createS();
			ir.pageRankCalculation();
		}
		else if(fileName.equals("wt2g_inlinks.txt"))
		{
			ir.readFile(fileName);
			ir.createL();
			ir.createS();
			ir.pageRankConvergence();
			ir.results();
		}
		else
		{
			System.out.println("Invalid file name");
		}
	}
    
	public void results()
	{
		int count = 0;
		double total = 0.0;
		PrintWriter out3 = null;
		try
		{
			out3 = new PrintWriter("Output3.txt");
			out3.println("Top 50 Document Ids by Page Rank");
			out2.println("----------------------------------");

			PR.entrySet().stream()
			.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(50).forEach(out3::println);

			out3.println("\r\n");
			out3.println("Top 50 Document Ids by In-link count");
			out2.println("----------------------------------");

			ILINK.entrySet().stream()
			.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(50).forEach(out3::println);

			out3.println("Proportion of pages with no inlinks "+(I.size()/N));
			out3.println("Proportion of pages with no outlinks "+(S.size()/N));
			System.out.println("Proportion of pages with no inlinks  "+(I.size()/N));
			System.out.println("Proportion of pages with no outlinks"+(S.size()/N));

			Iterator it = PR.entrySet().iterator();		
			while (it.hasNext()) 
			{
				Map.Entry i = (Map.Entry)it.next();
				if((double)i.getValue()<(1/N))
				{
					count++;
				}
				total += (double)i.getValue();
			}
				
			out3.println("Proportion of pages whose PageRank is less than their initial, uniform values "+(count/N));
			System.out.println("Proportion of pages whose PageRank is less than their initial, uniform values "+(count/N));
			System.out.println("list of the perplexity values you obtain in each round until convergence is written to Output2 file");
			System.out.println("top 50 sorted document ids and proportions are written to Output3 file");
			out3.flush();
			out3.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
			out3.println(e);
			out3.flush();
			out3.close();
		}
	}

	// This function pageRankCalculation() calculates the page rank for the nodes
	// A,B,C,D,E,F for 1,10,100 iterations.
	
	public void pageRankCalculation()
	{
		Map<String,Double> newPR= new HashMap<String,Double>();
		N = (double)P.size();
		initialValue  = (1.0/N);

		for(int i=0;i<N;i++)
		{
			PR.put(P.get(i),initialValue);
		}
		PrintWriter out = null;
		try
		{
			out = new PrintWriter("Output1.txt");

			for(int i=1;i<=100;i++)
			{
				sinkPR = 0;
				double val;

				for (String temp : S) 
					sinkPR += PR.get(temp);

				for (String temp : P)
				{
					val = ((1-d)/N);
					val += ((d*sinkPR)/N);
					for(String m_temp : M.get(temp))
					{
						val +=(d*PR.get(m_temp)/L.get(m_temp));
					}
					newPR.put(temp,val);
				}
				for(String temp :I)
				{
					newPR.put(temp, initialValue);
				}
				PR = newPR;
				if((i==1)||(i==10)||(i==100))
				{
					Iterator it = PR.entrySet().iterator();
					out.println("Iteration "+i+" Results");
					while (it.hasNext()) 
					{
						Map.Entry PR_VAL = (Map.Entry)it.next();
						out.println("Page rank of "+PR_VAL.getKey()+" is "+PR_VAL.getValue());						
					}

					out.println("---------------------------------------------");
					out.println("\r\n");
				}

			}
		}
		catch(Exception e)
		{
			System.out.println(e);
			out.flush();
			out.close();
		}
		System.out.println("Output written to Output1.txt");
		out.flush();
		out.close();
	}
   // calculate_perplexity calculate perplexity value.
	public double calculate_perplexity()
	{
		double entropy =0.0;
		Double  d_val=0.0;
		Iterator<Map.Entry<String,Double>> it = PR.entrySet().iterator();
		while (it.hasNext()) 
		{
			Map.Entry<String,Double> pair = (Map.Entry<String,Double>)it.next();
			d_val = (Double) pair.getValue();
			entropy += d_val*((Math.log(1/d_val))/log2);
		}
		return(Math.pow(2.0,entropy));
	}

	// calculate if the convergence has reached
	public  Boolean calculate_convergence(int jk)
	{
		perplexvalue = calculate_perplexity();    
		perplexity.add(perplexvalue);   
		if (perplexity.size()>4)
		{
			if((Math.abs(perplexity.get(jk-1)-perplexity.get(jk))<1)&&
					(Math.abs(perplexity.get(jk-2)-perplexity.get(jk-1))<1)&&
					(Math.abs(perplexity.get(jk-3)-perplexity.get(jk-2))<1)&&
					(Math.abs(perplexity.get(jk-4)-perplexity.get(jk-3))<1))
			{  
				print_perplexity((jk+1));
				out2.flush();
				out2.close();
				//	System.out.println("Perplexity: "+(jk+1)+" is "+calculate_perplexity());
				return false;

			}
			else
			{ 
				return true; 
			}
		}
		else
			return true;
	}

	//pageRankConvergence() calculates the Page Rank until convergence 
	// for inlink file for w2g collection.
	public void pageRankConvergence()
	{

		Map<String,Double> newPR= new HashMap<String,Double>();
		N = (double)P.size();
		initialValue  = (1.0/N);
		int j = 0;
		double val;
     // initializing the page rank value to 1/N
		for(int i=0;i<N;i++)
		{
			PR.put(P.get(i),initialValue);		
		}
		try
		{
			out2 = new PrintWriter("Output2.txt");
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		// Iterating until convergence is reached.
		while(calculate_convergence(j))
		{
			print_perplexity((j+1));
			//System.out.println("Perplexity: "+(j+1)+" "+calculate_perplexity());             
			sinkPR = 0;

			for (String temp : S) 
				sinkPR += PR.get(temp);


			for (String temp : P)
			{
				val = ((1.0-d)/N);
				val += d*sinkPR/N;
				for(String m_temp : M.get(temp))
				{
					val +=d*((double)PR.get(m_temp)/(double)L.get(m_temp));
				}
				newPR.put(temp,val);
			}

			Iterator<Map.Entry<String,Double>> it = newPR.entrySet().iterator();
			while (it.hasNext()) 
			{
				Map.Entry<String,Double> pair = (Map.Entry<String,Double>)it.next();
				PR.remove(pair.getKey());
				PR.put(pair.getKey(),pair.getValue());
			}
			j++;
		}
	}

	// print_perplexity writes the perplexity values to the Output2 file
	public void print_perplexity(int iteration)
	{
		try
		{

			out2.println("Iteration: "+iteration+"  Perplexity: "+calculate_perplexity());
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

   // create sink node
	public void createS()
	{
		Iterator it = L.entrySet().iterator();
		while (it.hasNext()) 
		{
			Map.Entry s_val = (Map.Entry)it.next();
			int j = (Integer) s_val.getValue();
			if(j==0)
			{
				S.add((String) s_val.getKey());
			}
		}
	}

	// creates in-link count for each document/node id
	public void createL()
	{
		for(String temp :P)
		{
			L.put(temp,0);
		}
		for(Set temp : M.values())
		{
			ArrayList<String> values = new ArrayList<String>();
			values.addAll(temp);
			for (String value : values)
			{
				int nl = L.get(value);
				nl++;
				L.put(value,nl);
			}
		}
	}

	// reads the input file
	public void readFile(String fileName)
	{
		{
			String[] a=new String[20];
			try
			{
				FileReader fr = new FileReader(fileName);
				//FileReader fr = new FileReader("test-data.txt");
				BufferedReader br = new BufferedReader(fr);
				String line;
				while ((line = br.readLine()) != null) 
				{
					a= line.split(" ");
					int l= a.length;
					P.add(a[0]);
					Set<String> st = new HashSet<String>();
					for(int j=1;j<a.length;j++)
					{
						st.add(a[j]);	 
					}

					if(l<2)
					{
						I.add(a[0]);
					}

					ILINK.put(a[0],st.size());
					M.put(a[0],st);
				}
				br.close();
				fr.close();
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}
}




