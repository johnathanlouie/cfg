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

/**
 *
 * @author Johnathan Louie
 */
public class ControlFlowNode {

    private FlowType type;
    private ControlFlowNode trueOut = null;
    private ControlFlowNode falseOut = null;
    private ControlFlowNode out = null;
    private String content;
    private boolean xmlVisited = false;
    private String xmlID = null;

    public FlowType getType() {
        return type;
    }

    public void setType(FlowType type) {
        this.type = type;
    }

    public ControlFlowNode getTrueOut() {
        return trueOut;
    }

    public void setTrueOut(ControlFlowNode trueOut) {
        this.trueOut = trueOut;
    }

    public ControlFlowNode getFalseOut() {
        return falseOut;
    }

    public void setFalseOut(ControlFlowNode falseOut) {
        this.falseOut = falseOut;
    }

    public ControlFlowNode getOut() {
        return out;
    }

    public void setOut(ControlFlowNode out) {
        this.out = out;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void xml(String inID, String edgeLabel) {
        //String xmlString = "";
        if (!xmlVisited) {
            xmlID = "n" + n++;
            //String thisXML = node(content, xmlID);
            xmlBuilder.append(node(content, xmlID));
            //String nextXML = "";
            if (out != null) {
                //nextXML += out.xml(xmlID, "");
                out.xml(xmlID, "");
            }
            if (trueOut != null) {
//                nextXML += trueOut.xml(xmlID, "TRUE");
                trueOut.xml(xmlID, "TRUE");
            }
            if (falseOut != null) {
//                nextXML += falseOut.xml(xmlID, "FALSE");
                falseOut.xml(xmlID, "FALSE");
            }
//            xmlString = thisXML + nextXML;
        }
        if (inID != null) {
//            String edgeFromPrevious = edge(edgeLabel, "e" + e++, inID, xmlID);
            xmlBuilder.append(edge(edgeLabel, "e" + e++, inID, xmlID));
//            xmlString += edgeFromPrevious;
        }
//        return xmlString;
    }

    public boolean isXmlVisited() {
        return xmlVisited;
    }

    public void setXmlVisited(boolean xmlVisited) {
        this.xmlVisited = xmlVisited;
    }

    public String getXmlID() {
        return xmlID;
    }

    public void setXmlID(String xmlID) {
        this.xmlID = xmlID;
    }

}
