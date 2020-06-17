# Simple Geyser Vote Listener

6/17/2020 - v0.2 - LEOcab


I made this plugin for Geyser users who need their Bedrock players to be able
to vote. By default, voting sites don't register votes for players with an
asterisk in their name or presumably other characters.  Run the plugin once
to generate the config, then change the geyser-prefix option to whatever your
prefix is set to on Geyser. The rest should be self-explanatory.

# How to use:
1. Install it on your Spigot server
2. Edit the following things in the config:
    - Change `geyser-prefix` to the prefix your Bedrock players have
    - Change the `commands` to the commands you want to execute when a player votes
3. If you want your players to see the websites they can vote on you can enable the `/vote` command from the config, it's the `vote-command`
4. If you enabled `vote-command` make sure to change `vote-command-message` to the message you want **MAKE SURE IT'S IN MINECRAFT JSON FORMAT, YOU CAN GENERATE ONE FROM https://minecraft.tools/en/json_text.php**

# VERY IMPORTANT

Tell your Bedrock players to *NOT* include their Geyser prefix in their name when on the voting websites!!

___________________________________________________________________________________________________________________


If you find any issues, please contact me @L.C.Coicraft#1938 on the Geyser Discord, or LEOcab on the Spigot forums.
