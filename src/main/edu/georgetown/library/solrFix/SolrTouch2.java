package edu.georgetown.library.solrFix;

/*
  */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

public class SolrTouch2 {

	static String conttype = "";
	
	public static void main(String[] args) {
		boolean win = System.getProperty("os.name").startsWith("Windows");
		conttype = win ? "text/xml" : "application/xml";
		
		String url = "http://localhost/solr/statistics";
		try {
			HttpSolrServer server = new HttpSolrServer( url );
			
			XMLResponseParser xrp = new XMLResponseParser() {
				public String getContentType() {return conttype;}
			};
			server.setParser(xrp);
			
			String myQueryFacet = "NOT(uid:*)";
			SolrQuery sqf = new SolrQuery();
			sqf.setQuery(myQueryFacet);
			sqf.setRows(0);
			sqf.setFacet(true);
			sqf.setFacetMinCount(0);
			sqf.addFacetField("id");
			sqf.setFacetSort("count");
			sqf.setFacetLimit(-1);
			
			int total = 0;
			int subtotal = 0;
			int batchcount = 0;
			QueryResponse fresp  = server.query(sqf);
			List<FacetField> flist = fresp.getFacetFields();

			ArrayList<String> solrids = new ArrayList<>();

			for(FacetField ff: flist) {
				for(org.apache.solr.client.solrj.response.FacetField.Count fc: ff.getValues()) {
					if (fc.getCount() == 0) continue;
					solrids.add(fc.getName());
					subtotal += fc.getCount();
					if (subtotal > 50_000 || solrids.size() >= 500) {
					    System.out.printf("\t%5d\t%5d\t%,d%n", ++batchcount, solrids.size(), subtotal);
						total += queryByIds(server, solrids);
						subtotal = 0;
					}
				}
			}
		    System.out.printf("\t%5d\t%5d\t%,d%n", ++batchcount, solrids.size(), subtotal);
			total += queryByIds(server, solrids);
			System.out.printf("Total %d%n", total);
			long etime = Calendar.getInstance().getTimeInMillis();
			System.out.format("%,d updated in %,d sec%n", total, (etime - jstime)/1000);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static long stime = Calendar.getInstance().getTimeInMillis();
	public static long jstime = stime;
 
    
    public static String getQuery(ArrayList<String> solrids) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("NOT(uid:*) AND id:(");
		boolean first = true;
		for(String s:solrids) {
			if (first) {
				first = false;
			} else {
				sbuf.append(" OR ");
			}
			sbuf.append(s);
		}
		sbuf.append(")");
        return sbuf.toString();    	
    }
	
	public static int queryByIds(HttpSolrServer server, ArrayList<String> solrids) throws SolrServerException, IOException {
		if (solrids.size() == 0) return 0;
		ArrayList<SolrInputDocument> idocs = new ArrayList<SolrInputDocument>();
		
		int MAX = 250_000;
		String myQuery = getQuery(solrids);
		//System.out.println(myQuery);
		
		SolrQuery sq = new SolrQuery();
		sq.setQuery(myQuery);
		sq.setRows(MAX);
		sq.setSort("time", ORDER.asc);
		
		
		QueryResponse resp  = server.query(sq);
		SolrDocumentList list = resp.getResults();
		
		if (list.size() > 0) {
			for(int i=0; i<list.size(); i++) {
				SolrDocument doc = list.get(i);
				SolrInputDocument idoc = ClientUtils.toSolrInputDocument(doc);
				idocs.add(idoc);
			}
			
		}
		
		server.add(idocs);
		server.commit(true, true);
		server.deleteByQuery(myQuery);
	    server.commit(true, true);
	    
	    int subtotal = idocs.size();
	    
	    //System.gc();
		long etime = Calendar.getInstance().getTimeInMillis();
		System.out.format("%,d updated in %,d sec%n", subtotal, (etime - stime)/1000);
		stime = etime;
	    solrids.clear();
	    idocs.clear();
		
    	return subtotal;
	}

}
