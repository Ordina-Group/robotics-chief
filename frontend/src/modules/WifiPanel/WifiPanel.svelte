<script lang="ts">
  import {
    Alert,
    Button,
    Table,
    TableBody,
    TableBodyCell,
    TableBodyRow,
    TableHead,
    TableHeadCell,
  } from "flowbite-svelte";
  import { onDestroy, onMount } from "svelte";

  import { closeModal } from "../ModalManager/modals";

  import NetworkRow from "./NetworkRow.svelte";
  import { register, sendCommand } from "$lib/robot";

  const update = register("Message.WifiNetworks", { networks: [] });

  let error: string | undefined = undefined;
  let timeout: number;

  const refresh = () => $sendCommand({ type: "Command.GetWifiNetworks" });

  const startScan = () => {
    $sendCommand({ type: "Command.GetWifiNetworks" });
    timeout = setInterval(() => {
      refresh();
    }, 1000);
  };

  const onDone = () => {
    clearInterval(timeout);
    closeModal("wifi");
  };

  onMount(refresh);

  onDestroy(onDone);
</script>

<form
    action="#"
    class="grid gap-2"
    on:submit|preventDefault={onDone}
>
    <h1>Connect Wifi network</h1>
    <Table striped>
        <TableHead>
            <TableHeadCell>Name</TableHeadCell>
            <TableHeadCell>Action</TableHeadCell>
        </TableHead>
        <TableBody>
            {#if $update.networks.length === 0}
                <TableBodyRow>
                    <TableBodyCell>No networks found</TableBodyCell>
                    <TableBodyCell>Try scanning</TableBodyCell>
                    <TableBodyCell />
                </TableBodyRow>
            {/if}

            {#each $update.networks as network (network.ssid)}
                <NetworkRow network={network} />
            {/each}
        </TableBody>
    </Table>
    {#if error !== undefined}
        <Alert color="red">{error}</Alert>
    {/if}
    <div class="flex flex-row justify-end gap-1">
        <Button on:click={startScan}>
            Scan
        </Button>
        <Button type="submit">Done</Button>
    </div>
</form>
