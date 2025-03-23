package me.fami6xx.blockywhitelist.utils.languages;

public class Lang {
    public static String prefix = "&b&lBW &8» &7";

    public static String previousPageItemDisplayName = "&bPrevious page";
    public static String previousPageItemLore = "&7Click to go to the previous page.";
    public static String nextPageItemDisplayName = "&bNext page";
    public static String nextPageItemLore = "&7Click to go to the next page.";
    public static String closeItemDisplayName = "&cClose";
    public static String closeItemLore = "&7Click to close this menu.";

    public static String errorMenuAlreadyOnFirstPage = "&cYou are already on the first page.";
    public static String errorMenuAlreadyOnLastPage = "&cYou are already on the last page.";
    public static String errorYouDontHavePermissionToUseThisCommandMessage = "&cYou don't have permission to use this command.";
    public static String errorOnlyPlayersCanUseThisCommandMessage = "&cOnly players can use this command.";

    public static String errorNotDiscordMember = "Only Discord server members can use this command.";
    public static String errorNotGuild = "This command can only be used on a Discord server.";
    public static String errorNoPermission = "You do not have permission to use this command!";
    public static String errorNoUserProvided = "No user was provided.";
    public static String errorNoRoleSet = "No role has been set for whitelisting.";
    public static String errorPlayerAlreadyWhitelisted = "User {player} is already whitelisted!";
    public static String successPlayerWhitelisted = "User {player} has been successfully whitelisted!";

    public static String errorNoCodeProvided = "No code was provided.";
    public static String errorAccountAlreadyLinked = "Your account is already linked!";
    public static String errorInvalidCode = "Invalid code.";
    public static String successAccountLinked = "Your account has been successfully linked!";

    public static String errorNoAttemptProvided = "No attempt number was provided.";
    public static String errorInvalidAttempt = "Invalid attempt number.";
    public static String errorNoRoleForAttempt = "No role has been set for this attempt.";
    public static String errorRoleNotFound = "Role for this attempt was not found.";
    public static String errorPlayerAlreadyHasRole = "User {player} already has this role!";
    public static String successRoleGivenForAttempt = "User {player} has been given the role for attempt {attempt}!";

    public static String unknown = "Unknown";

    public static String errorNoAccountLinked = "There is no account linked with this discord account.";
    public static String successUsernameRetrieved = "Username for {player} is {username}.";

    public static String linkDiscordCommandDescription = "Link your Minecraft account with your Discord account.";
    public static String linkDiscordCommandCodeProvided = "Your code shown when you try to connect to server.";

    public static String whitelistDiscordCommandDescription = "Whitelist a player.";
    public static String whitelistDiscordCommandUserProvided = "The user you want to whitelist.";

    public static String failedDiscordCommandDescription = "Set the player's whitelist attempt as failed.";
    public static String failedDiscordCommandAttemptProvided = "Indicates which whitelist attempt number it is. (1 to 3)";
    public static String failedDiscordCommandUserProvided = "The player the attempt pertains to.";

    public static String usernameDiscordCommandDescription = "Retrieves the player's Minecraft username.";
    public static String usernameDiscordCommandUserProvided = "The user for whom you want to retrieve the username.";

    public static String kickNotWhitelisted = "&b&lBlockyWhitelist\n\n&r&cYou do not have the whitelisted role.\n\n&r&7Please join our Discord and register for the whitelist.";
    public static String kickNotSetup = "&b&lBlockyWhitelist\n\n&r&cThe server is not set up.\n\n&r&7Please contact an admin.";
    public static String kickLoadingMembers = "&b&lBlockyWhitelist\n\n&r&cThe server is loading.\n\n&r&7Please try again in a moment.";
    public static String kickNotLinkedError = "&b&lBlockyWhitelist\n\n&r&cYou must link your account.\n\n&r&7We were unable to retrieve your code, please try again.";
    public static String kickNotLinkedCode = "&b&lBlockyWhitelist\n\n&r&cYou must link your account.\n\n&r&7Your code: &a{code}\n&r&7Please type &a/link {code} &7in Discord.";
    public static String kickNotLinkedContactAdmin = "&b&lBlockyWhitelist\n\n&r&cYou must link your account.\n\n&r&7Please contact an admin.";

}
