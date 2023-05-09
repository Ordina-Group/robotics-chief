import { derived, writable } from "svelte/store";

import { register } from "./socket";

export const alertStore = writable<String | undefined>(undefined);

export const statusStore = derived(
  register("Message.StatusTable", undefined),
  (message) => message,
);

export const settingsStore = writable({
  controller: "20:21:06:16:1B:F3",
  host: "192.168.55.1",
});

register("Message.Settings").subscribe((message) => {
  if (message?.value) {
    settingsStore.set(message.value);
  }
});

export const modalStore = writable<String | undefined>(undefined);
