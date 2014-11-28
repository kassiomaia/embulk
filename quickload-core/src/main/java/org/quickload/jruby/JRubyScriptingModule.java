package org.quickload.jruby;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import com.google.inject.Module;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import org.jruby.CompatVersion;
import org.jruby.embed.ScriptingContainer;
import org.quickload.plugin.PluginSource;

public class JRubyScriptingModule
        implements Module
{
    public JRubyScriptingModule()
    {
        // TODO get jruby-home from systemConfig to call jruby.container.setHomeDirectory
        // TODO get jruby-load-paths from systemConfig to call jruby.container.setLoadPaths
        // TODO get jruby-home from systemConfig to call jruby.container.setHomeDirectory
    }

    @Override
    public void configure(Binder binder)
    {
        binder.bind(ScriptingContainer.class).toProvider(new ScriptingContainerProvider()).in(Scopes.SINGLETON);
        //binder.bind(JRubyModule.class).in(Scopes.SINGLETON);

        Multibinder<PluginSource> multibinder = Multibinder.newSetBinder(binder, PluginSource.class);
        multibinder.addBinding().to(JRubyPluginSource.class);
    }

    private static class ScriptingContainerProvider
            implements Provider<ScriptingContainer>
    {
        public ScriptingContainer get()
        {
            ScriptingContainer jruby = new ScriptingContainer();
            jruby.setCompatVersion(CompatVersion.RUBY1_9);

            // search quickload.rb path
            List<String> loadPaths = new ArrayList<String>();

            String coreJarPath = JRubyScriptingModule.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            loadPaths.add(coreJarPath);

            String home = System.getenv("QUICKLOAD_HOME");
            if (home != null && !home.isEmpty()) {
                String homeLibPath = home + File.separator + "lib";
                loadPaths.add(homeLibPath);
            }

            jruby.setLoadPaths(loadPaths);

            // load quickload.rb from $QUICKLOAD_HOME/lib
            jruby.runScriptlet("require 'quickload'");

            return jruby;
        }
    }
}
