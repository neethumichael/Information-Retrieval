
public class LuceneSearchEngine 
{
	public static void main(String []args)
	{
		try
		{
			String indexLocation = null;
			
			LuceneIndex index = new LuceneIndex();
			index.createIndex();
			indexLocation = index.getIndexLocation();
			System.out.println("Index created is created successfully at "+indexLocation);
			
			LuceneZipf zipf = new LuceneZipf();
			zipf.word_frequency(indexLocation);
			System.out.println("Sorted Term-Frequency list written to file wordFrequency.txt");
			System.out.println("zip graph is plotted successfully zipf.jpg and log-zipf.jpg");
			
			LuceneQuerySearch search = new LuceneQuerySearch();
			search.search(indexLocation);
						
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

}
