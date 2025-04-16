package com.tru.popreallocation;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.zipfile.ZipAggregationStrategy;


public class MyZipAggregationStrategy extends ZipAggregationStrategy {
    
	//MyZipAggregationStrategy ZipAggregationStrategy = new MyZipAggregationStrategy ();
	private Object preserveFolderStructure;
	private boolean b;
	 



	public MyZipAggregationStrategy(boolean preserveFolderStructure) {
		// TODO Auto-generated constructor stub
		super(preserveFolderStructure);
		this.preserveFolderStructure=preserveFolderStructure;
	}

	/*public MyZipAggregationStrategy() {
		// TODO Auto-generated constructor stub
	}

	public boolean MyZipAggregationStrategy(boolean b) {
		return b;
	}*/

	@Override
    public Exchange aggregate(final Exchange oldExchange, final Exchange newExchange) {
		
		//this.MyZipAggregationStrategy(true);
        final Exchange answer = super.aggregate(oldExchange, newExchange);
       // answer.setProperty("ActualFileName", newExchange.getProperty("FileName")); 
        answer.getIn().setHeader("ActualFileName", newExchange.getIn().getHeader("FileName")); 

        //System.out.println(newExchange.getIn().getHeader("ActualFileName"));
        return answer;
    }
}