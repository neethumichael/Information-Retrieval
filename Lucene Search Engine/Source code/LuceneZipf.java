import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class LuceneZipf 
{
	public Map<Integer,Double> prob = new HashMap<Integer,Double>();

	public void word_frequency(String indexLocation)
	{
		try
		{
			Map<String,Integer> dl = new HashMap<String,Integer>();

			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
					indexLocation)));
			PrintWriter out = new PrintWriter("wordFrequency.txt");
			PrintWriter out2 = new PrintWriter("plots1.txt");
			TermsEnum termEnum = MultiFields.getTerms(reader, "contents").iterator(
					null);
			int total =0;
			int no_of_words =0;
			int rank =1;

			while (termEnum.next() != null) 
			{
				String term = termEnum.term().utf8ToString();
				{
					no_of_words++;
					Term termInstance = new Term("contents", term);
					int termFreq = (int)reader.totalTermFreq(termInstance);
					int docCount = reader.docFreq(termInstance);
					total += termFreq;
					dl.put(term,termFreq);
				}
			}

			//System.out.println("total "+total);
			//System.out.println("total number of words "+no_of_words);

			// sorting wrt frequency
			ValueComparator comp = new ValueComparator(dl);
			TreeMap<String,Integer> sorted_scores = new TreeMap<String,Integer>(comp);
			sorted_scores.putAll(dl);

			// writing sorted scores to a file
			rank = 1;
			Iterator it = sorted_scores.entrySet().iterator();		
			while (it.hasNext()) 
			{
				Map.Entry i = (Map.Entry)it.next();
				out.println(rank+"\r\t|"+i.getKey()+"\r\t|"+i.getValue());
				rank++;
			}

			rank=1;
			Iterator it2 = sorted_scores.entrySet().iterator();
			while(it2.hasNext())
			{
				Map.Entry i = (Map.Entry)it2.next();
				double nn = (int)i.getValue();
				double n = nn/total;
				prob.put(rank, n);
				out2.println(rank+"\t"+n);
				rank++;
			}
			zipf_chart();
			zipf_log_chart();
			out.flush();
			out.close();
			out2.flush();
			out2.close();

			// done writing to file
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public void zipf_chart()
	{
		try 
		{
			// Create a simple XY chart
			XYSeries series = new XYSeries("Log(Rank) vs Log(Probablity)");
			Iterator it = prob.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry i =(Map.Entry)it.next();
				double a = Math.log((Integer)i.getKey());
				double b =Math.log((Double)i.getValue());
				series.add(a, b);
			}

			// Add the series to data set
			XYSeriesCollection dataset = new XYSeriesCollection();
			dataset.addSeries(series);

			// Generate the graph
			JFreeChart chart = ChartFactory.createScatterPlot(
					"Log(Rank) vs Log(Probablity)", // Title
					"Log (Rank)", // x-axis Label
					"Log (Probablity)", // y-axis Label
					dataset, // Dataset
					PlotOrientation.VERTICAL, // Plot Orientation
					true, // Show Legend
					true, // Use tooltips
					false // Configure chart to generate URLs?
					);

			XYPlot xyPlot = (XYPlot)chart.getXYPlot();
			XYLineAndShapeRenderer csRenderer = new XYLineAndShapeRenderer();
			//csRenderer.setDrawOutlines(true);
			csRenderer.setDrawSeriesLineAsPath(false);
			csRenderer.setSeriesPaint(0, ChartColor.black);

			for(int k=0;k<series.getItemCount();k++)
			{
				csRenderer.setSeriesShapesVisible(k, true); 
				//csRenderer.setSer
				csRenderer.setSeriesLinesVisible(k, false);
			}
			//csRenderer.setLinesVisible(false);
			xyPlot.setRenderer(csRenderer);
			//xyPlot.setDomainCrosshairVisible(true);
			//xyPlot.setRangeCrosshairVisible(true);
			XYItemRenderer renderer = xyPlot.getRenderer();

			//renderer.setSeriesPaint(0, ChartColor.blue);
			NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();

			domain.setAutoRange(true);
			domain.setVerticalTickLabels(true);
			NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
			range.setAutoRange(true);
			range.setTickLabelsVisible(true);
			ChartUtilities.saveChartAsJPEG(new File("log-zipf.jpg"), chart, 800, 600);
		} 
		catch (IOException e) 
		{
			System.err.println("Problem occurred creating chart.");
		}
	}
	public void zipf_log_chart()
	{
		try 
		{
			// Create a simple XY chart
			XYSeries series = new XYSeries("Rank vs Probablity");

			Iterator it = prob.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry i =(Map.Entry)it.next();
				series.add((Integer)i.getKey(), (Double)i.getValue());
			}

			// Add the series to your data set
			XYSeriesCollection dataset = new XYSeriesCollection();
			dataset.addSeries(series);

			// Generate the graph
			JFreeChart chart = ChartFactory.createScatterPlot(
					"Rank vs Probablity", // Title
					"Rank", // x-axis Label
					"Probablity", // y-axis Label
					dataset, // Dataset
					PlotOrientation.VERTICAL, // Plot Orientation
					true, // Show Legend
					true, // Use tooltips
					false // Configure chart to generate URLs?
					);

			XYPlot xyPlot = (XYPlot)chart.getXYPlot();
			XYLineAndShapeRenderer csRenderer = new XYLineAndShapeRenderer();
			//csRenderer.setDrawOutlines(true);
			csRenderer.setDrawSeriesLineAsPath(false);
			//csRenderer.setSeriesLinesVisible(series, false);
			//CSRenderer.setSeriesShapesVisible(series, false);
			for(int k=0;k<series.getItemCount();k++)
				csRenderer.setSeriesShapesVisible(k, false); 
			xyPlot.setRenderer(csRenderer);
			xyPlot.setDomainCrosshairVisible(true);
			xyPlot.setRangeCrosshairVisible(true);
			XYItemRenderer renderer = xyPlot.getRenderer();
			renderer.setSeriesPaint(0, ChartColor.blue);

			NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
			domain.setRange(0.0, 2000);
			domain.setTickUnit(new NumberTickUnit(500));
			domain.setVerticalTickLabels(true);

			NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
			range.setRange(0.0, 0.04);
			range.setTickUnit(new NumberTickUnit(0.005));
			range.setTickLabelsVisible(true);

			ChartUtilities.saveChartAsJPEG(new File("zipf.jpg"), chart, 800, 600);

		} 
		catch (IOException e) 
		{
			System.err.println("Problem occurred creating chart.");
		}			 
	}

}

class ValueComparator implements Comparator<String> 
{
	Map<String,Integer> base;
	public ValueComparator(Map<String,Integer> base) 
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
