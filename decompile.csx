#r "System.IO.Compression.dll"
#r "System.IO.Compression.FileSystem.dll"

using System.IO;
using System.IO.Compression;
using System.Linq;

string outputDir = Path.Combine(
    Directory.GetCurrentDirectory(), "output"
);
Directory.CreateDirectory(outputDir);

// 1. Info game
File.WriteAllText(Path.Combine(outputDir, "info.txt"),
    $"Game: {Data.GeneralInfo.DisplayName.Content}\n" +
    $"GMS2: {Data.IsGameMaker2}\n" +
    $"Bytecode: {Data.GeneralInfo.BytecodeVersion}\n" +
    $"Objects: {Data.GameObjects.Count}\n" +
    $"Strings: {Data.Strings.Count}\n" +
    $"Sounds: {Data.Sounds.Count}\n" +
    $"Sprites: {Data.Sprites.Count}\n"
);
Console.WriteLine("Info written!");

// 2. Semua strings
File.WriteAllLines(
    Path.Combine(outputDir, "strings.txt"),
    Data.Strings.Select(s => s.Content)
);
Console.WriteLine("Strings written!");

// 3. Object names + sprite
File.WriteAllLines(
    Path.Combine(outputDir, "objects.txt"),
    Data.GameObjects.Select(o =>
        $"{o.Name.Content} | sprite={o.Sprite?.Name?.Content ?? "none"}"
    )
);
Console.WriteLine("Objects written!");

// 4. Sound names
File.WriteAllLines(
    Path.Combine(outputDir, "sounds.txt"),
    Data.Sounds.Select(s => s.Name.Content)
);
Console.WriteLine("Sounds written!");

// 5. Sprite names
File.WriteAllLines(
    Path.Combine(outputDir, "sprites.txt"),
    Data.Sprites.Select(s => s.Name.Content)
);
Console.WriteLine("Sprites written!");

// 6. Room names
File.WriteAllLines(
    Path.Combine(outputDir, "rooms.txt"),
    Data.Rooms.Select(r => r.Name.Content)
);
Console.WriteLine("Rooms written!");

// Zip
string zipPath = Path.Combine(
    Directory.GetCurrentDirectory(), "hasil_decompile.zip"
);
ZipFile.CreateFromDirectory(outputDir, zipPath);
Console.WriteLine($"DONE! ZIP created at: {zipPath}");
