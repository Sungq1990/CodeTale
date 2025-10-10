package com.zjj.fishPlugin.config;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

public final class PluginBundle extends AbstractBundle {
    @NonNls
    private static final String BUNDLE = "messages.Bundle";
    private static final PluginBundle INSTANCE = new PluginBundle();

    private PluginBundle() {
        super(BUNDLE);
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}