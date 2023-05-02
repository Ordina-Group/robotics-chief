<script lang="ts">
  import { slide } from 'svelte/transition';
  import { Table, TableBody, TableBodyCell, TableBodyRow, TableHead, TableHeadCell } from 'flowbite-svelte';

  import { roboStore, sendCommand, settingsStore } from "../lib/stores"

  let host: String = $settingsStore.host

</script>

<style>
    .wrapper {
        display: grid;
        grid-template-columns: 1fr 10fr;
        grid-gap: 0.5em;
    }

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

<h1>Welcome to RoboChief</h1>

Host: <input type="text" bind:value={host}/>
<button on:click={() => sendCommand({ type: 'nl.ordina.robotics.socket.Command.UpdateHost', host })}>Update</button>

<p class="wrapper">
    Status:
    <span class="message-container">
        {#key $roboStore}
            <pre class="message" transition:slide>{$roboStore.message}</pre>
        {/key}
    </span>
</p>

{#if $roboStore.type === "StatusTable"}
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
                    <TableBodyCell>{item.message}</TableBodyCell>
                    <TableBodyCell>
                        {#if item.fixUrl && !item.success && !item.pending}
                            <button on:click={() => fetch(item.fixUrl, {method: 'POST'})}>Fix</button>
                        {/if}
                    </TableBodyCell>
                </TableBodyRow>
            {/each}
        </TableBody>
    </Table>
{/if}
