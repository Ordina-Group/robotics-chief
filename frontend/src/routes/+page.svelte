<script lang="ts">
  import { slide } from 'svelte/transition';
  import {
    Button,
    Card,
    Input,
    Label,
    Skeleton,
    Table,
    TableBody,
    TableBodyCell,
    TableBodyRow,
    TableHead,
    TableHeadCell
  } from 'flowbite-svelte';

  import { roboStore, sendCommand, settingsStore } from "../lib/stores"

  let host: String = $settingsStore.host

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

<h1 class="text-6xl">Welcome to RoboChief</h1>

<div class="flex columns-2 gap-2">
    <Card class="gap-1" size="s">
        <div>
            <Label for="host">Host</Label>
            <Input id="host" type="text" bind:value={host}/>
        </div>
        <Button on:click={() => sendCommand({ type: 'nl.ordina.robotics.socket.Command.UpdateHost', host })}>
            Update
        </Button>
    </Card>

    <Card class="grow" size="l">
        <span>Message:</span>
        <span class="message-container">
        {#key $roboStore}
            <pre class="message" transition:slide>{$roboStore.message}</pre>
        {/key}
    </span>
    </Card>
</div>

{#if $roboStore.type === "StatusTable"}
    <Card size="xl">
        <h2 class="text-2xl mb-1">Status</h2>
        <Table>
            <TableHead>
                <TableHeadCell>Name</TableHeadCell>
                <TableHeadCell></TableHeadCell>
                <TableHeadCell>Message</TableHeadCell>
                <TableHeadCell>Fix</TableHeadCell>
            </TableHead>
            <TableBody class="divide-y">
                {#each $roboStore.items as item, i}
                    <TableBodyRow>
                        <TableBodyCell>{item.name}</TableBodyCell>
                        <TableBodyCell>
                            {#if item.pending}
                                🚧
                            {:else if item.success}
                                ✅
                            {:else}
                                ❌
                            {/if}
                        </TableBodyCell>
                        <TableBodyCell tdClass="px-6 py-4 font-medium">{item.message}</TableBodyCell>
                        <TableBodyCell>
                            {#if item.fixUrl && !item.success && !item.pending}
                                <Button on:click={() => fetch(item.fixUrl, {method: 'POST'})}>
                                    {item.fixLabel || 'Fix'}
                                </Button>
                            {/if}
                        </TableBodyCell>
                    </TableBodyRow>
                {/each}
            </TableBody>
        </Table>
    </Card>
{:else}
    <Card size="xl">
        <Skeleton size="2xl"/>
    </Card>
{/if}
