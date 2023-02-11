package com.lootintegrations.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration
{
    public final ForgeConfigSpec                      ForgeConfigSpecBuilder;
    public final ForgeConfigSpec.ConfigValue<Boolean> debugOutput;
    public final ForgeConfigSpec.ConfigValue<Boolean> showcontainerloottable;

    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {
        builder.push("Config category");

        builder.comment("Prints the added loot to the log if enabled: default:false");
        debugOutput = builder.define("debugOutput", false);

        builder.comment("Set to true to print containers loottable on first open: default:false");
        showcontainerloottable = builder.define("showcontainerloottable", false);

        // Escapes the current category level
        builder.pop();
        ForgeConfigSpecBuilder = builder.build();
    }
}
