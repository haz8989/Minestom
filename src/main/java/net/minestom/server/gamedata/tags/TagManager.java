package net.minestom.server.gamedata.tags;

import com.google.gson.JsonObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles loading and caching of tags.
 */
public final class TagManager {
    private final Map<Tag.BasicType, List<Tag>> tagMap = new ConcurrentHashMap<>();

    public TagManager() {
        // Load required tags from files
        for (var type : Tag.BasicType.values()) {
            final var json = Registry.load(type.getResource());
            final var tagIdentifierMap = tagMap.computeIfAbsent(type, s -> new CopyOnWriteArrayList<>());
            json.keySet().forEach(tagName -> {
                final var tag = new Tag(NamespaceID.from(tagName), getValues(json, tagName));
                tagIdentifierMap.add(tag);
            });
        }
    }

    public @Nullable Tag getTag(Tag.BasicType type, String namespace) {
        final var tags = tagMap.get(type);
        for (var tag : tags) {
            if (tag.getName().asString().equals(namespace))
                return tag;
        }
        return null;
    }

    public Map<Tag.BasicType, List<Tag>> getTagMap() {
        return Collections.unmodifiableMap(tagMap);
    }

    private Set<NamespaceID> getValues(JsonObject main, String value) {
        JsonObject tagObject = main.getAsJsonObject(value);
        final var tagValues = tagObject.getAsJsonArray("values");
        Set<NamespaceID> result = new HashSet<>(tagValues.size());
        tagValues.forEach(jsonElement -> {
            final String tagString = jsonElement.getAsString();
            if (tagString.startsWith("#")) {
                result.addAll(getValues(main, tagString.substring(1)));
            } else {
                result.add(NamespaceID.from(tagString));
            }
        });
        return result;
    }
}
