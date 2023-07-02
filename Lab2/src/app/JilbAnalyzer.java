package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JilbAnalyzer {

    public Table table;

    public String[] nestingInfluence = new String[]{"switch", "case", "?", "for", "if", "while", "do", "try", "catch", "finally", "else"};

    public String[] keyWords = new String[]{"abstract", "Array", "boolean", "byte", "char", "class",
            "const", "debugger", "double", "enum", "export", "extends", "float", "implements",
            "import", "int", "interface", "long", "native", "package", "private", "protected", "public", "short",
            "static", "super", "synchronized", "throws", "transient", "volatile", "yield",
            "await", "…obj", "arguments", "case", "catch", "let",
            "default", "delete", "do", "eval", "finally", "for", "function", "if", "in", "instanceof",
            "new", "return", "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with"};

    public String[] codeSymbols = new String[]{"[", "]", "{", "}", "(", ")", ";", ",", ".", "\"", "'", "`",
            "?", "~", ":",
            "!==", "!=", "!",
            "/=", "/", "%=", "%", "^=", "^", "-=", "--", "-", "+=", "++", "+", "**=", "*=", "**", "*",
            "&&=", "&=", "&&", "&",
            "||=", "|=", "||", "|",
            ">>>", ">>>=",
            "<<=", "<<", "<=", "<",
            ">>=", ">>", ">=", ">",
            "===", "==", "="};

    public HashSet<String> types = new HashSet<>(Arrays.asList("boolean", "number", "string",
            "any", "null", "undefined", "void", "never"));

    public Table analyze(File opFile) throws FileNotFoundException {
        table = new Table(new ArrayList<>());
        table.fileName = opFile.getAbsolutePath();
        parseToElements(opFile);
        countTotalOccurrences();
        table.cl = Double.parseDouble(String.format("%.3f", (double) table.CL / table.operatorsCount).replace(",", "."));
        return table;
    }

    private void parseToElements(File opFile) throws FileNotFoundException {
        String text = "\0\0\0\0\0 " + inputCode(opFile) + " \0\0\0\0\0";
        text = deleteComments(text);
        text = collectStringLiterals(text);
        text = separateLexemes(text);
        text = collectTypes(text);
        countMaxNestingLevel(text);
        text = collectFunctions(text);
        text = collectStandardFunctions(text);
        text = text.replaceAll(" +", " ");
        text = collectConditions(text);
        text = collectKeyWords(text);
        countOtherOperators(text);
    }

    private void countMaxNestingLevel(String text) { // do, for, if, while, try, catch, finally, ?
        text = placeLimiters(text);
        int maxNestingLevel = -1;
        int currentNestingLevel = -1;
        int currentLine = 1;
        int lineOfMaxNestingLevel = -1;

        for(int i = 0; i < text.length(); i++) {
            if(text.charAt(i) == '\n') currentLine++;
            if(text.charAt(i) == 'о') {
                currentNestingLevel++;
                if(currentNestingLevel > maxNestingLevel) {
                    maxNestingLevel = currentNestingLevel;
                    lineOfMaxNestingLevel = currentLine;
                }
            } else {
                if(text.charAt((i)) == 'з') {
                    currentNestingLevel--;
                }
            }
        }

        table.CLI = maxNestingLevel;
        table.lineOfCLI = lineOfMaxNestingLevel;
    }

    private String placeLimiters(String text) {
        text = text.replaceAll(" default ", " case ");
        text = text.replaceAll(" return ", " break ");
        // ?
        int nextIndex = text.indexOf("?", -1);
        while (nextIndex > -1) {
            int blockOpener = nextIndex;
            int blockCloser = findIndexOfBlockCloser(text, blockOpener, "?", ":");
            text = strInsertAfter(strInsertBefore(text, "з", blockCloser), "о", blockOpener);

            blockOpener = blockCloser + 2;
            blockCloser = findTernaryLimiter(text, blockOpener);
            text = strInsertAfter(strInsertBefore(text, "з", blockCloser), "о", blockOpener);
            nextIndex = text.indexOf("?", nextIndex + 1);
        }

        // switch case
        nextIndex = text.indexOf(" switch ", -1);
        while (nextIndex > -1) {
            int blockOpener = text.indexOf("{", nextIndex);
            int blockCloser = findIndexOfBlockCloser(text, blockOpener, "{", "}");

            int  caseIndex = findNextCase(text, blockOpener, blockCloser, blockOpener);
            while (caseIndex > -1) {
                int nextCaseIndex = findNextCase(text, blockOpener, blockCloser, caseIndex + 1);
                int nextBreak = findNextBreak(text, blockOpener, blockCloser, caseIndex + 1);

                if(nextBreak == -1 || nextBreak > nextCaseIndex) {
                    text = strInsertBefore(strInsertAfter(text, "о", text.indexOf(":", caseIndex)), "з", nextCaseIndex == -1 ? blockCloser : nextCaseIndex);
                } else {
                    text = strInsertBefore(strInsertAfter(text, "о", text.indexOf(":", caseIndex)), "з", blockCloser);
                }

                caseIndex = findNextCase(text, blockOpener, blockCloser, caseIndex + 1);
            }
            nextIndex = text.indexOf(" switch ", nextIndex + 1);
        }

        // if
        nextIndex = text.indexOf(" if ", -1);
        while (nextIndex > -1) {
            int blockOpener = text.indexOf("{", nextIndex);
            int lineCloser = text.indexOf(";", findIndexOfBlockCloser(text, text.indexOf("(", nextIndex), "(", ")"));

            if (blockOpener != -1 && blockOpener < lineCloser) {
                int blockCloser = findIndexOfBlockCloser(text, blockOpener, "{", "}");
                text = strInsertBefore(strInsertAfter(text, "о", blockOpener), "з", blockCloser + 1);
            } else {
                blockOpener = findIndexOfBlockCloser(text, text.indexOf("(", nextIndex), "(", ")") + 1;
                text = strInsertBefore(strInsertAfter(text, "о", blockOpener), "з", lineCloser + 1);
            }
            nextIndex = text.indexOf(" if ", nextIndex + 1);
        }

        // for
        nextIndex = text.indexOf(" for ", -1);
        while (nextIndex > -1) {
            int blockOpener = text.indexOf("{", nextIndex);
            int lineCloser = text.indexOf(";", findIndexOfBlockCloser(text, text.indexOf("(", nextIndex), "(", ")"));

            if (blockOpener != -1 && blockOpener < lineCloser) {
                int blockCloser = findIndexOfBlockCloser(text, blockOpener, "{", "}");
                text = strInsertBefore(strInsertAfter(text, "о", blockOpener), "з", blockCloser + 1);
            } else {
                blockOpener = findIndexOfBlockCloser(text, text.indexOf("(", nextIndex), "(", ")") + 1;
                text = strInsertBefore(strInsertAfter(text, "о", blockOpener), "з", lineCloser + 1);
            }
            nextIndex = text.indexOf(" for ", nextIndex + 1);
        }

        // while
        nextIndex = text.indexOf(" while ", -1);
        while (nextIndex > -1) {
            int blockOpener = text.indexOf("{", nextIndex);
            int lineCloser = text.indexOf(";", findIndexOfBlockCloser(text, text.indexOf("(", nextIndex), "(", ")"));

            if (blockOpener != -1 && blockOpener < lineCloser) {
                int blockCloser = findIndexOfBlockCloser(text, blockOpener, "{", "}");
                text = strInsertBefore(strInsertAfter(text, "о", blockOpener), "з", blockCloser + 1);
            }
            nextIndex = text.indexOf(" while ", nextIndex + 1);
        }

        //other
        for (int i = 6; i < nestingInfluence.length; i++) {
            nextIndex = text.indexOf(" " + nestingInfluence[i] + " ", -1);
            while (nextIndex > -1) {
                int blockOpener = text.indexOf("{", nextIndex);
                int blockCloser = findIndexOfBlockCloser(text, blockOpener, "{", "}");
                text = strInsertBefore(strInsertAfter(text, "о", blockOpener), "з", blockCloser + 1);
                nextIndex = text.indexOf(" " + nestingInfluence[i] + " ", nextIndex + 1);
            }
        }
        return text;
    }

    private int findTernaryLimiter(String text, int blockOpener) {
        int bracketLimiter = text.indexOf(")", blockOpener);
        int endLineLimiter = text.indexOf(";", blockOpener);
        int commaLimiter = text.indexOf(",", blockOpener);
        int sqBracketLimiter = text.indexOf("]", blockOpener);
        if(bracketLimiter == -1) {
            bracketLimiter = Integer.MAX_VALUE;
        }
        if(endLineLimiter == -1) {
            endLineLimiter = Integer.MAX_VALUE;
        }
        if(commaLimiter == -1) {
            commaLimiter = Integer.MAX_VALUE;
        }
        if(sqBracketLimiter == -1) {
            sqBracketLimiter = Integer.MAX_VALUE;
        }
        return Math.min(bracketLimiter, Math.min(endLineLimiter, Math.min(commaLimiter, sqBracketLimiter)));
    }

    private int findNextBreak(String text, int blockOpener, int blockCloser, int start) {
        int innerSwitch = text.indexOf(" switch ", start);
        if(innerSwitch == -1) {
            innerSwitch = Integer.MAX_VALUE;
        }
        int innerBO = text.indexOf("{", innerSwitch);
        if(innerBO == -1) {
            innerBO = Integer.MAX_VALUE;
        }
        int nextCase = text.indexOf(" break ", start + 1);
        if(nextCase < innerBO) {
            return nextCase;
        } else {
            int innerBC = findIndexOfBlockCloser(text, blockOpener, "{", "}");
            while (innerSwitch > -1 && innerSwitch < blockCloser) {
                innerBC = findIndexOfBlockCloser(text, blockOpener, "{", "}");
                innerSwitch = text.indexOf(" switch ", innerBC);
            }
            if (innerBC == -1) {
                innerBC = start;
            }

            nextCase = text.indexOf(" break ", innerBC);
            if(nextCase > blockCloser) {
                return -1;
            } else {
                return nextCase;
            }
        }
    }

    private int findNextCase(String text, int blockOpener, int blockCloser, int start) {
        int innerSwitch = text.indexOf(" switch ", start);
        if(innerSwitch == -1) {
            innerSwitch = Integer.MAX_VALUE;
        }
        int innerBO = text.indexOf("{", innerSwitch);
        if(innerBO == -1) {
            innerBO = Integer.MAX_VALUE;
        }
        int nextCase = text.indexOf(" case ", start + 1);
        if(nextCase < innerBO) {
            return nextCase;
        } else {
            int innerBC = findIndexOfBlockCloser(text, blockOpener, "{", "}");
            while (innerSwitch > -1 && innerSwitch < blockCloser) {
                innerBC = findIndexOfBlockCloser(text, blockOpener, "{", "}");
                innerSwitch = text.indexOf(" switch ", innerBC);
            }
            if (innerBC == -1) {
                innerBC = start;
            }

            nextCase = text.indexOf(" case ", innerBC);
            if(nextCase > blockCloser) {
                return -1;
            } else {
                return nextCase;
            }
        }
    }

    private String strInsertAfter(String text, String inserting, int index) {
        return text.substring(0, index + 1) + inserting + text.substring(index + 1);
    }

    private String strInsertBefore(String text, String inserting, int index) {
        return text.substring(0, index) + inserting + text.substring(index);
    }

    private void countTotalOccurrences() {
        int sum = 0;
        for (int i = 0; i < table.occurrences.size(); i++) {
            sum += table.occurrences.get(i);
        }
        table.operatorsCount = sum;
    }

    private String collectConditions(String text) {
        int conditionCount = 0;
        if (text.contains(" switch ")) {
            table.ops.add("switch..case..default");
            int switchCount = countMatches(text, " switch ");
            table.occurrences.add(switchCount);
            conditionCount += countMatches(text, " case ");
            text = text.replaceAll(" switch \\(", " ");
            text = text.replaceAll(" case ", " ");
            text = text.replaceAll(" default ", " ");
        }

        if (text.contains(" if ")) {
            table.ops.add("if..else");
            int ifCount = countMatches(text, " if ");
            conditionCount += ifCount;
            table.occurrences.add(ifCount);
            text = text.replaceAll(" if \\(", " ");
            text = text.replaceAll(" else ", " ");
        }

        if (text.contains(" ? ")) {
            table.ops.add("?..:");
            int ternaryCount = countMatches(text, " ? ");
            conditionCount += ternaryCount;
            table.occurrences.add(ternaryCount);
            text = deleteTernaryOps(text);
        }
        table.CL = conditionCount;
        return text;
    }

    private String deleteTernaryOps(String text) {
        int index = text.indexOf("?");
        while (index > 0) {
            int colonIndex = findIndexOfBlockCloser(text, index, "?", ":");
            text = deleteSubstring(text, colonIndex, 1);
            text = deleteSubstring(text, index, 1);
            index = text.indexOf("?");
        }
        return text;
    }

    private void countOtherOperators(String text) {
        if (text.contains("(")) {
            table.ops.add("()");
            table.occurrences.add(countMatches(text, "("));
            text = text.replace("( ", "");
        }
        if (text.contains("{")) {
            table.ops.add("{}");
            table.occurrences.add(countMatches(text, "{"));
            text = text.replace("{ ", "");
        }
        if (text.contains("[")) {
            table.ops.add("[]");
            table.occurrences.add(countMatches(text, "["));
            text = text.replace("[ ", "");
        }
        for (int i = 7; i < codeSymbols.length; i++) {
            String next = codeSymbols[i];
            if (text.contains(next)) {
                table.ops.add(next);
                table.occurrences.add(countMatches(text, " " + next + " "));
            }
            text = text.replace(next, "");
        }
    }

    private String collectKeyWords(String text) {

        if (text.contains(" try ")) {
            table.ops.add("try..catch..finally");
            table.occurrences.add(countMatches(text, " try "));
            text = text.replaceAll(" try ", " ");
            text = text.replaceAll(" catch \\(", " ");
            text = text.replaceAll(" finally ", " ");
        }

        if (text.contains(" do ")) {
            table.ops.add("do..while");
            int countMatches = countMatches(text, " do ");
            table.occurrences.add(countMatches);
            table.CL = table.CL + countMatches;
            int index = -1;
            while (text.indexOf(" do ", index + 1) > -1) {
                index = text.indexOf(" do ", index + 1);
                text = deleteSubstring(text, index, " do ".length());
                int whileIndex = findIndexOfBlockCloser(text, index, " do ", " while ");
                text = deleteSubstring(text, whileIndex, " while (".length());
            }
        }

        if (text.contains(" for ")) {
            table.ops.add("for");
            int countMatches = countMatches(text, " for ");
            table.occurrences.add(countMatches(text, " for "));
            table.CL = table.CL + countMatches;
            int index = -1;
            while (text.indexOf(" for ", index + 1) > -1) {
                index = text.indexOf(" for ", index + 1);
                text = deleteSubstring(text, index, " for (".length());
            }
        }

        if (text.contains(" while ")) {
            table.ops.add("while");
            int countMatches = countMatches(text, " while ");
            table.occurrences.add(countMatches(text, " while "));
            table.CL = table.CL + countMatches;
            int index = -1;
            while (text.indexOf(" while ", index + 1) > -1) {
                index = text.indexOf(" while ", index + 1);
                text = deleteSubstring(text, index, " while (".length());
            }
        }

        if (text.contains(" with ")) {
            table.ops.add("with");
            table.occurrences.add(countMatches(text, " with "));
            int index = -1;
            while (text.indexOf(" with ", index + 1) > -1) {
                index = text.indexOf(" with ", index + 1);
                text = deleteSubstring(text, index, " with ".length());
            }
        }

        for (String next : keyWords) {
            if (!table.ops.contains(next) && text.contains(" " + next + " ")) {
                table.ops.add(next);
                table.occurrences.add(countMatches(text, " " + next + " "));
                text = text.replace(" " + next + " ", " ");
            }
        }
        return text;
    }

    private int findIndexOfBlockCloser(String text, int start, String blockOpener, String blockCloser) {
        int openCloseCount = 1;
        for (int i = start + 1; i < text.length(); i++) {
            if (isSubstrOnStrIn(text, blockOpener, i)) {
                openCloseCount++;
            } else {
                if (isSubstrOnStrIn(text, blockCloser, i)) {
                    openCloseCount--;
                    if (openCloseCount == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private boolean isSubstrOnStrIn(String text, String blockCloser, int i) {
        return blockCloser.equals(text.substring(i, i + blockCloser.length()));
    }

    private String collectStandardFunctions(String text) {
        Matcher matcher = Pattern.compile("\\w+ \\(").matcher(text);
        while (matcher.find()) {
            if (!isInArray(keyWords, matcher.group().substring(0, matcher.group().length() - 2)) && !table.ops.contains((matcher.group() + ")").replace(" ", ""))) {
                table.ops.add((matcher.group() + ")").replace(" ", ""));
                table.occurrences.add(countMatches(text, matcher.group()));
                text = text.replace(matcher.group(), " ");
            }
        }
        return text;
    }

    private String collectFunctions(String text) {
        int index = -1;
        while (text.indexOf(" function ", index + 1) > -1) {
            index = text.indexOf(" function ", index + 1) + 1;
            String functionPrototype;

            functionPrototype = text.substring(index + "function ".length(), text.indexOf("{", index));

            if (isPreFunction(functionPrototype)) {
                text = text.substring(0, index - 1) + " " + text.substring(index + "function (".length());
                if (text.charAt(index - 2) == '=') {
                    String name = text.substring(text.lastIndexOf(" ", index - 4), index - 2).trim();

                    int countMatches = countMatches(text, " " + name + " (");
                    if (countMatches > 0) {
                        table.ops.add(name + "()");
                        table.occurrences.add(countMatches);
                    }
                    text = text.replaceAll(" " + name + " \\(?", " ");
                }

            } else {
                String name = functionPrototype.substring(0, functionPrototype.indexOf("(")).trim();
                text = text.substring(0, index - 1) + " " + text.substring(index + "function".length() + 1);

                int countMatches = countMatches(text, " " + name + " (") - 1;
                if (countMatches > 0) {
                    table.ops.add(name + "()");
                    table.occurrences.add(countMatches);
                }
                text = text.replaceAll(" " + name + " \\(?", " ");
            }
        }

        index = -1;
        while (text.indexOf(" = > ", index + 1) > -1) {
            index = text.indexOf(" = > ", index + 1);
            text = text.replaceFirst(" = > ", " ");
            int parametersIndex;
            if (text.charAt(index - 1) == ')') {
                parametersIndex = text.lastIndexOf("(", index) - 1;
            } else {
                parametersIndex = text.lastIndexOf(" ", index - 2);
            }

            if (text.charAt(parametersIndex - 1) == '=') {
                String name = text.substring(text.lastIndexOf(" ", parametersIndex - 3), parametersIndex - 2).trim();
                text = text.substring(0, parametersIndex + 1) + " " + text.substring(parametersIndex + 2);

                int countMatches = countMatches(text, " " + name + " (");
                if (countMatches > 0) {
                    table.ops.add(name + "()");
                    table.occurrences.add(countMatches);
                }
                text = text.replaceAll(" " + name + " \\(?", " ");
            }
        }
        return text;
    }

    private boolean isPreFunction(String functionPrototype) {
        return functionPrototype.trim().charAt(0) == '(';
    }

    private Integer countMatches(String text, String sl) {
        if (text.contains(sl)) {
            int count = 0;
            int index = text.indexOf(sl);
            while (text.indexOf(sl, index + 1) > -1) {
                count++;
                index = text.indexOf(sl, index + 1);
            }
            return count + 1;
        } else {
            return 0;
        }
    }

    private String collectTypes(String text) {
        int index = 0;
        while (text.indexOf(" enum ", index + 1) > -1) { // enums
            index = text.indexOf(" enum ", index + 1);
            text = text.substring(0, text.indexOf("{", index) - 1) + text.substring(text.indexOf("}", index) + 1);
        }

        text = text.replace(" async ", " ");
        for (String next : types) {
            text = text.replaceAll(" : " + next + "( \\[ ])*", " "); // other types
            text = text.replaceAll(" : \\[ \\w+( , \\w+)* ]", " "); //tuples
        }
        return text;
    }

    private String separateLexemes(String text) {
        int index;

        for (int i = 0; i < codeSymbols.length; i++) {
            index = nextIndexOfLexeme(text, codeSymbols[i], -1);
            while (index > -1) {
                text = separateWith(text, " ", index, index + codeSymbols[i].length());
                index = nextIndexOfLexeme(text, codeSymbols[i], index + 1 + " ".length());
            }
        }
        return text.replaceAll(" +", " ");
    }

    public String separateWith(String text, String separator, int begin, int end) {
        return text.substring(0, begin) + separator + text.substring(begin, end) + separator + text.substring(end);
    }

    private int nextIndexOfLexeme(String text, String codeSymbol, int target) {
        int index = text.indexOf(codeSymbol, target);
        while (index > -1) {

            String check = text.charAt(index - 1) + codeSymbol;
            if (!isInArray(codeSymbols, check)) {
                check = codeSymbol + text.charAt(index + codeSymbol.length());
                if (!isInArray(codeSymbols, check)) {
                    return index;
                }
            }

            index = text.indexOf(codeSymbol, index + 1);
        }
        return -1;
    }

    private boolean isInArray(String[] codeSymbols, String check) {
        for (int i = 0; i < codeSymbols.length; i++) {
            if (codeSymbols[i].equals(check)) return true;
        }
        return false;
    }

    private String inputCode(File opFile) throws FileNotFoundException {
        String text = "";
        Scanner in = new Scanner(opFile);
        while (in.hasNextLine()) {
            text = text + " " + in.nextLine() + "\n";
        }
        in.close();
        return text;
    }

    private String deleteComments(String text) {
        return deleteLineComments(deleteMultiLineComments(text));
    }

    private String deleteLineComments(String text) {
        int beginOneLineComm = -1;
        while (text.lastIndexOf("//") > beginOneLineComm) {
            beginOneLineComm = text.indexOf("//", beginOneLineComm + 1);

            if (beginOneLineComm == -1) {
                beginOneLineComm = text.length() + 1;
            } else {
                if (!isInString(text, beginOneLineComm)) {
                    text = deleteSubstring(text, beginOneLineComm, text.indexOf("\n", beginOneLineComm) - beginOneLineComm);
                }
            }
        }
        return text;
    }

    private String deleteMultiLineComments(String text) {
        int beginComm = -1;
        int endComm;
        while (text.lastIndexOf("/*") > beginComm) {
            beginComm = text.indexOf("/*", beginComm + 1);
            if (beginComm == -1) {
                beginComm = text.length() + 1;
            } else {
                if (!isInString(text, beginComm)) {
                    endComm = text.indexOf("*/", beginComm);
                    text = deleteSubstring(text, beginComm, endComm + 2 - beginComm);
                }
            }
        }
        return text;
    }

    private String deleteSubstring(String text, int begin, int length) {
        return text.substring(0, begin) + " " + text.substring(begin + length);
    }

    private String collectStringLiterals(String text) {
        for (int i = 0; i < text.length(); i++) {
            char qMark = text.charAt(i);
            int endOfLiteral;
            if (qMark == '`' || qMark == '"' || qMark == '\'') {
                if (!isScreened(text, i)) {
                    endOfLiteral = indexOfClosingQMark(text, i, qMark);
                    if (endOfLiteral > -1) {
                        text = deleteSubstring(text, i + 1, endOfLiteral - i);
                    }
                }
            }
        }
        return text;
    }

    private boolean isInString(String text, int target) {
        int i = 0;
        while (i < target) {
            char qMark = text.charAt(i);
            if (qMark == '`' || qMark == '"' || qMark == '\'') {
                if (!isScreened(text, i)) {
                    i = indexOfClosingQMark(text, i, qMark);
                    if (i > target) {
                        return true;
                    }
                }
            }
            i++;
        }
        return false;
    }

    private int indexOfClosingQMark(String text, int beginOfLiteral, char qMark) {
        for (int i = beginOfLiteral + 1; i < text.length(); i++) {
            if (text.charAt(i) == qMark && !isScreened(text, i)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isScreened(String text, int target) {
        int slashCount = 0;
        while (text.charAt(target - 1) == '\\') {
            slashCount++;
            target--;
        }
        return slashCount % 2 == 1;
    }

    public static class Table {
        public ArrayList<String> ops;
        public ArrayList<Integer> occurrences;
        public int CL, CLI, operatorsCount, lineOfCLI;
        public double cl;
        public String fileName;

        public Table(List<String> asList) {
            CL = CLI = operatorsCount = lineOfCLI = 0;
            cl = 0;
            fileName = "";
            ops = new ArrayList<>(asList);
            Integer[] occs = new Integer[ops.size()];
            Arrays.fill(occs, 0);
            occurrences = new ArrayList<>(Arrays.asList(occs));
        }
    }
}
