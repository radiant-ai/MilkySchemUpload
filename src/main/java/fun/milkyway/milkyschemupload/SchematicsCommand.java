package fun.milkyway.milkyschemupload;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.command.CommandSender;

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
            var post = new HttpPost("https://file.io/");
            var apiKey = MilkySchemUpload.getInstance().getConfig().getString("apiKey", "");
            if (!apiKey.isEmpty()) {
                post.addHeader("Authorization", "Bearer " + apiKey);
            }
            var fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
            var builder = MultipartEntityBuilder.create();
            builder.addPart("file", fileBody);
            var entity = builder.build();

            post.setEntity(entity);
            try {
                var client = HttpClientBuilder.create()
                        .setDefaultRequestConfig(RequestConfig.custom()
                                .setConnectTimeout(15000)
                                .setSocketTimeout(15000)
                                .setConnectionRequestTimeout(15000)
                                .build())
                        .build();

                var response = client.execute(post);

                var responseString = new BasicResponseHandler().handleResponse(response);

                if (responseString == null) {
                    commandSender.sendMessage("§cError while uploading schematic: "+response.getStatusLine().getStatusCode());
                    return;
                }

                var json = new Gson().fromJson(responseString, JsonObject.class);
                var link = json.get("link").getAsString();
                commandSender.sendMessage("§aSchematic uploaded: §f" + link);
                MilkySchemUpload.getInstance().getLogger().info("§aSchematic uploaded: §f" + link);
            } catch (Exception e) {
                commandSender.sendMessage("§cError while uploading schematic: §f" + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
