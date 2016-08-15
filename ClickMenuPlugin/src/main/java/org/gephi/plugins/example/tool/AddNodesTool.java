/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.plugins.example.tool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.AbstractAction;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.plugin.forceAtlas.ForceAtlas;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.tools.spi.MouseClickEventListener;
import org.gephi.tools.spi.NodeClickEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;
       

/**
 * Tool which reacts to clicks on the canvas by adding nodes and edges.
 * <p>
 * The tool works with two <code>ToolEventListener</code> listeners: One
 * {@link MouseClickEventListener} to react on a click on a empty part of the
 * canvas and one {@link NodeClickEventListener} to react on a click on multiple
 * nodes. The tool is creating a node at the mouse location and adds edges from
 * the newly created node to all selected nodes. That works when the user
 * increases its mouse selection area.
 * <p>
 * The tool also uses some non-api methods of <code>VizController</code>. It's
 * not really recommended at this point but we needed it for the mouse position.
 * The new Visualization API coming in a future version will expose much more
 * things...
 * <p>
 * This tool class also has an UI class which displays a simple checkbox in the
 * properties bar. The checkbox triggers a layout algorithm.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class AddNodesTool implements Tool {
        
    private int mouseX;
    private int mouseY;
    Node[] clickedNodes;
    String clickedNodeLabel;
    private final AddNodesToolUI ui = new AddNodesToolUI();

    @Override
    public void select() {
    }

    @Override
    public void unselect() {
    }
    
    @Override
    public ToolEventListener[] getListeners() {
        return new ToolEventListener[] {
            new NodeClickEventListener() {
                
                public JMenuItem collapseMenuItemFactory(final String childrenNodeType, final String parentNodeType){
                    JMenuItem menuitem = new JMenuItem("Collapse " + childrenNodeType);
                    menuitem.addActionListener(new AbstractAction("Collapse "+childrenNodeType){
                        public void actionPerformed(ActionEvent e) {
                            boolean hasNodes = false;
                            if (clickedNodes[0].getAttribute("nodetype").equals(parentNodeType) && //ipaddress
                                    clickedNodes[0].getAttribute("Notes")==null){
                                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                                Graph graph = gc.getGraphModel().getGraph();
                                GraphFactory factory = gc.getGraphModel().factory();
                                Edge[] graphEdges = graph.getEdges().toArray();
                                Node addedNode = factory.newNode();
                                for (Edge edge : graphEdges) {
                                    Node sourceNode = edge.getSource();
                                    Node targetNode = edge.getTarget();
                                      // sourceNode is the ip address, targetNode is the username
                                    if (targetNode.equals(clickedNodes[0]) && sourceNode.getAttribute("nodetype").equals(childrenNodeType)) { //username
                                        String sourceNodeLabel = sourceNode.getLabel();
                                        if (!sourceNodeLabel.equals("username Collection") && !sourceNodeLabel.equals("ipaddress Collection") &&
                                                !sourceNodeLabel.equals("useragent Collection") && !sourceNodeLabel.equals("session_id Collection")){
                                            boolean sharedEdge = false;
                                            for (Edge edge2 : graphEdges) { //check if other edges share the node
                                                Node sourceNode2 = edge2.getSource();
                                                Node targetNode2 = edge2.getTarget();
                                                if (!edge2.equals(edge) && (sourceNode.equals(sourceNode2))||sourceNode.equals(targetNode2)){
                                                    sharedEdge = true;
                                                }
                                            }
                                            if (!sharedEdge) {
                                                hasNodes = true;
                                                String existingNote = "";
                                                if (addedNode.getAttribute("Notes")!=null) {
                                                    existingNote = (String)addedNode.getAttribute("Notes");
                                                }
                                                addedNode.setAttribute("Notes", existingNote+sourceNode.getLabel()+","+Double.toString(sourceNode.getTimestamps()[0])
                                                    +","+sourceNode.getAttribute("nodetype")+"█");
                                                addedNode.addTimestamp(sourceNode.getTimestamps()[0]);
                                                addedNode.setColor(sourceNode.getColor());
                                                graph.removeNode(sourceNode);
                                                graph.removeEdge(edge);
                                            }
                                        }
                                    }
                                }
                                if (hasNodes){
                                    float[] position3d = VizController.getInstance().getGraphIO().getMousePosition3d();
                                    float centerX = position3d[0];
                                    float centerY = position3d[1];
                                    float radius = clickedNodes[0].size();
                                    float xPosition = centerX;
                                    float yPosition = centerY + 2.5f*radius;                                    
                                    //String[] fieldArray = node.split(";");
                                    addedNode.setX(xPosition);
                                    addedNode.setY(yPosition);
                                    addedNode.setSize(20f);
                                    addedNode.setAttribute("label", childrenNodeType+" Collection");
                                    addedNode.setAttribute("nodetype", childrenNodeType);
                                    graph.addNode(addedNode);
                                    Edge edge = factory.newEdge(addedNode,clickedNodes[0]);
                                    edge.setColor(clickedNodes[0].getColor());
                                    graph.addEdge(edge);
                                }
                            }
                        }
                    });
                    return menuitem;
                }
                
                public JMenuItem expandMenuItemFactory(final String childrenNodeType){//, final String parentNodeType){
                    JMenuItem menuitem = new JMenuItem("Expand "+childrenNodeType);
                    menuitem.addActionListener(new AbstractAction("Expand "+childrenNodeType){
                        public void actionPerformed(ActionEvent e) {
                            if (clickedNodes[0].getLabel().equals(childrenNodeType+" Collection")){ //if node is the correct collection
                                if (clickedNodes[0].getAttribute("nodetype").equals(childrenNodeType) && //username
                                        clickedNodes[0].getAttribute("Notes")!=null){
                                    GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                                    Graph graph = gc.getGraphModel().getGraph();
                                    GraphFactory factory = gc.getGraphModel().factory();
                                    Edge[] graphEdges = graph.getEdges().toArray();
                                    Node collectionNode = clickedNodes[0];
                                    Node parentNode = collectionNode;
                                    for (Edge edge : graphEdges) {
                                        if (edge.getSource().equals(collectionNode)) {
                                            parentNode = edge.getTarget();
                                        }
                                    }
                                    String nodeString = (String) collectionNode.getAttribute("Notes");
                                    String[] nodeArray = nodeString.split("█");
                                    float[] position3d = VizController.getInstance().getGraphIO().getMousePosition3d();
                                    float centerX = parentNode.x();
                                    float centerY = parentNode.y();
                                    float radius = collectionNode.size();
                                    float xPosition = centerX;
                                    float yPosition = centerY + 10f*radius;                                    
                                    int angleIncrement = (int)(360.0/(double)(nodeArray.length));
                                    int angle = angleIncrement;

                                    for (String node : nodeArray) {
                                        String[] fieldArray = node.split(",");
                                        Node addedNode = factory.newNode();
                                        addedNode.setX(xPosition);
                                        addedNode.setY(yPosition);
                                        xPosition = centerX + (10f*radius * (float) Math.sin(Math.toRadians(angle)));
                                        yPosition = centerY + (10f*radius * (float) Math.cos(Math.toRadians(angle)));
                                        angle += angleIncrement;
                                        addedNode.setSize(10f);
                                        addedNode.setAttribute("label", fieldArray[0]);
                                        addedNode.addTimestamp(Double.valueOf(fieldArray[1]));
                                        addedNode.setAttribute("nodetype", fieldArray[2]);
                                        Color color = collectionNode.getColor();
                                        addedNode.setColor(color);
                                        graph.addNode(addedNode);
                                        Edge edge = factory.newEdge(addedNode,parentNode);
                                        Color targetColor = parentNode.getColor();
                                        edge.setColor(targetColor);
                                        graph.addEdge(edge);
                                    }
                                    graph.removeNode(collectionNode);
                                }
                            }
                        }
                    });
                    return menuitem;
                }
                @Override
                public void clickNodes(Node[] nodes){
                    //Add edges with the clicked nodes
                   
                    clickedNodes = nodes;
                    clickedNodeLabel = clickedNodes[0].getLabel();
                    for (Node n : nodes) {
                        //JPanel gc = Lookup.getDefault().lookup(JPanel.class);
                        GraphDrawable graphDrawable = VizController.getInstance().getDrawable();
                        Component graphComp = graphDrawable.getGraphComponent();
                        float[] mousePosition = VizController.getInstance().getGraphIO().getMousePosition();
                        float globalScale = graphDrawable.getGlobalScale();
                        mouseX = (int) (mousePosition[0] / globalScale);
                        mouseY = (int) ((VizController.getInstance().getDrawable().getViewport().get(3) - mousePosition[1]) / globalScale);
                                
                        JPopupMenu popup = new JPopupMenu();
                        String nodeLabel = n.getLabel();
                        String nodetype = (String) n.getAttribute("nodetype");
                        
                        if (nodeLabel.equals("username Collection") || nodeLabel.equals("ipaddress Collection") ||
                                nodeLabel.equals("useragent Collection") || nodeLabel.equals("session_id Collection")) {
                            JMenuItem menuitem1 = null;
                            JMenuItem menuitem2 = null;
                            JMenuItem menuitem3 = null;
                            if (nodetype.equals("username")) {
                                menuitem1 = expandMenuItemFactory("username");//,"ipaddress");
                            } else if (nodetype.equals("ipaddress")) {
                                menuitem1 = expandMenuItemFactory("ipaddress");//,"ipaddress");
                            } else if (nodetype.equals("useragent")) {
                                menuitem1 = expandMenuItemFactory("useragent");//,"useragent");
                            } else if (nodetype.equals("session_id")) {
                                menuitem1 = expandMenuItemFactory("session_id");//,"session_id");
                            }
                            popup.add(menuitem1);
                            popup.show(graphComp, mouseX, mouseY);
                            
                        } else {
                            JMenuItem menuitem1 = null;
                            JMenuItem menuitem2 = null;
                            JMenuItem menuitem3 = null;
                            if (nodetype.equals("username")) {
                                menuitem1 = collapseMenuItemFactory("ipaddress","username");
                                menuitem2 = collapseMenuItemFactory("useragent","username");
                                menuitem3 = collapseMenuItemFactory("session_id","username");
                            } else if (nodetype.equals("ipaddress")) {
                                menuitem1 = collapseMenuItemFactory("useragent","ipaddress");
                                menuitem2 = collapseMenuItemFactory("session_id","ipaddress");
                                menuitem3 = collapseMenuItemFactory("username","ipaddress");
                            } else if (nodetype.equals("useragent")) {
                                menuitem1 = collapseMenuItemFactory("username","useragent");
                                menuitem2 = collapseMenuItemFactory("ipaddress","useragent");
                                menuitem3 = collapseMenuItemFactory("session_id","useragent");
                            } else if (nodetype.equals("session_id")) {
                                menuitem1 = collapseMenuItemFactory("username","session_id");
                                menuitem2 = collapseMenuItemFactory("ipaddress","session_id");
                                menuitem3 = collapseMenuItemFactory("useragent","session_id");
                            }
                            popup.add(menuitem1);
                            popup.add(menuitem2);
                            popup.add(menuitem3);
                            JMenuItem transformItem = transformFactory("Drilldown");
                            popup.add(transformItem);
                            JMenuItem transformAllItem = TransformAll();
                            popup.add(transformAllItem);
                            popup.show(graphComp, mouseX, mouseY);
                        }
                    }
                }
                
                private static final String USER_AGENT = "Chrome/51.0.2704.103";
                private static final String GET_URL = "http://127.0.0.1:5000/permissions/ip";
                private List<String> getActions(String NodeType) throws IOException {
                    URL obj = new URL(GET_URL);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("User-Agent", USER_AGENT);
                    int responseCode = con.getResponseCode();
                    List<String> actionArray = null;
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        String actionString = response.toString();
                        actionArray = Arrays.asList(actionString.split(","));
                    }
                    return actionArray;
                }
                
                private JMenuItem transformFactory(String actionName) {
                    JMenuItem menuItem = new JMenuItem(actionName);
                    final String NODE_URL = "http://gesman-centos7x64-001/demo/"+clickedNodes[0].getLabel();//"http://gesman-centos7x64-001/graph/test/"+clickedNodes[0].getLabel()+"/ipinf";
                    
                    menuItem.addActionListener(new AbstractAction("Do Transform"){
                        public void actionPerformed(ActionEvent e) {
                            GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                            Graph graph = gc.getGraphModel().getGraph();
                            GraphFactory factory = gc.getGraphModel().factory();
                            try {
                                //get comma separated node fields (label, modularity)
                                URL nodeURL = null;
                                try {
                                    nodeURL = new URL(NODE_URL);
                                } catch (IOException e1) {
                                    System.out.println(e1);
                                }
                                HttpURLConnection con = (HttpURLConnection) nodeURL.openConnection();
                                con.setRequestMethod("GET");
                                con.setRequestProperty("User-Agent", USER_AGENT);
                                int responseCode = con.getResponseCode();
                                String[] edgeArray = null;
                                //if (responseCode == HttpURLConnection.HTTP_OK) {
                                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                    //BufferedReader in = new BufferedReader(new FileReader("oneip_edges.txt"));
                                    String inputLine;
                                    StringBuffer response = new StringBuffer();
                                    while ((inputLine = in.readLine()) != null) {
                                        response.append(inputLine);
                                    }
                                    in.close();
                                    String actionString = response.toString();
                                    edgeArray = actionString.split("█"); //change this to handle edges
                                    clickedNodes[0].setSize(10f);
                                    float[] position3d = VizController.getInstance().getGraphIO().getMousePosition3d();
                                    float centerX = position3d[0];
                                    float centerY = position3d[1];
                                    float radius = clickedNodes[0].size();
                                    float xPosition = centerX;
                                    float yPosition = centerY + 10f*radius;                                    
                                    int angleIncrement = 360/(edgeArray.length);
                                    int angle = angleIncrement;
                                    
                                    for (String edge : edgeArray) {
                                        String[] fieldArray = edge.split("\\|"); //0=target,1=timestamp,2=nodetype
                                        Node[] graphNodes = graph.getNodes().toArray();
                                        // targetNode is the nodes being appended, to point to the clicked Node
                                        boolean targetNodeExists = false;
                                        Node desiredNode = null;
                                        for (Node existingNode : graphNodes) {
                                            if (existingNode.getLabel().equals(fieldArray[0])) {
                                                targetNodeExists = true;
                                                desiredNode = existingNode;
                                            }
                                        }
                                        if (!targetNodeExists) {
                                            Node addTargetNode = factory.newNode();
                                            addTargetNode.setX(xPosition);
                                            addTargetNode.setY(yPosition);
                                            xPosition = centerX + (10f*radius * (float) Math.sin(Math.toRadians(angle)));
                                            yPosition = centerY + (10f*radius * (float) Math.cos(Math.toRadians(angle)));
                                            angle += angleIncrement;
                                            
                                            addTargetNode.setSize(10f);
                                            addTargetNode.setAttribute("label", fieldArray[0]);
                                            addTargetNode.addTimestamp(Double.parseDouble(fieldArray[1]));
                                            addTargetNode.setAttribute("nodetype", fieldArray[2]);
                                            Color blueColor = new Color(0,191,255);
                                            Color greenColor = new Color(128,255,0);
                                            Color yellowColor = new Color(255,255,0);
                                            Color orangeColor = new Color(255,102,0);
                                            Color greyColor = new Color(192,192,192);
                                            Color purpleColor = new Color(138,43,226);
                                            Color redColor = new Color(220,20,60); //or 255,0,0
                                            if (addTargetNode.getAttribute("nodetype").equals("ipaddress")){
                                                addTargetNode.setColor(greenColor);
                                                addTargetNode.setAlpha(.5f);
                                            } else if (addTargetNode.getAttribute("nodetype").equals("username")){
                                                addTargetNode.setColor(blueColor);
                                                addTargetNode.setAlpha(.5f);
                                            } else if (addTargetNode.getAttribute("nodetype").equals("useragent")){
                                                addTargetNode.setColor(purpleColor);
                                                addTargetNode.setAlpha(.5f);
                                            } else if (addTargetNode.getAttribute("nodetype").equals("session_id")){
                                                addTargetNode.setColor(greyColor);
                                                addTargetNode.setAlpha(.5f);
                                            } else {
                                                addTargetNode.setColor(greyColor);
                                                addTargetNode.setAlpha(.5f);
                                            }
                                            
                                            graph.addNode(addTargetNode);
                                            Edge newEdge = factory.newEdge(addTargetNode,clickedNodes[0]);
                                            Color color = clickedNodes[0].getColor();
                                            newEdge.setColor(color);
                                            graph.addEdge(newEdge);
                                        } else {
                                            if (graph.getEdge(desiredNode,clickedNodes[0])==null && 
                                                    graph.getEdge(clickedNodes[0],desiredNode)==null){
                                                float existingX = desiredNode.x();
                                                float existingY = desiredNode.y();
                                                float midX = (existingX+centerX)/2f;
                                                float midY = (existingY+centerY)/2f;
                                                desiredNode.setX(midX);
                                                desiredNode.setY(midY);
                                                desiredNode.setColor(Color.red);
                                                Edge newEdge = factory.newEdge(desiredNode,clickedNodes[0]);
                                                newEdge.setColor(Color.red);
                                                graph.addEdge(newEdge);
                                            }
                                        }
                                    }
                                //}
                            } catch(IOException ioe) {
                                System.out.println("error");
                            }
                        }
                    });
                    return menuItem;
                }
                
                private JMenuItem TransformAll(){
                    
                    JMenuItem menuItem = new JMenuItem("Drilldown All");
                    GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                    Graph graph = gc.getGraphModel().getGraph();
                    GraphFactory factory = gc.getGraphModel().factory();
                    for (final Node graphNode : graph.getNodes().toArray()) {
                        if (graphNode.getAttribute("nodetype").equals("ipaddress")) {
                            final String NODE_URL = "http://gesman-centos7x64-001/demo/"+graphNode.getLabel();//"http://gesman-centos7x64-001/graph/test/"+graphNode.getLabel()+"/ipinf";

                            menuItem.addActionListener(new AbstractAction("Drilldown All"){
                                public void actionPerformed(ActionEvent e) {
                                    GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                                    Graph graph = gc.getGraphModel().getGraph();
                                    GraphFactory factory = gc.getGraphModel().factory();
                                    try {
                                        //get comma separated node fields (label, modularity)
                                        URL nodeURL = null;
                                        try {
                                            nodeURL = new URL(NODE_URL);
                                        } catch (IOException e1) {
                                            System.out.println(e1);
                                        }
                                        HttpURLConnection con = (HttpURLConnection) nodeURL.openConnection();
                                        con.setRequestMethod("GET");
                                        con.setRequestProperty("User-Agent", USER_AGENT);
                                        int responseCode = con.getResponseCode();
                                        String[] edgeArray = null;
                                        //if (responseCode == HttpURLConnection.HTTP_OK) {
                                            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                            //BufferedReader in = new BufferedReader(new FileReader("oneip_edges.txt"));
                                            String inputLine;
                                            StringBuffer response = new StringBuffer();
                                            while ((inputLine = in.readLine()) != null) {
                                                response.append(inputLine);
                                            }
                                            in.close();
                                            String actionString = response.toString();
                                            edgeArray = actionString.split("█"); //change this to handle edges
                                            graphNode.setSize(10f);
                                            float[] position3d = VizController.getInstance().getGraphIO().getMousePosition3d();
                                            float centerX = position3d[0];
                                            float centerY = position3d[1];
                                            float radius = graphNode.size();
                                            float xPosition = centerX;
                                            float yPosition = centerY + 10f*radius;                                    
                                            int angleIncrement = 360/(edgeArray.length);
                                            int angle = angleIncrement;

                                            for (String edge : edgeArray) {
                                                String[] fieldArray = edge.split("\\|"); //0=target,1=timestamp,2=nodetype
                                                Node[] graphNodes = graph.getNodes().toArray();
                                                // targetNode is the nodes being appended, to point to the clicked Node
                                                boolean targetNodeExists = false;
                                                Node desiredNode = null;
                                                for (Node existingNode : graphNodes) {
                                                    if (existingNode.getLabel().equals(fieldArray[0])) {
                                                        targetNodeExists = true;
                                                        desiredNode = existingNode;
                                                    }
                                                }
                                                if (!targetNodeExists) {
                                                    Node addTargetNode = factory.newNode();
                                                    addTargetNode.setX(xPosition);
                                                    addTargetNode.setY(yPosition);
                                                    xPosition = centerX + (10f*radius * (float) Math.sin(Math.toRadians(angle)));
                                                    yPosition = centerY + (10f*radius * (float) Math.cos(Math.toRadians(angle)));
                                                    angle += angleIncrement;

                                                    addTargetNode.setSize(10f);
                                                    addTargetNode.setAttribute("label", fieldArray[0]);
                                                    addTargetNode.addTimestamp(Double.parseDouble(fieldArray[1]));
                                                    addTargetNode.setAttribute("nodetype", fieldArray[2]);
                                                    Color blueColor = new Color(0,191,255);
                                                    Color greenColor = new Color(128,255,0);
                                                    Color yellowColor = new Color(255,255,0);
                                                    Color orangeColor = new Color(255,102,0);
                                                    Color greyColor = new Color(192,192,192);
                                                    Color blackColor = new Color(0,0,0);
                                                    Color purpleColor = new Color(138,43,226);
                                                    Color redColor = new Color(220,20,60); //or 255,0,0
                                                    if (addTargetNode.getAttribute("nodetype").equals("ipaddress")){
                                                        addTargetNode.setColor(greenColor);
                                                        addTargetNode.setAlpha(.5f);
                                                    } else if (addTargetNode.getAttribute("nodetype").equals("username")){
                                                        addTargetNode.setColor(blueColor);
                                                        addTargetNode.setAlpha(.5f);
                                                    } else if (addTargetNode.getAttribute("nodetype").equals("useragent")){
                                                        addTargetNode.setColor(purpleColor);
                                                        addTargetNode.setAlpha(.5f);
                                                    } else if (addTargetNode.getAttribute("nodetype").equals("session_id")){
                                                        addTargetNode.setColor(greyColor);
                                                        addTargetNode.setAlpha(.8f);
                                                    } else {
                                                        addTargetNode.setColor(greyColor);
                                                        addTargetNode.setAlpha(.5f);
                                                    }

                                                    graph.addNode(addTargetNode);
                                                    Edge newEdge = factory.newEdge(addTargetNode,graphNode);
                                                    Color color = graphNode.getColor();
                                                    newEdge.setColor(color);
                                                    graph.addEdge(newEdge);
                                                } else {
                                                    if (graph.getEdge(desiredNode,graphNode)==null && 
                                                            graph.getEdge(graphNode,desiredNode)==null){
                                                        float existingX = desiredNode.x();
                                                        float existingY = desiredNode.y();
                                                        float midX = (existingX+centerX)/2f;
                                                        float midY = (existingY+centerY)/2f;
                                                        desiredNode.setX(midX);
                                                        desiredNode.setY(midY);
                                                        desiredNode.setColor(Color.red);
                                                        Edge newEdge = factory.newEdge(desiredNode,graphNode);
                                                        newEdge.setColor(Color.red);
                                                        graph.addEdge(newEdge);
                                                    }
                                                }
                                            }
                                        //}
                                    } catch(IOException ioe) {
                                        System.out.println("error");
                                    }
                                }
                            });
                        }
                    }
                    return menuItem;
                }
                
            }};
    }

    @Override
    public ToolUI getUI() {
        return ui;
    }

    @Override
    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }

    private static class AddNodesToolUI implements ToolUI {
        @Override
        public JPanel getPropertiesBar(Tool tool) {
            JPanel panel = new JPanel();

            //Add a checkbox in the property bar to run a layout algorithm
            final JCheckBox checkBox = new JCheckBox("Run ForceAtlas layout");
            checkBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (checkBox.isSelected()) {
                        //Run the ForceAtlas layout
                        //The layout doesn't stop by itself
                        ForceAtlasLayout layout = Lookup.getDefault().lookup(ForceAtlas.class).buildLayout();
                        layout.resetPropertiesValues();
                        layout.setAdjustSizes(true);
                        LayoutController layoutController = Lookup.getDefault().lookup(LayoutController.class);
                        layoutController.setLayout(layout);
                        layoutController.executeLayout();
                        
                    } else {
                        //Stop layout
                        LayoutController layoutController = Lookup.getDefault().lookup(LayoutController.class);
                        layoutController.stopLayout();
                    }
                }
            });
            panel.add(checkBox);
            return panel;
        }
        
        @Override
        public Icon getIcon() {
            return new ImageIcon(getClass().getResource("/org/gephi/plugins/example/tool/resources/plus.png"));
        }

        @Override
        public String getName() {
            return "Get Actions";
        }

        @Override
        public String getDescription() {
            return "Get actions";
        }

        @Override
        public int getPosition() {
            return 1000;
        }
    }
}
