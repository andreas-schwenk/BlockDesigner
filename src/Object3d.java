/*
 * BlockDesigner
 *
 * (c) 2010 Andreas Schwenk
 * Licensed under the MIT License
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.jogamp.opengl.*;

public class Object3d {
    public class Token {
        String str;
        int srcLine;

        Token(String str, int srcLine) {
            this.str = str;
            this.srcLine = srcLine;
        }
    }

    public class INT2 {
        double x, y;

        public INT2() {
        }

        public INT2(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public INT2 clone() {
            return new INT2(x, y);
        }
    }

    public class VECTOR3 {
        double x, y, z;

        public VECTOR3() {
        }

        public VECTOR3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public VECTOR3 clone() {
            return new VECTOR3(x, y, z);
        }

        public Math3d.Double3 getDouble3() {
            return new Math3d.Double3(x, y, z);
        }
    }

    public class TEXCO2 {
        boolean rect;
        ArrayList<INT2> list;

        TEXCO2() {
            list = new ArrayList<INT2>();
            rect = false;
        }

        @Override
        public TEXCO2 clone() {
            TEXCO2 clone = new TEXCO2();
            clone.rect = rect;
            for (Iterator<INT2> it = list.iterator(); it.hasNext();)
                clone.list.add(((INT2) it.next()).clone());
            return clone;
        }
    }

    public class FACE {
        ArrayList<Integer> indicesVert;
        int textureCooIndex;

        FACE() {
            indicesVert = new ArrayList<Integer>();
            textureCooIndex = 0;
        }

        @Override
        public FACE clone() {
            FACE clone = new FACE();
            clone.textureCooIndex = textureCooIndex;
            for (Iterator<Integer> it = indicesVert.iterator(); it.hasNext();)
                clone.indicesVert.add((((Integer) it.next())));
            return clone;
        }
    }

    public class Sub {
        int id;
        String name;
        String caption;
        Math3d.Double3 scale;
        Math3d.Double3 translation;
        int textureSize;
        String textureFilename;
        ArrayList<VECTOR3> vertices;
        ArrayList<TEXCO2> textureCoordinates;
        ArrayList<FACE> faces;
        int glID = -1;

        public Sub() {
            name = "";
            caption = "";
            textureFilename = "";
            scale = new Math3d.Double3(1.0, 1.0, 1.0);
            translation = new Math3d.Double3(0.0, 0.0, 0.0);
            textureSize = 512;
            vertices = new ArrayList<VECTOR3>();
            textureCoordinates = new ArrayList<TEXCO2>();
            faces = new ArrayList<FACE>();
        }

        @Override
        public Sub clone() {
            Sub clone = new Sub();
            clone.id = id;
            clone.name = new String(name);
            clone.caption = new String(caption);
            clone.scale = scale.clone();
            clone.translation = translation.clone();
            clone.textureSize = textureSize;
            clone.textureFilename = new String(textureFilename);
            for (Iterator<VECTOR3> it = vertices.iterator(); it.hasNext();)
                clone.vertices.add(((VECTOR3) it.next()).clone());
            for (Iterator<TEXCO2> it = textureCoordinates.iterator(); it.hasNext();)
                clone.textureCoordinates.add(((TEXCO2) it.next()).clone());
            for (Iterator<FACE> it = faces.iterator(); it.hasNext();)
                clone.faces.add(((FACE) it.next()).clone());
            clone.glID = glID;
            return clone;
        }
    }

    // ***** ATTRIBUTES *****

    private ArrayList<Sub> subs = null;
    private Sub currentSub = null;
    private ArrayList<Token> tokens = null;
    private int tokenPos = 0;
    private int id;
    private static int idCounter = 0;

    public int getID() {
        return id;
    }

    public Object3d() {
        id = idCounter++;
    }

    public int getSubLength() {
        return subs.size();
    }

    public String getSubCaption(int i) {
        return subs.get(i).caption;
    }

    public int getSubId(int i) {
        return subs.get(i).id;
    }

    /**
     * only y-axis rotation implemented yet!!!!!
     */
    public Math3d.AABB getAABB(int subId, double rx, double ry, double rz) {
        ry = ry / 360.0 * 2.0 * Math.PI;
        Math3d.AABB aabb = new Math3d.AABB();
        Sub sub;
        for (Iterator<Sub> it = subs.iterator(); it.hasNext();) {
            sub = (Sub) it.next();
            if (sub.id == subId) {
                Math3d.Double3 vertex;
                boolean firstVertex = true;
                for (Iterator<VECTOR3> itVer = sub.vertices.iterator(); itVer.hasNext();) {
                    vertex = ((VECTOR3) itVer.next()).getDouble3().clone();
                    vertex.x = vertex.x * sub.scale.x + sub.translation.x;
                    vertex.y = vertex.y * sub.scale.y + sub.translation.y;
                    vertex.z = vertex.z * sub.scale.z + sub.translation.z;
                    // rotate vertex:
                    double tmpX = vertex.x * Math.cos(-ry) - vertex.z * Math.sin(-ry);
                    vertex.z = vertex.x * Math.sin(-ry) + vertex.z * Math.cos(-ry);
                    vertex.x = tmpX;
                    if (firstVertex || vertex.x < aabb.min.x)
                        aabb.min.x = vertex.x;
                    if (firstVertex || vertex.y < aabb.min.y)
                        aabb.min.y = vertex.y;
                    if (firstVertex || vertex.z < aabb.min.z)
                        aabb.min.z = vertex.z;
                    if (firstVertex || vertex.x > aabb.max.x)
                        aabb.max.x = vertex.x;
                    if (firstVertex || vertex.y > aabb.max.y)
                        aabb.max.y = vertex.y;
                    if (firstVertex || vertex.z > aabb.max.z)
                        aabb.max.z = vertex.z;
                    firstVertex = false;
                }
                break;
            }
        }
        return aabb;
    }

    public int getSubIndexFromSubId(int subId) {
        Sub sub;
        int retVal = 0;
        for (Iterator<Sub> it = subs.iterator(); it.hasNext();) {
            sub = (Sub) it.next();
            if (sub.id == subId)
                return retVal;
            retVal++;
        }
        return 0;
    }

    public final void renderBySubId(GL2 gl, int subId, double x, double y, double z, double rx, double ry, double rz) {
        Sub sub;
        for (Iterator<Sub> it = subs.iterator(); it.hasNext();) {
            sub = (Sub) it.next();
            if (sub.id == subId) {
                if (sub.glID == -1)
                    build(gl);
                gl.glPushMatrix();
                gl.glTranslated(x, y, z);
                if (rx != 0)
                    gl.glRotated(rx, 1, 0, 0);
                if (ry != 0)
                    gl.glRotated(ry, 0, 1, 0);
                if (rz != 0)
                    gl.glRotated(rz, 0, 0, 1);
                gl.glCallList(sub.glID);
                gl.glPopMatrix();
                break;
            }
        }
    }

    public final void renderBySubIndex(GL2 gl, int subIndex, double x, double y, double z, double rx, double ry,
            double rz) {
        Sub sub = subs.get(subIndex);
        if (sub == null)
            return;
        if (sub.glID == -1)
            build(gl);
        gl.glPushMatrix();
        gl.glTranslated(x, y, z);
        if (rx != 0)
            gl.glRotated(rx, 1, 0, 0);
        if (ry != 0)
            gl.glRotated(ry, 0, 1, 0);
        if (rz != 0)
            gl.glRotated(rz, 0, 0, 1);
        gl.glCallList(sub.glID);
        gl.glPopMatrix();
    }

    private void err(String str, int line) {
        System.out.println("Object3d: Parsing-Error in line: " + line + " (" + str + ")");
    }

    private void err(String str) {
        System.out.println("Object3d: Error: " + str);
    }

    // ********** LEXER **********

    private void lex(BufferedReader buff) throws IOException {
        char character;
        boolean inComment = false, inString = false;
        String line;
        int lineNo = 0;
        int currentTokenLineNo = 0;
        String currentTokenStr = "";
        while (buff.ready()) {
            line = buff.readLine();
            lineNo++;
            for (int i = 0; i < line.length(); i++) {
                character = line.charAt(i);
                if (!inComment && (character == '/' && i <= (line.length() - 2) && line.charAt(i + 1) == '/')) {
                    if (currentTokenStr.length() > 0)
                        tokens.add(new Token(currentTokenStr, currentTokenLineNo));
                    currentTokenStr = "";
                    break;
                } else if (!inComment && (character == '/' && i <= (line.length() - 2) && line.charAt(i + 1) == '*')) {
                    if (currentTokenStr.length() > 0)
                        tokens.add(new Token(currentTokenStr, currentTokenLineNo));
                    currentTokenStr = "";
                    inComment = true;
                } else if (character == '*' && i <= (line.length() - 2) && line.charAt(i + 1) == '/') {
                    i++;
                    inComment = false;
                } else if (!inComment && character == '"') {
                    if (!inString) {
                        if (currentTokenStr.length() > 0)
                            tokens.add(new Token(currentTokenStr, currentTokenLineNo));
                        currentTokenStr = "" + character;
                        currentTokenLineNo = lineNo;
                        inString = true;
                    } else {
                        currentTokenStr = currentTokenStr + character;
                        currentTokenLineNo = lineNo;
                        inString = false;
                    }
                } else if (!inComment && !inString && (character == ' ' || character == '\n' || character == '\t')) {
                    if (currentTokenStr.length() > 0)
                        tokens.add(new Token(currentTokenStr, currentTokenLineNo));
                    currentTokenStr = "";
                } else if (!inComment && !inString && (character == '{' || character == '}'
                        || character == '(' || character == ')'
                        || character == '[' || character == ']'
                        || character == ',' || character == ';'
                        || character == ':' || character == '='
                        || character == '+' || character == '-'
                        || character == '*' || character == '/'
                        || character == '%' || character == '.')) {
                    if (currentTokenStr.length() > 0)
                        tokens.add(new Token(currentTokenStr, currentTokenLineNo));
                    currentTokenStr = "" + character;
                    currentTokenLineNo = lineNo;
                    tokens.add(new Token(currentTokenStr, currentTokenLineNo));
                    currentTokenStr = "";
                } else if (!inComment) {
                    currentTokenStr = currentTokenStr + character;
                    currentTokenLineNo = lineNo;
                }
            }
        }
    }

    // ********** PARSER **********

    // <NUMBER> ::= '0'|'1'|...|'9'
    private boolean isNUMBER(char ch) {
        if (ch >= '0' && ch <= '9')
            return true;
        return false;
    }

    // <LETTER> ::= 'a'|'b'|...|'z'|'A'|'B'|...|'Z'|'_'
    private boolean isLETTER(char ch) {
        if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z')
            return true;
        return false;
    }

    // <INT> ::= <NUMBER>{<NUMBER>}
    private Object parseINT() {
        int res;
        int startTkPos = tokenPos;
        String tkStr;
        boolean ok;
        // ** <NUMBER>{<NUMBER>} **
        ok = true;
        res = 0;
        tokenPos = startTkPos;
        tkStr = tokens.get(tokenPos).str;
        if (tkStr.length() > 0) {
            char ch;
            for (int i = 0; i < tkStr.length(); i++) {
                ch = tkStr.charAt(i);
                if (isNUMBER(ch)) {
                    res *= 10;
                    res += ch - '0';
                } else {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                tokenPos++;
                return res;
            }
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return null;
    }

    // <INT2> ::= <INT>','<INT>
    private INT2 parseINT2() {
        INT2 res = new INT2();
        int startTkPos = tokenPos;
        Object tmp;
        // ** <INT>','<INT> **
        res.x = res.y = 0;
        tokenPos = startTkPos;
        if ((tmp = parseINT()) != null) {
            res.x = (Integer) tmp;
            if (tokens.get(tokenPos).str.equals(",")) {
                tokenPos++;
                if ((tmp = parseINT()) != null) {
                    res.y = (Integer) tmp;
                    return res;
                }
            }
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return null;
    }

    // <DEC> ::= ['-']<INT>'.'<INT>|['-']<INT>
    private Object parseDEC() {
        double res;
        boolean resNeg;
        int startTkPos = tokenPos;
        Object tmp;
        // ** ['-']<INT>'.'<INT> **
        res = 0.0;
        tokenPos = startTkPos;
        resNeg = false;
        if (tokens.get(tokenPos).str.equals("-")) {
            tokenPos++;
            resNeg = true;
        }
        if ((tmp = parseINT()) != null) {
            res += (Integer) tmp;
            if (tokens.get(tokenPos).str.equals(".")) {
                tokenPos++;
                if ((tmp = parseINT()) != null) {
                    if ((Integer) tmp != 0)
                        res += (Integer) tmp / Math.pow(10, Math.ceil(Math.log10((Integer) tmp)));
                    if (resNeg)
                        res = -res;
                    return res;
                }
            }
        }
        // ** ['-']<INT> **
        res = 0.0;
        tokenPos = startTkPos;
        resNeg = false;
        if (tokens.get(tokenPos).str.equals("-")) {
            tokenPos++;
            resNeg = true;
        }
        if ((tmp = parseINT()) != null) {
            res += (Integer) tmp;
            if (resNeg)
                res = -res;
            return res;
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return null;
    }

    // <VECTOR3> ::= <DEC>','<DEC>','<DEC>
    private VECTOR3 parseVECTOR3() {
        VECTOR3 res = new VECTOR3();
        int startTkPos = tokenPos;
        Object tmp;
        // ** <DEC>','<DEC>','<DEC> **
        res.x = res.y = res.z = 0.0;
        tokenPos = startTkPos;
        if ((tmp = parseDEC()) != null) {
            res.x = (Double) tmp;
            if (tokens.get(tokenPos).str.equals(",")) {
                tokenPos++;
                if ((tmp = parseDEC()) != null) {
                    res.y = (Double) tmp;
                    if (tokens.get(tokenPos).str.equals(",")) {
                        tokenPos++;
                        if ((tmp = parseDEC()) != null) {
                            res.z = (Double) tmp;
                            return res;
                        }
                    }
                }
            }
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return null;
    }

    // <IDENTIFIER> ::= <LETTER>{<LETTER>|<NUMBER>}
    private String parseIDENTIFIER() {
        String res;
        int startTkPos = tokenPos;
        String tkStr;
        boolean ok;
        // ** <LETTER>{<LETTER>|<NUMBER>} **
        ok = true;
        res = "";
        tokenPos = startTkPos;
        tkStr = tokens.get(tokenPos).str;
        if (tkStr.length() >= 1 && isLETTER(tkStr.charAt(0))) {
            for (int i = 1; i < tkStr.length(); i++) {
                if (!isLETTER(tkStr.charAt(i)) && !isNUMBER(tkStr.charAt(i)))
                    ok = false;
            }
            if (ok) {
                tokenPos++;
                res = tkStr;
                return res;
            }
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return null;
    }

    // <STRING> ::= '"'{<ASCII>}'"'
    private String parseSTRING() {
        String res;
        int startTkPos = tokenPos;
        String tkStr;
        // ** possibility I **
        res = "";
        tokenPos = startTkPos;
        tkStr = tokens.get(tokenPos).str;
        if (tkStr.length() >= 2 && tkStr.charAt(0) == '"' && tkStr.charAt(tkStr.length() - 1) == '"') {
            tokenPos++;
            res = tkStr.substring(1, tkStr.length() - 1);
            return res;
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return null;
    }

    // <STATEMENT> ::= 'ID''='<INTEGER>';'|'CAPTION''='<STRING>';'
    // |'SCALE''='<VECTOR3>';'|'TEX_SIZE'=<INTEGER>';'
    // |'TEX_FILE''='<STRING>';'|'TRANSLATION''='<VECTOR3>';'
    private boolean parseSTATEMENT() {
        int startTkPos = tokenPos;
        Object tmp;
        // ** ID''='<INTEGER>';' **
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("ID")) {
            tokenPos++;
            if (tokens.get(tokenPos).str.equals("=")) {
                tokenPos++;
                if ((tmp = parseINT()) != null) {
                    if (tokens.get(tokenPos).str.equals(";")) {
                        tokenPos++;
                        generateSubId((Integer) tmp);
                        return true;
                    }
                }
            }
        }
        // ** 'CAPTION''='<STRING>';' **
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("CAPTION")) {
            tokenPos++;
            if (tokens.get(tokenPos).str.equals("=")) {
                tokenPos++;
                if ((tmp = parseSTRING()) != null) {
                    if (tokens.get(tokenPos).str.equals(";")) {
                        tokenPos++;
                        generateSubCaption((String) tmp);
                        return true;
                    }
                }
            }
        }
        // ** 'SCALE''='<VECTOR3>'; **
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("SCALE")) {
            tokenPos++;
            if (tokens.get(tokenPos).str.equals("=")) {
                tokenPos++;
                if ((tmp = parseVECTOR3()) != null) {
                    if (tokens.get(tokenPos).str.equals(";")) {
                        tokenPos++;
                        generateSubScale((VECTOR3) tmp);
                        return true;
                    }
                }
            }
        }
        // ** 'TEX_SIZE'=<INTEGER>';' **
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("TEX_SIZE")) {
            tokenPos++;
            if (tokens.get(tokenPos).str.equals("=")) {
                tokenPos++;
                if ((tmp = parseINT()) != null) {
                    if (tokens.get(tokenPos).str.equals(";")) {
                        tokenPos++;
                        generateSubTextureSize((Integer) tmp);
                        return true;
                    }
                }
            }
        }
        // ** 'TEX_FILE''='<STRING> **
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("TEX_FILE")) {
            tokenPos++;
            if (tokens.get(tokenPos).str.equals("=")) {
                tokenPos++;
                if ((tmp = parseSTRING()) != null) {
                    if (tokens.get(tokenPos).str.equals(";")) {
                        tokenPos++;
                        generateSubTextureFilename((String) tmp);
                        return true;
                    }
                }
            }
        }
        // ** 'TRANSLATION''='<VECTOR3>';' **
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("TRANSLATION")) {
            tokenPos++;
            if (tokens.get(tokenPos).str.equals("=")) {
                tokenPos++;
                if ((tmp = parseVECTOR3()) != null) {
                    if (tokens.get(tokenPos).str.equals(";")) {
                        tokenPos++;
                        generateSubTranslation((VECTOR3) tmp);
                        return true;
                    }
                }
            }
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return false;
    }

    // <INDEX> ::= "["<INT>"]"
    private Object parseINDEX() {
        int res;
        int startTkPos = tokenPos;
        Object tmp;
        // ** "["<INT>"]" **
        res = 0;
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("[")) {
            tokenPos++;
            if ((tmp = parseINT()) != null) {
                res = (Integer) tmp;
                if (tokens.get(tokenPos).str.equals("]")) {
                    tokenPos++;
                    return res;
                }
            }
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return null;
    }

    // <VERTICES> ::= 'VERT''{'{[<INDEX>]<VECTOR3>';'}'}'
    private boolean parseVERTICES() {
        int startTkPos = tokenPos;
        Object tmp;
        int index;
        boolean ok;
        // ** 'VERT''{'{[<INDEX>]<VECTOR3>';'}'}' **
        ok = true;
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("VERT")) {
            tokenPos++;
            if (tokens.get(tokenPos).str.equals("{")) {
                tokenPos++;
                while (true) {
                    index = -1;
                    if (tokens.get(tokenPos).str.equals("}")) {
                        tokenPos++;
                        break;
                    }
                    if ((tmp = parseINDEX()) != null) {
                        index = (Integer) tmp;
                    }
                    if ((tmp = parseVECTOR3()) == null || !tokens.get(tokenPos).str.equals(";")) {
                        ok = false;
                        break;
                    } else {
                        tokenPos++;
                        generateSubVertex(index, (VECTOR3) tmp);
                    }
                }
                if (ok)
                    return true;
                else
                    err("error: parseVERTICES()", tokens.get(tokenPos).srcLine);
            } else
                err("error: parseVERTICES()", tokens.get(tokenPos).srcLine);
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return false;
    }

    // <TEXCO2> ::= <INT2>':'<INT2>|<INT2>','<INT2>','<INT2>{','<INT2>}
    private TEXCO2 parseTEXCO2() {
        TEXCO2 res;
        int startTkPos = tokenPos;
        Object tmp;
        boolean ok;
        // ** <INT2>':'<INT2> **
        res = new TEXCO2();
        ok = true;
        tokenPos = startTkPos;
        if ((tmp = parseINT2()) != null) {
            res.list.add((INT2) tmp);
            if (tokens.get(tokenPos).str.equals(":")) {
                tokenPos++;
                res.rect = true;
                if ((tmp = parseINT2()) != null) {
                    res.list.add((INT2) tmp);
                    return res;
                }
            }
        }
        // <INT2>','<INT2>','<INT2>{','<INT2>}
        res = new TEXCO2();
        ok = true;
        tokenPos = startTkPos;
        if ((tmp = parseINT2()) != null) {
            res.list.add((INT2) tmp);
            if (tokens.get(tokenPos).str.equals(",")) {
                tokenPos++;
                if ((tmp = parseINT2()) != null) {
                    res.list.add((INT2) tmp);
                    if (tokens.get(tokenPos).str.equals(",")) {
                        tokenPos++;
                        if ((tmp = parseINT2()) != null) {
                            res.list.add((INT2) tmp);
                            while (tokens.get(tokenPos).str.equals(",")) {
                                tokenPos++;
                                if ((tmp = parseINT2()) != null) {
                                    res.list.add((INT2) tmp);
                                } else {
                                    ok = false;
                                    break;
                                }
                            }
                            if (ok) {
                                return res;
                            }
                        }
                    }
                }
            }
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return null;
    }

    // <TEXTURECOORDS> ::= 'TEX_COO''{'{[<INDEX>]<TEXCO2>';'}'}'
    private boolean parseTEXTURECOORDS() {
        int startTkPos = tokenPos;
        Object tmp;
        int index;
        boolean ok;
        // ** 'TEX_COO''{'{[<INDEX>]<TEXCO2>';'}'}' **
        ok = true;
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("TEX_COO")) {
            tokenPos++;
            if (tokens.get(tokenPos).str.equals("{")) {
                tokenPos++;
                while (true) {
                    index = -1;
                    if (tokens.get(tokenPos).str.equals("}")) {
                        tokenPos++;
                        break;
                    }
                    if ((tmp = parseINDEX()) != null) {
                        index = (Integer) tmp;
                    }
                    if ((tmp = parseTEXCO2()) == null || !tokens.get(tokenPos).str.equals(";")) {
                        ok = false;
                        break;
                    } else {
                        tokenPos++;
                        generateSubTextureCoordinate(index, (TEXCO2) tmp);
                    }
                }
                if (ok)
                    return true;
                else
                    err("error: parseTEXTURECOORDS()", tokens.get(tokenPos).srcLine);
            } else
                err("error: parseTEXTURECOORDS()", tokens.get(tokenPos).srcLine);
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return false;
    }

    // <FACE> ::= <INT>','<INT>','<INT>{','<INT>}
    private FACE parseFACE() {
        FACE res;
        int startTkPos = tokenPos;
        Object tmp;
        boolean ok;
        // <INT>','<INT>','<INT>{','<INT>}
        res = new FACE();
        ok = true;
        tokenPos = startTkPos;
        if ((tmp = parseINT()) != null) {
            res.indicesVert.add((Integer) tmp);
            if (tokens.get(tokenPos).str.equals(",")) {
                tokenPos++;
                if ((tmp = parseINT()) != null) {
                    res.indicesVert.add((Integer) tmp);
                    if (tokens.get(tokenPos).str.equals(",")) {
                        tokenPos++;
                        if ((tmp = parseINT()) != null) {
                            res.indicesVert.add((Integer) tmp);
                            while (tokens.get(tokenPos).str.equals(",")) {
                                tokenPos++;
                                if ((tmp = parseINT()) != null) {
                                    res.indicesVert.add((Integer) tmp);
                                } else {
                                    ok = false;
                                    break;
                                }
                            }
                            if (ok) {
                                return res;
                            }
                        }
                    }
                }
            }
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return null;
    }

    // <FACES> ::= 'FACES''{'{[<INDEX>]<FACE>';'<INT>';'}'}'
    private boolean parseFACES() {
        int startTkPos = tokenPos;
        Object tmp;
        int index;
        FACE face;
        boolean ok;
        // ** 'FACES''{'{[<INDEX>]<FACE>';'<INT>';'}'}' **
        ok = true;
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("FACES")) {
            tokenPos++;
            if (tokens.get(tokenPos).str.equals("{")) {
                tokenPos++;
                while (true) {
                    index = -1;
                    if (tokens.get(tokenPos).str.equals("}")) {
                        tokenPos++;
                        break;
                    }
                    if ((tmp = parseINDEX()) != null) {
                        index = (Integer) tmp;
                    }
                    if ((tmp = parseFACE()) == null || !tokens.get(tokenPos).str.equals(";")) {
                        ok = false;
                        break;
                    } else {
                        tokenPos++;
                        face = (FACE) tmp;
                    }
                    if ((tmp = parseINT()) == null || !tokens.get(tokenPos).str.equals(";")) {
                        ok = false;
                        break;
                    } else {
                        tokenPos++;
                        face.textureCooIndex = (Integer) tmp;
                        generateSubFace(index, face);
                    }
                }
                if (ok)
                    return true;
                else
                    err("error: parseFACES()", tokens.get(tokenPos).srcLine);
            } else
                err("error: parseFACES()", tokens.get(tokenPos).srcLine);
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return false;
    }

    // <DEFINITION> ::= <VERTICES>|<TEXTURECOORDS>|<FACES>
    private boolean parseDEFINITION() {
        int startTkPos = tokenPos;
        boolean ok;
        // ** <VERTICES> **
        ok = true;
        tokenPos = startTkPos;
        if (parseVERTICES())
            return true;
        // ** <TEXTURECOORDS> **
        if (parseTEXTURECOORDS())
            return true;
        // ** <FACES> **
        if (parseFACES())
            return true;
        // ** no possibility **
        tokenPos = startTkPos;
        return false;
    }

    // <OBJECT> ::=
    // 'OBJ'<IDENTIFIER>['CLONEOF'<IDENTIFIER>]'{'{<STATEMENT>|<DEFINITION>}'}'
    private boolean parseOBJECT() {
        int startTkPos = tokenPos;
        Object tmp;
        boolean ok;
        // ** 'OBJ'<IDENTIFIER>['CLONEOF'<IDENTIFIER>]'{'{<STATEMENT>|<DEFINITION>}'}'
        // **
        ok = true;
        tokenPos = startTkPos;
        if (tokens.get(tokenPos).str.equals("OBJ")) {
            tokenPos++;
            if ((tmp = parseIDENTIFIER()) != null) {
                if (!generateNewSub((String) tmp)) {
                    err("parseOBJECT(): obj-name already exists", tokens.get(tokenPos).srcLine);
                    tokenPos = startTkPos;
                    return false;
                }
                if (tokens.get(tokenPos).str.equals("CLONEOF")) {
                    tokenPos++;
                    if ((tmp = parseIDENTIFIER()) == null || !generateClone((String) tmp)) {
                        tokenPos = startTkPos;
                        err("parseOBJECT(): cloning failed (check spelling of src-obj)", tokens.get(tokenPos).srcLine);
                        return false;
                    }
                }
                if (tokens.get(tokenPos).str.equals("{")) {
                    tokenPos++;
                    while (!tokens.get(tokenPos).str.equals("}")) {
                        if (parseSTATEMENT())
                            continue;
                        if (parseDEFINITION())
                            continue;
                        err("parseOBJECT(): error", tokens.get(tokenPos).srcLine);
                        ok = false;
                        break;
                    }
                    if (ok) {
                        tokenPos++;
                        return true;
                    }
                }
            }
        }
        // ** no possibility **
        tokenPos = startTkPos;
        return false;
    }

    // ********** GENERATOR **********

    private void generateSubId(int id) {
        if (currentSub != null)
            currentSub.id = id;
        else
            err("error: generateSubId(..): no currentSub", tokens.get(tokenPos).srcLine);
    }

    private void generateSubCaption(String caption) {
        if (currentSub != null)
            currentSub.caption = caption;
        else
            err("error: generateSubCaption(..): no currentSub", tokens.get(tokenPos).srcLine);
    }

    private void generateSubScale(VECTOR3 vec3) {
        if (currentSub != null) {
            currentSub.scale.x = vec3.x;
            currentSub.scale.y = vec3.y;
            currentSub.scale.z = vec3.z;
        } else
            err("error: generateScale(..): no currentSub", tokens.get(tokenPos).srcLine);
    }

    private void generateSubTranslation(VECTOR3 vec3) {
        if (currentSub != null) {
            currentSub.translation.x = vec3.x;
            currentSub.translation.y = vec3.y;
            currentSub.translation.z = vec3.z;
        } else
            err("error: generateSubTranslation(..): no currentSub", tokens.get(tokenPos).srcLine);
    }

    private void generateSubTextureSize(int textureSize) {
        if (currentSub != null)
            currentSub.textureSize = textureSize;
        else
            err("error: generateSubTextureSize(..): no currentSub", tokens.get(tokenPos).srcLine);
    }

    private void generateSubTextureFilename(String textureFilename) {
        if (currentSub != null)
            currentSub.textureFilename = textureFilename;
        else
            err("error: generateSubTextureFilename(..): no currentSub", tokens.get(tokenPos).srcLine);
    }

    private void generateSubVertex(int index, VECTOR3 vec) {
        if (currentSub != null) {
            if (index == -1)
                currentSub.vertices.add(vec);
            else if (index < currentSub.vertices.size())
                currentSub.vertices.set(index, vec);
            else
                err("error: generateSubVertex(..): invalid index (check also, if OBJ is clone)",
                        tokens.get(tokenPos).srcLine);
        } else
            err("error: generateSubVertex(..): no currentSub", tokens.get(tokenPos).srcLine);
    }

    private void generateSubTextureCoordinate(int index, TEXCO2 texco) {
        if (currentSub != null) {
            if (index == -1)
                currentSub.textureCoordinates.add(texco);
            else if (index < currentSub.textureCoordinates.size())
                currentSub.textureCoordinates.set(index, texco);
            else
                err("error: generateSubTextureCoordinate(..): invalid index (check also, if OBJ is clone)",
                        tokens.get(tokenPos).srcLine);
        } else
            err("error: generateSubTextureCoordinate(..): no currentSub", tokens.get(tokenPos).srcLine);
    }

    private void generateSubFace(int index, FACE face) {
        if (currentSub != null) {
            if (index == -1)
                currentSub.faces.add(face);
            else if (index < currentSub.faces.size())
                currentSub.faces.set(index, face);
            else
                err("error: generateSubFace(..): invalid index (check also, if OBJ is clone)",
                        tokens.get(tokenPos).srcLine);
        } else
            err("error: generateSubFace(..): no currentSub", tokens.get(tokenPos).srcLine);
    }

    private boolean generateNewSub(String name) {
        Sub sub;
        for (Iterator<Sub> it = subs.iterator(); it.hasNext();) {
            sub = (Sub) it.next();
            if (sub.name.equals(name))
                return false;
        }
        sub = new Sub();
        sub.name = name;
        subs.add(sub);
        currentSub = sub;
        return true;
    }

    private boolean generateClone(String sourceName) {
        if (currentSub != null) {
            Sub sub;
            int indexCurrentSub = 0;
            for (Iterator<Sub> it = subs.iterator(); it.hasNext();) {
                sub = (Sub) it.next();
                if (sub == currentSub)
                    break;
                indexCurrentSub++;
            }
            for (Iterator<Sub> it = subs.iterator(); it.hasNext();) {
                sub = (Sub) it.next();
                if (sub.name.equals(sourceName) && sub != currentSub) {
                    currentSub = sub.clone();
                    currentSub.name = subs.get(indexCurrentSub).name;
                    subs.set(indexCurrentSub, currentSub);
                    return true;
                }
            }
            err("error: generateClone(..): source-obj not found", tokens.get(tokenPos).srcLine);
        } else
            err("error: generateClone(..): no currentSub", tokens.get(tokenPos).srcLine);
        return false;
    }

    public void build(GL2 gl) {
        Math3d.Double3 vec0 = new Math3d.Double3();
        Math3d.Double3 vec1 = new Math3d.Double3();
        Math3d.Double3 vec2 = new Math3d.Double3();
        Math3d.Double3 norm0 = new Math3d.Double3();
        Math3d.Double2 tex0 = new Math3d.Double2();
        Math3d.Double2 tex1 = new Math3d.Double2();
        Math3d.Double2 tex2 = new Math3d.Double2();

        Sub sub;
        for (Iterator<Sub> it = this.subs.iterator(); it.hasNext();) {
            sub = (Sub) it.next();
            sub.glID = gl.glGenLists(1);
            gl.glNewList(sub.glID, GL2.GL_COMPILE);
            gl.glBegin(GL2.GL_TRIANGLES);

            FACE face;
            for (Iterator<FACE> it2 = sub.faces.iterator(); it2.hasNext();) {
                face = (FACE) it2.next();
                for (int i = 1; i < face.indicesVert.size() - 1; i++) {
                    tex0.x = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(0).x + 0.5)
                            / (double) sub.textureSize;
                    tex0.y = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(0).y + 0.5)
                            / (double) sub.textureSize;
                    if (sub.textureCoordinates.get(face.textureCooIndex).rect) {
                        if (i == 1) {
                            tex1.x = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(0).x + 0.5)
                                    / (double) sub.textureSize;
                            tex1.y = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(1).y + 0.5)
                                    / (double) sub.textureSize;
                            tex2.x = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(1).x + 0.5)
                                    / (double) sub.textureSize;
                            tex2.y = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(1).y + 0.5)
                                    / (double) sub.textureSize;
                        } else if (i == 2) {
                            tex1.x = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(1).x + 0.5)
                                    / (double) sub.textureSize;
                            tex1.y = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(1).y + 0.5)
                                    / (double) sub.textureSize;
                            tex2.x = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(1).x + 0.5)
                                    / (double) sub.textureSize;
                            tex2.y = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(0).y + 0.5)
                                    / (double) sub.textureSize;
                        } else {
                            err("build(..): too many triangles for texcoo");
                        }
                    } else {
                        tex1.x = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(i).x + 0.5)
                                / (double) sub.textureSize;
                        tex1.y = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(i).y + 0.5)
                                / (double) sub.textureSize;
                        tex2.x = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(i + 1).x + 0.5)
                                / (double) sub.textureSize;
                        tex2.y = ((double) sub.textureCoordinates.get(face.textureCooIndex).list.get(i + 1).y + 0.5)
                                / (double) sub.textureSize;
                    }

                    vec0.x = sub.vertices.get(face.indicesVert.get(0)).x;
                    vec0.y = sub.vertices.get(face.indicesVert.get(0)).y;
                    vec0.z = sub.vertices.get(face.indicesVert.get(0)).z;
                    vec1.x = sub.vertices.get(face.indicesVert.get(i)).x;
                    vec1.y = sub.vertices.get(face.indicesVert.get(i)).y;
                    vec1.z = sub.vertices.get(face.indicesVert.get(i)).z;
                    vec2.x = sub.vertices.get(face.indicesVert.get(i + 1)).x;
                    vec2.y = sub.vertices.get(face.indicesVert.get(i + 1)).y;
                    vec2.z = sub.vertices.get(face.indicesVert.get(i + 1)).z;

                    norm0 = Math3d.Double3.crossProduct(Math3d.Double3.sub(vec1, vec0), Math3d.Double3.sub(vec2, vec0));
                    norm0.normalize();

                    gl.glTexCoord2d(tex0.x, 1.0 - tex0.y);
                    gl.glNormal3d(norm0.x, norm0.y, norm0.z);
                    gl.glVertex3d(vec0.x * sub.scale.x + sub.translation.x, vec0.y * sub.scale.y + sub.translation.y,
                            vec0.z * sub.scale.z + sub.translation.z);

                    gl.glTexCoord2d(tex1.x, 1.0 - tex1.y);
                    gl.glNormal3d(norm0.x, norm0.y, norm0.z);
                    gl.glVertex3d(vec1.x * sub.scale.x + sub.translation.x, vec1.y * sub.scale.y + sub.translation.y,
                            vec1.z * sub.scale.z + sub.translation.z);

                    gl.glTexCoord2d(tex2.x, 1.0 - tex2.y);
                    gl.glNormal3d(norm0.x, norm0.y, norm0.z);
                    gl.glVertex3d(vec2.x * sub.scale.x + sub.translation.x, vec2.y * sub.scale.y + sub.translation.y,
                            vec2.z * sub.scale.z + sub.translation.z);
                }
            }
            gl.glEnd();
            gl.glEndList();
        }
    }

    public boolean loadFromFile(String filename) {
        subs = new ArrayList<Sub>();

        // ***** LEX *****
        try {
            // FileReader fileReader = new FileReader(filename);
            // BufferedReader buff = new BufferedReader(fileReader);
            InputStream is = GuiMainFrame.class.getResourceAsStream("/" + filename);
            BufferedReader buff = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            tokens = new ArrayList<Token>();

            lex(buff);

            // buff.close();
            // fileReader.close();
        } catch (IOException e) {
            err("loadFromFile(..) failed");
            return false;
        }
        // ***** PARSE and GENERATE *****
        while (tokenPos < tokens.size() && parseOBJECT()) {
        }

        // ***** BUILD *****
        // build(gl);

        return true;
    }
}
