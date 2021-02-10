package Worker;

import Costants.Costanti;
import Database.Connection;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Bot extends TelegramLongPollingBot {
    SendMessage messageToMe = new SendMessage().setChatId(Costanti.IDMochi);

    public Bot() {
        super();
        messageToMe.setText("Bot avviato");
        try {
            execute(messageToMe);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        if (System.getenv("Bot") != null && System.getenv("Bot").equals("Heroku")) {
            return Costanti.Bot_Name_Heroku;
        } else {
            return Costanti.Bot_Name_Locale;
        }
    }

    public String getBotToken() {
        if (System.getenv("Bot") != null && System.getenv("Bot").equals("Heroku")) {
            return Costanti.Bot_Token_Heroku;
        } else {
            return Costanti.Bot_Token_Locale;
        }
    }

    public void onUpdateReceived(Update update) {
        int tag = 0;
        String comando = update.getMessage().getText();
        User user = update.getMessage().getFrom();
        String Nome = user.getUserName();
        Integer ID = user.getId();
        Long chatID = update.getMessage().getChatId();
        Integer messageID = update.getMessage().getMessageId();
        int Cancella = 0;
        StringBuilder Messaggio = new StringBuilder();
        if (comando.startsWith("/list")) {
            Cancella = 1;
            try (java.sql.Connection con = new Connection().getConnection();
                 ResultSet Anime = con.createStatement().executeQuery("SELECT * FROM \"public\".\"Anime\"")) {
                Messaggio.append("@").append(Nome).append(":\n");
                while (Anime.next()) {
                    String NomeAnime = Anime.getString("Nome");
                    ResultSet Nomi = con.createStatement().executeQuery("SELECT * FROM \"public\".\"" + NomeAnime + "\"");
                    while (Nomi.next()) {
                        if (Nomi.getString("id").equals(String.valueOf(ID))) {
                            Messaggio.append("✅ ");
                            break;
                        }
                    }
                    Messaggio.append(NomeAnime).append("\n");
                }
            } catch (SQLException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if (comando.startsWith("/add")) {
            Cancella = 1;
            tag = 1;
            int flag = 0;
            String Anime = comando.replace("/add ", "");
            try (java.sql.Connection con = new Connection().getConnection();
                 ResultSet Nomi = con.createStatement().executeQuery("SELECT * FROM \"public\".\"" + Anime + "\"")) {
                while (Nomi.next() && flag == 0) {
                    if (Nome.equals(Nomi.getString("nome"))) {
                        flag = 1;
                    }
                }
                if (flag == 1) {
                    Messaggio.append("Sei già in questa lista");
                } else {
                    con.createStatement().executeUpdate("INSERT INTO \"public\".\"" + Anime + "\" (nome,id) VALUES ('" + Nome + "','" + ID + "')");
                    Messaggio.append("Aggiunto alla lista!");
                }
            } catch (SQLException | URISyntaxException e) {
                Messaggio.append("L'anime non esiste!");
            }
        }
        if (comando.startsWith("/tag") && String.valueOf(ID).equals(Costanti.IDMochi)) {
            Cancella = 1;
            String Anime = comando.replace("/tag ", "");
            try (java.sql.Connection con = new Connection().getConnection();
                 ResultSet Nomi = con.createStatement().executeQuery("SELECT * FROM \"public\".\"" + Anime + "\"")) {
                while (Nomi.next()) {
                    Messaggio.append("@").append(Nomi.getString("nome")).append("\n");
                }
            } catch (SQLException | URISyntaxException throwables) {
                throwables.printStackTrace();
            }
        }
        if (Cancella == 1) {
            DeleteMessage Delete = new DeleteMessage().setChatId(chatID).setMessageId(messageID);
            try {
                SendMessageToMe(comando, Nome);
                execute(Delete);
                SendMessage(Messaggio.toString(), String.valueOf(chatID), tag);
            } catch (TelegramApiException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void SendMessage(String Messaggio, String ID, int tag) throws TelegramApiException, InterruptedException {
        SendMessage message = new SendMessage().setText(Messaggio).setChatId(ID);
        Message Bot = execute(message);
        Thread.sleep(1000);
        if (tag == 1) {
            DeleteMessage Delete = new DeleteMessage().setChatId(Bot.getChatId()).setMessageId(Bot.getMessageId());
            execute(Delete);
        }
    }

    void SendMessageToMe(String comando, String Nome) throws TelegramApiException {
        SendMessage message = new SendMessage().setChatId(Costanti.IDMochi).setText(comando + "\n" + Nome);
        execute(message);
    }
}