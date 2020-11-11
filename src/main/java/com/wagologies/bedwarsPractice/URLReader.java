package com.wagologies.bedwarsPractice;

import com.wagologies.bedwarsPractice.BedwarsPractice;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class URLReader {
    public static boolean DownloadFile(URL url, String path)
    {
        try (BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
             FileOutputStream fileOS = new FileOutputStream(path)) {
            byte data[] = new byte[1024];
            int byteContent;
            while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                fileOS.write(data, 0, byteContent);
            }
            return true;
        } catch (IOException e) {
            // handles IO exceptions
            BedwarsPractice.instance.getLogger().info(e.getMessage());
            return false;
        }
    }
}