package com.tru.popreallocation;

import java.io.File;

import org.apache.camel.Exchange;

public class GetDirectoryName {
	 public String process(Exchange exchange) throws Exception {
		 
		 String filename=(String) exchange.getIn().getHeader("CamelFileName");
	     	System.out.println("filename in getdirect" +filename);

		 String separator=File.separator;
     	filename=filename.replace(separator,"#");
     	System.out.println(filename);
     	String parts[]=filename.split("#");
     	//for(String s:parts)
     		//System.out.println(s);
     	String ActualFileName="";
     	for(int i=1;i<parts.length;i++) {
     		ActualFileName=ActualFileName+parts[i];
     		if(i!=parts.length-1)
     			ActualFileName=ActualFileName+"/";
     		}
     	System.out.println("Actualfilename in getdirect" +filename);
     	
     	
		 return ActualFileName;
		 
	 }
	 

}
