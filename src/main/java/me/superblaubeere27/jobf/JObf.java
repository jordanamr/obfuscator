package me.superblaubeere27.jobf;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import me.superblaubeere27.hwid.HWID;
import me.superblaubeere27.jobf.processors.packager.Packager;
import me.superblaubeere27.jobf.ui.GUI;
import me.superblaubeere27.jobf.util.script.JObfScript;
import me.superblaubeere27.jobf.utils.Templates;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JObf {
    public static final String VERSION = "obfuscator " + (JObf.class.getPackage().getImplementationVersion() == null ? "DEV" : "v" + JObf.class.getPackage().getImplementationVersion()) + " by superblaubeere27";
    public final static Logger log = Logger.getLogger("obfuscator");
    private static GUI gui;

    public static void main(String[] args) throws Exception {
        Class.forName(JObfImpl.class.getCanonicalName());
        JObf.log.setUseParentHandlers(false);
        JObf.log.setLevel(Level.ALL);
        JObf.log.setFilter(record -> true);

//            if (log != null) {a

        JObf.log.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
//                    if (record.getLevel().intValue() < Level.INFO.intValue()) return;
//                System.out.println("ACAB");
                if (record.getMessage() == null)
                    return;
//                System.out.println(record.getMessage() + "/" + record.getParameters());
                if (gui != null) {
                    gui.logArea.append(String.format(record.getMessage(), record.getParameters()) + "\n");
                    gui.scrollDown();
//                    System.out.println("lloool");
                }

                System.out.println(String.format(record.getMessage(), record.getParameters()));
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
//        MethodHandle handle = MethodHandles.lookup().findVirtual(PrintStream.class, "println", MethodType.methodType(void.class, int.class));
//        handle.asType(MethodType.methodType(void.class, int.class));
//        MethodHandles.lookup().loo

//        try {
//            handle.invoke(System.out, 1337);
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }

        Templates.loadTemplates();

        OptionParser parser = new OptionParser();
        parser.accepts("help").forHelp();
        parser.accepts("version").forHelp();
        parser.accepts("jarIn").withOptionalArg().required();
        parser.accepts("jarOut").withRequiredArg();
        parser.accepts("package").withOptionalArg().describedAs("Encrypts all classes");
        parser.accepts("invokeDynamic").withOptionalArg().describedAs("Hides method calls.");
        parser.accepts("packagerMainClass").requiredIf("package").availableIf("package").withOptionalArg();
        parser.accepts("mode").withOptionalArg().describedAs("0 = Normal, 1 = Aggressive (Might not work)").ofType(Integer.class).defaultsTo(0);
        parser.accepts("cp").withOptionalArg().describedAs("ClassPath; Only for name obfuscation").ofType(File.class);
        parser.accepts("scriptFile").withOptionalArg().describedAs("[Not documented] JS script file").ofType(File.class);
        parser.accepts("nameobf").withOptionalArg().describedAs("!!! DEPRECATED !!!").ofType(boolean.class);
        parser.accepts("hwid").withOptionalArg().describedAs("Enabled HWID protection").ofType(String.class);
        parser.accepts("log").withRequiredArg();


        try {
            OptionSet options = parser.parse(args);
            if (options.has("help")) {
                System.out.println(VERSION);
                parser.printHelpOn(System.out);
                return;
            } else if (options.has("version")) {
                System.out.println(VERSION);
                return;
            }

            String jarIn = (String) options.valueOf("jarIn");
            String jarOut = (String) options.valueOf("jarOut");
            String log = (String) options.valueOf("log");
            int mode = (int) options.valueOf("mode");



            log(JObf.VERSION);
            log("Input:          " + jarIn);
            log("Output:         " + jarOut);
            log("Log:            " + log);

            List<File> fileList = new ArrayList<>();

            if (options.has("cp")) {
                for (Object cp : options.valuesOf("cp")) {
                    File file = (File) cp;

                    if (file.isDirectory()) {
                        Files.walk(file.toPath()).filter(p -> {
                            String path = p.toString().toLowerCase();

                            return path.endsWith(".jar") || path.endsWith(".zip");
                        }).forEach(p -> fileList.add(p.toFile()));
                    } else if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
                        fileList.add(file);
                    }
                }
            }

//            for (File file : fileList) {
//                System.out.println("ClassPath: " + file.getAbsolutePath());
//            }
            String scriptContent = "";

            if (options.has("scriptFile")) {
                scriptContent = new String(Files.readAllBytes(((File) options.valueOf("scriptFile")).toPath()), "UTF-8");
            }

            JObfScript script = new JObfScript(scriptContent);

            try {
                JObfImpl.processConsole(jarIn, jarOut, fileList, log, mode, options.has("package"), options.has("nameobf"), options.has("hwid"), options.has("invokeDynamic"), options.hasArgument("hwid") ? HWID.hexStringToByteArray((String) options.valueOf("hwid")) : HWID.generateHWID(), options.has("package") ? String.valueOf(options.valueOf("packagerMainClass")) : "", script);
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        } catch (OptionException e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            Packager.INSTANCE.isEnabled();
            gui = new GUI();
//            e.printStackTrace();
//            parser.printHelpOn(System.out);
            System.err.println("ERROR: " + e.getMessage() + " (try --help)");

//            e.printStackTrace();
        }
    }

    private static void log(String line) {
        log.info(line);
    }

    public static void report(String s) {

    }
}
