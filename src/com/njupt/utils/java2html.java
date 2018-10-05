package com.njupt.utils;

// java2html.java

import java.io.*;
import java.util.*;

/**
 * I found this litle gem at 
 * http://www.geocities.com/SiliconValley/Horizon/6481/Java2HTML.html
 * and modified it slightly to add unique colors for strings
 * and the two different comment types both of which are also italicized.
 * All default colors are websafe and can be customized by
 * specifying an optimal colors property file.
 *
 * @author Melinda Green - Superliminal Software
 */
public class java2html
{
    private static final String keywords[] = 
    {
        "abstract", "default",  "if",           "private",      "throw",
        "boolean",  "do",       "implements",   "protected",    "throws",
        "break",    "double",   "import",       "public",       "transient",
        "byte",     "else",     "instanceof",   "return",       "try",
        "case",     "extends",  "int",          "short",        "void",
        "catch",    "final",    "interface",    "static",       "volatile",
        "char",     "finally",  "long",         "super",        "while",
        "class",    "float",    "native",       "switch",
        "const",    "for",      "new",          "synchronized",
        "continue", "goto",     "package",      "this"
    };
    private static Vector keyw = new Vector(keywords.length);
    static 
    {
        for (int i = 0; i < keywords.length; i++)
            keyw.addElement(keywords[i]);
    }
    private static int tabsize = 4;
    private static String 
        bgcolor = "FFFFFF", // white
        txcolor = "000000", // black
        kwcolor = "3333FF", // light hard blue
        cmcolor = "009900", // dark faded green
        c2color = "999999", // light gray
        stcolor = "FF0000"; // red

    private static void convert(String source) throws IOException
    {
        String dest = source + ".html";
        System.out.println(dest);
        FileReader in = new FileReader(source);
        FileWriter out = new FileWriter(dest);
        out.write("<html>\r\n<head>\r\n<title>");
        out.write(source);
        out.write("</title>\r\n</head>\r\n<body ");
        out.write("bgcolor=\"" + bgcolor +"\" ");
        out.write("text=\"" + txcolor +"\">\r\n");
        out.write("<pre>\r\n");
        StringBuffer buf = new StringBuffer(2048);
        int c = 0, kwl = 0, bufl = 0;
        int nexttolast = 0; // just for handling case of >> "\\" << which is not escaping a double quote
        char ch = 0, lastch = 0;
        int s_normal  = 0;
        int s_string  = 1;
        int s_char    = 2;
        int s_comline = 3;
        int s_comment = 4;
        int state = s_normal;
        while (c != -1)
        {
            c = in.read();
            nexttolast = lastch;
            lastch = ch;
            ch = c >= 0 ? (char) c : 0;
            if (state == s_normal)
                if (kwl == 0 && Character.isJavaIdentifierStart(ch) 
                             && !Character.isJavaIdentifierPart(lastch)
                    || kwl > 0 && Character.isJavaIdentifierPart(ch))
                {
                    buf.append(ch);
                    bufl++;
                    kwl++;
                    continue;
                } else
                    if (kwl > 0)
                    {
                        String kw = buf.toString().substring(buf.length() - kwl);
                        if (keyw.contains(kw))
                        {
                            buf.insert(buf.length() - kwl, 
                                "<font color=\"" + kwcolor + "\">");
                            buf.append("</font>");
                        }
                        kwl = 0;
                    }
            switch (ch)
            {
                case '&':
                    buf.append("&amp;");
                    bufl++;
                    break;
                case '\"': // double quote
                    buf.append("&quot;");
                    bufl++;
                    if (state == s_normal) { // start string
                        state = s_string;
                        buf.insert(buf.length() - "&quot;".length(), 
                            "<font color=\"" + stcolor + "\"><i>");
                    }
                    else
                        if (state == s_string && ((lastch != '\\') || (lastch == '\\' && nexttolast == '\\'))) {
                            // inside a string and found either a non-escaped closing double quote,
                            // so close the string.
                            buf.append("</i></font>");
                            state = s_normal;
                        }
    
                    break;
                case '\'': // single quote
                    buf.append("\'");
                    bufl++;
                    if (state == s_normal)
                        state = s_char;
                    else
                        if (state == s_char && lastch != '\\')
                            state = s_normal;
                    break;
                case '\\': // backslash
                    buf.append("\\");
                    bufl++;
                    if(lastch == '\\')
                        nexttolast = '\\';
                    if (lastch == '\\' && (state == s_string || state == s_char))
                        ;//lastch = 0;
                    break;
                case '/': // forward slash
                    buf.append("/");
                    bufl++;
                    if(state == s_string || state == s_comline)
                        break;
                    if (state == s_comment && lastch == '*') // star slash ends c++ comment
                    {
                        buf.append("</i></font>");
                        state = s_normal;
                    }
                    if(state == s_comment)
                        break;
                    if (lastch == '/') // second forward slash starts line comment
                    {
                        buf.insert(buf.length() - 2, 
                            "<font color=\"" + cmcolor + "\"><i>");
                        state = s_comline;
                    }
                    break;
                case '*':
                    buf.append("*");
                    bufl++;
                    if (state == s_normal && lastch == '/') // slash star starts c++ comment
                    {
                        buf.insert(buf.length() - 2, 
                            "<font color=\"" + c2color + "\"><i>");
                        state = s_comment;
                    }
                    break;
                case '<':
                    buf.append("&lt;");
                    bufl++;
                    break;
                case '>':
                    buf.append("&gt;");
                    bufl++;
                    break;
                case '\t':
                    int n = bufl / tabsize * tabsize + tabsize;
                    while (bufl < n)
                    {
                        buf.append(' ');
                        bufl++;
                    }
                    break;
                case '\r':
                case '\n':
                    if (state == s_comline) // EOL ends line comment
                    {
                        buf.append("</i></font>");
                        state = s_normal;
                    }
                    buf.append(ch);
                    if (buf.length() >= 1024)
                    {
                        out.write(buf.toString());
                        buf.setLength(0);
                    }
                    bufl = 0;
                    if (kwl != 0)
                        kwl = 0; // This should never execute
                    if (state != s_normal && state != s_comment)
                        state = s_normal; // Syntax Error
                    break;
                case 0:
                    if (c < 0)
                    {
                        if (state == s_comline)
                        {
                            buf.append("</font>");
                            state = s_normal;
                        }
                        out.write(buf.toString());
                        buf.setLength(0);
                        bufl = 0;
                        if (state == s_comment)
                        {
                            // Syntax Error
                            buf.append("</font>");
                            state = s_normal;
                        }
                        break;
                    }
                default:
                    bufl++;
                    buf.append(ch);
            }
        }
        out.write("</pre>\r\n</body>\r\n</html>");
        in.close();
        out.close();
    }

    public static void main(String args[])
    {
        if (args.length < 1 || args.length > 2)
        {
            System.out.println("java2html converter + syntax coloring + tabs2spaces");
            System.out.println("");
            System.out.println("java  [java_opt]  java2html  [colors_file]  source");
            System.out.println("");
            System.out.println("  - java is the name of the Java interpreter");
            System.out.println("  - java_opt are the options of the Java interpreter");
            System.out.println("  - java2html is the name of this application");
            System.out.println("  - colors_file (optional) is the path ");
            System.out.println("    of a file which has a structure like this:");
            System.out.println("        tabsize=number  (default value is 4)");
            System.out.println("        bgcolor=RRGGBB  (default value is " + bgcolor + ") - background");
            System.out.println("        txcolor=RRGGBB  (default value is " + txcolor + ") - source code");
            System.out.println("        kwcolor=RRGGBB  (default value is " + kwcolor + ") - keywords");
            System.out.println("        cmcolor=RRGGBB  (default value is " + cmcolor + ") - // comments");
            System.out.println("        c2color=RRGGBB  (default value is " + c2color + ") - /* comments");
            System.out.println("        stcolor=RRGGBB  (default value is " + stcolor + ") - strings");
            System.out.println("  - source is a file or the directory to the Java source file(s)");
            System.out.println("");
            System.out.println("Examples:");
            System.out.println("    java  java2html  java2html.java");
            System.out.println("    java  java2html  C:\\TEMP");
            System.out.println("    java  java2html  java2html.properties  C:\\TEMP");
            System.exit(1);
        }
        String source, propfile;
        if (args.length == 2)
        {
            propfile = args[0];
            source = args[1];
        }
        else
        {
            propfile = "java2html.properties";
            source = args[0];
        }
        try
        {
            InputStream in = new FileInputStream(propfile);
            Properties prop = new Properties();
            prop.load(in);
            in.close();
            tabsize = Integer.parseInt(prop.getProperty("tabsize", "4"));
            bgcolor = "#" + prop.getProperty("bgcolor", bgcolor);
            txcolor = "#" + prop.getProperty("txcolor", txcolor);
            kwcolor = "#" + prop.getProperty("kwcolor", kwcolor);
            cmcolor = "#" + prop.getProperty("cmcolor", cmcolor);
            c2color = "#" + prop.getProperty("c2color", c2color);
            stcolor = "#" + prop.getProperty("stcolor", stcolor);
        }
        catch (FileNotFoundException e){}
        catch (IOException e) { System.out.println(e); }
        catch (NumberFormatException e) { System.out.println(e); }
        File f = new File(source);
        if (f.isFile())
        {
            try { convert(f.getPath()); }
            catch (IOException e) { System.out.println(e); }
        }
        else if (f.isDirectory())
        {
            try
            {
                String src[] = f.list();
                for (int i = 0; i < src.length; i++)
                    if (src[i].endsWith(".java"))
                        convert(new File(f, src[i]).getPath());
            }
            catch (IOException e) { System.out.println(e); }
        } 
        else
        {
            System.out.println("The source parameter must be an existent file or directory");
            System.out.println("Run java2html without parameters for help");
        }
    }

}

