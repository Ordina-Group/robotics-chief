<script lang="ts">
  import { Badge } from "flowbite-svelte";
  import { derived } from "svelte/store";

  import { withRefeshableData } from "$lib/withRefeshableData";
  import { ResultType, Status } from "$lib/state";

  import settings from "../Settings/Settings";

  interface Device {
    mac: string;
    connected: boolean;
  }

  interface BluetoothDevices {
    devices: Device[];
  }

  const [devices, refresh] = withRefeshableData<BluetoothDevices>("Message.BluetoothDevices", "GetBluetoothDevices");

  const controller = derived(
    devices,
    (message) => {
      if (message.status === Status.Loading) {
        return "Checking";
      } else if (message.type === ResultType.Success) {
        const mac = message.result.devices.find((d) => d.connected === true)?.mac;

        if (mac) {
          return settings.get(`bluetooth.device.${mac}.name`) ?? mac;
        } else {
          return "No controller";
        }
      }
    },
  );

  // setInterval(refresh, 1000);
</script>

<Badge large color="dark" class="whitespace-nowrap">🎮 {$controller}</Badge>
