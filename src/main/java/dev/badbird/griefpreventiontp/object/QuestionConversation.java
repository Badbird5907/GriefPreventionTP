package dev.badbird.griefpreventiontp.object;

import dev.badbird.griefpreventiontp.GriefPreventionTP;
import lombok.RequiredArgsConstructor;
import net.badbird5907.blib.objects.TypeCallback;
import net.kyori.adventure.text.Component;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class QuestionConversation extends StringPrompt {
    private final Component prompt;
    private final TypeCallback<Prompt, String> callback;

    @Override
    public String getPromptText(ConversationContext conversationContext) {
        return "";
    }

    @Override
    public Prompt acceptInput(ConversationContext conversationContext, String s) {
        return callback.callback(s);
    }

    public Prompt reprompt() {
        return this;
    }

    public void start(Player p) {
        p.closeInventory();
        p.sendMessage(prompt);
        GriefPreventionTP.getInstance().getConversationFactory().withFirstPrompt(this).withLocalEcho(false)
                .buildConversation(p).begin();
    }
}
