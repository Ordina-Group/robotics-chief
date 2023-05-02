import { derived, writable } from 'svelte/store';

import { register } from "./socket";

export const roboStore = derived(
  register("*", { message: 'Hailing the Chief!' }),
  (message) => message,
);

export const statusStore = derived(
  register("StatusTable", undefined),
  (message) => message,
);

export const settingsStore = writable({
  controller: "20:21:06:16:1B:F3",
  host: "192.168.55.1",
});

export const modalStore = writable<String | undefined>(undefined);
