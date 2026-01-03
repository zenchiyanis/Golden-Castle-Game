package save;

import java.nio.file.Files;
import java.nio.file.Path;

public class SaveManager {
    private final GameSerializer serializer = new GameSerializer();
    private final Path file = Path.of("savegame.txt");

    public void saveToDisk(SaveData data) {
        try {
            Files.writeString(file, serializer.serialize(data));
        } catch (Exception ignored) {
        }
    }

    public SaveData loadFromDisk() {
        try {
            if (!Files.exists(file)) return null;
            String raw = Files.readString(file);
            return serializer.deserialize(raw);
        } catch (Exception e) {
            return null;
        }
    }
}
