   package mars.assembler;

   import java.util.ArrayList;

/*
Copyright (c) 2003-2012,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * Class representing MIPS assembler directives.  If Java had enumerated types, these
 * would probably be implemented that way.  Each directive is represented by a unique object.
 * The directive name is indicative of the directive it represents.  For example, DATA
 * represents the MIPS .data directive.
 * 
 * @author Pete Sanderson
 * @version August 2003
 **/

    public final class Directives {
   
      private static ArrayList directiveList = new ArrayList();
      public static final Directives DATA   = new Directives(".data", "存储在下一个可用地址的数据段中的后续项");
      public static final Directives TEXT   = new Directives(".text", "存储在下一个可用地址的文本段中的后续项(指令)");
      public static final Directives WORD   = new Directives(".word", "将列出的值存储为32位的word");
      public static final Directives ASCII  = new Directives(".ascii", "将字符串存储在数据段中，但不添加空终止符");
      public static final Directives ASCIIZ = new Directives(".asciiz", "将字符串存储在数据段中并添加空终止符");
      public static final Directives BYTE   = new Directives(".byte", "将列出的值存储为8位的byte");
      public static final Directives ALIGN  = new Directives(".align", "将下一个数据字节对齐 (0=byte, 1=half, 2=word, 3=double)");
      public static final Directives HALF   = new Directives(".half", "将列出的值存储为16位的halfword");
      public static final Directives SPACE  = new Directives(".space", "预留数据段中的下一个指定字节数");
      public static final Directives DOUBLE = new Directives(".double", "将列出的值存储为双精度浮点");
      public static final Directives FLOAT  = new Directives(".float", "将列出的值存储为单精度浮点");
      public static final Directives EXTERN = new Directives(".extern", "声明列出的标签和字节长度为全局数据字段");
      public static final Directives KDATA  = new Directives(".kdata", "后续项目存储在内核数据段中的下一个可用地址");
      public static final Directives KTEXT  = new Directives(".ktext", "后续项目(指令)存储在内核文本段中下一个可用地址的");
      public static final Directives GLOBL  = new Directives(".globl", "将列出的标签声明为全局标签，以从其他文件进行引用");
      public static final Directives SET    = new Directives(".set", "设置汇编程序变量。 当前已忽略，但为了SLIM兼容性而被包括在内");
      /*  EQV added by DPS 11 July 2012 */
      public static final Directives EQV    = new Directives(".eqv", "将第二个操作符替换为第一个。第一个操作符是符号，第二个操作符是表达式 (类似 #define)");
      /* MACRO and END_MACRO added by Mohammad Sekhavat Oct 2012 */    
      public static final Directives MACRO  = new Directives(".macro", "开始宏定义。  参见 .end_macro");
      public static final Directives END_MACRO = new Directives(".end_macro", "结束宏定义。  参见 .macro");
      /*  INCLUDE added by DPS 11 Jan 2013 */
      public static final Directives INCLUDE   = new Directives(".include", "插入指定文件的内容。将文件名放在引号中。");
   
   
      private String descriptor;
      private String description; // help text
   
       private Directives() {
      // private ctor assures no objects can be created other than those above.
         this.descriptor  = "generic";
         this.description = "";
         directiveList.add(this);
      }
   
       private Directives(String name, String description) {
         this.descriptor  = name;
         this.description = description;
         directiveList.add(this);
      }
   
   /**
    * Find Directive object, if any, which matches the given String.
    * 
    * @param str A String containing candidate directive name (e.g. ".ascii")
    * @return If match is found, returns matching Directives object, else returns <tt>null</tt>.
    **/
    
       public static Directives matchDirective(String str) {
         Directives match;
         for (int i=0; i<directiveList.size(); i++) {
            match = (Directives) directiveList.get(i);
            if (str.equalsIgnoreCase(match.descriptor)) {
               return match;
            }
         }
         return null;
      }
   
   
   /**
    * Find Directive object, if any, which contains the given string as a prefix. For example,
    * ".a" will match ".ascii", ".asciiz" and ".align"
    * 
    * @param str A String 
    * @return If match is found, returns ArrayList of matching Directives objects, else returns <tt>null</tt>.
    **/
    
       public static ArrayList prefixMatchDirectives(String str) {
         ArrayList matches = null;
         for (int i=0; i<directiveList.size(); i++) {
            if (((Directives) directiveList.get(i)).descriptor.toLowerCase().startsWith(str.toLowerCase())) {
               if (matches == null) {
                  matches = new ArrayList();
               }
               matches.add(directiveList.get(i));
            }
         }
         return matches;
      }
   
   
   /**
   * Produces String-ified version of Directive object
   * 
   * @return String representing Directive: its MIPS name
   **/
   
       public String toString() {
         return this.descriptor;
      }
   
   
   /**
   * Get name of this Directives object
   * 
   * @return name of this MIPS directive as a String
   **/
   
       public String getName() {
         return this.descriptor;
      }
   
   /**
   * Get description of this Directives object
   * 
   * @return description of this MIPS directive (for help purposes)
   **/
   
       public String getDescription() {
         return this.description;
      }
   
   /**
   * Produces List of Directive objects
   * 
   * @return MIPS Directive
   **/
       public static ArrayList getDirectiveList() {
         return directiveList;
      }
   
   
   /**
   * Lets you know whether given directive is for integer (WORD,HALF,BYTE).
   * 
   * @param direct a MIPS directive
   * @return true if given directive is FLOAT or DOUBLE, false otherwise
   **/
       public static boolean isIntegerDirective(Directives direct) {
         return direct == Directives.WORD || direct == Directives.HALF || direct == Directives.BYTE;
      }
   	
   	
   /**
   * Lets you know whether given directive is for floating number (FLOAT,DOUBLE).
   *
   * @param direct a MIPS directive
   * @return true if given directive is FLOAT or DOUBLE, false otherwise.
   **/  		
       public static boolean isFloatingDirective(Directives direct) {
         return direct == Directives.FLOAT || direct == Directives.DOUBLE;
      }
   
   }