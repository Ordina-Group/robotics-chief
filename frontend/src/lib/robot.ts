import { derived, get, type Updater, type Writable, writable } from "svelte/store";
import { getSocket, registerConnectHandler, unregisterConnectHandler } from "$lib/socket";

export type Command = { [k: string]: string | boolean | number | undefined } & { type: string };

export type RobotSettings = {
    id: string,
    host: string,
    controller: string,
    domainId: number,
};

const subscribers: { [k: string]: Writable<any> } = {};

export const currentId = ((value: string | undefined): Writable<string | undefined> => {
    const store = writable<string | undefined>(value);

    const updateSubscriptions = (previous: string | undefined, current: string | undefined) => {
        if (previous !== undefined) {
            unregisterConnectHandler(`/boundary/robots/${previous}/updates`);
        }

        if (current !== undefined) {
            registerConnectHandler(`/boundary/robots/${current}/updates`, (message) => {
                handleMessage(JSON.parse(message.body));
            });
        }
    };

    const set = (value: string | undefined) => {
        const previous = get(store);
        updateSubscriptions(previous, value);

        store.set(value);
    };

    return {
        subscribe: store.subscribe,
        update: (fn: Updater<string | undefined>) => set(fn(get(store))),
        set,
    };
})(undefined);

export const settings = writable<{ robots: RobotSettings[] }>({ robots: [] });

export const currentRobot = derived(
    [currentId, settings],
    ([currentId, settings]) => {
        return settings.robots.find((robot) => robot.id === currentId) ?? undefined;
    }
);

export const sendCommand = derived(
    [currentId],
    ([currentId]) => (command: Command) => {
        if (getSocket()?.connected) {
            getSocket().publish({ destination: `/boundary/robots/${currentId}/commands`, body: JSON.stringify(command) });
        } else {
            console.error(`Discarding command ${command}, asked too soon`);
        }
    },
);

export const sendChiefCommand = derived(
    [currentId],
    ([currentId]) => (command: Command) => {
        if (getSocket()?.connected) {
            getSocket().publish({ destination: `/chief/command`, body: JSON.stringify(command) });
        } else {
            console.error(`Discarding command ${command}, asked too soon`);
        }
    },
);

registerConnectHandler("/chief/settings", (message) => {
    settings.set(JSON.parse(message.body));
});

const handleMessage = (message: { type: string }) => {
    const store = subscribers[message.type];

    if (store !== undefined) {
        store.set(message);
    } else {
        console.log(`Discarding ${message}, no store registered`);
    }

    if (subscribers["*"] !== undefined) {
        subscribers["*"].set(message);
    }

    if (store === undefined && subscribers["*"] === undefined) {
        console.log(`Discarding ${message}`);
    }
};

export const register = <T = any>(type: string, initial: any = undefined): Writable<T> => {
    if (subscribers[type] !== undefined) {
        return subscribers[type];
    }
    subscribers[type] = writable(initial);

    if (getSocket()?.connected) {
        getSocket().subscribe(type, (message) => {
            subscribers[type].set(JSON.parse(message.body));
        });
    }

    return subscribers[type];
};
