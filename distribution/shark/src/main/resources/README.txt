Configure shark/conf/Shark.conf changing the following line

  DatabaseManager.ConfigurationDir=${shark_webapp}/conf/dods

where ${shark_webapp} is the shark folder you've just copied.

Change 

CMDBuild.WS.ExtSync.EndPoint=http://${serverip}:${serverport}/${cmdbuild_webapp}/services/soap/ExternalSync
CMDBuild.WS.EndPoint=http://${serverip}:${serverport}/${cmdbuild_webapp}/services/soap/Webservices
CMDBuild.EndPoint=http://${serverip}:${serverport}/${cmdbuild_webapp}/shark/

to refer to CMDBuild's URL 

Ex. 

${serverip}:${serverport}/${cmdbuild_webapp} could be something like "localhost:8080/cmdbuild/"

and so we could write

   CMDBuild.WS.ExtSync.EndPoint=http://localhost:8080/cmdbuild/services/soap/ExternalSync
   CMDBuild.WS.EndPoint=http://localhost:8080/cmdbuild/services/soap/Webservices
   CMDBuild.EndPoint=http://localhost:8080/cmdbuild/shark/


In shark/META-INF/context.xml change the name of the database, putting the name of cmdbuild database.

Ex.

   url="jdbc:postgresql://localhost/${cmdbuild}" 

   url="jdbc:postgresql://localhost/cmdbuild"


Note: 	Actually shark uses the same db of CMDBuild, storing its data inside schema "shark".
	If you want to restore an empty schema you can run ${cmdbuild_home}/WEB-INF/sql/shark_schema/02_shark_emptydb.sql
	
	The user of shark in postgres is created by cmdbuild with the following sql (${cmdbuild_home}/WEB-INF/sql/shark_schema/01_shark_user.sql)

		CREATE ROLE shark LOGIN
			ENCRYPTED PASSWORD 'md5088dfc423ab6e29229aeed8eea5ad290'
			NOSUPERUSER NOINHERIT NOCREATEDB NOCREATEROLE;
			ALTER ROLE shark SET search_path=pg_default,shark; 

		Please note that the last line is absolutely needed when using shark on CMDBuild db.

When Shark is up and running, configure cmdbuild workflow inside Administration module or edit cmdbuild/WEB-INF/conf/workflow.conf putting the correct address of the Shark web services.

#is the workflow enabled?
enabled=true

#where is the sharkWebServices shark application
endpoint=http://${serverip}:${serverport}/${cmdbuild_webapp}

Ex.
endpoint=http://localhost:8181/shark

Restart Tomcat.


Now CMDBuild is configured ready to run workflow.

