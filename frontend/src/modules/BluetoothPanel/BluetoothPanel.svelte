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

  import DeviceRow from "./DeviceRow.svelte";
  import { closeModal } from "../ModalManager/modals";
  import { register, sendCommand } from "$lib/robot";

  const update = register("Message.BluetoothDevices", { devices: [] });

  $: {
      console.log($update?.devices)
  }

  let error: string | undefined = undefined;
  let timeout: number;

  const refresh = () => $sendCommand({ type: "Command.GetBluetoothDevices" });

  const startScan = () => {
    $sendCommand({ type: "Command.ScanBluetooth", scan: true });
    timeout = setInterval(() => {
      refresh();
    }, 1000);
  };

  const onDone = () => {
    clearInterval(timeout);
    $sendCommand({ type: "Command.ScanBluetooth", scan: false });
    closeModal("bluetooth");
  };

  onMount(refresh);

  onDestroy(onDone);
</script>

<form
    action="#"
    class="grid gap-2"
    on:submit|preventDefault={onDone}
>
    <h1>Connect Bluetooth device</h1>
    <Table striped>
        <TableHead>
            <TableHeadCell>Name</TableHeadCell>
            <TableHeadCell>MAC</TableHeadCell>
            <TableHeadCell>Action</TableHeadCell>
        </TableHead>
        <TableBody>
            {#if $update?.devices?.length === 0}
                <TableBodyRow>
                    <TableBodyCell>No devices found</TableBodyCell>
                    <TableBodyCell>Try scanning</TableBodyCell>
                    <TableBodyCell />
                </TableBodyRow>
            {/if}

            {#each $update?.devices ?? [] as device (device.mac)}
                <DeviceRow device={device} />
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
