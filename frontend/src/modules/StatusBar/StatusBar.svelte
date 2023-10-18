<script lang="ts">
  import ConnectedBadge from "./ConnectedBadge.svelte";
  import WifiBadge from "./WifiBadge.svelte";
  import BluetoothBadge from "./BluetoothBadge.svelte";
  import RobotBadge from "./RobotBadge.svelte";
  import { DarkMode } from "flowbite-svelte";
</script>

<div class="flex flex-row w-full gap-2">
  <ConnectedBadge/>
  <RobotBadge/>
  <WifiBadge/>
  <BluetoothBadge/>
  <DarkMode class="inline-block dark:hover:text-white hover:text-gray-900"/>
</div>
