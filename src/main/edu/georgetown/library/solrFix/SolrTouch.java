package edu.georgetown.library.solrFix;

/*
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Vector;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrTouch {

	static String conttype = "";
	
	public static void main(String[] args) {
		boolean win = System.getProperty("os.name").startsWith("Windows");
		conttype = win ? "text/xml" : "application/xml";
		long stime = Calendar.getInstance().getTimeInMillis();
		
		int MAX = 50_000;
		String url = "http://localhost/solr/statistics";
		String turl = "http://localhost/solr/tstatistics";
		try {
			HttpSolrServer server = new HttpSolrServer( url );
			HttpSolrServer tserver = new HttpSolrServer( turl );
			
			//server.setRequestWriter(new BinaryRequestWriter());

			XMLResponseParser xrp = new XMLResponseParser() {
				public String getContentType() {return conttype;}
			};

			
			SolrQuery tsq = new SolrQuery();
			tsq.setQuery("*:*");
			tsq.setRows(0);
			tserver.setParser(xrp);
			
			QueryResponse tresp = tserver.query(tsq);
			int start = (int)tresp.getResults().getNumFound();
			
			tsq = new SolrQuery();

			String myQuery = "*:*";
			
			SolrQuery sq = new SolrQuery();
			sq.setQuery(myQuery);
			sq.setRows(MAX);
			sq.setSort("time", ORDER.asc);
			
			server.setParser(xrp);
			
			for(int total = 0; total<100_000 ;) {
				System.out.format("%,d%n", start);
				sq.setStart(start);
				QueryResponse resp  = server.query(sq);
				SolrDocumentList list = resp.getResults();
				
				if (list.size() == 0) break;
				
				ArrayList<SolrInputDocument> idocs = new ArrayList<SolrInputDocument>();
				for(int i=0; i<list.size(); i++) {
					SolrDocument doc = list.get(i);
					SolrInputDocument idoc = new SolrInputDocument();
					Map<String, Object> m = doc.getFieldValueMap();
					for(String k: m.keySet()){
						if (k.equals("uid")) continue;
						if (k.equals("_version_")) continue;
						idoc.addField(k, m.get(k));
					}
					idocs.add(idoc);
				}
				tserver.add(idocs);
			    tserver.commit();
				start += list.size();				
				long etime = Calendar.getInstance().getTimeInMillis();
				total += idocs.size();
				System.out.format("%,d updated in %,d sec%n", total, (etime - stime)/1000);
				System.gc();
				etime = Calendar.getInstance().getTimeInMillis();
				//System.out.format("End GC at %,d sec%n", (etime - stime)/1000);
			} 
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
