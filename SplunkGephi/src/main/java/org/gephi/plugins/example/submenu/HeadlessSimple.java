/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.plugins.example.submenu;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.layout.api.LayoutController;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;


/**
 * This demo shows several actions done with the toolkit, aiming to do a
 * complete chain, from data import to results.
 * <p>
 * This demo shows the following steps:
 * <ul><li>Create a project and a workspace, it is mandatory.</li>
 * <li>Import the <code>ipusers.gml</code> graph file in an import
 * container.</li>
 * <li>Append the container to the main graph structure.</li>
 * <li>Filter the graph, using <code>DegreeFilter</code>.</li>
 * <li>Run layout manually.</li>
 * <li>Compute graph distance metrics.</li>
 * <li>Rank color by degree values.</li>
 * <li>Rank size by centrality values.</li>
 * <li>Configure preview to display labels and mutual edges differently.</li>
 * <li>Export graph as PDF.</li></ul>
 *
 * @author Mathieu Bastian
 */
public class HeadlessSimple {

    public void script() {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get models and controllers for this new workspace - will be useful later
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();

        //Import file       
        Container container;
        try {
            File file = new File("useCase2.gexf");///Users/ptow/Downloads/gephi-0.9.1/modules/application
             //System.out.println(file.getAbsolutePath());
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);   //Force DIRECTED
        } catch (Exception ex) {
            //ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //Run YifanHuLayout for 100 passes - The layout always takes the current visible view
        /*YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(200f);

        layout.initAlgo();
        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        layout.endAlgo(); */

        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = gc.getGraphModel().getGraph();
        Node[] graphNodes = graph.getNodes().toArray();
        Edge[] graphEdges = graph.getEdges().toArray();
        int colorCounter = 0;
        for (Node n : graphNodes) {
            Color blueColor = new Color(0,191,255);
            Color greenColor = new Color(128,255,0);
            Color yellowColor = new Color(255,255,0);
            Color orangeColor = new Color(255,102,0);
            Color greyColor = new Color(192,192,192);
            Color redColor = new Color(220,20,60); //or 255,0,0
            /*if (colorCounter % 4 == 0) {
                n.setColor(blueColor);
            } else if (colorCounter % 4 == 1) {
                n.setColor(greenColor);
            } else if (colorCounter % 4 == 2) {
                n.setColor(yellowColor);
            } else {
                n.setColor(greyColor);
            }
            colorCounter += 1;*/
            if (n.getAttribute("nodetype").equals("ipaddress")){
                n.setColor(greenColor);
                n.setAlpha(.5f);
            } else if (n.getAttribute("nodetype").equals("username")){
                n.setColor(blueColor);
                n.setAlpha(.5f);
            } else if (n.getAttribute("nodetype").equals("useragent")){
                n.setColor(yellowColor);
                n.setAlpha(.5f);
            } else if (n.getAttribute("nodetype").equals("session_id")){
                n.setColor(orangeColor);
                n.setAlpha(.5f);
            } else {
                n.setColor(greyColor);
                n.setAlpha(.5f);
            }

            Edge[] nodeEdges = graph.getEdges(n).toArray();
            double degree = Math.cbrt(graph.getDegree(n));
            
            if (degree>1) {
                n.setColor(redColor);
                n.setAlpha(.5f);
            }
            
            float nodeSize = (float) degree*30;
            n.setSize(nodeSize);
            
            if (nodeEdges.length > 0) {
                for (Edge nodeEdge : nodeEdges) {
                    Color targetColor = nodeEdge.getTarget().getColor();
                    nodeEdge.setColor(targetColor);
                }
            }
        }
        
        for (Edge graphEdge : graphEdges) {
            Node sourceNode = graphEdge.getSource();
            Node targetNode = graphEdge.getTarget();
            if (targetNode.getAttribute("nodetype").equals("ipaddress")) {
                for (double timestamp : sourceNode.getTimestamps()) {
                    targetNode.addTimestamp(timestamp);
                }
            }
        }

        //Preview
        model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
        model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, model.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));

    }
    public void script2() {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get models and controllers for this new workspace - will be useful later
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();

        //Import file       
        Container container;
        try {
            File file = new File("normal.gexf");///Users/ptow/Downloads/gephi-0.9.1/modules/application
             //System.out.println(file.getAbsolutePath());
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);   //Force DIRECTED
        } catch (Exception ex) {
            //ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //Run YifanHuLayout for 100 passes - The layout always takes the current visible view
        /*YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(200f);

        layout.initAlgo();
        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        layout.endAlgo(); */

        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = gc.getGraphModel().getGraph();
        Node[] graphNodes = graph.getNodes().toArray();
        Edge[] graphEdges = graph.getEdges().toArray();
        int colorCounter = 0;
        for (Node n : graphNodes) {
            Color blueColor = new Color(0,191,255);
            Color greenColor = new Color(128,255,0);
            Color yellowColor = new Color(255,255,0);
            Color orangeColor = new Color(255,102,0);
            Color greyColor = new Color(192,192,192);
            Color redColor = new Color(220,20,60); //or 255,0,0
            /*if (colorCounter % 4 == 0) {
                n.setColor(blueColor);
            } else if (colorCounter % 4 == 1) {
                n.setColor(greenColor);
            } else if (colorCounter % 4 == 2) {
                n.setColor(yellowColor);
            } else {
                n.setColor(greyColor);
            }
            colorCounter += 1;*/
            if (n.getAttribute("nodetype").equals("ipaddress")){
                n.setColor(greenColor);
                n.setAlpha(.5f);
            } else if (n.getAttribute("nodetype").equals("username")){
                n.setColor(blueColor);
                n.setAlpha(.5f);
            } else if (n.getAttribute("nodetype").equals("useragent")){
                n.setColor(yellowColor);
                n.setAlpha(.5f);
            } else if (n.getAttribute("nodetype").equals("session_id")){
                n.setColor(orangeColor);
                n.setAlpha(.5f);
            } else {
                n.setColor(greyColor);
                n.setAlpha(.5f);
            }

            Edge[] nodeEdges = graph.getEdges(n).toArray();
            double degree = Math.cbrt(graph.getDegree(n));
            
            float nodeSize = (float) degree*30;
            n.setSize(nodeSize);
            
            if (nodeEdges.length > 0) {
                for (Edge nodeEdge : nodeEdges) {
                    Color targetColor = nodeEdge.getTarget().getColor();
                    nodeEdge.setColor(targetColor);
                }
            }
        }
        
        for (Edge graphEdge : graphEdges) {
            Node sourceNode = graphEdge.getSource();
            Node targetNode = graphEdge.getTarget();
            if (targetNode.getAttribute("nodetype").equals("ipaddress")) {
                for (double timestamp : sourceNode.getTimestamps()) {
                    targetNode.addTimestamp(timestamp);
                }
            }
        }

        //Preview
        model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
        model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, model.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));

    }
}

