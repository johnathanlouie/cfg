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
public class CodeBlock {

    private BlockType type;
    private String condition;
    private LinkedList<CodeBlock> body;
    private LinkedList<CodeBlock> body2;
    private String init;
    private String increment;
    private String statement;
    private String code;
    private int endIndex;

    public void makeStatement(String content) {
        type = BlockType.STATEMENT;
        statement = content;
    }

    public void makeFunction(String body) {
        type = BlockType.FUNCTION;
        this.body = parse(body);
    }

    public void makeIf(String condition, String body) {
        type = BlockType.IF;
        this.condition = condition;
        this.body = parse(body);
    }

    public void makeIfElse(String condition, String ifBody, String elseBody) {
        type = BlockType.IFELSE;
        this.condition = condition;
        body = parse(ifBody);
        body2 = parse(elseBody);
    }

    public void makeFor(String condition, String body, String init, String increment) {
        type = BlockType.FOR;
        this.init = init;
        this.condition = condition;
        this.increment = increment;
        this.body = parse(body);
    }

    public void makeWhile(String condition, String body) {
        type = BlockType.WHILE;
        this.condition = condition;
        this.body = parse(body);
    }

    public void makeDoWhile(String condition, String body) {
        type = BlockType.DOWHILE;
        this.condition = condition;
        this.body = parse(body);
    }

    private LinkedList<CodeBlock> parse(String body) {
        code = body;
        LinkedList<CodeBlock> sequence = new LinkedList<>();
        for (int i = 0; i < code.length(); i++) {
            i = findNonwhitespace(i);
            if (i == -1) {
                return sequence;
            }
            CodeBlock block;
            switch (whichKeyword(i)) {
                case "if":
                    block = parseIf(i);
                    break;
                case "while":
                    block = parseWhile(i);
                    break;
                case "do":
                    block = parseDoWhile(i);
                    break;
                case "for":
                    block = parseFor(i);
                    break;
                case "else":
                    throw new RuntimeException("Unexpected else keyword.");
                default:
                    block = parseStatement(i);
            }
            i = block.endIndex;
            sequence.add(block);
        }
        return sequence;
    }

    private CodeBlock parseStatement(int i) {
        CodeBlock block = new CodeBlock();
        int j = findSemicolon(i);
        if (j == -1) {
            throw new RuntimeException();
        }
        block.endIndex = j;
        block.makeStatement(code.substring(i, j + 1));
        return block;
    }

    private CodeBlock parseIf(int i) {
        CodeBlock block = new CodeBlock();
        i += 2;
        Substring ifCondition = tokenizeCondition(i);
        i = ifCondition.getEnd() + 1;
        Substring ifBody = tokenizeBody(i);
        block.endIndex = ifBody.getEnd();
        i = ifBody.getEnd() + 1;
        i = findNonwhitespace(i);
        if (!isKeyword(i, "else")) {
            block.makeIf(ifCondition.getStr(), ifBody.getStr());
            return block;
        }
        i += 4;
        Substring elseBody = tokenizeBody(i);
        block.endIndex = elseBody.getEnd();
        block.makeIfElse(ifCondition.getStr(), ifBody.getStr(), elseBody.getStr());
        return block;
    }

    private CodeBlock parseFor(int i) {
        CodeBlock block = new CodeBlock();
        i += 3;
        ForSubstring fss = tokenizeForCondition(i);
        i = fss.getEnd() + 1;
        Substring forBody = tokenizeBody(i);
        block.endIndex = forBody.getEnd();
        block.makeFor(fss.getCondition(), forBody.getStr(), fss.getInit(), fss.getIncrement());
        return block;
    }

    private CodeBlock parseWhile(int i) {
        CodeBlock block = new CodeBlock();
        i += 5;
        Substring whileCondition = tokenizeCondition(i);
        i = whileCondition.getEnd() + 1;
        Substring whileBody = tokenizeBody(i);
        block.endIndex = whileBody.getEnd();
        block.makeWhile(whileCondition.getStr(), whileBody.getStr());
        return block;
    }

    private CodeBlock parseDoWhile(int i) {
        CodeBlock block = new CodeBlock();
        i += 2;
        Substring doWhileBody = tokenizeBody(i);
        i = doWhileBody.getEnd() + 1;
        i = findNonwhitespace(i);
        if (i == -1) {
            throw new RuntimeException("Expected while keyword after do body, but no characters.");
        }
        if (!isKeyword(i, "while")) {
            throw new RuntimeException("Do While: missing while keyword.");
        }
        i += 5;
        Substring doWhileCondition = tokenizeCondition(i);
        i = doWhileCondition.getEnd() + 1;
        i = nextCharEqual(i, ';');
        if (i == -1) {
            throw new RuntimeException("DoWhile missing semicolon after condition.");
        }
        block.endIndex = i;
        block.makeDoWhile(doWhileCondition.getStr(), doWhileBody.getStr());
        return block;
    }

    private Substring tokenizeBody(int fromIndex) {
        int i = findNonwhitespace(fromIndex);
        if (i == -1) {
            throw new RuntimeException();
        }
        boolean singleLine;
        int j;
        if (code.charAt(i) == '{') {
            j = findNested(i, '{', '}');
            singleLine = false;
        } else {
            j = findSemicolon(i);
            singleLine = true;
        }
        if (j == -1) {
            throw new RuntimeException();
        }
        if (singleLine) {
            return new Substring(code.substring(i, j + 1), j);
        } else {
            return new Substring(code.substring(i + 1, j), j);
        }
    }

    private int nextCharEqual(int fromIndex, char x) {
        int i = findNonwhitespace(fromIndex);
        if (i == -1) {
            return -1;
        }
        if (code.charAt(i) != x) {
            return -1;
        }
        return i;
    }

    private int findSemicolon(int fromIndex) {
        return code.indexOf(';', fromIndex);
    }

    private ForSubstring tokenizeForCondition(int fromIndex) {
        ForSubstring a = new ForSubstring();
        int begin = nextCharEqual(fromIndex, '(');
        if (begin == -1) {
            throw new RuntimeException();
        }
        int end = findNested(begin, '(', ')');
        if (end == -1) {
            throw new RuntimeException();
        }
        int semicolon1 = findSemicolon(begin);
        if (semicolon1 == -1) {
            throw new RuntimeException();
        }
        if (!isBetween(begin, semicolon1, end)) {
            throw new RuntimeException();
        }
        a.setInit(code.substring(begin + 1, semicolon1));
        int semicolon2 = findSemicolon(semicolon1 + 1);
        if (semicolon2 == -1) {
            throw new RuntimeException();
        }
        if (!isBetween(semicolon1, semicolon2, end)) {
            throw new RuntimeException();
        }
        a.setEnd(end);
        a.setCondition(code.substring(semicolon1 + 1, semicolon2));
        a.setIncrement(code.substring(semicolon2 + 1, end));
        return a;
    }

    private Substring tokenizeCondition(int fromIndex) {
        int i = findNonwhitespace(fromIndex);
        if (i == -1) {
            throw new RuntimeException();
        }
        if (code.charAt(i) != '(') {
            throw new RuntimeException();
        }
        int j = findNested(i, '(', ')');
        if (j == -1) {
            throw new RuntimeException();
        }
        return new Substring(code.substring(i + 1, j), j);
    }

    private boolean isKeyword(int fromIndex, String key) {
        if (code.length() - fromIndex < key.length()) {
            return false;
        }
        if (code.length() - fromIndex > key.length()) {
            if (Character.isJavaIdentifierPart(code.charAt(fromIndex + key.length()))) {
                return false;
            }
        }
        return code.substring(fromIndex, fromIndex + key.length()).equals(key);
    }

    private String whichKeyword(int start) {
        String[] a = {"if", "else", "while", "do", "for"};
        for (String i : a) {
            if (isKeyword(start, i)) {
                return i;
            }
        }
        return "";
    }

    private boolean isBetween(int begin, int test, int end) {
        return begin < test && test < end;
    }

    private int findNonwhitespace(int fromIndex) {
        for (int i = fromIndex; i < code.length(); i++) {
            if (!Character.isWhitespace(code.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findNested(int fromIndex, char begin, char end) {
        int level = 0;
        for (int i = fromIndex; i < code.length(); i++) {
            char x = code.charAt(i);
            if (x == begin) {
                level++;
            } else if (x == end) {
                level--;
                if (level == 0) {
                    return i;
                } else if (level < 0) {
                    throw new RuntimeException();
                }
            }
        }
        throw new RuntimeException();
    }

    public DStructure dStructure() {
        DStructure struct = new DStructure();
        struct.setStruct(this);
        switch (type) {
            case FOR:
                struct.setType(StructureType.D2);
                struct.setBody1(bodyStructure(body));
                break;
            case WHILE:
                struct.setType(StructureType.D2);
                struct.setBody1(bodyStructure(body));
                break;
            case DOWHILE:
                struct.setType(StructureType.D3);
                struct.setBody1(bodyStructure(body));
                break;
            case STATEMENT:
                struct.setType(StructureType.P1);
                break;
            case IF:
                struct.setType(StructureType.D0);
                struct.setBody1(bodyStructure(body));
                break;
            case IFELSE:
                struct.setType(StructureType.D1);
                struct.setBody1(bodyStructure(body));
                struct.setBody2(bodyStructure(body2));
                break;
            case FUNCTION:
                struct = bodyStructure(body);
                break;
            default:
                throw new RuntimeException();
        }
        return struct;
    }

    private static DStructure bodyStructure(LinkedList<CodeBlock> sequence) {
        if (sequence.isEmpty()) {
            DStructure dStruct = new DStructure();
            dStruct.setType(StructureType.P0);
            return dStruct;
        } else if (sequence.size() == 1) {
            return sequence.get(0).dStructure();
        } else {
            DStructure dStruct = new DStructure();
            dStruct.setType(StructureType.Pn);
            for (CodeBlock i : sequence) {
                dStruct.add(i.dStructure());
            }
            return dStruct;
        }
    }

    public String getCondition() {
        return condition;
    }

    public String getInit() {
        return init;
    }

    public String getIncrement() {
        return increment;
    }

    public String getStatement() {
        return statement;
    }

    public BlockType getType() {
        return type;
    }
}
