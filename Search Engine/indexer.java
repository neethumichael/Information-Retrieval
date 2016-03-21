import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class indexer {
	public ArrayList<String> docId= new ArrayList<String>();
	String last_id;
	public HashMap<String, HashMap<String, Integer>> index = new HashMap<String, HashMap<String,Integer>>();
	public HashMap<String,Integer> temp = new HashMap<String,Integer>();
	public HashMap<String,Integer> temp2 = new HashMap<String,Integer>();
	public HashMap<String,Integer> doc_words = new HashMap<String,Integer>();
	int document_counter =0;
	int document_length =0;
	public Map<String,Integer> dl = new HashMap<String,Integer>();
	public static void main(String args[])
	{
		indexer i = new indexer();
		String input_fileName =args[0];
		String output_fileName =args[1];
		if(input_fileName.equals("tccorpus.txt")&&
				(output_fileName.equals("index.out")))
		{
			i.readFile(input_fileName);
			i.displayOutput(output_fileName);
		}
		else
		{
			System.out.println("Invalid format");
			System.out.println("The format should be indexer tccorpus.txt index.out");
		}
	}
	public void readFile(String fileName)
	{
		{
			String[] a=new String[100];
			try
			{
				FileReader fr = new FileReader(fileName);
				BufferedReader br = new BufferedReader(fr);
				String line;
				int term_freq =0;
				document_counter = 0;
				while ((line = br.readLine()) != null) 
				{
					a= line.split(" ");					
					if(a[0].charAt(0) == '#')
					{	
						document_counter++;
						if(document_counter!=1)
						{
							doc_words.put(last_id, term_freq);
							dl.put(last_id,document_length);
						}						
						document_length =0;
						term_freq=0;
						for(int j=1;j<a.length;j++)
						{
							docId.add(a[j]);							
						}
					}
					else
					{
						last_id = docId.get(docId.size()-1);
						for(int i=0;i<a.length;i++)
						{
							String r[] = a[i].split(" ");
							document_length += r.length;
							if(index.containsKey(a[i]))
							{
								temp2 =index.get(a[i]);
								term_freq++;
								if(temp2.containsKey(last_id))
								{
									int b = temp2.get(last_id);
									index.remove(a[i]);	
									temp2.put(last_id,++b);
									index.put(a[i],temp2);
								}
								else
								{
									temp2.put(last_id,1);
									index.remove(a[i]);
									index.put(a[i],temp2);									
								}
							}
							else
							{			
								if(isNumeric(a[i]) == false)
								{
									term_freq++;
									HashMap<String,Integer> innermap_1 = new HashMap<String,Integer>();
									innermap_1.put(last_id,1);
									index.put(a[i],innermap_1);
								}
							}
						}
					}
				}
				dl.put(last_id,document_length);
				doc_words.put(last_id, term_freq);
				br.close();
				fr.close();
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}

	public static boolean isNumeric(String str)
	{
		for (char c : str.toCharArray())
		{
			if (!Character.isDigit(c)) return false;
		}
		return true;
	}


	public void displayOutput(String fileName)
	{
		try{
			PrintWriter out = new PrintWriter(fileName);
			int count=0;
			Iterator it = index.entrySet().iterator();		
			while (it.hasNext()) 
			{
				Map.Entry i = (Map.Entry)it.next();
				temp =(HashMap<String, Integer>) i.getValue();
				out.write((String)i.getKey());

				Iterator it2 =temp.entrySet().iterator();
				while(it2.hasNext())
				{
					Map.Entry ii = (Map.Entry)it2.next();
					out.print(" ");
					out.print("(");
					out.print(ii.getKey());
					out.print(",");
					out.print(ii.getValue());
					out.print(")");
					count += (Integer)ii.getValue();
				}
				out.println();
			}
			System.out.println("Inverted Index successfully written to file");
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}

	}

}
