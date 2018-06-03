package net.spikedboy.guerrilla.commands;

import net.spikedboy.guerrilla.commands.commandprocessors.ToggleGuerrillaChat;

import java.util.LinkedList;
import java.util.List;

class CommandListFactory {
    public List<CommandProcessor> build() {
        LinkedList<CommandProcessor> commandProcessors = new LinkedList<>();

        commandProcessors.add(new ToggleGuerrillaChat());

        return commandProcessors;
    }
}
