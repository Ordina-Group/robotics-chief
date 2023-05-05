import { writable } from "svelte/store";

export interface Alert {
  color: 'green' | 'red';
  message: string;
}

export const currentAlert = writable<Alert | undefined>(undefined);
