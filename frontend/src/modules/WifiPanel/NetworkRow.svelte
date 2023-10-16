<script lang="ts">
  import { Button, Spinner, TableBodyCell, TableBodyRow } from "flowbite-svelte";

  import { execute } from "$lib/actions";
  import { sendCommand } from "$lib/socket";
  import settings from "../Settings/Settings";
  import { openModal } from "../ModalManager/modals";

  export let network;

  let loading: boolean = false;

  const refresh = () => sendCommand({ type: "Command.GetWifiNetworks" });

  const connect = () => {
    if (network.known === true) {
      sendCommand({ type: "ConnectWifi", ssid: network.ssid });
    } else {
      openModal("connect_wifi", network.ssid)
    }
  };

  const disconnect = async () => {
      loading = true;
      try {
        await sendCommand({ type: "DisconnectWifi"  });
      } finally {
        loading = false;
        refresh();
      }
  };

  const remove = () => sendCommand({ type: "ForgetWifi", ssid: network.ssid });
</script>

<TableBodyRow>
    <TableBodyCell class="whitespace-break-spaces">
        {network.ssid}
    </TableBodyCell>
    <TableBodyCell>
        {#if !network.connected}
            <Button on:click={connect}>
                {#if loading}
                    <Spinner class="mr-3" size="4" color="white" />
                    Connecting
                {:else}
                    Connect
                {/if}
            </Button>
        {:else}
            <Button color="yellow" on:click={disconnect}>
                {#if loading}
                    <Spinner class="mr-3" size="4" color="white" />
                    Disconnecting
                {:else}
                    Disconnect
                {/if}
            </Button>
        {/if}
        {#if network.known}
            <Button color="red" on:click={remove}>
                X
            </Button>
        {/if}
    </TableBodyCell>
</TableBodyRow>
