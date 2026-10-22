package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestMap {
    public static final List<String> ROOMS = List.of(
            "0,Start,9,1,-1,-1,You are at the start.,Book:wisdom:10",
            "1,Library,2,5,-1,0,Shelves full of books.,Potion:health:30,,,Sword:10:strength:5,wisdom:15:The door is locked.",
            "2,Arena,-1,-1,1,-1,Sand and blood.,Gauntlets:strength:15,Goblin:30:6:5,A goblin blocks the way.",
            "5,Exit,-1,-1,-1,1,The way out.,,,,,strength:30:The gate is too heavy.,true,You win!",
            "9,Secret Room,-1,-1,0,-1,Nobody knows this room.,Stone:agility:-5,,,,,false,,true"
    );

    public static String write(List<String> rooms) throws IOException {
        Path file = Files.createTempFile("rooms", ".csv");
        file.toFile().deleteOnExit();
        Files.write(file, rooms);
        return file.toString();
    }
}
