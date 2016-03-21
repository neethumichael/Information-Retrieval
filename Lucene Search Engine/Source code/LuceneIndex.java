import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

public class LuceneIndex 
{
	private static Analyzer sAnalyzer = new SimpleAnalyzer(Version.LUCENE_47);
	private IndexWriter writer;
	private ArrayList<File> queue = new ArrayList<File>();
	String indexLocation = null;
	FSDirectory dir = null;
	IndexWriterConfig config = null;

	public void createIndex()
	{
		try
		{
			System.out.println("Enter the FULL path where the index will be created: (e.g. /Usr/index or c:\\temp\\index)");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String s = br.readLine();

			LuceneIndex indexer = null;

			try 
			{
				indexLocation = s;
				dir = FSDirectory.open(new File(s));
				config = new IndexWriterConfig(Version.LUCENE_47,sAnalyzer);
				writer = new IndexWriter(dir, config);
			} 
			catch (Exception ex) 
			{
				System.out.println("Cannot create index..." + ex.getMessage());
				System.exit(-1);
			}

			// ===================================================
			// read input from user until he enters q for quit
			// ===================================================
			while (!s.equalsIgnoreCase("q")) 
			{
				try 
				{
					System.out.println("Enter the FULL path to add into the index (q=quit): (e.g. /home/mydir/docs or c:\\Users\\mydir\\docs)");
					System.out.println("[Acceptable file types: .xml, .html, .html, .txt]");
					s = br.readLine();
					if (s.equalsIgnoreCase("q")) 
					{
						break;
					}

					// try to add file into the index
	
					indexFileOrDirectory(s);
				} 
				catch (Exception e) 
				{
					System.out.println("Error indexing " + s + " : "+ e.getMessage());
				}
			}
			// ===================================================
			// after adding, we always have to call the
			// closeIndex, otherwise the index is not created
			// ===================================================
			writer.close();

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}

	}

	public String getIndexLocation()
	{
		return indexLocation;
	}


	public void indexFileOrDirectory(String fileName) throws IOException
	{
		// ===================================================
		// gets the list of files in a folder (if user has submitted
		// the name of a folder) or gets a single file name (is user
		// has submitted only the file name)
		// ===================================================
		addFiles(new File(fileName));
		int originalNumDocs = writer.numDocs();
		for (File f : queue) 
		{
			FileReader fr = null;
			try 
			{
				Document doc = new Document();

				// ===================================================
				// add contents of file
				// ===================================================
				fr = new FileReader(f);
				BufferedReader br3 = new BufferedReader(fr); 
				String s_temp=""; 
				String h;
				while ((h=br3.readLine()) != null) 
				{ 					
					s_temp += h;
					s_temp +=" ";
				} 

				org.jsoup.nodes.Document doc2 = Jsoup.parse(s_temp);
				doc.add(new TextField("contents", doc2.text(),Field.Store.YES));
				doc.add(new StringField("path", f.getPath(), Field.Store.YES));
				doc.add(new StringField("filename", f.getName(),
						Field.Store.YES));

				writer.addDocument(doc);
			} 
			catch (Exception e) 
			{
				System.out.println("Could not add: " + f);
			} 
			finally 
			{
				fr.close();
			}
		}

		int newNumDocs = writer.numDocs();
		System.out.println("");
		System.out.println("************************");
		System.out
		.println((newNumDocs - originalNumDocs) + " documents added.");
		System.out.println("************************");
		queue.clear();
	}

	private void addFiles(File file) 
	{
		if (!file.exists()) 
		{
			System.out.println(file + " does not exist.");
		}
		if (file.isDirectory()) 
		{
			for (File f : file.listFiles()) 
			{
				addFiles(f);
			}
		} 
		else 
		{
			String filename = file.getName().toLowerCase();
			// ===================================================
			// Only index text files
			// ===================================================
			if (filename.endsWith(".htm") || filename.endsWith(".html")
					|| filename.endsWith(".xml") || filename.endsWith(".txt")) {
				queue.add(file);
			} 
			else 
			{
				System.out.println("Skipped " + filename);
			}
		}
	}

	/**
	 * Close the index.
	 * 
	 * @throws java.io.IOException
	 *             when exception closing
	 */
	public void closeIndex() throws IOException 
	{
		writer.close();
	}

}
