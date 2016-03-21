import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class nbtrain
{
	int N=0;
	public LinkedHashMap<String, String> Vocabulary = new LinkedHashMap<String, String>();
	ArrayList<String> Class = new ArrayList<String>();
	public LinkedHashMap<String,Double> prior= new LinkedHashMap<String,Double>();
	public LinkedHashMap<String,ArrayList<String>> text= new LinkedHashMap<String,ArrayList<String>>();
	public LinkedHashMap<String, LinkedHashMap<String, Integer>> T = new LinkedHashMap<String, LinkedHashMap<String,Integer>>();
	public LinkedHashMap<String, Double> Pw = new LinkedHashMap<String, Double>();
	public LinkedHashMap<String, String> file_class = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, ArrayList<String>> doc = new LinkedHashMap<String, ArrayList<String>>();
	public HashMap<String, HashMap<String, Double>> cond_prob = new HashMap<String, HashMap<String,Double>>();
	HashMap<String,Integer> temp_4= new HashMap<String,Integer>();

	public static void main(String args[])
	{
		nbtrain tf = new nbtrain();

		String training_dir = args[0];
		String modal =args[1];

		tf.createClassList();
		tf.countDocs();
		tf.extractVocabulary(training_dir);
		tf.trainMultinomial();
		tf.printModalFile(modal);
	}

	public void updateClass(String className)
	{
		try
		{
			LinkedHashMap<String,Integer> temp_1= new LinkedHashMap<String,Integer>();
			temp_1 = T.get(className);
			Iterator it = Vocabulary.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry i =(Map.Entry)it.next();

				if(!temp_1.containsKey((String)i.getKey()))
				{
					temp_1.put((String)i.getKey(),0);
				}
			}
			T.remove(className);
			T.put(className, temp_1);

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	public void trainMultinomial()
	{
		try
		{
			for(int j=0;j<Class.size();j++)
			{
				int sum =0;
				int nc = countDocsInClass(Class.get(j));
				prior.put(Class.get(j), (double)nc/(double)N);
				updateClass(Class.get(j));
				unigramFeatureSelection((Class.get(j)));
				compute_condprob(Class.get(j),sum);
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public void compute_condprob(String className,int sum)
	{
		HashMap<String,Integer> T_temp = new HashMap<String,Integer>();
		HashMap<String,Double> prob_temp = new HashMap<String,Double>();
		T_temp = T.get(className);
		sum = text.get(className).size();
		Iterator it = T_temp.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry i =(Map.Entry)it.next();
			double prob1 = (double)(Integer)i.getValue()+1;
			double prob2 = (double)sum + (double)(1*Vocabulary.size());

			//double prob1 = (double)(Integer)i.getValue()+((double)(Integer)temp_4.get(i.getKey())/N)*45;
			//double prob2 = (double)sum +45;
			prob_temp.put((String)i.getKey(),(prob1/prob2));
		}
		cond_prob.put(className,prob_temp);
	}

	public void unigramFeatureSelection(String className)
	{
		LinkedHashMap<String,Integer> T_temp = new LinkedHashMap<String,Integer>();
		LinkedHashMap<String,Integer> T_temp2 = new LinkedHashMap<String,Integer>();
		T_temp = T.get(className);
		Iterator it = T_temp.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry i =(Map.Entry)it.next();
			if(temp_4.containsKey(i.getKey()))
			{
				if(temp_4.get(i.getKey())>=5)
				{
					T_temp2.put((String)i.getKey(),(Integer)i.getValue());
				}
			}
		}
		T.remove(className);
		T.put(className,T_temp2);
	}

	public int countDocsInClass(String className)
	{
		return new File("textcat/train/"+className).list().length;
	}

	public void createClassList()
	{
		try
		{

			File file = new File("textcat/train/");
			String[] names = file.list();

			for(String name : names)
				if (new File("textcat/train/"+ name).isDirectory())
					Class.add(name);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public void printModalFile(String modal)
	{
		try{
			Formatter fmt = new Formatter();
			PrintWriter out = new PrintWriter(modal);
			Formatter output;
			output = new Formatter(modal);
			Iterator it = cond_prob.entrySet().iterator();
			String class_name =null;
			int count =0;
			String Slno = "Sl.no";
			String Class = "Class";
			String Prior = "Prior";
			String Word = "Word";
			String log = "log(Probablity)";
			out.format("%-10s|%-15s|%-15s|%-15s|%-15s ",Slno,Class,
					Prior,Word,log);
			out.print(System.getProperty("line.separator"));
			while(it.hasNext())
			{
				Map.Entry i =(Map.Entry)it.next();
				class_name = (String)i.getKey();
				HashMap<String,Double> temp= new HashMap<String,Double>();
				temp = (HashMap)i.getValue();
				Iterator it2 = temp.entrySet().iterator();

				while(it2.hasNext())
				{
					count++;
					Map.Entry i2 = (Map.Entry)it2.next();
					out.format("%-10s|%-15s|%-15s|%-15s|%-15s ",count,class_name,
							prior.get(class_name),(String)i2.getKey(),(Math.log((double)i2.getValue())));
					out.print(System.getProperty("line.separator"));
				}
			}
			compute_log_weight();
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	public void compute_log_weight()
	{
		try
		{
			Formatter fmt = new Formatter();
			Formatter output;

			HashMap<String,Double> temp_pos= new HashMap<String,Double>();
			HashMap<String,Double> temp_neg= new HashMap<String,Double>();
			LinkedHashMap<String,Double> temp_pos_neg= new LinkedHashMap<String,Double>();
			LinkedHashMap<String,Double> temp_neg_pos= new LinkedHashMap<String,Double>();
			temp_pos = cond_prob.get("pos");
			temp_neg = cond_prob.get("neg");

			Iterator it = temp_pos.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry i = (Map.Entry)it.next();

				if(temp_neg.containsKey(i.getKey()))
				{
					Double val_pos_neg = (double)i.getValue()/(double)temp_neg.get(i.getKey());
					Double val_neg_pos = (double)temp_neg.get(i.getKey())/(double)i.getValue();
					temp_pos_neg.put((String)i.getKey(),Math.log(val_pos_neg));
					temp_neg_pos.put((String)i.getKey(),Math.log(val_neg_pos));
				}
			}
			String term = "Term";
			String weight_ratio = "Weight Ratio";

			ValueComparator comp = new ValueComparator(temp_pos_neg);
			TreeMap<String,Double> sorted_scores = new TreeMap<String,Double>(comp);
			sorted_scores.putAll(temp_pos_neg);

			PrintWriter out = new PrintWriter("positive_negative_weight.txt");
			output = new Formatter("positive_negative_weight.txt");
			it = sorted_scores.entrySet().iterator();
			out.format("%-10s %-20s ",term,weight_ratio);
			out.print(System.getProperty("line.separator"));
			while(it.hasNext())
			{
				Map.Entry i = (Map.Entry)it.next();

				out.format("%-10s %-20s ",i.getKey(),i.getValue());
				out.print(System.getProperty("line.separator"));
			}

			ValueComparator comp2 = new ValueComparator(temp_neg_pos);
			TreeMap<String,Double> sorted_scores2 = new TreeMap<String,Double>(comp2);
			sorted_scores2.putAll(temp_neg_pos);

			PrintWriter out2 = new PrintWriter("negative_positive_weight.txt");
			output = new Formatter("negative_positive_weight.txt");
			it = sorted_scores2.entrySet().iterator();
			out2.format("%-10s %-20s ",term,weight_ratio);
			out2.print(System.getProperty("line.separator"));
			while(it.hasNext())
			{
				Map.Entry i = (Map.Entry)it.next();
				out2.format("%-10s %-20s ",i.getKey(),i.getValue());
				out2.print(System.getProperty("line.separator"));
			}
			out.flush();
			out.close();
			out2.flush();
			out2.close();
			System.out.println("Modal file and log weight output is written successfully");
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	public void extractVocabulary(String training_dir)
	{
		ArrayList<String> results = new ArrayList<String>();
		for(int i=0;i<Class.size();i++)
		{
			ArrayList<String> class_vocab = new ArrayList<String>();
			LinkedHashMap<String,Integer> temp_1= new LinkedHashMap<String,Integer>();
			int cnt =0;
			File[] files = new File("textcat/"+training_dir+"/"+Class.get(i)).listFiles();
			//If this pathname does not denote a directory, then listFiles() returns null.
			for (File file : files)
			{
				if (file.isFile())
				{
					results.add(file.getName());
					extract(file.getName(),Class.get(i),cnt,class_vocab,temp_1);
					cnt++;
				}
			}
			T.put(Class.get(i),temp_1);
			text.put(Class.get(i),class_vocab);
		}
	}

	public void extract(String fileName,String Classname,int cnt,ArrayList<String> class_vocab,LinkedHashMap temp_1)
	{
		try
		{
			ArrayList<String> doc_vocab = new ArrayList<String>();
			String[] a=new String[100];
			FileReader fr = new FileReader("textcat/train/"+Classname+"/"+fileName);
			BufferedReader br = new BufferedReader(fr);
			String line;
			String bigram=null;
			while ((line = br.readLine()) != null)
			{
				//String alphaAndDigits = line.replaceAll("[^a-zA-Z0-9]+"," ");
				String temp =line.replaceAll("\\s+", " ").trim();

				a= temp.split(" ");
				for(int i=0;i<a.length;i++)
				{

					if(!((a[i].length()==1)||(a[i].length()==2)))
						//if(isAlphaNumeric(a[i]))
						if(!a[i].equals("\\s+"))
						{
							a[i].replaceAll(" ", "");
							a[i].replaceAll("\\t","");
							if(!Vocabulary.containsKey(a[i]))
							{
								Vocabulary.put(a[i],Classname);
							}

							if(temp_4.containsKey(a[i]))
							{
								int n = temp_4.get(a[i]);
								temp_4.remove(a[i]);
								temp_4.put(a[i], (n+1));
							}
							else
							{
								temp_4.put(a[i],1);
							}

							class_vocab.add(a[i]);
							doc_vocab.add(a[i]);

							if(temp_1.containsKey(a[i]))
							{
								int n = (Integer)temp_1.get(a[i]);
								temp_1.remove(a[i]);
								temp_1.put(a[i], (n+1));
							}
							else
							{
								temp_1.put(a[i],1);
							}
						}
				}
			}
			doc.put(fileName, doc_vocab);
			fr.close();
			br.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
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

	public static boolean isAlphaNumeric(String str)
	{
		for (char c : str.toCharArray())
		{
			if (Character.isDigit(c)||Character.isAlphabetic(c)) return true;

		}
		return false;
	}


	public void countDocs()
	{
		for(int i=0;i<Class.size();i++)
		{
			N +=new File("textcat/train/"+Class.get(i)).list().length;
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
