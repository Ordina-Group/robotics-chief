import { writable } from 'svelte/store';

export const roboStore = writable('initializing');

const socket = new WebSocket('ws://localhost:8080/subscribe');

// Connection opened
socket.addEventListener('open', function (event) {
  console.log("It's open");
});

// Listen for messages
socket.addEventListener('message', function (event) {
  roboStore.set(event.data);
});
