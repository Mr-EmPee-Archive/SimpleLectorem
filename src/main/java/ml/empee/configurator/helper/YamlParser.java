package ml.empee.configurator.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.AnchorNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.UnicodeReader;

import com.google.common.base.Preconditions;

public class YamlParser {

  private final YamlConstructor constructor;
  private final YamlRepresenter representer;
  private final Yaml yaml;


  private final HashMap<String, List<CommentLine>> blockComments = new HashMap<>();
  private final HashMap<String, List<CommentLine>> inlineComments = new HashMap<>();

  public YamlParser() {
    constructor = new YamlConstructor();
    representer = new YamlRepresenter();
    representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

    DumperOptions yamlDumperOptions = new DumperOptions();
    yamlDumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    yamlDumperOptions.setProcessComments(true);
    LoaderOptions yamlLoaderOptions = new LoaderOptions();
    yamlLoaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE); // SPIGOT-5881: Not ideal, but was default pre SnakeYAML 1.26
    yamlLoaderOptions.setProcessComments(true);

    yaml = new Yaml(constructor, representer, yamlDumperOptions, yamlLoaderOptions);
  }

  public void loadComments(File file) throws IOException, InvalidConfigurationException {
    BufferedReader input = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
    StringBuilder builder = new StringBuilder();

    try {
      String line;

      while ((line = input.readLine()) != null) {
        builder.append(line);
        builder.append('\n');
      }
    } finally {
      input.close();
    }

    loadComments(builder.toString());
  }
  public void write(File file, ConfigurationSection config) throws IOException {
    try(BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
      yaml.serialize(toNodeTree(config), writer);
    }
  }

  public void loadComments(String contents) throws InvalidConfigurationException {
    Preconditions.checkArgument(contents != null, "Contents cannot be null");

    MappingNode node;
    try (Reader reader = new UnicodeReader(new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)))) {
      node = (MappingNode) yaml.compose(reader);
    } catch (YAMLException | IOException e) {
      throw new InvalidConfigurationException(e);
    } catch (ClassCastException e) {
      throw new InvalidConfigurationException("Top level is not a Map.");
    }

    if (node != null) {
      loadComments(node);
    }
  }
  private void loadComments(MappingNode input) {
    constructor.flattenMapping(input);
    for (NodeTuple nodeTuple : input.getValue()) {
      Node key = nodeTuple.getKeyNode();
      String keyString = String.valueOf(constructor.construct(key));
      Node value = nodeTuple.getValueNode();

      while (value instanceof AnchorNode) {
        value = ((AnchorNode) value).getRealNode();
      }

      blockComments.put(keyString, key.getBlockComments());
      if (value instanceof MappingNode || value instanceof SequenceNode) {
        inlineComments.put(keyString, key.getInLineComments());
      } else {
        inlineComments.put(keyString, value.getInLineComments());
      }
    }
  }

  private MappingNode toNodeTree(ConfigurationSection section) {
    List<NodeTuple> nodeTuples = new ArrayList<>();
    for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
      Node key = representer.represent(entry.getKey());
      String stringKey = section.getCurrentPath() + "." + entry.getKey();
      Node value;
      if (entry.getValue() instanceof ConfigurationSection) {
        value = toNodeTree((ConfigurationSection) entry.getValue());
      } else {
        value = representer.represent(entry.getValue());
      }
      key.setBlockComments(blockComments.get(stringKey));
      if (value instanceof MappingNode || value instanceof SequenceNode) {
        key.setInLineComments(inlineComments.get(stringKey));
      } else {
        value.setInLineComments(inlineComments.get(stringKey));
      }

      nodeTuples.add(new NodeTuple(key, value));
    }

    return new MappingNode(Tag.MAP, nodeTuples, DumperOptions.FlowStyle.BLOCK);
  }

  public void setComments(String path, List<String> comments) {
    if(comments == null) {
      blockComments.remove(path);
      return;
    }

    blockComments.put(path,
        comments.stream().map(c -> new CommentLine(null, null, c, CommentType.BLOCK))
        .collect(Collectors.toList())
    );
  }

  public void setInlineComments(String path, List<String> comments) {
    if(comments == null) {
      inlineComments.remove(path);
      return;
    }

    inlineComments.put(path,
        comments.stream().map(c -> new CommentLine(null, null, c, CommentType.IN_LINE))
            .collect(Collectors.toList())
    );
  }

}
