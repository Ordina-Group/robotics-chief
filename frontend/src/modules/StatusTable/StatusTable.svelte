<script lang="ts">
  import {
    Button,
    Card,
    Skeleton,
    Table,
    TableBody,
    TableBodyCell,
    TableBodyRow,
    TableHead,
    TableHeadCell,
  } from "flowbite-svelte";

  import { statusStore } from "$lib/dashboard";
  import { execute } from "$lib/actions";
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
                                <Button on:click={() => execute(item)}>
                                    {item.actionLabel || 'Fix'}
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
        <Skeleton size="2xl" />
    </Card>
{/if}
