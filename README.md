# Kid Ridicarus

## Mashup of Super Mario Bros., Metroid, and Kid Icarus

### Intent:

Mashup the first levels of 3 of my favorite games of the 80s.

Mario will be able to play in his world, in Metroid world and Kid Icarus world. Likewise with Samus and the Kid.

### Current State of Affairs:

* Super Mario Bros. 1 almost done, still needs:

  * End of level part of game needs time to points conversion countdown

  * Some minor fixes (e.g. sometimes mario stops using jump force too early, enemies tend to "stick" together when there are many close together)

* Metroid level 1 has begun, Zoomer and Skree implemented

### Keyboard Controls

* I use a custom setup:

  * RIGHT = 'F' key

  * UP = 'E' key

  * LEFT = 'S' key

  * DOWN = 'D' key

  * RUN = 'Left' key

  * JUMP = 'Down' key

### Process:

* Coding

  * Write code in Eclipse IDE

  * Use libGDX for graphics, sound, input

  * Using [Brent Aureli's SMB code](www.github.com/BrentAureli/SuperMario) (retrieved about Oct 17, 2018) as a starting point, modify as needed

  * When stumped, Google stuff and usually find things on [Stack Overflow](www.stackoverflow.com) or [BadLogicGames](www.badlogicgames.com).

* Graphics

  * Find sprites and tilesets at Spriters Resource, NESMAPS.COM

  * For Sprites (e.g. the mario sprite)

    * Process sprite images with GIMP (if necessary)

    * Pack sprite images with TexturePacker
 
  * For Tilesets (e.g. the background scenery in level 1-1)

    * Process tile images with GIMP (if necessary)

    * The images will be used in a tileset via TilEd so use the GIMP Gutter Add tool to add gutter to the tile images (if you don't add gutter to your tile images then you may see flickering black lines when running a map)

    * TilEd for creating maps

	* A very special tool for a very special use: Img2tmx = Convert maps in .png form to TilEd maps, complete with tileset .png output

	  * It is fun to draw game maps by hand, and also fun to trace maps in TilEd by adding image backgrounds and tracing what you see...

	  * However, if you want to double check your map and make sure you copied everything perfectly... then use this tool!

	  * The code is brilliant

* Audio

  * Find sounds at The Sounds Resource, and music at the Video Game Music Preservation Foundation

  * Use Audacity for modifying/converting audio

* Game

  * FCEUX with the emulation speed turned down to 50%
  
  * Save AVI videos from FCEUX, watch videos with ShotCut to see the action frame by frame

### Coding Resources:

[LibGDX](libgdx.badlogicgames.com)

Brent Aureli's YouTube series [Creating Super Mario Bros](www.youtube.com/watch?v=a8MPxzkwBwo&list=PLZm85UZQLd2SXQzsF-a0-pPF6IWDDdrXt)

[Img2tmx](www.github.com/GregSam/Img2Tmx)

  * Note: I modified the code cited above slightly for my purposes, I commented out lines 93 - 95:
  
    * readLayerFile("layer2.txt", tmxL2Tiles);

    * readLayerFile("layer3.txt", tmxL3Tiles);

    * readLayerFile("layer4.txt", tmxL4Tiles);

  * I seem to be missing these 3 files :(
  
  * Also, I had to modify the output files a bit

### Tools:

[Eclipse IDE](www.eclipse.org)

[FCEUX](www.fceux.com)

[TilEd](www.mapeditor.org)

[TexturePacker](www.codeandweb.com/texturepacker)

[libGDX Hiero tool for bitmap font conversion](www.github.com/libgdx/libgdx/wiki/Hiero)

[GIMP](www.gimp.org)

[GIMP Addon - Sprite Gutter Add/Remove tool](www.gimper.net/threads/add-remove-sprite-sheet-gutter-padding-and-spacing.14189)

  * Great for preparing tilesets for use (read up on TilED and libGDX tile maps to know why - hint: padding).

[ShotCut](www.shotcut.org)

[Audacity](www.audacityteam.org)

### Sprite / Tile / Map Resources:

[NESMAPS.COM](www.nesmaps.com)

[The Spriters Resource](www.spriters-resource.com)

### Audio Resources:

[The Sounds Resource](www.sounds-resource.com)

[Video Game Music Preservation Foundation](www.vgmpf.com)

### Other:

[SMB Font: Press Start by codeman38 at FontSpace](www.fontspace.com/codeman38/press-start)

[SMB Physics Doc](i276.photobucket.com/albums/kk21/jdaster64/smb_playerphysics.png.html)

[MarioWiki](www.mariowiki.com)

[Strategy Wiki](www.strategywiki.org)

### Credits:

I'd like to thank my arms for staying by my side. I'd like to thank my legs for all their support. And I'd like to thank my hands especially - I could always count on my hands.

If there are any more unthanked or unacknowledged contributors then I thank and acknowledge you now: Thanks!

This repo is maintained by David Loucks.
