<script lang="ts">
  import { Badge } from "flowbite-svelte";
  import { derived } from "svelte/store";
  import { withRefeshableData } from "$lib/withRefeshableData";
  import { ResultType, Status } from "$lib/state";

  interface Device {
    mac: string;
    connected: boolean;
  }

  interface BluetoothDevices {
    devices: Device[];
  }

  const [devices, refresh] = withRefeshableData<BluetoothDevices>("Message.BluetoothDevices", "Command.GetBluetoothDevices");

  const controller = derived(
    devices,
    (message) => {
      if (message.status === Status.Loading) {
        return "checking";
      } else if (message.type === ResultType.Success) {
        return message.result.devices.find((d) => d.connected === true)?.mac ?? "No controller";
      }
    },
  );

  setInterval(refresh, 1000);
</script>

<Badge large>🎮 {$controller}</Badge>
