package edu.georgetown.library.solrFix;

/*
 */

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrView {

	static String conttype = "";
	
	public static void main(String[] args) {
		boolean win = System.getProperty("os.name").startsWith("Windows");
		conttype = win ? "text/xml" : "application/xml";
		long stime = Calendar.getInstance().getTimeInMillis();
		
		int MAX = 2000;
		String url = "http://localhost/solr/statistics";
		try {
			String myQuery = "*:*";
			
			HttpSolrServer server = new HttpSolrServer( url );
			
			//server.setRequestWriter(new BinaryRequestWriter());
			SolrQuery sq = new SolrQuery();
			sq.setQuery(myQuery);
			sq.setRows(MAX);
			XMLResponseParser xrp = new XMLResponseParser() {
				public String getContentType() {return conttype;}
			};
			
			server.setParser(xrp);
			
			int countUid = 0;
			
			int start = 0;
			for(; ;) {
				sq.setStart(start);
				QueryResponse resp  = server.query(sq);
				SolrDocumentList list = resp.getResults();
				
				if (list.size() == 0) break;
				
				for(int i=0; i<list.size(); i++) {
					//System.out.print(start + i);
					//System.out.print("\t");
					SolrDocument doc = list.get(i);
					//System.out.print(doc.getFieldValue("time"));
					//System.out.print("\t");
					//System.out.println(doc.getFieldValue("uid"));
					if (doc.getFieldValue("uid")!=null) countUid++;
				}
				start += list.size();				
			} 
			
			long etime = Calendar.getInstance().getTimeInMillis();
			System.out.format("%d / %d in %,dsec", countUid, start, (etime - stime)/1000);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
