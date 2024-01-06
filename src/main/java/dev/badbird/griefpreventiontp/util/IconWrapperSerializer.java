package dev.badbird.griefpreventiontp.util;

import com.google.gson.*;
import dev.badbird.griefpreventiontp.GriefPreventionTP;
import dev.badbird.griefpreventiontp.api.IconWrapper;
import org.bukkit.Material;

import java.lang.reflect.Type;

public class IconWrapperSerializer implements JsonSerializer<IconWrapper>, JsonDeserializer<IconWrapper> {
    @Override
    public JsonElement serialize(IconWrapper iconWrapper, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        if (iconWrapper.isMaterial()) {
            object.addProperty("material", iconWrapper.getMaterial().name());
        } else if (iconWrapper.isCustomIcon()) {
            object.addProperty("id", iconWrapper.getIcon().getId());
        }
        return object;
    }

    @Override
    public IconWrapper deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        if (object.has("material")) {
            Material material = Material.valueOf(object.get("material").getAsString());
            for (IconWrapper allowedIcon : GriefPreventionTP.getAllowedIcons()) {
                if (allowedIcon.isMaterial() && allowedIcon.getMaterial() == material)
                    return allowedIcon;
            }
        } else if (object.has("id")) {
            String id = object.get("id").getAsString();
            for (IconWrapper allowedIcon : GriefPreventionTP.getAllowedIcons()) {
                if (allowedIcon.isCustomIcon() && allowedIcon.getIcon().getId().equals(id))
                    return allowedIcon;
            }
        } else if (object.has("icon") && object.get("icon").isJsonObject()) { // old icon
            JsonObject icon = object.get("icon").getAsJsonObject();
            String id = icon.get("id").getAsString();
            for (IconWrapper allowedIcon : GriefPreventionTP.getAllowedIcons()) {
                if (allowedIcon.isCustomIcon() && allowedIcon.getIcon().getId().equals(id))
                    return allowedIcon;
            }
        }
        return null;
    }
}
