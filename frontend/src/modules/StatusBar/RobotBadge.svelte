<script lang="ts">
    import { Badge, Button, Dropdown, DropdownItem, Tooltip } from "flowbite-svelte";
    import { ChevronDownSolid, PlusSolid } from "flowbite-svelte-icons";
    import { derived } from "svelte/store";

    import { connected } from "$lib/socket";
    import { currentId, register, settings } from "$lib/robot";

    const robotConnection = register("Message.RobotConnection", { connected: undefined });

    $: {
        if ($currentId === undefined && $settings?.robots?.length > 0) {
            console.log("Set new ID", $currentId, $settings.robots[0].id);
            $currentId = $settings.robots[0].id;
        }
    }

    const robotConnected = derived(
        [connected, robotConnection],
        ([connected, message]) => {
            return message?.connected ?? connected;
        },
    );
</script>

<div class="flex">
    <Badge
        large
        id="robot-connection"
        color="dark"
        class="cursor-pointer whitespace-nowrap !rounded-r-none hover:bg-gray-200 focus:ring-4 focus:outline-none focus:ring-gray-100 dark:bg-gray-700 dark:hover:bg-gray-600 dark:focus:ring-gray-700 dark:text-white dark:border-gray-600"
        type="button"
    >
        {#if $robotConnected === true}
            🟢
        {:else if $robotConnected === undefined}
            🟡
        {:else}
            🔴
        {/if}
        Robot {$currentId}
        <ChevronDownSolid class="outline-none w-3 h-3 ml-2"/>
    </Badge>
    <Button class="!rounded-l-none">
        <PlusSolid class="outline-none w-3 h-3"/>
    </Button>
</div>
<Dropdown triggeredBy="#robot-connection">
    {#each $settings.robots as robot}
        <DropdownItem class="flex items-center" on:click={() => $currentId = robot.id}>
            Robot {robot.id}
        </DropdownItem>
    {/each}
</Dropdown>

<Tooltip placement="bottom" triggeredBy="#robot-connection">
    Connection to the robot.
</Tooltip>
