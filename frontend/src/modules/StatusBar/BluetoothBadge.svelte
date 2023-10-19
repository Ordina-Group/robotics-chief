<script lang="ts">
    import { Badge, Tooltip } from "flowbite-svelte";
  import { derived } from "svelte/store";

  import { withRefeshableData } from "$lib/withRefeshableData";
  import { ResultType, Status } from "$lib/state";

  import settings from "../Settings/Settings";
  import { onDestroy } from "svelte";

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

  const intervalID = setInterval(refresh, 2000);

  onDestroy(() => clearInterval(intervalID));
</script>

<Badge large color="dark" class="whitespace-nowrap" id="bluetooth-connection">
    🎮 {$controller}
</Badge>

<Tooltip placement="bottom" triggeredBy="#bluetooth-connection">
    Controller/bluetooth connection
</Tooltip>
