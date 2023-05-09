<script lang="ts">
  import { Modal as FBModal } from "flowbite-svelte";

  import BluetoothPanel from "../BluetoothPanel/BluetoothPanel.svelte";
  import WifiPanel from "./WifiPanel.svelte";
  import SetSettingValue from "../Settings/SetSettingValue.svelte";

  export let resource: String | undefined = undefined;
  export let onClose: () => undefined;
  $: modalOpen = resource !== undefined;

  $: {
    if (modalOpen === false && resource !== undefined) {
      onClose();
    }
  }
</script>

<FBModal bind:open={modalOpen}>
    {#if resource === "wifi"}
        <WifiPanel />
    {:else if resource === "bluetooth"}
        <BluetoothPanel />
    {:else if resource === "set_setting"}
        <SetSettingValue />
    {/if}
</FBModal>
