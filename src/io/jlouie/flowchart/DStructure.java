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

import java.util.LinkedList;

/**
 *
 * @author Johnathan Louie
 */
public class DStructure {

    private StructureType type;
    private final LinkedList<DStructure> list = new LinkedList<>();
    private DStructure body1;
    private DStructure body2;
    private CodeBlock struct;

    public StructureType getType() {
        return type;
    }

    public void add(DStructure d) {
        list.add(d);
    }

    public LinkedList<DStructure> getSeq() {
        return list;
    }

    public int size() {
        return list.size();
    }

    public void setType(StructureType type) {
        this.type = type;
    }

    public CodeBlock getStruct() {
        return struct;
    }

    public void setStruct(CodeBlock struct) {
        this.struct = struct;
    }

    public void print(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("-");
        }
        switch (getType()) {
            case Pn:
                System.out.println("P" + size());
                break;
            case P1:
                System.out.println("P1 " + struct.getStatement());
                break;
            default:
                System.out.println(getType() + " " + struct.getCondition());
                break;
        }
        switch (getType()) {
            case P1:
                break;
            case Pn:
                for (DStructure d : list) {
                    d.print(level + 1);
                }
                break;
            case D1:
                if (body1 != null) {
                    body1.print(level + 1);
                } else {
                    for (int i = 0; i < level + 1; i++) {
                        System.out.print("-");
                    }
                    System.out.println("body 1 empty");
                }
                if (body2 != null) {
                    body2.print(level + 1);
                } else {
                    for (int i = 0; i < level + 1; i++) {
                        System.out.print("-");
                    }
                    System.out.println("body 2 empty");
                }
                break;
            default:
                if (body1 != null) {
                    body1.print(level + 1);
                } else {
                    for (int i = 0; i < level + 1; i++) {
                        System.out.print("-");
                    }
                    System.out.println("body empty");
                }
                break;
        }
    }

    public DStructure getBody1() {
        return body1;
    }

    public void setBody1(DStructure body1) {
        this.body1 = body1;
    }

    public DStructure getBody2() {
        return body2;
    }

    public void setBody2(DStructure body2) {
        this.body2 = body2;
    }

    public ControlFlowNode flow(ControlFlowNode out) {
        switch (type) {
            case P0:
                return flowP0(out);
            case P1:
                return flowP1(out);
            case Pn:
                return flowPn(out);
            case D0:
                return flowD0(out);
            case D1:
                return flowD1(out);
            case D2:
                if (struct.getType() == BlockType.WHILE) {
                    return flowD2While(out);
                } else if (struct.getType() == BlockType.FOR) {
                    return flowD2For(out);
                }
            case D3:
                return flowD3(out);
            default:
                throw new RuntimeException();
        }
    }

    public void condense() {
        if (type == StructureType.Pn) {
            boolean found = false;
            int j = 0;
            for (int i = 0; i <= list.size(); i++) {
                if (found) {
                    if (i == list.size() || list.get(i).type != StructureType.P1) {
                        if (i - j > 1) {
                            DStructure d = new DStructure();
                            CodeBlock l = new CodeBlock();
                            d.type = StructureType.P1;
                            String x = "";
                            d.setStruct(l);
                            for (int k = j; k < i; k++) {
                                x += l.getStatement() + "\r\n" + list.get(k).getStruct().getStatement();
                                list.set(k, null);
                            }
                            l.makeStatement(x);
                            list.set(j, d);
                        }
                        found = false;
                    }
                } else if (i < list.size()) {
                    if (list.get(i).type == StructureType.P1) {
                        found = true;
                        j = i;
                    }
                }
            }
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i) == null) {
                    list.remove(i);
                }
            }
        } else if (type == StructureType.D1) {
            body1.condense();
            body2.condense();
        } else if (type != StructureType.P1) {
            body1.condense();
        }
    }

    private ControlFlowNode flowD2While(ControlFlowNode out) {
        ControlFlowNode condition = new ControlFlowNode();
        ControlFlowNode body = body1.flow(condition);
        condition.setType(FlowType.PREDICATE);
        condition.setContent(struct.getCondition());
        condition.setFalseOut(out);
        condition.setTrueOut(body);
        return condition;
    }

    private ControlFlowNode flowD2For(ControlFlowNode out) {
        ControlFlowNode inc = new ControlFlowNode();
        ControlFlowNode condition = new ControlFlowNode();
        ControlFlowNode init = new ControlFlowNode();
        ControlFlowNode body = body1.flow(inc);
        // init
        inc.setType(FlowType.PROCEDURE);
        inc.setContent(struct.getIncrement());
        inc.setOut(condition);
        // increment
        init.setType(FlowType.PROCEDURE);
        init.setContent(struct.getInit());
        init.setOut(condition);
        // condition
        condition.setType(FlowType.PREDICATE);
        condition.setContent(struct.getCondition());
        condition.setFalseOut(out);
        condition.setTrueOut(body);
        return init;
    }

    private ControlFlowNode flowD3(ControlFlowNode out) {
        ControlFlowNode condition = new ControlFlowNode();
        ControlFlowNode body = body1.flow(condition);
        condition.setType(FlowType.PREDICATE);
        condition.setContent(struct.getCondition());
        condition.setFalseOut(out);
        condition.setTrueOut(body);
        return body;
    }

    private ControlFlowNode flowP0(ControlFlowNode out) {
        return out;
    }

    private ControlFlowNode flowP1(ControlFlowNode out) {
        ControlFlowNode c = new ControlFlowNode();
        c.setType(FlowType.PROCEDURE);
        c.setContent(struct.getStatement());
        c.setOut(out);
        return c;
    }

    private ControlFlowNode flowPn(ControlFlowNode out) {
        int i = list.size() - 1;
        do {
            out = list.get(i).flow(out);
            i--;
        } while (i >= 0);
        return out;
    }

    private ControlFlowNode flowD1(ControlFlowNode out) {
        ControlFlowNode ifBody = body1.flow(out);
        ControlFlowNode elseBody = body2.flow(out);
        ControlFlowNode condition = new ControlFlowNode();
        condition.setType(FlowType.PREDICATE);
        condition.setContent(struct.getCondition());
        condition.setFalseOut(elseBody);
        condition.setTrueOut(ifBody);
        return condition;
    }

    private ControlFlowNode flowD0(ControlFlowNode out) {
        ControlFlowNode body = body1.flow(out);
        ControlFlowNode condition = new ControlFlowNode();
        condition.setType(FlowType.PREDICATE);
        condition.setContent(struct.getCondition());
        condition.setFalseOut(out);
        condition.setTrueOut(body);
        return condition;
    }

}
