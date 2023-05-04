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
        <div>
            <Label for="host">Host</Label>
            <Input id="host" type="text" bind:value={$settingsStore.host}/>
        </div>
        <Button on:click={() => sendCommand({ type: 'nl.ordina.robotics.socket.Command.UpdateHost', host: $settingsStore.host })}>
            Update host
        </Button>
        <div>
            <Label for="host">Controller</Label>
            <Input id="host" type="text" bind:value={$settingsStore.controller}/>
        </div>
        <Button on:click={() => sendCommand({ type: 'nl.ordina.robotics.socket.Command.UpdateController', mac: $settingsStore.controller })}>
            Update controller
        </Button>
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
