DrillRig - SSH tunnel management server
=======================================

With DrillRig you can manage your SSH tunnel endpoints via Web GUI.
The application uses an embedded Jetty web server to serve the admin pages, sshj (https://github.com/shikhar/sshj) for the SSH communication and jsocks for socks proxy support.


Steps to Build the system
-------------------------

First download JSocks http://sourceforge.net/projects/jsocks/files/jsocks.jar/download and
install JSocks in your local maven repository.

mvn install:install-file -Dfile=jsocks.jar -DgroupId=de.flohrit.drillrig -DartifactId=jsocks -Dversion=1.0 -Dpackaging=jar

Build DrillRig: mvn install

Run DrillRig
------------

java -jar target/drillrig-1.0-jetty.jar
or within eclipse "run DrillRig"

Open WebGUI:

http://localhost:8080/
User: admin
Pwd: admin

Configuration
-------------

1. Create a connection object. A connection object holds the information (host, user, password) which is necessary for login at a foreign host via SSH. 

2. Create a session object. A session object is a group of port fowardings, for better management i.e. activate/deactivate session

3. Create a forward object. A forward object holds the information which is necessary to forward local TCP ports to foreign hosts and vice versa.

5. Save changes (Action -> Save changes) and wait til the status bar shows "New configuration activated."

6. Change to the "monitoring" tab to show the actual state of the tunnel endpoints.


Contribution:

Still a lot to do, so if anyone wants to contribute to this project - you are welcome!
