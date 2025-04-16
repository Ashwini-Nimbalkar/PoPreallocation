package com.tru.popreallocation;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.apache.camel.routepolicy.quartz.CronScheduledRoutePolicy;
import org.apache.camel.spi.PropertiesComponent;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import java.sql.SQLNonTransientConnectionException;
import org.apache.camel.LoggingLevel;

public class SqlRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {

		InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties");
		// ENV.load(in);

		Properties props = new Properties();

		props.load(in);
		PropertiesComponent prc = getContext().getPropertiesComponent();
		prc.setInitialProperties(props);
		getContext().setPropertiesComponent(prc);

		PropertiesComponent prc2 = getContext().getPropertiesComponent();

		String Url = prc2.loadProperties().getProperty("Url");
		String DriverClassName = prc2.loadProperties().getProperty("DriverClassName");
		String Username = prc2.loadProperties().getProperty("Username");
		String Password = prc2.loadProperties().getProperty("Password");

		DataSource dataSource = setupDataSource(Url, DriverClassName, Username, Password);

		// SimpleRegistry reg = new SimpleRegistry() ;
		// reg.bind("myds",dataSource);

		getContext().getRegistry().bind("mydata", dataSource);

		CsvDataFormat csv = new CsvDataFormat();
		csv.setQuoteDisabled(true);
		CronScheduledRoutePolicy startPolicy = new CronScheduledRoutePolicy();
		startPolicy.setRouteStartTime("* 41 16 * * ?");
		// startPolicy.setRouteSuspendTime("* 52 15 * * ?");
		startPolicy.setRouteStopTime("0 48 16 * * ?");

		onException(SQLNonTransientConnectionException.class).continued(true)
				.log(LoggingLevel.ERROR, "An Error occured!").log("Tried Again").maximumRedeliveries(5);

		from("{{sql.timer}}").routeId("PoPreallocation-SqlRoute").routePolicy(startPolicy).noAutoStartup()
		.to("{{seq.query}}")
				.setHeader("true", simple("true"))
				// .setHeader("skn",constant(seq))
				// .log("${header.skn}")
				// .choice()
				// .when(header("true").isEqualTo("true"))
				.split(body())
				// .log("${body[STORENO]}")
				// .to("log:row")
				//.setHeader("storeno", simple("${body[STORENO]}"))
				.setHeader("seq", simple("${body[FEEDSEQ]}"))

				// .to("sql:select SKN,CAST(CAST(CUSTDESC1 AS VARCHAR(255) CCSID 65535) AS
				// VARCHAR(255) CCSID 935) CUSTDESC1,CAST(CAST(CUSTDESC2 AS VARCHAR(255) CCSID
				// 65535) AS VARCHAR(255) CCSID 935) CUSTDESC2 from newpos.posp067s group by
				// SKN,CUSTDESC1,CUSTDESC2?dataSource=#mydata")
				//.to("{{cinappt.query}}").marshal(csv).toD("file:C://in/CIN?fileName=CIN_appointment_${date:now:yyyyMMdd}_${header.seq}.txt").end();
				.to("{{cinappt.query}}")
				 .marshal(csv)
			    .toD("{{cinappointmentsqlfiles.path}}")
			     .end();

	}

	private DataSource setupDataSource(String Url, String DriverClassName, String Username, String Password)
			throws SQLException {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(DriverClassName);
		ds.setUsername(Username);
		ds.setPassword(Password);
		ds.setUrl(Url);
		return ds;
	}

}
