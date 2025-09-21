package mod.fuji.module.initializer.profiler.config;

import java.util.ArrayList;
import java.util.List;

public class ProfilerConfigModel {

    public FileSystem fileSystem = new FileSystem();
    public static class FileSystem {
        public List<String> blacklisted_filesystem = new ArrayList<>() {
            {
                this.add(".*firmware.*");
                this.add(".*systemd.*");
                this.add(".*/proc.*");
                this.add(".*/boot.*");
                this.add(".*/run.*");
            }
        };

    }

}

