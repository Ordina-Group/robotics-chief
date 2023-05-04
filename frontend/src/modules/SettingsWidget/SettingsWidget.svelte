<script lang="ts">
  import { slide } from 'svelte/transition';
  import { Button, Card, Input, Label, } from 'flowbite-svelte';

  import { roboStore, settingsStore } from "$lib/stores"
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
            on:submit|preventDefault={() => sendCommand({ type: 'nl.ordina.robotics.socket.Command.UpdateHost', host: $settingsStore.host })}
        >
            <div>
                <Label for="host">Host</Label>
                <Input id="host" type="text" bind:value={$settingsStore.host}/>
            </div>
            <Button type="submit">
                Update host
            </Button>
        </form>
        <form
            action="#"
            class="flex flex-col gap-1"
            on:submit|preventDefault={() => sendCommand({ type: 'nl.ordina.robotics.socket.Command.UpdateController', mac: $settingsStore.controller })}
        >
            <div>
                <Label for="host">Controller</Label>
                <Input id="host" type="text" bind:value={$settingsStore.controller}/>
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
