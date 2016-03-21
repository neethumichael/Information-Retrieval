import java.util.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
public class bm25 {
	public HashMap<String, HashMap<String, Integer>> index = new HashMap<String, HashMap<String,Integer>>();
	public HashMap<Integer, Map<String, Double>> bm25 = new HashMap<Integer, Map<String,Double>>();
	public HashMap<Integer, HashMap<String, Double>> bm25_new = new HashMap<Integer, HashMap<String,Double>>();	
	public Map<Integer, ArrayList<String>> queryList = new HashMap<Integer, ArrayList<String>>();
	int N =0;
	double avdl=0.0;
	public Map<String,Integer> dl = new HashMap<String,Integer>();
	double value;
	int doucument_length;
	public static void main(String args[])
	{
		try{
			bm25 b = new bm25();
			String input_fileName =args[0];
			String input_fileName2 =args[1];
			int MaximumResult = Integer.parseInt(args[2]);
			String output_fileName = args[4];
			if(input_fileName.equals("index.out")&& input_fileName2.equals("queries.txt")&&
					output_fileName.equals("results.eval"))
			{
				b.readQuery(input_fileName2);
				b.readFile(input_fileName);
				b.computeNdl(input_fileName);
				b.compute_bm25(MaximumResult,output_fileName);
			}
			else
			{
				System.out.println("Invalid format");
				System.out.println("bm25 index.out queries.txt 100 > results.eval");
			}
		}
		catch(Exception e)
		{
			System.out.println("Enter a numeric value before >");
		}
	}

	public void computeNdl(String input_fileName)
	{		
		{
			String[] a=new String[1000];
			try
			{
				FileReader fr = new FileReader(input_fileName);
				BufferedReader br = new BufferedReader(fr);
				String line;
				while ((line = br.readLine()) != null) 
				{
					a= line.split(" ");
					HashMap<String,Integer> innermap = new HashMap<String,Integer>();
					for(int j=1;j<a.length;j++)
					{
						String[] b=new String[20];
						String c;
						int d;
						b = a[j].split(",");
						c = b[0].replace("(","");
						d = Integer.parseInt(b[1].replace(")",""));
						innermap.put(c,d);
					}
					Iterator it4 = innermap.entrySet().iterator();
					while(it4.hasNext())
					{
						Map.Entry i4 =(Map.Entry)it4.next();
						if(dl.containsKey(i4.getKey()))
						{
							Integer ln = dl.get(i4.getKey());
							ln = ln+(Integer)i4.getValue();
							dl.remove(i4.getKey());
							dl.put((String)i4.getKey(),ln);
						}
						else
						{
							N++;
							dl.put((String)i4.getKey(),(Integer)i4.getValue());
						}																		
					}
				}
				int sum =0;
				Iterator it = dl.entrySet().iterator();		
				while (it.hasNext()) 
				{
					Map.Entry i = (Map.Entry)it.next();
					sum += (int)i.getValue();
				}
				avdl = (double)sum/N;
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}				
	}

	public void printBM25(int queryId,HashMap<String,Double> bm25_a,int MaximumResult,PrintWriter out)
	{
		try{

			ValueComparator comp = new ValueComparator(bm25_a);
			TreeMap<String,Double> sorted_scores = new TreeMap<String,Double>(comp);			
			sorted_scores.putAll(bm25_a);
			Integer Rank = 1;
			String QueryLiteral = "Q0";
			String hostname = "";
			try 
			{
				hostname = InetAddress.getLocalHost().getHostName();
			} 
			catch (UnknownHostException e) 
			{
				System.out.println("Not able to Get the Host Name i.e. System Name, Please refer stack trace , might be that the current user does not have premission (Not an Admin)");
				e.printStackTrace();
			}
			for(Map.Entry<String,Double> entry : sorted_scores.entrySet())
			{
				String documentId = entry.getKey();
				Double score = entry.getValue();
				out.println(queryId +"\t|"+ QueryLiteral +"\t|"+ documentId + "\t|" + Rank +"\t|" + score + "\t|" + hostname);
				Rank++;
				if(Rank > MaximumResult)
					break;
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public void compute_bm25(int MaximumResult,String fileName)
	{
		double b = 0.75;
		double k1 = 1.2;
		double k2 = 100.0;
		double K = 0.0;
		double r =0.0;
		double R =0.0;
		double denominator_document;
		double numerator_document;
		double denominator_word;
		double numerator_word;
		double numerator_query;
		double denominator_query,document,word,query;
		double qf=0.0;
		int n=0,f=0;
		try{
			PrintWriter out = new PrintWriter(fileName);
			Iterator it = queryList.entrySet().iterator();
			while(it.hasNext())
			{
				value = 0;
				HashMap<String,Double> bm25_temp = new HashMap<String,Double>();
				Map.Entry i = (Map.Entry)it.next();
				//take query words
				ArrayList<String> v = new ArrayList();
				v = (ArrayList)i.getValue();
				for(String tmp : v)
				{
					n =0;
					qf = calculate_qf((Integer)i.getKey(),tmp);
					if(index.containsKey(tmp))
					{
						Map<String,Integer> temp = new HashMap<String,Integer>();
						temp = index.get(tmp);
						n = temp.size();
						Iterator it4 = temp.entrySet().iterator();
						while(it4.hasNext())
						{
							value =0;
							Map.Entry i4 =(Map.Entry)it4.next();
							f = (Integer)(i4.getValue());
							String docid = (String)i4.getKey();

							int dlx = dl.get(docid);
							K = k1 *((1-b)+ (b* (dlx/avdl)));

							numerator_document =(r+0.5)/(R-r+0.5);
							denominator_document = (double)(n-r+0.5)/(N-n+0.5);

							numerator_query = (double)(k2+1)*qf;
							denominator_query = (double)k2+qf;

							numerator_word = (double)((k1+1)*f);
							denominator_word = (double)K+f;

							document = (Math.log(numerator_document/denominator_document));
							word = numerator_word/denominator_word;
							query = numerator_query/denominator_query;

							value = (document*word*query);

							if(bm25_temp.containsKey(i4.getKey()))
							{
								double val = value+ bm25_temp.get(i4.getKey());
								bm25.remove(i.getKey());
								bm25_temp.remove(i4.getKey());
								bm25_temp.put((String)i4.getKey(),val);
								bm25.put((Integer)i.getKey(), bm25_temp);
							}
							else
							{
								bm25_temp.put((String)i4.getKey(),value);
								bm25.put((Integer)i.getKey(), bm25_temp);
							}
						}
					}
				}
				printBM25((Integer)i.getKey(),bm25_temp,MaximumResult,out);
			}
			System.out.println(" sorted BM25 scores are successfully written to file");
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	// Calculates the count of given word in the given query
	public int calculate_qf(int queryid,String word)
	{
		int count_qf =0;
		ArrayList<String> temp = new ArrayList<String>();
		temp = queryList.get(queryid);
		for (String value : temp)
		{
			if(value.equals(word))
			{
				count_qf++;
			}
		}
		return count_qf;
	}

	// reads the list of query from query.txt and write them to queryList
	public void readQuery(String fileName)
	{
		String[] a=new String[20];
		try
		{
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			ArrayList<String> temp = null;
			String line;
			int query_id =0;
			while ((line = br.readLine()) != null) 
			{		
				temp = new ArrayList<String>(); 
				a= line.split(" ");
				query_id++;
				for(int i=0;i<a.length;i++)
				{
					temp.add(a[i]);
				}
				queryList.put(query_id, temp);
			}
			fr.close();
			br.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	// Retrieve all inverted lists corresponding to terms in a query.
	public void readFile(String fileName)
	{
		{
			String[] a=new String[100];
			try
			{
				FileReader fr = new FileReader(fileName);
				BufferedReader br = new BufferedReader(fr);
				String line;
				while ((line = br.readLine()) != null) 
				{
					a= line.split(" ");
					Iterator it = queryList.entrySet().iterator();		
					while (it.hasNext()) 
					{
						ArrayList<String> temp = new ArrayList<String>(); 
						Map.Entry i = (Map.Entry)it.next();
						temp = (ArrayList)i.getValue();

						if(temp.contains(a[0]))
						{
							HashMap<String,Integer> innermap = new HashMap<String,Integer>();
							for(int j=1;j<a.length;j++)
							{
								String[] b=new String[20];
								String c;
								int d;
								b = a[j].split(",");
								c = b[0].replace("(","");
								d = Integer.parseInt(b[1].replace(")",""));
								innermap.put(c,d);
							}
							index.put(a[0],innermap);							
						}
					}
				}
				fr.close();
				br.close();
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}
}

class ValueComparator implements Comparator<String> 
{
	Map<String, Double> base;
	public ValueComparator(Map<String, Double> base) 
	{
		this.base = base;
	}
	public int compare(String a, String b)
	{
		if (base.get(a) >= base.get(b))
		{
			return -1;
		}
		else
		{
			return 1;
		}
	}
}

