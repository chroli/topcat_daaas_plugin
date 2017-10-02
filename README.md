
# Topcat DAaaS (Data Analysis as a Service) Plugin

The front end interface for accessing the Science and Technology Facilities Council's computational resources for remote analysis.

## Installation

1. Do a Bower install:

cd topcat_daaas_plugin/src/main/webapp
bower install

2. Build the plugin:

cd topcat_daaas_plugin/
mvn install

3. Extract the zipped file

4. As usual, ensure that the topcat_daaas_plugin-setup.properties file is set up correctly:


secure         = false
container      = Glassfish
home           = /home/glassfish/glassfish4
port           = 4848


# Derby Database
!db.target   = derby
!db.driver   = org.apache.derby.jdbc.ClientDataSource
!db.url      = jdbc:derby:topcat;create\\=true
!db.username = APP
!db.password = APP

# MySQL Database
db.target      = mysql
db.url         = jdbc:mysql://localhost:3306/topcat
db.driver      = com.mysql.jdbc.jdbc2.optional.MysqlDataSource
db.username    = icat
db.password    = icatpw


5. Install the plugin:

./setup install

6. In topcat.json add the following lines to make the 'My Machines' tab appear:

"plugins":[
        "http://vm303.nubes.stfc.ac.uk:8080/topcat_daaas_plugin"
    ],
	"daaas": {
        "createMachineDelaySeconds": 5
    }
	
7. Modify lang.json to give the tab the right name. Under ADMIN, add the following:

"MAIN_TAB": {
			"MACHINE_TYPES": "Machine Tab"
		}
8. Run another setup-install on TOPCAT and refresh your browser.

9. Make everything secure by changing secure = false to secure = true and changing http to https, as well as changing the port number from 8080 to 8181 where relevant.





