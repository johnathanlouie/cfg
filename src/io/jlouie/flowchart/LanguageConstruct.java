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
public class LanguageConstruct {

    private Type type;
    private String condition;
    private String body1;
    private String body2;
    private String init;
    private String increment;
    private String statement;
    private int end;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getBody1() {
        return body1;
    }

    public void setBody1(String body1) {
        this.body1 = body1;
    }

    public String getBody2() {
        return body2;
    }

    public void setBody2(String body2) {
        this.body2 = body2;
    }

    public String getInit() {
        return init;
    }

    public void setInit(String init) {
        this.init = init;
    }

    public String getIncrement() {
        return increment;
    }

    public void setIncrement(String increment) {
        this.increment = increment;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public DStructure getStructure() {
        DStructure struct = new DStructure();
        struct.setStruct(this);
        DStructure substruct;
        switch (type) {
            case FOR:
                struct.setType(StructType.D2);
                substruct = new Code(getBody1()).getStructure();
                struct.setBody1(substruct);
                break;
            case WHILE:
                struct.setType(StructType.D2);
                substruct = new Code(getBody1()).getStructure();
                struct.setBody1(substruct);
                break;
            case DOWHILE:
                struct.setType(StructType.D3);
                substruct = new Code(getBody1()).getStructure();
                struct.setBody1(substruct);
                break;
            case STATEMENT:
                struct.setType(StructType.P1);
                break;
            case IF:
                struct.setType(StructType.D0);
                substruct = new Code(getBody1()).getStructure();
                struct.setBody1(substruct);
                break;
            case IFELSE:
                struct.setType(StructType.D1);
                substruct = new Code(getBody1()).getStructure();
                struct.setBody1(substruct);
                substruct = new Code(getBody2()).getStructure();
                struct.setBody2(substruct);
                break;
            default:
                struct.setType(StructType.Empty);
        }
        return struct;
    }

}
