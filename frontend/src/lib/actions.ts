import { currentAlert } from "$lib/alert";
import { type Command, sendCommand } from "$lib/socket";
import { closeModal, openModal } from "../modules/ModalManager/modals";

export interface Action extends Command {
  actionUrl: string;
}

export const execute = async (command: Command): Promise<unknown> => {
  if (command.actionUrl !== undefined) {
    return executeAction(command as Action);
  }

  return sendCommand(command as Command);
}

export const executeAction = async (action: Action): Promise<unknown> => {
  const url = new URL(action.actionUrl, window.location.origin);

  if (url.pathname === "/actions/modal") {
    const resource = url.searchParams.get("resource");
    if (resource && resource !== "close") {
      openModal(resource);
    } else {
      closeModal();
    }

    return undefined;
  }

  try {
    const response = await fetch(action.actionUrl, { method: "POST" });
    const body = await response.json();

    if (body.message) {
      currentAlert.set({
        color: response.status === 200 ? 'green' : 'red',
        message: body.message,
      });
    }
  } catch (e: any) {
    currentAlert.set({
      color: "red",
      message: e.message,
    });
  }
};
