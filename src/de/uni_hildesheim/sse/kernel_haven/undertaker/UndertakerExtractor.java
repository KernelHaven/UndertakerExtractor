package de.uni_hildesheim.sse.kernel_haven.undertaker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.kernel_haven.SetUpException;
import de.uni_hildesheim.sse.kernel_haven.code_model.CodeModelProvider;
import de.uni_hildesheim.sse.kernel_haven.code_model.ICodeModelExtractor;
import de.uni_hildesheim.sse.kernel_haven.code_model.SourceFile;
import de.uni_hildesheim.sse.kernel_haven.config.CodeExtractorConfiguration;
import de.uni_hildesheim.sse.kernel_haven.util.BlockingQueue;
import de.uni_hildesheim.sse.kernel_haven.util.CodeExtractorException;
import de.uni_hildesheim.sse.kernel_haven.util.FormatException;
import de.uni_hildesheim.sse.kernel_haven.util.Logger;

/**
 * Wrapper to run Undertaker to extract code blocks.
 * 
 * @author Adam
 * @author Johannes
 */
public class UndertakerExtractor implements ICodeModelExtractor, Runnable {

    private static final Logger LOGGER = Logger.get();

    private File linuxSourceTree;

    /**
     * The provider to notify about results.
     */
    private CodeModelProvider provider;

    /**
     * The directory where this extractor can store its resources. Not null.
     */
    private File resourceDir;

    private boolean stopRequested;

    private long timeout;

    private int numberOfThreads;
    
    private BlockingQueue<File> filesToParse;
    
    private boolean fuzzyBooleanParsing;

    /**
     * Creates a new Undertaker wrapper.
     * 
     * @param config
     *            The configuration. Must not be null.
     * 
     * @throws SetUpException
     *             If the configuration is not valid.
     */
    public UndertakerExtractor(CodeExtractorConfiguration config) throws SetUpException {
        linuxSourceTree = config.getSourceTree();
        if (linuxSourceTree == null) {
            throw new SetUpException("Config does not contain source_tree setting");
        }

        resourceDir = config.getExtractorResourceDir(getClass());
        numberOfThreads = config.getThreads();
        
        if (numberOfThreads <= 0) {
            throw new SetUpException("Number of threads is " + numberOfThreads + "; This extractor needs"
                    + " at least one thread");
        }

        try {
            timeout = Long.parseLong(config.getProperty("code.extractor.hang_timeout", "20000"));
        } catch (NumberFormatException e) {
            throw new SetUpException(e);
        }
        
        fuzzyBooleanParsing = Boolean.parseBoolean(config.getProperty("code.extractor.fuzzy_parsing", "false"));
    }

    @Override
    public void setProvider(CodeModelProvider provider) {
        this.provider = provider;
    }

    @Override
    public void start(BlockingQueue<File> filesToParse) {
        this.filesToParse = filesToParse;
        
        Thread th = new Thread(this);
        th.setName("UndertakerExtractor");
        th.start();
    }

    @Override
    public void stop() {
        synchronized (this) {
            stopRequested = true;
        }
    }

    /**
     * Checks if the provider requested that we stop our extraction.
     * 
     * @return Whether stop is requested.
     */
    private synchronized boolean isStopRequested() {
        return stopRequested;
    }

    /**
     * A complete execution of the Undertaker.
     */
    @Override
    public void run() {
        LOGGER.logInfo("Starting " + numberOfThreads + " execution threads");
        
        try {
            UndertakerWrapper wrapper = new UndertakerWrapper(resourceDir, linuxSourceTree, timeout);

            List<Thread> threads = new ArrayList<>(numberOfThreads);
            for (int i = 0; i < numberOfThreads; i++) {
                Thread th = new Thread(() -> {
                    File file;
                    while ((file = filesToParse.get()) != null) {
                        
                        if (isStopRequested()) {
                            break;
                        }
                        
                        LOGGER.logDebug("Starting extraction for file " + file.getPath());
                        try {
                            runOnFile(file, wrapper);
                        } catch (IOException | FormatException e) {
                            if (!isStopRequested()) {
                                provider.addException(new CodeExtractorException(file, e));
                            }
                        }
                    }
                });
                th.setName("UndertakerThread-" + i);
                th.start();
                threads.add(th);
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                }
            }
            
            LOGGER.logInfo("All threads done");
            provider.addResult(null);

        } catch (IOException e) {
            if (!isStopRequested()) {
                provider.addException(new CodeExtractorException(new File("*"), e));
            }
        }

    }

    /**
     * Runs undertaker on a single file.
     * 
     * @param file
     *            The source file in the source code tree to run on.
     * @param wrapper
     *            The wrapper to run the undertaker executable.
     * @throws IOException
     *             If running the process fails.
     * @throws FormatException
     *             If the output of the process is invalid.
     */
    private void runOnFile(File file, UndertakerWrapper wrapper) throws IOException, FormatException {
        String csv = wrapper.runOnFile(file);

        if (csv != null && !isStopRequested()) {
            CsvToAstConverter converter = new CsvToAstConverter(fuzzyBooleanParsing);
            SourceFile result = converter.convert(file, csv);

            if (!isStopRequested()) {
                provider.addResult(result);
            }

        } else if (!isStopRequested()) {
            provider.addException(new CodeExtractorException(file, "Undertaker execution not successful"));
        }
    }

}
