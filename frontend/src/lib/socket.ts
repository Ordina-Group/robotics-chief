import { Client } from '@stomp/stompjs';

import { writable } from "svelte/store";
import type { Writable } from "svelte/types/runtime/store";

const subscribers: { [k: string]: Writable<any> } = {};

export type Command = { [k: string]: string | boolean | number } & { type: string };

export const connected = writable(false);

const handleMessage = (message: { type: string }) => {
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

let socket: Client;

const initializeSocket = async () => {
    if (socket !== undefined && socket.active) {
        return;
    }

    if (socket !== undefined) {
        socket.forceDisconnect();
    }

    socket = new Client({
        brokerURL: 'ws://localhost:8080/connect',
        debug: console.debug,
        reconnectDelay: 5000,
        onConnect: () => {
            connected.set(true);

            socket.subscribe("/robots/3/updates", (message) => {
                console.log("STATUS UPDATE", message);
                handleMessage(JSON.parse(message.body));
            });

            socket.publish({ destination: '/hello', body: 'First Message' });
        },
        onDisconnect: () => {
            connected.set(false);
        },
        onWebSocketClose: () => {
            connected.set(false);
        }
    });

    socket.activate();
};

initializeSocket().catch(console.error);

// let socket: WebSocket;
//
// socket = new WebSocket("ws://localhost:8080/connect", ["v12.stomp", "v11.stomp", "v10.stomp"]);
// socket.addEventListener("message", (event: any) => {
//    console.log("STATUS UPDATE", event);
// });
//
// socket.onopen = () => {
//     connected.set(true);
//     console.log("STATUS UPDATE", "Connected");
//
//     setInterval(() => {
//         socket.send("foobar client " + Math.floor(Math.random() * 1000))
//     }, 2000);
// }

export const register = <T = any>(type: string, initial: any = undefined): Writable<T> => {
    subscribers[type] ||= writable(initial);

    return subscribers[type];
};

export const sendCommand = (command: Command) => {
    // if (socket.connected) {
    //     socket.publish({ destination: '/command', body: JSON.stringify(command) });
    // } else {
        console.error(`Discarding command ${command}, asked too soon`);
    // }
};
