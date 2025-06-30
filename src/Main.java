import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import okhttp3.*;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main extends ListenerAdapter {
    private static final String userHome = System.getProperty("user.home");
    private static File currentDirectory = new File(System.getProperty("user.dir"));
    // private static String webhook = "https://canary.discord.com/api/webhooks/1240791438128840715/ObvwZMrvtq8DrLiKuHu4MDTwYUXtOysWkQoQMDy-nfR3TqB32TzSrJS99rpDH33DxHbD";
    private static String token;
    public static JDA jda;
    public static void main(String[] args) throws LoginException, IOException {
        Config.loadConfig("config.json");
        token = Config.getToken();

        jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("with Tux on Discord"))
                .addEventListeners(new Main())
                .build();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String command;

        try {
            while (true) {
                System.out.print(getCurrentDirectory());
                command = reader.readLine();

                if (command == null || command.equalsIgnoreCase("exit")) {
                    sendMessage("Exiting...");
                    break;
                }

                if (command.startsWith("cd ")) {
                    String directoryPath = command.substring(3).trim();
                    changeDirectory(directoryPath);

                } else {
                    executeCommand(command);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getCurrentDirectory() {
        return currentDirectory.toPath().normalize().toAbsolutePath().toString().replace(userHome, "~") + "> ";

    }

    private static void changeDirectory(String directoryPath) throws InterruptedException {
        File newDirectory = new File(currentDirectory, directoryPath);

        if (newDirectory.isDirectory()) {
            currentDirectory = newDirectory;
            System.setProperty("user.dir", currentDirectory.getAbsolutePath());
            sendMessage("*Change directory to*: " + getCurrentDirectory());
        } else {
            sendMessage("*Directory not found* : " + directoryPath);
        }
    }

    private static void executeCommand(String command) {
        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
            builder.directory(currentDirectory);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                sendMessage(line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                sendMessage("*Error*: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(String message) throws InterruptedException {

        TextChannel channel = jda.getTextChannelById(Config.getChannel());
        if (message.contains(token)) message = message.replace(token, "CENSORED");
        System.out.println(message);
        channel.sendMessage(message).queue();

        /*OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"content\":\"" + message + "\"}");

        Request request = new Request.Builder()
                .url(webhook)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //System.out.println("hello world");
        super.onMessageReceived(event);
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User author = message.getAuthor();

        if (message.getAuthor().isBot()) {
            return;
        }
        if (!event.getChannel().getId().equals(Config.getChannel())) {
            return;
        }

        message.delete().queue();

        try {
            sendMessage("*" + author.getName() + "*: " + getCurrentDirectory() + content);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        executeCommand(content);
    }


}
