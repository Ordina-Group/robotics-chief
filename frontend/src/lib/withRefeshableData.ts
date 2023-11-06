import { onMount } from "svelte";
import type { Readable } from "svelte/store";
import { derived, writable } from "svelte/store";

import { trackMessage } from "$lib/trackMessage";
import type { State } from "$lib/state";
import { sendCommand } from "$lib/robot";

export const withRefeshableData = derived(
    [sendCommand],
    ([sendCommand]) => <T>(messageType: string, commandType: string): [data: Readable<State<T, string>>, refresh: () => void] => {
        const loading = writable(false);
        const refresh = () => {
            loading.set(true);
            sendCommand({ type: commandType });
        };
        const result = trackMessage<T>(messageType, commandType, loading);

        onMount(refresh);

        return [result, refresh];
    },
);
