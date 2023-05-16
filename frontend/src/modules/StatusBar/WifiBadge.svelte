<script lang="ts">
  import { Badge } from "flowbite-svelte";
  import { onDestroy } from "svelte";
  import { derived } from "svelte/store";

  import { withRefeshableData } from "$lib/withRefeshableData";
  import { ResultType, Status } from "$lib/state";

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
        return "checking";
      } else if (message.type === ResultType.Success) {
        return `${message.result.ssid} ${message.result.signal} ${message.result.rate}`;
      }
    },
  );

  const intervalID = setInterval(refresh, 1000);

  onDestroy(() => clearInterval(intervalID));
</script>

<Badge large class="whitespace-nowrap">🛜 {$wifi}</Badge>
