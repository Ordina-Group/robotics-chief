<script lang="ts">
    import { Button, Card, Input, Label } from "flowbite-svelte";
    import { slide } from "svelte/transition";

    import { settingsStore } from "$lib/dashboard";
    import { onDestroy } from "svelte";
    import { currentId, register, sendChiefCommand } from "$lib/robot";
    import { sendCommand } from "$lib/robot.js";

    let customCommand = "";

    const excluded = [
        "Message.BluetoothDevices",
        "Message.WifiInfo",
        "Message.StatusTable",
        "Message.RobotConnection",
        "Message.Settings",
    ];

    let lastMessage = "";

    const unsub = register("*", { message: "Hailing the Chief!" }).subscribe((message) => {
        if (!excluded.includes(message.type)) {
            lastMessage = message.message;
        }
    });

    onDestroy(unsub);
</script>

<style>
    .message-container {
        display: grid;
    }

    .message {
        height: 1em;
        grid-column: 1/2;
        grid-row: 1/2;
        margin: 0;
    }
</style>

<div class="flex columns-2 gap-2">
    <Card class="gap-4 flex-row" size="sm">
        <div class="flex flex-col gap-4">
            <form
                    action="#"
                    class="flex flex-col gap-1"
                    on:submit|preventDefault={() => $sendChiefCommand({ type: 'Command.UpdateHost', robotId: $currentId, host: $settingsStore.host })}
            >
                <div>
                    <Label for="host">Host</Label>
                    <Input bind:value={$settingsStore.host} id="host" type="text"/>
                </div>
                <Button type="submit">
                    Update host
                </Button>
            </form>

            <form
                    action="#"
                    class="flex flex-col gap-1"
                    on:submit|preventDefault={() => $sendChiefCommand({ type: 'Command.UpdateDomain', robotId: $currentId, domain: $settingsStore.domainId })}
            >
                <div>
                    <Label for="host">Domain id</Label>
                    <Input bind:value={$settingsStore.domainId} id="domainId" type="number"/>
                </div>
                <Button type="submit">
                    Update domain
                </Button>
            </form>
        </div>

        <div class="flex flex-col gap-4">
            <form
                    action="#"
                    class="flex flex-col gap-1"
                    on:submit|preventDefault={() => $sendCommand({ type: 'Command.CustomCommand', robotId: $currentId, command: customCommand })}
            >
                <div>
                    <Label for="customCommand">Custom command</Label>
                    <Input bind:value={customCommand} id="customCommand" type="text"/>
                </div>
                <Button type="submit">
                    Execute
                </Button>
            </form>
        </div>
    </Card>

    <Card class="grow" size="lg">
        <span>Message:</span>
        <span class="message-container break-words">
            {#key lastMessage}
                <span class="message" transition:slide>{lastMessage}</span>
            {/key}
        </span>
    </Card>
</div>
