import { writable } from "svelte/store";

export const currentModal = writable<String | undefined>(undefined);
