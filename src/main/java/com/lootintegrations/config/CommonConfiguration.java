package com.lootintegrations.config;

import com.cupboard.config.ICommonConfig;
import com.google.gson.JsonObject;

public class CommonConfiguration implements ICommonConfig
{
    public boolean showcontainerloottable = false;
    public boolean debugOutput            = false;
    public boolean skipMapItems = true;
    public boolean skipExistingItems = false;
    public int     moddedItemWeight  = 3;

    public CommonConfiguration()
    {

    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "Set to true to show containers loottable on first open: default:false");
        entry.addProperty("showcontainerloottable", showcontainerloottable);
        root.add("showcontainerloottable", entry);

        final JsonObject entrentry3 = new JsonObject();
        entrentry3.addProperty("desc:", "Skips map items during additional item generation, to avoid structure search lag (maps in the original chest still exist): default:true");
        entrentry3.addProperty("skipMapItems", skipMapItems);
        root.add("skipMapItems", entrentry3);

        final JsonObject entry4 = new JsonObject();
        entry4.addProperty("desc:",
            "Skips adding items already present in the loot, to avoid stacking additional items. Note that this increases the chance for modded loot aswell. default:false");
        entry4.addProperty("skipExistingItems", skipExistingItems);
        root.add("skipExistingItems", entry4);

        final JsonObject entry5 = new JsonObject();
        entry5.addProperty("desc:", "Add additional weight to modded items (e.g. 10 = 10 times more likely), making them more likely to be chosen default:3, minimum:0");
        entry5.addProperty("moddedItemWeight", moddedItemWeight);
        root.add("moddedItemWeight", entry5);

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
        skipMapItems = data.get("skipMapItems").getAsJsonObject().get("skipMapItems").getAsBoolean();
        skipExistingItems = data.get("skipExistingItems").getAsJsonObject().get("skipExistingItems").getAsBoolean();
        moddedItemWeight = Math.max(0, data.get("moddedItemWeight").getAsJsonObject().get("moddedItemWeight").getAsInt());
    }
}
