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
            String content = new Scanner(new File(args[0])).useDelimiter("\\Z").next();
            content = Preprocess.removeComments(Preprocess.linuxEnding(content));
            Code code = new Code(content);
            DStructure struct = code.getStructure();
            if (struct == null) {
                System.out.println("Error in parsing.");
                return;
            }
            struct.condense();
            struct.print(0);
            ControlFlowNode cfn = struct.flow(null);
            labelAndCountNodes(cfn);
//            cfn = combineP1_2(cfn);
            PrintWriter writer = new PrintWriter(args[0] + ".graphml", "UTF-8");
            writer.println(xmlgraph(cfn));
            writer.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Error reading file.");
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Error writing file.");
        }
    }

    private static ControlFlowNode combineP1(ControlFlowNode[] list) {
        for (ControlFlowNode c : list) {
            if (c.getType() == FlowType.PROCEDURE && c.getOut() != null && c.getOut().getType() == FlowType.PROCEDURE) {
                ControlFlowNode x = new ControlFlowNode();
                x.setType(FlowType.PROCEDURE);
                x.setOut(c.getOut().getOut());
                x.setContent(c.getContent() + "\n" + c.getOut().getContent());
                for (ControlFlowNode c2 : list) {
                    if (c2.getOut() == c) {
                        c2.setOut(x);
                    }
                    if (c2.getTrueOut() == c) {
                        c2.setTrueOut(x);
                    }
                    if (c2.getFalseOut() == c) {
                        c2.setFalseOut(x);
                    }
                }
                if (list[0] == c) {
                    return x;
                } else {
                    return list[0];
                }
            }
        }
        return list[0];
    }

    private static ControlFlowNode combineP1_2(ControlFlowNode origin) {
        boolean change;
        ControlFlowNode[] cfNodes = listFromOrigin(origin);
        do {
            int size = cfNodes.length;
            origin = combineP1(cfNodes);
            cfNodes = listFromOrigin(origin);
            change = size > cfNodes.length;
        } while (change);
        return origin;
    }

    public static String node(String label, String id) {
        return "    <node id=\"" + id + "\">\r\n"
                + "      <data key=\"d5\"/>\r\n"
                + "      <data key=\"d6\">\r\n"
                + "        <y:ShapeNode>\r\n"
                + "          <y:Geometry height=\"30.0\" width=\"30.0\" x=\"277.0\" y=\"119.0\"/>\r\n"
                + "          <y:Fill color=\"#FFCC00\" transparent=\"false\"/>\r\n"
                + "          <y:BorderStyle color=\"#000000\" raised=\"false\" type=\"line\" width=\"1.0\"/>\r\n"
                + "          <y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" height=\"18.701171875\" horizontalTextPosition=\"center\" iconTextGap=\"4\" modelName=\"custom\" textColor=\"#000000\" verticalTextPosition=\"bottom\" visible=\"true\" width=\"13.33984375\" x=\"8.330078125\" xml:space=\"preserve\" y=\"5.6494140625\">" + label + "<y:LabelModel><y:SmartNodeLabelModel distance=\"4.0\"/></y:LabelModel><y:ModelParameter><y:SmartNodeLabelModelParameter labelRatioX=\"0.0\" labelRatioY=\"0.0\" nodeRatioX=\"0.0\" nodeRatioY=\"0.0\" offsetX=\"0.0\" offsetY=\"0.0\" upX=\"0.0\" upY=\"-1.0\"/></y:ModelParameter></y:NodeLabel>\r\n"
                + "          <y:Shape type=\"ellipse\"/>\r\n"
                + "        </y:ShapeNode>\r\n"
                + "      </data>\r\n"
                + "    </node>\r\n";
    }

    public static String edge(String label, String id, String src, String dst) {
        return "    <edge id=\"" + id + "\" source=\"" + src + "\" target=\"" + dst + "\">\n"
                + "      <data key=\"d9\"/>\n"
                + "      <data key=\"d10\">\n"
                + "        <y:PolyLineEdge>\n"
                + "          <y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>\n"
                + "          <y:LineStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>\n"
                + "          <y:Arrows source=\"none\" target=\"standard\"/>\n"
                + "          <y:EdgeLabel alignment=\"center\" configuration=\"AutoFlippingLabel\" distance=\"2.0\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" hasLineColor=\"false\" height=\"18.701171875\" horizontalTextPosition=\"center\" iconTextGap=\"4\" modelName=\"centered\" modelPosition=\"center\" preferredPlacement=\"anywhere\" ratio=\"0.5\" textColor=\"#000000\" verticalTextPosition=\"bottom\" visible=\"true\" width=\"84.05078125\" x=\"104.98489379882812\" xml:space=\"preserve\" y=\"-3.90576171875\">" + label + "<y:PreferredPlacementDescriptor angle=\"0.0\" angleOffsetOnRightSide=\"0\" angleReference=\"absolute\" angleRotationOnRightSide=\"co\" distance=\"-1.0\" frozen=\"true\" placement=\"anywhere\" side=\"anywhere\" sideReference=\"relative_to_edge_flow\"/></y:EdgeLabel>\n"
                + "          <y:BendStyle smoothed=\"false\"/>\n"
                + "        </y:PolyLineEdge>\n"
                + "      </data>\n"
                + "    </edge>\n";
    }

    public static String safeLabel(String unsafe) {
        unsafe = unsafe.replaceAll("&", "&amp;");
        unsafe = unsafe.replaceAll("<", "&lt;");
        unsafe = unsafe.replaceAll(">", "&gt;");
        unsafe = unsafe.replaceAll("\"", "&quot;");
        unsafe = unsafe.replaceAll("'", "&apos;");
        return unsafe;
    }

    private static String xmlgraph(ControlFlowNode origin) {
        StringBuilder xmlBuilder = new StringBuilder();
        StringBuilder edgeBuilder = new StringBuilder();
        int edgeID = 0;
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n"
                + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:java=\"http://www.yworks.com/xml/yfiles-common/1.0/java\" xmlns:sys=\"http://www.yworks.com/xml/yfiles-common/markup/primitives/2.0\" xmlns:x=\"http://www.yworks.com/xml/yfiles-common/markup/2.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:yed=\"http://www.yworks.com/xml/yed/3\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\">\r\n"
                + "  <!--Created by yEd 3.18.1-->\r\n"
                + "  <key attr.name=\"Description\" attr.type=\"string\" for=\"graph\" id=\"d0\"/>\r\n"
                + "  <key for=\"port\" id=\"d1\" yfiles.type=\"portgraphics\"/>\r\n"
                + "  <key for=\"port\" id=\"d2\" yfiles.type=\"portgeometry\"/>\r\n"
                + "  <key for=\"port\" id=\"d3\" yfiles.type=\"portuserdata\"/>\r\n"
                + "  <key attr.name=\"url\" attr.type=\"string\" for=\"node\" id=\"d4\"/>\r\n"
                + "  <key attr.name=\"description\" attr.type=\"string\" for=\"node\" id=\"d5\"/>\r\n"
                + "  <key for=\"node\" id=\"d6\" yfiles.type=\"nodegraphics\"/>\r\n"
                + "  <key for=\"graphml\" id=\"d7\" yfiles.type=\"resources\"/>\r\n"
                + "  <key attr.name=\"url\" attr.type=\"string\" for=\"edge\" id=\"d8\"/>\r\n"
                + "  <key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d9\"/>\r\n"
                + "  <key for=\"edge\" id=\"d10\" yfiles.type=\"edgegraphics\"/>\r\n"
                + "  <graph edgedefault=\"directed\" id=\"G\">\r\n"
                + "    <data key=\"d0\"/>\r\n");
        ControlFlowNode[] cfNodes = listFromOrigin(origin);
        for (ControlFlowNode i : cfNodes) {
            xmlBuilder.append(node(safeLabel(i.getContent()), i.getXmlID()));
            ControlFlowNode trueNode = i.getTrueOut();
            ControlFlowNode falseNode = i.getFalseOut();
            ControlFlowNode outNode = i.getOut();
            if (trueNode != null && i.getType() == FlowType.PREDICATE) {
                edgeBuilder.append(edge("TRUE", "e" + edgeID++, i.getXmlID(), trueNode.getXmlID()));
            }
            if (falseNode != null && i.getType() == FlowType.PREDICATE) {
                edgeBuilder.append(edge("FALSE", "e" + edgeID++, i.getXmlID(), falseNode.getXmlID()));
            }
            if (outNode != null && i.getType() == FlowType.PROCEDURE) {
                edgeBuilder.append(edge("", "e" + edgeID++, i.getXmlID(), outNode.getXmlID()));
            }
        }
        xmlBuilder.append(edgeBuilder);
        xmlBuilder.append("  </graph>\r\n"
                + "  <data key=\"d7\">\r\n"
                + "    <y:Resources/>\r\n"
                + "  </data>\r\n"
                + "</graphml>\r\n");
        return xmlBuilder.toString();
    }

    public static void labelAndCountNodes(ControlFlowNode origin) {
        ControlFlowNode.resetNodeID();
        origin.enumerate();
        origin.unvisit();
    }

    public static ControlFlowNode[] listFromOrigin(ControlFlowNode origin) {
        labelAndCountNodes(origin);
        ControlFlowNode[] cfNodes = new ControlFlowNode[ControlFlowNode.getNodeCount()];
        origin.toList(cfNodes);
        origin.unvisit();
        return cfNodes;
    }

}
