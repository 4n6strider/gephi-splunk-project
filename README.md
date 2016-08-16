#Instructions on how to install Gephi:
- Clone the git repository: https://github.com/splunk/gephi-splunk-project.git

- Download Netbeans Java SE: https://netbeans.org/downloads/ 

- Open up the Gephi 0.9.1 project that you just cloned from git via Netbeans.

- File > Open Project, navigate to folder: gephi-splunk-project, and select project: gephi-0.9.1 (its a folder but netbeans recognizes it as project file)

  - It should start installing the needed dependencies automatically (may need to hit close on any dialog box that shows up). 

- The .java files that were modified from the original Gephi are also provided in our git repository under "ModifiedClasses" in case you want to reverse engineer them.

- Right click on “Gephi” in the left "projects" tab and click “clean and build”

- Still within Netbeans, expand the “Modules” directory under the “Gephi” project on the left, right click on “application”, and click “clean and build”.

- On the left sidebar, click on "Projects" tab, then "gephi", then "Modules". 

- Expand "Modules"

- Find and open up “gephi-app” by double clicking on it - it will expand into tree (somewhere below on the same sidebar).

- Right click on this "gephi-app", and press “run”.

# Plugin Installation
- Now, within the Gephi application that you just started, click on the toolbar: Tools > Plugins > Downloaded > Add Plugins… 
- Go into gephi-splunk-project/ClickMenuPlugin/target directory (cloned from git)
- Select click-menu-plugin-1.0.0.nbm. 
- Do the same for gephi-splunk-project/SplunkGephi (cloned from git), which has a file called splunk-gephi-1.0.0.nbm. 
- Click install for these two plugins, follow prompts, which in turn will prompt a restart of Gephi, click Restart Now.

#Server set-up
**Dependencies:**
- Mac:
  - Pip
    - Download get-pip.py from https://bootstrap.pypa.io/get-pip.py
    - In the directory you downloaded get-pip.py to run: python get-pip.py
    - Splunklib:
    - Run: sudo pip install splunk-sdk
    
  - Flask:
    - Run: sudo pip install Flask
  - Requests:
    - Run: sudo pip install requests
- Windows:
  - Pip
    - Download get-pip.py from https://bootstrap.pypa.io/get-pip.py
    - In the directory you downloaded get-pip.py to run: python get-pip.py
  - Splunklib:
    - Run: pip install splunk-sdk
  - Flask:
    - Run: pip install Flask
  - Requests:
    - Run: pip install requests
    
**Starting local servers**
  - To set up the local Gephi-Splunk Server, open a terminal and move into the directory containing the gephi_gss folder. Then move into gephi_gss/app. 
    - Run: "flask __init__.py" and the local server should start up.
  - To set up the script server, open a terminal and move into the directory containing the script_server folder. Then move into script_server/app.
    - Run "flask __init__.py" and the local server should start up. 
    
  - You can visit http://127.0.0.1:5000/ and http://127.0.0.1:4999/ in your browser to see a (rough) front end and some administration functionality. Default username/password for the GSS and the script server is "admin"/"secret". 

  - If you have already set up Gephi, you should at this point be able to run through the demos. 
    - Note the first demo is just using locally stored data on the GSS, because the Splunk searches we are running will not work on your Splunk instance without importing the whole data set we used. To set up this project to use live Splunk searches in the second demo, you will first need to import data into your Splunk instance, then tweak some server code to point to the location of your instance. This will be discussed further below.
  
**Connecting with your Splunk instance**
- First you will need to import the sample.csv data set that we are using for our visualizations into a local Splunk instance you have access to. 
- Make sure to name the sourcetype of this data "sample" by clicking on "Save as" and changing the name from csv to "sample". 
- Now you need to make sure the scripts that will interact with Splunk are pointing to the right instance (default is localhost:8000) and have the right credentials.
- Shut down the script_server using Control-C
- Open the file script_server/app/templates/ipinf.py and change the lines with a #CHANGEME comment to match your Splunk instance credentials.
- Restart the script server by going into the script_server/app directory
- Run "flask __init__.py" and the local server should start up.
You should now be able to run the second demo, which will be enriching data in Gephi by using a live Splunk search.

#Putting Everything Together

![alt tag](https://github.com/splunk/gephi-splunk-project/blob/master/Screenshots/use_case_1.png)
![alt tag](https://github.com/splunk/gephi-splunk-project/blob/master/Screenshots/use_case_2.png)
You should now have all you need on the Gephi side to replicate the two screenshots above
- To create the first screenshot, open up “sixIPs.gexf” with Gephi, which was cloned from git. Then, with your server started (see server setup), click on the “Get – Actions” tool, which looks like a circle with a white plus sign. Make sure the “Direct Selection” tool is selected as well, and click on a node to populate the menu with actions. Click “Drilldown All”, which should make a call to the server to populate nodes on the graph. Then apply “Force Atlas” layout for a few seconds and then “Noverlap”.
- To create the second screenshot, click from the toolbar “Plugins” and select a dataset. Then apply Fruchterman Reingold, Force Atlas, and Noverlap layouts. Warning, the Noverlap layout can crash the application if certain things are clicked afterwards. Apply a trivial, non-transforming layout right after Noverlap to avoid this crash.
- To access icons, click on the “Show Node Labels” button on the bottom

#Altering Data Sources
To change the URLs on the server to request data from, go into ClickMenuPlugin/src/main/java/org/gephi/plugins/example/tool and open up AddNodesTool.java. 

The variable for the URL is called NODE_URL. You will then have to open up terminal and run “mvn clean package” from the ClickMenuPlugin directory (install apache maven if you don’t have it)-

  - Follow instructions here for windows: https://www.mkyong.com/maven/how-to-install-maven-in-windows/
  - Install homebrew for macs: http://brew.sh/, then type in the command “brew install maven” into terminal
  
Your .nbm file in “Target” should now be updated.

  - To update the plugin within the Gephi application, you will need to start the Gephi application via netbeans as you did before.
  - Then go into Tools>Plugins>Installed
  - Select the checkbox next to "Click Menu Plugin"
  - Click the "Uninstall" button, and go through the prompts to restart the application
  - Once the application restarts, install the plugin as you did above, and your changes should be made

The data for the second screenshot is populated from the SplunkGephi plugin. This plugin imports the respective .gexf file located in /gephi-0.9.1/modules/application. 
  - To change which file is imported, alter the file path in HeadlessSimple.java (from gephi-splunk-project/SplunkGephi/src/main/java/org/gephi/plugins/example/submenu). 

You can also choose to import data directly from Splunk by making a call to the server. 
  - To do this, set your headers and URL in Splunk.java, and call the method from TestAction.java. Then follow the same instructions as were provided to alter "Click Menu Plugin, using “mvn clean package” and reinstalling the plugin.

If you wish to use other datasets (other than IP addresses, usernames, session IDs, and user agents), then you will need to modify some of the code within AddNodesTool.java of the ClickMenuPlugin and TextManager.java of Gephi sourcecode (file provided in "ModifiedClasses" directory). Specifically, the AddNodesTool class implements the node drilldown, collapse, and expand logic, and the TextManager class dictates what icon will be used based on the gephi data column, "nodetype". 

#Expanding on the basic demos
To be more clear on what format Gephi expects, I will break down the two possibilities. 

The first is starting from a blank workspace. 

In this case, the Splunk search will fully populate the workspace with data, which requires the data to be much more robust. 

This project formats the results for this kind of use case into a .gexf (https://gephi.org/gexf/format/). 

Gephi recognizes this format and automatically populates the workspace with whatever is in that file. 

The other possibility is to enrich data in an existing workspace by bringing in additional nodes and edges. 

This is done in this project through actions, which involve clicking on a node, passing that data through the server pipeline and Splunk, and returning a list of nodes that are connected to the selected node Gephi, which visualizes that new data by building edges between the selected node and the newly added nodes.

In order to expand on these demos, you are probably interested in running your own searches and visualizing those. 

Unfortunately, this will require a bit of work on your end to ensure everything is formatted correctly, however the existing code should provide a good starting point to mimic and get your own searches into Splunk. 

I will describe where you will need to make changes for expansions to the basic demos. 

Note, this project was built only to demonstrate two specific use cases where Splunk data incorporated with data visualization from Gephi would be useful, it was not designed to be able to transform any Splunk search into a Gephi visualization with little work, the nature of the project makes achieving that more challenging than we had time for! 

However, once you understand the architecture, coming up with Splunk searches and formatting the output of those searches properly becomes the difficult part, and piecing the pipeline together to service that new action is much more straightforward.

If you want to add a new action to a project, you will need to tweak parts of each of the parts of the project. 

The script server will involve most of the work as this is where you will need to come up with a Splunk search that returns an output you are interested in, as well as formatting. 

A template for the actual script that interacts with Splunk is provided and for most cases should involve only changing the location of the Splunk instance, the authentication credentials, and the search itself (template is called connect.py and can be found in script_server/app/templates). 

Then you will need to transform this output into a format Gephi can understand. This is either a .gexf (basically xml) or a list of nodes. 

The demo did most of this formatting in script_server/app/__init__.py in the get_action() method, as well as using the to_xml.py script. 

These scripts may be usable for your searches if their output is in the same format as ours, but most likely you will need to change them to service your data. 

As for controlling when scripts get run, there are enough parameters in a url that it should be straightforward to use those to make sure the correct scripts are being called to run the Splunk search and further format its output. 

You can always add more by modifying or adding a url to those expected by flask.

In Gephi, you will need to change the getActions() method in AddNodesTool.java. 

For the demo, we have hardcoded in the menu to make development easier, but the code is there to make requests to the GSS and get back a list of available actions. 

You will need to change the nodetype in the request to make sure you are getting the correct list of actions based on the clicked nodetype.

If you are adding an action, you will also need to change some code in gephi_gss/app/__init__.py, this is just a small addition to the specific_node_info() method. Just follow the format of the other conditional statements, replacing the string in the if statement to the name of the action you are adding. Adding the action to the menu that is seen in Gephi can be done in the GSS web applications, under the Administration tab. You can also see what actions are available to each node under the Permissions tab. The default username/password is admin/secret.

#Project Architecture
This section will try to visualize the intended architecture of the project a little more, which will aid in customizing the project to your needs, as well as expanding the projects overall functionality. Note that some steps have been simplified for some of the demos A very rough overview of the architecture is pictured below. The reason for the separation of servers into a "permissions" server and a script server is to make it easier to expand this project to serve multiple use cases and leverage multiple Splunk instances, while keeping organization simple and limited to a single point. In other words, resources are separated, but management is centralized. 

![alt tag](https://github.com/splunk/gephi-splunk-project/blob/master/Screenshots/workflow.png)
