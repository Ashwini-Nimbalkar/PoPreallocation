package com.tru.popreallocation;


import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

public class MainApp {

	public static void main(String args[]) {
		CamelContext ctx = new DefaultCamelContext();

		ZipOutboundFiles zip = new ZipOutboundFiles();
		SqlRoute sqlroute = new SqlRoute();
		UnZipRoute unzipRoute = new UnZipRoute();
		
		try {
			ctx.addRoutes(sqlroute);
		ctx.addRoutes(zip);
			//ctx.addRoutes(unzipRoute);
			ctx.start();
			Thread.sleep(800000);
			ctx.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
