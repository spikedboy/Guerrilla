# Guerrillas

Guerrillas is a PVP gamemode I programmed long ago specifically for a server. I'm releasing it now because I've been asked for it somehow, but I'm completely ashamed of the code and I won't be releasing it. Other than that, you can do with it whatever you want. I will be occasionally updating it for new bukkit versions (ask me to if I haven't) or if there is broken features or bugs.

# How it works:

Guerrilla is meant to be played in small limited maps of around 1000m^2 and is a conquer-objective based gamemode. That means, certain Guerrilla may conquer a number of chunks of the map and then it will be declared winner by the plugin. You may configure this number of chunks needed for the victory condition. Then, an admin should reset the plugin and start a new map, or not.

A new Guerrilla may claim any chunk of unclaimed territory. And then should only be able to claim adjacent chunks (nswe) to that one. For every claim of unclaimed territory, a certain ammount of items will be taken as payment. Later, every Minecraft day, a maintenance price will be taken for every chunk of claimed territory from a chest every Guerrilla has to specify. If this maintenance price can't be taken, the Guerrilla will loose the last of their claimed chunks.

No one but the Guerrilla members may break or destroy blocks inside their claimed territory, except for lava. Lava can always be built in top of. This is on purpose so no chunk can be made inaccesible to conquer.

When two Guerrilla's territories met, they may conquer each other. You will only be able to attack other Guerrilla's territories when: There is someone from that guerrilla online. The chunk you're going to conquer has two or less adjacent (nswe) territories from the Guerrilla that owns the chunk. This is so you won't be able to claim in a straight line into their base. Then, in a configurable period of time, you may not leave the chunk you're conquering and the defenders will be warned they are being attacked. If you are killed, leave the chunk or unlog, you will be unsuccesful in conquering the territory. If the ammount of time specified has passed and you haven't died or left, the chunk will be yours. And you will pay a price for it too. Everything is paid for here, I think.

# Commands:

Straight from the ingame help:

This is the Guerrilla help :) Plugin made by DS

COMMANDS: you may also type /guerrilla instead of /g

/g create <name> - creates a Guerrilla with the name <name>

/g disband - deletes your guerrilla (only leader)

/g claim - claims the chunk you are standing on

/g join <name> - joins the guerrilla you have been invited to

/g invite <player> - invites the player to your guerrilla

/g kick <player> - kicks a player from your guerrilla

/g unclaim - unclaims the chunk you are standing on

/g unclaimall - unclaims all the chunks (leaders only)

/g list [page]- lists guerrillas

/g who [guerrilla] - gives guerrilla info

/g pchestset - sets payment chest (then open it)

/g pchestremove - removes payment chest (then open it)

/g leave - leaves the guerrilla (not leaders)

/g invitec <player> - cancel the invite for a player you've invited

/g decline - cancels a invitation you've been send

/gc - toggles intern guerrilla chat

/g help prices - see current Guerrilla prices

/g help - see this help

/g safec (leader only) - sets a safe chest only you can open or destroy (toggles set/remove)

/g changeleader [playername] - Changes the guerrilla leader

Admin commands:

These only work if you are OP in the OP's txt. Be warned. The parameters for these commands have to be correct, or you will probably screw things up because they are unfiltered.

/g adminsetsafechunk - set the current chunk as safe: no pvp and no claiming

/g adminremovesafechunk - remove safechunk

/g adminsetleader <guerrilla> <leader> - change the leader of a guerrilla forcefully

# Config:

    itemid: id of the item the chunks will be claimed with
    chunkprice: how much of itemid will be charged
    paymenttime: ingame time in ticks in which payments will be taken 0000 sunrise, 12000 noon, etc.
    iditemmaintennance: item id for the maintenance payments
    chunkmaintennanceprice: ammount taken for maintenance for every #nchunksminmaintenance chunks each minecraft day. #chunkmaintennanceprice=1 and #nchunksminmaintenance=10 will mean 1 item for every 10 chunks. (must be ints)
    gworldname: Name of the world the plugin will work in. Currently, only works in one world. Sorry.
    conquestdelay: Time in ticks it will take to conquer an enemy territory. 1 Second = 20 ticks.
    conquestpricemultiplier: price multiplier when conquering enemy territory.
    defenderdamagetakendivider: self explanatory.
    defenderdamagedealtmultiplier: self explanatory.
    nchunksminmaintenance: see above.
    ismatchwon: if true, someone has won this match and you may set it to false. if its true, the plugin will deactivate.
    winner: if someone has won, who. must be '' if no one has won.
    miniumplayersforasafechest: minimum number of players in a guerrilla needed to be able to create a safechest
    guerrillaexpirytime: time a guerrilla will enter in inactivity and be deleted. in ticks.
    explosionProtection: Explosion protection. Are things allowed to explode in claimed territory? (tnt, creepers) 

# Sample config:

http:pastebin.com/GsHKiXaY

# You should know:

There is a few bugs I'm aware of, some are inherent to minecraft and I can't fix them. The others aren't that important I guess. If there is something you will like to get fixed or added, contact me. Perhaps I will arse myself to do it.

The motivation behind this was a lot of talking with Sleepwalk (thanks :D ) and so I would learn java. Because of that, this had a lot of beginner level design flaws. Everything works nonetheless, I've rewritten some of it with better data structures over time, but a lot of bad code is still there and that's the reason I'm not publishing the source code. Also, no code comments and it is half in Spanish.

# MAP

There is no ingame map yet. We used a php map for browsers in a complex setting. I can't release that because it is not mine and it's too complex and requires a specific server setting. I will be coding a ingame map next. K? thnx.
