package com.wagologies.bedwarsPractice;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class WorldCopier {
    public JavaPlugin plugin;
    public final File WORLD_FOLDER;
    public WorldCopier(JavaPlugin plugin) {
        this.plugin = plugin;
        WORLD_FOLDER = createAndGetWorldFolder();
        checkAndDownloadWorldFiles();
    }
    private void checkAndDownloadWorldFiles()
    {
        File template1 = new File(WORLD_FOLDER.getAbsolutePath()+File.separatorChar+"Bedwars Template");
        if(!(template1.exists() && template1.isDirectory())) {
            deleteFolder(WORLD_FOLDER, false);
            try {
                String zipFilePath = WORLD_FOLDER.getAbsolutePath() + File.separatorChar + "Bedwars Template.zip";
                plugin.getLogger().info("Downloading template worlds...");
                if (URLReader.DownloadFile(new URL("https://wagologies.com/ManhuntTemplateWorlds/Bedwars%20Template.zip"), zipFilePath)) {
                    plugin.getLogger().info("Successfully downloaded zip file");
                    plugin.getLogger().info("Unzipping file...");
                    ZipFile templateWorldsZipFile = new ZipFile(zipFilePath);
                    templateWorldsZipFile.extractAll(WORLD_FOLDER.getAbsolutePath() + File.separatorChar);
                    plugin.getLogger().info("Successfully unzipped template worlds");
                    plugin.getLogger().info("Deleting zip file");
                    File zipFile = new File(zipFilePath);
                    if (zipFile.delete()) {
                        plugin.getLogger().info("Successfully deleted zip file");
                    } else {
                        plugin.getLogger().info("Failed to delete zip file");
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().info(e.toString());
            }
        }
    }
    private void deleteFolder(File folder, boolean deleteSelf) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f, true);
                } else {
                    f.delete();
                }
            }
        }
        if(deleteSelf)
            folder.delete();
    }
    private File createAndGetWorldFolder()
    {
        String worldFolderPath = plugin.getDataFolder().getAbsolutePath();
        File worldFolder = new File(worldFolderPath,"TemplateWorlds");
        if(worldFolder.exists())
        {
           return worldFolder;
        }
        if(worldFolder.mkdir())
        {
            plugin.getLogger().info("Got Here!");
        }
        return worldFolder;
    }
    public World copyTemplateWorld(String worldName)
    {
        String templatePath = WORLD_FOLDER.getAbsolutePath() + File.separatorChar + "Bedwars Template 2";
        File templateFile = new File(templatePath);
        if(!templateFile.exists() || !templateFile.isDirectory())
        {
            plugin.getLogger().info("Template directory doesn't exist");
            return null;
        }
        try {
            File worldDir = new File(Bukkit.getServer().getWorldContainer(), worldName);
            if(worldDir.exists())
                deleteFolder(worldDir,true);
            FileUtils.copyDirectory(templateFile, worldDir);
            return new WorldCreator(worldName).createWorld();
        } catch (IOException e) {
            plugin.getLogger().info(e.getMessage());
            return null;
        }
    }

}
