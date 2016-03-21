import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class nbtest
{
	ArrayList<String> Class = new ArrayList<String>();

	public HashMap<String, HashMap<String, Double>> cond_prob = new HashMap<String, HashMap<String,Double>>();
	public Map<String,Double> neg_score = new HashMap<String,Double>();
	public Map<String,Double> pos_score = new HashMap<String,Double>();
	public Map<String,Double> prior = new HashMap<String,Double>();
	public Map<String,String> prediction = new HashMap<String,String>();
	public Map<String,ArrayList<String>> class_fileName = new HashMap<String,ArrayList<String>>();
	public static void main(String args[])
	{
		nbtest test = new nbtest();
		String modal =args[0];
		String test_dir = args[1];
		String prediction_file = args[2];

		if(test_dir.contains("test")||test_dir.contains("dev")||(test_dir.contains("train")))
		{
			test.createClassList(test_dir);
			test.readModalFile(modal);
			test.readFile(test_dir);
			test.print_prediction(prediction_file,test_dir);
		}
		else
		{
			System.out.println("Invalid test directory: Input 'test' for test directory and 'dev' for dev directory");
		}
	}

	public void readFile(String test_dir)
	{
		if(test_dir.equals("test"))
		{
			ArrayList<String> results1 = new ArrayList<String>();
			File[] files = new File(System.getProperty("user.dir")+"/textcat/"+test_dir+"/").listFiles();
			//If this pathname does not denote a directory, then listFiles() returns null.

			for (File file : files)
			{
				if (file.isFile())
				{
					extract(file.getName(),"xxx",test_dir);
				}
			}
		}
		else
		{
			for(int i=0;i<Class.size();i++)
			{
				ArrayList<String> results1 = new ArrayList<String>();
				File[] files = new File(System.getProperty("user.dir")+"/textcat/"+test_dir+"/"+Class.get(i)+"/").listFiles();
				//If this pathname does not denote a directory, then listFiles() returns null.

				for (File file : files)
				{
					if (file.isFile())
					{
						results1.add(file.getName());
						extract(file.getName(),Class.get(i),test_dir);
					}
				}
				class_fileName.put(Class.get(i), results1);
			}
		}
	}

	public void extract(String fileName,String Classname,String test_dir)
	{
		try
		{
			FileReader fr =null;
			ArrayList<String> dev_vocab = new ArrayList<String>();
			String[] a=new String[100];
			if(test_dir.equals("test"))
				fr = new FileReader(System.getProperty("user.dir")+"/textcat/"+test_dir+"/"+fileName);
			else
				fr = new FileReader(System.getProperty("user.dir")+"/textcat/"+test_dir+"/"+Classname+"/"+fileName);
			BufferedReader br = new BufferedReader(fr);

			String line;
			double score =0.0;
			while ((line = br.readLine()) != null)
			{
				//String alphaAndDigits = line.replaceAll("[^a-zA-Z0-9]+"," ");
				//String temp = alphaAndDigits.replaceAll("\\s+", " ").trim();
				String temp = line.replaceAll("\\s+", " ").trim();
				temp.replaceAll("\\t", "");
				a= temp.split(" ");
				a= temp.split(" ");
				for(int i=0;i<a.length;i++)
				{
					//if(!(isNumeric(a[i])||(a[i].length()==1)||(a[i].length()==2)))
					if(isAlphaNumeric(a[i]))
					{
						dev_vocab.add(a[i]);
					}
				}
			}
			fr.close();
			br.close();
			calcScore(dev_vocab,fileName,score);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public void calcScore(ArrayList<String> s,String fileName,double score)
	{
		for(int i=0;i<Class.size();i++)
		{
			score = Math.log(prior.get(Class.get(i)));
			Map<String,Double> tmp = new HashMap<String,Double>();
			tmp=(HashMap)cond_prob.get(Class.get(i));
			for(int j=0;j<s.size();j++)
			{
				if(tmp.get(s.get(j))!=null)
				{
					double a = (double)tmp.get(s.get(j));
					score += a;
				}
			}
			if(Class.get(i).equals("neg"))
			{
				neg_score.put(fileName,score);
				score = 0.0;
			}
			else
			{
				pos_score.put(fileName,score);
				score =0.0;
			}
		}
	}

	public void print_prediction(String prediction_file,String test_dir)
	{
		try
		{
			String fileName = "FileName";
			String Negative_score = "Negative score";
			String Positive_score ="Positive score";
			PrintWriter out = new PrintWriter(prediction_file);
			Iterator it = neg_score.entrySet().iterator();
			out.format("%-20s %-30s %-30s",fileName,Negative_score,Positive_score);
			out.print(System.getProperty("line.separator"));
			while(it.hasNext())
			{
				Map.Entry i = (Map.Entry)it.next();
				out.format("%-20s %-30s %-30s",i.getKey(),i.getValue(),pos_score.get(i.getKey()));
				out.print(System.getProperty("line.separator"));
			}

			it = neg_score.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry i = (Map.Entry)it.next();
				double b = (double)i.getValue();
				double c =(double)pos_score.get(i.getKey());
				if(b>c)
					//if(new Double(b).compareTo(new Double(c))>0)
				{
					prediction.put((String)i.getKey(), "neg");
				}
				else
				{
					prediction.put((String)i.getKey(), "pos");
				}
			}
			if(test_dir.equals("dev")||(test_dir.equals("train")))
			{
				calculate_dev_correctness();
			}

			int cnt1 =0;
			int cnt2=0;
			it = neg_score.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry i = (Map.Entry)it.next();
				double b = (double)i.getValue();
				double c =(double)pos_score.get(i.getKey());
				if(new Double(b).compareTo(new Double(c))>0)
					cnt1++;
				else cnt2++;

			}
			System.out.println("neg count "+cnt1);
			System.out.println("pos count "+cnt2);

			out.flush();
			out.close();
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public void calculate_dev_correctness()
	{
		Iterator it = class_fileName.entrySet().iterator();

		int count1 =0;
		int count2=0;
		while(it.hasNext())
		{
			Map.Entry i = (Map.Entry)it.next();
			if(i.getKey().equals("neg"))
			{
				Iterator it2 = prediction.entrySet().iterator();
				while(it2.hasNext())
				{
					Map.Entry i2 = (Map.Entry)it2.next();

					if(((String)i2.getValue()).equals("neg"))
						if(((ArrayList)i.getValue()).contains((String)i2.getKey()))
							count1++;
				}
			}
			else
			{
				Iterator it2 = prediction.entrySet().iterator();
				while(it2.hasNext())
				{
					Map.Entry i2 = (Map.Entry)it2.next();
					if(((String)i2.getValue()).equals("pos"))
						if(((ArrayList)i.getValue()).contains((String)i2.getKey()))
							count2++;
				}
			}
		}

		System.out.println((double)count1/class_fileName.get("pos").size());
		System.out.println((double)count2/class_fileName.get("neg").size());
	}

	public void readModalFile(String modal)
	{
		try
		{
			Double prior_neg=0.0,prior_pos=0.0;
			HashMap<String,Double> neg_prob = new HashMap<String,Double>();
			HashMap<String,Double> pos_prob = new HashMap<String,Double>();
			String[] a=new String[10];
			FileReader fr = new FileReader(modal);
			BufferedReader br = new BufferedReader(fr);
			String line;
			int counter =0;
			while ((line = br.readLine()) != null)
			{
				if(counter==0)
				{
					counter++;
				}
				else
				{
					a= line.split("\\|");	;

					if(a[1].contains("neg"))
					{
						neg_prob.put(a[3].trim(),Double.parseDouble(a[4].trim()));
						prior_neg = Double.parseDouble(a[2].trim());
					}
					else
					{
						pos_prob.put(a[3].trim(),Double.parseDouble(a[4].trim()));
						prior_pos = Double.parseDouble(a[2].trim());
					}
				}


			}
			cond_prob.put("neg", neg_prob);
			cond_prob.put("pos", pos_prob);
			prior.put("neg", prior_neg);
			prior.put("pos",prior_pos);
			//System.out.println(cond_prob);
			fr.close();
			br.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	// list of classes for the Collection
	public void createClassList(String test_dir)
	{
		try
		{
			File file = new File(System.getProperty("user.dir")+"/textcat/train/");
			String[] names = file.list();

			for(String name : names)
			{
				if (new File(System.getProperty("user.dir")+"/textcat/train/"+ name).isDirectory())
				{
					Class.add(name);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public static boolean isAlphaNumeric(String str)
	{
		for (char c : str.toCharArray())
		{
			if (Character.isDigit(c)||Character.isAlphabetic(c)) return true;

		}
		return false;
	}

}
