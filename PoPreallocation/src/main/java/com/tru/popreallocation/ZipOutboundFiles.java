package com.tru.popreallocation;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.routepolicy.quartz.CronScheduledRoutePolicy;
import org.apache.camel.spi.PropertiesComponent;

public class ZipOutboundFiles extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		PropertiesComponent prc = getContext().getPropertiesComponent();
		String ziproutescheduler=prc.loadProperties().getProperty("scheduler");
               // System.out.println(ziproutescheduler);
		
		GetDirectoryName GetDirectoryName =new GetDirectoryName();
		CronScheduledRoutePolicy startPolicy = new CronScheduledRoutePolicy();
		startPolicy.setRouteStartTime("* 50 16 * * ?");
		//startPolicy.setRouteSuspendTime("* 52 15 * * ?");
		startPolicy.setRouteStopTime("0 55 16 * * ?");
		from("{{cinAppointment.path}}").routeId("PoPreallocation-ZipOutboundFiles-Route").routePolicy(startPolicy).noAutoStartup()
		//from("file:C://in?recursive=true&delay=5000&synchronous=true").routeId("ZipOutboundFiles-Route").routePolicy(startPolicy).noAutoStartup()
		//.marshal().zipFile()
		.log("${header.CamelFileName}")
		.log("$simple{date:now:yyyy-MM-dd HH:mm:ss}")
		.setProperty("CamelFileName", simple("${header.CamelFileName}"))
		.setHeader("CamelFileName").method(GetDirectoryName,"process")
		.log( "${header.CamelFileName} filename")
		 .process(new Processor() {
	            @Override
	            public void process(final Exchange exchange) throws Exception {
	              	
	            	String filename=(String) exchange.getProperty("CamelFileName");
	            	 String separator=File.separator;
	            	filename=filename.replace(separator,"#");
	            	String parts[]=filename.split("#");
	            	
	            	filename=parts[parts.length-1];
	            	//System.out.println(filename);
	            	String parts2[]=filename.split("_");
	            	parts2[0]="CIN";
	            	parts2[parts2.length-1]=parts2[parts2.length-1].replace(".txt", "");
	            	String Name="";
	            	for(int i=0;i<parts2.length;i++) {
	            		Name=Name+parts2[i];
	            	    if(i!=parts2.length-1)
	            	    	Name=Name+"_";}
	            	System.out.println(Name);
	            	exchange.getIn().setHeader("FileName", Name);
	            }
	        })
		// .log("${in.header.CamelFileName}")
		.aggregate(new MyZipAggregationStrategy (true))		
         .constant(true)    
		 .completionFromBatchConsumer()
		 .completionSize(3)
		 .setHeader(Exchange.FILE_NAME, simple("${in.header.ActualFileName}.zip"))
		  //.to("file:C://out")
		 .to("{{outboundfiles.path}}")
		.end();
		
		
		
		
		
	}

}
