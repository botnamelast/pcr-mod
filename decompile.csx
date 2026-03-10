using System.IO.Compression;

// Buat folder output
Directory.CreateDirectory("output/scripts");
Directory.CreateDirectory("output/data");

Console.WriteLine("Exporting scripts...");

// 1. Semua GML Scripts (decompiled)
int scriptCount = 0;
foreach (var code in Data.Code) {
    try {
        var name = code.Name.Content;
        var decompiled = new Decompiler(Data).Decompile(code);
        File.WriteAllText($"output/scripts/{name}.gml", decompiled);
        scriptCount++;
    } catch (Exception e) {
        File.WriteAllText($"output/scripts/{code.Name.Content}_ERROR.txt", e.Message);
    }
}
Console.WriteLine($"Scripts: {scriptCount} exported");

// 2. Variables
Console.WriteLine("Exporting variables...");
var vars = Data.Variables.Select(v => 
    $"{v.Name.Content} | InstanceType: {v.InstanceType}");
File.WriteAllLines("output/data/variables.txt", vars);

// 3. Objects + Events
Console.WriteLine("Exporting objects...");
var objList = new List<string>();
foreach (var obj in Data.GameObjects) {
    objList.Add($"\n=== {obj.Name.Content} ===");
    foreach (var ev in obj.Events) {
        foreach (var action in ev) {
            if (action.CodeId != null)
                objList.Add($"  Event: {action.CodeId.Name.Content}");
        }
    }
}
File.WriteAllLines("output/data/objects.txt", objList);

// 4. All Strings
Console.WriteLine("Exporting strings...");
var strings = Data.Strings.Select(s => s.Content);
File.WriteAllLines("output/data/strings.txt", strings);

// 5. Global Variables
Console.WriteLine("Exporting global init...");
var globals = new List<string>();
foreach (var code in Data.Code) {
    if (code.Name.Content.Contains("global") || 
        code.Name.Content.Contains("Global")) {
        try {
            var decompiled = new Decompiler(Data).Decompile(code);
            globals.Add($"=== {code.Name.Content} ===\n{decompiled}\n");
        } catch {}
    }
}
File.WriteAllLines("output/data/globals.txt", globals);

// 6. Room list
Console.WriteLine("Exporting rooms...");
var rooms = Data.Rooms.Select(r => r.Name.Content);
File.WriteAllLines("output/data/rooms.txt", rooms);

// 7. Sound list
Console.WriteLine("Exporting sounds...");
var sounds = Data.Sounds.Select(s => s.Name.Content);
File.WriteAllLines("output/data/sounds.txt", sounds);

Console.WriteLine("Zipping output...");

// Zip semua output
ZipFile.CreateFromDirectory("output", "hasil_decompile.zip");

Console.WriteLine("Done! hasil_decompile.zip ready!");