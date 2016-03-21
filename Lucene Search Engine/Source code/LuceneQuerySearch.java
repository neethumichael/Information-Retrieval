import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

public class LuceneQuerySearch 
{
	private static Analyzer sAnalyzer = new SimpleAnalyzer(Version.LUCENE_47);
	public Map<Integer,String> queryList = new HashMap<Integer,String>();

	public void search(String indexLocation)
	{
		try 
		{
			PrintWriter out = new PrintWriter("searchResult.txt");
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
					indexLocation)));
			IndexSearcher searcher = new IndexSearcher(reader);

			String s = "";
			readQuery("queries.txt");
			Iterator it = queryList.entrySet().iterator();
			int query_id =0;
			while(it.hasNext())
			{
				Map.Entry i = (Map.Entry)it.next();
				s = (String)i.getValue();
				query_id++;
				TopScoreDocCollector collector = TopScoreDocCollector.create(2000, true);
				Query q = new QueryParser(Version.LUCENE_47, "contents",
						sAnalyzer).parse(s);
				QueryScorer scorer = new QueryScorer(q, "contents");
				Highlighter highlighter = new Highlighter(scorer);
				searcher.search(q, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;

				//  display results
                System.out.print(i.getValue()+" ");
				System.out.println("Found " + hits.length + " hits.");
				out.println("Query"+"\t|"+"Rank" + "\t|" + "Doc ID"+ "\t|"+"Score"+"\t\t|"+"Text Snippets");
				out.println("-------------------------------------------------------------------------------");
				for (int j = 0; j < 100; ++j) 
				{
					int docId = hits[j].doc;
					Document d = searcher.doc(docId);
                    String a[] =new String[600];
					String storedField = d.get("contents");
					TokenStream stream = TokenSources.getAnyTokenStream(searcher
							.getIndexReader(), docId, "contents", d, sAnalyzer);
					Fragmenter fragmenter = new SimpleSpanFragmenter(scorer,200);
					
					highlighter.setTextFragmenter(fragmenter);
					String fragment = highlighter.getBestFragment(stream, storedField);
					//org.jsoup.nodes.Document doc2 = Jsoup.parse(fragment);
				    a = fragment.split(" ");
				    
				    String new_frag="";
				   // System.out.println(a.length);
				   for(int k=0;k<a.length;k++)
				    {
					   //System.out.print(a[k]+" ");
				    	if((k>200)||(a[k].equals("PM")||(a[k].equals("AM"))))
				    	{
				    		new_frag += a[k];
				    		break;
				    	}
				    	else
				    	{
				    	new_frag += a[k];
				    	new_frag +=" ";
				    	}
				    	
				    }
				  // System.out.println();
				  //  System.out.println(new_frag);
					out.println(query_id+"\t|"+(j + 1) + "\t|" + d.get("filename").substring(5,9)
							+ "\t|"+ hits[j].score+"\t|"+new_frag);
					out.println();
				}
			} 
			System.out.println("Query results written to file searchResult.txt");
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}		
	}

	//reads the list of query from query.txt and write them to queryList
	public void readQuery(String fileName)
	{
		String[] a = new String[20];
		try
		{
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			ArrayList<String> temp = null;
			String line;
			int query_id =1;
			while ((line = br.readLine()) != null) 
			{
				String query = line;
				queryList.put(query_id,query);
				query_id++;
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
