import { derived } from "svelte/store";
import type { State } from "$lib/state";
import { ResultType, Status } from "$lib/state";
import { register } from "$lib/robot";

export interface TopicMessage {
  topics: Topic[];
}

export interface Topic {
  id: string;
  count: number;
}

export interface CommandFailure {
  command: string;
  message: string;
}

export const topics = derived(
  [
    register<TopicMessage | undefined>("Message.Topics", undefined),
    register<CommandFailure | undefined>("Message.CommandFailure", undefined),
  ],
  ([topics, failure]): State<TopicMessage, string> => {
    if (topics  !== undefined) {
      return { status: Status.Done, type: ResultType.Success, result: topics };
    } else if (failure?.command === "Command.ListTopics") {
      return { status: Status.Done, type: ResultType.Failure, result: failure.message };
    }

    return { status: Status.Loading, type: ResultType.Empty, result: undefined };
  },
);
