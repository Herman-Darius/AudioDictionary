package com.dictionary.app.Services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class VirusScanService {
    /*
    @Value("${clam.scan-command}")
    private String scanCmd;
    */
    /**
     * @return true if 'clamscan <file>' returns exitCode == 0
     */
    /*
    public boolean isClean(File file) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(scanCmd, "--no-summary", file.getAbsolutePath())
                .redirectErrorStream(true)
                .start();
        int code = p.waitFor();
        return code == 0;
    }
    */

}

