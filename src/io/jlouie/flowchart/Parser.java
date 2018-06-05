/*
 * Copyright (C) 2018 Johnathan Louie
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.jlouie.flowchart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.Stack;

/**
 *
 * @author Johnathan Louie
 */
public class Parser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            System.out.println(args[0]);
            System.out.println(args[1]);
            System.out.println();
            String content = new Scanner(new File(args[0])).useDelimiter("\\Z").next();
//            System.out.println(content);
//            System.out.println();
            content = Preprocess.removeComments(Preprocess.linuxEnding(content));
//            System.out.println(content);
            Code code = new Code(content);
            DStructure struct = code.getStructure();
            if (struct == null) {
                System.out.println("Error in parsing.");
                return;
            }
            //struct.print(0);
            ControlFlowNode cfn = struct.flow(null);
            PrintWriter writer = new PrintWriter(args[1], "UTF-8");
            cfn.xml(null, null);
            writer.println(xmlgraph(cfn.getXML()));
            writer.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Error reading file.");
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Error writing file.");
        }
    }

    public static String node(String label, String id) {
        return "<node id=\"" + id + "\"><data key=\"d5\"/><data key=\"d6\"><y:ShapeNode><y:Geometry height=\"30.0\" width=\"30.0\" x=\"277.0\" y=\"253.0\"/><y:Fill color=\"#FFCC00\" transparent=\"false\"/><y:BorderStyle color=\"#000000\" raised=\"false\" type=\"line\" width=\"1.0\"/><y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" height=\"18.701171875\" horizontalTextPosition=\"center\" iconTextGap=\"4\" modelName=\"custom\" textColor=\"#000000\" verticalTextPosition=\"bottom\" visible=\"true\" width=\"40.703125\" x=\"-5.3515625\" xml:space=\"preserve\" y=\"5.6494140625\">" + label + "<y:LabelModel><y:SmartNodeLabelModel distance=\"4.0\"/></y:LabelModel><y:ModelParameter><y:SmartNodeLabelModelParameter labelRatioX=\"0.0\" labelRatioY=\"0.0\" nodeRatioX=\"0.0\" nodeRatioY=\"0.0\" offsetX=\"0.0\" offsetY=\"0.0\" upX=\"0.0\" upY=\"-1.0\"/></y:ModelParameter></y:NodeLabel><y:Shape type=\"ellipse\"/></y:ShapeNode></data></node>";
    }

    public static String edge(String label, String id, String src, String dst) {
        return "<edge id=\"" + id + "\" source=\"" + src + "\" target=\"" + dst + "\"><data key=\"d9\"/><data key=\"d10\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\"#000000\" type=\"line\" width=\"1.0\"/><y:Arrows source=\"none\" target=\"standard\"/><y:EdgeLabel alignment=\"center\" configuration=\"AutoFlippingLabel\" distance=\"2.0\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" height=\"18.701171875\" horizontalTextPosition=\"center\" iconTextGap=\"4\" modelName=\"custom\" preferredPlacement=\"anywhere\" ratio=\"0.5\" textColor=\"#000000\" verticalTextPosition=\"bottom\" visible=\"true\" width=\"50.69921875\" x=\"16.76238631931119\" xml:space=\"preserve\" y=\"75.18780899860948\">" + label + "<y:LabelModel><y:SmartEdgeLabelModel autoRotationEnabled=\"false\" defaultAngle=\"0.0\" defaultDistance=\"10.0\"/></y:LabelModel><y:ModelParameter><y:SmartEdgeLabelModelParameter angle=\"0.0\" distance=\"30.0\" distanceToCenter=\"true\" position=\"right\" ratio=\"0.5\" segment=\"0\"/></y:ModelParameter><y:PreferredPlacementDescriptor angle=\"0.0\" angleOffsetOnRightSide=\"0\" angleReference=\"absolute\" angleRotationOnRightSide=\"co\" distance=\"-1.0\" frozen=\"true\" placement=\"anywhere\" side=\"anywhere\" sideReference=\"relative_to_edge_flow\"/></y:EdgeLabel><y:BendStyle smoothed=\"false\"/></y:PolyLineEdge></data></edge>";
    }

    private static boolean notAllVisited(ControlFlowNode i) {
        if (i.getOut() != null && !i.getOut().isXmlVisited()) {
            return true;
        }
        if (i.getTrueOut() != null && !i.getTrueOut().isXmlVisited()) {
            return true;
        }
        if (i.getFalseOut() != null && !i.getFalseOut().isXmlVisited()) {
            return true;
        }
        return false;
    }

    private static String xmlgraph(ControlFlowNode origin) {
        StringBuilder xmlBuilder = new StringBuilder();
        int nodeID = 1;
        int edgeID = 0;
        Stack<ControlFlowNode> toExplore = new Stack<>();
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:java=\"http://www.yworks.com/xml/yfiles-common/1.0/java\" xmlns:sys=\"http://www.yworks.com/xml/yfiles-common/markup/primitives/2.0\" xmlns:x=\"http://www.yworks.com/xml/yfiles-common/markup/2.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:yed=\"http://www.yworks.com/xml/yed/3\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\"><!--Created by yEd 3.18.1--><key attr.name=\"Description\" attr.type=\"string\" for=\"graph\" id=\"d0\"/><key for=\"port\" id=\"d1\" yfiles.type=\"portgraphics\"/><key for=\"port\" id=\"d2\" yfiles.type=\"portgeometry\"/><key for=\"port\" id=\"d3\" yfiles.type=\"portuserdata\"/><key attr.name=\"url\" attr.type=\"string\" for=\"node\" id=\"d4\"/><key attr.name=\"description\" attr.type=\"string\" for=\"node\" id=\"d5\"/><key for=\"node\" id=\"d6\" yfiles.type=\"nodegraphics\"/><key for=\"graphml\" id=\"d7\" yfiles.type=\"resources\"/><key attr.name=\"url\" attr.type=\"string\" for=\"edge\" id=\"d8\"/><key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d9\"/><key for=\"edge\" id=\"d10\" yfiles.type=\"edgegraphics\"/><graph edgedefault=\"directed\" id=\"G\"><data key=\"d0\"/>");
        ControlFlowNode i = origin;
        origin.setXmlID("n0");

        if (!i.isXmlVisited()) {
            i.setXmlVisited(true);
            xmlBuilder.append(node(i.getContent(), i.getXmlID()));
            if (i.getType() == FlowType.PROCEDURE) {
                ControlFlowNode next = i.getOut();
                if (next.getXmlID() == null) {
                    next.setXmlID("n" + nodeID++);
                }
                xmlBuilder.append(edge(next.getContent(), "e" + edgeID++, i.getXmlID(), next.getXmlID()));
            } else {
                ControlFlowNode trueNode = i.getTrueOut();
                if (trueNode.getXmlID() == null) {
                    trueNode.setXmlID("n" + nodeID++);
                }
                xmlBuilder.append(edge(trueNode.getContent(), "e" + edgeID++, i.getXmlID(), trueNode.getXmlID()));
                ControlFlowNode falseNode = i.getFalseOut();
                if (falseNode.getXmlID() == null) {
                    falseNode.setXmlID("n" + nodeID++);
                }
                xmlBuilder.append(edge(falseNode.getContent(), "e" + edgeID++, i.getXmlID(), falseNode.getXmlID()));
                toExplore.add(i);
            }
        } else {
            i = toExplore.pop().getFalseOut();
        }

        xmlBuilder.append("</graph><data key=\"d7\"><y:Resources/></data></graphml>");
        return xmlBuilder.toString();
    }

}
