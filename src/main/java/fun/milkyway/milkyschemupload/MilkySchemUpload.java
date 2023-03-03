package fun.milkyway.milkyschemupload;

import co.aikar.commands.PaperCommandManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MilkySchemUpload extends JavaPlugin {

    private static MilkySchemUpload instance;

    @Override
    public void onEnable() {
        instance = this;
        var commandManager = new PaperCommandManager(this);
        commandManager.getCommandCompletions().registerAsyncCompletion("schematics", c -> {
            var files = listSchematics();
            return files.stream().map(File::getName).toList();
        });
        commandManager.registerCommand(new SchematicsCommand());
        saveDefaultConfig();
        reloadConfig();
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static MilkySchemUpload getInstance() {
        return instance;
    }

    public List<File> listSchematics() {
        var files = new ArrayList<File>();
        var faweFolderSchematics = new File("plugins/FastAsyncWorldEdit/schematics");
        if (faweFolderSchematics.exists()) {
            files.addAll(listFiles(faweFolderSchematics));
        }
        var worldEditFolderSchematics = new File("plugins/WorldEdit/schematics");
        if (worldEditFolderSchematics.exists()) {
            files.addAll(listFiles(worldEditFolderSchematics));
        }
        return files;
    }

    private List<File> listFiles(@NotNull File directory) {
        var files = new ArrayList<File>();
        var listFiles = directory.listFiles();
        if (listFiles == null) {
            return files;
        }
        for (File file : listFiles) {
            if (file.isDirectory()) {
                files.addAll(listFiles(file));
            } else {
                files.add(file);
            }
        }
        return files;
    }
}
