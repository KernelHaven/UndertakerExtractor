/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
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
package net.ssehub.kernel_haven.undertaker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * This class is a Wrapper for calling the tool Undertaker and receiving the
 * results. This Wrapper can only run on a Linux system.
 * 
 * @author Adam
 * @author Johannes
 */
public class UndertakerWrapper {

    private static final Logger LOGGER = Logger.get();
    
    private long timeout;
    
    private File sourceDir;
    
    private File pilztakerExe;
    
    /**
     * Creates a new wrapper.
     * 
     * @param resourceDir The directory where this extractor can stores. Must its resource not be null.
     * @param sourceDir The path to the source tree.
     * @param timeout The timeout in milliseconds until the pilztaker exe is killed.
     * 
     * @throws IOException If extracting the pilztaker exe fails.
     */
    public UndertakerWrapper(File resourceDir, File sourceDir, long timeout) throws IOException {
        this.sourceDir = sourceDir;
        this.timeout = timeout;
        
        // extract exe to run undertaker
        pilztakerExe = new File(resourceDir, "undertaker");
        if (!pilztakerExe.isFile()) {
            Util.extractJarResourceToFile("net/ssehub/kernel_haven/undertaker/res/pilztaker",
                    pilztakerExe);
            pilztakerExe.setExecutable(true);
        }
    }

    /**
     * Runs the wrapper on a single file.
     * 
     * @param file The file in the source tree to run on; relative to the source tree given
     *      in the constructor. Must not be <code>null</code>.
     * @return The resulting CSV. <code>null</code> if not successful.
     * 
     * @throws IOException If executing undertaker throws an IOException.
     */
    public String runOnFile(File file) throws IOException {
        LOGGER.logDebug("runUndertaker() called");
        
        boolean success = false;
        String stdout = null;
        
        if (new File(sourceDir, file.getPath()).isFile()) {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    pilztakerExe.getAbsolutePath(),
                    sourceDir.getAbsolutePath() + "/",
                    file.getPath());
            
            ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
            ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();
            
            success = Util.executeProcess(processBuilder, "Undertaker", stdoutStream, stderrStream, timeout);
            
            stdout = stdoutStream.toString();
            
            String stderr = stderrStream.toString();
            if (stderr != null && !stderr.equals("")) {
                LOGGER.logDebug(("Undertaker stderr:\n" + stderr).split("\n"));
            }
            
        }
        
        
        return success ? stdout : null;
    }

}
