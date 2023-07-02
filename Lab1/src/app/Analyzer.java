package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analyzer {

    public Table[] tables = new Table[]{new Table(Arrays.asList(",", ";",
            "-", "/", "%", "--", ">", "<", "<=", ">=", "==", "!=", "===", "&&", "!", "&",
            "^", "~", "<<", ">>", ">>>", "=", "-=", "/=", ">>=", "=>",
            "<<=", ">>>=", "&=", "^=", ":", "!==", "[]", "{}", "()", "*", "+", "?", "**", "++", "*=", "+=", "||", "|", "|=", ".")),
            new Table(new ArrayList<>())};

    public HashSet<String> keyWords = new HashSet<>(Arrays.asList("abstract", "Array", "boolean", "byte", "char", "class",
            "const", "debugger", "double", "enum", "export", "extends", "float", "goto", "implements",
            "import", "int", "interface", "long", "native", "package", "private", "protected", "public", "short",
            "static", "super", "synchronized", "throws", "transient", "volatile", "yield",
            "await", "…obj", "arguments", "break", "case", "catch", "continue",
            "default", "delete", "do", "eval", "finally", "for", "function", "if", "in", "instanceof",
            "new", "return", "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with"));

    public HashSet<String> operators = new HashSet<>(Arrays.asList(",", ";",
            "-", "/", "%", "--", ">", "<", "<=", ">=", "==", "!=", "===", "&&", "!", "&",
            "^", "~", "<<", ">>", ">>>", "=", "-=", "/=", ">>=",
            "<<=", ">>>=", "&=", "^=", ":", "!==", "[", "]", "{", "}", "(", ")", "*", "+", "?", "**", "++", "*=", "+=", "||", "|", "|=", "."));

    public HashSet<String> types = new HashSet<>(Arrays.asList("boolean", "number", "string",
            "any", "null", "undefined", "void", "never"));

    public Table[] analyze(File opFile) throws FileNotFoundException {
        parseToElements(opFile);
        return tables;
    }

    private void parseToElements(File opFile) throws FileNotFoundException {
        String fileText = inputCode(opFile);
        fileText = deleteComments(fileText);
        fileText = separateOperators(fileText);
        fileText = collectStringLiterals(fileText);
        fileText = deleteTypes(fileText);
        fileText = collectPreFunctions(fileText);
        fileText = collectOperands(fileText, "let");
        fileText = collectOperands(fileText, "const");
        fileText = collectOperands(fileText, "var");
        fileText = collectOperands(fileText, "enum");
        fileText = collectLiterals(fileText);
        fileText = collectPostFunctions(fileText);
        fileText = collectStandardFunctions(fileText);
        fileText = collectKeyWords(fileText);
        fileText = collectObjects(fileText);
        fileText = deleteTrash(fileText);
        fileText = countOtherOperators(fileText);

        //System.out.println(tables[0].ops.toString());
        //System.out.println(tables[0].occurrences.toString());
        //System.out.println(tables[1].ops.toString());
        //System.out.println(tables[1].occurrences.toString());
        // System.out.println(fileText);
    }

    private String countOtherOperators(String fileText) {
        if (fileText.contains("(")) {
            tables[0].occurrences.set(tables[0].ops.indexOf("()"), countMatches(fileText, "("));
            fileText = fileText.replace("(", "");
        }
        if (fileText.contains("{")) {
            tables[0].occurrences.set(tables[0].ops.indexOf("{}"), countMatches(fileText, "{"));
            fileText = fileText.replace("{", "");
        }
        if (fileText.contains("[")) {
            tables[0].occurrences.set(tables[0].ops.indexOf("[]"), countMatches(fileText, "["));
            fileText = fileText.replace("[", "");
        }
        Iterator<String> nextOp = operators.iterator();
        while (nextOp.hasNext()) {
            String next = nextOp.next();
            if (next.equals("(") || next.equals(")") || next.equals("[") || next.equals("]") || next.equals("{") || next.equals("}"))
                continue;
            if (fileText.contains(next)) {
                tables[0].occurrences.set(tables[0].ops.indexOf(next), countMatches(fileText, next));
            }
            fileText = fileText.replace(next, "");
        }
        return fileText;
    }

    private String deleteTrash(String fileText) { //jiooijkojok
        Matcher matcher = Pattern.compile(". \\w+ ").matcher(fileText);
        HashSet<String> deleted = new HashSet<>();
        while (matcher.find()) {
            String str = matcher.group().substring(1).trim();
            if (!deleted.contains(str)) {
                deleted.add(str);
                fileText = fileText.replace(" " + str + " ", " ");
            }
        }
        return fileText.replaceAll(" +", " ");
    }

    private String collectKeyWords(String fileText) {
        if (fileText.contains(" if ")) {
            tables[0].ops.add("if..else");
            tables[0].occurrences.add(countMatches(fileText, " if "));
            fileText = fileText.replaceAll(" if ", " ");
            fileText = fileText.replaceAll(" else ", " ");
        }
        if (fileText.contains(" try ")) {
            tables[0].ops.add("try..catch..finally");
            tables[0].occurrences.add(countMatches(fileText, " try "));
            fileText = fileText.replaceAll(" try ", " ");
            fileText = fileText.replaceAll(" catch ", " ");
            fileText = fileText.replaceAll(" finally ", " ");
        }
        if (fileText.contains(" switch ")) {
            tables[0].ops.add("switch..case..default");
            tables[0].occurrences.add(countMatches(fileText, " switch "));
            fileText = fileText.replaceAll(" switch ", " ");
            fileText = fileText.replaceAll(" case ", " ");
            fileText = fileText.replaceAll(" default ", " ");
        }
        if (fileText.contains(" do ")) {
            tables[0].ops.add("do..while");
            tables[0].occurrences.add(countMatches(fileText, " do "));
            int index = -1;
            while (fileText.indexOf(" do ", index + 1) > -1) {
                index = fileText.indexOf(" do ", index + 1);
                fileText = fileText.substring(0, index + 1) + " " + fileText.substring(index + " do ".length());
                fileText = fileText.substring(0, fileText.indexOf(" while ", index)) + " " + fileText.substring(fileText.indexOf(" while ", index) + " while ".length());
            }
        }
        Iterator<String> nextWord = keyWords.iterator();
        while (nextWord.hasNext()) {
            String next = nextWord.next();
            if (next.equals("new")) continue;
            if (!tables[0].ops.contains(next) && fileText.contains(" " + next + " ")) {
                tables[0].ops.add(next);
                tables[0].occurrences.add(countMatches(fileText, " " + next + " "));
                fileText = fileText.replace(" " + next + " ", " ");
            }
        }
        return fileText;
    }

    private String collectStandardFunctions(String fileText) {
        Matcher matcher = Pattern.compile("\\w+ \\(").matcher(fileText);
        while (matcher.find()) {
            if (!keyWords.contains(matcher.group().substring(0, matcher.group().length() - 2)) && !tables[0].ops.contains((matcher.group() + ")").replace(" ", ""))) {
                tables[0].ops.add((matcher.group() + ")").replace(" ", ""));
                tables[0].occurrences.add(countMatches(fileText, matcher.group()));
                fileText = fileText.replace(matcher.group(), " ");
            }
        }
        return fileText;
    }

    private String collectLiterals(String fileText) {
        fileText = collectNumericLiterals(fileText);
        fileText = collectBooleanLiterals(fileText);
        fileText = collectNulls(fileText);
        fileText = collectUndefineds(fileText);
        return fileText;
    }

    private String collectUndefineds(String fileText) {
        if (fileText.contains(" undefined ")) {
            tables[1].ops.add("undefined");
            tables[1].occurrences.add(countMatches(fileText, " undefined "));
            fileText = fileText.replace(" undefined ", " ");
        }
        return fileText;
    }

    private String collectNulls(String fileText) {
        if (fileText.contains(" null ")) {
            tables[1].ops.add("null");
            tables[1].occurrences.add(countMatches(fileText, " null "));
            fileText = fileText.replace(" null ", " ");
        }
        return fileText;
    }

    private String collectBooleanLiterals(String fileText) {
        if (fileText.contains(" true ")) {
            tables[1].ops.add("true");
            tables[1].occurrences.add(countMatches(fileText, " true "));
            fileText = fileText.replace(" true ", " ");
        }
        if (fileText.contains(" false ")) {
            tables[1].ops.add("false");
            tables[1].occurrences.add(countMatches(fileText, " false "));
            fileText = fileText.replace(" false ", " ");
        }
        return fileText;
    }

    private String collectNumericLiterals(String fileText) {
        Matcher matcher = Pattern.compile(" \\d* \\.? ?\\d*e\\d+ ").matcher(fileText);// exponential numbers
        while (matcher.find()) {
             if (!tables[1].ops.contains(matcher.group().trim())) {
            tables[1].ops.add(matcher.group().replace(" ", ""));
            tables[1].occurrences.add(countMatches(fileText, matcher.group()));
            fileText = fileText.replace(matcher.group(), " ");
             }
        }

        matcher = Pattern.compile(" 0[xX][\\dabcdefABCDEF]+ ").matcher(fileText); // 16-radix
        while (matcher.find()) {
             if (!tables[1].ops.contains(matcher.group().trim())) {
            tables[1].ops.add(matcher.group().trim());
            tables[1].occurrences.add(countMatches(fileText, matcher.group()));
            fileText = fileText.replace(matcher.group(), " ");
             }
        }

        matcher = Pattern.compile(" \\d+ (\\. \\d*)?").matcher(fileText); // 10-radix
        while (matcher.find()) {
            if (!tables[1].ops.contains(matcher.group().trim())) {
                tables[1].ops.add(matcher.group().replace(" ", ""));
                tables[1].occurrences.add(countMatches(fileText, matcher.group()));
                fileText = fileText.replace(matcher.group(), " ");
            }
        }

        matcher = Pattern.compile(" 0[oO]\\d+ ").matcher(fileText); // 8-radix
        while (matcher.find()) {
            if (!tables[1].ops.contains(matcher.group().trim())) {
            tables[1].ops.add(matcher.group().trim());
            tables[1].occurrences.add(countMatches(fileText, matcher.group()));
            fileText = fileText.replaceAll(matcher.group(), " ");
            }
        }
        return fileText;
    }

    private String collectObjects(String fileText) {
        int index = -1;
        while (fileText.indexOf(" new ", index + 1) > -1) {
            index = fileText.indexOf(" new ", index + 1) + 1;
            String object = fileText.substring(index + "new ".length(), fileText.indexOf(" ", index + "new ".length())).trim();
            if(object.matches("\\w+")) {
                tables[1].ops.add(object);
                tables[1].occurrences.add(countMatches(fileText, object));
                fileText = fileText.replace(" " + object + " ", " ");
            }
        }
        if (fileText.contains("new")) {
            fileText = fileText.replace(" new ", " "); //ojokjokjopipoijpko
        }

        fileText = fileText.replaceAll(" +", " ");
        Matcher matcher = Pattern.compile("[^.]? \\w+ \\.").matcher(fileText); // uhihiuhi
        while (matcher.find()) {
            String str = matcher.group().substring(1, matcher.group().length() - 1).trim();
            if (!tables[1].ops.contains(str)) {
                tables[1].ops.add(str);
                tables[1].occurrences.add(countMatches(fileText, " " + str + " "));
                fileText = fileText.replaceAll(" " + str + " ", " ");
            }
        }
        return fileText;
    }

    private String collectPreFunctions(String fileText) {
        if (fileText.contains(" function ")) {
            tables[0].ops.add("function");
            tables[0].occurrences.add(0);
        }
        if (fileText.contains(" => ")) {
            tables[0].ops.add("=>");
            tables[0].occurrences.add(0);
        }
        int index = -1;
        while (fileText.indexOf(" function ", index + 1) > -1) {
            index = fileText.indexOf(" function ", index + 1) + 1;
            String functionPrototype;

            if (fileText.charAt(index - 2) == '=') {
                tables[0].occurrences.set(tables[0].ops.indexOf("function"), tables[0].occurrences.get(tables[0].ops.indexOf("function")) + 1);
                functionPrototype = fileText.substring(index + "function".length(), fileText.indexOf("{", index));
                fileText = fileText.substring(0, index - 1) + " " + fileText.substring(index + "function".length() + 1);
                String name = fileText.substring(fileText.lastIndexOf(" ", index - 4), index - 2).trim();
                tables[0].ops.add(name + "()");
                tables[0].occurrences.add(countMatches(fileText, " " + name + " "));
                fileText = fileText.replaceAll(" " + name + " \\(?", " ");

                String parameters = functionPrototype.substring(functionPrototype.indexOf("(") + 1, functionPrototype.indexOf(")")).replace("?", "");
                if (parameters.trim().length() > 0) {
                    fileText = collectParameters(fileText, parameters);
                }
            }
        }
        index = -1;
        while (fileText.indexOf(" => ", index + 1) > -1) {
            index = fileText.indexOf(" => ", index + 1);
            tables[0].occurrences.set(tables[0].ops.indexOf("=>"), tables[0].occurrences.get(tables[0].ops.indexOf("=>")) + 1);
            fileText = fileText.replaceFirst(" => ", " ");
            String parameters = fileText.substring(fileText.lastIndexOf("(", index), fileText.lastIndexOf(")", index)).replace("?", "");
            fileText = fileText.substring(0, fileText.lastIndexOf(")", index)) + " " + fileText.substring(index + " => ".length());

            if (fileText.charAt(index - 2) == '=') {
                String name = fileText.substring(fileText.lastIndexOf(" ", index - 4), index - 2).trim();
                tables[0].ops.add(name + "()");
                tables[0].occurrences.add(countMatches(fileText, " " + name + " "));
                fileText = fileText.replaceAll(name + " ?( \\()?", " ");

                if (parameters.trim().length() > 0) {
                    fileText = collectParameters(fileText, parameters);
                }
            }
        }
        return fileText;
    }

    private String collectPostFunctions(String fileText) {
        int index = -1;
        while (fileText.indexOf(" function ", index + 1) > -1) {
            index = fileText.indexOf(" function ", index + 1) + 1;
            tables[0].occurrences.set(tables[0].ops.indexOf("function"), tables[0].occurrences.get(tables[0].ops.indexOf("function")) + 1);
            String functionPrototype = fileText.substring(index + "function".length(), fileText.indexOf("{", index));
            String name = functionPrototype.substring(0, functionPrototype.indexOf("(")).trim();
            fileText = fileText.substring(0, index - 1) + " " + fileText.substring(index + "function".length() + 1);

            tables[0].ops.add(name + "()");
            tables[0].occurrences.add(countMatches(fileText, " " + name + " "));
            fileText = fileText.replaceAll(" " + name + " \\(?", " ");

            String parameters = functionPrototype.substring(functionPrototype.indexOf("(") + 1, functionPrototype.indexOf(")")).replace("?", "");
            if (parameters.trim().length() > 0) {
                fileText = collectParameters(fileText, parameters);
            }
        }
        return fileText;
    }

    private String collectParameters(String fileText, String parameters) {
        String variable = "";
        if (parameters.contains("...")) {
            variable = parameters.substring(parameters.indexOf("...") + 3).trim();
            if(variable.matches("\\w+")) {
                tables[1].ops.add(variable);
                tables[1].occurrences.add(countMatches(fileText, variable));
                fileText = fileText.replaceAll("... " + variable, " ");
            }
            if (parameters.contains(",")) parameters = parameters.substring(0, parameters.lastIndexOf(","));
        }

        while (parameters.contains(",")) {
            variable = parameters.substring(0, parameters.indexOf(",")).trim();
            if(variable.matches("\\w+")) {
                tables[1].ops.add(variable);
                tables[1].occurrences.add(countMatches(fileText, " " + variable + " "));
                parameters = parameters.substring(parameters.indexOf(",") + 1);
            } else {
                parameters = parameters.substring(parameters.indexOf(",") + 1);
            }
            fileText = fileText.replaceAll(" " + variable, " ");
        }
        variable = parameters.trim();
        if(variable.matches("\\w+")) {
            tables[1].ops.add(variable);
            tables[1].occurrences.add(countMatches(fileText, " " + variable + " "));
            fileText = fileText.replaceAll(" " + variable, " ");
        }
        return fileText;
    }

    private String collectStringLiterals(String fileText) {
        int beginSL = -1;
        while (fileText.indexOf("\"", beginSL + 1) > -1) {
            beginSL = fileText.indexOf("\"", beginSL + 1);
            if (fileText.charAt(beginSL - 1) != '\\' && !isInApostrophe(fileText, beginSL) && !isInQuotes(fileText, beginSL)) {
                int endSL = fileText.indexOf("\"", beginSL + 1);
                while (fileText.charAt(endSL - 1) == '\\') {
                    endSL = fileText.indexOf("\"", endSL + 1);
                }
                String sl = fileText.substring(beginSL, endSL + 1);
                tables[1].ops.add(sl);
                tables[1].occurrences.add(countMatches(fileText, sl));
                fileText = fileText.replace(sl, "");
            }
        }
        beginSL = -1;
        while (fileText.indexOf("`", beginSL + 1) > -1) {
            beginSL = fileText.indexOf("`", beginSL + 1);
            if (fileText.charAt(beginSL - 1) != '\\' && !isInDblQuotes(fileText, beginSL) && !isInQuotes(fileText, beginSL)) {
                int endSL = fileText.indexOf("`", beginSL + 1);
                while (fileText.charAt(endSL - 1) == '\\') {
                    endSL = fileText.indexOf("`", endSL + 1);
                }
                String sl = fileText.substring(beginSL, endSL + 1);
                tables[1].ops.add(sl);
                tables[1].occurrences.add(countMatches(fileText, sl));
                fileText = fileText.replace(sl, "");
            }
        }
        beginSL = -1;
        while (fileText.indexOf("'", beginSL + 1) > -1) {
            beginSL = fileText.indexOf("'", beginSL + 1);
            if (fileText.charAt(beginSL - 1) != '\\' && !isInDblQuotes(fileText, beginSL) && !isInApostrophe(fileText, beginSL)) {
                int endSL = fileText.indexOf("'", beginSL + 1);
                while (fileText.charAt(endSL - 1) == '\\') {
                    endSL = fileText.indexOf("'`'", endSL + 1);
                }
                String sl = fileText.substring(beginSL, endSL + 1);
                tables[1].ops.add(sl);
                tables[1].occurrences.add(countMatches(fileText, sl));
                fileText = fileText.replace(sl, "");
            }
        }
        return fileText;
    }

    private Integer countMatches(String fileText, String sl) {
        int count = 0;
        int index = fileText.indexOf(sl);
        while (fileText.indexOf(sl, index + 1) > -1) {
            count++;
            index = fileText.indexOf(sl, index + 1);
        }
        return count + 1;
    }

    private String collectOperands(String fileText, String status) {
        if (fileText.contains(" " + status + " ")) {
            tables[0].ops.add(status);
            tables[0].occurrences.add(0);
        }
        while (fileText.contains(" " + status + " ")) {
            int index = fileText.indexOf(" " + status + " ");

            String operands = fileText.substring(index, fileText.indexOf(";", index) + 1);
            fileText = fileText.replaceFirst(" " + status + " ", " ");
            tables[0].occurrences.set(tables[0].ops.indexOf(status), tables[0].occurrences.get(tables[0].ops.indexOf(status)) + 1);
            operands = operands.replaceFirst(" " + status + " ", " ");

            String operand;
            String variable;
            while (operands.contains(",")) {

                operand = operands.substring(0, operands.indexOf(","));

                if (operand.contains("=")) {
                    variable = operand.substring(0, operand.indexOf("=")).trim();
                } else {
                    variable = operand.trim();
                }

                variable = variable.replaceAll("( \\[ ])*", "").replaceAll(" ", "").trim(); //hihihijhijh

                if (variable.matches("\\w+")) {
                    tables[1].ops.add(variable);
                    tables[1].occurrences.add(countMatches(fileText, " " + variable + " "));
                    fileText = fileText.replace(" " + variable + " ", " ");
                }
                while (operands.contains(",") && isInBrackets(operands, operands.indexOf(","))) {
                    operands = operands.substring(operands.indexOf(",") + 1);
                    if (!operands.contains(",")) {
                        operands = "";
                    }
                }
                operands = operands.substring(operands.indexOf(",") + 1);
            }

            if (operands.length() > 0 && operands.trim().charAt(0) != '=') {

                if (operands.contains("=")) {
                    variable = operands.substring(0, operands.indexOf("=")).trim();
                } else {
                    variable = operands.substring(0, operands.indexOf(";")).trim();
                }
                variable = variable.replaceAll("(\\[ ])*", "").replaceAll(" ", "").trim();
                if (variable.matches("\\w+")) {
                    tables[1].ops.add(variable);
                    tables[1].occurrences.add(countMatches(fileText, " " + variable + " "));
                    fileText = fileText.replace(" " + variable + " ", " ");
                }
            }
        }
        return fileText;
    }

    private boolean isInBrackets(String operands, int commaIndex) {
        int count = 0;
        for (int i = commaIndex; i < operands.length(); i++) {
            if (operands.charAt(i) == ')') {
                count++;
            }
        }
        if (count % 2 == 1) {
            return true;
        } else {
            count = 0;
            for (int i = commaIndex; i < operands.length(); i++) {
                if (operands.charAt(i) == ']') {
                    count++;
                }
            }
            return count % 2 == 1;
        }
    }

    private String deleteTypes(String fileText) {
        int index = 0;
        while (fileText.indexOf(" enum ", index + 1) > -1) { // enums
            index = fileText.indexOf(" enum ", index + 1);
            fileText = fileText.substring(0, fileText.indexOf("{", index)) + fileText.substring(fileText.indexOf("}", index) + 1);
        }
        tables[0].ops.add("async");
        tables[0].occurrences.add(countMatches(fileText, "async "));
        fileText = fileText.replace("async ", " ");
        Iterator<String> nextType = types.iterator();
        while (nextType.hasNext()) {
            String next = nextType.next();
            fileText = fileText.replaceAll(" : " + next + "( \\[ ])*", " "); // other types
            fileText = fileText.replaceAll(" : \\[ \\w+( , \\w+)* ]", " "); //tuples
        }
        return fileText;
    }

    private String separateOperators(String fileText) {
        String next = "=";
        int index = fileText.indexOf(next);
        while (index > -1) {
            if (!isInString(fileText, index)) {
                fileText = fileText.substring(0, index) + " " + fileText.substring(index, index + next.length()) +
                        " " + fileText.substring(index + next.length());
            }
            index = fileText.indexOf(next, index + next.length() + 3);
        }
        fileText = fileText.replaceAll(" +", " ").replace(next + " " + next, next + next)
                .replace(next + " " + next + " " + next, next + next + next);

        Iterator<String> nextOp = operators.iterator();
        while (nextOp.hasNext()) {
            next = nextOp.next();
            index = fileText.indexOf(next);
            while (index > -1) {
                if (!isInString(fileText, index)) {
                    fileText = fileText.substring(0, index) + " " + fileText.substring(index, index + next.length()) +
                            " " + fileText.substring(index + next.length());
                }
                index = fileText.indexOf(next, index + next.length() + 3);
            }
            fileText = fileText.replaceAll(" +", " ").replace(next + " " + next, next + next)
                    .replace(next + next + " " + next, next + next + next)
                    .replace(next + " " + "=", next + "=")
                    .replace(next + " " + next + " " + next, next + next + next)
                    .replace(next + " " + next + "=", next + next + "=")
                    .replace(next + " ==", next + "==")
                    .replace(next + " " + next + " " + next + "=", next + next + next + "=");
        }
        return fileText.replaceAll(" +", " ").replaceAll("\n+", "\n");
    }

    private String inputCode(File opFile) throws FileNotFoundException {
        String fileText = "";
        Scanner in = new Scanner(opFile);
        while (in.hasNextLine()) {
            fileText = fileText + " " + in.nextLine() + "\n";
        }
        in.close();
        return fileText;
    }

    private String deleteComments(String fileText) {
        int beginComm = -1;
        int beginOneLineComm = -1;
        int endComm;
        while (fileText.lastIndexOf("/*") > beginComm || fileText.lastIndexOf("//") > beginOneLineComm) {
            beginComm = fileText.indexOf("/*", beginComm + 1);
            if (beginComm == -1) {
                beginComm = fileText.length() + 1;
            }
            endComm = fileText.indexOf("*/", beginComm);
            beginOneLineComm = fileText.indexOf("//", beginOneLineComm + 1);

            if (!isInString(fileText, beginOneLineComm) && ((beginOneLineComm < beginComm) || (beginOneLineComm > endComm))) {
                fileText = fileText.substring(0, beginOneLineComm) + fileText.substring(fileText.indexOf("\n", beginOneLineComm));
            }

            if (beginComm < fileText.length() && !isInString(fileText, beginComm)) {
                fileText = fileText.substring(0, beginComm) + fileText.substring(endComm + 2);
            }
        }
        return fileText;
    }

    private boolean isInString(String fileText, int target) {
        return isInApostrophe(fileText, target) || isInDblQuotes(fileText, target) || isInQuotes(fileText, target);
    }

    private boolean isInQuotes(String fileText, int target) {
        int count = 0;
        while (target > 0 && fileText.lastIndexOf('\'', target) > -1) {
            if (fileText.charAt(target) == '\'' && fileText.charAt(target - 1) != '\\' && !isInDblQuotes(fileText, target)) {
                count++;
            }
            target--;
        }
        if (fileText.charAt(0) == '\'') {
            count++;
        }
        return count % 2 == 1;
    }

    private boolean isInApostrophe(String fileText, int target) {
        int count = 0;
        while (target > 0 && fileText.lastIndexOf('`', target) > -1) {
            if (fileText.charAt(target) == '`' && fileText.charAt(target - 1) != '\\' && !isInDblQuotes(fileText, target)) {
                count++;
            }
            target--;
        }
        if (fileText.charAt(0) == '`') {
            count++;
        }
        return count % 2 == 1;
    }

    private boolean isInDblQuotes(String fileText, int target) {
        int count = 0;
        while (target > 0 && fileText.lastIndexOf('"', target) > -1) {
            if (fileText.charAt(target) == '"' && fileText.charAt(target - 1) != '\\' && !isInApostrophe(fileText, target)) {
                count++;
            }
            target--;
        }
        if (fileText.charAt(0) == '"') {
            count++;
        }
        return count % 2 == 1;
    }
}
