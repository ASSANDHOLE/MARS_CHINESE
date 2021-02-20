   package mars.venus;
   import mars.*;
   import mars.mips.dump.*;
   import javax.swing.*;
   import java.awt.*;
   import java.awt.event.*;
   import javax.swing.event.*;
   import java.io.*;
   import java.net.*;

/*
Copyright (c) 2003-2013,  Pete Sanderson and Kenneth Vollmar

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
	  *  Top level container for Venus GUI.
	  *   @author Sanderson and Team JSpim
	  **/
	  
	  /* Heavily modified by Pete Sanderson, July 2004, to incorporate JSPIMMenu and JSPIMToolbar
	   * not as subclasses of JMenuBar and JToolBar, but as instances of them.  They are both
		* here primarily so both can share the Action objects.
		*/
	
    public class VenusUI extends JFrame{
      VenusUI mainUI;
      public JMenuBar menu;
      JToolBar toolbar;
      MainPane mainPane; 
      RegistersPane registersPane; 
      RegistersWindow registersTab;
      Coprocessor1Window coprocessor1Tab;
      Coprocessor0Window coprocessor0Tab;
      MessagesPane messagesPane;
      JSplitPane splitter, horizonSplitter;
      JPanel north;
   
      private int frameState; // see windowActivated() and windowDeactivated()
      private static int menuState = FileStatus.NO_FILE;
     	
   	// PLEASE PUT THESE TWO (& THEIR METHODS) SOMEWHERE THEY BELONG, NOT HERE
      private static boolean reset= true; // registers/memory reset for execution
      private static boolean started = false;  // started execution
      Editor editor;
   	
   	// components of the menubar
      private JMenu file, run, window, help, edit, settings;
      private JMenuItem fileNew, fileOpen, fileClose, fileCloseAll, fileSave, fileSaveAs, fileSaveAll, fileDumpMemory, filePrint, fileExit;
      private JMenuItem editUndo, editRedo, editCut, editCopy, editPaste, editFindReplace, editSelectAll;
      private JMenuItem runGo, runStep, runBackstep, runReset, runAssemble, runStop, runPause, runClearBreakpoints, runToggleBreakpoints;
      private JCheckBoxMenuItem settingsLabel, settingsPopupInput, settingsValueDisplayBase, settingsAddressDisplayBase,
              settingsExtended, settingsAssembleOnOpen, settingsAssembleAll, settingsWarningsAreErrors, settingsStartAtMain,
      		  settingsDelayedBranching, settingsProgramArguments, settingsSelfModifyingCode;
      private JMenuItem settingsExceptionHandler, settingsEditor, settingsHighlighting, settingsMemoryConfiguration;
      private JMenuItem helpHelp, helpAbout;
         
      // components of the toolbar
      private JButton Undo, Redo, Cut, Copy, Paste, FindReplace, SelectAll;
      private JButton New, Open, Save, SaveAs, SaveAll, DumpMemory, Print;
      private JButton Run, Assemble, Reset, Step, Backstep, Stop, Pause;
      private JButton Help;
   
      // The "action" objects, which include action listeners.  One of each will be created then
   	// shared between a menu item and its corresponding toolbar button.  This is a very cool
   	// technique because it relates the button and menu item so closely
   	
      private Action fileNewAction, fileOpenAction, fileCloseAction, fileCloseAllAction, fileSaveAction;
      private Action fileSaveAsAction, fileSaveAllAction, fileDumpMemoryAction, filePrintAction, fileExitAction;
      EditUndoAction editUndoAction;
      EditRedoAction editRedoAction;
      private Action editCutAction, editCopyAction, editPasteAction, editFindReplaceAction, editSelectAllAction;
      private Action runAssembleAction, runGoAction, runStepAction, runBackstepAction, runResetAction, 
                     runStopAction, runPauseAction, runClearBreakpointsAction, runToggleBreakpointsAction;
      private Action settingsLabelAction, settingsPopupInputAction, settingsValueDisplayBaseAction, settingsAddressDisplayBaseAction,
                     settingsExtendedAction, settingsAssembleOnOpenAction, settingsAssembleAllAction,
      					settingsWarningsAreErrorsAction, settingsStartAtMainAction, settingsProgramArgumentsAction,
      					settingsDelayedBranchingAction, settingsExceptionHandlerAction, settingsEditorAction,
      					settingsHighlightingAction, settingsMemoryConfigurationAction, settingsSelfModifyingCodeAction;    
      private Action helpHelpAction, helpAboutAction;
   
   
    /**
      *  Constructor for the Class. Sets up a window object for the UI
   	*   @param s Name of the window to be created.
   	**/     
   
       public VenusUI(String s) {
         super(s);
         mainUI = this;
         Globals.setGui(this);
         this.editor = new Editor(this);
      		 
         double screenWidth  = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
         double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
         // basically give up some screen space if running at 800 x 600
         double messageWidthPct = (screenWidth<1000.0)? 0.67 : 0.73;
         double messageHeightPct = (screenWidth<1000.0)? 0.12 : 0.15;
         double mainWidthPct = (screenWidth<1000.0)? 0.67 : 0.73;
         double mainHeightPct = (screenWidth<1000.0)? 0.60 : 0.65;
         double registersWidthPct = (screenWidth<1000.0)? 0.18 : 0.22;
         double registersHeightPct = (screenWidth<1000.0)? 0.72 : 0.80;
      				
         Dimension messagesPanePreferredSize = new Dimension((int)(screenWidth*messageWidthPct),(int)(screenHeight*messageHeightPct)); 
         Dimension mainPanePreferredSize = new Dimension((int)(screenWidth*mainWidthPct),(int)(screenHeight*mainHeightPct));
         Dimension registersPanePreferredSize = new Dimension((int)(screenWidth*registersWidthPct),(int)(screenHeight*registersHeightPct));
      	
         // the "restore" size (window control button that toggles with maximize)
      	// I want to keep it large, with enough room for user to get handles
         //this.setSize((int)(screenWidth*.8),(int)(screenHeight*.8));
      
         Globals.initialize(true);			
      
      	//  image courtesy of NASA/JPL.  
         URL im = this.getClass().getResource(Globals.imagesPath+"RedMars16.gif");
         if (im == null) {
            System.out.println("Internal Error: images folder or file not found");
            System.exit(0);
         }				
         Image mars = Toolkit.getDefaultToolkit().getImage(im);
         this.setIconImage(mars);
      	// Everything in frame will be arranged on JPanel "center", which is only frame component.
      	// "center" has BorderLayout and 2 major components:
      	//   -- panel (jp) on North with 2 components
      	//      1. toolbar
      	//      2. run speed slider.
      	//   -- split pane (horizonSplitter) in center with 2 components side-by-side
      	//      1. split pane (splitter) with 2 components stacked
      	//         a. main pane, with 2 tabs (edit, execute)
      	//         b. messages pane with 2 tabs (mars, run I/O)
      	//      2. registers pane with 3 tabs (register file, coproc 0, coproc 1)
      	// I should probably run this breakdown out to full detail.  The components are created
      	// roughly in bottom-up order; some are created in component constructors and thus are
      	// not visible here.
      	
         registersTab = new RegistersWindow();
         coprocessor1Tab = new Coprocessor1Window();
         coprocessor0Tab = new Coprocessor0Window();
         registersPane = new RegistersPane(mainUI, registersTab,coprocessor1Tab, coprocessor0Tab);
         registersPane.setPreferredSize(registersPanePreferredSize);
      	
      	//Insets defaultTabInsets = (Insets)UIManager.get("TabbedPane.tabInsets");
      	//UIManager.put("TabbedPane.tabInsets", new Insets(1, 1, 1, 1));
         mainPane = new MainPane(mainUI, editor, registersTab, coprocessor1Tab, coprocessor0Tab);
      	//UIManager.put("TabbedPane.tabInsets", defaultTabInsets); 
      	
         mainPane.setPreferredSize(mainPanePreferredSize);
         messagesPane= new MessagesPane();
         messagesPane.setPreferredSize(messagesPanePreferredSize);
         splitter= new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPane, messagesPane);
         splitter.setOneTouchExpandable(true);
         splitter.resetToPreferredSizes();
         horizonSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitter, registersPane);
         horizonSplitter.setOneTouchExpandable(true);
         horizonSplitter.resetToPreferredSizes();
         
         // due to dependencies, do not set up menu/toolbar until now.
         this.createActionObjects();
         menu= this.setUpMenuBar();
         this.setJMenuBar(menu);
      	
         toolbar= this.setUpToolBar();
      
         JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
         jp.add(toolbar);
         jp.add(RunSpeedPanel.getInstance());
         JPanel center= new JPanel(new BorderLayout());
         center.add(jp, BorderLayout.NORTH);
         center.add(horizonSplitter);
      				
      
      	
         this.getContentPane().add(center);
      
         FileStatus.reset();
      	// The following has side effect of establishing menu state
         FileStatus.set(FileStatus.NO_FILE);  
      				
         // This is invoked when opening the app.  It will set the app to
         // appear at full screen size.
         this.addWindowListener(
                new WindowAdapter() {
                   public void windowOpened(WindowEvent e) {
                     mainUI.setExtendedState(JFrame.MAXIMIZED_BOTH); 
                  }
               });
      
        // This is invoked when exiting the app through the X icon.  It will in turn
        // check for unsaved edits before exiting.
         this.addWindowListener(
                new WindowAdapter() {
                   public void windowClosing(WindowEvent e) {
                     if (mainUI.editor.closeAll()) {
                        System.exit(0);
                     } 
                  }
               });
      			
      	// The following will handle the windowClosing event properly in the 
      	// situation where user Cancels out of "save edits?" dialog.  By default,
      	// the GUI frame will be hidden but I want it to do nothing.
         this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      
         this.pack();
         this.setVisible(true);
      }
   	
   	
    /*
     * Action objects are used instead of action listeners because one can be easily shared between
     * a menu item and a toolbar button.  Does nice things like disable both if the action is
     * disabled, etc.
     *
     * 手工汉化
     * by An Guangyan
     * at February 12, 2021
     */
       private void createActionObjects() {
         Toolkit tk = Toolkit.getDefaultToolkit();
         Class cs = this.getClass(); 
         try {
            fileNewAction = new FileNewAction("新建",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"New22.png"))),
                                            "新建文件以供编辑", new Integer(KeyEvent.VK_N),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);		
            fileOpenAction = new FileOpenAction("打开...",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Open22.png"))),
               									  "打开文件", new Integer(KeyEvent.VK_O),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);	
            fileCloseAction = new FileCloseAction("关闭", null,
                                            "关闭当前文件", new Integer(KeyEvent.VK_C),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);						
            fileCloseAllAction = new FileCloseAllAction("关闭所有", null,
                                            "关闭所有打开的文件", new Integer(KeyEvent.VK_L),
               									  null, mainUI);	
            fileSaveAction = new FileSaveAction("保存",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Save22.png"))),
               									  "保存当前文件", new Integer(KeyEvent.VK_S),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);	
            fileSaveAsAction = new FileSaveAsAction("保存为...",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"SaveAs22.png"))),
               									  "以其他名字保存", new Integer(KeyEvent.VK_A),
               									  null, mainUI);	
            fileSaveAllAction = new FileSaveAllAction("保存所有", null,
                                            "保存所有打开的文件", new Integer(KeyEvent.VK_V),
               									  null, mainUI);	
            fileDumpMemoryAction = new FileDumpMemoryAction("转储内存...",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Dump22.png"))),
               									  "以可用格式转储机器代码或数据 ", new Integer(KeyEvent.VK_D),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);	
            filePrintAction = new FilePrintAction("打印...",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Print22.gif"))),
               									  "打印当前文件", new Integer(KeyEvent.VK_P),
               									  null, mainUI);	
            fileExitAction = new FileExitAction("退出", null,
               	                         "退出Mars", new Integer(KeyEvent.VK_X),
               									  null, mainUI);	
            editUndoAction = new EditUndoAction("撤销",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Undo22.png"))),
               									  "撤销上次改动", new Integer(KeyEvent.VK_U),
                                            KeyStroke.getKeyStroke( KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);	
            editRedoAction = new EditRedoAction("重做",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Redo22.png"))),
               									  "重做最近操作", new Integer(KeyEvent.VK_R),
                                            KeyStroke.getKeyStroke( KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);			
            editCutAction = new EditCutAction("剪切",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Cut22.gif"))),
               									  "Cut", new Integer(KeyEvent.VK_C),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);	
            editCopyAction = new EditCopyAction("复制",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Copy22.png"))),
               									  "Copy", new Integer(KeyEvent.VK_O),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);	
            editPasteAction = new EditPasteAction("粘贴",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Paste22.png"))),
               									  "Paste", new Integer(KeyEvent.VK_P),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);	
            editFindReplaceAction = new EditFindReplaceAction("寻找/替换",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Find22.png"))),
               									  "Find/Replace", new Integer(KeyEvent.VK_F),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);
            editSelectAllAction = new EditSelectAllAction("全选",
                                            null, //new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Find22.png"))),
               									  "Select All", new Integer(KeyEvent.VK_A),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);
            runAssembleAction = new RunAssembleAction("assemble",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Assemble22.png"))),
               									  "汇编当前文件并清除断点 ", new Integer(KeyEvent.VK_A),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_F3, 0), 
               									  mainUI);			
            runGoAction = new RunGoAction("Go", 
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Play22.png"))),
               									  "运行当前程序", new Integer(KeyEvent.VK_G),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_F5, 0),
               									  mainUI);	
            runStepAction = new RunStepAction("单步执行",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"StepForward22.png"))),
               									  "一次执行一步", new Integer(KeyEvent.VK_T),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_F7, 0),
               									  mainUI);	
            runBackstepAction = new RunBackstepAction("往前一步",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"StepBack22.png"))),
               									  "Undo the last step", new Integer(KeyEvent.VK_B),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_F8, 0), 
               									  mainUI);	
            runPauseAction = new RunPauseAction("暂停",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Pause22.png"))),
               									  "暂停当前程序", new Integer(KeyEvent.VK_P),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_F9, 0), 
               									  mainUI);	
            runStopAction = new RunStopAction("中止",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Stop22.png"))),
               									  "中止当前程序", new Integer(KeyEvent.VK_S),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_F11, 0), 
               									  mainUI);
            runResetAction = new RunResetAction("重置",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Reset22.png"))),
               									  "重置MIPS内存和寄存器 ", new Integer(KeyEvent.VK_R),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_F12,0),
               									  mainUI);	
            runClearBreakpointsAction = new RunClearBreakpointsAction("清除所有断点",
                                            null,
               									  "Clears all execution breakpoints set since the last assemble.",
               									  new Integer(KeyEvent.VK_K),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);  
            runToggleBreakpointsAction = new RunToggleBreakpointsAction("切换所有断点",
                                            null,
               									  "禁用/启用所有断点而无需清除(也可以单击Bkpt列的header)",
               									  new Integer(KeyEvent.VK_T),
               									  KeyStroke.getKeyStroke( KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               									  mainUI);  
            settingsLabelAction = new SettingsLabelAction("显示标签窗口(符号表)",
                                            null,
               									  "在\"执行\"选项卡中切换\"标签\"窗口(符号表)的可见性",
               									  null,null,
               									  mainUI);
            settingsPopupInputAction = new SettingsPopupInputAction("弹出对话框 for input syscalls (5,6,7,8,12)",
                                            null,
               									  "如果设置，将使用弹出对话框 for input syscalls (5,6,7,8,12)，而不是使用运行I/O窗口中的光标",
               									  null,null,
               									  mainUI);
         
            settingsValueDisplayBaseAction = new SettingsValueDisplayBaseAction("以十六进制显示值",
                                            null,
               									  "在内存和寄存器的值的十六进制和十进制显示之间切换",
               									  null,null,
               									  mainUI);
            settingsAddressDisplayBaseAction = new SettingsAddressDisplayBaseAction("以十六进制显示地址",
                                            null,
               									  "在内存地址的十六进制和十进制显示之间切换",
               									  null,null,
               									  mainUI);
            settingsExtendedAction          = new SettingsExtendedAction("允许Extended(Pseudo)指令和格式",
                                            null,
               									  "如果设置，则允许使用MIPS Extended(Pseudo)指令",
               									  null,null,
               									  mainUI);    
            settingsAssembleOnOpenAction    = new SettingsAssembleOnOpenAction("打开时汇编文件",
                                            null,
               									  "如果设置，则文件将在打开后自动进行汇编。打开文件对话框将显示最近打开的文件。",
               									  null,null,
               									  mainUI);
            settingsAssembleAllAction       = new SettingsAssembleAllAction("汇编文件夹内所有文件",
                                            null,
               									  "如果设置，则选择assemble操作时，将汇编当前目录中的所有文件。",
               									  null,null,
               									  mainUI);
            settingsWarningsAreErrorsAction = new SettingsWarningsAreErrorsAction("将汇编程序警告(warning)视为错误(error)",
                                            null,
               									  "如果设置，汇编程序警告将被解释为错误并阻止成功的汇编",
               									  null,null,
               									  mainUI);
            settingsStartAtMainAction       = new SettingsStartAtMainAction("将程序计数器(return address)初始化为 global 'main'(如果已定义)",
                                            null,
               									  "如果设置，则汇编器会将程序计数器(return address)初始化为全局标记为 'main' 的文本地址(如果已定义)",
               									  null,null,
               									  mainUI);
            settingsProgramArgumentsAction = new SettingsProgramArgumentsAction("提供给MIPS程序的程序参数 ",
                                            null,
               									  "如果设置，则可以在文本框的边框中输入MIPS程序的程序参数。 ",
               									  null,null,
               									  mainUI);
            settingsDelayedBranchingAction  = new SettingsDelayedBranchingAction("分支延迟",
                                            null,
               									  "如果设置，则延迟分支将在MIPS执行期间发生",
               									  null,null,
               									  mainUI);
            settingsSelfModifyingCodeAction  = new SettingsSelfModifyingCodeAction("Self-modifying code",
                                            null,
               									  "如果设置，则MIPS程序可以写入并分支到文本和数据段",
               									  null,null,
               									  mainUI);
            settingsEditorAction          = new SettingsEditorAction("编辑器...",
                                            null,
               									  "查看和修改文本编辑器设置",
               									  null,null,
               									  mainUI);
            settingsHighlightingAction          = new SettingsHighlightingAction("高亮显示...",
                                            null,
               									  "查看和修改执行选项卡突出显示的颜色",
               									  null,null,
               									  mainUI);
            settingsExceptionHandlerAction  = new SettingsExceptionHandlerAction("异常处理...",
                                            null,
               									  "如果设置，则指定的异常处理程序文件将包含在所有汇编操作中",
               									  null,null,
               									  mainUI);
            settingsMemoryConfigurationAction  = new SettingsMemoryConfigurationAction("内存配置...",
                                            null,
               									  "查看和修改模拟MIPS的内存段基地址",
               									  null,null,
               									  mainUI);
            helpHelpAction = new HelpHelpAction("帮助",
                                            new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Help22.png"))),
               									  "Help", new Integer(KeyEvent.VK_H),
               									  KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
               									  mainUI);	
            helpAboutAction = new HelpAboutAction("关于...",null,
                                            "Information about Mars", null,null, mainUI);	
         } 
             catch (NullPointerException e) {
               System.out.println("Internal Error: images folder not found, or other null pointer exception while creating Action objects");
               e.printStackTrace();
               System.exit(0);
            }
      }
   
    /*
     * build the menus and connect them to action objects (which serve as action listeners
     * shared between menu item and corresponding toolbar icon).
     */
    
       private JMenuBar setUpMenuBar() {
      
         Toolkit tk = Toolkit.getDefaultToolkit();
         Class cs = this.getClass(); 
         JMenuBar menuBar = new JMenuBar();
         file=new JMenu("文件");
         file.setMnemonic(KeyEvent.VK_F);
         edit = new JMenu("编辑");
         edit.setMnemonic(KeyEvent.VK_E);
         run=new JMenu("运行");
         run.setMnemonic(KeyEvent.VK_R);
         //window = new JMenu("Window");
         //window.setMnemonic(KeyEvent.VK_W);
         settings = new JMenu("设置");
         settings.setMnemonic(KeyEvent.VK_S);
         help = new JMenu("帮助");
         help.setMnemonic(KeyEvent.VK_H); 
      	// slight bug: user typing alt-H activates help menu item directly, not help menu
      
         fileNew = new JMenuItem(fileNewAction);
         fileNew.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"New16.png"))));
         fileOpen = new JMenuItem(fileOpenAction);
         fileOpen.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Open16.png"))));
         fileClose = new JMenuItem(fileCloseAction);
         fileClose.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         fileCloseAll = new JMenuItem(fileCloseAllAction);
         fileCloseAll.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         fileSave = new JMenuItem(fileSaveAction);
         fileSave.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Save16.png"))));
         fileSaveAs = new JMenuItem(fileSaveAsAction);
         fileSaveAs.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"SaveAs16.png"))));
         fileSaveAll = new JMenuItem(fileSaveAllAction);
         fileSaveAll.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         fileDumpMemory = new JMenuItem(fileDumpMemoryAction);
         fileDumpMemory.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Dump16.png"))));
         filePrint = new JMenuItem(filePrintAction);
         filePrint.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Print16.gif"))));
         fileExit = new JMenuItem(fileExitAction);
         fileExit.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         file.add(fileNew);
         file.add(fileOpen);
         file.add(fileClose);
         file.add(fileCloseAll);
         file.addSeparator();
         file.add(fileSave);
         file.add(fileSaveAs);
         file.add(fileSaveAll);
         if (new mars.mips.dump.DumpFormatLoader().loadDumpFormats().size() > 0) {
            file.add(fileDumpMemory);
         }
         file.addSeparator();
         file.add(filePrint);
         file.addSeparator();
         file.add(fileExit);
      	
         editUndo = new JMenuItem(editUndoAction);
         editUndo.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Undo16.png"))));//"Undo16.gif"))));
         editRedo = new JMenuItem(editRedoAction);
         editRedo.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Redo16.png"))));//"Redo16.gif"))));      
         editCut = new JMenuItem(editCutAction);
         editCut.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Cut16.gif"))));
         editCopy = new JMenuItem(editCopyAction);
         editCopy.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Copy16.png"))));//"Copy16.gif"))));
         editPaste = new JMenuItem(editPasteAction);
         editPaste.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Paste16.png"))));//"Paste16.gif"))));
         editFindReplace = new JMenuItem(editFindReplaceAction);
         editFindReplace.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Find16.png"))));//"Paste16.gif"))));
         editSelectAll = new JMenuItem(editSelectAllAction);
         editSelectAll.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         edit.add(editUndo);
         edit.add(editRedo);
         edit.addSeparator();
         edit.add(editCut);
         edit.add(editCopy);
         edit.add(editPaste);
         edit.addSeparator();
         edit.add(editFindReplace);
         edit.add(editSelectAll);
      
         runAssemble = new JMenuItem(runAssembleAction);
         runAssemble.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Assemble16.png"))));//"MyAssemble16.gif"))));
         runGo = new JMenuItem(runGoAction);
         runGo.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Play16.png"))));//"Play16.gif"))));
         runStep = new JMenuItem(runStepAction);
         runStep.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"StepForward16.png"))));//"MyStepForward16.gif"))));
         runBackstep = new JMenuItem(runBackstepAction);
         runBackstep.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"StepBack16.png"))));//"MyStepBack16.gif"))));
         runReset = new JMenuItem(runResetAction);
         runReset.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Reset16.png"))));//"MyReset16.gif"))));
         runStop = new JMenuItem(runStopAction);
         runStop.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Stop16.png"))));//"Stop16.gif"))));
         runPause = new JMenuItem(runPauseAction);
         runPause.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Pause16.png"))));//"Pause16.gif"))));
         runClearBreakpoints = new JMenuItem(runClearBreakpointsAction);
         runClearBreakpoints.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         runToggleBreakpoints = new JMenuItem(runToggleBreakpointsAction);
         runToggleBreakpoints.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
      
         run.add(runAssemble);
         run.add(runGo);
         run.add(runStep);
         run.add(runBackstep);
         run.add(runPause);
         run.add(runStop);
         run.add(runReset);
         run.addSeparator();
         run.add(runClearBreakpoints);
         run.add(runToggleBreakpoints);
      	
         settingsLabel = new JCheckBoxMenuItem(settingsLabelAction);
         settingsLabel.setSelected(Globals.getSettings().getLabelWindowVisibility());
         settingsPopupInput = new JCheckBoxMenuItem(settingsPopupInputAction);
         settingsPopupInput.setSelected(Globals.getSettings().getBooleanSetting(Settings.POPUP_SYSCALL_INPUT));
         settingsValueDisplayBase = new JCheckBoxMenuItem(settingsValueDisplayBaseAction);
         settingsValueDisplayBase.setSelected(Globals.getSettings().getDisplayValuesInHex());//mainPane.getExecutePane().getValueDisplayBaseChooser().isSelected());
         // Tell the corresponding JCheckBox in the Execute Pane about me -- it has already been created.
         mainPane.getExecutePane().getValueDisplayBaseChooser().setSettingsMenuItem(settingsValueDisplayBase);
         settingsAddressDisplayBase = new JCheckBoxMenuItem(settingsAddressDisplayBaseAction);
         settingsAddressDisplayBase.setSelected(Globals.getSettings().getDisplayAddressesInHex());//mainPane.getExecutePane().getValueDisplayBaseChooser().isSelected());
         // Tell the corresponding JCheckBox in the Execute Pane about me -- it has already been created.
         mainPane.getExecutePane().getAddressDisplayBaseChooser().setSettingsMenuItem(settingsAddressDisplayBase);
         settingsExtended = new JCheckBoxMenuItem(settingsExtendedAction);
         settingsExtended.setSelected(Globals.getSettings().getExtendedAssemblerEnabled());
         settingsDelayedBranching = new JCheckBoxMenuItem(settingsDelayedBranchingAction);
         settingsDelayedBranching.setSelected(Globals.getSettings().getDelayedBranchingEnabled());
         settingsSelfModifyingCode = new JCheckBoxMenuItem(settingsSelfModifyingCodeAction);
         settingsSelfModifyingCode.setSelected(Globals.getSettings().getBooleanSetting(Settings.SELF_MODIFYING_CODE_ENABLED));
         settingsAssembleOnOpen = new JCheckBoxMenuItem(settingsAssembleOnOpenAction);
         settingsAssembleOnOpen.setSelected(Globals.getSettings().getAssembleOnOpenEnabled());
         settingsAssembleAll = new JCheckBoxMenuItem(settingsAssembleAllAction);
         settingsAssembleAll.setSelected(Globals.getSettings().getAssembleAllEnabled());
         settingsWarningsAreErrors = new JCheckBoxMenuItem(settingsWarningsAreErrorsAction);
         settingsWarningsAreErrors.setSelected(Globals.getSettings().getWarningsAreErrors());
         settingsStartAtMain = new JCheckBoxMenuItem(settingsStartAtMainAction);
         settingsStartAtMain.setSelected(Globals.getSettings().getStartAtMain()); 
         settingsProgramArguments = new JCheckBoxMenuItem(settingsProgramArgumentsAction);
         settingsProgramArguments.setSelected(Globals.getSettings().getProgramArguments());
         settingsEditor = new JMenuItem(settingsEditorAction);
         settingsHighlighting = new JMenuItem(settingsHighlightingAction);
         settingsExceptionHandler = new JMenuItem(settingsExceptionHandlerAction);
         settingsMemoryConfiguration = new JMenuItem(settingsMemoryConfigurationAction);
      	
         settings.add(settingsLabel);
         settings.add(settingsProgramArguments);
         settings.add(settingsPopupInput);
         settings.add(settingsAddressDisplayBase);
         settings.add(settingsValueDisplayBase);
         settings.addSeparator();
         settings.add(settingsAssembleOnOpen);
         settings.add(settingsAssembleAll);
         settings.add(settingsWarningsAreErrors);
         settings.add(settingsStartAtMain);
         settings.addSeparator();
         settings.add(settingsExtended);
         settings.add(settingsDelayedBranching);
         settings.add(settingsSelfModifyingCode);
         settings.addSeparator();
         settings.add(settingsEditor);
         settings.add(settingsHighlighting);
         settings.add(settingsExceptionHandler);
         settings.add(settingsMemoryConfiguration);
      			
         helpHelp = new JMenuItem(helpHelpAction);
         helpHelp.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Help16.png"))));//"Help16.gif"))));
         helpAbout = new JMenuItem(helpAboutAction);
         helpAbout.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"MyBlank16.gif"))));
         help.add(helpHelp);
         help.addSeparator();
         help.add(helpAbout);
      
         menuBar.add(file);
         menuBar.add(edit);
         menuBar.add(run);
         menuBar.add(settings);
         JMenu toolMenu = new ToolLoader().buildToolsMenu();
         if (toolMenu != null) menuBar.add(toolMenu);
         menuBar.add(help);
      	
      	// experiment with popup menu for settings. 3 Aug 2006 PS
         //setupPopupMenu();
      	
         return menuBar;
      }
   
    /*
     * build the toolbar and connect items to action objects (which serve as action listeners
     * shared between toolbar icon and corresponding menu item).
     */
   
       JToolBar setUpToolBar() {
         JToolBar toolBar = new JToolBar();
      	
         New = new JButton(fileNewAction);
         New.setText("");
         Open = new JButton(fileOpenAction);
         Open.setText(""); 
         Save = new JButton(fileSaveAction);
         Save.setText("");
         SaveAs = new JButton(fileSaveAsAction);
         SaveAs.setText("");
         DumpMemory = new JButton(fileDumpMemoryAction);
         DumpMemory.setText("");
         Print= new JButton(filePrintAction);
         Print.setText("");
      
         Undo = new JButton(editUndoAction);
         Undo.setText(""); 
         Redo = new JButton(editRedoAction);
         Redo.setText("");   	
         Cut= new JButton(editCutAction);
         Cut.setText("");
         Copy= new JButton(editCopyAction);
         Copy.setText("");
         Paste= new JButton(editPasteAction);
         Paste.setText("");
         FindReplace = new JButton(editFindReplaceAction);
         FindReplace.setText("");
         SelectAll = new JButton(editSelectAllAction);
         SelectAll.setText("");
      	
         Run = new JButton(runGoAction);
         Run.setText("");
         Assemble = new JButton(runAssembleAction);
         Assemble.setText(""); 
         Step = new JButton(runStepAction);
         Step.setText(""); 
         Backstep = new JButton(runBackstepAction);
         Backstep.setText("");
         Reset = new JButton(runResetAction);
         Reset.setText(""); 
         Stop = new JButton(runStopAction);
         Stop.setText("");
         Pause = new JButton(runPauseAction);
         Pause.setText("");      	
         Help= new JButton(helpHelpAction);
         Help.setText("");
         
         toolBar.add(New);
         toolBar.add(Open);
         toolBar.add(Save);
         toolBar.add(SaveAs);
         if (new mars.mips.dump.DumpFormatLoader().loadDumpFormats().size() > 0) {
            toolBar.add(DumpMemory);
         }
         toolBar.add(Print);
         toolBar.add(new JToolBar.Separator());
         toolBar.add(Undo);
         toolBar.add(Redo);
         toolBar.add(Cut);
         toolBar.add(Copy);
         toolBar.add(Paste);
         toolBar.add(FindReplace);
         toolBar.add(new JToolBar.Separator());
         toolBar.add(Assemble);
         toolBar.add(Run);   
         toolBar.add(Step);
         toolBar.add(Backstep);
         toolBar.add(Pause);
         toolBar.add(Stop);
         toolBar.add(Reset);
         toolBar.add(new JToolBar.Separator());
         toolBar.add(Help);
         toolBar.add(new JToolBar.Separator());
      	
         return toolBar;
      }
      
   	
    /* Determine from FileStatus what the menu state (enabled/disabled)should 
     * be then call the appropriate method to set it.  Current states are:
     *
     * setMenuStateInitial: set upon startup and after File->Close
     * setMenuStateEditingNew: set upon File->New
     * setMenuStateEditing: set upon File->Open or File->Save or erroneous Run->Assemble
     * setMenuStateRunnable: set upon successful Run->Assemble
     * setMenuStateRunning: set upon Run->Go
     * setMenuStateTerminated: set upon completion of simulated execution
     */
       void setMenuState(int status) {
         menuState = status; 
         switch (status) {
            case FileStatus.NO_FILE:
               setMenuStateInitial();
               break;
            case FileStatus.NEW_NOT_EDITED:
               setMenuStateEditingNew();
               break;
            case FileStatus.NEW_EDITED:
               setMenuStateEditingNew();
               break;
            case FileStatus.NOT_EDITED:
               setMenuStateNotEdited(); // was MenuStateEditing. DPS 9-Aug-2011
               break;
            case FileStatus.EDITED:
               setMenuStateEditing();
               break;
            case FileStatus.RUNNABLE:
               setMenuStateRunnable();
               break;
            case FileStatus.RUNNING:
               setMenuStateRunning();
               break;
            case FileStatus.TERMINATED:
               setMenuStateTerminated();
               break;
            case FileStatus.OPENING:// This is a temporary state. DPS 9-Aug-2011
               break;
            default:
               System.out.println("Invalid File Status: "+status);
               break;
         }
      }
     
     
       void setMenuStateInitial() {
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(false);
         fileCloseAllAction.setEnabled(false);
         fileSaveAction.setEnabled(false);
         fileSaveAsAction.setEnabled(false);
         fileSaveAllAction.setEnabled(false);
         fileDumpMemoryAction.setEnabled(false);
         filePrintAction.setEnabled(false);
         fileExitAction.setEnabled(true);
         editUndoAction.setEnabled(false);
         editRedoAction.setEnabled(false);
         editCutAction.setEnabled(false);
         editCopyAction.setEnabled(false);
         editPasteAction.setEnabled(false);
         editFindReplaceAction.setEnabled(false);
         editSelectAllAction.setEnabled(false);
         settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
         runAssembleAction.setEnabled(false);
         runGoAction.setEnabled(false);
         runStepAction.setEnabled(false);
         runBackstepAction.setEnabled(false);
         runResetAction.setEnabled(false);
         runStopAction.setEnabled(false);
         runPauseAction.setEnabled(false);
         runClearBreakpointsAction.setEnabled(false);
         runToggleBreakpointsAction.setEnabled(false);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
   
      /* Added DPS 9-Aug-2011, for newly-opened files.  Retain
   	   existing Run menu state (except Assemble, which is always true).
   		Thus if there was a valid assembly it is retained. */
       void setMenuStateNotEdited() {
      /* Note: undo and redo are handled separately by the undo manager*/  
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(true);
         fileCloseAllAction.setEnabled(true);
         fileSaveAction.setEnabled(true);
         fileSaveAsAction.setEnabled(true);
         fileSaveAllAction.setEnabled(true);
         fileDumpMemoryAction.setEnabled(false);
         filePrintAction.setEnabled(true);
         fileExitAction.setEnabled(true);
         editCutAction.setEnabled(true);
         editCopyAction.setEnabled(true);
         editPasteAction.setEnabled(true);
         editFindReplaceAction.setEnabled(true);
         editSelectAllAction.setEnabled(true);
         settingsDelayedBranchingAction.setEnabled(true); 
         settingsMemoryConfigurationAction.setEnabled(true);
         runAssembleAction.setEnabled(true);
			// If assemble-all, allow previous Run menu settings to remain.
			// Otherwise, clear them out.  DPS 9-Aug-2011
         if (!Globals.getSettings().getBooleanSetting(mars.Settings.ASSEMBLE_ALL_ENABLED)) {
            runGoAction.setEnabled(false);
            runStepAction.setEnabled(false);
            runBackstepAction.setEnabled(false);
            runResetAction.setEnabled(false);
            runStopAction.setEnabled(false);
            runPauseAction.setEnabled(false);
            runClearBreakpointsAction.setEnabled(false);
            runToggleBreakpointsAction.setEnabled(false);
         } 
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
   
   
   
   
       void setMenuStateEditing() {
      /* Note: undo and redo are handled separately by the undo manager*/  
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(true);
         fileCloseAllAction.setEnabled(true);
         fileSaveAction.setEnabled(true);
         fileSaveAsAction.setEnabled(true);
         fileSaveAllAction.setEnabled(true);
         fileDumpMemoryAction.setEnabled(false);
         filePrintAction.setEnabled(true);
         fileExitAction.setEnabled(true);
         editCutAction.setEnabled(true);
         editCopyAction.setEnabled(true);
         editPasteAction.setEnabled(true);
         editFindReplaceAction.setEnabled(true);
         editSelectAllAction.setEnabled(true);
         settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
         runAssembleAction.setEnabled(true);
         runGoAction.setEnabled(false);
         runStepAction.setEnabled(false);
         runBackstepAction.setEnabled(false);
         runResetAction.setEnabled(false);
         runStopAction.setEnabled(false);
         runPauseAction.setEnabled(false);
         runClearBreakpointsAction.setEnabled(false);
         runToggleBreakpointsAction.setEnabled(false);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
   
     /* Use this when "File -> New" is used
      */
       void setMenuStateEditingNew() {
      /* Note: undo and redo are handled separately by the undo manager*/  
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(true);
         fileCloseAllAction.setEnabled(true);
         fileSaveAction.setEnabled(true);
         fileSaveAsAction.setEnabled(true);
         fileSaveAllAction.setEnabled(true);
         fileDumpMemoryAction.setEnabled(false);
         filePrintAction.setEnabled(true);
         fileExitAction.setEnabled(true);
         editCutAction.setEnabled(true);
         editCopyAction.setEnabled(true);
         editPasteAction.setEnabled(true);
         editFindReplaceAction.setEnabled(true);
         editSelectAllAction.setEnabled(true);
         settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
         runAssembleAction.setEnabled(false);
         runGoAction.setEnabled(false);
         runStepAction.setEnabled(false);
         runBackstepAction.setEnabled(false);
         runResetAction.setEnabled(false);
         runStopAction.setEnabled(false);
         runPauseAction.setEnabled(false);
         runClearBreakpointsAction.setEnabled(false);
         runToggleBreakpointsAction.setEnabled(false);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
    	 
     /* Use this upon successful assemble or reset
      */
       void setMenuStateRunnable() {
      /* Note: undo and redo are handled separately by the undo manager */  
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(true);
         fileCloseAllAction.setEnabled(true);
         fileSaveAction.setEnabled(true);
         fileSaveAsAction.setEnabled(true);
         fileSaveAllAction.setEnabled(true);
         fileDumpMemoryAction.setEnabled(true);
         filePrintAction.setEnabled(true);
         fileExitAction.setEnabled(true);
         editCutAction.setEnabled(true);
         editCopyAction.setEnabled(true);
         editPasteAction.setEnabled(true);
         editFindReplaceAction.setEnabled(true);
         editSelectAllAction.setEnabled(true);
         settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
         runAssembleAction.setEnabled(true);
         runGoAction.setEnabled(true);
         runStepAction.setEnabled(true);
         runBackstepAction.setEnabled(
            (Globals.getSettings().getBackSteppingEnabled()&& !Globals.program.getBackStepper().empty())
             ? true : false);
         runResetAction.setEnabled(true);
         runStopAction.setEnabled(false);
         runPauseAction.setEnabled(false);
         runToggleBreakpointsAction.setEnabled(true);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
   
     /* Use this while program is running
      */
       void setMenuStateRunning() {
      /* Note: undo and redo are handled separately by the undo manager */  
         fileNewAction.setEnabled(false);
         fileOpenAction.setEnabled(false);
         fileCloseAction.setEnabled(false);
         fileCloseAllAction.setEnabled(false);
         fileSaveAction.setEnabled(false);
         fileSaveAsAction.setEnabled(false);
         fileSaveAllAction.setEnabled(false);
         fileDumpMemoryAction.setEnabled(false);
         filePrintAction.setEnabled(false);
         fileExitAction.setEnabled(false);
         editCutAction.setEnabled(false);
         editCopyAction.setEnabled(false);
         editPasteAction.setEnabled(false);
         editFindReplaceAction.setEnabled(false);
         editSelectAllAction.setEnabled(false);
         settingsDelayedBranchingAction.setEnabled(false); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(false); // added 21 July 2009
         runAssembleAction.setEnabled(false);
         runGoAction.setEnabled(false);
         runStepAction.setEnabled(false);
         runBackstepAction.setEnabled(false);
         runResetAction.setEnabled(false);
         runStopAction.setEnabled(true);
         runPauseAction.setEnabled(true);
         runToggleBreakpointsAction.setEnabled(false);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.setEnabled(false);//updateUndoState(); // DPS 10 Jan 2008
         editRedoAction.setEnabled(false);//updateRedoState(); // DPS 10 Jan 2008
      }   
     /* Use this upon completion of execution
      */
       void setMenuStateTerminated() {
      /* Note: undo and redo are handled separately by the undo manager */  
         fileNewAction.setEnabled(true);
         fileOpenAction.setEnabled(true);
         fileCloseAction.setEnabled(true);
         fileCloseAllAction.setEnabled(true);
         fileSaveAction.setEnabled(true);
         fileSaveAsAction.setEnabled(true);
         fileSaveAllAction.setEnabled(true);
         fileDumpMemoryAction.setEnabled(true);
         filePrintAction.setEnabled(true);
         fileExitAction.setEnabled(true);
         editCutAction.setEnabled(true);
         editCopyAction.setEnabled(true);
         editPasteAction.setEnabled(true);
         editFindReplaceAction.setEnabled(true);
         editSelectAllAction.setEnabled(true);
         settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
         settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
         runAssembleAction.setEnabled(true);
         runGoAction.setEnabled(false);
         runStepAction.setEnabled(false);
         runBackstepAction.setEnabled(
            (Globals.getSettings().getBackSteppingEnabled()&& !Globals.program.getBackStepper().empty())
             ? true : false);
         runResetAction.setEnabled(true);
         runStopAction.setEnabled(false);
         runPauseAction.setEnabled(false);
         runToggleBreakpointsAction.setEnabled(true);
         helpHelpAction.setEnabled(true);
         helpAboutAction.setEnabled(true);
         editUndoAction.updateUndoState();
         editRedoAction.updateRedoState();
      }
   
    
    /**
     * Get current menu state.  State values are constants in FileStatus class.  DPS 23 July 2008
     * @return current menu state.
     **/
     
       public static int getMenuState() {
         return menuState;
      }
      
   	/**
   	  *  To set whether the register values are reset.
   	  *   @param b Boolean true if the register values have been reset.
   	  **/
   	
       public static void setReset(boolean b){
         reset=b;
      }
   
   	/**
   	  *  To set whether MIPS program execution has started.
   	  *   @param b true if the MIPS program execution has started.
   	  **/
   	
       public static void setStarted(boolean b){ 
         started=b;
      }
      /**
   	  *  To find out whether the register values are reset.
   	  *   @return Boolean true if the register values have been reset.
   	  **/
      
       public static boolean getReset(){
         return reset;
      }
   	
      /**
   	  *  To find out whether MIPS program is currently executing.
   	  *   @return  true if MIPS program is currently executing.
   	  **/
       public static boolean getStarted(){
         return started;
      }
   	
      /**
   	  *  Get reference to Editor object associated with this GUI.
   	  *   @return Editor for the GUI.
   	  **/
         	
       public Editor getEditor() {
         return editor;
      }		
   	
      /**
   	  *  Get reference to messages pane associated with this GUI.
   	  *   @return MessagesPane object associated with the GUI.
   	  **/
         	
       public MainPane getMainPane() {
         return mainPane;
      }      /**
   	  *  Get reference to messages pane associated with this GUI.
   	  *   @return MessagesPane object associated with the GUI.
   	  **/
         	
       public MessagesPane getMessagesPane() {
         return messagesPane;
      }
   
      /**
   	  *  Get reference to registers pane associated with this GUI.
   	  *   @return RegistersPane object associated with the GUI.
   	  **/
         	
       public RegistersPane getRegistersPane() {
         return registersPane;
      }   	
   
      /**
   	  *  Get reference to settings menu item for display base of memory/register values.
   	  *   @return the menu item
   	  **/
         	
       public JCheckBoxMenuItem getValueDisplayBaseMenuItem() {
         return settingsValueDisplayBase;
      }   	     
   
      /**
   	  *  Get reference to settings menu item for display base of memory/register values.
   	  *   @return the menu item
   	  **/
         	
       public JCheckBoxMenuItem getAddressDisplayBaseMenuItem() {
         return settingsAddressDisplayBase;
      }   	          
   	
   	/**
   	 * Return reference tothe Run->Assemble item's action.  Needed by File->Open in case
   	 * assemble-upon-open flag is set.
   	 * @return the Action object for the Run->Assemble operation.
   	 */
       public Action getRunAssembleAction() {
         return runAssembleAction;
      }
   	
   	/**
   	 * Have the menu request keyboard focus.  DPS 5-4-10
   	 */
       public void haveMenuRequestFocus() {
         this.menu.requestFocus();
      }
   	
   	/**
   	 * Send keyboard event to menu for possible processing.  DPS 5-4-10
   	 * @param evt KeyEvent for menu component to consider for processing.
   	 */
       public void dispatchEventToMenu(KeyEvent evt) {
         this.menu.dispatchEvent(evt);
      }
     
     // pop up menu experiment 3 Aug 2006.  Keep for possible later revival.
       private void setupPopupMenu() {
         JPopupMenu popup; 
         popup = new JPopupMenu();
      	// cannot put the same menu item object on two different menus.
      	// If you want to duplicate functionality, need a different item.
      	// Should be able to share listeners, but if both menu items are
      	// JCheckBoxMenuItem, how to keep their checked status in synch?
      	// If you popup this menu and check the box, the right action occurs
      	// but its counterpart on the regular menu is not checked.
         popup.add(new JCheckBoxMenuItem(settingsLabelAction)); 
      //Add listener to components that can bring up popup menus. 
         MouseListener popupListener = new PopupListener(popup); 
         this.addMouseListener(popupListener); 
      }
     
   
   
   }