package app;

import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChepinAnalyzer {

    private Table table;
    private double a1, a2, a3, a4;

    private final String[] codeSymbols = new String[]{"[", "]", "{", "}", "(", ")", ";", ",", ".", "\"", "'", "`",
            "?", "~", ":",
            "!==", "!=", "!",
            "/=", "/", "%=", "%", "^=", "^", "-=", "--", "-", "+=", "++", "+", "**=", "*=", "**", "*",
            "&&=", "&=", "&&", "&",
            "||=", "|=", "||", "|",
            ">>>", ">>>=",
            "<<=", "<<", "<=", "<",
            ">>=", ">>", ">=", ">",
            "===", "==", "="};

    private final HashSet<String> types = new HashSet<>(Arrays.asList("boolean", "number", "string",
            "any", "null", "undefined", "void", "never"));

    //============================================ANALYZE EXECUTING============================================
    public Table analyze(File opFile) throws FileNotFoundException {
        table = new Table();
        a1 = 1;
        a2 = 2;
        a3 = 3;
        a4 = 0.5;
        table.fileName = opFile.getAbsolutePath();
        parseToElements(opFile);
        return table;
    }

    public Table analyze(File opFile, double a1, double a2, double a3, double a4) throws FileNotFoundException {
        table = new Table();
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.a4 = a4;
        table.fileName = opFile.getAbsolutePath();
        parseToElements(opFile);
        return table;
    }

    //============================================CODE PARSING============================================
    private void parseToElements(File opFile) throws FileNotFoundException {
        String text = "\0\0\0\0\0 " + inputCode(opFile) + " \0\0\0\0\0";
        text = deleteComments(text);
        text = collectStringLiterals(text);
        text = separateLexemes(text);
        text = collectTypes(text);
        text = collectOperands(text);
        defineTypes(text);
        countMetrics();
    }

    // -------------------------------------------INPUT CODE-------------------------------------------
    private String inputCode(File opFile) throws FileNotFoundException {
        String text = "";
        Scanner in = new Scanner(opFile);
        while (in.hasNextLine()) {
            text = text + " " + in.nextLine() + "\n";
        }
        in.close();
        return text;
    }

    //-------------------------------------------DELETE COMMENTS-------------------------------------------
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

    //-------------------------------------------COLLECT STRINGS-------------------------------------------
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

    //-------------------------------------------SEPARATE LEXEMES-------------------------------------------
    private String separateLexemes(String text) {
        int index;
        for (String codeSymbol : codeSymbols) {
            index = nextIndexOfLexeme(text, codeSymbol, -1);
            while (index > -1) {
                text = separateWith(text, " ", index, index + codeSymbol.length());
                index = nextIndexOfLexeme(text, codeSymbol, index + 1 + " ".length());
            }
        }
        return text.replaceAll(" +", " ");
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

    private String separateWith(String text, String separator, int begin, int end) {
        return text.substring(0, begin) + separator + text.substring(begin, end) + separator + text.substring(end);
    }

    //-------------------------------------------COLLECT TYPES-------------------------------------------
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

    //-------------------------------------------COLLECT OPERANDS-------------------------------------------
        //--------------------------PARAMETERS--------------------------
    private String collectOperands(String text) {
        collectVariables(text);
        text = collectParameters(text);
        return text;
    }

    private String collectParameters(String text) {
        int index = -1;
        ArrayList<Integer> trashFuncs = new ArrayList<>();
        while (text.indexOf(" function ", index + 1) > -1) {
            index = text.indexOf(" function ", index + 1) + 1;
            String functionPrototype = text.substring(index + "function ".length(), text.indexOf("{", index));
            String name = getFunctionName(text, functionPrototype);
            boolean isTrashFunc = countMatches(text, " " + name + " ") == 1;
            if (isTrashFunc) trashFuncs.add(index);
            String parameters = functionPrototype.substring(functionPrototype.indexOf("(") + 1,
                    functionPrototype.lastIndexOf(")")).trim();

            if (parameters.length() == 0) continue;

            int commaIndex;
            while ((commaIndex = parameters.indexOf(",")) > -1) {
                String parameter = parameters.substring(0, commaIndex).trim();
                MyTableRow var = findVarInTable(parameter);
                if (var == null) {
                    table.ops.add(new MyTableRow(table.ops.size() + 1, parameter, countSpen(text, parameter),
                            (isTrashFunc || countSpen(text, parameter) == 0) ? MyTableRow.types.T : MyTableRow.types.M));
                    table.isIO.add(false);
                } else {
                    if (var.type.getValue().equals("T")) {
                        var.type.set(((isTrashFunc || var.spen.getValue().equals("0")) ? MyTableRow.types.T : MyTableRow.types.M) + "");
                    }
                }
                parameters = parameters.substring(commaIndex + 1);
            }

            String parameter = parameters.trim();
            MyTableRow var = findVarInTable(parameter);
            if (var == null) {
                table.ops.add(new MyTableRow(table.ops.size() + 1, parameter, countSpen(text, parameter),
                        (isTrashFunc || countSpen(text, parameter) == 0) ? MyTableRow.types.T : MyTableRow.types.M));
                table.isIO.add(false);
            } else {
                if (var.type.getValue().equals("T")) {
                    var.type.set(((isTrashFunc || var.spen.getValue().equals("0")) ? MyTableRow.types.T : MyTableRow.types.M) + "");
                }
            }
        }

        index = -1;
        while (text.indexOf(" = > ", index + 1) > -1) {
            index = text.indexOf(" = > ", index + 1);
            String functionPrototype = text.substring(index, index + " = > ".length());
            String name = getFunctionName(text, functionPrototype);
            //text = text.replaceFirst(" = > ", " ");

            boolean isTrashFunc = countMatches(text, " " + name + " ") == 1;
            if (isTrashFunc) trashFuncs.add(index);
            String parameters;

            if (isMultiParameter(text, index)) {
                parameters = getMultiParameters(text, index);
            } else {
                parameters = getAloneParameter(text, index);
            }

            int commaIndex;
            while ((commaIndex = parameters.indexOf(",")) > -1) {
                String parameter = parameters.substring(0, commaIndex).trim();
                MyTableRow var = findVarInTable(parameter);
                if (var == null) {
                    table.ops.add(new MyTableRow(table.ops.size() + 1, parameter, countSpen(text, parameter),
                            (isTrashFunc || countSpen(text, parameter) == 0) ? MyTableRow.types.T : MyTableRow.types.M));
                    table.isIO.add(false);
                } else {
                    if (var.type.getValue().equals("T")) {
                        var.type.set(((isTrashFunc || var.spen.getValue().equals("0")) ? MyTableRow.types.T : MyTableRow.types.M) + "");
                    }
                }
                parameters = parameters.substring(commaIndex + 1);
            }

            String parameter = parameters.trim();
            MyTableRow var = findVarInTable(parameter);
            if (var == null) {
                table.ops.add(new MyTableRow(table.ops.size() + 1, parameter, countSpen(text, parameter),
                        (isTrashFunc || countSpen(text, parameter) == 0) ? MyTableRow.types.T : MyTableRow.types.M));
                table.isIO.add(false);
            } else {
                if (var.type.getValue().equals("T")) {
                    var.type.set(((isTrashFunc || var.spen.getValue().equals("0")) ? MyTableRow.types.T : MyTableRow.types.M) + "");
                }
            }
        }

        return deleteTrashFunctions(text, trashFuncs);
    }

    private String getAloneParameter(String text, int index) {
        int endIndex;
        while (text.charAt(index) == ' ') {
            index--;
        }
        endIndex = index;
        while (text.charAt(index) != ' ') {
            index--;
        }
        return text.substring(index + 1, endIndex + 1);
    }

    private String getMultiParameters(String text, int index) {
        int closeBracket = text.length();
        while (text.charAt(index) != '(') {
            if (text.charAt(index) == ')') {
                closeBracket = index;
            }
            index--;
        }
        return text.substring(index + 1, closeBracket);
    }

    private boolean isMultiParameter(String text, int index) {
        while (text.charAt(index) != ')') {
            index--;
            if (text.charAt(index) == '=') return false;
        }
        return true;
    }

    private String getFunctionName(String text, String functionPrototype) {
        if (functionPrototype.equals(" = > ")) {
            int i = text.indexOf(functionPrototype) - 1;
            while (i > -1 && text.charAt(i) != ' ') i--;
            String name = text.substring(text.lastIndexOf(' ', i) + 1, i + 1);
            if (name.matches("\\w+")) {
                return name;
            } else {
                return "";
            }
        }

        if (functionPrototype.matches("(\\(.*\\))|(\\w+)")) {
            int i = text.indexOf(functionPrototype) - 1;
            while (i > -1 && text.charAt(i) != ' ') i--;
            String name = text.substring(text.lastIndexOf(' ', i) + 1, i + 1);
            if (name.matches("\\w+")) {
                return name;
            } else {
                return "";
            }
        } else {
            return functionPrototype.substring(0, functionPrototype.indexOf("(")).trim();
        }
    }

        //--------------------------VARIABLES--------------------------

    private void collectVariables(String text) {
        Matcher matcher = Pattern.compile(" (let)|(var)|(const)|(enum) ").matcher(text);
        while (matcher.find()) {
            String line = text.substring(matcher.start(), text.indexOf(";", matcher.start()) + 1);
            Matcher lineMatcher = Pattern.compile("(,|( (let)|(var)|(const)|(enum) ))[ ]*\\w+").matcher(line);
            while (lineMatcher.find()) {
                String variable = lineMatcher.group().substring(lineMatcher.group().lastIndexOf(" ") + 1).trim();
                if (!isFunction(line, variable) && findVarInTable(variable) == null) {
                    table.ops.add(new MyTableRow(table.ops.size() + 1, variable, countSpen(text, variable), MyTableRow.types.T));
                    table.isIO.add(false);
                }
            }
        }
    }

    private int countSpen(String text, String variable) {
        return countMatches(text, " " + variable + " ") - 1;
    }

    private boolean isFunction(String line, String variable) {
        line = line.replaceAll("\\(.*\\)", "");
        int varIndex = line.indexOf(variable);
        int separatorIndex = line.indexOf(",", varIndex) > -1 ? line.indexOf(",", varIndex) : line.length() - 1;
        int arrowFuncIndex = line.indexOf(" = > ", varIndex);
        int preFuncIndex = line.indexOf(" function ", varIndex);
        return (arrowFuncIndex != -1 && arrowFuncIndex < separatorIndex) || (preFuncIndex != -1 && preFuncIndex < separatorIndex);
    }

    //-------------------------------------------DEFINE TYPES-------------------------------------------
    private void defineTypes(String text) {
        defineM(text);
        defineC(text);
        defineP(text);
    }

        //--------------------------TYPE M--------------------------
    private void defineM(String text) {
        int index = 0;
        for (MyTableRow var : table.ops) {
            if (isOutputable(text, var.identifier.getValue())) {
                table.isIO.set(index, true);
                var.type.set(MyTableRow.types.M + "");
                findInteractingWith(text, var.identifier.getValue());
            }
            index++;
        }
    }

    private boolean isOutputable(String text, String identifier) {
        Matcher matcher = Pattern.compile(" (alert)|(log)[ ]*\\(").matcher(text);
        while (matcher.find()) {
            int blockCloser = findIndexOfBlockCloser(text, matcher.end() + 1, "(", ")");
            String outText = text.substring(matcher.start(), blockCloser);
            if (outText.contains(" " + identifier + " ")) return true;
        }

        matcher = Pattern.compile(" return ").matcher(text);
        while (matcher.find()) {
            if (text.substring(matcher.end() - 1, text.indexOf(";", matcher.end())).contains(" " + identifier + " "))
                return true;
        }
        return false;
    }

    private void findInteractingWith(String text, String name) {
        Pattern pattern = Pattern.compile(" " + name + " (\\[[ \\w]+] )*[ ]*(([/%^\\-+*&| ]|(\\*\\*)|(&&)|(\\|\\|)|(>>>)|(<<)|(>>))?=)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            int expStart = findExpStart(text, matcher.start());
            int expEnd = findExpEnd(text, matcher.start());
            String exp = text.substring(expStart, expEnd + 1);

            if (exp.contains(" function ") || exp.contains(" = > ")) continue;
            defineVarTypesIn(exp, name, text);
        }
    }

    private int findExpEnd(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            switch (text.charAt(i)) {
                case ';':
                case '{':
                case '}':
                    return i;
            }
        }
        return 0;
    }

    private int findExpStart(String text, int start) {
        for (int i = start; i > -1; i--) {
            switch (text.charAt(i)) {
                case ';':
                case '{':
                case '}':
                    return i;
            }
        }
        return 0;
    }

    private void defineVarTypesIn(String exp, String mainName, String text) {
        for (MyTableRow var : table.ops) {
            if (var.typeProperty().getValue().equals(mainName)) continue;
            if (var.type.getValue().equals("T") && exp.contains(" " + var.identifier.getValue() + " ")) {
                var.type.set(MyTableRow.types.M + "");
                findInteractingWith(text, var.identifier.getValue());
            }
        }
    }

        //--------------------------TYPE C--------------------------
    private void defineC(String text) {
        // if
        ArrayList<Block> trashBlocks = new ArrayList<>();
        Pattern pattern = Pattern.compile(" if ");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            defineVarTypesOfControl(text, matcher.start(), trashBlocks);
        }
        // switch
        pattern = Pattern.compile(" switch ");
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            defineVarTypesOfControl(text, matcher.start(), trashBlocks);
        }
        // for
        pattern = Pattern.compile(" for ");
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            defineVarTypesOfControl(text, matcher.start(), trashBlocks);
        }
        // while
        // do while
        pattern = Pattern.compile(" while ");
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            defineVarTypesOfWhileControl(text, matcher.start(), trashBlocks);
        }
        // ? :
        pattern = Pattern.compile(" \\? ");
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            defineVarTypesOfTernaryControl(text, matcher.start(), trashBlocks);
        }
        for (Block block : trashBlocks) {
            deleteInterval(text, block.begin, block.end);
        }
    }

    private void defineVarTypesOfControl(String text, int start, ArrayList<Block> trashBlocks) {
        int begin = text.indexOf("(", start);
        int end = findIndexOfBlockCloser(text, begin, "(", ")");
        String condition = text.substring(begin, end + 1);

        if (isTrashCondition(condition)) {
            if (isInlineFunc(text, end)) {
                begin = end;
                end = text.indexOf(";", begin);
            } else {
                begin = text.indexOf("{", end);
                end = findIndexOfBlockCloser(text, begin, "{", "}");
            }

            if (!isTrashBlock(text, begin, end, trashBlocks)) {
                defineVarTypesOfCondition(text, condition);
            }
        } else {
            defineVarTypesOfCondition(text, condition);
        }
    }

    private void defineVarTypesOfWhileControl(String text, int start, ArrayList<Block> trashBlocks) {
        int begin = text.indexOf("(", start);
        int end = findIndexOfBlockCloser(text, begin, "(", ")");
        String condition = text.substring(begin, end + 1);

        if (isTrashCondition(condition)) {
            if (isDoWhile(text, start)) {
                end = text.lastIndexOf("}", begin);
                begin = findIndexOfBlockCloser(text, end, "}", "{", -1);
            } else {
                begin = text.indexOf("{", end);
                end = findIndexOfBlockCloser(text, begin, "{", "}");
            }

            if (!isTrashBlock(text, begin, end, trashBlocks)) {
                defineVarTypesOfCondition(text, condition);
            }
        } else {
            defineVarTypesOfCondition(text, condition);
        }
    }

    private boolean isDoWhile(String text, int start) {
        return text.substring(start).matches("[ ]*while[ ]*\\(.*\\);");
    }

    private void defineVarTypesOfTernaryControl(String text, int start, ArrayList<Block> trashBlocks) {
        int begin = findTernaryConditionStart(text, start);
        int end = text.indexOf("?", start);
        String condition = text.substring(begin, end);

        if (isTrashCondition(condition)) {
            begin = end;
            end = findIndexOfBlockCloser(text, begin, "?", ":");

            for (int i = 0; i < text.length(); i++) {
                switch (text.charAt(i)) {
                    case ';':
                    case ')':
                        end = i;
                }
            }

            if (!isTrashBlock(text, begin, end, trashBlocks)) {
                defineVarTypesOfCondition(text, condition);
            }
        } else {
            defineVarTypesOfCondition(text, condition);
        }
    }

    private int findTernaryConditionStart(String text, int start) {
        int blockCount = 0;
        boolean condFounded = false;
        for (int i = start; i > -1; i--) {
            if (blockCount > 0) {
                switch (text.charAt(i)) {
                    case ')':
                        blockCount++;
                        break;
                    case '(':
                        blockCount--;
                        if (blockCount == 0 && !condFounded) return i;
                }
            } else {
                switch (text.charAt(i)) {
                    case ')':
                        blockCount++;
                        break;
                    case '=':
                    case '>':
                    case '<':
                    case '|':
                    case '&':
                    case '!':
                    case '~':
                    case '^':
                    case '(':
                    case '{':
                    case '[':
                        if (condFounded) {
                            return i;
                        }
                        condFounded = true;
                        i = text.lastIndexOf(" ", i);
                }
            }
        }
        return -1;
    }

    private boolean isTrashCondition(String condition) {
        for (MyTableRow var : table.ops) {
            if (!var.type.getValue().equals("T") && condition.contains(" " + var.identifier.getValue() + " ")) {
                return false;
            }
        }
        return true;
    }

    private boolean isTrashBlock(String text, int blockBegin, int blockEnd, ArrayList<Block> trashBlocks) {
        if (hasNotTrashVars(text.substring(blockBegin, blockEnd + 1))) {
            return false;
        } else {
            trashBlocks.add(new Block(blockBegin, blockEnd));
            return true;
        }
    }

    private void defineVarTypesOfCondition(String text, String condition) {
        for (MyTableRow var : table.ops) {
            if (condition.contains(" " + var.identifier.getValue() + " ")) {
                var.type.set("C");
                findInteractingWith(text, var.identifier.getValue());
            }
        }
    }

    private String deleteInterval(String text, Integer begin, int end) {
        return text.substring(0, begin) + " " + text.substring(end);
    }

        //--------------------------TYPE P--------------------------
    private void defineP(String text) {
        int index = 0;
        for (MyTableRow var : table.ops) {
            if (isInputable(text, var.identifier.getValue())){
                table.isIO.set(index, true);
            if(!var.type.getValue().equals("T") && !var.type.getValue().equals("C")) {
                    var.type.set(MyTableRow.types.P + "");
            }
            }
            index++;
        }
    }

    private boolean isInputable(String text, String identifier) {
        Matcher matcher = Pattern.compile(" " + identifier + "[ ]*=[^;)\\]]* prompt[ ]*\\(").matcher(text);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    //-------------------------------------------COUNT METRICS-------------------------------------------
    private void countMetrics() {
        int index = 0;
        for (MyTableRow var : table.ops) {
            table.sumSpen += Integer.parseInt(var.spen.getValue());
            switch (var.type.getValue()) {
                case "P":
                    if(table.isIO.get(index)) {
                        table.PIO++;
                    }
                    table.P++;
                    break;
                case "M":
                    if(table.isIO.get(index)) {
                        table.MIO++;
                    }
                    table.M++;
                    break;
                case "C":
                    if(table.isIO.get(index)) {
                        table.CIO++;
                    }
                    table.C++;
                    break;
                case "T":
                    if(table.isIO.get(index)) {
                        table.TIO++;
                    }
                    table.T++;
                    break;
            }
            index++;
        }
        table.sumSpenIO = table.sumSpen;
        table.Q = a1 * table.P + a2 * table.M + a3 * table.C + a4 * table.T;
        table.QIO = a1 * table.PIO + a2 * table.MIO + a3 * table.CIO + a4 * table.TIO;
    }

    //-------------------------------------------SUPPORT FUNCTIONS-------------------------------------------
        //DELETING
    private String deleteSubstring(String text, int begin, int length) {
        return text.substring(0, begin) + " " + text.substring(begin + length);
    }

    private String deleteTrashFunctions(String text, ArrayList<Integer> trashFuncs) {
        for (int i = trashFuncs.size() - 1; i > -1; i--) {
            if (isInlineFunc(text, trashFuncs.get(i))) {
                text = deleteSubstring(text, trashFuncs.get(i), text.indexOf(";", trashFuncs.get(i)));
            } else {
                int funcEnd = findIndexOfBlockCloser(text, text.indexOf("{", trashFuncs.get(i)), "{", "}");
                text = deleteInterval(text, trashFuncs.get(i), funcEnd);
            }
        }
        return text;
    }

        //SEARCHING
    private MyTableRow findVarInTable(String parameter) {
        for (MyTableRow var : table.ops) {
            if (var.identifier.getValue().equals(parameter)) return var;
        }
        return null;
    }

    private int findIndexOfBlockCloser(String text, int start, String blockOpener, String blockCloser) {
        int openCloseCount = 1;
        for (int i = start + 1; i < text.length(); i++) {
            if (isSubstringOnStr(text, blockOpener, i)) {
                openCloseCount++;
            } else {
                if (isSubstringOnStr(text, blockCloser, i)) {
                    openCloseCount--;
                    if (openCloseCount == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private int findIndexOfBlockCloser(String text, int start, String blockOpener, String blockCloser, int iterator) {
        int openCloseCount = 1;
        for (int i = start + iterator; i < text.length(); i = i + iterator) {
            if (isSubstringOnStr(text, blockOpener, i)) {
                openCloseCount++;
            } else {
                if (isSubstringOnStr(text, blockCloser, i)) {
                    openCloseCount--;
                    if (openCloseCount == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

        //CHECK OF HAVING
    private boolean hasNotTrashVars(String block) {
        if (block.contains(" alert ") || block.contains(" log ") || block.contains(" continue ") || block.contains(" break ") || block.contains(" goto "))
            return true;
        for (MyTableRow var : table.ops) {
            if (!var.type.getValue().equals("T") && block.contains(" " + var.identifier.getValue() + " ")) return true;
        }
        return false;
    }

        //COUNT
    private Integer countMatches(String text, String sl) {
        if (text.contains(sl)) {
            int count = 1;
            int index = text.indexOf(sl);
            while (text.indexOf(sl, index + sl.length()) > -1) {
                count++;
                index = text.indexOf(sl, index + sl.length());
            }
            return count;
        } else {
            return 0;
        }
    }

        //CHECK
    private boolean isInlineFunc(String text, Integer funcStart) {
        int dotComma = text.indexOf(";", funcStart);
        dotComma = dotComma == -1 ? Integer.MAX_VALUE : dotComma;
        int bracket = text.indexOf("{", funcStart);
        bracket = bracket == -1 ? Integer.MAX_VALUE : bracket;
        return bracket > dotComma;
    }

    private boolean isSubstringOnStr(String text, String blockCloser, int i) {
        return blockCloser.equals(text.substring(i, i + blockCloser.length()));
    }

    private boolean isInArray(String[] codeSymbols, String check) {
        for (String codeSymbol : codeSymbols) {
            if (codeSymbol.equals(check)) return true;
        }
        return false;
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

    //-------------------------------------------CLASSES-------------------------------------------
    public static class Table {
        public ArrayList<MyTableRow> ops;
        public ArrayList<Boolean> isIO;
        public int P, M, C, T, sumSpen;
        public double Q;
        public int PIO, MIO, CIO, TIO, sumSpenIO;
        public double QIO;
        public String fileName;

        public Table() {
            P = M = C = T = sumSpen = 0;
            Q = 0;
            PIO = MIO = CIO = TIO = sumSpenIO = 0;
            QIO = 0;
            fileName = "";
            ops = new ArrayList<>();
            isIO = new ArrayList<>();
        }
    }

    public static class MyTableRow {

        public MyTableRow(String index, String identifier, String spen, String type) {
            this.index = new SimpleStringProperty(index);
            this.identifier = new SimpleStringProperty(identifier);
            this.spen = new SimpleStringProperty(spen);
            this.type = new SimpleStringProperty(type);
        }

        public enum types {P, M, C, T};

        SimpleStringProperty index;
        SimpleStringProperty identifier;
        SimpleStringProperty spen;
        SimpleStringProperty type;

        MyTableRow(int index, String identifier, int spen, MyTableRow.types type) {
            this.index = new SimpleStringProperty(index + "");
            this.identifier = new SimpleStringProperty(identifier);
            this.spen = new SimpleStringProperty(spen + "");
            this.type = new SimpleStringProperty(type + "");
        }

        public SimpleStringProperty indexProperty() {
            return index;
        }

        public SimpleStringProperty identifierProperty() {
            return identifier;
        }

        public SimpleStringProperty spenProperty() {
            return spen;
        }

        public SimpleStringProperty typeProperty() {
            return type;
        }
    }

    class Block {
        private int begin, end;

        Block(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }
    }
}