package ml.empee.configurator;

import org.bukkit.configuration.ConfigurationSection;

import lombok.Getter;

public abstract class Config {

    @Getter
    protected final Config parent;
    protected ConfigurationSection configurationSection;

    protected Config(Config parent) {
        this.parent = parent;
    }

    public String getAbsolutePath() {
        Config config = this;
        StringBuilder path = new StringBuilder();
        while(config.parent != null) {
            path.insert(0, config.getPath() + ".");
            config = config.parent;
        };

        return config.getPath() + " / " + path.toString();
    }

    public String getPath() {
        return configurationSection.getCurrentPath();
    }

}
