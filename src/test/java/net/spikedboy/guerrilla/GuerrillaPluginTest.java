package net.spikedboy.guerrilla;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GuerrillaPluginTest {

    private GuerrillaPlugin guerrillaPlugin;

    @BeforeMethod
    public void setUp() throws Exception {
        guerrillaPlugin = new GuerrillaPlugin();
    }

    @Test
    public void testOnEnableIsSuccessful() throws Exception {
        guerrillaPlugin.onEnable();
    }

    @Test
    public void testOnDisableIsSuccessful() throws Exception {
        guerrillaPlugin.onDisable();
    }

    @Test
    public void testOnCommand() throws Exception {
//        guerrillaPlugin.onCommand();
    }

}