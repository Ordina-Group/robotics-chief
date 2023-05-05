import { currentModal } from "$lib/modal";

export interface Action {
  actionUrl: string;
}

export const execute = (action: Action): Promise<unknown> | undefined => {
  const url = new URL(action.actionUrl, window.location.origin);

  if (url.pathname === "/actions/modal") {
    const resource = url.searchParams.get("resource");
    if (resource && resource !== "close") {
      currentModal.set(resource);
    } else {
      currentModal.set(undefined);
    }

    return undefined;
  }

  return fetch(action.actionUrl, { method: "POST" });
};
