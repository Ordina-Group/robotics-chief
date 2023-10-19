<script lang="ts">
    import { Badge, Tooltip } from "flowbite-svelte";
  import { derived } from "svelte/store";

  import { withRefeshableData } from "$lib/withRefeshableData";
  import { ResultType, Status } from "$lib/state";
  import { onDestroy } from "svelte";

  interface WifiInfo {
    ssid: string;
    signal: string;
    rate: string;
    known: boolean;
  }

  const [wifiSignal, refresh] = withRefeshableData<WifiInfo>("Message.WifiInfo", "Command.GetWifiInfo");

  const wifi = derived(
    wifiSignal,
    (message) => {
      if (message.status === Status.Loading) {
        return "Checking";
      } else if (message.type === ResultType.Success) {
        return `${message.result.ssid} ${message.result.signal} ${message.result.rate}`;
      }
    },
  );

  const intervalID = setInterval(refresh, 2000);

  onDestroy(() => clearInterval(intervalID));
</script>

<Badge large color="dark" class="whitespace-nowrap" id="wifi-connection">
    🛜 {$wifi}
</Badge>

<Tooltip placement="bottom" triggeredBy="#wifi-connection">
    WiFi connection
</Tooltip>
