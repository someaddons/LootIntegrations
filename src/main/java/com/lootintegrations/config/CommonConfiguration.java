package com.lootintegrations.config;

import com.cupboard.config.ICommonConfig;
import com.google.gson.JsonObject;

public class CommonConfiguration implements ICommonConfig
{
    public boolean showcontainerloottable = false;
    public boolean debugOutput            = false;

    public CommonConfiguration()
    {

    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "Set to true to print containers loottable to chat on first open: default:false");
        entry.addProperty("showcontainerloottable", showcontainerloottable);
        root.add("showcontainerloottable", entry);

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty("desc:", "Shows the added loot in the log if enabled: default:false");
        entry2.addProperty("debugOutput", debugOutput);
        root.add("debugOutput", entry2);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        showcontainerloottable = data.get("showcontainerloottable").getAsJsonObject().get("showcontainerloottable").getAsBoolean();
        debugOutput = data.get("debugOutput").getAsJsonObject().get("debugOutput").getAsBoolean();
    }
}
