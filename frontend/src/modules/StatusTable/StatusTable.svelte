<script lang="ts">
  import { Card, Table, TableBody, TableHead, TableHeadCell } from "flowbite-svelte";

  import { statusStore } from "$lib/dashboard";

  import StatusLine from "./StatusLine.svelte";
</script>

<Card size="2xl">
    <h2 class="text-2xl mb-1">Status</h2>
    <Table class="table-fixed">
        <TableHead>
            <TableHeadCell>Name</TableHeadCell>
            <TableHeadCell>Message</TableHeadCell>
            <TableHeadCell>Action</TableHeadCell>
        </TableHead>
        <TableBody tableBodyClass="divide-y">
            {#if $statusStore !== undefined}
                {#each $statusStore.items as item, i}
                    <StatusLine item={item} />
                {/each}
            {:else}
                <StatusLine item={{ pending: true, name: 'loading', message: '' }} />
            {/if}
        </TableBody>
    </Table>
</Card>
