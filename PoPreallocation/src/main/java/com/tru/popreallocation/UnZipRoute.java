package com.tru.popreallocation;

import java.io.File;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLNonTransientConnectionException;
import java.util.zip.ZipFile;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.apache.camel.Processor;

public class UnZipRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		// TODO Auto-generated method stub
		
		onException(IllegalStateException.class)
		   .continued(true)
		   .log(LoggingLevel.ERROR,"An Error processing the ${header.CamelFileName} File")
//		   .setHeader("StoreNo").method(GetDataFromFile, "getStoreNo")
	       .setHeader("FileName",simple("${file:name}"))
	       .setHeader("RunDate",simple("${date:now:yyyyMMdd}"))
	       .setHeader("RjctReason",constant("Error while Unzipping the feed or No Files present"))
//		   .to("sql:insert into EODPSTS(RUNDATE,STORENO,APPNAME,FILENAME,RCVONESB,SNTFRMESB,RJCTREASN) values(:#RunDate,:#StoreNo,'EOD',:#FileName,'F','F',:#RjctReason)?dataSource=#csp")
	       .log("${header.RjctReason} in ${file:name} file")
	       .stop();
		  
	         onException(SQLNonTransientConnectionException.class)
	 	 	 .continued(true)
	 	 	 .log(LoggingLevel.ERROR,"An Error occured!")
	 	 	 .log("Tried Again")
	 	 	 .maximumRedeliveries(3)
	          .stop(); 
	 	    
	         onException(SQLIntegrityConstraintViolationException.class)
	 	 	 .continued(true)
	 	 	 .log(LoggingLevel.ERROR,"An Duplicate key Error occured!")
	 	 	 .log("Tried Again")
	          .stop(); 
	         
		from("{{unzip.path}}").routeId("UnZip-Route")
		.log("in unzip ${header.CamelFileName}")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				File target = exchange.getIn().getBody(File.class);
				System.out.println(target);
				ZipFile zipfile = null;
			    try {
			        zipfile = new ZipFile(target);
			        boolean isValid = true;
			        System.out.println("Is valid ZIP");
					exchange.getIn().setHeader("isValid", isValid);
			    } catch (IOException e) {
			    	boolean isValid = false;
			    	System.out.println("IS corrupted ZIP");
			    	exchange.getIn().setHeader("isValid", isValid);
			    } finally {
			        try {
			            if (zipfile != null) {
			                zipfile.close();
			                zipfile = null;
			            }
			        } catch (IOException e) {
			        }
			    }
			}
		})
		.log("${header.isValid}")
		.choice()
		.when(header("isValid").isEqualTo(true))
			.log("Sent")
			.to("{{outboundfiles.path}}")
		.endChoice()
		.when(header("isValid").isEqualTo(false))
			.log("failed")
			.setHeader("filename",simple("${file:name}"))
			.setHeader("reportTime",simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
			.log("${header.reportTime}")
			//.to("sql:insert into ISPLIBCIN.EFPPERRFIL(ERRFILENM,FLAG01) values(:#filename, 'F')")
			.to("{{errorZip.path}}")
			.log("sent to error folder")
			.to("sql:insert into ISPLIBCIN.EFPPERRFIL(ERRFILENM,REPORTTIME,RESENDTIME,FLAG01) values(:#filename,:#reportTime,:#reportTime,'F')")
			.log("inserted record")
		.endChoice()
        .end();
	}
}
