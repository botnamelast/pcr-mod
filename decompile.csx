// decompile.csx - No prompt version!
string outputDir = Path.Combine(
    Directory.GetCurrentDirectory(), "output"
);
Directory.CreateDirectory(outputDir + "/code");
Directory.CreateDirectory(outputDir + "/data");

// Export semua GML code (tanpa prompt folder)
foreach (var code in Data.Code) {
    try {
        var name = code.Name.Content;
        // Sanitize nama file
        foreach (var c in Path.GetInvalidFileNameChars())
            name = name.Replace(c, '_');
            
        var decompiled = new Decompiler(Data)
            .Decompile(code, Data);
        File.WriteAllText(
            Path.Combine(outputDir, "code", name + ".gml"),
            decompiled
        );
    } catch (Exception e) {
        File.AppendAllText(
            Path.Combine(outputDir, "errors.txt"),
            $"{code.Name.Content}: {e.Message}\n"
        );
    }
}

// Export strings
var strings = Data.Strings
    .Select(s => s.Content)
    .ToList();
File.WriteAllLines(
    Path.Combine(outputDir, "data", "strings.txt"), 
    strings
);

// Export variable names
var vars = Data.Variables
    .Select(v => $"{v.Name.Content} ({v.InstanceType})")
    .ToList();
File.WriteAllLines(
    Path.Combine(outputDir, "data", "variables.txt"),
    vars
);

// Export object list + events
var objLines = new List<string>();
foreach (var obj in Data.GameObjects) {
    objLines.Add($"\n=== {obj.Name.Content} ===");
    foreach (var evList in obj.Events)
        foreach (var ev in evList)
            if (ev.CodeId != null)
                objLines.Add($"  {ev.CodeId.Name.Content}");
}
File.WriteAllLines(
    Path.Combine(outputDir, "data", "objects.txt"),
    objLines
);

// Zip semua
System.IO.Compression.ZipFile.CreateFromDirectory(
    outputDir, "hasil_decompile.zip"
);

Console.WriteLine("DONE! hasil_decompile.zip ready!");
