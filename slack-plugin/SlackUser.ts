export interface SlackUser {
    readonly teamId: string;
    readonly userId: string;
    readonly is_bot: boolean;
    readonly timezone: string;
    readonly update_time: number;
}
