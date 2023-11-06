import { Client, type IMessage } from '@stomp/stompjs';
import { writable } from "svelte/store";

export const connected = writable(false);

let socket: Client;

const connectHandlers: { [k: string]: [(message: IMessage) => void, () => void] } = {};

export const registerConnectHandler = (id: string, handler: (message: IMessage) => void) => {
    connectHandlers[id] = [handler, () => socket.unsubscribe(id)];

    if (socket?.connected) {
        socket.subscribe(id, handler);
    }
};

export const unregisterConnectHandler = (id: string) => {
    connectHandlers[id]?.[1]();
    delete connectHandlers[id];
}

export const getSocket = (): Client => socket;

export const initializeSocket = async () => {
    if (socket !== undefined && socket.active) {
        return;
    }

    if (socket !== undefined) {
        socket.forceDisconnect();
    }

    socket = new Client({
        brokerURL: 'ws://localhost:8080/connect',
        // debug: console.debug,
        reconnectDelay: 5000,
        onConnect: () => {
            Object.entries(connectHandlers).forEach(([id, [handler]]) => {
                socket.subscribe(id, (message) => {
                    handler(message);
                });
            });

            connected.set(true);
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
