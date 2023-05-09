import { writable } from "svelte/store";
import type { Writable } from "svelte/types/runtime/store";

const subscribers: { [k: string]: Writable<any> } = {};

let socket: WebSocket;
let reconnect: number | undefined;

export const connected = writable(false);

const open = () => {
  if (reconnect) {
    clearInterval(reconnect);
  }
  reconnect = undefined;
  connected.set(true);
}

const close = () => {
  connected.set(false);
  if (reconnect === undefined) {
    reconnect = setInterval(() => initializeSocket(), 1000);
  }
}

const handleMessage = (event: MessageEvent<any>) => {
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
};

const initializeSocket = () => {
  if (socket !== undefined && socket.readyState === socket.CONNECTING) {
    return;
  }

  if (socket !== undefined) {
    socket.close();
    socket.removeEventListener("open", open);
    socket.removeEventListener("close", close);
    socket.removeEventListener("message", handleMessage);
  }

  socket = new WebSocket("ws://localhost:8080/subscribe");

  socket.addEventListener("open", open);
  socket.addEventListener("close", close);
  socket.addEventListener("message", handleMessage);
};

initializeSocket();

export const register = <T = any>(type: string, initial: any = undefined): Writable<T> => {
  subscribers[type] ||= writable(initial);

  return subscribers[type];
};

export const sendCommand = (command: {}) => {
  if (socket.readyState === socket.OPEN) {
    socket.send(JSON.stringify(command));
  } else {
    console.error(`Discarding command ${command}, asked too soon`);
  }
};
