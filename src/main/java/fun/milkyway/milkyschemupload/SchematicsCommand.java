package fun.milkyway.milkyschemupload;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CommandAlias("schematics")
@CommandPermission("milkyschemupload.admin")
public class SchematicsCommand extends BaseCommand {

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Subcommand("reload")
    public void reload(CommandSender commandSender) {
        MilkySchemUpload.getInstance().reloadConfig();
        commandSender.sendMessage("§aReloaded config.");
    }

    @CommandCompletion("@schematics")
    @Subcommand("upload")
    public void upload(CommandSender commandSender, String schematicName) {
        commandSender.sendMessage("§eQueuing schematic upload...");
        executorService.submit(() -> {
            var file = MilkySchemUpload.getInstance().listSchematics().stream().filter(f -> f.getName().equals(schematicName)).findFirst().orElse(null);
            if (file == null) {
                commandSender.sendMessage("§cSchematic not found.");
                return;
            }
            commandSender.sendMessage("§eUploading schematic... Please wait...");
            try {
                var httpRequest = HttpRequest.newBuilder()
                        .uri(java.net.URI.create("https://temp.sh/"+file.getName()))
                        .header("Content-Type", "multipart/form-data")
                        .timeout(java.time.Duration.ofSeconds(20))
                        .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                        .build();
                var response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    commandSender.sendMessage("§cError while uploading schematic: §f" + response.body());
                    return;
                }
                commandSender.sendMessage("§aSchematic uploaded: §f" + response.body());
                MilkySchemUpload.getInstance().getLogger().info("§aSchematic uploaded: §f" + response.body());
            } catch (Exception e) {
                commandSender.sendMessage("§cError while uploading schematic: §f" + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
