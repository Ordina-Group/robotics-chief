<script lang="ts">
  import { Badge, Tooltip } from "flowbite-svelte";
  import { derived } from "svelte/store";

  import { connected, register } from "$lib/socket";

  interface RobotConnection {
    connected: boolean;
  }

  const robotConnection = register("Message.RobotConnection", { connected: undefined });
  // const robotConnection = derived(robotConnectionMessage, (message) => message.connected);

  const robotConnected = derived(
    [connected, robotConnection],
    ([connected, message]) => {
      console.log(connected, message);
      return message?.connected;
      // if (!connected || message?.status === Status.Loading) {
      //   return undefined;
      // } else if (message?.type === ResultType.Success) {
      //   return message.result.connected;
      // }
    },
  );
</script>

<Badge large class="whitespace-nowrap" id="robot-connection">
    {#if $robotConnected === true}
        🟢
    {:else if $robotConnected === undefined}
        🟡
    {:else}
        🔴
    {/if}
    Robot
</Badge>

<Tooltip placement="bottom" triggeredBy="#robot-connection">
    Connection to the robot.
</Tooltip>
