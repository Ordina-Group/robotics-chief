import { derived } from "svelte/store";
import type { Writable } from "svelte/store";

import { register } from "$lib/socket";
import type { State } from "$lib/state";
import { ResultType, Status } from "$lib/state";

export interface CommandFailure {
  command: string;
  message: string;
}

export const trackMessage = <T>(messageType: string, commandType: string, loadingStore: Writable<boolean>) => {
  let previousMessage: T | undefined;
  let previousFailure: CommandFailure | undefined;

  return derived(
    [
      register<T | undefined>(messageType, undefined),
      register<CommandFailure | undefined>("Message.CommandFailure", undefined),
      loadingStore,
    ],
    ([message, failure, loading]): State<T, string> => {
      if (previousMessage !== message || previousFailure !== failure) {
        loadingStore.set(false);
      }

      previousMessage = message;
      previousFailure = failure;

      if (message !== undefined && !loading) {
        return { status: Status.Done, type: ResultType.Success, result: message };
      } else if (message !== undefined && loading) {
        return { status: Status.StaleWhileRevalidate, type: ResultType.Success, result: message };
      } else if (failure?.command === commandType) {
        return { status: Status.Done, type: ResultType.Failure, result: failure.message };
      }

      return { status: Status.Loading, type: ResultType.Empty, result: undefined };
    },
  );

}
