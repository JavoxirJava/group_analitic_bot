package group.bot.group_analitic_bot.bot;

import group.bot.group_analitic_bot.entity.Group;
import group.bot.group_analitic_bot.service.GroupService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class BotMethods {

    private final GroupService groupService;
    private final BotSettings botSettings;
    private final ButtonSettings buttonSettings;

    Map<Long, Integer> userCount = new HashMap<>(); // user qushgan son
    Map<Long, String> choose = new HashMap<>(); // careator panelda kerakli bulimni tanlash
    Map<Long, String> groupUsername = new HashMap<>();
    Map<Long, String> groupCount = new HashMap<>(); // gruppaga omdam qushish limiti
    Map<Long, Group> groupMap = new HashMap<>();
    List<Group> groups = new ArrayList<>(); // Groupni malumotlarini olish uchun harsafar bazaga bormaslik uchun

    Integer maxCount = 3; // user qushishi shart bulgan son

    public BotMethods(GroupService groupService, @Lazy BotSettings botSettings, ButtonSettings buttonSettings) {
        this.groupService = groupService;
        this.botSettings = botSettings;
        this.buttonSettings = buttonSettings;
    }

    public void message(Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        int count = userCount.get(userId) == null ? 0 : userCount.get(userId);
        if (message.hasText()) {
            SendMessage sm = new SendMessage(chatId.toString(), message.getText());
            String text = message.getText();
            if (Template.CREATOR_ID.equals(userId.toString())) creatorPanel(sm, text, chatId);
            else if (message.getChat().getType().equals("supergroup")) {
                if (isAdmin(chatId, userId)) adminPanel(sm, text);
                else userPanel(message, sm, chatId, userId);
            } else if (text.equals("/start")) sendMSG(sm, "Assalomu alaykum Adminga murojat qiling! @M_Javoxir_1");
        } else if (!message.getNewChatMembers().isEmpty()) {
            message.getNewChatMembers().forEach(user -> userCount.put(userId, count + 1));
            deleteMSG(message.getMessageId(), chatId);
        }
    }

    public void creatorPanel(SendMessage sm, String text, Long userId) {
        switch (text) {
            case "/start": {
                sm.setReplyMarkup(buttonSettings.getKeyboardButton(Template.ADMIN_PANEL));
                sendMSG(sm, "Kerakli bulimni tanlang!");
            }
            case "Add Group": {
                sendMSG(sm, "Guruh nomini username sini kiriting: ");
                choose.put(userId, "username");
            }
            case "Get Groups": {
                sm.setReplyMarkup(buttonSettings.getInlineMarkup(groupService.getAllGroups().stream().map(Group::getUsername).toList()));
                sendMSG(sm, "Guruhlar");
            }
            case "Delete Group": {
                sm.setReplyMarkup(buttonSettings.getInlineMarkup(groupService.getAllGroups().stream().map(Group::getUsername).toList()));
                sendMSG(sm, "O'chirmoqchi bulgan kruppangizni tanlang!");
                choose.put(userId, "DeleteGroup");
            }
            default: {
                if (choose.get(userId) != null) {
                    if (choose.get(userId).equals("username")) {
                        if (getGroup(text) == null) sendMSG(sm, "Guruh topilmadi! qayta urunib kuring");
                        else {
                            groupUsername.put(userId, text);
                            sendMSG(sm, "Groupga odam qushish limitini kiriting: ");
                            choose.put(userId, "count");
                        }
                    } else if (choose.get(userId).equals("count")) {
                        Chat group = getGroup(groupUsername.get(userId));
                        groupService.addGroup(Group.builder()
                                .chatId(group.getId())
                                .username(groupUsername.get(userId))
                                .addCount(Integer.parseInt(text)).build());
                        sendMSG(sm, "Guruh qo'shildi");
                        groupService.getAllGroups().forEach(g -> groupMap.put(g.getChatId(), g));
                    }
                }
            }
        }
    }

    public void adminPanel(SendMessage sm, String text) {
        if (text.startsWith("#set ")) {
            try {
                maxCount = Integer.parseInt(text.split(" ")[1]);
                sendMSG(sm, "Guruga odam qushish limiti " + maxCount + " ta ga uzgartirildi");
            } catch (Exception e) {
                sendMSG(sm, "Limitni tug'ri kiriting Msol: #set 5");
            }
        }
    }

    public void userPanel(Message message, SendMessage sm, Long chatId, Long userId) {
        if (userCount.get(userId) != null && userCount.get(userId) >= maxCount) {
            deleteMSG(message.getMessageId(), chatId);
            sendMSG(sm, "Guruhga " + maxCount + " ta odam qushing kiyin yoza olasiz! #" +
                    message.getFrom().getFirstName() + "\nsiz hozir " + userCount.get(userId)  + " ta odam qushgansiz");
            restrictUser(chatId, userId, 60);
        }
    }

    //  +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=  Messages  +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=
    public void sendMSG(SendMessage sendMessage, String text) {
        try {
            sendMessage.setText(text);
            botSettings.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("not execute");
        }
    }

    public void editCallbackQuery(CallbackQuery callbackQuery, InlineKeyboardMarkup newInlineKeyboard) {
        try {
            botSettings.execute(new EditMessageReplyMarkup(
                    callbackQuery.getMessage().getChatId().toString(),
                    callbackQuery.getMessage().getMessageId(),
                    null,
                    newInlineKeyboard
            ));
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendAnswer(String callbackQueryId, String text) {
        AnswerCallbackQuery acq = new AnswerCallbackQuery();
        acq.setText(text);
        acq.setShowAlert(true);
        acq.setCallbackQueryId(callbackQueryId);
        try {
            botSettings.executeAsync(acq);
        } catch (TelegramApiException e) {
            System.err.println("not answer");
        }
    }

    public void deleteMSG(Integer messageId, Long chatId) {
        try {
            botSettings.execute(new DeleteMessage(String.valueOf(chatId), messageId));
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void restrictUser(Long chatId, Long userId, Integer seconds) {
        ChatPermissions permissions = new ChatPermissions(false, false, false, false, false, false, false, false);
        RestrictChatMember rcm = new RestrictChatMember(chatId.toString(), userId, permissions);
        rcm.setUntilDate((int) (System.currentTimeMillis() / 1000L + seconds));
        try {
            botSettings.execute(rcm);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isAdmin(Long chatId, long userId) {
        try {
            String status = botSettings.execute(new GetChatMember(chatId.toString(), userId)).getStatus();
            return (status.equals("administrator") || status.equals("creator"));
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public Chat getGroup(String username) {
        try {
            return botSettings.execute(new GetChat(username));
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}