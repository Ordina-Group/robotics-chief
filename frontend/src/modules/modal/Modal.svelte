<script lang="ts">
  import { Modal as FBModal } from "flowbite-svelte";

  import BluetoothPanel from "../BluetoothPanel/BluetoothPanel.svelte";
  import ConnectWifiPanel from "../WifiPanel/ConnectWifiPanel.svelte";
  import SetSettingValue from "../Settings/SetSettingValue.svelte";
  import WifiPanel from "../WifiPanel/WifiPanel.svelte";

  export let resource: String;

  export let value: String | undefined = undefined;

  let modalOpen = true;
  let closing = false;
  export let onClose: () => undefined;

  $: {
    if (!modalOpen && !closing) {
      closing = true;
      onClose();
    }
  }
</script>

<p>Modal: {resource}</p>
<p>Value: {value}</p>

<FBModal bind:open={modalOpen}>
    {#if resource === "wifi"}
        <WifiPanel />
    {:else if resource === "connect_wifi"}
        <ConnectWifiPanel ssid={value} />
    {:else if resource === "bluetooth"}
        <BluetoothPanel />
    {:else if resource === "set_setting"}
        <SetSettingValue />
    {/if}
</FBModal>
