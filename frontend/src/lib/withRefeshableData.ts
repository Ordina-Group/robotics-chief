import { onMount } from "svelte";
import type { Readable } from "svelte/store";

import { trackMessage } from "$lib/trackMessage";
import { sendCommand } from "$lib/socket";
import type { State } from "$lib/state";
import { writable } from "svelte/store";

export const withRefeshableData = <T>(messageType: string, commandType: string): [data: Readable<State<T, string>>, refresh: () => void] => {
  const loading = writable(false);
  const refresh = () => {
    loading.set(true);
    sendCommand({ type: commandType });
  };
  const result = trackMessage<T>( messageType, commandType, loading);

  onMount(refresh);

  return [result, refresh];
};
