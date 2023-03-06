import { BucketType } from "../model/BucketType";
import { InvalidCommandError } from "../error/InvalidCommandError";
import { SlackCommand } from "../model/SlackCommand";
import { Context } from "../model/Context";
import { TeamService } from "./TeamService";



export class CommandService {
    static readonly COMMAND_REGEX = /(?<bucketType>[^\s]+)\s+(?<message>.+)/s;

    constructor(readonly context: Context,
                readonly teamService: TeamService) {}

    async parseCommand(commandText: string): Promise<SlackCommand> {
        const commandMatch = commandText.match(CommandService.COMMAND_REGEX);
        if (commandMatch == null) {
            throw new InvalidCommandError(`Invalid command`);
        }

        const bucketType = commandMatch.groups.bucketType.toLowerCase();

        let bucket = await this.teamService.getBucketByName(bucketType);
        let shortcutUsed = false;
        if (bucket === undefined) {
            shortcutUsed = true;
            bucket = await this.teamService.getBucketByShortcut(bucketType);
        }

        if (bucket === undefined) {
            throw new InvalidCommandError(`Unknown bucket`);
        }

        const commandMessage = commandMatch.groups.message;

        return new SlackCommand(this.context.teamId, bucket, commandMessage, shortcutUsed);
    }
}
