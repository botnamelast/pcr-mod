// decompile.csx - Assets only (YYC game)
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
    $"YYC: {Data.GeneralInfo.IsYYC}\n" +
    $"Bytecode: {Data.GeneralInfo.BytecodeVersion}\n" +
    $"Objects: {Data.GameObjects.Count}\n" +
    $"Scripts: {Data.Code.Count}\n" +
    $"Strings: {Data.Strings.Count}\n" +
    $"Sounds: {Data.Sounds.Count}\n" +
    $"Sprites: {Data.Sprites.Count}\n"
);

// 2. Semua string
File.WriteAllLines(
    Path.Combine(outputDir, "strings.txt"),
    Data.Strings.Select(s => s.Content)
);

// 3. Semua object names + sprite
File.WriteAllLines(
    Path.Combine(outputDir, "objects.txt"),
    Data.GameObjects.Select(o => 
        $"{o.Name.Content} | sprite={o.Sprite?.Name?.Content ?? "none"}"
    )
);

// 4. Semua sound names
File.WriteAllLines(
    Path.Combine(outputDir, "sounds.txt"),
    Data.Sounds.Select(s => s.Name.Content)
);

// 5. Semua sprite names
File.WriteAllLines(
    Path.Combine(outputDir, "sprites.txt"),
    Data.Sprites.Select(s => s.Name.Content)
);

// 6. Semua room names
File.WriteAllLines(
    Path.Combine(outputDir, "rooms.txt"),
    Data.Rooms.Select(r => r.Name.Content)
);

// Zip semua
ZipFile.CreateFromDirectory(outputDir, "hasil_decompile.zip");
Console.WriteLine("DONE! hasil_decompile.zip ready!");
