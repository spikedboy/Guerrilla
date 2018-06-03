package net.spikedboy.guerrilla.guice;

import com.google.inject.AbstractModule;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import net.spikedboy.guerrilla.commands.CommandExecutor;
import net.spikedboy.guerrilla.commands.OldCommandExecutor;

public class GuerrillaModule extends AbstractModule {

    private GuerrillaPlugin guerrillaPluginSingletonInstance;

    @Override
    protected void configure() {
        bind(GuerrillaPlugin.class).toInstance(guerrillaPluginSingletonInstance);
        bind(CommandExecutor.class).to(OldCommandExecutor.class);
    }

    public void setGuerrillaPluginSingletonInstance(GuerrillaPlugin guerrillaPluginSingletonInstance) {
        this.guerrillaPluginSingletonInstance = guerrillaPluginSingletonInstance;
    }
}
