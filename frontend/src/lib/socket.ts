import { writable } from "svelte/store";
import type { Writable } from "svelte/types/runtime/store";

const subscribers: { [k: string]: Writable<any> } = {};

const socket = new WebSocket("ws://localhost:8080/subscribe");

// Connection opened
socket.addEventListener("open", function (event) {
  console.log("It's open");
});

// Listen for messages
socket.addEventListener("message", function (event) {
  const message = JSON.parse(event.data);
  const store = subscribers[message.type];

  if (store !== undefined) {
    store.set(message);
  }

  if (subscribers["*"] !== undefined) {
    subscribers["*"].set(message);
  }

  if (store === undefined && subscribers["*"] === undefined) {
    console.log(`Discarding ${message}`);
  }
});

export const register = (type: string, initial: any = undefined): Writable<any> => {
  subscribers[type] ||= writable(initial);

  return subscribers[type];
};

export const sendCommand = (command: {}) => {
  socket.send(JSON.stringify(command));
};
