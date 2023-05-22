import { derived } from "svelte/store";
import { register } from "$lib/socket";
import type { State } from "$lib/state";
import { ResultType, Status } from "$lib/state";

export interface TopicMessage {
  topics: Topic[];
}

export interface Topic {
  id: string;
  type: string;
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
