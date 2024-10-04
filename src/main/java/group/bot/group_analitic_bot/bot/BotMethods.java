package group.bot.group_analitic_bot.bot;

import group.bot.group_analitic_bot.entity.Group;
import group.bot.group_analitic_bot.service.GroupService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
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
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

@Service
public class BotMethods {

    private final GroupService groupService;
    private final BotSettings botSettings;
    private final ButtonSettings buttonSettings;

    Map<Long, Integer> userCount = new HashMap<>(); // user qushgan son
    Map<Long, String> choose = new HashMap<>(); // careator panelda kerakli bulimni tanlash
    Map<Long, String> groupUsername = new HashMap<>();
    Map<Long, Group> groupMap = new HashMap<>(); // Groupni malumotlarini olish uchun harsafar bazaga bormaslik uchun
    Map<Long, Long> chooseGroup = new HashMap<>(); // vaqtinchalik tanlangan user
    int blockSeconds = 60; // 5 daqiqa

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
            if (groupMap.isEmpty()) groupService.getAllGroups().forEach(g -> groupMap.put(g.getChatId(), g));
            else if (message.getChat().getType().equals("supergroup") && groupMap.get(chatId) != null) {
                if (isAdmin(chatId, userId)) adminPanel(chatId, sm, text);
                else userPanel(message, sm, chatId, userId);
            } else if (text.equals("/start")) sendMSG(sm, "Assalomu alaykum Adminga murojat qiling! @M_Javoxir_1");
        } else if (!message.getNewChatMembers().isEmpty()) {
            message.getNewChatMembers().forEach(user -> {
                if (user.getIsBot()) deleteUserInGroup(chatId, user.getId());
                else userCount.put(userId, count + 1);
            });
            deleteMSG(message.getMessageId(), chatId);
        } else if (message.getLeftChatMember() != null) deleteMSG(message.getMessageId(), chatId);

    }

    public void callbackData(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();
        SendMessage sm = new SendMessage(userId.toString(), data);
        switch (data) {
            case "O'chirish": { // TODO guruh o'chirishda xatolik bor
                groupService.deleteGroup(chooseGroup.get(userId));
                sm.setReplyMarkup(buttonSettings.getKeyboardButton(Template.ADMIN_PANEL));
                sendMSG(sm, "Guruh o'chirildi!");
                groupService.getAllGroups().forEach(g -> groupMap.put(g.getChatId(), g));
            }
            case "Bekor qilish": {
                sm.setReplyMarkup(buttonSettings.getKeyboardButton(Template.ADMIN_PANEL));
                sendMSG(sm, "Kearakli bulimni tanlang!");
            }
            default: {
                if (choose.get(userId).equals("DeleteGroup")) {
                    chooseGroup.put(userId, getGroup(data).getId());
                    sm.setReplyMarkup(buttonSettings.getInlineMarkup(Template.IS_DELETED));
                    sendMSG(sm, "Haqiqatdanham guruhni o'chirmoqchimisiz?");
                }
            }
        }
    }

    public void creatorPanel(SendMessage sm, String text, Long userId) {
        switch (text) {
            case "/start": {
                sm.setReplyMarkup(buttonSettings.getKeyboardButton(Template.ADMIN_PANEL));
                sendMSG(sm, "Kerakli bulimni tanlang!");
                break;
            }
            case "Add Group": {
                sendMSG(sm, "Guruh username sini kiriting: ");
                choose.put(userId, "username");
                break;
            }
            case "Get Groups": {
                sm.setReplyMarkup(buttonSettings.getInlineMarkup(groupService.getAllGroups().stream().map(Group::getUsername).toList()));
                sendMSG(sm, "Guruhlar");
                break;
            }
            case "Delete Group": {
                sm.setReplyMarkup(buttonSettings.getInlineMarkup(groupService.getAllGroups().stream().map(Group::getUsername).toList()));
                sendMSG(sm, "O'chirmoqchi bulgan kruppangizni tanlang!");
                choose.put(userId, "DeleteGroup");
                break;
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
                                .username(group.getUserName() == null ? group.getId().toString() : group.getUserName())
                                .addCount(Integer.parseInt(text)).build());
                        sendMSG(sm, "Guruh qo'shildi");
                        groupService.getAllGroups().forEach(g -> groupMap.put(g.getChatId(), g));
                    }
                }
            }
        }
    }

    public void adminPanel(Long chatId, SendMessage sm, String text) {
        if (text.startsWith("#set ")) {
            try {
                groupService.editGroupAddCountByChatId(chatId, Integer.parseInt(text.split(" ")[1]));
                groupService.getAllGroups().forEach(g -> groupMap.put(g.getChatId(), g));
                sendMSG(sm, "Guruga odam qushish limiti " + groupMap.get(chatId).getAddCount() + " ta ga uzgartirildi");
            } catch (Exception e) {
                sendMSG(sm, "Limitni tug'ri kiriting Misol: #set 5");
            }
        }
    }

    public void userPanel(Message message, SendMessage sm, Long chatId, Long userId) {
        String regex = ".*(https?://|www\\.|@[a-zA-Z0-9._-]+|\\b[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,3}\\b).*";
        String text = message.getText();
        if (text.matches(regex)) deleteMSG(message.getMessageId(), chatId);
        int count = userCount.get(userId) == null ? 0 : userCount.get(userId);
        if (count < groupMap.get(chatId).getAddCount()) {
            deleteMSG(message.getMessageId(), chatId);
            sendMSG(sm, "Guruhga " + groupMap.get(chatId).getAddCount() + " ta odam qo'shing keyin yoza olasiz! #" +
                    message.getFrom().getFirstName() + "\nsiz hozir " + count  + " ta odam qo'shgansiz.");
            restrictUser(chatId, userId, blockSeconds);
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
            return null;
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
        ChatPermissions permissions = new ChatPermissions(false, false, false, false, true, false, true, false);
        RestrictChatMember rcm = new RestrictChatMember(chatId.toString(), userId, permissions);
        rcm.setUntilDate((int) (System.currentTimeMillis() / 1000L + seconds));
        try {
            botSettings.execute(rcm);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteUserInGroup(Long chatId, Long userId) {
        try {
            botSettings.execute(new BanChatMember(chatId.toString(), userId));
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}