# Kid Ridicarus
Mashup of Super Mario Bros. 1, Metroid, and Kid Icarus.

Intent:
  Mashup the first levels of 3 of my favorite games of the 80s.
  Mario will be able to play in his world, in Metroid world and Kid Icarus world. Likewise with Samus and the Kid.

Process:
  Using Brent Aureli's code (https://github.com/BrentAureli/SuperMario, retrieved about Oct 17, 2018) as a starting point, modify as needed.

Coding Sources:
  Brent Aureli's YouTube series:
    Creating Super Mario Bros
      https://www.youtube.com/watch?v=a8MPxzkwBwo&list=PLZm85UZQLd2SXQzsF-a0-pPF6IWDDdrXt

Tools:
  TilEd
    https://www.mapeditor.org/
  TexturePacker
    https://www.codeandweb.com/texturepacker

Sprite / Tile / Map Resources:
  http://www.nesmaps.com/index.html
  https://www.spriters-resource.com/

SMB Physics Doc:
  http://i276.photobucket.com/albums/kk21/jdaster64/smb_playerphysics.png.html

Current State of Affairs:
  Fire Mario. Most mario characteristics are done, e.g. jumping, braking, falling.
  Physics work - will need to refactor so lineSegs have direction (e.g. horizontal line can be either a floor or a ceiling - so use a bit to mark which).
