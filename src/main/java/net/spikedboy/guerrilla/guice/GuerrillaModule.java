package net.spikedboy.guerrilla.guice;

import com.google.inject.AbstractModule;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.commands.CommandExecutor;
import net.spikedboy.guerrilla.commands.OldCommandExecutor;
import net.spikedboy.guerrilla.listeners.GuerrillaBlockListener;
import net.spikedboy.guerrilla.listeners.GuerrillaEntityListener;
import net.spikedboy.guerrilla.listeners.GuerrillaPlayerListener;
import net.spikedboy.guerrilla.listeners.GuerrillaWorldListener;

public class GuerrillaModule extends AbstractModule {

    private GuerrillaPlugin guerrillaPluginSingletonInstance;

    @Override
    public void configure() {
        bind(GuerrillaPlugin.class).toInstance(guerrillaPluginSingletonInstance);
        bind(CommandExecutor.class).to(OldCommandExecutor.class);

        bind(GuerrillaBlockListener.class).toInstance(new GuerrillaBlockListener(guerrillaPluginSingletonInstance));
        bind(GuerrillaEntityListener.class).toInstance(new GuerrillaEntityListener(guerrillaPluginSingletonInstance));
        bind(GuerrillaPlayerListener.class).toInstance(new GuerrillaPlayerListener(guerrillaPluginSingletonInstance));
        bind(GuerrillaWorldListener.class).toInstance(new GuerrillaWorldListener(guerrillaPluginSingletonInstance));
    }

    public void setGuerrillaPluginSingletonInstance(GuerrillaPlugin guerrillaPluginSingletonInstance) {
        this.guerrillaPluginSingletonInstance = guerrillaPluginSingletonInstance;
    }
}
