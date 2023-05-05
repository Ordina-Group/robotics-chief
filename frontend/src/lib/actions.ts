import { currentAlert } from "$lib/alert";
import { currentModal } from "$lib/modal";

export interface Action {
  actionUrl: string;
}

export const execute = async (action: Action): Promise<unknown> => {
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

  try {
    const response = await fetch(action.actionUrl, { method: "POST" });
    const body = await response.json();
    currentAlert.set({
      color: response.status === 200 ? 'green' : 'red',
      message: body.message,
    });
  } catch (e: any) {
    currentAlert.set({
      color: "red",
      message: e.message,
    });
  }
};
