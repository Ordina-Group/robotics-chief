<script lang="ts">

  import { execute } from "$lib/actions";
  import { Button, Spinner, TableBodyCell, TableBodyRow } from "flowbite-svelte";

  import type { Command } from "$lib/robot";

  export interface StatusItem {
    name: string;
    pending: boolean;
    success: boolean;
    message: string;
    command: Command;
    commandLabel: string | undefined;
  }

  export let item: StatusItem;

  let loading = false;

  const execAction = async () => {
    loading = true;
    try {
        await $execute(item.command);
    } finally {
        loading = false;
    }
  }
</script>

<TableBodyRow>
    <TableBodyCell>
        {#if item.pending}
            🚧
        {:else if item.success}
            ✅
        {:else}
            ❌
        {/if}
        {item.name}
    </TableBodyCell>
    <TableBodyCell tdClass="px-6 py-4 break-words font-medium">{item.message}</TableBodyCell>
    <TableBodyCell>
        {#if item.command && item.commandLabel}
            <Button disabled={loading} on:click={execAction}>
                {#if loading}
                    <Spinner class="mr-3" size="4" color="white" />
                {/if}
                {item.commandLabel || 'Fix'}
            </Button>
        {/if}
    </TableBodyCell>
</TableBodyRow>
