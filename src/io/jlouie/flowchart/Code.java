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
public class Code {

    private final String code;

    public Code(String code) {
        this.code = code;
    }

    public DStructure getStructure() {
        LinkedList<LanguageConstruct> lcs = parse();
        if (lcs == null) {
            return null;
        }
        if (lcs.isEmpty()) {
            return null;
        } else if (lcs.size() == 1) {
            return lcs.get(0).getStructure();
        } else {
            DStructure struct = new DStructure();
            struct.setType(StructType.Pn);
            for (LanguageConstruct lc : lcs) {
                DStructure struct2 = lc.getStructure();
                if (struct2 != null) {
                    struct.add(struct2);
                }
            }
            return struct;
        }
    }

    public LinkedList<LanguageConstruct> parse() {
        LinkedList<LanguageConstruct> a = new LinkedList<>();
        for (int i = 0; i < code.length(); i++) {
            LanguageConstruct construct;
            int j = i;
            i = findFirstNonWhitespace(i);
            if (i == -1) {
//                System.out.println("Cannot find nonwhitespace. " + j + " of " + code.length());
                return a;
            }
            String s = whichKeyword(i);
            if (s == null) {
                construct = parseStatement(i);
                if (construct == null) {
                    return null;
                }
                i = construct.getEnd();
            } else {
                switch (s) {
                    case "if":
                        construct = parseIf(i);
                        if (construct == null) {
                            return null;
                        }
                        i = construct.getEnd();
                        break;
                    case "while":
                        construct = parseWhile(i);
                        if (construct == null) {
                            return null;
                        }
                        i = construct.getEnd();
                        break;
                    case "do":
                        construct = parseDoWhile(i);
                        if (construct == null) {
                            return null;
                        }
                        i = construct.getEnd();
                        break;
                    case "for":
                        construct = parseFor(i);
                        if (construct == null) {
                            return null;
                        }
                        i = construct.getEnd();
                        break;
                    default:
                        return null;
                }
            }
            a.add(construct);
        }
        return a;
    }

    private LanguageConstruct parseStatement(int i) {
        LanguageConstruct construct = new LanguageConstruct();
        construct.setType(Type.STATEMENT);
        int j = findNextChar(i, ';');
        if (j == -1) {
            System.out.println("Did not find expected statement semicolon.");
            return null;
        }
        construct.setStatement(code.substring(i, j + 1));
        construct.setEnd(j);
        return construct;
    }

    private LanguageConstruct parseIf(int i) {
        LanguageConstruct construct = new LanguageConstruct();
        Substring ss;
        construct.setType(Type.IF);
        i += 2;
        ss = getCondition(i);
        if (ss == null) {
            System.out.println("If condition error.");
            return null;
        }
        construct.setCondition(ss.getStr());
        i = ss.getEnd() + 1;
        ss = getBody(i);
        if (ss == null) {
            System.out.println("If body error.");
            return null;
        }
        construct.setBody1(ss.getStr());
        construct.setEnd(ss.getEnd());
        i = ss.getEnd() + 1;
        i = findFirstNonWhitespace(i);
        if (!isKeyword(i, "else")) {
            return construct;
        }
        construct.setType(Type.IFELSE);
        i += 4;
        ss = getBody(i);
        if (ss == null) {
            System.out.println("Else body error.");
            return null;
        }
        construct.setBody2(ss.getStr());
        construct.setEnd(ss.getEnd());
        return construct;
    }

    private LanguageConstruct parseFor(int i) {
        LanguageConstruct construct = new LanguageConstruct();
        Substring ss;
        construct.setType(Type.FOR);
        i += 3;
        ForSubstring fss = getForCondition(i);
        if (fss == null) {
            System.out.println("For condition error.");
            return null;
        }
        construct.setCondition(fss.getCondition());
        construct.setInit(fss.getInit());
        construct.setIncrement(fss.getIncrement());
        i = fss.getEnd() + 1;
        ss = getBody(i);
        if (ss == null) {
            System.out.println("For body error.");
            return null;
        }
        construct.setBody1(ss.getStr());
        construct.setEnd(ss.getEnd());
        return construct;
    }

    private LanguageConstruct parseWhile(int i) {
        LanguageConstruct construct = new LanguageConstruct();
        Substring ss;
        construct.setType(Type.WHILE);
        i += 5;
        ss = getCondition(i);
        if (ss == null) {
            System.out.println("While condition error.");
            return null;
        }
        construct.setCondition(ss.getStr());
        i = ss.getEnd() + 1;
        ss = getBody(i);
        if (ss == null) {
            System.out.println("While body error.");
            return null;
        }
        construct.setBody1(ss.getStr());
        construct.setEnd(ss.getEnd());
        return construct;
    }

    private LanguageConstruct parseDoWhile(int i) {
        LanguageConstruct construct = new LanguageConstruct();
        Substring ss;
        construct.setType(Type.DOWHILE);
        i += 2;
        // after do
        // body
        ss = getBody(i);
        if (ss == null) {
            System.out.println("DoWhile body error.");
            return null;
        }
        construct.setBody1(ss.getStr());
        i = ss.getEnd() + 1;
        // end body
        // while
        i = findFirstNonWhitespace(i);
        if (i == -1) {
            System.out.println("Expected while keyword after do body, but no characters.");
            return null;
        }
        if (!isKeyword(i, "while")) {
            System.out.println("Do While: missing while keyword.");
            return null;
        }
        i += 5;
        // end while
        // condition
        ss = getCondition(i);
        if (ss == null) {
            System.out.println("DoWhile condition error.");
            return null;
        }
        construct.setCondition(ss.getStr());
        i = ss.getEnd() + 1;
        // end condition
        // semicolon
        i = nextCharEqual(i, ';');
        if (i == -1) {
            System.out.println("DoWhile missing semicolon after condition.");
            return null;
        }
        construct.setEnd(i);
        return construct;
    }

    private Substring getBody(int start) {
        int begin = findFirstNonWhitespace(start);
        if (begin == -1) {
            return null;
        }
        boolean singleLine;
        int end;
        if (code.charAt(begin) == '{') {
            end = findClosing(begin, '{', '}');
            singleLine = false;
        } else {
            end = findNextChar(begin, ';');
            singleLine = true;
        }
        if (end == -1) {
            return null;
        }
        if (singleLine) {
            return new Substring(code.substring(begin, end + 1), end);
        } else {
            return new Substring(code.substring(begin + 1, end), end);
        }
    }

    private int nextCharEqual(int start, char x) {
        int firstCharPosition = findFirstNonWhitespace(start);
        if (firstCharPosition == -1) {
            return -1;
        }
        if (code.charAt(firstCharPosition) != x) {
            return -1;
        }
        return firstCharPosition;
    }

    private ForSubstring getForCondition(int start) {
        ForSubstring a = new ForSubstring();
        int begin = nextCharEqual(start, '(');
        if (begin == -1) {
            return null;
        }
        int end = findClosing(begin, '(', ')');
        if (end == -1) {
            return null;
        }
        a.setEnd(end);
        int semicolon1 = findNextChar(begin, ';');
        if (semicolon1 == -1) {
            return null;
        }
        if (!isBetween(begin, end, semicolon1)) {
            return null;
        }
        a.setInit(code.substring(begin + 1, semicolon1));
        int semicolon2 = findNextChar(semicolon1 + 1, ';');
        if (semicolon2 == -1) {
            return null;
        }
        if (!isBetween(semicolon1, end, semicolon2)) {
            return null;
        }
        a.setCondition(code.substring(semicolon1 + 1, semicolon2));
        a.setIncrement(code.substring(semicolon2 + 1, end));
        return a;
    }

    private Substring getCondition(int start) {
        int begin = findFirstNonWhitespace(start);
        if (begin == -1) {
            return null;
        }
        if (code.charAt(begin) == '(') {
            int end = findClosing(begin, '(', ')');
            if (end == -1) {
                return null;
            }
            return new Substring(code.substring(begin + 1, end), end);
        }
        return null;
    }

    private boolean isKeyword(int start, String key) {
        try {
            return code.substring(start, start + key.length()).equals(key);
        } catch (IndexOutOfBoundsException ex) {
            return false;
        }
    }

    private String whichKeyword(int start) {
        String[] a = {"if", "else", "while", "do", "for"};
        for (String i : a) {
            if (isKeyword(start, i)) {
                return i;
            }
        }
        return null;
    }

    private boolean isBetween(int start, int end, int test) {
        return start < test && test < end;
    }

    private int findNextChar(int start, char c) {
        for (int i = start; i < code.length(); i++) {
            if (code.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    private int findFirstNonWhitespace(int start) {
        for (int i = start; i < code.length(); i++) {
            char x = code.charAt(i);
            switch (x) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    break;
                default:
                    return i;
            }
        }
        return -1;
    }

    private int findClosing(int start, char begin, char end) {
        int level = 0;
        for (int i = start; i < code.length(); i++) {
            char x = code.charAt(i);
            if (x == end) {
                level--;
                if (level == 0) {
                    return i;
                } else if (level < 0) {
                    System.out.println("Found unexpected " + end + " character.");
                    return -1;
                }
            } else if (x == begin) {
                level++;
            }
        }
        System.out.println("Cannot find closing " + end + " character.");
        return -1;
    }
}
