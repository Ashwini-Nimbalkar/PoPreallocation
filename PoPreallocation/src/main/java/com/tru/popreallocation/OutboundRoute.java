package com.tru.popreallocation;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

//import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.PropertiesComponent;
//import org.apache.camel.dataformat.csv.CsvDataFormat;
//import org.apache.camel.dataformat.zipfile.ZipSplitter;
//import org.apache.camel.impl.DefaultCamelContext;
//import org.apache.camel.model.dataformat.ZipFileDataFormat;
import org.apache.camel.routepolicy.quartz.CronScheduledRoutePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboundRoute extends RouteBuilder{
	
 	final Logger logger = LoggerFactory.getLogger(OutboundRoute.class);
 	@Override
	public void configure() throws Exception {
 		InputStream in=getClass().getClassLoader().getResourceAsStream("application.properties");
		//ENV.load(in);
		
		Properties props=new Properties();
		props.load(in); 
		PropertiesComponent prc = getContext().getPropertiesComponent();
 	    //prc.setLocation("classpath:com/tru/posas400integration2/application.properties");
 	    prc.setInitialProperties(props);
 	    getContext().setPropertiesComponent(prc);

                  CronScheduledRoutePolicy startPolicy = new CronScheduledRoutePolicy();
		startPolicy.setRouteStartTime("* 52 17 * * ?");
		//startPolicy.setRouteSuspendTime("* 52 15 * * ?");
		startPolicy.setRouteStopTime("0 58 17 * * ?");
	
 		
                 // from("file:C://in?delete=true&delay=20000").routePolicy(startPolicy).routeId("Outbound-Route") 
					from("{{outboundfiles.path}}{{outboundfiles.parameter}}").routeId("PoPreallocationoutbound-Route").multicast()
							.log("${header.CamelFileName}").log("$simple{date:now:yyyy-MM-dd HH:mm:ss}")
					.to("{{efuture.path}}","{{as400files.backup}}");
		      
		}
  }
