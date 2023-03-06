import { expect, test } from '@jest/globals';
import { InvalidCommandError } from '../../src/error/InvalidCommandError';
import { BucketType } from '../../src/model/BucketType';
import { CommandService } from '../../src/service/CommandsService';
import { TeamService } from '../../src/service/TeamService';
import { createContext } from '../helpers';

test('/re fyi', () => {
    testSuccess('fyi I\'ve added a comment in INV-6',
        'fyi', '#', "I've added a comment in INV-6");
});

test('/re #', () => {
    testSuccess('# test',
        'fyi', '#', 'test');
});

test('/re Fyi case insensitive', () => {
    testSuccess('Fyi test',
        'fyi', '#', 'test');
});

test('/re whitespaces trim', () => {
    testSuccess('fyi \t test ',
        'fyi', '#', 'test');
});

test('/re todo', () => {
    testSuccess('todo Please send me invoicei information',
        'todo', '+', 'Please send me invoicei information');
});

test('/re +', () => {
    testSuccess('+ test',
        'todo', '+', 'test');
});

test('/re ToDo case insensitive', () => {
    testSuccess('ToDo test',
        'todo', '+', 'test');
});

test('/re question', () => {
    testSuccess('question What language is the back end written in ?',
        'question', '?', 'What language is the back end written in ?');
});

test('/re ?', () => {
    testSuccess('? test',
        'question', '?', 'test');
});

test('/re Question case insensitive', () => {
    testSuccess('Question test',
        'question', '?', 'test');
});

// edge cases
test('/re fyi no message', () => {
    testFailure('fyi', 'Invalid command');
});

// test('/re fyi whitespaces no message', () => {
//     testFailure('fyi \t', 'Invalid command');
// });

test('/re custombucket', () => {
    testFailure('custombucket test', 'Unknown bucket');
});

function testSuccess(test_command_msg: string,
                     expected_bucket_name: string, expected_bucket_shortcut: string, expected_message: string) {
    const context = createContext();
    const teamService = new TeamService(context);
    const commandService = new CommandService(context, teamService);
    const command = commandService.parse_command(test_command_msg);
    expect(command).not.toBeUndefined();
    expect(command.bucketType.name).toBe(expected_bucket_name);
    expect(command.bucketType.shortcut).toBe(expected_bucket_shortcut);
    expect(command.bucketType.teamId).toBe(context.teamId);
    expect(command.teamId).toBe(context.teamId);
    expect(command.message).toBe(expected_message);
}

function testFailure(test_command_msg: string,
                     expected_error_message: string) {
    const context = createContext();
    const teamService = new TeamService(context);
    const commandService = new CommandService(context, teamService);
    expect(() => commandService.parse_command(test_command_msg)).toThrow(new InvalidCommandError(expected_error_message));
}
