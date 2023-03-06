import { SlackConversation } from "../model/SlackConversation";
import { SlackClient } from "../external/slack/client";
import { Context } from "../model/Context";
import { SlackUser } from "../model/SlackUser";
import { DynamoDb } from "../external/aws/dynamodb";
import { UserSettings, UserSettingsNotificationDaysDefault, UserSettingsNotificationTimesDefault } from "../model/User";
import { KeysValues, QueryResponse } from "../external/aws/dynamodb-types";
import { UserScheduleMapper } from "../mapper/UserScheduleMapper";
import moment from "moment-timezone";
import { UtilsService } from "./UtilsService";

export class UserService {
    constructor(readonly context: Context,
                readonly userScheduleMapper = new UserScheduleMapper(context),
                readonly dynamodb: DynamoDb = new DynamoDb(context),
                readonly slackClient: SlackClient = new SlackClient(context)) {
    }

    async getBatchSlackUsers(userIds: string[]): Promise<SlackUser[]> {
        return await Promise.all(userIds.map(userId => this.getSlackUser(userId)));
    }

    async getSlackUser(userId: string): Promise<SlackUser> {
        const slackUser = await this.slackClient.get_user(userId);
        this.slackClient.validate_response(slackUser);

        // TODO remote JSON.stringify
        this.context.logger.debug(`User ${JSON.stringify(slackUser)}`);

        return <SlackUser> {
            teamId: this.context.teamId,
            userId: userId,
            is_bot: slackUser.user.is_bot,
            timezone: slackUser.user.tz,
            update_time: new Date().getTime()
        };
    }

    async createNewUserSettings(existingUserSettings: UserSettings[], userIds: string[]): Promise<UserSettings[]> {
        const existingUserIdToSettings = existingUserSettings.reduce((set, userSettings) => {
            set.add(userSettings.userId);
            return set;
        }, new Set<string>());

        const newUserIds = userIds.filter(userId => !existingUserIdToSettings.has(userId));

        const userIdToSlackUser = UtilsService.toMap((await this.getBatchSlackUsers(newUserIds)), slackUser => slackUser.userId);

        return newUserIds.map(userId => <UserSettings>{
            teamId: this.context.teamId,
            userId: userId,
            createdTime: moment.tz('utc').unix(),
            timeZone: userIdToSlackUser.get(userId).timezone,
            notificationDays: UserSettingsNotificationDaysDefault,
            notificationTimes: UserSettingsNotificationTimesDefault
        });
    }

    async getOrCreateUserSettings(userId: string): Promise<UserSettings> {
        let userSettings = await this.dynamodb.get<UserSettings>({
            table_name: 'user-settings',
            key: {
                teamId: this.context.teamId,
                userId: userId
            }
        });

        if (!userSettings) {

            const slackUser = await this.getSlackUser(userId);

            userSettings = <UserSettings>{
                teamId: this.context.teamId,
                userId: userId,
                createdTime: moment.tz('utc').unix(),
                timeZone: slackUser.timezone,
                notificationDays: UserSettingsNotificationDaysDefault,
                notificationTimes: UserSettingsNotificationTimesDefault
            };

            await this.dynamodb.create('user-settings', userSettings);
        }

        return userSettings;
    }


    async changeUserTimezone(userId: string, timeZone: string) {
        const userSettings = await this.getOrCreateUserSettings(userId);

        if (userSettings.timeZone !== timeZone) {
            await this.dynamodb.update({
                table_name: 'user-settings',
                key: {
                    teamId: this.context.teamId,
                    userId: userId
                },
                set_fields: {
                    'timeZone': timeZone
                }
            });
        }
    }
}
