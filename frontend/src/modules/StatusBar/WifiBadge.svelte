<script lang="ts">
  import { Badge } from "flowbite-svelte";
  import { derived } from "svelte/store";

  import { withRefeshableData } from "$lib/withRefeshableData";
  import { ResultType, Status } from "$lib/state";

  interface WifiInfo {
    ssid: string;
    signal: string;
    rate: string;
  }

  const [wifiSignal, refresh] = withRefeshableData<WifiInfo>("Message.WifiInfo", "Command.GetWifiInfo");

  const wifi = derived(
    wifiSignal,
    (message) => {
      if (message.status === Status.Loading) {
        return "checking";
      } else if (message.type === ResultType.Success) {
        return `${message.result.ssid} ${message.result.signal} ${message.result.rate}`;
      }
    },
  );

  setInterval(refresh, 1000);
</script>

<Badge large class="whitespace-nowrap">🛜 {$wifi}</Badge>
