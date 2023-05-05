<script lang="ts">
  import { Card, Skeleton, Table, TableBody, TableHead, TableHeadCell } from "flowbite-svelte";

  import { statusStore } from "$lib/dashboard";
  import StatusLine from "./StatusLine.svelte";
</script>

{#if $statusStore !== undefined}
    <Card size="xl">
        <h2 class="text-2xl mb-1">Status</h2>
        <Table>
            <TableHead>
                <TableHeadCell>Name</TableHeadCell>
                <TableHeadCell></TableHeadCell>
                <TableHeadCell>Message</TableHeadCell>
                <TableHeadCell>Action</TableHeadCell>
            </TableHead>
            <TableBody class="divide-y">
                {#each $statusStore.items as item, i}
                    <StatusLine item={item} />
                {/each}
            </TableBody>
        </Table>
    </Card>
{:else}
    <Card size="xl">
        <Skeleton size="2xl" />
    </Card>
{/if}
