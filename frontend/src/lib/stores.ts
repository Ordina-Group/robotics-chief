import { writable } from 'svelte/store';

export const roboStore = writable({ message: 'Hailing the Chief!' });
export const settingsStore = writable({
  controller: "20:21:06:16:1B:F3",
  host: "192.168.55.1",
});

const socket = new WebSocket('ws://localhost:8080/subscribe');

// Connection opened
socket.addEventListener('open', function (event) {
  console.log("It's open");
});

// Listen for messages
socket.addEventListener('message', function (event) {
  const message = JSON.parse(event.data)

  console.log(message);

  if (message.type === "nl.ordina.robotics.socket.Message.Settings") {
    settingsStore.set(message.value)
  } else if (message.message || message.type === "StatusTable") {
    roboStore.set(message);
  } else {
    console.log(`Discarding ${message}`);
  }
});

export const sendCommand = (command: {}) => {
  socket.send(JSON.stringify(command));
}
