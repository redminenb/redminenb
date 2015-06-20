package com.kenai.redminenb.util;

import com.kenai.redminenb.RedmineConfig;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mykolas
 */
public class RedminePreferences {

    public static Preferences getPreferences() {
        return NbPreferences.forModule(RedmineConfig.class);
    }

}
