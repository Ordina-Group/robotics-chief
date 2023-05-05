<script lang="ts">

  import { execute } from "$lib/actions";
  import { Button, Spinner, TableBodyCell, TableBodyRow } from "flowbite-svelte";

  export interface StatusItem {
    name: string;
    pending: boolean;
    success: boolean;
    message: string;
    actionUrl: string;
    actionLabel: string | undefined;
  }

  export let item: StatusItem;

  let loading = false;

  const execAction = async () => {
    loading = true;
    try {
        await execute(item);
    } finally {
        loading = false;
    }
  }
</script>

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
        {#if item.actionUrl && item.actionLabel}
            <Button disabled={loading} on:click={execAction}>
                {#if loading}
                    <Spinner class="mr-3" size="4" color="white" />
                {/if}
                {item.actionLabel || 'Fix'}
            </Button>
        {/if}
    </TableBodyCell>
</TableBodyRow>
