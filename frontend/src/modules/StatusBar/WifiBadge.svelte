<script lang="ts">
    import { Badge, Tooltip } from "flowbite-svelte";
    import { derived } from "svelte/store";


    import { register } from "$lib/robot";

    interface WifiInfo {
        ssid: string;
        signal: string;
        rate: string;
        known: boolean;
    }

    const wifiSignal = register<WifiInfo>("Message.WifiInfo");
    // const [wifiSignal, refresh] = withRefeshableData<WifiInfo>("Message.WifiInfo", "Command.GetWifiInfo");

    const wifi = derived(
        wifiSignal,
        (message) => {
            if (message === undefined) {
                return "Checking";
            }
            // } else if (message.type === ResultType.Success) {
            return `${message.ssid} ${message.signal} ${message.rate}`;
            // }
        },
    );

    // const intervalID = setInterval(refresh, 2000);
    // onDestroy(() => clearInterval(intervalID));
</script>

<Badge large color="dark" class="whitespace-nowrap" id="wifi-connection">
    🛜 {$wifi}
</Badge>

<Tooltip placement="bottom" triggeredBy="#wifi-connection">
    WiFi connection
</Tooltip>
