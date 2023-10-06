<script lang="ts">
  import { Button, Card, Input, Label } from "flowbite-svelte";
  import { slide } from "svelte/transition";

  import { settingsStore } from "$lib/dashboard";
  import { register, sendCommand } from "$lib/socket";
  import { onDestroy } from "svelte";

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
    <Card class="gap-1" size="s">
        <form
            action="#"
            class="flex flex-col gap-1"
            on:submit|preventDefault={() => sendCommand({ type: 'UpdateHost', host: $settingsStore.host })}
        >
            <div>
                <Label for="host">Host</Label>
                <Input bind:value={$settingsStore.host} id="host" type="text" />
            </div>
            <Button type="submit">
                Update host
            </Button>
        </form>

        <form
            action="#"
            class="flex flex-col gap-1"
            on:submit|preventDefault={() => sendCommand({ type: 'UpdateDomain', domain: $settingsStore.domainId })}
        >
            <div>
                <Label for="host">Domain id</Label>
                <Input bind:value={$settingsStore.domainId} id="domainId" type="number" />
            </div>
            <Button type="submit">
                Update domain
            </Button>
        </form>
    </Card>

    <Card class="grow" size="l">
        <span>Message:</span>
        <span class="message-container break-words">
            {#key lastMessage}
                <span class="message" transition:slide>{lastMessage}</span>
            {/key}
        </span>
    </Card>
</div>
