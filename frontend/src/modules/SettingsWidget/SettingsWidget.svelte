<script lang="ts">
  import { Button, Card, Input, Label } from "flowbite-svelte";
  import { slide } from "svelte/transition";

  import { roboStore, settingsStore } from "$lib/dashboard"
  import { sendCommand } from "$lib/socket";
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
            on:submit|preventDefault={() => sendCommand({ type: 'Command.UpdateHost', host: $settingsStore.host })}
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
            on:submit|preventDefault={() => sendCommand({ type: 'Command.UpdateController', mac: $settingsStore.controller })}
        >
            <div>
                <Label for="host">Controller</Label>
                <Input bind:value={$settingsStore.controller} id="host" type="text" />
            </div>
            <Button type="submit">
                Update controller
            </Button>
        </form>
    </Card>

    <Card class="grow" size="l">
        <span>Message:</span>
        <span class="message-container break-words">
            {#key $roboStore}
                <span class="message" transition:slide>{$roboStore.message}</span>
            {/key}
        </span>
    </Card>
</div>
