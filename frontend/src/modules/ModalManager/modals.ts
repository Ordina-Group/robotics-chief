import { writable } from "svelte/store";

export const values = writable<Array<string | undefined>>([]);

export const modals = writable<string[]>([]);

export const openModal = (name: string, value: string | undefined = undefined) => {
  values.update((v) => [...v, value]);
  modals.update((v) => [...v, name]);
};

export const closeModal = (name: string | undefined = undefined) => {
  let index: number | undefined = undefined;

  modals.update((it) => {
    if (name !== undefined) {
      index = it.indexOf(name);
      return it.filter((v) => v != name);
    } else {
      return it.slice(1);
    }
  });

  if (index !== undefined) {
    values.update((v) => v.filter((_, i) => i !== index));
  } else {
    values.update((v) => v.slice(1));
  }
};
