# Jetpacks
Tested Versions:  
1.20.6 - 1.21.4  
Java 21  
  
This plugin allows you to add many different jetpacks to your game! Nearly everything is customizable (besides the shape of the particle effect). You can define the vertical thrust, horizontal velocity, acceptable fuel, etc. All kinds of things!  
  
# How to create a jetpack type
To get started with creating jetpacks, you basically create new ".yml" files in the items folder within this plugin's data-folder. Please don't put any spaces in this file name, and keep in mind these names are case sensitive. You can rename this file later, but items created from this file previously will no longer function as jetpacks.  
  
We will proceed by looking at the example file I provide "test.yml"  
```yaml
name: "test"
material: "minecraft:phantom_membrane"
lore:
  - "Click to toggle on/off"
attributes:
  - "generic.armor,10,hand"
jetpack-item: true
activation-type: "right_click"
slots:
  - "hand"
max-fuel: 500
consumption: 1
thrust: 1.25
horizontal-velocity: 0.2
max-y-velocity: 2
uses-fuel: "minecraft:coal"
effect:
  weighted: true
  particles:
    - "FLAME,0,2"
    - "SMOKE_NORMAL,0,1"
  has-sound: true
  sound: "minecraft:block.blastfurnace.fire_crackle"
  vol: 1
  pitch: 1
```  
The first field ```name``` refers to the name that the jetpack item will have when given to a player. You can use hexcoded preceeded by a '#' and regular color codes.  
  
The ```material``` field refers to the material of the jetpack item. You may just put in things like "iron_ingot" for vanilla items instead of the whole key.  
  
The ```lore``` field is the item's flavortext. Much like the name, all lines of lore may contain hex colors and color codes.  
  
The ```attributes``` field defines what item attributes you want your jetpack to have. All entries in this list must consist of the Attribute's key, the modifier amount, and the equipment slot the modifier should apply in separated by commas, as shown in the example above.  
  
The ```jetpack-item``` field is how the plugin knows this file is for a jetpack  item. Just make sure to set it to true.  
  
The field, ```activation-type``` defines how you want this jetpack to be toggled. You may choose from "LEFT_CLICK", "RIGHT_CLICK", "SHIFT_LEFT_CLICk", "SHIFT_RIGHT_CLICK", "OFFHAND", "SHIFT_OFFHAND".  
Note that the options for OFFHAND mean the jetpack will be toggled when the offhand swap hotkey is pressed.  
  
The field, ```slots```, determines what equipment slots the jetpack may be in in order to get toggled by the player.  
You can enter items such as "HAND", "OFFHAND", "FEET", "CHEST", "LEGS", "HEAD".  
  
The ```max-fuel``` field determines that maximum amount of fuel this jetpack can hold. Must be a whole number.  
  
The ```consumption``` field determines fuel consumption per tick. This field must also be a whole number.  

The ```thrust``` field determines your vertical acceleration. Your vertical acceleration with be 0.8 multiplied by whatever number you input here. Note this number does not need to be a whole number.  
  
The ```horizontal-velocity``` field determines the velocity at which the player will move horizontally when using the jetpack. Doesn't need to be a whole number.  
  
The ```max-y-velocity``` field determines the maximum speed vertically a jetpack user will be able to fly.  
  
The ```uses-fuel``` field defines what kind of fuel this jetpack needs. May be a minecraft material, or a fuel id which we will go over later.  
  
The following fields are for the sound and particle effect of the jetpack.  
  
The ```weighted``` field should be a true/false value determining whether you want the particles listed in the particle section to all be displayed if you want them to be displayed with a weighted distribution.  
  
The ```particles``` field contains entries that must consist of the name of the Particle you want to display, the "amount" of this particle (should generally stick with 0), and finally the weight of this particle to be used if ```weighted``` was set to true.  
  
The ```has-sound``` field determines whether a sound will play while the jetpack is active.  
  
The ```sound``` field determines the sound to play if ```has-sound``` was true. Try to follow the format I used in my example.  
  
The ```volume``` field determines the volume of this sound  
  
The ```pitch``` field determines the pitch of the sound.
  
If you don't include the ```jetpack-item``` field in your file, the file is then treated as a fuel item, whose name (minus the .yml) may be placed in the fuel-file.yml file that looks like so:  
```yaml
generic:
  - "minecraft:coal,25"
specific:
  test-fuel: 25
```
In the generic section, you add entries where the first part is a material in minecraft, and the second is how many fuel points this item gives.  
  
In the specific section, you put the name of a ".yml" file in the items folder without the .yml part that defines a non-jetpack item. The number after this name represents the amount of fuel this item gives.  

# Commands
/jetpackgive <jetpack-id> <player> <amount>  
Permission: "jetpacks.commands.givejetpack"  
The jetpack-id should come from the file names within the items folder. Please follow the tab completer for best results!  
This command gives the chosen player the jetpack corresponding to provided jetpack id.  
  
/jetpackreload  
Permission: "jetpacks.commands.reload"
