<script lang="ts">
  import { Badge, Tooltip } from "flowbite-svelte";
  import { derived } from "svelte/store";

  import { withRefeshableData } from "$lib/withRefeshableData";
  import { ResultType, Status } from "$lib/state";
  import { connected } from "$lib/socket";

  interface RobotConnection {
    connected: boolean;
  }

  const [robotConnection, refresh] = withRefeshableData<RobotConnection>("Message.RobotConnection", "Command.CheckRobotConnection");

  const robotConnected = derived(
    [connected, robotConnection],
    ([connected, message]) => {
      if (!connected || message.status === Status.Loading) {
        return undefined;
      } else if (message.type === ResultType.Success) {
        return message.result.connected;
      }
    },
  );

  setInterval(refresh, 1000);
</script>

<Badge large id="robot-connection">
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
