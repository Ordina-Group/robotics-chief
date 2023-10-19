<script lang="ts">
  import { Button, Spinner, TableBodyCell, TableBodyRow } from "flowbite-svelte";

  import { execute } from "$lib/actions";
  import { sendCommand } from "$lib/socket";
  import settings from "../Settings/Settings";

  export let device;

  let loading: boolean = false;

  const friendlyName = settings.get(`bluetooth.device.${device.mac}.name`);

  const refresh = () => sendCommand({ type: "Command.GetBluetoothDevices" });
  const connectWS = () => sendCommand({ type: "Command.BluetoothConnect", mac: device.mac });
  const disconnectWS = () => sendCommand({ type: "Command.BluetoothDisconnect", mac: device.mac });
  const connect = async () => {
      loading = true;
      try {
        await execute({ actionUrl: `/commands/connect/${device.mac}` });
      } finally {
        loading = false;
        refresh();
      }
  };
  const disconnect = async () => {
      loading = true;
      try {
        await execute({ actionUrl: `/commands/disconnect/${device.mac}` });
      } finally {
        loading = false;
        refresh();
      }
  };
</script>

<TableBodyRow>
    <TableBodyCell class="whitespace-break-spaces">
        {device.name}
        {#if friendlyName }
            ({friendlyName})
        {/if}
    </TableBodyCell>
    <TableBodyCell>{device.mac}</TableBodyCell>
    <TableBodyCell>
        {#if !device.connected}
            <Button on:click={connectWS}>
                {#if loading}
                    <Spinner class="mr-3" size="4" color="white" />
                    Connecting
                {:else}
                    Connect
                {/if}
            </Button>
        {:else}
            <Button color="yellow" on:click={disconnectWS}>
                {#if loading}
                    <Spinner class="mr-3" size="4" color="white" />
                    Disconnecting
                {:else}
                    Disconnect
                {/if}
            </Button>
        {/if}
    </TableBodyCell>
</TableBodyRow>
