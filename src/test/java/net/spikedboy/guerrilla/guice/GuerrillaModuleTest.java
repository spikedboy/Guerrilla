package net.spikedboy.guerrilla.guice;

import com.google.inject.Guice;
import net.spikedboy.guerrilla.GuerrillaPlugin;
import org.bukkit.Server;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class GuerrillaModuleTest {

    private GuerrillaModule guerrillaModule;

    @Mock
    private GuerrillaPlugin guerrillaPlugin;

    @Mock
    private Server server;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        guerrillaModule = new GuerrillaModule();

    }

    @Test(enabled = false)
    public void testConfigure() throws Exception {
        guerrillaModule.setGuerrillaPluginSingletonInstance(guerrillaPlugin);
        when(guerrillaPlugin.getServer()).thenReturn(server);

        Guice.createInjector(guerrillaModule);
    }

}
