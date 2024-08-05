package com.keurig.combatlogger.punishment;

import com.keurig.combatlogger.CombatLogger;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PunishmentLoader {

    @Getter
    private CombatLogger plugin;

    @Getter
    private YamlDocument config;


    public PunishmentLoader(CombatLogger plugin, String fileName) {
        this.plugin = plugin;
        try {
            config = YamlDocument.create(new File(plugin.getDataFolder(), fileName), plugin.getResource(fileName),
                    GeneralSettings.builder().setDefaultString("").setDefaultList(ArrayList::new).setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadPunishments() {
        config.getSection("permission");

        // time needs to be set in here
    }
}
