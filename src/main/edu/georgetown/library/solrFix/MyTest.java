package edu.georgetown.library.solrFix;

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

import org.apache.solr.common.SolrInputDocument;

public class MyTest {
    int fooi = 0;
    Foo foo;
    
    MyTest() {
    	foo = new Foo();
    }
    
    private static class Foo {
    	int fun() {
    		return 2;//ooi;
    	}
    	public String toString() {return "xx" + fun();}
    }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Foo ffoo = new Foo();
		
		
		Random rand = new Random();
		for(int i=0;i<100; i++) {
			//BigInteger bi = new BigInteger(500,rand);
			UUID bi = UUID.randomUUID();
			System.out.println(bi);
		}
		for(int i=0;i<3; i++) {
  		    System.out.println(new UUID(1L, rand.nextLong()));
		}
		for(int i=0;i<3; i++) {
  		    System.out.println(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
		}
		
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("title", "foo");
		

	}

}
