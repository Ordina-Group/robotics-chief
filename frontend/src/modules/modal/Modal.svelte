<script lang="ts">
  import { Modal as FBModal } from "flowbite-svelte";

  import WifiPanel from "./WifiPanel.svelte";
  import BluetoothPanel from "./BluetoothPanel.svelte";

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
        <WifiPanel/>
    {:else if resource === "bluetooth"}
        <BluetoothPanel/>
    {/if}
</FBModal>
