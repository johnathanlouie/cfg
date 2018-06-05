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

    public static int getNodeCount() {
        return nodeID;
    }

    private FlowType type = null;
    private ControlFlowNode trueOut = null;
    private ControlFlowNode falseOut = null;
    private ControlFlowNode out = null;
    private String content;
    private boolean xmlVisited = false;
    private int id;

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
        if (type == null || type != FlowType.PREDICATE) {
            throw new RuntimeException("node type is null set true");
        }
        this.trueOut = trueOut;
    }

    public ControlFlowNode getFalseOut() {
        return falseOut;
    }

    public void setFalseOut(ControlFlowNode falseOut) {
        if (type == null || type != FlowType.PREDICATE) {
            throw new RuntimeException("node type is null set false");
        }
        this.falseOut = falseOut;
    }

    public ControlFlowNode getOut() {
        return out;
    }

    public void setOut(ControlFlowNode out) {
        if (type == null || type != FlowType.PROCEDURE) {
            throw new RuntimeException("node type is null set out");
        }
        this.out = out;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isXmlVisited() {
        return xmlVisited;
    }

    public void setXmlVisited(boolean xmlVisited) {
        this.xmlVisited = xmlVisited;
    }

    public String getXmlID() {
        return "n" + id;
    }

    private static int nodeID = 0;

    public void unvisit() {
        if (xmlVisited) {
            xmlVisited = false;
            if (out != null) {
                out.unvisit();
            }
            if (trueOut != null) {
                trueOut.unvisit();
            }
            if (falseOut != null) {
                falseOut.unvisit();
            }
        }
    }

    public void toList(ControlFlowNode[] list) {
        if (!xmlVisited) {
            xmlVisited = true;
            list[id] = this;
            if (out != null) {
                out.toList(list);
            }
            if (trueOut != null) {
                trueOut.toList(list);
            }
            if (falseOut != null) {
                falseOut.toList(list);
            }
        }
    }

    public void enumerate() {
        if (!xmlVisited) {
            xmlVisited = true;
            id = nodeID++;
            if (out != null) {
                out.enumerate();
            }
            if (trueOut != null) {
                trueOut.enumerate();
            }
            if (falseOut != null) {
                falseOut.enumerate();
            }
        }
    }

}
